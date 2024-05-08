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
package sparx.collection.internal.lazy.iterator;

import org.jetbrains.annotations.NotNull;
import sparx.util.Require;
import sparx.util.SizeOverflowException;

public class AppendIteratorMaterializer<E> implements IteratorMaterializer<E> {

  private volatile IteratorMaterializer<E> state;

  public AppendIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      final E element) {
    state = new ImmaterialState(Require.notNull(wrapped, "wrapped"), element);
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

    private final E element;
    private final IteratorMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final IteratorMaterializer<E> wrapped,
        final E element) {
      this.wrapped = Require.notNull(wrapped, "wrapped");
      this.element = element;
    }

    @Override
    public int knownSize() {
      final int knownSize = wrapped.knownSize();
      if (knownSize >= 0) {
        return SizeOverflowException.safeCast((long) knownSize + 1);
      }
      return -1;
    }

    @Override
    public boolean materializeHasNext() {
      return true;
    }

    @Override
    public E materializeNext() {
      final IteratorMaterializer<E> wrapped = this.wrapped;
      if (wrapped.materializeHasNext()) {
        return wrapped.materializeNext();
      }
      state = EmptyIteratorMaterializer.instance();
      return element;
    }

    @Override
    public int materializeSkip(final int count) {
      final int skipped = wrapped.materializeSkip(count);
      if (skipped < count) {
        state = EmptyIteratorMaterializer.instance();
        return skipped + 1;
      }
      return skipped;
    }
  }
}