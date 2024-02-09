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

import java.util.Properties;
import org.jetbrains.annotations.NotNull;
import sparx.logging.Log;
import sparx.logging.alert.Alerts.Alert;
import sparx.util.Require;

public class SchedulerQueueAlert implements Alert<Integer> {

  private static final String PROP_PREFIX = SchedulerQueueAlert.class.getName();
  public static final String MAX_TASK_COUNT_PROP = PROP_PREFIX + ".maxCount";

  private final int maxTasksCount;

  public SchedulerQueueAlert(final int maxTasksCount) {
    this.maxTasksCount = Require.positive(maxTasksCount, "maxTasksCount");
  }

  public SchedulerQueueAlert(@NotNull final Properties properties) {
    this(Integer.parseInt(properties.getProperty(MAX_TASK_COUNT_PROP, "1000")));
  }

  @Override
  public void disable() {
  }

  @Override
  public void notify(final int afterQueueSize, final Integer beforeQueueSize) {
    final int totalSize = afterQueueSize + beforeQueueSize;
    if (totalSize > maxTasksCount) {
      Log.wrn(SchedulerQueueAlert.class,
          "Pending tasks count exceeded the limit of %d: %d!\nPlease consider adding backpressure to the consumer future.",
          maxTasksCount, totalSize);
    }
  }
}
