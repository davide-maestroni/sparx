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
import sparx1.util.SizeOverflowException;
import sparx1.util.annotation.NotNegative;
import sparx1.util.annotation.NotNull;
import sparx1.util.annotation.Positive;

public class InsertAllAfterIteratorMaterializer<E> extends StatefulIteratorMaterializer<E> {

  public InsertAllAfterIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      final @NotNegative int numElements,
      final @NotNull IteratorMaterializer<E> elementsMaterializer) {
    setState(new ImmaterialState(wrapped, numElements, elementsMaterializer));
  }

  private class ImmaterialState implements IteratorMaterializer<E> {

    private final IteratorMaterializer<E> elementsMaterializer;
    private final int numElements;
    private final IteratorMaterializer<E> wrapped;

    private int pos;

    private ImmaterialState(final @NotNull IteratorMaterializer<E> wrapped, final int numElements,
        final @NotNull IteratorMaterializer<E> elementsMaterializer) {
      this.wrapped = wrapped;
      this.numElements = numElements;
      this.elementsMaterializer = elementsMaterializer;
    }

    @Override
    public int currentKnownSize() {
      final int knownSize = wrapped.currentKnownSize();
      if (knownSize >= 0) {
        if (knownSize >= numElements - pos) {
          final int elementsSize = elementsMaterializer.currentKnownSize();
          if (elementsSize >= 0) {
            return SizeOverflowException.safeCast((long) knownSize + elementsSize);
          }
        }
        return knownSize;
      }
      return -1;
    }

    @Override
    public boolean materializeHasNext() {
      return (pos == numElements && elementsMaterializer.materializeHasNext())
          || wrapped.materializeHasNext();
    }

    @Override
    public E materializeNext() {
      if (pos == numElements) {
        final IteratorMaterializer<E> elementsMaterializer = this.elementsMaterializer;
        if (elementsMaterializer.materializeHasNext()) {
          return elementsMaterializer.materializeNext();
        }
        return setState(wrapped).materializeNext();
      }
      ++pos;
      return wrapped.materializeNext();
    }

    @Override
    public int materializeSkip(final @Positive int count) {
      final int numElements = this.numElements;
      final int remaining = numElements - pos;
      if (count <= remaining) {
        final int skipped = wrapped.materializeSkip(count);
        pos += skipped;
        return skipped;
      }
      final IteratorMaterializer<E> wrapped = this.wrapped;
      int skipped = wrapped.materializeSkip(remaining);
      pos += skipped;
      if (skipped == remaining) {
        skipped += elementsMaterializer.materializeSkip(count - skipped);
        if (count > skipped) {
          return skipped + wrapped.materializeSkip(count - skipped);
        }
      }
      return skipped;
    }
  }
}
