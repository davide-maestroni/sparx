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
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import sparx.util.UncheckedException;
import sparx.util.function.IndexedFunction;

public class DistinctByListMaterializer<E, K> implements ListMaterializer<E> {

  private volatile ListMaterializer<E> state;

  public DistinctByListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      @NotNull final IndexedFunction<? super E, K> keyExtractor) {
    state = new ImmaterialState(wrapped, keyExtractor);
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

    private final HashSet<Object> distinctKeys = new HashSet<Object>();
    private final ArrayList<E> elements = new ArrayList<E>();
    private final IndexedFunction<? super E, K> keyExtractor;
    private final AtomicInteger modCount = new AtomicInteger();
    private final ListMaterializer<E> wrapped;

    private int pos;

    private ImmaterialState(@NotNull final ListMaterializer<E> wrapped,
        @NotNull final IndexedFunction<? super E, K> keyExtractor) {
      this.wrapped = wrapped;
      this.keyExtractor = keyExtractor;
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

    private int materializeUntil(final int index) {
      final ArrayList<E> elements = this.elements;
      int currSize = elements.size();
      if (currSize > index) {
        return currSize;
      }
      final ListMaterializer<E> wrapped = this.wrapped;
      final HashSet<Object> distinctKeys = this.distinctKeys;
      final IndexedFunction<? super E, K> keyExtractor = this.keyExtractor;
      final AtomicInteger modCount = this.modCount;
      final int expectedCount = modCount.incrementAndGet();
      try {
        int i = pos;
        while (true) {
          if (wrapped.canMaterializeElement(i)) {
            final E element = wrapped.materializeElement(i);
            if (distinctKeys.add(keyExtractor.apply(i, element))) {
              elements.add(element);
              if (++currSize > index) {
                if (expectedCount != modCount.get()) {
                  throw new ConcurrentModificationException();
                }
                pos = i + 1;
                return currSize;
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
        state = new FailedListMaterializer<E>(e);
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }
}
