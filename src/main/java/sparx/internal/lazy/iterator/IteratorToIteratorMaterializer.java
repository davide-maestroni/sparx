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

import java.util.Iterator;
import org.jetbrains.annotations.NotNull;

public class IteratorToIteratorMaterializer<E> extends AutoSkipIteratorMaterializer<E> {

  private final Iterator<E> elements;

  public IteratorToIteratorMaterializer(@NotNull final Iterator<E> elements) {
    this.elements = elements;
  }

  @Override
  public int knownSize() {
    return -1;
  }

  @Override
  public boolean materializeHasNext() {
    return elements.hasNext();
  }

  @Override
  public E materializeNext() {
    return elements.next();
  }
}
