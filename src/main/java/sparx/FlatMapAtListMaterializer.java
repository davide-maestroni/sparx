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
package sparx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import sparx.collection.ListMaterializer;
import sparx.util.Require;
import sparx.util.UncheckedException;
import sparx.util.function.Function;

class FlatMapAtListMaterializer<E> implements ListMaterializer<E> {

  private final int index;
  private final ListMaterializer<E> wrapped;

  private volatile ListMaterializer<E> state;

  FlatMapAtListMaterializer(@NotNull final ListMaterializer<E> wrapped, final int index,
      @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
    this.index = Require.notNegative(index, "index");
    state = new ImmaterialState(Require.notNull(mapper, "mapper"));
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    if (index < 0) {
      return false;
    }
    if (index < this.index) {
      return wrapped.canMaterializeElement(index);
    }
    return state.canMaterializeElement(index);
  }

  @Override
  public int knownSize() {
    return state.knownSize();
  }

  @Override
  public E materializeElement(final int index) {
    return state.materializeElement(index);
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

  private class ImmaterialState implements ListMaterializer<E> {

    private final ArrayList<E> elements = new ArrayList<E>();
    private final Iterator<E> iterator;
    private final Function<? super E, ? extends Iterable<? extends E>> mapper;
    private final AtomicInteger modCount = new AtomicInteger();

    private Iterator<? extends E> elementIterator = Collections.<E>emptySet().iterator();

    private ImmaterialState(
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      this.mapper = mapper;
      iterator = wrapped.materializeIterator();
    }

    @Override
    public boolean canMaterializeElement(final int index) {
      if (index < 0) {
        return false;
      }
      return materializeUntil(index) > index;
    }

    @Override
    public int knownSize() {
      return -1;
    }

    @Override
    public E materializeElement(final int index) {
      if (index < 0 || materializeUntil(index) <= index) {
        throw new IndexOutOfBoundsException(String.valueOf(index));
      }
      return elements.get(index);
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
      final Iterator<E> iterator = this.iterator;
      final Function<? super E, ? extends Iterable<? extends E>> mapper = this.mapper;
      final AtomicInteger modCount = this.modCount;
      final int expectedCount = modCount.getAndIncrement() + 1;
      try {
        Iterator<? extends E> elementIterator = this.elementIterator;
        while (true) {
          while (elementIterator.hasNext()) {
            elements.add(elementIterator.next());
            if (++currSize > index) {
              if (expectedCount != modCount.get()) {
                throw new ConcurrentModificationException();
              }
              return currSize;
            }
          }
          if (iterator.hasNext()) {
            elementIterator = this.elementIterator = mapper.apply(iterator.next()).iterator();
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
