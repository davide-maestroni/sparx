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
import sparx1.util.UncheckedException;
import sparx1.util.annotation.NotNull;
import sparx1.util.function.IndexedFunction;
import sparx1.util.function.IndexedPredicate;

public class LoopToIteratorMaterializer<E> extends AbstractIteratorMaterializer<E> {

  private final IndexedPredicate<? super E> predicate;
  private final IndexedFunction<? super E, ? extends E> update;

  private E current;
  private int pos;

  public LoopToIteratorMaterializer(final E initialValue,
      final @NotNull IndexedPredicate<? super E> predicate,
      final @NotNull IndexedFunction<? super E, ? extends E> update) {
    this.predicate = predicate;
    this.update = update;
    current = initialValue;
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
