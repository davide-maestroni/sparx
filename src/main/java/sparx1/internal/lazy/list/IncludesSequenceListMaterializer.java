/*
 * Copyright 2026 Davide Maestroni
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
package sparx1.internal.lazy.list;

import static sparx1.util.function.Functions.objectsEqual;

import sparx1.internal.lazy.ListMaterializer;
import sparx1.util.annotation.NotNegative;
import sparx1.util.annotation.NotNull;

public class IncludesSequenceListMaterializer<E> extends SuppliedListMaterializer<Boolean> {

  private ListMaterializer<?> elementsMaterializer;
  private ListMaterializer<E> wrapped;

  public IncludesSequenceListMaterializer(final @NotNull ListMaterializer<E> wrapped,
      final @NotNull ListMaterializer<?> elementsMaterializer) {
    this.wrapped = wrapped;
    this.elementsMaterializer = elementsMaterializer;
  }

  @Override
  public boolean canMaterializeElement(final @NotNegative int index) {
    return index == 0;
  }

  @Override
  public ListMaterializer<Boolean> get() throws Exception {
    final ListMaterializer<E> wrapped = this.wrapped;
    final ListMaterializer<?> elementsMaterializer = this.elementsMaterializer;
    if (wrapped.materializeEmpty()) {
      clear();
      return elementsMaterializer.materializeEmpty() ? ElementToListMaterializer.TRUE
          : ElementToListMaterializer.FALSE;
    }
    final int maxIndex = wrapped.materializeSize() - elementsMaterializer.materializeSize();
    if (maxIndex < 0) {
      clear();
      return ElementToListMaterializer.FALSE;
    }
    for (int i = 0; i <= maxIndex; ++i) {
      final IndexedIterator<E> iterator = wrapped.materializeForwardIterator(i);
      final IndexedIterator<?> elementsIterator = elementsMaterializer.materializeForwardIterator(
          0);
      while (iterator.hasNext() && elementsIterator.hasNext()) {
        if (!objectsEqual(iterator.next(), elementsIterator.next())) {
          break;
        }
      }
      if (!elementsIterator.hasNext()) {
        clear();
        return ElementToListMaterializer.TRUE;
      }
    }
    clear();
    return ElementToListMaterializer.FALSE;
  }

  @Override
  public boolean isRandomAccess() {
    return true;
  }

  @Override
  public boolean isSizeKnown() {
    return true;
  }

  @Override
  public int knownSize() {
    return 1;
  }

  @Override
  public boolean materializeEmpty() {
    return false;
  }

  @Override
  public int materializeSize() {
    return 1;
  }

  private void clear() {
    this.wrapped = null;
    this.elementsMaterializer = null;
  }
}
