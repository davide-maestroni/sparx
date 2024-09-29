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
import static sparx.internal.future.FutureConsumers.safeConsumeComplete;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.FutureConsumer;
import sparx.internal.future.IndexedFutureConsumer;
import sparx.internal.future.IndexedFuturePredicate;
import sparx.util.DequeueList;
import sparx.util.annotation.Positive;

public abstract class TransformIteratorFutureMaterializer<E, F> extends
    AbstractIteratorFutureMaterializer<F> {

  private static final Logger LOGGER = Logger.getLogger(
      TransformIteratorFutureMaterializer.class.getName());

  private final int knownSize;

  public TransformIteratorFutureMaterializer(@NotNull final IteratorFutureMaterializer<E> wrapped,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException, final int knownSize) {
    super(context, new AtomicInteger(STATUS_RUNNING));
    this.knownSize = knownSize;
    setState(new ImmaterialState(wrapped, cancelException));
  }

  @Override
  public boolean isMaterializedAtOnce() {
    return true;
  }

  @Override
  public int knownSize() {
    return knownSize;
  }

  protected abstract int skip(int count, @NotNull Iterator<F> iterator);

  protected abstract @NotNull Iterator<F> transform(@NotNull Iterator<E> iterator);

  private interface StateConsumer<E> {

    void accept(@NotNull IteratorFutureMaterializer<E> state);
  }

  private class ImmaterialState implements IteratorFutureMaterializer<F> {

    private final AtomicReference<CancellationException> cancelException;
    private final ArrayList<StateConsumer<F>> stateConsumers = new ArrayList<StateConsumer<F>>(2);
    private final IteratorFutureMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final IteratorFutureMaterializer<E> wrapped,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      this.wrapped = wrapped;
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
      return wrapped.knownSize();
    }

    @Override
    public void materializeCancel(@NotNull final CancellationException exception) {
      setCancelled(exception);
    }

    @Override
    public void materializeElements(@NotNull final FutureConsumer<List<F>> consumer) {
      materialized(new StateConsumer<F>() {
        @Override
        public void accept(@NotNull final IteratorFutureMaterializer<F> state) {
          state.materializeElements(consumer);
        }
      });
    }

    @Override
    public void materializeHasNext(@NotNull final FutureConsumer<Boolean> consumer) {
      if (knownSize == 0) {
        safeConsume(consumer, false, LOGGER);
      } else {
        materialized(new StateConsumer<F>() {
          @Override
          public void accept(@NotNull final IteratorFutureMaterializer<F> state) {
            state.materializeHasNext(consumer);
          }
        });
      }
    }

    @Override
    public void materializeIterator(@NotNull final FutureConsumer<Iterator<F>> consumer) {
      materialized(new StateConsumer<F>() {
        @Override
        public void accept(@NotNull final IteratorFutureMaterializer<F> state) {
          state.materializeIterator(consumer);
        }
      });
    }

    @Override
    public void materializeNext(@NotNull final IndexedFutureConsumer<F> consumer) {
      if (knownSize == 0) {
        safeConsumeComplete(consumer, 0, LOGGER);
      } else {
        materialized(new StateConsumer<F>() {
          @Override
          public void accept(@NotNull final IteratorFutureMaterializer<F> state) {
            state.materializeNext(consumer);
          }
        });
      }
    }

    @Override
    public void materializeNextWhile(@NotNull final IndexedFuturePredicate<F> predicate) {
      if (knownSize == 0) {
        safeConsumeComplete(predicate, 0, LOGGER);
      } else {
        materialized(new StateConsumer<F>() {
          @Override
          public void accept(@NotNull final IteratorFutureMaterializer<F> state) {
            state.materializeNextWhile(predicate);
          }
        });
      }
    }

    @Override
    public void materializeSkip(@Positive final int count,
        @NotNull final FutureConsumer<Integer> consumer) {
      if (knownSize == 0) {
        safeConsume(consumer, 0, LOGGER);
      } else {
        materialized(new StateConsumer<F>() {
          @Override
          public void accept(@NotNull final IteratorFutureMaterializer<F> state) {
            state.materializeSkip(count, consumer);
          }
        });
      }
    }

    @Override
    public int weightElements() {
      return stateConsumers.isEmpty() ? wrapped.weightElements() : 1;
    }

    @Override
    public int weightHasNext() {
      return knownSize == 0 ? 1 : weightElements();
    }

    @Override
    public int weightNext() {
      return knownSize == 0 ? 1 : weightElements();
    }

    @Override
    public int weightNextWhile() {
      return knownSize == 0 ? 1 : weightElements();
    }

    @Override
    public int weightSkip() {
      return knownSize == 0 ? 1 : weightElements();
    }

    private void consumeState(@NotNull final IteratorFutureMaterializer<F> state) {
      final ArrayList<StateConsumer<F>> stateConsumers = this.stateConsumers;
      for (final StateConsumer<F> stateConsumer : stateConsumers) {
        stateConsumer.accept(state);
      }
      stateConsumers.clear();
    }

    private void materialized(@NotNull final StateConsumer<F> consumer) {
      final ArrayList<StateConsumer<F>> stateConsumers = this.stateConsumers;
      stateConsumers.add(consumer);
      if (stateConsumers.size() == 1) {
        wrapped.materializeIterator(new FutureConsumer<Iterator<E>>() {
          @Override
          public void accept(final Iterator<E> iterator) {
            consumeState(setState(new TransformState(transform(iterator), cancelException)));
          }

          @Override
          public void error(@NotNull final Exception error) {
            final CancellationException exception = cancelException.get();
            if (exception != null) {
              consumeState(setCancelled(exception));
            } else {
              consumeState(setFailed(error));
            }
          }
        });
      }
    }
  }

  private class TransformState implements IteratorFutureMaterializer<F> {

    private final AtomicReference<CancellationException> cancelException;
    private final Iterator<F> iterator;

    private int index;

    private TransformState(@NotNull final Iterator<F> iterator,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      this.iterator = iterator;
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
      return -1;
    }

    @Override
    public void materializeCancel(@NotNull final CancellationException exception) {
      setCancelled(exception);
    }

    @Override
    public void materializeElements(@NotNull final FutureConsumer<List<F>> consumer) {
      try {
        final Iterator<F> iterator = this.iterator;
        final DequeueList<F> elements = new DequeueList<F>();
        while (iterator.hasNext()) {
          elements.add(iterator.next());
        }
        if (elements.isEmpty()) {
          setDone(EmptyIteratorFutureMaterializer.<F>instance());
        } else {
          setDone(new DequeueToIteratorFutureMaterializer<F>(elements, context));
        }
        safeConsume(consumer, elements, LOGGER);
      } catch (final Exception error) {
        consumeError(consumer, error);
      }
    }

    @Override
    public void materializeHasNext(@NotNull final FutureConsumer<Boolean> consumer) {
      try {
        safeConsume(consumer, iterator.hasNext(), LOGGER);
      } catch (final Exception error) {
        consumeError(consumer, error);
      }
    }

    @Override
    public void materializeIterator(@NotNull final FutureConsumer<Iterator<F>> consumer) {
      safeConsume(consumer, new Iterator<F>() {
        @Override
        public boolean hasNext() {
          return iterator.hasNext();
        }

        @Override
        public F next() {
          if (!iterator.hasNext()) {
            throw new NoSuchElementException();
          }
          ++index;
          return iterator.next();
        }

        @Override
        public void remove() {
          throw new UnsupportedOperationException("remove");
        }
      }, LOGGER);
    }

    @Override
    public void materializeNext(@NotNull final IndexedFutureConsumer<F> consumer) {
      try {
        safeConsume(consumer, -1, index++, iterator.next(), LOGGER);
      } catch (final Exception error) {
        consumeError(consumer, error);
      }
    }

    @Override
    public void materializeNextWhile(@NotNull IndexedFuturePredicate<F> predicate) {
      try {
        final Iterator<F> iterator = this.iterator;
        while (iterator.hasNext()) {
          final F element = iterator.next();
          if (!predicate.test(-1, index++, element)) {
            return;
          }
        }
        safeConsumeComplete(predicate, 0, LOGGER);
      } catch (final Exception error) {
        consumeError(predicate, error);
      }
    }

    @Override
    public void materializeSkip(@Positive final int count,
        @NotNull final FutureConsumer<Integer> consumer) {
      try {
        safeConsume(consumer, skip(count, iterator), LOGGER);
      } catch (final Exception error) {
        consumeError(consumer, error);
      }
    }

    @Override
    public int weightElements() {
      return 1;
    }

    @Override
    public int weightHasNext() {
      return 1;
    }

    @Override
    public int weightNext() {
      return 1;
    }

    @Override
    public int weightNextWhile() {
      return 1;
    }

    @Override
    public int weightSkip() {
      return 1;
    }

    private void consumeError(@NotNull final FutureConsumer<?> consumer,
        @NotNull final Exception error) {
      if (error instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      final CancellationException exception = cancelException.get();
      if (exception != null) {
        setCancelled(exception);
        try {
          consumer.error(exception);
        } catch (final Exception e) {
          if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
          }
          LOGGER.log(Level.SEVERE, "Ignored exception", e);
        }
      } else {
        setFailed(error);
        try {
          consumer.error(error);
        } catch (final Exception e) {
          if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
          }
          LOGGER.log(Level.SEVERE, "Ignored exception", e);
        }
      }
    }

    private void consumeError(@NotNull final IndexedFutureConsumer<?> consumer,
        @NotNull final Exception error) {
      if (error instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      final CancellationException exception = cancelException.get();
      if (exception != null) {
        setCancelled(exception);
        try {
          consumer.error(exception);
        } catch (final Exception e) {
          if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
          }
          LOGGER.log(Level.SEVERE, "Ignored exception", e);
        }
      } else {
        setFailed(error);
        try {
          consumer.error(error);
        } catch (final Exception e) {
          if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
          }
          LOGGER.log(Level.SEVERE, "Ignored exception", e);
        }
      }
    }

    private void consumeError(@NotNull final IndexedFuturePredicate<?> predicate,
        @NotNull final Exception error) {
      if (error instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      final CancellationException exception = cancelException.get();
      if (exception != null) {
        setCancelled(exception);
        try {
          predicate.error(exception);
        } catch (final Exception e) {
          if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
          }
          LOGGER.log(Level.SEVERE, "Ignored exception", e);
        }
      } else {
        setFailed(error);
        try {
          predicate.error(error);
        } catch (final Exception e) {
          if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
          }
          LOGGER.log(Level.SEVERE, "Ignored exception", e);
        }
      }
    }
  }
}
