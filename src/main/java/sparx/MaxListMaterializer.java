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

import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import sparx.collection.ListMaterializer;
import sparx.util.Require;
import sparx.util.UncheckedException;

class MaxListMaterializer<E> implements ListMaterializer<E> {

  private static final EmptyListMaterializer<?> EMPTY_STATE = new EmptyListMaterializer<Object>();

  private volatile ListMaterializer<E> state;

  MaxListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      @NotNull final Comparator<? super E> comparator) {
    state = new ImmaterialState(Require.notNull(wrapped, "wrapped"),
        Require.notNull(comparator, "comparator"));
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

    private final AtomicBoolean isMaterialized = new AtomicBoolean(false);
    private final Comparator<? super E> comparator;
    private final ListMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final ListMaterializer<E> wrapped,
        @NotNull final Comparator<? super E> comparator) {
      this.wrapped = wrapped;
      this.comparator = comparator;
    }

    @Override
    public boolean canMaterializeElement(final int index) {
      return index == 0 && !materializeEmpty();
    }

    @Override
    public int knownSize() {
      final int knownSize = wrapped.knownSize();
      if (knownSize > 0) {
        return 1;
      }
      if (knownSize == 0) {
        return 0;
      }
      return -1;
    }

    @Override
    public E materializeElement(final int index) {
      if (index != 0) {
        throw new IndexOutOfBoundsException(Integer.toString(index));
      }
      return materialized().materializeElement(index);
    }

    @Override
    public boolean materializeEmpty() {
      return wrapped.materializeEmpty();
    }

    @Override
    public @NotNull Iterator<E> materializeIterator() {
      return materialized().materializeIterator();
    }

    @Override
    public int materializeSize() {
      return materializeEmpty() ? 0 : 1;
    }

    @SuppressWarnings("unchecked")
    private @NotNull ListMaterializer<E> materialized() {
      if (!isMaterialized.compareAndSet(false, true)) {
        throw new ConcurrentModificationException();
      }
      try {
        final Comparator<? super E> comparator = this.comparator;
        final Iterator<E> iterator = wrapped.materializeIterator();
        if (!iterator.hasNext()) {
          return state = (ListMaterializer<E>) EMPTY_STATE;
        }
        E max = iterator.next();
        while (iterator.hasNext()) {
          final E next = iterator.next();
          if (comparator.compare(next, max) > 0) {
            max = next;
          }
        }
        return state = new ElementToListMaterializer<E>(max);
      } catch (final Exception e) {
        isMaterialized.set(false);
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }
}
