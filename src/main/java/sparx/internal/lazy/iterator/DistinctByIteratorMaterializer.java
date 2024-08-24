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

import java.util.HashSet;
import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;
import sparx.util.UncheckedException;
import sparx.util.function.IndexedFunction;

public class DistinctByIteratorMaterializer<E, K> extends AutoSkipIteratorMaterializer<E> {

  private final HashSet<Object> distinctKeys = new HashSet<Object>();
  private final IndexedFunction<? super E, K> keyExtractor;
  private final IteratorMaterializer<E> wrapped;

  private boolean hasNext;
  private int index;
  private E next;
  private int pos;

  public DistinctByIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      @NotNull final IndexedFunction<? super E, K> keyExtractor) {
    this.wrapped = wrapped;
    this.keyExtractor = keyExtractor;
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
      final HashSet<Object> distinctKeys = this.distinctKeys;
      final IndexedFunction<? super E, K> keyExtractor = this.keyExtractor;
      while (wrapped.materializeHasNext()) {
        final E element = wrapped.materializeNext();
        final int index = pos++;
        if (distinctKeys.add(keyExtractor.apply(index, element))) {
          hasNext = true;
          next = element;
          return true;
        }
      }
      distinctKeys.clear();
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
    final E next = this.next;
    hasNext = false;
    this.next = null;
    ++index;
    return next;
  }

  @Override
  public int nextIndex() {
    return index;
  }
}
