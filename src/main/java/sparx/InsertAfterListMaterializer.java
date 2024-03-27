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

class InsertAfterListMaterializer<E> implements ListMaterializer<E> {

  private final E element;
  private final int numElements;
  private final ListMaterializer<E> wrapped;

  InsertAfterListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      final int numElements, final E element) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
    this.numElements = Math.max(0, numElements);
    this.element = element;
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    if (index < 0) {
      return false;
    }
    if (index == 0) {
      return true;
    }
    if (numElements <= index) {
      return wrapped.canMaterializeElement(index - 1);
    }
    final ListMaterializer<E> wrapped = this.wrapped;
    if (wrapped.canMaterializeElement(index)) {
      return true;
    }
    final int size = wrapped.materializeSize();
    return index <= size;
  }

  @Override
  public int knownSize() {
    final int wrappedSize = wrapped.knownSize();
    if (wrappedSize >= 0) {
      return wrappedSize + 1;
    }
    return -1;
  }

  @Override
  public E materializeElement(final int index) {
    if (index < 0) {
      throw new IndexOutOfBoundsException(String.valueOf(index));
    }
    final int i = numElements;
    if (i == index) {
      return element;
    }
    if (i < index) {
      return wrapped.materializeElement(index - 1);
    }
    final ListMaterializer<E> wrapped = this.wrapped;
    if (wrapped.canMaterializeElement(index)) {
      return wrapped.materializeElement(index);
    }
    if (index > wrapped.materializeSize()) {
      throw new IndexOutOfBoundsException(String.valueOf(index));
    }
    return element;
  }

  @Override
  public boolean materializeEmpty() {
    return false;
  }

  @Override
  public @NotNull Iterator<E> materializeIterator() {
    return new InsertIterator();
  }

  @Override
  public int materializeSize() {
    return wrapped.materializeSize() + 1;
  }

  private class InsertIterator implements Iterator<E> {

    private final Iterator<E> iterator = wrapped.materializeIterator();

    private int pos = 0;

    @Override
    public boolean hasNext() {
      return iterator.hasNext() || pos <= numElements;
    }

    @Override
    public E next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      final int numElements = InsertAfterListMaterializer.this.numElements;
      if (pos != numElements) {
        final Iterator<E> iterator = this.iterator;
        if (iterator.hasNext()) {
          ++pos;
          return iterator.next();
        } else if (pos > numElements) {
          throw new NoSuchElementException();
        }
        pos = numElements + 1;
        return element;
      }
      ++pos;
      return element;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove");
    }
  }
}
