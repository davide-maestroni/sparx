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
package sparx.internal.lazy.list;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import sparx.util.UncheckedException;

public class DiffListMaterializer<E> implements ListMaterializer<E> {

  private volatile ListMaterializer<E> state;

  public DiffListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      @NotNull final ListMaterializer<?> elementsMaterializer) {
    state = new ImmaterialState(wrapped, elementsMaterializer);
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    return state.canMaterializeElement(index);
  }

  @Override
  public int knownSize() {
    return state.knownSize();
  }

  @Override
  public boolean materializeContains(final Object element) {
    return state.materializeContains(element);
  }

  @Override
  public E materializeElement(final int index) {
    return state.materializeElement(index);
  }

  @Override
  public int materializeElements() {
    return state.materializeElements();
  }

  @Override
  public boolean materializeEmpty() {
    return state.materializeEmpty();
  }

  @Override
  public @NotNull Iterator<E> materializeIterator() {
    return state.materializeIterator();
  }

  @Override
  public int materializeSize() {
    return state.materializeSize();
  }

  private class ImmaterialState extends AbstractListMaterializer<E> {

    private final ArrayList<E> elements = new ArrayList<E>();
    private final ListMaterializer<?> elementsMaterializer;
    private final AtomicInteger modCount = new AtomicInteger();
    private final ListMaterializer<E> wrapped;

    private HashMap<Object, Integer> elementsBag;
    private int pos;

    private ImmaterialState(@NotNull final ListMaterializer<E> wrapped,
        @NotNull final ListMaterializer<?> elementsMaterializer) {
      this.wrapped = wrapped;
      this.elementsMaterializer = elementsMaterializer;
    }

    @Override
    public boolean canMaterializeElement(final int index) {
      return index >= 0 && materializeUntil(index) > index;
    }

    @Override
    public int knownSize() {
      return -1;
    }

    @Override
    public E materializeElement(final int index) {
      if (index < 0 || materializeUntil(index) <= index) {
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
    public @NotNull Iterator<E> materializeIterator() {
      return new ListMaterializerIterator<E>(this);
    }

    @Override
    public int materializeSize() {
      return materializeUntil(Integer.MAX_VALUE);
    }

    private @NotNull HashMap<Object, Integer> fillElementsBag() {
      if (elementsBag == null) {
        final AtomicInteger modCount = this.modCount;
        final int expectedCount = modCount.incrementAndGet();
        final HashMap<Object, Integer> bag = elementsBag = new HashMap<Object, Integer>();
        final ListMaterializer<?> elementsMaterializer = this.elementsMaterializer;
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
        if (expectedCount != modCount.get()) {
          throw new ConcurrentModificationException();
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
      final ListMaterializer<E> wrapped = this.wrapped;
      final HashMap<Object, Integer> elementsBag = fillElementsBag();
      final AtomicInteger modCount = this.modCount;
      final int expectedCount = modCount.incrementAndGet();
      try {
        int i = pos;
        while (true) {
          if (wrapped.canMaterializeElement(i)) {
            final E element = wrapped.materializeElement(i);
            final Integer count = elementsBag.get(element);
            if (count == null) {
              elements.add(element);
              if (++currSize > index) {
                if (expectedCount != modCount.get()) {
                  throw new ConcurrentModificationException();
                }
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
            if (expectedCount != modCount.get()) {
              throw new ConcurrentModificationException();
            }
            state = new ListToListMaterializer<E>(elements);
            return currSize;
          }
        }
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }
}
