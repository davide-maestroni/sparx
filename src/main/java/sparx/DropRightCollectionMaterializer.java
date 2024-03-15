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
import org.jetbrains.annotations.NotNull;
import sparx.util.CollectionMaterializer;
import sparx.util.Require;

class DropRightCollectionMaterializer<E> implements CollectionMaterializer<E> {

  private final int maxElements;
  private final CollectionMaterializer<E> wrapped;

  DropRightCollectionMaterializer(@NotNull final CollectionMaterializer<E> wrapped,
      final int maxElements) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
    this.maxElements = Require.positive(maxElements, "maxElements");
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    final CollectionMaterializer<E> wrapped = this.wrapped;
    if (index >= wrapped.materializeSize() - maxElements) {
      return false;
    }
    return wrapped.canMaterializeElement(index);
  }

  @Override
  public E materializeElement(final int index) {
    final CollectionMaterializer<E> wrapped = this.wrapped;
    if (index >= wrapped.materializeSize() - maxElements) {
      throw new IndexOutOfBoundsException(String.valueOf(index));
    }
    return wrapped.materializeElement(index);
  }

  @Override
  public boolean materializeEmpty() {
    return wrapped.materializeSize() <= maxElements;
  }

  @Override
  public @NotNull Iterator<E> materializeIterator() {
    return new CollectionMaterializerIterator<E>(this);
  }

  @Override
  public int materializeSize() {
    return Math.max(0, wrapped.materializeSize() - maxElements);
  }
}
