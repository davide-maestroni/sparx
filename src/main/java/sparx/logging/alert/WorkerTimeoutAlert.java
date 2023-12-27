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
package sparx.logging.alert;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.WeakHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import sparx.logging.Log;
import sparx.util.Requires;

class WorkerTimeoutAlert implements SchedulerWorkerAlert, Runnable {

  private final ScheduledFuture<?> future;
  private final Object mutex = new Object();
  private final Object tag;
  private final long timeout;
  private final long timeoutMillis;
  private final TimeUnit timeoutUnit;
  private final WeakHashMap<Thread, Long> timestamps = new WeakHashMap<Thread, Long>();

  WorkerTimeoutAlert(final Object tag, @NotNull final ScheduledExecutorService executorService,
      final long interval, @NotNull final TimeUnit intervalUnit, final long timeout,
      @NotNull final TimeUnit timeoutUnit) {
    this.tag = tag;
    this.timeout = timeout;
    this.timeoutUnit = timeoutUnit;
    this.timeoutMillis = timeoutUnit.toMillis(Requires.positive(timeout, "timeout"));
    this.future = executorService.scheduleAtFixedRate(this, interval, interval, intervalUnit);
  }

  @Override
  public void notifyTaskStart(@NotNull final Thread currentThread) {
    synchronized (mutex) {
      timestamps.put(currentThread, System.currentTimeMillis());
    }
  }

  @Override
  public void notifyTaskStop(@NotNull final Thread currentThread) {
    synchronized (mutex) {
      timestamps.remove(currentThread);
    }
  }

  @Override
  public void turnOff() {
    future.cancel(false);
  }

  @Override
  public void run() {
    final long now = System.currentTimeMillis();
    final long timeoutMillis = this.timeoutMillis;
    final HashMap<Thread, Long> timeouts = new HashMap<Thread, Long>();
    synchronized (mutex) {
      for (final Entry<Thread, Long> entry : timestamps.entrySet()) {
        if (now - entry.getValue() > timeoutMillis) {
          timeouts.put(entry.getKey(), entry.getValue());
        }
      }
    }
    for (final Entry<Thread, Long> entry : timeouts.entrySet()) {
      Log.wrn(tag,
          "Tasks on thread %s is taking too long: timeout was %d %s but task is still running after %d %s!",
          entry.getKey(), timeout, timeoutUnit, now - entry.getValue(), TimeUnit.MILLISECONDS);
    }
  }
}
