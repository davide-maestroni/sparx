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
import org.jetbrains.annotations.NotNull;
import sparx.util.Require;

public class OrElseListMaterializer<E> implements ListMaterializer<E> {

  private volatile ListMaterializer<E> state;

  public OrElseListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      @NotNull final ListMaterializer<E> elementsMaterializer) {
    state = new ImmaterialState(Require.notNull(wrapped, "wrapped"),
        Require.notNull(elementsMaterializer, "elementsMaterializer"));
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
  public boolean materializeContains(final Object element) {
    return state.materializeContains(element);
  }

  @Override
  public E materializeElement(final int index) {
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
  public @NotNull Iterator<E> materializeIterator() {
    return state.materializeIterator();
  }

  @Override
  public int materializeSize() {
    return state.materializeSize();
  }

  private class ImmaterialState implements ListMaterializer<E> {

    private final ListMaterializer<E> wrapped;
    private final ListMaterializer<E> elementsMaterializer;

    private ImmaterialState(@NotNull final ListMaterializer<E> wrapped,
        @NotNull final ListMaterializer<E> elementsMaterializer) {
      this.wrapped = wrapped;
      this.elementsMaterializer = elementsMaterializer;
    }

    @Override
    public boolean canMaterializeElement(final int index) {
      return materialized().canMaterializeElement(index);
    }

    @Override
    public int knownSize() {
      final ListMaterializer<E> wrapped = this.wrapped;
      final int knownSize = wrapped.knownSize();
      if (knownSize > 0) {
        state = wrapped;
        return knownSize;
      }
      if (knownSize == 0) {
        return (state = elementsMaterializer).knownSize();
      }
      return knownSize;
    }

    @Override
    public boolean materializeContains(final Object element) {
      return materialized().materializeContains(element);
    }

    @Override
    public E materializeElement(final int index) {
      return materialized().materializeElement(index);
    }

    @Override
    public int materializeElements() {
      return materialized().materializeElements();
    }

    @Override
    public boolean materializeEmpty() {
      return materialized().materializeEmpty();
    }

    @Override
    public @NotNull Iterator<E> materializeIterator() {
      return materialized().materializeIterator();
    }

    @Override
    public int materializeSize() {
      return materialized().materializeSize();
    }

    private @NotNull ListMaterializer<E> materialized() {
      if (wrapped.materializeEmpty()) {
        return state = elementsMaterializer;
      }
      return state = wrapped;
    }
  }
}
