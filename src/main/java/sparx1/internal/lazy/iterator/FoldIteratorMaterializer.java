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
package sparx1.internal.lazy.iterator;

import sparx1.internal.lazy.IteratorMaterializer;
import sparx1.util.UncheckedException;
import sparx1.util.annotation.NotNull;
import sparx1.util.annotation.Positive;
import sparx1.util.function.BinaryFunction;

public class FoldIteratorMaterializer<E, F> extends StatefulIteratorMaterializer<F> {

  public FoldIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      final F identity,
      final @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation) {
    setState(new ImmaterialState(wrapped, identity, operation));
  }

  private class ImmaterialState implements IteratorMaterializer<F> {

    private final F identity;
    private final BinaryFunction<? super F, ? super E, ? extends F> operation;
    private final IteratorMaterializer<E> wrapped;

    private ImmaterialState(final @NotNull IteratorMaterializer<E> wrapped, final F identity,
        final @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation) {
      this.wrapped = wrapped;
      this.identity = identity;
      this.operation = operation;
    }

    @Override
    public int currentKnownSize() {
      return 1;
    }

    @Override
    public boolean materializeHasNext() {
      return true;
    }

    @Override
    public F materializeNext() {
      try {
        final IteratorMaterializer<E> wrapped = this.wrapped;
        final BinaryFunction<? super F, ? super E, ? extends F> operation = this.operation;
        F current = identity;
        while (wrapped.materializeHasNext()) {
          current = operation.apply(current, wrapped.materializeNext());
        }
        setEmptyState();
        return current;
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public int materializeSkip(@Positive final int count) {
      setEmptyState();
      return 1;
    }
  }
}
