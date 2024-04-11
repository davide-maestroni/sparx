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
import sparx.collection.ListMaterializer;
import sparx.util.Require;

class DropRightListMaterializer<E> implements ListMaterializer<E> {

  private final int maxElements;
  private final ListMaterializer<E> wrapped;

  DropRightListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      final int maxElements) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
    this.maxElements = Require.positive(maxElements, "maxElements");
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    return index >= 0 && index < wrapped.materializeSize() - maxElements;
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
  public E materializeElement(final int index) {
    final ListMaterializer<E> wrapped = this.wrapped;
    if (index < 0 || index >= wrapped.materializeSize() - maxElements) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    return wrapped.materializeElement(index);
  }

  @Override
  public boolean materializeEmpty() {
    return !wrapped.canMaterializeElement(maxElements);
  }

  @Override
  public @NotNull Iterator<E> materializeIterator() {
    return new ListMaterializerIterator<E>(this);
  }

  @Override
  public int materializeSize() {
    return Math.max(0, wrapped.materializeSize() - maxElements);
  }
}
