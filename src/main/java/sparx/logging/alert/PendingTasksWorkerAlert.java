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

import sparx.logging.Log;
import sparx.util.Requires;

class PendingTasksWorkerAlert implements SchedulerQueueAlert {

  private final int maxTasksCount;
  private final Object tag;

  PendingTasksWorkerAlert(final Object tag, final int maxTasksCount) {
    this.maxTasksCount = Requires.positive(maxTasksCount, "maxTasksCount");
    this.tag = tag;
  }

  @Override
  public void notifyPendingTasks(final int beforeQueueCount, final int afterQueueCount) {
    final int count = afterQueueCount - beforeQueueCount;
    if (count > maxTasksCount) {
      Log.wrn(tag, "Pending tasks count exceeded the limit of %d: %d!", maxTasksCount, count);
    }
  }
}
