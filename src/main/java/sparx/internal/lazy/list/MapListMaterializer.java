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
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import sparx.internal.util.ElementsCache;
import sparx.util.UncheckedException;
import sparx.util.function.IndexedFunction;

public class MapListMaterializer<E, F> extends AbstractListMaterializer<F> {

  private volatile ListMaterializer<F> state;

  public MapListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      @NotNull final IndexedFunction<? super E, F> mapper) {
    state = new ImmaterialState(wrapped, mapper);
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    return state.canMaterializeElement(index);
  }

  @Override
  public int knownSize() {
    return state.knownSize();
  }

  @Override
  public boolean materializeContains(final Object element) {
    return state.materializeContains(element);
  }

  @Override
  public F materializeElement(final int index) {
    return state.materializeElement(index);
  }

  @Override
  public boolean materializeEmpty() {
    return state.materializeEmpty();
  }

  @Override
  public @NotNull Iterator<F> materializeIterator() {
    return state.materializeIterator();
  }

  @Override
  public int materializeSize() {
    return state.materializeSize();
  }

  private class ImmaterialState extends AbstractListMaterializer<F> {

    private final ElementsCache<F> elementsCache;
    private final IndexedFunction<? super E, F> mapper;
    private final AtomicInteger modCount = new AtomicInteger();
    private final ListMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final ListMaterializer<E> wrapped,
        @NotNull final IndexedFunction<? super E, F> mapper) {
      this.wrapped = wrapped;
      this.mapper = mapper;
      elementsCache = new ElementsCache<F>(wrapped.knownSize());
    }

    @Override
    public boolean canMaterializeElement(final int index) {
      return wrapped.canMaterializeElement(index);
    }

    @Override
    public F materializeElement(final int index) {
      final ElementsCache<F> elementsCache = this.elementsCache;
      if (elementsCache.has(index)) {
        return elementsCache.get(index);
      }
      final AtomicInteger modCount = this.modCount;
      final int expectedCount = modCount.incrementAndGet();
      try {
        final ListMaterializer<E> wrapped = this.wrapped;
        final F element = mapper.apply(index, wrapped.materializeElement(index));
        if (elementsCache.set(index, element) == wrapped.knownSize()) {
          state = new ListToListMaterializer<F>(elementsCache.toList());
        }
        if (expectedCount != modCount.get()) {
          throw new ConcurrentModificationException();
        }
        return element;
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public int materializeElements() {
      final int size = super.materializeElements();
      final AtomicInteger modCount = this.modCount;
      final int expectedCount = modCount.incrementAndGet();
      try {
        state = new ListToListMaterializer<F>(elementsCache.toList());
        if (expectedCount != modCount.get()) {
          throw new ConcurrentModificationException();
        }
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
      return size;
    }

    @Override
    public int knownSize() {
      return wrapped.knownSize();
    }

    @Override
    public boolean materializeEmpty() {
      return wrapped.materializeEmpty();
    }

    @Override
    public @NotNull Iterator<F> materializeIterator() {
      return new ListMaterializerIterator<F>(this);
    }

    @Override
    public int materializeSize() {
      final int wrappedSize = wrapped.materializeSize();
      elementsCache.setSize(wrappedSize);
      return wrappedSize;
    }
  }
}
