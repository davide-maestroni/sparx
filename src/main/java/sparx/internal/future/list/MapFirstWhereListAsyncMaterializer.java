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
import sparx.util.function.Function;
import sparx.util.function.IndexedFunction;
import sparx.util.function.IndexedPredicate;
import sparx.util.function.TernaryFunction;

public class MapFirstWhereListAsyncMaterializer<E> extends AbstractListAsyncMaterializer<E> {

  private static final Function<? extends List<?>, ? extends List<?>> DUMMY_DECORATE_FUNCTION = new Function<List<Object>, List<Object>>() {
    @Override
    public List<Object> apply(final List<Object> elements) {
      return elements;
    }
  };
  private static final Logger LOGGER = Logger.getLogger(
      MapFirstWhereListAsyncMaterializer.class.getName());

  private final int knownSize;

  public MapFirstWhereListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      @NotNull final IndexedPredicate<? super E> predicate,
      @NotNull final IndexedFunction<? super E, ? extends E> mapper,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final TernaryFunction<List<E>, Integer, E, List<E>> replaceFunction) {
    super(new AtomicInteger(STATUS_RUNNING));
    knownSize = wrapped.knownSize();
    setState(
        new ImmaterialState(wrapped, predicate, mapper, context, cancelException, replaceFunction));
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
    private final IndexedFunction<? super E, ? extends E> mapper;
    private final IndexedPredicate<? super E> predicate;
    private final TernaryFunction<List<E>, Integer, E, List<E>> replaceFunction;
    private final ArrayList<StateConsumer<E>> stateConsumers = new ArrayList<StateConsumer<E>>(2);
    private final ListAsyncMaterializer<E> wrapped;

    private int testedIndex = -1;
    private int wrappedSize = knownSize;

    public ImmaterialState(@NotNull final ListAsyncMaterializer<E> wrapped,
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final TernaryFunction<List<E>, Integer, E, List<E>> replaceFunction) {
      this.wrapped = wrapped;
      this.predicate = predicate;
      this.mapper = mapper;
      this.context = context;
      this.cancelException = cancelException;
      this.replaceFunction = replaceFunction;
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
            wrappedSize = size;
            consumer.accept(false);
          }

          @Override
          @SuppressWarnings("unchecked")
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            wrappedSize = Math.max(wrappedSize, size);
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
            wrappedSize = size;
            consumer.accept(false);
          }

          @Override
          @SuppressWarnings("unchecked")
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            wrappedSize = Math.max(wrappedSize, size);
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
            wrappedSize = Math.max(wrappedSize, size);
            consumer.accept(-1, index, element);
          }

          @Override
          public void cancellableComplete(final int size) throws Exception {
            wrappedSize = size;
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
            consumer.complete(wrappedSize = size);
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            wrappedSize = Math.max(wrappedSize, size);
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
              consumer.accept(wrappedSize, index, element);
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
      } else if (wrappedSize >= 0) {
        safeConsume(consumer, wrappedSize == 0, LOGGER);
      } else {
        wrapped.materializeEmpty(new CancellableAsyncConsumer<Boolean>() {
          @Override
          public void cancellableAccept(final Boolean empty) throws Exception {
            consumer.accept(empty);
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
        safeConsumeError(consumer, new IndexOutOfBoundsException(Integer.toString(index)), LOGGER);
      } else if (wrappedSize >= 0) {
        safeConsume(consumer, index < wrappedSize, LOGGER);
      } else if (index <= testedIndex) {
        safeConsume(consumer, true, LOGGER);
      } else {
        wrapped.materializeHasElement(index, new CancellableAsyncConsumer<Boolean>() {
          @Override
          public void cancellableAccept(final Boolean hasElement) throws Exception {
            consumer.accept(hasElement);
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
              predicate.complete(wrappedSize = size);
            }

            @Override
            public boolean cancellableTest(final int size, final int index, final E element)
                throws Exception {
              wrappedSize = Math.max(wrappedSize, size);
              if (testedIndex < index) {
                try {
                  if (ImmaterialState.this.predicate.test(index, element)) {
                    final ListAsyncMaterializer<E> state = setState(index);
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
                return predicate.test(wrappedSize, index, element);
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
            predicate.complete(wrappedSize = Math.max(wrappedSize, size));
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            return predicate.test(wrappedSize = Math.max(wrappedSize, size), index, element);
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
      if (wrappedSize >= 0) {
        safeConsume(consumer, wrappedSize, LOGGER);
      } else {
        wrapped.materializeSize(new CancellableAsyncConsumer<Integer>() {
          @Override
          public void cancellableAccept(final Integer size) throws Exception {
            wrappedSize = size;
            consumer.accept(size);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
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
      return testedIndex < 0 && wrappedSize < 0 ? wrapped.weightEmpty() : 1;
    }

    @Override
    public int weightHasElement() {
      return wrappedSize < 0 ? wrapped.weightHasElement() : 1;
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
      return wrappedSize < 0 ? wrapped.weightSize() : 1;
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
            consumeState(setState(new WrappingState(wrapped, context, cancelException)));
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            wrappedSize = Math.max(wrappedSize, size);
            if (predicate.test(index, element)) {
              consumeState(setState(index));
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
      final ListAsyncMaterializer<E> state = MapFirstWhereListAsyncMaterializer.this.setState(
          new MapAfterListAsyncMaterializer<E>(wrapped, index, mapper, status, context,
              cancelException, replaceFunction));
      consumeState(state);
      return state;
    }

    private @NotNull ListAsyncMaterializer<E> setState(
        @NotNull final ListAsyncMaterializer<E> newState) {
      final ListAsyncMaterializer<E> state = MapFirstWhereListAsyncMaterializer.this.setState(
          newState);
      consumeState(state);
      return state;
    }
  }
}
