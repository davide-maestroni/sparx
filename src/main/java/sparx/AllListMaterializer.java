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

class AllListMaterializer<E> implements ListMaterializer<Boolean> {

  private static final State FALSE_STATE = new FalseState();
  private static final State TRUE_STATE = new TrueState();

  private volatile State state;

  AllListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      @NotNull final Predicate<? super E> predicate, final boolean defaultState) {
    state = new ImmaterialState(wrapped, Require.notNull(predicate, "predicate"), defaultState);
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
  public Boolean materializeElement(final int index) {
    if (index != 0) {
      throw new IndexOutOfBoundsException(String.valueOf(index));
    }
    return state.materialized();
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

    private final Predicate<? super E> predicate;
    private final boolean defaultState;
    private final AtomicBoolean isMaterialized = new AtomicBoolean(false);
    private final ListMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final ListMaterializer<E> wrapped,
        @NotNull final Predicate<? super E> predicate, final boolean defaultState) {
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
        final Iterator<E> iterator = wrapped.materializeIterator();
        if (!iterator.hasNext()) {
          if (defaultState) {
            state = TRUE_STATE;
            return true;
          } else {
            state = FALSE_STATE;
            return false;
          }
        }
        final Predicate<? super E> predicate = this.predicate;
        while (iterator.hasNext()) {
          final E next = iterator.next();
          if (!predicate.test(next)) {
            state = FALSE_STATE;
            return false;
          }
        }
        state = TRUE_STATE;
        return true;
      } catch (final Exception e) {
        isMaterialized.set(false);
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }
}
