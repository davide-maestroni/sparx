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
package sparx;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;
import sparx.util.CollectionMaterializer;
import sparx.util.Require;

class CollectionMaterializerIterator<E> implements Iterator<E> {

  private final CollectionMaterializer<E> materializer;
  private int nextIndex;

  // Cannot use materializer.materializeIterator()
  CollectionMaterializerIterator(@NotNull final CollectionMaterializer<E> materializer) {
    this.materializer = Require.notNull(materializer, "materializer");
  }

  @Override
  public boolean hasNext() {
    return materializer.canMaterializeElement(nextIndex);
  }

  @Override
  public E next() {
    if (!hasNext()) {
      throw new NoSuchElementException();
    }
    return materializer.materializeElement(nextIndex++);
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("remove");
  }
}
