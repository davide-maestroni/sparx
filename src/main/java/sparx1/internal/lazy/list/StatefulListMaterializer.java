/*
 * Copyright 2026 Davide Maestroni
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
package sparx1.internal.lazy.list;

import java.util.Iterator;
import sparx1.internal.lazy.ListMaterializer;
import sparx1.util.annotation.NotNegative;
import sparx1.util.annotation.NotNull;

public abstract class StatefulListMaterializer<E> implements ListMaterializer<E> {

  private ListMaterializer<E> state;

  @Override
  public boolean canMaterializeElement(final @NotNegative int index) {
    return state.canMaterializeElement(index);
  }

  @Override
  public int knownSize() {
    return state.knownSize();
  }

  @Override
  public boolean isRandomAccess() {
    return state.isRandomAccess();
  }

  @Override
  public boolean isSizeKnown() {
    return state.isSizeKnown();
  }

  @Override
  public Iterator<E> materializeBackwardIterator(final @NotNegative int index) {
    return state.materializeBackwardIterator(index);
  }

  @Override
  public boolean materializeContains(final Object element) {
    return state.materializeContains(element);
  }

  @Override
  public E materializeElement(final @NotNegative int index) {
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
  public Iterator<E> materializeForwardIterator(final @NotNegative int index) {
    return state.materializeForwardIterator(index);
  }

  @Override
  public int materializeSize() {
    return state.materializeSize();
  }

  @Override
  public Iterator<E> materializeUnorderedIterator() {
    return state.materializeUnorderedIterator();
  }

  protected final @NotNull ListMaterializer<E> getState() {
    return state;
  }

  protected final @NotNull ListMaterializer<E> setState(
      final @NotNull ListMaterializer<E> newState) {
    return state = newState;
  }
}
