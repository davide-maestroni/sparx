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
package sparx.collection.internal.list;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import sparx.collection.ListMaterializer;
import sparx.util.IndexOverflowException;
import sparx.util.Require;
import sparx.util.SizeOverflowException;
import sparx.util.UncheckedException;
import sparx.util.function.IndexedFunction;

public class FlatMapAfterListMaterializer<E> implements ListMaterializer<E> {

  private final int numElements;
  private final ListMaterializer<E> wrapped;

  private volatile State<E> state;

  public FlatMapAfterListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      final int numElements,
      @NotNull final IndexedFunction<? super E, ? extends ListMaterializer<E>> mapper) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
    this.numElements = Require.notNegative(numElements, "numElements");
    state = new ImmaterialState(Require.notNull(mapper, "mapper"));
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    if (index < 0) {
      return false;
    }
    final int numElements = this.numElements;
    if (index < numElements) {
      return wrapped.canMaterializeElement(index);
    }
    if (numElements >= wrapped.materializeSize()) {
      return false;
    }
    final int elementIndex = index - numElements;
    final ListMaterializer<E> materializer = state.materialized();
    final long wrappedIndex = (long) index - materializer.materializeSize() + 1;
    return materializer.canMaterializeElement(elementIndex) || (wrappedIndex < Integer.MAX_VALUE
        && wrapped.canMaterializeElement((int) wrappedIndex));
  }

  @Override
  public int knownSize() {
    final int knownSize = wrapped.knownSize();
    if (knownSize >= 0 && knownSize <= numElements) {
      return knownSize;
    }
    return -1;
  }

  @Override
  public E materializeElement(final int index) {
    if (index < 0) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    final int numElements = this.numElements;
    if (index < numElements) {
      return wrapped.materializeElement(index);
    }
    final int elementIndex = index - numElements;
    final ListMaterializer<E> materializer = state.materialized();
    if (materializer.canMaterializeElement(elementIndex)) {
      return materializer.materializeElement(elementIndex);
    }
    return wrapped.materializeElement(
        IndexOverflowException.safeCast((long) index - materializer.materializeSize() + 1));
  }

  @Override
  public boolean materializeEmpty() {
    final ListMaterializer<E> wrapped = this.wrapped;
    return wrapped.materializeEmpty() || (wrapped.materializeSize() == 1 && numElements == 0
        && state.materialized().materializeEmpty());
  }

  @Override
  public @NotNull Iterator<E> materializeIterator() {
    return new ListMaterializerIterator<E>(this);
  }

  @Override
  public int materializeSize() {
    final ListMaterializer<E> wrapped = this.wrapped;
    final int wrappedSize = wrapped.materializeSize();
    if (wrappedSize <= numElements) {
      return wrappedSize;
    }
    final long size = (long) wrappedSize + state.materialized().materializeSize() - 1;
    return SizeOverflowException.safeCast(size);
  }

  private interface State<E> {

    @NotNull
    ListMaterializer<E> materialized();
  }

  private static class MaterialState<E> implements State<E> {

    private final ListMaterializer<E> materializer;

    private MaterialState(@NotNull final ListMaterializer<E> materializer) {
      this.materializer = materializer;
    }

    @Override
    public @NotNull ListMaterializer<E> materialized() {
      return materializer;
    }
  }

  private class ImmaterialState implements State<E> {

    private final AtomicBoolean isMaterialized = new AtomicBoolean(false);
    private final IndexedFunction<? super E, ? extends ListMaterializer<E>> mapper;

    private ImmaterialState(
        @NotNull final IndexedFunction<? super E, ? extends ListMaterializer<E>> mapper) {
      this.mapper = mapper;
    }

    @Override
    public @NotNull ListMaterializer<E> materialized() {
      if (!isMaterialized.compareAndSet(false, true)) {
        throw new ConcurrentModificationException();
      }
      try {
        final int numElements = FlatMapAfterListMaterializer.this.numElements;
        final ListMaterializer<E> materializer = mapper.apply(numElements,
            wrapped.materializeElement(numElements));
        state = new MaterialState<E>(materializer);
        return materializer;
      } catch (final Exception e) {
        isMaterialized.set(false);
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }
}
