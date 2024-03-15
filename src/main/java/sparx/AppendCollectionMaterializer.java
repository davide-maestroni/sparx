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
import sparx.util.CollectionMaterializer;
import sparx.util.Require;

class AppendCollectionMaterializer<E> implements CollectionMaterializer<E> {

  private final E element;
  private final CollectionMaterializer<E> wrapped;

  AppendCollectionMaterializer(@NotNull final CollectionMaterializer<E> wrapped, final E element) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
    this.element = element;
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    final CollectionMaterializer<E> wrapped = this.wrapped;
    if (wrapped.canMaterializeElement(index)) {
      return true;
    }
    final int size = wrapped.materializeSize();
    return index == size;
  }

  @Override
  public E materializeElement(final int index) {
    final CollectionMaterializer<E> wrapped = this.wrapped;
    if (wrapped.canMaterializeElement(index)) {
      return wrapped.materializeElement(index);
    }
    final int size = wrapped.materializeSize();
    if (index != size) {
      throw new IndexOutOfBoundsException(String.valueOf(index));
    }
    return element;
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
    return wrapped.materializeSize() + 1;
  }

  private class AppendIterator implements Iterator<E> {

    private final Iterator<E> iterator = wrapped.materializeIterator();

    private boolean consumedElement;

    @Override
    public boolean hasNext() {
      if (iterator.hasNext()) {
        return true;
      }
      return !consumedElement;
    }

    @Override
    public E next() {
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
