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
package sparx1.internal.lazy.iterator;

import sparx1.internal.lazy.IteratorMaterializer;
import sparx1.util.SizeOverflowException;
import sparx1.util.annotation.NotNull;
import sparx1.util.annotation.Positive;

public class AppendIteratorMaterializer<E> extends StatefulIteratorMaterializer<E> {

  public AppendIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      final E element) {
    setState(new ImmaterialState(wrapped, element));
  }

  private class ImmaterialState implements IteratorMaterializer<E> {

    private final E element;
    private final IteratorMaterializer<E> wrapped;

    private ImmaterialState(final @NotNull IteratorMaterializer<E> wrapped, final E element) {
      this.wrapped = wrapped;
      this.element = element;
    }

    @Override
    public int currentKnownSize() {
      final int knownSize = wrapped.currentKnownSize();
      if (knownSize >= 0) {
        return SizeOverflowException.safeCast((long) knownSize + 1);
      }
      return -1;
    }

    @Override
    public boolean materializeHasNext() {
      return true;
    }

    @Override
    public E materializeNext() {
      final IteratorMaterializer<E> wrapped = this.wrapped;
      if (wrapped.materializeHasNext()) {
        return wrapped.materializeNext();
      }
      setEmptyState();
      return element;
    }

    @Override
    public int materializeSkip(final @Positive int count) {
      final int skipped = wrapped.materializeSkip(count);
      if (skipped < count) {
        setEmptyState();
        return skipped + 1;
      }
      return skipped;
    }
  }
}
