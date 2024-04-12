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
package sparx.collection.internal.list;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import sparx.collection.ListMaterializer;
import sparx.util.Require;
import sparx.util.UncheckedException;
import sparx.util.function.Predicate;

public class RemoveLastWhereListMaterializer<E> implements ListMaterializer<E> {

  private final ListMaterializer<E> wrapped;

  private volatile State state;

  public RemoveLastWhereListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      @NotNull final Predicate<? super E> predicate) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
    state = new ImmaterialState(Require.notNull(predicate, "predicate"));
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    if (index < 0) {
      return false;
    }
    if (state.materialized() <= index) {
      final long wrappedIndex = (long) index + 1;
      return wrappedIndex < Integer.MAX_VALUE && wrapped.canMaterializeElement((int) wrappedIndex);
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
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    if (state.materialized() <= index) {
      final long wrappedIndex = (long) index + 1;
      if (wrappedIndex >= Integer.MAX_VALUE) {
        throw new IndexOutOfBoundsException(Integer.toString(index));
      }
      return wrapped.materializeElement((int) wrappedIndex);
    }
    return wrapped.materializeElement(index);
  }

  @Override
  public boolean materializeEmpty() {
    return wrapped.materializeEmpty() || materializeSize() == 0;
  }

  @Override
  public @NotNull Iterator<E> materializeIterator() {
    return new RemoveIterator();
  }

  @Override
  public int materializeSize() {
    final int wrappedSize = wrapped.materializeSize();
    if (wrappedSize > state.materialized()) {
      return wrappedSize - 1;
    }
    return wrappedSize;
  }

  private interface State {

    int known();

    int materialized();
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
    public int materialized() {
      return index;
    }
  }

  private class ImmaterialState implements State {

    private final AtomicBoolean isMaterialized = new AtomicBoolean(false);
    private final Predicate<? super E> predicate;

    private ImmaterialState(@NotNull final Predicate<? super E> predicate) {
      this.predicate = predicate;
    }

    @Override
    public int known() {
      return -1;
    }

    @Override
    public int materialized() {
      if (!isMaterialized.compareAndSet(false, true)) {
        throw new ConcurrentModificationException();
      }
      try {
        final ListMaterializer<E> wrapped = RemoveLastWhereListMaterializer.this.wrapped;
        final Predicate<? super E> predicate = this.predicate;
        final int size = wrapped.materializeSize();
        for (int i = size - 1; i >= 0; --i) {
          final E element = wrapped.materializeElement(i);
          if (predicate.test(element)) {
            state = new IndexState(i);
            return i;
          }
        }
        state = new IndexState(size);
        return size;
      } catch (final Exception e) {
        isMaterialized.set(false);
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }

  private class RemoveIterator implements Iterator<E> {

    private int pos = 0;

    @Override
    public boolean hasNext() {
      final int pos = this.pos;
      if (pos == state.materialized()) {
        final long wrappedIndex = (long) pos + 1;
        return wrappedIndex < Integer.MAX_VALUE && wrapped.canMaterializeElement(
            (int) wrappedIndex);
      }
      return wrapped.canMaterializeElement(pos);
    }

    @Override
    public E next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      if (pos == state.materialized()) {
        ++pos;
      }
      return wrapped.materializeElement(pos++);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove");
    }
  }
}
