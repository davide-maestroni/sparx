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
import static sparx.internal.future.AsyncConsumers.safeConsumeComplete;
import static sparx.internal.future.AsyncConsumers.safeConsumeError;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.internal.future.AsyncConsumer;
import sparx.internal.future.IndexedAsyncConsumer;

public abstract class TransformListAsyncMaterializer<E, F> extends
    AbstractListAsyncMaterializer<F> {

  private static final Logger LOGGER = Logger.getLogger(
      TransformListAsyncMaterializer.class.getName());

  private final AtomicReference<CancellationException> cancelException;
  private final int knownSize;

  public TransformListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      @NotNull final AtomicReference<CancellationException> cancelException, final int knownSize) {
    super(new AtomicInteger(STATUS_RUNNING));
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
  public void materializeDone(@NotNull final AsyncConsumer<List<F>> consumer) {
    super.materializeDone(new AsyncConsumer<List<F>>() {
      @Override
      public void accept(final List<F> elements) throws Exception {
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

  @Override
  protected void finalizeElements(final List<F> elements) {
    materialize(elements);
  }

  protected abstract int knownSize(@NotNull List<F> elements);

  protected abstract void materialize(@NotNull List<F> elements);

  protected abstract @NotNull List<F> transform(@NotNull List<E> elements);

  private interface StateConsumer<E> {

    void accept(@NotNull ListAsyncMaterializer<E> state);
  }

  private class ImmaterialState implements ListAsyncMaterializer<F> {

    private final AtomicReference<CancellationException> cancelException;
    private final ArrayList<StateConsumer<F>> stateConsumers = new ArrayList<StateConsumer<F>>(2);
    private final ListAsyncMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final ListAsyncMaterializer<E> wrapped,
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
    public int knownSize() {
      return wrapped.knownSize();
    }

    @Override
    public void materializeCancel(@NotNull final CancellationException exception) {
      setCancelled(exception);
    }

    @Override
    public void materializeContains(final Object element,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      materialized(new StateConsumer<F>() {
        @Override
        public void accept(@NotNull final ListAsyncMaterializer<F> state) {
          state.materializeContains(element, consumer);
        }
      });
    }

    @Override
    public void materializeDone(@NotNull final AsyncConsumer<List<F>> consumer) {
      safeConsumeError(consumer, new UnsupportedOperationException(), LOGGER);
    }

    @Override
    public void materializeEach(@NotNull final IndexedAsyncConsumer<F> consumer) {
      materialized(new StateConsumer<F>() {
        @Override
        public void accept(@NotNull final ListAsyncMaterializer<F> state) {
          state.materializeEach(consumer);
        }
      });
    }

    @Override
    public void materializeElement(final int index,
        @NotNull final IndexedAsyncConsumer<F> consumer) {
      if (index < 0) {
        safeConsumeError(consumer, new IndexOutOfBoundsException(Integer.toString(index)), LOGGER);
      } else {
        materialized(new StateConsumer<F>() {
          @Override
          public void accept(@NotNull final ListAsyncMaterializer<F> state) {
            state.materializeElement(index, consumer);
          }
        });
      }
    }

    @Override
    public void materializeElements(@NotNull final AsyncConsumer<List<F>> consumer) {
      materialized(new StateConsumer<F>() {
        @Override
        public void accept(@NotNull final ListAsyncMaterializer<F> state) {
          state.materializeElements(consumer);
        }
      });
    }

    @Override
    public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
      materialized(new StateConsumer<F>() {
        @Override
        public void accept(@NotNull final ListAsyncMaterializer<F> state) {
          state.materializeEmpty(consumer);
        }
      });
    }

    @Override
    public void materializeHasElement(final int index,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      materialized(new StateConsumer<F>() {
        @Override
        public void accept(@NotNull final ListAsyncMaterializer<F> state) {
          state.materializeHasElement(index, consumer);
        }
      });
    }

    @Override
    public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
      final int knownSize = TransformListAsyncMaterializer.this.knownSize;
      if (knownSize >= 0) {
        safeConsume(consumer, knownSize, LOGGER);
      } else {
        materialized(new StateConsumer<F>() {
          @Override
          public void accept(@NotNull final ListAsyncMaterializer<F> state) {
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
    public int weightEach() {
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
    public int weightSize() {
      return weightElements();
    }

    private void consumeState(@NotNull final ListAsyncMaterializer<F> state) {
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
        wrapped.materializeElements(new AsyncConsumer<List<E>>() {
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

  private class TransformState implements ListAsyncMaterializer<F> {

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
    public int knownSize() {
      return TransformListAsyncMaterializer.this.knownSize(elements);
    }

    @Override
    public void materializeCancel(@NotNull final CancellationException exception) {
      setCancelled(exception);
    }

    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    public void materializeContains(final Object element,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      try {
        safeConsume(consumer, elements.contains(element), LOGGER);
      } catch (final Exception error) {
        consumeError(consumer, error);
      }
    }

    @Override
    public void materializeDone(@NotNull final AsyncConsumer<List<F>> consumer) {
      safeConsumeError(consumer, new UnsupportedOperationException(), LOGGER);
    }

    @Override
    public void materializeEach(@NotNull final IndexedAsyncConsumer<F> consumer) {
      int i = 0;
      try {
        for (final F element : elements) {
          if (!safeConsume(consumer, -1, i++, element, LOGGER)) {
            break;
          }
        }
        safeConsumeComplete(consumer, i, LOGGER);
      } catch (final Exception error) {
        consumeError(consumer, error);
      }
    }

    @Override
    public void materializeElement(final int index,
        @NotNull final IndexedAsyncConsumer<F> consumer) {
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
    public void materializeElements(@NotNull final AsyncConsumer<List<F>> consumer) {
      safeConsume(consumer, elements, LOGGER);
    }

    @Override
    public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
      try {
        safeConsume(consumer, elements.isEmpty(), LOGGER);
      } catch (final Exception error) {
        consumeError(consumer, error);
      }
    }

    @Override
    public void materializeHasElement(final int index,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      try {
        safeConsume(consumer, index >= 0 && index < elements.size(), LOGGER);
      } catch (final Exception error) {
        consumeError(consumer, error);
      }
    }

    @Override
    public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
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
    public int weightEach() {
      return elements.size();
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
    public int weightSize() {
      return 1;
    }

    private void consumeError(@NotNull final AsyncConsumer<?> consumer,
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

    private void consumeError(@NotNull final IndexedAsyncConsumer<?> consumer,
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
  }
}
