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
import sparx.util.function.Function;

class SingleMapListMaterializer<E, F> implements ListMaterializer<F> {

  private volatile ListMaterializer<F> state;

  SingleMapListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      @NotNull final Function<? super E, ? extends ListMaterializer<F>> mapper) {
    state = new ImmaterialState(Require.notNull(wrapped, "wrapped"),
        Require.notNull(mapper, "mapper"));
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    return state.canMaterializeElement(index);
  }

  @Override
  public int knownSize() {
    return state.knownSize();
  }

  @Override
  public F materializeElement(final int index) {
    return state.materializeElement(index);
  }

  @Override
  public boolean materializeEmpty() {
    return state.materializeEmpty();
  }

  @Override
  public @NotNull Iterator<F> materializeIterator() {
    return state.materializeIterator();
  }

  @Override
  public int materializeSize() {
    return state.materializeSize();
  }

  private class ImmaterialState implements ListMaterializer<F> {

    private final AtomicBoolean isMaterialized = new AtomicBoolean(false);
    private final Function<? super E, ? extends ListMaterializer<F>> mapper;
    private final ListMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final ListMaterializer<E> wrapped,
        @NotNull final Function<? super E, ? extends ListMaterializer<F>> mapper) {
      this.wrapped = wrapped;
      this.mapper = mapper;
    }

    @Override
    public boolean canMaterializeElement(final int index) {
      if (index < 0) {
        return false;
      }
      if (!isMaterialized.compareAndSet(false, true)) {
        throw new ConcurrentModificationException();
      }
      try {
        final ListMaterializer<F> elementsMaterializer =
            mapper.apply(wrapped.materializeElement(0));
        state = elementsMaterializer;
        return elementsMaterializer.canMaterializeElement(index);
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public int knownSize() {
      return -1;
    }

    @Override
    public F materializeElement(final int index) {
      if (!isMaterialized.compareAndSet(false, true)) {
        throw new ConcurrentModificationException();
      }
      try {
        final ListMaterializer<F> elementsMaterializer =
            mapper.apply(wrapped.materializeElement(0));
        state = elementsMaterializer;
        return elementsMaterializer.materializeElement(index);
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public boolean materializeEmpty() {
      return wrapped.materializeEmpty();
    }

    @Override
    public @NotNull Iterator<F> materializeIterator() {
      return new ListMaterializerIterator<F>(this);
    }

    @Override
    public int materializeSize() {
      if (!isMaterialized.compareAndSet(false, true)) {
        throw new ConcurrentModificationException();
      }
      try {
        final ListMaterializer<F> elementsMaterializer =
            mapper.apply(wrapped.materializeElement(0));
        state = elementsMaterializer;
        return elementsMaterializer.materializeSize();
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }
}
