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
package sparx.collection.internal.iterator;

import org.jetbrains.annotations.NotNull;
import sparx.util.Require;
import sparx.util.UncheckedException;
import sparx.util.function.IndexedConsumer;

public class PeekIteratorMaterializer<E> implements IteratorMaterializer<E> {

  private final IndexedConsumer<? super E> consumer;
  private final IteratorMaterializer<E> wrapped;

  private int pos;

  public PeekIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      @NotNull final IndexedConsumer<? super E> consumer) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
    this.consumer = Require.notNull(consumer, "consumer");
  }

  @Override
  public int knownSize() {
    return wrapped.knownSize();
  }

  @Override
  public boolean materializeHasNext() {
    return wrapped.materializeHasNext();
  }

  @Override
  public E materializeNext() {
    try {
      final E next = wrapped.materializeNext();
      consumer.accept(pos++, next);
      return next;
    } catch (final Exception e) {
      throw UncheckedException.throwUnchecked(e);
    }
  }

  @Override
  public int materializeSkip(final int count) {
    return wrapped.materializeSkip(count);
  }
}
