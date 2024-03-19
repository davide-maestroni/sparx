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
import sparx.collection.ListMaterializer;
import sparx.util.Require;

class AppendAllListMaterializer<E> implements ListMaterializer<E> {

  private final ListMaterializer<E> elementsMaterializer;
  private final ListMaterializer<E> wrapped;

  AppendAllListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      @NotNull final ListMaterializer<E> elementsMaterializer) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
    this.elementsMaterializer = Require.notNull(elementsMaterializer, "elementsMaterializer");
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    final ListMaterializer<E> wrapped = this.wrapped;
    if (wrapped.canMaterializeElement(index)) {
      return true;
    }
    final int size = wrapped.materializeSize();
    return elementsMaterializer.canMaterializeElement(index - size);
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
    final ListMaterializer<E> wrapped = this.wrapped;
    if (wrapped.canMaterializeElement(index)) {
      return wrapped.materializeElement(index);
    }
    final int size = wrapped.materializeSize();
    return elementsMaterializer.materializeElement(index - size);
  }

  @Override
  public boolean materializeEmpty() {
    return wrapped.materializeEmpty() && elementsMaterializer.materializeEmpty();
  }

  @Override
  public @NotNull Iterator<E> materializeIterator() {
    return new AppendIterator();
  }

  @Override
  public int materializeSize() {
    return wrapped.materializeSize() + elementsMaterializer.materializeSize();
  }

  private class AppendIterator implements Iterator<E> {

    private boolean consumedElements;
    private Iterator<E> iterator = wrapped.materializeIterator();

    @Override
    public boolean hasNext() {
      if (iterator.hasNext()) {
        return true;
      }
      if (!consumedElements) {
        consumedElements = true;
        iterator = elementsMaterializer.materializeIterator();
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
    public void remove() {
      throw new UnsupportedOperationException("remove");
    }
  }
}
