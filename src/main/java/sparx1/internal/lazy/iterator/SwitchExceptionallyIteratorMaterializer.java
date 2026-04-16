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

import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;
import sparx1.internal.lazy.IteratorMaterializer;
import sparx1.util.UncheckedException;
import sparx1.util.function.IndexedFunction;

public class SwitchExceptionallyIteratorMaterializer<E> extends StatefulIteratorMaterializer<E> {

  public SwitchExceptionallyIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      final @NotNull IndexedFunction<? super Throwable, ? extends IteratorMaterializer<E>> mapper) {
    setState(new InitialState(wrapped, mapper));
  }

  private class InitialState extends AbstractStateIteratorMaterializer {

    private final IndexedFunction<? super Throwable, ? extends IteratorMaterializer<E>> mapper;
    private final IteratorMaterializer<E> wrapped;

    private int pos;

    private InitialState(final @NotNull IteratorMaterializer<E> wrapped,
        final @NotNull IndexedFunction<? super Throwable, ? extends IteratorMaterializer<E>> mapper) {
      this.wrapped = wrapped;
      this.mapper = mapper;
    }

    @Override
    public int currentKnownSize() {
      return -1;
    }

    @Override
    public boolean isSizeKnown() {
      return false;
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
        ++pos;
        final IteratorMaterializer<E> state = getState();
        return (state == this ? wrapped : state).materializeNext();
      } catch (final Throwable t) {
        try {
          return setState(mapper.apply(pos++, t)).materializeNext();
        } catch (final Exception e) {
          throw UncheckedException.throwUnchecked(e);
        }
      }
    }
  }
}
