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
import sparx1.util.annotation.NotNull;
import sparx1.util.annotation.Positive;

public class SliceIteratorMaterializer<E> extends StatefulIteratorMaterializer<E> {

  public SliceIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped, final int start,
      final int end) {
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
      setState(new MaterialState(wrapped, materializedStart, materializedLength));
    } else if (start >= 0) {
      if (end >= 0) {
        setState(new MaterialState(wrapped, start, Math.max(0, end - start)));
      } else {
        setState(new PendingState(wrapped, start, end));
      }
    } else {
      setState(new InitialState(wrapped, start, end));
    }
  }

  private class InitialState implements IteratorMaterializer<E> {

    private final int end;
    private final int start;
    private final IteratorMaterializer<E> wrapped;

    private InitialState(final @NotNull IteratorMaterializer<E> wrapped, final int start,
        final int end) {
      this.wrapped = wrapped;
      this.start = start;
      this.end = end;
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
          materializedStart = wrappedSize + materializedStart;
        }
        int materializedEnd = end;
        if (materializedEnd < 0) {
          materializedEnd = wrappedSize + materializedEnd;
        }
        final int materializedLength;
        if (materializedStart >= 0 && materializedEnd >= 0) {
          materializedLength = Math.max(0, materializedEnd - materializedStart);
        } else {
          materializedLength = 0;
        }
        return setState(new MaterialState(new DequeToIteratorMaterializer<E>(elements),
            Math.max(0, materializedStart), materializedLength)).materializeHasNext();
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

  private class MaterialState implements IteratorMaterializer<E> {

    private final int length;
    private final int start;
    private final IteratorMaterializer<E> wrapped;

    private MaterialState(final @NotNull IteratorMaterializer<E> wrapped, final int start,
        final int length) {
      this.wrapped = wrapped;
      this.start = start;
      this.length = length;
    }

    @Override
    public int currentKnownSize() {
      final int knownSize = wrapped.currentKnownSize();
      if (knownSize >= 0) {
        if (knownSize == 0) {
          return 0;
        }
        return Math.min(length, knownSize - start);
      }
      return -1;
    }

    @Override
    public boolean isSizeKnown() {
      return wrapped.isSizeKnown();
    }

    @Override
    public boolean materializeHasNext() {
      final IteratorMaterializer<E> wrapped = this.wrapped;
      if (start > 0) {
        wrapped.materializeSkip(start);
      }
      return setState(new TakeFirstIteratorMaterializer<E>(wrapped, length)).materializeHasNext();
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
      final IteratorMaterializer<E> wrapped = this.wrapped;
      if (start > 0) {
        wrapped.materializeSkip(start);
      }
      final int length = this.length;
      if (count < length) {
        final int skipped = wrapped.materializeSkip(count);
        if (skipped == count) {
          setState(new TakeFirstIteratorMaterializer<E>(wrapped, length - count));
        } else {
          setEmptyState();
        }
        return skipped;
      }
      setEmptyState();
      return length > 0 ? wrapped.materializeSkip(length) : 0;
    }
  }

  private class PendingState implements IteratorMaterializer<E> {

    private final int end;
    private final int start;
    private final IteratorMaterializer<E> wrapped;

    private PendingState(final @NotNull IteratorMaterializer<E> wrapped, final int start,
        final int end) {
      this.wrapped = wrapped;
      this.start = start;
      this.end = end;
    }

    @Override
    public int currentKnownSize() {
      final int knownSize = wrapped.currentKnownSize();
      if (knownSize >= 0) {
        if (knownSize == 0) {
          return 0;
        }
        final int materializedEnd = knownSize + end;
        return Math.min(Math.max(0, materializedEnd - start), knownSize - start);
      }
      return -1;
    }

    @Override
    public boolean isSizeKnown() {
      return wrapped.isSizeKnown();
    }

    @Override
    public boolean materializeHasNext() {
      final IteratorMaterializer<E> wrapped = this.wrapped;
      if (wrapped.materializeHasNext()) {
        if (start > 0) {
          wrapped.materializeSkip(start);
        }
        final DequeArrayList<E> elements = new DequeArrayList<E>(true);
        while (wrapped.materializeHasNext()) {
          elements.add(wrapped.materializeNext());
        }
        final int materializeLength = Math.max(0, elements.size() + end);
        if (materializeLength == 0) {
          setEmptyState();
          return false;
        }
        while (elements.size() > materializeLength) {
          elements.removeLast();
        }
        return setState(new DequeToIteratorMaterializer<E>(elements)).materializeHasNext();
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
      final IteratorMaterializer<E> state = getState();
      return (state == this ? wrapped : state).materializeNext();
    }

    @Override
    public int materializeSkip(final @Positive int count) {
      final IteratorMaterializer<E> wrapped = this.wrapped;
      if (start > 0) {
        wrapped.materializeSkip(start);
      }
      final DequeArrayList<E> elements = new DequeArrayList<E>(true);
      while (wrapped.materializeHasNext()) {
        elements.add(wrapped.materializeNext());
      }
      final int materializeLength = Math.max(0, elements.size() + end);
      if (materializeLength == 0) {
        setEmptyState();
        return 0;
      }
      while (elements.size() > materializeLength) {
        elements.removeLast();
      }
      if (count < elements.size()) {
        return setState(new DequeToIteratorMaterializer<E>(elements)).materializeSkip(count);
      }
      setEmptyState();
      return elements.size();
    }
  }
}
