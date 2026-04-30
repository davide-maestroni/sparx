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
import sparx1.util.function.IndexedPredicate;

public class FlatMapWhileListMaterializer<E> extends StatefulListMaterializer<E> {

  public FlatMapWhileListMaterializer(final @NotNull ListMaterializer<E> wrapped,
      final @NotNull IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper,
      final @NotNull IndexedPredicate<? super E> condition) {
    setState(new InitialState(wrapped, mapper, condition));
  }

  private class InitialState extends AbstractListMaterializer<E> {

    private final IndexedPredicate<? super E> condition;
    private final ArrayList<E> elements = new ArrayList<E>();
    private final Iterator<E> iterator;
    private final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper;
    private final ListMaterializer<E> wrapped;

    private Iterator<? extends E> elementIterator = EmptyListMaterializer.iteratorInstance();
    private int pos;

    private InitialState(final @NotNull ListMaterializer<E> wrapped,
        final @NotNull IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper,
        final @NotNull IndexedPredicate<? super E> condition) {
      this.wrapped = wrapped;
      this.mapper = mapper;
      this.condition = condition;
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
    public E materializeElement(final @NotNegative int index) {
      if (materializeUntil(index) <= index) {
        throw new IndexOutOfBoundsException(Integer.toString(index));
      }
      final ListMaterializer<E> state = getState();
      return state == this ? elements.get(index) : state.materializeElement(index);
    }

    @Override
    public int materializeElements() {
      materializeUntil(Integer.MAX_VALUE);
      return getState().materializeElements();
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
      final ArrayList<E> elements = this.elements;
      int currSize = elements.size();
      if (currSize > index) {
        return currSize;
      }
      final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper = this.mapper;
      final IndexedPredicate<? super E> condition = this.condition;
      try {
        final Iterator<E> wrappedIterator = this.iterator;
        Iterator<? extends E> elementIterator = this.elementIterator;
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
            if (!condition.test(i, element)) {
              final ListMaterializer<E> state = setState(
                  new AppendAllListMaterializer<E>(new ListToListMaterializer<E>(elements),
                      new DropFirstListMaterializer<E>(wrapped, i)));
              return state.canMaterializeElement(index) ? index + 1 : state.materializeSize();
            }
            elementIterator = mapper.apply(i, element).iterator();
            ++i;
          } else {
            setState(new ListToListMaterializer<E>(elements));
            return currSize;
          }
        }
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }
}
