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

class FindIndexOfSliceCollectionMaterializer<E> implements CollectionMaterializer<Integer> {

  private static final State NOT_FOUND = new NotFoundState();

  private volatile State state;

  FindIndexOfSliceCollectionMaterializer(@NotNull final CollectionMaterializer<E> wrapped,
      @NotNull final CollectionMaterializer<?> elementsMaterializer) {
    state = new ImmaterialState(Require.notNull(wrapped, "wrapped"),
        Require.notNull(elementsMaterializer, "elementsMaterializer"));
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    return index == 0 && state.materialized() >= 0;
  }

  @Override
  public int knownSize() {
    return state.knownSize();
  }

  @Override
  public Integer materializeElement(final int index) {
    final int i = state.materialized();
    if (i >= 0 && index == 0) {
      return i;
    }
    throw new IndexOutOfBoundsException(String.valueOf(index));
  }

  @Override
  public boolean materializeEmpty() {
    return state.materialized() < 0;
  }

  @Override
  public @NotNull Iterator<Integer> materializeIterator() {
    return new CollectionMaterializerIterator<Integer>(this);
  }

  @Override
  public int materializeSize() {
    return state.materialized() < 0 ? 0 : 1;
  }

  private interface State {

    int knownSize();

    int materialized();
  }

  private static class ExceptionState implements State {

    private final Exception ex;

    private ExceptionState(@NotNull final Exception ex) {
      this.ex = ex;
    }

    @Override
    public int knownSize() {
      return 1;
    }

    @Override
    public int materialized() {
      throw UncheckedException.throwUnchecked(ex);
    }
  }

  private static class IndexState implements State {

    private final int index;

    private IndexState(final int index) {
      this.index = index;
    }

    @Override
    public int knownSize() {
      return 1;
    }

    @Override
    public int materialized() {
      return index;
    }
  }

  private static class NotFoundState implements State {

    @Override
    public int knownSize() {
      return 0;
    }

    @Override
    public int materialized() {
      return -1;
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
    public int knownSize() {
      return -1;
    }

    @Override
    public int materialized() {
      if (!isMaterialized.compareAndSet(false, true)) {
        throw new ConcurrentModificationException();
      }
      try {
        final DequeueList<E> queue = new DequeueList<E>();
        final Iterator<E> iterator = wrapped.materializeIterator();
        final CollectionMaterializer<?> elementsMaterializer = this.elementsMaterializer;
        Iterator<?> elementsIterator = elementsMaterializer.materializeIterator();
        int index = 0;
        while (iterator.hasNext()) {
          if (!elementsIterator.hasNext()) {
            state = new IndexState(index);
            return index;
          }
          final E left = iterator.next();
          Object right = elementsIterator.next();
          if (left != right && (left == null || !left.equals(right))) {
            boolean matches = false;
            while (!queue.isEmpty() && !matches) {
              ++index;
              queue.pop();
              matches = true;
              elementsIterator = elementsMaterializer.materializeIterator();
              for (final E e : queue) {
                if (!iterator.hasNext()) {
                  final int i = index - queue.size();
                  state = new IndexState(i);
                  return i;
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
            ++index;
          } else {
            queue.add(left);
          }
        }
        state = NOT_FOUND;
        return -1;
      } catch (final Exception e) {
        state = new ExceptionState(e);
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }
}
