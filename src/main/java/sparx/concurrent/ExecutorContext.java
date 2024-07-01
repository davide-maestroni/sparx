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
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sparx.util.Require;
import sparx.util.UncheckedException;

public class ExecutorContext implements ExecutionContext {

  private static final Alert<?> DISABLED = new Alert<Object>() {
    @Override
    public void disable() {
    }

    @Override
    public void notify(final int state, final Object payload) {
    }
  };
  private static final Logger LOGGER = Logger.getLogger(ExecutorContext.class.getName());
  private static final Executor SYNCHRONOUS_EXECUTOR = new Executor() {
    @Override
    public void execute(@NotNull final Runnable command) {
      command.run();
    }
  };

  private static final int IDLE = 0;
  private static final int RUNNING = 1;
  private static final int PAUSING = 2;
  private static final int PAUSED = 3;

  @SuppressWarnings("unchecked")
  private static volatile Alert<Thread> backpressureAlert = (Alert<Thread>) DISABLED;
  @SuppressWarnings("unchecked")
  private static volatile Alert<Integer> queueAlert = (Alert<Integer>) DISABLED;
  @SuppressWarnings("unchecked")
  private static volatile Alert<Thread> workerAlert = (Alert<Thread>) DISABLED;

  private final ArrayDeque<Task> afterQueue = new ArrayDeque<Task>();
  private final ArrayDeque<Task> beforeQueue = new ArrayDeque<Task>();
  private final Executor executor;
  private final Object lock = new Object();
  private final int minThroughput;
  private final Worker worker;

  private Task runningTask;
  private Thread runningThread;
  private int status = IDLE;

  private ExecutorContext(@NotNull final Executor executor, final int minThroughput) {
    this.executor = executor;
    this.minThroughput = minThroughput;
    if (minThroughput == Integer.MAX_VALUE) {
      // infinite throughput
      this.worker = new InfiniteWorker();
    } else if (minThroughput == 1) {
      this.worker = new SingleWorker();
    } else {
      this.worker = new ThroughputWorker();
    }
  }

  private ExecutorContext(@NotNull final Executor executor, final long minTime,
      @NotNull final TimeUnit unit) {
    this.executor = executor;
    this.minThroughput = Integer.MAX_VALUE;
    this.worker = new TimeoutWorker(minTime, unit);
  }

  public static void alertBackpressureDelay(final long interval,
      @NotNull final TimeUnit intervalUnit, final long timeout,
      @NotNull final TimeUnit timeoutUnit) {
    backpressureAlert.disable();
    if (interval > 0 && timeout > 0) {
      backpressureAlert = new BackpressureAlert(LOGGER, interval, intervalUnit, timeout,
          timeoutUnit);
    }
  }

  public static void alertPendingTasks(final int maxQueueSize) {
    queueAlert.disable();
    if (maxQueueSize >= 0) {
      queueAlert = new QueueAlert(LOGGER, maxQueueSize);
    }
  }

  public static void alertProcessingTime(final long interval, @NotNull final TimeUnit intervalUnit,
      final long timeout, @NotNull final TimeUnit timeoutUnit) {
    workerAlert.disable();
    if (interval > 0 && timeout > 0) {
      workerAlert = new WorkerAlert(LOGGER, interval, intervalUnit, timeout, timeoutUnit);
    }
  }

  public static @NotNull ExecutorContext of(@NotNull final Executor executor) {
    return of(executor, Integer.MAX_VALUE);
  }

  public static @NotNull ExecutorContext of(@NotNull final Executor executor,
      @NotNull final BackpressureStrategy backpressureStrategy) {
    return of(executor, Integer.MAX_VALUE, backpressureStrategy);
  }

  public static @NotNull ExecutorContext of(@NotNull final Executor executor,
      final int minThroughput) {
    return new ExecutorContext(Require.notNull(executor, "executor"),
        Require.positive(minThroughput, "minThroughput"));
  }

  public static @NotNull ExecutorContext of(@NotNull final Executor executor,
      final int minThroughput, @NotNull final BackpressureStrategy backpressureStrategy) {
    return new SchedulerWithBackpressure(Require.notNull(executor, "executor"),
        Require.positive(minThroughput, "minThroughput"),
        Require.notNull(backpressureStrategy, "backpressureStrategy"));
  }

  public static @NotNull ExecutorContext of(@NotNull final Executor executor, final long minTime,
      @NotNull final TimeUnit unit) {
    return new ExecutorContext(Require.notNull(executor, "executor"),
        Require.positive(minTime, "minTime"), Require.notNull(unit, "unit"));
  }

  public static @NotNull ExecutorContext of(@NotNull final Executor executor, final long minTime,
      @NotNull final TimeUnit unit, @NotNull final BackpressureStrategy backpressureStrategy) {
    return new SchedulerWithBackpressure(Require.notNull(executor, "executor"),
        Require.positive(minTime, "minTime"), Require.notNull(unit, "unit"),
        Require.notNull(backpressureStrategy, "backpressureStrategy"));
  }

  public static @NotNull ExecutorContext trampoline() {
    return of(SYNCHRONOUS_EXECUTOR);
  }

  public static @NotNull ExecutorContext trampoline(
      @NotNull final BackpressureStrategy backpressureStrategy) {
    return of(SYNCHRONOUS_EXECUTOR, backpressureStrategy);
  }

  @Override
  public @NotNull ExecutionContext fork() {
    return worker.fork();
  }

  @Override
  public @Nullable String currentTaskID() {
    synchronized (lock) {
      final Task runningTask = this.runningTask;
      if (runningTask != null) {
        return runningTask.taskID();
      }
    }
    return null;
  }

  @Override
  public boolean interruptTask(@NotNull final String taskID) {
    synchronized (lock) {
      final Task runningTask = this.runningTask;
      if (runningTask != null && runningTask.taskID().equals(taskID)) {
        try {
          runningThread.interrupt();
          return true;
        } catch (final SecurityException e) {
          LOGGER.log(Level.SEVERE, "Cannot interrupt running thread " + runningThread, e);
        }
      }
    }
    return false;
  }

  @Override
  public boolean isCurrent() {
    return Thread.currentThread().equals(runningThread);
  }

  @Override
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

  @Override
  public int pendingTasks() {
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

  @Override
  @SuppressWarnings("AssignmentUsedAsCondition")
  public void scheduleAfter(@NotNull Task task) {
    final boolean needsExecution;
    synchronized (lock) {
      task = applyBackpressure(task, lock, runningThread);
      final ArrayDeque<Task> afterQueue = this.afterQueue;
      afterQueue.offer(task);
      queueAlert.notify(afterQueue.size(), beforeQueue.size());
      if (needsExecution = status == IDLE) {
        status = RUNNING;
      }
    }
    if (needsExecution) {
      executor.execute(worker);
    }
  }

  @Override
  @SuppressWarnings("AssignmentUsedAsCondition")
  public void scheduleBefore(@NotNull final Task task) {
    final boolean needsExecution;
    synchronized (lock) {
      final ArrayDeque<Task> beforeQueue = this.beforeQueue;
      beforeQueue.offer(task);
      queueAlert.notify(afterQueue.size(), beforeQueue.size());
      if (needsExecution = status == IDLE) {
        status = RUNNING;
      }
    }
    if (needsExecution) {
      executor.execute(worker);
    }
  }

  protected @NotNull Task applyBackpressure(@NotNull final Task task, @NotNull final Object lock,
      @NotNull final Thread runningThread) {
    return task;
  }

  protected void releaseBackpressure(@NotNull final Object lock) {
  }

  private interface Worker extends Runnable {

    @NotNull
    ExecutorContext fork();
  }

  private static class SchedulerWithBackpressure extends ExecutorContext {

    private final BackpressureStrategy backpressureStrategy;
    private final ArrayDeque<Task> waitingTasks = new ArrayDeque<Task>();

    private SchedulerWithBackpressure(@NotNull final Executor executor, final int minThroughput,
        @NotNull final BackpressureStrategy backpressureStrategy) {
      super(executor, minThroughput);
      this.backpressureStrategy = backpressureStrategy;
    }

    private SchedulerWithBackpressure(@NotNull final Executor executor, final long minTime,
        @NotNull final TimeUnit unit, @NotNull final BackpressureStrategy backpressureStrategy) {
      super(executor, minTime, unit);
      this.backpressureStrategy = backpressureStrategy;
    }

    @Override
    protected @NotNull Task applyBackpressure(@NotNull final Task task, @NotNull final Object lock,
        @NotNull final Thread runningThread) {
      final int pendingCount = pendingTasks();
      final int throughput = minThroughput();
      final ArrayDeque<Task> waitingTasks = this.waitingTasks;
      final int waitingCount = waitingTasks.size();
      final BackpressureStrategy backpressureStrategy = this.backpressureStrategy;
      if (backpressureStrategy.applyBackpressure(pendingCount, waitingCount, throughput)) {
        final Thread currentThread = Thread.currentThread();
        if (!currentThread.equals(runningThread)) {
          final long delayMillis = backpressureStrategy.getDelayMillis(pendingCount, waitingCount,
              throughput);
          final Alert<Thread> backpressureAlert = ExecutorContext.backpressureAlert;
          backpressureAlert.notify(BackpressureAlert.WAIT_START, currentThread);
          waitingTasks.offer(task);
          try {
            lock.wait(delayMillis);
          } catch (final InterruptedException e) {
            throw UncheckedException.toUnchecked(e);
          } finally {
            backpressureAlert.notify(BackpressureAlert.WAIT_STOP, currentThread);
          }
        }
        return waitingTasks.pop();
      }
      return task;
    }

    @Override
    protected void releaseBackpressure(@NotNull final Object lock) {
      final ArrayDeque<Task> waitingTasks = this.waitingTasks;
      if (!waitingTasks.isEmpty() && backpressureStrategy.releaseBackpressure(pendingTasks(),
          waitingTasks.size(), minThroughput())) {
        lock.notify();
      }
    }
  }

  private class InfiniteWorker implements Worker {

    @Override
    public @NotNull ExecutorContext fork() {
      return new ExecutorContext(executor, minThroughput);
    }

    @Override
    public void run() {
      final Alert<Thread> workerAlert = ExecutorContext.workerAlert;
      final ArrayDeque<Task> beforeQueue = ExecutorContext.this.beforeQueue;
      final ArrayDeque<Task> afterQueue = ExecutorContext.this.afterQueue;
      final Thread currentThread = Thread.currentThread();
      workerAlert.notify(WorkerAlert.WAIT_START, currentThread);
      while (true) {
        Task task;
        synchronized (lock) {
          runningTask = null;
          runningThread = null;
          if (status == PAUSING) {
            status = PAUSED;
            workerAlert.notify(WorkerAlert.WAIT_STOP, currentThread);
            return;
          }

          task = beforeQueue.poll();
          if (task == null) {
            task = afterQueue.poll();
            if (task == null) {
              // move to IDLE
              status = IDLE;
              workerAlert.notify(WorkerAlert.WAIT_STOP, currentThread);
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
          boolean hasNext = false;
          synchronized (lock) {
            runningTask = null;
            runningThread = null;
            workerAlert.notify(WorkerAlert.WAIT_STOP, currentThread);
            if (status == PAUSING) {
              status = PAUSED;
            } else {
              hasNext = !beforeQueue.isEmpty() || !afterQueue.isEmpty();
            }
          }
          if (hasNext) {
            executor.execute(this);
          }
          LOGGER.log(Level.SEVERE, "Uncaught exception", t);
          throw UncheckedException.throwUnchecked(t);
        }
      }
    }
  }

  private class SingleWorker implements Worker {

    @Override
    public @NotNull ExecutorContext fork() {
      return new ExecutorContext(executor, minThroughput);
    }

    @Override
    public void run() {
      final Alert<Thread> workerAlert = ExecutorContext.workerAlert;
      final Executor executor = ExecutorContext.this.executor;
      final ArrayDeque<Task> beforeQueue = ExecutorContext.this.beforeQueue;
      final ArrayDeque<Task> afterQueue = ExecutorContext.this.afterQueue;
      final Thread currentThread = Thread.currentThread();
      Task task;
      synchronized (lock) {
        if (status == PAUSING) {
          status = PAUSED;
          workerAlert.notify(WorkerAlert.WAIT_STOP, currentThread);
          return;
        }

        task = beforeQueue.poll();
        if (task == null) {
          task = afterQueue.poll();
          if (task == null) {
            // move to IDLE
            status = IDLE;
            workerAlert.notify(WorkerAlert.WAIT_STOP, currentThread);
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
        LOGGER.log(Level.SEVERE, "Uncaught exception", t);
        throw UncheckedException.throwUnchecked(t);
      } finally {
        boolean hasNext = false;
        synchronized (lock) {
          runningTask = null;
          runningThread = null;
          workerAlert.notify(WorkerAlert.WAIT_STOP, currentThread);
          if (status == PAUSING) {
            status = PAUSED;
          } else if (beforeQueue.isEmpty() && afterQueue.isEmpty()) {
            // move to IDLE
            status = IDLE;
          } else {
            hasNext = true;
          }
        }
        if (hasNext) {
          executor.execute(this);
        }
      }
    }
  }

  private class ThroughputWorker implements Worker {

    @Override
    public @NotNull ExecutorContext fork() {
      return new ExecutorContext(executor, minThroughput);
    }

    @Override
    public void run() {
      final Alert<Thread> workerAlert = ExecutorContext.workerAlert;
      final ArrayDeque<Task> beforeQueue = ExecutorContext.this.beforeQueue;
      final ArrayDeque<Task> afterQueue = ExecutorContext.this.afterQueue;
      final Thread currentThread = Thread.currentThread();
      int minThroughput = ExecutorContext.this.minThroughput;
      while (minThroughput > 0) {
        Task task;
        synchronized (lock) {
          runningTask = null;
          runningThread = null;
          if (status == PAUSING) {
            status = PAUSED;
            workerAlert.notify(WorkerAlert.WAIT_STOP, currentThread);
            return;
          }

          task = beforeQueue.poll();
          if (task == null) {
            task = afterQueue.poll();
            if (task == null) {
              // move to IDLE
              status = IDLE;
              workerAlert.notify(WorkerAlert.WAIT_STOP, currentThread);
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
          boolean hasNext = false;
          synchronized (lock) {
            runningTask = null;
            runningThread = null;
            workerAlert.notify(WorkerAlert.WAIT_STOP, currentThread);
            if (status == PAUSING) {
              status = PAUSED;
            } else {
              hasNext = !beforeQueue.isEmpty() || !afterQueue.isEmpty();
            }
          }
          if (hasNext) {
            executor.execute(this);
          }
          LOGGER.log(Level.SEVERE, "Uncaught exception", t);
          throw UncheckedException.throwUnchecked(t);
        }
      }

      synchronized (lock) {
        runningTask = null;
        runningThread = null;
        workerAlert.notify(WorkerAlert.WAIT_STOP, currentThread);
        if (status == PAUSING) {
          status = PAUSED;
          return;
        }
        if (beforeQueue.isEmpty() && afterQueue.isEmpty()) {
          // move to IDLE
          status = IDLE;
          return;
        }
      }
      executor.execute(this);
    }
  }

  private class TimeoutWorker implements Worker {

    private final long minTimeMillis;

    public TimeoutWorker(final long minTime, @NotNull final TimeUnit unit) {
      this.minTimeMillis = unit.toMillis(minTime);
    }

    @Override
    public @NotNull ExecutorContext fork() {
      return new ExecutorContext(executor, minTimeMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
      final Alert<Thread> workerAlert = ExecutorContext.workerAlert;
      final ArrayDeque<Task> beforeQueue = ExecutorContext.this.beforeQueue;
      final ArrayDeque<Task> afterQueue = ExecutorContext.this.afterQueue;
      final Thread currentThread = Thread.currentThread();
      workerAlert.notify(WorkerAlert.WAIT_START, currentThread);
      long minTimeMillis = this.minTimeMillis;
      while (minTimeMillis > 0) {
        Task task;
        synchronized (lock) {
          runningTask = null;
          runningThread = null;
          if (status == PAUSING) {
            status = PAUSED;
            workerAlert.notify(WorkerAlert.WAIT_STOP, currentThread);
            return;
          }

          task = beforeQueue.poll();
          if (task == null) {
            task = afterQueue.poll();
            if (task == null) {
              // move to IDLE
              status = IDLE;
              workerAlert.notify(WorkerAlert.WAIT_STOP, currentThread);
              return;
            }
          }
          runningTask = task;
          runningThread = currentThread;
          releaseBackpressure(lock);
        }

        final long startTimeMillis = System.currentTimeMillis();
        try {
          task.run();
          minTimeMillis -= System.currentTimeMillis() - startTimeMillis;
        } catch (final Throwable t) {
          boolean hasNext = false;
          synchronized (lock) {
            runningTask = null;
            runningThread = null;
            workerAlert.notify(WorkerAlert.WAIT_STOP, currentThread);
            if (status == PAUSING) {
              status = PAUSED;
            } else {
              hasNext = !beforeQueue.isEmpty() || !afterQueue.isEmpty();
            }
          }
          if (hasNext) {
            executor.execute(this);
          }
          LOGGER.log(Level.SEVERE, "Uncaught exception", t);
          throw UncheckedException.throwUnchecked(t);
        }
      }

      synchronized (lock) {
        runningTask = null;
        runningThread = null;
        workerAlert.notify(WorkerAlert.WAIT_STOP, currentThread);
        if (status == PAUSING) {
          status = PAUSED;
          return;
        }
        if (beforeQueue.isEmpty() && afterQueue.isEmpty()) {
          // move to IDLE
          status = IDLE;
          return;
        }
      }
      executor.execute(this);
    }
  }
}
