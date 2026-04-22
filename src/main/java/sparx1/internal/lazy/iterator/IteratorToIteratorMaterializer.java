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

import java.util.Iterator;
import sparx1.util.annotation.NotNull;

public class IteratorToIteratorMaterializer<E> extends AbstractIteratorMaterializer<E> {

  private final Iterator<? extends E> elements;
  private final int size;

  private int pos;

  public IteratorToIteratorMaterializer(final @NotNull Iterator<? extends E> elements) {
    this(elements, -1);
  }

  public IteratorToIteratorMaterializer(final @NotNull Iterator<? extends E> elements,
      final int size) {
    this.elements = elements;
    this.size = size;
  }

  @Override
  public int currentKnownSize() {
    final int size = this.size;
    if (size >= 0) {
      return size - pos;
    }
    return -1;
  }

  @Override
  public boolean isSizeKnown() {
    return size >= 0;
  }

  @Override
  public boolean materializeHasNext() {
    return elements.hasNext();
  }

  @Override
  public E materializeNext() {
    final E next = elements.next();
    ++pos;
    return next;
  }
}
