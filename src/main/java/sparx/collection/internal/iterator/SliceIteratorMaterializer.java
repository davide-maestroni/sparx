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
package sparx.collection.internal.iterator;

import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;
import sparx.util.DequeueList;
import sparx.util.Require;

public class SliceIteratorMaterializer<E> implements IteratorMaterializer<E> {

  private volatile IteratorMaterializer<E> state;

  public SliceIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped, final int start,
      final int end) {
    if (start >= 0 && end >= 0) {
      state = new MaterialState(Require.notNull(wrapped, "wrapped"), start,
          Math.max(0, end - start));
    } else {
      state = new ImmaterialState(Require.notNull(wrapped, "wrapped"), start, end);
    }
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
  public E materializeNext() {
    return state.materializeNext();
  }

  @Override
  public int materializeSkip(final int count) {
    return state.materializeSkip(count);
  }

  private class ImmaterialState implements IteratorMaterializer<E> {

    private final int end;
    private final int start;
    private final IteratorMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final IteratorMaterializer<E> wrapped, final int start,
        final int end) {
      this.wrapped = wrapped;
      this.start = start;
      this.end = end;
    }

    @Override
    public int knownSize() {
      return -1;
    }

    @Override
    public boolean materializeHasNext() {
      final IteratorMaterializer<E> wrapped = this.wrapped;
      if (wrapped.materializeHasNext()) {
        final DequeueList<E> elements = new DequeueList<E>();
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
        return (state = new MaterialState(new DequeueToIteratorMaterializer<E>(elements),
            Math.max(0, materializedStart), materializedLength)).materializeHasNext();
      }
      state = EmptyIteratorMaterializer.instance();
      return false;
    }

    @Override
    public E materializeNext() {
      if (!materializeHasNext()) {
        throw new NoSuchElementException();
      }
      return state.materializeNext();
    }

    @Override
    public int materializeSkip(final int count) {
      if (count > 0) {
        materializeHasNext();
        return state.materializeSkip(count);
      }
      return 0;
    }
  }

  private class MaterialState implements IteratorMaterializer<E> {

    private final int length;
    private final int start;
    private final IteratorMaterializer<E> wrapped;

    private MaterialState(@NotNull final IteratorMaterializer<E> wrapped, final int start,
        final int length) {
      this.wrapped = wrapped;
      this.start = start;
      this.length = length;
    }

    @Override
    public int knownSize() {
      final int knownSize = wrapped.knownSize();
      if (knownSize >= 0) {
        if (knownSize == 0) {
          return 0;
        }
        return Math.max(length, knownSize - start);
      }
      return -1;
    }

    @Override
    public boolean materializeHasNext() {
      final IteratorMaterializer<E> wrapped = this.wrapped;
      wrapped.materializeSkip(start);
      return (state = new TakeIteratorMaterializer<E>(wrapped, length)).materializeHasNext();
    }

    @Override
    public E materializeNext() {
      if (!materializeHasNext()) {
        throw new NoSuchElementException();
      }
      return state.materializeNext();
    }

    @Override
    public int materializeSkip(final int count) {
      if (count > 0) {
        final IteratorMaterializer<E> wrapped = this.wrapped;
        wrapped.materializeSkip(start);
        final int length = this.length;
        if (count < length) {
          final int skipped = wrapped.materializeSkip(count);
          if (skipped == count) {
            state = new TakeIteratorMaterializer<E>(wrapped, length - count);
          } else {
            state = EmptyIteratorMaterializer.instance();
          }
          return skipped;
        }
        state = EmptyIteratorMaterializer.instance();
        return wrapped.materializeSkip(length);
      }
      return 0;
    }
  }
}
