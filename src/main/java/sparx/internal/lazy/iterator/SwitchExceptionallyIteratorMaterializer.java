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

public class SwitchExceptionallyIteratorMaterializer<E> extends
    StatefulAutoSkipIteratorMaterializer<E> {

  public SwitchExceptionallyIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      @NotNull final IndexedFunction<? super Throwable, ? extends IteratorMaterializer<E>> mapper) {
    setState(new ImmaterialState(wrapped, mapper));
  }

  private class ImmaterialState implements IteratorMaterializer<E> {

    private final IndexedFunction<? super Throwable, ? extends IteratorMaterializer<E>> mapper;
    private final IteratorMaterializer<E> wrapped;

    private int pos;

    private ImmaterialState(@NotNull final IteratorMaterializer<E> wrapped,
        @NotNull final IndexedFunction<? super Throwable, ? extends IteratorMaterializer<E>> mapper) {
      this.wrapped = wrapped;
      this.mapper = mapper;
    }

    @Override
    public int knownSize() {
      return -1;
    }

    @Override
    public boolean materializeHasNext() {
      try {
        return wrapped.materializeHasNext();
      } catch (final Throwable t) {
        try {
          return setState(mapper.apply(pos, t)).materializeHasNext();
        } catch (final Exception e) {
          throw UncheckedException.throwUnchecked(e);
        }
      }
    }

    @Override
    public E materializeNext() {
      if (!materializeHasNext()) {
        throw new NoSuchElementException();
      }
      try {
        final IteratorMaterializer<E> state = getState();
        final E next = (state == this ? wrapped : state).materializeNext();
        ++pos;
        return next;
      } catch (final Throwable t) {
        try {
          return setState(mapper.apply(pos++, t)).materializeNext();
        } catch (final Exception e) {
          throw UncheckedException.throwUnchecked(e);
        }
      }
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
