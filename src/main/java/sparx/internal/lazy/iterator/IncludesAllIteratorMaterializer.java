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
package sparx.internal.lazy.iterator;

import java.util.HashSet;
import org.jetbrains.annotations.NotNull;

public class IncludesAllIteratorMaterializer<E> extends StatefulIteratorMaterializer<Boolean> {

  public IncludesAllIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      @NotNull final Iterable<?> elements) {
    setState(new ImmaterialState(wrapped, elements));
  }

  private class ImmaterialState implements IteratorMaterializer<Boolean> {

    private final Iterable<?> elements;
    private final IteratorMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final IteratorMaterializer<E> wrapped,
        @NotNull final Iterable<?> elements) {
      this.wrapped = wrapped;
      this.elements = elements;
    }

    @Override
    public int knownSize() {
      return 1;
    }

    @Override
    public boolean materializeHasNext() {
      return true;
    }

    @Override
    public Boolean materializeNext() {
      final HashSet<Object> elements = new HashSet<Object>();
      for (final Object element : this.elements) {
        elements.add(element);
      }
      if (elements.isEmpty()) {
        setEmptyState();
        return true;
      }
      final IteratorMaterializer<E> wrapped = this.wrapped;
      while (wrapped.materializeHasNext()) {
        elements.remove(wrapped.materializeNext());
        if (elements.isEmpty()) {
          setEmptyState();
          return true;
        }
      }
      setEmptyState();
      return false;
    }

    @Override
    public int materializeSkip(final int count) {
      if (count > 0) {
        setEmptyState();
        return 1;
      }
      return 0;
    }
  }
}
