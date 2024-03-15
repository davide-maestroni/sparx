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

class CountByCollectionMaterializer<E> implements CollectionMaterializer<Integer> {

  private volatile State state;

  CountByCollectionMaterializer(@NotNull final CollectionMaterializer<E> wrapped,
      @NotNull final Predicate<? super E> predicate) {
    state = new ImmaterialState(Require.notNull(wrapped, "wrapped"),
        Require.notNull(predicate, "predicate"));
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    return index == 0;
  }

  @Override
  public int knownSize() {
    return 1;
  }

  @Override
  public Integer materializeElement(final int index) {
    if (index != 0) {
      throw new IndexOutOfBoundsException(String.valueOf(index));
    }
    return state.materialized();
  }

  @Override
  public boolean materializeEmpty() {
    return false;
  }

  @Override
  public @NotNull Iterator<Integer> materializeIterator() {
    return new CollectionMaterializerIterator<Integer>(this);
  }

  @Override
  public int materializeSize() {
    return 1;
  }

  private interface State {

    int materialized();
  }

  private static class CountState implements State {

    private final int count;

    private CountState(final int count) {
      this.count = count;
    }

    @Override
    public int materialized() {
      return count;
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
    private final CollectionMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final CollectionMaterializer<E> wrapped,
        @NotNull final Predicate<? super E> predicate) {
      this.wrapped = wrapped;
      this.predicate = predicate;
    }

    @Override
    public int materialized() {
      if (!isMaterialized.compareAndSet(false, true)) {
        throw new ConcurrentModificationException();
      }
      try {
        int count = 0;
        final Predicate<? super E> predicate = this.predicate;
        final Iterator<E> iterator = wrapped.materializeIterator();
        while (iterator.hasNext()) {
          final E next = iterator.next();
          if (predicate.test(next)) {
            ++count;
          }
        }
        state = new CountState(count);
        return count;
      } catch (final Exception e) {
        state = new ExceptionState(e);
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }
}
