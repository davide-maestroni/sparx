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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.concurrent.ExecutionContext.Task;
import sparx.internal.future.AsyncConsumer;
import sparx.internal.future.IndexedAsyncConsumer;
import sparx.util.function.Function;

public class DropRightListAsyncMaterializer<E> extends AbstractListAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      DropRightListAsyncMaterializer.class.getName());

  private final int knownSize;

  // maxElements: positive
  public DropRightListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      final int maxElements, @NotNull final ExecutionContext context,
      @NotNull final AtomicBoolean isCancelled,
      @NotNull final Function<List<E>, List<E>> decorateFunction) {
    this(wrapped, maxElements, new AtomicInteger(STATUS_RUNNING), context, isCancelled,
        decorateFunction);
  }

  DropRightListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      final int maxElements, @NotNull final AtomicInteger status,
      @NotNull final ExecutionContext context, @NotNull final AtomicBoolean isCancelled,
      @NotNull final Function<List<E>, List<E>> decorateFunction) {
    super(status);
    final int wrappedSize = wrapped.knownSize();
    knownSize = wrappedSize >= 0 ? Math.max(0, wrappedSize - maxElements) : -1;
    setState(new ImmaterialState(wrapped, maxElements, context, isCancelled, decorateFunction),
        STATUS_RUNNING);
  }

  @Override
  public int knownSize() {
    return knownSize;
  }

  private class ImmaterialState implements ListAsyncMaterializer<E> {

    private final ExecutionContext context;
    private final Function<List<E>, List<E>> decorateFunction;
    private final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = new ArrayList<AsyncConsumer<List<E>>>(
        2);
    private final int maxElements;
    private final AtomicBoolean isCancelled;
    private final ListAsyncMaterializer<E> wrapped;

    private int wrappedSize;

    public ImmaterialState(@NotNull final ListAsyncMaterializer<E> wrapped, final int maxElements,
        @NotNull final ExecutionContext context, @NotNull final AtomicBoolean isCancelled,
        @NotNull final Function<List<E>, List<E>> decorateFunction) {
      this.wrapped = wrapped;
      this.maxElements = maxElements;
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
      return knownSize;
    }

    @Override
    public void materializeCancel(final boolean mayInterruptIfRunning) {
      wrapped.materializeCancel(mayInterruptIfRunning);
      setState(new CancelledListAsyncMaterializer<E>(), STATUS_CANCELLED);
    }

    @Override
    public void materializeContains(final Object element,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      if (wrappedSize < 0) {
        wrapped.materializeSize(new AsyncConsumer<Integer>() {
          @Override
          public void accept(final Integer size) {
            wrappedSize = size;
            materializeContains(element, consumer);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            setState(new FailedListAsyncMaterializer<E>(error), STATUS_DONE);
            consumer.error(error);
          }
        });
      }
      final int maxIndex = wrappedSize - maxElements - 1;
      if (maxIndex < 0) {
        safeConsume(consumer, false, LOGGER);
      } else {
        if (element == null) {
          wrapped.materializeElement(0, new MaterializingContainsNullAsyncConsumer(consumer));
        } else {
          wrapped.materializeElement(maxElements,
              new MaterializingContainsElementAsyncConsumer(element, consumer));
        }
      }
    }

    @Override
    public void materializeEach(@NotNull final IndexedAsyncConsumer<E> consumer) {
      if (wrappedSize < 0) {
        wrapped.materializeSize(new AsyncConsumer<Integer>() {
          @Override
          public void accept(final Integer size) {
            wrappedSize = size;
            materializeEach(consumer);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            setState(new FailedListAsyncMaterializer<E>(error), STATUS_DONE);
            consumer.error(-1, error);
          }
        });
      }
      final int maxIndex = wrappedSize - maxElements - 1;
      if (maxIndex < 0) {
        safeConsumeComplete(consumer, 0, LOGGER);
      } else {
        wrapped.materializeElement(0, new MaterializingEachAsyncConsumer(consumer));
      }
    }

    @Override
    public void materializeElement(final int index,
        @NotNull final IndexedAsyncConsumer<E> consumer) {
      if (index < 0) {
        safeConsumeError(consumer, index, new IndexOutOfBoundsException(Integer.toString(index)),
            LOGGER);
      } else {
        if (wrappedSize < 0) {
          wrapped.materializeSize(new AsyncConsumer<Integer>() {
            @Override
            public void accept(final Integer size) {
              wrappedSize = size;
              materializeElement(index, consumer);
            }

            @Override
            public void error(@NotNull final Exception error) throws Exception {
              setState(new FailedListAsyncMaterializer<E>(error), STATUS_DONE);
              consumer.error(-1, error);
            }
          });
        }
        final int maxIndex = wrappedSize - maxElements - 1;
        if (maxIndex < 0) {
          safeConsumeComplete(consumer, 0, LOGGER);
        } else if (index > maxIndex) {
          safeConsumeComplete(consumer, maxIndex + 1, LOGGER);
        } else {
          wrapped.materializeElement(index, new IndexedAsyncConsumer<E>() {
            @Override
            public void accept(final int size, final int index, final E element) throws Exception {
              consumer.accept(maxIndex + 1, index, element);
            }

            @Override
            public void complete(final int size) throws Exception {
              consumer.complete(maxIndex + 1);
            }

            @Override
            public void error(final int index, @NotNull final Exception error) throws Exception {
              consumer.error(index, error);
            }
          });
        }
      }
    }

    @Override
    public void materializeElements(@NotNull final AsyncConsumer<List<E>> consumer) {
      final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      elementsConsumers.add(consumer);
      if (elementsConsumers.size() == 1) {
        if (wrappedSize >= 0 && wrappedSize <= maxElements) {
          try {
            final List<E> materialized = decorateFunction.apply(Collections.<E>emptyList());
            setState(new ListToListAsyncMaterializer<E>(materialized), STATUS_DONE);
          } catch (final Exception e) {
            if (e instanceof InterruptedException) {
              Thread.currentThread().interrupt();
            }
            if (isCancelled.get()) {
              setState(new CancelledListAsyncMaterializer<E>(), STATUS_CANCELLED);
              consumeError(new CancellationException());
            } else {
              setState(new FailedListAsyncMaterializer<E>(e), STATUS_DONE);
              consumeError(e);
            }
          }
        } else {
          wrapped.materializeSize(new MaterializingAsyncConsumer());
        }
      }
    }

    @Override
    public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
      wrapped.materializeSize(new AsyncConsumer<Integer>() {
        @Override
        public void accept(final Integer size) throws Exception {
          wrappedSize = size;
          consumer.accept(size <= maxElements);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
      if (wrappedSize >= 0) {
        safeConsume(consumer, Math.max(0, wrappedSize - maxElements), LOGGER);
      } else {
        wrapped.materializeSize(new AsyncConsumer<Integer>() {
          @Override
          public void accept(final Integer size) throws Exception {
            consumer.accept(Math.max(0, (wrappedSize = size) - maxElements));
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
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

    private class MaterializingAsyncConsumer implements AsyncConsumer<Integer>,
        IndexedAsyncConsumer<E>, Task {

      private final ArrayList<E> elements = new ArrayList<E>();

      private int index;
      private int maxIndex;
      private String taskID;

      @Override
      public void accept(final Integer size) throws Exception {
        if ((maxIndex = Math.max(0, size - maxElements)) == 0) {
          consumeElements(decorateFunction.apply(Collections.<E>emptyList()));
        } else {
          taskID = getTaskID();
          context.scheduleAfter(this);
        }
      }

      @Override
      public void accept(final int size, final int index, final E element) throws Exception {
        elements.add(element);
        this.index = index + 1;
        if (this.index >= maxIndex) {
          consumeElements(decorateFunction.apply(elements));
        } else {
          taskID = getTaskID();
          context.scheduleAfter(this);
        }
      }

      @Override
      public void complete(final int size) throws Exception {
        consumeElements(decorateFunction.apply(elements));
      }

      @Override
      public void error(@NotNull final Exception error) {
        if (isCancelled.get()) {
          setState(new CancelledListAsyncMaterializer<E>(), STATUS_CANCELLED);
          consumeError(new CancellationException());
        } else {
          setState(new FailedListAsyncMaterializer<E>(error), STATUS_DONE);
          consumeError(error);
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

    private class MaterializingContainsElementAsyncConsumer implements IndexedAsyncConsumer<E>,
        Task {

      private final AsyncConsumer<Boolean> consumer;
      private final Object element;
      private final int maxIndex = wrappedSize - maxElements - 1;

      private int index;
      private String taskID;

      private MaterializingContainsElementAsyncConsumer(@NotNull final Object element,
          @NotNull final AsyncConsumer<Boolean> consumer) {
        this.element = element;
        this.consumer = consumer;
      }

      @Override
      public void accept(final int size, final int index, final E element) throws Exception {
        wrappedSize = Math.max(wrappedSize, size);
        if (this.element.equals(element)) {
          consumer.accept(true);
        } else if (index < maxIndex) {
          this.index = index + 1;
          taskID = getTaskID();
          context.scheduleAfter(this);
        }
      }

      @Override
      public void complete(final int size) throws Exception {
        wrappedSize = size;
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
        return 1;
      }
    }

    private class MaterializingContainsNullAsyncConsumer implements IndexedAsyncConsumer<E>, Task {

      private final AsyncConsumer<Boolean> consumer;
      private final int maxIndex = wrappedSize - maxElements - 1;

      private int index;
      private String taskID;

      private MaterializingContainsNullAsyncConsumer(
          @NotNull final AsyncConsumer<Boolean> consumer) {
        this.consumer = consumer;
      }

      @Override
      public void accept(final int size, final int index, final E element) throws Exception {
        wrappedSize = Math.max(wrappedSize, size);
        if (element == null) {
          consumer.accept(true);
        } else if (index < maxIndex) {
          this.index = index + 1;
          taskID = getTaskID();
          context.scheduleAfter(this);
        }
      }

      @Override
      public void complete(final int size) throws Exception {
        wrappedSize = size;
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
        return 1;
      }
    }

    private class MaterializingEachAsyncConsumer implements IndexedAsyncConsumer<E>, Task {

      private final IndexedAsyncConsumer<E> consumer;
      private final int maxIndex = wrappedSize - maxElements - 1;

      private int index;
      private String taskID;

      private MaterializingEachAsyncConsumer(@NotNull final IndexedAsyncConsumer<E> consumer) {
        this.consumer = consumer;
      }

      @Override
      public void accept(final int size, final int index, final E element) throws Exception {
        consumer.accept(maxIndex + 1, index, element);
        if (index < maxIndex) {
          this.index = index + 1;
          taskID = getTaskID();
          context.scheduleAfter(this);
        } else {
          complete(0);
        }
      }

      @Override
      public void complete(final int size) throws Exception {
        consumer.complete(maxIndex + 1);
      }

      @Override
      public void error(final int index, @NotNull final Exception error) throws Exception {
        consumer.error(index, error);
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
