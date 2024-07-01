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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.concurrent.ExecutionContext.Task;
import sparx.internal.future.AsyncConsumer;
import sparx.internal.future.IndexedAsyncConsumer;
import sparx.internal.future.iterator.IteratorAsyncMaterializer;
import sparx.util.function.Function;
import sparx.util.function.IndexedFunction;
import sparx.util.function.IndexedPredicate;

public class FlatMapWhereListAsyncMaterializer<E> extends AbstractListAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      FlatMapWhereListAsyncMaterializer.class.getName());

  public FlatMapWhereListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      @NotNull final IndexedPredicate<? super E> predicate,
      @NotNull final IndexedFunction<? super E, ? extends IteratorAsyncMaterializer<E>> mapper,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final Function<List<E>, List<E>> decorateFunction) {
    super(new AtomicInteger(STATUS_RUNNING));
    setState(new ImmaterialState(wrapped, predicate, mapper, context, cancelException,
        decorateFunction));
  }

  @Override
  public int knownSize() {
    return -1;
  }

  private class ImmaterialState implements ListAsyncMaterializer<E> {

    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final Function<List<E>, List<E>> decorateFunction;
    private final ArrayList<E> elements = new ArrayList<E>();
    private final HashMap<Integer, ArrayList<IndexedAsyncConsumer<E>>> elementsConsumers = new HashMap<Integer, ArrayList<IndexedAsyncConsumer<E>>>(
        2);
    private final IndexedFunction<? super E, ? extends IteratorAsyncMaterializer<E>> mapper;
    private final IndexedPredicate<? super E> predicate;
    private final ListAsyncMaterializer<E> wrapped;

    private IteratorAsyncMaterializer<E> elementsMaterializer;
    private int nextIndex;

    public ImmaterialState(@NotNull final ListAsyncMaterializer<E> wrapped,
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends IteratorAsyncMaterializer<E>> mapper,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Function<List<E>, List<E>> decorateFunction) {
      this.wrapped = wrapped;
      this.predicate = predicate;
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
    public boolean isFailed() {
      return false;
    }

    @Override
    public boolean isMaterializedAtOnce() {
      return false;
    }

    @Override
    public int knownSize() {
      return -1;
    }

    @Override
    public void materializeCancel(@NotNull final CancellationException exception) {
      wrapped.materializeCancel(exception);
      final IteratorAsyncMaterializer<E> elementsMaterializer = this.elementsMaterializer;
      if (elementsMaterializer != null) {
        elementsMaterializer.materializeCancel(exception);
      }
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
      materializeUntil(Integer.MAX_VALUE, consumer);
    }

    @Override
    public void materializeElement(final int index,
        @NotNull final IndexedAsyncConsumer<E> consumer) {
      if (index < 0) {
        safeConsumeError(consumer, new IndexOutOfBoundsException(Integer.toString(index)), LOGGER);
      } else {
        final ArrayList<E> elements = this.elements;
        if (elements.size() > index) {
          safeConsume(consumer, -1, index, elements.get(index), LOGGER);
        } else {
          final int originalIndex = index;
          materializeUntil(index, new IndexedAsyncConsumer<E>() {
            @Override
            public void accept(final int size, final int index, final E element) throws Exception {
              if (originalIndex == index) {
                consumer.accept(size, index, element);
              }
            }

            @Override
            public void complete(final int size) throws Exception {
              if (originalIndex >= size) {
                consumer.complete(size);
              }
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
      materializeUntil(Integer.MAX_VALUE, new IndexedAsyncConsumer<E>() {
        @Override
        public void accept(final int size, final int index, final E element) {
        }

        @Override
        public void complete(final int size) {
          getState().materializeElements(consumer);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
      materializeUntil(0, new IndexedAsyncConsumer<E>() {
        @Override
        public void accept(final int size, final int index, final E element) {
        }

        @Override
        public void complete(final int size) throws Exception {
          consumer.accept(size == 0);
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
      } else {
        final ArrayList<E> elements = this.elements;
        if (elements.size() > index) {
          safeConsume(consumer, true, LOGGER);
        } else {
          final int originalIndex = index;
          materializeUntil(index, new IndexedAsyncConsumer<E>() {
            @Override
            public void accept(final int size, final int index, final E element) throws Exception {
              if (originalIndex == index) {
                consumer.accept(true);
              }
            }

            @Override
            public void complete(final int size) throws Exception {
              if (originalIndex >= size) {
                consumer.accept(false);
              }
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
    public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
      materializeUntil(Integer.MAX_VALUE, new IndexedAsyncConsumer<E>() {
        @Override
        public void accept(final int size, final int index, final E element) {
        }

        @Override
        public void complete(final int size) throws Exception {
          consumer.accept(size);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public int weightContains() {
      return weightElements();
    }

    @Override
    public int weightEach() {
      return weightElements();
    }

    @Override
    public int weightElement() {
      return weightElements();
    }

    @Override
    public int weightElements() {
      return elementsConsumers.isEmpty() ? wrapped.weightElement() : 1;
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

    private void consumeComplete(final int size) {
      final HashMap<Integer, ArrayList<IndexedAsyncConsumer<E>>> elementsConsumers = this.elementsConsumers;
      for (final ArrayList<IndexedAsyncConsumer<E>> consumers : elementsConsumers.values()) {
        for (final IndexedAsyncConsumer<E> consumer : consumers) {
          safeConsumeComplete(consumer, size, LOGGER);
        }
      }
      elementsConsumers.clear();
    }

    private void consumeElement(final int index, final E element) {
      @SuppressWarnings("unchecked") final HashMap<Integer, ArrayList<IndexedAsyncConsumer<E>>> elementsConsumers = (HashMap<Integer, ArrayList<IndexedAsyncConsumer<E>>>) this.elementsConsumers.clone();
      final HashSet<Integer> keysToRemove = new HashSet<Integer>();
      for (final Entry<Integer, ArrayList<IndexedAsyncConsumer<E>>> entry : elementsConsumers.entrySet()) {
        final int key = entry.getKey();
        if (key < index) {
          final Iterator<IndexedAsyncConsumer<E>> consumers = entry.getValue().iterator();
          while (consumers.hasNext()) {
            if (!safeConsume(consumers.next(), -1, index, element, LOGGER)) {
              consumers.remove();
            }
          }
          if (entry.getValue().isEmpty()) {
            keysToRemove.add(key);
          }
        } else if (key == index) {
          for (final IndexedAsyncConsumer<E> consumer : entry.getValue()) {
            if (safeConsume(consumer, -1, index, element, LOGGER)) {
              safeConsumeComplete(consumer, index + 1, LOGGER);
            }
          }
          keysToRemove.add(key);
        }
      }
      this.elementsConsumers.keySet().removeAll(keysToRemove);
    }

    private void consumeError(@NotNull final Exception error) {
      final HashMap<Integer, ArrayList<IndexedAsyncConsumer<E>>> elementsConsumers = this.elementsConsumers;
      for (final ArrayList<IndexedAsyncConsumer<E>> consumers : elementsConsumers.values()) {
        for (final IndexedAsyncConsumer<E> consumer : consumers) {
          safeConsumeError(consumer, error, LOGGER);
        }
      }
      elementsConsumers.clear();
    }

    private @NotNull String getTaskID() {
      final String taskID = context.currentTaskID();
      return taskID != null ? taskID : "";
    }

    private void materializeUntil(final int index,
        @NotNull final IndexedAsyncConsumer<E> consumer) {
      final ArrayList<E> elements = this.elements;
      if (elements.size() > index) {
        final int size = index + 1;
        for (int i = 0; i < size; ++i) {
          if (!safeConsume(consumer, -1, i, elements.get(i), LOGGER)) {
            return;
          }
        }
        safeConsumeComplete(consumer, size, LOGGER);
      } else {
        final int size = elements.size();
        for (int i = 0; i < size; ++i) {
          if (!safeConsume(consumer, -1, i, elements.get(i), LOGGER)) {
            return;
          }
        }
        final HashMap<Integer, ArrayList<IndexedAsyncConsumer<E>>> elementsConsumers = this.elementsConsumers;
        final boolean needsRun = elementsConsumers.isEmpty();
        ArrayList<IndexedAsyncConsumer<E>> indexConsumers = elementsConsumers.get(index);
        if (indexConsumers == null) {
          elementsConsumers.put(index, indexConsumers = new ArrayList<IndexedAsyncConsumer<E>>(2));
        }
        indexConsumers.add(consumer);
        if (needsRun) {
          if (elementsMaterializer != null) {
            new MaterializingElementAsyncConsumer().run();
          } else {
            new MaterializingAsyncConsumer().run();
          }
        }
      }
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

    private class MaterializingAsyncConsumer extends CancellableIndexedAsyncConsumer<E> implements
        Task {

      private final MaterializingElementAsyncConsumer consumer;

      private String taskID;

      private MaterializingAsyncConsumer() {
        consumer = new MaterializingElementAsyncConsumer(this);
      }

      private MaterializingAsyncConsumer(
          @NotNull final MaterializingElementAsyncConsumer consumer) {
        this.consumer = consumer;
      }

      @Override
      public void cancellableAccept(final int size, final int index, final E element)
          throws Exception {
        ++nextIndex;
        if (predicate.test(index, element)) {
          final IteratorAsyncMaterializer<E> materializer = mapper.apply(index, element);
          if (materializer.knownSize() == 0) {
            schedule();
          } else {
            (elementsMaterializer = materializer).materializeNext(consumer);
          }
        } else {
          final ArrayList<E> elements = ImmaterialState.this.elements;
          final int elementIndex = elements.size();
          elements.add(element);
          consumeElement(elementIndex, element);
          if (!elementsConsumers.isEmpty()) {
            schedule();
          }
        }
      }

      @Override
      public void cancellableComplete(final int size) throws Exception {
        final List<E> materialized = decorateFunction.apply(elements);
        setState(new ListToListAsyncMaterializer<E>(materialized));
        consumeComplete(elements.size());
      }

      @Override
      public void error(@NotNull final Exception error) {
        setError(error);
      }

      @Override
      public void run() {
        wrapped.materializeElement(nextIndex, this);
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
        if (this.element.equals(element)) {
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
        materializeElement(index, this);
      }

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        return weightElement();
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
        if (element == null) {
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
        materializeElement(index, this);
      }

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        return weightElement();
      }
    }

    private class MaterializingElementAsyncConsumer extends
        CancellableIndexedAsyncConsumer<E> implements Task {

      private final MaterializingAsyncConsumer consumer;

      private String taskID;

      private MaterializingElementAsyncConsumer() {
        consumer = new MaterializingAsyncConsumer(this);
      }

      private MaterializingElementAsyncConsumer(
          @NotNull final MaterializingAsyncConsumer consumer) {
        this.consumer = consumer;
      }

      @Override
      public void cancellableAccept(final int size, final int index, final E element) {
        final ArrayList<E> elements = FlatMapWhereListAsyncMaterializer.ImmaterialState.this.elements;
        final int elementIndex = elements.size();
        elements.add(element);
        consumeElement(elementIndex, element);
        if (!elementsConsumers.isEmpty()) {
          taskID = getTaskID();
          context.scheduleAfter(this);
        }
      }

      @Override
      public void cancellableComplete(final int size) {
        consumer.schedule();
      }

      @Override
      public void error(@NotNull final Exception error) {
        setError(error);
      }

      @Override
      public void run() {
        elementsMaterializer.materializeNext(this);
      }

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        return elementsMaterializer.weightNext();
      }
    }
  }
}
