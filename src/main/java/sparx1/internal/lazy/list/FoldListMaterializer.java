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
import sparx1.util.function.BinaryFunction;

public class FoldListMaterializer<E, F> extends SuppliedListMaterializer<F> {

  private ListMaterializer<E> wrapped;
  private F identity;
  private BinaryFunction<? super F, ? super E, ? extends F> operation;

  public FoldListMaterializer(final @NotNull ListMaterializer<E> wrapped, final F identity,
      final @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation) {
    this.wrapped = wrapped;
    this.identity = identity;
    this.operation = operation;
  }

  @Override
  public boolean canMaterializeElement(final @NotNegative int index) {
    return index == 0;
  }

  @Override
  public ListMaterializer<F> get() throws Exception {
    final ListMaterializer<E> wrapped = this.wrapped;
    F current = identity;
    if (!wrapped.materializeEmpty()) {
      final BinaryFunction<? super F, ? super E, ? extends F> operation = this.operation;
      final IndexedIterator<E> iterator = iterate(wrapped);
      while (iterator.hasNext()) {
        current = operation.apply(current, iterator.next());
      }
    }
    clear();
    return new ElementToListMaterializer<F>(current);
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

  @NotNull
  IndexedIterator<E> iterate(final @NotNull ListMaterializer<E> wrapped) {
    return wrapped.materializeUnorderedIterator();
  }

  private void clear() {
    this.wrapped = null;
    this.identity = null;
    this.operation = null;
  }
}
