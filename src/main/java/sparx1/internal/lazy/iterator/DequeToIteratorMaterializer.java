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

import sparx.util.annotation.Positive;
import sparx1.internal.lazy.IteratorMaterializer;
import sparx1.util.DequeArrayList;
import sparx1.util.annotation.NotNull;

public class DequeToIteratorMaterializer<E> implements IteratorMaterializer<E> {

  private final DequeArrayList<E> elements;

  public DequeToIteratorMaterializer(final @NotNull DequeArrayList<E> elements) {
    this.elements = elements;
  }

  @Override
  public int currentKnownSize() {
    return elements.size();
  }

  @Override
  public boolean materializeHasNext() {
    return !elements.isEmpty();
  }

  @Override
  public E materializeNext() {
    return elements.removeFirst();
  }

  @Override
  public int materializeSkip(@Positive int count) {
    final int skipped = Math.min(count, elements.size());
    elements.removeRange(0, skipped);
    return skipped;
  }
}
