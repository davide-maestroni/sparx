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

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;

public class SliceListMaterializer<E> extends AbstractListMaterializer<E> implements
    ListMaterializer<E> {

  private final ListMaterializer<E> wrapped;

  private volatile State state;

  public SliceListMaterializer(@NotNull final ListMaterializer<E> wrapped, final int start,
      final int end) {
    this.wrapped = wrapped;
    final int knownSize = wrapped.knownSize();
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
      state = new MaterialState(materializedStart, materializedLength);
    } else {
      state = new ImmaterialState(start, end);
    }
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    if (index < 0 || index >= state.materializedLength()) {
      return false;
    }
    final long wrappedIndex = (long) index + state.materializedStart();
    return wrappedIndex < Integer.MAX_VALUE && wrapped.canMaterializeElement((int) wrappedIndex);
  }

  @Override
  public int knownSize() {
    final int knownSize = wrapped.knownSize();
    if (knownSize >= 0) {
      if (knownSize == 0) {
        return 0;
      }
      final int knownLength = state.knownLength();
      if (knownLength >= 0) {
        return knownLength;
      }
    }
    return -1;
  }

  @Override
  public E materializeElement(final int index) {
    if (index < 0 || index >= state.materializedLength()) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    final long wrappedIndex = (long) index + state.materializedStart();
    if (wrappedIndex >= Integer.MAX_VALUE) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    return wrapped.materializeElement((int) wrappedIndex);
  }

  @Override
  public int materializeElements() {
    wrapped.materializeElements();
    return materializeSize();
  }

  @Override
  public boolean materializeEmpty() {
    return wrapped.materializeEmpty() || materializeSize() == 0;
  }

  @Override
  public @NotNull Iterator<E> materializeIterator() {
    return new SliceIterator();
  }

  @Override
  public int materializeSize() {
    return Math.min(state.materializedLength(),
        wrapped.materializeSize() - state.materializedStart());
  }

  private interface State {

    int knownLength();

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
      return state = new MaterialState(Math.max(0, materializedStart), materializedLength);
    }
  }

  private class SliceIterator implements Iterator<E> {

    private long pos = state.materializedStart();
    private final long end = pos + state.materializedLength();

    @Override
    public boolean hasNext() {
      final long pos = this.pos;
      return pos < end && wrapped.canMaterializeElement((int) pos);
    }

    @Override
    public E next() {
      try {
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
