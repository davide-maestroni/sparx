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
package sparx.internal.lazy.list;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;
import sparx.util.SizeOverflowException;
import sparx.util.annotation.NotNegative;

public class AppendListMaterializer<E> implements ListMaterializer<E> {

  private final E element;
  private final ListMaterializer<E> wrapped;

  public AppendListMaterializer(@NotNull final ListMaterializer<E> wrapped, final E element) {
    this.wrapped = wrapped;
    this.element = element;
  }

  @Override
  public boolean canMaterializeElement(@NotNegative final int index) {
    final ListMaterializer<E> wrapped = this.wrapped;
    return wrapped.canMaterializeElement(index) || index == wrapped.materializeSize();
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
  public boolean materializeContains(final Object element) {
    if (element == this.element || (element != null && element.equals(this.element))) {
      return true;
    }
    return wrapped.materializeContains(element);
  }

  @Override
  public E materializeElement(final int index) {
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
  public @NotNull Iterator<E> materializeIterator() {
    return new AppendIterator();
  }

  @Override
  public int materializeSize() {
    return SizeOverflowException.safeCast((long) wrapped.materializeSize() + 1);
  }

  private class AppendIterator implements Iterator<E> {

    private final Iterator<E> iterator = wrapped.materializeIterator();

    private boolean consumedElement;

    @Override
    public boolean hasNext() {
      return iterator.hasNext() || !consumedElement;
    }

    @Override
    public E next() {
      final Iterator<E> iterator = this.iterator;
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
    public void remove() {
      throw new UnsupportedOperationException("remove");
    }
  }
}
