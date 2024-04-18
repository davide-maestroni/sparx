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
package sparx.collection.internal;

import java.util.Iterator;

public abstract class AbstractCollectionMaterializer<E> implements CollectionMaterializer<E> {

  @Override
  public boolean materializeContains(final Object element) {
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
    return false;
  }

  @Override
  public boolean materializeEmpty() {
    return !materializeIterator().hasNext();
  }

  @Override
  public int materializeSize() {
    int size = 0;
    final Iterator<E> iterator = materializeIterator();
    while (iterator.hasNext()) {
      iterator.next();
      ++size;
    }
    return size;
  }
}
