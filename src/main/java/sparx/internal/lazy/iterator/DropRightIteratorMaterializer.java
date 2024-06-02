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
import sparx.util.DequeueList;
import sparx.util.Require;

public class DropRightIteratorMaterializer<E> extends AbstractIteratorMaterializer<E> {

  private volatile IteratorMaterializer<E> state;

  public DropRightIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      final int maxElements) {
    state = new ImmaterialState(Require.notNull(wrapped, "wrapped"),
        Require.positive(maxElements, "maxElements"));
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

  private class ImmaterialState implements IteratorMaterializer<E> {

    private final DequeueList<E> elements = new DequeueList<E>();
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
      final DequeueList<E> elements = this.elements;
      final IteratorMaterializer<E> wrapped = this.wrapped;
      if (elements.isEmpty()) {
        final int maxElements = this.maxElements;
        while (elements.size() < maxElements && wrapped.materializeHasNext()) {
          elements.add(wrapped.materializeNext());
        }
        if (elements.size() < maxElements) {
          state = EmptyIteratorMaterializer.instance();
          return false;
        }
      }
      return wrapped.materializeHasNext();
    }

    @Override
    public E materializeNext() {
      if (!materializeHasNext()) {
        throw new NoSuchElementException();
      }
      final DequeueList<E> elements = this.elements;
      elements.add(wrapped.materializeNext());
      return elements.removeFirst();
    }

    @Override
    public int materializeSkip(final int count) {
      throw new UnsupportedOperationException();
    }
  }
}
