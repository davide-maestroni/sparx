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
package sparx.internal.lazy.list;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import sparx.util.Require;
import sparx.util.UncheckedException;
import sparx.util.function.BinaryFunction;

public class ReduceRightListMaterializer<E> implements ListMaterializer<E> {

  private volatile ListMaterializer<E> state;

  public ReduceRightListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      @NotNull final BinaryFunction<? super E, ? super E, ? extends E> operation) {
    state = new ImmaterialState(Require.notNull(wrapped, "wrapped"),
        Require.notNull(operation, "operation"));
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    return state.canMaterializeElement(index);
  }

  @Override
  public int knownSize() {
    return state.knownSize();
  }

  @Override
  public boolean materializeContains(final Object element) {
    return state.materializeContains(element);
  }

  @Override
  public E materializeElement(final int index) {
    return state.materializeElement(index);
  }

  @Override
  public int materializeElements() {
    return state.materializeElements();
  }

  @Override
  public boolean materializeEmpty() {
    return state.materializeEmpty();
  }

  @Override
  public @NotNull Iterator<E> materializeIterator() {
    return state.materializeIterator();
  }

  @Override
  public int materializeSize() {
    return state.materializeSize();
  }

  private class ImmaterialState implements ListMaterializer<E> {

    private final AtomicBoolean isMaterialized = new AtomicBoolean(false);
    private final BinaryFunction<? super E, ? super E, ? extends E> operation;
    private final ListMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final ListMaterializer<E> wrapped,
        @NotNull final BinaryFunction<? super E, ? super E, ? extends E> operation) {
      this.wrapped = wrapped;
      this.operation = operation;
    }

    @Override
    public boolean canMaterializeElement(final int index) {
      return index == 0 && !materializeEmpty();
    }

    @Override
    public int knownSize() {
      return Math.min(1, wrapped.knownSize());
    }

    @Override
    public boolean materializeContains(final Object element) {
      return materialized().materializeContains(element);
    }

    @Override
    public E materializeElement(final int index) {
      if (index != 0) {
        throw new IndexOutOfBoundsException(Integer.toString(index));
      }
      return materialized().materializeElement(index);
    }

    @Override
    public int materializeElements() {
      return materialized().materializeElements();
    }

    @Override
    public boolean materializeEmpty() {
      return wrapped.materializeEmpty();
    }

    @Override
    public @NotNull Iterator<E> materializeIterator() {
      return materialized().materializeIterator();
    }

    @Override
    public int materializeSize() {
      return materializeEmpty() ? 0 : 1;
    }

    private @NotNull ListMaterializer<E> materialized() {
      if (!isMaterialized.compareAndSet(false, true)) {
        throw new ConcurrentModificationException();
      }
      try {
        final BinaryFunction<? super E, ? super E, ? extends E> operation = this.operation;
        final ListMaterializer<E> wrapped = this.wrapped;
        if (wrapped.materializeEmpty()) {
          return state = EmptyListMaterializer.instance();
        }
        final int size = wrapped.materializeSize();
        E current = wrapped.materializeElement(size - 1);
        for (int i = size - 2; i >= 0; --i) {
          current = operation.apply(wrapped.materializeElement(i), current);
        }
        return state = new ElementToListMaterializer<E>(current);
      } catch (final Exception e) {
        isMaterialized.set(false);
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }
}
