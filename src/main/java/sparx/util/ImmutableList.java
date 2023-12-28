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
package sparx.util;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.RandomAccess;
import org.jetbrains.annotations.NotNull;

public class ImmutableList<E> extends AbstractList<E> implements RandomAccess, Serializable {

  private static final ImmutableList<?> EMPTY_LIST = new ImmutableList<Object>(new Object[0]);

  @SuppressWarnings("unchecked")
  public static @NotNull <E> ImmutableList<E> of() {
    return (ImmutableList<E>) EMPTY_LIST;
  }

  public static @NotNull <E> ImmutableList<E> of(@NotNull final E... array) {
    return new ImmutableList<E>(array);
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <E> ImmutableList<E> ofElementsIn(
      @NotNull final Collection<E> collection) {
    return new ImmutableList<E>((E[]) collection.toArray());
  }

  private final E[] array;

  private ImmutableList(@NotNull final E[] array) {
    this.array = Requires.notNull(array, "array");
  }

  @Override
  public int size() {
    return array.length;
  }

  @Override
  public @NotNull Object[] toArray() {
    return Arrays.copyOf(array, array.length, Object[].class);
  }

  @Override
  @SuppressWarnings({"unchecked", "SuspiciousSystemArraycopy"})
  public @NotNull <T> T[] toArray(final T[] array) {
    int size = size();
    if (array.length < size) {
      return Arrays.copyOf(this.array, size, (Class<? extends T[]>) array.getClass());
    }
    System.arraycopy(this.array, 0, array, 0, size);
    if (array.length > size) {
      array[size] = null;
    }
    return array;
  }

  @Override
  public E get(final int index) {
    return array[index];
  }

  @Override
  public int indexOf(final Object o) {
    final E[] array = this.array;
    if (o == null) {
      for (int i = 0; i < array.length; i++) {
        if (array[i] == null) {
          return i;
        }
      }
    } else {
      for (int i = 0; i < array.length; i++) {
        if (o.equals(array[i])) {
          return i;
        }
      }
    }
    return -1;
  }

  @Override
  public boolean contains(final Object o) {
    return indexOf(o) >= 0;
  }

  @Override
  public @NotNull Iterator<E> iterator() {
    return super.iterator();
  }
}
