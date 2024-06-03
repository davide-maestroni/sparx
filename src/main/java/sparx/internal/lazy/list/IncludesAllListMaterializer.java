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

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import sparx.util.UncheckedException;

public class IncludesAllListMaterializer<E> implements ListMaterializer<Boolean> {

  private static final State FALSE_STATE = new FalseState();
  private static final State TRUE_STATE = new TrueState();

  private volatile State state;

  public IncludesAllListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      @NotNull final Iterable<?> elements) {
    state = new ImmaterialState(wrapped, elements);
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
  public boolean materializeContains(final Object element) {
    return Boolean.valueOf(state.materialized()).equals(element);
  }

  @Override
  public Boolean materializeElement(final int index) {
    if (index == 0) {
      return state.materialized();
    }
    throw new IndexOutOfBoundsException(Integer.toString(index));
  }

  @Override
  public int materializeElements() {
    state.materialized();
    return 1;
  }

  @Override
  public boolean materializeEmpty() {
    return false;
  }

  @Override
  public @NotNull Iterator<Boolean> materializeIterator() {
    return new ListMaterializerIterator<Boolean>(this);
  }

  @Override
  public int materializeSize() {
    return 1;
  }

  private interface State {

    boolean materialized();
  }

  private static class FalseState implements State {

    @Override
    public boolean materialized() {
      return false;
    }
  }

  private static class TrueState implements State {

    @Override
    public boolean materialized() {
      return true;
    }
  }

  private class ImmaterialState implements State {

    private final Iterable<?> elements;
    private final AtomicBoolean isMaterialized = new AtomicBoolean(false);
    private final ListMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final ListMaterializer<E> wrapped,
        @NotNull final Iterable<?> elements) {
      this.wrapped = wrapped;
      this.elements = elements;
    }

    @Override
    public boolean materialized() {
      if (!isMaterialized.compareAndSet(false, true)) {
        throw new ConcurrentModificationException();
      }
      try {
        final HashSet<Object> elements = new HashSet<Object>();
        for (final Object element : this.elements) {
          elements.add(element);
        }
        if (elements.isEmpty()) {
          state = TRUE_STATE;
          return true;
        }
        final ListMaterializer<E> wrapped = this.wrapped;
        int i = 0;
        while (wrapped.canMaterializeElement(i)) {
          elements.remove(wrapped.materializeElement(i));
          if (elements.isEmpty()) {
            state = TRUE_STATE;
            return true;
          }
          ++i;
        }
        state = FALSE_STATE;
        return false;
      } catch (final Exception e) {
        isMaterialized.set(false);
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }
}
