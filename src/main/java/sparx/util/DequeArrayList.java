/*
 * Copyright 2025 Davide Maestroni
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
import sparx.util.annotation.NotNegative;
import sparx.util.annotation.Positive;
import sparx.util.function.Predicate;

public class DequeArrayList<E> extends AbstractList<E> implements Cloneable, Deque<E>, RandomAccess,
    Serializable {

  private static final int DEFAULT_SIZE = 8;
  private static final Object[] EMPTY_DATA = new Object[0];
  private static final int MAX_BINARY_GROWTH = 64;
  private static final int MIN_SHRINK_SIZE = 16;
  private static final int MIN_SHRINK_THRESHOLD = MIN_SHRINK_SIZE >> 1;

  private final boolean autoShrink;

  private Object[] data;
  private int first;
  private int gapIndex = Integer.MAX_VALUE;
  private int gapOffset = 0;
  private int last;
  private int shrinkLowerThreshold;
  private int shrinkUpperThreshold = Integer.MAX_VALUE;
  private int size;

  /**
   * Creates a new empty list with a pre-defined initial capacity.
   */
  public DequeArrayList() {
    this(false);
  }

  /**
   * Constructs an empty list with a pre-defined initial capacity.
   *
   * @param autoShrink if the capacity automatically shrinks when elements count is below an
   *                   adaptive threshold
   */
  public DequeArrayList(final boolean autoShrink) {
    data = new Object[DEFAULT_SIZE];
    this.autoShrink = autoShrink;
    updateShrinkThreshold();
  }

  /**
   * Constructs an empty list with at least the specified minimum capacity.
   *
   * @param minCapacity the minimum capacity
   * @throws IllegalArgumentException if the specified capacity is less than 1
   */
  public DequeArrayList(@Positive final int minCapacity) {
    this(minCapacity, false);
  }

  /**
   * Constructs an empty list with at least the specified minimum capacity.
   *
   * @param minCapacity the minimum capacity
   * @param autoShrink  if the capacity automatically shrinks when elements count is below an
   *                    adaptive threshold
   * @throws IllegalArgumentException if the specified capacity is less than 0
   */
  public DequeArrayList(@NotNegative final int minCapacity, final boolean autoShrink) {
    if (minCapacity == 0) {
      data = EMPTY_DATA;
    } else {
      data = new Object[computeCapacity(Require.notNegative(minCapacity, "minCapacity"))];
      shrinkUpperThreshold = minCapacity;
    }
    this.autoShrink = autoShrink;
  }

  /**
   * Constructs a list containing the elements of the specified collection, in the order they are
   * returned by the collection's iterator.
   *
   * @param collection the collection whose elements are to be placed into this list
   * @throws NullPointerException if the specified collection is null
   */
  public DequeArrayList(@NotNull final Collection<? extends E> collection) {
    this(collection, false);
  }

  /**
   * Constructs a list containing the elements of the specified collection, in the order they are
   * returned by the collection's iterator.
   *
   * @param collection the collection whose elements are to be placed into this list
   * @param autoShrink if the capacity automatically shrinks when elements count is below an
   *                   adaptive threshold
   * @throws NullPointerException if the specified collection is null
   */
  public DequeArrayList(@NotNull final Collection<? extends E> collection,
      final boolean autoShrink) {
    if (collection.getClass() == DequeArrayList.class) {
      final DequeArrayList<?> other = (DequeArrayList<?>) collection;
      data = Arrays.copyOf(other.data, other.data.length);
      first = other.first;
      last = other.last;
      size = other.size;
    } else if (collection.isEmpty()) {
      data = EMPTY_DATA;
    } else {
      data = new Object[computeCapacity(collection.size())];
      addAll(collection);
    }
    this.autoShrink = autoShrink;
    updateShrinkThreshold();
  }

  private static int computeCapacity(@NotNegative final int minCapacity) {
    final int msb = Integer.highestOneBit(minCapacity);
    if (minCapacity == msb) {
      return msb;
    }
    final int binaryCapacity = msb << 1;
    if (binaryCapacity < minCapacity) {
      throw new IllegalStateException("Maximum size exceeded");
    }
    if (binaryCapacity > MAX_BINARY_GROWTH) {
      final int approximateCapacity = binaryCapacity - (binaryCapacity >> 2);
      if (approximateCapacity >= minCapacity) {
        return approximateCapacity;
      }
    }
    return binaryCapacity;
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
    if (index < 0 || index > size()) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    if (gapOffset > 0) {
      throw new ConcurrentModificationException();
    }
    if (size == data.length) {
      growCapacity();
    }
    addElement(index, element);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean addAll(@NotNull final Collection<? extends E> collection) {
    if (collection.isEmpty()) {
      return false;
    }
    if (gapOffset > 0) {
      throw new ConcurrentModificationException();
    }
    addElements(size, collection);
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean addAll(final int index, @NotNull final Collection<? extends E> collection) {
    if (index < 0 || index > size()) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    if (collection.isEmpty()) {
      return false;
    }
    if (gapOffset > 0) {
      throw new ConcurrentModificationException();
    }
    addElements(index, collection);
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addFirst(@Nullable final E element) {
    if (gapOffset > 0) {
      throw new ConcurrentModificationException();
    }
    if (size == data.length) {
      growCapacity();
    }
    final Object[] data = this.data;
    data[first = modDec(first, 1, data.length)] = element;
    if (++size >= shrinkUpperThreshold) {
      shrinkUpperThreshold = Integer.MAX_VALUE;
      updateShrinkThreshold();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addLast(@Nullable final E element) {
    if (gapOffset > 0) {
      throw new ConcurrentModificationException();
    }
    if (size == data.length) {
      growCapacity();
    }
    final int last = this.last;
    final Object[] data = this.data;
    data[last] = element;
    this.last = modInc(last, 1, data.length);
    if (++size >= shrinkUpperThreshold) {
      shrinkUpperThreshold = Integer.MAX_VALUE;
      updateShrinkThreshold();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void clear() {
    if (gapOffset > 0) {
      throw new ConcurrentModificationException();
    }
    final Object[] data = this.data;
    final int capacity = data.length;
    if (autoShrink && capacity > MIN_SHRINK_SIZE) {
      this.data = new Object[MIN_SHRINK_SIZE];
    } else if (size > 0) {
      final int last = this.last;
      for (int i = first, to = (i < last) ? last : capacity; ; i = 0, to = last) {
        for (; i < to; ++i) {
          data[i] = null;
        }
        if (to == last) {
          break;
        }
      }
    }
    first = 0;
    last = 0;
    size = 0;
  }

  /**
   * Returns a shallow copy of this {@code DequeueList} instance (the elements themselves are not
   * copied).
   *
   * @return a clone of this {@code DequeueList} instance
   */
  @Override
  @SuppressWarnings("unchecked")
  public DequeArrayList<E> clone() {
    try {
      final Object[] data = this.data;
      final DequeArrayList<E> clone = (DequeArrayList<E>) super.clone();
      if (gapOffset > 0) {
        final int first = this.first;
        final int last = this.last;
        final int capacity = data.length;
        final int size = size();
        final int newCapacity = size < shrinkLowerThreshold ? shrinkLowerThreshold << 1 : capacity;
        final Object[] newData = new Object[newCapacity];
        final int gapFirst = modInc(first, gapIndex, capacity);
        final int front = gapFirst - first;
        if (gapFirst > first) {
          System.arraycopy(data, first, newData, 0, front);
        }
        final int gapLast = modInc(gapFirst, gapOffset, capacity);
        if (gapLast < last) {
          System.arraycopy(data, gapLast, newData, front, last - gapLast);
        } else {
          final int back = capacity - gapLast;
          System.arraycopy(data, gapLast, newData, front, back);
          System.arraycopy(data, 0, newData, front + back, last);
        }
        clone.first = 0;
        clone.last = size;
        clone.data = newData;
        clone.gapIndex = Integer.MAX_VALUE;
        clone.gapOffset = 0;
        clone.size = size;
        clone.updateShrinkThreshold();
      } else {
        clone.data = Arrays.copyOf(data, data.length);
      }
      return clone;
    } catch (final CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
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
   * Increases the capacity of this {@code ArrayList} instance, if necessary, to ensure that it can
   * hold at least the number of elements specified by the minimum capacity argument.
   *
   * @param minCapacity the desired minimum capacity
   * @return {@code true} if this collection changed as a result of the call
   * @throws IllegalArgumentException if the specified capacity is less than 0
   */
  public boolean ensureCapacity(@NotNegative final int minCapacity) {
    final Object[] data = this.data;
    if (data.length >= minCapacity) {
      return false;
    }
    if (gapOffset > 0) {
      throw new ConcurrentModificationException();
    }
    resizeCapacity(computeCapacity(Require.notNegative(minCapacity, "minCapacity")));
    return true;
  }

  /**
   * Decreases the capacity of this {@code ArrayList} instance, if possible,
   *
   * @param minCapacity the desired minimum capacity
   * @return {@code true} if this collection changed as a result of the call
   * @throws IllegalArgumentException if the specified capacity is less than 0
   */
  public boolean freeCapacity(@NotNegative final int minCapacity) {
    final Object[] data = this.data;
    final int newCapacity = Math.max(computeCapacity(size),
        computeCapacity(Require.notNegative(minCapacity, "minCapacity")));
    if (data.length <= newCapacity) {
      return false;
    }
    if (gapOffset > 0) {
      throw new ConcurrentModificationException();
    }
    resizeCapacity(newCapacity);
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings("unchecked")
  public E get(int index) {
    if (index < 0 || index >= size()) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    if (index > gapIndex) {
      index += gapOffset;
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
    if (isEmpty()) {
      return -1;
    }
    final int first = this.first;
    final int last = this.last;
    final Object[] data = this.data;
    final int capacity = data.length;
    final int gapOffset = this.gapOffset;
    if (gapOffset > 0) {
      final int gapFirst = modInc(first, gapIndex, capacity);
      if (o == null) {
        for (int i = first, to = (i < gapFirst) ? gapFirst : capacity; ; ) {
          for (; i < to; ++i) {
            if (data[i] == null) {
              return modDec(i, first + gapOffset, capacity);
            }
          }
          if (to == gapFirst) {
            i = modInc(gapFirst, gapOffset, capacity);
            to = (i < last) ? last : capacity;
          } else if (to == capacity) {
            i = 0;
            to = gapFirst;
          } else {
            break;
          }
        }
      } else {
        for (int i = first, to = (i < gapFirst) ? gapFirst : capacity; ; ) {
          for (; i < to; ++i) {
            if (o.equals(data[i])) {
              return modDec(i, first + gapOffset, capacity);
            }
          }
          if (to == gapFirst) {
            i = modInc(gapFirst, gapOffset, capacity);
            to = (i < last) ? last : capacity;
          } else if (to == capacity) {
            i = 0;
            to = gapFirst;
          } else {
            break;
          }
        }
      }
    } else {
      if (o == null) {
        for (int i = first, to = (i < last) ? last : capacity; ; i = 0, to = last) {
          for (; i < to; ++i) {
            if (data[i] == null) {
              return modDec(i, first, capacity);
            }
          }
          if (to == last) {
            break;
          }
        }
      } else {
        for (int i = first, to = (i < last) ? last : capacity; ; i = 0, to = last) {
          for (; i < to; ++i) {
            if (o.equals(data[i])) {
              return modDec(i, first, capacity);
            }
          }
          if (to == last) {
            break;
          }
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
    return size() == 0;
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
    if (isEmpty()) {
      return -1;
    }
    final int first = this.first;
    final int last = this.last;
    final Object[] data = this.data;
    final int capacity = data.length;
    final int gapOffset = this.gapOffset;
    if (gapOffset > 0) {
      final int gapFirst =
          gapIndex == 0 ? modDec(first, 1, capacity) : modInc(first, gapIndex - 1, capacity);
      final int gapLast = modInc(gapFirst, gapOffset, capacity);
      if (o == null) {
        for (int i = modDec(last, 1, capacity), to = (i > gapLast) ? gapLast : 0; ; ) {
          for (; i > to - 1; --i) {
            if (data[i] == null) {
              return modDec(i, first + gapOffset, capacity);
            }
          }
          if (to == gapLast) {
            i = gapFirst;
            to = (i > first) ? first : 0;
          } else if (to == 0) {
            i = (first > gapFirst) ? capacity - 1 : gapLast;
            to = first;
          } else {
            break;
          }
        }
      } else {
        for (int i = modDec(last, 1, capacity), to = (i > gapLast) ? gapLast : 0; ; ) {
          for (; i > to - 1; --i) {
            if (o.equals(data[i])) {
              return modDec(i, first + gapOffset, capacity);
            }
          }
          if (to == gapLast) {
            i = gapFirst;
            to = (i > first) ? first : 0;
          } else if (to == 0) {
            i = (first > gapFirst) ? capacity - 1 : gapLast;
            to = first;
          } else {
            break;
          }
        }
      }
    } else {
      if (o == null) {
        for (int i = modDec(last, 1, capacity), to = (i > first) ? first : 0; ;
            i = capacity - 1, to = first) {
          for (; i > to - 1; --i) {
            if (data[i] == null) {
              return modDec(i, first, capacity);
            }
          }
          if (to == first) {
            break;
          }
        }
      } else {
        for (int i = modDec(last, 1, capacity), to = (i > first) ? first : 0; ;
            i = capacity - 1, to = first) {
          for (; i > to - 1; --i) {
            if (o.equals(data[i])) {
              return modDec(i, first, capacity);
            }
          }
          if (to == first) {
            break;
          }
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
    // TODO => gap
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
   * <p>
   * NOTE: the method might return {@code null} also if list contains {@code null} elements. It's up
   * to the caller to disambiguate the returned value in that case.
   */
  @Override
  public E peek() {
    return peekFirst();
  }

  /**
   * {@inheritDoc}
   * <p>
   * NOTE: the method might return {@code null} also if list contains {@code null} elements. It's up
   * to the caller to disambiguate the returned value in that case.
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
   * <p>
   * NOTE: the method might return {@code null} also if list contains {@code null} elements. It's up
   * to the caller to disambiguate the returned value in that case.
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
   * <p>
   * NOTE: the method might return {@code null} also if list contains {@code null} elements. It's up
   * to the caller to disambiguate the returned value in that case.
   */
  @Override
  public E poll() {
    return pollFirst();
  }

  /**
   * {@inheritDoc}
   * <p>
   * NOTE: the method might return {@code null} also if list contains {@code null} elements. It's up
   * to the caller to disambiguate the returned value in that case.
   */
  @Override
  public E pollFirst() {
    if (isEmpty()) {
      return null;
    }
    if (gapOffset > 0) {
      throw new ConcurrentModificationException();
    }
    return unsafeRemoveFirst();
  }

  /**
   * {@inheritDoc}
   * <p>
   * NOTE: the method might return {@code null} also if list contains {@code null} elements. It's up
   * to the caller to disambiguate the returned value in that case.
   */
  @Override
  public E pollLast() {
    if (isEmpty()) {
      return null;
    }
    if (gapOffset > 0) {
      throw new ConcurrentModificationException();
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
    if (index < 0 || index >= size()) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    if (gapOffset > 0) {
      throw new ConcurrentModificationException();
    }
    final E element = get(index);
    removeElement(index);
    return element;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeRange(final int fromIndex, final int toIndex) {
    final int size = size();
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
    if (gapOffset > 0) {
      throw new ConcurrentModificationException();
    }
    unsafeRemoveRange(fromIndex, toIndex);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean removeAll(@NotNull final Collection<?> collection) {
    Require.notNull(collection, "collection");
    return removeAll(new Predicate<E>() {
      @Override
      public boolean test(final E element) {
        return collection.contains(element);
      }
    });
  }

  /**
   * Removes all of the elements of this collection that satisfy the given predicate.  Errors or
   * runtime exceptions thrown during iteration or by the predicate are relayed to the caller.
   *
   * @param predicate a predicate which returns {@code true} for elements to be removed
   * @return {@code true} if any elements were removed
   * @throws NullPointerException if the specified predicate is {@code null}
   */
  @SuppressWarnings("unchecked")
  public boolean removeAll(@NotNull final Predicate<? super E> predicate) {
    Require.notNull(predicate, "predicate");
    if (isEmpty()) {
      return false;
    }
    final boolean canModify = gapOffset == 0;
    final int first = this.first;
    final int last = this.last;
    final Object[] data = this.data;
    final int capacity = data.length;
    final int end = (first < last) ? last : capacity;
    int newFirst = first;
    int newLast = last;
    boolean next = false;
    int i = first, to = end;
    try {
      // remove from head
      for (; ; i = 0, to = last) {
        for (; i < to; ++i) {
          if (predicate.test((E) data[i])) {
            if (!canModify) {
              throw new ConcurrentModificationException();
            }
            data[i] = null;
          } else {
            newFirst = i;
            if (++i == to) {
              i = 0;
              to = last;
            }
            next = true;
            break;
          }
        }
        if (next || to == last) {
          break;
        }
      }
    } catch (final Exception e) {
      if (this.first != i) {
        this.first = i;
        size -= modDec(i, first, capacity);
        shrinkCapacity();
      }
      throw UncheckedException.throwUnchecked(e);
    }
    next = false;
    try {
      // find next element to remove
      for (; ; i = 0, to = last) {
        for (; i < to; ++i) {
          if (predicate.test((E) data[i])) {
            if (!canModify) {
              throw new ConcurrentModificationException();
            }
            newLast = i;
            if (++i == to) {
              i = 0;
              to = last;
            }
            next = true;
            break;
          }
        }
        if (next || to == last) {
          break;
        }
      }
    } catch (final Exception e) {
      if (this.first != newFirst) {
        this.first = newFirst;
        size -= modDec(newFirst, first, capacity);
        shrinkCapacity();
      }
      throw UncheckedException.throwUnchecked(e);
    }
    if (next) {
      gapIndex = modDec(newLast, newFirst, capacity);
      gapOffset = 1;
    }
    try {
      // shift or remove remaining elements
      for (; ; i = 0, to = last) {
        for (; i < to; ++i) {
          final E element = (E) data[i];
          if (!predicate.test(element)) {
            ++gapIndex;
            data[newLast] = element;
            newLast = modInc(newLast, 1, capacity);
          } else {
            ++gapOffset;
          }
        }
        if (to == last) {
          break;
        }
      }
    } catch (final Exception e) {
      gapIndex = Integer.MAX_VALUE;
      gapOffset = 0;
      this.first = newFirst;
      size -= modDec(newFirst, first, capacity);
      unsafeRemoveRange(modDec(newLast, newFirst, capacity), modDec(i, newFirst, capacity));
      throw UncheckedException.throwUnchecked(e);
    }
    // nullify exceeding elements
    for (i = newLast, to = (newLast <= last) ? last : capacity; ; i = 0, to = last) {
      for (; i < to; ++i) {
        data[i] = null;
      }
      if (to == last) {
        break;
      }
    }
    gapIndex = Integer.MAX_VALUE;
    gapOffset = 0;
    if (this.first != newFirst || this.last != newLast) {
      this.first = newFirst;
      this.last = newLast;
      size = modDec(newLast, newFirst, capacity);
      shrinkCapacity();
      return true;
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public E removeFirst() {
    if (isEmpty()) {
      throw new NoSuchElementException();
    }
    if (gapOffset > 0) {
      throw new ConcurrentModificationException();
    }
    return unsafeRemoveFirst();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean removeFirstOccurrence(final Object o) {
    if (isEmpty()) {
      return false;
    }
    final int first = this.first;
    final int last = this.last;
    final Object[] data = this.data;
    if (o == null) {
      for (int i = first, to = (i < last) ? last : data.length; ; i = 0, to = last) {
        for (; i < to; ++i) {
          if (data[i] == null) {
            if (gapOffset > 0) {
              throw new ConcurrentModificationException();
            }
            removeElement(modDec(i, first, data.length));
            return true;
          }
        }
        if (to == last) {
          break;
        }
      }
    } else {
      for (int i = first, to = (i < last) ? last : data.length; ; i = 0, to = last) {
        for (; i < to; ++i) {
          if (o.equals(data[i])) {
            if (gapOffset > 0) {
              throw new ConcurrentModificationException();
            }
            removeElement(modDec(i, first, data.length));
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
    if (gapOffset > 0) {
      throw new ConcurrentModificationException();
    }
    return unsafeRemoveLast();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean removeLastOccurrence(final Object o) {
    if (isEmpty()) {
      return false;
    }
    final int first = this.first;
    final int last = this.last;
    final Object[] data = this.data;
    if (o == null) {
      for (int i = last, to = (i >= first) ? first : 0; ; i = data.length, to = first) {
        for (i--; i > to - 1; i--) {
          if (data[i] == null) {
            if (gapOffset > 0) {
              throw new ConcurrentModificationException();
            }
            removeElement(modDec(i, first, data.length));
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
            if (gapOffset > 0) {
              throw new ConcurrentModificationException();
            }
            removeElement(modDec(i, first, data.length));
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
  public boolean retainAll(@NotNull final Collection<?> collection) {
    Require.notNull(collection, "collection");
    return removeAll(new Predicate<E>() {
      @Override
      public boolean test(final E element) {
        return !collection.contains(element);
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public E set(final int index, @Nullable final E element) {
    final Object[] data = this.data;
    final E old = get(index);
    data[modInc(first, index + gapOffset, data.length)] = element;
    return old;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int size() {
    return size - gapOffset;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NotNull Object[] toArray() {
    if (isEmpty()) {
      return EMPTY_DATA;
    }
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

  protected int capacity() {
    return data.length;
  }

  private void addElement(final int index, final E element) {
    final int first = this.first;
    final int last = this.last;
    final Object[] data = this.data;
    final int capacity = data.length;
    final int back = size - index;
    final int i = modInc(first, index, capacity);
    if (index >= back) {
      if (back != 0) {
        if (i < last) {
          System.arraycopy(data, i, data, i + 1, back);
        } else {
          final int rightmost = capacity - 1;
          System.arraycopy(data, 0, data, 1, last);
          data[0] = data[rightmost];
          System.arraycopy(data, i, data, i + 1, rightmost - i);
        }
      }
      this.data[i] = element;
      this.last = modInc(last, 1, capacity);
    } else {
      if (index != 0) {
        if (first == 0) {
          data[capacity - 1] = data[0];
          System.arraycopy(data, 1, data, 0, i - 1);
        } else if (first < i) {
          System.arraycopy(data, first, data, first - 1, i - first);
        } else {
          System.arraycopy(data, first, data, first - 1, capacity - first);
          data[capacity - 1] = data[0];
          if (i != 0) {
            System.arraycopy(data, 1, data, 0, i - 1);
          }
        }
        this.data[modDec(i, 1, capacity)] = element;
        this.first = modDec(first, 1, capacity);
      } else {
        this.data[this.first = modDec(first, 1, capacity)] = element;
      }
    }
    if (++size >= shrinkUpperThreshold) {
      shrinkUpperThreshold = Integer.MAX_VALUE;
      updateShrinkThreshold();
    }
  }

  private void addElements(final int index, @NotNull final Collection<? extends E> collection) {
    final int added = collection.size();
    final int totalSize = size + added;
    if (totalSize < 0) {
      throw new IllegalStateException("Maximum size exceeded");
    }
    final int newCapacity = computeCapacity(totalSize);
    final Object[] data = this.data;
    final int capacity = data.length;
    final int first = this.first;
    final int last = this.last;
    if (newCapacity > capacity) {
      final Object[] newData = new Object[newCapacity];
      if (isEmpty()) {
        int i = 0;
        for (final E element : collection) {
          newData[i++] = element;
        }
      } else if (first < last) {
        System.arraycopy(data, first, newData, 0, index);
        int i = index;
        for (final E element : collection) {
          newData[i++] = element;
        }
        System.arraycopy(data, first + index, newData, i, size - index);
      } else {
        final int remainder = capacity - first;
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
      updateShrinkThreshold();
    } else if (first < last || isEmpty()) {
      if (first >= added) {
        final int newFirst = first - added;
        System.arraycopy(data, first, data, newFirst, index);
        int i = newFirst + index;
        for (final E element : collection) {
          data[i++] = element;
        }
        this.first = newFirst;
      } else if (capacity - last >= added) {
        final int shift = first + index;
        System.arraycopy(data, shift, data, shift + added, last - index);
        int i = shift;
        for (final E element : collection) {
          data[i++] = element;
        }
        this.last = modInc(last, added, capacity);
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
      final int remainder = capacity - first;
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
    if ((size += added) >= shrinkUpperThreshold) {
      shrinkUpperThreshold = Integer.MAX_VALUE;
      updateShrinkThreshold();
    }
  }

  @NotNull
  @SuppressWarnings("SuspiciousSystemArraycopy")
  private <T> T[] copyElements(@NotNull final T[] dst) {
    // TODO => gap
    if (size > 0) {
      final int first = this.first;
      final int last = this.last;
      final Object[] data = this.data;
      if (first < last) {
        System.arraycopy(data, first, dst, 0, size);
      } else {
        final int front = data.length - first;
        System.arraycopy(data, first, dst, 0, front);
        System.arraycopy(data, 0, dst, front, last);
      }
    }
    return dst;
  }

  private void growCapacity() {
    final Object[] data = this.data;
    final int capacity = data.length;
    final int first = this.first;
    final int front = capacity - first;
    final Object[] newData = new Object[computeCapacity(capacity + 1)];
    System.arraycopy(data, first, newData, 0, front);
    System.arraycopy(data, 0, newData, front, first);
    this.data = newData;
    this.first = 0;
    last = capacity;
    updateShrinkThreshold();
  }

  private void removeElement(final int index) {
    final int first = this.first;
    final int last = this.last;
    final Object[] data = this.data;
    final int capacity = data.length;
    if (size == shrinkLowerThreshold) {
      final int newCapacity = shrinkLowerThreshold << 1;
      final Object[] newData = new Object[newCapacity];
      final int front = capacity - first;
      if (first < last) {
        final int i = first + index + 1;
        System.arraycopy(data, first, newData, 0, index);
        System.arraycopy(data, i, newData, index, last - i);
      } else if (index < front) {
        final int i = modInc(first, index + 1, capacity);
        System.arraycopy(data, first, newData, 0, index);
        System.arraycopy(data, i, newData, index, front - index - 1);
        System.arraycopy(data, 0, newData, front - 1, last);
      } else {
        final int i = index - front;
        System.arraycopy(data, first, newData, 0, front);
        System.arraycopy(data, 0, newData, front, i);
        System.arraycopy(data, i + 1, newData, index, last - i - 1);
      }
      this.data = newData;
      this.first = 0;
      this.last = size - 1;
      updateShrinkThreshold();
    } else if (index == 0) {
      data[first] = null;
      this.first = modInc(first, 1, capacity);
    } else if (index == size - 1) {
      data[this.last = modDec(last, 1, capacity)] = null;
    } else {
      final int back = size - index;
      if (index <= back) {
        final int remainder = index - capacity + first;
        if (remainder < 0) {
          System.arraycopy(data, first, data, first + 1, index);
        } else {
          final int rightmost = capacity - 1;
          System.arraycopy(data, 0, data, 1, remainder);
          data[0] = data[rightmost];
          System.arraycopy(data, first, data, first + 1, rightmost - first);
        }
        this.data[first] = null;
        this.first = modInc(first, 1, capacity);
      } else {
        final int remainder = last - size + index;
        if (remainder >= 0) {
          System.arraycopy(data, remainder + 1, data, remainder, back);
        } else {
          final int i = first + index;
          final int rightmost = capacity - 1;
          System.arraycopy(data, i + 1, data, i, rightmost - i);
          data[rightmost] = data[0];
          System.arraycopy(data, 1, data, 0, last);
        }
        this.data[this.last = modDec(last, 1, capacity)] = null;
      }
    }
    --size;
  }

  private void resizeCapacity(final int capacity) {
    final Object[] newData = new Object[capacity];
    final int first = this.first;
    final int last = this.last;
    if (first < last) {
      System.arraycopy(data, first, newData, 0, size);
    } else {
      final Object[] data = this.data;
      final int currCapacity = data.length;
      final int front = currCapacity - first;
      System.arraycopy(data, first, newData, 0, currCapacity - first);
      System.arraycopy(data, 0, newData, front, last);
    }
    this.data = newData;
    this.first = 0;
    this.last = size;
    updateShrinkThreshold();
  }

  private void shrinkCapacity() {
    if (size < shrinkLowerThreshold) {
      final int newCapacity = shrinkLowerThreshold << 1;
      if (data.length > newCapacity) {
        resizeCapacity(newCapacity);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private E unsafeRemoveFirst() {
    final Object[] data = this.data;
    final int first = this.first;
    this.first = modInc(first, 1, data.length);
    final Object output = data[first];
    data[first] = null;
    --size;
    shrinkCapacity();
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
    shrinkCapacity();
    return (E) output;
  }

  private void unsafeRemoveRange(final int fromIndex, final int toIndex) {
    final int first = this.first;
    final int last = this.last;
    final Object[] data = this.data;
    final int capacity = data.length;
    final int length = toIndex - fromIndex;
    if (size - length < shrinkLowerThreshold) {
      final int newCapacity = shrinkLowerThreshold << 1;
      final Object[] newData = new Object[newCapacity];
      if (first < last) {
        final int back = size - toIndex;
        System.arraycopy(data, first, newData, 0, fromIndex);
        System.arraycopy(data, toIndex, newData, fromIndex, back);
      } else if (first + fromIndex < capacity) {
        System.arraycopy(data, first, newData, 0, fromIndex);
        final int remainder = first - capacity + toIndex;
        if (remainder > 0) {
          System.arraycopy(data, remainder, newData, fromIndex, last - remainder);
        } else {
          System.arraycopy(data, first + toIndex, newData, fromIndex, -remainder);
          System.arraycopy(data, 0, newData, fromIndex - remainder, last);
        }
      } else {
        final int front = capacity - first;
        final int frontLength = fromIndex - 1;
        final int backIndex = modInc(first, toIndex, capacity);
        System.arraycopy(data, first, newData, 0, front);
        System.arraycopy(data, 0, newData, front, frontLength);
        System.arraycopy(data, backIndex, newData, front + frontLength, last - backIndex);
      }
      this.data = newData;
      this.first = 0;
      this.last = size - length;
      updateShrinkThreshold();
    } else if (first < last) {
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
    } else if (first + fromIndex < capacity) {
      final int remainder = first - capacity + toIndex;
      if (remainder > 0) {
        final int front = length - remainder;
        final int dst = first + front;
        System.arraycopy(data, first, data, dst, front);
        for (int i = first; i < dst; ++i) {
          data[i] = null;
        }
        this.first += front;
        System.arraycopy(data, remainder, data, 0, last - remainder);
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
      final int dst = fromIndex - capacity + first;
      final int src = dst + length;
      System.arraycopy(data, src, data, dst, last - src);
      for (int i = last - length; i < last; ++i) {
        data[i] = null;
      }
      this.last -= length;
    }
    size -= length;
  }

  private void updateShrinkThreshold() {
    if (autoShrink) {
      final int capacity = data.length;
      shrinkLowerThreshold = capacity > MIN_SHRINK_SIZE ? Math.max(MIN_SHRINK_THRESHOLD,
          computeCapacity((capacity >> 3) - 1)) : 0;
    }
  }

  private class AscendingIterator implements Iterator<E> {

    protected int expectedFirst = first;
    protected int expectedLast = last;
    protected int index;
    protected boolean isRemoved = true;

    private AscendingIterator(final int offset) {
      index = offset;
    }

    @Override
    public boolean hasNext() {
      return (index != size);
    }

    @Override
    @SuppressWarnings("unchecked")
    public E next() {
      checkForComodification();
      if (index == size) {
        throw new NoSuchElementException();
      }
      isRemoved = false;
      final Object[] data = DequeArrayList.this.data;
      return (E) data[modInc(index++, first, data.length)];
    }

    @Override
    public void remove() {
      if (isRemoved) {
        throw new IllegalStateException();
      }
      checkForComodification();
      removeElement(--index);
      expectedFirst = first;
      expectedLast = last;
      isRemoved = true;
    }

    final void checkForComodification() {
      if (first != expectedFirst || last != expectedLast) {
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
      if (size == data.length) {
        growCapacity();
      }
      addElement(index++, e);
      expectedFirst = first;
      expectedLast = last;
      isRemoved = true; // disable remove
    }

    @Override
    public boolean hasPrevious() {
      return (index != 0);
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
      return index;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E previous() {
      checkForComodification();
      if (index == 0) {
        throw new NoSuchElementException();
      }
      isForward = false;
      isRemoved = false;
      final Object[] data = DequeArrayList.this.data;
      return (E) data[modInc(--index, first, data.length)];
    }

    @Override
    public int previousIndex() {
      checkForComodification();
      return index - 1;
    }

    @Override
    public void remove() {
      if (!isForward) {
        if (isRemoved || index == size) {
          throw new IllegalStateException();
        }
        checkForComodification();
        removeElement(index);
        expectedFirst = first;
        expectedLast = last;
        isRemoved = true;
      } else {
        if (index == 0) {
          throw new IllegalStateException();
        }
        super.remove();
      }
    }

    @Override
    public void set(final E element) {
      checkForComodification();
      if (size == 0) {
        throw new IndexOutOfBoundsException("0");
      }
      final Object[] data = DequeArrayList.this.data;
      final int capacity = data.length;
      if (isForward) {
        data[modInc(index - 1, first, capacity)] = element;
      } else {
        data[modInc(index, first, capacity)] = element;
      }
    }
  }

  private class DescendingIterator implements Iterator<E> {

    private int expectedFirst = first;
    private int expectedLast = last;
    private boolean isRemoved = true;
    private int index = size;

    @Override
    public boolean hasNext() {
      return (index > 0);
    }

    @Override
    @SuppressWarnings("unchecked")
    public E next() {
      checkForComodification();
      if (index == 0) {
        throw new NoSuchElementException();
      }
      isRemoved = false;
      final Object[] data = DequeArrayList.this.data;
      return (E) data[modInc(--index, first, data.length)];
    }

    @Override
    public void remove() {
      if (isRemoved) {
        throw new IllegalStateException();
      }
      checkForComodification();
      removeElement(index);
      expectedFirst = first;
      expectedLast = last;
      isRemoved = true;
    }

    private void checkForComodification() {
      if (first != expectedFirst || last != expectedLast) {
        throw new ConcurrentModificationException();
      }
    }
  }
}

