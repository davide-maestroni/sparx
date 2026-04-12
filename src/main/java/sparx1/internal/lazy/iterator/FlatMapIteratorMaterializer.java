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

import java.util.NoSuchElementException;
import sparx1.internal.lazy.IteratorMaterializer;
import sparx1.util.UncheckedException;
import sparx1.util.annotation.NotNull;
import sparx1.util.annotation.Positive;
import sparx1.util.function.IndexedFunction;

public class FlatMapIteratorMaterializer<E, F> extends StatefulIteratorMaterializer<F> {

  public FlatMapIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      final @NotNull IndexedFunction<? super E, ? extends IteratorMaterializer<F>> mapper) {
    setState(new InitialState(wrapped, mapper));
  }

  private class InitialState implements IteratorMaterializer<F> {

    private final IndexedFunction<? super E, ? extends IteratorMaterializer<F>> mapper;
    private final IteratorMaterializer<E> wrapped;

    private IteratorMaterializer<F> materializer = EmptyIteratorMaterializer.instance();
    private int pos;

    private InitialState(final @NotNull IteratorMaterializer<E> wrapped,
        final @NotNull IndexedFunction<? super E, ? extends IteratorMaterializer<F>> mapper) {
      this.wrapped = wrapped;
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
        final IndexedFunction<? super E, ? extends IteratorMaterializer<F>> mapper = this.mapper;
        while (wrapped.materializeHasNext()) {
          final IteratorMaterializer<F> materializer = mapper.apply(pos++,
              wrapped.materializeNext());
          if (materializer.materializeHasNext()) {
            this.materializer = materializer;
            return true;
          }
        }
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
      setEmptyState();
      return false;
    }

    @Override
    public F materializeNext() {
      if (!materializeHasNext()) {
        throw new NoSuchElementException();
      }
      return materializer.materializeNext();
    }

    @Override
    public int materializeSkip(final @Positive int count) {
      int skipped = 0;
      while (skipped < count && materializeHasNext()) {
        skipped += materializer.materializeSkip(count - skipped);
      }
      return skipped;
    }
  }
}
