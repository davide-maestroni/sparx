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

import org.jetbrains.annotations.NotNull;
import sparx.util.DequeueList;
import sparx.util.Require;
import sparx.util.UncheckedException;
import sparx.util.function.IndexedFunction;
import sparx.util.function.IndexedPredicate;

public class FlatMapLastWhereIteratorMaterializer<E> extends AbstractIteratorMaterializer<E> {

  private volatile IteratorMaterializer<E> state;

  public FlatMapLastWhereIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      @NotNull final IndexedPredicate<? super E> predicate,
      @NotNull final IndexedFunction<? super E, ? extends IteratorMaterializer<E>> mapper) {
    state = new ImmaterialState(Require.notNull(wrapped, "wrapped"),
        Require.notNull(predicate, "predicate"), Require.notNull(mapper, "mapper"));
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

  private class ImmaterialState implements IteratorMaterializer<E> {

    private final IndexedFunction<? super E, ? extends IteratorMaterializer<E>> mapper;
    private final IndexedPredicate<? super E> predicate;
    private final IteratorMaterializer<E> wrapped;

    private int pos;

    private ImmaterialState(@NotNull final IteratorMaterializer<E> wrapped,
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends IteratorMaterializer<E>> mapper) {
      this.wrapped = wrapped;
      this.predicate = predicate;
      this.mapper = mapper;
    }

    @Override
    public int knownSize() {
      return -1;
    }

    @Override
    public boolean materializeHasNext() {
      return materializer().materializeHasNext();
    }

    @Override
    public E materializeNext() {
      final E next = materializer().materializeNext();
      ++pos;
      return next;
    }

    private @NotNull IteratorMaterializer<E> materializer() {
      final IteratorMaterializer<E> wrapped = this.wrapped;
      if (wrapped.materializeHasNext()) {
        final DequeueList<E> elements = new DequeueList<E>();
        while (wrapped.materializeHasNext()) {
          elements.add(wrapped.materializeNext());
        }
        try {
          final int pos = this.pos;
          final IndexedPredicate<? super E> predicate = this.predicate;
          for (int i = elements.size() - 1; i >= 0; --i) {
            if (predicate.test(pos + i, elements.get(i))) {
              return (state = new FlatMapAfterIteratorMaterializer<E>(
                  new DequeueToIteratorMaterializer<E>(elements), i, mapper));
            }
          }
          return (state = new DequeueToIteratorMaterializer<E>(elements));
        } catch (final Exception e) {
          throw UncheckedException.throwUnchecked(e);
        }
      }
      return (state = wrapped);
    }

    @Override
    public int materializeSkip(final int count) {
      throw new UnsupportedOperationException();
    }
  }
}
