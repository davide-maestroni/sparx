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
package sparx.collection.internal.lazy.list;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;
import sparx.util.Require;
import sparx.util.SizeOverflowException;

public class ReplaceSliceListMaterializer<E> implements ListMaterializer<E> {

  private final ListMaterializer<E> elementsMaterializer;
  private final ListMaterializer<E> wrapped;

  private volatile State state;

  public ReplaceSliceListMaterializer(@NotNull final ListMaterializer<E> wrapped, final int start,
      final int end, @NotNull final ListMaterializer<E> elementsMaterializer) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
    this.elementsMaterializer = Require.notNull(elementsMaterializer, "elementsMaterializer");
    if (start >= 0 && end >= 0) {
      state = new MaterialState(start, Math.max(0, end - start));
    } else {
      state = new ImmaterialState(start, end);
    }
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    if (index < 0) {
      return false;
    }
    final int start = state.materializedStart();
    if (start <= index) {
      final ListMaterializer<E> elementsMaterializer = this.elementsMaterializer;
      if (elementsMaterializer.canMaterializeElement(index - start)) {
        return true;
      }
      final long wrappedIndex =
          (long) index + state.materializedLength() - elementsMaterializer.materializeSize();
      return wrappedIndex < Integer.MAX_VALUE && wrapped.canMaterializeElement((int) wrappedIndex);
    }
    return wrapped.canMaterializeElement(index);
  }

  @Override
  public int knownSize() {
    final int knownSize = wrapped.knownSize();
    if (knownSize >= 0) {
      final int elementsSize = elementsMaterializer.knownSize();
      if (elementsSize >= 0) {
        final State state = this.state;
        final int knownStart = state.knownStart();
        final int knownLength = state.knownLength();
        if (knownStart >= 0 && knownLength >= 0) {
          final long size = Math.max(knownStart, knownSize - knownLength);
          if (size > knownSize) {
            return knownSize;
          }
          return SizeOverflowException.safeCast(size + elementsSize);
        }
      }
    }
    return -1;
  }

  @Override
  public boolean materializeContains(final Object element) {
    final ListMaterializer<E> wrapped = this.wrapped;
    final int start = state.materializedStart();
    int i = 0;
    if (element == null) {
      while (wrapped.canMaterializeElement(i)) {
        if (start == i) {
          if (elementsMaterializer.materializeContains(null)) {
            return true;
          }
          final int length = state.materializedLength();
          if (length > 0) {
            i += length;
            continue;
          }
        }
        if (wrapped.materializeElement(i) == null) {
          return true;
        }
        ++i;
      }
    } else {
      while (wrapped.canMaterializeElement(i)) {
        if (start == i) {
          if (elementsMaterializer.materializeContains(element)) {
            return true;
          }
          final int length = state.materializedLength();
          if (length > 0) {
            i += length;
            continue;
          }
        }
        if (element.equals(wrapped.materializeElement(i))) {
          return true;
        }
        ++i;
      }
    }
    return false;
  }

  @Override
  public E materializeElement(final int index) {
    if (index < 0) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    final int start = state.materializedStart();
    if (start <= index) {
      final ListMaterializer<E> elementsMaterializer = this.elementsMaterializer;
      final int elementsIndex = index - start;
      if (elementsMaterializer.canMaterializeElement(elementsIndex)) {
        return elementsMaterializer.materializeElement(elementsIndex);
      }
      final long wrappedIndex =
          (long) index + state.materializedLength() - elementsMaterializer.materializeSize();
      if (wrappedIndex >= Integer.MAX_VALUE) {
        throw new IndexOutOfBoundsException(Integer.toString(index));
      }
      return wrapped.materializeElement((int) wrappedIndex);
    }
    return wrapped.materializeElement(index);
  }

  @Override
  public int materializeElements() {
    final int start = state.materializedStart();
    final int size = wrapped.materializeElements();
    if (start < size) {
      final long remaining = Math.max(start, size - state.materializedLength());
      return SizeOverflowException.safeCast(remaining + elementsMaterializer.materializeSize());
    }
    return size;
  }

  @Override
  public boolean materializeEmpty() {
    return wrapped.materializeEmpty() || materializeSize() == 0;
  }

  @Override
  public @NotNull Iterator<E> materializeIterator() {
    return new ReplaceIterator();
  }

  @Override
  public int materializeSize() {
    final int start = state.materializedStart();
    final int wrappedSize = wrapped.materializeSize();
    if (start < wrappedSize) {
      final long size = Math.max(start, wrappedSize - state.materializedLength());
      return SizeOverflowException.safeCast(size + elementsMaterializer.materializeSize());
    }
    return wrappedSize;
  }

  private interface State {

    int knownLength();

    int knownStart();

    int materializedLength();

    int materializedStart();
  }

  private static class MaterialState implements State {

    private final int length;
    private final int start;

    MaterialState(final int start, final int length) {
      this.start = start;
      this.length = length;
    }

    @Override
    public int knownLength() {
      return length;
    }

    @Override
    public int knownStart() {
      return start;
    }

    @Override
    public int materializedLength() {
      return length;
    }

    @Override
    public int materializedStart() {
      return start;
    }
  }

  private class ImmaterialState implements State {

    private final int end;
    private final int start;

    ImmaterialState(final int start, final int end) {
      this.start = start;
      this.end = end;
    }

    @Override
    public int knownLength() {
      return -1;
    }

    @Override
    public int knownStart() {
      return start;
    }

    @Override
    public int materializedLength() {
      return materialized().materializedLength();
    }

    @Override
    public int materializedStart() {
      return materialized().materializedStart();
    }

    private @NotNull State materialized() {
      final int wrappedSize = wrapped.materializeSize();
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
      return state = new MaterialState(Math.max(0, materializedStart), materializedLength);
    }
  }

  private class ReplaceIterator implements Iterator<E> {

    private final Iterator<E> iterator = elementsMaterializer.materializeIterator();

    private long pos = 0;

    @Override
    public boolean hasNext() {
      final long pos = this.pos;
      if (pos == state.materializedStart()) {
        if (iterator.hasNext()) {
          return true;
        }
        final long wrappedIndex = pos + state.materializedLength();
        return wrappedIndex < Integer.MAX_VALUE && wrapped.canMaterializeElement(
            (int) wrappedIndex);
      }
      return wrapped.canMaterializeElement((int) pos);
    }

    @Override
    public E next() {
      try {
        if (pos == state.materializedStart()) {
          final Iterator<E> iterator = this.iterator;
          if (iterator.hasNext()) {
            return iterator.next();
          }
          pos += state.materializedLength();
        }
        return wrapped.materializeElement((int) pos++);
      } catch (final IndexOutOfBoundsException ignored) {
        throw new NoSuchElementException();
      }
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove");
    }
  }
}
