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
import java.util.HashMap;
import java.util.Iterator;
import sparx1.internal.lazy.ListMaterializer;
import sparx1.util.annotation.NotNegative;
import sparx1.util.annotation.NotNull;

public class DiffListMaterializer<E> extends StatefulListMaterializer<E> {

  public DiffListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      @NotNull final ListMaterializer<?> elementsMaterializer) {
    setState(new InitialState(wrapped, elementsMaterializer));
  }

  private class InitialState extends AbstractListMaterializer<E> {

    private final ArrayList<E> elements = new ArrayList<E>();
    private final ListMaterializer<?> elementsMaterializer;
    private final Iterator<E> iterator;

    private HashMap<Object, Integer> elementsBag;
    private int pos;

    private InitialState(@NotNull final ListMaterializer<E> wrapped,
        @NotNull final ListMaterializer<?> elementsMaterializer) {
      this.elementsMaterializer = elementsMaterializer;
      iterator = wrapped.materializeForwardIterator(0);
    }

    @Override
    public boolean canMaterializeElement(@NotNegative final int index) {
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
    public E materializeElement(@NotNegative final int index) {
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

    private @NotNull HashMap<Object, Integer> fillElementsBag() {
      if (elementsBag == null) {
        final HashMap<Object, Integer> bag = elementsBag = new HashMap<Object, Integer>();
        final ListMaterializer<?> elementsMaterializer = this.elementsMaterializer;
        if (elementsMaterializer.isRandomAccess()) {
          int i = 0;
          while (elementsMaterializer.canMaterializeElement(i)) {
            final Object element = elementsMaterializer.materializeElement(i++);
            final Integer count = bag.get(element);
            if (count == null) {
              bag.put(element, 1);
            } else {
              bag.put(element, count + 1);
            }
          }
        } else {
          final Iterator<?> iterator = elementsMaterializer.materializeUnorderedIterator();
          while (iterator.hasNext()) {
            final Object element = iterator.next();
            final Integer count = bag.get(element);
            if (count == null) {
              bag.put(element, 1);
            } else {
              bag.put(element, count + 1);
            }
          }
        }
      }
      return elementsBag;
    }

    private int materializeUntil(final int index) {
      final ArrayList<E> elements = this.elements;
      int currSize = elements.size();
      if (currSize > index) {
        return currSize;
      }
      final HashMap<Object, Integer> elementsBag = fillElementsBag();
      int i = pos;
      final Iterator<E> iterator = this.iterator;
      while (true) {
        if (iterator.hasNext()) {
          final E element = iterator.next();
          final Integer count = elementsBag.get(element);
          if (count == null) {
            elements.add(element);
            if (++currSize > index) {
              pos = i + 1;
              return currSize;
            }
          } else {
            final int decCount = count - 1;
            if (decCount == 0) {
              elementsBag.remove(element);
            } else {
              elementsBag.put(element, decCount);
            }
          }
          ++i;
        } else {
          setState(new ListToListMaterializer<E>(elements));
          return currSize;
        }
      }
    }
  }
}
