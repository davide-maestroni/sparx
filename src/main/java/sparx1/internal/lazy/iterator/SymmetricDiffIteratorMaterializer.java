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

import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import sparx1.internal.lazy.IteratorMaterializer;
import sparx1.internal.lazy.ListMaterializer;
import sparx1.util.annotation.NotNull;

public class SymmetricDiffIteratorMaterializer<E> extends AbstractIteratorMaterializer<E> {

  private final Iterator<E> elementsIterator;
  private final ListMaterializer<E> elementsMaterializer;
  private final IteratorMaterializer<E> wrapped;

  private HashMap<E, Integer> elementsBag;
  private boolean hasNext;
  private boolean isWrapped = true;
  private E next;

  public SymmetricDiffIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      final @NotNull ListMaterializer<E> elementsMaterializer) {
    this.wrapped = wrapped;
    this.elementsMaterializer = elementsMaterializer;
    this.elementsIterator = elementsMaterializer.materializeIterator();
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
    final HashMap<E, Integer> elementsBag = fillElementsBag();
    final IteratorMaterializer<E> wrapped = this.wrapped;
    final Iterator<E> elementsIterator = this.elementsIterator;
    while (true) {
      if (isWrapped) {
        while (wrapped.materializeHasNext()) {
          final E element = wrapped.materializeNext();
          final Integer count = elementsBag.get(element);
          if (count == null) {
            hasNext = true;
            next = element;
            return true;
          }
          final int decCount = count - 1;
          if (decCount == 0) {
            elementsBag.remove(element);
          } else {
            elementsBag.put(element, decCount);
          }
        }
        isWrapped = false;
      } else {
        while (elementsIterator.hasNext()) {
          final E element = elementsIterator.next();
          final Integer count = elementsBag.get(element);
          if (count != null) {
            final int decCount = count - 1;
            if (decCount == 0) {
              elementsBag.remove(element);
            } else {
              elementsBag.put(element, decCount);
            }
            hasNext = true;
            next = element;
            return true;
          }
        }
        break;
      }
    }
    return false;
  }

  @Override
  public E materializeNext() {
    if (!materializeHasNext()) {
      throw new NoSuchElementException();
    }
    final E next = this.next;
    hasNext = false;
    this.next = null;
    return next;
  }

  private @NotNull HashMap<E, Integer> fillElementsBag() {
    if (elementsBag == null) {
      final HashMap<E, Integer> bag = elementsBag = new HashMap<E, Integer>();
      final ListMaterializer<E> elementsMaterializer = this.elementsMaterializer;
      int i = 0;
      while (elementsMaterializer.canMaterializeElement(i)) {
        final E element = elementsMaterializer.materializeElement(i++);
        final Integer count = bag.get(element);
        if (count == null) {
          bag.put(element, 1);
        } else {
          bag.put(element, count + 1);
        }
      }
    }
    return elementsBag;
  }
}
