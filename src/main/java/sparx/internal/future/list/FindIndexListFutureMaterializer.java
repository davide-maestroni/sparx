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
package sparx.internal.future.list;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.FutureConsumer;
import sparx.internal.future.IndexedFutureConsumer;
import sparx.internal.future.IndexedFuturePredicate;
import sparx.util.annotation.NotNegative;
import sparx.util.function.IndexedPredicate;

public class FindIndexListFutureMaterializer<E> extends AbstractListFutureMaterializer<Integer> {

  public FindIndexListFutureMaterializer(@NotNull final ListFutureMaterializer<E> wrapped,
      @NotNull final IndexedPredicate<? super E> predicate, @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    super(context);
    setState(new ImmaterialState(wrapped, predicate, cancelException));
  }

  @Override
  public boolean isMaterializedAtOnce() {
    return true;
  }

  @Override
  public int knownSize() {
    return -1;
  }

  private interface StateConsumer {

    void accept(@NotNull ListFutureMaterializer<Integer> state);
  }

  private class ImmaterialState implements ListFutureMaterializer<Integer> {

    private final AtomicReference<CancellationException> cancelException;
    private final IndexedPredicate<? super E> predicate;
    private final ArrayList<StateConsumer> stateConsumers = new ArrayList<StateConsumer>(2);
    private final ListFutureMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final ListFutureMaterializer<E> wrapped,
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      this.wrapped = wrapped;
      this.predicate = predicate;
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
      return true;
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
      consumeState(setCancelled(exception));
    }

    @Override
    public void materializeContains(final Object element,
        @NotNull final FutureConsumer<Boolean> consumer) {
      materialized(new StateConsumer() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<Integer> state) {
          state.materializeContains(element, consumer);
        }
      });
    }

    @Override
    public void materializeElement(@NotNegative final int index,
        @NotNull final IndexedFutureConsumer<Integer> consumer) {
      materialized(new StateConsumer() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<Integer> state) {
          state.materializeElement(index, consumer);
        }
      });
    }

    @Override
    public void materializeElements(@NotNull final FutureConsumer<List<Integer>> consumer) {
      materialized(new StateConsumer() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<Integer> state) {
          state.materializeElements(consumer);
        }
      });
    }

    @Override
    public void materializeEmpty(@NotNull final FutureConsumer<Boolean> consumer) {
      materialized(new StateConsumer() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<Integer> state) {
          state.materializeEmpty(consumer);
        }
      });
    }

    @Override
    public void materializeHasElement(@NotNegative final int index,
        @NotNull final FutureConsumer<Boolean> consumer) {
      materialized(new StateConsumer() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<Integer> state) {
          state.materializeHasElement(index, consumer);
        }
      });
    }

    @Override
    public void materializeNextWhile(@NotNegative final int index,
        @NotNull final IndexedFuturePredicate<Integer> predicate) {
      materialized(new StateConsumer() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<Integer> state) {
          state.materializeNextWhile(index, predicate);
        }
      });
    }

    @Override
    public void materializePrevWhile(@NotNegative final int index,
        @NotNull final IndexedFuturePredicate<Integer> predicate) {
      materialized(new StateConsumer() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<Integer> state) {
          state.materializePrevWhile(index, predicate);
        }
      });
    }

    @Override
    public void materializeSize(@NotNull final FutureConsumer<Integer> consumer) {
      materialized(new StateConsumer() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<Integer> state) {
          state.materializeSize(consumer);
        }
      });
    }

    @Override
    public int weightContains() {
      return weightElements();
    }

    @Override
    public int weightElement() {
      return weightElements();
    }

    @Override
    public int weightElements() {
      return stateConsumers.isEmpty() ? wrapped.weightNextWhile() : 1;
    }

    @Override
    public int weightEmpty() {
      return weightElements();
    }

    @Override
    public int weightNextWhile() {
      return weightElements();
    }

    @Override
    public int weightPrevWhile() {
      return weightElements();
    }

    @Override
    public int weightHasElement() {
      return weightElements();
    }

    @Override
    public int weightSize() {
      return weightElements();
    }

    private void consumeState(@NotNull final ListFutureMaterializer<Integer> state) {
      final ArrayList<StateConsumer> stateConsumers = this.stateConsumers;
      for (final StateConsumer stateConsumer : stateConsumers) {
        stateConsumer.accept(state);
      }
      stateConsumers.clear();
    }

    private void materialized(@NotNull final StateConsumer consumer) {
      final ArrayList<StateConsumer> stateConsumers = this.stateConsumers;
      stateConsumers.add(consumer);
      if (stateConsumers.size() == 1) {
        wrapped.materializeNextWhile(0, new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) {
            setState();
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            if (predicate.test(index, element)) {
              setState(index);
              return false;
            }
            return true;
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

    private void setState() {
      consumeState(setDone(EmptyListFutureMaterializer.<Integer>instance()));
    }

    private void setState(final int index) {
      consumeState(setDone(new ElementToListFutureMaterializer<Integer>(index)));
    }
  }
}
