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

class InsertAfterListMaterializer<E> implements ListMaterializer<E> {

  private final E element;
  private final int numElements;
  private final ListMaterializer<E> wrapped;

  InsertAfterListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      final int numElements, final E element) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
    this.numElements = Require.notNegative(numElements, "numElements");
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
    return wrapped.canMaterializeElement(index) || index <= wrapped.materializeSize();
  }

  @Override
  public int knownSize() {
    final int knownSize = wrapped.knownSize();
    if (knownSize >= 0) {
      if (knownSize < numElements) {
        return SizeOverflowException.safeCast((long) knownSize + 1);
      }
      return knownSize;
    }
    return -1;
  }

  @Override
  public E materializeElement(final int index) {
    if (index < 0) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    final int numElements = this.numElements;
    if (numElements == index) {
      return element;
    }
    if (numElements < index) {
      return wrapped.materializeElement(index - 1);
    }
    final ListMaterializer<E> wrapped = this.wrapped;
    if (wrapped.canMaterializeElement(index)) {
      return wrapped.materializeElement(index);
    }
    final int wrappedSize = wrapped.materializeSize();
    if (wrappedSize < numElements) {
      if (index > wrappedSize) {
        throw new IndexOutOfBoundsException(Integer.toString(index));
      }
      return element;
    }
    throw new IndexOutOfBoundsException(Integer.toString(index));
  }

  @Override
  public boolean materializeEmpty() {
    return wrapped.materializeEmpty() && numElements != 0;
  }

  @Override
  public @NotNull Iterator<E> materializeIterator() {
    return new InsertIterator();
  }

  @Override
  public int materializeSize() {
    final int wrappedSize = wrapped.materializeSize();
    if (wrappedSize < numElements) {
      return wrappedSize;
    }
    return SizeOverflowException.safeCast((long) wrappedSize + 1);
  }

  private class InsertIterator implements Iterator<E> {

    private int pos;

    @Override
    public boolean hasNext() {
      final int pos = this.pos;
      return wrapped.canMaterializeElement(pos) || pos == numElements;
    }

    @Override
    public E next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      final int numElements = InsertAfterListMaterializer.this.numElements;
      final int i = this.pos;
      if (i != numElements) {
        return wrapped.materializeElement(pos++);
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
