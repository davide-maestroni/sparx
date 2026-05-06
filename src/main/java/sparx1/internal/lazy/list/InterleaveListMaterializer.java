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

public class InterleaveListMaterializer<E> extends AbstractListMaterializer<E> {

  private final ListMaterializer<E> elementsMaterializer;
  private final ListMaterializer<E> wrapped;

  public InterleaveListMaterializer(final @NotNull ListMaterializer<E> wrapped,
      final @NotNull ListMaterializer<E> elementsMaterializer) {
    this.wrapped = wrapped;
    this.elementsMaterializer = elementsMaterializer;
  }

  @Override
  public boolean canMaterializeElement(final @NotNegative int index) {
    final int i = index >> 1;
    return wrapped.canMaterializeElement(i) && elementsMaterializer.canMaterializeElement(i);
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
        final long min = Math.min(knownSize, elementsSize);
        return SizeOverflowException.safeCast(min << 1);
      }
    }
    return -1;
  }

  @Override
  public IndexedIterator<E> materializeBackwardIterator(final @NotNegative int index) {
    final ListMaterializer<E> wrapped = this.wrapped;
    final ListMaterializer<E> elementsMaterializer = this.elementsMaterializer;
    final boolean isWrapped;
    final ListMaterializer<E> materializer;
    if ((index & 0x1) == 0) {
      isWrapped = true;
      materializer = wrapped;
    } else {
      isWrapped = false;
      materializer = elementsMaterializer;
    }
    final int i = index >> 1;
    if (!materializer.canMaterializeElement(i)) {
      return EmptyListMaterializer.backwardIterator();
    }
    return new BackwardIterator<E>(wrapped.materializeBackwardIterator(i),
        elementsMaterializer.materializeBackwardIterator(i), isWrapped, index);
  }

  @Override
  public E materializeElement(final @NotNegative int index) {
    final ListMaterializer<E> materializer = (index & 0x1) == 0 ? wrapped : elementsMaterializer;
    final int i = index >> 1;
    if (!materializer.canMaterializeElement(i)) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    return materializer.materializeElement(i);
  }

  @Override
  public int materializeElements() {
    long size = 0;
    final IndexedIterator<E> iterator = materializeForwardIterator(0);
    while (iterator.hasNext()) {
      iterator.next();
      ++size;
    }
    return SizeOverflowException.safeCast(size);
  }

  @Override
  public boolean materializeEmpty() {
    return wrapped.materializeEmpty() || elementsMaterializer.materializeEmpty();
  }

  @Override
  public IndexedIterator<E> materializeForwardIterator(final @NotNegative int index) {
    final ListMaterializer<E> wrapped = this.wrapped;
    final ListMaterializer<E> elementsMaterializer = this.elementsMaterializer;
    final boolean isWrapped;
    final ListMaterializer<E> materializer;
    if ((index & 0x1) == 0) {
      isWrapped = true;
      materializer = wrapped;
    } else {
      isWrapped = false;
      materializer = elementsMaterializer;
    }
    final int i = index >> 1;
    if (!materializer.canMaterializeElement(i)) {
      return EmptyListMaterializer.forwardIterator();
    }
    return new ForwardIterator<E>(wrapped.materializeBackwardIterator(i),
        elementsMaterializer.materializeBackwardIterator(i), isWrapped, index);
  }

  @Override
  public int materializeSize() {
    final int wrappedSize = wrapped.materializeSize();
    if (wrappedSize > 0) {
      final int elementsSize = elementsMaterializer.materializeSize();
      final long min = Math.min(wrappedSize, elementsSize);
      return SizeOverflowException.safeCast(min << 1);
    }
    return 0;
  }

  private static class BackwardIterator<E> implements IndexedIterator<E> {

    private final IndexedIterator<E> elementsIterator;
    private final IndexedIterator<E> wrappedIterator;

    private boolean isWrapped;
    private int pos;

    private BackwardIterator(final @NotNull IndexedIterator<E> wrappedIterator,
        final @NotNull IndexedIterator<E> elementsIterator, final boolean isWrapped,
        final @NotNegative int index) {
      this.wrappedIterator = wrappedIterator;
      this.elementsIterator = elementsIterator;
      this.isWrapped = isWrapped;
      pos = index;
    }

    @Override
    public boolean hasNext() {
      return wrappedIterator.hasNext();
    }

    @Override
    public E next() {
      final E next = isWrapped ? wrappedIterator.next() : elementsIterator.next();
      isWrapped = !isWrapped;
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
    private final IndexedIterator<E> wrappedIterator;

    private boolean isWrapped;
    private int pos;

    private ForwardIterator(final @NotNull IndexedIterator<E> wrappedIterator,
        final @NotNull IndexedIterator<E> elementsIterator, final boolean isWrapped,
        final @NotNegative int index) {
      this.wrappedIterator = wrappedIterator;
      this.elementsIterator = elementsIterator;
      this.isWrapped = isWrapped;
      pos = index;
    }

    @Override
    public boolean hasNext() {
      if (!elementsIterator.hasNext()) {
        return false;
      }
      return !isWrapped || wrappedIterator.hasNext();
    }

    @Override
    public E next() {
      final E next = isWrapped ? wrappedIterator.next() : elementsIterator.next();
      isWrapped = !isWrapped;
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
