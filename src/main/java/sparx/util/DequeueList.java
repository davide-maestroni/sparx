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
import java.util.ConcurrentModificationException;
import java.util.Deque;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DequeueList<E> extends AbstractList<E> implements Deque<E>, RandomAccess,
    Serializable {

  private static final int DEFAULT_SIZE = 1 << 3;
  private Object[] data;
  private int first;
  private int last;
  private int mask;
  private int size;

  /**
   * Creates a new empty queue with a pre-defined initial capacity.
   */
  public DequeueList() {
    data = new Object[DEFAULT_SIZE];
    mask = DEFAULT_SIZE - 1;
  }

  /**
   * Creates a new empty queue with the specified minimum capacity.
   *
   * @param minCapacity the minimum capacity.
   * @throws IllegalArgumentException if the specified capacity is less than 1.
   */
  public DequeueList(final int minCapacity) {
    final int msb = Integer.highestOneBit(Require.positive(minCapacity, "minCapacity"));
    final int initialCapacity = (minCapacity == msb) ? msb : msb << 1;
    data = new Object[initialCapacity];
    mask = initialCapacity - 1;
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
    addElement(index, element);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addFirst(@Nullable final E element) {
    int mask = this.mask;
    int newFirst = (first = (first - 1) & mask);
    data[newFirst] = element;
    if (newFirst == last) {
      doubleCapacity();
    }
    ++size;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addLast(@Nullable final E element) {
    final int last = this.last;
    data[last] = element;
    if (first == (this.last = (last + 1) & mask)) {
      doubleCapacity();
    }
    ++size;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void clear() {
    final int mask = this.mask;
    final int last = this.last;
    final Object[] data = this.data;
    int index = first;
    while (index != last) {
      data[index] = null;
      index = (index + 1) & mask;
    }
    first = 0;
    this.last = 0;
    size = 0;
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

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings("unchecked")
  public E get(final int index) {
    if ((index < 0) || (index >= size)) {
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
    int index = first;
    if (o == null) {
      while (index != last) {
        if (data[index] == null) {
          return (index + first) & mask;
        }
        index = (index + 1) & mask;
      }
    } else {
      while (index != last) {
        if (o.equals(data[index])) {
          return (index + first) & mask;
        }
        index = (index + 1) & mask;
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
    return new AscendingIterator();
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
    int index = last;
    if (o == null) {
      while (index != first) {
        if (data[index] == null) {
          return (index + first) & mask;
        }
        index = (index - 1) & mask;
      }
    } else {
      while (index != first) {
        if (o.equals(data[index])) {
          return (index + first) & mask;
        }
        index = (index - 1) & mask;
      }
    }
    return -1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NotNull ListIterator<E> listIterator(final int index) {
    return new DequeueListIterator();
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
    final int mask = this.mask;
    final Object[] data = this.data;
    int index = first;
    if (o == null) {
      while (index != last) {
        if (data[index] == null) {
          removeElement(index);
          return true;
        }
        index = (index + 1) & mask;
      }
    } else {
      while (index != last) {
        if (o.equals(data[index])) {
          removeElement(index);
          return true;
        }
        index = (index + 1) & mask;
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
    final int mask = this.mask;
    final Object[] data = this.data;
    int index = last;
    if (o == null) {
      while (index != first) {
        if (data[index] == null) {
          removeElement(index);
          return true;
        }
        index = (index - 1) & mask;
      }
    } else {
      while (index != first) {
        if (o.equals(data[index])) {
          removeElement(index);
          return true;
        }
        index = (index - 1) & mask;
      }
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings("unchecked")
  public E set(final int index, @Nullable final E element) {
    if ((index < 0) || (index >= size())) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    final Object[] data = this.data;
    final int pos = (first + index) & mask;
    final E old = (E) data[pos];
    data[pos] = element;
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
  public @NotNull <T> T[] toArray(@NotNull T[] array) {
    int size = size();
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

  private boolean addElement(final int index, final E element) {
    final int first = this.first;
    final int last = this.last;
    final Object[] data = this.data;
    final int mask = this.mask;
    final int front = (index - first) & mask;
    final int back = (last - index) & mask;
    final boolean isForward;
    if (front > back) {
      if (index < last) {
        System.arraycopy(data, index, data, index + 1, back);

      } else {
        System.arraycopy(data, 0, data, 1, last);
        data[0] = data[mask];
        System.arraycopy(data, index, data, index + 1, mask - index);
      }
      this.data[index] = element;
      this.last = (last + 1) & mask;
      isForward = true;

    } else {
      if (first == 0) {
        data[mask] = data[0];
        System.arraycopy(data, 1, data, 0, index - 1);
      } else if (first <= index) {
        System.arraycopy(data, first, data, first - 1, index - first);
      } else {
        System.arraycopy(data, first, data, first - 1, mask - first + 1);
      }
      this.data[(index - 1) & mask] = element;
      this.first = (first - 1) & mask;
      isForward = false;
    }
    --size;
    if (this.first == this.last) {
      doubleCapacity();
    }
    return isForward;
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

  private void doubleCapacity() {
    final Object[] data = this.data;
    final int size = data.length;
    final int newSize = size << 1;
    if (newSize < size) {
      throw new OutOfMemoryError();
    }
    final int first = this.first;
    final int remainder = size - first;
    final Object[] newData = new Object[newSize];
    System.arraycopy(data, first, newData, 0, remainder);
    System.arraycopy(data, 0, newData, remainder, first);
    this.data = newData;
    this.first = 0;
    last = size;
    mask = newSize - 1;
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
    return (E) output;
  }

  private class AscendingIterator implements Iterator<E> {

    protected boolean isRemoved;
    protected int originalFirst;
    protected int originalLast;
    protected int pointer;

    private AscendingIterator() {
      pointer = (originalFirst = first);
      originalLast = last;
    }

    @Override
    public boolean hasNext() {
      return (pointer != originalLast);
    }

    @Override
    @SuppressWarnings("unchecked")
    public E next() {
      final int pointer = this.pointer;
      final int originalLast = this.originalLast;
      if (pointer == originalLast) {
        throw new NoSuchElementException();
      }
      if ((first != originalFirst) || (last != originalLast)) {
        throw new ConcurrentModificationException();
      }
      isRemoved = false;
      this.pointer = (pointer + 1) & mask;
      return (E) data[pointer];
    }

    @Override
    public void remove() {
      if (isRemoved) {
        throw new IllegalStateException("element already removed");
      }
      final int pointer = this.pointer;
      final int originalFirst = this.originalFirst;
      if (pointer == originalFirst) {
        throw new IllegalStateException();
      }
      if ((first != originalFirst) || (last != originalLast)) {
        throw new ConcurrentModificationException();
      }
      final int mask = DequeueList.this.mask;
      final int index = (pointer - 1) & mask;
      if (removeElement(index)) {
        this.originalFirst = first;

      } else {
        originalLast = last;
        this.pointer = index;
      }
      isRemoved = true;
    }
  }

  private class DequeueListIterator extends AscendingIterator implements ListIterator<E> {

    private boolean isForward = true;

    @Override
    public boolean hasPrevious() {
      return (pointer != originalFirst);
    }

    @Override
    public E next() {
      final E e = super.next();
      isForward = true;
      return e;
    }

    @Override
    public int nextIndex() {
      return pointer & mask;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E previous() {
      final int pointer = this.pointer;
      final int originalFirst = this.originalFirst;
      if (pointer == originalFirst) {
        throw new NoSuchElementException();
      }
      if ((first != originalFirst) || (last != originalLast)) {
        throw new ConcurrentModificationException();
      }
      isForward = false;
      isRemoved = false;
      return (E) data[this.pointer = (pointer - 1) & mask];
    }

    @Override
    public int previousIndex() {
      final int pointer = this.pointer;
      if (pointer == originalFirst) {
        return -1;
      }
      return (pointer - 1) & mask;
    }

    @Override
    public void remove() {
      if (!isForward) {
        if (isRemoved) {
          throw new IllegalStateException("element already removed");
        }
        final int pointer = this.pointer;
        final int originalFirst = this.originalFirst;
        if (pointer == originalFirst) {
          throw new IllegalStateException();
        }
        if ((first != originalFirst) || (last != originalLast)) {
          throw new ConcurrentModificationException();
        }
        if (removeElement(pointer)) {
          this.originalFirst = first;
        } else {
          originalLast = last;
        }
        isRemoved = true;
      } else {
        super.remove();
      }
    }

    @Override
    public void set(final E e) {
      DequeueList.this.set(pointer, e);
    }

    @Override
    public void add(final E e) {
      final int pointer = this.pointer;
      DequeueList.this.add(pointer, e);
      this.pointer = (pointer + 1) & mask;
    }
  }

  private class DescendingIterator implements Iterator<E> {

    private boolean isRemoved;
    private int originalFirst;
    private int originalLast;
    private int pointer;

    private DescendingIterator() {
      originalFirst = first;
      pointer = (originalLast = last);
    }

    @Override
    public boolean hasNext() {
      return (pointer != originalFirst);
    }

    @Override
    @SuppressWarnings("unchecked")
    public E next() {
      final int pointer = this.pointer;
      final int originalFirst = this.originalFirst;
      if (pointer == originalFirst) {
        throw new NoSuchElementException();
      }
      if ((first != originalFirst) || (last != originalLast)) {
        throw new ConcurrentModificationException();
      }
      isRemoved = false;
      return (E) data[this.pointer = (pointer - 1) & mask];
    }

    @Override
    public void remove() {
      if (isRemoved) {
        throw new IllegalStateException("element already removed");
      }
      final int pointer = this.pointer;
      final int originalFirst = this.originalFirst;
      if (pointer == originalFirst) {
        throw new IllegalStateException();
      }
      if ((first != originalFirst) || (last != originalLast)) {
        throw new ConcurrentModificationException();
      }
      if (removeElement(pointer)) {
        this.originalFirst = first;
      } else {
        originalLast = last;
      }
      isRemoved = true;
    }
  }
}

