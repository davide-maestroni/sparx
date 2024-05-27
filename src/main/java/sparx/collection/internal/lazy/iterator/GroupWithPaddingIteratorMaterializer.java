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
package sparx.collection.internal.lazy.iterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;
import sparx.util.Require;
import sparx.util.SizeOverflowException;
import sparx.util.UncheckedException;
import sparx.util.function.Function;

public class GroupWithPaddingIteratorMaterializer<E, I extends Iterator<E>> implements
    IteratorMaterializer<I> {

  private volatile IteratorMaterializer<I> state;

  public GroupWithPaddingIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      final int size, final E padding,
      @NotNull final Function<? super List<E>, ? extends I> mapper) {
    state = new ImmaterialState(Require.notNull(wrapped, "wrapped"), Require.positive(size, "size"),
        padding, Require.notNull(mapper, "mapper"));
  }

  @Override
  public int knownSize() {
    return state.knownSize();
  }

  @Override
  public boolean materializeHasNext() {
    return state.materializeHasNext();
  }

  @Override
  public I materializeNext() {
    return state.materializeNext();
  }

  @Override
  public int materializeSkip(final int count) {
    return state.materializeSkip(count);
  }

  private class ImmaterialState implements IteratorMaterializer<I> {

    private final Function<? super List<E>, ? extends I> mapper;
    private final int size;
    private final E padding;
    private final IteratorMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final IteratorMaterializer<E> wrapped, final int size,
        final E padding, @NotNull final Function<? super List<E>, ? extends I> mapper) {
      this.wrapped = wrapped;
      this.size = size;
      this.padding = padding;
      this.mapper = mapper;
    }

    @Override
    public int knownSize() {
      final int knownSize = wrapped.knownSize();
      if (knownSize > 0) {
        final long size = this.size;
        if (knownSize < size) {
          return 1;
        }
        return SizeOverflowException.safeCast((knownSize + (size >> 1)) / size);
      }
      return -1;
    }

    @Override
    public boolean materializeHasNext() {
      return wrapped.materializeHasNext();
    }

    @Override
    public I materializeNext() {
      final IteratorMaterializer<E> wrapped = this.wrapped;
      if (!wrapped.materializeHasNext()) {
        throw new NoSuchElementException();
      }
      final int size = this.size;
      final ArrayList<E> chunk = new ArrayList<E>(size);
      do {
        chunk.add(wrapped.materializeNext());
      } while (chunk.size() < size && wrapped.materializeHasNext());
      final E padding = this.padding;
      while (chunk.size() < size) {
        chunk.add(padding);
      }
      if (!wrapped.materializeHasNext()) {
        state = EmptyIteratorMaterializer.instance();
      }
      try {
        return mapper.apply(chunk);
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public int materializeSkip(final int count) {
      if (count > 0) {
        final long size = this.size;
        final int skipped = wrapped.materializeSkip(
            (int) Math.min(Integer.MAX_VALUE, size * count));
        return (int) ((skipped + (size >> 1)) / size);
      }
      return 0;
    }
  }
}
