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

import org.jetbrains.annotations.NotNull;
import sparx.util.SizeOverflowException;

public class InsertIteratorMaterializer<E> extends StatefulIteratorMaterializer<E> {

  public InsertIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      final E element) {
    setState(new ImmaterialState(wrapped, element));
  }

  private class ImmaterialState implements IteratorMaterializer<E> {

    private final E element;
    private final IteratorMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final IteratorMaterializer<E> wrapped, final E element) {
      this.wrapped = wrapped;
      this.element = element;
    }

    @Override
    public int knownSize() {
      final int knownSize = wrapped.knownSize();
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
      setState(wrapped);
      return element;
    }

    @Override
    public int materializeSkip(final int count) {
      if (count > 0) {
        return setState(wrapped).materializeSkip(count - 1) + 1;
      }
      return 0;
    }
  }
}
