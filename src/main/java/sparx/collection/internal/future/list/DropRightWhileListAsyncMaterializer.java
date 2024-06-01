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
import org.jetbrains.annotations.NotNull;
import sparx.collection.internal.future.AsyncConsumer;
import sparx.collection.internal.future.IndexedAsyncConsumer;
import sparx.concurrent.ExecutionContext;
import sparx.concurrent.ExecutionContext.Task;
import sparx.util.Require;
import sparx.util.function.Function;
import sparx.util.function.IndexedPredicate;

public class DropRightWhileListAsyncMaterializer<E> extends AbstractListAsyncMaterializer<E> {

  public DropRightWhileListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      @NotNull final IndexedPredicate<? super E> predicate, @NotNull final ExecutionContext context,
      @NotNull final AtomicBoolean isCancelled,
      @NotNull final Function<List<E>, List<E>> decorateFunction) {
    super(new AtomicInteger(STATUS_RUNNING));
    setState(new ImmaterialState(wrapped, Require.notNull(predicate, "predicate"),
        Require.notNull(context, "context"), Require.notNull(isCancelled, "isCancelled"),
        Require.notNull(decorateFunction, "decorateFunction")), STATUS_RUNNING);
  }

  @Override
  public int knownSize() {
    return -1;
  }

  private interface StateConsumer<E> {

    void accept(@NotNull ListAsyncMaterializer<E> state);
  }

  private class ImmaterialState implements ListAsyncMaterializer<E> {

    private final ExecutionContext context;
    private final Function<List<E>, List<E>> decorateFunction;
    private final AtomicBoolean isCancelled;
    private final IndexedPredicate<? super E> predicate;
    private final ArrayList<StateConsumer<E>> stateConsumers = new ArrayList<StateConsumer<E>>(2);
    private final ListAsyncMaterializer<E> wrapped;

    private int wrappedSize;

    public ImmaterialState(@NotNull final ListAsyncMaterializer<E> wrapped,
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final ExecutionContext context, @NotNull final AtomicBoolean isCancelled,
        @NotNull final Function<List<E>, List<E>> decorateFunction) {
      this.wrapped = wrapped;
      this.predicate = predicate;
      this.context = context;
      this.isCancelled = isCancelled;
      this.decorateFunction = decorateFunction;
      wrappedSize = wrapped.knownSize();
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
      setState(new CancelledListAsyncMaterializer<E>(), STATUS_CANCELLED);
    }

    @Override
    public void materializeContains(final Object element,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final ListAsyncMaterializer<E> state) {
          state.materializeContains(element, consumer);
        }
      });
    }

    @Override
    public void materializeEach(@NotNull final IndexedAsyncConsumer<E> consumer) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final ListAsyncMaterializer<E> state) {
          state.materializeEach(consumer);
        }
      });
    }

    @Override
    public void materializeElement(final int index,
        @NotNull final IndexedAsyncConsumer<E> consumer) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final ListAsyncMaterializer<E> state) {
          state.materializeElement(index, consumer);
        }
      });
    }

    @Override
    public void materializeElements(@NotNull final AsyncConsumer<List<E>> consumer) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final ListAsyncMaterializer<E> state) {
          state.materializeElements(consumer);
        }
      });
    }

    @Override
    public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final ListAsyncMaterializer<E> state) {
          state.materializeEmpty(consumer);
        }
      });
    }

    @Override
    public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final ListAsyncMaterializer<E> state) {
          state.materializeSize(consumer);
        }
      });
    }

    private @NotNull String getTaskID() {
      final String taskID = context.currentTaskID();
      return taskID != null ? taskID : "";
    }

    private void materialized(@NotNull final StateConsumer<E> consumer) {
      if (wrappedSize < 0) {
        wrapped.materializeSize(new AsyncConsumer<Integer>() {
          @Override
          public void accept(final Integer size) {
            wrappedSize = size;
            materialized(consumer);
          }

          @Override
          public void error(@NotNull final Exception error) {
            setState(new FailedListAsyncMaterializer<E>(error), STATUS_DONE);
          }
        });
      } else {
        final ArrayList<StateConsumer<E>> stateConsumers = this.stateConsumers;
        stateConsumers.add(consumer);
        if (stateConsumers.size() == 1) {
          wrapped.materializeElement(wrappedSize - 1, new MaterializingAsyncConsumer());
        }
      }
    }

    private void setState(@NotNull final ListAsyncMaterializer<E> newState, final int statusCode) {
      final ListAsyncMaterializer<E> state = DropRightWhileListAsyncMaterializer.this.setState(
          newState, statusCode);
      final ArrayList<StateConsumer<E>> stateConsumers = this.stateConsumers;
      for (final StateConsumer<E> stateConsumer : stateConsumers) {
        stateConsumer.accept(state);
      }
      stateConsumers.clear();
    }

    private class MaterializingAsyncConsumer implements IndexedAsyncConsumer<E>, Task {

      private int index;
      private String taskID;

      @Override
      public void accept(final int size, final int index, final E element) throws Exception {
        if (predicate.test(index, element)) {
          if (index > 0) {
            this.index = index - 1;
            taskID = getTaskID();
            context.scheduleAfter(this);
          } else {
            final List<E> materialized = decorateFunction.apply(Collections.<E>emptyList());
            setState(new ListToListAsyncMaterializer<E>(materialized), STATUS_DONE);
          }
        } else {
          final int maxElements = wrappedSize - index - 1;
          if (maxElements == 0) {
            setState(wrapped, STATUS_DONE);
          } else {
            setState(new DropRightListAsyncMaterializer<E>(wrapped, maxElements, status, context,
                isCancelled, decorateFunction), STATUS_RUNNING);
          }
        }
      }

      @Override
      public void complete(final int size) {
      }

      @Override
      public void error(final int index, @NotNull final Exception error) {
        if (isCancelled.get()) {
          setState(new CancelledListAsyncMaterializer<E>(), STATUS_CANCELLED);
        } else {
          setState(new FailedListAsyncMaterializer<E>(error), STATUS_DONE);
        }
      }

      @Override
      public void run() {
        wrapped.materializeElement(index, this);
      }

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        return 1;
      }
    }
  }
}
