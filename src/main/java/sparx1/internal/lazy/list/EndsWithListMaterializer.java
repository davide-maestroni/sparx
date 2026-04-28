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

import java.util.Iterator;
import sparx1.internal.lazy.ListMaterializer;
import sparx1.util.annotation.NotNegative;
import sparx1.util.annotation.NotNull;
import sparx1.util.function.Functions;

public class EndsWithListMaterializer<E> extends SuppliedListMaterializer<Boolean> {

  private ListMaterializer<E> wrapped;
  private ListMaterializer<?> elementsMaterializer;

  public EndsWithListMaterializer(final @NotNull ListMaterializer<E> wrapped,
      final @NotNull ListMaterializer<?> elementsMaterializer) {
    this.wrapped = wrapped;
    this.elementsMaterializer = elementsMaterializer;
  }

  @Override
  public boolean canMaterializeElement(final @NotNegative int index) {
    return index == 0;
  }

  @Override
  public ListMaterializer<Boolean> get() {
    final ListMaterializer<E> wrapped = this.wrapped;
    final ListMaterializer<?> elementsMaterializer = this.elementsMaterializer;
    final int wrappedSize = wrapped.materializeSize();
    final int elementsSize = elementsMaterializer.materializeSize();
    if (wrappedSize < elementsSize) {
      this.wrapped = null;
      this.elementsMaterializer = null;
      return ElementToListMaterializer.FALSE;
    }
    final Iterator<E> wrappedIterator = wrapped.materializeForwardIterator(
        wrappedSize - elementsSize);
    final Iterator<?> elementsIterator = elementsMaterializer.materializeForwardIterator(0);
    while (wrappedIterator.hasNext() && elementsIterator.hasNext()) {
      final E left = wrappedIterator.next();
      final Object right = elementsIterator.next();
      if (!Functions.objectsEqual(left, right)) {
        this.wrapped = null;
        this.elementsMaterializer = null;
        return ElementToListMaterializer.FALSE;
      }
    }
    this.wrapped = null;
    this.elementsMaterializer = null;
    return ElementToListMaterializer.TRUE;
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
}
