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
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import sparx.collection.ListMaterializer;
import sparx.util.Require;
import sparx.util.UncheckedException;
import sparx.util.function.Predicate;

class FindIndexListMaterializer<E> implements ListMaterializer<Integer> {

  private static final State NOT_FOUND = new NotFoundState();

  private volatile State state;

  FindIndexListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      final int minIndex, @NotNull final Predicate<? super E> predicate) {
    if (minIndex <= 0) {
      state = new ImmaterialState(Require.notNull(wrapped, "wrapped"),
          Require.notNull(predicate, "predicate"));
    } else {
      state = new IndexImmaterialState(Require.notNull(wrapped, "wrapped"), minIndex,
          Require.notNull(predicate, "predicate"));
    }
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    return index == 0 && state.materialized() >= 0;
  }

  @Override
  public int knownSize() {
    return state.knownSize();
  }

  @Override
  public Integer materializeElement(final int index) {
    if (index == 0) {
      final int i = state.materialized();
      if (i >= 0) {
        return i;
      }
    }
    throw new IndexOutOfBoundsException(String.valueOf(index));
  }

  @Override
  public boolean materializeEmpty() {
    return state.materialized() < 0;
  }

  @Override
  public @NotNull Iterator<Integer> materializeIterator() {
    return new ListMaterializerIterator<Integer>(this);
  }

  @Override
  public int materializeSize() {
    return state.materialized() < 0 ? 0 : 1;
  }

  private interface State {

    int knownSize();

    int materialized();
  }

  private static class IndexState implements State {

    private final int index;

    private IndexState(final int index) {
      this.index = index;
    }

    @Override
    public int knownSize() {
      return 1;
    }

    @Override
    public int materialized() {
      return index;
    }
  }

  private static class NotFoundState implements State {

    @Override
    public int knownSize() {
      return 0;
    }

    @Override
    public int materialized() {
      return -1;
    }
  }

  private class ImmaterialState implements State {

    private final AtomicBoolean isMaterialized = new AtomicBoolean(false);
    private final Predicate<? super E> predicate;
    private final ListMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final ListMaterializer<E> wrapped,
        @NotNull final Predicate<? super E> predicate) {
      this.wrapped = wrapped;
      this.predicate = predicate;
    }

    @Override
    public int knownSize() {
      return -1;
    }

    @Override
    public int materialized() {
      if (!isMaterialized.compareAndSet(false, true)) {
        throw new ConcurrentModificationException();
      }
      try {
        final Predicate<? super E> predicate = this.predicate;
        final Iterator<E> iterator = wrapped.materializeIterator();
        int index = 0;
        while (iterator.hasNext()) {
          final E next = iterator.next();
          if (predicate.test(next)) {
            state = new IndexState(index);
            return index;
          }
          ++index;
        }
        state = NOT_FOUND;
        return -1;
      } catch (final Exception e) {
        isMaterialized.set(false);
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }

  private class IndexImmaterialState implements State {

    private final AtomicBoolean isMaterialized = new AtomicBoolean(false);
    private final int minIndex;
    private final Predicate<? super E> predicate;
    private final ListMaterializer<E> wrapped;

    private IndexImmaterialState(@NotNull final ListMaterializer<E> wrapped, final int minIndex,
        @NotNull final Predicate<? super E> predicate) {
      this.wrapped = wrapped;
      this.minIndex = minIndex;
      this.predicate = predicate;
    }

    @Override
    public int knownSize() {
      return -1;
    }

    @Override
    public int materialized() {
      if (!isMaterialized.compareAndSet(false, true)) {
        throw new ConcurrentModificationException();
      }
      try {
        final Predicate<? super E> predicate = this.predicate;
        final ListMaterializer<E> wrapped = this.wrapped;
        int index = minIndex;
        while (wrapped.canMaterializeElement(index)) {
          final E element = wrapped.materializeElement(index);
          if (predicate.test(element)) {
            state = new IndexState(index);
            return index;
          }
          ++index;
        }
        state = NOT_FOUND;
        return -1;
      } catch (final Exception e) {
        isMaterialized.set(false);
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }
}
