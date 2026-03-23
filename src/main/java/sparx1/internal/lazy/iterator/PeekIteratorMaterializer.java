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
package sparx1.internal.lazy.iterator;

import sparx1.internal.lazy.IteratorMaterializer;
import sparx1.util.UncheckedException;
import sparx1.util.annotation.NotNull;
import sparx1.util.annotation.Positive;
import sparx1.util.function.IndexedConsumer;

public class PeekIteratorMaterializer<E> extends StatefulIteratorMaterializer<E> {

  public PeekIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      final @NotNull IndexedConsumer<? super E> consumer) {
    setState(new ImmaterialState(wrapped, consumer));
  }

  private class ImmaterialState implements IteratorMaterializer<E> {

    private final IndexedConsumer<? super E> consumer;
    private final IteratorMaterializer<E> wrapped;

    private int pos;

    private ImmaterialState(final @NotNull IteratorMaterializer<E> wrapped,
        final @NotNull IndexedConsumer<? super E> consumer) {
      this.wrapped = wrapped;
      this.consumer = consumer;
    }

    @Override
    public int currentKnownSize() {
      return wrapped.currentKnownSize();
    }

    @Override
    public boolean materializeHasNext() {
      if (!wrapped.materializeHasNext()) {
        setEmptyState();
        return false;
      }
      return true;
    }

    @Override
    public E materializeNext() {
      final E next = wrapped.materializeNext();
      final int index = pos++;
      try {
        consumer.accept(index, next);
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
      return next;
    }

    @Override
    public int materializeSkip(final @Positive int count) {
      final int skipped = wrapped.materializeSkip(count);
      pos += skipped;
      return skipped;
    }
  }
}
