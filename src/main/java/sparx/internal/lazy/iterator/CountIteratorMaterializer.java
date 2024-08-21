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

public class CountIteratorMaterializer<E> extends StatefulIteratorMaterializer<Integer> {

  public CountIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped) {
    super(wrapped.nextIndex());
    setState(new ImmaterialState(wrapped));
  }

  private class ImmaterialState implements IteratorMaterializer<Integer> {

    private final IteratorMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final IteratorMaterializer<E> wrapped) {
      this.wrapped = wrapped;
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
    public Integer materializeNext() {
      final int size = wrapped.materializeSkip(Integer.MAX_VALUE);
      setState(EmptyIteratorMaterializer.<Integer>instance());
      return size;
    }

    @Override
    public int materializeSkip(final int count) {
      if (count > 0) {
        setState(EmptyIteratorMaterializer.<Integer>instance());
        return 1;
      }
      return 0;
    }

    @Override
    public int nextIndex() {
      return -1;
    }
  }
}
