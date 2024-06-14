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
import sparx.util.DequeueList;
import sparx.util.UncheckedException;
import sparx.util.function.IndexedPredicate;

public class DropRightWhileIteratorMaterializer<E> extends AbstractIteratorMaterializer<E> {

  private volatile IteratorMaterializer<E> state;

  public DropRightWhileIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
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
  public E materializeNext() {
    return state.materializeNext();
  }

  private class ImmaterialState implements IteratorMaterializer<E> {

    private final DequeueList<E> elements = new DequeueList<E>();
    private final IndexedPredicate<? super E> predicate;
    private final IteratorMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final IteratorMaterializer<E> wrapped,
        @NotNull final IndexedPredicate<? super E> predicate) {
      this.wrapped = wrapped;
      this.predicate = predicate;
    }

    @Override
    public int knownSize() {
      final DequeueList<E> elements = this.elements;
      return !elements.isEmpty() ? elements.size() : -1;
    }

    @Override
    public boolean materializeHasNext() {
      final DequeueList<E> elements = this.elements;
      final IteratorMaterializer<E> wrapped = this.wrapped;
      if (elements.isEmpty()) {
        while (wrapped.materializeHasNext()) {
          elements.add(wrapped.materializeNext());
        }
        try {
          final IndexedPredicate<? super E> predicate = this.predicate;
          int i = elements.size() - 1;
          while (!elements.isEmpty() && predicate.test(i, elements.getLast())) {
            elements.removeLast();
            --i;
          }
        } catch (final Exception e) {
          throw UncheckedException.throwUnchecked(e);
        }
        if (elements.isEmpty()) {
          state = EmptyIteratorMaterializer.instance();
          return false;
        }
      }
      return true;
    }

    @Override
    public E materializeNext() {
      if (!materializeHasNext()) {
        throw new NoSuchElementException();
      }
      return elements.removeFirst();
    }

    @Override
    public int materializeSkip(final int count) {
      throw new UnsupportedOperationException();
    }
  }
}