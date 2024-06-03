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

import java.util.Comparator;
import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;

public class MaxIteratorMaterializer<E> implements IteratorMaterializer<E> {

  private volatile IteratorMaterializer<E> state;

  public MaxIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      @NotNull final Comparator<? super E> comparator) {
    state = new ImmaterialState(wrapped, comparator);
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

    private final Comparator<? super E> comparator;
    private final IteratorMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final IteratorMaterializer<E> wrapped,
        @NotNull final Comparator<? super E> comparator) {
      this.wrapped = wrapped;
      this.comparator = comparator;
    }

    @Override
    public int knownSize() {
      final int knownSize = wrapped.knownSize();
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
        state = EmptyIteratorMaterializer.instance();
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
      state = new ElementToIteratorMaterializer<E>(max);
      return true;
    }

    @Override
    public E materializeNext() {
      if (!materializeHasNext()) {
        throw new NoSuchElementException();
      }
      return state.materializeNext();
    }

    @Override
    public int materializeSkip(final int count) {
      if (count > 0) {
        state = EmptyIteratorMaterializer.instance();
        return wrapped.materializeHasNext() ? 1 : 0;
      }
      return 0;
    }
  }
}
