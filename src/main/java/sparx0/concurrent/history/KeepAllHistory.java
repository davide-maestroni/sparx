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
package sparx0.concurrent.history;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import sparx0.util.logging.alert.Alerts;
import sparx0.util.logging.alert.Alerts.Alert;
import sparx0.util.logging.alert.HistorySizeAlert;

class KeepAllHistory<V> implements SignalHistory<V> {

  private static final Alert<Void> historyAlert = Alerts.get(HistorySizeAlert.class);

  private final ArrayList<V> history = new ArrayList<V>();

  @Override
  public void onClear() {
    history.clear();
  }

  @Override
  public void onClose() {
  }

  @Override
  public void onPush(final V value) {
    final ArrayList<V> history = this.history;
    history.add(value);
    historyAlert.notify(history.size(), null);
  }

  @Override
  public void onPushBulk(@NotNull final List<V> values) {
    final ArrayList<V> history = this.history;
    history.addAll(values);
    historyAlert.notify(history.size(), null);
  }

  @Override
  public @NotNull List<V> onSubscribe() {
    return history;
  }
}
