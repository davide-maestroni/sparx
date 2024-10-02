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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.FutureConsumer;
import sparx.internal.future.IndexedFutureConsumer;
import sparx.internal.future.IndexedFuturePredicate;
import sparx.util.annotation.Positive;

abstract class ImmediateIteratorFutureMaterializer<E, F> extends
    AbstractIteratorFutureMaterializer<F> {

  public ImmediateIteratorFutureMaterializer(@NotNull final ExecutionContext context,
      @NotNull final AtomicInteger status) {
    super(context, status);
  }

  @Override
  public int knownSize() {
    return -1;
  }

  abstract class ImmaterialState implements IteratorFutureMaterializer<F> {

    private final ArrayList<FutureConsumer<List<F>>> elementsConsumers = new ArrayList<FutureConsumer<List<F>>>(
        2);
    private final Logger logger;
    private final IteratorFutureMaterializer<E> wrapped;

    public ImmaterialState(@NotNull final IteratorFutureMaterializer<E> wrapped,
        @NotNull final Logger logger) {
      this.wrapped = wrapped;
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
        materialize();
      }
    }

    @Override
    public void materializeHasNext(@NotNull final FutureConsumer<Boolean> consumer) {
      materializeElements(new FutureConsumer<List<F>>() {
        @Override
        public void accept(final List<F> elements) {
          getState().materializeHasNext(consumer);
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
      materializeElements(new FutureConsumer<List<F>>() {
        @Override
        public void accept(final List<F> elements) {
          getState().materializeNext(consumer);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeNextWhile(@NotNull final IndexedFuturePredicate<F> predicate) {
      materializeElements(new FutureConsumer<List<F>>() {
        @Override
        public void accept(final List<F> elements) {
          getState().materializeNextWhile(predicate);
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
      materializeElements(new FutureConsumer<List<F>>() {
        @Override
        public void accept(final List<F> elements) {
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
      return elementsConsumers.isEmpty() ? wrapped.weightSkip() : 1;
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

    void consumeElements(@NotNull final List<F> elements) {
      final ArrayList<FutureConsumer<List<F>>> elementsConsumers = this.elementsConsumers;
      for (final FutureConsumer<List<F>> elementsConsumer : elementsConsumers) {
        safeConsume(elementsConsumer, elements, logger);
      }
      elementsConsumers.clear();
    }

    void consumeError(@NotNull final Exception error) {
      final ArrayList<FutureConsumer<List<F>>> elementsConsumers = this.elementsConsumers;
      for (final FutureConsumer<List<F>> elementsConsumer : elementsConsumers) {
        safeConsumeError(elementsConsumer, error, logger);
      }
      elementsConsumers.clear();
    }

    boolean isMaterializing() {
      return !elementsConsumers.isEmpty();
    }

    abstract void materialize();
  }
}
