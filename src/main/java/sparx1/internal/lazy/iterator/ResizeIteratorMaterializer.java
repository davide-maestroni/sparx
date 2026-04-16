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
import sparx1.internal.lazy.IteratorMaterializer;
import sparx1.util.annotation.NotNull;
import sparx1.util.annotation.Positive;

public class ResizeIteratorMaterializer<E> extends StatefulIteratorMaterializer<E> {

  public ResizeIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      final @Positive int numElements, final E padding) {
    setState(new InitialState(wrapped, numElements, padding));
  }

  private class InitialState implements IteratorMaterializer<E> {

    private final int numElements;
    private final E padding;
    private final IteratorMaterializer<E> wrapped;

    private int pos;

    private InitialState(final @NotNull IteratorMaterializer<E> wrapped, final int numElements,
        final E padding) {
      this.wrapped = wrapped;
      this.numElements = numElements;
      this.padding = padding;
    }

    @Override
    public int currentKnownSize() {
      return numElements;
    }

    @Override
    public boolean isSizeKnown() {
      return true;
    }

    @Override
    public boolean materializeHasNext() {
      return pos < numElements;
    }

    @Override
    public E materializeNext() {
      if (!materializeHasNext()) {
        throw new NoSuchElementException();
      }
      final IteratorMaterializer<E> wrapped = this.wrapped;
      if (wrapped.materializeHasNext()) {
        ++pos;
        return wrapped.materializeNext();
      }
      return setState(
          new RepeatIteratorMaterializer<E>(numElements - pos, padding)).materializeNext();
    }

    @Override
    public int materializeSkip(final @Positive int count) {
      final int remaining = numElements - pos;
      final int toSkip = Math.max(0, Math.min(count, remaining));
      int skipped = toSkip > 0 ? wrapped.materializeSkip(toSkip) : 0;
      if (skipped < toSkip) {
        skipped += setState(new RepeatIteratorMaterializer<E>(remaining, padding)).materializeSkip(
            toSkip - skipped);
      }
      pos += skipped;
      return skipped;
    }
  }
}
