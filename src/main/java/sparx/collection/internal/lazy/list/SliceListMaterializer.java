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
import sparx.collection.internal.lazy.AbstractCollectionMaterializer;
import sparx.util.Require;

public class SliceListMaterializer<E> extends AbstractCollectionMaterializer<E> implements
    ListMaterializer<E> {

  private final ListMaterializer<E> wrapped;

  private volatile State state;

  public SliceListMaterializer(@NotNull final ListMaterializer<E> wrapped, final int start,
      final int end) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
    if (start >= 0 && end >= 0) {
      state = new MaterialState(start, Math.max(0, end - start));
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
      final State state = this.state;
      final int knownStart = state.knownStart();
      final int knownLength = state.knownLength();
      if (knownStart >= 0 && knownLength >= 0) {
        return Math.min(knownLength, knownSize - knownStart);
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