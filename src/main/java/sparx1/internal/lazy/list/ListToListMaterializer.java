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

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;
import sparx1.internal.lazy.ListMaterializer;
import sparx1.util.annotation.NotNegative;
import sparx1.util.annotation.NotNull;

public class ListToListMaterializer<E> implements ListMaterializer<E> {

  private final List<E> elements;
  private final boolean isRandomAccess;

  public ListToListMaterializer(final @NotNull List<E> elements) {
    this.elements = elements;
    isRandomAccess = elements instanceof RandomAccess;
  }

  @Override
  public boolean canMaterializeElement(final @NotNegative int index) {
    return index < elements.size();
  }

  @Override
  public boolean isRandomAccess() {
    return isRandomAccess;
  }

  @Override
  public int knownSize() {
    return elements.size();
  }

  @Override
  public boolean isSizeKnown() {
    return true;
  }

  @Override
  @SuppressWarnings("SuspiciousMethodCalls")
  public boolean materializeContains(final Object element) {
    return elements.contains(element);
  }

  @Override
  public E materializeElement(final @NotNegative int index) {
    return elements.get(index);
  }

  @Override
  public Iterator<E> materializeBackwardIterator(final @NotNegative int index) {
    final ListIterator<E> listIterator = elements.listIterator(index + 1);
    return new Iterator<E>() {
      @Override
      public boolean hasNext() {
        return listIterator.hasPrevious();
      }

      @Override
      public E next() {
        return listIterator.previous();
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("remove");
      }
    };
  }

  @Override
  public Iterator<E> materializeForwardIterator(final @NotNegative int index) {
    return elements.listIterator(index);
  }

  @Override
  public Iterator<E> materializeUnorderedIterator() {
    return elements.iterator();
  }

  @Override
  public int materializeElements() {
    return elements.size();
  }

  @Override
  public boolean materializeEmpty() {
    return elements.isEmpty();
  }

  @Override
  public int materializeSize() {
    return elements.size();
  }
}
