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
import sparx.util.IndexOverflowException;
import sparx.util.SizeOverflowException;
import sparx.util.UncheckedException;
import sparx.util.function.IndexedFunction;
import sparx.util.function.IndexedPredicate;

public class FlatMapLastWhereListMaterializer<E> implements ListMaterializer<E> {

  private volatile ListMaterializer<E> state;

  public FlatMapLastWhereListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      @NotNull final IndexedPredicate<? super E> predicate,
      @NotNull final IndexedFunction<? super E, ? extends ListMaterializer<E>> mapper) {
    state = new ImmaterialState(wrapped, predicate, mapper);
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
  public E materializeElement(final int index) {
    return state.materializeElement(index);
  }

  @Override
  public int materializeElements() {
    return state.materializeElements();
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

    private final ListMaterializer<E> materializer;
    private final int numElements;
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
      final long wrappedIndex = (long) index - materializer.materializeSize() + 1;
      return materializer.canMaterializeElement(elementIndex) || (wrappedIndex < Integer.MAX_VALUE
          && wrapped.canMaterializeElement((int) wrappedIndex));
    }

    @Override
    public int knownSize() {
      final int knownSize = wrapped.knownSize();
      if (knownSize >= 0) {
        final int elementsSize = materializer.knownSize();
        if (elementsSize >= 0) {
          return SizeOverflowException.safeCast((long) knownSize + elementsSize - 1);
        }
      }
      return -1;
    }

    @Override
    public boolean materializeContains(final Object element) {
      final int numElements = this.numElements;
      final ListMaterializer<E> wrapped = this.wrapped;
      final ListMaterializer<E> materializer = this.materializer;
      int i = 0;
      if (element == null) {
        while (wrapped.canMaterializeElement(i)) {
          if (i != numElements) {
            if (wrapped.materializeElement(i) == null) {
              return true;
            }
          } else {
            if (materializer.materializeContains(null)) {
              return true;
            }
          }
          ++i;
        }
      } else {
        while (wrapped.canMaterializeElement(i)) {
          if (i != numElements) {
            if (element.equals(wrapped.materializeElement(i))) {
              return true;
            }
          } else {
            if (materializer.materializeContains(element)) {
              return true;
            }
          }
          ++i;
        }
      }
      return false;
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
      return wrapped.materializeElement(
          IndexOverflowException.safeCast((long) index - materializer.materializeSize() + 1));
    }

    @Override
    public int materializeElements() {
      final long size = wrapped.materializeElements();
      if (numElements < size) {
        return SizeOverflowException.safeCast(size + materializer.materializeElements() - 1);
      }
      return (int) size;
    }

    @Override
    public boolean materializeEmpty() {
      final ListMaterializer<E> wrapped = this.wrapped;
      return wrapped.materializeEmpty() || (wrapped.materializeSize() == 1 && numElements == 0
          && materializer.materializeEmpty());
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
      final long size = (long) wrappedSize + materializer.materializeSize() - 1;
      return SizeOverflowException.safeCast(size);
    }
  }

  private class ImmaterialState implements ListMaterializer<E> {

    private final IndexedFunction<? super E, ? extends ListMaterializer<E>> mapper;
    private final AtomicInteger modCount = new AtomicInteger();
    private final IndexedPredicate<? super E> predicate;
    private final ListMaterializer<E> wrapped;

    private boolean found;
    private int pos = Integer.MIN_VALUE;

    private ImmaterialState(@NotNull final ListMaterializer<E> wrapped,
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends ListMaterializer<E>> mapper) {
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
      if (index < numElements || materializeUntil(0) < 0) {
        return wrapped.canMaterializeElement(index);
      }
      final int elementIndex = index - materializeUntil(0);
      final ListMaterializer<E> materializer = materialized();
      final long wrappedIndex = (long) index - materializer.materializeSize() + 1;
      return materializer.canMaterializeElement(elementIndex) || (wrappedIndex < Integer.MAX_VALUE
          && wrapped.canMaterializeElement((int) wrappedIndex));
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
    public boolean materializeContains(final Object element) {
      final ListMaterializer<E> wrapped = this.wrapped;
      int i = 0;
      if (element == null) {
        while (wrapped.canMaterializeElement(i)) {
          final int numElements = materializeUntil(i);
          if (i != numElements) {
            if (wrapped.materializeElement(i) == null) {
              return true;
            }
          } else {
            if (materialized().materializeContains(null)) {
              return true;
            }
          }
          ++i;
        }
      } else {
        while (wrapped.canMaterializeElement(i)) {
          final int numElements = materializeUntil(i);
          if (i != numElements) {
            if (element.equals(wrapped.materializeElement(i))) {
              return true;
            }
          } else {
            if (materialized().materializeContains(element)) {
              return true;
            }
          }
          ++i;
        }
      }
      return false;
    }

    @Override
    public E materializeElement(final int index) {
      if (index < 0) {
        throw new IndexOutOfBoundsException(Integer.toString(index));
      }
      final int numElements = materializeUntil(index);
      if (index < numElements || materializeUntil(0) < 0) {
        return wrapped.materializeElement(index);
      }
      final int elementIndex = index - materializeUntil(0);
      final ListMaterializer<E> materializer = materialized();
      if (materializer.canMaterializeElement(elementIndex)) {
        return materializer.materializeElement(elementIndex);
      }
      return wrapped.materializeElement(
          IndexOverflowException.safeCast((long) index - materializer.materializeSize() + 1));
    }

    @Override
    public int materializeElements() {
      final ListMaterializer<E> wrapped = this.wrapped;
      final long size = wrapped.materializeElements();
      final ListMaterializer<E> materializer = materialized();
      if (materializer != wrapped) {
        return SizeOverflowException.safeCast(size + materializer.materializeElements() - 1);
      }
      return (int) size;
    }

    @Override
    public boolean materializeEmpty() {
      final ListMaterializer<E> wrapped = this.wrapped;
      return wrapped.materializeEmpty() || (wrapped.materializeSize() == 1
          && materializeUntil(0) == 0 && materialized().materializeEmpty());
    }

    @Override
    public @NotNull Iterator<E> materializeIterator() {
      return new ListMaterializerIterator<E>(this);
    }

    @Override
    public int materializeSize() {
      if (materializeUntil(0) < 0) {
        return wrapped.materializeSize();
      }
      final long size = (long) wrapped.materializeSize() + materialized().materializeSize() - 1;
      return SizeOverflowException.safeCast(size);
    }

    private @NotNull ListMaterializer<E> materialized() {
      try {
        final int index = materializeUntil(0);
        final AtomicInteger modCount = this.modCount;
        final int expectedCount = modCount.incrementAndGet();
        final ListMaterializer<E> wrapped = this.wrapped;
        if (wrapped.canMaterializeElement(index)) {
          final ListMaterializer<E> materializer = mapper.apply(index,
              wrapped.materializeElement(index));
          if (expectedCount != modCount.get()) {
            throw new ConcurrentModificationException();
          }
          state = new MaterialState<E>(wrapped, materializer, index);
          return materializer;
        } else {
          state = wrapped;
          return wrapped;
        }
      } catch (final Exception e) {
        state = new FailedListMaterializer<E>(e);
        throw UncheckedException.throwUnchecked(e);
      }
    }

    private int materializeUntil(final int index) {
      if (found || pos == -1) {
        return pos;
      }
      final AtomicInteger modCount = this.modCount;
      final int expectedCount = modCount.incrementAndGet();
      final ListMaterializer<E> wrapped = this.wrapped;
      if (pos == Integer.MIN_VALUE) {
        pos = wrapped.materializeSize() - 1;
      }
      try {
        final IndexedPredicate<? super E> predicate = this.predicate;
        int i = pos;
        while (i >= index) {
          if (predicate.test(i, wrapped.materializeElement(i))) {
            if (expectedCount != modCount.get()) {
              throw new ConcurrentModificationException();
            }
            found = true;
            return pos = i;
          }
          --i;
        }
        if (expectedCount != modCount.get()) {
          throw new ConcurrentModificationException();
        }
        pos = i;
        return -1;
      } catch (final Exception e) {
        state = new FailedListMaterializer<E>(e);
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }
}
