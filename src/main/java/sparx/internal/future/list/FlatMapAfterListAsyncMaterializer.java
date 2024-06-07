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
import sparx.util.SizeOverflowException;
import sparx.util.function.Function;
import sparx.util.function.IndexedFunction;

public class FlatMapAfterListAsyncMaterializer<E> extends AbstractListAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      FlatMapAfterListAsyncMaterializer.class.getName());

  // numElements: not negative
  public FlatMapAfterListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      final int numElements,
      @NotNull final IndexedFunction<? super E, ? extends ListAsyncMaterializer<E>> mapper,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final Function<List<E>, List<E>> decorateFunction) {
    super(new AtomicInteger(STATUS_RUNNING));
    setState(new ImmaterialState(wrapped, numElements, mapper, context, cancelException,
        decorateFunction), STATUS_RUNNING);
  }

  @Override
  public boolean isMaterializedOnce() {
    return false;
  }

  @Override
  public int knownSize() {
    return -1;
  }

  private class ImmaterialState implements ListAsyncMaterializer<E> {

    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final Function<List<E>, List<E>> decorateFunction;
    private final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = new ArrayList<AsyncConsumer<List<E>>>(
        2);
    private final IndexedFunction<? super E, ? extends ListAsyncMaterializer<E>> mapper;
    private final int numElements;
    private final ListAsyncMaterializer<E> wrapped;

    private ListAsyncMaterializer<E> elementsMaterializer;
    private int elementsSize = -1;
    private int wrappedSize = -1;

    public ImmaterialState(@NotNull final ListAsyncMaterializer<E> wrapped, final int numElements,
        @NotNull final IndexedFunction<? super E, ? extends ListAsyncMaterializer<E>> mapper,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Function<List<E>, List<E>> decorateFunction) {
      this.wrapped = wrapped;
      this.numElements = numElements;
      this.mapper = mapper;
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
    public boolean isMaterializedOnce() {
      return false;
    }

    @Override
    public int knownSize() {
      return -1;
    }

    @Override
    public void materializeCancel(@NotNull final CancellationException exception) {
      wrapped.materializeCancel(exception);
      final ListAsyncMaterializer<E> elementsMaterializer = this.elementsMaterializer;
      if (elementsMaterializer != null) {
        elementsMaterializer.materializeCancel(exception);
      }
      setState(new CancelledListAsyncMaterializer<E>(exception), STATUS_CANCELLED);
    }

    @Override
    public void materializeContains(final Object element,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      materialized(new AsyncConsumer<ListAsyncMaterializer<E>>() {
        @Override
        public void accept(final ListAsyncMaterializer<E> materializer) {
          if (materializer == wrapped) {
            materializer.materializeContains(element, consumer);
          } else {
            if (element == null) {
              new MaterializingContainsNullAsyncConsumer(consumer).schedule();
            } else {
              new MaterializingContainsElementAsyncConsumer(element, consumer).schedule();
            }
          }
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeDone(@NotNull final AsyncConsumer<List<E>> consumer) {
      safeConsumeError(consumer, new UnsupportedOperationException(), LOGGER);
    }

    @Override
    public void materializeEach(@NotNull final IndexedAsyncConsumer<E> consumer) {
      wrapped.materializeElement(0, new MaterializingEachAsyncConsumer(consumer));
    }

    @Override
    public void materializeElement(final int index,
        @NotNull final IndexedAsyncConsumer<E> consumer) {
      if (index < 0) {
        safeConsumeError(consumer, index, new IndexOutOfBoundsException(Integer.toString(index)),
            LOGGER);
      } else if (index >= numElements) {
        final int wrappedSize = this.wrappedSize;
        if (wrappedSize >= 0) {
          if (numElements >= wrappedSize) {
            wrapped.materializeElement(index, consumer);
          } else if (elementsSize >= 0) {
            if (index < numElements + elementsSize) {
              elementsMaterializer.materializeElement(index - numElements, consumer);
            } else {
              wrapped.materializeElement(index - elementsSize + 1, consumer);
            }
          } else {
            materialized(new AsyncConsumer<ListAsyncMaterializer<E>>() {
              @Override
              public void accept(final ListAsyncMaterializer<E> materializer) {
                materializer.materializeElement(index - numElements, new IndexedAsyncConsumer<E>() {
                  @Override
                  public void accept(final int size, final int index, final E element)
                      throws Exception {
                    consumer.accept(safeSize(), index + numElements, element);
                  }

                  @Override
                  public void complete(final int size) {
                    elementsSize = size;
                    materializeElement(index, consumer);
                  }

                  @Override
                  public void error(final int index, @NotNull final Exception error)
                      throws Exception {
                    consumer.error(index + numElements, error);
                  }
                });
              }

              @Override
              public void error(@NotNull Exception error) throws Exception {
                consumer.error(-1, error);
              }
            });
          }
        } else {
          wrapped.materializeSize(new AsyncConsumer<Integer>() {
            @Override
            public void accept(final Integer size) {
              ImmaterialState.this.wrappedSize = size;
              materializeElement(index, consumer);
            }

            @Override
            public void error(@NotNull final Exception error) throws Exception {
              consumer.error(-1, error);
            }
          });
        }
      } else {
        wrapped.materializeElement(index, consumer);
      }
    }

    @Override
    public void materializeElements(@NotNull final AsyncConsumer<List<E>> consumer) {
      final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      elementsConsumers.add(consumer);
      if (elementsConsumers.size() == 1) {
        wrapped.materializeElement(0, new MaterializingAsyncConsumer());
      }
    }

    @Override
    public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
      final int wrappedSize = this.wrappedSize;
      if (wrappedSize == 0) {
        safeConsume(consumer, true, LOGGER);
      } else if (numElements == 0 && wrappedSize == 1) {
        materialized(new AsyncConsumer<ListAsyncMaterializer<E>>() {
          @Override
          public void accept(final ListAsyncMaterializer<E> materializer) {
            materializer.materializeEmpty(consumer);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      } else if (wrappedSize > 0) {
        safeConsume(consumer, false, LOGGER);
      } else {
        wrapped.materializeSize(new AsyncConsumer<Integer>() {
          @Override
          public void accept(final Integer size) {
            ImmaterialState.this.wrappedSize = size;
            materializeEmpty(consumer);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
    }

    @Override
    public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
      if (wrappedSize >= 0) {
        if (elementsSize >= 0) {
          safeConsume(consumer, safeSize(), LOGGER);
        } else {
          materialized(new AsyncConsumer<ListAsyncMaterializer<E>>() {
            @Override
            public void accept(final ListAsyncMaterializer<E> materializer) {
              materializer.materializeSize(new AsyncConsumer<Integer>() {
                @Override
                public void accept(final Integer size) throws Exception {
                  elementsSize = size;
                  consumer.accept(safeSize());
                }

                @Override
                public void error(@NotNull final Exception error) throws Exception {
                  consumer.error(error);
                }
              });
            }

            @Override
            public void error(@NotNull final Exception error) throws Exception {
              consumer.error(error);
            }
          });
        }
      } else {
        wrapped.materializeSize(new AsyncConsumer<Integer>() {
          @Override
          public void accept(final Integer size) {
            wrappedSize = size;
            materializeSize(consumer);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
    }

    @Override
    public int weightElement() {
      return weightElements();
    }

    @Override
    public int weightElements() {
      return (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightSize() + wrapped.weightElement());
    }

    @Override
    public int weightSize() {
      return weightElements();
    }

    private void consumeElements(@NotNull final List<E> elements) {
      final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      for (final AsyncConsumer<List<E>> elementsConsumer : elementsConsumers) {
        safeConsume(elementsConsumer, elements, LOGGER);
      }
      elementsConsumers.clear();
    }

    private void consumeError(@NotNull final Exception error) {
      final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      for (final AsyncConsumer<List<E>> elementsConsumer : elementsConsumers) {
        safeConsumeError(elementsConsumer, error, LOGGER);
      }
      elementsConsumers.clear();
    }

    private @NotNull String getTaskID() {
      final String taskID = context.currentTaskID();
      return taskID != null ? taskID : "";
    }

    private void materialized(@NotNull final AsyncConsumer<ListAsyncMaterializer<E>> consumer) {
      if (elementsMaterializer != null) {
        safeConsume(consumer, elementsMaterializer, LOGGER);
      } else {
        wrapped.materializeElement(numElements, new IndexedAsyncConsumer<E>() {
          @Override
          public void accept(final int size, final int index, final E element) throws Exception {
            consumer.accept(elementsMaterializer = mapper.apply(index, element));
          }

          @Override
          public void complete(final int size) throws Exception {
            setState(wrapped, STATUS_RUNNING);
            consumer.accept(wrapped);
          }

          @Override
          public void error(final int index, @NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
    }

    private int safeSize() {
      final int wrappedSize = this.wrappedSize;
      final long elementsSize = this.elementsSize;
      if (wrappedSize >= 0 && elementsSize >= 0) {
        if (numElements < wrappedSize) {
          return SizeOverflowException.safeCast(wrappedSize + elementsSize - 1);
        }
        return wrappedSize;
      }
      return -1;
    }

    private class MaterializingAsyncConsumer implements IndexedAsyncConsumer<E>, Task {

      private final ArrayList<E> elements = new ArrayList<E>();

      private int elementsIndex;
      private int wrappedIndex;
      private boolean isWrapped = true;
      private String taskID;

      @Override
      public void accept(final int size, final int index, final E element) throws Exception {
        if (isWrapped) {
          wrappedSize = Math.max(wrappedSize, size);
          if (index == numElements) {
            if (elementsMaterializer == null) {
              elementsMaterializer = mapper.apply(index, element);
            }
            isWrapped = false;
            taskID = getTaskID();
            context.scheduleAfter(this);
          } else {
            elements.add(element);
            wrappedIndex = index + 1;
            schedule();
          }
        } else {
          elementsSize = Math.max(elementsSize, size);
          elements.add(element);
          elementsIndex = index + 1;
          schedule();
        }
      }

      @Override
      public void complete(final int size) throws Exception {
        if (isWrapped) {
          wrappedSize = size;
          setState(new ListToListAsyncMaterializer<E>(decorateFunction.apply(elements)),
              STATUS_RUNNING);
          consumeElements(elements);
        } else {
          elementsSize = size;
          isWrapped = true;
          ++wrappedIndex;
          schedule();
        }
      }

      @Override
      public void error(final int index, @NotNull final Exception error) {
        final CancellationException exception = cancelException.get();
        if (exception != null) {
          setState(new CancelledListAsyncMaterializer<E>(exception), STATUS_CANCELLED);
          consumeError(exception);
        } else {
          setState(new FailedListAsyncMaterializer<E>(error), STATUS_DONE);
          consumeError(error);
        }
      }

      @Override
      public void run() {
        if (isWrapped) {
          wrapped.materializeElement(wrappedIndex, this);
        } else {
          elementsMaterializer.materializeElement(elementsIndex, this);
        }
      }

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        return Math.max(wrapped.weightElement(), elementsMaterializer.weightElement());
      }

      private void schedule() {
        taskID = getTaskID();
        context.scheduleAfter(this);
      }
    }

    private class MaterializingContainsElementAsyncConsumer implements IndexedAsyncConsumer<E>,
        Task {

      private final AsyncConsumer<Boolean> consumer;
      private final Object element;

      private int index;
      private String taskID;

      private MaterializingContainsElementAsyncConsumer(@NotNull final Object element,
          @NotNull final AsyncConsumer<Boolean> consumer) {
        this.element = element;
        this.consumer = consumer;
      }

      @Override
      public void accept(final int size, final int index, final E element) throws Exception {
        if (index != numElements && this.element.equals(element)) {
          consumer.accept(true);
        } else {
          this.index = index + 1;
          schedule();
        }
      }

      @Override
      public void complete(final int size) throws Exception {
        consumer.accept(false);
      }

      @Override
      public void error(final int index, @NotNull final Exception error) throws Exception {
        consumer.error(error);
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
        return wrapped.weightElement();
      }

      private void schedule() {
        taskID = getTaskID();
        context.scheduleAfter(this);
      }
    }

    private class MaterializingContainsNullAsyncConsumer implements IndexedAsyncConsumer<E>, Task {

      private final AsyncConsumer<Boolean> consumer;

      private int index;
      private String taskID;

      private MaterializingContainsNullAsyncConsumer(
          @NotNull final AsyncConsumer<Boolean> consumer) {
        this.consumer = consumer;
      }

      @Override
      public void accept(final int size, final int index, final E element) throws Exception {
        if (index != numElements && element == null) {
          consumer.accept(true);
        } else {
          this.index = index + 1;
          schedule();
        }
      }

      @Override
      public void complete(final int size) throws Exception {
        consumer.accept(false);
      }

      @Override
      public void error(final int index, @NotNull final Exception error) throws Exception {
        consumer.error(error);
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
        return wrapped.weightElement();
      }

      private void schedule() {
        taskID = getTaskID();
        context.scheduleAfter(this);
      }
    }

    private class MaterializingEachAsyncConsumer implements IndexedAsyncConsumer<E>, Task {

      private final IndexedAsyncConsumer<E> consumer;

      private int elementsIndex;
      private int index;
      private int wrappedIndex;
      private boolean isWrapped = true;
      private String taskID;

      private MaterializingEachAsyncConsumer(@NotNull final IndexedAsyncConsumer<E> consumer) {
        this.consumer = consumer;
      }

      @Override
      public void accept(final int size, final int index, final E element) throws Exception {
        if (isWrapped) {
          wrappedSize = Math.max(wrappedSize, size);
          if (index == numElements) {
            if (elementsMaterializer == null) {
              elementsMaterializer = mapper.apply(index, element);
            }
            isWrapped = false;
            taskID = getTaskID();
            context.scheduleAfter(this);
          } else {
            consumer.accept(safeSize(), this.index++, element);
            wrappedIndex = index + 1;
            schedule();
          }
        } else {
          elementsSize = Math.max(elementsSize, size);
          consumer.accept(safeSize(), this.index++, element);
          elementsIndex = index + 1;
          schedule();
        }
      }

      @Override
      public void complete(final int size) throws Exception {
        if (isWrapped) {
          wrappedSize = size;
          consumer.complete(safeSize());
        } else {
          elementsSize = size;
          isWrapped = true;
          ++wrappedIndex;
          schedule();
        }
      }

      @Override
      public void error(final int index, @NotNull final Exception error) throws Exception {
        consumer.error(this.index, error);
      }

      @Override
      public void run() {
        if (isWrapped) {
          wrapped.materializeElement(wrappedIndex, this);
        } else {
          elementsMaterializer.materializeElement(elementsIndex, this);
        }
      }

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        return Math.max(wrapped.weightElement(), elementsMaterializer.weightElement());
      }

      private void schedule() {
        taskID = getTaskID();
        context.scheduleAfter(this);
      }
    }
  }
}
