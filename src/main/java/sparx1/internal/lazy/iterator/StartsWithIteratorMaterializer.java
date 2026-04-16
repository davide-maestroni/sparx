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

public class StartsWithIteratorMaterializer<E> extends StatefulIteratorMaterializer<Boolean> {

  public StartsWithIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      final @NotNull IteratorMaterializer<?> elementsMaterializer) {
    setState(new InitialState(wrapped, elementsMaterializer));
  }

  private class InitialState implements IteratorMaterializer<Boolean> {

    private final IteratorMaterializer<?> elementsMaterializer;
    private final IteratorMaterializer<E> wrapped;

    private InitialState(final @NotNull IteratorMaterializer<E> wrapped,
        final @NotNull IteratorMaterializer<?> elementsMaterializer) {
      this.wrapped = wrapped;
      this.elementsMaterializer = elementsMaterializer;
    }

    @Override
    public int currentKnownSize() {
      return 1;
    }

    @Override
    public boolean isSizeKnown() {
      return true;
    }

    @Override
    public boolean materializeHasNext() {
      return true;
    }

    @Override
    public Boolean materializeNext() {
      final IteratorMaterializer<?> elementsMaterializer = this.elementsMaterializer;
      if (!elementsMaterializer.materializeHasNext()) {
        setEmptyState();
        return true;
      }
      final IteratorMaterializer<E> wrapped = this.wrapped;
      while (wrapped.materializeHasNext() && elementsMaterializer.materializeHasNext()) {
        final E left = wrapped.materializeNext();
        final Object right = elementsMaterializer.materializeNext();
        if (left != right && (left == null || !left.equals(right))) {
          setEmptyState();
          return false;
        }
      }
      setEmptyState();
      return !elementsMaterializer.materializeHasNext();
    }

    @Override
    public int materializeSkip(final @Positive int count) {
      setEmptyState();
      return 1;
    }
  }
}
