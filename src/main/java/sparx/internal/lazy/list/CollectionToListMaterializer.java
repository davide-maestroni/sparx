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
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import sparx.util.annotation.NotNegative;

public class CollectionToListMaterializer<E> implements ListMaterializer<E> {

  private static final int SIZE_THRESHOLD = 64; // TODO: need to benchmark this

  private volatile ListMaterializer<E> state;

  public CollectionToListMaterializer(@NotNull final Collection<E> elements) {
    if (elements.size() > SIZE_THRESHOLD) {
      this.state = new ImmaterialState(elements);
    } else {
      this.state = new WrapperState(elements);
    }
  }

  @Override
  public boolean canMaterializeElement(@NotNegative final int index) {
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
  public E materializeElement(@NotNegative final int index) {
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
    public boolean canMaterializeElement(@NotNegative final int index) {
      return index < elements.size();
    }

    @Override
    public int knownSize() {
      return elements.size();
    }

    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    public boolean materializeContains(final Object element) {
      try {
        return elements.contains(element);
      } catch (final NullPointerException e) {
        // some collections do not support null elements
        if (element == null) {
          return false;
        }
        throw e;
      }
    }

    @Override
    public E materializeElement(@NotNegative final int index) {
      final ArrayList<E> elements = this.elementsList;
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
      if (elements.isEmpty()) {
        return 0;
      }
      final int size = elements.size();
      materializeElement(size - 1);
      return size;
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
    public boolean canMaterializeElement(@NotNegative final int index) {
      return index < elements.size();
    }

    @Override
    public int knownSize() {
      return elements.size();
    }

    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    public boolean materializeContains(final Object element) {
      try {
        return elements.contains(element);
      } catch (final NullPointerException e) {
        // some collections do not support null elements
        if (element == null) {
          return false;
        }
        throw e;
      }
    }

    @Override
    public E materializeElement(@NotNegative int index) {
      final Iterator<E> iterator = elements.iterator();
      while (iterator.hasNext() && index-- > 0) {
        iterator.next();
      }
      try {
        return iterator.next();
      } catch (final NoSuchElementException ignored) {
        throw new IndexOutOfBoundsException(Integer.toString(index));
      }
    }

    @Override
    public int materializeElements() {
      return elements.size();
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
