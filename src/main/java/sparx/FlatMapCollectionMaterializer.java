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
import sparx.util.CollectionMaterializer;
import sparx.util.Require;
import sparx.util.UncheckedException;
import sparx.util.function.Function;

class FlatMapCollectionMaterializer<E, F> implements CollectionMaterializer<F> {

  private volatile CollectionMaterializer<F> state;

  FlatMapCollectionMaterializer(@NotNull final CollectionMaterializer<E> wrapper,
      @NotNull final Function<? super E, ? extends Iterable<F>> mapper) {
    state = new ImmaterialState(Require.notNull(wrapper, "wrapper"),
        Require.notNull(mapper, "mapper"));
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
  public F materializeElement(final int index) {
    return state.materializeElement(index);
  }

  @Override
  public boolean materializeEmpty() {
    return state.materializeEmpty();
  }

  @Override
  public @NotNull Iterator<F> materializeIterator() {
    return state.materializeIterator();
  }

  @Override
  public int materializeSize() {
    return state.materializeSize();
  }

  private static class ExceptionState<E> implements CollectionMaterializer<E> {

    private final Exception ex;

    private ExceptionState(@NotNull final Exception ex) {
      this.ex = ex;
    }

    @Override
    public boolean canMaterializeElement(final int index) {
      throw UncheckedException.throwUnchecked(ex);
    }

    @Override
    public int knownSize() {
      return 1;
    }

    @Override
    public E materializeElement(final int index) {
      throw UncheckedException.throwUnchecked(ex);
    }

    @Override
    public boolean materializeEmpty() {
      throw UncheckedException.throwUnchecked(ex);
    }

    @Override
    public @NotNull Iterator<E> materializeIterator() {
      throw UncheckedException.throwUnchecked(ex);
    }

    @Override
    public int materializeSize() {
      throw UncheckedException.throwUnchecked(ex);
    }
  }

  private class ImmaterialState implements CollectionMaterializer<F> {

    private final ArrayList<F> elements = new ArrayList<F>();
    private final Iterator<E> iterator;
    private final Function<? super E, ? extends Iterable<F>> mapper;
    private final AtomicInteger modCount = new AtomicInteger();

    private Iterator<F> elementIterator = Collections.<F>emptySet().iterator();

    private ImmaterialState(@NotNull final CollectionMaterializer<E> wrapped,
        @NotNull final Function<? super E, ? extends Iterable<F>> mapper) {
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
    public F materializeElement(final int index) {
      if (index < 0 || materializeUntil(index) <= index) {
        throw new IndexOutOfBoundsException(String.valueOf(index));
      }
      return null;
    }

    @Override
    public boolean materializeEmpty() {
      return materializeUntil(0) < 1;
    }

    @Override
    public @NotNull Iterator<F> materializeIterator() {
      return new CollectionMaterializerIterator<F>(this);
    }

    @Override
    public int materializeSize() {
      return materializeUntil(Integer.MAX_VALUE);
    }

    private int materializeUntil(final int index) {
      int currSize = elements.size();
      if (currSize > index) {
        return currSize;
      }
      final Iterator<E> iterator = this.iterator;
      final AtomicInteger modCount = this.modCount;
      final int expectedCount = modCount.getAndIncrement() + 1;
      try {
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
            elementIterator = mapper.apply(iterator.next()).iterator();
          } else {
            if (expectedCount != modCount.get()) {
              throw new ConcurrentModificationException();
            }
            state = new ListToCollectionMaterializer<F>(elements);
            return currSize;
          }
        }
      } catch (final Exception e) {
        state = new ExceptionState<F>(e);
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }
}
