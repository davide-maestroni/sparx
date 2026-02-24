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
import org.jetbrains.annotations.NotNull;
import sparx1.itf.Iterator;
import sparx1.itf.List;
import sparx1.itf.ListIterator;
import sparx1.util.UncheckedException;

abstract class AbstractList<E, T extends AbstractList<E, T>> extends
    java.util.AbstractList<E> implements List<E, T> {

  @Override
  public boolean add(final E e) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(@NotNull final Collection<? extends E> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(final int index, @NotNull final Collection<? extends E> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
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
  public @NotNull <A extends Appendable> A toString(@NotNull final A appendable) {
    try {
      for (E element : this) {
        appendable.append(element == null ? null : element.toString());
      }
    } catch (final IOException e) {
      throw UncheckedException.throwUnchecked(e);
    }
    return appendable;
  }

  @Override
  public @NotNull <A extends Appendable> A toString(@NotNull final A appendable,
      @NotNull final String separator) {
    try {
      final java.util.Iterator<E> iterator = iterator();
      if (iterator.hasNext()) {
        final E element = iterator.next();
        appendable.append(element == null ? null : element.toString());
        while (iterator.hasNext()) {
          appendable.append(separator);
          appendable.append(element == null ? null : element.toString());
        }
      }
    } catch (final IOException e) {
      throw UncheckedException.throwUnchecked(e);
    }
    return appendable;
  }

  @Override
  public @NotNull <A extends Appendable> A toString(@NotNull final A appendable,
      @NotNull final String separator, @NotNull final String prefix, @NotNull final String suffix) {
    try {
      final java.util.Iterator<E> iterator = iterator();
      appendable.append(prefix);
      if (iterator.hasNext()) {
        final E element = iterator.next();
        appendable.append(element == null ? null : element.toString());
        while (iterator.hasNext()) {
          appendable.append(separator);
          appendable.append(element == null ? null : element.toString());
        }
      }
      appendable.append(suffix);
    } catch (final IOException e) {
      throw UncheckedException.throwUnchecked(e);
    }
    return appendable;
  }
}
