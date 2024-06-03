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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.concurrent.ExecutionContext.Task;
import sparx.internal.future.AsyncConsumer;
import sparx.internal.future.IndexedAsyncConsumer;
import sparx.internal.future.iterator.IteratorAsyncMaterializer;
import sparx.util.function.Function;
import sparx.util.function.IndexedFunction;

public class FlatMapListAsyncMaterializer<E, F> extends AbstractListAsyncMaterializer<F> {

  private static final Logger LOGGER = Logger.getLogger(
      FlatMapListAsyncMaterializer.class.getName());

  public FlatMapListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      @NotNull final IndexedFunction<? super E, ? extends IteratorAsyncMaterializer<F>> mapper,
      @NotNull final ExecutionContext context, @NotNull final AtomicBoolean isCancelled,
      @NotNull final Function<List<F>, List<F>> decorateFunction) {
    super(new AtomicInteger(STATUS_RUNNING));
    setState(new ImmaterialState(wrapped, mapper, context, isCancelled, decorateFunction),
        STATUS_RUNNING);
  }

  @Override
  public int knownSize() {
    return -1;
  }

  private class ImmaterialState implements ListAsyncMaterializer<F> {

    private final ExecutionContext context;
    private final Function<List<F>, List<F>> decorateFunction;
    private final HashMap<Integer, ArrayList<IndexedAsyncConsumer<F>>> elementsConsumers = new HashMap<Integer, ArrayList<IndexedAsyncConsumer<F>>>(
        2);
    private final ArrayList<F> elements = new ArrayList<F>();
    private final AtomicBoolean isCancelled;
    private final IndexedFunction<? super E, ? extends IteratorAsyncMaterializer<F>> mapper;
    private final ListAsyncMaterializer<E> wrapped;

    private IteratorAsyncMaterializer<F> elementsMaterializer;
    private int nextIndex;

    public ImmaterialState(@NotNull final ListAsyncMaterializer<E> wrapped,
        @NotNull final IndexedFunction<? super E, ? extends IteratorAsyncMaterializer<F>> mapper,
        @NotNull final ExecutionContext context, @NotNull final AtomicBoolean isCancelled,
        @NotNull final Function<List<F>, List<F>> decorateFunction) {
      this.wrapped = wrapped;
      this.mapper = mapper;
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
      return -1;
    }

    @Override
    public void materializeCancel(final boolean mayInterruptIfRunning) {
      wrapped.materializeCancel(mayInterruptIfRunning);
      final IteratorAsyncMaterializer<F> elementsMaterializer = this.elementsMaterializer;
      if (elementsMaterializer != null) {
        elementsMaterializer.materializeCancel(mayInterruptIfRunning);
      }
      setState(new CancelledListAsyncMaterializer<F>(), STATUS_CANCELLED);
    }

    @Override
    public void materializeContains(final Object element,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      if (element == null) {
        materializeUntil(0, new MaterializingContainsNullAsyncConsumer(consumer));
      } else {
        materializeUntil(0, new MaterializingContainsElementAsyncConsumer(element, consumer));
      }
    }

    @Override
    public void materializeEach(@NotNull final IndexedAsyncConsumer<F> consumer) {
      materializeUntil(Integer.MAX_VALUE, consumer);
    }

    @Override
    public void materializeElement(final int index,
        @NotNull final IndexedAsyncConsumer<F> consumer) {
      if (index < 0) {
        safeConsumeError(consumer, index, new IndexOutOfBoundsException(Integer.toString(index)),
            LOGGER);
      } else {
        final ArrayList<F> elements = this.elements;
        if (elements.size() > index) {
          safeConsume(consumer, -1, index, elements.get(index), LOGGER);
        } else {
          final int originalIndex = index;
          materializeUntil(index, new IndexedAsyncConsumer<F>() {
            @Override
            public void accept(final int size, final int index, final F element) throws Exception {
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
            public void error(final int index, @NotNull final Exception error) throws Exception {
              consumer.error(index, error);
            }
          });
        }
      }
    }

    @Override
    public void materializeElements(@NotNull final AsyncConsumer<List<F>> consumer) {
      materializeUntil(Integer.MAX_VALUE, new IndexedAsyncConsumer<F>() {
        @Override
        public void accept(final int size, final int index, final F element) {
        }

        @Override
        public void complete(final int size) {
          getState().materializeElements(consumer);
        }

        @Override
        public void error(final int index, @NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
      materializeUntil(0, new IndexedAsyncConsumer<F>() {
        @Override
        public void accept(final int size, final int index, final F element) {
        }

        @Override
        public void complete(final int size) throws Exception {
          consumer.accept(size == 0);
        }

        @Override
        public void error(final int index, @NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
      materializeUntil(Integer.MAX_VALUE, new IndexedAsyncConsumer<F>() {
        @Override
        public void accept(final int size, final int index, final F element) {
        }

        @Override
        public void complete(final int size) throws Exception {
          consumer.accept(size);
        }

        @Override
        public void error(final int index, @NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    private void consumeComplete(final int size) {
      final HashMap<Integer, ArrayList<IndexedAsyncConsumer<F>>> elementsConsumers = this.elementsConsumers;
      for (final ArrayList<IndexedAsyncConsumer<F>> consumers : elementsConsumers.values()) {
        for (final IndexedAsyncConsumer<F> consumer : consumers) {
          safeConsumeComplete(consumer, size, LOGGER);
        }
      }
      elementsConsumers.clear();
    }

    private void consumeElement(final int index, final F element) {
      @SuppressWarnings("unchecked") final HashMap<Integer, ArrayList<IndexedAsyncConsumer<F>>> elementsConsumers = (HashMap<Integer, ArrayList<IndexedAsyncConsumer<F>>>) this.elementsConsumers.clone();
      final HashSet<Integer> keysToRemove = new HashSet<Integer>();
      for (final Entry<Integer, ArrayList<IndexedAsyncConsumer<F>>> entry : elementsConsumers.entrySet()) {
        final int key = entry.getKey();
        if (key < index) {
          final Iterator<IndexedAsyncConsumer<F>> consumers = entry.getValue().iterator();
          while (consumers.hasNext()) {
            if (!safeConsume(consumers.next(), -1, index, element, LOGGER)) {
              consumers.remove();
            }
          }
          if (entry.getValue().isEmpty()) {
            keysToRemove.add(key);
          }
        } else if (key == index) {
          for (final IndexedAsyncConsumer<F> consumer : entry.getValue()) {
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
      final HashMap<Integer, ArrayList<IndexedAsyncConsumer<F>>> elementsConsumers = this.elementsConsumers;
      for (final ArrayList<IndexedAsyncConsumer<F>> consumers : elementsConsumers.values()) {
        for (final IndexedAsyncConsumer<F> consumer : consumers) {
          safeConsumeError(consumer, -1, error, LOGGER);
        }
      }
      elementsConsumers.clear();
    }

    private @NotNull String getTaskID() {
      final String taskID = context.currentTaskID();
      return taskID != null ? taskID : "";
    }

    private void materializeUntil(final int index,
        @NotNull final IndexedAsyncConsumer<F> consumer) {
      final ArrayList<F> elements = this.elements;
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
        final HashMap<Integer, ArrayList<IndexedAsyncConsumer<F>>> elementsConsumers = this.elementsConsumers;
        final boolean needsRun = elementsConsumers.isEmpty();
        ArrayList<IndexedAsyncConsumer<F>> indexConsumers = elementsConsumers.get(index);
        if (indexConsumers == null) {
          elementsConsumers.put(index, indexConsumers = new ArrayList<IndexedAsyncConsumer<F>>(2));
        }
        indexConsumers.add(consumer);
        if (needsRun) {
          if (elementsMaterializer != null) {
            elementsMaterializer.materializeNext(new MaterializingElementAsyncConsumer(index));
          } else {
            wrapped.materializeElement(nextIndex, new MaterializingAsyncConsumer(index));
          }
        }
      }
    }

    private class MaterializingAsyncConsumer implements IndexedAsyncConsumer<E>, Task {

      private final MaterializingElementAsyncConsumer consumer;

      private String taskID;

      private MaterializingAsyncConsumer(final int maxIndex) {
        consumer = new MaterializingElementAsyncConsumer(maxIndex, this);
      }

      private MaterializingAsyncConsumer(
          @NotNull final MaterializingElementAsyncConsumer consumer) {
        this.consumer = consumer;
      }

      @Override
      public void accept(final int size, final int index, final E element) throws Exception {
        final IteratorAsyncMaterializer<F> materializer = mapper.apply(index, element);
        if (materializer.knownSize() == 0) {
          schedule();
        } else {
          (elementsMaterializer = materializer).materializeNext(consumer);
        }
      }

      @Override
      public void complete(final int size) throws Exception {
        final List<F> materialized = decorateFunction.apply(elements);
        setState(new ListToListAsyncMaterializer<F>(materialized), STATUS_DONE);
        consumeComplete(elements.size());
      }

      @Override
      public void error(final int index, @NotNull final Exception error) {
        if (isCancelled.get()) {
          setState(new CancelledListAsyncMaterializer<F>(), STATUS_CANCELLED);
          consumeError(new CancellationException());
        } else {
          setState(new FailedListAsyncMaterializer<F>(error), STATUS_DONE);
          consumeError(error);
        }
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
        return 1;
      }

      private void schedule() {
        ++nextIndex;
        taskID = getTaskID();
        context.scheduleAfter(this);
      }
    }

    private class MaterializingContainsElementAsyncConsumer implements IndexedAsyncConsumer<F>,
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
      public void accept(final int size, final int index, final F element) throws Exception {
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
        consumer.accept(false);
      }

      @Override
      public void error(final int index, @NotNull final Exception error) throws Exception {
        consumer.error(error);
      }

      @Override
      public void run() {
        materializeUntil(index, this);
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

    private class MaterializingContainsNullAsyncConsumer implements IndexedAsyncConsumer<F>, Task {

      private final AsyncConsumer<Boolean> consumer;

      private int index;
      private String taskID;

      private MaterializingContainsNullAsyncConsumer(
          @NotNull final AsyncConsumer<Boolean> consumer) {
        this.consumer = consumer;
      }

      @Override
      public void accept(final int size, final int index, final F element) throws Exception {
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
        consumer.accept(false);
      }

      @Override
      public void error(final int index, @NotNull final Exception error) throws Exception {
        consumer.error(error);
      }

      @Override
      public void run() {
        materializeUntil(index, this);
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

    private class MaterializingElementAsyncConsumer implements IndexedAsyncConsumer<F>, Task {

      private final MaterializingAsyncConsumer consumer;
      private final int maxIndex;

      private String taskID;

      private MaterializingElementAsyncConsumer(final int maxIndex) {
        this.maxIndex = maxIndex;
        consumer = new MaterializingAsyncConsumer(this);
      }

      private MaterializingElementAsyncConsumer(final int maxIndex,
          @NotNull final MaterializingAsyncConsumer consumer) {
        this.maxIndex = maxIndex;
        this.consumer = consumer;
      }

      @Override
      public void accept(final int size, final int index, final F element) {
        final ArrayList<F> elements = ImmaterialState.this.elements;
        final int elementIndex = elements.size();
        elements.add(element);
        consumeElement(elementIndex, element);
        if (elements.size() > maxIndex) {
          consumeComplete(elements.size());
        } else {
          taskID = getTaskID();
          context.scheduleAfter(this);
        }
      }

      @Override
      public void complete(final int size) {
        consumer.schedule();
      }

      @Override
      public void error(final int index, @NotNull final Exception error) {
        if (isCancelled.get()) {
          setState(new CancelledListAsyncMaterializer<F>(), STATUS_CANCELLED);
          consumeError(new CancellationException());
        } else {
          setState(new FailedListAsyncMaterializer<F>(error), STATUS_DONE);
          consumeError(error);
        }
      }

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        return 1;
      }

      @Override
      public void run() {
        elementsMaterializer.materializeNext(this);
      }
    }
  }
}
