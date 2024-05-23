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
package sparx.collection.internal.future.sequential.list;

import java.util.List;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.collection.internal.future.AsyncConsumer;
import sparx.collection.internal.future.IndexedAsyncConsumer;
import sparx.util.Require;

public class ListToListAsyncMaterializer<E> extends AbstractListAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      ListToListAsyncMaterializer.class.getName());

  private final List<E> elements;

  public ListToListAsyncMaterializer(@NotNull final List<E> elements) {
    this.elements = Require.notNull(elements, "elements");
  }

  @Override
  public boolean knownEmpty() {
    return elements.isEmpty();
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
  public void materializeCancel(final boolean mayInterruptIfRunning) {
  }

  @Override
  @SuppressWarnings("SuspiciousMethodCalls")
  public void materializeContains(final Object element,
      @NotNull final AsyncConsumer<Boolean> consumer) {
    safeConsume(consumer, elements.contains(element), LOGGER);
  }

  @Override
  public void materializeElement(final int index, @NotNull final IndexedAsyncConsumer<E> consumer) {
    final List<E> elements = this.elements;
    if (index < 0) {
      safeConsumeError(consumer, index, new IndexOutOfBoundsException(Integer.toString(index)),
          LOGGER);
    } else {
      final int size = elements.size();
      if (index >= size) {
        safeConsumeComplete(consumer, size, LOGGER);
      } else {
        safeConsume(consumer, size, index, elements.get(index), LOGGER);
      }
    }
  }

  @Override
  public void materializeElements(@NotNull final AsyncConsumer<List<E>> consumer) {
    safeConsume(consumer, elements, LOGGER);
  }

  @Override
  public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
    safeConsume(consumer, elements.isEmpty(), LOGGER);
  }

  @Override
  public void materializeOrdered(@NotNull final IndexedAsyncConsumer<E> consumer) {
    final List<E> elements = this.elements;
    final int size = elements.size();
    int i = 0;
    while (i < size) {
      if (!safeConsume(consumer, size, i, elements.get(i), LOGGER)) {
        return;
      }
      ++i;
    }
    safeConsumeComplete(consumer, size, LOGGER);
  }

  @Override
  public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
    safeConsume(consumer, elements.size(), LOGGER);
  }

  @Override
  public void materializeUnordered(@NotNull final IndexedAsyncConsumer<E> consumer) {
    materializeOrdered(consumer);
  }
}
