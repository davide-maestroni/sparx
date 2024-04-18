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

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import sparx.collection.internal.AbstractCollectionMaterializer;
import sparx.util.Require;
import sparx.util.UncheckedException;
import sparx.util.function.IndexedFunction;

public class MapListMaterializer<E, F> implements ListMaterializer<F> {

  private static final Object EMPTY = new Object();
  private static final int SIZE_THRESHOLD = 64; // TODO: need to benchmark this

  private volatile ListMaterializer<F> state;

  public MapListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      @NotNull final IndexedFunction<? super E, F> mapper) {
    final int knownSize = wrapped.knownSize();
    if (knownSize < 0 || knownSize > SIZE_THRESHOLD) {
      state = new MapState(Require.notNull(wrapped, "wrapped"), Require.notNull(mapper, "mapper"));
    } else {
      final Object[] elements = new Object[knownSize];
      Arrays.fill(elements, EMPTY);
      state = new ArrayState(Require.notNull(wrapped, "wrapped"), Require.notNull(mapper, "mapper"),
          elements);
    }
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

  private class ArrayState extends AbstractCollectionMaterializer<F> implements
      ListMaterializer<F> {

    private final Object[] elements;
    private final IndexedFunction<? super E, F> mapper;
    private final AtomicInteger modCount = new AtomicInteger();
    private final ListMaterializer<E> wrapped;

    private ArrayState(@NotNull final ListMaterializer<E> wrapped,
        @NotNull final IndexedFunction<? super E, F> mapper, @NotNull final Object[] elements) {
      this.wrapped = wrapped;
      this.mapper = mapper;
      this.elements = elements;
    }

    @Override
    public boolean canMaterializeElement(final int index) {
      return index >= 0 && index < elements.length;
    }

    @Override
    public int knownSize() {
      return elements.length;
    }

    @Override
    @SuppressWarnings("unchecked")
    public F materializeElement(final int index) {
      final Object[] elements = this.elements;
      final Object current = elements[index];
      if (current != EMPTY) {
        return (F) current;
      }
      final AtomicInteger modCount = this.modCount;
      final int expectedCount = modCount.incrementAndGet();
      try {
        final F element = mapper.apply(index, wrapped.materializeElement(index));
        if (expectedCount != modCount.get()) {
          throw new ConcurrentModificationException();
        }
        return (F) (elements[index] = element);
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public boolean materializeEmpty() {
      return elements.length == 0;
    }

    @Override
    public @NotNull Iterator<F> materializeIterator() {
      return new ListMaterializerIterator<F>(this);
    }

    @Override
    public int materializeSize() {
      return elements.length;
    }
  }

  private class MapState extends AbstractCollectionMaterializer<F> implements ListMaterializer<F> {

    private final HashMap<Integer, F> elements = new HashMap<Integer, F>();
    private final IndexedFunction<? super E, F> mapper;
    private final AtomicInteger modCount = new AtomicInteger();
    private final ListMaterializer<E> wrapped;

    private MapState(@NotNull final ListMaterializer<E> wrapped,
        @NotNull final IndexedFunction<? super E, F> mapper) {
      this.wrapped = wrapped;
      this.mapper = mapper;
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
    public F materializeElement(final int index) {
      final HashMap<Integer, F> elements = this.elements;
      if (elements.containsKey(index)) {
        return elements.get(index);
      }
      final AtomicInteger modCount = this.modCount;
      final int expectedCount = modCount.incrementAndGet();
      try {
        final IndexedFunction<? super E, F> mapper = this.mapper;
        final ListMaterializer<E> wrapped = this.wrapped;
        final F element = mapper.apply(index, wrapped.materializeElement(index));
        if (expectedCount != modCount.get()) {
          throw new ConcurrentModificationException();
        }
        elements.put(index, element);
        final int knownSize = wrapped.knownSize();
        if ((knownSize >= 0 && knownSize < (elements.size() << 4)) || elements.size() > (
            Integer.MAX_VALUE >> 4)) {
          final Object[] elementsArray = new Object[knownSize];
          Arrays.fill(elementsArray, EMPTY);
          for (final Entry<Integer, F> entry : elements.entrySet()) {
            elementsArray[entry.getKey()] = entry.getValue();
          }
          if (expectedCount != modCount.get()) {
            throw new ConcurrentModificationException();
          }
          state = new ArrayState(wrapped, mapper, elementsArray);
        }
        return element;
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
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
      return wrapped.materializeSize();
    }
  }
}
