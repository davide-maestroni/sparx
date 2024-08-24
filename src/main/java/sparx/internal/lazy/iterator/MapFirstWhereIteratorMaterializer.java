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

import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;
import sparx.util.UncheckedException;
import sparx.util.function.IndexedFunction;
import sparx.util.function.IndexedPredicate;

public class MapFirstWhereIteratorMaterializer<E> extends StatefulAutoSkipIteratorMaterializer<E> {

  public MapFirstWhereIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      @NotNull final IndexedPredicate<? super E> predicate,
      @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
    setState(new ImmaterialState(wrapped, predicate, mapper));
  }

  private class ImmaterialState implements IteratorMaterializer<E> {

    private final IndexedFunction<? super E, ? extends E> mapper;
    private final IndexedPredicate<? super E> predicate;
    private final IteratorMaterializer<E> wrapped;

    private boolean hasNext;
    private E next;
    private int pos;

    private ImmaterialState(@NotNull final IteratorMaterializer<E> wrapped,
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
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
      if (hasNext) {
        return true;
      }
      final IteratorMaterializer<E> wrapped = this.wrapped;
      if (wrapped.materializeHasNext()) {
        final int pos = this.pos;
        ++this.pos;
        final E next = wrapped.materializeNext();
        try {
          if (predicate.test(pos, next)) {
            hasNext = false;
            this.next = null;
            return setState(new InsertIteratorMaterializer<E>(wrapped,
                mapper.apply(pos, next))).materializeHasNext();
          }
        } catch (final Exception e) {
          throw UncheckedException.throwUnchecked(e);
        }
        hasNext = true;
        this.next = next;
        return true;
      }
      return false;
    }

    @Override
    public E materializeNext() {
      if (!materializeHasNext()) {
        throw new NoSuchElementException();
      }
      if (hasNext) {
        hasNext = false;
        final E next = this.next;
        this.next = null;
        ++pos;
        return next;
      }
      return getState().materializeNext();
    }

    @Override
    public int materializeSkip(final int count) {
      throw new UnsupportedOperationException();
    }

    @Override
    public int nextIndex() {
      return -1;
    }
  }
}
