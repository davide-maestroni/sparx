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

  public List<E> addElement(final int knownSize, final int index, final E element) {
    return state.addElement(knownSize, index, element);
  }

  public E getElement(final int index) {
    return state.getElement(index);
  }

  public List<E> getElements() {
    return state.getElements();
  }

  public boolean hasElement(final int index) {
    return state.hasElement(index);
  }

  public int knownSize() {
    return state.knownSize();
  }

  public int numElements() {
    return state.numElements();
  }

  private interface CacheState<E> {

    List<E> addElement(int knownSize, int index, E element);

    E getElement(int index);

    List<E> getElements();

    boolean hasElement(int index);

    int knownSize();

    int numElements();
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
    public List<E> addElement(final int knownSize, final int index, final E element) {
      if (array[index] == EMPTY) {
        ++added;
      }
      array[index] = element;
      return (knownSize >= 0 && added >= knownSize) ? elements : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E getElement(final int index) {
      final Object element = array[index];
      return element != EMPTY ? (E) element : null;
    }

    @Override
    public boolean hasElement(final int index) {
      return index < array.length && index >= 0 && array[index] != EMPTY;
    }

    @Override
    public List<E> getElements() {
      return elements;
    }

    @Override
    public int knownSize() {
      return array.length;
    }

    @Override
    public int numElements() {
      return added;
    }
  }

  private class MapState implements CacheState<E> {

    private final HashMap<Integer, E> elements = new HashMap<Integer, E>();

    @Override
    public List<E> addElement(final int knownSize, final int index, final E element) {
      final HashMap<Integer, E> elements = this.elements;
      if ((knownSize >= 0 && knownSize < (elements.size() << 4)) || elements.size() > (
          Integer.MAX_VALUE >> 4)) {
        final ArrayState arrayState = new ArrayState(knownSize);
        for (final Entry<Integer, E> entry : elements.entrySet()) {
          arrayState.addElement(knownSize, entry.getKey(), entry.getValue());
        }
        return (state = arrayState).addElement(knownSize, index, element);
      }
      elements.put(index, element);
      return null;
    }

    public E getElement(final int index) {
      return elements.get(index);
    }

    @Override
    public List<E> getElements() {
      final int size = elements.size();
      final ArrayState arrayState = new ArrayState(size);
      for (final Entry<Integer, E> entry : elements.entrySet()) {
        arrayState.addElement(size, entry.getKey(), entry.getValue());
      }
      return (state = arrayState).getElements();
    }

    public boolean hasElement(final int index) {
      return elements.containsKey(index);
    }

    @Override
    public int knownSize() {
      return -1;
    }

    @Override
    public int numElements() {
      return elements.size();
    }
  }
}
