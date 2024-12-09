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
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import sparx.util.IndexOverflowException;
import sparx.util.UncheckedException;
import sparx.util.annotation.NotNegative;
import sparx.util.function.IndexedPredicate;

public class TakeRightWhileListMaterializer<E> extends AbstractListMaterializer<E> implements
    ListMaterializer<E> {

  private final ListMaterializer<E> wrapped;

  private volatile State state;

  public TakeRightWhileListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      @NotNull final IndexedPredicate<? super E> predicate) {
    this.wrapped = wrapped;
    state = new ImmaterialState(predicate);
  }

  @Override
  public boolean canMaterializeElement(@NotNegative final int index) {
    final int maxElements = state.materialized();
    if (index >= maxElements) {
      return false;
    }
    final ListMaterializer<E> wrapped = this.wrapped;
    final long wrappedIndex = (long) index + Math.max(0, wrapped.materializeSize() - maxElements);
    return wrappedIndex < Integer.MAX_VALUE && wrapped.canMaterializeElement((int) wrappedIndex);
  }

  @Override
  public boolean isRandomAccess() {
    return wrapped.isRandomAccess();
  }

  @Override
  public int knownSize() {
    final int knownSize = wrapped.knownSize();
    if (knownSize == 0) {
      return 0;
    } else if (knownSize > 0) {
      final int stateSize = state.knownSize();
      if (stateSize >= 0) {
        return Math.min(knownSize, stateSize);
      }
    }
    return -1;
  }

  @Override
  public E materializeElement(@NotNegative final int index) {
    final int maxElements = state.materialized();
    if (index >= maxElements) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    final ListMaterializer<E> wrapped = this.wrapped;
    final long wrappedIndex = (long) index + Math.max(0, wrapped.materializeSize() - maxElements);
    return wrapped.materializeElement(IndexOverflowException.safeCast(wrappedIndex));
  }

  @Override
  public int materializeElements() {
    return Math.min(wrapped.materializeElements(), state.materialized());
  }

  @Override
  public boolean materializeEmpty() {
    final ListMaterializer<E> wrapped = this.wrapped;
    return wrapped.materializeEmpty() || state.materialized() == 0;
  }

  @Override
  public @NotNull Iterator<E> materializeIterator() {
    return new TakeIterator();
  }

  @Override
  public int materializeSize() {
    return Math.min(wrapped.materializeSize(), state.materialized());
  }

  private interface State {

    int knownSize();

    int materialized();
  }

  private static class ElementsState implements State {

    private final int elements;

    private ElementsState(final int elements) {
      this.elements = elements;
    }

    @Override
    public int knownSize() {
      return elements;
    }

    @Override
    public int materialized() {
      return elements;
    }
  }

  private class ImmaterialState implements State {

    private final AtomicBoolean isMaterialized = new AtomicBoolean(false);
    private final IndexedPredicate<? super E> predicate;

    private ImmaterialState(@NotNull final IndexedPredicate<? super E> predicate) {
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
        final ListMaterializer<E> wrapped = TakeRightWhileListMaterializer.this.wrapped;
        final IndexedPredicate<? super E> predicate = this.predicate;
        if (wrapped.isRandomAccess()) {
          final int size = wrapped.materializeSize();
          int i = size - 1;
          for (; i >= 0; --i) {
            if (!predicate.test(i, wrapped.materializeElement(i))) {
              break;
            }
          }
          final int elements = size - i - 1;
          state = new ElementsState(elements);
          return elements;
        } else {
          final Iterator<E> iterator = wrapped.materializeIterator();
          int i = 0;
          int elements = -1;
          while (iterator.hasNext()) {
            if (!predicate.test(i++, iterator.next())) {
              elements = i;
            }
          }
          if (elements >= 0) {
            state = new ElementsState(elements);
            return elements;
          }
          state = new ElementsState(i);
          return i;
        }
      } catch (final Exception e) {
        isMaterialized.set(false);
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }

  private class TakeIterator implements Iterator<E> {

    private final Iterator<E> iterator = wrapped.materializeIterator();

    private long pos;

    @Override
    public boolean hasNext() {
      return pos < state.materialized() && iterator.hasNext();
    }

    @Override
    public E next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      ++pos;
      return iterator.next();
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove");
    }
  }
}
