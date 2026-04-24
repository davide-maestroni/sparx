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
import java.util.Iterator;
import java.util.NoSuchElementException;
import sparx1.internal.lazy.ListMaterializer;
import sparx1.util.annotation.NotNegative;
import sparx1.util.annotation.NotNull;
import sparx1.util.annotation.Positive;

public class DropFirstListMaterializer<E> extends AbstractListMaterializer<E> {

  private final int maxElements;
  private final ListMaterializer<E> wrapped;

  public DropFirstListMaterializer(final @NotNull ListMaterializer<E> wrapped,
      final @Positive int maxElements) {
    this.wrapped = wrapped;
    this.maxElements = maxElements;
  }

  @Override
  public boolean canMaterializeElement(final @NotNegative int index) {
    final long wrappedIndex = (long) index + maxElements;
    return wrappedIndex < Integer.MAX_VALUE && wrapped.canMaterializeElement((int) wrappedIndex);
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
  public Iterator<E> materializeBackwardIterator(final @NotNegative int index) {
    final long wrappedIndex = (long) index + maxElements;
    if (wrappedIndex >= Integer.MAX_VALUE) {
      return Collections.<E>emptyList().iterator();
    }
    return new BackwardIterator(index);
  }

  @Override
  public E materializeElement(final @NotNegative int index) {
    final long wrappedIndex = (long) index + maxElements;
    if (wrappedIndex >= Integer.MAX_VALUE) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    return wrapped.materializeElement((int) wrappedIndex);
  }

  @Override
  public int materializeElements() {
    return Math.max(0, wrapped.materializeElements() - maxElements);
  }

  @Override
  public boolean materializeEmpty() {
    return !wrapped.canMaterializeElement(maxElements);
  }

  @Override
  public Iterator<E> materializeForwardIterator(final @NotNegative int index) {
    final long wrappedIndex = (long) index + maxElements;
    if (wrappedIndex >= Integer.MAX_VALUE) {
      return Collections.<E>emptyList().iterator();
    }
    return wrapped.materializeForwardIterator((int) wrappedIndex);
  }

  @Override
  public int materializeSize() {
    return Math.max(0, wrapped.materializeSize() - maxElements);
  }

  private class BackwardIterator implements Iterator<E> {

    private final Iterator<E> iterator;

    private int pos;

    private BackwardIterator(final @NotNegative int index) {
      iterator = wrapped.materializeBackwardIterator(index + maxElements);
      pos = index;
    }

    @Override
    public boolean hasNext() {
      return pos >= maxElements && iterator.hasNext();
    }

    @Override
    public E next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      final E next = iterator.next();
      --pos;
      return next;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove");
    }
  }
}
