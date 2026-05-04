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
import sparx1.util.IndexOverflowException;
import sparx1.util.SizeOverflowException;
import sparx1.util.annotation.NotNegative;
import sparx1.util.annotation.NotNull;

public class InsertAfterListMaterializer<E> extends AbstractListMaterializer<E> {

  private final E element;
  private final int numElements;
  private final ListMaterializer<E> wrapped;

  public InsertAfterListMaterializer(final @NotNull ListMaterializer<E> wrapped,
      final @NotNegative int numElements, final E element) {
    this.wrapped = wrapped;
    this.numElements = numElements;
    this.element = element;
  }

  @Override
  public boolean canMaterializeElement(final @NotNegative int index) {
    final int numElements = this.numElements;
    if (numElements == index) {
      return true;
    }
    if (numElements < index) {
      return index >= 1 && wrapped.canMaterializeElement(index - 1);
    }
    return wrapped.canMaterializeElement(index);
  }

  @Override
  public boolean isRandomAccess() {
    return wrapped.isRandomAccess();
  }

  @Override
  public boolean isSizeKnown() {
    return wrapped.isSizeKnown();
  }

  @Override
  public int knownSize() {
    final int knownSize = wrapped.knownSize();
    if (knownSize >= 0) {
      if (knownSize >= numElements) {
        return SizeOverflowException.safeCast((long) knownSize + 1);
      }
      return knownSize;
    }
    return -1;
  }

  @Override
  public IndexedIterator<E> materializeBackwardIterator(final @NotNegative int index) {
    final int numElements = this.numElements;
    if (numElements < index) {
      return new BackwardIterator<E>(wrapped.materializeBackwardIterator(index - 1), numElements,
          element);
    }
    if (numElements == index) {
      return new BackwardIterator<E>(wrapped.materializeBackwardIterator(index), numElements,
          element);
    }
    return wrapped.materializeBackwardIterator(index);
  }

  @Override
  public boolean materializeContains(final Object element) {
    final ListMaterializer<E> wrapped = this.wrapped;
    if (wrapped.materializeSize() < numElements) {
      return wrapped.materializeContains(element);
    }
    return super.materializeContains(element);
  }

  @Override
  public E materializeElement(final @NotNegative int index) {
    final int numElements = this.numElements;
    if (numElements == index) {
      return element;
    }
    if (numElements < index) {
      return wrapped.materializeElement(index - 1);
    }
    final ListMaterializer<E> wrapped = this.wrapped;
    if (!wrapped.canMaterializeElement(index)) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    return wrapped.materializeElement(index);
  }

  @Override
  public int materializeElements() {
    final int size = wrapped.materializeElements();
    if (size < numElements) {
      return size;
    }
    return SizeOverflowException.safeCast((long) size + 1);
  }

  @Override
  public boolean materializeEmpty() {
    return wrapped.materializeEmpty() && numElements != 0;
  }

  @Override
  public IndexedIterator<E> materializeForwardIterator(final @NotNegative int index) {
    if (numElements > index) {
      return new WrapForwardIterator<E>(wrapped.materializeForwardIterator(index - 1), index);
    }
    return new ForwardIterator<E>(wrapped.materializeForwardIterator(index), numElements, element);
  }

  @Override
  public int materializeSize() {
    final int wrappedSize = wrapped.materializeSize();
    if (wrappedSize < numElements) {
      return wrappedSize;
    }
    return SizeOverflowException.safeCast((long) wrappedSize + 1);
  }

  @Override
  public IndexedIterator<E> materializeUnorderedIterator() {
    return new ForwardIterator<E>(wrapped.materializeUnorderedIterator(), numElements, element);
  }

  private static class BackwardIterator<E> implements IndexedIterator<E> {

    private final E element;
    private final int elementIndex;
    private final IndexedIterator<E> iterator;

    private boolean consumedElement;

    private BackwardIterator(final @NotNull IndexedIterator<E> iterator,
        final @NotNegative int numElements, final E element) {
      this.iterator = iterator;
      this.element = element;
      elementIndex = numElements - 1;
    }

    @Override
    public boolean hasNext() {
      final IndexedIterator<E> iterator = this.iterator;
      if (iterator.nextIndex() == elementIndex && !consumedElement) {
        return true;
      }
      return iterator.hasNext();
    }

    @Override
    public E next() {
      final IndexedIterator<E> iterator = this.iterator;
      final int index = iterator.nextIndex();
      if (iterator.nextIndex() == index && !consumedElement) {
        consumedElement = true;
        return element;
      }
      return iterator.next();
    }

    @Override
    public int nextIndex() {
      final IndexedIterator<E> iterator = this.iterator;
      final int index = iterator.nextIndex();
      final int elementIndex = this.elementIndex;
      return elementIndex < index || (elementIndex == index && !consumedElement)
          ? IndexOverflowException.safeCast(index + 1L) : index;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove");
    }
  }

  private static class ForwardIterator<E> implements IndexedIterator<E> {

    private final E element;
    private final IndexedIterator<E> iterator;
    private final int numElements;

    private boolean consumedElement;

    private ForwardIterator(final @NotNull IndexedIterator<E> iterator,
        final @NotNegative int numElements, final E element) {
      this.iterator = iterator;
      this.numElements = numElements;
      this.element = element;
    }

    @Override
    public boolean hasNext() {
      final IndexedIterator<E> iterator = this.iterator;
      final int index = iterator.nextIndex();
      final int numElements = this.numElements;
      if (numElements == index && !consumedElement) {
        return true;
      }
      return iterator.hasNext();
    }

    @Override
    public E next() {
      final IndexedIterator<E> iterator = this.iterator;
      final int index = iterator.nextIndex();
      final int numElements = this.numElements;
      if (numElements == index && !consumedElement) {
        consumedElement = true;
        return element;
      }
      return iterator.next();
    }

    @Override
    public int nextIndex() {
      final IndexedIterator<E> iterator = this.iterator;
      final int index = iterator.nextIndex();
      final int numElements = this.numElements;
      return numElements < index || (numElements == index && consumedElement)
          ? IndexOverflowException.safeCast(index + 1L) : index;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove");
    }
  }
}
