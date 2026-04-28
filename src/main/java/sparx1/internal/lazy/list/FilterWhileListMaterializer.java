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
import sparx1.util.function.IndexedPredicate;

public class FilterWhileListMaterializer<E> extends StatefulListMaterializer<E> {

  public FilterWhileListMaterializer(final @NotNull ListMaterializer<E> wrapped,
      final @NotNull IndexedPredicate<? super E> predicate,
      final @NotNull IndexedPredicate<? super E> condition) {
    setState(new InitialState(wrapped, predicate, condition));
  }

  private class InitialState extends AbstractListMaterializer<E> {

    private final IndexedPredicate<? super E> condition;
    private final ArrayList<E> elements = new ArrayList<E>();
    private final Iterator<E> iterator;
    private final IndexedPredicate<? super E> predicate;
    private final ListMaterializer<E> wrapped;

    private int pos;

    private InitialState(final @NotNull ListMaterializer<E> wrapped,
        final @NotNull IndexedPredicate<? super E> predicate,
        final @NotNull IndexedPredicate<? super E> condition) {
      iterator = (this.wrapped = wrapped).materializeUnorderedIterator();
      this.predicate = predicate;
      this.condition = condition;
    }

    @Override
    public boolean canMaterializeElement(final @NotNegative int index) {
      final ArrayList<E> elements = this.elements;
      if (elements.size() <= index) {
        final IndexedPredicate<? super E> condition = this.condition;
        final IndexedPredicate<? super E> predicate = this.predicate;
        try {
          final Iterator<E> iterator = this.iterator;
          do {
            if (!iterator.hasNext()) {
              return false;
            }
            final E next = iterator.next();
            final int pos = this.pos++;
            if (condition.test(pos, next)) {
              if (predicate.test(pos, next)) {
                elements.add(next);
              }
            } else {
              final ListMaterializer<E> state = setState(
                  new AppendAllListMaterializer<E>(new ListToListMaterializer<E>(elements),
                      new DropFirstListMaterializer<E>(wrapped, pos)));
              return state.canMaterializeElement(index);
            }
          } while (elements.size() <= index);
          if (!iterator.hasNext()) {
            setState(new ListToListMaterializer<E>(elements));
          }
        } catch (final Exception e) {
          throw UncheckedException.throwUnchecked(e);
        }
      }
      return true;
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
      if (!canMaterializeElement(index)) {
        throw new IndexOutOfBoundsException(Integer.toString(index));
      }
      final ListMaterializer<E> state = getState();
      return state == this ? elements.get(index) : state.materializeElement(index);
    }

    @Override
    public int materializeElements() {
      return materializeSize();
    }

    @Override
    public boolean materializeEmpty() {
      if (elements.isEmpty()) {
        return !canMaterializeElement(0);
      }
      return false;
    }

    @Override
    public int materializeSize() {
      canMaterializeElement(Integer.MAX_VALUE);
      final ListMaterializer<E> state = getState();
      return state == this ? elements.size() : state.materializeSize();
    }
  }
}
