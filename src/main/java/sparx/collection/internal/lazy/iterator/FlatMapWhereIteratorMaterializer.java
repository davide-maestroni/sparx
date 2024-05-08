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

import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;
import sparx.util.Require;
import sparx.util.UncheckedException;
import sparx.util.function.IndexedFunction;
import sparx.util.function.IndexedPredicate;

public class FlatMapWhereIteratorMaterializer<E> implements IteratorMaterializer<E> {

  private final IndexedFunction<? super E, ? extends IteratorMaterializer<E>> mapper;
  private final IndexedPredicate<? super E> predicate;
  private final IteratorMaterializer<E> wrapped;

  private boolean hasNext;
  private IteratorMaterializer<E> materializer = EmptyIteratorMaterializer.instance();
  private E next;
  private int pos;

  public FlatMapWhereIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      @NotNull final IndexedPredicate<? super E> predicate,
      @NotNull final IndexedFunction<? super E, ? extends IteratorMaterializer<E>> mapper) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
    this.predicate = Require.notNull(predicate, "predicate");
    this.mapper = Require.notNull(mapper, "mapper");
  }

  @Override
  public int knownSize() {
    return -1;
  }

  @Override
  public boolean materializeHasNext() {
    if (hasNext || materializer.materializeHasNext()) {
      return true;
    }
    final IteratorMaterializer<E> wrapped = this.wrapped;
    final IndexedPredicate<? super E> predicate = this.predicate;
    final IndexedFunction<? super E, ? extends IteratorMaterializer<E>> mapper = this.mapper;
    while (wrapped.materializeHasNext()) {
      final int pos = this.pos;
      ++this.pos;
      final E next = wrapped.materializeNext();
      try {
        if (predicate.test(pos, next)) {
          final IteratorMaterializer<E> materializer = mapper.apply(pos, next);
          if (materializer.materializeHasNext()) {
            this.materializer = materializer;
            return true;
          }
        } else {
          hasNext = true;
          this.next = next;
          return true;
        }
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
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
      return next;
    }
    return materializer.materializeNext();
  }

  @Override
  public int materializeSkip(final int count) {
    if (count > 0) {
      int skipped = 0;
      while (skipped < count && materializeHasNext()) {
        if (hasNext) {
          hasNext = false;
          this.next = null;
          ++skipped;
        } else {
          skipped += materializer.materializeSkip(count - skipped);
        }
      }
      return skipped;
    }
    return 0;
  }
}
