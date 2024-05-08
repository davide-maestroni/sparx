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
package sparx.collection.internal.lazy.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import sparx.util.Require;

public class CollectionToListMaterializer<E> implements ListMaterializer<E> {

  private static final int SIZE_THRESHOLD = 64; // TODO: need to benchmark this

  private volatile ListMaterializer<E> state;

  public CollectionToListMaterializer(@NotNull final Collection<E> elements) {
    if (elements.size() > SIZE_THRESHOLD) {
      this.state = new ImmaterialState(Require.notNull(elements, "elements"));
    } else {
      this.state = new WrapperState(Require.notNull(elements, "elements"));
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
  public boolean materializeContains(final Object element) {
    return state.materializeContains(element);
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

    private final Collection<E> elements;
    private final ArrayList<E> elementsList = new ArrayList<E>();
    private final Iterator<E> iterator;

    private final AtomicInteger modCount = new AtomicInteger();

    private ImmaterialState(@NotNull final Collection<E> elements) {
      this.elements = elements;
      iterator = elements.iterator();
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
    @SuppressWarnings("SuspiciousMethodCalls")
    public boolean materializeContains(final Object element) {
      return elements.contains(element);
    }

    @Override
    public E materializeElement(final int index) {
      final ArrayList<E> elements = this.elementsList;
      if (elements.size() <= index) {
        final AtomicInteger modCount = this.modCount;
        final int expectedCount = modCount.incrementAndGet();
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
      }
      return elements.get(index);
    }

    @Override
    public boolean materializeEmpty() {
      return elements.isEmpty();
    }

    @Override
    public @NotNull Iterator<E> materializeIterator() {
      return iterator;
    }

    @Override
    public int materializeSize() {
      return elements.size();
    }
  }

  private class WrapperState implements ListMaterializer<E> {

    private final Collection<E> elements;

    private WrapperState(@NotNull final Collection<E> elements) {
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
    @SuppressWarnings("SuspiciousMethodCalls")
    public boolean materializeContains(final Object element) {
      return elements.contains(element);
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
