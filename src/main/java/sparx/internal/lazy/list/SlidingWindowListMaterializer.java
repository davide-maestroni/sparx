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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import sparx.internal.util.ElementsCache;
import sparx.util.SizeOverflowException;
import sparx.util.UncheckedException;
import sparx.util.annotation.Positive;

public class SlidingWindowListMaterializer<E, L extends List<E>> implements ListMaterializer<L> {

  private volatile ListMaterializer<L> state;

  public SlidingWindowListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      @Positive final int maxSize, @Positive final int step,
      @NotNull final Splitter<E, ? extends L> splitter) {
    state = new ImmaterialState(wrapped, maxSize, step, splitter);
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
  public L materializeElement(final int index) {
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
  public @NotNull Iterator<L> materializeIterator() {
    return state.materializeIterator();
  }

  @Override
  public int materializeSize() {
    return state.materializeSize();
  }

  public interface Splitter<E, L extends List<E>> {

    @NotNull
    L getChunk(@NotNull ListMaterializer<E> materializer, int start, int end);
  }

  private class ImmaterialState extends AbstractListMaterializer<L> implements ListMaterializer<L> {

    private final Splitter<E, ? extends L> splitter;
    private final ElementsCache<L> elements;
    private final int maxSize;
    private final AtomicInteger modCount = new AtomicInteger();
    private final int step;
    private final ListMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final ListMaterializer<E> wrapped, final int maxSize,
        final int step, @NotNull final Splitter<E, ? extends L> splitter) {
      this.wrapped = wrapped;
      this.maxSize = maxSize;
      this.step = step;
      this.splitter = splitter;
      elements = new ElementsCache<L>(knownSize());
    }

    @Override
    public boolean canMaterializeElement(final int index) {
      if (index < 0) {
        return false;
      }
      final long step = this.step;
      final long wrappedIndex = index * step;
      return wrappedIndex < Integer.MAX_VALUE && wrapped.canMaterializeElement((int) wrappedIndex);
    }

    @Override
    public int knownSize() {
      final int knownSize = wrapped.knownSize();
      if (knownSize >= 0) {
        final long step = this.step;
        if (knownSize < step) {
          return 1;
        }
        return SizeOverflowException.safeCast((knownSize + step - 1) / step);
      }
      return -1;
    }

    @Override
    public L materializeElement(final int index) {
      if (index < 0) {
        throw new IndexOutOfBoundsException(Integer.toString(index));
      }
      final ElementsCache<L> elements = this.elements;
      if (elements.has(index)) {
        return elements.get(index);
      }
      final long step = this.step;
      final ListMaterializer<E> wrapped = this.wrapped;
      final long wrappedIndex = index * step;
      if (wrappedIndex >= Integer.MAX_VALUE || !wrapped.canMaterializeElement((int) wrappedIndex)) {
        throw new IndexOutOfBoundsException(Integer.toString(index));
      }
      final AtomicInteger modCount = this.modCount;
      final int expectedCount = modCount.incrementAndGet();
      final int wrappedSize = wrapped.materializeSize();
      final int endIndex = (int) Math.min(wrappedSize, wrappedIndex + maxSize);
      try {
        final L element = splitter.getChunk(wrapped, (int) wrappedIndex, endIndex);
        elements.set(index, element);
        if (expectedCount != modCount.get()) {
          throw new ConcurrentModificationException();
        }
        if (elements.count() == (wrappedSize + step - 1) / step) {
          state = new ListToListMaterializer<L>(elements.toList());
        }
        return element;
      } catch (final Exception e) {
        state = new FailedListMaterializer<L>(e);
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public boolean materializeEmpty() {
      return wrapped.materializeEmpty();
    }

    @Override
    public @NotNull Iterator<L> materializeIterator() {
      return new ListMaterializerIterator<L>(this);
    }

    @Override
    public int materializeSize() {
      final long step = this.step;
      final int wrappedSize = wrapped.materializeSize();
      final ElementsCache<L> elements = this.elements;
      final long size = (wrappedSize + step - 1) / step;
      if (elements.count() == size) {
        state = new ListToListMaterializer<L>(elements.toList());
      }
      return SizeOverflowException.safeCast(size);
    }
  }
}
