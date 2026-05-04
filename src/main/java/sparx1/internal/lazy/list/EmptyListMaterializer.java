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
package sparx1.internal.lazy.list;

import java.util.NoSuchElementException;
import sparx1.internal.lazy.ListMaterializer;
import sparx1.util.annotation.NotNegative;
import sparx1.util.annotation.NotNull;

public class EmptyListMaterializer<E> implements ListMaterializer<E> {

  private static final BackwardIterator BACKWARD_ITERATOR = new BackwardIterator();
  private static final ForwardIterator FORWARD_ITERATOR = new ForwardIterator();
  private static final EmptyListMaterializer<?> INSTANCE = new EmptyListMaterializer<Object>();

  private EmptyListMaterializer() {
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <E> IndexedIterator<E> backwardIterator() {
    return (IndexedIterator<E>) BACKWARD_ITERATOR;
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <E> IndexedIterator<E> forwardIterator() {
    return (IndexedIterator<E>) FORWARD_ITERATOR;
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <E> EmptyListMaterializer<E> instance() {
    return (EmptyListMaterializer<E>) INSTANCE;
  }

  @Override
  public boolean canMaterializeElement(final @NotNegative int index) {
    return false;
  }

  @Override
  public int knownSize() {
    return 0;
  }

  @Override
  public boolean isRandomAccess() {
    return true;
  }

  @Override
  public boolean isSizeKnown() {
    return true;
  }

  @Override
  public @NotNull IndexedIterator<E> materializeBackwardIterator(final @NotNegative int index) {
    return backwardIterator();
  }

  @Override
  public boolean materializeContains(final Object element) {
    return false;
  }

  @Override
  public E materializeElement(final @NotNegative int index) {
    throw new IndexOutOfBoundsException(Integer.toString(index));
  }

  @Override
  public int materializeElements() {
    return 0;
  }

  @Override
  public boolean materializeEmpty() {
    return true;
  }

  @Override
  public @NotNull IndexedIterator<E> materializeForwardIterator(final @NotNegative int index) {
    return forwardIterator();
  }

  @Override
  public int materializeSize() {
    return 0;
  }

  @Override
  public @NotNull IndexedIterator<E> materializeUnorderedIterator() {
    return forwardIterator();
  }

  private static class BackwardIterator implements IndexedIterator<Object> {

    @Override
    public boolean hasNext() {
      return false;
    }

    @Override
    public Object next() {
      throw new NoSuchElementException();
    }

    @Override
    public int nextIndex() {
      return -1;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove");
    }
  }

  private static class ForwardIterator implements IndexedIterator<Object> {

    @Override
    public boolean hasNext() {
      return false;
    }

    @Override
    public Object next() {
      throw new NoSuchElementException();
    }

    @Override
    public int nextIndex() {
      return 0;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove");
    }
  }
}
