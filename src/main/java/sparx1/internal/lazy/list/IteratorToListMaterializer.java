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
import sparx1.util.annotation.NotNegative;
import sparx1.util.annotation.NotNull;

public class IteratorToListMaterializer<E> extends StatefulListMaterializer<E> {

  public IteratorToListMaterializer(final @NotNull Iterator<E> iterator) {
    setState(new InitialState(iterator));
  }

  private class InitialState extends AbstractListMaterializer<E> {

    private final ArrayList<E> elements = new ArrayList<E>();
    private final Iterator<E> iterator;

    private InitialState(final @NotNull Iterator<E> iterator) {
      this.iterator = iterator;
    }

    @Override
    public boolean canMaterializeElement(final @NotNegative int index) {
      final ArrayList<E> elements = this.elements;
      if (elements.size() <= index) {
        final Iterator<E> iterator = this.iterator;
        do {
          if (!iterator.hasNext()) {
            return false;
          }
          elements.add(iterator.next());
        } while (elements.size() <= index);

        if (!iterator.hasNext()) {
          setState(new ListToListMaterializer<E>(elements));
        }
      }
      return true;
    }

    @Override
    public int knownSize() {
      return -1;
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
    public E materializeElement(final @NotNegative int index) {
      final ArrayList<E> elements = this.elements;
      if (elements.size() <= index) {
        final Iterator<E> iterator = this.iterator;
        do {
          if (!iterator.hasNext()) {
            throw new IndexOutOfBoundsException(Integer.toString(index));
          }
          elements.add(iterator.next());
        } while (elements.size() <= index);

        if (!iterator.hasNext()) {
          setState(new ListToListMaterializer<E>(elements));
        }
      }
      return elements.get(index);
    }

    @Override
    public int materializeElements() {
      return materializeSize();
    }

    @Override
    public boolean materializeEmpty() {
      if (elements.isEmpty()) {
        return !iterator.hasNext();
      }
      return false;
    }

    @Override
    public int materializeSize() {
      final ArrayList<E> elements = this.elements;
      final Iterator<E> iterator = this.iterator;
      while (iterator.hasNext()) {
        elements.add(iterator.next());
      }
      setState(new ListToListMaterializer<E>(elements));
      return elements.size();
    }
  }
}
