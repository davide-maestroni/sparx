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
import sparx.util.function.IndexedFunction;

public class MapAfterIteratorMaterializer<E> implements IteratorMaterializer<E> {

  private volatile IteratorMaterializer<E> state;

  // numElements: not negative
  public MapAfterIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      final int numElements, @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
    state = new ImmaterialState(wrapped, numElements, mapper);
  }

  @Override
  public int knownSize() {
    return state.knownSize();
  }

  @Override
  public boolean materializeHasNext() {
    return state.materializeHasNext();
  }

  @Override
  public E materializeNext() {
    return state.materializeNext();
  }

  @Override
  public int materializeSkip(final int count) {
    return state.materializeSkip(count);
  }

  private class ImmaterialState implements IteratorMaterializer<E> {

    private final IndexedFunction<? super E, ? extends E> mapper;
    private final int numElements;
    private final IteratorMaterializer<E> wrapped;

    private int pos;

    private ImmaterialState(@NotNull final IteratorMaterializer<E> wrapped, final int numElements,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      this.wrapped = wrapped;
      this.numElements = numElements;
      this.mapper = mapper;
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
      final int pos = this.pos;
      if (pos == numElements) {
        try {
          return mapper.apply(pos, (state = wrapped).materializeNext());
        } catch (final Exception e) {
          throw UncheckedException.throwUnchecked(e);
        }
      }
      final E next = wrapped.materializeNext();
      ++this.pos;
      return next;
    }

    @Override
    public int materializeSkip(final int count) {
      if (count > 0) {
        final int numElements = this.numElements;
        final int pos = this.pos;
        if (count <= numElements - pos) {
          final int skipped = wrapped.materializeSkip(count);
          this.pos += skipped;
          return skipped;
        }
        return (state = wrapped).materializeSkip(count);
      }
      return 0;
    }
  }
}
