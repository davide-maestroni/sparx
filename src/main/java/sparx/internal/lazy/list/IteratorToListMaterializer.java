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
package sparx.internal.lazy.list;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;

public class IteratorToListMaterializer<E> implements ListMaterializer<E> {

  private volatile ListMaterializer<E> state;

  public IteratorToListMaterializer(@NotNull final Iterator<E> iterator) {
    this.state = new ImmaterialState(iterator);
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
  public boolean materializeContains(final Object element) {
    return state.materializeContains(element);
  }

  @Override
  public E materializeElement(final int index) {
    return state.materializeElement(index);
  }

  @Override
  public int materializeElements() {
    return state.materializeElements();
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

  private class ImmaterialState extends AbstractListMaterializer<E> implements ListMaterializer<E> {

    private final ArrayList<E> elements = new ArrayList<E>();
    private final Iterator<E> iterator;

    private final AtomicInteger modCount = new AtomicInteger();

    private ImmaterialState(@NotNull final Iterator<E> iterator) {
      this.iterator = iterator;
    }

    @Override
    public boolean canMaterializeElement(final int index) {
      final ArrayList<E> elements = this.elements;
      if (elements.size() <= index) {
        final AtomicInteger modCount = this.modCount;
        final int expectedCount = modCount.incrementAndGet();
        try {
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
            state = new ListToListMaterializer<E>(elements);
          }
        } catch (final RuntimeException e) {
          state = new FailedListMaterializer<E>(e);
          throw e;
        }
      }
      return true;
    }

    @Override
    public int knownSize() {
      return -1;
    }

    @Override
    public E materializeElement(final int index) {
      final ArrayList<E> elements = this.elements;
      if (elements.size() <= index) {
        final AtomicInteger modCount = this.modCount;
        final int expectedCount = modCount.incrementAndGet();
        try {
          final Iterator<E> iterator = this.iterator;
          do {
            if (!iterator.hasNext()) {
              throw new IndexOutOfBoundsException(Integer.toString(index));
            }
            elements.add(iterator.next());
          } while (elements.size() <= index);

          if (expectedCount != modCount.get()) {
            throw new ConcurrentModificationException();
          }
          if (!iterator.hasNext()) {
            state = new ListToListMaterializer<E>(elements);
          }
        } catch (final RuntimeException e) {
          state = new FailedListMaterializer<E>(e);
          throw e;
        }
      }
      return elements.get(index);
    }

    @Override
    public int materializeElements() {
      return materializeSize();
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
      return new ListMaterializerIterator<E>(this);
    }

    @Override
    public int materializeSize() {
      final ArrayList<E> elements = this.elements;
      final AtomicInteger modCount = this.modCount;
      final int expectedCount = modCount.incrementAndGet();
      try {
        final Iterator<E> iterator = this.iterator;
        while (iterator.hasNext()) {
          elements.add(iterator.next());
        }
        if (expectedCount != modCount.get()) {
          throw new ConcurrentModificationException();
        }
        state = new ListToListMaterializer<E>(elements);
        return elements.size();
      } catch (final RuntimeException e) {
        state = new FailedListMaterializer<E>(e);
        throw e;
      }
    }
  }
}
