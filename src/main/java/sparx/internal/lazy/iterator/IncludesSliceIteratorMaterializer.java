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

import java.util.Iterator;
import org.jetbrains.annotations.NotNull;
import sparx.internal.lazy.list.ListMaterializer;
import sparx.util.DequeueList;

public class IncludesSliceIteratorMaterializer<E> extends StatefulIteratorMaterializer<Boolean> {

  public IncludesSliceIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      @NotNull final ListMaterializer<?> elementsMaterializer) {
    setState(new ImmaterialState(wrapped, elementsMaterializer));
  }

  private class ImmaterialState implements IteratorMaterializer<Boolean> {

    private final ListMaterializer<?> elementsMaterializer;
    private final IteratorMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final IteratorMaterializer<E> wrapped,
        @NotNull final ListMaterializer<?> elementsMaterializer) {
      this.wrapped = wrapped;
      this.elementsMaterializer = elementsMaterializer;
    }

    @Override
    public int knownSize() {
      return 1;
    }

    @Override
    public boolean materializeHasNext() {
      return true;
    }

    @Override
    public Boolean materializeNext() {
      final DequeueList<E> queue = new DequeueList<E>();
      final ListMaterializer<?> elementsMaterializer = this.elementsMaterializer;
      Iterator<?> elementsIterator = elementsMaterializer.materializeIterator();
      if (!elementsIterator.hasNext()) {
        setEmptyState();
        return true;
      }
      final IteratorMaterializer<E> wrapped = this.wrapped;
      while (wrapped.materializeHasNext()) {
        if (!elementsIterator.hasNext()) {
          setEmptyState();
          return true;
        }
        final E left = wrapped.materializeNext();
        Object right = elementsIterator.next();
        if (left != right && (left == null || !left.equals(right))) {
          boolean matches = false;
          while (!queue.isEmpty() && !matches) {
            queue.pop();
            matches = true;
            elementsIterator = elementsMaterializer.materializeIterator();
            for (final E e : queue) {
              if (!wrapped.materializeHasNext()) {
                setEmptyState();
                return true;
              }
              right = elementsIterator.next();
              if (e != right && (e == null || !e.equals(right))) {
                matches = false;
                break;
              }
            }
          }
          if (!matches) {
            elementsIterator = elementsMaterializer.materializeIterator();
          }
        } else {
          queue.add(left);
        }
      }
      setEmptyState();
      return false;
    }

    @Override
    public int materializeSkip(final int count) {
      if (count > 0) {
        setEmptyState();
        return 1;
      }
      return 0;
    }

    @Override
    public int nextIndex() {
      return -1;
    }
  }
}
