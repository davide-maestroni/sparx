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
package sparx.internal.lazy.iterator;

import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;
import sparx.util.DequeueList;

public class ReplaceSliceIteratorMaterializer<E> extends AbstractIteratorMaterializer<E> {

  private volatile IteratorMaterializer<E> state;

  public ReplaceSliceIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      final int start, final int end, @NotNull final IteratorMaterializer<E> elementsMaterializer) {
    if (start >= 0 && end >= 0) {
      state = new MaterialState(wrapped, start, Math.max(0, end - start), elementsMaterializer);
    } else {
      state = new ImmaterialState(wrapped, start, end, elementsMaterializer);
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

  private class ImmaterialState implements IteratorMaterializer<E> {

    private final IteratorMaterializer<E> elementsMaterializer;
    private final int end;
    private final int start;
    private final IteratorMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final IteratorMaterializer<E> wrapped, final int start,
        final int end, @NotNull final IteratorMaterializer<E> elementsMaterializer) {
      this.wrapped = wrapped;
      this.start = start;
      this.end = end;
      this.elementsMaterializer = elementsMaterializer;
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
            Math.max(0, materializedStart), materializedLength,
            elementsMaterializer)).materializeHasNext();
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
      throw new UnsupportedOperationException();
    }
  }

  private class MaterialState implements IteratorMaterializer<E> {

    private final IteratorMaterializer<E> elementsMaterializer;
    private final int length;
    private final int start;
    private final IteratorMaterializer<E> wrapped;

    private int pos;

    private MaterialState(@NotNull final IteratorMaterializer<E> wrapped, final int start,
        final int length, @NotNull final IteratorMaterializer<E> elementsMaterializer) {
      this.wrapped = wrapped;
      this.start = start;
      this.length = length;
      this.elementsMaterializer = elementsMaterializer;
    }

    @Override
    public int knownSize() {
      return -1;
    }

    @Override
    public boolean materializeHasNext() {
      final IteratorMaterializer<E> wrapped = this.wrapped;
      if (pos == start) {
        final IteratorMaterializer<E> elementsMaterializer = this.elementsMaterializer;
        if (elementsMaterializer.materializeHasNext()) {
          wrapped.materializeSkip(length);
          return (state = new AppendAllIteratorMaterializer<E>(elementsMaterializer,
              wrapped)).materializeHasNext();
        }
        (state = wrapped).materializeSkip(length);
      }
      if (wrapped.materializeHasNext()) {
        return true;
      }
      state = EmptyIteratorMaterializer.instance();
      return false;
    }

    @Override
    public E materializeNext() {
      if (!materializeHasNext()) {
        throw new NoSuchElementException();
      }
      ++pos;
      final IteratorMaterializer<E> state = ReplaceSliceIteratorMaterializer.this.state;
      return (state == this ? wrapped : state).materializeNext();
    }

    @Override
    public int materializeSkip(final int count) {
      throw new UnsupportedOperationException();
    }
  }
}
