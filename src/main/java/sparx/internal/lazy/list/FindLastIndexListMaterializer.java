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

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import sparx.util.UncheckedException;
import sparx.util.annotation.NotNegative;
import sparx.util.function.IndexedPredicate;

public class FindLastIndexListMaterializer<E> implements ListMaterializer<Integer> {

  private static final State NOT_FOUND = new NotFoundState();

  private volatile State state;

  public FindLastIndexListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      @NotNull final IndexedPredicate<? super E> predicate) {
    state = new ImmaterialState(wrapped, predicate);
  }

  @Override
  public boolean canMaterializeElement(@NotNegative final int index) {
    return index == 0 && state.materialized() >= 0;
  }

  @Override
  public int knownSize() {
    return state.knownSize();
  }

  @Override
  public boolean materializeContains(final Object element) {
    return Integer.valueOf(state.materialized()).equals(element);
  }

  @Override
  public Integer materializeElement(@NotNegative final int index) {
    if (index == 0) {
      final int i = state.materialized();
      if (i >= 0) {
        return i;
      }
    }
    throw new IndexOutOfBoundsException(Integer.toString(index));
  }

  @Override
  public int materializeElements() {
    return state.materialized() >= 0 ? 1 : 0;
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
    private final IndexedPredicate<? super E> predicate;
    private final ListMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final ListMaterializer<E> wrapped,
        @NotNull final IndexedPredicate<? super E> predicate) {
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
        final ListMaterializer<E> wrapped = this.wrapped;
        final IndexedPredicate<? super E> predicate = this.predicate;
        final int wrappedSize = wrapped.materializeSize();
        for (int i = wrappedSize - 1; i >= 0; --i) {
          final E element = wrapped.materializeElement(i);
          if (predicate.test(i, element)) {
            state = new IndexState(i);
            return i;
          }
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
