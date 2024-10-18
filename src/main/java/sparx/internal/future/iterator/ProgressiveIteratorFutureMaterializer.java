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
import sparx.util.DequeueList;
import sparx.util.annotation.Positive;

abstract class ProgressiveIteratorFutureMaterializer<E, F> extends
    AbstractIteratorFutureMaterializer<F> {

  ProgressiveIteratorFutureMaterializer(@NotNull final ExecutionContext context,
      @NotNull final AtomicInteger status) {
    super(context, status);
  }

  @Override
  public int knownSize() {
    return -1;
  }

  abstract class ImmaterialState implements IteratorFutureMaterializer<F> {

    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final ArrayList<FutureConsumer<List<F>>> elementsConsumers = new ArrayList<FutureConsumer<List<F>>>(
        2);
    private final Logger logger;
    private final DequeueList<FutureConsumer<DequeueList<F>>> nextElementConsumers = new DequeueList<FutureConsumer<DequeueList<F>>>(
        2);
    private final DequeueList<F> nextElements = new DequeueList<F>(1);
    private final IteratorFutureMaterializer<E> wrapped;

    private int index;

    public ImmaterialState(@NotNull final IteratorFutureMaterializer<E> wrapped,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Logger logger) {
      this.wrapped = wrapped;
      this.context = context;
      this.cancelException = cancelException;
      this.logger = logger;
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
    public void materializeElements(@NotNull final FutureConsumer<List<F>> consumer) {
      final ArrayList<FutureConsumer<List<F>>> elementsConsumers = this.elementsConsumers;
      elementsConsumers.add(consumer);
      if (elementsConsumers.size() == 1) {
        final DequeueList<F> elements = new DequeueList<F>();
        materializeNext(new FutureConsumer<DequeueList<F>>() {
          @Override
          public void accept(final DequeueList<F> nextElements) {
            if (nextElements.isEmpty()) {
              if (elements.isEmpty()) {
                setDone(EmptyIteratorFutureMaterializer.<F>instance());
                consumeElements(Collections.<F>emptyList());
              } else {
                setDone(new DequeueToIteratorFutureMaterializer<F>(elements, context, index));
                consumeElements(elements);
              }
            } else {
              elements.addAll(nextElements);
              nextElements.clear();
              materializeNext(this);
            }
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
      materializeNext(new FutureConsumer<DequeueList<F>>() {
        @Override
        public void accept(final DequeueList<F> nextElements) throws Exception {
          consumer.accept(!nextElements.isEmpty());
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeIterator(@NotNull final FutureConsumer<Iterator<F>> consumer) {
      materializeElements(new FutureConsumer<List<F>>() {
        @Override
        public void accept(final List<F> elements) {
          getState().materializeIterator(consumer);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeNext(@NotNull final IndexedFutureConsumer<F> consumer) {
      materializeNext(new FutureConsumer<DequeueList<F>>() {
        @Override
        public void accept(final DequeueList<F> nextElements) throws Exception {
          if (nextElements.isEmpty()) {
            setDone(EmptyIteratorFutureMaterializer.<F>instance());
            consumer.complete(0);
          } else {
            consumer.accept(-1, ImmaterialState.this.index++, nextElements.removeFirst());
          }
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeNextWhile(@NotNull final IndexedFuturePredicate<F> predicate) {
      materializeNext(new FutureConsumer<DequeueList<F>>() {
        @Override
        public void accept(final DequeueList<F> nextElements) throws Exception {
          if (nextElements.isEmpty()) {
            setDone(EmptyIteratorFutureMaterializer.<F>instance());
            predicate.complete(0);
          } else if (predicate.test(-1, ImmaterialState.this.index++, nextElements.removeFirst())) {
            materializeNext(this);
          }
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
      materializeNext(new FutureConsumer<DequeueList<F>>() {
        private int skipped;

        @Override
        public void accept(final DequeueList<F> nextElements) throws Exception {
          if (nextElements.isEmpty()) {
            consumer.accept(skipped);
          } else {
            while (skipped < count && !nextElements.isEmpty()) {
              nextElements.removeFirst();
              ++index;
              ++skipped;
            }
            if (skipped < count) {
              materializeNext(this);
            } else {
              consumer.accept(skipped);
            }
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
      return elementsConsumers.isEmpty() ? weightNextElements() : 1;
    }

    @Override
    public int weightHasNext() {
      return weightNextElements();
    }

    @Override
    public int weightNext() {
      return weightNextElements();
    }

    @Override
    public int weightNextWhile() {
      return weightNextElements();
    }

    @Override
    public int weightSkip() {
      return weightNextElements();
    }

    abstract boolean addElement(E element) throws Exception;

    abstract F mapElement(E element) throws Exception;

    void materializeUntilConsumed() {
      final DequeueList<F> nextElements = this.nextElements;
      final DequeueList<FutureConsumer<DequeueList<F>>> elementConsumers = this.nextElementConsumers;
      if (!nextElements.isEmpty()) {
        while (!elementConsumers.isEmpty()) {
          if (nextElements.isEmpty()) {
            materializeUntilConsumed();
            return;
          }
          safeConsume(elementConsumers.getFirst(), nextElements, logger);
          elementConsumers.removeFirst();
        }
      } else {
        wrapped.materializeNextWhile(new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) {
            for (final FutureConsumer<DequeueList<F>> consumer : elementConsumers) {
              safeConsume(consumer, nextElements, logger);
            }
            elementConsumers.clear();
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            if (addElement(element)) {
              nextElements.add(mapElement(element));
              while (!elementConsumers.isEmpty()) {
                if (nextElements.isEmpty()) {
                  return true;
                }
                safeConsume(elementConsumers.getFirst(), nextElements, logger);
                elementConsumers.removeFirst();
              }
              return false;
            }
            return true;
          }

          @Override
          public void error(@NotNull final Exception error) {
            setNextError(error);
          }
        });
      }
    }

    void setNextError(@NotNull final Exception error) {
      final DequeueList<FutureConsumer<DequeueList<F>>> elementConsumers = this.nextElementConsumers;
      while (!elementConsumers.isEmpty()) {
        safeConsumeError(elementConsumers.getFirst(), error, logger);
        elementConsumers.removeFirst();
      }
    }

    int weightUntilConsumed() {
      return wrapped.weightNextWhile();
    }

    private void consumeElements(@NotNull final List<F> elements) {
      final ArrayList<FutureConsumer<List<F>>> elementsConsumers = this.elementsConsumers;
      for (final FutureConsumer<List<F>> elementsConsumer : elementsConsumers) {
        safeConsume(elementsConsumer, elements, logger);
      }
      elementsConsumers.clear();
    }

    private void consumeError(@NotNull final Exception error) {
      final ArrayList<FutureConsumer<List<F>>> elementsConsumers = this.elementsConsumers;
      for (final FutureConsumer<List<F>> elementsConsumer : elementsConsumers) {
        safeConsumeError(elementsConsumer, error, logger);
      }
      elementsConsumers.clear();
    }

    private void materializeNext(@NotNull final FutureConsumer<DequeueList<F>> consumer) {
      final DequeueList<FutureConsumer<DequeueList<F>>> elementConsumers = this.nextElementConsumers;
      elementConsumers.add(consumer);
      if (elementConsumers.size() == 1) {
        materializeUntilConsumed();
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

    private int weightNextElements() {
      return nextElementConsumers.isEmpty() ? weightUntilConsumed() : 1;
    }
  }
}
