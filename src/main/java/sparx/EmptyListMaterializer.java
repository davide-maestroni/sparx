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

import java.util.Collections;
import java.util.Iterator;
import org.jetbrains.annotations.NotNull;
import sparx.collection.ListMaterializer;

class EmptyListMaterializer<E> implements ListMaterializer<E> {

  @Override
  public boolean canMaterializeElement(final int index) {
    return false;
  }

  @Override
  public int knownSize() {
    return 0;
  }

  @Override
  public E materializeElement(final int index) {
    throw new IndexOutOfBoundsException(Integer.toString(index));
  }

  @Override
  public boolean materializeEmpty() {
    return true;
  }

  @Override
  public @NotNull Iterator<E> materializeIterator() {
    return Collections.<E>emptyList().iterator();
  }

  @Override
  public int materializeSize() {
    return 0;
  }
}
