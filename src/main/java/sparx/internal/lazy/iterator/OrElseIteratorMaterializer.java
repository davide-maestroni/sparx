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

public class OrElseIteratorMaterializer<E> extends StatefulIteratorMaterializer<E> {

  public OrElseIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      @NotNull final IteratorMaterializer<E> elementsMaterializer) {
    setState(new ImmaterialState(wrapped, elementsMaterializer));
  }

  private class ImmaterialState implements IteratorMaterializer<E> {

    private final IteratorMaterializer<E> elementsMaterializer;
    private final IteratorMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final IteratorMaterializer<E> wrapped,
        @NotNull final IteratorMaterializer<E> elementsMaterializer) {
      this.wrapped = wrapped;
      this.elementsMaterializer = elementsMaterializer;
    }

    @Override
    public int knownSize() {
      final int knownSize = wrapped.knownSize();
      if (knownSize == 0) {
        return elementsMaterializer.knownSize();
      }
      return knownSize;
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
    public int materializeSkip(final int count) {
      if (count > 0) {
        final IteratorMaterializer<E> wrapped = this.wrapped;
        final int skipped = wrapped.materializeSkip(count);
        if (skipped > 0) {
          setState(wrapped);
          return skipped;
        }
        return setState(elementsMaterializer).materializeSkip(count);
      }
      return 0;
    }
  }
}
