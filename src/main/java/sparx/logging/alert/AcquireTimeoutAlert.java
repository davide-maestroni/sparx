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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import sparx.logging.Log;
import sparx.util.Requires;
import sparx.util.SharedTimer;

class AcquireTimeoutAlert implements JoinAlert, Runnable {

  private final ScheduledFuture<?> future;
  private final Object mutex = new Object();
  private final SharedTimer timer;
  private final long timeoutMillis;
  private final WeakHashMap<Thread, Long> timestamps = new WeakHashMap<Thread, Long>();

  AcquireTimeoutAlert(final long interval, @NotNull final TimeUnit intervalUnit, final long timeout,
      @NotNull final TimeUnit timeoutUnit) {
    timeoutMillis = timeoutUnit.toMillis(Requires.positive(timeout, "timeout"));
    timer = SharedTimer.acquire();
    future = timer.scheduleAtFixedRate(this, interval, interval, intervalUnit);
  }

  @Override
  public void notifyJoinStart() {
    synchronized (mutex) {
      timestamps.put(Thread.currentThread(), System.currentTimeMillis());
    }
  }

  @Override
  public void notifyJoinStop() {
    synchronized (mutex) {
      timestamps.remove(Thread.currentThread());
    }
  }

  @Override
  public void turnOff() {
    future.cancel(false);
    timer.release();
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
      Log.wrn(JoinAlert.class,
          "Join on thread %s is taking too long: still waiting after %d %s!\nPlease verify no deadlock is happening.",
          entry.getKey(), now - entry.getValue(), TimeUnit.MILLISECONDS);
    }
  }
}
