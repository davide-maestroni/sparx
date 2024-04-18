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
package sparx.collection.internal.list;

import java.util.Iterator;
import org.jetbrains.annotations.NotNull;
import sparx.collection.internal.AbstractCollectionMaterializer;
import sparx.util.Require;

public class DropListMaterializer<E> extends AbstractCollectionMaterializer<E> implements
    ListMaterializer<E> {

  private final int maxElements;
  private final ListMaterializer<E> wrapped;

  public DropListMaterializer(@NotNull final ListMaterializer<E> wrapped, final int maxElements) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
    this.maxElements = Require.positive(maxElements, "maxElements");
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    final long wrappedIndex = (long) index + maxElements;
    return index >= 0 && wrappedIndex < Integer.MAX_VALUE && wrapped.canMaterializeElement(
        (int) wrappedIndex);
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
    final long wrappedIndex = (long) index + maxElements;
    if (index < 0 || wrappedIndex >= Integer.MAX_VALUE) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    return wrapped.materializeElement((int) wrappedIndex);
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
