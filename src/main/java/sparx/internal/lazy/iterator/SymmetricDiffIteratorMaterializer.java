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
package sparx.internal.lazy.iterator;

import java.util.HashMap;
import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;

public class SymmetricDiffIteratorMaterializer<E> extends AbstractIteratorMaterializer<E> {

  private final IteratorMaterializer<?> elementsMaterializer;
  private final IteratorMaterializer<E> wrapped;

  private HashMap<Object, Integer> elementsBag;
  private boolean hasNext;
  private boolean isWrapped = true;
  private E next;

  public SymmetricDiffIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      @NotNull final IteratorMaterializer<?> elementsMaterializer) {
    this.wrapped = wrapped;
    this.elementsMaterializer = elementsMaterializer;
  }

  @Override
  public int knownSize() {
    return -1;
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean materializeHasNext() {
    if (hasNext) {
      return true;
    }
    final HashMap<Object, Integer> elementsBag = fillElementsBag();
    IteratorMaterializer<E> materializer =
        isWrapped ? wrapped : (IteratorMaterializer<E>) elementsMaterializer;
    while (true) {
      while (materializer.materializeHasNext()) {
        final E element = materializer.materializeNext();
        final Integer count = elementsBag.get(element);
        if (isWrapped) {
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
        } else {
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
      }
      if (isWrapped) {
        isWrapped = false;
        materializer = (IteratorMaterializer<E>) elementsMaterializer;
      } else {
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

  private @NotNull HashMap<Object, Integer> fillElementsBag() {
    if (elementsBag == null) {
      final HashMap<Object, Integer> bag = elementsBag = new HashMap<Object, Integer>();
      final IteratorMaterializer<?> elementsMaterializer = this.elementsMaterializer;
      while (elementsMaterializer.materializeHasNext()) {
        final Object element = elementsMaterializer.materializeNext();
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
