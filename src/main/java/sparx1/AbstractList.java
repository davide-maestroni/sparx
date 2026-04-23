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
package sparx1;

import java.io.IOException;
import java.util.Collection;
import sparx1.itf.Iterator;
import sparx1.itf.List;
import sparx1.itf.ListIterator;
import sparx1.util.UncheckedException;
import sparx1.util.annotation.NotNull;

abstract class AbstractList<E, T extends AbstractList<E, T>> extends
    java.util.AbstractList<E> implements List<E, T> {

  @Override
  public boolean add(final E e) {
    throw new UnsupportedOperationException("add");
  }

  @Override
  public boolean addAll(final @NotNull Collection<? extends E> c) {
    throw new UnsupportedOperationException("addAll");
  }

  @Override
  public boolean addAll(final int index, final @NotNull Collection<? extends E> c) {
    throw new UnsupportedOperationException("addAll");
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException("clear");
  }

  @Override
  public abstract @NotNull List<E, T> clone();

  @Override
  public boolean isNotEmpty() {
    return !isEmpty();
  }

  @Override
  public abstract @NotNull Iterator<E, ? extends Iterator<E, ?>> iterator();

  @Override
  public abstract @NotNull ListIterator<E, ? extends ListIterator<E, ?>> listIterator();

  @Override
  public abstract @NotNull ListIterator<E, ? extends ListIterator<E, ?>> listIterator(int index);

  @Override
  public boolean remove(final Object o) {
    throw new UnsupportedOperationException("remove");
  }

  @Override
  public boolean removeAll(final @NotNull Collection<?> c) {
    throw new UnsupportedOperationException("removeAll");
  }

  @Override
  public boolean retainAll(final @NotNull Collection<?> c) {
    throw new UnsupportedOperationException("retainAll");
  }

  @Override
  public @NotNull <A extends Appendable> A toString(final @NotNull A appendable) {
    try {
      for (E element : this) {
        appendable.append(String.valueOf(element));
      }
    } catch (final IOException e) {
      throw UncheckedException.throwUnchecked(e);
    }
    return appendable;
  }

  @Override
  public @NotNull <A extends Appendable> A toString(final @NotNull A appendable,
      final @NotNull String separator) {
    try {
      final java.util.Iterator<E> iterator = iterator();
      if (iterator.hasNext()) {
        appendable.append(String.valueOf(iterator.next()));
        while (iterator.hasNext()) {
          appendable.append(separator);
          appendable.append(String.valueOf(iterator.next()));
        }
      }
    } catch (final IOException e) {
      throw UncheckedException.throwUnchecked(e);
    }
    return appendable;
  }

  @Override
  public @NotNull <A extends Appendable> A toString(final @NotNull A appendable,
      final @NotNull String separator, final @NotNull String prefix, final @NotNull String suffix) {
    try {
      final java.util.Iterator<E> iterator = iterator();
      appendable.append(prefix);
      if (iterator.hasNext()) {
        appendable.append(String.valueOf(iterator.next()));
        while (iterator.hasNext()) {
          appendable.append(separator);
          appendable.append(String.valueOf(iterator.next()));
        }
      }
      appendable.append(suffix);
    } catch (final IOException e) {
      throw UncheckedException.throwUnchecked(e);
    }
    return appendable;
  }
}
