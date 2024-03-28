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
package sparx;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import sparx.collection.ListMaterializer;
import sparx.util.DequeueList;
import sparx.util.Require;
import sparx.util.UncheckedException;

class IncludesSliceListMaterializer<E> implements ListMaterializer<Boolean> {

  private static final State FALSE_STATE = new FalseState();
  private static final State TRUE_STATE = new TrueState();

  private volatile State state;

  IncludesSliceListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      @NotNull final ListMaterializer<?> elementsMaterializer) {
    state = new ImmaterialState(Require.notNull(wrapped, "wrapped"),
        Require.notNull(elementsMaterializer, "elementsMaterializer"));
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    return index == 0;
  }

  @Override
  public int knownSize() {
    return 1;
  }

  @Override
  public Boolean materializeElement(final int index) {
    if (index == 0) {
      return state.materialized();
    }
    throw new IndexOutOfBoundsException(Integer.toString(index));
  }

  @Override
  public boolean materializeEmpty() {
    return false;
  }

  @Override
  public @NotNull Iterator<Boolean> materializeIterator() {
    return new ListMaterializerIterator<Boolean>(this);
  }

  @Override
  public int materializeSize() {
    return 1;
  }

  private interface State {

    boolean materialized();
  }

  private static class FalseState implements State {

    @Override
    public boolean materialized() {
      return false;
    }
  }

  private static class TrueState implements State {

    @Override
    public boolean materialized() {
      return true;
    }
  }

  private class ImmaterialState implements State {

    private final ListMaterializer<?> elementsMaterializer;
    private final AtomicBoolean isMaterialized = new AtomicBoolean(false);
    private final ListMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final ListMaterializer<E> wrapped,
        @NotNull final ListMaterializer<?> elementsMaterializer) {
      this.wrapped = wrapped;
      this.elementsMaterializer = elementsMaterializer;
    }

    @Override
    public boolean materialized() {
      if (!isMaterialized.compareAndSet(false, true)) {
        throw new ConcurrentModificationException();
      }
      try {
        final DequeueList<E> queue = new DequeueList<E>();
        final ListMaterializer<?> elementsMaterializer = this.elementsMaterializer;
        Iterator<?> elementsIterator = elementsMaterializer.materializeIterator();
        if (!elementsIterator.hasNext()) {
          state = TRUE_STATE;
          return true;
        }
        final ListMaterializer<E> wrapped = this.wrapped;
        int i = 0;
        while (wrapped.canMaterializeElement(i)) {
          if (!elementsIterator.hasNext()) {
            state = TRUE_STATE;
            return true;
          }
          final E left = wrapped.materializeElement(i);
          Object right = elementsIterator.next();
          if (left != right && (left == null || !left.equals(right))) {
            boolean matches = false;
            while (!queue.isEmpty() && !matches) {
              queue.pop();
              matches = true;
              elementsIterator = elementsMaterializer.materializeIterator();
              for (final E e : queue) {
                if (!wrapped.canMaterializeElement(i + 1)) {
                  state = TRUE_STATE;
                  return true;
                }
                right = elementsIterator.next();
                if (e != right && (e == null || !e.equals(right))) {
                  matches = false;
                  break;
                }
              }
            }
            if (!matches) {
              elementsIterator = elementsMaterializer.materializeIterator();
            }
          } else {
            queue.add(left);
          }
          ++i;
        }
        state = FALSE_STATE;
        return false;
      } catch (final Exception e) {
        isMaterialized.set(false);
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }
}
