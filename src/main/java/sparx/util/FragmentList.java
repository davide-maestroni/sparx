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
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;
import sparx.util.annotation.NotNegative;
import sparx.util.annotation.Positive;

public class FragmentList<E> extends AbstractList<E> implements Cloneable, Serializable {

  private static final int DEFAULT_CHUNK_SIZE = 4;
  private static final int DEFAULT_INITIAL_CAPACITY = 8;
  private static final int DEFAULT_MAX_FRAGMENTS = 64;
  private static final Object[] EMPTY_DATA = new Object[0];

  private final int initialCapacity;
  private final int maxFragments;
  private final int minCapacity;

  private Fragment head;
  private int numFragments;
  private Fragment pointer;
  private int size;
  private Fragment tail;

  public FragmentList() {
    this(DEFAULT_INITIAL_CAPACITY);
  }

  public FragmentList(@NotNegative final int initialCapacity) {
    this(initialCapacity, DEFAULT_MAX_FRAGMENTS);
  }

  public FragmentList(@NotNegative final int initialCapacity, @Positive final int maxFragments) {
    this.initialCapacity = Require.notNegative(initialCapacity, "initialCapacity");
    this.maxFragments = Require.positive(maxFragments, "maxFragments");
    minCapacity = Math.max(DEFAULT_INITIAL_CAPACITY, initialCapacity);
    numFragments = 1;
    if (initialCapacity == 0) {
      head = tail = new OpenFragment(new EmptyChunk());
    } else {
      head = tail = new OpenFragment(new Chunk(initialCapacity));
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean add(final E element) {
    if (size == Integer.MAX_VALUE) {
      throw new OutOfMemoryError();
    }
    final Chunk chunk = tail.chunk;
    chunk.append(element);
    if (chunk.first == chunk.last) {
      mergeFragmentsLeft(tail);
    }
    ++size;
    ++modCount;
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void add(final int index, final E element) {
    if (index < 0 || index > size) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    if (index == 0) {
      addFirst(element);
    } else {
      final int relIndex = goToIndex(index);
      addToFragment(pointer, relIndex, element);
    }
  }

  @Override
  public boolean addAll(@NotNull final Collection<? extends E> collection) {
    for (final E element : collection) {
      addLast(element);
    }
    return true;
  }

  @Override
  public boolean addAll(int index, @NotNull final Collection<? extends E> collection) {
    if (index < 0 || index > size) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    if (index == size - 1) {
      return addAll(collection);
    }
    int relIndex = goToIndex(index);
    Fragment fragment = pointer;
    for (final E element : collection) {
      if (addToFragment(fragment, relIndex++, element)) {
        relIndex = goToIndex(++index);
        fragment = pointer;
      } else {
        ++index;
      }
    }
    return true;
  }

  public boolean addFirst(final E element) {
    if (size == Integer.MAX_VALUE) {
      throw new OutOfMemoryError();
    }
    final Chunk chunk = head.chunk;
    chunk.prepend(element);
    if (chunk.first == chunk.last) {
      mergeFragmentsRight(head);
    }
    ++size;
    ++modCount;
    return true;
  }

  public boolean addLast(final E element) {
    return add(element);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void clear() {
    final int size = this.size;
    if (size > 0) {
      if (numFragments > 1 || size > minCapacity) {
        head = tail = new OpenFragment(new Chunk(minCapacity));
      } else {
        final Chunk chunk = head.chunk;
        while (chunk.first != chunk.last) {
          chunk.removeLast();
        }
      }
      this.size = 0;
      ++modCount;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings("unchecked")
  public @NotNull FragmentList<E> clone() throws CloneNotSupportedException {
    final FragmentList<E> clone = (FragmentList<E>) super.clone();
    clone.mergeAllFragments();
    return clone;
  }

  public void defrag() {
    if (numFragments > 1) {
      mergeAllFragments();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings("unchecked")
  public E get(int index) {
    if (index < 0 || index >= size) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    final int relIndex = goToIndex(index);
    return (E) pointer.get(relIndex);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int indexOf(final Object o) {
    int index = 0;
    Fragment fragment = head;
    if (o == null) {
      do {
        final int size = fragment.size();
        for (int i = 0; i < size; ++i) {
          if (fragment.get(i) == null) {
            return index;
          }
          ++index;
        }
        fragment = fragment.next;
      } while (fragment != null);
    } else {
      do {
        final int size = fragment.size();
        for (int i = 0; i < size; ++i) {
          if (o.equals(fragment.get(i))) {
            return index;
          }
          ++index;
        }
        fragment = fragment.next;
      } while (fragment != null);
    }
    return -1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NotNull Iterator<E> iterator() {
    return listIterator();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int lastIndexOf(final Object o) {
    int index = size - 1;
    Fragment fragment = tail;
    if (o == null) {
      do {
        final int size = fragment.size();
        for (int i = size - 1; i >= 0; --i) {
          if (fragment.get(i) == null) {
            return index;
          }
          --index;
        }
        fragment = fragment.next;
      } while (fragment != null);
    } else {
      do {
        final int size = fragment.size();
        for (int i = size - 1; i >= 0; --i) {
          if (o.equals(fragment.get(i))) {
            return index;
          }
          --index;
        }
        fragment = fragment.next;
      } while (fragment != null);
    }
    return -1;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public @NotNull ListIterator<E> listIterator(int index) {
    if (index < 0 || index > size) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    final int relIndex = goToIndex(index);
    return new FragmentListIterator(pointer, relIndex, index);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings("unchecked")
  public E remove(int index) {
    if (index < 0 || index >= size) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    if (index == 0) {
      return removeFirst();
    }
    final int relIndex = goToIndex(index);
    final Fragment fragment = pointer;
    final E element = (E) fragment.get(relIndex);
    removeFromFragment(fragment, relIndex);
    return element;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean remove(final Object o) {
    Fragment fragment = head;
    if (o == null) {
      do {
        final int size = fragment.size();
        for (int i = 0; i < size; ++i) {
          if (fragment.get(i) == null) {
            removeFromFragment(fragment, i);
            return true;
          }
        }
        fragment = fragment.next;
      } while (fragment != null);
    } else {
      do {
        final int size = fragment.size();
        for (int i = 0; i < size; ++i) {
          if (o.equals(fragment.get(i))) {
            removeFromFragment(fragment, i);
            return true;
          }
        }
        fragment = fragment.next;
      } while (fragment != null);
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean removeAll(@NotNull final Collection<?> collection) {
    boolean modified = false;
    Fragment fragment = head;
    int pos = 0;
    do {
      for (int i = 0; i < fragment.size(); ) {
        if (collection.contains(fragment.get(i))) {
          if (removeFromFragment(fragment, i)) {
            i = goToIndex(pos);
            fragment = pointer;
          }
          modified = true;
        } else {
          ++pos;
          ++i;
        }
      }
      fragment = fragment.next;
    } while (fragment != null);
    return modified;
  }

  @SuppressWarnings("unchecked")
  public E removeFirst() {
    final Fragment fragment = head;
    final E element = (E) fragment.get(0);
    removeFromFragment(fragment, 0);
    return element;
  }

  @SuppressWarnings("unchecked")
  public E removeLast() {
    final Fragment fragment = tail;
    final int index = fragment.size() - 1;
    final E element = (E) fragment.get(index);
    removeFromFragment(fragment, index);
    return element;
  }

  @Override
  public boolean retainAll(@NotNull final Collection<?> collection) {
    boolean modified = false;
    Fragment fragment = head;
    int pos = 0;
    do {
      for (int i = 0; i < fragment.size(); ) {
        if (!collection.contains(fragment.get(i))) {
          if (removeFromFragment(fragment, i)) {
            i = goToIndex(pos);
            fragment = pointer;
          }
          modified = true;
        } else {
          ++pos;
          ++i;
        }
      }
      fragment = fragment.next;
    } while (fragment != null);
    return modified;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings("unchecked")
  public E set(final int index, final E element) {
    if (index < 0 || index >= size) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    final int relIndex = goToIndex(index);
    final E old = (E) pointer.set(relIndex, element);
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
    final Object[] data = new Object[size];
    Fragment fragment = head;
    int offset = 0;
    while (fragment != null) {
      offset = fragment.copyData(offset, data);
      fragment = fragment.next;
    }
    return data;
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
      Fragment fragment = head;
      int offset = 0;
      while (fragment != null) {
        offset = fragment.copyData(offset, array);
        fragment = fragment.next;
      }
    } else {
      Fragment fragment = head;
      int offset = 0;
      while (fragment != null) {
        offset = fragment.copyData(offset, array);
        fragment = fragment.next;
      }
      if (array.length > size) {
        array[size] = null;
      }
    }
    return array;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void removeRange(final int fromIndex, final int toIndex) {
    int index = goToIndex(fromIndex);
    Fragment fragment = pointer;
    for (int i = fromIndex; i < toIndex; ++i) {
      if (removeFromFragment(fragment, index)) {
        index = goToIndex(fromIndex);
        fragment = pointer;
      }
    }
  }

  private boolean addToFragment(@NotNull final Fragment fragment, final int index,
      final E element) {
    if (size == Integer.MAX_VALUE) {
      throw new OutOfMemoryError();
    }
    boolean fragmentsModified = false;
    if (index == 0) {
      final Fragment prevFragment = fragment.prev;
      if (!fragment.isOpenLeft() && !prevFragment.isOpenRight()) {
        final Chunk chunk = fragment.chunk;
        final int indexStart = (fragment.getIndexStart() - 1) & chunk.mask;
        fragment.setIndexStart(indexStart);
        chunk.insert(indexStart, element);
        if (prevFragment.getIndexEnd() == fragment.getIndexStart()) {
          if (fragment.isOpenRight()) {
            insertFragment(prevFragment.openRight(), prevFragment.prev, fragment.next);
          } else {
            prevFragment.setIndexEnd(fragment.getIndexEnd());
            final Fragment nextFragment = fragment.next;
            prevFragment.next = nextFragment;
            if (nextFragment != null) {
              nextFragment.prev = prevFragment;
            } else {
              tail = prevFragment;
            }
          }
          --numFragments;
          fragmentsModified = true;
        }
      } else if (fragment.isOpenLeft()) {
        final Chunk chunk = fragment.chunk;
        chunk.prepend(element);
        if (chunk.first == chunk.last) {
          fragmentsModified = mergeFragmentsRight(fragment);
        }
      } else {
        final Chunk chunk = prevFragment.chunk;
        chunk.append(element);
        if (chunk.first == chunk.last) {
          fragmentsModified = mergeFragmentsLeft(prevFragment);
        }
      }
    } else if (index == fragment.size()) {
      final Chunk chunk = fragment.chunk;
      chunk.append(element);
      if (chunk.first == chunk.last) {
        fragmentsModified = mergeFragmentsLeft(fragment);
      }
    } else {
      final Chunk chunk = fragment.chunk;
      final Chunk newChunk = new Chunk();
      newChunk.append(element);
      final Fragment middleFragment = new OpenFragment(newChunk);
      final Fragment leftFragment = fragment.closeRight();
      final Fragment rightFragment = fragment.closeLeft();
      final int offset = fragment.getIndexStart();
      final int indexEnd = (offset + index) & chunk.mask;
      leftFragment.setIndexEnd(indexEnd);
      rightFragment.setIndexStart(indexEnd);
      final Fragment prevFragment = fragment.prev;
      leftFragment.prev = prevFragment;
      if (prevFragment != null) {
        prevFragment.next = leftFragment;
      } else {
        head = leftFragment;
      }
      leftFragment.next = middleFragment;
      middleFragment.prev = leftFragment;
      middleFragment.next = rightFragment;
      rightFragment.prev = middleFragment;
      final Fragment nextFragment = fragment.next;
      rightFragment.next = nextFragment;
      if (nextFragment != null) {
        nextFragment.prev = rightFragment;
      } else {
        tail = rightFragment;
      }
      numFragments += 2;
      fragmentsModified = true;
      if (numFragments > maxFragments) {
        mergeAllFragments();
      }
    }
    ++size;
    ++modCount;
    return fragmentsModified;
  }

  private int goToIndex(int index) {
    Fragment fragment;
    if (numFragments == 1) {
      fragment = head;
    } else if (index > size >> 1) {
      index = size - index - 1;
      fragment = tail;
      int size;
      while ((size = fragment.size()) <= index) {
        index -= size;
        fragment = fragment.prev;
      }
      index = fragment.size() - index - 1;
    } else {
      fragment = head;
      int size;
      while ((size = fragment.size()) <= index) {
        index -= size;
        fragment = fragment.next;
      }
    }
    pointer = fragment;
    return index;
  }

  private void insertFragment(@NotNull final Fragment fragment, final Fragment prevFragment,
      final Fragment nextFragment) {
    fragment.prev = prevFragment;
    if (prevFragment != null) {
      prevFragment.next = fragment;
    } else {
      head = fragment;
    }
    fragment.next = nextFragment;
    if (nextFragment != null) {
      nextFragment.prev = fragment;
    } else {
      tail = fragment;
    }
  }

  private void mergeAllFragments() {
    final int chunkSize = Math.max(initialCapacity, size);
    if (chunkSize == 0) {
      head = tail = new OpenFragment(new EmptyChunk());
    } else {
      final Chunk newChunk = new Chunk(chunkSize);
      final Object[] newData = newChunk.data;
      Fragment fragment = head;
      int offset = 0;
      while (fragment != null) {
        offset = fragment.copyData(offset, newData);
        fragment = fragment.next;
      }
      newChunk.first = 0;
      newChunk.last = newChunk.used = size;
      head = tail = new OpenFragment(newChunk);
    }
    ++modCount;
  }

  private boolean mergeFragmentsLeft(@NotNull final Fragment fragment) {
    boolean fragmentsModified = false;
    if (fragment.isOpen()) {
      resizeChunk(fragment);
    } else {
      final Chunk chunk = fragment.chunk;
      final Fragment nextFragment = fragment.next;
      int removedFragments = 1;
      int totalSize = fragment.size();
      Fragment startFragment = fragment.prev;
      while (!startFragment.isOpenLeft() || startFragment.chunk != chunk) {
        ++removedFragments;
        totalSize += startFragment.size();
        startFragment = startFragment.prev;
      }
      totalSize += startFragment.size();
      final Fragment prevFragment = startFragment.prev;
      final Chunk newChunk = new Chunk(Math.max(DEFAULT_CHUNK_SIZE, totalSize + 1));
      final Object[] newData = newChunk.data;
      int offset = 0;
      do {
        offset = startFragment.copyData(offset, newData);
        startFragment = startFragment.next;
      } while (startFragment != nextFragment);
      newChunk.first = 0;
      newChunk.last = newChunk.used = totalSize;
      insertFragment(new OpenFragment(newChunk), prevFragment, nextFragment);
      numFragments -= removedFragments;
      fragmentsModified = true;
    }
    ++modCount;
    return fragmentsModified;
  }

  private boolean mergeFragmentsRight(@NotNull final Fragment fragment) {
    boolean fragmentsModified = false;
    if (fragment.isOpen()) {
      resizeChunk(fragment);
    } else {
      final Chunk chunk = fragment.chunk;
      int removedFragments = 1;
      int totalSize = fragment.size();
      Fragment endFragment = fragment.next;
      while (!endFragment.isOpenRight() || endFragment.chunk != chunk) {
        ++removedFragments;
        totalSize += endFragment.size();
        endFragment = endFragment.next;
      }
      totalSize += endFragment.size();
      final Fragment nextFragment = endFragment.next;
      final Chunk newChunk = new Chunk(Math.max(DEFAULT_CHUNK_SIZE, totalSize + 1));
      final Object[] newData = newChunk.data;
      Fragment startFragment = fragment;
      int offset = 0;
      do {
        offset = startFragment.copyData(offset, newData);
        startFragment = startFragment.next;
      } while (startFragment != nextFragment);
      newChunk.first = 0;
      newChunk.last = newChunk.used = totalSize;
      insertFragment(new OpenFragment(newChunk), fragment.prev, nextFragment);
      numFragments -= removedFragments;
      fragmentsModified = true;
    }
    ++modCount;
    return fragmentsModified;
  }

  /*
  - no fragment with length 0
  - head is open left and tail is open right
  - if fragment is open left perv fragment is closed right
  - if fragment is open right perv fragment is closed left
  - when hole in chunk fragments before and after are adjacent
   */

  private boolean removeFromFragment(@NotNull Fragment fragment, final int index) {
    boolean fragmentsModified = false;
    if (index == 0) {
      final Chunk chunk = fragment.chunk;
      if (fragment.isOpenLeft()) {
        chunk.removeFirst();
      } else {
        final int indexStart = fragment.getIndexStart();
        chunk.remove(indexStart);
        fragment.setIndexStart((indexStart + 1) & chunk.mask);
      }
    } else if (fragment.isOpenRight() && index == fragment.size() - 1) {
      fragment.chunk.removeLast();
    } else {
      final Chunk chunk = fragment.chunk;
      final Fragment leftFragment = fragment.closeRight();
      final Fragment rightFragment = fragment.closeLeft();
      final int indexEnd = (fragment.getIndexStart() + index) & chunk.mask;
      leftFragment.setIndexEnd(indexEnd);
      rightFragment.setIndexStart((indexEnd + 1) & chunk.mask);
      chunk.remove(indexEnd);
      final Fragment prevFragment = fragment.prev;
      leftFragment.prev = prevFragment;
      if (prevFragment != null) {
        prevFragment.next = leftFragment;
      } else {
        head = leftFragment;
      }
      leftFragment.next = rightFragment;
      rightFragment.prev = leftFragment;
      final Fragment nextFragment = fragment.next;
      rightFragment.next = nextFragment;
      if (nextFragment != null) {
        nextFragment.prev = rightFragment;
      } else {
        tail = rightFragment;
      }
      fragment = rightFragment;
      ++numFragments;
      fragmentsModified = true;
      if (numFragments > maxFragments) {
        mergeAllFragments();
        fragment = head;
      }
    }
    if (fragment.size() == 0 && numFragments > 1) {
      final Fragment prevFragment = fragment.prev;
      final Fragment nextFragment = fragment.next;
      if (prevFragment != null) {
        prevFragment.next = nextFragment;
        if (fragment.isOpenRight()) {
          if (prevFragment.isOpenRight()) {
            --size;
            return mergeFragmentsLeft(fragment);
          } else if (!fragment.isOpenLeft()) {
            final Chunk chunk = prevFragment.chunk;
            final int indexEnd = prevFragment.getIndexEnd();
            do {
              chunk.removeLast();
            } while (chunk.last != indexEnd);
            insertFragment(prevFragment.openRight(), prevFragment.prev, prevFragment.next);
          }
        }
      } else {
        head = nextFragment;
      }
      if (nextFragment != null) {
        nextFragment.prev = prevFragment;
        if (fragment.isOpenLeft()) {
          if (nextFragment.isOpenLeft()) {
            --size;
            return mergeFragmentsRight(fragment);
          } else if (!fragment.isOpenRight()) {
            final Chunk chunk = nextFragment.chunk;
            final int indexStart = nextFragment.getIndexStart();
            do {
              chunk.removeFirst();
            } while (chunk.first != indexStart);
            insertFragment(nextFragment.openLeft(), nextFragment.prev, nextFragment.next);
          }
        }
      } else {
        tail = prevFragment;
      }
      if (prevFragment != null && prevFragment.isOpenLeft() && nextFragment != null
          && nextFragment.isOpenRight()
          && prevFragment.getIndexEnd() == nextFragment.getIndexStart()) {
        insertFragment(prevFragment.openRight(), prevFragment.prev, nextFragment.next);
        numFragments -= 2;
      } else {
        --numFragments;
      }
      fragmentsModified = true;
    } else {
      final Chunk chunk = fragment.chunk;
      if (chunk.used < chunk.data.length >> 2) {
        if (fragment.isOpenLeft()) {
          mergeFragmentsRight(fragment);
        } else if (fragment.isOpenRight()) {
          mergeFragmentsLeft(fragment);
        } else {
          do {
            fragment = fragment.next;
          } while (!fragment.isOpenRight() || fragment.chunk != chunk);
          mergeFragmentsLeft(fragment);
        }
      }
    }
    --size;
    ++modCount;
    return fragmentsModified;
  }

  private void resizeChunk(@NotNull final Fragment fragment) {
    final Chunk chunk = fragment.chunk;
    final Object[] data = chunk.data;
    if (size == 0) {
      return;
    }
    if (chunk.first == chunk.last) {
      // double capacity
      final int size = data.length;
      final Chunk newChunk = new Chunk(size << 1);
      final int first = chunk.first;
      final int remainder = size - first;
      final Object[] newData = newChunk.data;
      System.arraycopy(data, first, newData, 0, remainder);
      System.arraycopy(data, 0, newData, remainder, first);
      newChunk.first = 0;
      newChunk.last = newChunk.used = size;
      fragment.chunk = newChunk;
    } else {
      final int size = fragment.size();
      final Chunk newChunk = new Chunk(Math.max(DEFAULT_CHUNK_SIZE, size << 1));
      final Object[] newData = newChunk.data;
      final int first = chunk.first;
      final int last = chunk.last;
      if (first < last) {
        System.arraycopy(data, first, newData, 0, last - first);
      } else {
        final int remainder = data.length - first;
        System.arraycopy(data, first, newData, 0, remainder);
        System.arraycopy(data, 0, newData, remainder, last);
      }
      newChunk.first = 0;
      newChunk.last = newChunk.used = size;
      fragment.chunk = newChunk;
    }
  }

  private static class Chunk {

    private final Object[] data;
    private final int mask;

    private int first;
    private int last;
    private int used;

    private Chunk() {
      data = new Object[DEFAULT_CHUNK_SIZE];
      mask = 3;
    }

    private Chunk(@NotNull final Object[] data) {
      this.data = data;
      mask = data.length - 1;
    }

    private Chunk(@Positive final int minCapacity) {
      final int msb = Integer.highestOneBit(minCapacity);
      final int initialCapacity = (minCapacity == msb) ? msb : msb << 1;
      data = new Object[initialCapacity];
      mask = initialCapacity - 1;
    }

    void append(final Object element) {
      data[last] = element;
      last = (last + 1) & mask;
      ++used;
    }

    void insert(final int index, final Object element) {
      data[index] = element;
      ++used;
    }

    void prepend(final Object element) {
      first = (first - 1) & mask;
      data[first] = element;
      ++used;
    }

    void remove(final int index) {
      data[index] = null;
      --used;
    }

    void removeFirst() {
      data[first] = null;
      first = (first + 1) & mask;
      --used;
    }

    void removeLast() {
      last = (last - 1) & mask;
      data[last] = null;
      --used;
    }
  }

  private static abstract class Fragment {

    Chunk chunk;
    Fragment next;
    Fragment prev;

    private Fragment(@NotNull final Chunk chunk) {
      this.chunk = chunk;
    }

    abstract @NotNull Fragment closeLeft();

    abstract @NotNull Fragment closeRight();

    int copyData(@Positive final int offset, @NotNull final Object[] dstData) {
      final int first = getIndexStart();
      final int last = getIndexEnd();
      if (first == last) {
        return offset;
      }
      final Chunk chunk = this.chunk;
      final Object[] data = chunk.data;
      if (first < last) {
        System.arraycopy(data, first, dstData, offset, last - first);
      } else {
        final int remainder = data.length - first;
        System.arraycopy(data, first, dstData, offset, remainder);
        System.arraycopy(data, 0, dstData, offset + remainder, last);
      }
      final int size = last > first ? last - first : last - first + chunk.data.length;
      return offset + size;
    }

    Object get(final int index) {
      final Chunk chunk = this.chunk;
      return chunk.data[(getIndexStart() + index) & chunk.mask];
    }

    abstract int getIndexEnd();

    abstract void setIndexEnd(int indexEnd);

    abstract int getIndexStart();

    abstract void setIndexStart(int indexStart);

    abstract boolean isOpen();

    abstract boolean isOpenLeft();

    abstract boolean isOpenRight();

    abstract @NotNull Fragment openLeft();

    abstract @NotNull Fragment openRight();

    Object set(int index, final Object element) {
      final Chunk chunk = this.chunk;
      final Object[] data = chunk.data;
      index = (getIndexStart() + index) & chunk.mask;
      final Object old = data[index];
      data[index] = element;
      return old;
    }

    int size() {
      final Chunk chunk = this.chunk;
      final int size = getIndexEnd() - getIndexStart();
      return size >= 0 ? size : size + chunk.data.length;
    }
  }

  private static class ClosedFragment extends Fragment {

    private int indexEnd;
    private int indexStart;

    private ClosedFragment(@NotNull final Chunk chunk) {
      super(chunk);
    }

    @Override
    @NotNull
    Fragment closeLeft() {
      return copy();
    }

    @Override
    @NotNull
    Fragment closeRight() {
      return copy();
    }

    @Override
    int getIndexEnd() {
      return indexEnd;
    }

    @Override
    void setIndexEnd(final int indexEnd) {
      this.indexEnd = indexEnd;
    }

    @Override
    int getIndexStart() {
      return indexStart;
    }

    @Override
    void setIndexStart(final int indexStart) {
      this.indexStart = indexStart;
    }

    @Override
    boolean isOpen() {
      return false;
    }

    @Override
    boolean isOpenLeft() {
      return false;
    }

    @Override
    boolean isOpenRight() {
      return false;
    }

    @Override
    @NotNull
    Fragment openLeft() {
      final OpenLeftFragment fragment = new OpenLeftFragment(chunk);
      fragment.setIndexEnd(indexEnd);
      return fragment;
    }

    @Override
    @NotNull
    Fragment openRight() {
      final OpenRightFragment fragment = new OpenRightFragment(chunk);
      fragment.setIndexStart(indexStart);
      return fragment;
    }

    private @NotNull Fragment copy() {
      final ClosedFragment fragment = new ClosedFragment(chunk);
      fragment.setIndexStart(indexStart);
      fragment.setIndexEnd(indexEnd);
      return fragment;
    }
  }

  private static class OpenFragment extends Fragment {

    private OpenFragment(@NotNull final Chunk chunk) {
      super(chunk);
    }

    @Override
    @NotNull
    Fragment closeLeft() {
      return new OpenRightFragment(chunk);
    }

    @Override
    @NotNull
    Fragment closeRight() {
      return new OpenLeftFragment(chunk);
    }

    @Override
    int getIndexEnd() {
      return chunk.last;
    }

    @Override
    void setIndexEnd(final int indexEnd) {
    }

    @Override
    int getIndexStart() {
      return chunk.first;
    }

    @Override
    void setIndexStart(final int indexStart) {
    }

    @Override
    boolean isOpen() {
      return true;
    }

    @Override
    boolean isOpenLeft() {
      return true;
    }

    @Override
    boolean isOpenRight() {
      return true;
    }

    @Override
    @NotNull
    Fragment openLeft() {
      return new OpenFragment(chunk);
    }

    @Override
    @NotNull
    Fragment openRight() {
      return new OpenFragment(chunk);
    }
  }

  private static class OpenLeftFragment extends Fragment {

    private int indexEnd;

    private OpenLeftFragment(@NotNull final Chunk chunk) {
      super(chunk);
    }

    @Override
    @NotNull
    Fragment closeLeft() {
      final ClosedFragment fragment = new ClosedFragment(chunk);
      fragment.setIndexEnd(indexEnd);
      return fragment;
    }

    @Override
    @NotNull
    Fragment closeRight() {
      return copy();
    }

    @Override
    int getIndexEnd() {
      return indexEnd;
    }

    @Override
    void setIndexEnd(final int indexEnd) {
      this.indexEnd = indexEnd;
    }

    @Override
    int getIndexStart() {
      return chunk.first;
    }

    @Override
    void setIndexStart(final int indexStart) {
    }

    @Override
    boolean isOpen() {
      return false;
    }

    @Override
    boolean isOpenLeft() {
      return true;
    }

    @Override
    boolean isOpenRight() {
      return false;
    }

    @Override
    @NotNull
    Fragment openLeft() {
      return copy();
    }

    @Override
    @NotNull
    Fragment openRight() {
      return new OpenFragment(chunk);
    }

    private @NotNull Fragment copy() {
      final OpenLeftFragment fragment = new OpenLeftFragment(chunk);
      fragment.setIndexEnd(indexEnd);
      return fragment;
    }
  }

  private static class OpenRightFragment extends Fragment {

    private int indexStart;

    private OpenRightFragment(@NotNull final Chunk chunk) {
      super(chunk);
    }

    @Override
    @NotNull
    Fragment closeLeft() {
      return copy();
    }

    @Override
    @NotNull
    Fragment closeRight() {
      final ClosedFragment fragment = new ClosedFragment(chunk);
      fragment.setIndexStart(indexStart);
      return fragment;
    }

    @Override
    int getIndexEnd() {
      return chunk.last;
    }

    @Override
    void setIndexEnd(final int indexEnd) {
    }

    @Override
    int getIndexStart() {
      return indexStart;
    }

    @Override
    void setIndexStart(final int indexStart) {
      this.indexStart = indexStart;
    }

    @Override
    boolean isOpen() {
      return false;
    }

    @Override
    boolean isOpenLeft() {
      return false;
    }

    @Override
    boolean isOpenRight() {
      return true;
    }

    @Override
    @NotNull
    Fragment openLeft() {
      return new OpenFragment(chunk);
    }

    @Override
    @NotNull
    Fragment openRight() {
      return copy();
    }

    private @NotNull Fragment copy() {
      final OpenRightFragment fragment = new OpenRightFragment(chunk);
      fragment.setIndexStart(indexStart);
      return fragment;
    }
  }

  private class EmptyChunk extends Chunk {

    private EmptyChunk() {
      super(EMPTY_DATA);
    }

    @Override
    void append(final Object element) {
      init().append(element);
    }

    @Override
    void insert(final int index, final Object element) {
      init().insert(index, element);
    }

    @Override
    void prepend(final Object element) {
      init().prepend(element);
    }

    @Override
    void remove(final int index) {
      init().remove(index);
    }

    @Override
    void removeFirst() {
      init().removeFirst();
    }

    @Override
    void removeLast() {
      init().removeLast();
    }

    private @NotNull Chunk init() {
      return (head.chunk = new Chunk(2));
    }
  }

  private class FragmentListIterator implements ListIterator<E> {

    private static final int BACKWARD = -1;
    private static final int FORWARD = 1;
    private static final int NONE = 0;

    private int dir = NONE;
    private int expectedModCount = modCount;
    private Fragment fragment;
    private int index;
    private int pos;

    private FragmentListIterator(@NotNull final Fragment fragment, final int index, final int pos) {
      this.fragment = fragment;
      this.index = index;
      this.pos = pos;
    }

    @Override
    public void add(final E element) {
      checkForComodification();
      final int size = fragment.size();
      if (addToFragment(fragment, index, element)) {
        index = goToIndex(++pos);
        fragment = pointer;
      } else {
        if (size != fragment.size()) {
          ++index;
        }
        ++pos;
      }
      expectedModCount = modCount;
    }

    @Override
    public boolean hasNext() {
      checkForComodification();
      return (index < fragment.size()) || fragment.next != null;
    }

    @Override
    public boolean hasPrevious() {
      checkForComodification();
      return (index > 0) || fragment.prev != null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E next() {
      checkForComodification();
      final E next;
      final Fragment fragment = this.fragment;
      if (index < fragment.size()) {
        next = (E) fragment.get(index++);
      } else {
        final Fragment nextFragment = fragment.next;
        if (nextFragment != null) {
          index = 1;
          next = (E) (this.fragment = nextFragment).get(0);
        } else {
          throw new NoSuchElementException();
        }
      }
      ++pos;
      dir = FORWARD;
      return next;
    }

    @Override
    public int nextIndex() {
      checkForComodification();
      return pos;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E previous() {
      checkForComodification();
      final E prev;
      final Fragment fragment = this.fragment;
      if (index > 0) {
        prev = (E) fragment.get(--index);
      } else {
        final Fragment prevFragment = fragment.prev;
        if (prevFragment != null) {
          this.fragment = prevFragment;
          index = prevFragment.size();
          prev = (E) prevFragment.get(--index);
        } else {
          throw new NoSuchElementException();
        }
      }
      --pos;
      dir = BACKWARD;
      return prev;
    }

    @Override
    public int previousIndex() {
      checkForComodification();
      return pos - 1;
    }

    @Override
    public void remove() {
      if (dir == NONE) {
        throw new IllegalStateException();
      }
      checkForComodification();
      if (dir == FORWARD) {
        if (removeFromFragment(fragment, index - 1)) {
          index = goToIndex(--pos);
          fragment = pointer;
        } else {
          --pos;
        }
        --index;
      } else if (removeFromFragment(fragment, index)) {
        index = goToIndex(pos);
        fragment = pointer;
      }
      dir = NONE;
      expectedModCount = modCount;
    }

    @Override
    public void set(final E element) {
      final int index = this.index;
      if (index == 0) {
        throw new IllegalStateException();
      }
      checkForComodification();
      fragment.set(dir == FORWARD ? index - 1 : index, element);
      expectedModCount = modCount;
    }

    private void checkForComodification() {
      if (modCount != expectedModCount) {
        throw new ConcurrentModificationException();
      }
    }
  }
}
