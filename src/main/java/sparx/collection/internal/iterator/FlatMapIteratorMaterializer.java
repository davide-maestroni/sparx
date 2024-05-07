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

import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;
import sparx.util.Require;
import sparx.util.UncheckedException;
import sparx.util.function.IndexedFunction;

public class FlatMapIteratorMaterializer<E, F> implements IteratorMaterializer<F> {

  private final IndexedFunction<? super E, ? extends IteratorMaterializer<F>> mapper;
  private final IteratorMaterializer<E> wrapped;

  private IteratorMaterializer<F> materializer = EmptyIteratorMaterializer.instance();
  private int pos;

  public FlatMapIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      @NotNull final IndexedFunction<? super E, ? extends IteratorMaterializer<F>> mapper) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
    this.mapper = Require.notNull(mapper, "mapper");
  }

  @Override
  public int knownSize() {
    return -1;
  }

  @Override
  public boolean materializeHasNext() {
    if (materializer.materializeHasNext()) {
      return true;
    }
    try {
      final IteratorMaterializer<E> wrapped = this.wrapped;
      final IndexedFunction<? super E, ? extends IteratorMaterializer<F>> mapper = this.mapper;
      while (wrapped.materializeHasNext()) {
        final IteratorMaterializer<F> materializer = mapper.apply(pos++, wrapped.materializeNext());
        if (materializer.materializeHasNext()) {
          this.materializer = materializer;
          return true;
        }
      }
    } catch (final Exception e) {
      throw UncheckedException.throwUnchecked(e);
    }
    return false;
  }

  @Override
  public F materializeNext() {
    if (!materializeHasNext()) {
      throw new NoSuchElementException();
    }
    return materializer.materializeNext();
  }

  @Override
  public int materializeSkip(final int count) {
    if (count > 0) {
      int skipped = 0;
      while (skipped < count && materializeHasNext()) {
        skipped += materializer.materializeSkip(count - skipped);
      }
      return skipped;
    }
    return 0;
  }
}
