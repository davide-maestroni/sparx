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
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import sparx.collection.ListMaterializer;
import sparx.util.Require;
import sparx.util.UncheckedException;

class IncludesAllListMaterializer<E> implements ListMaterializer<Boolean> {

  private static final State FALSE_STATE = new FalseState();
  private static final State TRUE_STATE = new TrueState();

  private volatile State state;

  IncludesAllListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      @NotNull final Iterable<?> elements) {
    state = new ImmaterialState(Require.notNull(wrapped, "wrapped"),
        elements.iterator());
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

    private final Iterator<?> elementsIterator;
    private final AtomicBoolean isMaterialized = new AtomicBoolean(false);
    private final ListMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final ListMaterializer<E> wrapped,
        @NotNull final Iterator<?> elementsIterator) {
      this.wrapped = wrapped;
      this.elementsIterator = elementsIterator;
    }

    @Override
    public boolean materialized() {
      if (!isMaterialized.compareAndSet(false, true)) {
        throw new ConcurrentModificationException();
      }
      try {
        final HashSet<Object> elements = new HashSet<Object>();
        final Iterator<?> elementsIterator = this.elementsIterator;
        while (elementsIterator.hasNext()) {
          elements.add(elementsIterator.next());
        }
        if (elements.isEmpty()) {
          state = TRUE_STATE;
          return true;
        }
        final Iterator<E> iterator = wrapped.materializeIterator();
        while (iterator.hasNext()) {
          elements.remove(iterator.next());
          if (elements.isEmpty()) {
            state = TRUE_STATE;
            return true;
          }
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
