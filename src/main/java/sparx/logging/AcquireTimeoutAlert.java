/*
 * Copyright 2023 Davide Maestroni
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
package sparx.logging;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import sparx.util.Requires;

class AcquireTimeoutAlert implements JoinAlert {

  private final Object tag;
  private final long timeout;
  private final long timeoutMillis;
  private final TimeUnit timeoutUnit;

  AcquireTimeoutAlert(final Object tag, final long timeout, @NotNull final TimeUnit timeoutUnit) {
    this.tag = tag;
    this.timeout = timeout;
    this.timeoutUnit = timeoutUnit;
    this.timeoutMillis = timeoutUnit.toMillis(Requires.positive(timeout, "timeout"));
  }

  @Override
  public void notifyAcquire(@NotNull final Semaphore semaphore) throws InterruptedException {
    if (!semaphore.tryAcquire(timeout, timeoutUnit)) {
      Log.wrn(tag, "Join on thread %s is taking too long: still waiting after %d %s!",
          Thread.currentThread(), timeout, timeoutUnit);
      semaphore.acquire();
    }
  }

  @Override
  public boolean notifyTryAcquire(@NotNull final Semaphore semaphore, final long timeout,
      @NotNull final TimeUnit unit) throws InterruptedException {
    final long timeoutMillis = unit.toMillis(timeout);
    final long maxTimeoutMillis = this.timeoutMillis;
    if (timeoutMillis > maxTimeoutMillis) {
      if (!semaphore.tryAcquire(maxTimeoutMillis, TimeUnit.MILLISECONDS)) {
        Log.wrn(tag, "Join on thread %s is taking too long: still waiting after %d %s!",
            Thread.currentThread(), this.timeout, this.timeoutUnit);
        return semaphore.tryAcquire(timeoutMillis - maxTimeoutMillis, TimeUnit.MILLISECONDS);
      }
      return true;
    }
    return semaphore.tryAcquire(timeout, unit);
  }
}
