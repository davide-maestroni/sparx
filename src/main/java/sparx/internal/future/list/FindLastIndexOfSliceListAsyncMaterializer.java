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

public class FindLastIndexOfSliceListAsyncMaterializer<E> extends
    AbstractListAsyncMaterializer<Integer> {

  private static final Logger LOGGER = Logger.getLogger(
      FindLastIndexOfSliceListAsyncMaterializer.class.getName());

  public FindLastIndexOfSliceListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      @NotNull final ListAsyncMaterializer<Object> elementsMaterializer,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final Function<List<Integer>, List<Integer>> decorateFunction) {
    super(new AtomicInteger(STATUS_RUNNING));
    setState(new ImmaterialState(wrapped, elementsMaterializer, context, cancelException,
        decorateFunction));
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
      return -1;
    }

    @Override
    public void materializeCancel(@NotNull final CancellationException exception) {
      wrapped.materializeCancel(exception);
      consumeState(setCancelled(exception));
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
        safeConsumeError(consumer, new IndexOutOfBoundsException(Integer.toString(index)), LOGGER);
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
    public void materializeHasElement(final int index,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      materialized(new StateConsumer() {
        @Override
        public void accept(@NotNull final ListAsyncMaterializer<Integer> state) {
          state.materializeHasElement(index, consumer);
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
    public int weightContains() {
      return weightElements();
    }

    @Override
    public int weightElement() {
      return weightElements();
    }

    @Override
    public int weightElements() {
      final ListAsyncMaterializer<Object> elementsMaterializer = this.elementsMaterializer;
      return (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightSize() + elementsMaterializer.weightSize()
              + elementsMaterializer.weightElement());
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

    private void consumeState(@NotNull final ListAsyncMaterializer<Integer> state) {
      final ArrayList<StateConsumer> stateConsumers = this.stateConsumers;
      for (final StateConsumer stateConsumer : stateConsumers) {
        stateConsumer.accept(state);
      }
      stateConsumers.clear();
    }

    private @NotNull String getTaskID() {
      final String taskID = context.currentTaskID();
      return taskID != null ? taskID : "";
    }

    private void materialized(@NotNull final StateConsumer consumer) {
      final ArrayList<StateConsumer> stateConsumers = this.stateConsumers;
      stateConsumers.add(consumer);
      if (stateConsumers.size() == 1) {
        elementsMaterializer.materializeSize(new CancellableAsyncConsumer<Integer>() {
          @Override
          public void cancellableAccept(final Integer size) {
            wrapped.materializeSize(new MaterializingAsyncConsumer(size));
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

    private void setState() throws Exception {
      consumeState(FindLastIndexOfSliceListAsyncMaterializer.this.setState(
          new ListToListAsyncMaterializer<Integer>(
              decorateFunction.apply(Collections.<Integer>emptyList()))));
    }

    private void setState(final int index) throws Exception {
      consumeState(FindLastIndexOfSliceListAsyncMaterializer.this.setState(
          new ListToListAsyncMaterializer<Integer>(
              decorateFunction.apply(Collections.singletonList(index)))));
    }

    private class MaterializingAsyncConsumer extends
        CancellableMultiAsyncConsumer<Integer, Object> implements Task {

      private final int elementsSize;

      private Object element;
      private int elementsIndex;
      private int index;
      private boolean isWrapped;
      private String taskID;
      private int wrappedIndex;

      private MaterializingAsyncConsumer(final int elementsSize) {
        this.elementsSize = elementsSize;
      }

      @Override
      public void cancellableAccept(final Integer size) throws Exception {
        if (elementsSize == 0) {
          setState(size);
        } else if (size == 0) {
          setState();
        } else {
          wrappedIndex = index = size - 1;
          isWrapped = false;
          elementsMaterializer.materializeElement(elementsIndex = elementsSize - 1, this);
        }
      }

      @Override
      public void cancellableAccept(final int size, final int index, final Object element)
          throws Exception {
        if (isWrapped) {
          --wrappedIndex;
          isWrapped = false;
          final ListAsyncMaterializer<Object> elementsMaterializer = ImmaterialState.this.elementsMaterializer;
          if (element == this.element || (element != null && element.equals(this.element))) {
            if (elementsIndex == 0) {
              setState(this.index - elementsSize + 1);
            } else if (wrappedIndex < 0) {
              setState();
            } else {
              elementsMaterializer.materializeElement(--elementsIndex, this);
            }
          } else if (this.index == 0) {
            setState();
          } else {
            wrappedIndex = --this.index;
            elementsMaterializer.materializeElement(elementsIndex = elementsSize - 1, this);
          }
        } else {
          this.element = element;
          taskID = getTaskID();
          context.scheduleAfter(this);
        }
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
