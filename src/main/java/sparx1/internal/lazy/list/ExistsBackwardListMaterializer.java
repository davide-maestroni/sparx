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

import sparx1.internal.lazy.ListMaterializer;
import sparx1.util.annotation.NotNegative;
import sparx1.util.annotation.NotNull;
import sparx1.util.function.IndexedPredicate;

public class ExistsBackwardListMaterializer<E> extends SuppliedListMaterializer<Boolean> {

  private final boolean defaultResult;

  private ListMaterializer<E> wrapped;
  private IndexedPredicate<? super E> predicate;

  public ExistsBackwardListMaterializer(final @NotNull ListMaterializer<E> wrapped,
      final @NotNull IndexedPredicate<? super E> predicate, final boolean defaultResult) {
    this.wrapped = wrapped;
    this.predicate = predicate;
    this.defaultResult = defaultResult;
  }

  @Override
  public boolean canMaterializeElement(final @NotNegative int index) {
    return index == 0;
  }

  @Override
  public ListMaterializer<Boolean> get() throws Exception {
    final ListMaterializer<E> wrapped = this.wrapped;
    if (wrapped.materializeEmpty()) {
      this.wrapped = null;
      this.predicate = null;
      return defaultResult ? ElementToListMaterializer.TRUE : ElementToListMaterializer.FALSE;
    }
    final IndexedIterator<E> iterator = wrapped.materializeForwardIterator(
        wrapped.materializeSize() - 1);
    final IndexedPredicate<? super E> predicate = this.predicate;
    do {
      if (predicate.test(iterator.nextIndex(), iterator.next())) {
        this.wrapped = null;
        this.predicate = null;
        return ElementToListMaterializer.TRUE;
      }
    } while (iterator.hasNext());
    this.wrapped = null;
    this.predicate = null;
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
}
