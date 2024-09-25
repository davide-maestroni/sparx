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

import static sparx.internal.future.FutureConsumers.safeConsume;
import static sparx.internal.future.FutureConsumers.safeConsumeComplete;
import static sparx.internal.future.FutureConsumers.safeConsumeError;

import java.util.ArrayList;
import java.util.List;
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

public abstract class TransformListFutureMaterializer<E, F> extends
    AbstractListFutureMaterializer<F> {

  private static final Logger LOGGER = Logger.getLogger(
      TransformListFutureMaterializer.class.getName());

  private final AtomicReference<CancellationException> cancelException;
  private final int knownSize;

  public TransformListFutureMaterializer(@NotNull final ListFutureMaterializer<E> wrapped,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException, final int knownSize) {
    super(context, new AtomicInteger(STATUS_RUNNING));
    this.cancelException = cancelException;
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

  @Override
  public void materializeElements(@NotNull final FutureConsumer<List<F>> consumer) {
    super.materializeElements(new FutureConsumer<List<F>>() {
      @Override
      public void accept(final List<F> elements) throws Exception {
        materialize(elements);
        setDone(new ListToListFutureMaterializer<F>(elements, context));
        consumer.accept(elements);
      }

      @Override
      public void error(@NotNull final Exception error) throws Exception {
        final CancellationException exception = cancelException.get();
        if (exception != null) {
          setCancelled(exception);
          consumer.error(exception);
        } else {
          setFailed(error);
          consumer.error(error);
        }
      }
    });
  }

  protected abstract void materialize(@NotNull List<F> list);

  protected abstract @NotNull List<F> transform(@NotNull List<E> list);

  private interface StateConsumer<E> {

    void accept(@NotNull ListFutureMaterializer<E> state);
  }

  private class ImmaterialState implements ListFutureMaterializer<F> {

    private final AtomicReference<CancellationException> cancelException;
    private final ArrayList<StateConsumer<F>> stateConsumers = new ArrayList<StateConsumer<F>>(2);
    private final ListFutureMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final ListFutureMaterializer<E> wrapped,
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
    public void materializeContains(final Object element,
        @NotNull final FutureConsumer<Boolean> consumer) {
      materialized(new StateConsumer<F>() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<F> state) {
          state.materializeContains(element, consumer);
        }
      });
    }

    @Override
    public void materializeElement(final int index,
        @NotNull final IndexedFutureConsumer<F> consumer) {
      if (index < 0) {
        safeConsumeError(consumer, new IndexOutOfBoundsException(Integer.toString(index)), LOGGER);
      } else {
        materialized(new StateConsumer<F>() {
          @Override
          public void accept(@NotNull final ListFutureMaterializer<F> state) {
            state.materializeElement(index, consumer);
          }
        });
      }
    }

    @Override
    public void materializeElements(@NotNull final FutureConsumer<List<F>> consumer) {
      materialized(new StateConsumer<F>() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<F> state) {
          state.materializeElements(consumer);
        }
      });
    }

    @Override
    public void materializeEmpty(@NotNull final FutureConsumer<Boolean> consumer) {
      materialized(new StateConsumer<F>() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<F> state) {
          state.materializeEmpty(consumer);
        }
      });
    }

    @Override
    public void materializeHasElement(final int index,
        @NotNull final FutureConsumer<Boolean> consumer) {
      materialized(new StateConsumer<F>() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<F> state) {
          state.materializeHasElement(index, consumer);
        }
      });
    }

    @Override
    public void materializeNextWhile(final int index,
        @NotNull final IndexedFuturePredicate<F> predicate) {
      materialized(new StateConsumer<F>() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<F> state) {
          state.materializeNextWhile(index, predicate);
        }
      });
    }

    @Override
    public void materializePrevWhile(final int index,
        @NotNull final IndexedFuturePredicate<F> predicate) {
      materialized(new StateConsumer<F>() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<F> state) {
          state.materializePrevWhile(index, predicate);
        }
      });
    }

    @Override
    public void materializeSize(@NotNull final FutureConsumer<Integer> consumer) {
      final int knownSize = TransformListFutureMaterializer.this.knownSize;
      if (knownSize >= 0) {
        safeConsume(consumer, knownSize, LOGGER);
      } else {
        materialized(new StateConsumer<F>() {
          @Override
          public void accept(@NotNull final ListFutureMaterializer<F> state) {
            state.materializeSize(consumer);
          }
        });
      }
    }

    @Override
    public int weightContains() {
      return weightElements();
    }

    @Override
    public int weightElement() {
      return weightElements();
    }

    @Override
    public int weightElements() {
      return stateConsumers.isEmpty() ? wrapped.weightElements() : 1;
    }

    @Override
    public int weightEmpty() {
      return weightElements();
    }

    @Override
    public int weightHasElement() {
      return weightElements();
    }

    @Override
    public int weightNextWhile() {
      return weightElements();
    }

    @Override
    public int weightPrevWhile() {
      return weightElements();
    }

    @Override
    public int weightSize() {
      return weightElements();
    }

    private void consumeState(@NotNull final ListFutureMaterializer<F> state) {
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
        wrapped.materializeElements(new FutureConsumer<List<E>>() {
          @Override
          public void accept(final List<E> elements) {
            consumeState(setState(new TransformState(transform(elements), cancelException)));
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

  private class TransformState implements ListFutureMaterializer<F> {

    private final AtomicReference<CancellationException> cancelException;
    private final List<F> elements;

    private TransformState(@NotNull final List<F> elements,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      this.elements = elements;
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
    @SuppressWarnings("SuspiciousMethodCalls")
    public void materializeContains(final Object element,
        @NotNull final FutureConsumer<Boolean> consumer) {
      try {
        safeConsume(consumer, elements.contains(element), LOGGER);
      } catch (final Exception error) {
        consumeError(consumer, error);
      }
    }

    @Override
    public void materializeElement(final int index,
        @NotNull final IndexedFutureConsumer<F> consumer) {
      try {
        final List<F> elements = this.elements;
        if (index < 0) {
          safeConsumeError(consumer, new IndexOutOfBoundsException(Integer.toString(index)),
              LOGGER);
        } else {
          final int size = elements.size();
          if (index >= size) {
            safeConsumeComplete(consumer, size, LOGGER);
          } else {
            safeConsume(consumer, size, index, elements.get(index), LOGGER);
          }
        }
      } catch (final Exception error) {
        consumeError(consumer, error);
      }
    }

    @Override
    public void materializeElements(@NotNull final FutureConsumer<List<F>> consumer) {
      safeConsume(consumer, elements, LOGGER);
    }

    @Override
    public void materializeEmpty(@NotNull final FutureConsumer<Boolean> consumer) {
      try {
        safeConsume(consumer, elements.isEmpty(), LOGGER);
      } catch (final Exception error) {
        consumeError(consumer, error);
      }
    }

    @Override
    public void materializeHasElement(final int index,
        @NotNull final FutureConsumer<Boolean> consumer) {
      try {
        safeConsume(consumer, index >= 0 && index < elements.size(), LOGGER);
      } catch (final Exception error) {
        consumeError(consumer, error);
      }
    }

    @Override
    public void materializeNextWhile(final int index,
        @NotNull IndexedFuturePredicate<F> predicate) {
      try {
        final List<F> elements = this.elements;
        final int size = elements.size();
        for (int i = index; i < size; ++i) {
          final F element = elements.get(i);
          if (!predicate.test(size, i, element)) {
            return;
          }
        }
        safeConsumeComplete(predicate, size, LOGGER);
      } catch (final Exception error) {
        consumeError(predicate, error);
      }
    }

    @Override
    public void materializePrevWhile(final int index,
        @NotNull IndexedFuturePredicate<F> predicate) {
      try {
        final List<F> elements = this.elements;
        final int size = elements.size();
        for (int i = Math.min(index, size - 1); i >= 0; --i) {
          final F element = elements.get(i);
          if (!predicate.test(size, i, element)) {
            return;
          }
        }
        safeConsumeComplete(predicate, size, LOGGER);
      } catch (final Exception error) {
        consumeError(predicate, error);
      }
    }

    @Override
    public void materializeSize(@NotNull final FutureConsumer<Integer> consumer) {
      try {
        safeConsume(consumer, elements.size(), LOGGER);
      } catch (final Exception error) {
        consumeError(consumer, error);
      }
    }

    @Override
    public int weightContains() {
      return 1;
    }

    @Override
    public int weightElement() {
      return 1;
    }

    @Override
    public int weightElements() {
      return elements.size();
    }

    @Override
    public int weightEmpty() {
      return 1;
    }

    @Override
    public int weightHasElement() {
      return 1;
    }

    @Override
    public int weightNextWhile() {
      return elements.size();
    }

    @Override
    public int weightPrevWhile() {
      return elements.size();
    }

    @Override
    public int weightSize() {
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
