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

import org.jetbrains.annotations.NotNull;

class DummyBackpressureAlert implements BackpressureAlert {

  private static final DummyBackpressureAlert INSTANCE = new DummyBackpressureAlert();

  public static @NotNull DummyBackpressureAlert instance() {
    return INSTANCE;
  }

  private DummyBackpressureAlert() {
  }

  @Override
  public void notifyWaitStart(@NotNull final Thread currentThread) {
  }

  @Override
  public void notifyWaitStop(@NotNull final Thread currentThread) {
  }

  @Override
  public void turnOff() {
  }
}
