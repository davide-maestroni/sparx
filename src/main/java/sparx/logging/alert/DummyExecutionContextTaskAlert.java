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
import sparx.function.Consumer;
import sparx.function.Function;

class DummyExecutionContextTaskAlert implements ExecutionContextTaskAlert {

  private static final DummyExecutionContextTaskAlert INSTANCE = new DummyExecutionContextTaskAlert();

  public static @NotNull DummyExecutionContextTaskAlert instance() {
    return INSTANCE;
  }

  private DummyExecutionContextTaskAlert() {
  }

  @Override
  public void notifyCall(@NotNull final Function<?, ?> function) {
  }

  @Override
  public void notifyRun(@NotNull final Consumer<?> consumer) {
  }
}
