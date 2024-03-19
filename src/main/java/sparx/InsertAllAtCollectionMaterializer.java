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
package sparx;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;
import sparx.collection.CollectionMaterializer;
import sparx.util.Require;

class InsertAllAtCollectionMaterializer<E> implements CollectionMaterializer<E> {

  private final CollectionMaterializer<E> elementsMaterializer;
  private final int index;
  private final CollectionMaterializer<E> wrapped;

  InsertAllAtCollectionMaterializer(@NotNull final CollectionMaterializer<E> wrapped,
      final int index, @NotNull final CollectionMaterializer<E> elementsMaterializer) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
    this.index = Math.max(0, index);
    this.elementsMaterializer = Require.notNull(elementsMaterializer, "elementsMaterializer");
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    if (index < 0) {
      return false;
    }
    if (this.index == 0) {
      final CollectionMaterializer<E> elementsMaterializer = this.elementsMaterializer;
      if (elementsMaterializer.canMaterializeElement(index)) {
        return true;
      }
      final int size = elementsMaterializer.materializeSize();
      return wrapped.canMaterializeElement(index + size);

    } else if (this.index <= index) {
      final CollectionMaterializer<E> elementsMaterializer = this.elementsMaterializer;
      if (elementsMaterializer.canMaterializeElement(index - this.index)) {
        return true;
      }
      final int size = elementsMaterializer.materializeSize();
      return wrapped.canMaterializeElement(index + size);

    } else {
      final CollectionMaterializer<E> wrapped = this.wrapped;
      if (wrapped.canMaterializeElement(index)) {
        return true;
      }
      final int size = wrapped.materializeSize();
      return elementsMaterializer.canMaterializeElement(index + size);
    }
  }

  @Override
  public int knownSize() {
    final int wrappedSize = wrapped.knownSize();
    if (wrappedSize >= 0) {
      final int elementsKnownSize = elementsMaterializer.knownSize();
      if (elementsKnownSize >= 0) {
        return wrappedSize + elementsKnownSize;
      }
    }
    return -1;
  }

  @Override
  public E materializeElement(final int index) {
    if (index < 0) {
      throw new IndexOutOfBoundsException(String.valueOf(index));
    }
    if (this.index == 0) {
      final CollectionMaterializer<E> elementsMaterializer = this.elementsMaterializer;
      if (elementsMaterializer.canMaterializeElement(index)) {
        return elementsMaterializer.materializeElement(index);
      }
      final int size = elementsMaterializer.materializeSize();
      return wrapped.materializeElement(index - size);

    } else if (this.index <= index) {
      final int i = index - this.index;
      final CollectionMaterializer<E> elementsMaterializer = this.elementsMaterializer;
      if (elementsMaterializer.canMaterializeElement(i)) {
        return elementsMaterializer.materializeElement(i);
      }
      final int size = elementsMaterializer.materializeSize();
      return wrapped.materializeElement(index - size);

    } else {
      final CollectionMaterializer<E> wrapped = this.wrapped;
      if (wrapped.canMaterializeElement(index)) {
        return wrapped.materializeElement(index);
      }
      final int size = wrapped.materializeSize();
      return elementsMaterializer.materializeElement(index - size);
    }
  }

  @Override
  public boolean materializeEmpty() {
    return wrapped.materializeEmpty() && elementsMaterializer.materializeEmpty();
  }

  @Override
  public @NotNull Iterator<E> materializeIterator() {
    return new InsertIterator();
  }

  @Override
  public int materializeSize() {
    return wrapped.materializeSize() + elementsMaterializer.materializeSize();
  }

  private class InsertIterator implements Iterator<E> {

    private final Iterator<E> iterator = wrapped.materializeIterator();
    private final Iterator<E> elementsIterator = elementsMaterializer.materializeIterator();

    private int pos = 0;

    @Override
    public boolean hasNext() {
      return iterator.hasNext() || elementsIterator.hasNext();
    }

    @Override
    public E next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      if (pos != index) {
        final Iterator<E> iterator = this.iterator;
        if (iterator.hasNext()) {
          ++pos;
          return iterator.next();
        }
        return elementsIterator.next();
      } else {
        final Iterator<E> elementsIterator = this.elementsIterator;
        if (elementsIterator.hasNext()) {
          return elementsIterator.next();
        }
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
