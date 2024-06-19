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
package sparx.internal.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import org.jetbrains.annotations.NotNull;

public class ElementsCache<E> {

  private static final Object MISSING = new Object();
  private static final int SIZE_THRESHOLD = 64; // TODO: need to benchmark this

  private CacheState<E> state;

  public ElementsCache(final int knownSize) {
    if (knownSize < 0 || knownSize > SIZE_THRESHOLD) {
      state = new MapState(knownSize);
    } else {
      state = new ArrayState(knownSize);
    }
  }

  // TODO: remove??
  public boolean contains(final Object element) {
    return state.contains(element);
  }

  public int count() {
    return state.count();
  }

  public E get(final int index) {
    return state.get(index);
  }

  public boolean has(final int index) {
    return state.has(index);
  }

  public int set(final int index, final E element) {
    return state.set(index, element);
  }

  public void setSize(final int size) {
    state.setSize(size);
  }

  public @NotNull List<E> toList() {
    return state.toList();
  }

  private interface CacheState<E> {

    boolean contains(Object element);

    int count();

    E get(int index);

    boolean has(int index);

    int set(int index, E element);

    void setSize(int size);

    @NotNull
    List<E> toList();
  }

  private class ArrayState implements CacheState<E> {

    private final ArrayList<Object> elements;

    private int count;

    private ArrayState(final int size) {
      if (size <= SIZE_THRESHOLD) {
        elements = new ArrayList<Object>(size);
      } else {
        elements = new ArrayList<Object>();
      }
    }

    @Override
    public boolean contains(final Object element) {
      return elements.contains(element);
    }

    @Override
    public int count() {
      return count;
    }

    @Override
    @SuppressWarnings("unchecked")
    public E get(final int index) {
      return (E) elements.get(index);
    }

    @Override
    public boolean has(final int index) {
      final ArrayList<Object> elements = this.elements;
      return index < elements.size() && elements.get(index) != MISSING;
    }

    @Override
    public int set(final int index, final E element) {
      final ArrayList<Object> elements = this.elements;
      if (elements.size() <= index) {
        while (elements.size() < index) {
          elements.add(MISSING);
        }
        elements.add(element);
        return ++count;
      } else if (elements.set(index, element) == MISSING) {
        return ++count;
      }
      return count;
    }

    @Override
    public void setSize(final int size) {
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull List<E> toList() {
      return (List<E>) elements;
    }
  }

  private class MapState implements CacheState<E> {

    private final HashMap<Integer, E> elements = new HashMap<Integer, E>();

    private int maxIndex = -1;
    private int size;

    private MapState(final int size) {
      this.size = size;
    }

    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    public boolean contains(final Object element) {
      return elements.containsValue(element);
    }

    @Override
    public int count() {
      return elements.size();
    }

    @Override
    public E get(final int index) {
      return elements.get(index);
    }

    @Override
    public boolean has(final int index) {
      return elements.containsKey(index);
    }

    @Override
    public int set(final int index, final E element) {
      maxIndex = Math.max(index, maxIndex);
      final HashMap<Integer, E> elements = this.elements;
      elements.put(index, element);
      setSize(size);
      return elements.size();
    }

    @Override
    public void setSize(final int size) {
      final HashMap<Integer, E> elements = this.elements;
      if ((size >= 0 && size < (elements.size() << 4)) || elements.size() > (Integer.MAX_VALUE
          >> 4)) {
        state = toArray(size);
      } else {
        this.size = Math.max(size, this.size);
      }
    }

    @Override
    public @NotNull List<E> toList() {
      return toArray(maxIndex + 1).toList();
    }

    private @NotNull ArrayState toArray(final int size) {
      final ArrayState arrayState = new ArrayState(size);
      for (final Entry<Integer, E> entry : elements.entrySet()) {
        arrayState.set(entry.getKey(), entry.getValue());
      }
      return arrayState;
    }
  }
}
