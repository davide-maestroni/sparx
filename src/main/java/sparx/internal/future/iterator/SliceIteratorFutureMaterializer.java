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

import static sparx.internal.future.FutureConsumers.safeConsume;
import static sparx.internal.future.FutureConsumers.safeConsumeError;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.FutureConsumer;
import sparx.internal.future.IndexedFutureConsumer;
import sparx.internal.future.IndexedFuturePredicate;
import sparx.util.annotation.Positive;
import sparx.util.function.TernaryFunction;

public class SliceIteratorFutureMaterializer<E> extends AbstractIteratorFutureMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      SliceIteratorFutureMaterializer.class.getName());

  private final boolean isMaterializedAtOnce;

  public SliceIteratorFutureMaterializer(@NotNull final IteratorFutureMaterializer<E> wrapped,
      final int start, final int end, @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final TernaryFunction<List<E>, Integer, Integer, List<E>> sliceFunction) {
    super(context);
    if (start >= 0) {
      isMaterializedAtOnce = false;
      setState(new PendingState(wrapped, start, end, context, cancelException, sliceFunction));
    } else {
      isMaterializedAtOnce = wrapped.isMaterializedAtOnce();
      setState(new ImmaterialState(wrapped, start, end, cancelException, sliceFunction));
    }
  }

  @Override
  public boolean isMaterializedAtOnce() {
    return isMaterializedAtOnce || super.isMaterializedAtOnce();
  }

  @Override
  public int knownSize() {
    return -1;
  }

  private class ImmaterialState extends ImmediateIteratorFutureMaterializerState<E, E> {

    private final AtomicReference<CancellationException> cancelException;
    private final int end;
    private final TernaryFunction<List<E>, Integer, Integer, List<E>> sliceFunction;
    private final int start;
    private final IteratorFutureMaterializer<E> wrapped;

    public ImmaterialState(@NotNull final IteratorFutureMaterializer<E> wrapped, final int start,
        final int end, @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final TernaryFunction<List<E>, Integer, Integer, List<E>> sliceFunction) {
      super(SliceIteratorFutureMaterializer.this, wrapped, LOGGER);
      this.wrapped = wrapped;
      this.start = start;
      this.end = end;
      this.cancelException = cancelException;
      this.sliceFunction = sliceFunction;
    }

    @Override
    public int weightElements() {
      return isMaterializing() ? 1 : wrapped.weightElements();
    }

    @Override
    void materialize() {
      wrapped.materializeElements(new CancellableFutureConsumer<List<E>>() {
        @Override
        public void cancellableAccept(final List<E> elements) throws Exception {
          final List<E> materialized = sliceFunction.apply(elements, start, end);
          setDone(new ListToIteratorFutureMaterializer<E>(materialized, context));
          consumeElements(materialized);
        }

        @Override
        public void error(@NotNull final Exception error) {
          final CancellationException exception = cancelException.get();
          if (exception != null) {
            setCancelled(exception);
            consumeError(exception);
          } else {
            setFailed(error);
            consumeError(error);
          }
        }
      });
    }
  }

  private class PendingState implements IteratorFutureMaterializer<E> {

    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final ArrayList<FutureConsumer<List<E>>> elementsConsumers = new ArrayList<FutureConsumer<List<E>>>(
        2);
    private final int end;
    private final ArrayList<FutureConsumer<IteratorFutureMaterializer<E>>> materializerConsumers = new ArrayList<FutureConsumer<IteratorFutureMaterializer<E>>>(
        2);
    private final TernaryFunction<List<E>, Integer, Integer, List<E>> sliceFunction;
    private final int start;
    private final IteratorFutureMaterializer<E> wrapped;

    private PendingState(@NotNull final IteratorFutureMaterializer<E> wrapped, final int start,
        final int end, @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final TernaryFunction<List<E>, Integer, Integer, List<E>> sliceFunction) {
      this.wrapped = wrapped;
      this.start = start;
      this.end = end;
      this.context = context;
      this.cancelException = cancelException;
      this.sliceFunction = sliceFunction;
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
      setCancelled(exception);
      consumeError(exception);
    }

    @Override
    public void materializeElements(@NotNull final FutureConsumer<List<E>> consumer) {
      final ArrayList<FutureConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      elementsConsumers.add(consumer);
      if (elementsConsumers.size() == 1) {
        materialize(new FutureConsumer<IteratorFutureMaterializer<E>>() {
          @Override
          public void accept(final IteratorFutureMaterializer<E> materializer) {
            materializer.materializeElements(new FutureConsumer<List<E>>() {
              @Override
              public void accept(final List<E> elements) {
                setDone(new ListToIteratorFutureMaterializer<E>(elements, context));
                consumeElements(elements);
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

    @Override
    public void materializeHasNext(@NotNull final FutureConsumer<Boolean> consumer) {
      materialize(new FutureConsumer<IteratorFutureMaterializer<E>>() {
        @Override
        public void accept(final IteratorFutureMaterializer<E> materializer) {
          materializer.materializeHasNext(consumer);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeIterator(@NotNull final FutureConsumer<Iterator<E>> consumer) {
      materializeElements(new FutureConsumer<List<E>>() {
        @Override
        public void accept(final List<E> elements) {
          getState().materializeIterator(consumer);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeNext(@NotNull final IndexedFutureConsumer<E> consumer) {
      materialize(new FutureConsumer<IteratorFutureMaterializer<E>>() {
        @Override
        public void accept(final IteratorFutureMaterializer<E> materializer) {
          materializer.materializeNext(consumer);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeNextWhile(@NotNull final IndexedFuturePredicate<E> predicate) {
      materialize(new FutureConsumer<IteratorFutureMaterializer<E>>() {
        @Override
        public void accept(final IteratorFutureMaterializer<E> materializer) {
          materializer.materializeNextWhile(predicate);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          predicate.error(error);
        }
      });
    }

    @Override
    public void materializeSkip(@Positive final int count,
        @NotNull final FutureConsumer<Integer> consumer) {
      materialize(new FutureConsumer<IteratorFutureMaterializer<E>>() {
        @Override
        public void accept(final IteratorFutureMaterializer<E> materializer) {
          materializer.materializeSkip(count, consumer);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public int weightElements() {
      return elementsConsumers.isEmpty() ? 1 : weightMaterialize();
    }

    @Override
    public int weightHasNext() {
      return weightMaterialize();
    }

    @Override
    public int weightNext() {
      return weightMaterialize();
    }

    @Override
    public int weightNextWhile() {
      return weightMaterialize();
    }

    @Override
    public int weightSkip() {
      return weightMaterialize();
    }

    private void consumeElements(@NotNull final List<E> elements) {
      final ArrayList<FutureConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      for (final FutureConsumer<List<E>> elementsConsumer : elementsConsumers) {
        safeConsume(elementsConsumer, elements, LOGGER);
      }
      elementsConsumers.clear();
    }

    private void consumeError(@NotNull final Exception error) {
      final ArrayList<FutureConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      for (final FutureConsumer<List<E>> elementsConsumer : elementsConsumers) {
        safeConsumeError(elementsConsumer, error, LOGGER);
      }
      elementsConsumers.clear();
    }

    private void materialize(
        @NotNull final FutureConsumer<IteratorFutureMaterializer<E>> consumer) {
      final ArrayList<FutureConsumer<IteratorFutureMaterializer<E>>> materializerConsumers = this.materializerConsumers;
      materializerConsumers.add(consumer);
      if (materializerConsumers.size() == 1) {
        wrapped.materializeSkip(start, new CancellableFutureConsumer<Integer>() {
          @Override
          public void cancellableAccept(final Integer skipped) {
            if (skipped < start) {
              final IteratorFutureMaterializer<E> newState = setDone(
                  EmptyIteratorFutureMaterializer.<E>instance());
              for (FutureConsumer<IteratorFutureMaterializer<E>> consumer : materializerConsumers) {
                safeConsume(consumer, newState, LOGGER);
              }
              materializerConsumers.clear();
            } else {
              wrapped.materializeElements(new CancellableFutureConsumer<List<E>>() {
                @Override
                public void cancellableAccept(final List<E> elements) throws Exception {
                  final int materializedEnd;
                  if (end < 0) {
                    materializedEnd = end;
                  } else {
                    materializedEnd = Math.max(0, end - start);
                  }
                  final List<E> materialized = sliceFunction.apply(elements, 0, materializedEnd);
                  final IteratorFutureMaterializer<E> newState = setState(
                      new ListToIteratorFutureMaterializer<E>(materialized, context));
                  for (FutureConsumer<IteratorFutureMaterializer<E>> consumer : materializerConsumers) {
                    safeConsume(consumer, newState, LOGGER);
                  }
                  materializerConsumers.clear();
                }

                @Override
                public void error(@NotNull final Exception error) {
                  final IteratorFutureMaterializer<E> newState;
                  final CancellationException exception = cancelException.get();
                  if (exception != null) {
                    newState = setCancelled(exception);
                  } else {
                    newState = setFailed(error);
                  }
                  for (FutureConsumer<IteratorFutureMaterializer<E>> consumer : materializerConsumers) {
                    safeConsume(consumer, newState, LOGGER);
                  }
                  materializerConsumers.clear();
                }
              });
            }
          }

          @Override
          public void error(@NotNull final Exception error) {
            final IteratorFutureMaterializer<E> newState;
            final CancellationException exception = cancelException.get();
            if (exception != null) {
              newState = setCancelled(exception);
            } else {
              newState = setFailed(error);
            }
            for (FutureConsumer<IteratorFutureMaterializer<E>> consumer : materializerConsumers) {
              safeConsume(consumer, newState, LOGGER);
            }
            materializerConsumers.clear();
          }
        });
      }
    }

    private void setError(@NotNull final Exception error) {
      final CancellationException exception = cancelException.get();
      if (exception != null) {
        setCancelled(exception);
        consumeError(exception);
      } else {
        setFailed(error);
        consumeError(error);
      }
    }

    private int weightMaterialize() {
      return materializerConsumers.isEmpty() ? 1 : (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightSkip() + wrapped.weightElements());
    }
  }
}
