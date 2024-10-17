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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.FutureConsumer;
import sparx.internal.future.IndexedFutureConsumer;
import sparx.internal.future.IndexedFuturePredicate;
import sparx.util.SizeOverflowException;
import sparx.util.annotation.Positive;
import sparx.util.function.TernaryFunction;

public class InsertAfterIteratorFutureMaterializer<E> extends
    AbstractIteratorFutureMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      InsertAfterIteratorFutureMaterializer.class.getName());

  private final int knownSize;
  private final boolean isMaterializedAtOnce;

  public InsertAfterIteratorFutureMaterializer(@NotNull final IteratorFutureMaterializer<E> wrapped,
      @Positive final int numElements, final E element, @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final TernaryFunction<List<E>, Integer, E, List<E>> insertFunction) {
    this(wrapped, numElements, element, new AtomicInteger(STATUS_RUNNING), context, cancelException,
        insertFunction);
  }

  InsertAfterIteratorFutureMaterializer(@NotNull final IteratorFutureMaterializer<E> wrapped,
      @Positive final int numElements, final E element, @NotNull final AtomicInteger status,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final TernaryFunction<List<E>, Integer, E, List<E>> insertFunction) {
    super(context, status);
    knownSize = safeSize(wrapped.knownSize(), numElements);
    isMaterializedAtOnce = wrapped.isMaterializedAtOnce();
    setState(new ImmaterialState(wrapped, numElements, element, context, cancelException,
        insertFunction));
  }

  private static int safeSize(final int wrappedSize, final int numElements) {
    if (wrappedSize >= numElements) {
      return SizeOverflowException.safeCast((long) wrappedSize + 1);
    }
    return -1;
  }

  @Override
  public boolean isMaterializedAtOnce() {
    return isMaterializedAtOnce || super.isMaterializedAtOnce();
  }

  @Override
  public int knownSize() {
    return knownSize;
  }

  private class ImmaterialState implements IteratorFutureMaterializer<E> {

    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final E element;
    private final ArrayList<FutureConsumer<List<E>>> elementsConsumers = new ArrayList<FutureConsumer<List<E>>>(
        2);
    private final TernaryFunction<List<E>, Integer, E, List<E>> insertFunction;
    private final int numElements;
    private final IteratorFutureMaterializer<E> wrapped;

    private int index;

    public ImmaterialState(@NotNull final IteratorFutureMaterializer<E> wrapped,
        @Positive final int numElements, final E element, @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final TernaryFunction<List<E>, Integer, E, List<E>> insertFunction) {
      this.wrapped = wrapped;
      this.numElements = numElements;
      this.element = element;
      this.context = context;
      this.cancelException = cancelException;
      this.insertFunction = insertFunction;
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
      return knownSize;
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
        wrapped.materializeElements(new CancellableFutureConsumer<List<E>>() {
          @Override
          public void cancellableAccept(final List<E> elements) throws Exception {
            if (index > numElements) {
              setDone(new ListToIteratorFutureMaterializer<E>(elements, context, index));
              consumeElements(elements);
            } else {
              final List<E> materialized = insertFunction.apply(elements, numElements - index,
                  element);
              if (materialized.isEmpty()) {
                setDone(EmptyIteratorFutureMaterializer.<E>instance());
                consumeElements(Collections.<E>emptyList());
              } else {
                setDone(new ListToIteratorFutureMaterializer<E>(materialized, context, index));
                consumeElements(materialized);
              }
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

    @Override
    public void materializeHasNext(@NotNull final FutureConsumer<Boolean> consumer) {
      if (index == numElements) {
        safeConsume(consumer, true, LOGGER);
      } else {
        wrapped.materializeHasNext(new CancellableFutureConsumer<Boolean>() {
          @Override
          public void cancellableAccept(final Boolean hasNext) throws Exception {
            consumer.accept(hasNext);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
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
      if (index == numElements) {
        setState(new WrappingState(wrapped, cancelException, ++index));
        safeConsume(consumer, -1, numElements, element, LOGGER);
      } else {
        wrapped.materializeNext(new CancellableIndexedFutureConsumer<E>() {
          @Override
          public void cancellableAccept(final int size, final int index, final E element)
              throws Exception {
            consumer.accept(safeSize(size), ImmaterialState.this.index++, element);
          }

          @Override
          public void cancellableComplete(final int size) throws Exception {
            setDone(EmptyIteratorFutureMaterializer.<E>instance());
            consumer.complete(0);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
    }

    @Override
    public void materializeNextWhile(@NotNull final IndexedFuturePredicate<E> predicate) {
      if (index == numElements) {
        final IteratorFutureMaterializer<E> state = setState(
            new WrappingState(wrapped, cancelException, ++index));
        if (safeConsume(predicate, -1, numElements, element, LOGGER)) {
          state.materializeNextWhile(predicate);
        }
      } else {
        wrapped.materializeNextWhile(new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) throws Exception {
            setDone(EmptyIteratorFutureMaterializer.<E>instance());
            predicate.complete(0);
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            final boolean next = predicate.test(safeSize(size), ImmaterialState.this.index++,
                element);
            if (next && ImmaterialState.this.index == numElements) {
              materializeNextWhile(predicate);
              return false;
            }
            return next;
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            predicate.error(error);
          }
        });
      }
    }

    @Override
    public void materializeSkip(@Positive final int count,
        @NotNull final FutureConsumer<Integer> consumer) {
      if (index == numElements) {
        ++index;
        final IteratorFutureMaterializer<E> state = setState(
            new WrappingState(wrapped, cancelException, index));
        if (count == 1) {
          safeConsume(consumer, 1, LOGGER);
        } else {
          state.materializeSkip(count - 1, new FutureConsumer<Integer>() {
            @Override
            public void accept(final Integer skipped) throws Exception {
              index += skipped;
              consumer.accept(skipped + 1);
            }

            @Override
            public void error(@NotNull final Exception error) throws Exception {
              consumer.error(error);
            }
          });
        }
      } else {
        final int remaining = numElements - index;
        if (remaining < count) {
          wrapped.materializeSkip(remaining, new CancellableFutureConsumer<Integer>() {
            @Override
            public void cancellableAccept(final Integer skipped) throws Exception {
              index += skipped;
              if (skipped < remaining) {
                consumer.accept(skipped);
              } else {
                final int offset = skipped;
                materializeSkip(count - skipped, new FutureConsumer<Integer>() {
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
            }

            @Override
            public void error(@NotNull final Exception error) throws Exception {
              consumer.error(error);
            }
          });
        } else {
          wrapped.materializeSkip(count, new CancellableFutureConsumer<Integer>() {
            @Override
            public void cancellableAccept(final Integer skipped) throws Exception {
              index += skipped;
              consumer.accept(skipped);
            }

            @Override
            public void error(@NotNull final Exception error) throws Exception {
              consumer.error(error);
            }
          });
        }
      }
    }

    @Override
    public int weightElements() {
      return elementsConsumers.isEmpty() ? wrapped.weightElements() : 1;
    }

    @Override
    public int weightHasNext() {
      return index == numElements ? 1 : wrapped.weightHasNext();
    }

    @Override
    public int weightNext() {
      return index == numElements ? 1 : wrapped.weightNext();
    }

    @Override
    public int weightNextWhile() {
      return wrapped.weightNextWhile();
    }

    @Override
    public int weightSkip() {
      return wrapped.weightSkip();
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

    private int safeSize(final int size) {
      if (size >= 0) {
        return index < numElements ? SizeOverflowException.safeCast((long) size + 1) : size;
      }
      return -1;
    }
  }
}
