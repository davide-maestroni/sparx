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
package sparx.internal.future.iterator;

import static sparx.internal.future.AsyncConsumers.safeConsume;
import static sparx.internal.future.AsyncConsumers.safeConsumeComplete;

import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.internal.future.AsyncConsumer;
import sparx.internal.future.IndexedAsyncConsumer;

public class ElementToIteratorAsyncMaterializer<E> implements IteratorAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      ElementToIteratorAsyncMaterializer.class.getName());

  private final E element;

  private boolean consumed;

  public ElementToIteratorAsyncMaterializer(final E element) {
    this.element = element;
  }

  @Override
  public boolean isCancelled() {
    return false;
  }

  @Override
  public boolean isDone() {
    return true;
  }

  @Override
  public int knownSize() {
    return 1;
  }

  @Override
  public void materializeCancel(final boolean mayInterruptIfRunning) {
  }

  @Override
  public void materializeHasNext(@NotNull final AsyncConsumer<Boolean> consumer) {
    safeConsume(consumer, !consumed, LOGGER);
  }

  @Override
  public void materializeNext(@NotNull final IndexedAsyncConsumer<E> consumer) {
    if (consumed) {
      safeConsumeComplete(consumer, 1, LOGGER);
    } else {
      consumed = true;
      safeConsume(consumer, 1, 0, element, LOGGER);
    }
  }

  @Override
  public void materializeSkip(final int count, @NotNull final AsyncConsumer<Integer> consumer) {
    if (count <= 0 || consumed) {
      safeConsume(consumer, 0, LOGGER);
    } else {
      consumed = true;
      safeConsume(consumer, 1, LOGGER);
    }
  }

  @Override
  public int weightHasNext() {
    return 1;
  }

  @Override
  public int weightNext() {
    return 1;
  }

  @Override
  public int weightSkip(final int count) {
    return 1;
  }
}
