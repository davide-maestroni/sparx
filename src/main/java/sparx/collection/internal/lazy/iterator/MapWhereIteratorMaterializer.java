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
package sparx.collection.internal.lazy.iterator;

import org.jetbrains.annotations.NotNull;
import sparx.util.Require;
import sparx.util.UncheckedException;
import sparx.util.function.IndexedFunction;
import sparx.util.function.IndexedPredicate;

public class MapWhereIteratorMaterializer<E> implements IteratorMaterializer<E> {

  private final IndexedFunction<? super E, ? extends E> mapper;
  private final IndexedPredicate<? super E> predicate;
  private final IteratorMaterializer<E> wrapped;

  private int pos;

  public MapWhereIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      @NotNull final IndexedPredicate<? super E> predicate,
      @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
    this.predicate = Require.notNull(predicate, "predicate");
    this.mapper = Require.notNull(mapper, "mapper");
  }

  @Override
  public int knownSize() {
    return wrapped.knownSize();
  }

  @Override
  public boolean materializeHasNext() {
    return wrapped.materializeHasNext();
  }

  @Override
  public E materializeNext() {
    try {
      final int pos = this.pos;
      ++this.pos;
      final E next = wrapped.materializeNext();
      if (predicate.test(pos, next)) {
        return mapper.apply(pos, next);
      }
      return next;
    } catch (final Exception e) {
      throw UncheckedException.throwUnchecked(e);
    }
  }

  @Override
  public int materializeSkip(final int count) {
    return wrapped.materializeSkip(count);
  }
}
