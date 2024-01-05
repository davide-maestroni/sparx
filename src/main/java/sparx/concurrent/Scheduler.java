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
import sparx.logging.Log;
import sparx.logging.alert.Alerts;
import sparx.logging.alert.BackpressureAlert;
import sparx.logging.alert.SchedulerQueueAlert;
import sparx.logging.alert.SchedulerWorkerAlert;
import sparx.util.Requires;
import sparx.util.UncheckedException;

public class Scheduler {

  private static final BackpressureAlert backpressureAlert = Alerts.backpressureAlert();
  private static final SchedulerQueueAlert queueAlert = Alerts.schedulerQueueAlert();
  private static final SchedulerWorkerAlert workerAlert = Alerts.schedulerWorkerAlert();
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

  private final ArrayDeque<Task> afterQueue = new ArrayDeque<Task>();
  private final ArrayDeque<Task> beforeQueue = new ArrayDeque<Task>();
  private final Executor executor;
  private final Object lock = new Object();
  private final int minThroughput;
  private final Runnable worker;

  private Task runningTask;
  private Thread runningThread;
  private int status = IDLE;

  public static @NotNull Scheduler of(@NotNull final Executor executor) {
    return of(executor, Integer.MAX_VALUE);
  }

  public static @NotNull Scheduler of(@NotNull final Executor executor,
      @NotNull final BackpressureStrategy backpressureStrategy) {
    return of(executor, Integer.MAX_VALUE, backpressureStrategy);
  }

  public static @NotNull Scheduler of(@NotNull final Executor executor, final int minThroughput) {
    return new Scheduler(executor, minThroughput);
  }

  public static @NotNull Scheduler of(@NotNull final Executor executor, final int minThroughput,
      @NotNull final BackpressureStrategy backpressureStrategy) {
    return new SchedulerWithBackpressure(executor, minThroughput, backpressureStrategy);
  }

  public static @NotNull Scheduler trampoline() {
    return of(synchronousExecutor);
  }

  public static @NotNull Scheduler trampoline(
      @NotNull final BackpressureStrategy backpressureStrategy) {
    return of(synchronousExecutor, backpressureStrategy);
  }

  private Scheduler(@NotNull final Executor executor, final int minThroughput) {
    this.executor = Requires.notNull(executor, "executor");
    this.minThroughput = Requires.positive(minThroughput, "minThroughput");
    if (minThroughput == Integer.MAX_VALUE) {
      // infinite throughput
      this.worker = new InfiniteWorker();
    } else if (minThroughput == 1) {
      this.worker = new SingleWorker();
    } else {
      this.worker = new ThroughputWorker();
    }
  }

  public boolean interruptTask(@NotNull final String taskID) {
    synchronized (lock) {
      final Task runningTask = this.runningTask;
      if (runningTask != null && runningTask.taskID().equals(taskID)) {
        try {
          runningThread.interrupt();
          return true;
        } catch (final SecurityException e) {
          Log.err(Scheduler.class, "Cannot interrupt running thread %s: %s", runningThread,
              Log.printable(e));
        }
      }
    }
    return false;
  }

  public int minThroughput() {
    return minThroughput;
  }

  public void pause() {
    synchronized (lock) {
      if (status == RUNNING) {
        status = PAUSING;
      } else if (status == IDLE) {
        status = PAUSED;
      }
    }
  }

  public int pendingCount() {
    synchronized (lock) {
      return beforeQueue.size() + afterQueue.size();
    }
  }

  public void resume() {
    final boolean needsExecution;
    synchronized (lock) {
      final boolean isPaused = status == PAUSED;
      needsExecution = isPaused && !(beforeQueue.isEmpty() && afterQueue.isEmpty());
      if (isPaused || (status == PAUSING)) {
        status = RUNNING;
      }
    }
    if (needsExecution) {
      executor.execute(worker);
    }
  }

  @SuppressWarnings("AssignmentUsedAsCondition")
  public void scheduleAfter(@NotNull Task task) {
    final boolean needsExecution;
    synchronized (lock) {
      task = applyBackpressure(task, lock, runningThread);
      final ArrayDeque<Task> afterQueue = this.afterQueue;
      afterQueue.offer(task);
      queueAlert.notifyPendingTasks(beforeQueue.size(), afterQueue.size());
      if (needsExecution = status == IDLE) {
        status = RUNNING;
      }
    }
    if (needsExecution) {
      executor.execute(worker);
    }
  }

  @SuppressWarnings("AssignmentUsedAsCondition")
  public void scheduleBefore(@NotNull final Task task) {
    final boolean needsExecution;
    synchronized (lock) {
      final ArrayDeque<Task> beforeQueue = this.beforeQueue;
      beforeQueue.offer(task);
      queueAlert.notifyPendingTasks(beforeQueue.size(), afterQueue.size());
      if (needsExecution = status == IDLE) {
        status = RUNNING;
      }
    }
    if (needsExecution) {
      executor.execute(worker);
    }
  }

  protected @NotNull Task applyBackpressure(@NotNull final Task task, @NotNull final Object lock,
      final Thread runningThread) {
    return task;
  }

  protected void releaseBackpressure(@NotNull final Object lock) {
  }

  public interface Task extends Runnable {

    @NotNull String taskID();

    int weight();
  }

  public interface BackpressureStrategy {

    boolean applyBackpressure(int pendingCount, int waitingCount, int minThroughput);

    long getDelayMillis(int pendingCount, int waitingCount, int minThroughput);

    boolean releaseBackpressure(int pendingCount, int waitingCount, int minThroughput);
  }

  private static class SchedulerWithBackpressure extends Scheduler {

    private final BackpressureStrategy backpressureStrategy;
    private final ArrayDeque<Task> waitingTasks = new ArrayDeque<Task>();

    private SchedulerWithBackpressure(@NotNull final Executor executor, final int minThroughput,
        @NotNull final BackpressureStrategy backpressureStrategy) {
      super(executor, minThroughput);
      this.backpressureStrategy = Requires.notNull(backpressureStrategy, "backpressureStrategy");
    }

    @Override
    protected @NotNull Task applyBackpressure(@NotNull final Task task,
        @NotNull final Object lock, final Thread runningThread) {
      final int pendingCount = pendingCount();
      final int throughput = minThroughput();
      final ArrayDeque<Task> waitingTasks = this.waitingTasks;
      final int waitingCount = waitingTasks.size();
      if (backpressureStrategy.applyBackpressure(pendingCount, waitingCount, throughput)) {
        final Thread currentThread = Thread.currentThread();
        if (!currentThread.equals(runningThread)) {
          final long delayMillis = backpressureStrategy.getDelayMillis(pendingCount, waitingCount,
              throughput);
          final BackpressureAlert backpressureAlert = Scheduler.backpressureAlert;
          backpressureAlert.notifyWaitStart(currentThread);
          waitingTasks.offer(task);
          try {
            lock.wait(delayMillis);
          } catch (final InterruptedException e) {
            throw UncheckedException.toUnchecked(e);
          } finally {
            backpressureAlert.notifyWaitStop(currentThread);
          }
        }
        return waitingTasks.pop();
      }
      return task;
    }

    @Override
    protected void releaseBackpressure(@NotNull final Object lock) {
      final ArrayDeque<Task> waitingTasks = this.waitingTasks;
      if (!waitingTasks.isEmpty() && backpressureStrategy.releaseBackpressure(pendingCount(),
          waitingTasks.size(), minThroughput())) {
        lock.notify();
      }
    }
  }

  private class InfiniteWorker implements Runnable {

    @Override
    public void run() {
      final SchedulerWorkerAlert workerAlert = Scheduler.workerAlert;
      final ArrayDeque<Task> beforeQueue = Scheduler.this.beforeQueue;
      final ArrayDeque<Task> afterQueue = Scheduler.this.afterQueue;
      final Thread currentThread = Thread.currentThread();
      workerAlert.notifyTaskStart(currentThread);
      while (true) {
        Task task;
        synchronized (lock) {
          runningTask = null;
          runningThread = null;
          if (status == PAUSING) {
            status = PAUSED;
            workerAlert.notifyTaskStop(currentThread);
            return;
          }

          task = beforeQueue.poll();
          if (task == null) {
            task = afterQueue.poll();
            if (task == null) {
              // move to IDLE
              status = IDLE;
              workerAlert.notifyTaskStop(currentThread);
              return;
            }
          }
          runningTask = task;
          runningThread = currentThread;
          releaseBackpressure(lock);
        }

        try {
          task.run();
        } catch (final Throwable t) {
          synchronized (lock) {
            runningTask = null;
            runningThread = null;
            workerAlert.notifyTaskStop(currentThread);
          }
          Log.err(Scheduler.class, "Uncaught exception: %s", Log.printable(t));
          throw UncheckedException.throwUnchecked(t);
        }
      }
    }
  }

  private class SingleWorker implements Runnable {

    @Override
    public void run() {
      final SchedulerWorkerAlert workerAlert = Scheduler.workerAlert;
      final Executor executor = Scheduler.this.executor;
      final ArrayDeque<Task> beforeQueue = Scheduler.this.beforeQueue;
      final ArrayDeque<Task> afterQueue = Scheduler.this.afterQueue;
      final Thread currentThread = Thread.currentThread();
      workerAlert.notifyTaskStart(currentThread);
      Task task;
      synchronized (lock) {
        if (status == PAUSING) {
          status = PAUSED;
          workerAlert.notifyTaskStop(currentThread);
          return;
        }

        task = beforeQueue.poll();
        if (task == null) {
          task = afterQueue.poll();
          if (task == null) {
            // move to IDLE
            status = IDLE;
            workerAlert.notifyTaskStop(currentThread);
            return;
          }
        }
        runningTask = task;
        runningThread = currentThread;
        releaseBackpressure(lock);
      }

      boolean hasNext = false;
      try {
        task.run();
      } catch (final Throwable t) {
        Log.err(Scheduler.class, "Uncaught exception: %s", Log.printable(t));
        throw UncheckedException.throwUnchecked(t);
      } finally {
        synchronized (lock) {
          runningTask = null;
          runningThread = null;
          workerAlert.notifyTaskStop(currentThread);
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

  private class ThroughputWorker implements Runnable {

    @Override
    public void run() {
      final SchedulerWorkerAlert workerAlert = Scheduler.workerAlert;
      final ArrayDeque<Task> beforeQueue = Scheduler.this.beforeQueue;
      final ArrayDeque<Task> afterQueue = Scheduler.this.afterQueue;
      final Thread currentThread = Thread.currentThread();
      workerAlert.notifyTaskStart(currentThread);
      int minThroughput = Scheduler.this.minThroughput;
      while (minThroughput > 0) {
        Task task;
        synchronized (lock) {
          runningTask = null;
          runningThread = null;
          if (status == PAUSING) {
            status = PAUSED;
            workerAlert.notifyTaskStop(currentThread);
            return;
          }

          task = beforeQueue.poll();
          if (task == null) {
            task = afterQueue.poll();
            if (task == null) {
              // move to IDLE
              status = IDLE;
              workerAlert.notifyTaskStop(currentThread);
              return;
            }
          }
          runningTask = task;
          runningThread = currentThread;
          releaseBackpressure(lock);
        }

        try {
          minThroughput -= Math.max(task.weight(), 1);
          task.run();
        } catch (final Throwable t) {
          synchronized (lock) {
            runningTask = null;
            runningThread = null;
            workerAlert.notifyTaskStop(currentThread);
          }
          Log.err(Scheduler.class, "Uncaught exception: %s", Log.printable(t));
          throw UncheckedException.throwUnchecked(t);
        }
      }

      boolean hasNext = false;
      synchronized (lock) {
        workerAlert.notifyTaskStop(currentThread);
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
