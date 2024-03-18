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
import sparx.util.CollectionMaterializer;
import sparx.util.DequeueList;
import sparx.util.Require;
import sparx.util.UncheckedException;

class IncludesSliceCollectionMaterializer<E> implements CollectionMaterializer<Boolean> {

  private static final State FALSE_STATE = new FalseState();
  private static final State TRUE_STATE = new TrueState();

  private volatile State state;

  IncludesSliceCollectionMaterializer(@NotNull final CollectionMaterializer<E> wrapped,
      @NotNull final CollectionMaterializer<?> elementsMaterializer) {
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
    throw new IndexOutOfBoundsException(String.valueOf(index));
  }

  @Override
  public boolean materializeEmpty() {
    return false;
  }

  @Override
  public @NotNull Iterator<Boolean> materializeIterator() {
    return new CollectionMaterializerIterator<Boolean>(this);
  }

  @Override
  public int materializeSize() {
    return 1;
  }

  private interface State {

    boolean materialized();
  }

  private static class ExceptionState implements State {

    private final Exception ex;

    private ExceptionState(@NotNull final Exception ex) {
      this.ex = ex;
    }

    @Override
    public boolean materialized() {
      throw UncheckedException.throwUnchecked(ex);
    }
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

    private final CollectionMaterializer<?> elementsMaterializer;
    private final AtomicBoolean isMaterialized = new AtomicBoolean(false);
    private final CollectionMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final CollectionMaterializer<E> wrapped,
        @NotNull final CollectionMaterializer<?> elementsMaterializer) {
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
        final Iterator<E> iterator = wrapped.materializeIterator();
        final CollectionMaterializer<?> elementsMaterializer = this.elementsMaterializer;
        Iterator<?> elementsIterator = elementsMaterializer.materializeIterator();
        while (iterator.hasNext()) {
          if (!elementsIterator.hasNext()) {
            state = TRUE_STATE;
            return true;
          }
          final E left = iterator.next();
          Object right = elementsIterator.next();
          if (left != right && (left == null || !left.equals(right))) {
            boolean matches = false;
            while (!queue.isEmpty() && !matches) {
              queue.pop();
              matches = true;
              elementsIterator = elementsMaterializer.materializeIterator();
              for (final E e : queue) {
                if (!iterator.hasNext()) {
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
        }
        state = FALSE_STATE;
        return false;
      } catch (final Exception e) {
        state = new ExceptionState(e);
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }
}
