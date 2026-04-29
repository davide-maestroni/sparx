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

import java.util.NoSuchElementException;
import sparx1.internal.lazy.ListMaterializer;
import sparx1.util.IndexOverflowException;
import sparx1.util.SizeOverflowException;
import sparx1.util.annotation.NotNegative;
import sparx1.util.annotation.NotNull;

public class AppendAllListMaterializer<E> implements ListMaterializer<E> {

  private final ListMaterializer<E> elementsMaterializer;
  private final ListMaterializer<E> wrapped;

  public AppendAllListMaterializer(final @NotNull ListMaterializer<E> wrapped,
      final @NotNull ListMaterializer<E> elementsMaterializer) {
    this.wrapped = wrapped;
    this.elementsMaterializer = elementsMaterializer;
  }

  @Override
  public boolean canMaterializeElement(final @NotNegative int index) {
    final ListMaterializer<E> wrapped = this.wrapped;
    if (wrapped.canMaterializeElement(index)) {
      return true;
    }
    final int wrappedSize = wrapped.materializeSize();
    return wrappedSize <= index && elementsMaterializer.canMaterializeElement(index - wrappedSize);
  }

  @Override
  public boolean isRandomAccess() {
    return wrapped.isRandomAccess() && elementsMaterializer.isRandomAccess();
  }

  @Override
  public boolean isSizeKnown() {
    return wrapped.isSizeKnown() && elementsMaterializer.isSizeKnown();
  }

  @Override
  public int knownSize() {
    final int knownSize = wrapped.knownSize();
    if (knownSize >= 0) {
      final int elementsSize = elementsMaterializer.knownSize();
      if (elementsSize >= 0) {
        return SizeOverflowException.safeCast((long) knownSize + elementsSize);
      }
    }
    return -1;
  }

  @Override
  public @NotNull IndexedIterator<E> materializeBackwardIterator(final @NotNegative int index) {
    final ListMaterializer<E> wrapped = this.wrapped;
    final int wrappedSize = wrapped.materializeSize();
    if (index < wrappedSize) {
      return wrapped.materializeBackwardIterator(index);
    }
    return new OrderedIterator<E>(
        elementsMaterializer.materializeBackwardIterator(index - wrappedSize),
        wrapped.materializeBackwardIterator(wrappedSize - 1));
  }

  @Override
  public boolean materializeContains(final Object element) {
    return wrapped.materializeContains(element) || elementsMaterializer.materializeContains(
        element);
  }

  @Override
  public E materializeElement(final @NotNegative int index) {
    final ListMaterializer<E> wrapped = this.wrapped;
    if (wrapped.canMaterializeElement(index)) {
      return wrapped.materializeElement(index);
    }
    final int wrappedSize = wrapped.materializeSize();
    if (wrappedSize > index) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    return elementsMaterializer.materializeElement(index - wrappedSize);
  }

  @Override
  public int materializeElements() {
    return SizeOverflowException.safeCast(
        (long) wrapped.materializeElements() + elementsMaterializer.materializeElements());
  }

  @Override
  public boolean materializeEmpty() {
    return wrapped.materializeEmpty() && elementsMaterializer.materializeEmpty();
  }

  @Override
  public @NotNull IndexedIterator<E> materializeForwardIterator(final @NotNegative int index) {
    final ListMaterializer<E> wrapped = this.wrapped;
    final int wrappedSize = wrapped.materializeSize();
    if (index < wrappedSize) {
      return new OrderedIterator<E>(wrapped.materializeForwardIterator(index),
          elementsMaterializer.materializeForwardIterator(0));
    }
    return elementsMaterializer.materializeForwardIterator(index - wrappedSize);
  }

  @Override
  public int materializeSize() {
    return SizeOverflowException.safeCast(
        (long) wrapped.materializeSize() + elementsMaterializer.materializeSize());
  }

  @Override
  public @NotNull IndexedIterator<E> materializeUnorderedIterator() {
    return new UnorderedIterator<E>(wrapped.materializeUnorderedIterator(),
        elementsMaterializer.materializeUnorderedIterator());
  }

  private static class OrderedIterator<E> implements IndexedIterator<E> {

    private final IndexedIterator<E> appendIterator;

    private boolean consumedElements;
    private IndexedIterator<E> iterator;

    private OrderedIterator(final @NotNull IndexedIterator<E> iterator,
        final @NotNull IndexedIterator<E> appendIterator) {
      this.iterator = iterator;
      this.appendIterator = appendIterator;
    }

    @Override
    public boolean hasNext() {
      if (iterator.hasNext()) {
        return true;
      }
      if (!consumedElements) {
        consumedElements = true;
        iterator = appendIterator;
      }
      return iterator.hasNext();
    }

    @Override
    public E next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      return iterator.next();
    }

    @Override
    public int nextIndex() {
      return IndexOverflowException.safeCast(
          (long) iterator.nextIndex() + appendIterator.nextIndex());
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove");
    }
  }

  private static class UnorderedIterator<E> extends OrderedIterator<E> {

    private int pos;

    private UnorderedIterator(final @NotNull IndexedIterator<E> iterator,
        final @NotNull IndexedIterator<E> appendIterator) {
      super(iterator, appendIterator);
    }

    @Override
    public E next() {
      final E next = super.next();
      ++pos;
      return next;
    }

    @Override
    public int nextIndex() {
      return pos;
    }
  }
}
