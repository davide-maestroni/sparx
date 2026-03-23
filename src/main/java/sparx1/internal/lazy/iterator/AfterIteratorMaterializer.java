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
package sparx1.internal.lazy.iterator;

import java.util.NoSuchElementException;
import sparx1.internal.lazy.IteratorMaterializer;
import sparx1.util.UncheckedException;
import sparx1.util.annotation.NotNull;
import sparx1.util.annotation.Positive;
import sparx1.util.function.Action;

public class AfterIteratorMaterializer<E> extends StatefulIteratorMaterializer<E> {

  public AfterIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      final @NotNull Action action) {
    setState(new ImmaterialState(wrapped, action));
  }

  private class ImmaterialState implements IteratorMaterializer<E> {

    private final Action action;
    private final IteratorMaterializer<E> wrapped;

    private ImmaterialState(final @NotNull IteratorMaterializer<E> wrapped,
        final @NotNull Action action) {
      this.wrapped = wrapped;
      this.action = action;
    }

    @Override
    public int currentKnownSize() {
      try {
        return wrapped.currentKnownSize();
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public boolean materializeHasNext() {
      try {
        if (!wrapped.materializeHasNext()) {
          materialize();
          return false;
        }
        return true;
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public E materializeNext() {
      try {
        return wrapped.materializeNext();
      } catch (final NoSuchElementException e) {
        materialize();
        throw e;
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public int materializeSkip(final @Positive int count) {
      try {
        final int skipped = wrapped.materializeSkip(count);
        if (skipped < count) {
          materialize();
        }
        return skipped;
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    private void materialize() {
      try {
        action.run();
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      } finally {
        setEmptyState();
      }
    }
  }
}
