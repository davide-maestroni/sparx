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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.FutureConsumer;
import sparx.internal.future.IndexedFutureConsumer;
import sparx.internal.future.IndexedFuturePredicate;
import sparx.util.DequeArrayList;
import sparx.util.annotation.Positive;
import sparx.util.function.IndexedFunction;

public class SwitchExceptionallyIteratorFutureMaterializer<E> extends
    AbstractIteratorFutureMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      SwitchExceptionallyIteratorFutureMaterializer.class.getName());

  public SwitchExceptionallyIteratorFutureMaterializer(
      @NotNull final IteratorFutureMaterializer<E> wrapped,
      @NotNull final IndexedFunction<? super Throwable, ? extends IteratorFutureMaterializer<E>> mapper,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    super(context);
    setState(new ImmaterialState(wrapped, mapper, context, cancelException));
  }

  @Override
  public int knownSize() {
    return -1;
  }

  private class ImmaterialState implements IteratorFutureMaterializer<E> {

    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final ArrayList<FutureConsumer<List<E>>> elementsConsumers = new ArrayList<FutureConsumer<List<E>>>(
        2);
    private final IndexedFunction<? super Throwable, ? extends IteratorFutureMaterializer<E>> mapper;
    private final IteratorFutureMaterializer<E> wrapped;

    private IteratorFutureMaterializer<E> elementsMaterializer;
    private int index;
    private boolean isMaterialized;

    public ImmaterialState(@NotNull final IteratorFutureMaterializer<E> wrapped,
        @NotNull final IndexedFunction<? super Throwable, ? extends IteratorFutureMaterializer<E>> mapper,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      this.wrapped = wrapped;
      this.mapper = mapper;
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
      if (elementsMaterializer != null) {
        elementsMaterializer.materializeCancel(exception);
      }
      setCancelled(exception);
      consumeError(exception);
    }

    @Override
    public void materializeElements(@NotNull final FutureConsumer<List<E>> consumer) {
      final ArrayList<FutureConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      elementsConsumers.add(consumer);
      if (elementsConsumers.size() == 1) {
        final DequeArrayList<E> materialized = new DequeArrayList<E>(true);
        wrapped.materializeNextWhile(new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) {
            if (materialized.isEmpty()) {
              setDone(EmptyIteratorFutureMaterializer.<E>instance());
              consumeElements(Collections.<E>emptyList());
            } else {
              setDone(new DequeToIteratorFutureMaterializer<E>(materialized, context, index));
              consumeElements(materialized.clone());
            }
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element) {
            materialized.add(element);
            return true;
          }

          @Override
          public void error(@NotNull final Exception error) {
            final IteratorFutureMaterializer<E> materializer = mapError(error);
            if (materializer != null) {
              materializer.materializeElements(new CancellableFutureConsumer<List<E>>() {
                @Override
                public void cancellableAccept(final List<E> elements) {
                  materialized.addAll(elements);
                  if (materialized.isEmpty()) {
                    setDone(EmptyIteratorFutureMaterializer.<E>instance());
                    consumeElements(Collections.<E>emptyList());
                  } else {
                    setDone(
                        new DequeToIteratorFutureMaterializer<E>(materialized, context, index));
                    consumeElements(materialized.clone());
                  }
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
        });
      }
    }

    @Override
    public void materializeHasNext(@NotNull final FutureConsumer<Boolean> consumer) {
      wrapped.materializeHasNext(new CancellableFutureConsumer<Boolean>() {
        @Override
        public void cancellableAccept(final Boolean hasNext) throws Exception {
          if (!hasNext) {
            setDone(EmptyIteratorFutureMaterializer.<E>instance());
            consumer.accept(false);
          } else {
            consumer.accept(true);
          }
        }

        @Override
        public void error(@NotNull final Exception error) {
          final IteratorFutureMaterializer<E> materializer = mapError(error);
          if (materializer != null) {
            setMaterializer(materializer).materializeHasNext(consumer);
          }
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
      wrapped.materializeNext(new CancellableIndexedFutureConsumer<E>() {
        @Override
        public void cancellableAccept(final int size, final int index, final E element)
            throws Exception {
          consumer.accept(size, ImmaterialState.this.index++, element);
        }

        @Override
        public void cancellableComplete(final int size) throws Exception {
          setDone(EmptyIteratorFutureMaterializer.<E>instance());
          consumer.complete(0);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          final IteratorFutureMaterializer<E> materializer = mapError(error);
          if (materializer != null) {
            setMaterializer(materializer).materializeNext(consumer);
          } else {
            consumer.error(error);
          }
        }
      });
    }

    @Override
    public void materializeNextWhile(@NotNull final IndexedFuturePredicate<E> predicate) {
      wrapped.materializeNextWhile(new CancellableIndexedFuturePredicate<E>() {
        @Override
        public void cancellableComplete(final int size) throws Exception {
          setDone(EmptyIteratorFutureMaterializer.<E>instance());
          predicate.complete(0);
        }

        @Override
        public boolean cancellableTest(final int size, final int index, final E element)
            throws Exception {
          return predicate.test(size, ImmaterialState.this.index++, element);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          final IteratorFutureMaterializer<E> materializer = mapError(error);
          if (materializer != null) {
            setMaterializer(materializer).materializeNextWhile(predicate);
          } else {
            predicate.error(error);
          }
        }
      });
    }

    @Override
    public void materializeSkip(@Positive final int count,
        @NotNull final FutureConsumer<Integer> consumer) {
      wrapped.materializeNextWhile(new CancellableIndexedFuturePredicate<E>() {
        private int skipped;

        @Override
        public void cancellableComplete(final int size) throws Exception {
          consumer.accept(skipped);
        }

        @Override
        public boolean cancellableTest(final int size, final int index, final E element)
            throws Exception {
          ++ImmaterialState.this.index;
          if (++skipped >= count) {
            consumer.accept(skipped);
            return false;
          }
          return true;
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          final IteratorFutureMaterializer<E> materializer = mapError(error);
          if (materializer != null) {
            setMaterializer(materializer);
            if (skipped < count) {
              final int offset = skipped;
              getState().materializeSkip(count - skipped, new FutureConsumer<Integer>() {
                @Override
                public void accept(final Integer skipped) throws Exception {
                  consumer.accept(offset + skipped);
                }

                @Override
                public void error(@NotNull final Exception error) throws Exception {
                  consumer.error(error);
                }
              });
            }
          } else {
            consumer.error(error);
          }
        }
      });
    }

    @Override
    public int weightElements() {
      return elementsConsumers.isEmpty() ? wrapped.weightNextWhile() : 1;
    }

    @Override
    public int weightHasNext() {
      return wrapped.weightHasNext();
    }

    @Override
    public int weightNext() {
      return wrapped.weightNext();
    }

    @Override
    public int weightNextWhile() {
      return wrapped.weightNextWhile();
    }

    @Override
    public int weightSkip() {
      return wrapped.weightNextWhile();
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

    private @Nullable IteratorFutureMaterializer<E> mapError(@NotNull final Exception error) {
      final CancellationException exception = cancelException.get();
      if (exception != null) {
        setCancelled(exception);
        consumeError(exception);
      } else if (elementsMaterializer != null) {
        return elementsMaterializer;
      } else {
        try {
          return elementsMaterializer = mapper.apply(index, error);
        } catch (final Exception e) {
          setFailed(e);
          consumeError(e);
        }
      }
      return null;
    }

    private @NotNull IteratorFutureMaterializer<E> setMaterializer(
        @NotNull final IteratorFutureMaterializer<E> materializer) {
      if (isMaterialized) {
        return getState();
      } else {
        isMaterialized = true;
        return setState(new WrappingState(materializer, cancelException, index));
      }
    }
  }
}
