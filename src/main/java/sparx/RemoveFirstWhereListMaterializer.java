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
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import sparx.collection.ListMaterializer;
import sparx.util.Require;
import sparx.util.UncheckedException;
import sparx.util.function.Predicate;

class RemoveFirstWhereListMaterializer<E> implements ListMaterializer<E> {

  private final ListMaterializer<E> wrapped;

  private volatile State state;

  RemoveFirstWhereListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      @NotNull final Predicate<? super E> predicate) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
    state = new ImmaterialState(Require.notNull(predicate, "predicate"));
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    if (index < 0) {
      return false;
    }
    if (state.materializeUntil(index) <= index) {
      return wrapped.canMaterializeElement(index + 1);
    }
    return wrapped.canMaterializeElement(index);
  }

  @Override
  public int knownSize() {
    final int knownSize = wrapped.knownSize();
    if (knownSize >= 0) {
      if (knownSize == 0) {
        return 0;
      }
      final int index = state.known();
      if (index >= 0 && knownSize > index) {
        return knownSize - 1;
      }
    }
    return -1;
  }

  @Override
  public E materializeElement(final int index) {
    if (index < 0) {
      throw new IndexOutOfBoundsException(String.valueOf(index));
    }
    if (state.materializeUntil(index) <= index) {
      return wrapped.materializeElement(index + 1);
    }
    return wrapped.materializeElement(index);
  }

  @Override
  public boolean materializeEmpty() {
    if (wrapped.materializeEmpty()) {
      return true;
    }
    return materializeSize() == 0;
  }

  @Override
  public @NotNull Iterator<E> materializeIterator() {
    return new RemoveIterator();
  }

  @Override
  public int materializeSize() {
    final int size = wrapped.materializeSize();
    if (size > state.materializeUntil(size)) {
      return size - 1;
    }
    return size;
  }

  private interface State {

    int known();

    int materializeUntil(int index);
  }

  private static class IndexState implements State {

    private final int index;

    private IndexState(final int index) {
      this.index = index;
    }

    @Override
    public int known() {
      return index;
    }

    @Override
    public int materializeUntil(final int index) {
      return this.index;
    }
  }

  private class ImmaterialState implements State {

    private final Iterator<E> iterator = wrapped.materializeIterator();
    private final AtomicInteger modCount = new AtomicInteger();
    private final Predicate<? super E> predicate;

    private int pos;

    private ImmaterialState(@NotNull final Predicate<? super E> predicate) {
      this.predicate = predicate;
    }

    @Override
    public int known() {
      return -1;
    }

    @Override
    public int materializeUntil(final int index) {
      final Iterator<E> iterator = this.iterator;
      final Predicate<? super E> predicate = this.predicate;
      final AtomicInteger modCount = this.modCount;
      final int expectedCount = modCount.getAndIncrement() + 1;
      try {
        while (pos <= index && iterator.hasNext()) {
          if (predicate.test(iterator.next())) {
            if (expectedCount != modCount.get()) {
              throw new ConcurrentModificationException();
            }
            state = new IndexState(pos);
            return pos;
          }
          ++pos;
        }
        if (expectedCount != modCount.get()) {
          throw new ConcurrentModificationException();
        }
        if (!iterator.hasNext()) {
          state = new IndexState(pos);
        }
        return pos;
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }

  private class RemoveIterator implements Iterator<E> {

    private final Iterator<E> iterator = wrapped.materializeIterator();

    private int pos = 0;

    @Override
    public boolean hasNext() {
      final int pos = this.pos;
      if (pos == state.materializeUntil(pos)) {
        return wrapped.canMaterializeElement(pos + 1);
      }
      return iterator.hasNext();
    }

    @Override
    public E next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      final Iterator<E> iterator = this.iterator;
      if (!iterator.hasNext()) {
        throw new NoSuchElementException();
      }
      if (pos == state.materializeUntil(pos)) {
        iterator.next();
        if (!iterator.hasNext()) {
          throw new NoSuchElementException();
        }
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
