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

import java.util.Collection;
import java.util.Iterator;
import sparx1.util.annotation.NotNull;

public class CollectionToIteratorMaterializer<E> extends AbstractIteratorMaterializer<E> {

  private final Collection<E> elements;
  private final Iterator<E> iterator;

  private int pos;

  public CollectionToIteratorMaterializer(final @NotNull Collection<E> elements) {
    this.elements = elements;
    iterator = elements.iterator();
  }

  @NotNull
  public Collection<E> elements() {
    return elements;
  }

  @Override
  public int currentKnownSize() {
    return elements.size() - pos;
  }

  @Override
  public boolean materializeHasNext() {
    return iterator.hasNext();
  }

  @Override
  public E materializeNext() {
    ++pos;
    return iterator.next();
  }
}
