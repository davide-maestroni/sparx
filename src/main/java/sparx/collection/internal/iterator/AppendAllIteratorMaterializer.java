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
package sparx.collection.internal.iterator;

import org.jetbrains.annotations.NotNull;
import sparx.util.Require;
import sparx.util.SizeOverflowException;

public class AppendAllIteratorMaterializer<E> implements IteratorMaterializer<E> {

  private IteratorMaterializer<E> state;

  public AppendAllIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      @NotNull final IteratorMaterializer<E> elementsMaterializer) {
    state = new ImmaterialState(Require.notNull(wrapped, "wrapped"),
        Require.notNull(elementsMaterializer, "elementsMaterializer"));
  }

  @Override
  public int knownSize() {
    return state.knownSize();
  }

  @Override
  public boolean materializeHasNext() {
    return state.materializeHasNext();
  }

  @Override
  public E materializeNext() {
    return state.materializeNext();
  }

  @Override
  public int materializeSkip(final int count) {
    return state.materializeSkip(count);
  }

  private class ImmaterialState implements IteratorMaterializer<E> {

    private final IteratorMaterializer<E> elementsMaterializer;
    private final IteratorMaterializer<E> wrapped;

    private int pos;

    private ImmaterialState(@NotNull final IteratorMaterializer<E> wrapped,
        @NotNull final IteratorMaterializer<E> elementsMaterializer) {
      this.wrapped = wrapped;
      this.elementsMaterializer = elementsMaterializer;
    }

    @Override
    public int knownSize() {
      final int knownSize = wrapped.knownSize();
      if (knownSize >= 0) {
        final int elementsSize = elementsMaterializer.knownSize();
        if (elementsSize >= 0) {
          return SizeOverflowException.safeCast((long) knownSize + elementsSize);
        }
      }
      return -1;
    }

    @Override
    public boolean materializeHasNext() {
      if (wrapped.materializeHasNext()) {
        return true;
      }
      return (state = elementsMaterializer).materializeHasNext();
    }

    @Override
    public E materializeNext() {
      final IteratorMaterializer<E> wrapped = this.wrapped;
      if (wrapped.materializeHasNext()) {
        return wrapped.materializeNext();
      }
      return (state = elementsMaterializer).materializeNext();
    }

    @Override
    public int materializeSkip(final int count) {
      if (count > 0) {
        final int skipped = wrapped.materializeSkip(count);
        if (skipped < count) {
          return skipped + (state = elementsMaterializer).materializeSkip(count - skipped);
        }
        return skipped;
      }
      return 0;
    }
  }
}
