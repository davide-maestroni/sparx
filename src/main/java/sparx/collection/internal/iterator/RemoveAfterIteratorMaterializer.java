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

public class RemoveAfterIteratorMaterializer<E> implements IteratorMaterializer<E> {

  private final int numElements;
  private final IteratorMaterializer<E> wrapped;

  private int pos;

  public RemoveAfterIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      final int numElements) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
    this.numElements = Require.notNegative(numElements, "numElements");
  }

  @Override
  public int knownSize() {
    final int knownSize = wrapped.knownSize();
    if (knownSize >= 0) {
      if (knownSize == 0) {
        return 0;
      }
      if (knownSize > numElements) {
        return knownSize - 1;
      }
      return knownSize;
    }
    return -1;
  }

  @Override
  public boolean materializeHasNext() {
    if (numElements == pos) {
      final IteratorMaterializer<E> wrapped = this.wrapped;
      if (wrapped.materializeHasNext()) {
        wrapped.materializeSkip(1);
        ++pos;
        return wrapped.materializeHasNext();
      }
      return false;
    }
    return wrapped.materializeHasNext();
  }

  @Override
  public E materializeNext() {
    if (numElements == pos) {
      wrapped.materializeSkip(1);
      ++pos;
    }
    ++pos;
    return wrapped.materializeNext();
  }

  @Override
  public int materializeSkip(final int count) {
    final int skipped = wrapped.materializeSkip(count);
    pos += skipped;
    return skipped;
  }
}
