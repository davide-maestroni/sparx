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
package sparx1.internal.lazy.iterator;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import sparx1.internal.lazy.IteratorMaterializer;
import sparx1.util.annotation.NotNull;

public class UnionIteratorMaterializer<E> extends StatefulIteratorMaterializer<E> {

  public UnionIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      final @NotNull IteratorMaterializer<E> elementsMaterializer) {
    setState(new InitialState(wrapped, elementsMaterializer));
  }

  private class InitialState extends AbstractStateIteratorMaterializer {

    private final ArrayList<E> elements = new ArrayList<E>();
    private final IteratorMaterializer<E> elementsMaterializer;
    private final IteratorMaterializer<E> wrapped;

    private InitialState(final @NotNull IteratorMaterializer<E> wrapped,
        final @NotNull IteratorMaterializer<E> elementsMaterializer) {
      this.wrapped = wrapped;
      this.elementsMaterializer = elementsMaterializer;
    }

    @Override
    public int currentKnownSize() {
      return -1;
    }

    @Override
    public boolean isSizeKnown() {
      return false;
    }

    @Override
    public boolean materializeHasNext() {
      if (wrapped.materializeHasNext()) {
        return true;
      }
      if (elements.isEmpty()) {
        return setState(elementsMaterializer).materializeHasNext();
      }
      return setState(new DiffIteratorMaterializer<E>(elementsMaterializer,
          new ListToIteratorMaterializer<E>(elements))).materializeHasNext();
    }

    @Override
    public E materializeNext() {
      if (!getState().materializeHasNext()) {
        throw new NoSuchElementException();
      }
      final IteratorMaterializer<E> state = getState();
      if (state == this) {
        final E next = wrapped.materializeNext();
        elements.add(next);
        return next;
      }
      return state.materializeNext();
    }
  }
}
