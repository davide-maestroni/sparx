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

import sparx1.util.annotation.NotNegative;
import sparx1.util.function.Functions;

public class ElementToListMaterializer<E> extends AbstractListMaterializer<E> {

  private final E element;

  public ElementToListMaterializer(final E element) {
    this.element = element;
  }

  @Override
  public boolean canMaterializeElement(final @NotNegative int index) {
    return index == 0;
  }

  @Override
  public int knownSize() {
    return 1;
  }

  @Override
  public boolean isRandomAccess() {
    return true;
  }

  @Override
  public boolean isSizeKnown() {
    return true;
  }

  @Override
  public boolean materializeContains(final Object element) {
    return Functions.objectsEqual(element, this.element);
  }

  @Override
  public E materializeElement(final @NotNegative int index) {
    if (index != 0) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    return element;
  }

  @Override
  public int materializeElements() {
    return 1;
  }

  @Override
  public boolean materializeEmpty() {
    return false;
  }

  @Override
  public int materializeSize() {
    return 1;
  }
}
