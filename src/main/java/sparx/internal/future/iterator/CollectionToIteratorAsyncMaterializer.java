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

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CancellationException;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.internal.future.AsyncConsumer;
import sparx.internal.future.IndexedAsyncConsumer;

public class CollectionToIteratorAsyncMaterializer<E> implements IteratorAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      CollectionToIteratorAsyncMaterializer.class.getName());

  private final Collection<E> elements;
  private final Iterator<E> iterator;

  private int index;

  public CollectionToIteratorAsyncMaterializer(@NotNull final Collection<E> elements) {
    this.elements = elements;
    iterator = elements.iterator();
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
    return elements.size();
  }

  @Override
  public void materializeCancel(@NotNull final CancellationException exception) {
  }

  @Override
  public void materializeEach(@NotNull final IndexedAsyncConsumer<E> consumer) {
    final Collection<E> elements = this.elements;
    final Iterator<E> iterator = this.iterator;
    while (iterator.hasNext()) {
      if (!safeConsume(consumer, elements.size(), index++, iterator.next(), LOGGER)) {
        return;
      }
    }
    safeConsumeComplete(consumer, elements.size(), LOGGER);
  }

  @Override
  public void materializeHasNext(@NotNull final AsyncConsumer<Boolean> consumer) {
    safeConsume(consumer, iterator.hasNext(), LOGGER);
  }

  @Override
  public void materializeNext(@NotNull final IndexedAsyncConsumer<E> consumer) {
    final Iterator<E> iterator = this.iterator;
    if (iterator.hasNext()) {
      safeConsume(consumer, elements.size(), index++, iterator.next(), LOGGER);
    } else {
      safeConsumeComplete(consumer, elements.size(), LOGGER);
    }
  }

  @Override
  public void materializeSkip(final int count, @NotNull final AsyncConsumer<Integer> consumer) {
    if (count <= 0) {
      safeConsume(consumer, 0, LOGGER);
    } else {
      int skipped = 0;
      final Iterator<E> iterator = this.iterator;
      for (int i = 0; i < count && iterator.hasNext(); ++i) {
        ++skipped;
        iterator.next();
      }
      index += skipped;
      safeConsume(consumer, skipped, LOGGER);
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
    return Math.max(1, count);
  }
}
