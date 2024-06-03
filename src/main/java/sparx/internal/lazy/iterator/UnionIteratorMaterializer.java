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

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import sparx.util.function.IndexedPredicate;

public class UnionIteratorMaterializer<E> extends AbstractIteratorMaterializer<E> {

  private volatile IteratorMaterializer<E> state;

  public UnionIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      @NotNull final IteratorMaterializer<E> elementsMaterializer) {
    state = new ImmaterialState(wrapped, elementsMaterializer);
  }

  private static @NotNull <E> IndexedPredicate<E> elementsContains(@NotNull final Set<E> elements) {
    return new IndexedPredicate<E>() {
      @Override
      public boolean test(final int index, final E element) {
        return elements.contains(element);
      }
    };
  }

  @Override
  public int knownSize() {
    return state.knownSize();
  }

  @Override
  public boolean materializeHasNext() {
    return state.materializeHasNext();
  }

  @Override
  public E materializeNext() {
    return state.materializeNext();
  }

  private class ImmaterialState implements IteratorMaterializer<E> {

    private final HashSet<E> elements = new HashSet<E>();
    private final IteratorMaterializer<E> elementsMaterializer;
    private final IteratorMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final IteratorMaterializer<E> wrapped,
        @NotNull final IteratorMaterializer<E> elementsMaterializer) {
      this.wrapped = wrapped;
      this.elementsMaterializer = elementsMaterializer;
    }

    @Override
    public int knownSize() {
      return -1;
    }

    @Override
    public boolean materializeHasNext() {
      if (wrapped.materializeHasNext()) {
        return true;
      }
      if (elements.isEmpty()) {
        return (state = elementsMaterializer).materializeHasNext();
      }
      return (state = new RemoveWhereIteratorMaterializer<E>(elementsMaterializer,
          elementsContains(elements))).materializeHasNext();
    }

    @Override
    public E materializeNext() {
      final IteratorMaterializer<E> wrapped = this.wrapped;
      if (wrapped.materializeHasNext()) {
        final E next = wrapped.materializeNext();
        elements.add(next);
        return next;
      }
      if (!materializeHasNext()) {
        throw new NoSuchElementException();
      }
      return state.materializeNext();
    }

    @Override
    public int materializeSkip(final int count) {
      throw new UnsupportedOperationException();
    }
  }
}
