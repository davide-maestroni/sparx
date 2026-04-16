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

import static java.util.Collections.unmodifiableList;

import java.util.List;
import java.util.NoSuchElementException;
import sparx1.internal.lazy.IteratorMaterializer;
import sparx1.internal.lazy.ListMaterializer;
import sparx1.util.DequeArrayList;
import sparx1.util.UncheckedException;
import sparx1.util.annotation.NotNull;
import sparx1.util.function.Functions;
import sparx1.util.function.IndexedFunction;

public class ReplaceSequenceIteratorMaterializer<E> extends StatefulIteratorMaterializer<E> {

  public ReplaceSequenceIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      final @NotNull ListMaterializer<?> elementsMaterializer,
      final @NotNull IndexedFunction<? super List<E>, IteratorMaterializer<E>> mapper) {
    setState(new InitialState(wrapped, elementsMaterializer, mapper));
  }

  private class InitialState extends AbstractStateIteratorMaterializer {

    private final DequeArrayList<E> elements = new DequeArrayList<E>(true);
    private final ListMaterializer<?> elementsMaterializer;
    private final IndexedFunction<? super List<E>, IteratorMaterializer<E>> mapper;
    private final IteratorMaterializer<E> wrapped;

    private boolean hasNext = false;
    private IteratorMaterializer<E> materializer;
    private int pos;

    private InitialState(final @NotNull IteratorMaterializer<E> wrapped,
        final @NotNull ListMaterializer<?> elementsMaterializer,
        final @NotNull IndexedFunction<? super List<E>, IteratorMaterializer<E>> mapper) {
      this.wrapped = wrapped;
      this.elementsMaterializer = elementsMaterializer;
      this.mapper = mapper;
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
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer != null) {
        if (materializer.materializeHasNext()) {
          return true;
        }
        this.materializer = null;
      } else if (hasNext) {
        return true;
      }
      final IteratorMaterializer<E> wrapped = this.wrapped;
      final DequeArrayList<E> wrappedElements = this.elements;
      final ListMaterializer<?> elementsMaterializer = this.elementsMaterializer;
      final int elementsSize = elementsMaterializer.materializeSize();
      while (elementsSize > 0) {
        int index = 0;
        boolean found = false;
        if (!wrappedElements.isEmpty()) {
          for (final E wrappedElement : wrappedElements) {
            if (!elementsMaterializer.canMaterializeElement(index)) {
              found = true;
              break;
            }
            final Object element = elementsMaterializer.materializeElement(index++);
            if (!Functions.objectsEqual(wrappedElement, element)) {
              return hasNext = true;
            }
          }
          if (found) {
            try {
              final IteratorMaterializer<E> nextMaterializer = mapper.apply(pos,
                  unmodifiableList(wrappedElements.subList(0, elementsSize)));
              wrappedElements.removeRange(0, elementsSize);
              if (nextMaterializer.materializeHasNext()) {
                this.materializer = nextMaterializer;
                return true;
              }
              continue;
            } catch (final Exception e) {
              throw UncheckedException.throwUnchecked(e);
            }
          }
        }
        while (wrapped.materializeHasNext()) {
          if (!elementsMaterializer.canMaterializeElement(index)) {
            found = true;
            break;
          }
          final E next = wrapped.materializeNext();
          wrappedElements.add(next);
          final Object element = elementsMaterializer.materializeElement(index++);
          if (!Functions.objectsEqual(next, element)) {
            return hasNext = true;
          }
        }
        if (found || !elementsMaterializer.canMaterializeElement(index)) {
          try {
            final IteratorMaterializer<E> nextMaterializer = mapper.apply(pos,
                unmodifiableList(wrappedElements.subList(0, elementsSize)));
            wrappedElements.removeRange(0, elementsSize);
            if (nextMaterializer.materializeHasNext()) {
              this.materializer = nextMaterializer;
              return true;
            }
            continue;
          } catch (final Exception e) {
            throw UncheckedException.throwUnchecked(e);
          }
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
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer != null) {
        return materializer.materializeNext();
      } else if (hasNext) {
        ++pos;
        hasNext = false;
        return elements.removeFirst();
      }
      ++pos;
      return getState().materializeNext();
    }
  }
}
