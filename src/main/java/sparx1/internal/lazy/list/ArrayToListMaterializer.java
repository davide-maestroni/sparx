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
import sparx1.util.annotation.NotNull;

public class ArrayToListMaterializer<E> extends AbstractListMaterializer<E> {

  private final E[] elements;

  public ArrayToListMaterializer(final @NotNull E... elements) {
    this.elements = elements;
  }

  @Override
  public boolean canMaterializeElement(final @NotNegative int index) {
    return index < elements.length;
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
  public int knownSize() {
    return elements.length;
  }

  @Override
  public boolean materializeContains(final Object element) {
    if (element == null) {
      for (final E e : elements) {
        if (e == null) {
          return true;
        }
      }
    } else {
      for (final E e : elements) {
        if (element.equals(e)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public E materializeElement(final @NotNegative int index) {
    return elements[index];
  }

  @Override
  public int materializeElements() {
    return elements.length;
  }

  @Override
  public boolean materializeEmpty() {
    return elements.length == 0;
  }

  @Override
  public int materializeSize() {
    return elements.length;
  }
}
