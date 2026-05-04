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

import java.util.Collections;
import java.util.NoSuchElementException;
import sparx1.internal.lazy.ListMaterializer;
import sparx1.util.IndexOverflowException;
import sparx1.util.SizeOverflowException;
import sparx1.util.annotation.NotNegative;
import sparx1.util.annotation.NotNull;
import sparx1.util.function.Functions;

public class AppendListMaterializer<E> implements ListMaterializer<E> {

  private final E element;
  private final ListMaterializer<E> wrapped;

  public AppendListMaterializer(final @NotNull ListMaterializer<E> wrapped, final E element) {
    this.wrapped = wrapped;
    this.element = element;
  }

  @Override
  public boolean canMaterializeElement(final @NotNegative int index) {
    final ListMaterializer<E> wrapped = this.wrapped;
    return wrapped.canMaterializeElement(index) || index == wrapped.materializeSize();
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
      return SizeOverflowException.safeCast((long) knownSize + 1);
    }
    return -1;
  }

  @Override
  public @NotNull IndexedIterator<E> materializeBackwardIterator(final @NotNegative int index) {
    final int size = materializeSize();
    if (index >= size) {
      return EmptyListMaterializer.backwardIterator();
    }
    if (index == size - 1) {
      if (index == 0) {
        return new WrapBackwardIterator<E>(Collections.singleton(element).iterator());
      }
      return new PrependIterator<E>(wrapped.materializeBackwardIterator(index - 1), element);
    }
    return wrapped.materializeBackwardIterator(index);
  }

  @Override
  public boolean materializeContains(final Object element) {
    if (Functions.objectsEqual(element, this.element)) {
      return true;
    }
    return wrapped.materializeContains(element);
  }

  @Override
  public E materializeElement(final @NotNegative int index) {
    final ListMaterializer<E> wrapped = this.wrapped;
    if (wrapped.canMaterializeElement(index)) {
      return wrapped.materializeElement(index);
    }
    if (index != wrapped.materializeSize()) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    return element;
  }

  @Override
  public int materializeElements() {
    return SizeOverflowException.safeCast((long) wrapped.materializeElements() + 1);
  }

  @Override
  public boolean materializeEmpty() {
    return false;
  }

  @Override
  public @NotNull IndexedIterator<E> materializeForwardIterator(final @NotNegative int index) {
    final int size = materializeSize();
    if (index >= size) {
      return EmptyListMaterializer.forwardIterator();
    }
    if (index == size - 1) {
      return new WrapForwardIterator<E>(Collections.singleton(element).iterator(), index);
    }
    return new AppendIterator<E>(wrapped.materializeForwardIterator(index), element);
  }

  @Override
  public int materializeSize() {
    return SizeOverflowException.safeCast((long) wrapped.materializeSize() + 1);
  }

  @Override
  public @NotNull IndexedIterator<E> materializeUnorderedIterator() {
    return new UnorderedIterator<E>(wrapped.materializeUnorderedIterator(), element);
  }

  private static class AppendIterator<E> implements IndexedIterator<E> {

    private final E element;
    private final IndexedIterator<E> iterator;

    private boolean consumedElement;

    private AppendIterator(final @NotNull IndexedIterator<E> iterator, final E element) {
      this.iterator = iterator;
      this.element = element;
    }

    @Override
    public boolean hasNext() {
      return !consumedElement || iterator.hasNext();
    }

    @Override
    public E next() {
      final IndexedIterator<E> iterator = this.iterator;
      if (!iterator.hasNext()) {
        if (consumedElement) {
          throw new NoSuchElementException();
        }
        consumedElement = true;
        return element;
      }
      return iterator.next();
    }

    @Override
    public int nextIndex() {
      return consumedElement ? IndexOverflowException.safeCast(iterator.nextIndex() + 1L)
          : iterator.nextIndex();
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove");
    }
  }

  private static class PrependIterator<E> implements IndexedIterator<E> {

    private final E element;
    private final IndexedIterator<E> iterator;

    private boolean consumedElement;

    private PrependIterator(final @NotNull IndexedIterator<E> iterator, final E element) {
      this.iterator = iterator;
      this.element = element;
    }

    @Override
    public boolean hasNext() {
      return !consumedElement || iterator.hasNext();
    }

    @Override
    public E next() {
      if (consumedElement) {
        return iterator.next();
      }
      consumedElement = true;
      return element;
    }

    @Override
    public int nextIndex() {
      return consumedElement ? iterator.nextIndex()
          : IndexOverflowException.safeCast(iterator.nextIndex() + 1L);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove");
    }
  }

  private static class UnorderedIterator<E> extends AppendIterator<E> {

    private int pos;

    private UnorderedIterator(final @NotNull IndexedIterator<E> iterator, final E element) {
      super(iterator, element);
    }

    @Override
    public E next() {
      final E next = super.next();
      pos = IndexOverflowException.safeCast(pos + 1L);
      return next;
    }

    @Override
    public int nextIndex() {
      return super.consumedElement ? pos : super.iterator.nextIndex();
    }
  }
}
