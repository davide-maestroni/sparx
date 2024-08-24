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

import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;
import sparx.util.annotation.Positive;

public class ResizeIteratorMaterializer<E> extends StatefulIteratorMaterializer<E> {

  public ResizeIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      @Positive final int numElements, final E padding) {
    setState(new ImmaterialState(wrapped, numElements, padding));
  }

  private class ImmaterialState implements IteratorMaterializer<E> {

    private final int numElements;
    private final E padding;
    private final IteratorMaterializer<E> wrapped;

    private int pos;

    private ImmaterialState(@NotNull final IteratorMaterializer<E> wrapped, final int numElements,
        final E padding) {
      this.wrapped = wrapped;
      this.numElements = numElements;
      this.padding = padding;
    }

    @Override
    public int knownSize() {
      return numElements;
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
    public int materializeSkip(final int count) {
      if (count > 0) {
        final int remaining = numElements - pos;
        final int toSkip = Math.min(count, remaining);
        int skipped = wrapped.materializeSkip(toSkip);
        if (skipped < toSkip) {
          skipped += setState(
              new RepeatIteratorMaterializer<E>(remaining, padding)).materializeSkip(
              toSkip - skipped);
        }
        return skipped;
      }
      return 0;
    }

    @Override
    public int nextIndex() {
      return -1;
    }
  }
}
