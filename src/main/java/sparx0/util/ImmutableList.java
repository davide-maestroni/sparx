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
package sparx0.util;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

///////////////////////////////////////////////
// WARNING: GENERATED CODE - DO NOT MODIFY!!
// - templating engine: Mustache 0.9.11
// - template path: template/util/ImmutableList.mustache
///////////////////////////////////////////////

public class ImmutableList<E> extends AbstractList<E> implements RandomAccess, Serializable {

  private static final Iterator<?> EMPTY_ITERATOR = new ListIterator<Object>() {
    @Override
    public void add(final Object object) {
      throw new UnsupportedOperationException("add");
    }

    @Override
    public boolean hasNext() {
      return false;
    }

    @Override
    public boolean hasPrevious() {
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
    public Object previous() {
      throw new NoSuchElementException();
    }

    @Override
    public int previousIndex() {
      return 0;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove");
    }

    @Override
    public void set(final Object object) {
      throw new UnsupportedOperationException("set");
    }
  };
  private static final ImmutableList<?> EMPTY_LIST = new ImmutableList<Object>() {
    @Override
    public void clear() {
    }

    @Override
    public boolean contains(final Object o) {
      return false;
    }

    @Override
    public boolean equals(Object o) {
      return (o instanceof List) && ((List<?>) o).isEmpty();
    }

    @Override
    public Object get(final int index) {
      throw new IndexOutOfBoundsException("Index: " + index);
    }

    @Override
    public int hashCode() {
      return 1;
    }

    @Override
    public int indexOf(final Object o) {
      return -1;
    }

    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull Iterator<Object> iterator() {
      return (Iterator<Object>) EMPTY_ITERATOR;
    }

    @Override
    public int lastIndexOf(final Object o) {
      return -1;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull ListIterator<Object> listIterator() {
      return (ListIterator<Object>) EMPTY_ITERATOR;
    }

    @Override
    public int size() {
      return 0;
    }

    @Override
    public @NotNull Object[] toArray() {
      return new Object[0];
    }

    @Override
    public @NotNull <T> T[] toArray(final T[] array) {
      if (array.length > 0) {
        array[0] = null;
      }
      return array;
    }
  };

  private final E[] elements;

  private ImmutableList(@NotNull final E... elements) {
    this.elements = elements;
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <E> ImmutableList<E> of() {
    return (ImmutableList<E>) EMPTY_LIST;
  }

  @SuppressWarnings("ConstantValue")
  public static @NotNull <E> ImmutableList<E> of(final E element) {
    if (element == null) {
      return new ImmutableList<E>(element) {
        @Override
        public boolean contains(final Object o) {
          return (o == null);
        }

        @Override
        public int indexOf(final Object o) {
          return (o == null) ? 0 : -1;
        }

        @Override
        public int lastIndexOf(final Object o) {
          return indexOf(o);
        }

        @Override
        public int size() {
          return 1;
        }
      };
    }
    return new ImmutableList<E>(element) {
      @Override
      public boolean contains(final Object o) {
        return element.equals(o);
      }

      @Override
      public int indexOf(final Object o) {
        return element.equals(o) ? 0 : -1;
      }

      @Override
      public int lastIndexOf(final Object o) {
        return indexOf(o);
      }

      @Override
      public int size() {
        return 1;
      }
    };
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <E> ImmutableList<E> of(final E firstElement, final E secondElement) {
    return new ImmutableList<E>(firstElement, secondElement);
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <E> ImmutableList<E> of(final E firstElement, final E secondElement, final E thirdElement) {
    return new ImmutableList<E>(firstElement, secondElement, thirdElement);
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <E> ImmutableList<E> of(final E firstElement, final E secondElement, final E thirdElement, final E fourthElement) {
    return new ImmutableList<E>(firstElement, secondElement, thirdElement, fourthElement);
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <E> ImmutableList<E> of(final E firstElement, final E secondElement, final E thirdElement, final E fourthElement, final E fifthElement) {
    return new ImmutableList<E>(firstElement, secondElement, thirdElement, fourthElement, fifthElement);
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <E> ImmutableList<E> of(final E firstElement, final E secondElement, final E thirdElement, final E fourthElement, final E fifthElement, final E sixthElement) {
    return new ImmutableList<E>(firstElement, secondElement, thirdElement, fourthElement, fifthElement, sixthElement);
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <E> ImmutableList<E> of(final E firstElement, final E secondElement, final E thirdElement, final E fourthElement, final E fifthElement, final E sixthElement, final E seventhElement) {
    return new ImmutableList<E>(firstElement, secondElement, thirdElement, fourthElement, fifthElement, sixthElement, seventhElement);
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <E> ImmutableList<E> of(final E firstElement, final E secondElement, final E thirdElement, final E fourthElement, final E fifthElement, final E sixthElement, final E seventhElement, final E eighthElement) {
    return new ImmutableList<E>(firstElement, secondElement, thirdElement, fourthElement, fifthElement, sixthElement, seventhElement, eighthElement);
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <E> ImmutableList<E> of(final E firstElement, final E secondElement, final E thirdElement, final E fourthElement, final E fifthElement, final E sixthElement, final E seventhElement, final E eighthElement, final E ninthElement) {
    return new ImmutableList<E>(firstElement, secondElement, thirdElement, fourthElement, fifthElement, sixthElement, seventhElement, eighthElement, ninthElement);
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <E> ImmutableList<E> of(final E firstElement, final E secondElement, final E thirdElement, final E fourthElement, final E fifthElement, final E sixthElement, final E seventhElement, final E eighthElement, final E ninthElement, final E tenthElement) {
    return new ImmutableList<E>(firstElement, secondElement, thirdElement, fourthElement, fifthElement, sixthElement, seventhElement, eighthElement, ninthElement, tenthElement);
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <E> ImmutableList<E> of(final E firstElement, final E secondElement, final E thirdElement, final E fourthElement, final E fifthElement, final E sixthElement, final E seventhElement, final E eighthElement, final E ninthElement, final E tenthElement, final E eleventhElement) {
    return new ImmutableList<E>(firstElement, secondElement, thirdElement, fourthElement, fifthElement, sixthElement, seventhElement, eighthElement, ninthElement, tenthElement, eleventhElement);
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <E> ImmutableList<E> of(final E firstElement, final E secondElement, final E thirdElement, final E fourthElement, final E fifthElement, final E sixthElement, final E seventhElement, final E eighthElement, final E ninthElement, final E tenthElement, final E eleventhElement, final E twelfthElement) {
    return new ImmutableList<E>(firstElement, secondElement, thirdElement, fourthElement, fifthElement, sixthElement, seventhElement, eighthElement, ninthElement, tenthElement, eleventhElement, twelfthElement);
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <E> ImmutableList<E> of(final E firstElement, final E secondElement, final E thirdElement, final E fourthElement, final E fifthElement, final E sixthElement, final E seventhElement, final E eighthElement, final E ninthElement, final E tenthElement, final E eleventhElement, final E twelfthElement, final E thirteenthElement) {
    return new ImmutableList<E>(firstElement, secondElement, thirdElement, fourthElement, fifthElement, sixthElement, seventhElement, eighthElement, ninthElement, tenthElement, eleventhElement, twelfthElement, thirteenthElement);
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <E> ImmutableList<E> of(final E firstElement, final E secondElement, final E thirdElement, final E fourthElement, final E fifthElement, final E sixthElement, final E seventhElement, final E eighthElement, final E ninthElement, final E tenthElement, final E eleventhElement, final E twelfthElement, final E thirteenthElement, final E fourteenthElement) {
    return new ImmutableList<E>(firstElement, secondElement, thirdElement, fourthElement, fifthElement, sixthElement, seventhElement, eighthElement, ninthElement, tenthElement, eleventhElement, twelfthElement, thirteenthElement, fourteenthElement);
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <E> ImmutableList<E> of(final E firstElement, final E secondElement, final E thirdElement, final E fourthElement, final E fifthElement, final E sixthElement, final E seventhElement, final E eighthElement, final E ninthElement, final E tenthElement, final E eleventhElement, final E twelfthElement, final E thirteenthElement, final E fourteenthElement, final E fifteenthElement) {
    return new ImmutableList<E>(firstElement, secondElement, thirdElement, fourthElement, fifthElement, sixthElement, seventhElement, eighthElement, ninthElement, tenthElement, eleventhElement, twelfthElement, thirteenthElement, fourteenthElement, fifteenthElement);
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <E> ImmutableList<E> of(final E firstElement, final E secondElement, final E thirdElement, final E fourthElement, final E fifthElement, final E sixthElement, final E seventhElement, final E eighthElement, final E ninthElement, final E tenthElement, final E eleventhElement, final E twelfthElement, final E thirteenthElement, final E fourteenthElement, final E fifteenthElement, final E sixteenthElement) {
    return new ImmutableList<E>(firstElement, secondElement, thirdElement, fourthElement, fifthElement, sixthElement, seventhElement, eighthElement, ninthElement, tenthElement, eleventhElement, twelfthElement, thirteenthElement, fourteenthElement, fifteenthElement, sixteenthElement);
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <E> ImmutableList<E> of(final E firstElement, final E secondElement, final E thirdElement, final E fourthElement, final E fifthElement, final E sixthElement, final E seventhElement, final E eighthElement, final E ninthElement, final E tenthElement, final E eleventhElement, final E twelfthElement, final E thirteenthElement, final E fourteenthElement, final E fifteenthElement, final E sixteenthElement, final E seventeenthElement) {
    return new ImmutableList<E>(firstElement, secondElement, thirdElement, fourthElement, fifthElement, sixthElement, seventhElement, eighthElement, ninthElement, tenthElement, eleventhElement, twelfthElement, thirteenthElement, fourteenthElement, fifteenthElement, sixteenthElement, seventeenthElement);
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <E> ImmutableList<E> of(final E firstElement, final E secondElement, final E thirdElement, final E fourthElement, final E fifthElement, final E sixthElement, final E seventhElement, final E eighthElement, final E ninthElement, final E tenthElement, final E eleventhElement, final E twelfthElement, final E thirteenthElement, final E fourteenthElement, final E fifteenthElement, final E sixteenthElement, final E seventeenthElement, final E eighteenthElement) {
    return new ImmutableList<E>(firstElement, secondElement, thirdElement, fourthElement, fifthElement, sixthElement, seventhElement, eighthElement, ninthElement, tenthElement, eleventhElement, twelfthElement, thirteenthElement, fourteenthElement, fifteenthElement, sixteenthElement, seventeenthElement, eighteenthElement);
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <E> ImmutableList<E> of(final E firstElement, final E secondElement, final E thirdElement, final E fourthElement, final E fifthElement, final E sixthElement, final E seventhElement, final E eighthElement, final E ninthElement, final E tenthElement, final E eleventhElement, final E twelfthElement, final E thirteenthElement, final E fourteenthElement, final E fifteenthElement, final E sixteenthElement, final E seventeenthElement, final E eighteenthElement, final E nineteenthElement) {
    return new ImmutableList<E>(firstElement, secondElement, thirdElement, fourthElement, fifthElement, sixthElement, seventhElement, eighthElement, ninthElement, tenthElement, eleventhElement, twelfthElement, thirteenthElement, fourteenthElement, fifteenthElement, sixteenthElement, seventeenthElement, eighteenthElement, nineteenthElement);
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <E> ImmutableList<E> of(final E firstElement, final E secondElement, final E thirdElement, final E fourthElement, final E fifthElement, final E sixthElement, final E seventhElement, final E eighthElement, final E ninthElement, final E tenthElement, final E eleventhElement, final E twelfthElement, final E thirteenthElement, final E fourteenthElement, final E fifteenthElement, final E sixteenthElement, final E seventeenthElement, final E eighteenthElement, final E nineteenthElement, final E twentiethElement) {
    return new ImmutableList<E>(firstElement, secondElement, thirdElement, fourthElement, fifthElement, sixthElement, seventhElement, eighthElement, ninthElement, tenthElement, eleventhElement, twelfthElement, thirteenthElement, fourteenthElement, fifteenthElement, sixteenthElement, seventeenthElement, eighteenthElement, nineteenthElement, twentiethElement);
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <E> ImmutableList<E> of(@Nullable final E... elements) {
    if (elements == null || elements.length == 0) {
      return (ImmutableList<E>) EMPTY_LIST;
    }
    if (elements.length == 1) {
      return of(elements[0]);
    }
    return new ImmutableList<E>(
        (E[]) Arrays.copyOf(elements, elements.length, elements.getClass()));
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <E> ImmutableList<E> ofElementsIn(
      @NotNull final Collection<E> collection) {
    if (collection.isEmpty()) {
      return (ImmutableList<E>) EMPTY_LIST;
    }
    if (collection.size() == 1) {
      return of(collection.iterator().next());
    }
    return new ImmutableList<E>((E[]) collection.toArray());
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean contains(final Object o) {
    return indexOf(o) >= 0;
  }

  @Override
  public E get(final int index) {
    return elements[index];
  }

  @Override
  public int indexOf(final Object o) {
    final E[] elements = this.elements;
    if (o == null) {
      for (int i = 0; i < elements.length; ++i) {
        if (elements[i] == null) {
          return i;
        }
      }
    } else {
      for (int i = 0; i < elements.length; ++i) {
        if (o.equals(elements[i])) {
          return i;
        }
      }
    }
    return -1;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public int lastIndexOf(final Object o) {
    final E[] elements = this.elements;
    if (o == null) {
      for (int i = elements.length - 1; i >= 0; --i) {
        if (elements[i] == null) {
          return i;
        }
      }
    } else {
      for (int i = elements.length - 1; i >= 0; --i) {
        if (o.equals(elements[i])) {
          return i;
        }
      }
    }
    return -1;
  }

  @Override
  public int size() {
    return elements.length;
  }

  @Override
  public @NotNull Object[] toArray() {
    return Arrays.copyOf(elements, elements.length, Object[].class);
  }

  @Override
  @SuppressWarnings({"unchecked", "SuspiciousSystemArraycopy"})
  public @NotNull <T> T[] toArray(final T[] array) {
    int size = size();
    if (array.length < size) {
      return Arrays.copyOf(this.elements, size, (Class<? extends T[]>) array.getClass());
    }
    System.arraycopy(this.elements, 0, array, 0, size);
    if (array.length > size) {
      array[size] = null;
    }
    return array;
  }
}
