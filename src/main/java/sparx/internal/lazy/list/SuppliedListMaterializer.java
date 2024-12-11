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

import org.jetbrains.annotations.NotNull;
import sparx.util.UncheckedException;
import sparx.util.annotation.NotNegative;
import sparx.util.function.Supplier;

public class SuppliedListMaterializer<E> implements ListMaterializer<E> {

  private volatile ListMaterializer<E> state;

  public SuppliedListMaterializer(@NotNull final Supplier<? extends ListMaterializer<E>> supplier) {
    state = new ImmaterialState(supplier);
  }

  @Override
  public boolean canMaterializeElement(@NotNegative final int index) {
    return state.canMaterializeElement(index);
  }

  @Override
  public boolean isRandomAccess() {
    return state.isRandomAccess();
  }

  @Override
  public int knownSize() {
    return state.knownSize();
  }

  @Override
  public boolean materializeContains(final Object element) {
    return state.materializeContains(element);
  }

  @Override
  public E materializeElement(@NotNegative final int index) {
    return state.materializeElement(index);
  }

  @Override
  public int materializeElements() {
    return state.materializeElements();
  }

  @Override
  public boolean materializeEmpty() {
    return state.materializeEmpty();
  }

  @Override
  public @NotNull java.util.Iterator<E> materializeIterator() {
    return state.materializeIterator();
  }

  @Override
  public int materializeSize() {
    return state.materializeSize();
  }

  private class ImmaterialState implements ListMaterializer<E> {

    private final Supplier<? extends ListMaterializer<E>> supplier;

    private ImmaterialState(@NotNull final Supplier<? extends ListMaterializer<E>> supplier) {
      this.supplier = supplier;
    }

    @Override
    public boolean canMaterializeElement(@NotNegative final int index) {
      try {
        final ListMaterializer<E> elementsMaterializer = supplier.get();
        return (state = elementsMaterializer).canMaterializeElement(index);
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public boolean isRandomAccess() {
      return false;
    }

    @Override
    public int knownSize() {
      return -1;
    }

    @Override
    public boolean materializeContains(final Object element) {
      try {
        return (state = supplier.get()).materializeContains(element);
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public E materializeElement(@NotNegative final int index) {
      try {
        return (state = supplier.get()).materializeElement(index);
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public int materializeElements() {
      try {
        return (state = supplier.get()).materializeElements();
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public boolean materializeEmpty() {
      try {
        return (state = supplier.get()).materializeEmpty();
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public @NotNull java.util.Iterator<E> materializeIterator() {
      try {
        return (state = supplier.get()).materializeIterator();
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public int materializeSize() {
      try {
        return (state = supplier.get()).materializeSize();
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }
}
