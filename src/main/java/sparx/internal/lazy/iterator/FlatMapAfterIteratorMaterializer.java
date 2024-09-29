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
package sparx.internal.lazy.iterator;

import org.jetbrains.annotations.NotNull;
import sparx.util.UncheckedException;
import sparx.util.annotation.NotNegative;
import sparx.util.annotation.Positive;
import sparx.util.function.IndexedFunction;

public class FlatMapAfterIteratorMaterializer<E> extends StatefulIteratorMaterializer<E> {

  public FlatMapAfterIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      @NotNegative final int numElements,
      @NotNull final IndexedFunction<? super E, ? extends IteratorMaterializer<E>> mapper) {
    setState(new ImmaterialState(wrapped, numElements, mapper));
  }

  private class ImmaterialState implements IteratorMaterializer<E> {

    private final IndexedFunction<? super E, ? extends IteratorMaterializer<E>> mapper;
    private final int numElements;
    private final IteratorMaterializer<E> wrapped;

    private int pos;

    private ImmaterialState(@NotNull final IteratorMaterializer<E> wrapped, final int numElements,
        @NotNull final IndexedFunction<? super E, ? extends IteratorMaterializer<E>> mapper) {
      this.wrapped = wrapped;
      this.numElements = numElements;
      this.mapper = mapper;
    }

    @Override
    public int knownSize() {
      return -1;
    }

    @Override
    public boolean materializeHasNext() {
      return materializer().materializeHasNext();
    }

    @Override
    public E materializeNext() {
      final IteratorMaterializer<E> materializer = materializer();
      ++pos;
      return materializer.materializeNext();
    }

    @Override
    public int materializeSkip(@Positive final int count) {
      final int numElements = this.numElements;
      final int pos = this.pos;
      final int remaining = numElements - pos;
      if (count <= remaining) {
        final int skipped = wrapped.materializeSkip(count);
        this.pos += skipped;
        return skipped;
      }
      int skipped = wrapped.materializeSkip(remaining);
      this.pos += skipped;
      if (skipped == remaining) {
        return skipped + materializer().materializeSkip(count - skipped);
      }
      return skipped;
    }

    private @NotNull IteratorMaterializer<E> materializer() {
      final IteratorMaterializer<E> wrapped = this.wrapped;
      final int pos = this.pos;
      if (pos == numElements) {
        try {
          if (wrapped.materializeHasNext()) {
            final IteratorMaterializer<E> materializer = mapper.apply(pos,
                wrapped.materializeNext());
            return setState(new AppendAllIteratorMaterializer<E>(materializer, wrapped));
          }
        } catch (final Exception e) {
          setState(wrapped);
          throw UncheckedException.throwUnchecked(e);
        }
      }
      return wrapped;
    }
  }
}
