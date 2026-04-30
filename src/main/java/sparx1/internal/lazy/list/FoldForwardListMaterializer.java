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
import sparx1.util.annotation.NotNull;
import sparx1.util.function.BinaryFunction;

public class FoldForwardListMaterializer<E, F> extends FoldListMaterializer<E, F> {

  public FoldForwardListMaterializer(final @NotNull ListMaterializer<E> wrapped, final F identity,
      final @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation) {
    super(wrapped, identity, operation);
  }

  @NotNull
  IndexedIterator<E> iterate(final @NotNull ListMaterializer<E> wrapped) {
    return wrapped.materializeForwardIterator(0);
  }
}
