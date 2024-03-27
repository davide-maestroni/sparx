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
import sparx.util.Require;
import sparx.util.UncheckedException;
import sparx.util.function.Function;
import sparx.util.function.Predicate;

class FlatMapFirstWhereListMaterializer<E> implements ListMaterializer<E> {

  private final ListMaterializer<E> wrapped;

  private volatile State<E> state;

  FlatMapFirstWhereListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      @NotNull final Predicate<? super E> predicate,
      @NotNull final Function<? super E, ? extends ListMaterializer<E>> mapper) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
    state = new ImmaterialState(Require.notNull(predicate, "predicate"),
        Require.notNull(mapper, "mapper"));
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    if (index < 0) {
      return false;
    }
    final State<E> state = this.state;
    final int numElements = state.materializeIndex();
    if (index < numElements) {
      return wrapped.canMaterializeElement(index);
    }
    final int elementIndex = index - numElements;
    final ListMaterializer<E> materializer = state.materialized();
    if (materializer.canMaterializeElement(elementIndex)) {
      return true;
    }
    return wrapped.canMaterializeElement(index - materializer.materializeSize());
  }

  @Override
  public int knownSize() {
    final int knownSize = wrapped.knownSize();
    if (knownSize == 0) {
      return 0;
    }
    return -1;
  }

  @Override
  public E materializeElement(final int index) {
    if (index < 0) {
      throw new IndexOutOfBoundsException(String.valueOf(index));
    }
    final State<E> state = this.state;
    final int numElements = state.materializeIndex();
    if (index < numElements) {
      return wrapped.materializeElement(index);
    }
    final int elementIndex = index - numElements;
    final ListMaterializer<E> materializer = state.materialized();
    if (materializer.canMaterializeElement(elementIndex)) {
      return materializer.materializeElement(elementIndex);
    }
    return wrapped.materializeElement(index - materializer.materializeSize());
  }

  @Override
  public boolean materializeEmpty() {
    final ListMaterializer<E> wrapped = this.wrapped;
    if (wrapped.materializeEmpty()) {
      return true;
    }
    final State<E> state = this.state;
    if (wrapped.materializeSize() == 1 && state.materializeIndex() == 0) {
      return state.materialized().materializeEmpty();
    }
    return false;
  }

  @Override
  public @NotNull Iterator<E> materializeIterator() {
    return new ListMaterializerIterator<E>(this);
  }

  @Override
  public int materializeSize() {
    final ListMaterializer<E> wrapped = this.wrapped;
    final int size = wrapped.materializeSize();
    final State<E> state = this.state;
    if (size <= state.materializeIndex()) {
      return size;
    }
    final ListMaterializer<E> materializer = state.materialized();
    return size + materializer.materializeSize() - 1;
  }

  private interface State<E> {

    @NotNull ListMaterializer<E> materialized();

    int materializeIndex();
  }

  private static class MaterialState<E> implements State<E> {

    private final int index;
    private final ListMaterializer<E> materializer;

    private MaterialState(@NotNull final ListMaterializer<E> materializer, final int index) {
      this.materializer = materializer;
      this.index = index;
    }

    @Override
    public @NotNull ListMaterializer<E> materialized() {
      return materializer;
    }

    @Override
    public int materializeIndex() {
      return index;
    }
  }

  private class ImmaterialState implements State<E> {

    private final AtomicBoolean isMaterialized = new AtomicBoolean(false);
    private final Function<? super E, ? extends ListMaterializer<E>> mapper;
    private final Predicate<? super E> predicate;

    private int index;

    private ImmaterialState(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends ListMaterializer<E>> mapper) {
      this.predicate = predicate;
      this.mapper = mapper;
    }

    @Override
    public @NotNull ListMaterializer<E> materialized() {
      if (!isMaterialized.compareAndSet(false, true)) {
        throw new ConcurrentModificationException();
      }
      try {
        final int index = materializeIndex();
        final ListMaterializer<E> materializer = mapper.apply(
            wrapped.materializeElement(index));
        state = new MaterialState<E>(materializer, index);
        return materializer;
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public int materializeIndex() {
      if (index >= 0) {
        return index;
      }
      try {
        int i = 0;
        final Predicate<? super E> predicate = this.predicate;
        final Iterator<E> iterator = wrapped.materializeIterator();
        while (iterator.hasNext()) {
          final E next = iterator.next();
          if (predicate.test(next)) {
            break;
          }
          ++i;
        }
        index = i;
        return i;
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }
}
