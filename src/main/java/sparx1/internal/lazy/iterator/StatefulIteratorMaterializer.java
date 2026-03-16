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
package sparx1.internal.lazy.iterator;

import sparx1.internal.lazy.IteratorMaterializer;
import sparx1.util.annotation.NotNull;
import sparx1.util.annotation.Positive;

abstract class StatefulIteratorMaterializer<E> implements IteratorMaterializer<E> {

  private volatile IteratorMaterializer<E> state;

  @Override
  public int currentKnownSize() {
    return state.currentKnownSize();
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
  public int materializeSkip(@Positive final int count) {
    return state.materializeSkip(count);
  }

  protected final @NotNull IteratorMaterializer<E> getState() {
    return state;
  }

  protected final @NotNull IteratorMaterializer<E> setEmptyState() {
    return setState(EmptyIteratorMaterializer.<E>instance());
  }

  protected final @NotNull IteratorMaterializer<E> setState(
      final @NotNull IteratorMaterializer<E> newState) {
    return state = newState;
  }
}
