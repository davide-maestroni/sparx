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
import sparx.util.SizeOverflowException;
import sparx.util.annotation.NotNegative;

public class InsertAllAfterIteratorMaterializer<E> extends StatefulIteratorMaterializer<E> {

  public InsertAllAfterIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      @NotNegative final int numElements,
      @NotNull final IteratorMaterializer<E> elementsMaterializer) {
    setState(new ImmaterialState(wrapped, numElements, elementsMaterializer));
  }

  private class ImmaterialState implements IteratorMaterializer<E> {

    private final IteratorMaterializer<E> elementsMaterializer;
    private final int numElements;
    private final IteratorMaterializer<E> wrapped;

    private int pos;

    private ImmaterialState(@NotNull final IteratorMaterializer<E> wrapped, final int numElements,
        @NotNull final IteratorMaterializer<E> elementsMaterializer) {
      this.wrapped = wrapped;
      this.numElements = numElements;
      this.elementsMaterializer = elementsMaterializer;
    }

    @Override
    public int knownSize() {
      final int knownSize = wrapped.knownSize();
      if (knownSize >= 0) {
        if (knownSize >= numElements - pos) {
          final int elementsSize = elementsMaterializer.knownSize();
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
      final E next = wrapped.materializeNext();
      ++pos;
      return next;
    }

    @Override
    public int materializeSkip(final int count) {
      if (count > 0) {
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
          return skipped + wrapped.materializeSkip(count - skipped);
        }
        return skipped;
      }
      return 0;
    }

    @Override
    public int nextIndex() {
      return -1;
    }
  }
}
