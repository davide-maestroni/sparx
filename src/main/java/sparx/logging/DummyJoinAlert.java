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

class DummyJoinAlert implements JoinAlert {

  private static final DummyJoinAlert INSTANCE = new DummyJoinAlert();

  public static @NotNull DummyJoinAlert instance() {
    return INSTANCE;
  }

  @Override
  public void notifyAcquire(@NotNull final Semaphore semaphore) throws InterruptedException {
    semaphore.acquire();
  }

  @Override
  public boolean notifyTryAcquire(@NotNull final Semaphore semaphore, final long timeout,
      @NotNull final TimeUnit unit) throws InterruptedException {
    return semaphore.tryAcquire(timeout, unit);
  }
}
