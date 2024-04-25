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
import sparx.util.Require;
import sparx.util.UncheckedException;
import sparx.util.function.IndexedFunction;

public class FlatMapExceptionallyIteratorMaterializer<E> extends AbstractIteratorMaterializer<E> {

  private final IndexedFunction<? super Throwable, ? extends IteratorMaterializer<E>> mapper;
  private final IteratorMaterializer<E> wrapped;

  private IteratorMaterializer<E> materializer;
  private int pos;

  public FlatMapExceptionallyIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      @NotNull final IndexedFunction<? super Throwable, ? extends IteratorMaterializer<E>> mapper) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
    this.mapper = Require.notNull(mapper, "mapper");
  }

  @Override
  public int knownSize() {
    return -1;
  }

  @Override
  public boolean materializeHasNext() {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer != null && materializer.materializeHasNext()) {
      return true;
    }
    try {
      return wrapped.materializeHasNext();
    } catch (final Throwable t) {
      try {
        final IteratorMaterializer<E> elementsMaterializer = mapper.apply(pos, t);
        if (elementsMaterializer.materializeHasNext()) {
          this.materializer = elementsMaterializer;
          return true;
        }
        this.materializer = null;
        return materializeHasNext();
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }

  @Override
  public E materializeNext() {
    final IteratorMaterializer<E> materializer = this.materializer;
    if (materializer != null && materializer.materializeHasNext()) {
      return materializer.materializeNext();
    }
    try {
      ++pos;
      return wrapped.materializeNext();
    } catch (final Throwable t) {
      try {
        final IteratorMaterializer<E> elementsMaterializer = mapper.apply(pos, t);
        if (elementsMaterializer.materializeHasNext()) {
          return (this.materializer = elementsMaterializer).materializeNext();
        }
        this.materializer = null;
        ++pos;
        return materializeNext();
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }
}
