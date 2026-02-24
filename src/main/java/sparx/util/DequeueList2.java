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
import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Deque;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sparx.util.annotation.Positive;

public class DequeueList2<E> extends AbstractList<E> implements Cloneable, Deque<E>, RandomAccess,
    Serializable {

  private static final int DEFAULT_SIZE = 1 << 3;

  private Object[] data;
  private int first;
  private int last;
  private int mask;
  private int size;

  // TODO: shrink

  /**
   * Creates a new empty list with a pre-defined initial capacity.
   */
  public DequeueList2() {
    data = new Object[DEFAULT_SIZE];
    mask = DEFAULT_SIZE - 1;
  }

  /**
   * Creates a new empty list with the specified minimum capacity.
   *
   * @param minCapacity the minimum capacity.
   * @throws IllegalArgumentException if the specified capacity is less than 1.
   */
  public DequeueList2(@Positive final int minCapacity) {
    final int initialCapacity = computeCapacity(Require.positive(minCapacity, "minCapacity"));
    data = new Object[initialCapacity];
    mask = initialCapacity - 1;
  }

  private static int computeCapacity(@Positive final int minCapacity) {
    final int msb = Integer.highestOneBit(minCapacity);
    return (minCapacity == msb) ? msb : msb << 1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean add(@Nullable final E element) {
    addLast(element);
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void add(final int index, @Nullable final E element) {
    if (index == 0) {
      addFirst(element);
    } else if (index == size) {
      addLast(element);
    } else {
      addElement((first + index) & mask, element);
      if (first == last) {
        growCapacity();
      }
    }
  }

  @Override
  public boolean addAll(@NotNull final Collection<? extends E> collection) {
    if (collection.isEmpty()) {
      return false;
    }
    addElements(size, collection);
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean addAll(final int index, final Collection<? extends E> collection) {
    if (index < 0 || index > size) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    if (collection.isEmpty()) {
      return false;
    }
    addElements(index, collection);
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addFirst(@Nullable final E element) {
    final int newFirst = (first = (first - 1) & mask);
    data[newFirst] = element;
    if (newFirst == last) {
      growCapacity();
    }
    ++size;
    ++modCount;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addLast(@Nullable final E element) {
    final int last = this.last;
    data[last] = element;
    if (first == (this.last = (last + 1) & mask)) {
      growCapacity();
    }
    ++size;
    ++modCount;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void clear() {
    final int last = this.last;
    final Object[] data = this.data;
    for (int i = first, to = (i <= last) ? last : data.length; ; i = 0, to = last) {
      for (; i < to; i++) {
        data[i] = null;
      }
      if (to == last) {
        break;
      }
    }
    first = 0;
    this.last = 0;
    size = 0;
    ++modCount;
  }

  @Override
  @SuppressWarnings("unchecked")
  public DequeueList2<E> clone() throws CloneNotSupportedException {
    final DequeueList2<E> clone = (DequeueList2<E>) super.clone();
    clone.data = Arrays.copyOf(data, data.length);
    return clone;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NotNull Iterator<E> descendingIterator() {
    return new DescendingIterator();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public E element() {
    return getFirst();
  }

  public boolean ensureCapacity(final int minCapacity) {
    final Object[] data = this.data;
    if (data.length >= minCapacity) {
      return false;
    }
    final int newCapacity = computeCapacity(minCapacity);
    final Object[] newData = new Object[newCapacity];
    final int first = this.first;
    final int last = this.last;
    if (first < last) {
      System.arraycopy(data, first, newData, 0, size);
    } else {
      final int front = data.length - first;
      System.arraycopy(data, first, newData, 0, data.length - first);
      System.arraycopy(data, 0, newData, front, last);
    }
    this.data = newData;
    this.first = 0;
    this.last = size;
    mask = newCapacity - 1;
    ++modCount;
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings("unchecked")
  public E get(final int index) {
    if (index < 0 || index >= size) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    return (E) data[(first + index) & mask];
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings("unchecked")
  public E getFirst() {
    if (isEmpty()) {
      throw new NoSuchElementException();
    }
    return (E) data[first];
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings("unchecked")
  public E getLast() {
    if (isEmpty()) {
      throw new NoSuchElementException();
    }
    return (E) data[(last - 1) & mask];
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int indexOf(final Object o) {
    final int first = this.first;
    final int last = this.last;
    final int mask = this.mask;
    final Object[] data = this.data;
    if (o == null) {
      for (int i = first, to = (i <= last) ? last : data.length; ; i = 0, to = last) {
        for (; i < to; i++) {
          if (data[i] == null) {
            return (i - first) & mask;
          }
        }
        if (to == last) {
          break;
        }
      }
    } else {
      for (int i = first, to = (i <= last) ? last : data.length; ; i = 0, to = last) {
        for (; i < to; i++) {
          if (o.equals(data[i])) {
            return (i - first) & mask;
          }
        }
        if (to == last) {
          break;
        }
      }
    }
    return -1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isEmpty() {
    return size == 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NotNull Iterator<E> iterator() {
    return new AscendingIterator(0);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int lastIndexOf(final Object o) {
    final int first = this.first;
    final int last = this.last;
    final int mask = this.mask;
    final Object[] data = this.data;
    if (o == null) {
      for (int i = last, to = (i >= first) ? first : 0; ; i = data.length, to = first) {
        for (i--; i > to - 1; i--) {
          if (data[i] == null) {
            return (i - first) & mask;
          }
        }
        if (to == first) {
          break;
        }
      }
    } else {
      for (int i = last, to = (i >= first) ? first : 0; ; i = data.length, to = first) {
        for (i--; i > to - 1; i--) {
          if (o.equals(data[i])) {
            return (i - first) & mask;
          }
        }
        if (to == first) {
          break;
        }
      }
    }
    return -1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NotNull ListIterator<E> listIterator(final int index) {
    if (index < 0 || index > size) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    return new DequeueListIterator(index);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean offer(final E e) {
    addLast(e);
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean offerFirst(final E e) {
    addFirst(e);
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean offerLast(final E e) {
    addLast(e);
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public E peek() {
    // TODO: support
    throw new UnsupportedOperationException("peek");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public E peekFirst() {
    throw new UnsupportedOperationException("peekFirst");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public E peekLast() {
    throw new UnsupportedOperationException("peekLast");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public E poll() {
    throw new UnsupportedOperationException("poll");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public E pollFirst() {
    throw new UnsupportedOperationException("pollFirst");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public E pollLast() {
    throw new UnsupportedOperationException("pollLast");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void push(final E e) {
    addFirst(e);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public E pop() {
    return removeFirst();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public E remove() {
    return removeFirst();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public E remove(final int index) {
    final E element = get(index);
    removeElement((first + index) & mask);
    return element;
  }

  public void remove(final int fromIndex, final int toIndex) {
    if (fromIndex < 0 || fromIndex >= size) {
      throw new IndexOutOfBoundsException(Integer.toString(fromIndex));
    }
    if (toIndex > size) {
      throw new IndexOutOfBoundsException(Integer.toString(toIndex));
    }
    if (toIndex < fromIndex) {
      throw new IllegalArgumentException("toIndex is less than fromIndex");
    }
    if (fromIndex == toIndex) {
      return;
    }
    removeRange(fromIndex, toIndex);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public E removeFirst() {
    if (isEmpty()) {
      throw new NoSuchElementException();
    }
    return unsafeRemoveFirst();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean removeFirstOccurrence(final Object o) {
    final int first = this.first;
    final int last = this.last;
    final Object[] data = this.data;
    if (o == null) {
      for (int i = first, to = (i <= last) ? last : data.length; ; i = 0, to = last) {
        for (; i < to; i++) {
          if (data[i] == null) {
            removeElement(i);
            return true;
          }
        }
        if (to == last) {
          break;
        }
      }
    } else {
      for (int i = first, to = (i <= last) ? last : data.length; ; i = 0, to = last) {
        for (; i < to; i++) {
          if (o.equals(data[i])) {
            removeElement(i);
            return true;
          }
        }
        if (to == last) {
          break;
        }
      }
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public E removeLast() {
    if (isEmpty()) {
      throw new NoSuchElementException();
    }
    return unsafeRemoveLast();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean removeLastOccurrence(final Object o) {
    final int first = this.first;
    final int last = this.last;
    final Object[] data = this.data;
    if (o == null) {
      for (int i = last, to = (i >= first) ? first : 0; ; i = data.length, to = first) {
        for (i--; i > to - 1; i--) {
          if (data[i] == null) {
            removeElement(i);
            return true;
          }
        }
        if (to == first) {
          break;
        }
      }
    } else {
      for (int i = last, to = (i >= first) ? first : 0; ; i = data.length, to = first) {
        for (i--; i > to - 1; i--) {
          if (o.equals(data[i])) {
            removeElement(i);
            return true;
          }
        }
        if (to == first) {
          break;
        }
      }
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public E set(final int index, @Nullable final E element) {
    final E old = get(index);
    data[(first + index) & mask] = element;
    ++modCount;
    return old;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int size() {
    return size;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NotNull Object[] toArray() {
    return copyElements(new Object[size()]);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <T> T[] toArray(T[] array) {
    final int size = size();
    if (array.length < size) {
      array = (T[]) Array.newInstance(array.getClass().getComponentType(), size);
      copyElements(array);
    } else {
      copyElements(array);
      if (array.length > size) {
        array[size] = null;
      }
    }
    return array;
  }

  @Override
  protected void removeRange(final int fromIndex, final int toIndex) {
    final Object[] data = this.data;
    final int first = this.first;
    final int last = this.last;
    final int length = toIndex - fromIndex;
    if (first < last) {
      if (fromIndex < size - toIndex) {
        final int dst = first + length;
        System.arraycopy(data, first, data, dst, fromIndex);
        for (int i = first; i < dst; ++i) {
          data[i] = null;
        }
        this.first += length;
      } else {
        System.arraycopy(data, first + toIndex, data, first + fromIndex, size - toIndex);
        for (int i = last - length; i < last; ++i) {
          data[i] = null;
        }
        this.last -= length;
      }
    } else if (first + fromIndex < data.length) {
      final int remainder = first - data.length + toIndex;
      if (remainder > 0) {
        final int front = length - remainder;
        final int dst = first + front;
        System.arraycopy(data, first, data, dst, front);
        for (int i = first; i < dst; ++i) {
          data[i] = null;
        }
        this.first += front;
        System.arraycopy(data, remainder, data, 0, remainder);
        for (int i = last - remainder; i < last; ++i) {
          data[i] = null;
        }
        this.last -= remainder;
      } else {
        final int dst = first + length;
        System.arraycopy(data, first, data, dst, fromIndex);
        for (int i = first; i < dst; ++i) {
          data[i] = null;
        }
        this.first += length;
      }
    } else {
      final int dst = fromIndex - data.length + first;
      final int src = dst + length;
      System.arraycopy(data, src, data, dst, last - src);
      for (int i = last - length; i < last; ++i) {
        data[i] = null;
      }
      this.last -= length;
    }
    size -= length;
    ++modCount;
  }

  private boolean addElement(final int index, final E element) {
    final int first = this.first;
    final int last = this.last;
    final Object[] data = this.data;
    final int mask = this.mask;
    final int front = (index - first) & mask;
    final int back = (last - index) & mask;
    final boolean isForward;
    if (front >= back) {
      if (back != 0) {
        if (index < last) {
          System.arraycopy(data, index, data, index + 1, back);
        } else {
          System.arraycopy(data, 0, data, 1, last);
          data[0] = data[mask];
          System.arraycopy(data, index, data, index + 1, mask - index);
        }
      }
      this.data[index] = element;
      this.last = (last + 1) & mask;
      isForward = true;
    } else {
      if (front != 0) {
        if (first == 0) {
          data[mask] = data[0];
          System.arraycopy(data, 1, data, 0, index - 1);
        } else if (first < index) {
          System.arraycopy(data, first, data, first - 1, index - first);
        } else {
          System.arraycopy(data, first, data, first - 1, mask - first + 1);
          data[mask] = data[0];
          if (index != 0) {
            System.arraycopy(data, 1, data, 0, index - 1);
          }
        }
      }
      this.data[(index - 1) & mask] = element;
      this.first = (first - 1) & mask;
      isForward = false;
    }
    ++size;
    ++modCount;
    return isForward;
  }

  private void addElements(final int index, @NotNull final Collection<? extends E> collection) {
    final int added = collection.size();
    final int totalSize = size + added;
    if (totalSize < 0) {
      throw new IllegalStateException("Maximum size exceeded");
    }
    final int newCapacity = computeCapacity(totalSize + 1);
    final Object[] data = this.data;
    final int first = this.first;
    final int last = this.last;
    if (newCapacity > data.length) {
      final Object[] newData = new Object[newCapacity];
      if (first < last) {
        System.arraycopy(data, first, newData, 0, index);
        System.arraycopy(collection.toArray(), 0, newData, index, added);
        System.arraycopy(data, first + index, newData, index + added, size - index);
      } else {
        final int remainder = data.length - first;
        if (index < remainder) {
          System.arraycopy(data, first, newData, 0, index);
          int i = index;
          for (final E element : collection) {
            newData[i++] = element;
          }
          System.arraycopy(data, first + index, newData, index + added, remainder - index);
          System.arraycopy(data, 0, newData, remainder + added, last);
        } else {
          final int offset = index - remainder;
          System.arraycopy(data, first, newData, 0, remainder);
          System.arraycopy(data, 0, newData, remainder, offset);
          int i = index;
          for (final E element : collection) {
            newData[i++] = element;
          }
          System.arraycopy(data, offset, newData, index + added, last - offset);
        }
      }
      this.data = newData;
      this.first = 0;
      this.last = size + added;
      mask = newCapacity - 1;
    } else if (first < last) {
      if (first >= added) {
        final int newFirst = first - added;
        System.arraycopy(data, first, data, newFirst, index);
        int i = newFirst + index;
        for (final E element : collection) {
          data[i++] = element;
        }
        this.first = newFirst;
      } else if (data.length - last >= added) {
        final int shift = first + index;
        System.arraycopy(data, shift, data, shift + added, last - index);
        int i = shift;
        for (final E element : collection) {
          data[i++] = element;
        }
        this.last = (last + added) & mask;
      } else {
        final int shift = first + index;
        System.arraycopy(data, first, data, 0, index);
        System.arraycopy(data, shift, data, index + added, last - shift);
        int i = index;
        for (final E element : collection) {
          data[i++] = element;
        }
        this.first = 0;
        this.last = size + added;
      }
    } else {
      final int remainder = data.length - first;
      if (index < remainder) {
        final int newFirst = first - added;
        System.arraycopy(data, first, data, newFirst, index);
        int i = newFirst + index;
        for (final E element : collection) {
          data[i++] = element;
        }
        this.first = newFirst;
      } else {
        final int offset = index - remainder;
        System.arraycopy(data, offset, data, offset + added, last - offset);
        int i = offset;
        for (final E element : collection) {
          data[i++] = element;
        }
        this.last = last + added;
      }
    }
    size += added;
    ++modCount;
  }

  @NotNull
  @SuppressWarnings("SuspiciousSystemArraycopy")
  private <T> T[] copyElements(@NotNull final T[] dst) {
    final Object[] data = this.data;
    final int first = this.first;
    final int last = this.last;
    if (first <= last) {
      System.arraycopy(data, first, dst, 0, size);
    } else {
      final int length = data.length - first;
      System.arraycopy(data, first, dst, 0, length);
      System.arraycopy(data, 0, dst, length, last);
    }
    return dst;
  }

  private void growCapacity() {
    final int size = data.length;
    final int newCapacity = size << 1;
    if (newCapacity < size) {
      throw new IllegalStateException("Maximum size exceeded");
    }
    final Object[] data = this.data;
    final int first = this.first;
    final int remainder = size - first;
    final Object[] newData = new Object[newCapacity];
    System.arraycopy(data, first, newData, 0, remainder);
    System.arraycopy(data, 0, newData, remainder, first);
    this.data = newData;
    this.first = 0;
    last = size;
    mask = newCapacity - 1;
    ++modCount;
  }

  private boolean removeElement(final int index) {
    final int first = this.first;
    final int last = this.last;
    final Object[] data = this.data;
    final int mask = this.mask;
    final int front = (index - first) & mask;
    final int back = (last - index) & mask;
    final boolean isForward;
    if (front <= back) {
      if (first <= index) {
        System.arraycopy(data, first, data, first + 1, front);
      } else {
        System.arraycopy(data, 0, data, 1, index);
        data[0] = data[mask];
        System.arraycopy(data, first, data, first + 1, mask - first);
      }
      this.data[first] = null;
      this.first = (first + 1) & mask;
      isForward = true;
    } else {
      if (index < last) {
        System.arraycopy(data, index + 1, data, index, back);
      } else {
        System.arraycopy(data, index + 1, data, index, mask - index);
        data[mask] = data[0];
        System.arraycopy(data, 1, data, 0, last);
      }
      this.data[last] = null;
      this.last = (last - 1) & mask;
      isForward = false;
    }
    --size;
    ++modCount;
    return isForward;
  }

  @SuppressWarnings("unchecked")
  private E unsafeRemoveFirst() {
    final Object[] data = this.data;
    final int first = this.first;
    this.first = (first + 1) & mask;
    final Object output = data[first];
    data[first] = null;
    --size;
    ++modCount;
    return (E) output;
  }

  @SuppressWarnings("unchecked")
  private E unsafeRemoveLast() {
    final Object[] data = this.data;
    final int mask = this.mask;
    final int newLast = (last - 1) & mask;
    last = newLast;
    final Object output = data[newLast];
    data[newLast] = null;
    --size;
    ++modCount;
    return (E) output;
  }

  private class AscendingIterator implements Iterator<E> {

    protected int expectedModCount = modCount;
    protected boolean isRemoved = true;
    protected int pointer;

    private AscendingIterator(final int offset) {
      pointer = (first + offset) & mask;
    }

    @Override
    public boolean hasNext() {
      return (pointer != last);
    }

    @Override
    @SuppressWarnings("unchecked")
    public E next() {
      checkForComodification();
      final int pointer = this.pointer;
      if (pointer == last) {
        throw new NoSuchElementException();
      }
      isRemoved = false;
      this.pointer = (pointer + 1) & mask;
      return (E) data[pointer];
    }

    @Override
    public void remove() {
      if (isRemoved) {
        throw new IllegalStateException();
      }
      final int pointer = this.pointer;
      final int mask = DequeueList2.this.mask;
      final int index = (pointer - 1) & mask;
      checkForComodification();
      if (!removeElement(index)) {
        this.pointer = index;
      }
      expectedModCount = modCount;
      isRemoved = true;
    }

    final void checkForComodification() {
      if (modCount != expectedModCount) {
        throw new ConcurrentModificationException();
      }
    }
  }

  private class DequeueListIterator extends AscendingIterator implements ListIterator<E> {

    private boolean isForward = true;

    private DequeueListIterator(final int offset) {
      super(offset);
    }

    @Override
    public void add(final E e) {
      checkForComodification();
      final int pointer = this.pointer;
      if (addElement(pointer, e)) {
        this.pointer = (pointer + 1) & mask;
      }
      final int first = DequeueList2.this.first;
      final int last = DequeueList2.this.last;
      if (first == last) {
        final int index = (pointer - first) & mask;
        growCapacity();
        this.pointer = index;
      }
      expectedModCount = modCount;
      isRemoved = true; // disable remove
    }

    @Override
    public boolean hasPrevious() {
      return (pointer != first);
    }

    @Override
    public E next() {
      final E e = super.next();
      isForward = true;
      return e;
    }

    @Override
    public int nextIndex() {
      checkForComodification();
      return (pointer - first) & mask;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E previous() {
      checkForComodification();
      final int pointer = this.pointer;
      if (pointer == first) {
        throw new NoSuchElementException();
      }
      isForward = false;
      isRemoved = false;
      return (E) data[this.pointer = (pointer - 1) & mask];
    }

    @Override
    public int previousIndex() {
      checkForComodification();
      final int pointer = this.pointer;
      if (pointer == first) {
        return -1;
      }
      return (pointer - first - 1) & mask;
    }

    @Override
    public void remove() {
      if (!isForward) {
        if (isRemoved) {
          throw new IllegalStateException();
        }
        checkForComodification();
        final int pointer = this.pointer;
        if (removeElement(pointer)) {
          this.pointer = (pointer + 1) & mask;
        }
        expectedModCount = modCount;
        isRemoved = true;
      } else {
        super.remove();
      }
    }

    @Override
    public void set(final E element) {
      checkForComodification();
      int index = pointer;
      if (size == 0) {
        throw new IndexOutOfBoundsException(Integer.toString(index));
      }
      if (isForward) {
        index = (index - 1) & mask;
      }
      data[index] = element;
      expectedModCount = ++modCount;
    }
  }

  private class DescendingIterator implements Iterator<E> {

    private int expectedModCount = modCount;
    private boolean isRemoved = true;
    private int pointer = last;

    @Override
    public boolean hasNext() {
      return (pointer != first);
    }

    @Override
    @SuppressWarnings("unchecked")
    public E next() {
      checkForComodification();
      final int pointer = this.pointer;
      if (pointer == first) {
        throw new NoSuchElementException();
      }
      isRemoved = false;
      return (E) data[this.pointer = (pointer - 1) & mask];
    }

    @Override
    public void remove() {
      if (isRemoved) {
        throw new IllegalStateException();
      }
      checkForComodification();
      final int pointer = this.pointer;
      if (removeElement(pointer)) {
        this.pointer = (pointer + 1) & mask;
      }
      expectedModCount = modCount;
      isRemoved = true;
    }

    private void checkForComodification() {
      if (modCount != expectedModCount) {
        throw new ConcurrentModificationException();
      }
    }
  }
}

