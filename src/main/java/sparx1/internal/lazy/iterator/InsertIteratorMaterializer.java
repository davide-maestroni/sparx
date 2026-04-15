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

public class InsertIteratorMaterializer<E> extends StatefulIteratorMaterializer<E> {

  public InsertIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      final E element) {
    setState(new InitialState(wrapped, element));
  }

  private class InitialState implements IteratorMaterializer<E> {

    private final E element;
    private final IteratorMaterializer<E> wrapped;

    private InitialState(final @NotNull IteratorMaterializer<E> wrapped, final E element) {
      this.wrapped = wrapped;
      this.element = element;
    }

    @Override
    public int currentKnownSize() {
      final int knownSize = wrapped.currentKnownSize();
      if (knownSize >= 0) {
        return SizeOverflowException.safeCast(knownSize + 1L);
      }
      return -1;
    }

    @Override
    public boolean isSizeKnown() {
      return wrapped.isSizeKnown();
    }

    @Override
    public boolean materializeHasNext() {
      return true;
    }

    @Override
    public E materializeNext() {
      setState(wrapped);
      return element;
    }

    @Override
    public int materializeSkip(final @Positive int count) {
      final IteratorMaterializer<E> state = setState(wrapped);
      if (count > 1) {
        return state.materializeSkip(count - 1) + 1;
      }
      return 1;
    }
  }
}
