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
import sparx.util.annotation.Positive;

public class FragmentList<E> extends AbstractList<E> implements Cloneable, Serializable {

  private Fragment head;
  private int numFragments;
  private Fragment pointer;
  private int size;
  private Fragment tail;

  public FragmentList() {
    init();
  }

  @Override
  public boolean add(final E element) {
    final Chunk chunk = tail.chunk;
    chunk.append(element);
    if (chunk.first == chunk.last) {
      mergeFragmentsLeft(tail);
    }
    ++size;
    ++modCount;
    return true;
  }

  @Override
  public void add(final int index, final E element) {
    if (index < 0 || index > size) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    final int relIndex = goToIndex(index);
    addToFragment(pointer, relIndex, element);
  }

  @Override
  public void clear() {
    init();
    size = 0;
    ++modCount;
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull FragmentList<E> clone() throws CloneNotSupportedException {
    final FragmentList<E> clone = (FragmentList<E>) super.clone();
    clone.mergeAllFragments();
    return clone;
  }

  @Override
  @SuppressWarnings("unchecked")
  public E get(int index) {
    if (index < 0 || index >= size) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    final int relIndex = goToIndex(index);
    return (E) pointer.get(relIndex);
  }

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

  @Override
  public @NotNull Iterator<E> iterator() {
    return listIterator();
  }

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

  @Override
  public @NotNull ListIterator<E> listIterator(int index) {
    if (index < 0 || index > size) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    final int relIndex = goToIndex(index);
    return new FragmentListIterator(pointer, relIndex, index);
  }

  @Override
  @SuppressWarnings("unchecked")
  public E remove(int index) {
    if (index < 0 || index >= size) {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
    final int relIndex = goToIndex(index);
    final Fragment fragment = pointer;
    final E element = (E) fragment.get(relIndex);
    removeFromFragment(fragment, relIndex);
    return element;
  }

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

  @Override
  public boolean removeAll(@NotNull final Collection<?> c) {
    boolean modified = false;
    Fragment fragment = head;
    do {
      for (int i = 0; i < fragment.size(); ) {
        if (c.contains(fragment.get(i))) {
          removeFromFragment(fragment, i);
          modified = true;
        } else {
          ++i;
        }
      }
      fragment = fragment.next;
    } while (fragment != null);
    return modified;
  }

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

  @Override
  protected void removeRange(final int fromIndex, final int toIndex) {
    int index = goToIndex(fromIndex);
    Fragment fragment = pointer;
    for (int i = fromIndex; i < toIndex; ++i) {
      if (removeFromFragment(fragment, index)) {
        index = fromIndex;
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
      }
    }
  }

  private boolean addToFragment(@NotNull final Fragment fragment, final int index,
      final E element) {
    boolean fragmentsModified = false;
    if (index == 0) {
      if (fragment.isClosedLeft() && fragment.prev.isClosedRight()) {
        final Fragment prevFragment = fragment.prev;
        final Chunk chunk = fragment.chunk;
        fragment.indexStart = (fragment.indexStart - 1) & chunk.mask;
        chunk.data[fragment.indexStart] = element;
        if (prevFragment.indexEnd == fragment.indexStart) {
          prevFragment.openLeft();
          prevFragment.next = fragment.next;
          if (fragment.next != null) {
            fragment.next.prev = prevFragment;
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
        final Fragment prevFragment = fragment.prev;
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
      newChunk.data[0] = element;
      newChunk.last = 1;
      final Fragment middleFragment = new Fragment(newChunk);
      final Fragment rightFragment = new Fragment(fragment.type, fragment.chunk);
      rightFragment.indexStart = (chunk.first + index) & chunk.mask;
      rightFragment.closeLeft();
      fragment.indexEnd = rightFragment.indexStart;
      fragment.closeRight();
      final Fragment nextFragment = fragment.next;
      if (nextFragment != null) {
        nextFragment.prev = rightFragment;
        rightFragment.next = nextFragment;
      } else {
        tail = rightFragment;
      }
      rightFragment.prev = middleFragment;
      middleFragment.next = rightFragment;
      fragment.next = middleFragment;
      middleFragment.prev = fragment;
      numFragments += 2;
      fragmentsModified = true;
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

  private void init() {
    head = tail = new Fragment(new Chunk());
    numFragments = 1;
  }

  private void mergeAllFragments() {
    final Chunk newChunk = new Chunk(size);
    Fragment fragment = head;
    int offset = 0;
    while (fragment != null) {
      offset = fragment.copyData(offset, newChunk.data);
      fragment = fragment.next;
    }
    newChunk.first = 0;
    newChunk.last = size;
    head = tail = new Fragment(newChunk);
    ++modCount;
  }

  private boolean mergeFragmentsLeft(@NotNull final Fragment fragment) {
    boolean fragmentsModified = false;
    if (fragment.isOpen()) {
      final Chunk chunk = fragment.chunk;
      final Object[] data = chunk.data;
      final int size = data.length;
      final Chunk newChunk = new Chunk(size + 1);
      final int first = chunk.first;
      final int remainder = size - first;
      final Object[] newData = newChunk.data;
      System.arraycopy(data, first, newData, 0, remainder);
      System.arraycopy(data, 0, newData, remainder, first);
      newChunk.first = 0;
      newChunk.last = size;
      fragment.chunk = newChunk;
    } else {
      final Chunk chunk = fragment.chunk;
      final Fragment nextFragment = fragment.next;
      int removedFragments = 1;
      int totalSize = fragment.size();
      Fragment startFragment = fragment.prev;
      while (startFragment.chunk != chunk) {
        ++removedFragments;
        totalSize += startFragment.size();
        startFragment = startFragment.prev;
      }
      totalSize += startFragment.size();
      final Fragment prevFragment = startFragment.prev;
      final Chunk newChunk = new Chunk(totalSize + 1);
      int offset = 0;
      do {
        offset = startFragment.copyData(offset, newChunk.data);
        startFragment = startFragment.next;
      } while (startFragment != nextFragment);
      newChunk.first = 0;
      newChunk.last = totalSize;
      final Fragment newFragment = new Fragment(newChunk);
      newFragment.prev = prevFragment;
      if (prevFragment != null) {
        prevFragment.next = newFragment;
      } else {
        head = newFragment;
      }
      newFragment.next = nextFragment;
      if (nextFragment != null) {
        nextFragment.prev = newFragment;
      } else {
        tail = newFragment;
      }
      numFragments -= removedFragments;
      fragmentsModified = true;
    }
    ++modCount;
    return fragmentsModified;
  }

  private boolean mergeFragmentsRight(@NotNull final Fragment fragment) {
    boolean fragmentsModified = false;
    if (fragment.isOpen()) {
      final Chunk chunk = fragment.chunk;
      final Object[] data = chunk.data;
      final int size = data.length;
      final Chunk newChunk = new Chunk(size + 1);
      final int first = chunk.first;
      final int remainder = size - first;
      final Object[] newData = newChunk.data;
      System.arraycopy(data, first, newData, 0, remainder);
      System.arraycopy(data, 0, newData, remainder, first);
      newChunk.first = 0;
      newChunk.last = size;
      fragment.chunk = newChunk;
    } else {
      final Chunk chunk = fragment.chunk;
      int removedFragments = 1;
      int totalSize = fragment.size();
      Fragment endFragment = fragment.next;
      while (endFragment.chunk != chunk) {
        ++removedFragments;
        totalSize += endFragment.size();
        endFragment = endFragment.next;
      }
      totalSize += endFragment.size();
      final Fragment nextFragment = endFragment.next;
      final Chunk newChunk = new Chunk(totalSize + 1);
      Fragment startFragment = fragment;
      int offset = 0;
      do {
        offset = startFragment.copyData(offset, newChunk.data);
        startFragment = startFragment.next;
      } while (startFragment != nextFragment);
      newChunk.first = 0;
      newChunk.last = totalSize;
      final Fragment newFragment = new Fragment(newChunk);
      final Fragment prevFragment = fragment.prev;
      newFragment.prev = prevFragment;
      if (prevFragment != null) {
        prevFragment.next = newFragment;
      } else {
        head = newFragment;
      }
      newFragment.next = nextFragment;
      if (nextFragment != null) {
        nextFragment.prev = newFragment;
      } else {
        tail = newFragment;
      }
      numFragments -= removedFragments;
      fragmentsModified = true;
    }
    ++modCount;
    return fragmentsModified;
  }

  private boolean removeFromFragment(@NotNull final Fragment fragment, final int index) {
    boolean fragmentsModified = false;
    if (index == 0) {
      final Chunk chunk = fragment.chunk;
      if (fragment.isOpenLeft()) {
        chunk.removeFirst();
      } else {
        chunk.data[fragment.indexStart] = null;
        fragment.indexStart = (fragment.indexStart + 1) & chunk.mask;
      }
    } else if (index == fragment.size()) {
      fragment.chunk.removeLast();
    } else {
      final Chunk chunk = fragment.chunk;
      final Fragment newFragment = new Fragment(fragment.type, chunk);
      fragment.indexEnd = (chunk.first + index) & chunk.mask;
      newFragment.indexStart = (fragment.indexEnd + 1) & chunk.mask;
      fragment.closeRight();
      newFragment.closeLeft();
      chunk.data[fragment.indexEnd] = null;
      final Fragment nextFragment = fragment.next;
      newFragment.prev = fragment;
      newFragment.next = nextFragment;
      fragment.next = newFragment;
      if (nextFragment != null) {
        nextFragment.prev = newFragment;
      }
      ++numFragments;
      fragmentsModified = true;
    }
    if (fragment.size() == 0 && numFragments > 1) {
      final Fragment prevFragment = fragment.prev;
      final Fragment nextFragment = fragment.next;
      if (prevFragment != null) {
        prevFragment.next = nextFragment;
      } else {
        head = nextFragment;
      }
      if (nextFragment != null) {
        nextFragment.prev = prevFragment;
      } else {
        tail = prevFragment;
      }
      --numFragments;
      fragmentsModified = true;
    }
    ++modCount;
    return fragmentsModified;
  }

  private enum FragmentType {
    OPEN {
      @Override
      void closeLeft(@NotNull final Fragment fragment) {
        fragment.type = FragmentType.RIGHT;
      }

      @Override
      void closeRight(@NotNull final Fragment fragment) {
        fragment.type = FragmentType.LEFT;
      }

      @Override
      int copyData(@NotNull final Fragment fragment, @Positive final int offset,
          @NotNull final Object[] dstData) {
        final int size = size(fragment);
        final Chunk chunk = fragment.chunk;
        final int first = chunk.first;
        final int last = chunk.last;
        if (first < last) {
          System.arraycopy(chunk.data, first, dstData, offset, last - first);
        } else {
          final int remainder = chunk.data.length - first;
          System.arraycopy(chunk.data, first, dstData, offset, remainder);
          System.arraycopy(chunk.data, 0, dstData, offset + remainder, last);
        }
        return offset + size;
      }

      @Override
      Object get(@NotNull final Fragment fragment, final int index) {
        final Chunk chunk = fragment.chunk;
        final Object[] data = chunk.data;
        return data[(chunk.first + index) & (data.length - 1)];
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
      Object set(@NotNull final Fragment fragment, int index, final Object element) {
        final Chunk chunk = fragment.chunk;
        index = (chunk.first + index) & chunk.mask;
        final Object old = chunk.data[index];
        chunk.data[index] = element;
        return old;
      }

      @Override
      int size(@NotNull final Fragment fragment) {
        final Chunk chunk = fragment.chunk;
        final int size = chunk.last - chunk.first;
        return size >= 0 ? size : size + chunk.data.length;
      }
    }, CLOSED {
      @Override
      int copyData(@NotNull final Fragment fragment, @Positive final int offset,
          @NotNull final Object[] dstData) {
        final int size = size(fragment);
        final Chunk chunk = fragment.chunk;
        final int first = fragment.indexStart;
        final int last = fragment.indexEnd;
        if (first < last) {
          System.arraycopy(chunk.data, first, dstData, offset, last - first);
        } else {
          final int remainder = chunk.data.length - first;
          System.arraycopy(chunk.data, first, dstData, offset, remainder);
          System.arraycopy(chunk.data, 0, dstData, offset + remainder, last);
        }
        return offset + size;
      }

      @Override
      Object get(@NotNull final Fragment fragment, final int index) {
        final Object[] data = fragment.chunk.data;
        return data[(fragment.indexStart + index) & (data.length - 1)];
      }

      @Override
      boolean isClosed() {
        return true;
      }

      @Override
      boolean isClosedLeft() {
        return true;
      }

      @Override
      boolean isClosedRight() {
        return true;
      }

      @Override
      void openLeft(@NotNull final Fragment fragment) {
        fragment.type = FragmentType.LEFT;
      }

      @Override
      void openRight(@NotNull final Fragment fragment) {
        fragment.type = FragmentType.RIGHT;
      }

      @Override
      Object set(@NotNull final Fragment fragment, int index, final Object element) {
        final Chunk chunk = fragment.chunk;
        index = (fragment.indexStart + index) & chunk.mask;
        final Object old = chunk.data[index];
        chunk.data[index] = element;
        return old;
      }

      @Override
      int size(@NotNull final Fragment fragment) {
        final Chunk chunk = fragment.chunk;
        final int size = fragment.indexEnd - fragment.indexStart;
        return size >= 0 ? size : size + chunk.data.length;
      }
    }, LEFT {
      @Override
      void closeLeft(@NotNull final Fragment fragment) {
        fragment.type = FragmentType.CLOSED;
      }

      @Override
      int copyData(@NotNull final Fragment fragment, @Positive final int offset,
          @NotNull final Object[] dstData) {
        final int size = size(fragment);
        final Chunk chunk = fragment.chunk;
        final int first = chunk.first;
        final int last = fragment.indexEnd;
        if (first < last) {
          System.arraycopy(chunk.data, first, dstData, offset, last - first);
        } else {
          final int remainder = chunk.data.length - first;
          System.arraycopy(chunk.data, first, dstData, offset, remainder);
          System.arraycopy(chunk.data, 0, dstData, offset + remainder, last);
        }
        return offset + size;
      }

      @Override
      Object get(@NotNull final Fragment fragment, final int index) {
        final Chunk chunk = fragment.chunk;
        final Object[] data = chunk.data;
        return data[(chunk.first + index) & (data.length - 1)];
      }

      @Override
      boolean isClosedRight() {
        return true;
      }

      @Override
      boolean isOpenLeft() {
        return true;
      }

      @Override
      void openRight(@NotNull final Fragment fragment) {
        fragment.type = FragmentType.OPEN;
      }

      @Override
      Object set(@NotNull final Fragment fragment, int index, final Object element) {
        final Chunk chunk = fragment.chunk;
        index = (chunk.first + index) & chunk.mask;
        final Object old = chunk.data[index];
        chunk.data[index] = element;
        return old;
      }

      @Override
      int size(@NotNull final Fragment fragment) {
        final Chunk chunk = fragment.chunk;
        final int size = fragment.indexEnd - chunk.first;
        return size >= 0 ? size : size + chunk.data.length;
      }
    }, RIGHT {
      @Override
      void closeRight(@NotNull final Fragment fragment) {
        fragment.type = FragmentType.CLOSED;
      }

      @Override
      int copyData(@NotNull final Fragment fragment, @Positive final int offset,
          @NotNull final Object[] dstData) {
        final int size = size(fragment);
        final Chunk chunk = fragment.chunk;
        final int first = fragment.indexStart;
        final int last = chunk.last;
        if (first < last) {
          System.arraycopy(chunk.data, first, dstData, offset, last - first);
        } else {
          final int remainder = chunk.data.length - first;
          System.arraycopy(chunk.data, first, dstData, offset, remainder);
          System.arraycopy(chunk.data, 0, dstData, offset + remainder, last);
        }
        return offset + size;
      }

      @Override
      Object get(@NotNull final Fragment fragment, final int index) {
        final Object[] data = fragment.chunk.data;
        return data[(fragment.indexStart + index) & (data.length - 1)];
      }

      @Override
      boolean isClosedLeft() {
        return true;
      }

      @Override
      boolean isOpenRight() {
        return true;
      }

      @Override
      void openLeft(@NotNull final Fragment fragment) {
        fragment.type = FragmentType.OPEN;
      }

      @Override
      Object set(@NotNull final Fragment fragment, int index, final Object element) {
        final Chunk chunk = fragment.chunk;
        index = (fragment.indexStart + index) & chunk.mask;
        final Object old = chunk.data[index];
        chunk.data[index] = element;
        return old;
      }

      @Override
      int size(@NotNull final Fragment fragment) {
        final Chunk chunk = fragment.chunk;
        final int size = chunk.last - fragment.indexStart;
        return size >= 0 ? size : size + chunk.data.length;
      }
    };

    void closeLeft(@NotNull final Fragment fragment) {
    }

    void closeRight(@NotNull final Fragment fragment) {
    }

    int copyData(@NotNull final Fragment fragment, @Positive final int offset,
        @NotNull final Object[] dstData) {
      return offset;
    }

    Object get(@NotNull final Fragment fragment, final int index) {
      return null;
    }

    boolean isClosed() {
      return false;
    }

    boolean isClosedLeft() {
      return false;
    }

    boolean isClosedRight() {
      return false;
    }

    boolean isOpen() {
      return false;
    }

    boolean isOpenLeft() {
      return false;
    }

    boolean isOpenRight() {
      return false;
    }

    void openLeft(@NotNull final Fragment fragment) {
    }

    void openRight(@NotNull final Fragment fragment) {
    }

    Object set(@NotNull final Fragment fragment, final int index, final Object element) {
      return null;
    }

    int size(@NotNull final Fragment fragment) {
      return 0;
    }
  }

  private static class Chunk {

    private final Object[] data;
    private final int mask;

    private int first;
    private int last;

    private Chunk() {
      data = new Object[4];
      mask = 3;
    }

    private Chunk(@Positive final int minCapacity) {
      final int msb = Integer.highestOneBit(minCapacity);
      final int initialCapacity = (minCapacity == msb) ? msb : msb << 1;
      data = new Object[initialCapacity];
      mask = initialCapacity - 1;
    }

    private void append(Object element) {
      data[last] = element;
      last = (last + 1) & mask;
    }

    private void prepend(Object element) {
      first = (first - 1) & mask;
      data[first] = element;
    }

    private void removeFirst() {
      data[first] = null;
      first = (first + 1) & mask;
    }

    private void removeLast() {
      last = (last - 1) & mask;
      data[last] = null;
    }
  }

  private static class Fragment {

    private Chunk chunk;
    private int indexEnd;
    private int indexStart;
    private Fragment next;
    private Fragment prev;
    private FragmentType type;

    private Fragment(@NotNull final Chunk chunk) {
      this(FragmentType.OPEN, chunk);
    }

    private Fragment(final FragmentType type, @NotNull final Chunk chunk) {
      this.type = type;
      this.chunk = chunk;
    }

    private void closeLeft() {
      type.closeLeft(this);
    }

    private void closeRight() {
      type.closeRight(this);
    }

    private int copyData(@Positive final int offset, @NotNull final Object[] dstData) {
      return type.copyData(this, offset, dstData);
    }

    private Object get(final int index) {
      return type.get(this, index);
    }

    private boolean isClosed() {
      return type.isClosed();
    }

    private boolean isClosedLeft() {
      return type.isClosedLeft();
    }

    private boolean isClosedRight() {
      return type.isClosedRight();
    }

    private boolean isOpen() {
      return type.isOpen();
    }

    private boolean isOpenLeft() {
      return type.isOpenLeft();
    }

    private boolean isOpenRight() {
      return type.isOpenRight();
    }

    private void openLeft() {
      type.openLeft(this);
    }

    private void openRight() {
      type.openRight(this);
    }

    private Object set(final int index, final Object element) {
      return type.set(this, index, element);
    }

    private int size() {
      return type.size(this);
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
      if (addToFragment(fragment, index, element)) {
        index = goToIndex(++pos);
        fragment = pointer;
      } else {
        ++pos;
      }
      ++size;
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
      return (index > 1) || fragment.prev != null;
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
      } else if (removeFromFragment(fragment, index)) {
        index = goToIndex(pos);
        fragment = pointer;
      }
      dir = NONE;
      --size;
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
