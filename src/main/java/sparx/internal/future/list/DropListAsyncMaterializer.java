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
import sparx.util.IndexOverflowException;
import sparx.util.Require;
import sparx.util.function.Function;

public class DropListAsyncMaterializer<E> extends AbstractListAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(DropListAsyncMaterializer.class.getName());

  private final int knownSize;

  public DropListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      final int maxElements, @NotNull final ExecutionContext context,
      @NotNull final AtomicBoolean isCancelled,
      @NotNull final Function<List<E>, List<E>> decorateFunction) {
    this(wrapped, maxElements, new AtomicInteger(STATUS_RUNNING), context, isCancelled,
        decorateFunction);
  }

  DropListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped, final int maxElements,
      @NotNull final AtomicInteger status, @NotNull final ExecutionContext context,
      @NotNull final AtomicBoolean isCancelled,
      @NotNull final Function<List<E>, List<E>> decorateFunction) {
    super(status);
    final int wrappedSize = wrapped.knownSize();
    knownSize = wrappedSize >= 0 ? Math.max(0, wrappedSize - maxElements) : -1;
    setState(new ImmaterialState(wrapped, Require.positive(maxElements, "maxElements"),
        Require.notNull(context, "context"), Require.notNull(isCancelled, "isCancelled"),
        Require.notNull(decorateFunction, "decorateFunction")), STATUS_RUNNING);
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

    private int wrappedSize = -1;

    public ImmaterialState(@NotNull final ListAsyncMaterializer<E> wrapped, final int maxElements,
        @NotNull final ExecutionContext context, @NotNull final AtomicBoolean isCancelled,
        @NotNull final Function<List<E>, List<E>> decorateFunction) {
      this.wrapped = wrapped;
      this.maxElements = maxElements;
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
      if (element == null) {
        wrapped.materializeElement(maxElements,
            new MaterializingContainsNullAsyncConsumer(consumer));
      } else {
        wrapped.materializeElement(maxElements,
            new MaterializingContainsElementAsyncConsumer(element, consumer));
      }
    }

    @Override
    public void materializeEach(@NotNull final IndexedAsyncConsumer<E> consumer) {
      wrapped.materializeElement(maxElements, new MaterializingEachAsyncConsumer(consumer));
    }

    @Override
    public void materializeElement(final int index,
        @NotNull final IndexedAsyncConsumer<E> consumer) {
      if (index < 0) {
        safeConsumeError(consumer, index, new IndexOutOfBoundsException(Integer.toString(index)),
            LOGGER);
      } else {
        final int originalIndex = index;
        wrapped.materializeElement(safeIndex(index), new IndexedAsyncConsumer<E>() {
          @Override
          public void accept(final int size, final int index, final E element) throws Exception {
            consumer.accept(safeSize(wrappedSize = Math.max(wrappedSize, size)), originalIndex,
                element);
          }

          @Override
          public void complete(final int size) throws Exception {
            consumer.complete(safeSize(wrappedSize = size));
          }

          @Override
          public void error(final int index, @NotNull final Exception error) throws Exception {
            consumer.error(originalIndex, error);
          }
        });
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
          wrapped.materializeElement(maxElements, new MaterializingAsyncConsumer());
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
        safeConsume(consumer, safeSize(wrappedSize), LOGGER);
      } else {
        wrapped.materializeSize(new AsyncConsumer<Integer>() {
          @Override
          public void accept(final Integer size) throws Exception {
            consumer.accept(safeSize(wrappedSize = size));
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

    private int safeIndex(final int index) {
      if (index >= 0) {
        return IndexOverflowException.safeCast((long) maxElements + index);
      }
      return index;
    }

    private int safeSize(final int wrappedSize) {
      if (wrappedSize >= 0) {
        return Math.max(0, wrappedSize - maxElements);
      }
      return -1;
    }

    private class MaterializingAsyncConsumer implements IndexedAsyncConsumer<E>, Task {

      private final ArrayList<E> elements = new ArrayList<E>();

      private int index;
      private String taskID;

      @Override
      public void accept(final int size, final int index, final E element) {
        elements.add(element);
        this.index = index + 1;
        taskID = getTaskID();
        context.scheduleAfter(this);
      }

      @Override
      public void complete(final int size) throws Exception {
        consumeElements(decorateFunction.apply(elements));
      }

      @Override
      public void error(final int index, @NotNull final Exception error) {
        if (isCancelled.get()) {
          setState(new CancelledListAsyncMaterializer<E>(), STATUS_CANCELLED);
          consumeError(new CancellationException());
        } else {
          setState(new FailedListAsyncMaterializer<E>(error), STATUS_DONE);
          consumeError(error);
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
        wrappedSize = Math.max(wrappedSize, size);
        if (this.element.equals(element)) {
          consumer.accept(true);
        } else {
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
        } else {
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

      private int index;
      private String taskID;

      private MaterializingEachAsyncConsumer(@NotNull final IndexedAsyncConsumer<E> consumer) {
        this.consumer = consumer;
      }

      @Override
      public void accept(final int size, final int index, final E element) throws Exception {
        consumer.accept(safeSize(wrappedSize = Math.max(wrappedSize, size)), index - maxElements,
            element);
        this.index = index + 1;
        taskID = getTaskID();
        context.scheduleAfter(this);
      }

      @Override
      public void complete(final int size) throws Exception {
        consumer.complete(safeSize(wrappedSize = size));
      }

      @Override
      public void error(final int index, @NotNull final Exception error) throws Exception {
        consumer.error(index - maxElements, error);
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
