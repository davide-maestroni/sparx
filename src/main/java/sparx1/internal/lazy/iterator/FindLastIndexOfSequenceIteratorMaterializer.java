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

import java.util.NoSuchElementException;
import sparx1.internal.lazy.IteratorMaterializer;
import sparx1.internal.lazy.ListMaterializer;
import sparx1.util.DequeArrayList;
import sparx1.util.annotation.NotNull;
import sparx1.util.annotation.Positive;

public class FindLastIndexOfSequenceIteratorMaterializer<E> extends
    StatefulIteratorMaterializer<Integer> {

  public FindLastIndexOfSequenceIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      final @NotNull ListMaterializer<?> elementsMaterializer) {
    setState(new ImmaterialState(wrapped, elementsMaterializer));
  }

  private class ImmaterialState implements IteratorMaterializer<Integer> {

    private final ListMaterializer<?> elementsMaterializer;
    private final IteratorMaterializer<E> wrapped;

    private ImmaterialState(final @NotNull IteratorMaterializer<E> wrapped,
        final @NotNull ListMaterializer<?> elementsMaterializer) {
      this.wrapped = wrapped;
      this.elementsMaterializer = elementsMaterializer;
    }

    @Override
    public int currentKnownSize() {
      return -1;
    }

    @Override
    public boolean materializeHasNext() {
      final IteratorMaterializer<E> wrapped = this.wrapped;
      final ListMaterializer<?> elementsMaterializer = this.elementsMaterializer;
      final int elementsSize = elementsMaterializer.materializeSize();
      boolean found = false;
      int last = 0;
      int pos = 0;
      if (elementsSize > 0) {
        final DequeArrayList<E> wrappedElements = new DequeArrayList<E>();
        int index = 0;
        while (wrapped.materializeHasNext()) {
          if (!elementsMaterializer.canMaterializeElement(index)) {
            last = pos;
            found = true;
            index = 0;
          }
          while (!wrappedElements.isEmpty()) {
            if (!elementsMaterializer.canMaterializeElement(index)) {
              last = pos;
              found = true;
              index = 0;
            }
            for (final E next : wrappedElements) {
              final Object element = elementsMaterializer.materializeElement(index++);
              if (next == element || (next != null && next.equals(element))) {
                continue;
              }
              wrappedElements.removeFirst();
              index = 0;
              break;
            }
          }
          if (!elementsMaterializer.canMaterializeElement(index)) {
            last = pos;
            found = true;
            index = 0;
          }
          ++pos;
          final E next = wrapped.materializeNext();
          final Object element = elementsMaterializer.materializeElement(index++);
          wrappedElements.add(next);
          if (next == element || (next != null && next.equals(element))) {
            continue;
          }
          wrappedElements.removeFirst();
          index = 0;
        }
        if (found) {
          setState(new ElementToIteratorMaterializer<Integer>(last));
          return true;
        }
        setEmptyState();
        return false;
      }
      final int size = materializeSkip(Integer.MAX_VALUE);
      setState(new ElementToIteratorMaterializer<Integer>(size));
      return true;
    }

    @Override
    public Integer materializeNext() {
      if (!materializeHasNext()) {
        throw new NoSuchElementException();
      }
      return getState().materializeNext();
    }

    @Override
    public int materializeSkip(@Positive final int count) {
      if (materializeHasNext()) {
        setEmptyState();
        return 1;
      }
      return 0;
    }
  }
}
