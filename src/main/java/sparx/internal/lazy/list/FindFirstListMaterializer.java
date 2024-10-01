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
package sparx.internal.lazy.list;

import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import sparx.util.UncheckedException;
import sparx.util.annotation.NotNegative;
import sparx.util.function.IndexedPredicate;

public class FindFirstListMaterializer<E> implements ListMaterializer<E> {

  private static final EmptyState<?> EMPTY_STATE = new EmptyState<Object>();

  private volatile State<E> state;

  public FindFirstListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      @NotNull final IndexedPredicate<? super E> predicate) {
    state = new ImmaterialState(wrapped, predicate);
  }

  @Override
  public boolean canMaterializeElement(@NotNegative final int index) {
    return !state.materialized().isEmpty();
  }

  @Override
  public int knownSize() {
    return state.knownSize();
  }

  @Override
  @SuppressWarnings("SuspiciousMethodCalls")
  public boolean materializeContains(final Object element) {
    return state.materialized().contains(element);
  }

  @Override
  public E materializeElement(@NotNegative final int index) {
    return state.materialized().get(index);
  }

  @Override
  public int materializeElements() {
    return state.materialized().size();
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

    @NotNull
    List<E> materialized();
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
    private final IndexedPredicate<? super E> predicate;
    private final ListMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final ListMaterializer<E> wrapped,
        @NotNull final IndexedPredicate<? super E> predicate) {
      this.wrapped = wrapped;
      this.predicate = predicate;
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
        final ListMaterializer<E> wrapped = this.wrapped;
        final IndexedPredicate<? super E> predicate = this.predicate;
        int i = 0;
        while (wrapped.canMaterializeElement(i)) {
          final E element = wrapped.materializeElement(i);
          if (predicate.test(i, element)) {
            return (state = new ElementState<E>(element)).materialized();
          }
          ++i;
        }
        state = (State<E>) EMPTY_STATE;
        return Collections.emptyList();
      } catch (final Exception e) {
        isMaterialized.set(false);
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }
}
