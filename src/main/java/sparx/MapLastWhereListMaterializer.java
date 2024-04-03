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

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import sparx.collection.ListMaterializer;
import sparx.util.Require;
import sparx.util.UncheckedException;
import sparx.util.function.Function;
import sparx.util.function.Predicate;

class MapLastWhereListMaterializer<E> implements ListMaterializer<E> {

  private final ListMaterializer<E> wrapped;

  private volatile State<E> state;

  MapLastWhereListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      @NotNull final Predicate<? super E> predicate,
      @NotNull final Function<? super E, ? extends E> mapper) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
    state = new ImmaterialState(Require.notNull(predicate, "predicate"),
        Require.notNull(mapper, "mapper"));
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    return wrapped.canMaterializeElement(index);
  }

  @Override
  public int knownSize() {
    return wrapped.knownSize();
  }

  @Override
  public E materializeElement(final int index) {
    if (index < 0) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    if (state.materializeUntil(index) == index) {
      return state.materialized();
    }
    return wrapped.materializeElement(index);
  }

  @Override
  public boolean materializeEmpty() {
    return wrapped.materializeEmpty();
  }

  @Override
  public @NotNull Iterator<E> materializeIterator() {
    return new ListMaterializerIterator<E>(this);
  }

  @Override
  public int materializeSize() {
    return wrapped.materializeSize();
  }

  private interface State<E> {

    E materialized();

    int materializeUntil(int index);
  }

  private static class ElementState<E> implements State<E> {

    private final E element;
    private final int index;

    private ElementState(final int index, final E element) {
      this.index = index;
      this.element = element;
    }

    @Override
    public E materialized() {
      return element;
    }

    @Override
    public int materializeUntil(final int index) {
      return this.index;
    }
  }

  private class ImmaterialState implements State<E> {

    private final Function<? super E, ? extends E> mapper;
    private final AtomicInteger modCount = new AtomicInteger();
    private final Predicate<? super E> predicate;

    private int pos = -1;

    private ImmaterialState(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends E> mapper) {
      this.predicate = predicate;
      this.mapper = mapper;
    }

    @Override
    public int materializeUntil(final int index) {
      final ListMaterializer<E> wrapped = MapLastWhereListMaterializer.this.wrapped;
      final Predicate<? super E> predicate = this.predicate;
      final AtomicInteger modCount = this.modCount;
      final int expectedCount = modCount.getAndIncrement() + 1;
      try {
        if (pos == -1) {
          pos = wrapped.materializeSize();
        }
        int i = pos - 1;
        while (i >= index && wrapped.canMaterializeElement(i)) {
          final E element = wrapped.materializeElement(i);
          if (predicate.test(element)) {
            if (expectedCount != modCount.get()) {
              throw new ConcurrentModificationException();
            }
            state = new ElementState<E>(i, mapper.apply(element));
            return i;
          }
          --i;
        }
        if (expectedCount != modCount.get()) {
          throw new ConcurrentModificationException();
        }
        if (!wrapped.canMaterializeElement(i)) {
          state = new ElementState<E>(i, null);
        } else {
          pos = i;
        }
        return i;
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public E materialized() {
      return null;
    }
  }
}