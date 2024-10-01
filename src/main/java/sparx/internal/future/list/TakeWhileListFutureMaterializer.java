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

import static sparx.internal.future.FutureConsumers.safeConsume;
import static sparx.internal.future.FutureConsumers.safeConsumeError;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.FutureConsumer;
import sparx.internal.future.IndexedFutureConsumer;
import sparx.internal.future.IndexedFuturePredicate;
import sparx.util.annotation.NotNegative;
import sparx.util.function.IndexedPredicate;

public class TakeWhileListFutureMaterializer<E> extends AbstractListFutureMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      TakeWhileListFutureMaterializer.class.getName());

  private final boolean isMaterializedAtOnce;

  public TakeWhileListFutureMaterializer(@NotNull final ListFutureMaterializer<E> wrapped,
      @NotNull final IndexedPredicate<? super E> predicate, @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    super(context, new AtomicInteger(STATUS_RUNNING));
    isMaterializedAtOnce = wrapped.isMaterializedAtOnce();
    setState(new ImmaterialState(wrapped, predicate, context, cancelException));
  }

  @Override
  public boolean isMaterializedAtOnce() {
    return isMaterializedAtOnce || super.isMaterializedAtOnce();
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

    private int testedIndex = -1;

    public ImmaterialState(@NotNull final ListFutureMaterializer<E> wrapped,
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      this.wrapped = wrapped;
      this.predicate = predicate;
      this.context = context;
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
      consumeState(setCancelled(exception));
    }

    @Override
    public void materializeContains(final Object element,
        @NotNull final FutureConsumer<Boolean> consumer) {
      if (element == null) {
        wrapped.materializeNextWhile(0, new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) throws Exception {
            setState(new WrappingState(wrapped, cancelException));
            consumer.accept(false);
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            if (testedIndex < index) {
              try {
                if (!predicate.test(index, element)) {
                  setState(index);
                  consumer.accept(false);
                  return false;
                }
              } catch (final Exception e) {
                setError(e);
                throw e;
              }
              testedIndex = index;
            }
            if (element == null) {
              consumer.accept(true);
              return false;
            }
            return true;
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      } else {
        final Object other = element;
        wrapped.materializeNextWhile(0, new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) throws Exception {
            setState(new WrappingState(wrapped, cancelException));
            consumer.accept(false);
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            if (testedIndex < index) {
              try {
                if (!predicate.test(index, element)) {
                  setState(index);
                  consumer.accept(false);
                  return false;
                }
              } catch (final Exception e) {
                setError(e);
                throw e;
              }
              testedIndex = index;
            }
            if (other.equals(element)) {
              consumer.accept(true);
              return false;
            }
            return true;
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
    }

    @Override
    public void materializeElement(@NotNegative final int index,
        @NotNull final IndexedFutureConsumer<E> consumer) {
      if (index <= testedIndex) {
        wrapped.materializeElement(index, new CancellableIndexedFutureConsumer<E>() {
          @Override
          public void cancellableAccept(final int size, final int index, final E element)
              throws Exception {
            consumer.accept(-1, index, element);
          }

          @Override
          public void cancellableComplete(final int size) throws Exception {
            consumer.complete(size);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      } else {
        final int originalIndex = index;
        wrapped.materializeNextWhile(testedIndex + 1, new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) throws Exception {
            setState(new WrappingState(wrapped, cancelException));
            consumer.complete(size);
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            if (testedIndex < index) {
              try {
                if (!predicate.test(index, element)) {
                  final ListFutureMaterializer<E> state = setState(index);
                  state.materializeElement(originalIndex, consumer);
                  return false;
                }
              } catch (final Exception e) {
                setError(e);
                throw e;
              }
              testedIndex = index;
            }
            if (originalIndex == index) {
              consumer.accept(-1, index, element);
              return false;
            }
            return true;
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
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
      if (testedIndex >= 0) {
        safeConsume(consumer, false, LOGGER);
      } else {
        wrapped.materializeElement(0, new CancellableIndexedFutureConsumer<E>() {
          @Override
          public void cancellableAccept(final int size, final int index, final E element)
              throws Exception {
            if (testedIndex < index) {
              try {
                if (!predicate.test(index, element)) {
                  setState(index).materializeEmpty(consumer);
                  return;
                }
              } catch (final Exception e) {
                setError(e);
                throw e;
              }
              testedIndex = index;
            }
            consumer.accept(false);
          }

          @Override
          public void cancellableComplete(final int size) throws Exception {
            consumer.accept(true);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
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
      final int originalIndex = index;
      wrapped.materializeNextWhile(Math.min(index, testedIndex + 1),
          new CancellableIndexedFuturePredicate<E>() {
            @Override
            public void cancellableComplete(final int size) throws Exception {
              setState(new WrappingState(wrapped, cancelException));
              predicate.complete(size);
            }

            @Override
            public boolean cancellableTest(final int size, final int index, final E element)
                throws Exception {
              if (testedIndex < index) {
                try {
                  if (!ImmaterialState.this.predicate.test(index, element)) {
                    final ListFutureMaterializer<E> state = setState(index);
                    state.materializeNextWhile(index, predicate);
                    return false;
                  }
                } catch (final Exception e) {
                  setError(e);
                  throw e;
                }
                testedIndex = index;
              }
              if (originalIndex <= index) {
                return predicate.test(-1, index, element);
              }
              return true;
            }

            @Override
            public void error(@NotNull final Exception error) throws Exception {
              predicate.error(error);
            }
          });
    }

    @Override
    public void materializePrevWhile(@NotNegative final int index,
        @NotNull final IndexedFuturePredicate<E> predicate) {
      if (index <= testedIndex) {
        wrapped.materializePrevWhile(index, new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) throws Exception {
            predicate.complete(-1);
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            return predicate.test(-1, index, element);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            predicate.error(error);
          }
        });
      } else {
        materialized(new StateConsumer<E>() {
          @Override
          public void accept(@NotNull final ListFutureMaterializer<E> state) {
            state.materializePrevWhile(index, predicate);
          }
        });
      }
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
      return wrapped.weightNextWhile();
    }

    @Override
    public int weightElement() {
      final ListFutureMaterializer<E> wrapped = this.wrapped;
      return Math.max(wrapped.weightElement(), wrapped.weightNextWhile());
    }

    @Override
    public int weightElements() {
      return stateConsumers.isEmpty() ? wrapped.weightNextWhile() : 1;
    }

    @Override
    public int weightEmpty() {
      return testedIndex < 0 ? wrapped.weightElement() : 1;
    }

    @Override
    public int weightHasElement() {
      return weightElements();
    }

    @Override
    public int weightNextWhile() {
      return wrapped.weightNextWhile();
    }

    @Override
    public int weightPrevWhile() {
      return Math.max(wrapped.weightPrevWhile(), weightElements());
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

    private void materialized(@NotNull final StateConsumer<E> consumer) {
      final ArrayList<StateConsumer<E>> stateConsumers = this.stateConsumers;
      stateConsumers.add(consumer);
      if (stateConsumers.size() == 1) {
        wrapped.materializeNextWhile(testedIndex + 1, new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) {
            consumeState(setState(new WrappingState(wrapped, cancelException)));
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            if (testedIndex < index) {
              if (!predicate.test(index, element)) {
                setState(index);
                return false;
              }
              testedIndex = index;
            }
            return true;
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

    private @NotNull ListFutureMaterializer<E> setState(final int index) {
      final ListFutureMaterializer<E> state;
      if (index == 0) {
        state = setDone(EmptyListFutureMaterializer.<E>instance());
      } else {
        state = TakeWhileListFutureMaterializer.this.setState(
            new TakeListFutureMaterializer<E>(wrapped, index, status, context, cancelException));
      }
      consumeState(state);
      return state;
    }

    private @NotNull ListFutureMaterializer<E> setState(
        @NotNull final ListFutureMaterializer<E> newState) {
      final ListFutureMaterializer<E> state = TakeWhileListFutureMaterializer.this.setState(
          newState);
      consumeState(state);
      return state;
    }
  }
}
