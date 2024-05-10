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

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import sparx.util.Require;
import sparx.util.UncheckedException;
import sparx.util.function.IndexedFunction;
import sparx.util.function.IndexedPredicate;

public class SingleMapWhereListMaterializer<E> implements ListMaterializer<E> {

  private volatile ListMaterializer<E> state;

  public SingleMapWhereListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      @NotNull final IndexedPredicate<? super E> predicate,
      @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
    state = new ImmaterialState(Require.notNull(wrapped, "wrapped"),
        Require.notNull(predicate, "predicate"), Require.notNull(mapper, "mapper"));
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

    private final AtomicBoolean isMaterialized = new AtomicBoolean(false);
    private final IndexedFunction<? super E, ? extends E> mapper;
    private final IndexedPredicate<? super E> predicate;
    private final ListMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final ListMaterializer<E> wrapped,
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      this.wrapped = wrapped;
      this.predicate = predicate;
      this.mapper = mapper;
    }

    @Override
    public boolean canMaterializeElement(final int index) {
      return index == 0;
    }

    @Override
    public int knownSize() {
      return 1;
    }

    @Override
    public boolean materializeContains(final Object element) {
      if (!isMaterialized.compareAndSet(false, true)) {
        throw new ConcurrentModificationException();
      }
      try {
        E first = wrapped.materializeElement(0);
        if (predicate.test(0, first)) {
          first = mapper.apply(0, first);
        }
        state = new ElementToListMaterializer<E>(first);
        return element == first || (element != null && element.equals(first));
      } catch (final Exception e) {
        isMaterialized.set(false);
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public E materializeElement(final int index) {
      if (index != 0) {
        throw new IndexOutOfBoundsException(Integer.toString(index));
      }
      if (!isMaterialized.compareAndSet(false, true)) {
        throw new ConcurrentModificationException();
      }
      try {
        E element = wrapped.materializeElement(0);
        if (predicate.test(0, element)) {
          element = mapper.apply(0, element);
        }
        state = new ElementToListMaterializer<E>(element);
        return element;
      } catch (final Exception e) {
        isMaterialized.set(false);
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public int materializeElements() {
      materializeElement(0);
      return 1;
    }

    @Override
    public boolean materializeEmpty() {
      return wrapped.materializeEmpty();
    }

    @Override
    public @NotNull Iterator<E> materializeIterator() {
      return new ListMaterializerIterator<E>(this);
    }

    @Override
    public int materializeSize() {
      return 1;
    }
  }
}
