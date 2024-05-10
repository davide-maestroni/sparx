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
package sparx.collection.internal.lazy.list;

public abstract class AbstractListMaterializer<E> implements ListMaterializer<E> {

  @Override
  public boolean materializeContains(final Object element) {
    int i = 0;
    if (element == null) {
      while (canMaterializeElement(i)) {
        if (materializeElement(i++) == null) {
          return true;
        }
      }
    } else {
      while (canMaterializeElement(i)) {
        if (element.equals(materializeElement(i++))) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public int materializeElements() {
    int i = 0;
    while (canMaterializeElement(i)) {
      materializeElement(i++);
    }
    return i;
  }

  @Override
  public boolean materializeEmpty() {
    return !canMaterializeElement(0);
  }

  @Override
  public int materializeSize() {
    int size = 0;
    while (canMaterializeElement(size)) {
      ++size;
    }
    return size;
  }
}
