/*
 * Copyright 2026 Davide Maestroni
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
package sparx1.internal.lazy.iterator;

import java.util.NoSuchElementException;
import sparx1.internal.lazy.IteratorMaterializer;
import sparx1.util.DequeArrayList;
import sparx1.util.SizeOverflowException;
import sparx1.util.UncheckedException;
import sparx1.util.annotation.NotNull;
import sparx1.util.function.IndexedPredicate;

public class RemoveLastIteratorMaterializer<E> extends StatefulIteratorMaterializer<E> {

  public RemoveLastIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      @NotNull final IndexedPredicate<? super E> predicate) {
    setState(new InitialState(wrapped, predicate));
  }

  private class InitialState extends AbstractStateIteratorMaterializer {

    private final IndexedPredicate<? super E> predicate;
    private final IteratorMaterializer<E> wrapped;

    private boolean hasNext;
    private E next;
    private int pos;

    private InitialState(@NotNull final IteratorMaterializer<E> wrapped,
        @NotNull final IndexedPredicate<? super E> predicate) {
      this.wrapped = wrapped;
      this.predicate = predicate;
    }

    @Override
    public int currentKnownSize() {
      return -1;
    }

    @Override
    public boolean isSizeKnown() {
      return false;
    }

    @Override
    public boolean materializeHasNext() {
      if (hasNext) {
        return true;
      }
      final IteratorMaterializer<E> wrapped = this.wrapped;
      final IndexedPredicate<? super E> predicate = this.predicate;
      if (wrapped.materializeHasNext()) {
        final int pos = this.pos;
        final E next = wrapped.materializeNext();
        this.pos++;
        try {
          if (predicate.test(pos, next)) {
            return setState(new FoundState(wrapped, predicate, pos + 1, next)).materializeHasNext();
          }
          hasNext = true;
          this.next = next;
          return true;
        } catch (final Exception e) {
          throw UncheckedException.throwUnchecked(e);
        }
      }
      setEmptyState();
      return false;
    }

    @Override
    public E materializeNext() {
      if (hasNext) {
        return next;
      }
      if (!materializeHasNext()) {
        throw new NoSuchElementException();
      }
      return getState().materializeNext();
    }
  }

  private class FoundState extends AbstractStateIteratorMaterializer {

    private final DequeArrayList<E> elements = new DequeArrayList<E>(true);
    private final IndexedPredicate<? super E> predicate;
    private final IteratorMaterializer<E> wrapped;

    private int pos;

    private FoundState(@NotNull final IteratorMaterializer<E> wrapped,
        @NotNull final IndexedPredicate<? super E> predicate, final int pos, final E element) {
      this.wrapped = wrapped;
      this.predicate = predicate;
      this.pos = pos;
      elements.add(element);
    }

    @Override
    public int currentKnownSize() {
      final int knownSize = wrapped.currentKnownSize();
      if (knownSize >= 0) {
        return SizeOverflowException.safeCast(knownSize + elements.size() - 1L);
      }
      return -1;
    }

    @Override
    public boolean isSizeKnown() {
      return wrapped.isSizeKnown();
    }

    @Override
    public boolean materializeHasNext() {
      final DequeArrayList<E> elements = this.elements;
      if (elements.size() > 1) {
        return true;
      }
      final IteratorMaterializer<E> wrapped = this.wrapped;
      final IndexedPredicate<? super E> predicate = this.predicate;
      while (wrapped.materializeHasNext()) {
        final int pos = this.pos;
        final E next = wrapped.materializeNext();
        this.pos++;
        elements.add(next);
        try {
          if (predicate.test(pos, next)) {
            elements.removeFirst();
            if (elements.size() > 1) {
              return true;
            }
          }
        } catch (final Exception e) {
          throw UncheckedException.throwUnchecked(e);
        }
      }
      if (elements.size() > 1) {
        elements.removeFirst();
        setState(new DequeToIteratorMaterializer<E>(elements));
        return true;
      }
      setEmptyState();
      return false;
    }

    @Override
    public E materializeNext() {
      if (!materializeHasNext()) {
        throw new NoSuchElementException();
      }
      return elements.removeFirst();
    }
  }
}
