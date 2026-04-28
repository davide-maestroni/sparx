/*
 * Copyright 2026 Davide Maestroni
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
package sparx1.internal.lazy.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import sparx1.util.annotation.NotNegative;
import sparx1.util.annotation.NotNull;

public class CollectionToListMaterializer<E> extends StatefulListMaterializer<E> {

  private static final int SIZE_THRESHOLD = 64; // TODO: need to benchmark this

  public CollectionToListMaterializer(final @NotNull Collection<E> elements) {
    if (elements.size() > SIZE_THRESHOLD) {
      setState(new InitialState(elements));
    } else {
      setState(new WrapperState<E>(elements));
    }
  }

  private class InitialState extends AbstractListMaterializer<E> {

    private final Collection<E> elements;
    private final ArrayList<E> elementsList = new ArrayList<E>();
    private final Iterator<E> iterator;

    private InitialState(final @NotNull Collection<E> elements) {
      this.elements = elements;
      iterator = elements.iterator();
    }

    @Override
    public boolean canMaterializeElement(final @NotNegative int index) {
      return index < elements.size();
    }

    @Override
    public int knownSize() {
      return elements.size();
    }

    @Override
    public boolean isRandomAccess() {
      return true;
    }

    @Override
    public boolean isSizeKnown() {
      return true;
    }

    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    public boolean materializeContains(final Object element) {
      try {
        return elements.contains(element);
      } catch (final NullPointerException e) {
        // some collections do not support null elements
        if (element == null) {
          return false;
        }
        throw e;
      }
    }

    @Override
    public E materializeElement(final @NotNegative int index) {
      final ArrayList<E> elements = this.elementsList;
      if (elements.size() <= index) {
        final Iterator<E> iterator = this.iterator;
        do {
          if (!iterator.hasNext()) {
            throw new IndexOutOfBoundsException(Integer.toString(index));
          }
          elements.add(iterator.next());
        } while (elements.size() <= index);
        if (!iterator.hasNext()) {
          setState(new ListToListMaterializer<E>(elements));
        }
      }
      return elements.get(index);
    }

    @Override
    public int materializeElements() {
      if (elements.isEmpty()) {
        return 0;
      }
      final int size = elements.size();
      materializeElement(size - 1);
      return size;
    }

    @Override
    public boolean materializeEmpty() {
      return elements.isEmpty();
    }

    @Override
    public int materializeSize() {
      return elements.size();
    }
  }

  private static class WrapperState<E> extends AbstractListMaterializer<E> {

    private final Collection<E> elements;

    private WrapperState(final @NotNull Collection<E> elements) {
      this.elements = elements;
    }

    @Override
    public boolean canMaterializeElement(final @NotNegative int index) {
      return index < elements.size();
    }

    @Override
    public int knownSize() {
      return elements.size();
    }

    @Override
    public boolean isRandomAccess() {
      return false;
    }

    @Override
    public boolean isSizeKnown() {
      return true;
    }

    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    public boolean materializeContains(final Object element) {
      try {
        return elements.contains(element);
      } catch (final NullPointerException e) {
        // some collections do not support null elements
        if (element == null) {
          return false;
        }
        throw e;
      }
    }

    @Override
    public E materializeElement(@NotNegative int index) {
      final Iterator<E> iterator = elements.iterator();
      while (iterator.hasNext() && index-- > 0) {
        iterator.next();
      }
      try {
        return iterator.next();
      } catch (final NoSuchElementException ignored) {
        throw new IndexOutOfBoundsException(Integer.toString(index));
      }
    }

    @Override
    public int materializeElements() {
      return elements.size();
    }

    @Override
    public boolean materializeEmpty() {
      return elements.isEmpty();
    }

    @Override
    public Iterator<E> materializeForwardIterator(final @NotNegative int index) {
      final Iterator<E> iterator = elements.iterator();
      for (int i = 0; i < index && iterator.hasNext(); ++i) {
        iterator.next();
      }
      return iterator;
    }

    @Override
    public int materializeSize() {
      return elements.size();
    }

    @Override
    public IndexedIterator<E> materializeUnorderedIterator() {
      return new WrapIndexedIterator<E>(elements.iterator());
    }
  }
}
