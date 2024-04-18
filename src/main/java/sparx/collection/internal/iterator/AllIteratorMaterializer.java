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
package sparx.collection.internal.iterator;

import org.jetbrains.annotations.NotNull;
import sparx.util.Require;
import sparx.util.UncheckedException;
import sparx.util.function.IndexedPredicate;

public class AllIteratorMaterializer<E> implements IteratorMaterializer<Boolean> {

  private volatile IteratorMaterializer<Boolean> state;

  public AllIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      @NotNull final IndexedPredicate<? super E> predicate, final boolean defaultState) {
    state = new ImmaterialState(Require.notNull(wrapped, "wrapped"),
        Require.notNull(predicate, "predicate"), defaultState);
  }

  @Override
  public int knownSize() {
    return state.knownSize();
  }

  @Override
  public boolean materializeHasNext() {
    return state.materializeHasNext();
  }

  @Override
  public Boolean materializeNext() {
    return state.materializeNext();
  }

  @Override
  public int skip(final int count) {
    return state.skip(count);
  }

  private class ImmaterialState implements IteratorMaterializer<Boolean> {

    private final boolean defaultState;
    private final IndexedPredicate<? super E> predicate;
    private final IteratorMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final IteratorMaterializer<E> wrapped,
        @NotNull final IndexedPredicate<? super E> predicate, final boolean defaultState) {
      this.wrapped = wrapped;
      this.predicate = predicate;
      this.defaultState = defaultState;
    }

    @Override
    public int knownSize() {
      return 1;
    }

    @Override
    public boolean materializeHasNext() {
      return true;
    }

    @Override
    public Boolean materializeNext() {
      try {
        final IteratorMaterializer<E> wrapped = this.wrapped;
        if (!wrapped.materializeHasNext()) {
          state = EmptyIteratorMaterializer.instance();
          return defaultState;
        }
        final IndexedPredicate<? super E> predicate = this.predicate;
        int i = 0;
        do {
          if (!predicate.test(i, wrapped.materializeNext())) {
            state = EmptyIteratorMaterializer.instance();
            return false;
          }
          ++i;
        } while (wrapped.materializeHasNext());
        state = EmptyIteratorMaterializer.instance();
        return true;
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public int skip(final int count) {
      if (count > 0) {
        state = EmptyIteratorMaterializer.instance();
        return 1;
      }
      return 0;
    }
  }
}
