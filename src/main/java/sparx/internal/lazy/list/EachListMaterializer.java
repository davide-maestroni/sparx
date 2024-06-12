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
import sparx.util.function.IndexedPredicate;

public class EachListMaterializer<E> implements ListMaterializer<Boolean> {

  private static final State FALSE_STATE = new FalseState();
  private static final State TRUE_STATE = new TrueState();

  private volatile State state;

  public EachListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      @NotNull final IndexedPredicate<? super E> predicate, final boolean defaultResult) {
    state = new ImmaterialState(wrapped, predicate, defaultResult ? TRUE_STATE : FALSE_STATE);
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    return index == 0;
  }

  @Override
  public int knownSize() {
    return 1;
  }

  @Override
  public boolean materializeContains(final Object element) {
    return Boolean.valueOf(state.materialized()).equals(element);
  }

  @Override
  public Boolean materializeElement(final int index) {
    if (index != 0) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    return state.materialized();
  }

  @Override
  public int materializeElements() {
    state.materialized();
    return 1;
  }

  @Override
  public boolean materializeEmpty() {
    return false;
  }

  @Override
  public @NotNull Iterator<Boolean> materializeIterator() {
    return new ListMaterializerIterator<Boolean>(this);
  }

  @Override
  public int materializeSize() {
    return 1;
  }

  private interface State {

    boolean materialized();
  }

  private static class FalseState implements State {

    @Override
    public boolean materialized() {
      return false;
    }
  }

  private static class TrueState implements State {

    @Override
    public boolean materialized() {
      return true;
    }
  }

  private class ImmaterialState implements State {

    private final State defaultState;
    private final AtomicBoolean isMaterialized = new AtomicBoolean(false);
    private final IndexedPredicate<? super E> predicate;
    private final ListMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final ListMaterializer<E> wrapped,
        @NotNull final IndexedPredicate<? super E> predicate, @NotNull final State defaultState) {
      this.wrapped = wrapped;
      this.predicate = predicate;
      this.defaultState = defaultState;
    }

    @Override
    public boolean materialized() {
      if (!isMaterialized.compareAndSet(false, true)) {
        throw new ConcurrentModificationException();
      }
      try {
        final ListMaterializer<E> wrapped = this.wrapped;
        if (wrapped.materializeEmpty()) {
          return (state = defaultState).materialized();
        }
        final IndexedPredicate<? super E> predicate = this.predicate;
        int i = 0;
        do {
          if (!predicate.test(i, wrapped.materializeElement(i))) {
            state = FALSE_STATE;
            return false;
          }
          ++i;
        } while (wrapped.canMaterializeElement(i));
        state = TRUE_STATE;
        return true;
      } catch (final Exception e) {
        isMaterialized.set(false);
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }
}
