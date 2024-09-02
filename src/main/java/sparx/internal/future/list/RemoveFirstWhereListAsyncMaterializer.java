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
import static sparx.internal.future.AsyncConsumers.safeConsumeError;

import java.util.ArrayList;
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
import sparx.util.function.IndexedPredicate;

public class RemoveFirstWhereListAsyncMaterializer<E> extends AbstractListAsyncMaterializer<E> {

  private static final Function<? extends List<?>, ? extends List<?>> DUMMY_DECORATE_FUNCTION = new Function<List<Object>, List<Object>>() {
    @Override
    public List<Object> apply(final List<Object> elements) {
      return elements;
    }
  };
  private static final Logger LOGGER = Logger.getLogger(
      RemoveFirstWhereListAsyncMaterializer.class.getName());

  public RemoveFirstWhereListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      @NotNull final IndexedPredicate<? super E> predicate, @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final BinaryFunction<List<E>, Integer, List<E>> removeFunction) {
    super(context, new AtomicInteger(STATUS_RUNNING));
    setState(new ImmaterialState(wrapped, predicate, context, cancelException, removeFunction));
  }

  @Override
  public int knownSize() {
    return -1;
  }

  private interface StateConsumer<E> {

    void accept(@NotNull ListAsyncMaterializer<E> state);
  }

  private class ImmaterialState implements ListAsyncMaterializer<E> {

    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final IndexedPredicate<? super E> predicate;
    private final BinaryFunction<List<E>, Integer, List<E>> removeFunction;
    private final ArrayList<StateConsumer<E>> stateConsumers = new ArrayList<StateConsumer<E>>(2);
    private final ListAsyncMaterializer<E> wrapped;

    private int testedIndex = -1;

    public ImmaterialState(@NotNull final ListAsyncMaterializer<E> wrapped,
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final BinaryFunction<List<E>, Integer, List<E>> removeFunction) {
      this.wrapped = wrapped;
      this.predicate = predicate;
      this.context = context;
      this.cancelException = cancelException;
      this.removeFunction = removeFunction;
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
      return -1;
    }

    @Override
    public void materializeCancel(@NotNull final CancellationException exception) {
      wrapped.materializeCancel(exception);
      consumeState(setCancelled(exception));
    }

    @Override
    public void materializeContains(final Object element,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      if (element == null) {
        wrapped.materializeNextWhile(0, new CancellableIndexedAsyncPredicate<E>() {
          @Override
          public void cancellableComplete(final int size) throws Exception {
            setState(new WrappingState(wrapped, cancelException));
            consumer.accept(false);
          }

          @Override
          @SuppressWarnings("unchecked")
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            if (testedIndex < index) {
              try {
                if (predicate.test(index, element)) {
                  final ListAsyncMaterializer<E> state = setState(index);
                  if (index == 0) {
                    state.materializeContains(null, consumer);
                  } else {
                    new DropListAsyncMaterializer<E>(state, index, context, cancelException,
                        (Function<List<E>, List<E>>) DUMMY_DECORATE_FUNCTION).materializeContains(
                        null, consumer);
                  }
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
        wrapped.materializeNextWhile(0, new CancellableIndexedAsyncPredicate<E>() {
          @Override
          public void cancellableComplete(final int size) throws Exception {
            setState(new WrappingState(wrapped, cancelException));
            consumer.accept(false);
          }

          @Override
          @SuppressWarnings("unchecked")
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            if (testedIndex < index) {
              try {
                if (predicate.test(index, element)) {
                  final ListAsyncMaterializer<E> state = setState(index);
                  if (index == 0) {
                    state.materializeContains(null, consumer);
                  } else {
                    new DropListAsyncMaterializer<E>(state, index, context, cancelException,
                        (Function<List<E>, List<E>>) DUMMY_DECORATE_FUNCTION).materializeContains(
                        other, consumer);
                  }
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
    public void materializeDone(@NotNull final AsyncConsumer<List<E>> consumer) {
      safeConsumeError(consumer, new UnsupportedOperationException(), LOGGER);
    }

    @Override
    public void materializeElement(final int index,
        @NotNull final IndexedAsyncConsumer<E> consumer) {
      if (index < 0) {
        safeConsumeError(consumer, new IndexOutOfBoundsException(Integer.toString(index)), LOGGER);
      } else if (index <= testedIndex) {
        wrapped.materializeElement(index, new CancellableIndexedAsyncConsumer<E>() {
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
        wrapped.materializeNextWhile(testedIndex + 1, new CancellableIndexedAsyncPredicate<E>() {
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
                if (predicate.test(index, element)) {
                  final ListAsyncMaterializer<E> state = setState(index);
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
    public void materializeElements(@NotNull final AsyncConsumer<List<E>> consumer) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final ListAsyncMaterializer<E> state) {
          state.materializeElements(consumer);
        }
      });
    }

    @Override
    public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
      if (testedIndex >= 0) {
        safeConsume(consumer, false, LOGGER);
      } else {
        wrapped.materializeElement(0, new CancellableIndexedAsyncConsumer<E>() {
          @Override
          public void cancellableAccept(final int size, final int index, final E element)
              throws Exception {
            if (testedIndex < index) {
              try {
                if (predicate.test(index, element)) {
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
    public void materializeHasElement(final int index,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      if (index < 0) {
        safeConsume(consumer, false, LOGGER);
      } else if (index <= testedIndex) {
        safeConsume(consumer, true, LOGGER);
      } else {
        final int originalIndex = index;
        wrapped.materializeNextWhile(testedIndex + 1, new CancellableIndexedAsyncPredicate<E>() {
          @Override
          public void cancellableComplete(final int size) throws Exception {
            consumer.accept(false);
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            if (testedIndex < index) {
              try {
                if (predicate.test(index, element)) {
                  final ListAsyncMaterializer<E> state = setState(index);
                  state.materializeHasElement(originalIndex, consumer);
                  return false;
                }
              } catch (final Exception e) {
                setError(e);
                throw e;
              }
              testedIndex = index;
            }
            if (originalIndex == index) {
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
    public void materializeNextWhile(final int index,
        @NotNull final IndexedAsyncPredicate<E> predicate) {
      final int originalIndex = index;
      wrapped.materializeNextWhile(Math.min(index, testedIndex + 1),
          new CancellableIndexedAsyncPredicate<E>() {
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
                  if (ImmaterialState.this.predicate.test(index, element)) {
                    final ListAsyncMaterializer<E> state = setState(index);
                    state.materializeNextWhile(Math.max(index, originalIndex), predicate);
                    return false;
                  }
                } catch (final Exception e) {
                  setError(e);
                  throw e;
                }
                testedIndex = index;
              }
              if (index >= originalIndex) {
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
    public void materializePrevWhile(final int index,
        @NotNull final IndexedAsyncPredicate<E> predicate) {
      if (index <= testedIndex) {
        wrapped.materializePrevWhile(index, new CancellableIndexedAsyncPredicate<E>() {
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
          public void accept(@NotNull final ListAsyncMaterializer<E> state) {
            state.materializePrevWhile(index, predicate);
          }
        });
      }
    }

    @Override
    public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final ListAsyncMaterializer<E> state) {
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
      final ListAsyncMaterializer<E> wrapped = this.wrapped;
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
      return wrapped.weightNextWhile();
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

    private void consumeState(@NotNull final ListAsyncMaterializer<E> state) {
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
        wrapped.materializeNextWhile(testedIndex + 1, new CancellableIndexedAsyncPredicate<E>() {
          @Override
          public void cancellableComplete(final int size) {
            setState(new WrappingState(wrapped, cancelException));
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            if (testedIndex < index) {
              if (predicate.test(index, element)) {
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

    private @NotNull ListAsyncMaterializer<E> setState(final int index) {
      final ListAsyncMaterializer<E> state = RemoveFirstWhereListAsyncMaterializer.this.setState(
          new RemoveAfterListAsyncMaterializer<E>(wrapped, index, status, context, cancelException,
              removeFunction));
      consumeState(state);
      return state;
    }

    private void setState(@NotNull final ListAsyncMaterializer<E> newState) {
      final ListAsyncMaterializer<E> state = RemoveFirstWhereListAsyncMaterializer.this.setState(
          newState);
      consumeState(state);
    }
  }
}
