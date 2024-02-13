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
package sparx.concurrent.history;

import java.util.List;
import org.jetbrains.annotations.NotNull;

class SwitchOnSubscribeHistory<V> implements SignalHistory<V> {

  private final SignalHistory<V> strategy;
  private SignalHistory<V> status;

  SwitchOnSubscribeHistory(@NotNull final SignalHistory<V> initialStrategy,
      @NotNull final SignalHistory<V> finalStrategy) {
    status = initialStrategy;
    strategy = finalStrategy;
  }

  @Override
  public void onClear() {
    status.onClear();
  }

  @Override
  public void onClose() {
    status.onClose();
  }

  @Override
  public void onPush(final V value) {
    status.onPush(value);
  }

  @Override
  public void onPushBulk(@NotNull final List<V> values) {
    status.onPushBulk(values);
  }

  @Override
  public @NotNull List<V> onSubscribe() {
    final List<V> values = status.onSubscribe();
    status = strategy;
    return values;
  }
}
