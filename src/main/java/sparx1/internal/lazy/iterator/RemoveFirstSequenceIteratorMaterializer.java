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
import sparx1.internal.lazy.ListMaterializer;
import sparx1.util.DequeArrayList;
import sparx1.util.annotation.NotNull;
import sparx1.util.function.Functions;

public class RemoveFirstSequenceIteratorMaterializer<E> extends StatefulIteratorMaterializer<E> {

  public RemoveFirstSequenceIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      final @NotNull ListMaterializer<?> elementsMaterializer) {
    setState(new InitialState(wrapped, elementsMaterializer));
  }

  private class InitialState extends AbstractStateIteratorMaterializer {

    private final DequeArrayList<E> elements = new DequeArrayList<E>(true);
    private final ListMaterializer<?> elementsMaterializer;
    private final IteratorMaterializer<E> wrapped;

    private boolean hasNext = false;

    private InitialState(final @NotNull IteratorMaterializer<E> wrapped,
        final @NotNull ListMaterializer<?> elementsMaterializer) {
      this.wrapped = wrapped;
      this.elementsMaterializer = elementsMaterializer;
    }

    @Override
    public int currentKnownSize() {
      return -1;
    }

    @Override
    public boolean isSizeKnown() {
      return false;
    }

    @Override
    public boolean materializeHasNext() {
      if (hasNext) {
        return true;
      }
      final IteratorMaterializer<E> wrapped = this.wrapped;
      final DequeArrayList<E> wrappedElements = this.elements;
      final ListMaterializer<?> elementsMaterializer = this.elementsMaterializer;
      final int elementsSize = elementsMaterializer.materializeSize();
      if (elementsSize > 0) {
        int index = 0;
        if (!wrappedElements.isEmpty()) {
          for (final E wrappedElement : wrappedElements) {
            if (!elementsMaterializer.canMaterializeElement(index)) {
              return setState(wrapped).materializeHasNext();
            }
            final Object element = elementsMaterializer.materializeElement(index++);
            if (!Functions.objectsEqual(wrappedElement, element)) {
              return hasNext = true;
            }
          }
        }
        while (wrapped.materializeHasNext()) {
          if (!elementsMaterializer.canMaterializeElement(index)) {
            return setState(wrapped).materializeHasNext();
          }
          final E next = wrapped.materializeNext();
          wrappedElements.add(next);
          final Object element = elementsMaterializer.materializeElement(index++);
          if (!Functions.objectsEqual(next, element)) {
            return hasNext = true;
          }
        }
        if (!elementsMaterializer.canMaterializeElement(index)) {
          return setState(wrapped).materializeHasNext();
        }
        return setState(new DequeToIteratorMaterializer<E>(wrappedElements)).materializeHasNext();
      }
      return setState(wrapped).materializeHasNext();
    }

    @Override
    public E materializeNext() {
      if (!materializeHasNext()) {
        throw new NoSuchElementException();
      }
      if (hasNext) {
        hasNext = false;
        return elements.removeFirst();
      }
      return getState().materializeNext();
    }
  }
}
