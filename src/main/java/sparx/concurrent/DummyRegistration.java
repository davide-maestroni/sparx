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

import org.jetbrains.annotations.NotNull;
import sparx.concurrent.FutureContext.Registration;

class DummyRegistration implements Registration {

  private final static DummyRegistration INSTANCE = new DummyRegistration();

  public static @NotNull DummyRegistration instance() {
    return INSTANCE;
  }

  private DummyRegistration() {
  }

  @Override
  public void cancel() {
  }

  @Override
  public void onUncaughtError(@NotNull final Exception error) {
  }
}
