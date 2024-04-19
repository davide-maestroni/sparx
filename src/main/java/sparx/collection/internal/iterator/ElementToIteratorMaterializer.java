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
package sparx.collection.internal.iterator;

import java.util.NoSuchElementException;

public class ElementToIteratorMaterializer<E> implements IteratorMaterializer<E> {

  private final E element;

  private boolean consumed;

  public ElementToIteratorMaterializer(final E element) {
    this.element = element;
  }

  @Override
  public int knownSize() {
    return consumed ? 0 : 1;
  }

  @Override
  public boolean materializeHasNext() {
    return !consumed;
  }

  @Override
  public E materializeNext() {
    if (consumed) {
      throw new NoSuchElementException();
    }
    consumed = true;
    return element;
  }

  @Override
  public int materializeSkip(final int count) {
    if (count > 0 && !consumed) {
      consumed = true;
      return 1;
    }
    return 0;
  }
}
