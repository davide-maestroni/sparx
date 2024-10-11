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
import sparx.util.function.BinaryFunction;

public class InsertAllIteratorFutureMaterializer<E> extends AbstractIteratorFutureMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      InsertAllIteratorFutureMaterializer.class.getName());

  private final int knownSize;
  private final boolean isMaterializedAtOnce;

  public InsertAllIteratorFutureMaterializer(@NotNull final IteratorFutureMaterializer<E> wrapped,
      @NotNull final IteratorFutureMaterializer<E> elementsMaterializer,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final BinaryFunction<List<E>, List<E>, List<E>> prependFunction) {
    this(wrapped, elementsMaterializer, new AtomicInteger(STATUS_RUNNING), context, cancelException,
        prependFunction, 0);
  }

  InsertAllIteratorFutureMaterializer(@NotNull final IteratorFutureMaterializer<E> wrapped,
      @NotNull final IteratorFutureMaterializer<E> elementsMaterializer,
      @NotNull final AtomicInteger status, @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final BinaryFunction<List<E>, List<E>, List<E>> prependFunction, final int offset) {
    super(context, status);
    knownSize = safeSize(wrapped.knownSize(), elementsMaterializer.knownSize());
    isMaterializedAtOnce =
        wrapped.isMaterializedAtOnce() && elementsMaterializer.isMaterializedAtOnce();
    setState(new ImmaterialState(wrapped, elementsMaterializer, context, cancelException,
        prependFunction, offset));
  }

  private static int safeSize(final int wrappedSize, final int elementsSize) {
    if (wrappedSize >= 0 && elementsSize >= 0) {
      if (elementsSize > 0) {
        return SizeOverflowException.safeCast((long) wrappedSize + elementsSize);
      }
      return wrappedSize;
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
    private final ArrayList<FutureConsumer<List<E>>> elementsConsumers = new ArrayList<FutureConsumer<List<E>>>(
        2);
    private final IteratorFutureMaterializer<E> elementsMaterializer;
    private final BinaryFunction<List<E>, List<E>, List<E>> prependFunction;
    private final IteratorFutureMaterializer<E> wrapped;

    private int index;

    public ImmaterialState(@NotNull final IteratorFutureMaterializer<E> wrapped,
        @NotNull final IteratorFutureMaterializer<E> elementsMaterializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final BinaryFunction<List<E>, List<E>, List<E>> prependFunction,
        final int offset) {
      this.wrapped = wrapped;
      this.elementsMaterializer = elementsMaterializer;
      this.context = context;
      this.cancelException = cancelException;
      this.prependFunction = prependFunction;
      index = offset;
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
      return wrapped.isMaterializedAtOnce() && elementsMaterializer.isMaterializedAtOnce();
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
      elementsMaterializer.materializeCancel(exception);
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
          public void cancellableAccept(final List<E> elements) {
            final List<E> wrappedElements = elements;
            elementsMaterializer.materializeElements(new CancellableFutureConsumer<List<E>>() {
              @Override
              public void cancellableAccept(final List<E> elements) throws Exception {
                final List<E> materialized = prependFunction.apply(wrappedElements, elements);
                if (materialized.isEmpty()) {
                  setDone(EmptyIteratorFutureMaterializer.<E>instance());
                  consumeElements(Collections.<E>emptyList());
                } else {
                  setDone(new ListToIteratorFutureMaterializer<E>(materialized, context, index));
                  consumeElements(materialized);
                }
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
      elementsMaterializer.materializeHasNext(new CancellableFutureConsumer<Boolean>() {
        @Override
        public void cancellableAccept(final Boolean hasNext) throws Exception {
          if (hasNext) {
            consumer.accept(true);
          } else {
            setState(new WrappingState(wrapped, cancelException, index)).materializeHasNext(
                consumer);
          }
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
      elementsMaterializer.materializeNext(new CancellableIndexedFutureConsumer<E>() {
        @Override
        public void cancellableAccept(final int size, final int index, final E element)
            throws Exception {
          consumer.accept(-1, ImmaterialState.this.index++, element);
        }

        @Override
        public void cancellableComplete(final int size) {
          setState(new WrappingState(wrapped, cancelException, index)).materializeNext(consumer);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeNextWhile(@NotNull final IndexedFuturePredicate<E> predicate) {
      elementsMaterializer.materializeNextWhile(new CancellableIndexedFuturePredicate<E>() {
        @Override
        public void cancellableComplete(final int size) {
          setState(new WrappingState(wrapped, cancelException, index)).materializeNextWhile(
              predicate);
        }

        @Override
        public boolean cancellableTest(final int size, final int index, final E element)
            throws Exception {
          return predicate.test(-1, ImmaterialState.this.index++, element);
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
      elementsMaterializer.materializeSkip(count, new CancellableFutureConsumer<Integer>() {
        @Override
        public void cancellableAccept(final Integer skipped) throws Exception {
          index += skipped;
          if (skipped < count) {
            final int elementsSkipped = skipped;
            setState(new WrappingState(wrapped, cancelException, index)).materializeSkip(
                count - skipped, new CancellableFutureConsumer<Integer>() {
                  @Override
                  public void cancellableAccept(final Integer skipped) throws Exception {
                    index += skipped;
                    consumer.accept(elementsSkipped + skipped);
                  }

                  @Override
                  public void error(@NotNull final Exception error) throws Exception {
                    consumer.error(error);
                  }
                });
          } else {
            consumer.accept(skipped);
          }
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public int weightElements() {
      return elementsConsumers.isEmpty() ? (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightElements() + elementsMaterializer.weightElements()) : 1;
    }

    @Override
    public int weightHasNext() {
      return (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightHasNext() + elementsMaterializer.weightHasNext());
    }

    @Override
    public int weightNext() {
      return (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightNext() + elementsMaterializer.weightNext());
    }

    @Override
    public int weightNextWhile() {
      return (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightNextWhile() + elementsMaterializer.weightNextWhile());
    }

    @Override
    public int weightSkip() {
      return (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightSkip() + elementsMaterializer.weightSkip());
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
  }
}
