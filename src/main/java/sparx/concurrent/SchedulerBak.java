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

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import sparx.logging.Log;
import sparx.util.Require;
import sparx.util.UncheckedException;

public class SchedulerBak {

  private static final Executor synchronousExecutor = new Executor() {
    @Override
    public void execute(@NotNull Runnable command) {
      command.run();
    }
  };

  private static final int IDLE = 0;
  private static final int READING = 1;
  private static final int RUNNING = 2;
  private static final int PAUSING = 3;
  private static final int PAUSED = 4;

  private final Executor executor;
  private final ConcurrentLinkedQueue<Task> beforeQueue = new ConcurrentLinkedQueue<Task>();
  private final ConcurrentLinkedQueue<Task> afterQueue = new ConcurrentLinkedQueue<Task>();
  private final int minThroughput;
  private final AtomicInteger pauseStatus = new AtomicInteger(RUNNING);
  private final Runnable runner;
  private final AtomicInteger status = new AtomicInteger(IDLE);

  public static @NotNull SchedulerBak of(@NotNull final Executor executor) {
    return of(executor, -1);
  }

  public static @NotNull SchedulerBak of(@NotNull final Executor executor,
      final int minThroughput) {
    return new SchedulerBak(executor, minThroughput);
  }

  public static @NotNull SchedulerBak trampoline() {
    return new SchedulerBak(synchronousExecutor, Integer.MAX_VALUE);
  }

  private SchedulerBak(@NotNull final Executor executor, final int minThroughput) {
    this.executor = Require.notNull(executor, "executor");
    this.minThroughput = Require.positive(minThroughput, "minThroughput");
    if (minThroughput == Integer.MAX_VALUE) {
      // infinite throughput
      this.runner = new InfiniteRunner();
    } else if (minThroughput == 1) {
      this.runner = new SingleRunner();
    } else {
      this.runner = new ThroughputRunner();
    }
  }

  public int minThroughput() {
    return minThroughput;
  }

  public void pause() {
    pauseStatus.compareAndSet(RUNNING, PAUSING);
  }

  public int pendingCount() {
    return beforeQueue.size() + afterQueue.size();
  }

  public void resume() {
    final AtomicInteger pauseStatus = this.pauseStatus;
    if (!pauseStatus.compareAndSet(PAUSING, RUNNING)
        && pauseStatus.compareAndSet(PAUSED, RUNNING)) {
      executor.execute(runner);
    }
  }

  public void scheduleAfter(@NotNull final Task task) {
    afterQueue.offer(task);
    final AtomicInteger status = this.status;
    if (!status.compareAndSet(READING, RUNNING) && status.compareAndSet(IDLE, RUNNING)) {
      executor.execute(runner);
    }
  }

  public void scheduleBefore(@NotNull final Task task) {
    beforeQueue.offer(task);
    final AtomicInteger status = this.status;
    if (!status.compareAndSet(READING, RUNNING) && status.compareAndSet(IDLE, RUNNING)) {
      executor.execute(runner);
    }
  }

  public interface Task extends Runnable {

    int weight();
  }

  private class InfiniteRunner implements Runnable {

    public void run() {
      final AtomicInteger status = SchedulerBak.this.status;
      final AtomicInteger pauseStatus = SchedulerBak.this.pauseStatus;
      final ConcurrentLinkedQueue<Task> beforeQueue = SchedulerBak.this.beforeQueue;
      final ConcurrentLinkedQueue<Task> afterQueue = SchedulerBak.this.afterQueue;
      while (true) {
        if (pauseStatus.compareAndSet(PAUSING, PAUSED)) {
          return;
        }

        status.set(READING);
        Task task = beforeQueue.poll();
        if (task == null) {
          task = afterQueue.poll();
          if (task == null) {
            // move to IDLE
            if (status.compareAndSet(READING, IDLE)) {
              return;
            } else {
              continue;
            }
          }
        }

        status.set(RUNNING);
        try {
          task.run();
        } catch (final Throwable t) {
          Log.err(SchedulerBak.class, "Uncaught exception: %s", Log.printable(t));
          throw UncheckedException.throwUnchecked(t);
        }
      }
    }
  }

  private class SingleRunner implements Runnable {

    public void run() {
      if (pauseStatus.compareAndSet(PAUSING, PAUSED)) {
        return;
      }

      final AtomicInteger status = SchedulerBak.this.status;
      final Executor executor = SchedulerBak.this.executor;
      status.set(READING);
      Task task = beforeQueue.poll();
      if (task == null) {
        task = afterQueue.poll();
        if (task == null) {
          // move to IDLE
          if (!status.compareAndSet(READING, IDLE)) {
            executor.execute(this);
          }
          return;
        }
      }

      status.set(RUNNING);
      try {
        task.run();
      } catch (final Throwable t) {
        Log.err(SchedulerBak.class, "Uncaught exception: %s", Log.printable(t));
        throw UncheckedException.throwUnchecked(t);
      }
      executor.execute(this);
    }
  }

  private class ThroughputRunner implements Runnable {

    public void run() {
      final AtomicInteger status = SchedulerBak.this.status;
      final AtomicInteger pauseStatus = SchedulerBak.this.pauseStatus;
      final ConcurrentLinkedQueue<Task> beforeQueue = SchedulerBak.this.beforeQueue;
      final ConcurrentLinkedQueue<Task> afterQueue = SchedulerBak.this.afterQueue;
      int minThroughput = SchedulerBak.this.minThroughput;
      while (minThroughput > 0) {
        if (pauseStatus.compareAndSet(PAUSING, PAUSED)) {
          return;
        }

        status.set(READING);
        Task task = beforeQueue.poll();
        if (task == null) {
          task = afterQueue.poll();
          if (task == null) {
            // move to IDLE
            if (status.compareAndSet(READING, IDLE)) {
              return;
            } else {
              continue;
            }
          }
        }

        status.set(RUNNING);
        try {
          minThroughput -= Math.max(task.weight(), 1);
          task.run();
        } catch (final Throwable t) {
          Log.err(SchedulerBak.class, "Uncaught exception: %s", Log.printable(t));
          throw UncheckedException.throwUnchecked(t);
        }
      }

      if (pauseStatus.compareAndSet(PAUSING, PAUSED)) {
        return;
      }

      status.set(READING);
      Task task = beforeQueue.peek();
      if (task == null) {
        task = beforeQueue.peek();
        if (task == null) {
          // move to IDLE
          if (!status.compareAndSet(READING, IDLE)) {
            executor.execute(this);
          }
        } else {
          executor.execute(this);
        }
      } else {
        executor.execute(this);
      }
    }
  }
}
