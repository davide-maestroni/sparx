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
import sparx1.util.SizeOverflowException;
import sparx1.util.annotation.NotNull;
import sparx1.util.function.Functions;

public class RemoveLastSequenceIteratorMaterializer<E> extends StatefulIteratorMaterializer<E> {

  public RemoveLastSequenceIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
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
              return setState(new FoundState(wrapped, elementsMaterializer,
                  wrappedElements)).materializeHasNext();
            }
            final Object element = elementsMaterializer.materializeElement(index++);
            if (!Functions.objectsEqual(wrappedElement, element)) {
              return hasNext = true;
            }
          }
        }
        while (wrapped.materializeHasNext()) {
          if (!elementsMaterializer.canMaterializeElement(index)) {
            return setState(new FoundState(wrapped, elementsMaterializer,
                wrappedElements)).materializeHasNext();
          }
          final E next = wrapped.materializeNext();
          wrappedElements.add(next);
          final Object element = elementsMaterializer.materializeElement(index++);
          if (!Functions.objectsEqual(next, element)) {
            return hasNext = true;
          }
        }
        if (!elementsMaterializer.canMaterializeElement(index)) {
          return setState(
              new FoundState(wrapped, elementsMaterializer, wrappedElements)).materializeHasNext();
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

  private class FoundState extends AbstractStateIteratorMaterializer {

    private final DequeArrayList<E> elements;
    private final ListMaterializer<?> elementsMaterializer;
    private final IteratorMaterializer<E> wrapped;

    private FoundState(final @NotNull IteratorMaterializer<E> wrapped,
        final @NotNull ListMaterializer<?> elementsMaterializer,
        final @NotNull DequeArrayList<E> elements) {
      this.wrapped = wrapped;
      this.elementsMaterializer = elementsMaterializer;
      this.elements = elements;
    }

    @Override
    public int currentKnownSize() {
      final int knownSize = wrapped.currentKnownSize();
      if (knownSize >= 0) {
        final long elementsKnownSize = elementsMaterializer.knownSize();
        if (elementsKnownSize >= 0) {
          return SizeOverflowException.safeCast(knownSize + elements.size() - elementsKnownSize);
        }
      }
      return -1;
    }

    @Override
    public boolean isSizeKnown() {
      return wrapped.isSizeKnown() && elementsMaterializer.isSizeKnown();
    }

    @Override
    public boolean materializeHasNext() {
      final DequeArrayList<E> wrappedElements = this.elements;
      final ListMaterializer<?> elementsMaterializer = this.elementsMaterializer;
      final int elementsSize = elementsMaterializer.materializeSize();
      if (wrappedElements.size() > elementsSize) {
        return true;
      }
      final IteratorMaterializer<E> wrapped = this.wrapped;
      int pos = 1;
      int index = 0;
      while (true) {
        boolean needsNext = true;
        for (int i = pos; i < wrappedElements.size(); ++i) {
          final E wrappedElement = wrappedElements.get(i);
          final Object element = elementsMaterializer.materializeElement(index++);
          if (!Functions.objectsEqual(wrappedElement, element)) {
            ++pos;
            index = 0;
            needsNext = false;
            break;
          }
        }
        if (needsNext) {
          while (wrapped.materializeHasNext()) {
            if (!elementsMaterializer.canMaterializeElement(index)) {
              return true;
            }
            final E next = wrapped.materializeNext();
            wrappedElements.add(next);
            if (!Functions.objectsEqual(next, elementsMaterializer.materializeElement(index++))) {
              ++pos;
              index = 0;
              break;
            }
          }
          if (index > 0) {
            if (!elementsMaterializer.canMaterializeElement(index)) {
              final int size = wrappedElements.size();
              wrappedElements.removeRange(size - elementsSize, size);
            } else {
              wrappedElements.removeRange(0, elementsSize);
            }
            if (wrappedElements.isEmpty()) {
              setEmptyState();
              return false;
            }
            return setState(
                new DequeToIteratorMaterializer<E>(wrappedElements)).materializeHasNext();
          }
        }
      }
    }

    @Override
    public E materializeNext() {
      if (!materializeHasNext()) {
        throw new NoSuchElementException();
      }
      final IteratorMaterializer<E> state = getState();
      if (state == this) {
        return elements.removeFirst();
      }
      return state.materializeNext();
    }
  }
}
