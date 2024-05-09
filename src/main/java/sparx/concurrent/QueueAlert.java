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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.util.Require;

class QueueAlert implements Alert<Integer> {

  private final Logger logger;
  private final int maxTasksCount;

  QueueAlert(@NotNull final Logger logger, final int maxTasksCount) {
    this.logger = Require.notNull(logger, "logger");
    this.maxTasksCount = Require.positive(maxTasksCount, "maxTasksCount");
  }

  @Override
  public void disable() {
  }

  @Override
  public void notify(final int afterQueueSize, final Integer beforeQueueSize) {
    final int totalSize = afterQueueSize + beforeQueueSize;
    if (totalSize > maxTasksCount) {
      logger.log(Level.WARNING, String.format(
          "Pending tasks count exceeded the limit of %d: %d!\nPlease consider adding backpressure to the execution context.",
          maxTasksCount, totalSize));
    }
  }
}
