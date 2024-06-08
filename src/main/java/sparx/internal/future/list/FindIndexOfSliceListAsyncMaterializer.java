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

import static sparx.internal.future.AsyncConsumers.safeConsumeError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.concurrent.ExecutionContext.Task;
import sparx.internal.future.AsyncConsumer;
import sparx.internal.future.IndexedAsyncConsumer;
import sparx.util.function.Function;

public class FindIndexOfSliceListAsyncMaterializer<E> extends
    AbstractListAsyncMaterializer<Integer> {

  private static final Logger LOGGER = Logger.getLogger(
      FindIndexOfSliceListAsyncMaterializer.class.getName());

  public FindIndexOfSliceListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      @NotNull final ListAsyncMaterializer<Object> elementsMaterializer,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final Function<List<Integer>, List<Integer>> decorateFunction) {
    super(new AtomicInteger(STATUS_RUNNING));
    setState(new ImmaterialState(wrapped, elementsMaterializer, context, cancelException,
        decorateFunction), STATUS_RUNNING);
  }

  @Override
  public boolean isMaterializedAtOnce() {
    return true;
  }

  @Override
  public int knownSize() {
    return -1;
  }

  private interface StateConsumer {

    void accept(@NotNull ListAsyncMaterializer<Integer> state);
  }

  private class ImmaterialState implements ListAsyncMaterializer<Integer> {

    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final Function<List<Integer>, List<Integer>> decorateFunction;
    private final ListAsyncMaterializer<Object> elementsMaterializer;
    private final ArrayList<StateConsumer> stateConsumers = new ArrayList<StateConsumer>(2);
    private final ListAsyncMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final ListAsyncMaterializer<E> wrapped,
        @NotNull final ListAsyncMaterializer<Object> elementsMaterializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Function<List<Integer>, List<Integer>> decorateFunction) {
      this.wrapped = wrapped;
      this.elementsMaterializer = elementsMaterializer;
      this.context = context;
      this.cancelException = cancelException;
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
    public boolean isMaterializedAtOnce() {
      return true;
    }

    @Override
    public int knownSize() {
      return -1;
    }

    @Override
    public void materializeCancel(@NotNull final CancellationException exception) {
      wrapped.materializeCancel(exception);
      setState(new CancelledListAsyncMaterializer<Integer>(exception), STATUS_CANCELLED);
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
    public void materializeDone(@NotNull final AsyncConsumer<List<Integer>> consumer) {
      safeConsumeError(consumer, new UnsupportedOperationException(), LOGGER);
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

    @Override
    public int weightElement() {
      return weightElements();
    }

    @Override
    public int weightElements() {
      return (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightElement() + ((long) elementsMaterializer.weightElement() * 2));
    }

    @Override
    public int weightSize() {
      return weightElements();
    }

    private @NotNull String getTaskID() {
      final String taskID = context.currentTaskID();
      return taskID != null ? taskID : "";
    }

    private void materialized(@NotNull final StateConsumer consumer) {
      final ArrayList<StateConsumer> stateConsumers = this.stateConsumers;
      stateConsumers.add(consumer);
      if (stateConsumers.size() == 1) {
        elementsMaterializer.materializeEmpty(new AsyncConsumer<Boolean>() {
          @Override
          public void accept(final Boolean empty) throws Exception {
            if (empty) {
              setState(0);
            } else {
              wrapped.materializeEmpty(new MaterializingAsyncConsumer());
            }
          }

          @Override
          public void error(@NotNull final Exception error) {
            final CancellationException exception = cancelException.get();
            if (exception != null) {
              setState(new CancelledListAsyncMaterializer<Integer>(exception), STATUS_CANCELLED);
            } else {
              setState(new FailedListAsyncMaterializer<Integer>(error), STATUS_DONE);
            }
          }
        });
      }
    }

    private void setState() throws Exception {
      setState(new ListToListAsyncMaterializer<Integer>(
          decorateFunction.apply(Collections.<Integer>emptyList())), STATUS_RUNNING);
    }

    private void setState(final int index) throws Exception {
      setState(new ListToListAsyncMaterializer<Integer>(
          decorateFunction.apply(Collections.singletonList(index))), STATUS_RUNNING);
    }

    private void setState(@NotNull final ListAsyncMaterializer<Integer> newState,
        final int statusCode) {
      final ListAsyncMaterializer<Integer> state = FindIndexOfSliceListAsyncMaterializer.this.setState(
          newState, statusCode);
      final ArrayList<StateConsumer> stateConsumers = this.stateConsumers;
      for (final StateConsumer stateConsumer : stateConsumers) {
        stateConsumer.accept(state);
      }
      stateConsumers.clear();
    }

    private class MaterializingAsyncConsumer implements AsyncConsumer<Boolean>,
        IndexedAsyncConsumer<Object>, Task {

      private Object element;
      private int elementsIndex;
      private int index;
      private boolean isWrapped;
      private String taskID;
      private int wrappedIndex;

      @Override
      public void accept(final Boolean empty) throws Exception {
        if (empty) {
          setState();
        } else {
          isWrapped = false;
          elementsMaterializer.materializeElement(elementsIndex, this);
        }
      }

      @Override
      public void accept(final int size, final int index, final Object element) {
        if (isWrapped) {
          ++wrappedIndex;
          isWrapped = false;
          final ListAsyncMaterializer<Object> elementsMaterializer = ImmaterialState.this.elementsMaterializer;
          if (element == this.element || (element != null && element.equals(this.element))) {
            elementsMaterializer.materializeElement(++elementsIndex, this);
          } else {
            wrappedIndex = ++this.index;
            elementsMaterializer.materializeElement(elementsIndex = 0, this);
          }
        } else {
          this.element = element;
          taskID = getTaskID();
          context.scheduleAfter(this);
        }
      }

      @Override
      public void complete(final int size) throws Exception {
        if (isWrapped) {
          setState();
        } else {
          setState(index);
        }
      }

      @Override
      public void error(@NotNull final Exception error) {
        final CancellationException exception = cancelException.get();
        if (exception != null) {
          setState(new CancelledListAsyncMaterializer<Integer>(exception), STATUS_CANCELLED);
        } else {
          setState(new FailedListAsyncMaterializer<Integer>(error), STATUS_DONE);
        }
      }

      @Override
      public void error(final int index, @NotNull final Exception error) {
        error(error);
      }

      @Override
      @SuppressWarnings("unchecked")
      public void run() {
        isWrapped = true;
        ((ListAsyncMaterializer<Object>) wrapped).materializeElement(wrappedIndex, this);
      }

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        return (int) Math.min(Integer.MAX_VALUE,
            (long) wrapped.weightElement() + elementsMaterializer.weightElement());
      }
    }
  }
}
