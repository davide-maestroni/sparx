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
import sparx1.util.SizeOverflowException;
import sparx1.util.annotation.NotNegative;
import sparx1.util.annotation.NotNull;
import sparx1.util.annotation.Positive;

public class InsertAfterIteratorMaterializer<E> extends StatefulIteratorMaterializer<E> {

  public InsertAfterIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      @NotNegative final int numElements, final E element) {
    setState(new ImmaterialState(wrapped, numElements, element));
  }

  private class ImmaterialState implements IteratorMaterializer<E> {

    private final E element;
    private final int numElements;
    private final IteratorMaterializer<E> wrapped;

    private int pos;

    private ImmaterialState(final @NotNull IteratorMaterializer<E> wrapped, final int numElements,
        final E element) {
      this.wrapped = wrapped;
      this.numElements = numElements;
      this.element = element;
    }

    @Override
    public int currentKnownSize() {
      final int knownSize = wrapped.currentKnownSize();
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
        setState(wrapped);
        return element;
      }
      ++pos;
      return wrapped.materializeNext();
    }

    @Override
    public int materializeSkip(@Positive final int count) {
      final int remaining = numElements - pos;
      if (count <= remaining) {
        final int skipped = wrapped.materializeSkip(count);
        this.pos += skipped;
        return skipped;
      }
      final IteratorMaterializer<E> wrapped = this.wrapped;
      int skipped = wrapped.materializeSkip(remaining);
      pos += skipped;
      if (skipped == remaining) {
        final IteratorMaterializer<E> state = setState(wrapped);
        final int toSkip = count - remaining - 1;
        return skipped + (toSkip > 0 ? state.materializeSkip(toSkip) : 0) + 1;
      }
      return skipped;
    }
  }
}
