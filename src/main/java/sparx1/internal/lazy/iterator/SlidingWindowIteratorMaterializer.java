/*
 * Copyright 2026 Davide Maestroni
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
package sparx1.internal.lazy.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;
import sparx1.internal.lazy.IteratorMaterializer;
import sparx1.util.DequeArrayList;
import sparx1.util.SizeOverflowException;
import sparx1.util.UncheckedException;
import sparx1.util.annotation.NotNull;
import sparx1.util.annotation.Positive;
import sparx1.util.function.Function;

public class SlidingWindowIteratorMaterializer<E, I extends Iterator<E>> extends
    StatefulIteratorMaterializer<I> {

  public SlidingWindowIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      final @Positive int maxSize, final @Positive int step,
      final @NotNull Function<? super DequeArrayList<E>, ? extends I> mapper) {
    setState(new InitialState(wrapped, maxSize, 0, step, null, mapper));
  }

  public SlidingWindowIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      final @Positive int size, final @Positive int step, final E padding,
      final @NotNull Function<? super DequeArrayList<E>, ? extends I> mapper) {
    setState(new InitialState(wrapped, size, size, step, padding, mapper));
  }

  private class InitialState implements IteratorMaterializer<I> {

    private final DequeArrayList<E> elements;
    private final Function<? super DequeArrayList<E>, ? extends I> mapper;
    private final int maxSize;
    private final int size;
    private final int skip;
    private final int step;
    private final E padding;
    private final IteratorMaterializer<E> wrapped;

    private boolean hasNext;

    private InitialState(final @NotNull IteratorMaterializer<E> wrapped, final int maxSize,
        final int size, final int step, final E padding,
        final @NotNull Function<? super DequeArrayList<E>, ? extends I> mapper) {
      this.wrapped = wrapped;
      this.maxSize = maxSize;
      this.size = size;
      this.step = step;
      this.padding = padding;
      this.mapper = mapper;
      skip = Math.max(0, step - maxSize);
      elements = new DequeArrayList<E>(maxSize);
    }

    @Override
    public int currentKnownSize() {
      final int knownSize = wrapped.currentKnownSize();
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
    public boolean isSizeKnown() {
      return wrapped.isSizeKnown();
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
        final DequeArrayList<E> clone = elements.clone();
        while (clone.size() < size) {
          clone.add(padding);
        }
        return mapper.apply(clone);
      } catch (final Exception e) {
        throw UncheckedException.toUnchecked(e);
      }
    }

    @Override
    public int materializeSkip(final @Positive int count) {
      final IteratorMaterializer<E> wrapped = this.wrapped;
      final DequeArrayList<E> elements = this.elements;
      final int maxSize = this.maxSize;
      final int skip = this.skip;
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
        } else if (skip > 0) {
          elements.clear();
          wrapped.materializeSkip(skip);
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
          final int step = this.step;
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

    private boolean advance() {
      final IteratorMaterializer<E> wrapped = this.wrapped;
      final DequeArrayList<E> elements = this.elements;
      final int maxSize = this.maxSize;
      final int skip = this.skip;
      if (elements.isEmpty()) {
        if (!wrapped.materializeHasNext()) {
          setEmptyState();
          return false;
        }
        for (int i = 0; i < maxSize; ++i) {
          elements.add(wrapped.materializeNext());
          if (!wrapped.materializeHasNext()) {
            break;
          }
        }
      } else if (skip > 0) {
        elements.clear();
        wrapped.materializeSkip(skip);
        if (!wrapped.materializeHasNext()) {
          setEmptyState();
          return false;
        }
        for (int i = 0; i < maxSize; ++i) {
          elements.add(wrapped.materializeNext());
          if (!wrapped.materializeHasNext()) {
            break;
          }
        }
      } else {
        final int step = this.step;
        for (int i = 0; i < step; ++i) {
          if (wrapped.materializeHasNext()) {
            elements.add(wrapped.materializeNext());
          }
          if (elements.size() <= 1) {
            setEmptyState();
            return false;
          }
          elements.removeFirst();
        }
      }
      return true;
    }
  }
}
