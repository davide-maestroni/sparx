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
import sparx.util.function.IndexedFunction;
import sparx.util.function.TernaryFunction;

public class MapAfterListAsyncMaterializer<E> extends AbstractListAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      MapAfterListAsyncMaterializer.class.getName());
  private static final Object MISSING = new Object();

  private final int knownSize;

  // numElements: not negative
  public MapAfterListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      final int numElements, @NotNull final IndexedFunction<? super E, ? extends E> mapper,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final TernaryFunction<List<E>, Integer, E, List<E>> replaceFunction) {
    this(wrapped, numElements, mapper, new AtomicInteger(STATUS_RUNNING), context, cancelException,
        replaceFunction);
  }

  MapAfterListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      final int numElements, @NotNull final IndexedFunction<? super E, ? extends E> mapper,
      @NotNull final AtomicInteger status, @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final TernaryFunction<List<E>, Integer, E, List<E>> replaceFunction) {
    super(status);
    setState(new ImmaterialState(wrapped, numElements, mapper, context, cancelException,
        replaceFunction));
    knownSize = wrapped.knownSize();
  }

  @Override
  public int knownSize() {
    return knownSize;
  }

  private class ImmaterialState implements ListAsyncMaterializer<E> {

    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = new ArrayList<AsyncConsumer<List<E>>>(
        2);
    private final IndexedFunction<? super E, ? extends E> mapper;
    private final int numElements;
    private final TernaryFunction<List<E>, Integer, E, List<E>> replaceFunction;
    private final ListAsyncMaterializer<E> wrapped;

    private Object mappedElement = MISSING;
    private int wrappedSize = knownSize;

    public ImmaterialState(@NotNull final ListAsyncMaterializer<E> wrapped, final int numElements,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final TernaryFunction<List<E>, Integer, E, List<E>> replaceFunction) {
      this.wrapped = wrapped;
      this.numElements = numElements;
      this.mapper = mapper;
      this.context = context;
      this.cancelException = cancelException;
      this.replaceFunction = replaceFunction;
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
      if (element == null) {
        new MaterializingContainsNullAsyncConsumer(consumer).run();
      } else {
        new MaterializingContainsElementAsyncConsumer(element, consumer).run();
      }
    }

    @Override
    public void materializeDone(@NotNull final AsyncConsumer<List<E>> consumer) {
      safeConsumeError(consumer, new UnsupportedOperationException(), LOGGER);
    }

    @Override
    public void materializeEach(@NotNull final IndexedAsyncConsumer<E> consumer) {
      new MaterializingEachAsyncConsumer(consumer).run();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void materializeElement(final int index,
        @NotNull final IndexedAsyncConsumer<E> consumer) {
      if (index < 0) {
        safeConsumeError(consumer, new IndexOutOfBoundsException(Integer.toString(index)), LOGGER);
      } else if (index == numElements) {
        if (mappedElement != MISSING) {
          safeConsume(consumer, wrappedSize, index, (E) mappedElement, LOGGER);
        } else if (wrappedSize >= 0 && numElements >= wrappedSize) {
          wrapped.materializeElement(index, new CancellableIndexedAsyncConsumer<E>() {
            @Override
            public void cancellableAccept(final int size, final int index, final E element)
                throws Exception {
              wrappedSize = Math.max(wrappedSize, size);
              consumer.accept(size, index, element);
            }

            @Override
            public void cancellableComplete(final int size) throws Exception {
              wrappedSize = size;
              consumer.complete(size);
            }

            @Override
            public void error(@NotNull final Exception error) throws Exception {
              consumer.error(error);
            }
          });
        } else {
          wrapped.materializeElement(index, new CancellableIndexedAsyncConsumer<E>() {
            @Override
            public void cancellableAccept(final int size, final int index, final E element)
                throws Exception {
              wrappedSize = Math.max(wrappedSize, size);
              consumer.accept(size, index, getMapped(element));
            }

            @Override
            public void cancellableComplete(final int size) throws Exception {
              wrappedSize = size;
              consumer.complete(size);
            }

            @Override
            public void error(@NotNull final Exception error) throws Exception {
              consumer.error(error);
            }
          });
        }
      } else {
        wrapped.materializeElement(index, new CancellableIndexedAsyncConsumer<E>() {
          @Override
          public void cancellableAccept(final int size, final int index, final E element)
              throws Exception {
            consumer.accept(wrappedSize = Math.max(wrappedSize, size), index, element);
          }

          @Override
          public void cancellableComplete(final int size) throws Exception {
            wrappedSize = size;
            consumer.complete(size);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
    }

    @Override
    public void materializeElements(@NotNull final AsyncConsumer<List<E>> consumer) {
      final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      elementsConsumers.add(consumer);
      if (elementsConsumers.size() == 1) {
        wrapped.materializeElements(new CancellableAsyncConsumer<List<E>>() {
          @Override
          public void cancellableAccept(final List<E> elements) throws Exception {
            if (numElements < elements.size()) {
              final List<E> materialized = replaceFunction.apply(elements, numElements,
                  getMapped(elements.get(numElements)));
              setState(new ListToListAsyncMaterializer<E>(materialized));
              consumeElements(materialized);
            } else {
              setState(new ListToListAsyncMaterializer<E>(elements));
              consumeElements(elements);
            }
          }

          @Override
          public void error(@NotNull final Exception error) {
            setError(error);
            consumeError(error);
          }
        });
      }
    }

    @Override
    public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
      if (wrappedSize == 0) {
        safeConsume(consumer, true, LOGGER);
      } else {
        wrapped.materializeEmpty(new CancellableAsyncConsumer<Boolean>() {
          @Override
          public void cancellableAccept(final Boolean empty) throws Exception {
            consumer.accept(empty);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
    }

    @Override
    public void materializeHasElement(final int index,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      if (index < 0) {
        safeConsume(consumer, false, LOGGER);
      } else if (index < wrappedSize) {
        safeConsume(consumer, true, LOGGER);
      } else {
        wrapped.materializeHasElement(index, new CancellableAsyncConsumer<Boolean>() {
          @Override
          public void cancellableAccept(final Boolean hasElement) throws Exception {
            consumer.accept(hasElement);
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
        safeConsume(consumer, wrappedSize, LOGGER);
      } else {
        wrapped.materializeSize(new CancellableAsyncConsumer<Integer>() {
          @Override
          public void cancellableAccept(final Integer size) throws Exception {
            wrappedSize = size;
            consumer.accept(size);
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
      return wrapped.weightElement();
    }

    @Override
    public int weightEach() {
      return wrapped.weightElement();
    }

    @Override
    public int weightElement() {
      return wrapped.weightElement();
    }

    @Override
    public int weightElements() {
      return elementsConsumers.isEmpty() ? wrapped.weightElements() : 1;
    }

    @Override
    public int weightEmpty() {
      return wrappedSize < 0 ? wrapped.weightEmpty() : 1;
    }

    @Override
    public int weightHasElement() {
      return wrappedSize < 0 ? wrapped.weightHasElement() : 1;
    }

    @Override
    public int weightSize() {
      return wrappedSize < 0 ? wrapped.weightSize() : 1;
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

    @SuppressWarnings("unchecked")
    private E getMapped(final E element) throws Exception {
      if (mappedElement == MISSING) {
        mappedElement = mapper.apply(numElements, element);
      }
      return (E) mappedElement;
    }

    private @NotNull String getTaskID() {
      final String taskID = context.currentTaskID();
      return taskID != null ? taskID : "";
    }

    private void setError(@NotNull final Exception error) {
      final CancellationException exception = cancelException.get();
      if (exception != null) {
        setCancelled(exception);
        consumeError(exception);
      } else {
        setFailed(error);
        consumeError(error);
      }
    }

    private class MaterializingContainsElementAsyncConsumer extends
        CancellableIndexedAsyncConsumer<E> implements Task {

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
      public void cancellableAccept(final int size, final int index, final E element)
          throws Exception {
        if (index == numElements ? this.element.equals(getMapped(element))
            : this.element.equals(element)) {
          consumer.accept(true);
        } else {
          this.index = index + 1;
          taskID = getTaskID();
          context.scheduleAfter(this);
        }
      }

      @Override
      public void cancellableComplete(final int size) throws Exception {
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

      private int index;
      private String taskID;

      private MaterializingContainsNullAsyncConsumer(
          @NotNull final AsyncConsumer<Boolean> consumer) {
        this.consumer = consumer;
      }

      @Override
      public void cancellableAccept(final int size, final int index, final E element)
          throws Exception {
        if (index == numElements ? getMapped(element) == null : element == null) {
          consumer.accept(true);
        } else {
          this.index = index + 1;
          taskID = getTaskID();
          context.scheduleAfter(this);
        }
      }

      @Override
      public void cancellableComplete(final int size) throws Exception {
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

      private int index;
      private String taskID;

      private MaterializingEachAsyncConsumer(@NotNull final IndexedAsyncConsumer<E> consumer) {
        this.consumer = consumer;
      }

      @Override
      public void cancellableAccept(final int size, final int index, final E element)
          throws Exception {
        wrappedSize = Math.max(wrappedSize, size);
        if (index == numElements) {
          consumer.accept(wrappedSize, index, getMapped(element));
        } else {
          consumer.accept(wrappedSize, index, element);
        }
        this.index = index + 1;
        taskID = getTaskID();
        context.scheduleAfter(this);
      }

      @Override
      public void cancellableComplete(final int size) throws Exception {
        wrappedSize = size;
        consumer.complete(size);
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
