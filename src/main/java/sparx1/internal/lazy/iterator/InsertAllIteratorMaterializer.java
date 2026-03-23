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

import sparx1.internal.lazy.IteratorMaterializer;
import sparx1.util.SizeOverflowException;
import sparx1.util.annotation.NotNull;
import sparx1.util.annotation.Positive;

public class InsertAllIteratorMaterializer<E> extends StatefulIteratorMaterializer<E> {

  public InsertAllIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      final @NotNull IteratorMaterializer<E> elementsMaterializer) {
    setState(new ImmaterialState(wrapped, elementsMaterializer));
  }

  private class ImmaterialState implements IteratorMaterializer<E> {

    private final IteratorMaterializer<E> elementsMaterializer;
    private final IteratorMaterializer<E> wrapped;

    private ImmaterialState(final @NotNull IteratorMaterializer<E> wrapped,
        final @NotNull IteratorMaterializer<E> elementsMaterializer) {
      this.wrapped = wrapped;
      this.elementsMaterializer = elementsMaterializer;
    }

    @Override
    public int currentKnownSize() {
      final int knownSize = wrapped.currentKnownSize();
      if (knownSize >= 0) {
        final int elementsSize = elementsMaterializer.currentKnownSize();
        if (elementsSize >= 0) {
          return SizeOverflowException.safeCast((long) knownSize + elementsSize);
        }
      }
      return -1;
    }

    @Override
    public boolean materializeHasNext() {
      return wrapped.materializeHasNext() || elementsMaterializer.materializeHasNext();
    }

    @Override
    public E materializeNext() {
      final IteratorMaterializer<E> elementsMaterializer = this.elementsMaterializer;
      if (elementsMaterializer.materializeHasNext()) {
        return elementsMaterializer.materializeNext();
      }
      return setState(wrapped).materializeNext();
    }

    @Override
    public int materializeSkip(final @Positive int count) {
      final int skipped = elementsMaterializer.materializeSkip(count);
      if (skipped < count) {
        return skipped + setState(wrapped).materializeSkip(count - skipped);
      }
      return skipped;
    }
  }
}
