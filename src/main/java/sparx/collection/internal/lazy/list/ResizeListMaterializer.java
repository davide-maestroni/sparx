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
import org.jetbrains.annotations.NotNull;
import sparx.util.Require;

public class ResizeListMaterializer<E> extends AbstractListMaterializer<E> implements
    ListMaterializer<E> {

  private final int numElements;
  private final E padding;
  private final ListMaterializer<E> wrapped;

  public ResizeListMaterializer(@NotNull final ListMaterializer<E> wrapped, final int numElements,
      final E padding) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
    this.numElements = Require.positive(numElements, "numElements");
    this.padding = padding;
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    return index >= 0 && index < numElements;
  }

  @Override
  public int knownSize() {
    return numElements;
  }

  @Override
  public boolean materializeContains(final Object element) {
    final int numElements = this.numElements;
    final ListMaterializer<E> wrapped = this.wrapped;
    final int wrappedSize = wrapped.materializeSize();
    if (wrappedSize == numElements) {
      return wrapped.materializeContains(element);
    }
    if (wrappedSize < numElements) {
      if (element == padding || (element != null && element.equals(padding))) {
        return true;
      }
      return wrapped.materializeContains(element);
    }
    return super.materializeContains(element);
  }

  @Override
  public E materializeElement(final int index) {
    if (index < 0 || index >= numElements) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    final ListMaterializer<E> wrapped = this.wrapped;
    if (wrapped.canMaterializeElement(index)) {
      return wrapped.materializeElement(index);
    }
    return padding;
  }

  @Override
  public int materializeElements() {
    wrapped.materializeElements();
    return numElements;
  }

  @Override
  public boolean materializeEmpty() {
    return false;
  }

  @Override
  public @NotNull Iterator<E> materializeIterator() {
    return new ListMaterializerIterator<E>(this);
  }

  @Override
  public int materializeSize() {
    return numElements;
  }
}
