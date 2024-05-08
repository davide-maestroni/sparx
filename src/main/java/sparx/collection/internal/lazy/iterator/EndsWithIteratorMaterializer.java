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
import sparx.collection.internal.lazy.list.ListMaterializer;
import sparx.util.DequeueList;
import sparx.util.Require;
import sparx.util.SizeOverflowException;

public class EndsWithIteratorMaterializer<E> implements IteratorMaterializer<Boolean> {

  private volatile IteratorMaterializer<Boolean> state;

  public EndsWithIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      @NotNull final ListMaterializer<?> elementsMaterializer) {
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
  public Boolean materializeNext() {
    return state.materializeNext();
  }

  @Override
  public int materializeSkip(final int count) {
    return state.materializeSkip(count);
  }

  private class ImmaterialState implements IteratorMaterializer<Boolean> {

    private final ListMaterializer<?> elementsMaterializer;
    private final IteratorMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final IteratorMaterializer<E> wrapped,
        @NotNull final ListMaterializer<?> elementsMaterializer) {
      this.wrapped = wrapped;
      this.elementsMaterializer = elementsMaterializer;
    }

    @Override
    public int knownSize() {
      return 1;
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
      final DequeueList<E> wrappedElements = new DequeueList<E>(
          SizeOverflowException.safeCast((long) elementsSize + 1));
      while (wrapped.materializeHasNext()) {
        wrappedElements.add(wrapped.materializeNext());
        if (wrappedElements.size() > elementsSize) {
          wrappedElements.removeFirst();
        }
      }
      final int wrappedSize = wrappedElements.size();
      if (wrappedSize < elementsSize) {
        state = EmptyIteratorMaterializer.instance();
        return false;
      }
      for (int i = wrappedSize - 1, j = elementsSize - 1; i >= 0 && j >= 0; --i, --j) {
        final E left = wrappedElements.get(i);
        final Object right = elementsMaterializer.materializeElement(j);
        if (left != right && (left == null || !left.equals(right))) {
          state = EmptyIteratorMaterializer.instance();
          return false;
        }
      }
      state = EmptyIteratorMaterializer.instance();
      return true;
    }

    @Override
    public int materializeSkip(final int count) {
      if (count > 0) {
        state = EmptyIteratorMaterializer.instance();
        return 1;
      }
      return 0;
    }
  }
}
