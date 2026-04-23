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

import java.util.Iterator;
import sparx1.internal.lazy.ListMaterializer;
import sparx1.util.UncheckedException;
import sparx1.util.annotation.NotNegative;
import sparx1.util.annotation.NotNull;
import sparx1.util.function.Supplier;

public abstract class SuppliedListMaterializer<E> extends StatefulListMaterializer<E> implements
    Supplier<ListMaterializer<E>> {

  public SuppliedListMaterializer() {
    setState(new InitialState());
  }

  private class InitialState implements ListMaterializer<E> {

    @Override
    public boolean canMaterializeElement(final @NotNegative int index) {
      return materialize().canMaterializeElement(index);
    }

    @Override
    public int knownSize() {
      return -1;
    }

    @Override
    public boolean isRandomAccess() {
      return false;
    }

    @Override
    public boolean isSizeKnown() {
      return false;
    }

    @Override
    public Iterator<E> materializeBackwardIterator(final @NotNegative int index) {
      return materialize().materializeBackwardIterator(index);
    }

    @Override
    public boolean materializeContains(final Object element) {
      return materialize().materializeContains(element);
    }

    @Override
    public E materializeElement(final @NotNegative int index) {
      return materialize().materializeElement(index);
    }

    @Override
    public int materializeElements() {
      return materialize().materializeElements();
    }

    @Override
    public boolean materializeEmpty() {
      return materialize().materializeEmpty();
    }

    @Override
    public Iterator<E> materializeForwardIterator(final @NotNegative int index) {
      return materialize().materializeForwardIterator(index);
    }

    @Override
    public int materializeSize() {
      return materialize().materializeSize();
    }

    @Override
    public Iterator<E> materializeUnorderedIterator() {
      return materialize().materializeUnorderedIterator();
    }

    private @NotNull ListMaterializer<E> materialize() {
      try {
        return setState(get());
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }
}
