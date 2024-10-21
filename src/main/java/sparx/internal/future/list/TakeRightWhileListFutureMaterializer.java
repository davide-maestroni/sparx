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

public class TakeRightWhileListFutureMaterializer<E> extends AbstractListFutureMaterializer<E> {

  public TakeRightWhileListFutureMaterializer(@NotNull final ListFutureMaterializer<E> wrapped,
      @NotNull final IndexedPredicate<? super E> predicate, @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    super(context);
    setState(new ImmaterialState(wrapped, predicate, context, cancelException));
  }

  @Override
  public int knownSize() {
    return -1;
  }

  private interface StateConsumer<E> {

    void accept(@NotNull ListFutureMaterializer<E> state);
  }

  private class ImmaterialState implements ListFutureMaterializer<E> {

    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final IndexedPredicate<? super E> predicate;
    private final ArrayList<StateConsumer<E>> stateConsumers = new ArrayList<StateConsumer<E>>(2);
    private final ListFutureMaterializer<E> wrapped;

    private int wrappedSize;

    public ImmaterialState(@NotNull final ListFutureMaterializer<E> wrapped,
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      this.wrapped = wrapped;
      this.predicate = predicate;
      this.context = context;
      this.cancelException = cancelException;
      wrappedSize = wrapped.knownSize();
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
      consumeState(setCancelled(exception));
    }

    @Override
    public void materializeContains(final Object element,
        @NotNull final FutureConsumer<Boolean> consumer) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<E> state) {
          state.materializeContains(element, consumer);
        }
      });
    }

    @Override
    public void materializeElement(@NotNegative final int index,
        @NotNull final IndexedFutureConsumer<E> consumer) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<E> state) {
          state.materializeElement(index, consumer);
        }
      });
    }

    @Override
    public void materializeElements(@NotNull final FutureConsumer<List<E>> consumer) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<E> state) {
          state.materializeElements(new FutureConsumer<List<E>>() {
            @Override
            public void accept(final List<E> elements) throws Exception {
              setDone(state);
              consumer.accept(elements);
            }

            @Override
            public void error(@NotNull final Exception error) throws Exception {
              consumer.error(error);
            }
          });
        }
      });
    }

    @Override
    public void materializeEmpty(@NotNull final FutureConsumer<Boolean> consumer) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<E> state) {
          state.materializeEmpty(consumer);
        }
      });
    }

    @Override
    public void materializeHasElement(@NotNegative final int index,
        @NotNull final FutureConsumer<Boolean> consumer) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<E> state) {
          state.materializeHasElement(index, consumer);
        }
      });
    }

    @Override
    public void materializeNextWhile(@NotNegative final int index,
        @NotNull final IndexedFuturePredicate<E> predicate) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<E> state) {
          state.materializeNextWhile(index, predicate);
        }
      });
    }

    @Override
    public void materializePrevWhile(@NotNegative final int index,
        @NotNull final IndexedFuturePredicate<E> predicate) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<E> state) {
          state.materializePrevWhile(index, predicate);
        }
      });
    }

    @Override
    public void materializeSize(@NotNull final FutureConsumer<Integer> consumer) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<E> state) {
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
      if (stateConsumers.isEmpty()) {
        if (wrappedSize < 0) {
          return (int) Math.min(Integer.MAX_VALUE,
              (long) wrapped.weightSize() + wrapped.weightPrevWhile());
        }
        return wrapped.weightPrevWhile();
      }
      return 1;
    }

    @Override
    public int weightEmpty() {
      return weightElements();
    }

    @Override
    public int weightHasElement() {
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
    public int weightSize() {
      return weightElements();
    }

    private void consumeState(@NotNull final ListFutureMaterializer<E> state) {
      final ArrayList<StateConsumer<E>> stateConsumers = this.stateConsumers;
      for (final StateConsumer<E> stateConsumer : stateConsumers) {
        stateConsumer.accept(state);
      }
      stateConsumers.clear();
    }

    private void materialize() {
      wrapped.materializePrevWhile(wrappedSize - 1, new CancellableIndexedFuturePredicate<E>() {
        @Override
        public void cancellableComplete(final int size) {
          consumeState(setState(new WrappingState(wrapped, cancelException)));
        }

        @Override
        public boolean cancellableTest(final int size, final int index, final E element)
            throws Exception {
          if (predicate.test(index, element)) {
            return true;
          } else {
            final int maxElements = wrappedSize - index - 1;
            if (maxElements == 0) {
              consumeState(setDone(EmptyListFutureMaterializer.<E>instance()));
            } else {
              consumeState(setState(
                  new TakeRightListFutureMaterializer<E>(wrapped, maxElements, context,
                      cancelException)));
            }
          }
          return false;
        }

        @Override
        public void error(@NotNull final Exception error) {
          setError(error);
        }
      });
    }

    private void materialized(@NotNull final StateConsumer<E> consumer) {
      final ArrayList<StateConsumer<E>> stateConsumers = this.stateConsumers;
      stateConsumers.add(consumer);
      if (stateConsumers.size() == 1) {
        if (wrappedSize < 0) {
          wrapped.materializeSize(new CancellableFutureConsumer<Integer>() {
            @Override
            public void cancellableAccept(final Integer size) {
              wrappedSize = size;
              materialize();
            }

            @Override
            public void error(@NotNull final Exception error) {
              setError(error);
            }
          });
        } else {
          materialize();
        }
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
  }
}
