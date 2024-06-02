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

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import sparx.util.Require;
import sparx.util.SizeOverflowException;
import sparx.util.UncheckedException;

public class GroupListMaterializer<E, L extends List<E>> implements ListMaterializer<L> {

  private volatile ListMaterializer<L> state;

  public GroupListMaterializer(@NotNull final ListMaterializer<E> wrapped, final int maxSize,
      @NotNull final Chunker<E, ? extends L> chunker) {
    state = new ImmaterialState(Require.notNull(wrapped, "wrapped"),
        Require.positive(maxSize, "maxSize"), Require.notNull(chunker, "chunker"));
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

  public interface Chunker<E, L extends List<E>> {

    @NotNull
    L getChunk(@NotNull ListMaterializer<E> materializer, int start, int end);
  }

  private class ImmaterialState extends AbstractListMaterializer<L> implements ListMaterializer<L> {

    private final Chunker<E, ? extends L> chunker;
    private final ArrayList<L> elements = new ArrayList<L>();
    private final int maxSize;
    private final AtomicInteger modCount = new AtomicInteger();
    private final ListMaterializer<E> wrapped;

    private int elementsCount;

    private ImmaterialState(@NotNull final ListMaterializer<E> wrapped, final int maxSize,
        @NotNull final Chunker<E, ? extends L> chunker) {
      this.wrapped = wrapped;
      this.maxSize = maxSize;
      this.chunker = chunker;
    }

    @Override
    public boolean canMaterializeElement(final int index) {
      if (index < 0) {
        return false;
      }
      final long maxSize = this.maxSize;
      final long wrappedIndex = index * maxSize;
      return wrappedIndex < Integer.MAX_VALUE && wrapped.canMaterializeElement((int) wrappedIndex);
    }

    @Override
    public int knownSize() {
      final int knownSize = wrapped.knownSize();
      if (knownSize > 0) {
        final long maxSize = this.maxSize;
        if (knownSize < maxSize) {
          return 1;
        }
        return SizeOverflowException.safeCast((knownSize + (maxSize >> 1)) / maxSize);
      }
      return -1;
    }

    @Override
    public L materializeElement(final int index) {
      if (index < 0) {
        throw new IndexOutOfBoundsException(Integer.toString(index));
      }
      final long maxSize = this.maxSize;
      final ListMaterializer<E> wrapped = this.wrapped;
      final long wrappedIndex = index * maxSize;
      if (wrappedIndex >= Integer.MAX_VALUE || !wrapped.canMaterializeElement((int) wrappedIndex)) {
        throw new IndexOutOfBoundsException(Integer.toString(index));
      }
      final ArrayList<L> elements = this.elements;
      if (elements.size() > index) {
        final L element = elements.get(index);
        if (element != null) {
          return element;
        }
      }
      final AtomicInteger modCount = this.modCount;
      final int expectedCount = modCount.incrementAndGet();
      final int endIndex = (int) Math.min(Integer.MAX_VALUE, wrappedIndex + maxSize);
      try {
        final L element = chunker.getChunk(wrapped, (int) wrappedIndex, endIndex);
        while (elements.size() <= index) {
          elements.add(null);
        }
        elements.set(index, element);
        if (expectedCount != modCount.get()) {
          throw new ConcurrentModificationException();
        }
        if (++elementsCount == (wrapped.knownSize() + maxSize - 1) / maxSize) {
          state = new ListToListMaterializer<L>(elements);
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
    public @NotNull Iterator<L> materializeIterator() {
      return new ListMaterializerIterator<L>(this);
    }

    @Override
    public int materializeSize() {
      final long maxSize = this.maxSize;
      return SizeOverflowException.safeCast((wrapped.materializeSize() + (maxSize >> 1)) / maxSize);
    }
  }
}
