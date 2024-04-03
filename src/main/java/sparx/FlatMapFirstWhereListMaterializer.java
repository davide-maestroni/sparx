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
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import sparx.collection.ListMaterializer;
import sparx.util.Require;
import sparx.util.UncheckedException;
import sparx.util.function.Function;
import sparx.util.function.Predicate;

class FlatMapFirstWhereListMaterializer<E> implements ListMaterializer<E> {

  private volatile ListMaterializer<E> state;

  FlatMapFirstWhereListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      @NotNull final Predicate<? super E> predicate,
      @NotNull final Function<? super E, ? extends ListMaterializer<E>> mapper) {
    state = new ImmaterialState(Require.notNull(wrapped, "wrapped"),
        Require.notNull(predicate, "predicate"),
        Require.notNull(mapper, "mapper"));
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
    return new ListMaterializerIterator<E>(this);
  }

  @Override
  public int materializeSize() {
    return state.materializeSize();
  }

  private static class MaterialState<E> implements ListMaterializer<E> {

    private final int numElements;
    private final ListMaterializer<E> materializer;
    private final ListMaterializer<E> wrapped;

    private MaterialState(@NotNull final ListMaterializer<E> wrapped,
        @NotNull final ListMaterializer<E> materializer, final int numElements) {
      this.wrapped = wrapped;
      this.materializer = materializer;
      this.numElements = numElements;
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
      final int elementIndex = index - numElements;
      final ListMaterializer<E> materializer = this.materializer;
      return materializer.canMaterializeElement(elementIndex) ||
          wrapped.canMaterializeElement(index - materializer.materializeSize() + 1);
    }

    @Override
    public int knownSize() {
      final int knownSize = wrapped.knownSize();
      if (knownSize >= 0) {
        final int elementsSize = materializer.knownSize();
        if (elementsSize >= 0) {
          return knownSize + elementsSize - 1;
        }
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
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.canMaterializeElement(elementIndex)) {
        return materializer.materializeElement(elementIndex);
      }
      return wrapped.materializeElement(index - materializer.materializeSize() + 1);
    }

    @Override
    public boolean materializeEmpty() {
      final ListMaterializer<E> wrapped = this.wrapped;
      return wrapped.materializeEmpty() ||
          (wrapped.materializeSize() == 1 && numElements == 0 && materializer.materializeEmpty());
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
      return wrappedSize + materializer.materializeSize() - 1;
    }
  }

  private class ImmaterialState implements ListMaterializer<E> {

    private final Function<? super E, ? extends ListMaterializer<E>> mapper;
    private final AtomicInteger modCount = new AtomicInteger();
    private final Predicate<? super E> predicate;
    private final ListMaterializer<E> wrapped;

    private boolean found;
    private int pos;

    private ImmaterialState(@NotNull final ListMaterializer<E> wrapped,
        @NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends ListMaterializer<E>> mapper) {
      this.wrapped = wrapped;
      this.predicate = predicate;
      this.mapper = mapper;
    }

    @Override
    public boolean canMaterializeElement(final int index) {
      if (index < 0) {
        return false;
      }
      final int numElements = materializeUntil(index);
      if (index < numElements) {
        return wrapped.canMaterializeElement(index);
      }
      final int elementIndex = index - numElements;
      final ListMaterializer<E> materializer = materialized();
      return materializer.canMaterializeElement(elementIndex) ||
          wrapped.canMaterializeElement(index - materializer.materializeSize() + 1);
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
        throw new IndexOutOfBoundsException(Integer.toString(index));
      }
      final int numElements = materializeUntil(index);
      if (index < numElements) {
        return wrapped.materializeElement(index);
      }
      final int elementIndex = index - numElements;
      final ListMaterializer<E> materializer = materialized();
      if (materializer.canMaterializeElement(elementIndex)) {
        return materializer.materializeElement(elementIndex);
      }
      return wrapped.materializeElement(index - materializer.materializeSize() + 1);
    }

    @Override
    public boolean materializeEmpty() {
      final ListMaterializer<E> wrapped = this.wrapped;
      return wrapped.materializeEmpty() ||
          (wrapped.materializeSize() == 1 && materializeUntil(0) == 0
              && materialized().materializeEmpty());
    }

    @Override
    public @NotNull Iterator<E> materializeIterator() {
      return new ListMaterializerIterator<E>(this);
    }

    @Override
    public int materializeSize() {
      final ListMaterializer<E> wrapped = this.wrapped;
      final int wrappedSize = wrapped.materializeSize();
      if (wrappedSize <= materializeUntil(wrappedSize)) {
        state = wrapped;
        return wrappedSize;
      }
      final ListMaterializer<E> materializer = materialized();
      return wrappedSize + materializer.materializeSize() - 1;
    }

    private @NotNull ListMaterializer<E> materialized() {
      try {
        final int numElements = materializeUntil(Integer.MAX_VALUE);
        final AtomicInteger modCount = this.modCount;
        final int expectedCount = modCount.getAndIncrement() + 1;
        final ListMaterializer<E> wrapped = this.wrapped;
        if (wrapped.canMaterializeElement(numElements)) {
          final ListMaterializer<E> materializer = mapper.apply(
              wrapped.materializeElement(numElements));
          if (expectedCount != modCount.get()) {
            throw new ConcurrentModificationException();
          }
          state = new MaterialState<E>(wrapped, materializer, numElements);
          return materializer;
        } else {
          state = wrapped;
          return wrapped;
        }
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    private int materializeUntil(final int index) {
      if (found) {
        return pos;
      }
      final AtomicInteger modCount = this.modCount;
      final int expectedCount = modCount.getAndIncrement() + 1;
      try {
        int i = pos;
        final ListMaterializer<E> wrapped = this.wrapped;
        final Predicate<? super E> predicate = this.predicate;
        while (i <= index && wrapped.canMaterializeElement(i)) {
          if (predicate.test(wrapped.materializeElement(i))) {
            if (expectedCount != modCount.get()) {
              throw new ConcurrentModificationException();
            }
            found = true;
            pos = i;
            return i;
          }
          ++i;
        }
        if (expectedCount != modCount.get()) {
          throw new ConcurrentModificationException();
        }
        pos = i;
        return index + 1;
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }
}