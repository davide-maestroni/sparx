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

import static sparx.internal.future.AsyncConsumers.safeConsume;
import static sparx.internal.future.AsyncConsumers.safeConsumeComplete;
import static sparx.internal.future.AsyncConsumers.safeConsumeError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.AsyncConsumer;
import sparx.internal.future.IndexedAsyncConsumer;
import sparx.internal.future.IndexedAsyncPredicate;
import sparx.util.function.BinaryFunction;
import sparx.util.function.Function;

public class FoldRightListAsyncMaterializer<E, F> extends AbstractListAsyncMaterializer<F> {

  private static final Logger LOGGER = Logger.getLogger(
      FoldRightListAsyncMaterializer.class.getName());

  public FoldRightListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      final F identity, @NotNull final BinaryFunction<? super E, ? super F, ? extends F> operation,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final Function<List<F>, List<F>> decorateFunction) {
    super(context, new AtomicInteger(STATUS_RUNNING));
    setState(new ImmaterialState(wrapped, identity, operation, cancelException, decorateFunction));
  }

  @Override
  public int knownSize() {
    return 1;
  }

  private interface StateConsumer<E> {

    void accept(@NotNull ListAsyncMaterializer<E> state);
  }

  private class ImmaterialState implements ListAsyncMaterializer<F> {

    private final AtomicReference<CancellationException> cancelException;
    private final Function<List<F>, List<F>> decorateFunction;
    private final F identity;
    private final BinaryFunction<? super E, ? super F, ? extends F> operation;
    private final ArrayList<StateConsumer<F>> stateConsumers = new ArrayList<StateConsumer<F>>(2);
    private final ListAsyncMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final ListAsyncMaterializer<E> wrapped, final F identity,
        @NotNull final BinaryFunction<? super E, ? super F, ? extends F> operation,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Function<List<F>, List<F>> decorateFunction) {
      this.wrapped = wrapped;
      this.identity = identity;
      this.operation = operation;
      this.cancelException = cancelException;
      this.decorateFunction = decorateFunction;
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
      return false;
    }

    @Override
    public boolean isSucceeded() {
      return false;
    }

    @Override
    public int knownSize() {
      return 1;
    }

    @Override
    public void materializeCancel(@NotNull final CancellationException exception) {
      wrapped.materializeCancel(exception);
      consumeState(setCancelled(exception));
    }

    @Override
    public void materializeContains(final Object element,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      materialized(new StateConsumer<F>() {
        @Override
        public void accept(@NotNull final ListAsyncMaterializer<F> state) {
          state.materializeContains(element, consumer);
        }
      });
    }

    @Override
    public void materializeElement(final int index,
        @NotNull final IndexedAsyncConsumer<F> consumer) {
      if (index < 0) {
        safeConsumeError(consumer, new IndexOutOfBoundsException(Integer.toString(index)), LOGGER);
      } else if (index > 1) {
        safeConsumeComplete(consumer, 1, LOGGER);
      } else {
        materialized(new StateConsumer<F>() {
          @Override
          public void accept(@NotNull final ListAsyncMaterializer<F> state) {
            state.materializeElement(index, consumer);
          }
        });
      }
    }

    @Override
    public void materializeElements(@NotNull final AsyncConsumer<List<F>> consumer) {
      materialized(new StateConsumer<F>() {
        @Override
        public void accept(@NotNull final ListAsyncMaterializer<F> state) {
          state.materializeElements(consumer);
        }
      });
    }

    @Override
    public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
      safeConsume(consumer, false, LOGGER);
    }

    @Override
    public void materializeHasElement(final int index,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      safeConsume(consumer, index == 0, LOGGER);
    }

    @Override
    public void materializeNextWhile(final int index,
        @NotNull final IndexedAsyncPredicate<F> predicate) {
      materialized(new StateConsumer<F>() {
        @Override
        public void accept(@NotNull final ListAsyncMaterializer<F> state) {
          state.materializeNextWhile(index, predicate);
        }
      });
    }

    @Override
    public void materializePrevWhile(final int index,
        @NotNull final IndexedAsyncPredicate<F> predicate) {
      materialized(new StateConsumer<F>() {
        @Override
        public void accept(@NotNull final ListAsyncMaterializer<F> state) {
          state.materializePrevWhile(index, predicate);
        }
      });
    }

    @Override
    public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
      safeConsume(consumer, 1, LOGGER);
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
      return stateConsumers.isEmpty() ? wrapped.weightSize() : 1;
    }

    @Override
    public int weightEmpty() {
      return 1;
    }

    @Override
    public int weightHasElement() {
      return 1;
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
    public int weightSize() {
      return 1;
    }

    private void consumeState(@NotNull final ListAsyncMaterializer<F> state) {
      final ArrayList<StateConsumer<F>> stateConsumers = this.stateConsumers;
      for (final StateConsumer<F> stateConsumer : stateConsumers) {
        stateConsumer.accept(state);
      }
      stateConsumers.clear();
    }

    private void materialized(@NotNull final StateConsumer<F> consumer) {
      final ArrayList<StateConsumer<F>> stateConsumers = this.stateConsumers;
      stateConsumers.add(consumer);
      if (stateConsumers.size() == 1) {
        wrapped.materializeSize(new CancellableAsyncConsumer<Integer>() {
          @Override
          public void cancellableAccept(final Integer size) {
            wrapped.materializePrevWhile(size - 1, new CancellableIndexedAsyncPredicate<E>() {
              private F current = identity;

              @Override
              public void cancellableComplete(final int size) throws Exception {
                setState(identity);
              }

              @Override
              public boolean cancellableTest(final int size, final int index, final E element)
                  throws Exception {
                current = operation.apply(element, current);
                if (index == 0) {
                  setState(current);
                  return false;
                }
                return true;
              }

              @Override
              public void error(@NotNull final Exception error) {
                setError(error);
              }
            });
          }

          @Override
          public void error(@NotNull final Exception error) {
            setError(error);
          }
        });
      }
    }

    private void setError(@NotNull final Exception error) {
      final CancellationException exception = cancelException.get();
      if (exception != null) {
        consumeState(setCancelled(exception));
      } else {
        consumeState(setFailed(error));
      }
    }

    private void setState(final F result) throws Exception {
      consumeState(setDone(new ElementToListAsyncMaterializer<F>(
          decorateFunction.apply(Collections.singletonList(result)))));
    }
  }
}
