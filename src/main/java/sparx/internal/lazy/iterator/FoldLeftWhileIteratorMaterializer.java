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
package sparx.internal.lazy.iterator;

import org.jetbrains.annotations.NotNull;
import sparx.util.UncheckedException;
import sparx.util.function.BinaryFunction;
import sparx.util.function.Predicate;

public class FoldLeftWhileIteratorMaterializer<E, F> extends StatefulIteratorMaterializer<F> {

  public FoldLeftWhileIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      final F identity, @NotNull final Predicate<? super F> predicate,
      @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
    setState(new ImmaterialState(wrapped, identity, predicate, operation));
  }

  private class ImmaterialState implements IteratorMaterializer<F> {

    private final F identity;
    private final BinaryFunction<? super F, ? super E, ? extends F> operation;
    private final Predicate<? super F> predicate;
    private final IteratorMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final IteratorMaterializer<E> wrapped, final F identity,
        @NotNull final Predicate<? super F> predicate,
        @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
      this.wrapped = wrapped;
      this.identity = identity;
      this.predicate = predicate;
      this.operation = operation;
    }

    @Override
    public int knownSize() {
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
        final Predicate<? super F> predicate = this.predicate;
        final BinaryFunction<? super F, ? super E, ? extends F> operation = this.operation;
        F current = identity;
        while (predicate.test(current) && wrapped.materializeHasNext()) {
          current = operation.apply(current, wrapped.materializeNext());
        }
        setEmptyState();
        return current;
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public int materializeSkip(final int count) {
      if (count > 0) {
        setEmptyState();
        return 1;
      }
      return 0;
    }

    @Override
    public int nextIndex() {
      return -1;
    }
  }
}
