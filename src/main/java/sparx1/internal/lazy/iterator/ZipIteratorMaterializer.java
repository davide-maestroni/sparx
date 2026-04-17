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
import sparx1.util.ZipEntry;
import sparx1.util.annotation.NotNull;
import sparx1.util.annotation.Positive;

public class ZipIteratorMaterializer<E, F> extends StatefulIteratorMaterializer<ZipEntry<E, F>> {

  public ZipIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      final @NotNull IteratorMaterializer<F> elementsMaterializer) {
    setState(new InitialState(wrapped, elementsMaterializer));
  }

  private class InitialState extends AbstractStateIteratorMaterializer {

    private final IteratorMaterializer<F> elementsMaterializer;
    private final IteratorMaterializer<E> wrapped;

    private InitialState(final @NotNull IteratorMaterializer<E> wrapped,
        final @NotNull IteratorMaterializer<F> elementsMaterializer) {
      this.wrapped = wrapped;
      this.elementsMaterializer = elementsMaterializer;
    }

    @Override
    public int currentKnownSize() {
      final int knownSize = wrapped.currentKnownSize();
      if (knownSize >= 0) {
        final int elementsKnownSize = elementsMaterializer.currentKnownSize();
        if (elementsKnownSize >= 0) {
          return Math.min(knownSize, elementsKnownSize);
        }
      }
      return -1;
    }

    @Override
    public boolean isSizeKnown() {
      return wrapped.isSizeKnown() && elementsMaterializer.isSizeKnown();
    }

    @Override
    public boolean materializeHasNext() {
      final boolean hasNext =
          wrapped.materializeHasNext() && elementsMaterializer.materializeHasNext();
      if (!hasNext) {
        setEmptyState();
      }
      return hasNext;
    }

    @Override
    public ZipEntry<E, F> materializeNext() {
      return ZipEntry.of(wrapped.materializeNext(), elementsMaterializer.materializeNext());
    }

    @Override
    public int materializeSkip(final @Positive int count) {
      final IteratorMaterializer<E> wrapped = this.wrapped;
      final IteratorMaterializer<F> elementsMaterializer = this.elementsMaterializer;
      final int knownSize = wrapped.currentKnownSize();
      final int elementsKnownSize = elementsMaterializer.currentKnownSize();
      if (knownSize >= 0 && elementsKnownSize >= 0) {
        final int toSkip = Math.min(count, Math.min(knownSize, elementsKnownSize));
        wrapped.materializeSkip(toSkip);
        elementsMaterializer.materializeSkip(toSkip);
        return toSkip;
      }
      return super.materializeSkip(count);
    }
  }
}
