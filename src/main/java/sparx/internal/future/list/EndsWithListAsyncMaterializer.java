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
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.concurrent.ExecutionContext.Task;
import sparx.internal.future.AsyncConsumer;
import sparx.internal.future.IndexedAsyncConsumer;
import sparx.internal.future.IndexedAsyncPredicate;

public class EndsWithListAsyncMaterializer<E> extends AbstractListAsyncMaterializer<Boolean> {

  private static final Logger LOGGER = Logger.getLogger(
      EndsWithListAsyncMaterializer.class.getName());

  public EndsWithListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      @NotNull final ListAsyncMaterializer<Object> elementsMaterializer,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    super(context, new AtomicInteger(STATUS_RUNNING));
    setState(new ImmaterialState(wrapped, elementsMaterializer, context, cancelException));
  }

  @Override
  public int knownSize() {
    return 1;
  }

  private interface StateConsumer {

    void accept(@NotNull ListAsyncMaterializer<Boolean> state);
  }

  private class ImmaterialState implements ListAsyncMaterializer<Boolean> {

    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final ListAsyncMaterializer<Object> elementsMaterializer;
    private final ArrayList<StateConsumer> stateConsumers = new ArrayList<StateConsumer>(2);
    private final ListAsyncMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final ListAsyncMaterializer<E> wrapped,
        @NotNull final ListAsyncMaterializer<Object> elementsMaterializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      this.wrapped = wrapped;
      this.elementsMaterializer = elementsMaterializer;
      this.context = context;
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
      return false;
    }

    @Override
    public boolean isSucceeded() {
      return false;
    }

    @Override
    public int knownSize() {
      return 1;
    }

    @Override
    public void materializeCancel(@NotNull final CancellationException exception) {
      wrapped.materializeCancel(exception);
      elementsMaterializer.materializeCancel(exception);
      consumeState(setCancelled(exception));
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
    public void materializeElement(final int index,
        @NotNull final IndexedAsyncConsumer<Boolean> consumer) {
      if (index < 0) {
        safeConsumeError(consumer, new IndexOutOfBoundsException(Integer.toString(index)), LOGGER);
      } else if (index > 1) {
        safeConsumeComplete(consumer, 1, LOGGER);
      } else {
        materialized(new StateConsumer() {
          @Override
          public void accept(@NotNull final ListAsyncMaterializer<Boolean> state) {
            state.materializeElement(index, consumer);
          }
        });
      }
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
    public void materializeHasElement(final int index,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      safeConsume(consumer, index == 0, LOGGER);
    }

    @Override
    public void materializeNextWhile(final int index,
        @NotNull final IndexedAsyncPredicate<Boolean> predicate) {
      materialized(new StateConsumer() {
        @Override
        public void accept(@NotNull final ListAsyncMaterializer<Boolean> state) {
          state.materializeNextWhile(index, predicate);
        }
      });
    }

    @Override
    public void materializePrevWhile(final int index,
        @NotNull final IndexedAsyncPredicate<Boolean> predicate) {
      materialized(new StateConsumer() {
        @Override
        public void accept(@NotNull final ListAsyncMaterializer<Boolean> state) {
          state.materializePrevWhile(index, predicate);
        }
      });
    }

    @Override
    public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
      safeConsume(consumer, 1, LOGGER);
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
      if (!stateConsumers.isEmpty()) {
        return 1;
      }
      final ListAsyncMaterializer<Object> elementsMaterializer = this.elementsMaterializer;
      return (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightSize() + elementsMaterializer.weightSize()
              + elementsMaterializer.weightElement());
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
      return weightElements();
    }

    @Override
    public int weightPrevWhile() {
      return weightElements();
    }

    @Override
    public int weightSize() {
      return 1;
    }

    private void consumeState(@NotNull final ListAsyncMaterializer<Boolean> state) {
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
        final ListAsyncMaterializer<E> wrapped = this.wrapped;
        final ListAsyncMaterializer<Object> elementsMaterializer = this.elementsMaterializer;
        elementsMaterializer.materializeSize(new CancellableAsyncConsumer<Integer>() {
          @Override
          public void cancellableAccept(final Integer elementsSize) {
            if (elementsSize == 0) {
              setState(true);
            } else {
              wrapped.materializeSize(new CancellableAsyncConsumer<Integer>() {
                @Override
                public void cancellableAccept(final Integer wrappedSize) {
                  if (wrappedSize == 0) {
                    setState(false);
                  } else {
                    elementsMaterializer.materializeElement(elementsSize - 1,
                        new MaterializingAsyncConsumer(wrappedSize));
                  }
                }

                @Override
                public void error(@NotNull final Exception error) {
                  setError(error);
                }
              });
            }
          }

          @Override
          public void error(@NotNull final Exception error) {
            setError(error);
          }
        });
      }
    }

    private void setError(@NotNull final Exception error) {
      final CancellationException exception = cancelException.get();
      if (exception != null) {
        consumeState(setCancelled(exception));
      } else {
        consumeState(setFailed(error));
      }
    }

    private void setState(final boolean endsWith) {
      consumeState(setDone(new ElementToListAsyncMaterializer<Boolean>(endsWith)));
    }

    private class MaterializingAsyncConsumer extends
        CancellableIndexedAsyncConsumer<Object> implements Task {

      private Object element;
      private int elementsIndex;
      private boolean isWrapped;
      private String taskID;
      private int wrappedIndex;

      private MaterializingAsyncConsumer(final int wrappedSize) {
        wrappedIndex = wrappedSize;
      }

      @Override
      public void cancellableAccept(final int size, final int index, final Object element) {
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
      public void error(@NotNull final Exception error) {
        setError(error);
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

      @Override
      @SuppressWarnings("unchecked")
      protected void runWithContext() {
        isWrapped = true;
        ((ListAsyncMaterializer<Object>) wrapped).materializeElement(--wrappedIndex, this);
      }
    }
  }
}
