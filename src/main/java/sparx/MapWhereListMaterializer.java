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

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import sparx.collection.ListMaterializer;
import sparx.util.Require;
import sparx.util.UncheckedException;
import sparx.util.function.Function;
import sparx.util.function.Predicate;

class MapWhereListMaterializer<E> implements ListMaterializer<E> {

  private static final Object EMPTY = new Object();
  private static final int SIZE_THRESHOLD = 64; // TODO: need to benchmark this

  private volatile ListMaterializer<E> state;

  MapWhereListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      @NotNull final Predicate<? super E> predicate,
      @NotNull final Function<? super E, ? extends E> mapper) {
    final int knownSize = wrapped.knownSize();
    if (knownSize < 0 || knownSize > SIZE_THRESHOLD) {
      state = new MapState(Require.notNull(wrapped, "wrapped"),
          Require.notNull(predicate, "predicate"),
          Require.notNull(mapper, "mapper"));
    } else {
      final Object[] elements = new Object[knownSize];
      Arrays.fill(elements, EMPTY);
      state = new ArrayState(Require.notNull(wrapped, "wrapped"),
          Require.notNull(predicate, "predicate"),
          Require.notNull(mapper, "mapper"), elements);
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
  public E materializeElement(final int index) {
    return state.materializeElement(index);
  }

  @Override
  public boolean materializeEmpty() {
    return state.materializeEmpty();
  }

  @Override
  public @NotNull Iterator<E> materializeIterator() {
    return state.materializeIterator();
  }

  @Override
  public int materializeSize() {
    return state.materializeSize();
  }

  private class ArrayState implements ListMaterializer<E> {

    private final Object[] elements;
    private final Function<? super E, ? extends E> mapper;
    private final AtomicInteger modCount = new AtomicInteger();
    private final Predicate<? super E> predicate;
    private final ListMaterializer<E> wrapped;

    private ArrayState(@NotNull final ListMaterializer<E> wrapped,
        @NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends E> mapper, @NotNull final Object[] elements) {
      this.wrapped = wrapped;
      this.predicate = predicate;
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
    public E materializeElement(final int index) {
      final Object[] elements = this.elements;
      final Object current = elements[index];
      if (current != EMPTY) {
        return (E) current;
      }
      final AtomicInteger modCount = this.modCount;
      final int expectedCount = modCount.incrementAndGet();
      try {
        E element = wrapped.materializeElement(index);
        if (predicate.test(element)) {
          element = mapper.apply(element);
        }
        if (expectedCount != modCount.get()) {
          throw new ConcurrentModificationException();
        }
        return (E) (elements[index] = element);
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public boolean materializeEmpty() {
      return elements.length == 0;
    }

    @Override
    public @NotNull Iterator<E> materializeIterator() {
      return new ListMaterializerIterator<E>(this);
    }

    @Override
    public int materializeSize() {
      return elements.length;
    }
  }

  private class MapState implements ListMaterializer<E> {

    private final HashMap<Integer, E> elements = new HashMap<Integer, E>();
    private final Function<? super E, ? extends E> mapper;
    private final AtomicInteger modCount = new AtomicInteger();
    private final Predicate<? super E> predicate;
    private final ListMaterializer<E> wrapped;

    private MapState(@NotNull final ListMaterializer<E> wrapped,
        @NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends E> mapper) {
      this.wrapped = wrapped;
      this.predicate = predicate;
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
    public E materializeElement(final int index) {
      final HashMap<Integer, E> elements = this.elements;
      if (elements.containsKey(index)) {
        return elements.get(index);
      }
      final AtomicInteger modCount = this.modCount;
      final int expectedCount = modCount.incrementAndGet();
      try {
        final Predicate<? super E> predicate = this.predicate;
        final Function<? super E, ? extends E> mapper = this.mapper;
        final ListMaterializer<E> wrapped = this.wrapped;
        E element = wrapped.materializeElement(index);
        if (predicate.test(element)) {
          element = mapper.apply(wrapped.materializeElement(index));
        }
        if (expectedCount != modCount.get()) {
          throw new ConcurrentModificationException();
        }
        elements.put(index, element);
        final int knownSize = wrapped.knownSize();
        if ((knownSize >= 0 && knownSize < (elements.size() << 4)) ||
            elements.size() > (Integer.MAX_VALUE >> 4)) {
          final Object[] elementsArray = new Object[knownSize];
          Arrays.fill(elementsArray, EMPTY);
          for (final Entry<Integer, E> entry : elements.entrySet()) {
            elementsArray[entry.getKey()] = entry.getValue();
          }
          if (expectedCount != modCount.get()) {
            throw new ConcurrentModificationException();
          }
          state = new ArrayState(wrapped, predicate, mapper, elementsArray);
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
    public @NotNull Iterator<E> materializeIterator() {
      return new ListMaterializerIterator<E>(this);
    }

    @Override
    public int materializeSize() {
      return wrapped.materializeSize();
    }
  }
}
