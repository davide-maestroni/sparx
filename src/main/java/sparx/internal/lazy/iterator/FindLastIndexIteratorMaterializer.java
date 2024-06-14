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
package sparx.internal.lazy.iterator;

import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;
import sparx.util.UncheckedException;
import sparx.util.function.IndexedPredicate;

public class FindLastIndexIteratorMaterializer<E> implements IteratorMaterializer<Integer> {

  private volatile IteratorMaterializer<Integer> state;

  public FindLastIndexIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      @NotNull final IndexedPredicate<? super E> predicate) {
    state = new ImmaterialState(wrapped, predicate);
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
  public Integer materializeNext() {
    return state.materializeNext();
  }

  @Override
  public int materializeSkip(final int count) {
    return state.materializeSkip(count);
  }

  private class ImmaterialState implements IteratorMaterializer<Integer> {

    private final IndexedPredicate<? super E> predicate;
    private final IteratorMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final IteratorMaterializer<E> wrapped,
        @NotNull final IndexedPredicate<? super E> predicate) {
      this.wrapped = wrapped;
      this.predicate = predicate;
    }

    @Override
    public int knownSize() {
      return -1;
    }

    @Override
    public boolean materializeHasNext() {
      try {
        final IteratorMaterializer<E> wrapped = this.wrapped;
        final IndexedPredicate<? super E> predicate = this.predicate;
        boolean found = false;
        int last = 0;
        int i = 0;
        while (wrapped.materializeHasNext()) {
          if (predicate.test(i, wrapped.materializeNext())) {
            last = i;
            found = true;
          }
          ++i;
        }
        if (found) {
          state = new ElementToIteratorMaterializer<Integer>(last);
          return true;
        }
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
      state = EmptyIteratorMaterializer.instance();
      return false;
    }

    @Override
    public Integer materializeNext() {
      if (!materializeHasNext()) {
        throw new NoSuchElementException();
      }
      return state.materializeNext();
    }

    @Override
    public int materializeSkip(final int count) {
      if (count > 0) {
        return materializeHasNext() ? 1 : 0;
      }
      return 0;
    }
  }
}