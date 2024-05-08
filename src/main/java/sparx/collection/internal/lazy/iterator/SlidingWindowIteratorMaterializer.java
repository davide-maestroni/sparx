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

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;
import sparx.util.DequeueList;
import sparx.util.Require;
import sparx.util.SizeOverflowException;
import sparx.util.UncheckedException;
import sparx.util.function.Function;

public class SlidingWindowIteratorMaterializer<E, I extends Iterator<E>> implements
    IteratorMaterializer<I> {

  private final IteratorMaterializer<I> state;

  public SlidingWindowIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      final int maxSize, final int step,
      @NotNull final Function<? super List<E>, ? extends I> mapper) {
    state = new ImmaterialState(Require.notNull(wrapped, "wrapped"),
        Require.positive(maxSize, "maxSize"), Require.positive(step, "step"),
        Require.notNull(mapper, "mapper"));
  }

  public SlidingWindowIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      final int size, final int step, final E padding,
      @NotNull final Function<? super List<E>, ? extends I> mapper) {
    state = new ImmaterialPaddingState(Require.notNull(wrapped, "wrapped"),
        Require.positive(size, "size"), Require.positive(step, "step"), padding,
        Require.notNull(mapper, "mapper"));
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

  private class ImmaterialPaddingState implements IteratorMaterializer<I> {

    private final DequeueList<E> elements;
    private final Function<? super List<E>, ? extends I> mapper;
    private final int size;
    private final int step;
    private final E padding;
    private final IteratorMaterializer<E> wrapped;

    private boolean hasNext;

    private ImmaterialPaddingState(@NotNull final IteratorMaterializer<E> wrapped, final int size,
        final int step, final E padding,
        @NotNull final Function<? super List<E>, ? extends I> mapper) {
      this.wrapped = wrapped;
      this.size = size;
      this.step = step;
      this.padding = padding;
      this.mapper = mapper;
      elements = new DequeueList<E>(size);
    }

    @Override
    public int knownSize() {
      final int knownSize = wrapped.knownSize();
      if (knownSize > 0) {
        final long step = this.step;
        if (knownSize < step) {
          return 1;
        }
        return SizeOverflowException.safeCast((knownSize + (step >> 1)) / step);
      }
      return -1;
    }

    @Override
    public boolean materializeHasNext() {
      if (hasNext) {
        return true;
      }
      return hasNext = advance();
    }

    @Override
    public I materializeNext() {
      if (!materializeHasNext()) {
        throw new NoSuchElementException();
      }
      hasNext = false;
      final int size = this.size;
      final E padding = this.padding;
      try {
        final DequeueList<E> clone = elements.clone();
        while (clone.size() < size) {
          clone.add(padding);
        }
        return mapper.apply(clone);
      } catch (final Exception e) {
        throw UncheckedException.toUnchecked(e);
      }
    }

    @Override
    public int materializeSkip(final int count) {
      if (count > 0) {
        final IteratorMaterializer<E> wrapped = this.wrapped;
        final DequeueList<E> elements = this.elements;
        int skipped = 0;
        while (skipped < count) {
          if (elements.isEmpty()) {
            if (!wrapped.materializeHasNext()) {
              return skipped;
            }
            for (int i = 0; i < size; ++i) {
              if (!wrapped.materializeHasNext()) {
                break;
              }
              elements.add(wrapped.materializeNext());
            }
          } else {
            for (int i = 0; i < step; ++i) {
              if (wrapped.materializeHasNext()) {
                elements.add(wrapped.materializeNext());
              }
              if (elements.size() <= 1) {
                return skipped;
              }
              elements.removeFirst();
            }
          }
          ++skipped;
        }
        return skipped;
      }
      return 0;
    }

    private boolean advance() {
      final IteratorMaterializer<E> wrapped = this.wrapped;
      final DequeueList<E> elements = this.elements;
      if (elements.isEmpty()) {
        if (!wrapped.materializeHasNext()) {
          return false;
        }
        for (int i = 0; i < size; ++i) {
          elements.add(wrapped.materializeNext());
          if (!wrapped.materializeHasNext()) {
            break;
          }
        }
      } else {
        for (int i = 0; i < step; ++i) {
          if (wrapped.materializeHasNext()) {
            elements.add(wrapped.materializeNext());
          }
          if (elements.size() <= 1) {
            return false;
          }
          elements.removeFirst();
        }
      }
      return true;
    }
  }

  private class ImmaterialState implements IteratorMaterializer<I> {

    private final DequeueList<E> elements;
    private final Function<? super List<E>, ? extends I> mapper;
    private final int maxSize;
    private final int step;
    private final IteratorMaterializer<E> wrapped;

    private boolean hasNext;

    private ImmaterialState(@NotNull final IteratorMaterializer<E> wrapped, final int maxSize,
        final int step, @NotNull final Function<? super List<E>, ? extends I> mapper) {
      this.wrapped = wrapped;
      this.maxSize = maxSize;
      this.step = step;
      this.mapper = mapper;
      elements = new DequeueList<E>(maxSize);
    }

    @Override
    public int knownSize() {
      final int knownSize = wrapped.knownSize();
      if (knownSize > 0) {
        final long step = this.step;
        if (knownSize < step) {
          return 1;
        }
        return SizeOverflowException.safeCast((knownSize + (step >> 1)) / step);
      }
      return -1;
    }

    @Override
    public boolean materializeHasNext() {
      if (hasNext) {
        return true;
      }
      return hasNext = advance();
    }

    @Override
    public I materializeNext() {
      if (!materializeHasNext()) {
        throw new NoSuchElementException();
      }
      hasNext = false;
      try {
        return mapper.apply(elements.clone());
      } catch (final Exception e) {
        throw UncheckedException.toUnchecked(e);
      }
    }

    @Override
    public int materializeSkip(final int count) {
      if (count > 0) {
        final IteratorMaterializer<E> wrapped = this.wrapped;
        final DequeueList<E> elements = this.elements;
        int skipped = 0;
        while (skipped < count) {
          if (elements.isEmpty()) {
            if (!wrapped.materializeHasNext()) {
              return skipped;
            }
            for (int i = 0; i < maxSize; ++i) {
              if (!wrapped.materializeHasNext()) {
                break;
              }
              elements.add(wrapped.materializeNext());
            }
          } else {
            for (int i = 0; i < step; ++i) {
              if (wrapped.materializeHasNext()) {
                elements.add(wrapped.materializeNext());
              }
              if (elements.size() <= 1) {
                return skipped;
              }
              elements.removeFirst();
            }
          }
          ++skipped;
        }
        return skipped;
      }
      return 0;
    }

    private boolean advance() {
      final IteratorMaterializer<E> wrapped = this.wrapped;
      final DequeueList<E> elements = this.elements;
      if (elements.isEmpty()) {
        if (!wrapped.materializeHasNext()) {
          return false;
        }
        for (int i = 0; i < maxSize; ++i) {
          elements.add(wrapped.materializeNext());
          if (!wrapped.materializeHasNext()) {
            break;
          }
        }
      } else {
        for (int i = 0; i < step; ++i) {
          if (wrapped.materializeHasNext()) {
            elements.add(wrapped.materializeNext());
          }
          if (elements.size() <= 1) {
            return false;
          }
          elements.removeFirst();
        }
      }
      return true;
    }
  }
}
