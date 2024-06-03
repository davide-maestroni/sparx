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

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import sparx.util.UncheckedException;
import sparx.util.function.IndexedFunction;

public class MapAfterListMaterializer<E> extends AbstractListMaterializer<E> implements
    ListMaterializer<E> {

  private final int numElements;
  private final ListMaterializer<E> wrapped;

  private volatile State<E> state;

  // numElements: not negative
  public MapAfterListMaterializer(@NotNull final ListMaterializer<E> wrapped, final int numElements,
      @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
    this.wrapped = wrapped;
    this.numElements = numElements;
    state = new ImmaterialState(mapper);
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    return wrapped.canMaterializeElement(index);
  }

  @Override
  public int knownSize() {
    return wrapped.knownSize();
  }

  @Override
  public boolean materializeContains(final Object element) {
    final ListMaterializer<E> wrapped = this.wrapped;
    if (wrapped.materializeSize() <= numElements) {
      return wrapped.materializeContains(element);
    }
    return super.materializeContains(element);
  }

  @Override
  public E materializeElement(final int index) {
    if (index < 0) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    if (index != numElements) {
      return wrapped.materializeElement(index);
    }
    return state.materialized();
  }

  @Override
  public int materializeElements() {
    final int size = wrapped.materializeElements();
    if (numElements < size) {
      state.materialized();
    }
    return size;
  }

  @Override
  public boolean materializeEmpty() {
    return wrapped.materializeEmpty();
  }

  @Override
  public @NotNull Iterator<E> materializeIterator() {
    return new ListMaterializerIterator<E>(this);
  }

  @Override
  public int materializeSize() {
    return wrapped.materializeSize();
  }

  private interface State<E> {

    E materialized();
  }

  private static class ElementState<E> implements State<E> {

    private final E element;

    private ElementState(final E element) {
      this.element = element;
    }

    @Override
    public E materialized() {
      return element;
    }
  }

  private class ImmaterialState implements State<E> {

    private final AtomicBoolean isMaterialized = new AtomicBoolean(false);
    private final IndexedFunction<? super E, ? extends E> mapper;

    private ImmaterialState(@NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      this.mapper = mapper;
    }

    @Override
    public E materialized() {
      if (!isMaterialized.compareAndSet(false, true)) {
        throw new ConcurrentModificationException();
      }
      try {
        final int numElements = MapAfterListMaterializer.this.numElements;
        final E element = mapper.apply(numElements, wrapped.materializeElement(numElements));
        state = new ElementState<E>(element);
        return element;
      } catch (final Exception e) {
        isMaterialized.set(false);
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }
}
