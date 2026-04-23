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

import java.util.Collections;
import java.util.Iterator;
import sparx1.internal.lazy.ListMaterializer;
import sparx1.util.annotation.NotNegative;
import sparx1.util.annotation.NotNull;
import sparx1.util.function.Functions;

public abstract class AbstractListMaterializer<E> implements ListMaterializer<E> {

  @Override
  public @NotNull Iterator<E> materializeBackwardIterator(final @NotNegative int index) {
    if (!canMaterializeElement(index)) {
      return Collections.<E>emptyList().iterator();
    }
    return new BackwardIterator<E>(this, index);
  }

  @Override
  public boolean materializeContains(final Object element) {
    final Iterator<E> iterator = materializeUnorderedIterator();
    while (iterator.hasNext()) {
      if (Functions.objectsEqual(element, iterator.next())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public @NotNull Iterator<E> materializeForwardIterator(final @NotNegative int index) {
    if (!canMaterializeElement(index)) {
      return Collections.<E>emptyList().iterator();
    }
    return new ForwardIterator<E>(this, index);
  }

  @Override
  public @NotNull Iterator<E> materializeUnorderedIterator() {
    return materializeForwardIterator(0);
  }
}
