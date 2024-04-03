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

import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import sparx.collection.ListMaterializer;
import sparx.util.Require;
import sparx.util.UncheckedException;

class MaxListMaterializer<E> implements ListMaterializer<E> {

  private static final EmptyState<?> EMPTY_STATE = new EmptyState<Object>();

  private volatile State<E> state;

  MaxListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      @NotNull final Comparator<? super E> comparator) {
    state = new ImmaterialState(Require.notNull(wrapped, "wrapped"),
        Require.notNull(comparator, "comparator"));
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    return index == 0 && !state.materialized().isEmpty();
  }

  @Override
  public int knownSize() {
    return state.knownSize();
  }

  @Override
  public E materializeElement(final int index) {
    return state.materialized().get(index);
  }

  @Override
  public boolean materializeEmpty() {
    return state.materialized().isEmpty();
  }

  @Override
  public @NotNull Iterator<E> materializeIterator() {
    return state.materialized().iterator();
  }

  @Override
  public int materializeSize() {
    return state.materialized().size();
  }

  private interface State<E> {

    int knownSize();

    @NotNull List<E> materialized();
  }

  private static class EmptyState<E> implements State<E> {

    @Override
    public int knownSize() {
      return 0;
    }

    @Override
    public @NotNull List<E> materialized() {
      return Collections.emptyList();
    }
  }

  private static class ElementState<E> implements State<E> {

    private final List<E> elements;

    private ElementState(@NotNull final E element) {
      this.elements = Collections.singletonList(element);
    }

    @Override
    public int knownSize() {
      return 1;
    }

    @Override
    public @NotNull List<E> materialized() {
      return elements;
    }
  }

  private class ImmaterialState implements State<E> {

    private final AtomicBoolean isMaterialized = new AtomicBoolean(false);
    private final Comparator<? super E> comparator;
    private final ListMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final ListMaterializer<E> wrapped,
        @NotNull final Comparator<? super E> comparator) {
      this.wrapped = wrapped;
      this.comparator = comparator;
    }

    @Override
    public int knownSize() {
      return -1;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull List<E> materialized() {
      if (!isMaterialized.compareAndSet(false, true)) {
        throw new ConcurrentModificationException();
      }
      try {
        final Comparator<? super E> comparator = this.comparator;
        final Iterator<E> iterator = wrapped.materializeIterator();
        if (!iterator.hasNext()) {
          state = (State<E>) EMPTY_STATE;
          return Collections.emptyList();
        }
        E max = iterator.next();
        while (iterator.hasNext()) {
          final E next = iterator.next();
          if (comparator.compare(next, max) > 0) {
            max = next;
          }
        }
        state = new ElementState<E>(max);
        return state.materialized();
      } catch (final Exception e) {
        isMaterialized.set(false);
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }
}