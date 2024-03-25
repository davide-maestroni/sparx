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

class RemoveSegmentListMaterializer<E> implements ListMaterializer<E> {

  private final int start;
  private final int shift;
  private final ListMaterializer<E> wrapped;

  RemoveSegmentListMaterializer(@NotNull final ListMaterializer<E> wrapped, final int start,
      final int maxSize) {
    Require.positive(maxSize, "maxSize");
    this.wrapped = Require.notNull(wrapped, "wrapped");
    this.start = start;
    shift = start >= 0 ? maxSize : Math.max(0, start + maxSize);
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    if (index < 0) {
      return false;
    }
    if (start <= index) {
      return wrapped.canMaterializeElement(index + shift);
    }
    return wrapped.canMaterializeElement(index);
  }

  @Override
  public int knownSize() {
    final int wrappedSize = wrapped.knownSize();
    if (wrappedSize >= 0) {
      if (wrappedSize == 0) {
        return 0;
      }
      return Math.max(Math.max(0, start), wrappedSize - shift);
    }
    return -1;
  }

  @Override
  public E materializeElement(final int index) {
    if (index < 0) {
      throw new IndexOutOfBoundsException(String.valueOf(index));
    }
    if (start <= index) {
      return wrapped.materializeElement(index + shift);
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
    return Math.max(Math.max(0, start), wrapped.materializeSize() - shift);
  }

  private class RemoveIterator implements Iterator<E> {

    private final int index = Math.max(0, start);
    private final Iterator<E> iterator = wrapped.materializeIterator();

    private int pos = 0;

    @Override
    public boolean hasNext() {
      if (pos == index) {
        return wrapped.canMaterializeElement(pos + shift);
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
      if (pos == index) {
        for (int i = 0; i < shift; ++i) {
          iterator.next();
        }
        if (!iterator.hasNext()) {
          throw new NoSuchElementException();
        }
        pos += shift;
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
