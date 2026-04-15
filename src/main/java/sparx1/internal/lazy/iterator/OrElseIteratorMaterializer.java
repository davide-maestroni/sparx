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
import sparx1.util.annotation.NotNull;
import sparx1.util.annotation.Positive;

public class OrElseIteratorMaterializer<E> extends StatefulIteratorMaterializer<E> {

  public OrElseIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      final @NotNull IteratorMaterializer<E> elementsMaterializer) {
    setState(new InitialState(wrapped, elementsMaterializer));
  }

  private class InitialState implements IteratorMaterializer<E> {

    private final IteratorMaterializer<E> elementsMaterializer;
    private final IteratorMaterializer<E> wrapped;

    private InitialState(final @NotNull IteratorMaterializer<E> wrapped,
        final @NotNull IteratorMaterializer<E> elementsMaterializer) {
      this.wrapped = wrapped;
      this.elementsMaterializer = elementsMaterializer;
    }

    @Override
    public int currentKnownSize() {
      final int knownSize = wrapped.currentKnownSize();
      if (knownSize == 0) {
        return elementsMaterializer.currentKnownSize();
      }
      return knownSize;
    }

    @Override
    public boolean isSizeKnown() {
      final int knownSize = wrapped.currentKnownSize();
      if (knownSize == 0) {
        return elementsMaterializer.isSizeKnown();
      }
      return knownSize > 0;
    }

    @Override
    public boolean materializeHasNext() {
      return wrapped.materializeHasNext() || elementsMaterializer.materializeHasNext();
    }

    @Override
    public E materializeNext() {
      final IteratorMaterializer<E> wrapped = this.wrapped;
      if (wrapped.materializeHasNext()) {
        return setState(wrapped).materializeNext();
      }
      return setState(elementsMaterializer).materializeNext();
    }

    @Override
    public int materializeSkip(final @Positive int count) {
      final IteratorMaterializer<E> wrapped = this.wrapped;
      final int skipped = wrapped.materializeSkip(count);
      if (skipped > 0) {
        setState(wrapped);
        return skipped;
      }
      return setState(elementsMaterializer).materializeSkip(count);
    }
  }
}
