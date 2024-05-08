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
package sparx.collection;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Iterator;
import org.jetbrains.annotations.NotNull;
import sparx.collection.internal.lazy.list.ListMaterializer;
import sparx.util.Require;

public abstract class AbstractListSequence<E> extends AbstractList<E> implements ListSequence<E> {

  private final ListMaterializer<E> materializer;

  public AbstractListSequence(@NotNull final ListMaterializer<E> materializer) {
    this.materializer = Require.notNull(materializer, "materializer");
  }

  @Override
  public boolean add(final E e) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(@NotNull final Collection<? extends E> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(final int index, final @NotNull Collection<? extends E> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean contains(final Object o) {
    return materializer.materializeContains(o);
  }

  @Override
  public E first() {
    return materializer.materializeElement(0);
  }

  @Override
  public E get(final int index) {
    return materializer.materializeElement(index);
  }

  @Override
  public int indexOf(final Object o) {
    final Iterator<E> iterator = materializer.materializeIterator();
    int index = 0;
    if (o == null) {
      while (iterator.hasNext()) {
        if (iterator.next() == null) {
          return index;
        }
        ++index;
      }
    } else {
      while (iterator.hasNext()) {
        if (o.equals(iterator.next())) {
          return index;
        }
        ++index;
      }
    }
    return -1;
  }

  @Override
  public boolean isEmpty() {
    return materializer.materializeEmpty();
  }

  @Override
  public @NotNull Iterator<E> iterator() {
    return materializer.materializeIterator();
  }

  @Override
  public E last() {
    return materializer.materializeElement(size() - 1);
  }

  @Override
  public boolean notEmpty() {
    return !isEmpty();
  }

  @Override
  public E remove(final int index) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean remove(final Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAll(@NotNull final Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean retainAll(@NotNull final Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int size() {
    return materializer.materializeSize();
  }
}
