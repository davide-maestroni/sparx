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
package sparx.internal.lazy.list;

import java.util.Iterator;

public abstract class AbstractListMaterializer<E> implements ListMaterializer<E> {

  @Override
  public boolean materializeContains(final Object element) {
    if (isRandomAccess()) {
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
    } else {
      final Iterator<E> iterator = materializeIterator();
      if (element == null) {
        while (iterator.hasNext()) {
          if (iterator.next() == null) {
            return true;
          }
        }
      } else {
        while (iterator.hasNext()) {
          if (element.equals(iterator.next())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  @Override
  public int materializeElements() {
    int i = 0;
    if (isRandomAccess()) {
      while (canMaterializeElement(i)) {
        materializeElement(i++);
      }
    } else {
      final Iterator<E> iterator = materializeIterator();
      while (iterator.hasNext()) {
        iterator.next();
        ++i;
      }
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
