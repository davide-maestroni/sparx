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
package sparx.util;

import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;

public class SharedTimer {

  private static final Object mutex = new Object();
  private static int sharedCount = 0;
  private static ScheduledExecutorService executorService;

  private final AtomicBoolean released = new AtomicBoolean(false);

  public static @NotNull SharedTimer acquire() {
    synchronized (mutex) {
      if (sharedCount == 0) {
        executorService = createsExecutorService();
      }
      ++sharedCount;
    }
    return new SharedTimer();
  }

  private static @NotNull ScheduledExecutorService createsExecutorService() {
    return Executors.newSingleThreadScheduledExecutor(
        new ThreadFactory() {
          @Override
          public Thread newThread(@NotNull final Runnable r) {
            final Thread thread = new Thread(r, "sparx-timer");
            thread.setPriority(Thread.MIN_PRIORITY);
            return thread;
          }
        });
  }

  private SharedTimer() {
  }

  public void release() {
    if (released.compareAndSet(false, true)) {
      synchronized (mutex) {
        if (--sharedCount == 0) {
          // defer shutdown to avoid destroying and re-creating threads too frequently
          executorService.schedule(new Runnable() {
            @Override
            public void run() {
              synchronized (mutex) {
                if (sharedCount == 0) {
                  executorService.shutdownNow();
                  executorService = null;
                }
              }
            }
          }, 10, TimeUnit.SECONDS);
        }
      }
    }
  }

  public @NotNull ScheduledFuture<?> schedule(@NotNull final Runnable command, final long delay,
      @NotNull final TimeUnit unit) {
    if (released.get()) {
      throw new RejectedExecutionException("Timer already released");
    }
    return executorService.schedule(command, delay, unit);
  }

  public @NotNull ScheduledFuture<?> scheduleAtFixedRate(@NotNull final Runnable command,
      final long initialDelay, final long period, @NotNull final TimeUnit unit) {
    if (released.get()) {
      throw new RejectedExecutionException("Timer already released");
    }
    return executorService.scheduleAtFixedRate(command, initialDelay, period, unit);
  }

  public @NotNull ScheduledFuture<?> scheduleWithFixedDelay(@NotNull final Runnable command,
      final long initialDelay, final long delay, @NotNull final TimeUnit unit) {
    if (released.get()) {
      throw new RejectedExecutionException("Timer already released");
    }
    return executorService.scheduleWithFixedDelay(command, initialDelay, delay, unit);
  }
}
