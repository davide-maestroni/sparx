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

import java.util.HashSet;
import java.util.Iterator;
import sparx1.internal.lazy.ListMaterializer;
import sparx1.util.annotation.NotNegative;
import sparx1.util.annotation.NotNull;

public class IncludesAllListMaterializer<E> extends SuppliedListMaterializer<Boolean> {

  private Iterable<?> elements;
  private ListMaterializer<E> wrapped;

  public IncludesAllListMaterializer(final @NotNull ListMaterializer<E> wrapped,
      final @NotNull Iterable<?> elements) {
    this.wrapped = wrapped;
    this.elements = elements;
  }

  @Override
  public boolean canMaterializeElement(final @NotNegative int index) {
    return index == 0;
  }

  @Override
  public ListMaterializer<Boolean> get() throws Exception {
    final ListMaterializer<E> wrapped = this.wrapped;
    final Iterator<?> elementsIterator = this.elements.iterator();
    if (wrapped.materializeEmpty()) {
      clear();
      return elementsIterator.hasNext() ? ElementToListMaterializer.FALSE
          : ElementToListMaterializer.TRUE;
    }
    final IndexedIterator<E> iterator = wrapped.materializeUnorderedIterator();
    final HashSet<Object> elements = new HashSet<Object>();
    while (elementsIterator.hasNext()) {
      elements.add(elementsIterator.next());
    }
    while (iterator.hasNext()) {
      elements.remove(iterator.next());
    }
    clear();
    return elements.isEmpty() ? ElementToListMaterializer.TRUE : ElementToListMaterializer.FALSE;
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
    this.elements = null;
  }
}
