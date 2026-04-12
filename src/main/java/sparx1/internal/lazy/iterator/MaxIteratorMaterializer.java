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

import java.util.Comparator;
import java.util.NoSuchElementException;
import sparx1.internal.lazy.IteratorMaterializer;
import sparx1.util.annotation.NotNull;
import sparx1.util.annotation.Positive;

public class MaxIteratorMaterializer<E> extends StatefulIteratorMaterializer<E> {

  public MaxIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      final @NotNull Comparator<? super E> comparator) {
    setState(new InitialState(wrapped, comparator));
  }

  private class InitialState implements IteratorMaterializer<E> {

    private final Comparator<? super E> comparator;
    private final IteratorMaterializer<E> wrapped;

    private InitialState(final @NotNull IteratorMaterializer<E> wrapped,
        final @NotNull Comparator<? super E> comparator) {
      this.wrapped = wrapped;
      this.comparator = comparator;
    }

    @Override
    public int currentKnownSize() {
      final int knownSize = wrapped.currentKnownSize();
      if (knownSize > 0) {
        return 1;
      }
      if (knownSize == 0) {
        return 0;
      }
      return -1;
    }

    @Override
    public boolean materializeHasNext() {
      final IteratorMaterializer<E> wrapped = this.wrapped;
      if (!wrapped.materializeHasNext()) {
        setEmptyState();
        return false;
      }
      final Comparator<? super E> comparator = this.comparator;
      E max = wrapped.materializeNext();
      while (wrapped.materializeHasNext()) {
        final E next = wrapped.materializeNext();
        if (comparator.compare(next, max) > 0) {
          max = next;
        }
      }
      setState(new ElementToIteratorMaterializer<E>(max));
      return true;
    }

    @Override
    public E materializeNext() {
      if (!materializeHasNext()) {
        throw new NoSuchElementException();
      }
      return getState().materializeNext();
    }

    @Override
    public int materializeSkip(final @Positive int count) {
      setEmptyState();
      return wrapped.materializeHasNext() ? 1 : 0;
    }
  }
}
