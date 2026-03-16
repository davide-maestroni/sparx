/*
 * Copyright 2026 Davide Maestroni
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
import sparx1.util.function.IndexedFunction;
import sparx1.util.function.IndexedPredicate;

public class MapWhileIteratorMaterializer<E> extends StatefulIteratorMaterializer<E> {

  public MapWhileIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      final @NotNull IndexedPredicate<? super E> condition,
      final @NotNull IndexedFunction<? super E, ? extends E> mapper) {
    setState(new ImmaterialState(wrapped, condition, mapper));
  }

  private class ImmaterialState implements IteratorMaterializer<E> {

    private final IndexedPredicate<? super E> condition;
    private final IndexedFunction<? super E, ? extends E> mapper;
    private final IteratorMaterializer<E> wrapped;

    private int pos;

    private ImmaterialState(final @NotNull IteratorMaterializer<E> wrapped,
        final @NotNull IndexedPredicate<? super E> condition,
        final @NotNull IndexedFunction<? super E, ? extends E> mapper) {
      this.wrapped = wrapped;
      this.condition = condition;
      this.mapper = mapper;
    }

    @Override
    public int currentKnownSize() {
      return wrapped.currentKnownSize();
    }

    @Override
    public boolean materializeHasNext() {
      return wrapped.materializeHasNext();
    }

    @Override
    public E materializeNext() {
      try {
        final IteratorMaterializer<E> wrapped = this.wrapped;
        final E next = wrapped.materializeNext();
        if (this.condition.test(pos, next)) {
          return mapper.apply(pos++, wrapped.materializeNext());
        }
        setState(wrapped);
        return next;
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public int materializeSkip(@Positive final int count) {
      final int skipped = wrapped.materializeSkip(count);
      pos += skipped;
      return skipped;
    }
  }
}
