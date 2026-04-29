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
package sparx1.internal.lazy.list;

import java.util.ArrayList;
import java.util.Iterator;
import sparx1.internal.lazy.ListMaterializer;
import sparx1.util.UncheckedException;
import sparx1.util.annotation.NotNegative;
import sparx1.util.annotation.NotNull;
import sparx1.util.function.IndexedFunction;

public class FlatMapListMaterializer<E, F> extends StatefulListMaterializer<F> {

  public FlatMapListMaterializer(final @NotNull ListMaterializer<E> wrapped,
      final @NotNull IndexedFunction<? super E, ? extends Iterable<F>> mapper) {
    setState(new InitialState(wrapped, mapper));
  }

  private class InitialState extends AbstractListMaterializer<F> {

    private final ArrayList<F> elements = new ArrayList<F>();
    private final Iterator<E> iterator;
    private final IndexedFunction<? super E, ? extends Iterable<F>> mapper;

    private Iterator<F> elementIterator = EmptyListMaterializer.iteratorInstance();
    private int pos;

    private InitialState(final @NotNull ListMaterializer<E> wrapped,
        final @NotNull IndexedFunction<? super E, ? extends Iterable<F>> mapper) {
      this.mapper = mapper;
      iterator = wrapped.materializeForwardIterator(0);
    }

    @Override
    public boolean canMaterializeElement(final @NotNegative int index) {
      return materializeUntil(index) > index;
    }

    @Override
    public boolean isRandomAccess() {
      return true;
    }

    @Override
    public boolean isSizeKnown() {
      return false;
    }

    @Override
    public int knownSize() {
      return -1;
    }

    @Override
    public F materializeElement(final @NotNegative int index) {
      if (materializeUntil(index) <= index) {
        throw new IndexOutOfBoundsException(Integer.toString(index));
      }
      return elements.get(index);
    }

    @Override
    public int materializeElements() {
      return materializeUntil(Integer.MAX_VALUE);
    }

    @Override
    public boolean materializeEmpty() {
      return materializeUntil(0) < 1;
    }

    @Override
    public int materializeSize() {
      return materializeUntil(Integer.MAX_VALUE);
    }

    private int materializeUntil(final @NotNegative int index) {
      final ArrayList<F> elements = this.elements;
      int currSize = elements.size();
      if (currSize > index) {
        return currSize;
      }
      final IndexedFunction<? super E, ? extends Iterable<F>> mapper = this.mapper;
      try {
        final Iterator<E> wrappedIterator = this.iterator;
        Iterator<F> elementIterator = this.elementIterator;
        int i = pos;
        while (true) {
          while (elementIterator.hasNext()) {
            elements.add(elementIterator.next());
            if (++currSize > index) {
              pos = i;
              this.elementIterator = elementIterator;
              return currSize;
            }
          }
          if (wrappedIterator.hasNext()) {
            final E element = wrappedIterator.next();
            elementIterator = mapper.apply(i, element).iterator();
            ++i;
          } else {
            setState(new ListToListMaterializer<F>(elements));
            return currSize;
          }
        }
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }
}
