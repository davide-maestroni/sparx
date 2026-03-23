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
package sparx1.internal.lazy.iterator;

import java.util.NoSuchElementException;
import sparx1.internal.lazy.IteratorMaterializer;
import sparx1.util.UncheckedException;
import sparx1.util.annotation.NotNull;
import sparx1.util.annotation.Positive;
import sparx1.util.function.BinaryFunction;
import sparx1.util.function.Predicate;

public class ReduceWhileIteratorMaterializer<E> extends StatefulIteratorMaterializer<E> {

  public ReduceWhileIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      final @NotNull Predicate<? super E> predicate,
      final @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation) {
    setState(new ImmaterialState(wrapped, predicate, operation));
  }

  private class ImmaterialState implements IteratorMaterializer<E> {

    private final BinaryFunction<? super E, ? super E, ? extends E> operation;
    private final Predicate<? super E> predicate;
    private final IteratorMaterializer<E> wrapped;

    private ImmaterialState(final @NotNull IteratorMaterializer<E> wrapped,
        final @NotNull Predicate<? super E> predicate,
        final @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation) {
      this.wrapped = wrapped;
      this.predicate = predicate;
      this.operation = operation;
    }

    @Override
    public int currentKnownSize() {
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
      try {
        final Predicate<? super E> predicate = this.predicate;
        final BinaryFunction<? super E, ? super E, ? extends E> operation = this.operation;
        E current = wrapped.materializeNext();
        if (predicate.test(current)) {
          while (wrapped.materializeHasNext()) {
            current = operation.apply(current, wrapped.materializeNext());
            if (!predicate.test(current)) {
              break;
            }
          }
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
      return wrapped.materializeHasNext() ? 1 : 0;
    }
  }
}
