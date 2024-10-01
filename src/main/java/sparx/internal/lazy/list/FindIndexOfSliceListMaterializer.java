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

public class FindIndexOfSliceListMaterializer<E> implements ListMaterializer<Integer> {

  private static final State NOT_FOUND = new NotFoundState();

  private volatile State state;

  public FindIndexOfSliceListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      @NotNull final ListMaterializer<?> elementsMaterializer) {
    state = new ImmaterialState(wrapped, elementsMaterializer);
  }

  @Override
  public boolean canMaterializeElement(@NotNegative final int index) {
    return state.materialized() >= 0;
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

    private final ListMaterializer<?> elementsMaterializer;
    private final AtomicBoolean isMaterialized = new AtomicBoolean(false);
    private final ListMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final ListMaterializer<E> wrapped,
        @NotNull final ListMaterializer<?> elementsMaterializer) {
      this.wrapped = wrapped;
      this.elementsMaterializer = elementsMaterializer;
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
        final ListMaterializer<?> elementsMaterializer = this.elementsMaterializer;
        Iterator<?> elementsIterator = elementsMaterializer.materializeIterator();
        if (!elementsIterator.hasNext()) {
          state = new IndexState(0);
          return 0;
        }
        int i = 0;
        int index = 0;
        while (wrapped.canMaterializeElement(i)) {
          if (!elementsIterator.hasNext()) {
            state = new IndexState(index);
            return index;
          }
          final E left = wrapped.materializeElement(i);
          Object right = elementsIterator.next();
          if (left == right || (left != null && left.equals(right))) {
            ++i;
          } else {
            i = ++index;
            elementsIterator = elementsMaterializer.materializeIterator();
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
