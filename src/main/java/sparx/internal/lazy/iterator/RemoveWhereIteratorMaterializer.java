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
import sparx.util.annotation.Positive;
import sparx.util.function.IndexedPredicate;

public class RemoveWhereIteratorMaterializer<E> extends AutoSkipIteratorMaterializer<E> {

  private final IndexedPredicate<? super E> predicate;
  private final IteratorMaterializer<E> wrapped;

  private boolean hasNext;
  private E next;
  private int pos;

  public RemoveWhereIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      @NotNull final IndexedPredicate<? super E> predicate) {
    this.wrapped = wrapped;
    this.predicate = predicate;
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
    try {
      final IteratorMaterializer<E> wrapped = this.wrapped;
      final IndexedPredicate<? super E> predicate = this.predicate;
      while (wrapped.materializeHasNext()) {
        final int pos = this.pos++;
        final E element = wrapped.materializeNext();
        if (!predicate.test(pos, element)) {
          next = element;
          hasNext = true;
          return true;
        }
      }
    } catch (final Exception e) {
      throw UncheckedException.throwUnchecked(e);
    }
    return false;
  }

  @Override
  public E materializeNext() {
    if (!materializeHasNext()) {
      throw new NoSuchElementException();
    }
    hasNext = false;
    final E next = this.next;
    this.next = null;
    return next;
  }

  @Override
  public int materializeSkip(@Positive final int count) {
    final int skipped = super.materializeSkip(count);
    pos += skipped;
    return skipped;
  }
}
