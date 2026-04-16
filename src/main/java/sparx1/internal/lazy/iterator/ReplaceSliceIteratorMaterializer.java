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

import java.util.NoSuchElementException;
import sparx1.internal.lazy.IteratorMaterializer;
import sparx1.util.DequeArrayList;
import sparx1.util.SizeOverflowException;
import sparx1.util.annotation.NotNull;
import sparx1.util.annotation.Positive;

public class ReplaceSliceIteratorMaterializer<E> extends StatefulIteratorMaterializer<E> {

  public ReplaceSliceIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      final int start, final int end, final @NotNull IteratorMaterializer<E> elementsMaterializer) {
    final int knownSize = wrapped.currentKnownSize();
    if (knownSize >= 0) {
      int materializedStart = start;
      if (materializedStart < 0) {
        materializedStart = Math.max(0, knownSize + materializedStart);
      } else {
        materializedStart = Math.min(knownSize, materializedStart);
      }
      int materializedEnd = end;
      if (materializedEnd < 0) {
        materializedEnd = Math.max(0, knownSize + materializedEnd);
      } else {
        materializedEnd = Math.min(knownSize, materializedEnd);
      }
      final int materializedLength = Math.max(0, materializedEnd - materializedStart);
      setState(
          new MaterialState(wrapped, materializedStart, materializedLength, elementsMaterializer));
    } else if (start >= 0) {
      if (end >= 0) {
        setState(new MaterialState(wrapped, start, Math.max(0, end - start), elementsMaterializer));
      } else {
        setState(new PendingState(wrapped, start, end, elementsMaterializer));
      }
    } else {
      setState(new InitialState(wrapped, start, end, elementsMaterializer));
    }
  }

  private class InitialState implements IteratorMaterializer<E> {

    private final IteratorMaterializer<E> elementsMaterializer;
    private final int end;
    private final int start;
    private final IteratorMaterializer<E> wrapped;

    private InitialState(final @NotNull IteratorMaterializer<E> wrapped, final int start,
        final int end, final @NotNull IteratorMaterializer<E> elementsMaterializer) {
      this.wrapped = wrapped;
      this.start = start;
      this.end = end;
      this.elementsMaterializer = elementsMaterializer;
    }

    @Override
    public int currentKnownSize() {
      return -1;
    }

    @Override
    public boolean isSizeKnown() {
      return false;
    }

    @Override
    public boolean materializeHasNext() {
      final IteratorMaterializer<E> wrapped = this.wrapped;
      if (wrapped.materializeHasNext()) {
        final DequeArrayList<E> elements = new DequeArrayList<E>(true);
        do {
          elements.add(wrapped.materializeNext());
        } while (wrapped.materializeHasNext());
        final int wrappedSize = elements.size();
        int materializedStart = start;
        if (materializedStart < 0) {
          materializedStart = Math.max(0, wrappedSize + materializedStart);
        } else {
          materializedStart = Math.min(wrappedSize, materializedStart);
        }
        int materializedEnd = end;
        if (materializedEnd < 0) {
          materializedEnd = Math.max(0, wrappedSize + materializedEnd);
        } else {
          materializedEnd = Math.min(wrappedSize, materializedEnd);
        }
        final int materializedLength = Math.max(0, materializedEnd - materializedStart);
        return setState(
            new MaterialState(new DequeToIteratorMaterializer<E>(elements), materializedStart,
                materializedLength, elementsMaterializer)).materializeHasNext();
      }
      setEmptyState();
      return false;
    }

    @Override
    public E materializeNext() {
      if (!materializeHasNext()) {
        throw new NoSuchElementException();
      }
      return getState().materializeNext();
    }

    @Override
    public int materializeSkip(final @Positive int count) {
      materializeHasNext();
      return getState().materializeSkip(count);
    }
  }

  private class MaterialState extends AbstractStateIteratorMaterializer {

    private final IteratorMaterializer<E> elementsMaterializer;
    private final int length;
    private final int start;
    private final IteratorMaterializer<E> wrapped;

    private int pos;

    private MaterialState(final @NotNull IteratorMaterializer<E> wrapped, final int start,
        final int length, final @NotNull IteratorMaterializer<E> elementsMaterializer) {
      this.wrapped = wrapped;
      this.start = start;
      this.length = length;
      this.elementsMaterializer = elementsMaterializer;
    }

    @Override
    public int currentKnownSize() {
      final int knownSize = wrapped.currentKnownSize();
      if (knownSize >= 0) {
        if (knownSize == 0) {
          return 0;
        }
        final long elementsKnownSize = elementsMaterializer.currentKnownSize();
        if (elementsKnownSize >= 0) {
          return SizeOverflowException.safeCast(
              Math.max(start - pos, knownSize - length) + elementsKnownSize);
        }
      }
      return -1;
    }

    @Override
    public boolean isSizeKnown() {
      final int knownSize = wrapped.currentKnownSize();
      if (knownSize >= 0) {
        return knownSize == 0 || elementsMaterializer.isSizeKnown();
      }
      return false;
    }

    @Override
    public boolean materializeHasNext() {
      final IteratorMaterializer<E> wrapped = this.wrapped;
      if (pos == start) {
        final IteratorMaterializer<E> elementsMaterializer = this.elementsMaterializer;
        if (elementsMaterializer.materializeHasNext()) {
          if (length > 0) {
            wrapped.materializeSkip(length);
          }
          return setState(new AppendAllIteratorMaterializer<E>(elementsMaterializer,
              wrapped)).materializeHasNext();
        }
        final IteratorMaterializer<E> state = setState(wrapped);
        if (length > 0) {
          state.materializeSkip(length);
        }
      }
      if (wrapped.materializeHasNext()) {
        return true;
      }
      setEmptyState();
      return false;
    }

    @Override
    public E materializeNext() {
      if (!materializeHasNext()) {
        throw new NoSuchElementException();
      }
      ++pos;
      final IteratorMaterializer<E> state = getState();
      return (state == this ? wrapped : state).materializeNext();
    }
  }

  private class PendingState extends AbstractStateIteratorMaterializer {

    private final IteratorMaterializer<E> elementsMaterializer;
    private final int end;
    private final int start;
    private final IteratorMaterializer<E> wrapped;

    private int pos;

    private PendingState(final @NotNull IteratorMaterializer<E> wrapped, final int start,
        final int end, final @NotNull IteratorMaterializer<E> elementsMaterializer) {
      this.wrapped = wrapped;
      this.start = start;
      this.end = end;
      this.elementsMaterializer = elementsMaterializer;
    }

    @Override
    public int currentKnownSize() {
      return -1;
    }

    @Override
    public boolean isSizeKnown() {
      return false;
    }

    @Override
    public boolean materializeHasNext() {
      final IteratorMaterializer<E> wrapped = this.wrapped;
      if (pos < start) {
        if (!wrapped.materializeHasNext()) {
          return setState(elementsMaterializer).materializeHasNext();
        }
        return true;
      }
      if (wrapped.materializeHasNext()) {
        final DequeArrayList<E> elements = new DequeArrayList<E>(true);
        do {
          elements.add(wrapped.materializeNext());
        } while (wrapped.materializeHasNext());
        final int materializedEnd = elements.size() + pos + end;
        final int toSkip = Math.max(0, materializedEnd - start);
        for (int i = 0; i < toSkip; ++i) {
          elements.removeFirst();
        }
        return setState(
            new InsertAllIteratorMaterializer<E>(new DequeToIteratorMaterializer<E>(elements),
                elementsMaterializer)).materializeHasNext();
      }
      setEmptyState();
      return false;
    }

    @Override
    public E materializeNext() {
      if (!materializeHasNext()) {
        throw new NoSuchElementException();
      }
      ++pos;
      final IteratorMaterializer<E> state = getState();
      return (state == this ? wrapped : state).materializeNext();
    }
  }
}
