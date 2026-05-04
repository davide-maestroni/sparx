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

import java.util.Iterator;
import java.util.NoSuchElementException;
import sparx1.internal.lazy.ListMaterializer;
import sparx1.util.annotation.NotNegative;
import sparx1.util.annotation.NotNull;
import sparx1.util.annotation.Positive;

public class DropLastListMaterializer<E> extends AbstractListMaterializer<E> {

  private final int maxElements;
  private final ListMaterializer<E> wrapped;

  public DropLastListMaterializer(final @NotNull ListMaterializer<E> wrapped,
      final @Positive int maxElements) {
    this.wrapped = wrapped;
    this.maxElements = maxElements;
  }

  @Override
  public boolean canMaterializeElement(final @NotNegative int index) {
    return index < wrapped.materializeSize() - maxElements;
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
      return Math.max(0, knownSize - maxElements);
    }
    return -1;
  }

  @Override
  public @NotNull IndexedIterator<E> materializeBackwardIterator(final @NotNegative int index) {
    if (index >= materializeSize()) {
      return EmptyListMaterializer.backwardIterator();
    }
    return wrapped.materializeBackwardIterator(index);
  }

  @Override
  public E materializeElement(final @NotNegative int index) {
    final ListMaterializer<E> wrapped = this.wrapped;
    if (index >= wrapped.materializeSize() - maxElements) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    return wrapped.materializeElement(index);
  }

  @Override
  public int materializeElements() {
    return Math.max(0, wrapped.materializeElements() - maxElements);
  }

  @Override
  public boolean materializeEmpty() {
    final ListMaterializer<E> wrapped = this.wrapped;
    return wrapped.materializeEmpty() || wrapped.materializeSize() <= maxElements;
  }

  @Override
  public @NotNull IndexedIterator<E> materializeForwardIterator(final @NotNegative int index) {
    if (index >= materializeSize()) {
      return EmptyListMaterializer.forwardIterator();
    }
    return new OrderedIterator<E>(wrapped.materializeForwardIterator(index), index,
        materializeSize());
  }

  @Override
  public int materializeSize() {
    return Math.max(0, wrapped.materializeSize() - maxElements);
  }

  @Override
  public @NotNull IndexedIterator<E> materializeUnorderedIterator() {
    return new UnorderedIterator<E>(wrapped.materializeUnorderedIterator(), materializeSize());
  }

  private static class OrderedIterator<E> implements IndexedIterator<E> {

    private final Iterator<E> iterator;
    private final int size;

    private int pos;

    private OrderedIterator(final @NotNull Iterator<E> iterator, final @NotNegative int index,
        final int size) {
      this.iterator = iterator;
      this.size = size;
      pos = index;
    }

    @Override
    public boolean hasNext() {
      return pos < size && iterator.hasNext();
    }

    @Override
    public E next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      ++pos;
      return iterator.next();
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

  private static class UnorderedIterator<E> implements IndexedIterator<E> {

    private final IndexedIterator<E> iterator;
    private final int size;

    private UnorderedIterator(final @NotNull IndexedIterator<E> iterator, final int size) {
      this.iterator = iterator;
      this.size = size;
    }

    @Override
    public boolean hasNext() {
      final IndexedIterator<E> iterator = this.iterator;
      while (iterator.hasNext() && iterator.nextIndex() >= size) {
        iterator.next();
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
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public int nextIndex() {
      hasNext();
      return iterator.nextIndex();
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove");
    }
  }
}
