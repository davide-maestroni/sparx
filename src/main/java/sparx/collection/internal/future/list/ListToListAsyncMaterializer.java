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
package sparx.collection.internal.future.list;

import java.util.List;
import java.util.logging.Level;
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
  public boolean cancel(final boolean mayInterruptIfRunning) {
    return false;
  }

  @Override
  public boolean knownEmpty() {
    try {
      return elements.isEmpty();
    } catch (final Exception e) {
      LOGGER.log(Level.SEVERE, "Ignored exception", e);
    }
    return false;
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
  @SuppressWarnings("SuspiciousMethodCalls")
  public void materializeContains(final Object element,
      @NotNull final AsyncConsumer<Boolean> consumer) {
    boolean contains;
    try {
      contains = elements.contains(element);
    } catch (final Exception e) {
      safeConsumeError(consumer, e, LOGGER);
      return;
    }
    safeConsume(consumer, contains, LOGGER);
  }

  @Override
  public void materializeElement(final int index, @NotNull final IndexedAsyncConsumer<E> consumer) {
    final List<E> elements = this.elements;
    if (index < 0) {
      safeConsumeError(consumer, index, new IndexOutOfBoundsException(Integer.toString(index)),
          LOGGER);
    } else {
      try {
        final int size = elements.size();
        if (index >= size) {
          safeConsumeComplete(consumer, size, LOGGER);
        } else {
          safeConsume(consumer, size, index, elements.get(index), LOGGER);
        }
      } catch (final Exception e) {
        safeConsumeError(consumer, index, e, LOGGER);
      }
    }
  }

  @Override
  public void materializeElements(@NotNull final AsyncConsumer<List<E>> consumer) {
    safeConsume(consumer, elements, LOGGER);
  }

  @Override
  public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
    boolean empty;
    try {
      empty = elements.isEmpty();
    } catch (final Exception e) {
      safeConsumeError(consumer, e, LOGGER);
      return;
    }
    safeConsume(consumer, empty, LOGGER);
  }

  @Override
  public void materializeOrdered(@NotNull final IndexedAsyncConsumer<E> consumer) {
    int i = 0;
    try {
      final List<E> elements = this.elements;
      final int size = elements.size();
      while (i < size) {
        if (!safeConsume(consumer, size, i, elements.get(i), LOGGER)) {
          return;
        }
        ++i;
      }
      safeConsumeComplete(consumer, size, LOGGER);
    } catch (final Exception e) {
      safeConsumeError(consumer, i, e, LOGGER);
    }
  }

  @Override
  public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
    int size;
    try {
      size = elements.size();
    } catch (final Exception e) {
      safeConsumeError(consumer, e, LOGGER);
      return;
    }
    safeConsume(consumer, size, LOGGER);
  }

  @Override
  public void materializeUnordered(@NotNull final IndexedAsyncConsumer<E> consumer) {
    materializeOrdered(consumer);
  }
}
