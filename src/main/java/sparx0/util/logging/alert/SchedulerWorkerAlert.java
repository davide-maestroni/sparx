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
package sparx0.util.logging.alert;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.WeakHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import sparx0.util.Require;
import sparx0.util.SharedTimer;
import sparx0.util.logging.Log;
import sparx0.util.logging.alert.Alerts.Alert;

public class SchedulerWorkerAlert implements Alert<Thread>, Runnable {

  private static final String PROP_PREFIX = SchedulerWorkerAlert.class.getName();
  public static final String INTERVAL_PROP = PROP_PREFIX + ".interval";
  public static final String INTERVAL_UNIT_PROP = PROP_PREFIX + ".intervalUnit";
  public static final String TIMEOUT_PROP = PROP_PREFIX + ".timeout";
  public static final String TIMEOUT_UNIT_PROP = PROP_PREFIX + ".timeoutUnit";

  public static final int WAIT_START = 1;
  public static final int WAIT_STOP = 0;

  private final ScheduledFuture<?> future;
  private final long timeout;
  private final long timeoutMillis;
  private final TimeUnit timeoutUnit;
  private final SharedTimer timer;
  private final WeakHashMap<Thread, Long> timestamps = new WeakHashMap<Thread, Long>();

  public SchedulerWorkerAlert(final long interval, @NotNull final TimeUnit intervalUnit,
      final long timeout, @NotNull final TimeUnit timeoutUnit) {
    this.timeout = timeout;
    this.timeoutUnit = timeoutUnit;
    timeoutMillis = timeoutUnit.toMillis(Require.positive(timeout, "timeout"));
    timer = SharedTimer.acquire();
    future = timer.scheduleAtFixedRate(this, interval, interval, intervalUnit);
  }

  public SchedulerWorkerAlert(@NotNull final Properties properties) {
    final long interval;
    final TimeUnit intervalUnit;
    final String intervalProp = properties.getProperty(INTERVAL_PROP);
    if (intervalProp == null) {
      interval = 10;
      intervalUnit = TimeUnit.SECONDS;
    } else {
      interval = Long.parseLong(intervalProp);
      intervalUnit = TimeUnit.valueOf(
          properties.getProperty(INTERVAL_UNIT_PROP, TimeUnit.SECONDS.name()));
    }
    final long timeout;
    final TimeUnit timeoutUnit;
    final String timeoutProp = properties.getProperty(TIMEOUT_PROP);
    if (intervalProp == null) {
      timeout = 1;
      timeoutUnit = TimeUnit.MINUTES;
    } else {
      timeout = Long.parseLong(timeoutProp);
      timeoutUnit = TimeUnit.valueOf(
          properties.getProperty(TIMEOUT_UNIT_PROP, TimeUnit.SECONDS.name()));
    }
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
      Log.wrn(SchedulerWorkerAlert.class,
          "Tasks on thread %s is taking too long: timeout was %d %s but task is still running after %d %s!\nPlease consider decreasing the throughput or splitting the task into smaller steps.",
          entry.getKey(), timeout, timeoutUnit, now - entry.getValue(), TimeUnit.MILLISECONDS);
    }
  }
}
