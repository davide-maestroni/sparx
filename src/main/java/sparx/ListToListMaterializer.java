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
import java.util.List;
import org.jetbrains.annotations.NotNull;
import sparx.collection.ListMaterializer;

class ListToListMaterializer<E> implements ListMaterializer<E> {

  private final List<E> elements;

  ListToListMaterializer(@NotNull final List<E> elements) {
    this.elements = elements;
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    return index >= 0 && index < elements.size();
  }

  @Override
  public int knownSize() {
    return elements.size();
  }

  @Override
  public E materializeElement(final int index) {
    return elements.get(index);
  }

  @Override
  public boolean materializeEmpty() {
    return elements.isEmpty();
  }

  @Override
  public @NotNull Iterator<E> materializeIterator() {
    return elements.iterator();
  }

  @Override
  public int materializeSize() {
    return elements.size();
  }
}
