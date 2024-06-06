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

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.internal.future.AsyncConsumer;
import sparx.internal.future.IndexedAsyncConsumer;

public abstract class TransformListAsyncMaterializer<E, F> extends
    AbstractListAsyncMaterializer<F> {

  private static final Logger LOGGER = Logger.getLogger(
      TransformListAsyncMaterializer.class.getName());

  private final AtomicBoolean isCancelled;

  public TransformListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      @NotNull final AtomicBoolean isCancelled) {
    super(new AtomicInteger(STATUS_RUNNING));
    this.isCancelled = isCancelled;
    setState(new ImmaterialState(wrapped, isCancelled), STATUS_RUNNING);
  }

  @Override
  public boolean isMaterializedOnce() {
    return true;
  }

  @Override
  public int knownSize() {
    return 1;
  }

  @Override
  public void materializeDone(@NotNull final AsyncConsumer<List<F>> consumer) {
    getState().materializeElements(new AsyncConsumer<List<F>>() {
      @Override
      public void accept(final List<F> elements) throws Exception {
        materialize(elements);
        setState(getState(), STATUS_DONE);
        consumer.accept(elements);
      }

      @Override
      public void error(@NotNull final Exception error) throws Exception {
        if (isCancelled.get()) {
          setState(new CancelledListAsyncMaterializer<F>(), STATUS_CANCELLED);
          consumer.error(new CancellationException());
        } else {
          setState(new FailedListAsyncMaterializer<F>(error), STATUS_DONE);
          consumer.error(error);
        }
      }
    });
  }

  protected abstract int knownSize(@NotNull List<F> elements);

  protected abstract void materialize(@NotNull List<F> elements);

  protected abstract @NotNull List<F> transform(@NotNull List<E> elements);

  private interface StateConsumer<E> {

    void accept(@NotNull ListAsyncMaterializer<E> state);
  }

  private class ImmaterialState implements ListAsyncMaterializer<F> {

    private final AtomicBoolean isCancelled;
    private final ListAsyncMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final ListAsyncMaterializer<E> wrapped,
        @NotNull final AtomicBoolean isCancelled) {
      this.wrapped = wrapped;
      this.isCancelled = isCancelled;
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
    public boolean isMaterializedOnce() {
      return true;
    }

    @Override
    public int knownSize() {
      return wrapped.knownSize();
    }

    @Override
    public void materializeCancel(final boolean mayInterruptIfRunning) {
      setState(new CancelledListAsyncMaterializer<F>(), STATUS_CANCELLED);
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
      materialized(new StateConsumer<F>() {
        @Override
        public void accept(@NotNull final ListAsyncMaterializer<F> state) {
          state.materializeElement(index, consumer);
        }
      });
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
      safeConsume(consumer, false, LOGGER);
    }

    @Override
    public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
      safeConsume(consumer, 1, LOGGER);
    }

    @Override
    public int weightElement() {
      return 1;
    }

    @Override
    public int weightElements() {
      return 1;
    }

    @Override
    public int weightSize() {
      return 1;
    }

    private void materialized(@NotNull final StateConsumer<F> consumer) {
      wrapped.materializeElements(new AsyncConsumer<List<E>>() {
        @Override
        public void accept(final List<E> elements) {
          final LazyState state = new LazyState(transform(elements), isCancelled);
          setState(state, STATUS_RUNNING);
          consumer.accept(state);
        }

        @Override
        public void error(@NotNull final Exception error) {
          if (isCancelled.get()) {
            final CancelledListAsyncMaterializer<F> state = new CancelledListAsyncMaterializer<F>();
            setState(state, STATUS_CANCELLED);
            consumer.accept(state);
          } else {
            final FailedListAsyncMaterializer<F> state = new FailedListAsyncMaterializer<F>(error);
            setState(state, STATUS_DONE);
            consumer.accept(state);
          }
        }
      });

    }
  }

  private class LazyState implements ListAsyncMaterializer<F> {

    private final List<F> elements;
    private final AtomicBoolean isCancelled;

    private LazyState(@NotNull final List<F> elements, @NotNull final AtomicBoolean isCancelled) {
      this.elements = elements;
      this.isCancelled = isCancelled;
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
    public boolean isMaterializedOnce() {
      return true;
    }

    @Override
    public int knownSize() {
      return TransformListAsyncMaterializer.this.knownSize(elements);
    }

    @Override
    public void materializeCancel(final boolean mayInterruptIfRunning) {
      setState(new CancelledListAsyncMaterializer<F>(), STATUS_CANCELLED);
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
        consumeError(consumer, i, error);
      }
    }

    @Override
    public void materializeElement(final int index,
        @NotNull final IndexedAsyncConsumer<F> consumer) {
      int i = -1;
      try {
        final List<F> elements = this.elements;
        if (index < 0) {
          safeConsumeError(consumer, index, new IndexOutOfBoundsException(Integer.toString(index)),
              LOGGER);
        } else {
          final int size = elements.size();
          if (index >= size) {
            safeConsumeComplete(consumer, size, LOGGER);
          } else {
            i = index;
            safeConsume(consumer, size, index, elements.get(index), LOGGER);
          }
        }
      } catch (final Exception error) {
        consumeError(consumer, i, error);
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
    public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
      try {
        safeConsume(consumer, elements.size(), LOGGER);
      } catch (final Exception error) {
        consumeError(consumer, error);
      }
    }

    @Override
    public int weightElement() {
      return 1;
    }

    @Override
    public int weightElements() {
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
      if (isCancelled.get()) {
        setState(new CancelledListAsyncMaterializer<F>(), STATUS_CANCELLED);
        try {
          consumer.error(new CancellationException());
        } catch (final Exception e) {
          if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
          }
          LOGGER.log(Level.SEVERE, "Ignored exception", e);
        }
      } else {
        setState(new FailedListAsyncMaterializer<F>(error), STATUS_DONE);
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

    private void consumeError(@NotNull final IndexedAsyncConsumer<?> consumer, final int index,
        @NotNull final Exception error) {
      if (error instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      if (isCancelled.get()) {
        setState(new CancelledListAsyncMaterializer<F>(), STATUS_CANCELLED);
        try {
          consumer.error(index, new CancellationException());
        } catch (final Exception e) {
          if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
          }
          LOGGER.log(Level.SEVERE, "Ignored exception", e);
        }
      } else {
        setState(new FailedListAsyncMaterializer<F>(error), STATUS_DONE);
        try {
          consumer.error(index, error);
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
