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
import sparx1.util.function.IndexedConsumer;

public class PeekExceptionallyIteratorMaterializer<E> extends StatefulIteratorMaterializer<E> {

  public PeekExceptionallyIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      final @NotNull IndexedConsumer<? super Throwable> consumer) {
    setState(new InitialState(wrapped, consumer));
  }

  private class InitialState extends AbstractIteratorMaterializer<E> {

    private final IndexedConsumer<? super Throwable> consumer;
    private final IteratorMaterializer<E> wrapped;

    private int pos;

    private InitialState(final @NotNull IteratorMaterializer<E> wrapped,
        final @NotNull IndexedConsumer<? super Throwable> consumer) {
      this.wrapped = wrapped;
      this.consumer = consumer;
    }

    @Override
    public int currentKnownSize() {
      return wrapped.currentKnownSize();
    }

    @Override
    public boolean materializeHasNext() {
      try {
        if (!wrapped.materializeHasNext()) {
          setEmptyState();
          return false;
        }
        return true;
      } catch (final Throwable t) {
        try {
          consumer.accept(pos, t);
        } catch (final Exception e) {
          throw UncheckedException.throwUnchecked(e);
        }
        throw UncheckedException.throwUnchecked(t);
      }
    }

    @Override
    public E materializeNext() {
      try {
        ++pos;
        return wrapped.materializeNext();
      } catch (final Throwable t) {
        try {
          consumer.accept(pos++, t);
        } catch (final Exception e) {
          throw UncheckedException.throwUnchecked(e);
        }
        throw UncheckedException.throwUnchecked(t);
      }
    }
  }
}
