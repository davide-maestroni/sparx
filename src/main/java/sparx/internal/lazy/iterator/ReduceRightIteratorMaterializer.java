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

import java.util.ArrayList;
import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;
import sparx.util.UncheckedException;
import sparx.util.function.BinaryFunction;

public class ReduceRightIteratorMaterializer<E> implements IteratorMaterializer<E> {

  private volatile IteratorMaterializer<E> state;

  public ReduceRightIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      @NotNull final BinaryFunction<? super E, ? super E, ? extends E> operation) {
    state = new ImmaterialState(wrapped, operation);
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
  public E materializeNext() {
    return state.materializeNext();
  }

  @Override
  public int materializeSkip(final int count) {
    return state.materializeSkip(count);
  }

  private class ImmaterialState implements IteratorMaterializer<E> {

    private final BinaryFunction<? super E, ? super E, ? extends E> operation;
    private final IteratorMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final IteratorMaterializer<E> wrapped,
        @NotNull final BinaryFunction<? super E, ? super E, ? extends E> operation) {
      this.wrapped = wrapped;
      this.operation = operation;
    }

    @Override
    public int knownSize() {
      return 1;
    }

    @Override
    public boolean materializeHasNext() {
      return wrapped.materializeHasNext();
    }

    @Override
    public E materializeNext() {
      final IteratorMaterializer<E> wrapped = this.wrapped;
      if (!wrapped.materializeHasNext()) {
        throw new NoSuchElementException();
      }
      final int knownSize = wrapped.knownSize();
      final ArrayList<E> elements = new ArrayList<E>(knownSize >= 0 ? knownSize : 16);
      do {
        elements.add(wrapped.materializeNext());
      } while (wrapped.materializeHasNext());
      try {
        final BinaryFunction<? super E, ? super E, ? extends E> operation = this.operation;
        E current = elements.get(elements.size() - 1);
        for (int i = elements.size() - 2; i >= 0; --i) {
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
