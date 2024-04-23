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
package sparx.collection.internal.iterator;

import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;
import sparx.util.Require;
import sparx.util.UncheckedException;
import sparx.util.function.BinaryFunction;

public class FoldRightIteratorMaterializer<E, F> implements IteratorMaterializer<F> {

  private volatile IteratorMaterializer<F> state;

  public FoldRightIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      final F identity,
      @NotNull final BinaryFunction<? super E, ? super F, ? extends F> operation) {
    state = new ImmaterialState(Require.notNull(wrapped, "wrapped"), identity,
        Require.notNull(operation, "operation"));
  }

  @Override
  public int knownSize() {
    return state.knownSize();
  }

  @Override
  public boolean materializeHasNext() {
    return state.materializeHasNext();
  }

  @Override
  public F materializeNext() {
    return state.materializeNext();
  }

  @Override
  public int materializeSkip(final int count) {
    return state.materializeSkip(count);
  }

  private class ImmaterialState implements IteratorMaterializer<F> {

    private final F identity;
    private final BinaryFunction<? super E, ? super F, ? extends F> operation;
    private final IteratorMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final IteratorMaterializer<E> wrapped, final F identity,
        @NotNull final BinaryFunction<? super E, ? super F, ? extends F> operation) {
      this.wrapped = wrapped;
      this.identity = identity;
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
        final int knownSize = wrapped.knownSize();
        final ArrayList<E> elements = new ArrayList<E>(knownSize >= 0 ? knownSize : 16);
        while (wrapped.materializeHasNext()) {
          elements.add(wrapped.materializeNext());
        }
        final BinaryFunction<? super E, ? super F, ? extends F> operation = this.operation;
        F current = identity;
        for (int i = elements.size() - 1; i >= 0; --i) {
          current = operation.apply(elements.get(i), current);
        }
        state = EmptyIteratorMaterializer.instance();
        return current;
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public int materializeSkip(final int count) {
      if (count > 0) {
        state = EmptyIteratorMaterializer.instance();
        return 1;
      }
      return 0;
    }
  }
}
