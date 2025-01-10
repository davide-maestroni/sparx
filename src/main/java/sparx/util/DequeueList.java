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

public class DequeueList<E> extends AbstractList<E> implements Cloneable, Deque<E>, RandomAccess,
    Serializable {

  private static final int DEFAULT_SIZE = 1 << 3;
  private static final int MAX_BINARY_GROWTH = 64;

  private Object[] data;
  private int first;
  private int last;
  private int size;

  // TODO: shrink

  /**
   * Creates a new empty list with a pre-defined initial capacity.
   */
  public DequeueList() {
    data = new Object[DEFAULT_SIZE];
  }

  /**
   * Creates a new empty list with the specified minimum capacity.
   *
   * @param minCapacity the minimum capacity.
   * @throws IllegalArgumentException if the specified capacity is less than 1.
   */
  public DequeueList(@Positive final int minCapacity) {
    final int initialCapacity = computeCapacity(Require.positive(minCapacity, "minCapacity"));
    data = new Object[initialCapacity];
  }

  private static int computeCapacity(@Positive final int minCapacity) {
    if (minCapacity <= MAX_BINARY_GROWTH) {
      final int msb = Integer.highestOneBit(minCapacity);
      return (minCapacity == msb) ? msb : msb << 1;
    }
    return minCapacity;
  }

  private static int growCapacity(@Positive final int toCapacity) {
    final int msb = Integer.highestOneBit(toCapacity);
    final int binaryCapacity = (toCapacity == msb) ? msb : msb << 1;
    final int minCapacity = binaryCapacity - (binaryCapacity >> 2);
    if (minCapacity > toCapacity) {
      return minCapacity;
    }
    if (binaryCapacity > toCapacity) {
      return binaryCapacity;
    }
    final int newCapacity = binaryCapacity < MAX_BINARY_GROWTH ? binaryCapacity << 1
        : binaryCapacity + (binaryCapacity >> 1);
    if (newCapacity < toCapacity) {
      throw new IllegalStateException("Maximum size exceeded");
    }
    return newCapacity;
  }

  private static int modDec(final int index, final int decrement, final int mod) {
    final int res = index - decrement;
    return (res < 0) ? res + mod : res;
  }

  private static int modInc(final int index, final int increment, final int mod) {
    final int res = index + increment;
    return (res >= mod) ? res - mod : res;
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
      addElement(modInc(first, index, data.length), element);
      if (first == last) {
        enlargeCapacity();
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
    final Object[] data = this.data;
    final int newFirst = (first = modDec(first, 1, data.length));
    data[newFirst] = element;
    if (newFirst == last) {
      enlargeCapacity();
    }
    ++size;
    ++modCount;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addLast(@Nullable final E element) {
    final Object[] data = this.data;
    final int last = this.last;
    data[last] = element;
    if (first == (this.last = modInc(last, 1, data.length))) {
      enlargeCapacity();
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
  public DequeueList<E> clone() throws CloneNotSupportedException {
    final Object[] data = this.data;
    final DequeueList<E> clone = (DequeueList<E>) super.clone();
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
    final Object[] newData = new Object[growCapacity(minCapacity)];
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
    final Object[] data = this.data;
    return (E) data[modInc(first, index, data.length)];
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
    final Object[] data = this.data;
    return (E) data[modDec(last, 1, data.length)];
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int indexOf(final Object o) {
    final int first = this.first;
    final int last = this.last;
    final Object[] data = this.data;
    if (o == null) {
      for (int i = first, to = (i <= last) ? last : data.length; ; i = 0, to = last) {
        for (; i < to; i++) {
          if (data[i] == null) {
            return modDec(i, first, data.length);
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
            return modDec(i, first, data.length);
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
    final Object[] data = this.data;
    if (o == null) {
      for (int i = last, to = (i >= first) ? first : 0; ; i = data.length, to = first) {
        for (i--; i > to - 1; i--) {
          if (data[i] == null) {
            return modDec(i, first, data.length);
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
            return modDec(i, first, data.length);
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
    return peekFirst();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings("unchecked")
  public E peekFirst() {
    if (isEmpty()) {
      return null;
    }
    return (E) data[first];
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings("unchecked")
  public E peekLast() {
    if (isEmpty()) {
      return null;
    }
    final Object[] data = this.data;
    return (E) data[modDec(last, 1, data.length)];
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public E poll() {
    return pollFirst();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public E pollFirst() {
    if (isEmpty()) {
      return null;
    }
    return unsafeRemoveFirst();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public E pollLast() {
    if (isEmpty()) {
      return null;
    }
    return unsafeRemoveLast();
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
    removeElement(modInc(first, index, data.length));
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
    final Object[] data = this.data;
    final E old = get(index);
    data[modInc(first, index, data.length)] = element;
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
    final int mod = data.length;
    final int front = modDec(index, first, mod);
    final int back = modDec(last, index, mod);
    final boolean isForward;
    if (front >= back) {
      if (back != 0) {
        if (index < last) {
          System.arraycopy(data, index, data, index + 1, back);
        } else {
          final int rightmost = data.length - 1;
          System.arraycopy(data, 0, data, 1, last);
          data[0] = data[rightmost];
          System.arraycopy(data, index, data, index + 1, rightmost - index);
        }
      }
      this.data[index] = element;
      this.last = modInc(last, 1, mod);
      isForward = true;
    } else {
      if (front != 0) {
        if (first == 0) {
          data[data.length - 1] = data[0];
          System.arraycopy(data, 1, data, 0, index - 1);
        } else if (first < index) {
          System.arraycopy(data, first, data, first - 1, index - first);
        } else {
          System.arraycopy(data, first, data, first - 1, data.length - first);
          data[data.length - 1] = data[0];
          if (index != 0) {
            System.arraycopy(data, 1, data, 0, index - 1);
          }
        }
      }
      this.data[modDec(index, 1, mod)] = element;
      this.first = modDec(first, 1, mod);
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
    final int newCapacity = growCapacity(totalSize);
    final Object[] data = this.data;
    final int mod = data.length;
    final int first = this.first;
    final int last = this.last;
    if (newCapacity > mod) {
      final Object[] newData = new Object[newCapacity];
      if (first < last) {
        System.arraycopy(data, first, newData, 0, index);
        System.arraycopy(collection.toArray(), 0, newData, index, added);
        System.arraycopy(data, first + index, newData, index + added, size - index);
      } else {
        final int remainder = mod - first;
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
    } else if (first < last) {
      if (first >= added) {
        final int newFirst = first - added;
        System.arraycopy(data, first, data, newFirst, index);
        int i = newFirst + index;
        for (final E element : collection) {
          data[i++] = element;
        }
        this.first = newFirst;
      } else if (mod - last >= added) {
        final int shift = first + index;
        System.arraycopy(data, shift, data, shift + added, last - index);
        int i = shift;
        for (final E element : collection) {
          data[i++] = element;
        }
        this.last = modInc(last, added, mod);
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
      final int remainder = mod - first;
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

  private void enlargeCapacity() {
    final Object[] data = this.data;
    final int length = data.length;
    final int first = this.first;
    final int front = length - first;
    final Object[] newData = new Object[growCapacity(length + 1)];
    System.arraycopy(data, first, newData, 0, front);
    System.arraycopy(data, 0, newData, front, first);
    this.data = newData;
    this.first = 0;
    last = length;
    ++modCount;
  }

  private boolean removeElement(final int index) {
    final int first = this.first;
    final int last = this.last;
    final Object[] data = this.data;
    final int mod = data.length;
    final int front = modDec(index, first, mod);
    final int back = modDec(last, index, mod);
    final boolean isForward;
    if (front <= back) {
      if (first <= index) {
        System.arraycopy(data, first, data, first + 1, front);
      } else {
        final int rightmost = data.length - 1;
        System.arraycopy(data, 0, data, 1, index);
        data[0] = data[rightmost];
        System.arraycopy(data, first, data, first + 1, rightmost - first);
      }
      this.data[first] = null;
      this.first = modInc(first, 1, mod);
      isForward = true;
    } else {
      if (index < last) {
        System.arraycopy(data, index + 1, data, index, back);
      } else {
        final int rightmost = data.length - 1;
        System.arraycopy(data, index + 1, data, index, rightmost - index);
        data[rightmost] = data[0];
        System.arraycopy(data, 1, data, 0, last);
      }
      this.data[last] = null;
      this.last = modDec(last, 1, mod);
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
    this.first = modInc(first, 1, data.length);
    final Object output = data[first];
    data[first] = null;
    --size;
    ++modCount;
    return (E) output;
  }

  @SuppressWarnings("unchecked")
  private E unsafeRemoveLast() {
    final Object[] data = this.data;
    final int newLast = modDec(last, 1, data.length);
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
      pointer = modInc(first, offset, data.length);
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
      final Object[] data = DequeueList.this.data;
      this.pointer = modInc(pointer, 1, data.length);
      return (E) data[pointer];
    }

    @Override
    public void remove() {
      if (isRemoved) {
        throw new IllegalStateException();
      }
      final int pointer = this.pointer;
      final int index = modDec(pointer, 1, data.length);
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
      final int mod = data.length;
      if (addElement(pointer, e)) {
        this.pointer = modInc(pointer, 1, mod);
      }
      final int first = DequeueList.this.first;
      final int last = DequeueList.this.last;
      if (first == last) {
        final int index = modDec(pointer, first, mod);
        enlargeCapacity();
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
      return modDec(pointer, first, data.length);
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
      final Object[] data = DequeueList.this.data;
      return (E) data[this.pointer = modDec(pointer, 1, data.length)];
    }

    @Override
    public int previousIndex() {
      checkForComodification();
      final int pointer = this.pointer;
      if (pointer == first) {
        return -1;
      }
      return modDec(pointer, first + 1, data.length);
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
          this.pointer = modInc(pointer, 1, data.length);
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
      final Object[] data = DequeueList.this.data;
      if (isForward) {
        index = modDec(index, 1, data.length);
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
      final Object[] data = DequeueList.this.data;
      return (E) data[this.pointer = modDec(pointer, 1, data.length)];
    }

    @Override
    public void remove() {
      if (isRemoved) {
        throw new IllegalStateException();
      }
      checkForComodification();
      final int pointer = this.pointer;
      if (removeElement(pointer)) {
        this.pointer = modInc(pointer, 1, data.length);
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

