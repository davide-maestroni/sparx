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

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;
import sparx.collection.ListMaterializer;
import sparx.util.Require;
import sparx.util.IndexOverflowException;

public class InsertAllAfterListMaterializer<E> implements ListMaterializer<E> {

  private final ListMaterializer<E> elementsMaterializer;
  private final int numElements;
  private final ListMaterializer<E> wrapped;

  public InsertAllAfterListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      final int numElements, @NotNull final ListMaterializer<E> elementsMaterializer) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
    this.numElements = Require.notNegative(numElements, "numElements");
    this.elementsMaterializer = Require.notNull(elementsMaterializer, "elementsMaterializer");
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    if (index < 0) {
      return false;
    }
    final int numElements = this.numElements;
    if (numElements == 0) {
      final ListMaterializer<E> elementsMaterializer = this.elementsMaterializer;
      if (elementsMaterializer.canMaterializeElement(index)) {
        return true;
      }
      final long wrappedIndex = (long) index + elementsMaterializer.materializeSize();
      return wrappedIndex < Integer.MAX_VALUE && wrapped.canMaterializeElement((int) wrappedIndex);
    }
    if (numElements <= index) {
      final ListMaterializer<E> elementsMaterializer = this.elementsMaterializer;
      if (elementsMaterializer.canMaterializeElement(index - numElements)) {
        return true;
      }
      final long wrappedIndex = (long) index + elementsMaterializer.materializeSize();
      return wrappedIndex < Integer.MAX_VALUE && wrapped.canMaterializeElement((int) wrappedIndex);
    }
    final ListMaterializer<E> wrapped = this.wrapped;
    if (wrapped.canMaterializeElement(index)) {
      return true;
    }
    final int wrappedSize = wrapped.materializeSize();
    if (wrappedSize < numElements) {
      final long wrappedIndex = (long) index + wrappedSize;
      return wrappedIndex < Integer.MAX_VALUE && elementsMaterializer.canMaterializeElement(
          (int) wrappedIndex);
    }
    return false;
  }

  @Override
  public int knownSize() {
    final int knownSize = wrapped.knownSize();
    if (knownSize >= 0) {
      if (knownSize < numElements) {
        final int elementsSize = elementsMaterializer.knownSize();
        if (elementsSize >= 0) {
          return IndexOverflowException.safeCast((long) knownSize + elementsSize);
        }
      } else {
        return knownSize;
      }
    }
    return -1;
  }

  @Override
  public E materializeElement(final int index) {
    if (index < 0) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    final int numElements = this.numElements;
    if (numElements == 0) {
      final ListMaterializer<E> elementsMaterializer = this.elementsMaterializer;
      if (elementsMaterializer.canMaterializeElement(index)) {
        return elementsMaterializer.materializeElement(index);
      }
      final int elementsSize = elementsMaterializer.materializeSize();
      return wrapped.materializeElement(index - elementsSize);
    }
    if (numElements <= index) {
      final int elementsIndex = index - numElements;
      final ListMaterializer<E> elementsMaterializer = this.elementsMaterializer;
      if (elementsMaterializer.canMaterializeElement(elementsIndex)) {
        return elementsMaterializer.materializeElement(elementsIndex);
      }
      final int elementsSize = elementsMaterializer.materializeSize();
      return wrapped.materializeElement(index - elementsSize);
    }
    final ListMaterializer<E> wrapped = this.wrapped;
    if (wrapped.canMaterializeElement(index)) {
      return wrapped.materializeElement(index);
    }
    final int wrappedSize = wrapped.materializeSize();
    if (wrappedSize < numElements) {
      return elementsMaterializer.materializeElement(index - wrappedSize);
    }
    throw new IndexOutOfBoundsException(Integer.toString(index));
  }

  @Override
  public boolean materializeEmpty() {
    if (wrapped.materializeEmpty()) {
      return numElements != 0 || elementsMaterializer.materializeEmpty();
    }
    return false;
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
    return IndexOverflowException.safeCast(
        (long) wrappedSize + elementsMaterializer.materializeSize());
  }

  private class InsertIterator implements Iterator<E> {

    private final Iterator<E> elementsIterator = elementsMaterializer.materializeIterator();

    private int pos;

    @Override
    public boolean hasNext() {
      final int pos = this.pos;
      return wrapped.canMaterializeElement(pos) || (pos == numElements
          && elementsIterator.hasNext());
    }

    @Override
    public E next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      if (pos == numElements) {
        final Iterator<E> elementsIterator = this.elementsIterator;
        if (elementsIterator.hasNext()) {
          return elementsIterator.next();
        }
      }
      return wrapped.materializeElement(pos++);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove");
    }
  }
}
