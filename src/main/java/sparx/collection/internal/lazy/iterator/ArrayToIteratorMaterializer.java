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
package sparx.collection.internal.lazy.iterator;

import org.jetbrains.annotations.NotNull;
import sparx.util.Require;

public class ArrayToIteratorMaterializer<E> implements IteratorMaterializer<E> {

  private final E[] elements;

  private int pos;

  public ArrayToIteratorMaterializer(@NotNull final E... elements) {
    this.elements = Require.notNull(elements, "elements");
  }

  @Override
  public int knownSize() {
    return elements.length - pos;
  }

  @Override
  public boolean materializeHasNext() {
    return pos < elements.length;
  }

  @Override
  public E materializeNext() {
    return elements[pos++];
  }

  @Override
  public int materializeSkip(final int count) {
    final int skipped = Math.max(0, Math.min(count, elements.length - pos));
    pos += skipped;
    return skipped;
  }
}