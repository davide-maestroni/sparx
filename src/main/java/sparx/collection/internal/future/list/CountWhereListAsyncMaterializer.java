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
package sparx.collection.internal.future.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.collection.internal.future.AsyncConsumer;
import sparx.collection.internal.future.IndexedAsyncConsumer;
import sparx.concurrent.ExecutionContext;
import sparx.concurrent.ExecutionContext.Task;
import sparx.util.Require;
import sparx.util.function.Function;
import sparx.util.function.IndexedPredicate;

public class CountWhereListAsyncMaterializer<E> implements ListAsyncMaterializer<Integer> {

  private static final Logger LOGGER = Logger.getLogger(
      CountWhereListAsyncMaterializer.class.getName());

  private static final int STATUS_CANCELLED = 2;
  private static final int STATUS_DONE = 1;
  private static final int STATUS_RUNNING = 0;

  private final AtomicInteger status = new AtomicInteger(STATUS_RUNNING);

  private ListAsyncMaterializer<Integer> state;

  public CountWhereListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      @NotNull final IndexedPredicate<? super E> predicate, @NotNull final ExecutionContext context,
      @NotNull final AtomicBoolean isCancelled,
      @NotNull final Function<List<Integer>, List<Integer>> decorateFunction) {
    state = new ImmaterialState(Require.notNull(wrapped, "wrapped"),
        Require.notNull(predicate, "predicate"), Require.notNull(context, "context"),
        Require.notNull(isCancelled, "isCancelled"),
        Require.notNull(decorateFunction, "decorateFunction"));
  }

  @Override
  public boolean isCancelled() {
    return status.get() == STATUS_CANCELLED;
  }

  @Override
  public boolean isDone() {
    return status.get() != STATUS_RUNNING;
  }

  @Override
  public int knownSize() {
    return -1;
  }

  @Override
  public void materializeCancel(final boolean mayInterruptIfRunning) {
    state.materializeCancel(mayInterruptIfRunning);
  }

  @Override
  public void materializeContains(final Object element,
      @NotNull final AsyncConsumer<Boolean> consumer) {
    state.materializeContains(element, consumer);
  }

  @Override
  public void materializeEach(@NotNull final IndexedAsyncConsumer<Integer> consumer) {
    state.materializeEach(consumer);
  }

  @Override
  public void materializeElement(final int index,
      @NotNull final IndexedAsyncConsumer<Integer> consumer) {
    state.materializeElement(index, consumer);
  }

  @Override
  public void materializeElements(@NotNull final AsyncConsumer<List<Integer>> consumer) {
    state.materializeElements(consumer);
  }

  @Override
  public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
    state.materializeEmpty(consumer);
  }

  @Override
  public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
    state.materializeSize(consumer);
  }

  private interface StateConsumer {

    void accept(@NotNull ListAsyncMaterializer<Integer> state);
  }

  private class ImmaterialState extends AbstractListAsyncMaterializer<Integer> {

    private final ExecutionContext context;
    private final Function<List<Integer>, List<Integer>> decorateFunction;
    private final AtomicBoolean isCancelled;
    private final IndexedPredicate<? super E> predicate;
    private final ArrayList<StateConsumer> stateConsumers = new ArrayList<StateConsumer>(2);
    private final ListAsyncMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final ListAsyncMaterializer<E> wrapped,
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final ExecutionContext context, @NotNull final AtomicBoolean isCancelled,
        @NotNull final Function<List<Integer>, List<Integer>> decorateFunction) {
      this.wrapped = wrapped;
      this.predicate = predicate;
      this.context = context;
      this.isCancelled = isCancelled;
      this.decorateFunction = decorateFunction;
    }

    @Override
    public boolean isCancelled() {
      return status.get() == STATUS_CANCELLED;
    }

    @Override
    public boolean isDone() {
      return status.get() != STATUS_RUNNING;
    }

    @Override
    public int knownSize() {
      return -1;
    }

    @Override
    public void materializeCancel(final boolean mayInterruptIfRunning) {
      wrapped.materializeCancel(mayInterruptIfRunning);
      setState(new CancelledListAsyncMaterializer<Integer>(), STATUS_CANCELLED);
    }

    @Override
    public void materializeContains(final Object element,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      materialized(new StateConsumer() {
        @Override
        public void accept(@NotNull final ListAsyncMaterializer<Integer> state) {
          state.materializeContains(element, consumer);
        }
      });
    }

    @Override
    public void materializeEach(@NotNull final IndexedAsyncConsumer<Integer> consumer) {
      materialized(new StateConsumer() {
        @Override
        public void accept(@NotNull final ListAsyncMaterializer<Integer> state) {
          state.materializeEach(consumer);
        }
      });
    }

    @Override
    public void materializeElement(final int index,
        @NotNull final IndexedAsyncConsumer<Integer> consumer) {
      if (index < 0) {
        safeConsumeError(consumer, index, new IndexOutOfBoundsException(Integer.toString(index)),
            LOGGER);
      } else {
        materialized(new StateConsumer() {
          @Override
          public void accept(@NotNull final ListAsyncMaterializer<Integer> state) {
            state.materializeElement(index, consumer);
          }
        });
      }
    }

    @Override
    public void materializeElements(@NotNull final AsyncConsumer<List<Integer>> consumer) {
      materialized(new StateConsumer() {
        @Override
        public void accept(@NotNull final ListAsyncMaterializer<Integer> state) {
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

    private @NotNull String getTaskID() {
      final String taskID = context.currentTaskID();
      return taskID != null ? taskID : "";
    }

    private void materialized(@NotNull final StateConsumer consumer) {
      final ArrayList<StateConsumer> stateConsumers = this.stateConsumers;
      stateConsumers.add(consumer);
      if (stateConsumers.size() == 1) {
        final ListAsyncMaterializer<E> wrapped = this.wrapped;
        wrapped.materializeElement(0, new IndexedAsyncConsumer<E>() {
          private int count;

          @Override
          public void accept(final int size, final int index, final E element) {
            try {
              if (predicate.test(index, element)) {
                ++count;
              }
              final String taskID = getTaskID();
              final IndexedAsyncConsumer<E> elementConsumer = this;
              context.scheduleAfter(new Task() {
                @Override
                public @NotNull String taskID() {
                  return taskID;
                }

                @Override
                public int weight() {
                  return 1;
                }

                @Override
                public void run() {
                  wrapped.materializeElement(index + 1, elementConsumer);
                }
              });
            } catch (final Exception e) {
              if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
              }
              if (isCancelled.get()) {
                setState(new CancelledListAsyncMaterializer<Integer>(), STATUS_CANCELLED);
              } else {
                setState(new FailedListAsyncMaterializer<Integer>(e), STATUS_DONE);
              }
            }
          }

          @Override
          public void complete(final int size) throws Exception {
            setState(count);
          }

          @Override
          public void error(final int index, @NotNull final Exception error) {
            if (isCancelled.get()) {
              setState(new CancelledListAsyncMaterializer<Integer>(), STATUS_CANCELLED);
            } else {
              setState(new FailedListAsyncMaterializer<Integer>(error), STATUS_DONE);
            }
          }
        });
      }
    }

    private void setState(final int size) throws Exception {
      setState(new ListToListAsyncMaterializer<Integer>(
          decorateFunction.apply(Collections.singletonList(size))), STATUS_DONE);
    }

    private void setState(@NotNull final ListAsyncMaterializer<Integer> newState,
        final int statusCode) {
      final ListAsyncMaterializer<Integer> state;
      if (status.compareAndSet(STATUS_RUNNING, statusCode)) {
        state = CountWhereListAsyncMaterializer.this.state = newState;
      } else {
        state = CountWhereListAsyncMaterializer.this.state;
      }
      final ArrayList<StateConsumer> stateConsumers = this.stateConsumers;
      for (final StateConsumer stateConsumer : stateConsumers) {
        stateConsumer.accept(state);
      }
      stateConsumers.clear();
    }
  }
}