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

public class FindLastListMaterializer<E> extends SuppliedListMaterializer<E> {

  private ListMaterializer<E> wrapped;
  private IndexedPredicate<? super E> predicate;

  public FindLastListMaterializer(final @NotNull ListMaterializer<E> wrapped,
      final @NotNull IndexedPredicate<? super E> predicate) {
    this.wrapped = wrapped;
    this.predicate = predicate;
  }

  @Override
  public boolean canMaterializeElement(final @NotNegative int index) {
    return index == 0 && super.canMaterializeElement(0);
  }

  @Override
  public ListMaterializer<E> get() throws Exception {
    final ListMaterializer<E> wrapped = this.wrapped;
    if (wrapped.materializeEmpty()) {
      clear();
      return EmptyListMaterializer.instance();
    }
    final IndexedPredicate<? super E> predicate = this.predicate;
    final IndexedIterator<E> iterator = wrapped.materializeBackwardIterator(
        wrapped.materializeSize() - 1);
    while (iterator.hasNext()) {
      final int index = iterator.nextIndex();
      final E element = iterator.next();
      if (predicate.test(index, element)) {
        clear();
        return new ElementToListMaterializer<E>(element);
      }
    }
    clear();
    return EmptyListMaterializer.instance();
  }

  @Override
  public boolean isRandomAccess() {
    return true;
  }

  private void clear() {
    this.wrapped = null;
    this.predicate = null;
  }
}
