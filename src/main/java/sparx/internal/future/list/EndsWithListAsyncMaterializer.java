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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.concurrent.ExecutionContext.Task;
import sparx.internal.future.AsyncConsumer;
import sparx.internal.future.IndexedAsyncConsumer;
import sparx.util.function.Function;

public class EndsWithListAsyncMaterializer<E> extends AbstractListAsyncMaterializer<Boolean> {

  private static final Logger LOGGER = Logger.getLogger(
      EndsWithListAsyncMaterializer.class.getName());

  public EndsWithListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      @NotNull final ListAsyncMaterializer<Object> elementsMaterializer,
      @NotNull final ExecutionContext context, @NotNull final AtomicBoolean isCancelled,
      @NotNull final Function<List<Boolean>, List<Boolean>> decorateFunction) {
    super(new AtomicInteger(STATUS_RUNNING));
    setState(
        new ImmaterialState(wrapped, elementsMaterializer, context, isCancelled, decorateFunction),
        STATUS_RUNNING);
  }

  @Override
  public boolean isMaterializedOnce() {
    return true;
  }

  @Override
  public int knownSize() {
    return 1;
  }

  private interface StateConsumer {

    void accept(@NotNull ListAsyncMaterializer<Boolean> state);
  }

  private class ImmaterialState implements ListAsyncMaterializer<Boolean> {

    private final ExecutionContext context;
    private final Function<List<Boolean>, List<Boolean>> decorateFunction;
    private final ListAsyncMaterializer<Object> elementsMaterializer;
    private final AtomicBoolean isCancelled;
    private final ArrayList<StateConsumer> stateConsumers = new ArrayList<StateConsumer>(2);
    private final ListAsyncMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final ListAsyncMaterializer<E> wrapped,
        @NotNull final ListAsyncMaterializer<Object> elementsMaterializer,
        @NotNull final ExecutionContext context, @NotNull final AtomicBoolean isCancelled,
        @NotNull final Function<List<Boolean>, List<Boolean>> decorateFunction) {
      this.wrapped = wrapped;
      this.elementsMaterializer = elementsMaterializer;
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
    public boolean isMaterializedOnce() {
      return true;
    }

    @Override
    public int knownSize() {
      return 1;
    }

    @Override
    public void materializeCancel(final boolean mayInterruptIfRunning) {
      wrapped.materializeCancel(mayInterruptIfRunning);
      elementsMaterializer.materializeCancel(mayInterruptIfRunning);
      setState(new CancelledListAsyncMaterializer<Boolean>(), STATUS_CANCELLED);
    }

    @Override
    public void materializeContains(final Object element,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      materialized(new StateConsumer() {
        @Override
        public void accept(@NotNull final ListAsyncMaterializer<Boolean> state) {
          state.materializeContains(element, consumer);
        }
      });
    }

    @Override
    public void materializeEach(@NotNull final IndexedAsyncConsumer<Boolean> consumer) {
      materialized(new StateConsumer() {
        @Override
        public void accept(@NotNull final ListAsyncMaterializer<Boolean> state) {
          state.materializeEach(consumer);
        }
      });
    }

    @Override
    public void materializeElement(final int index,
        @NotNull final IndexedAsyncConsumer<Boolean> consumer) {
      materialized(new StateConsumer() {
        @Override
        public void accept(@NotNull final ListAsyncMaterializer<Boolean> state) {
          state.materializeElement(index, consumer);
        }
      });
    }

    @Override
    public void materializeElements(@NotNull final AsyncConsumer<List<Boolean>> consumer) {
      materialized(new StateConsumer() {
        @Override
        public void accept(@NotNull final ListAsyncMaterializer<Boolean> state) {
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
      return weightElements();
    }

    @Override
    public int weightElements() {
      return (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightSize() + elementsMaterializer.weightElement());
    }

    @Override
    public int weightSize() {
      return 1;
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
        final ListAsyncMaterializer<Object> elementsMaterializer = this.elementsMaterializer;
        elementsMaterializer.materializeSize(new AsyncConsumer<Integer>() {
          @Override
          public void accept(final Integer elementsSize) throws Exception {
            if (elementsSize == 0) {
              setState(true);
            } else {
              wrapped.materializeSize(new AsyncConsumer<Integer>() {
                @Override
                public void accept(final Integer wrappedSize) throws Exception {
                  if (wrappedSize == 0) {
                    setState(false);
                  } else {
                    elementsMaterializer.materializeElement(elementsSize - 1,
                        new MaterializingAsyncConsumer(wrappedSize));
                  }
                }

                @Override
                public void error(@NotNull final Exception error) {
                  setState(error);
                }
              });
            }
          }

          @Override
          public void error(@NotNull final Exception error) {
            setState(error);
          }
        });
      }
    }

    private void setState(final boolean endsWith) throws Exception {
      setState(new ListToListAsyncMaterializer<Boolean>(
          decorateFunction.apply(Collections.singletonList(endsWith))), STATUS_DONE);
    }

    private void setState(@NotNull final Exception error) {
      if (isCancelled.get()) {
        setState(new CancelledListAsyncMaterializer<Boolean>(), STATUS_CANCELLED);
      } else {
        setState(new FailedListAsyncMaterializer<Boolean>(error), STATUS_DONE);
      }
    }

    private void setState(@NotNull final ListAsyncMaterializer<Boolean> newState,
        final int statusCode) {
      final ListAsyncMaterializer<Boolean> state = EndsWithListAsyncMaterializer.this.setState(
          newState, statusCode);
      final ArrayList<StateConsumer> stateConsumers = this.stateConsumers;
      for (final StateConsumer stateConsumer : stateConsumers) {
        stateConsumer.accept(state);
      }
      stateConsumers.clear();
    }

    private class MaterializingAsyncConsumer implements IndexedAsyncConsumer<Object>, Task {

      private Object element;
      private int elementsIndex;
      private boolean isWrapped;
      private String taskID;
      private int wrappedIndex;

      private MaterializingAsyncConsumer(final int wrappedSize) {
        wrappedIndex = wrappedSize;
      }

      @Override
      public void accept(final int size, final int index, final Object element) throws Exception {
        if (isWrapped) {
          if (this.element == null ? element != null : !this.element.equals(element)) {
            setState(false);
          } else if (elementsIndex == 0) {
            setState(true);
          } else {
            isWrapped = false;
            elementsMaterializer.materializeElement(elementsIndex - 1, this);
          }
        } else {
          if (wrappedIndex == 0) {
            setState(false);
          } else {
            this.element = element;
            elementsIndex = index;
            taskID = getTaskID();
            context.scheduleAfter(this);
          }
        }
      }

      @Override
      public void complete(final int size) {
      }

      @Override
      public void error(final int index, @NotNull final Exception error) {
        setState(error);
      }

      @Override
      @SuppressWarnings("unchecked")
      public void run() {
        isWrapped = true;
        ((ListAsyncMaterializer<Object>) wrapped).materializeElement(--wrappedIndex, this);
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
