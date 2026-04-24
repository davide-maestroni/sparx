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
  public Iterator<E> materializeBackwardIterator(final @NotNegative int index) {
    if (index >= materializeSize()) {
      return Collections.<E>emptyList().iterator();
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
  public Iterator<E> materializeForwardIterator(final @NotNegative int index) {
    if (index >= materializeSize()) {
      return Collections.<E>emptyList().iterator();
    }
    return new ForwardIterator(index);
  }

  @Override
  public int materializeSize() {
    return Math.max(0, wrapped.materializeSize() - maxElements);
  }

  private class ForwardIterator implements Iterator<E> {

    private final Iterator<E> iterator;
    private final int size = materializeSize();

    private int pos;

    private ForwardIterator(final @NotNegative int index) {
      iterator = wrapped.materializeForwardIterator(index);
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
    public void remove() {
      throw new UnsupportedOperationException("remove");
    }
  }
}
