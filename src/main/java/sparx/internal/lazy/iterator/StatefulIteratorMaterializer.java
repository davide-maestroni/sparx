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

abstract class StatefulIteratorMaterializer<E> implements IteratorMaterializer<E> {

  private int index;
  private volatile IteratorMaterializer<E> state;

  protected StatefulIteratorMaterializer() {
    this(0);
  }

  protected StatefulIteratorMaterializer(final int index) {
    this.index = index;
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
    final E next = state.materializeNext();
    ++index;
    return next;
  }

  @Override
  public int materializeSkip(final int count) {
    final int skipped = state.materializeSkip(count);
    index += skipped;
    return skipped;
  }

  @Override
  public int nextIndex() {
    return index;
  }

  protected final @NotNull IteratorMaterializer<E> getState() {
    return state;
  }

  protected final @NotNull IteratorMaterializer<E> setEmptyState() {
    return setState(EmptyIteratorMaterializer.<E>instance());
  }

  protected final @NotNull IteratorMaterializer<E> setState(
      @NotNull final IteratorMaterializer<E> newState) {
    return state = newState;
  }
}
