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
import sparx.util.SizeOverflowException;

public class InsertAfterIteratorMaterializer<E> implements IteratorMaterializer<E> {

  private volatile IteratorMaterializer<E> state;

  public InsertAfterIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      final int numElements, final E element) {
    state = new ImmaterialState(Require.notNull(wrapped, "wrapped"),
        Require.notNegative(numElements, "numElements"), element);
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

    private final E element;
    private final int numElements;
    private final IteratorMaterializer<E> wrapped;

    private int pos;

    private ImmaterialState(@NotNull final IteratorMaterializer<E> wrapped, final int numElements,
        final E element) {
      this.wrapped = wrapped;
      this.numElements = numElements;
      this.element = element;
    }

    @Override
    public int knownSize() {
      final int knownSize = wrapped.knownSize();
      if (knownSize >= 0) {
        if (knownSize >= numElements - pos) {
          return SizeOverflowException.safeCast((long) knownSize + 1);
        }
        return knownSize;
      }
      return -1;
    }

    @Override
    public boolean materializeHasNext() {
      return pos == numElements || wrapped.materializeHasNext();
    }

    @Override
    public E materializeNext() {
      if (pos == numElements) {
        state = wrapped;
        return element;
      }
      ++pos;
      return wrapped.materializeNext();
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
        return (state = wrapped).materializeSkip(count - 1) + 1;
      }
      return 0;
    }
  }
}
