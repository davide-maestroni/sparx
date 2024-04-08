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

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;
import sparx.collection.ListMaterializer;
import sparx.util.Require;
import sparx.util.SizeOverflowException;

class RemoveFractionListMaterializer<E> implements ListMaterializer<E> {

  private final long length;
  private final int start;
  private final ListMaterializer<E> wrapped;

  RemoveFractionListMaterializer(@NotNull final ListMaterializer<E> wrapped, final int start,
      final int maxLength) {
    Require.positive(maxLength, "maxLength");
    this.wrapped = Require.notNull(wrapped, "wrapped");
    this.start = start;
    length = start >= 0 ? maxLength : Math.max(0, (long) start + maxLength);
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    if (index < 0) {
      return false;
    }
    if (start <= index) {
      final long wrappedIndex = index + length;
      return wrappedIndex <= Integer.MAX_VALUE && wrapped.canMaterializeElement((int) wrappedIndex);
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
      final long size = Math.min(knownSize, Math.max(Math.max(0, start), knownSize - length));
      return SizeOverflowException.safeCast(size);
    }
    return -1;
  }

  @Override
  public E materializeElement(final int index) {
    if (index < 0) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    if (start <= index) {
      final long wrappedIndex = index + length;
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
    return (int) Math.max(Math.max(0, start), wrapped.materializeSize() - length);
  }

  private class RemoveIterator implements Iterator<E> {

    private final int index = Math.max(0, start);

    private long pos = 0;

    @Override
    public boolean hasNext() {
      final long pos = this.pos;
      if (pos == index) {
        return wrapped.canMaterializeElement(SizeOverflowException.safeCast(pos + length));
      }
      return wrapped.canMaterializeElement((int) pos);
    }

    @Override
    public E next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      final ListMaterializer<E> wrapped = RemoveFractionListMaterializer.this.wrapped;
      if (!wrapped.canMaterializeElement((int) pos)) {
        throw new NoSuchElementException();
      }
      if (pos == index) {
        pos += length;
        if (!wrapped.canMaterializeElement((int) pos)) {
          throw new NoSuchElementException();
        }
      }
      return wrapped.materializeElement((int) pos++);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove");
    }
  }
}