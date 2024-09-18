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
import sparx.util.UncheckedException;
import sparx.util.function.IndexedPredicate;

public class EachIteratorMaterializer<E> extends StatefulIteratorMaterializer<Boolean> {

  public EachIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      @NotNull final IndexedPredicate<? super E> predicate, final boolean defaultResult) {
    setState(new ImmaterialState(wrapped, predicate, defaultResult));
  }

  private class ImmaterialState implements IteratorMaterializer<Boolean> {

    private final boolean defaultResult;
    private final IndexedPredicate<? super E> predicate;
    private final IteratorMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final IteratorMaterializer<E> wrapped,
        @NotNull final IndexedPredicate<? super E> predicate, final boolean defaultResult) {
      this.wrapped = wrapped;
      this.predicate = predicate;
      this.defaultResult = defaultResult;
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
      final IteratorMaterializer<E> wrapped = this.wrapped;
      if (!wrapped.materializeHasNext()) {
        setEmptyState();
        return defaultResult;
      }
      try {
        final IndexedPredicate<? super E> predicate = this.predicate;
        int i = 0;
        do {
          if (!predicate.test(i, wrapped.materializeNext())) {
            setEmptyState();
            return false;
          }
          ++i;
        } while (wrapped.materializeHasNext());
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
      setEmptyState();
      return true;
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
