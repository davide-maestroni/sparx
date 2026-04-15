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
import sparx1.util.annotation.Positive;

public class IncludesSequenceIteratorMaterializer<E> extends StatefulIteratorMaterializer<Boolean> {

  public IncludesSequenceIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      final @NotNull ListMaterializer<?> elementsMaterializer) {
    setState(new InitialState(wrapped, elementsMaterializer));
  }

  private class InitialState implements IteratorMaterializer<Boolean> {

    private final ListMaterializer<?> elementsMaterializer;
    private final IteratorMaterializer<E> wrapped;

    private InitialState(final @NotNull IteratorMaterializer<E> wrapped,
        final @NotNull ListMaterializer<?> elementsMaterializer) {
      this.wrapped = wrapped;
      this.elementsMaterializer = elementsMaterializer;
    }

    @Override
    public int currentKnownSize() {
      return 1;
    }

    @Override
    public boolean isSizeKnown() {
      return true;
    }

    @Override
    public boolean materializeHasNext() {
      final IteratorMaterializer<E> wrapped = this.wrapped;
      final ListMaterializer<?> elementsMaterializer = this.elementsMaterializer;
      final int elementsSize = elementsMaterializer.materializeSize();
      if (elementsSize > 0) {
        final DequeArrayList<E> wrappedElements = new DequeArrayList<E>();
        int index = 0;
        while (wrapped.materializeHasNext()) {
          if (!elementsMaterializer.canMaterializeElement(index)) {
            setState(new ElementToIteratorMaterializer<Boolean>(true));
            return true;
          }
          while (!wrappedElements.isEmpty()) {
            if (!elementsMaterializer.canMaterializeElement(index)) {
              setState(new ElementToIteratorMaterializer<Boolean>(true));
              return true;
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
            setState(new ElementToIteratorMaterializer<Boolean>(true));
            return true;
          }
          final E next = wrapped.materializeNext();
          final Object element = elementsMaterializer.materializeElement(index++);
          wrappedElements.add(next);
          if (next == element || (next != null && next.equals(element))) {
            continue;
          }
          wrappedElements.removeFirst();
          index = 0;
        }
        setState(new ElementToIteratorMaterializer<Boolean>(false));
        return true;
      }
      setState(new ElementToIteratorMaterializer<Boolean>(true));
      return true;
    }

    @Override
    public Boolean materializeNext() {
      if (!materializeHasNext()) {
        throw new NoSuchElementException();
      }
      return getState().materializeNext();
    }

    @Override
    public int materializeSkip(final @Positive int count) {
      setEmptyState();
      return 1;
    }
  }
}
