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
package sparx;

import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import sparx.collection.ListMaterializer;
import sparx.util.Require;
import sparx.util.UncheckedException;
import sparx.util.function.BinaryFunction;

class FoldLeftListMaterializer<E, F> implements ListMaterializer<F> {

  private volatile State<F> state;

  FoldLeftListMaterializer(@NotNull final ListMaterializer<E> wrapped, final F identity,
      @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
    state = new ImmaterialState(Require.notNull(wrapped, "wrapped"), identity,
        Require.notNull(operation, "operation"));
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    return index == 0 && !state.materialized().isEmpty();
  }

  @Override
  public int knownSize() {
    return state.knownSize();
  }

  @Override
  public F materializeElement(final int index) {
    return state.materialized().get(index);
  }

  @Override
  public boolean materializeEmpty() {
    return state.materialized().isEmpty();
  }

  @Override
  public @NotNull Iterator<F> materializeIterator() {
    return state.materialized().iterator();
  }

  @Override
  public int materializeSize() {
    return state.materialized().size();
  }

  private interface State<E> {

    int knownSize();

    @NotNull List<E> materialized();
  }

  private static class ElementState<E> implements State<E> {

    private final List<E> elements;

    private ElementState(@NotNull final E element) {
      this.elements = Collections.singletonList(element);
    }

    @Override
    public int knownSize() {
      return 1;
    }

    @Override
    public @NotNull List<E> materialized() {
      return elements;
    }
  }

  private class ImmaterialState implements State<F> {

    private final F identity;
    private final AtomicBoolean isMaterialized = new AtomicBoolean(false);
    private final BinaryFunction<? super F, ? super E, ? extends F> operation;
    private final ListMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final ListMaterializer<E> wrapped, final F identity,
        @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
      this.wrapped = wrapped;
      this.identity = identity;
      this.operation = operation;
    }

    @Override
    public int knownSize() {
      return 1;
    }

    @Override
    public @NotNull List<F> materialized() {
      if (!isMaterialized.compareAndSet(false, true)) {
        throw new ConcurrentModificationException();
      }
      try {
        final BinaryFunction<? super F, ? super E, ? extends F> operation = this.operation;
        final Iterator<E> iterator = wrapped.materializeIterator();
        F current = identity;
        while (iterator.hasNext()) {
          current = operation.apply(current, iterator.next());
        }
        state = new ElementState<F>(current);
        return state.materialized();
      } catch (final Exception e) {
        isMaterialized.set(false);
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }
}
