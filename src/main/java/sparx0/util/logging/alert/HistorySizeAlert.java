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
package sparx0.util.logging.alert;

import java.util.Properties;
import org.jetbrains.annotations.NotNull;
import sparx0.util.Require;
import sparx0.util.logging.Log;
import sparx0.util.logging.alert.Alerts.Alert;

public class HistorySizeAlert implements Alert<Void> {

  private static final String PROP_PREFIX = SchedulerQueueAlert.class.getName();
  public static final String MAX_SIZE_PROP = PROP_PREFIX + ".maxSize";

  private final int maxSize;

  public HistorySizeAlert(final int maxSize) {
    this.maxSize = Require.positive(maxSize, "maxSize");
  }

  public HistorySizeAlert(@NotNull final Properties properties) {
    this(Integer.parseInt(properties.getProperty(MAX_SIZE_PROP, "100000")));
  }

  @Override
  public void disable() {
  }

  @Override
  public void notify(final int size, final Void payload) {
    if (size > maxSize) {
      Log.wrn(HistorySizeAlert.class,
          "History size exceeded the limit of %d: %d!\nPlease consider using a dropping strategy.",
          maxSize, size);
    }
  }
}
