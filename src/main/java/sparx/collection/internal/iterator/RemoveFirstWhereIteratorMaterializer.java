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

import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;
import sparx.util.Require;
import sparx.util.UncheckedException;
import sparx.util.function.IndexedPredicate;

public class RemoveFirstWhereIteratorMaterializer<E> extends AbstractIteratorMaterializer<E> {

  private volatile IteratorMaterializer<E> state;

  public RemoveFirstWhereIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      @NotNull final IndexedPredicate<? super E> predicate) {
    state = new ImmaterialState(Require.notNull(wrapped, "wrapped"),
        Require.notNull(predicate, "predicate"));
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
  public E materializeNext() {
    return state.materializeNext();
  }

  private class ImmaterialState implements IteratorMaterializer<E> {

    private final IndexedPredicate<? super E> predicate;
    private final IteratorMaterializer<E> wrapped;

    private boolean hasNext;
    private E next;
    private int pos;

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
      if (hasNext) {
        return true;
      }
      final IteratorMaterializer<E> wrapped = this.wrapped;
      if (wrapped.materializeHasNext()) {
        final IndexedPredicate<? super E> predicate = this.predicate;
        final int pos = this.pos;
        ++this.pos;
        E next = wrapped.materializeNext();
        try {
          if (!predicate.test(pos, next)) {
            this.next = next;
            return hasNext = true;
          }
        } catch (final Exception e) {
          throw UncheckedException.throwUnchecked(e);
        }
        if ((state = wrapped).materializeHasNext()) {
          return true;
        }
      }
      state = EmptyIteratorMaterializer.instance();
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
      return state.materializeNext();
    }

    @Override
    public int materializeSkip(final int count) {
      throw new UnsupportedOperationException();
    }
  }
}
