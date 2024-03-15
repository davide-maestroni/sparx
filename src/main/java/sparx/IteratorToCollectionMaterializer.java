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
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import sparx.util.CollectionMaterializer;
import sparx.util.Require;

class IteratorToCollectionMaterializer<E> implements CollectionMaterializer<E> {

  private volatile CollectionMaterializer<E> state;

  IteratorToCollectionMaterializer(@NotNull final Iterator<E> iterator) {
    this.state = new ImmaterialState(iterator);
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    return state.canMaterializeElement(index);
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

  private class ImmaterialState implements CollectionMaterializer<E> {

    private final ArrayList<E> elements = new ArrayList<E>();
    private final Iterator<E> iterator;

    private final AtomicInteger modCount = new AtomicInteger();

    private ImmaterialState(@NotNull final Iterator<E> iterator) {
      this.iterator = Require.notNull(iterator, "iterator");
    }

    @Override
    public boolean canMaterializeElement(final int index) {
      final ArrayList<E> elements = this.elements;
      if (elements.size() <= index) {
        final AtomicInteger modCount = this.modCount;
        final int expectedCount = modCount.getAndIncrement() + 1;
        final Iterator<E> iterator = this.iterator;
        do {
          if (!iterator.hasNext()) {
            return false;
          }
          elements.add(iterator.next());
        } while (elements.size() <= index);
        if (expectedCount != modCount.get()) {
          throw new ConcurrentModificationException();
        }
        if (!iterator.hasNext()) {
          state = new ListToCollectionMaterializer<E>(elements);
        }
      }
      return true;
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
          state = new ListToCollectionMaterializer<E>(elements);
        }
      }
      return elements.get(index);
    }

    @Override
    public boolean materializeEmpty() {
      if (elements.isEmpty()) {
        return !iterator.hasNext();
      }
      return false;
    }

    @Override
    public @NotNull Iterator<E> materializeIterator() {
      return iterator;
    }

    @Override
    public int materializeSize() {
      final ArrayList<E> elements = this.elements;
      final AtomicInteger modCount = this.modCount;
      final int expectedCount = modCount.getAndIncrement() + 1;
      final Iterator<E> iterator = this.iterator;
      while (iterator.hasNext()) {
        elements.add(iterator.next());
      }
      if (expectedCount != modCount.get()) {
        throw new ConcurrentModificationException();
      }
      state = new ListToCollectionMaterializer<E>(elements);
      return elements.size();
    }
  }
}