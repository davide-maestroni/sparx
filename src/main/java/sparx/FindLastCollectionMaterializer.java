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
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import sparx.util.CollectionMaterializer;
import sparx.util.Require;
import sparx.util.UncheckedException;
import sparx.util.function.Predicate;

class FindLastCollectionMaterializer<E> implements CollectionMaterializer<E> {

  private static final EmptyState<?> EMPTY_STATE = new EmptyState<Object>();

  private volatile State<E> state;

  FindLastCollectionMaterializer(@NotNull final CollectionMaterializer<E> wrapped,
      @NotNull final Predicate<? super E> predicate) {
    state = new ImmaterialState(Require.notNull(wrapped, "wrapped"),
        Require.notNull(predicate, "predicate"));
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    return !state.materialized().isEmpty() && index == 0;
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

    @NotNull List<E> materialized();
  }

  private static class EmptyState<E> implements State<E> {

    @Override
    public @NotNull List<E> materialized() {
      return Collections.emptyList();
    }
  }

  private static class ExceptionState<E> implements State<E> {

    private final Exception ex;

    private ExceptionState(@NotNull final Exception ex) {
      this.ex = ex;
    }

    @Override
    public @NotNull List<E> materialized() {
      throw UncheckedException.throwUnchecked(ex);
    }
  }

  private static class ElementState<E> implements State<E> {

    private final List<E> elements;

    private ElementState(@NotNull final E element) {
      this.elements = Collections.singletonList(element);
    }

    @Override
    public @NotNull List<E> materialized() {
      return elements;
    }
  }

  private class ImmaterialState implements State<E> {

    private final AtomicBoolean isMaterialized = new AtomicBoolean(false);
    private final Predicate<? super E> predicate;
    private final CollectionMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final CollectionMaterializer<E> wrapped,
        @NotNull final Predicate<? super E> predicate) {
      this.wrapped = wrapped;
      this.predicate = predicate;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull List<E> materialized() {
      if (!isMaterialized.compareAndSet(false, true)) {
        throw new ConcurrentModificationException();
      }
      try {
        final CollectionMaterializer<E> wrapped = this.wrapped;
        final Predicate<? super E> predicate = this.predicate;
        final int size = wrapped.materializeSize();
        for (int i = size - 1; i >= 0; --i) {
          final E element = wrapped.materializeElement(i);
          if (predicate.test(element)) {
            final ElementState<E> elementState = new ElementState<E>(element);
            state = elementState;
            return elementState.materialized();
          }
        }
        state = (State<E>) EMPTY_STATE;
        return Collections.emptyList();
      } catch (final Exception e) {
        state = new ExceptionState<E>(e);
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }
}
