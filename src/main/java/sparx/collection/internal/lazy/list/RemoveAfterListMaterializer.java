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
package sparx.collection.internal.lazy.list;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;
import sparx.collection.internal.lazy.AbstractCollectionMaterializer;
import sparx.util.Require;

public class RemoveAfterListMaterializer<E> extends AbstractCollectionMaterializer<E> implements
    ListMaterializer<E> {

  private final int numElements;
  private final ListMaterializer<E> wrapped;

  public RemoveAfterListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      final int numElements) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
    this.numElements = Require.notNegative(numElements, "numElements");
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    if (index < 0) {
      return false;
    }
    if (numElements <= index) {
      final long wrappedIndex = (long) index + 1;
      return wrappedIndex < Integer.MAX_VALUE && wrapped.canMaterializeElement((int) wrappedIndex);
    }
    return wrapped.canMaterializeElement(index);
  }

  @Override
  public int knownSize() {
    final int knownSize = wrapped.knownSize();
    if (knownSize >= 0) {
      if (knownSize == 0) {
        return 0;
      }
      if (knownSize > numElements) {
        return knownSize - 1;
      }
      return knownSize;
    }
    return -1;
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
  public E materializeElement(final int index) {
    if (index < 0) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    if (numElements <= index) {
      final long wrappedIndex = (long) index + 1;
      if (wrappedIndex >= Integer.MAX_VALUE) {
        throw new IndexOutOfBoundsException(Integer.toString(index));
      }
      return wrapped.materializeElement((int) wrappedIndex);
    }
    return wrapped.materializeElement(index);
  }

  @Override
  public boolean materializeEmpty() {
    return wrapped.materializeEmpty() || materializeSize() == 0;
  }

  @Override
  public @NotNull Iterator<E> materializeIterator() {
    return new RemoveIterator();
  }

  @Override
  public int materializeSize() {
    final int wrappedSize = wrapped.materializeSize();
    if (wrappedSize > numElements) {
      return wrappedSize - 1;
    }
    return wrappedSize;
  }

  private class RemoveIterator implements Iterator<E> {

    private int pos = 0;

    @Override
    public boolean hasNext() {
      final int pos = this.pos;
      if (pos == numElements) {
        final long wrappedIndex = (long) pos + 1;
        return wrappedIndex < Integer.MAX_VALUE && wrapped.canMaterializeElement(
            (int) wrappedIndex);
      }
      return wrapped.canMaterializeElement(pos);
    }

    @Override
    public E next() {
      try {
        if (pos == numElements) {
          ++pos;
        }
        return wrapped.materializeElement(pos++);
      } catch (final IndexOutOfBoundsException ignored) {
        throw new NoSuchElementException();
      }
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove");
    }
  }
}
