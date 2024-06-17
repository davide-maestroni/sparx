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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
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
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final Function<List<E>, List<E>> decorateFunction) {
    this(wrapped, maxElements, new AtomicInteger(STATUS_RUNNING), context, cancelException,
        decorateFunction);
  }

  DropRightListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      final int maxElements, @NotNull final AtomicInteger status,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final Function<List<E>, List<E>> decorateFunction) {
    super(status);
    final int wrappedSize = wrapped.knownSize();
    knownSize = wrappedSize >= 0 ? Math.max(0, wrappedSize - maxElements) : -1;
    setState(new ImmaterialState(wrapped, maxElements, context, cancelException, decorateFunction));
  }

  @Override
  public int knownSize() {
    return knownSize;
  }

  private class ImmaterialState implements ListAsyncMaterializer<E> {

    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final Function<List<E>, List<E>> decorateFunction;
    private final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = new ArrayList<AsyncConsumer<List<E>>>(
        2);
    private final int maxElements;
    private final ListAsyncMaterializer<E> wrapped;

    private int wrappedSize;

    public ImmaterialState(@NotNull final ListAsyncMaterializer<E> wrapped, final int maxElements,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Function<List<E>, List<E>> decorateFunction) {
      this.wrapped = wrapped;
      this.maxElements = maxElements;
      this.context = context;
      this.cancelException = cancelException;
      this.decorateFunction = decorateFunction;
      wrappedSize = wrapped.knownSize();
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
    public int knownSize() {
      return knownSize;
    }

    @Override
    public void materializeCancel(@NotNull final CancellationException exception) {
      wrapped.materializeCancel(exception);
      setCancelled(exception);
      consumeError(exception);
    }

    @Override
    public void materializeContains(final Object element,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      if (wrappedSize < 0) {
        wrapped.materializeSize(new CancellableAsyncConsumer<Integer>() {
          @Override
          public void cancellableAccept(final Integer size) {
            wrappedSize = size;
            materializeContains(element, consumer);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            final CancellationException exception = cancelException.get();
            if (exception != null) {
              setCancelled(exception);
              consumer.error(exception);
            } else {
              setFailed(error);
              consumer.error(error);
            }
          }
        });
      }
      final int maxIndex = wrappedSize - maxElements - 1;
      if (maxIndex < 0) {
        safeConsume(consumer, false, LOGGER);
      } else {
        if (element == null) {
          wrapped.materializeElement(maxIndex,
              new MaterializingContainsNullAsyncConsumer(consumer));
        } else {
          wrapped.materializeElement(maxIndex,
              new MaterializingContainsElementAsyncConsumer(element, consumer));
        }
      }
    }

    @Override
    public void materializeDone(@NotNull final AsyncConsumer<List<E>> consumer) {
      safeConsumeError(consumer, new UnsupportedOperationException(), LOGGER);
    }

    @Override
    public void materializeEach(@NotNull final IndexedAsyncConsumer<E> consumer) {
      if (wrappedSize < 0) {
        wrapped.materializeSize(new CancellableAsyncConsumer<Integer>() {
          @Override
          public void cancellableAccept(final Integer size) {
            wrappedSize = size;
            materializeEach(consumer);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            final CancellationException exception = cancelException.get();
            if (exception != null) {
              setCancelled(exception);
              consumer.error(exception);
            } else {
              setFailed(error);
              consumer.error(error);
            }
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
        safeConsumeError(consumer, new IndexOutOfBoundsException(Integer.toString(index)), LOGGER);
      } else {
        if (wrappedSize < 0) {
          wrapped.materializeSize(new CancellableAsyncConsumer<Integer>() {
            @Override
            public void cancellableAccept(final Integer size) {
              wrappedSize = size;
              materializeElement(index, consumer);
            }

            @Override
            public void error(@NotNull final Exception error) throws Exception {
              final CancellationException exception = cancelException.get();
              if (exception != null) {
                setCancelled(exception);
                consumer.error(exception);
              } else {
                setFailed(error);
                consumer.error(error);
              }
            }
          });
        }
        final int maxIndex = wrappedSize - maxElements - 1;
        if (maxIndex < 0) {
          safeConsumeComplete(consumer, 0, LOGGER);
        } else if (index > maxIndex) {
          safeConsumeComplete(consumer, maxIndex + 1, LOGGER);
        } else {
          wrapped.materializeElement(index, new CancellableIndexedAsyncConsumer<E>() {
            @Override
            public void cancellableAccept(final int size, final int index, final E element)
                throws Exception {
              consumer.accept(maxIndex + 1, index, element);
            }

            @Override
            public void cancellableComplete(final int size) throws Exception {
              consumer.complete(maxIndex + 1);
            }

            @Override
            public void error(@NotNull final Exception error) throws Exception {
              consumer.error(error);
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
            setState(new ListToListAsyncMaterializer<E>(materialized));
            consumeElements(materialized);
          } catch (final Exception e) {
            if (e instanceof InterruptedException) {
              Thread.currentThread().interrupt();
            }
            final CancellationException exception = cancelException.get();
            if (exception != null) {
              setCancelled(exception);
              consumeError(exception);
            } else {
              setFailed(e);
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
      wrapped.materializeSize(new CancellableAsyncConsumer<Integer>() {
        @Override
        public void cancellableAccept(final Integer size) throws Exception {
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
    public void materializeHasElement(final int index,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      if (index < 0) {
        safeConsume(consumer, false, LOGGER);
      } else if (index < Math.max(0, wrappedSize - maxElements)) {
        safeConsume(consumer, true, LOGGER);
      } else {
        materializeElement(index, new CancellableIndexedAsyncConsumer<E>() {
          @Override
          public void cancellableAccept(final int size, final int index, final E element)
              throws Exception {
            consumer.accept(true);
          }

          @Override
          public void cancellableComplete(final int size) throws Exception {
            consumer.accept(false);
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
        safeConsume(consumer, Math.max(0, wrappedSize - maxElements), LOGGER);
      } else {
        wrapped.materializeSize(new CancellableAsyncConsumer<Integer>() {
          @Override
          public void cancellableAccept(final Integer size) throws Exception {
            consumer.accept(Math.max(0, (wrappedSize = size) - maxElements));
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
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
      return Math.max(wrapped.weightSize(), wrapped.weightElement());
    }

    @Override
    public int weightEmpty() {
      return weightSize();
    }

    @Override
    public int weightHasElement() {
      return weightElements();
    }

    @Override
    public int weightSize() {
      return wrapped.weightSize();
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

    private class MaterializingAsyncConsumer extends
        CancellableMultiAsyncConsumer<Integer, E> implements Task {

      private final ArrayList<E> elements = new ArrayList<E>();

      private int index;
      private int maxIndex;
      private String taskID;

      @Override
      public void cancellableAccept(final Integer size) throws Exception {
        if ((maxIndex = Math.max(0, size - maxElements)) == 0) {
          final List<E> materialized = decorateFunction.apply(Collections.<E>emptyList());
          setState(new ListToListAsyncMaterializer<E>(materialized));
          consumeElements(materialized);
        } else {
          taskID = getTaskID();
          context.scheduleAfter(this);
        }
      }

      @Override
      public void cancellableAccept(final int size, final int index, final E element)
          throws Exception {
        elements.add(element);
        this.index = index + 1;
        if (this.index >= maxIndex) {
          final List<E> materialized = decorateFunction.apply(elements);
          setState(new ListToListAsyncMaterializer<E>(materialized));
          consumeElements(materialized);
        } else {
          taskID = getTaskID();
          context.scheduleAfter(this);
        }
      }

      @Override
      public void cancellableComplete(final int size) throws Exception {
        final List<E> materialized = decorateFunction.apply(elements);
        setState(new ListToListAsyncMaterializer<E>(materialized));
        consumeElements(materialized);
      }

      @Override
      public void error(@NotNull final Exception error) {
        final CancellationException exception = cancelException.get();
        if (exception != null) {
          setCancelled(exception);
          consumeError(exception);
        } else {
          setFailed(error);
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
        return wrapped.weightElement();
      }
    }

    private class MaterializingContainsElementAsyncConsumer extends
        CancellableIndexedAsyncConsumer<E> implements Task {

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
      public void cancellableAccept(final int size, final int index, final E element)
          throws Exception {
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
      public void cancellableComplete(final int size) throws Exception {
        wrappedSize = size;
        consumer.accept(false);
      }

      @Override
      public void error(@NotNull final Exception error) throws Exception {
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
    }

    private class MaterializingContainsNullAsyncConsumer extends
        CancellableIndexedAsyncConsumer<E> implements Task {

      private final AsyncConsumer<Boolean> consumer;
      private final int maxIndex = wrappedSize - maxElements - 1;

      private int index;
      private String taskID;

      private MaterializingContainsNullAsyncConsumer(
          @NotNull final AsyncConsumer<Boolean> consumer) {
        this.consumer = consumer;
      }

      @Override
      public void cancellableAccept(final int size, final int index, final E element)
          throws Exception {
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
      public void cancellableComplete(final int size) throws Exception {
        wrappedSize = size;
        consumer.accept(false);
      }

      @Override
      public void error(@NotNull final Exception error) throws Exception {
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
    }

    private class MaterializingEachAsyncConsumer extends
        CancellableIndexedAsyncConsumer<E> implements Task {

      private final IndexedAsyncConsumer<E> consumer;
      private final int maxIndex = wrappedSize - maxElements - 1;

      private int index;
      private String taskID;

      private MaterializingEachAsyncConsumer(@NotNull final IndexedAsyncConsumer<E> consumer) {
        this.consumer = consumer;
      }

      @Override
      public void cancellableAccept(final int size, final int index, final E element)
          throws Exception {
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
      public void cancellableComplete(final int size) throws Exception {
        consumer.complete(maxIndex + 1);
      }

      @Override
      public void error(@NotNull final Exception error) throws Exception {
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
    }
  }
}
