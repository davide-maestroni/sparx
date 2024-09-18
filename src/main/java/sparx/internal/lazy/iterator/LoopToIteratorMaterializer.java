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
import sparx.util.function.IndexedPredicate;

public class LoopToIteratorMaterializer<E> extends AutoSkipIteratorMaterializer<E> {

  private final IndexedPredicate<? super E> predicate;
  private final IndexedFunction<? super E, ? extends E> update;

  private E current;
  private int pos;

  public LoopToIteratorMaterializer(final E initialValue,
      @NotNull final IndexedPredicate<? super E> predicate,
      @NotNull final IndexedFunction<? super E, ? extends E> update) {
    this.predicate = predicate;
    this.update = update;
    current = initialValue;
  }

  @Override
  public int knownSize() {
    return -1;
  }

  @Override
  public boolean materializeHasNext() {
    try {
      return predicate.test(pos, current);
    } catch (final Exception e) {
      throw UncheckedException.toUnchecked(e);
    }
  }

  @Override
  public E materializeNext() {
    try {
      final E current = this.current;
      if (!predicate.test(pos, current)) {
        throw new NoSuchElementException();
      }
      return this.current = update.apply(pos++, current);
    } catch (final Exception e) {
      throw UncheckedException.toUnchecked(e);
    }
  }
}
