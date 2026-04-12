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
import sparx1.util.UncheckedException;
import sparx1.util.annotation.NotNull;
import sparx1.util.annotation.Positive;
import sparx1.util.function.IndexedPredicate;

public class ExistsIteratorMaterializer<E> extends StatefulIteratorMaterializer<Boolean> {

  public ExistsIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      final @NotNull IndexedPredicate<? super E> predicate, final boolean defaultResult) {
    setState(new InitialState(wrapped, predicate, defaultResult));
  }

  private class InitialState implements IteratorMaterializer<Boolean> {

    private final boolean defaultResult;
    private final IndexedPredicate<? super E> predicate;
    private final IteratorMaterializer<E> wrapped;

    private InitialState(final @NotNull IteratorMaterializer<E> wrapped,
        final @NotNull IndexedPredicate<? super E> predicate, final boolean defaultResult) {
      this.wrapped = wrapped;
      this.predicate = predicate;
      this.defaultResult = defaultResult;
    }

    @Override
    public int currentKnownSize() {
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
          if (predicate.test(i, wrapped.materializeNext())) {
            setEmptyState();
            return true;
          }
          ++i;
        } while (wrapped.materializeHasNext());
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
      setEmptyState();
      return false;
    }

    @Override
    public int materializeSkip(final @Positive int count) {
      setEmptyState();
      return 1;
    }
  }
}
