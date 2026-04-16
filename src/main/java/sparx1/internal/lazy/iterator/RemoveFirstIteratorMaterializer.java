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
import sparx1.util.function.IndexedPredicate;

public class RemoveFirstIteratorMaterializer<E> extends StatefulIteratorMaterializer<E> {

  public RemoveFirstIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      final @NotNull IndexedPredicate<? super E> predicate) {
    setState(new InitialState(wrapped, predicate));
  }

  private class InitialState extends AbstractStateIteratorMaterializer {

    private final IndexedPredicate<? super E> predicate;
    private final IteratorMaterializer<E> wrapped;

    private boolean hasNext;
    private E next;
    private int pos;

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
    public boolean isSizeKnown() {
      return false;
    }

    @Override
    public boolean materializeHasNext() {
      if (hasNext) {
        return true;
      }
      final IteratorMaterializer<E> wrapped = this.wrapped;
      if (wrapped.materializeHasNext()) {
        final IndexedPredicate<? super E> predicate = this.predicate;
        final int pos = this.pos++;
        E next = wrapped.materializeNext();
        try {
          if (!predicate.test(pos, next)) {
            this.next = next;
            return hasNext = true;
          }
        } catch (final Exception e) {
          throw UncheckedException.throwUnchecked(e);
        }
        if (setState(wrapped).materializeHasNext()) {
          return true;
        }
      }
      setEmptyState();
      return false;
    }

    @Override
    public E materializeNext() {
      if (hasNext) {
        hasNext = false;
        final E next = this.next;
        this.next = null;
        return next;
      }
      if (!materializeHasNext()) {
        throw new NoSuchElementException();
      }
      return getState().materializeNext();
    }
  }
}
