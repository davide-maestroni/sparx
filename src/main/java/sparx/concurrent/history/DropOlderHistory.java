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
import sparx.util.DequeueList;
import sparx.util.Require;

class DropOlderHistory<V> implements SignalHistory<V> {

  private final DequeueList<V> history = new DequeueList<V>();
  private final int maxSize;

  DropOlderHistory(final int maxSize) {
    this.maxSize = Require.positive(maxSize, "maxSize");
  }

  @Override
  public void onClear() {
    history.clear();
  }

  @Override
  public void onClose() {
  }

  @Override
  public void onPush(final V value) {
    history.add(value);
    drop();
  }

  @Override
  public void onPushBulk(@NotNull final List<V> values) {
    history.addAll(values);
    drop();
  }

  @Override
  public @NotNull List<V> onSubscribe() {
    return history;
  }

  private void drop() {
    final int maxSize = this.maxSize;
    final DequeueList<V> history = this.history;
    while (history.size() > maxSize) {
      history.removeFirst();
    }
  }
}
