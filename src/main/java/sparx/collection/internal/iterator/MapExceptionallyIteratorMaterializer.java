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

public class MapExceptionallyIteratorMaterializer<E> extends AbstractIteratorMaterializer<E> {

  private final IndexedFunction<? super Throwable, E> mapper;
  private final IteratorMaterializer<E> wrapped;

  private boolean hasNext;
  private E next;
  private int pos;

  public MapExceptionallyIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      @NotNull final IndexedFunction<? super Throwable, E> mapper) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
    this.mapper = Require.notNull(mapper, "mapper");
  }

  @Override
  public int knownSize() {
    return wrapped.knownSize();
  }

  @Override
  public boolean materializeHasNext() {
    if (hasNext) {
      return true;
    }
    try {
      return wrapped.materializeHasNext();
    } catch (final Throwable t) {
      try {
        next = mapper.apply(pos++, t);
        return (hasNext = true);
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }

  @Override
  public E materializeNext() {
    if (hasNext) {
      hasNext = false;
      final E next = this.next;
      this.next = null;
      return next;
    }
    try {
      final E next = wrapped.materializeNext();
      ++pos;
      return next;
    } catch (final Throwable t) {
      try {
        final E next = mapper.apply(pos++, t);
        hasNext = true;
        return (this.next = next);
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }
}
