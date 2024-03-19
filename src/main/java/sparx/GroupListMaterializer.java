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

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import sparx.collection.ListMaterializer;
import sparx.util.Require;
import sparx.util.UncheckedException;
import sparx.util.function.Function;

class GroupListMaterializer<E, L extends List<E>> implements ListMaterializer<L> {

  private volatile ListMaterializer<L> state;

  GroupListMaterializer(@NotNull final ListMaterializer<E> wrapper, final int maxSize,
      @NotNull final Function<? super List<E>, ? extends L> mapper) {
    state = new ImmaterialState(Require.notNull(wrapper, "wrapper"),
        Require.positive(maxSize, "maxSize"),
        Require.notNull(mapper, "mapper"));
  }

  GroupListMaterializer(@NotNull final ListMaterializer<E> wrapper, final int maxSize,
      final E filler, @NotNull final Function<? super List<E>, ? extends L> mapper) {
    state = new ImmaterialFillerState(Require.notNull(wrapper, "wrapper"),
        Require.positive(maxSize, "maxSize"), filler,
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
  public L materializeElement(final int index) {
    return state.materializeElement(index);
  }

  @Override
  public boolean materializeEmpty() {
    return state.materializeEmpty();
  }

  @Override
  public @NotNull Iterator<L> materializeIterator() {
    return state.materializeIterator();
  }

  @Override
  public int materializeSize() {
    return state.materializeSize();
  }

  private static class ExceptionState<E> implements ListMaterializer<E> {

    private final Exception ex;

    private ExceptionState(@NotNull final Exception ex) {
      this.ex = ex;
    }

    @Override
    public boolean canMaterializeElement(final int index) {
      throw UncheckedException.throwUnchecked(ex);
    }

    @Override
    public int knownSize() {
      return 1;
    }

    @Override
    public E materializeElement(final int index) {
      throw UncheckedException.throwUnchecked(ex);
    }

    @Override
    public boolean materializeEmpty() {
      throw UncheckedException.throwUnchecked(ex);
    }

    @Override
    public @NotNull Iterator<E> materializeIterator() {
      throw UncheckedException.throwUnchecked(ex);
    }

    @Override
    public int materializeSize() {
      throw UncheckedException.throwUnchecked(ex);
    }
  }

  private class ImmaterialFillerState implements ListMaterializer<L> {

    private final ArrayList<L> elements = new ArrayList<L>();
    private final E filler;
    private final Function<? super List<E>, ? extends L> mapper;
    private final int maxSize;
    private final AtomicInteger modCount = new AtomicInteger();
    private final ListMaterializer<E> wrapped;

    private int elementsCount;

    private ImmaterialFillerState(@NotNull final ListMaterializer<E> wrapped,
        final int maxSize,
        final E filler, @NotNull final Function<? super List<E>, ? extends L> mapper) {
      this.wrapped = wrapped;
      this.maxSize = maxSize;
      this.filler = filler;
      this.mapper = mapper;
    }

    @Override
    public boolean canMaterializeElement(final int index) {
      if (index < 0) {
        return false;
      }
      return wrapped.canMaterializeElement(index * maxSize);
    }

    @Override
    public int knownSize() {
      final int wrappedSize = wrapped.knownSize();
      if (wrappedSize > 0) {
        final int maxSize = this.maxSize;
        return (wrappedSize + (maxSize >> 1)) / maxSize;
      }
      return -1;
    }

    @Override
    public L materializeElement(final int index) {
      final int maxSize = this.maxSize;
      final ListMaterializer<E> wrapped = this.wrapped;
      if (index < 0 || !wrapped.canMaterializeElement(index * maxSize)) {
        throw new IndexOutOfBoundsException(String.valueOf(index));
      }
      final ArrayList<L> elements = this.elements;
      if (elements.size() > index) {
        final L element = elements.get(index);
        if (element != null) {
          return element;
        }
      }
      final AtomicInteger modCount = this.modCount;
      final int expectedCount = modCount.getAndIncrement() + 1;
      try {
        final int endIndex = (index + 1) * maxSize;
        final ArrayList<E> chunk = new ArrayList<E>();
        for (int i = index * maxSize; i < endIndex && wrapped.canMaterializeElement(i); ++i) {
          chunk.add(wrapped.materializeElement(i));
        }
        while (chunk.size() < maxSize) {
          chunk.add(filler);
        }
        if (expectedCount != modCount.get()) {
          throw new ConcurrentModificationException();
        }
        final L element = mapper.apply(chunk);
        while (elements.size() <= index) {
          elements.add(null);
        }
        elements.set(index, element);
        if (++elementsCount == (wrapped.knownSize() + maxSize - 1) / maxSize) {
          if (expectedCount != modCount.get()) {
            throw new ConcurrentModificationException();
          }
          state = new ListToListMaterializer<L>(elements);
        }
        return element;
      } catch (final Exception e) {
        state = new ExceptionState<L>(e);
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public boolean materializeEmpty() {
      return wrapped.materializeEmpty();
    }

    @Override
    public @NotNull Iterator<L> materializeIterator() {
      return new CollectionMaterializerIterator<L>(this);
    }

    @Override
    public int materializeSize() {
      final int maxSize = this.maxSize;
      return (wrapped.materializeSize() + (maxSize >> 1)) / maxSize;
    }
  }

  private class ImmaterialState implements ListMaterializer<L> {

    private final ArrayList<L> elements = new ArrayList<L>();
    private final Function<? super List<E>, ? extends L> mapper;
    private final int maxSize;
    private final AtomicInteger modCount = new AtomicInteger();
    private final ListMaterializer<E> wrapped;

    private int elementsCount;

    private ImmaterialState(@NotNull final ListMaterializer<E> wrapped, final int maxSize,
        @NotNull final Function<? super List<E>, ? extends L> mapper) {
      this.wrapped = wrapped;
      this.maxSize = maxSize;
      this.mapper = mapper;
    }

    @Override
    public boolean canMaterializeElement(final int index) {
      if (index < 0) {
        return false;
      }
      return wrapped.canMaterializeElement(index * maxSize);
    }

    @Override
    public int knownSize() {
      final int wrappedSize = wrapped.knownSize();
      if (wrappedSize > 0) {
        final int maxSize = this.maxSize;
        return (wrappedSize + (maxSize >> 1)) / maxSize;
      }
      return -1;
    }

    @Override
    public L materializeElement(final int index) {
      final int maxSize = this.maxSize;
      final ListMaterializer<E> wrapped = this.wrapped;
      if (index < 0 || !wrapped.canMaterializeElement(index * maxSize)) {
        throw new IndexOutOfBoundsException(String.valueOf(index));
      }
      final ArrayList<L> elements = this.elements;
      if (elements.size() > index) {
        final L element = elements.get(index);
        if (element != null) {
          return element;
        }
      }
      final AtomicInteger modCount = this.modCount;
      final int expectedCount = modCount.getAndIncrement() + 1;
      try {
        final int endIndex = (index + 1) * maxSize;
        final ArrayList<E> chunk = new ArrayList<E>();
        for (int i = index * maxSize; i < endIndex && wrapped.canMaterializeElement(i); ++i) {
          chunk.add(wrapped.materializeElement(i));
        }
        if (expectedCount != modCount.get()) {
          throw new ConcurrentModificationException();
        }
        final L element = mapper.apply(chunk);
        while (elements.size() <= index) {
          elements.add(null);
        }
        elements.set(index, element);
        if (++elementsCount == (wrapped.knownSize() + maxSize - 1) / maxSize) {
          state = new ListToListMaterializer<L>(elements);
        }
        return element;
      } catch (final Exception e) {
        state = new ExceptionState<L>(e);
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public boolean materializeEmpty() {
      return wrapped.materializeEmpty();
    }

    @Override
    public @NotNull Iterator<L> materializeIterator() {
      return new CollectionMaterializerIterator<L>(this);
    }

    @Override
    public int materializeSize() {
      final int maxSize = this.maxSize;
      return (wrapped.materializeSize() + (maxSize >> 1)) / maxSize;
    }
  }
}
