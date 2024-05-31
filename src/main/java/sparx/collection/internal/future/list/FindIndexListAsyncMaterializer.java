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

public class FindIndexListAsyncMaterializer<E> extends AbstractListAsyncMaterializer<Integer> {

  private ListAsyncMaterializer<Integer> state;

  public FindIndexListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      @NotNull final IndexedPredicate<? super E> predicate, @NotNull final ExecutionContext context,
      @NotNull final AtomicBoolean isCancelled,
      @NotNull final Function<List<Integer>, List<Integer>> decorateFunction) {
    super(new AtomicInteger(STATUS_RUNNING));
    state = new ImmaterialState(Require.notNull(wrapped, "wrapped"),
        Require.notNull(predicate, "predicate"), Require.notNull(context, "context"),
        Require.notNull(isCancelled, "isCancelled"),
        Require.notNull(decorateFunction, "decorateFunction"));
  }

  @Override
  public int knownSize() {
    return 1;
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

  private class ImmaterialState implements ListAsyncMaterializer<Integer> {

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
      return 1;
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
      materialized(new StateConsumer() {
        @Override
        public void accept(@NotNull final ListAsyncMaterializer<Integer> state) {
          state.materializeElement(index, consumer);
        }
      });
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
      materialized(new StateConsumer() {
        @Override
        public void accept(@NotNull final ListAsyncMaterializer<Integer> state) {
          state.materializeEmpty(consumer);
        }
      });
    }

    @Override
    public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
      materialized(new StateConsumer() {
        @Override
        public void accept(@NotNull final ListAsyncMaterializer<Integer> state) {
          state.materializeSize(consumer);
        }
      });
    }

    private @NotNull String getTaskID() {
      final String taskID = context.currentTaskID();
      return taskID != null ? taskID : "";
    }

    private void materialized(@NotNull final StateConsumer consumer) {
      final ArrayList<StateConsumer> stateConsumers = this.stateConsumers;
      stateConsumers.add(consumer);
      if (stateConsumers.size() == 1) {
        wrapped.materializeEmpty(new MaterializingAsyncConsumer());
      }
    }

    private void setState() throws Exception {
      setState(new ListToListAsyncMaterializer<Integer>(
          decorateFunction.apply(Collections.<Integer>emptyList())), STATUS_DONE);
    }

    private void setState(final int index) throws Exception {
      setState(new ListToListAsyncMaterializer<Integer>(
          decorateFunction.apply(Collections.singletonList(index))), STATUS_DONE);
    }

    private void setState(@NotNull final ListAsyncMaterializer<Integer> newState,
        final int statusCode) {
      final ListAsyncMaterializer<Integer> state;
      if (status.compareAndSet(STATUS_RUNNING, statusCode)) {
        state = FindIndexListAsyncMaterializer.this.state = newState;
      } else {
        state = FindIndexListAsyncMaterializer.this.state;
      }
      final ArrayList<StateConsumer> stateConsumers = this.stateConsumers;
      for (final StateConsumer stateConsumer : stateConsumers) {
        stateConsumer.accept(state);
      }
      stateConsumers.clear();
    }

    private class MaterializingAsyncConsumer implements AsyncConsumer<Boolean>,
        IndexedAsyncConsumer<E>, Task {

      private int index;
      private String taskID;

      @Override
      public void accept(final Boolean empty) throws Exception {
        if (empty) {
          setState();
        } else {
          taskID = getTaskID();
          context.scheduleAfter(this);
        }
      }

      @Override
      public void accept(final int size, final int index, final E element) throws Exception {
        if (predicate.test(index, element)) {
          setState(index);
        } else {
          this.index = index + 1;
          taskID = getTaskID();
          context.scheduleAfter(this);
        }
      }

      @Override
      public void complete(final int size) throws Exception {
        setState();
      }

      @Override
      public void error(@NotNull final Exception error) {
        if (isCancelled.get()) {
          setState(new CancelledListAsyncMaterializer<Integer>(), STATUS_CANCELLED);
        } else {
          setState(new FailedListAsyncMaterializer<Integer>(error), STATUS_DONE);
        }
      }

      @Override
      public void error(final int index, @NotNull final Exception error) {
        error(error);
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
