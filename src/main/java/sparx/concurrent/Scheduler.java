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
import sparx.util.Requires;

public class Scheduler {

  private static final Executor synchronousExecutor = new Executor() {
    @Override
    public void execute(@NotNull Runnable command) {
      command.run();
    }
  };

  private static final int RUNNING = 1;
  private static final int PAUSING = 2;
  private static final int PAUSED = 3;

  private final Executor executor;
  private final ArrayDeque<Task> beforeQueue = new ArrayDeque<Task>();
  private final ArrayDeque<Task> afterQueue = new ArrayDeque<Task>();
  private final int minThroughput;
  private final Object mutex = new Object();
  private final Runnable runner;

  private int pauseStatus = RUNNING;
  private Task runningTask;
  private Thread runningThread;

  public static @NotNull Scheduler of(@NotNull final Executor executor) {
    return of(executor, Integer.MAX_VALUE);
  }

  public static @NotNull Scheduler of(@NotNull final Executor executor, final int minThroughput) {
    return new Scheduler(executor, minThroughput);
  }

  public static @NotNull Scheduler trampoline() {
    return new Scheduler(synchronousExecutor, Integer.MAX_VALUE);
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
      if (pauseStatus == RUNNING) {
        pauseStatus = PAUSING;
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
      needsExecution = (pauseStatus == PAUSED);
      pauseStatus = RUNNING;
    }
    if (needsExecution) {
      executor.execute(runner);
    }
  }

  public void scheduleAfter(@NotNull final Task task) {
    final boolean needsExecution;
    synchronized (mutex) {
      final ArrayDeque<Task> afterQueue = this.afterQueue;
      needsExecution = afterQueue.isEmpty();
      afterQueue.offer(task);
      // TODO: alert => too many pending
    }
    if (needsExecution) {
      executor.execute(runner);
    }
  }

  public void scheduleBefore(@NotNull final Task task) {
    final boolean needsExecution;
    synchronized (mutex) {
      final ArrayDeque<Task> beforeQueue = this.beforeQueue;
      needsExecution = beforeQueue.isEmpty();
      beforeQueue.offer(task);
    }
    if (needsExecution) {
      executor.execute(runner);
    }
  }

  public interface Task extends Runnable {

    @NotNull String taskID();

    int weight();
  }

  private class InfiniteRunner implements Runnable {

    // TODO: alert => set volatile startTime (hook) => periodically check

    @Override
    public void run() {
      final Thread currentThread = Thread.currentThread();
      final ArrayDeque<Task> beforeQueue = Scheduler.this.beforeQueue;
      final ArrayDeque<Task> afterQueue = Scheduler.this.afterQueue;
      while (true) {
        Task task;
        synchronized (mutex) {
          runningTask = null;
          runningThread = null;
          if (pauseStatus == PAUSING) {
            pauseStatus = PAUSED;
            return;
          }

          task = beforeQueue.poll();
          if (task == null) {
            task = afterQueue.poll();
            if (task == null) {
              // move to IDLE
              return;
            }
          }
          runningTask = task;
          runningThread = currentThread;
        }

        try {
          task.run();
          // TODO: alert => takes too long?
        } catch (final Throwable t) {
          synchronized (mutex) {
            runningTask = null;
            runningThread = null;
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
      final Executor executor = Scheduler.this.executor;
      Task task;
      synchronized (mutex) {
        if (pauseStatus == PAUSING) {
          pauseStatus = PAUSED;
          return;
        }

        task = beforeQueue.poll();
        if (task == null) {
          task = afterQueue.poll();
          if (task == null) {
            // move to IDLE
            return;
          }
        }
        runningTask = task;
        runningThread = Thread.currentThread();
      }

      try {
        task.run();
        // TODO: alert => takes too long?
      } catch (final Throwable t) {
        Log.err(Scheduler.class, "Uncaught exception: %s", Log.printable(t));
        UncheckedException.throwUnchecked(t);
      } finally {
        synchronized (mutex) {
          runningTask = null;
          runningThread = null;
        }
      }
      executor.execute(this);
    }
  }

  private class ThroughputRunner implements Runnable {

    @Override
    public void run() {
      final Thread currentThread = Thread.currentThread();
      final ArrayDeque<Task> beforeQueue = Scheduler.this.beforeQueue;
      final ArrayDeque<Task> afterQueue = Scheduler.this.afterQueue;
      int minThroughput = Scheduler.this.minThroughput;
      while (minThroughput > 0) {
        Task task;
        synchronized (mutex) {
          runningTask = null;
          runningThread = null;
          if (pauseStatus == PAUSING) {
            pauseStatus = PAUSED;
            return;
          }

          task = beforeQueue.poll();
          if (task == null) {
            task = afterQueue.poll();
            if (task == null) {
              // move to IDLE
              return;
            }
          }
          runningTask = task;
          runningThread = currentThread;
        }

        try {
          minThroughput -= Math.max(task.weight(), 1);
          task.run();
          // TODO: alert => takes too long?
        } catch (final Throwable t) {
          synchronized (mutex) {
            runningTask = null;
            runningThread = null;
          }
          Log.err(Scheduler.class, "Uncaught exception: %s", Log.printable(t));
          UncheckedException.throwUnchecked(t);
        }
      }

      int size;
      synchronized (mutex) {
        if (pauseStatus == PAUSING) {
          pauseStatus = PAUSED;
          return;
        }

        size = beforeQueue.size();
        if (size == 0) {
          size = afterQueue.size();
        }
      }
      if (size > 0) {
        executor.execute(this);
      }
    }
  }
}
