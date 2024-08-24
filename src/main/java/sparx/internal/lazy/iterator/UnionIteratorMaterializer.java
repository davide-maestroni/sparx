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
package sparx.internal.lazy.iterator;

import java.util.ArrayList;
import org.jetbrains.annotations.NotNull;

public class UnionIteratorMaterializer<E> extends StatefulIteratorMaterializer<E> {

  public UnionIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      @NotNull final IteratorMaterializer<E> elementsMaterializer) {
    setState(new ImmaterialState(wrapped, elementsMaterializer));
  }

  private class ImmaterialState extends AutoSkipIteratorMaterializer<E> {

    private final ArrayList<E> elements = new ArrayList<E>();
    private final IteratorMaterializer<E> elementsMaterializer;
    private final IteratorMaterializer<E> wrapped;


    private ImmaterialState(@NotNull final IteratorMaterializer<E> wrapped,
        @NotNull final IteratorMaterializer<E> elementsMaterializer) {
      this.wrapped = wrapped;
      this.elementsMaterializer = elementsMaterializer;
    }

    @Override
    public int knownSize() {
      return -1;
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
      final E next = wrapped.materializeNext();
      elements.add(next);
      return next;
    }

    @Override
    public int nextIndex() {
      return -1;
    }
  }
}
