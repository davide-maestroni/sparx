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

import org.jetbrains.annotations.NotNull;
import sparx.util.annotation.NotNegative;

public class RemoveAfterIteratorMaterializer<E> extends StatefulIteratorMaterializer<E> {

  public RemoveAfterIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      @NotNegative final int numElements) {
    setState(new ImmaterialState(wrapped, numElements));
  }

  private class ImmaterialState implements IteratorMaterializer<E> {

    private final int numElements;
    private final IteratorMaterializer<E> wrapped;

    private int pos;

    private ImmaterialState(@NotNull final IteratorMaterializer<E> wrapped, final int numElements) {
      this.wrapped = wrapped;
      this.numElements = numElements;
    }

    @Override
    public int knownSize() {
      final int knownSize = wrapped.knownSize();
      if (knownSize >= 0) {
        if (knownSize >= numElements - pos) {
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
          setState(wrapped).materializeSkip(1);
          return wrapped.materializeHasNext();
        }
        return false;
      }
      return wrapped.materializeHasNext();
    }

    @Override
    public E materializeNext() {
      final IteratorMaterializer<E> wrapped = this.wrapped;
      if (wrapped.materializeHasNext() && numElements == pos++) {
        setState(wrapped).materializeSkip(1);
      }
      return wrapped.materializeNext();
    }

    @Override
    public int materializeSkip(final int count) {
      if (count > 0) {
        final int remaining = numElements - pos;
        if (count <= remaining) {
          final int skipped = wrapped.materializeSkip(count);
          this.pos += skipped;
          return skipped;
        }
        final IteratorMaterializer<E> wrapped = this.wrapped;
        int skipped = wrapped.materializeSkip(remaining);
        pos += skipped;
        if (skipped == remaining) {
          wrapped.materializeSkip(1);
          return skipped + setState(wrapped).materializeSkip(count - remaining);
        }
        return skipped;
      }
      return 0;
    }
  }
}
