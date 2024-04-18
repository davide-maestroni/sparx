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
package sparx.collection.internal.iterator;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import sparx.util.Require;
import sparx.util.UncheckedException;
import sparx.util.function.Action;

public class FinallyIteratorMaterializer<E> implements IteratorMaterializer<E> {

  private final Action action;
  private final AtomicBoolean isMaterialized = new AtomicBoolean(false);
  private final IteratorMaterializer<E> wrapped;

  public FinallyIteratorMaterializer(@NotNull final IteratorMaterializer<E> wrapped,
      @NotNull final Action action) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
    this.action = Require.notNull(action, "action");
  }

  @Override
  public int knownSize() {
    try {
      return wrapped.knownSize();
    } catch (final Exception e) {
      materialize();
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
      materialize();
      throw UncheckedException.throwUnchecked(e);
    }
  }

  @Override
  public E materializeNext() {
    try {
      if (!materializeHasNext()) {
        throw new NoSuchElementException();
      }
      return wrapped.materializeNext();
    } catch (final Exception e) {
      materialize();
      throw UncheckedException.throwUnchecked(e);
    }
  }

  @Override
  public int skip(final int count) {
    try {
      return wrapped.skip(count);
    } catch (final Exception e) {
      materialize();
      throw UncheckedException.throwUnchecked(e);
    }
  }

  private void materialize() {
    if (isMaterialized.compareAndSet(false, true)) {
      try {
        action.run();
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }
}
