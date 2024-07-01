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
    private final ElementsCache<F> elements = new ElementsCache<F>(knownSize);
    private final ArrayList<AsyncConsumer<List<F>>> elementsConsumers = new ArrayList<AsyncConsumer<List<F>>>(
        2);
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
      wrapped.materializeElement(i, new MaterializingEachAsyncConsumer(consumer));
    }

    @Override
    public void materializeElement(final int index,
        @NotNull final IndexedAsyncConsumer<F> consumer) {
      if (index < 0) {
        safeConsumeError(consumer, new IndexOutOfBoundsException(Integer.toString(index)), LOGGER);
      } else {
        final ElementsCache<F> elements = this.elements;
        if (elements.has(index)) {
          safeConsume(consumer, knownSize, index, elements.get(index), LOGGER);
        } else {
          wrapped.materializeElement(index, new CancellableIndexedAsyncConsumer<E>() {
            @Override
            public void cancellableAccept(final int size, final int index, final E element)
                throws Exception {
              final ElementsCache<F> elements = ImmaterialState.this.elements;
              elements.setSize(size);
              final F mapped;
              if (!elements.has(index)) {
                try {
                  mapped = mapper.apply(index, element);
                } catch (final Exception e) {
                  setError(e);
                  throw e;
                }
                elements.set(index, mapped);
              } else {
                mapped = elements.get(index);
              }
              consumer.accept(size, index, mapped);
            }

            @Override
            public void cancellableComplete(final int size) throws Exception {
              elements.setSize(size);
              consumer.complete(size);
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
    public void materializeElements(@NotNull final AsyncConsumer<List<F>> consumer) {
      final ArrayList<AsyncConsumer<List<F>>> elementsConsumers = this.elementsConsumers;
      elementsConsumers.add(consumer);
      if (elementsConsumers.size() == 1) {
        final ElementsCache<F> elements = this.elements;
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
      return elementsConsumers.isEmpty() ? wrapped.weightElement() : 1;
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

    private void consumeElements(@NotNull final List<F> elements) {
      final ArrayList<AsyncConsumer<List<F>>> elementsConsumers = this.elementsConsumers;
      for (final AsyncConsumer<List<F>> elementsConsumer : elementsConsumers) {
        safeConsume(elementsConsumer, elements, LOGGER);
      }
      elementsConsumers.clear();
    }

    private void consumeError(@NotNull final Exception error) {
      final ArrayList<AsyncConsumer<List<F>>> elementsConsumers = this.elementsConsumers;
      for (final AsyncConsumer<List<F>> elementsConsumer : elementsConsumers) {
        safeConsumeError(elementsConsumer, error, LOGGER);
      }
      elementsConsumers.clear();
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

    private class MaterializingAsyncConsumer extends CancellableIndexedAsyncConsumer<E> implements
        Task {

      private int index;
      private String taskID;

      @Override
      public void cancellableAccept(final int size, int index, final E element) throws Exception {
        final ElementsCache<F> elements = ImmaterialState.this.elements;
        elements.setSize(size);
        if (!elements.has(index)) {
          final F mapped = mapper.apply(index, element);
          elements.set(index, mapped);
        }
        do {
          ++index;
        } while (elements.has(index));
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
        consumeElements(materialized);
      }

      @Override
      public void error(@NotNull final Exception error) {
        setError(error);
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
          try {
            mapped = mapper.apply(index, element);
          } catch (final Exception e) {
            setError(e);
            throw e;
          }
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
          try {
            mapped = mapper.apply(index, element);
          } catch (final Exception e) {
            setError(e);
            throw e;
          }
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

      private final IndexedAsyncConsumer<F> consumer;

      private int index;
      private String taskID;

      private MaterializingEachAsyncConsumer(@NotNull final IndexedAsyncConsumer<F> consumer) {
        this.consumer = consumer;
      }

      @Override
      public void cancellableAccept(final int size, int index, final E element) throws Exception {
        final ElementsCache<F> elements = ImmaterialState.this.elements;
        elements.setSize(size);
        final F mapped;
        if (!elements.has(index)) {
          try {
            mapped = mapper.apply(index, element);
          } catch (final Exception e) {
            setError(e);
            throw e;
          }
          elements.set(index, mapped);
        } else {
          mapped = elements.get(index);
        }
        final IndexedAsyncConsumer<F> consumer = this.consumer;
        consumer.accept(size, index, mapped);
        while (elements.has(++index)) {
          consumer.accept(size, index, elements.get(index));
        }
        if (!elementsConsumers.isEmpty()) {
          this.index = index;
          taskID = getTaskID();
          context.scheduleAfter(this);
        }
      }

      @Override
      public void cancellableComplete(final int size) throws Exception {
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
