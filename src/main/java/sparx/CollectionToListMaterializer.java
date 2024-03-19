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

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import sparx.collection.ListMaterializer;
import sparx.util.Require;

class CollectionToListMaterializer<E> implements ListMaterializer<E> {

  private static final int SIZE_THRESHOLD = 64; // TODO: need to benchmark this

  private volatile ListMaterializer<E> state;

  CollectionToListMaterializer(@NotNull final Collection<E> elements) {
    if (elements.size() > SIZE_THRESHOLD) {
      this.state = new ImmaterialState(elements.iterator(), elements.size());
    } else {
      this.state = new WrapperState(elements);
    }
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    return state.canMaterializeElement(index);
  }

  @Override
  public int knownSize() {
    return state.knownSize();
  }

  @Override
  public E materializeElement(final int index) {
    return state.materializeElement(index);
  }

  @Override
  public boolean materializeEmpty() {
    return state.materializeEmpty();
  }

  @Override
  public @NotNull Iterator<E> materializeIterator() {
    return state.materializeIterator();
  }

  @Override
  public int materializeSize() {
    return state.materializeSize();
  }

  private class ImmaterialState implements ListMaterializer<E> {

    private final ArrayList<E> elements = new ArrayList<E>();
    private final Iterator<E> iterator;
    private final int size;

    private final AtomicInteger modCount = new AtomicInteger();

    private ImmaterialState(@NotNull final Iterator<E> iterator, final int size) {
      this.iterator = Require.notNull(iterator, "iterator");
      this.size = size;
    }

    @Override
    public boolean canMaterializeElement(final int index) {
      return index >= 0 && index < size;
    }

    @Override
    public int knownSize() {
      return size;
    }

    @Override
    public E materializeElement(final int index) {
      final ArrayList<E> elements = this.elements;
      if (elements.size() <= index) {
        final AtomicInteger modCount = this.modCount;
        final int expectedCount = modCount.getAndIncrement() + 1;
        final Iterator<E> iterator = this.iterator;
        do {
          if (!iterator.hasNext()) {
            throw new IndexOutOfBoundsException(String.valueOf(index));
          }
          elements.add(iterator.next());
        } while (elements.size() <= index);
        if (expectedCount != modCount.get()) {
          throw new ConcurrentModificationException();
        }
        if (!iterator.hasNext()) {
          state = new ListToListMaterializer<E>(elements);
        }
      }
      return elements.get(index);
    }

    @Override
    public boolean materializeEmpty() {
      return size == 0;
    }

    @Override
    public @NotNull Iterator<E> materializeIterator() {
      return iterator;
    }

    @Override
    public int materializeSize() {
      return size;
    }
  }

  private class WrapperState implements ListMaterializer<E> {

    private final Collection<E> elements;

    private WrapperState(@NotNull final Collection<E> elements) {
      this.elements = Require.notNull(elements, "elements");
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
    public E materializeElement(int index) {
      final Iterator<E> iterator = elements.iterator();
      while (iterator.hasNext() && index-- > 0) {
        iterator.next();
      }
      return iterator.next();
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
}
