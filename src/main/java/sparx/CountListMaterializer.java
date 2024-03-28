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
package sparx;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import sparx.collection.ListMaterializer;
import sparx.util.Require;
import sparx.util.UncheckedException;

class CountListMaterializer<E> implements ListMaterializer<Integer> {

  private volatile State state;

  CountListMaterializer(@NotNull final ListMaterializer<E> wrapped) {
    state = new ImmaterialState(Require.notNull(wrapped, "wrapped"));
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    return index == 0;
  }

  @Override
  public int knownSize() {
    return 1;
  }

  @Override
  public Integer materializeElement(final int index) {
    if (index != 0) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    return state.materialized();
  }

  @Override
  public boolean materializeEmpty() {
    return false;
  }

  @Override
  public @NotNull Iterator<Integer> materializeIterator() {
    return new ListMaterializerIterator<Integer>(this);
  }

  @Override
  public int materializeSize() {
    return 1;
  }

  private interface State {

    int materialized();
  }

  private static class CountState implements State {

    private final int count;

    private CountState(final int count) {
      this.count = count;
    }

    @Override
    public int materialized() {
      return count;
    }
  }

  private class ImmaterialState implements State {

    private final AtomicBoolean isMaterialized = new AtomicBoolean(false);
    private final ListMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final ListMaterializer<E> wrapped) {
      this.wrapped = wrapped;
    }

    @Override
    public int materialized() {
      if (!isMaterialized.compareAndSet(false, true)) {
        throw new ConcurrentModificationException();
      }
      try {
        final int count = wrapped.materializeSize();
        state = new CountState(count);
        return count;
      } catch (final Exception e) {
        isMaterialized.set(false);
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }
}
