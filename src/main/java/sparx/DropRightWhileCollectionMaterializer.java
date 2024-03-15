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
package sparx;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import sparx.util.CollectionMaterializer;
import sparx.util.Require;
import sparx.util.UncheckedException;
import sparx.util.function.Predicate;

class DropRightWhileCollectionMaterializer<E> implements CollectionMaterializer<E> {

  private final CollectionMaterializer<E> wrapped;

  private volatile State state;

  DropRightWhileCollectionMaterializer(@NotNull final CollectionMaterializer<E> wrapped,
      @NotNull final Predicate<? super E> predicate) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
    state = new ImmaterialState(Require.notNull(predicate, "predicate"));
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    final CollectionMaterializer<E> wrapped = this.wrapped;
    if (index >= wrapped.materializeSize() - state.materialized()) {
      return false;
    }
    return wrapped.canMaterializeElement(index);
  }

  @Override
  public E materializeElement(final int index) {
    final CollectionMaterializer<E> wrapped = this.wrapped;
    if (index >= wrapped.materializeSize() - state.materialized()) {
      throw new IndexOutOfBoundsException(String.valueOf(index));
    }
    return wrapped.materializeElement(index);
  }

  @Override
  public boolean materializeEmpty() {
    return wrapped.materializeSize() <= state.materialized();
  }

  @Override
  public @NotNull Iterator<E> materializeIterator() {
    return new CollectionMaterializerIterator<E>(this);
  }

  @Override
  public int materializeSize() {
    return Math.max(0, wrapped.materializeSize() - state.materialized());
  }

  private interface State {

    int materialized();
  }

  private static class ElementsState implements State {

    private final int elements;

    private ElementsState(final int elements) {
      this.elements = elements;
    }

    @Override
    public int materialized() {
      return elements;
    }
  }

  private static class ExceptionState implements State {

    private final Exception ex;

    private ExceptionState(@NotNull final Exception ex) {
      this.ex = ex;
    }

    @Override
    public int materialized() {
      throw UncheckedException.throwUnchecked(ex);
    }
  }

  private class ImmaterialState implements State {

    private final AtomicBoolean isMaterialized = new AtomicBoolean(false);
    private final Predicate<? super E> predicate;

    private ImmaterialState(@NotNull final Predicate<? super E> predicate) {
      this.predicate = predicate;
    }

    @Override
    public int materialized() {
      if (!isMaterialized.compareAndSet(false, true)) {
        throw new ConcurrentModificationException();
      }
      try {
        final CollectionMaterializer<E> wrapped = DropRightWhileCollectionMaterializer.this.wrapped;
        final Predicate<? super E> predicate = this.predicate;
        final int size = wrapped.materializeSize();
        int i = size - 1;
        for (; i >= 0; --i) {
          if (!predicate.test(wrapped.materializeElement(i))) {
            break;
          }
        }
        final int elements = size - i - 1;
        state = new ElementsState(elements);
        return elements;
      } catch (final Exception e) {
        state = new ExceptionState(e);
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }
}
