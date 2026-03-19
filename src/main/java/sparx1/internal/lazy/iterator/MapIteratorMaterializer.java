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

import sparx1.internal.lazy.IteratorMaterializer;
import sparx1.util.UncheckedException;
import sparx1.util.annotation.NotNull;
import sparx1.util.annotation.Positive;
import sparx1.util.function.IndexedFunction;

public class MapIteratorMaterializer<E, F> extends StatefulIteratorMaterializer<F> {

  public MapIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      final @NotNull IndexedFunction<? super E, F> mapper) {
    setState(new ImmaterialState(wrapped, mapper));
  }

  private class ImmaterialState implements IteratorMaterializer<F> {

    private final IndexedFunction<? super E, F> mapper;
    private final IteratorMaterializer<E> wrapped;

    private int pos;

    private ImmaterialState(final @NotNull IteratorMaterializer<E> wrapped,
        final @NotNull IndexedFunction<? super E, F> mapper) {
      this.wrapped = wrapped;
      this.mapper = mapper;
    }

    @Override
    public int currentKnownSize() {
      return wrapped.currentKnownSize();
    }

    @Override
    public boolean materializeHasNext() {
      if (wrapped.materializeHasNext()) {
        return true;
      }
      setEmptyState();
      return false;
    }

    @Override
    public F materializeNext() {
      try {
        final IteratorMaterializer<E> wrapped = this.wrapped;
        return mapper.apply(pos++, wrapped.materializeNext());
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public int materializeSkip(@Positive final int count) {
      final int skipped = wrapped.materializeSkip(count);
      pos += skipped;
      return skipped;
    }
  }
}
