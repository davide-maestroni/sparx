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
import sparx1.internal.lazy.ListMaterializer;
import sparx1.util.DequeArrayList;
import sparx1.util.annotation.NotNull;
import sparx1.util.annotation.Positive;

public class EndsWithIteratorMaterializer<E> extends StatefulIteratorMaterializer<Boolean> {

  public EndsWithIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      final @NotNull ListMaterializer<?> elementsMaterializer) {
    setState(new InitialState(wrapped, elementsMaterializer));
  }

  private class InitialState implements IteratorMaterializer<Boolean> {

    private final ListMaterializer<?> elementsMaterializer;
    private final IteratorMaterializer<E> wrapped;

    private InitialState(final @NotNull IteratorMaterializer<E> wrapped,
        final @NotNull ListMaterializer<?> elementsMaterializer) {
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
      final IteratorMaterializer<E> wrapped = this.wrapped;
      final ListMaterializer<?> elementsMaterializer = this.elementsMaterializer;
      final int elementsSize = elementsMaterializer.materializeSize();
      if (elementsSize > 0) {
        final DequeArrayList<E> wrappedElements = new DequeArrayList<E>();
        while (wrapped.materializeHasNext()) {
          wrappedElements.add(wrapped.materializeNext());
          if (wrappedElements.size() > elementsSize) {
            wrappedElements.removeFirst();
          }
        }
        final int wrappedSize = wrappedElements.size();
        if (wrappedSize < elementsSize) {
          setEmptyState();
          return false;
        }
        for (int i = wrappedSize - 1, j = elementsSize - 1; i >= 0 && j >= 0; --i, --j) {
          final E left = wrappedElements.get(i);
          final Object right = elementsMaterializer.materializeElement(j);
          if (left != right && (left == null || !left.equals(right))) {
            setEmptyState();
            return false;
          }
        }
      }
      setEmptyState();
      return true;
    }

    @Override
    public int materializeSkip(final @Positive int count) {
      setEmptyState();
      return 1;
    }
  }
}
