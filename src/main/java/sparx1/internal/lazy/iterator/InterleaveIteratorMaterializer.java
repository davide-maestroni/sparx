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
import sparx1.util.annotation.NotNull;

public class InterleaveIteratorMaterializer<E> extends StatefulIteratorMaterializer<E> {

  public InterleaveIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      final @NotNull IteratorMaterializer<E> elementsMaterializer) {
    setState(new ImmaterialState(wrapped, elementsMaterializer));
  }

  private class ImmaterialState extends AbstractIteratorMaterializer<E> {

    private final IteratorMaterializer<E> elementsMaterializer;
    private final IteratorMaterializer<E> wrapped;

    private boolean isWrapped = true;

    private ImmaterialState(final @NotNull IteratorMaterializer<E> wrapped,
        final @NotNull IteratorMaterializer<E> elementsMaterializer) {
      this.wrapped = wrapped;
      this.elementsMaterializer = elementsMaterializer;
    }

    @Override
    public int currentKnownSize() {
      final int knownSize = wrapped.currentKnownSize();
      if (knownSize >= 0) {
        final int elementsSize = elementsMaterializer.currentKnownSize();
        if (elementsSize >= 0) {
          return isWrapped ? Math.max(knownSize, elementsSize) * 2
              : (Math.max(knownSize + 1, elementsSize) * 2) - 1;
        }
      }
      return -1;
    }

    @Override
    public boolean materializeHasNext() {
      if (isWrapped ? wrapped.materializeHasNext() && elementsMaterializer.materializeHasNext()
          : elementsMaterializer.materializeHasNext()) {
        return true;
      }
      setEmptyState();
      return false;
    }

    @Override
    public E materializeNext() {
      final boolean isWrapped = this.isWrapped;
      final E next;
      if (isWrapped) {
        if (elementsMaterializer.materializeHasNext()) {
          next = wrapped.materializeNext();
        } else {
          throw new NoSuchElementException();
        }
      } else {
        next = elementsMaterializer.materializeNext();
      }
      this.isWrapped = !isWrapped;
      return next;
    }
  }
}
