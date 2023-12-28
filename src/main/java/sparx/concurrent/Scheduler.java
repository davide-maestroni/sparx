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
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import sparx.logging.Log;
import sparx.logging.alert.Alerts;
import sparx.logging.alert.BackpressureAlert;
import sparx.logging.alert.SchedulerQueueAlert;
import sparx.logging.alert.SchedulerWorkerAlert;
import sparx.util.Requires;

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

  private final Executor executor;
  private final ArrayDeque<Task> beforeQueue = new ArrayDeque<Task>();
  private final ArrayDeque<Task> afterQueue = new ArrayDeque<Task>();
  private final int minThroughput;
  private final Object mutex = new Object();
  private final Runnable worker;

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
    synchronized (mutex) {
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
      executor.execute(worker);
    }
  }

  @SuppressWarnings("AssignmentUsedAsCondition")
  public void scheduleAfter(@NotNull final Task task) {
    final boolean needsExecution;
    synchronized (mutex) {
      applyBackpressure(mutex, runningThread);
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
    synchronized (mutex) {
      applyBackpressure(mutex, runningThread);
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

  // TODO: withBackpressure(List<Triple>)???

  public @NotNull Scheduler withBackpressure(final int taskThreshold) {
    Requires.positive(taskThreshold, "taskThreshold");
    return new Scheduler(executor, minThroughput) {
      @Override
      protected void applyBackpressure(@NotNull final Object mutex, final Thread runningThread) {
        final Thread currentThread = Thread.currentThread();
        if (!currentThread.equals(runningThread)) {
          final BackpressureAlert backpressureAlert = Scheduler.backpressureAlert;
          while (pendingCount() >= taskThreshold) {
            backpressureAlert.notifyWaitStart(currentThread);
            try {
              mutex.wait();
            } catch (final InterruptedException e) {
              throw new UncheckedInterruptedException(e);
            } finally {
              backpressureAlert.notifyWaitStop(currentThread);
            }
          }
        }
      }

      @Override
      protected void releaseBackpressure(@NotNull final Object mutex) {
        if (pendingCount() < taskThreshold) {
          mutex.notify();
        }
      }
    };
  }

  public @NotNull Scheduler withBackpressure(final int taskThreshold, final long delay,
      @NotNull final TimeUnit unit) {
    Requires.positive(taskThreshold, "taskThreshold");
    final long delayMillis = unit.toMillis(delay);
    return new Scheduler(executor, minThroughput) {
      @Override
      protected void applyBackpressure(@NotNull final Object mutex, final Thread runningThread) {
        final Thread currentThread = Thread.currentThread();
        if (!currentThread.equals(runningThread)) {
          if (pendingCount() >= taskThreshold) {
            final BackpressureAlert backpressureAlert = Scheduler.backpressureAlert;
            backpressureAlert.notifyWaitStart(currentThread);
            try {
              mutex.wait(delayMillis);
            } catch (final InterruptedException e) {
              throw new UncheckedInterruptedException(e);
            } finally {
              backpressureAlert.notifyWaitStop(currentThread);
            }
          }
        }
      }

      @Override
      protected void releaseBackpressure(@NotNull final Object mutex) {
        if (pendingCount() < taskThreshold) {
          mutex.notify();
        }
      }
    };
  }

  public @NotNull Scheduler withRejection(final int taskThreshold) {
    Requires.positive(taskThreshold, "taskThreshold");
    return new Scheduler(executor, minThroughput) {
      @Override
      protected void applyBackpressure(@NotNull final Object mutex, final Thread runningThread) {
        final Thread currentThread = Thread.currentThread();
        if (!currentThread.equals(runningThread) && (pendingCount() >= taskThreshold)) {
          throw new RejectedExecutionException();
        }
      }

      @Override
      protected void releaseBackpressure(@NotNull final Object mutex) {
      }
    };
  }

  public @NotNull Scheduler withRejection(final int taskThreshold, final long delay,
      @NotNull final TimeUnit unit) {
    Requires.positive(taskThreshold, "taskThreshold");
    final long delayMillis = unit.toMillis(delay);
    return new Scheduler(executor, minThroughput) {
      @Override
      protected void applyBackpressure(@NotNull final Object mutex, final Thread runningThread) {
        final Thread currentThread = Thread.currentThread();
        if (!currentThread.equals(runningThread)) {
          if (pendingCount() >= taskThreshold) {
            final BackpressureAlert backpressureAlert = Scheduler.backpressureAlert;
            backpressureAlert.notifyWaitStart(currentThread);
            try {
              mutex.wait(delayMillis);
            } catch (final InterruptedException e) {
              throw new UncheckedInterruptedException(e);
            } finally {
              backpressureAlert.notifyWaitStop(currentThread);
            }
            if (pendingCount() >= taskThreshold) {
              throw new RejectedExecutionException();
            }
          }
        }
      }

      @Override
      protected void releaseBackpressure(@NotNull final Object mutex) {
        if (pendingCount() < taskThreshold) {
          mutex.notify();
        }
      }
    };
  }

  protected void applyBackpressure(@NotNull final Object mutex, final Thread runningThread) {
  }

  protected void releaseBackpressure(@NotNull final Object mutex) {
  }

  public interface Task extends Runnable {

    @NotNull String taskID();

    int weight();
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
        synchronized (mutex) {
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
          releaseBackpressure(mutex);
        }

        try {
          task.run();
        } catch (final Throwable t) {
          synchronized (mutex) {
            runningTask = null;
            runningThread = null;
            workerAlert.notifyTaskStop(currentThread);
          }
          Log.err(Scheduler.class, "Uncaught exception: %s", Log.printable(t));
          UncheckedException.throwUnchecked(t);
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
      synchronized (mutex) {
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
        synchronized (mutex) {
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
          releaseBackpressure(mutex);
        }

        try {
          minThroughput -= Math.max(task.weight(), 1);
          task.run();
        } catch (final Throwable t) {
          synchronized (mutex) {
            runningTask = null;
            runningThread = null;
            workerAlert.notifyTaskStop(currentThread);
          }
          Log.err(Scheduler.class, "Uncaught exception: %s", Log.printable(t));
          UncheckedException.throwUnchecked(t);
        }
      }

      boolean hasNext = false;
      synchronized (mutex) {
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
