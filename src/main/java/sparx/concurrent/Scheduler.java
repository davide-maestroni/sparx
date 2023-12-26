/*
 * Copyright 2024 Davide Maestroni
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sparx.concurrent;

import java.util.ArrayDeque;
import java.util.concurrent.Executor;
import org.jetbrains.annotations.NotNull;
import sparx.logging.Alerts;
import sparx.logging.Log;
import sparx.logging.SchedulerAlert;
import sparx.util.Requires;

public class Scheduler {

  private static final SchedulerAlert alert = Alerts.schedulerAlert();
  private static final Executor synchronousExecutor = new Executor() {
    @Override
    public void execute(@NotNull Runnable command) {
      command.run();
    }
  };

  private static final int IDLE = 0;
  private static final int RUNNING = 1;
  private static final int PAUSING = 2;
  private static final int PAUSED = 3;

  private final Executor executor;
  private final ArrayDeque<Task> beforeQueue = new ArrayDeque<Task>();
  private final ArrayDeque<Task> afterQueue = new ArrayDeque<Task>();
  private final int minThroughput;
  private final Object mutex = new Object();
  private final Runnable runner;

  private Task runningTask;
  private Thread runningThread;
  private int status = IDLE;

  public static @NotNull Scheduler of(@NotNull final Executor executor) {
    return of(executor, Integer.MAX_VALUE);
  }

  public static @NotNull Scheduler of(@NotNull final Executor executor, final int minThroughput) {
    return new Scheduler(executor, minThroughput);
  }

  public static @NotNull Scheduler trampoline() {
    return of(synchronousExecutor);
  }

  public static @NotNull Scheduler withBackpressure(@NotNull final Executor executor,
      final int taskThreshold) {
    return withBackpressure(executor, Integer.MAX_VALUE, taskThreshold);
  }

  public static @NotNull Scheduler withBackpressure(@NotNull final Executor executor,
      final int minThroughput, final int taskThreshold) {
    return new SchedulerWithBackpressure(executor, minThroughput, taskThreshold);
  }

  private Scheduler(@NotNull final Executor executor, final int minThroughput) {
    this.executor = Requires.notNull(executor, "executor");
    this.minThroughput = Requires.positive(minThroughput, "minThroughput");
    if (minThroughput == Integer.MAX_VALUE) {
      // infinite throughput
      this.runner = new InfiniteRunner();
    } else if (minThroughput == 1) {
      this.runner = new SingleRunner();
    } else {
      this.runner = new ThroughputRunner();
    }
  }

  public boolean interruptTask(@NotNull final String taskID) {
    synchronized (mutex) {
      final Task runningTask = this.runningTask;
      if (runningTask != null && runningTask.taskID().equals(taskID)) {
        runningThread.interrupt();
        return true;
      }
    }
    return false;
  }

  public int minThroughput() {
    return minThroughput;
  }

  public void pause() {
    synchronized (mutex) {
      if (status == RUNNING) {
        status = PAUSING;
      } else if (status == IDLE) {
        status = PAUSED;
      }
    }
  }

  public int pendingCount() {
    synchronized (mutex) {
      return beforeQueue.size() + afterQueue.size();
    }
  }

  public void resume() {
    final boolean needsExecution;
    synchronized (mutex) {
      final boolean isPaused = status == PAUSED;
      needsExecution = isPaused && !(beforeQueue.isEmpty() && afterQueue.isEmpty());
      if (isPaused || (status == PAUSING)) {
        status = RUNNING;
      }
    }
    if (needsExecution) {
      executor.execute(runner);
    }
  }

  @SuppressWarnings("AssignmentUsedAsCondition")
  public void scheduleAfter(@NotNull final Task task) {
    final boolean needsExecution;
    synchronized (mutex) {
      applyBackpressure(mutex, runningThread);
      final ArrayDeque<Task> afterQueue = this.afterQueue;
      afterQueue.offer(task);
      alert.notifyPendingTasks(beforeQueue.size(), afterQueue.size());
      if (needsExecution = status == IDLE) {
        status = RUNNING;
      }
    }
    if (needsExecution) {
      executor.execute(runner);
    }
  }

  @SuppressWarnings("AssignmentUsedAsCondition")
  public void scheduleBefore(@NotNull final Task task) {
    final boolean needsExecution;
    synchronized (mutex) {
      applyBackpressure(mutex, runningThread);
      final ArrayDeque<Task> beforeQueue = this.beforeQueue;
      beforeQueue.offer(task);
      alert.notifyPendingTasks(beforeQueue.size(), afterQueue.size());
      if (needsExecution = status == IDLE) {
        status = RUNNING;
      }
    }
    if (needsExecution) {
      executor.execute(runner);
    }
  }

  protected void applyBackpressure(@NotNull final Object mutex, final Thread runningThread) {
  }

  protected void releaseBackpressure(@NotNull final Object mutex) {
  }

  public interface Task extends Runnable {

    @NotNull String taskID();

    int weight();
  }

  private static class SchedulerWithBackpressure extends Scheduler {

    private final int taskThreshold;

    private SchedulerWithBackpressure(@NotNull final Executor executor, final int minThroughput,
        final int taskThreshold) {
      super(executor, minThroughput);
      this.taskThreshold = Requires.positive(taskThreshold, "taskThreshold");
    }

    @Override
    protected void applyBackpressure(@NotNull final Object mutex, final Thread runningThread) {
      if (!Thread.currentThread().equals(runningThread)) {
        while (pendingCount() > taskThreshold) {
          try {
            mutex.wait();
          } catch (final InterruptedException e) {
            throw new UncheckedInterruptedException(e);
          }
        }
      }
    }

    @Override
    protected void releaseBackpressure(@NotNull final Object mutex) {
      if (pendingCount() <= taskThreshold) {
        mutex.notifyAll();
      }
    }
  }

  private class InfiniteRunner implements Runnable {

    @Override
    public void run() {
      final SchedulerAlert alert = Scheduler.alert;
      final ArrayDeque<Task> beforeQueue = Scheduler.this.beforeQueue;
      final ArrayDeque<Task> afterQueue = Scheduler.this.afterQueue;
      final Thread currentThread = Thread.currentThread();
      alert.notifyTaskStart(currentThread);
      while (true) {
        Task task;
        synchronized (mutex) {
          runningTask = null;
          runningThread = null;
          if (status == PAUSING) {
            status = PAUSED;
            alert.notifyTaskStop(currentThread);
            return;
          }

          task = beforeQueue.poll();
          if (task == null) {
            task = afterQueue.poll();
            if (task == null) {
              // move to IDLE
              status = IDLE;
              alert.notifyTaskStop(currentThread);
              return;
            }
          }
          runningTask = task;
          runningThread = currentThread;
          releaseBackpressure(mutex);
        }

        try {
          task.run();
        } catch (final Throwable t) {
          synchronized (mutex) {
            runningTask = null;
            runningThread = null;
            alert.notifyTaskStop(currentThread);
          }
          Log.err(Scheduler.class, "Uncaught exception: %s", Log.printable(t));
          UncheckedException.throwUnchecked(t);
        }
      }
    }
  }

  private class SingleRunner implements Runnable {

    @Override
    public void run() {
      final SchedulerAlert alert = Scheduler.alert;
      final Executor executor = Scheduler.this.executor;
      final ArrayDeque<Task> beforeQueue = Scheduler.this.beforeQueue;
      final ArrayDeque<Task> afterQueue = Scheduler.this.afterQueue;
      final Thread currentThread = Thread.currentThread();
      alert.notifyTaskStart(currentThread);
      Task task;
      synchronized (mutex) {
        if (status == PAUSING) {
          status = PAUSED;
          alert.notifyTaskStop(currentThread);
          return;
        }

        task = beforeQueue.poll();
        if (task == null) {
          task = afterQueue.poll();
          if (task == null) {
            // move to IDLE
            status = IDLE;
            alert.notifyTaskStop(currentThread);
            return;
          }
        }
        runningTask = task;
        runningThread = currentThread;
        releaseBackpressure(mutex);
      }

      boolean hasNext = false;
      try {
        task.run();
      } catch (final Throwable t) {
        Log.err(Scheduler.class, "Uncaught exception: %s", Log.printable(t));
        UncheckedException.throwUnchecked(t);
      } finally {
        synchronized (mutex) {
          runningTask = null;
          runningThread = null;
          alert.notifyTaskStop(currentThread);
          if (!beforeQueue.isEmpty() || !afterQueue.isEmpty()) {
            hasNext = true;
            status = IDLE;
          }
        }
      }
      if (hasNext) {
        executor.execute(this);
      }
    }
  }

  private class ThroughputRunner implements Runnable {

    @Override
    public void run() {
      final SchedulerAlert alert = Scheduler.alert;
      final ArrayDeque<Task> beforeQueue = Scheduler.this.beforeQueue;
      final ArrayDeque<Task> afterQueue = Scheduler.this.afterQueue;
      final Thread currentThread = Thread.currentThread();
      alert.notifyTaskStart(currentThread);
      int minThroughput = Scheduler.this.minThroughput;
      while (minThroughput > 0) {
        Task task;
        synchronized (mutex) {
          runningTask = null;
          runningThread = null;
          if (status == PAUSING) {
            status = PAUSED;
            alert.notifyTaskStop(currentThread);
            return;
          }

          task = beforeQueue.poll();
          if (task == null) {
            task = afterQueue.poll();
            if (task == null) {
              // move to IDLE
              status = IDLE;
              alert.notifyTaskStop(currentThread);
              return;
            }
          }
          runningTask = task;
          runningThread = currentThread;
          releaseBackpressure(mutex);
        }

        try {
          minThroughput -= Math.max(task.weight(), 1);
          task.run();
        } catch (final Throwable t) {
          synchronized (mutex) {
            runningTask = null;
            runningThread = null;
            alert.notifyTaskStop(currentThread);
          }
          Log.err(Scheduler.class, "Uncaught exception: %s", Log.printable(t));
          UncheckedException.throwUnchecked(t);
        }
      }

      boolean hasNext = false;
      synchronized (mutex) {
        alert.notifyTaskStop(currentThread);
        if (status == PAUSING) {
          status = PAUSED;
          return;
        }

        if (!beforeQueue.isEmpty() || !afterQueue.isEmpty()) {
          hasNext = true;
          status = IDLE;
        }
      }
      if (hasNext) {
        executor.execute(this);
      }
    }
  }
}
