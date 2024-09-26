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
import sparx.util.function.IndexedPredicate;

public class CountWhereIteratorFutureMaterializer<E> extends
    AbstractIteratorFutureMaterializer<Integer> {

  private static final Logger LOGGER = Logger.getLogger(
      CountWhereIteratorFutureMaterializer.class.getName());

  public CountWhereIteratorFutureMaterializer(@NotNull final IteratorFutureMaterializer<E> wrapped,
      @NotNull final IndexedPredicate<? super E> predicate, @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    super(context, new AtomicInteger(STATUS_RUNNING));
    setState(new ImmaterialState(wrapped, predicate, cancelException));
  }

  @Override
  public boolean isMaterializedAtOnce() {
    return true;
  }

  @Override
  public int knownSize() {
    return 1;
  }

  private class ImmaterialState implements IteratorFutureMaterializer<Integer> {

    private final AtomicReference<CancellationException> cancelException;
    private final ArrayList<FutureConsumer<List<Integer>>> elementsConsumers = new ArrayList<FutureConsumer<List<Integer>>>(
        2);
    private final IndexedPredicate<? super E> predicate;
    private final IteratorFutureMaterializer<E> wrapped;

    public ImmaterialState(@NotNull final IteratorFutureMaterializer<E> wrapped,
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
      return 1;
    }

    @Override
    public void materializeCancel(@NotNull final CancellationException exception) {
      wrapped.materializeCancel(exception);
      setCancelled(exception);
      consumeError(exception);
    }

    @Override
    public void materializeElements(@NotNull final FutureConsumer<List<Integer>> consumer) {
      final ArrayList<FutureConsumer<List<Integer>>> elementsConsumers = this.elementsConsumers;
      elementsConsumers.add(consumer);
      if (elementsConsumers.size() == 1) {
        wrapped.materializeNextWhile(new CancellableIndexedFuturePredicate<E>() {
          private int count;
          private int wrappedIndex;

          @Override
          public void cancellableComplete(final int size) {
            setDone(new ElementToIteratorFutureMaterializer<Integer>(count));
            consumeElements(Collections.singletonList(count));
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            if (predicate.test(wrappedIndex++, element)) {
              ++count;
            }
            return true;
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
      materializeElements(new FutureConsumer<List<Integer>>() {
        @Override
        public void accept(final List<Integer> elements) {
          getState().materializeHasNext(consumer);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeIterator(@NotNull final FutureConsumer<Iterator<Integer>> consumer) {
      materializeElements(new FutureConsumer<List<Integer>>() {
        @Override
        public void accept(final List<Integer> elements) {
          getState().materializeIterator(consumer);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeNext(@NotNull final IndexedFutureConsumer<Integer> consumer) {
      materializeElements(new FutureConsumer<List<Integer>>() {
        @Override
        public void accept(final List<Integer> elements) {
          getState().materializeNext(consumer);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeNextWhile(@NotNull final IndexedFuturePredicate<Integer> predicate) {
      materializeElements(new FutureConsumer<List<Integer>>() {
        @Override
        public void accept(final List<Integer> elements) {
          getState().materializeNextWhile(predicate);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          predicate.error(error);
        }
      });
    }

    @Override
    public void materializeSkip(final int count, @NotNull final FutureConsumer<Integer> consumer) {
      materializeElements(new FutureConsumer<List<Integer>>() {
        @Override
        public void accept(final List<Integer> elements) {
          getState().materializeSkip(count, consumer);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public int weightElements() {
      return elementsConsumers.isEmpty() ? wrapped.weightElements() : 1;
    }

    @Override
    public int weightHasNext() {
      return weightElements();
    }

    @Override
    public int weightNext() {
      return weightElements();
    }

    @Override
    public int weightNextWhile() {
      return weightElements();
    }

    @Override
    public int weightSkip() {
      return weightElements();
    }

    private void consumeElements(@NotNull final List<Integer> elements) {
      final ArrayList<FutureConsumer<List<Integer>>> elementsConsumers = this.elementsConsumers;
      for (final FutureConsumer<List<Integer>> elementsConsumer : elementsConsumers) {
        safeConsume(elementsConsumer, elements, LOGGER);
      }
      elementsConsumers.clear();
    }

    private void consumeError(@NotNull final Exception error) {
      final ArrayList<FutureConsumer<List<Integer>>> elementsConsumers = this.elementsConsumers;
      for (final FutureConsumer<List<Integer>> elementsConsumer : elementsConsumers) {
        safeConsumeError(elementsConsumer, error, LOGGER);
      }
      elementsConsumers.clear();
    }
  }
}
