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

import org.jetbrains.annotations.NotNull;
import sparx.util.UncheckedException;
import sparx.util.function.Supplier;

public class SuppliedIteratorMaterializer<E> implements IteratorMaterializer<E> {

  private volatile IteratorMaterializer<E> state;

  public SuppliedIteratorMaterializer(
      @NotNull final Supplier<? extends IteratorMaterializer<E>> supplier) {
    state = new ImmaterialState(supplier);
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

    private final Supplier<? extends IteratorMaterializer<E>> supplier;

    private ImmaterialState(@NotNull final Supplier<? extends IteratorMaterializer<E>> supplier) {
      this.supplier = supplier;
    }

    @Override
    public int knownSize() {
      return -1;
    }

    @Override
    public boolean materializeHasNext() {
      try {
        final IteratorMaterializer<E> elementsMaterializer = supplier.get();
        return (state = elementsMaterializer).materializeHasNext();
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public E materializeNext() {
      try {
        final IteratorMaterializer<E> elementsMaterializer = supplier.get();
        return (state = elementsMaterializer).materializeNext();
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public int materializeSkip(final int count) {
      try {
        final IteratorMaterializer<E> elementsMaterializer = supplier.get();
        return (state = elementsMaterializer).materializeSkip(count);
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }
}
