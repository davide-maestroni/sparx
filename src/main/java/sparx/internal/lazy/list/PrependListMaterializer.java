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
import org.jetbrains.annotations.NotNull;
import sparx.util.Require;
import sparx.util.SizeOverflowException;

public class PrependListMaterializer<E> implements ListMaterializer<E> {

  private final E element;
  private final ListMaterializer<E> wrapped;

  public PrependListMaterializer(@NotNull final ListMaterializer<E> wrapped, final E element) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
    this.element = element;
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    return index == 0 || wrapped.canMaterializeElement(index - 1);
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
    if (index < 0) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    if (index == 0) {
      return element;
    }
    return wrapped.materializeElement(index - 1);
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
    return new PrependIterator();
  }

  @Override
  public int materializeSize() {
    return SizeOverflowException.safeCast((long) wrapped.materializeSize() + 1);
  }

  private class PrependIterator implements Iterator<E> {

    private final Iterator<E> iterator = wrapped.materializeIterator();

    private boolean consumedElement;

    @Override
    public boolean hasNext() {
      return !consumedElement || iterator.hasNext();
    }

    @Override
    public E next() {
      if (!consumedElement) {
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
