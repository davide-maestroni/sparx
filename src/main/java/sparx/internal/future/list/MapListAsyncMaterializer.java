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
import sparx.internal.util.ElementsCache;
import sparx.util.function.Function;
import sparx.util.function.IndexedFunction;

public class MapListAsyncMaterializer<E, F> extends AbstractListAsyncMaterializer<F> {

  private static final Logger LOGGER = Logger.getLogger(MapListAsyncMaterializer.class.getName());

  private final int knownSize;

  public MapListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      @NotNull final IndexedFunction<? super E, ? extends F> mapper,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final Function<List<F>, List<F>> decorateFunction) {
    super(new AtomicInteger(STATUS_RUNNING));
    knownSize = wrapped.knownSize();
    setState(new ImmaterialState(wrapped, mapper, context, cancelException, decorateFunction));
  }

  @Override
  public int knownSize() {
    return knownSize;
  }

  private class ImmaterialState implements ListAsyncMaterializer<F> {

    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final Function<List<F>, List<F>> decorateFunction;
    private final HashMap<Integer, ArrayList<IndexedAsyncConsumer<F>>> elementsConsumers = new HashMap<Integer, ArrayList<IndexedAsyncConsumer<F>>>(
        2);
    private final ElementsCache<F> elements = new ElementsCache<F>(knownSize);
    private final IndexedFunction<? super E, ? extends F> mapper;
    private final ListAsyncMaterializer<E> wrapped;

    public ImmaterialState(@NotNull final ListAsyncMaterializer<E> wrapped,
        @NotNull final IndexedFunction<? super E, ? extends F> mapper,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Function<List<F>, List<F>> decorateFunction) {
      this.wrapped = wrapped;
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
      setCancelled(exception);
      consumeError(exception);
    }

    @Override
    public void materializeContains(final Object element,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      final ElementsCache<F> elements = this.elements;
      if (element == null) {
        int i = 0;
        while (elements.has(i)) {
          if (elements.get(i) == null) {
            safeConsume(consumer, true, LOGGER);
            return;
          }
          ++i;
        }
        wrapped.materializeElement(i, new MaterializingContainsNullAsyncConsumer(consumer));
      } else {
        int i = 0;
        while (elements.has(i)) {
          if (element.equals(elements.get(i))) {
            safeConsume(consumer, true, LOGGER);
            return;
          }
          ++i;
        }
        wrapped.materializeElement(i,
            new MaterializingContainsElementAsyncConsumer(element, consumer));
      }
    }

    @Override
    public void materializeDone(@NotNull final AsyncConsumer<List<F>> consumer) {
      safeConsumeError(consumer, new UnsupportedOperationException(), LOGGER);
    }

    @Override
    public void materializeEach(@NotNull final IndexedAsyncConsumer<F> consumer) {
      final ElementsCache<F> elements = this.elements;
      int i = 0;
      while (elements.has(i)) {
        if (!safeConsume(consumer, knownSize, i, elements.get(i), LOGGER)) {
          return;
        }
        ++i;
      }
      final HashMap<Integer, ArrayList<IndexedAsyncConsumer<F>>> elementsConsumers = this.elementsConsumers;
      final boolean needsRun = elementsConsumers.isEmpty();
      ArrayList<IndexedAsyncConsumer<F>> indexConsumers = elementsConsumers.get(Integer.MAX_VALUE);
      if (indexConsumers == null) {
        elementsConsumers.put(Integer.MAX_VALUE,
            indexConsumers = new ArrayList<IndexedAsyncConsumer<F>>(2));
      }
      indexConsumers.add(consumer);
      if (needsRun) {
        wrapped.materializeElement(i, new MaterializingAsyncConsumer());
      }
    }

    @Override
    public void materializeElement(final int index,
        @NotNull final IndexedAsyncConsumer<F> consumer) {
      if (index < 0) {
        safeConsumeError(consumer, new IndexOutOfBoundsException(Integer.toString(index)), LOGGER);
      } else {
        final ElementsCache<F> elements = this.elements;
        if (elements.has(index)) {
          safeConsume(consumer, -1, index, elements.get(index), LOGGER);
        } else {
          final HashMap<Integer, ArrayList<IndexedAsyncConsumer<F>>> elementsConsumers = this.elementsConsumers;
          final boolean needsRun = elementsConsumers.isEmpty();
          ArrayList<IndexedAsyncConsumer<F>> indexConsumers = elementsConsumers.get(index);
          if (indexConsumers == null) {
            elementsConsumers.put(index,
                indexConsumers = new ArrayList<IndexedAsyncConsumer<F>>(2));
          }
          final int originalIndex = index;
          indexConsumers.add(new IndexedAsyncConsumer<F>() {
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
            public void error(@NotNull final Exception error) throws Exception {
              consumer.error(error);
            }
          });
          if (needsRun) {
            wrapped.materializeElement(index, new MaterializingAsyncConsumer());
          }
        }
      }
    }

    @Override
    public void materializeElements(@NotNull final AsyncConsumer<List<F>> consumer) {
      final HashMap<Integer, ArrayList<IndexedAsyncConsumer<F>>> elementsConsumers = this.elementsConsumers;
      final boolean needsRun = elementsConsumers.isEmpty();
      ArrayList<IndexedAsyncConsumer<F>> indexConsumers = elementsConsumers.get(Integer.MAX_VALUE);
      if (indexConsumers == null) {
        elementsConsumers.put(Integer.MAX_VALUE,
            indexConsumers = new ArrayList<IndexedAsyncConsumer<F>>(2));
      }
      indexConsumers.add(new IndexedAsyncConsumer<F>() {
        @Override
        public void accept(final int size, final int index, final F element) {
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
      if (needsRun) {
        int i = 0;
        while (elements.has(i)) {
          ++i;
        }
        wrapped.materializeElement(i, new MaterializingAsyncConsumer());
      }
    }

    @Override
    public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
      if (elements.count() > 0) {
        safeConsume(consumer, false, LOGGER);
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
      wrapped.materializeSize(new CancellableAsyncConsumer<Integer>() {
        @Override
        public void cancellableAccept(final Integer size) throws Exception {
          elements.setSize(size);
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
      return weightElement();
    }

    @Override
    public int weightElement() {
      return wrapped.weightElement();
    }

    @Override
    public int weightElements() {
      return weightElement();
    }

    @Override
    public int weightEmpty() {
      return wrapped.weightEmpty();
    }

    @Override
    public int weightHasElement() {
      return wrapped.weightHasElement();
    }

    @Override
    public int weightSize() {
      return wrapped.weightSize();
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

    private void consumeElement(final int size, final int index, final F element) {
      @SuppressWarnings("unchecked") final HashMap<Integer, ArrayList<IndexedAsyncConsumer<F>>> elementsConsumers = (HashMap<Integer, ArrayList<IndexedAsyncConsumer<F>>>) this.elementsConsumers.clone();
      final HashSet<Integer> keysToRemove = new HashSet<Integer>();
      for (final Entry<Integer, ArrayList<IndexedAsyncConsumer<F>>> entry : elementsConsumers.entrySet()) {
        final int key = entry.getKey();
        if (key < index) {
          final Iterator<IndexedAsyncConsumer<F>> consumers = entry.getValue().iterator();
          while (consumers.hasNext()) {
            if (!safeConsume(consumers.next(), size, index, element, LOGGER)) {
              consumers.remove();
            }
          }
          if (entry.getValue().isEmpty()) {
            keysToRemove.add(key);
          }
        } else if (key == index) {
          for (final IndexedAsyncConsumer<F> consumer : entry.getValue()) {
            if (safeConsume(consumer, size, index, element, LOGGER)) {
              safeConsumeComplete(consumer, index + 1, LOGGER);
            }
          }
          keysToRemove.add(key);
        }
      }
      elementsConsumers.keySet().removeAll(keysToRemove);
    }

    private void consumeError(@NotNull final Exception error) {
      final HashMap<Integer, ArrayList<IndexedAsyncConsumer<F>>> elementsConsumers = this.elementsConsumers;
      for (final ArrayList<IndexedAsyncConsumer<F>> consumers : elementsConsumers.values()) {
        for (final IndexedAsyncConsumer<F> consumer : consumers) {
          safeConsumeError(consumer, error, LOGGER);
        }
      }
      elementsConsumers.clear();
    }

    private @NotNull String getTaskID() {
      final String taskID = context.currentTaskID();
      return taskID != null ? taskID : "";
    }

    private class MaterializingAsyncConsumer extends CancellableIndexedAsyncConsumer<E> implements
        Task {

      private int index;
      private String taskID;

      @Override
      public void cancellableAccept(final int size, int index, final E element) throws Exception {
        final ElementsCache<F> elements = ImmaterialState.this.elements;
        elements.setSize(size);
        final F mapped;
        if (!elements.has(index)) {
          mapped = mapper.apply(index, element);
          elements.set(index, mapped);
        } else {
          mapped = elements.get(index);
        }
        consumeElement(size, index, mapped);
        while (elements.has(++index)) {
          consumeElement(size, index, elements.get(index));
        }
        if (!elementsConsumers.isEmpty()) {
          this.index = index;
          taskID = getTaskID();
          context.scheduleAfter(this);
        }
      }

      @Override
      public void cancellableComplete(final int size) throws Exception {
        final List<F> materialized = decorateFunction.apply(elements.toList());
        setState(new ListToListAsyncMaterializer<F>(materialized));
        consumeComplete(size);
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
        final int weight = wrapped.weightElement();
        return weight == Integer.MAX_VALUE ? weight : weight + 1;
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
      public void cancellableAccept(final int size, int index, final E element) throws Exception {
        final ElementsCache<F> elements = ImmaterialState.this.elements;
        elements.setSize(size);
        final F mapped;
        if (!elements.has(index)) {
          mapped = mapper.apply(index, element);
          elements.set(index, mapped);
        } else {
          mapped = elements.get(index);
        }
        if (this.element.equals(mapped)) {
          consumer.accept(true);
        } else {
          while (elements.has(++index)) {
            if (this.element.equals(elements.get(index))) {
              consumer.accept(true);
              return;
            }
          }
          this.index = index;
          taskID = getTaskID();
          context.scheduleAfter(this);
        }
      }

      @Override
      public void cancellableComplete(final int size) throws Exception {
        elements.setSize(size);
        consumer.accept(false);
      }

      @Override
      public void error(@NotNull final Exception error) throws Exception {
        final CancellationException exception = cancelException.get();
        if (exception != null) {
          setCancelled(exception);
          consumeError(exception);
        } else {
          setFailed(error);
          consumeError(error);
        }
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
      public void cancellableAccept(final int size, int index, final E element) throws Exception {
        final ElementsCache<F> elements = ImmaterialState.this.elements;
        elements.setSize(size);
        final F mapped;
        if (!elements.has(index)) {
          mapped = mapper.apply(index, element);
          elements.set(index, mapped);
        } else {
          mapped = elements.get(index);
        }
        if (mapped == null) {
          consumer.accept(true);
        } else {
          while (elements.has(++index)) {
            if (elements.get(index) == null) {
              consumer.accept(true);
              return;
            }
          }
          this.index = index;
          taskID = getTaskID();
          context.scheduleAfter(this);
        }
      }

      @Override
      public void cancellableComplete(final int size) throws Exception {
        elements.setSize(size);
        consumer.accept(false);
      }

      @Override
      public void error(@NotNull final Exception error) throws Exception {
        final CancellationException exception = cancelException.get();
        if (exception != null) {
          setCancelled(exception);
          consumeError(exception);
        } else {
          setFailed(error);
          consumeError(error);
        }
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
