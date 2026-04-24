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
import sparx1.util.UncheckedException;
import sparx1.util.annotation.NotNull;
import sparx1.util.function.IndexedPredicate;

public class DropFirstWhileListMaterializer<E> extends SuppliedListMaterializer<E> {

  private IndexedPredicate<? super E> predicate;
  private ListMaterializer<E> wrapped;

  public DropFirstWhileListMaterializer(final @NotNull ListMaterializer<E> wrapped,
      final @NotNull IndexedPredicate<? super E> predicate) {
    this.wrapped = wrapped;
    this.predicate = predicate;
  }

  @Override
  public ListMaterializer<E> get() throws Exception {
    try {
      final ListMaterializer<E> wrapped = this.wrapped;
      final IndexedPredicate<? super E> predicate = this.predicate;
      int i = 0;
      final Iterator<E> iterator = wrapped.materializeForwardIterator(0);
      while (iterator.hasNext() && predicate.test(i, iterator.next())) {
        ++i;
      }
      this.wrapped = null;
      this.predicate = null;
      return new DropFirstListMaterializer<E>(wrapped, i);
    } catch (final Exception e) {
      throw UncheckedException.throwUnchecked(e);
    }
  }
}
