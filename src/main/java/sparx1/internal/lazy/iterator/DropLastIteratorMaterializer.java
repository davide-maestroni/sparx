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
import sparx1.util.DequeArrayList;
import sparx1.util.annotation.NotNull;
import sparx1.util.annotation.Positive;

public class DropLastIteratorMaterializer<E> extends StatefulIteratorMaterializer<E> {

  public DropLastIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      final @Positive int maxElements) {
    setState(new InitialState(wrapped, maxElements));
  }

  private class InitialState extends AbstractIteratorMaterializer<E> {

    private final DequeArrayList<E> elements = new DequeArrayList<E>();
    private final int maxElements;
    private final IteratorMaterializer<E> wrapped;

    private InitialState(final @NotNull IteratorMaterializer<E> wrapped, final int maxElements) {
      this.wrapped = wrapped;
      this.maxElements = maxElements;
    }

    @Override
    public int currentKnownSize() {
      final int knownSize = wrapped.currentKnownSize();
      if (knownSize >= 0) {
        return Math.max(0, knownSize - maxElements);
      }
      return -1;
    }

    @Override
    public boolean isSizeKnown() {
      return wrapped.isSizeKnown();
    }

    @Override
    public boolean materializeHasNext() {
      final DequeArrayList<E> elements = this.elements;
      final IteratorMaterializer<E> wrapped = this.wrapped;
      if (elements.isEmpty()) {
        final int maxElements = this.maxElements;
        while (elements.size() < maxElements && wrapped.materializeHasNext()) {
          elements.add(wrapped.materializeNext());
        }
        if (elements.size() < maxElements) {
          setEmptyState();
          return false;
        }
      }
      return wrapped.materializeHasNext();
    }

    @Override
    public E materializeNext() {
      if (!materializeHasNext()) {
        throw new NoSuchElementException();
      }
      final DequeArrayList<E> elements = this.elements;
      elements.add(wrapped.materializeNext());
      return elements.removeFirst();
    }
  }
}
