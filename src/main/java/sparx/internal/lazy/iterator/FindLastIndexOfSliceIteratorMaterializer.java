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
import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;
import sparx.internal.lazy.list.ListMaterializer;
import sparx.util.DequeueList;
import sparx.util.annotation.Positive;

public class FindLastIndexOfSliceIteratorMaterializer<E> extends
    StatefulIteratorMaterializer<Integer> {

  public FindLastIndexOfSliceIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      @NotNull final ListMaterializer<?> elementsMaterializer) {
    setState(new ImmaterialState(wrapped, elementsMaterializer));
  }

  private class ImmaterialState implements IteratorMaterializer<Integer> {

    private final ListMaterializer<?> elementsMaterializer;
    private final IteratorMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final IteratorMaterializer<E> wrapped,
        @NotNull final ListMaterializer<?> elementsMaterializer) {
      this.wrapped = wrapped;
      this.elementsMaterializer = elementsMaterializer;
    }

    @Override
    public int knownSize() {
      return -1;
    }

    @Override
    public boolean materializeHasNext() {
      final DequeueList<E> queue = new DequeueList<E>();
      final IteratorMaterializer<E> wrapped = this.wrapped;
      final ListMaterializer<?> elementsMaterializer = this.elementsMaterializer;
      Iterator<?> elementsIterator = elementsMaterializer.materializeIterator();
      if (!elementsIterator.hasNext()) {
        setState(
            new ElementToIteratorMaterializer<Integer>(wrapped.materializeSkip(Integer.MAX_VALUE)));
        return true;
      }
      boolean found = false;
      int last = 0;
      int index = 0;
      while (wrapped.materializeHasNext()) {
        if (!elementsIterator.hasNext()) {
          found = true;
          last = index;
          elementsIterator = elementsMaterializer.materializeIterator();
          boolean matches = false;
          while (!queue.isEmpty() && !matches) {
            ++index;
            queue.pop();
            matches = true;
            elementsIterator = elementsMaterializer.materializeIterator();
            for (final E e : queue) {
              if (!wrapped.materializeHasNext()) {
                last = index - queue.size();
                elementsIterator = elementsMaterializer.materializeIterator();
                break;
              }
              final Object right = elementsIterator.next();
              if (e != right && (e == null || !e.equals(right))) {
                matches = false;
                break;
              }
            }
          }
          if (!matches) {
            elementsIterator = elementsMaterializer.materializeIterator();
          }
          ++index;
          continue;
        }
        final E left = wrapped.materializeNext();
        Object right = elementsIterator.next();
        if (left != right && (left == null || !left.equals(right))) {
          boolean matches = false;
          while (!queue.isEmpty() && !matches) {
            ++index;
            queue.pop();
            matches = true;
            elementsIterator = elementsMaterializer.materializeIterator();
            for (final E e : queue) {
              if (!wrapped.materializeHasNext()) {
                final int result = index - queue.size();
                found = true;
                last = result;
                elementsIterator = elementsMaterializer.materializeIterator();
                break;
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
          ++index;
        } else {
          queue.add(left);
        }
      }
      if (found) {
        setState(new ElementToIteratorMaterializer<Integer>(last));
        return true;
      }
      setEmptyState();
      return false;
    }

    @Override
    public Integer materializeNext() {
      if (!materializeHasNext()) {
        throw new NoSuchElementException();
      }
      return getState().materializeNext();
    }

    @Override
    public int materializeSkip(@Positive final int count) {
      if (materializeHasNext()) {
        setEmptyState();
        return 1;
      }
      return 0;
    }
  }
}
