/*
 * Copyright 2026 Davide Maestroni
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
package sparx1.internal.lazy.iterator;

import java.util.NoSuchElementException;
import sparx1.internal.lazy.IteratorMaterializer;
import sparx1.util.UncheckedException;
import sparx1.util.annotation.NotNull;
import sparx1.util.annotation.Positive;
import sparx1.util.function.IndexedPredicate;

public class FindLastIndexIteratorMaterializer<E> extends StatefulIteratorMaterializer<Integer> {

  public FindLastIndexIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      final @NotNull IndexedPredicate<? super E> predicate) {
    setState(new InitialState(wrapped, predicate));
  }

  private class InitialState implements IteratorMaterializer<Integer> {

    private final IndexedPredicate<? super E> predicate;
    private final IteratorMaterializer<E> wrapped;

    private InitialState(final @NotNull IteratorMaterializer<E> wrapped,
        final @NotNull IndexedPredicate<? super E> predicate) {
      this.wrapped = wrapped;
      this.predicate = predicate;
    }

    @Override
    public int currentKnownSize() {
      return -1;
    }

    @Override
    public boolean materializeHasNext() {
      try {
        final IteratorMaterializer<E> wrapped = this.wrapped;
        final IndexedPredicate<? super E> predicate = this.predicate;
        boolean found = false;
        int last = 0;
        int pos = 0;
        while (wrapped.materializeHasNext()) {
          if (predicate.test(pos, wrapped.materializeNext())) {
            last = pos;
            found = true;
          }
          ++pos;
        }
        if (found) {
          setState(new ElementToIteratorMaterializer<Integer>(last));
          return true;
        }
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
      setEmptyState();
      return false;
    }

    @Override
    public Integer materializeNext() {
      if (!materializeHasNext()) {
        throw new NoSuchElementException();
      }
      return getState().materializeNext();
    }

    @Override
    public int materializeSkip(final @Positive int count) {
      if (materializeHasNext()) {
        setEmptyState();
        return 1;
      }
      return 0;
    }
  }
}
