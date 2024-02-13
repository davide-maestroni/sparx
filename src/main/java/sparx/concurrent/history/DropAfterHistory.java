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
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import sparx.util.DequeueList;
import sparx.util.logging.alert.Alerts;
import sparx.util.logging.alert.Alerts.Alert;
import sparx.util.logging.alert.HistorySizeAlert;

class DropAfterHistory<V> implements SignalHistory<V> {

  private static final Alert<Void> historyAlert = Alerts.get(HistorySizeAlert.class);

  private final DequeueList<V> history = new DequeueList<V>();
  private final DequeueList<Long> timestamps = new DequeueList<Long>();
  private final long timeoutMillis;

  DropAfterHistory(final long timeout, @NotNull final TimeUnit unit) {
    timeoutMillis = unit.toMillis(timeout);
  }

  @Override
  public void onClear() {
    timestamps.clear();
    history.clear();
  }

  @Override
  public void onClose() {
  }

  @Override
  public void onPush(final V value) {
    final long timestamp = System.currentTimeMillis();
    timestamps.add(timestamp);
    history.add(value);
    drop(timestamp);
    historyAlert.notify(history.size(), null);
  }

  @Override
  public void onPushBulk(@NotNull final List<V> values) {
    final Long timestamp = System.currentTimeMillis();
    final DequeueList<V> history = this.history;
    final DequeueList<Long> timestamps = this.timestamps;
    for (final V value : values) {
      timestamps.add(timestamp);
      history.add(value);
    }
    drop(timestamp);
    historyAlert.notify(history.size(), null);
  }

  @Override
  public @NotNull List<V> onSubscribe() {
    return history;
  }

  private void drop(final long currentTimeMillis) {
    final long minTimestamp = currentTimeMillis - timeoutMillis;
    final DequeueList<V> history = this.history;
    final DequeueList<Long> timestamps = this.timestamps;
    while (!timestamps.isEmpty() && timestamps.getFirst() < minTimestamp) {
      timestamps.removeFirst();
      history.removeFirst();
    }
  }
}
