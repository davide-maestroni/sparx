/*
 * Copyright 2026 Davide Maestroni
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
package sparx1.internal.lazy.list;

import sparx1.internal.lazy.ListMaterializer;
import sparx1.util.SizeOverflowException;
import sparx1.util.annotation.NotNegative;
import sparx1.util.annotation.NotNull;

public class InsertAllAfterListMaterializer<E> extends AbstractListMaterializer<E> {

  private final ListMaterializer<E> elementsMaterializer;
  private final int numElements;
  private final ListMaterializer<E> wrapped;

  public InsertAllAfterListMaterializer(final @NotNull ListMaterializer<E> wrapped,
      final @NotNegative int numElements, final @NotNull ListMaterializer<E> elementsMaterializer) {
    this.wrapped = wrapped;
    this.numElements = numElements;
    this.elementsMaterializer = elementsMaterializer;
  }

  @Override
  public boolean canMaterializeElement(final @NotNegative int index) {
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
      if (numElements >= wrapped.materializeSize()) {
        return false;
      }
      final ListMaterializer<E> elementsMaterializer = this.elementsMaterializer;
      if (elementsMaterializer.canMaterializeElement(index - numElements)) {
        return true;
      }
      final int wrappedIndex = index - elementsMaterializer.materializeSize();
      return wrappedIndex >= 0 && wrapped.canMaterializeElement(wrappedIndex);
    }
    return wrapped.canMaterializeElement(index);
  }

  @Override
  public boolean isRandomAccess() {
    return wrapped.isRandomAccess() && elementsMaterializer.isRandomAccess();
  }

  @Override
  public boolean isSizeKnown() {
    final int knownSize = wrapped.knownSize();
    if (knownSize >= 0) {
      return elementsMaterializer.isSizeKnown();
    }
    return false;
  }

  @Override
  public int knownSize() {
    final int knownSize = wrapped.knownSize();
    if (knownSize >= 0) {
      if (numElements < knownSize) {
        final int elementsSize = elementsMaterializer.knownSize();
        if (elementsSize >= 0) {
          return SizeOverflowException.safeCast((long) knownSize + elementsSize);
        }
      } else {
        return knownSize;
      }
    }
    return -1;
  }

  @Override
  public IndexedIterator<E> materializeBackwardIterator(final @NotNegative int index) {
    final ListMaterializer<E> elementsMaterializer = this.elementsMaterializer;
    final int numElements = this.numElements;
    if (index < numElements) {
      return new WrapBackwardIterator<E>(wrapped.materializeBackwardIterator(index), index);
    }
    final int elementsSize = elementsMaterializer.materializeSize();
    final int elementsIndex = index - numElements;
    if (elementsMaterializer.canMaterializeElement(elementsIndex)) {
      return new BackwardIterator<E>(wrapped.materializeBackwardIterator(index - elementsSize),
          elementsMaterializer.materializeBackwardIterator(elementsIndex), numElements);
    }
    return new BackwardIterator<E>(wrapped.materializeBackwardIterator(index - elementsSize),
        elementsMaterializer.materializeBackwardIterator(elementsSize - 1), numElements);
  }

  @Override
  public boolean materializeContains(final Object element) {
    final ListMaterializer<E> wrapped = this.wrapped;
    if (wrapped.materializeContains(element)) {
      return true;
    }
    final int numElements = this.numElements;
    if (wrapped.canMaterializeElement(numElements) || wrapped.materializeSize() == numElements) {
      return elementsMaterializer.materializeContains(element);
    }
    return false;
  }

  @Override
  public E materializeElement(final @NotNegative int index) {
    final int numElements = this.numElements;
    if (numElements == 0) {
      final ListMaterializer<E> elementsMaterializer = this.elementsMaterializer;
      if (elementsMaterializer.canMaterializeElement(index)) {
        return elementsMaterializer.materializeElement(index);
      }
      final int elementsSize = elementsMaterializer.materializeSize();
      if (index < elementsSize) {
        throw new IndexOutOfBoundsException(Integer.toString(index));
      }
      return wrapped.materializeElement(index - elementsSize);
    }
    final ListMaterializer<E> wrapped = this.wrapped;
    if (numElements <= index) {
      if (numElements >= wrapped.materializeSize()) {
        throw new IndexOutOfBoundsException(Integer.toString(index));
      }
      final int elementsIndex = index - numElements;
      final ListMaterializer<E> elementsMaterializer = this.elementsMaterializer;
      if (elementsIndex >= 0 && elementsMaterializer.canMaterializeElement(elementsIndex)) {
        return elementsMaterializer.materializeElement(elementsIndex);
      }
      final int elementsSize = elementsMaterializer.materializeSize();
      if (index < elementsSize) {
        throw new IndexOutOfBoundsException(Integer.toString(index));
      }
      return wrapped.materializeElement(index - elementsSize);
    }
    if (wrapped.canMaterializeElement(index)) {
      return wrapped.materializeElement(index);
    }
    throw new IndexOutOfBoundsException(Integer.toString(index));
  }

  @Override
  public int materializeElements() {
    final ListMaterializer<E> wrapped = this.wrapped;
    final long size = wrapped.materializeElements();
    if (numElements >= size) {
      return SizeOverflowException.safeCast(size + elementsMaterializer.materializeElements());
    }
    return (int) size;
  }

  @Override
  public boolean materializeEmpty() {
    if (wrapped.materializeEmpty()) {
      return numElements != 0 || elementsMaterializer.materializeEmpty();
    }
    return false;
  }

  @Override
  public IndexedIterator<E> materializeForwardIterator(final @NotNegative int index) {
    final ListMaterializer<E> elementsMaterializer = this.elementsMaterializer;
    final int numElements = this.numElements;
    if (index <= numElements) {
      return new ForwardIterator<E>(wrapped.materializeForwardIterator(index),
          elementsMaterializer.materializeForwardIterator(0), numElements);
    }
    final int elementsIndex = index - numElements;
    if (elementsMaterializer.canMaterializeElement(elementsIndex)) {
      return new ForwardIterator<E>(wrapped.materializeForwardIterator(index),
          elementsMaterializer.materializeForwardIterator(elementsIndex), numElements);
    }
    return new WrapForwardIterator<E>(
        wrapped.materializeForwardIterator(index - elementsMaterializer.materializeSize()), index);
  }

  @Override
  public int materializeSize() {
    final int wrappedSize = wrapped.materializeSize();
    if (wrappedSize < numElements) {
      return wrappedSize;
    }
    return SizeOverflowException.safeCast(
        (long) wrappedSize + elementsMaterializer.materializeSize());
  }

  private static class BackwardIterator<E> implements IndexedIterator<E> {

    private final IndexedIterator<E> elementsIterator;
    private final int numElements;
    private final IndexedIterator<E> wrappedIterator;

    private int pos;

    private BackwardIterator(final @NotNull IndexedIterator<E> wrappedIterator,
        final @NotNull IndexedIterator<E> elementsIterator, final @NotNegative int numElements) {
      this.wrappedIterator = wrappedIterator;
      this.elementsIterator = elementsIterator;
      this.numElements = numElements - 1;
      pos = wrappedIterator.nextIndex() + elementsIterator.nextIndex() + 1;
    }

    @Override
    public boolean hasNext() {
      final IndexedIterator<E> wrappedIterator = this.wrappedIterator;
      return wrappedIterator.hasNext() || (wrappedIterator.nextIndex() == numElements
          && elementsIterator.hasNext());
    }

    @Override
    public E next() {
      final IndexedIterator<E> wrappedIterator = this.wrappedIterator;
      if (wrappedIterator.nextIndex() == numElements) {
        final IndexedIterator<E> elementsIterator = this.elementsIterator;
        if (elementsIterator.hasNext()) {
          final E next = elementsIterator.next();
          --pos;
          return next;
        }
      }
      final E next = wrappedIterator.next();
      --pos;
      return next;
    }

    @Override
    public int nextIndex() {
      return pos;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove");
    }
  }

  private static class ForwardIterator<E> implements IndexedIterator<E> {

    private final IndexedIterator<E> elementsIterator;
    private final int numElements;
    private final IndexedIterator<E> wrappedIterator;

    private int pos;

    private ForwardIterator(final @NotNull IndexedIterator<E> wrappedIterator,
        final @NotNull IndexedIterator<E> elementsIterator, final @NotNegative int numElements) {
      this.wrappedIterator = wrappedIterator;
      this.elementsIterator = elementsIterator;
      this.numElements = numElements;
      pos = wrappedIterator.nextIndex() + elementsIterator.nextIndex() + 1;
    }

    @Override
    public boolean hasNext() {
      final IndexedIterator<E> wrappedIterator = this.wrappedIterator;
      return wrappedIterator.hasNext() || (wrappedIterator.nextIndex() == numElements
          && elementsIterator.hasNext());
    }

    @Override
    public E next() {
      final IndexedIterator<E> wrappedIterator = this.wrappedIterator;
      if (wrappedIterator.nextIndex() == numElements) {
        final IndexedIterator<E> elementsIterator = this.elementsIterator;
        if (elementsIterator.hasNext()) {
          final E next = elementsIterator.next();
          ++pos;
          return next;
        }
      }
      final E next = wrappedIterator.next();
      ++pos;
      return next;
    }

    @Override
    public int nextIndex() {
      return pos;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove");
    }
  }
}
