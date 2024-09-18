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

public class DropIteratorMaterializer<E> extends StatefulIteratorMaterializer<E> {

  public DropIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      @Positive final int maxElements) {
    setState(new ImmaterialState(wrapped, maxElements));
  }

  private class ImmaterialState implements IteratorMaterializer<E> {

    private final int maxElements;
    private final IteratorMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final IteratorMaterializer<E> wrapped, final int maxElements) {
      this.wrapped = wrapped;
      this.maxElements = maxElements;
    }

    @Override
    public int knownSize() {
      final int knownSize = wrapped.knownSize();
      if (knownSize >= 0) {
        return Math.max(0, knownSize - maxElements);
      }
      return -1;
    }

    @Override
    public boolean materializeHasNext() {
      final int maxElements = this.maxElements;
      final IteratorMaterializer<E> wrapped = this.wrapped;
      final int skipped = wrapped.materializeSkip(maxElements);
      if (skipped < maxElements) {
        setEmptyState();
        return false;
      }
      return setState(wrapped).materializeHasNext();
    }

    @Override
    public E materializeNext() {
      if (!materializeHasNext()) {
        throw new NoSuchElementException();
      }
      return getState().materializeNext();
    }

    @Override
    public int materializeSkip(final int count) {
      if (count > 0 && materializeHasNext()) {
        return getState().materializeSkip(count);
      }
      return 0;
    }
  }
}
