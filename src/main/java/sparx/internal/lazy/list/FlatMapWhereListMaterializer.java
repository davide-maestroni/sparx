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
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import sparx.util.UncheckedException;
import sparx.util.function.IndexedFunction;
import sparx.util.function.IndexedPredicate;

public class FlatMapWhereListMaterializer<E> implements ListMaterializer<E> {

  private volatile ListMaterializer<E> state;

  public FlatMapWhereListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      @NotNull final IndexedPredicate<? super E> predicate,
      @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
    state = new ImmaterialState(wrapped, predicate, mapper);
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

  private class ImmaterialState implements ListMaterializer<E> {

    private final ArrayList<E> elements = new ArrayList<E>();
    private final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper;
    private final AtomicInteger modCount = new AtomicInteger();
    private final IndexedPredicate<? super E> predicate;
    private final ListMaterializer<E> wrapped;

    private Iterator<? extends E> elementIterator = Collections.<E>emptySet().iterator();
    private int pos;

    private ImmaterialState(@NotNull final ListMaterializer<E> wrapped,
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      this.wrapped = wrapped;
      this.mapper = mapper;
      this.predicate = predicate;
    }

    @Override
    public boolean canMaterializeElement(final int index) {
      return index >= 0 && materializeUntil(index) > index;
    }

    @Override
    public int knownSize() {
      return -1;
    }

    @Override
    @SuppressWarnings({"SuspiciousMethodCalls", "unchecked"})
    public boolean materializeContains(final Object element) {
      final ArrayList<E> elements = this.elements;
      if (elements.contains(element)) {
        return true;
      }
      final ListMaterializer<E> wrapped = this.wrapped;
      final IndexedPredicate<? super E> predicate = this.predicate;
      final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper = this.mapper;
      final AtomicInteger modCount = this.modCount;
      final int expectedCount = modCount.incrementAndGet();
      try {
        Iterator<? extends E> elementIterator = this.elementIterator;
        int i = pos;
        if (element == null) {
          while (true) {
            while (elementIterator.hasNext()) {
              final E next = elementIterator.next();
              elements.add(next);
              if (next == null) {
                if (expectedCount != modCount.get()) {
                  throw new ConcurrentModificationException();
                }
                pos = i;
                this.elementIterator = elementIterator;
                return true;
              }
            }
            if (wrapped.canMaterializeElement(i)) {
              final E next = wrapped.materializeElement(i);
              if (predicate.test(i, next)) {
                final Iterable<? extends E> mapping = mapper.apply(i, next);
                elementIterator = mapping.iterator();
                if (mapping instanceof Collection) {
                  if (((Collection<E>) mapping).contains(null)) {
                    return true;
                  }
                }
              } else {
                elements.add(next);
                if (next == null) {
                  if (expectedCount != modCount.get()) {
                    throw new ConcurrentModificationException();
                  }
                  pos = i;
                  this.elementIterator = elementIterator;
                  return true;
                }
              }
              ++i;
            } else {
              if (expectedCount != modCount.get()) {
                throw new ConcurrentModificationException();
              }
              state = new ListToListMaterializer<E>(elements);
              return false;
            }
          }
        } else {
          while (true) {
            while (elementIterator.hasNext()) {
              final E next = elementIterator.next();
              elements.add(next);
              if (element.equals(next)) {
                if (expectedCount != modCount.get()) {
                  throw new ConcurrentModificationException();
                }
                pos = i;
                this.elementIterator = elementIterator;
                return true;
              }
            }
            if (wrapped.canMaterializeElement(i)) {
              final E next = wrapped.materializeElement(i);
              if (predicate.test(i, next)) {
                final Iterable<? extends E> mapping = mapper.apply(i, next);
                elementIterator = mapping.iterator();
                if (mapping instanceof Collection) {
                  if (((Collection<E>) mapping).contains(null)) {
                    return true;
                  }
                }
              } else {
                elements.add(next);
                if (element.equals(next)) {
                  if (expectedCount != modCount.get()) {
                    throw new ConcurrentModificationException();
                  }
                  pos = i;
                  this.elementIterator = elementIterator;
                  return true;
                }
              }
              ++i;
            } else {
              if (expectedCount != modCount.get()) {
                throw new ConcurrentModificationException();
              }
              state = new ListToListMaterializer<E>(elements);
              return false;
            }
          }
        }
      } catch (final Exception e) {
        state = new FailedListMaterializer<E>(e);
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public E materializeElement(final int index) {
      if (index < 0 || materializeUntil(index) <= index) {
        throw new IndexOutOfBoundsException(Integer.toString(index));
      }
      return elements.get(index);
    }

    @Override
    public int materializeElements() {
      return materializeUntil(Integer.MAX_VALUE);
    }

    @Override
    public boolean materializeEmpty() {
      return materializeUntil(0) < 1;
    }

    @Override
    public @NotNull Iterator<E> materializeIterator() {
      return new ListMaterializerIterator<E>(this);
    }

    @Override
    public int materializeSize() {
      return materializeUntil(Integer.MAX_VALUE);
    }

    private int materializeUntil(final int index) {
      final ArrayList<E> elements = this.elements;
      int currSize = elements.size();
      if (currSize > index) {
        return currSize;
      }
      final ListMaterializer<E> wrapped = this.wrapped;
      final IndexedPredicate<? super E> predicate = this.predicate;
      final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper = this.mapper;
      final AtomicInteger modCount = this.modCount;
      final int expectedCount = modCount.incrementAndGet();
      try {
        Iterator<? extends E> elementIterator = this.elementIterator;
        int i = pos;
        while (true) {
          while (elementIterator.hasNext()) {
            elements.add(elementIterator.next());
            if (++currSize > index) {
              if (expectedCount != modCount.get()) {
                throw new ConcurrentModificationException();
              }
              pos = i;
              this.elementIterator = elementIterator;
              return currSize;
            }
          }
          if (wrapped.canMaterializeElement(i)) {
            final E element = wrapped.materializeElement(i);
            if (predicate.test(i, element)) {
              elementIterator = mapper.apply(i, element).iterator();
            } else {
              elements.add(element);
              if (++currSize > index) {
                if (expectedCount != modCount.get()) {
                  throw new ConcurrentModificationException();
                }
                pos = i + 1;
                this.elementIterator = elementIterator;
                return currSize;
              }
            }
            ++i;
          } else {
            if (expectedCount != modCount.get()) {
              throw new ConcurrentModificationException();
            }
            state = new ListToListMaterializer<E>(elements);
            return currSize;
          }
        }
      } catch (final Exception e) {
        state = new FailedListMaterializer<E>(e);
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }
}
