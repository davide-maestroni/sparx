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
package sparx.internal.future.iterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.FutureConsumer;
import sparx.internal.future.IndexedFutureConsumer;
import sparx.internal.future.IndexedFuturePredicate;
import sparx.util.annotation.Positive;

public class OrElseIteratorFutureMaterializer<E> extends AbstractIteratorFutureMaterializer<E> {

  public OrElseIteratorFutureMaterializer(@NotNull final IteratorFutureMaterializer<E> wrapped,
      @NotNull final IteratorFutureMaterializer<E> elementsMaterializer,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    super(context);
    setState(new ImmaterialState(wrapped, elementsMaterializer, cancelException));
  }

  @Override
  public int knownSize() {
    return -1;
  }

  private interface StateConsumer<E> {

    void accept(@NotNull IteratorFutureMaterializer<E> state);
  }

  private class ImmaterialState implements IteratorFutureMaterializer<E> {

    private final AtomicReference<CancellationException> cancelException;
    private final IteratorFutureMaterializer<E> elementsMaterializer;
    private final ArrayList<StateConsumer<E>> stateConsumers = new ArrayList<StateConsumer<E>>(2);
    private final IteratorFutureMaterializer<E> wrapped;

    public ImmaterialState(@NotNull final IteratorFutureMaterializer<E> wrapped,
        @NotNull final IteratorFutureMaterializer<E> elementsMaterializer,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      this.wrapped = wrapped;
      this.elementsMaterializer = elementsMaterializer;
      this.cancelException = cancelException;
    }

    @Override
    public boolean isCancelled() {
      return false;
    }

    @Override
    public boolean isDone() {
      return false;
    }

    @Override
    public boolean isFailed() {
      return false;
    }

    @Override
    public boolean isMaterializedAtOnce() {
      return wrapped.isMaterializedAtOnce();
    }

    @Override
    public boolean isSucceeded() {
      return false;
    }

    @Override
    public int knownSize() {
      return -1;
    }

    @Override
    public void materializeCancel(@NotNull final CancellationException exception) {
      wrapped.materializeCancel(exception);
      elementsMaterializer.materializeCancel(exception);
      consumeState(setCancelled(exception));
    }

    @Override
    public void materializeElements(@NotNull final FutureConsumer<List<E>> consumer) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final IteratorFutureMaterializer<E> state) {
          state.materializeElements(consumer);
        }
      });
    }

    @Override
    public void materializeHasNext(@NotNull final FutureConsumer<Boolean> consumer) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final IteratorFutureMaterializer<E> state) {
          state.materializeHasNext(consumer);
        }
      });
    }

    @Override
    public void materializeIterator(@NotNull final FutureConsumer<Iterator<E>> consumer) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final IteratorFutureMaterializer<E> state) {
          state.materializeIterator(consumer);
        }
      });
    }

    @Override
    public void materializeNext(@NotNull final IndexedFutureConsumer<E> consumer) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final IteratorFutureMaterializer<E> state) {
          state.materializeNext(consumer);
        }
      });
    }

    @Override
    public void materializeNextWhile(@NotNull final IndexedFuturePredicate<E> predicate) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final IteratorFutureMaterializer<E> state) {
          state.materializeNextWhile(predicate);
        }
      });
    }

    @Override
    public void materializeSkip(@Positive final int count,
        @NotNull final FutureConsumer<Integer> consumer) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final IteratorFutureMaterializer<E> state) {
          state.materializeSkip(count, consumer);
        }
      });
    }

    @Override
    public int weightElements() {
      return stateConsumers.isEmpty() ? wrapped.weightHasNext() : 1;
    }

    @Override
    public int weightHasNext() {
      return weightElements();
    }

    @Override
    public int weightNext() {
      return weightElements();
    }

    @Override
    public int weightNextWhile() {
      return weightElements();
    }

    @Override
    public int weightSkip() {
      return weightElements();
    }

    private void consumeState(@NotNull final IteratorFutureMaterializer<E> state) {
      final ArrayList<StateConsumer<E>> stateConsumers = this.stateConsumers;
      for (final StateConsumer<E> stateConsumer : stateConsumers) {
        stateConsumer.accept(state);
      }
      stateConsumers.clear();
    }

    private void materialized(@NotNull final StateConsumer<E> consumer) {
      final ArrayList<StateConsumer<E>> stateConsumers = this.stateConsumers;
      stateConsumers.add(consumer);
      if (stateConsumers.size() == 1) {
        wrapped.materializeHasNext(new CancellableFutureConsumer<Boolean>() {
          @Override
          public void cancellableAccept(final Boolean hasNext) {
            if (hasNext) {
              consumeState(setState(new WrappingState(wrapped, cancelException)));
            } else {
              consumeState(setState(new WrappingState(elementsMaterializer, cancelException)));
            }
          }

          @Override
          public void error(@NotNull final Exception error) {
            final CancellationException exception = cancelException.get();
            if (exception != null) {
              consumeState(setCancelled(exception));
            } else {
              consumeState(setFailed(error));
            }
          }
        });
      }
    }
  }
}