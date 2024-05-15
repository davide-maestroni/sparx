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
package sparx.collection.internal.future.list;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

class ElementsCache<E> {

  private static final Object EMPTY = new Object();
  private static final int SIZE_THRESHOLD = 64; // TODO: need to benchmark this

  private CacheState<E> state;

  ElementsCache(final int knownSize) {
    if (knownSize < 0 || knownSize > SIZE_THRESHOLD) {
      state = new MapState();
    } else {
      state = new ArrayState(knownSize);
    }
  }

  public List<E> add(final int knownSize, final int index, final E element) {
    return state.add(knownSize, index, element);
  }

  public List<E> get(final int knownSize) {
    return state.get(knownSize);
  }

  public E getElement(final int index) {
    return state.getElement(index);
  }

  public boolean hasElement(final int index) {
    return state.hasElement(index);
  }

  public int getSize() {
    return state.getSize();
  }

  private interface CacheState<E> {

    List<E> add(int knownSize, int index, E element);

    List<E> get(int knownSize);

    E getElement(int index);

    boolean hasElement(int index);

    int getSize();
  }

  private class ArrayState implements CacheState<E> {

    private final Object[] array;
    private final List<E> elements;

    private int added;

    @SuppressWarnings("unchecked")
    private ArrayState(final int size) {
      final Object[] elementsArray = new Object[size];
      Arrays.fill(elementsArray, EMPTY);
      array = elementsArray;
      elements = (List<E>) Arrays.asList(elementsArray);
    }

    @Override
    public List<E> add(final int knownSize, final int index, final E element) {
      if (elements.get(index) == EMPTY) {
        ++added;
      }
      array[index] = element;
      return (knownSize >= 0 && added >= knownSize) ? elements : null;
    }

    @Override
    public List<E> get(final int knownSize) {
      return (knownSize >= 0 && added >= knownSize) ? elements : null;
    }

    @Override
    public E getElement(final int index) {
      final E element = elements.get(index);
      return element != EMPTY ? element : null;
    }

    @Override
    public boolean hasElement(final int index) {
      return index < array.length && index >= 0 && elements.get(index) != EMPTY;
    }

    @Override
    public int getSize() {
      return array.length;
    }
  }

  private class MapState implements CacheState<E> {

    private final HashMap<Integer, E> elements = new HashMap<Integer, E>();

    @Override
    public List<E> add(final int knownSize, final int index, final E element) {
      final HashMap<Integer, E> elements = this.elements;
      if ((knownSize >= 0 && knownSize < (elements.size() << 4)) || elements.size() > (
          Integer.MAX_VALUE >> 4)) {
        final ArrayState arrayState = new ArrayState(knownSize);
        for (final Entry<Integer, E> entry : elements.entrySet()) {
          arrayState.add(knownSize, entry.getKey(), entry.getValue());
        }
        return (state = arrayState).add(knownSize, index, element);
      }
      elements.put(index, element);
      return null;
    }

    @Override
    public List<E> get(final int knownSize) {
      if ((knownSize >= 0 && knownSize < (elements.size() << 4)) || elements.size() > (
          Integer.MAX_VALUE >> 4)) {
        final ArrayState arrayState = new ArrayState(knownSize);
        for (final Entry<Integer, E> entry : elements.entrySet()) {
          arrayState.add(knownSize, entry.getKey(), entry.getValue());
        }
        return (state = arrayState).get(knownSize);
      }
      return null;
    }

    public E getElement(final int index) {
      return elements.get(index);
    }

    public boolean hasElement(final int index) {
      return elements.containsKey(index);
    }

    @Override
    public int getSize() {
      return -1;
    }
  }
}
