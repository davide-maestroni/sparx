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

import java.util.NoSuchElementException;
import sparx1.internal.lazy.IteratorMaterializer;
import sparx1.util.UncheckedException;
import sparx1.util.annotation.NotNull;
import sparx1.util.annotation.Positive;
import sparx1.util.function.IndexedFunction;
import sparx1.util.function.IndexedPredicate;

public class FlatMapWhileIteratorMaterializer<E> extends StatefulIteratorMaterializer<E> {

  public FlatMapWhileIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      final @NotNull IndexedPredicate<? super E> condition,
      final @NotNull IndexedFunction<? super E, ? extends IteratorMaterializer<? extends E>> mapper) {
    setState(new ImmaterialState(wrapped, condition, mapper));
  }

  private class ImmaterialState implements IteratorMaterializer<E> {

    private final IndexedPredicate<? super E> condition;
    private final IndexedFunction<? super E, ? extends IteratorMaterializer<? extends E>> mapper;
    private final IteratorMaterializer<E> wrapped;

    private IteratorMaterializer<? extends E> materializer = EmptyIteratorMaterializer.instance();
    private int pos;

    private ImmaterialState(final @NotNull IteratorMaterializer<E> wrapped,
        final @NotNull IndexedPredicate<? super E> condition,
        final @NotNull IndexedFunction<? super E, ? extends IteratorMaterializer<? extends E>> mapper) {
      this.wrapped = wrapped;
      this.condition = condition;
      this.mapper = mapper;
    }

    @Override
    public int currentKnownSize() {
      return -1;
    }

    @Override
    public boolean materializeHasNext() {
      if (materializer.materializeHasNext()) {
        return true;
      }
      try {
        final IteratorMaterializer<E> wrapped = this.wrapped;
        final IndexedFunction<? super E, ? extends IteratorMaterializer<? extends E>> mapper = this.mapper;
        while (wrapped.materializeHasNext()) {
          final E next = wrapped.materializeNext();
          if (this.condition.test(pos, next)) {
            final IteratorMaterializer<? extends E> materializer = mapper.apply(pos++, next);
            if (materializer.materializeHasNext()) {
              this.materializer = materializer;
              return true;
            }
          } else {
            setState(wrapped);
            return false;
          }
        }
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
      return false;
    }

    @Override
    public E materializeNext() {
      if (!materializeHasNext()) {
        throw new NoSuchElementException();
      }
      return materializer.materializeNext();
    }

    @Override
    public int materializeSkip(@Positive final int count) {
      int skipped = 0;
      while (skipped < count && materializeHasNext()) {
        skipped += materializer.materializeSkip(count - skipped);
      }
      return skipped;
    }
  }
}
