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

class RemoveSectionListMaterializer<E> implements ListMaterializer<E> {

  private final int length;
  private final int start;
  private final ListMaterializer<E> wrapped;

  RemoveSectionListMaterializer(@NotNull final ListMaterializer<E> wrapped, final int start,
      final int maxLength) {
    Require.positive(maxLength, "maxLength");
    this.wrapped = Require.notNull(wrapped, "wrapped");
    this.start = start;
    length = start >= 0 ? maxLength : Math.max(0, start + maxLength);
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    if (index < 0) {
      return false;
    }
    if (start <= index) {
      return wrapped.canMaterializeElement(index + length);
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
      return Math.max(Math.max(0, start), knownSize - length);
    }
    return -1;
  }

  @Override
  public E materializeElement(final int index) {
    if (index < 0) {
      throw new IndexOutOfBoundsException(String.valueOf(index));
    }
    if (start <= index) {
      return wrapped.materializeElement(index + length);
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
    return Math.max(Math.max(0, start), wrapped.materializeSize() - length);
  }

  private class RemoveIterator implements Iterator<E> {

    private final int index = Math.max(0, start);
    private final Iterator<E> iterator = wrapped.materializeIterator();

    private int pos = 0;

    @Override
    public boolean hasNext() {
      final int pos = this.pos;
      if (pos == index) {
        return wrapped.canMaterializeElement(pos + length);
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
        for (int i = 0; i < length; ++i) {
          iterator.next();
        }
        if (!iterator.hasNext()) {
          throw new NoSuchElementException();
        }
        pos += length;
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
