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
import sparx.collection.CollectionMaterializer;
import sparx.util.Require;
import sparx.util.UncheckedException;
import sparx.util.function.Predicate;

class FilterCollectionMaterializer<E> implements CollectionMaterializer<E> {

  private volatile CollectionMaterializer<E> state;

  FilterCollectionMaterializer(@NotNull final CollectionMaterializer<E> wrapped,
      @NotNull final Predicate<? super E> predicate) {
    state = new ImmaterialState(Require.notNull(wrapped, "wrapped"),
        Require.notNull(predicate, "predicate"));
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    return state.canMaterializeElement(index);
  }

  @Override
  public int knownSize() {
    final int wrappedSize = state.knownSize();
    if (wrappedSize == 0) {
      return 0;
    }
    return -1;
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

  private static class ExceptionState<E> implements CollectionMaterializer<E> {

    private final Exception ex;

    private ExceptionState(@NotNull final Exception ex) {
      this.ex = ex;
    }

    @Override
    public boolean canMaterializeElement(final int index) {
      throw UncheckedException.throwUnchecked(ex);
    }

    @Override
    public int knownSize() {
      throw UncheckedException.throwUnchecked(ex);
    }

    @Override
    public E materializeElement(final int index) {
      throw UncheckedException.throwUnchecked(ex);
    }

    @Override
    public boolean materializeEmpty() {
      throw UncheckedException.throwUnchecked(ex);
    }

    @Override
    public @NotNull Iterator<E> materializeIterator() {
      throw UncheckedException.throwUnchecked(ex);
    }

    @Override
    public int materializeSize() {
      throw UncheckedException.throwUnchecked(ex);
    }
  }

  private class ImmaterialState implements CollectionMaterializer<E> {

    private final ArrayList<E> elements = new ArrayList<E>();
    private final Iterator<E> iterator;
    private final int knownSize;
    private final AtomicInteger modCount = new AtomicInteger();
    private final Predicate<? super E> predicate;

    private ImmaterialState(@NotNull final CollectionMaterializer<E> wrapped,
        @NotNull final Predicate<? super E> predicate) {
      iterator = wrapped.materializeIterator();
      knownSize = wrapped.knownSize();
      this.predicate = predicate;
    }

    @Override
    public boolean canMaterializeElement(final int index) {
      final ArrayList<E> elements = this.elements;
      if (elements.size() <= index) {
        final AtomicInteger modCount = this.modCount;
        final int expectedCount = modCount.getAndIncrement() + 1;
        final Predicate<? super E> predicate = this.predicate;
        try {
          final Iterator<E> iterator = this.iterator;
          do {
            if (!iterator.hasNext()) {
              return false;
            }
            final E next = iterator.next();
            if (predicate.test(next)) {
              elements.add(next);
            }
          } while (elements.size() <= index);
          if (expectedCount != modCount.get()) {
            throw new ConcurrentModificationException();
          }
          if (!iterator.hasNext()) {
            state = new ListToCollectionMaterializer<E>(elements);
          }
        } catch (final Exception e) {
          state = new ExceptionState<E>(e);
          throw UncheckedException.throwUnchecked(e);
        }
      }
      return true;
    }

    @Override
    public int knownSize() {
      return knownSize;
    }

    @Override
    public E materializeElement(final int index) {
      final ArrayList<E> elements = this.elements;
      if (elements.size() <= index) {
        final AtomicInteger modCount = this.modCount;
        final int expectedCount = modCount.getAndIncrement() + 1;
        final Predicate<? super E> predicate = this.predicate;
        try {
          final Iterator<E> iterator = this.iterator;
          do {
            if (!iterator.hasNext()) {
              throw new IndexOutOfBoundsException(String.valueOf(index));
            }
            final E next = iterator.next();
            if (predicate.test(next)) {
              elements.add(next);
            }
          } while (elements.size() <= index);
          if (expectedCount != modCount.get()) {
            throw new ConcurrentModificationException();
          }
          if (!iterator.hasNext()) {
            state = new ListToCollectionMaterializer<E>(elements);
          }
        } catch (final Exception e) {
          state = new ExceptionState<E>(e);
          throw UncheckedException.throwUnchecked(e);
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
      return new CollectionMaterializerIterator<E>(this);
    }

    @Override
    public int materializeSize() {
      final ArrayList<E> elements = this.elements;
      final AtomicInteger modCount = this.modCount;
      final int expectedCount = modCount.getAndIncrement() + 1;
      final Predicate<? super E> predicate = this.predicate;
      try {
        final Iterator<E> iterator = this.iterator;
        while (iterator.hasNext()) {
          final E next = iterator.next();
          if (predicate.test(next)) {
            elements.add(next);
          }
        }
      } catch (final Exception e) {
        state = new ExceptionState<E>(e);
        throw UncheckedException.throwUnchecked(e);
      }
      if (expectedCount != modCount.get()) {
        throw new ConcurrentModificationException();
      }
      state = new ListToCollectionMaterializer<E>(elements);
      return elements.size();
    }
  }
}
