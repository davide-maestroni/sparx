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
package sparx.internal.lazy.list;

import java.util.Arrays;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import sparx.util.UncheckedException;

public class SortedListMaterializer<E> implements ListMaterializer<E> {

  private volatile State<E> state;

  public SortedListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      @NotNull final Comparator<? super E> comparator) {
    state = new ImmaterialState(wrapped, comparator);
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    return state.canMaterializeElement(index);
  }

  @Override
  public int knownSize() {
    return state.knownSize();
  }

  @Override
  public boolean materializeContains(final Object element) {
    return state.materializeContains(element);
  }

  @Override
  public E materializeElement(final int index) {
    return state.materialized().get(index);
  }

  @Override
  public int materializeElements() {
    return state.materialized().size();
  }

  @Override
  public boolean materializeEmpty() {
    return !state.canMaterializeElement(0);
  }

  @Override
  public @NotNull Iterator<E> materializeIterator() {
    return state.materialized().iterator();
  }

  @Override
  public int materializeSize() {
    return state.materializeSize();
  }

  private interface State<E> {

    boolean canMaterializeElement(int index);

    int knownSize();

    @NotNull
    List<E> materialized();

    boolean materializeContains(Object element);

    int materializeSize();
  }

  private static class ElementsState<E> implements State<E> {

    private final List<E> elements;

    @SuppressWarnings("unchecked")
    private ElementsState(@NotNull final Object[] elements) {
      this.elements = (List<E>) Arrays.asList(elements);
    }

    @Override
    public boolean canMaterializeElement(final int index) {
      return index >= 0 && index < elements.size();
    }

    @Override
    public int knownSize() {
      return elements.size();
    }

    @Override
    public @NotNull List<E> materialized() {
      return elements;
    }

    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    public boolean materializeContains(final Object element) {
      return elements.contains(element);
    }

    @Override
    public int materializeSize() {
      return elements.size();
    }
  }

  private class ImmaterialState implements State<E> {

    private final Comparator<? super E> comparator;
    private final AtomicBoolean isMaterialized = new AtomicBoolean(false);
    private final ListMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final ListMaterializer<E> wrapped,
        @NotNull final Comparator<? super E> comparator) {
      this.wrapped = wrapped;
      this.comparator = comparator;
    }

    @Override
    public boolean canMaterializeElement(final int index) {
      return wrapped.canMaterializeElement(index);
    }

    @Override
    public int knownSize() {
      return wrapped.knownSize();
    }

    @Override
    public boolean materializeContains(final Object element) {
      return wrapped.materializeContains(element);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull List<E> materialized() {
      if (!isMaterialized.compareAndSet(false, true)) {
        throw new ConcurrentModificationException();
      }
      try {
        final ListMaterializer<E> wrapped = this.wrapped;
        final Object[] elements = new Object[wrapped.materializeSize()];
        int i = 0;
        while (wrapped.canMaterializeElement(i)) {
          elements[i] = wrapped.materializeElement(i);
          ++i;
        }
        Arrays.sort(elements, (Comparator<? super Object>) comparator);
        return (state = new ElementsState<E>(elements)).materialized();
      } catch (final Exception e) {
        isMaterialized.set(false);
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public int materializeSize() {
      return wrapped.materializeSize();
    }
  }
}
