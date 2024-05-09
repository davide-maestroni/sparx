/*
 * Copyright 2024ß Davide Maestroni
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

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.util.Require;
import sparx.util.SharedTimer;

class BackpressureAlert implements Alert<Thread>, Runnable {

  public static final int WAIT_START = 1;
  public static final int WAIT_STOP = 0;

  private final ScheduledFuture<?> future;
  private final Logger logger;
  private final long timeout;
  private final long timeoutMillis;
  private final TimeUnit timeoutUnit;
  private final SharedTimer timer;
  private final WeakHashMap<Thread, Long> timestamps = new WeakHashMap<Thread, Long>();

  BackpressureAlert(@NotNull final Logger logger, final long interval,
      @NotNull final TimeUnit intervalUnit, final long timeout,
      @NotNull final TimeUnit timeoutUnit) {
    this.logger = Require.notNull(logger, "logger");
    this.timeout = timeout;
    this.timeoutUnit = timeoutUnit;
    timeoutMillis = timeoutUnit.toMillis(Require.positive(timeout, "timeout"));
    timer = SharedTimer.acquire();
    future = timer.scheduleAtFixedRate(this, interval, interval, intervalUnit);
  }

  @Override
  public void disable() {
    future.cancel(false);
    timer.release();
  }

  @Override
  public void notify(final int state, final Thread currentThread) {
    synchronized (timestamps) {
      if (state == WAIT_START) {
        timestamps.put(currentThread, System.currentTimeMillis());
      } else if (state == WAIT_STOP) {
        timestamps.remove(currentThread);
      }
    }
  }

  @Override
  public void run() {
    final long now = System.currentTimeMillis();
    final long timeoutMillis = this.timeoutMillis;
    final HashMap<Thread, Long> timeouts = new HashMap<Thread, Long>();
    synchronized (timestamps) {
      for (final Entry<Thread, Long> entry : timestamps.entrySet()) {
        if (now - entry.getValue() > timeoutMillis) {
          timeouts.put(entry.getKey(), entry.getValue());
        }
      }
    }
    for (final Entry<Thread, Long> entry : timeouts.entrySet()) {
      logger.log(Level.WARNING, String.format(
          "Producer on thread %s is waiting too long: timeout was %d %s but still waiting after %d %s!\nPlease consider distributing processing over more consumers.",
          entry.getKey(), timeout, timeoutUnit, now - entry.getValue(), TimeUnit.MILLISECONDS));
    }
  }
}
