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
import sparx.util.function.Function;

public class SymmetricDiffListAsyncMaterializer<E> extends AbstractListAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      SymmetricDiffListAsyncMaterializer.class.getName());

  public SymmetricDiffListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      @NotNull final ListAsyncMaterializer<?> elementsMaterializer,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final Function<List<E>, List<E>> decorateFunction) {
    super(new AtomicInteger(STATUS_RUNNING));
    setState(new ImmaterialState(wrapped, elementsMaterializer, context, cancelException,
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
    private final ListAsyncMaterializer<?> elementsMaterializer;
    private final ListAsyncMaterializer<E> wrapped;

    private HashMap<Object, Integer> elementsBag;
    private boolean isWrapped = true;
    private int nextIndex;

    public ImmaterialState(@NotNull final ListAsyncMaterializer<E> wrapped,
        @NotNull final ListAsyncMaterializer<?> elementsMaterializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Function<List<E>, List<E>> decorateFunction) {
      this.wrapped = wrapped;
      this.elementsMaterializer = elementsMaterializer;
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
      elementsMaterializer.materializeCancel(exception);
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
      materializeUntil(Integer.MAX_VALUE, false, consumer);
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
          materializeUntil(index, true, new IndexedAsyncConsumer<E>() {
            private E lastElement;
            private int lastIndex = -1;

            @Override
            public void accept(final int size, final int index, final E element) {
              lastIndex = index;
              lastElement = element;
            }

            @Override
            public void complete(final int size) throws Exception {
              if (lastIndex < index) {
                consumer.complete(size);
              } else {
                consumer.accept(-1, lastIndex, lastElement);
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
      materializeUntil(Integer.MAX_VALUE, true, new IndexedAsyncConsumer<E>() {
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
      materializeUntil(0, true, new IndexedAsyncConsumer<E>() {
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
          materializeUntil(index, true, new IndexedAsyncConsumer<E>() {
            private int lastIndex = -1;

            @Override
            public void accept(final int size, final int index, final E element) {
              lastIndex = index;
            }

            @Override
            public void complete(final int size) throws Exception {
              consumer.accept(lastIndex == index);
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
      materializeUntil(Integer.MAX_VALUE, true, new IndexedAsyncConsumer<E>() {
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
      if (elementsConsumers.isEmpty()) {
        if (elementsBag == null) {
          return (int) Math.min(Integer.MAX_VALUE,
              (long) wrapped.weightElement() + elementsMaterializer.weightElements());
        }
        return wrapped.weightElement();
      }
      return 1;
    }

    @Override
    public int weightHasElement() {
      return weightElements();
    }

    @Override
    public int weightEmpty() {
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
        if (index < key) {
          final Iterator<IndexedAsyncConsumer<E>> consumers = entry.getValue().iterator();
          while (consumers.hasNext()) {
            if (!safeConsume(consumers.next(), -1, index, element, LOGGER)) {
              consumers.remove();
            }
          }
          if (entry.getValue().isEmpty()) {
            keysToRemove.add(key);
          }
        } else if (index == key) {
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

    @SuppressWarnings("unchecked")
    private void materializeUntil(final int index, final boolean skipPrevious,
        @NotNull final IndexedAsyncConsumer<E> consumer) {
      final ArrayList<E> elements = this.elements;
      if (elements.size() > index) {
        final int size = index + 1;
        if (skipPrevious) {
          if (!safeConsume(consumer, -1, index, elements.get(index), LOGGER)) {
            return;
          }
        } else {
          for (int i = 0; i < size; ++i) {
            if (!safeConsume(consumer, -1, i, elements.get(i), LOGGER)) {
              return;
            }
          }
        }
        safeConsumeComplete(consumer, size, LOGGER);
      } else {
        final int size = elements.size();
        if (skipPrevious) {
          final int lastIndex = size - 1;
          if (lastIndex >= 0 && !safeConsume(consumer, -1, lastIndex, elements.get(lastIndex),
              LOGGER)) {
            return;
          }
        } else {
          for (int i = 0; i < size; ++i) {
            if (!safeConsume(consumer, -1, i, elements.get(i), LOGGER)) {
              return;
            }
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
          if (elementsBag == null) {
            ((ListAsyncMaterializer<Object>) elementsMaterializer).materializeElements(
                new CancellableAsyncConsumer<List<Object>>() {
                  @Override
                  public void cancellableAccept(final List<Object> elements) {
                    final HashMap<Object, Integer> bag = elementsBag = new HashMap<Object, Integer>();
                    for (final Object element : elements) {
                      final Integer count = bag.get(element);
                      if (count == null) {
                        bag.put(element, 1);
                      } else {
                        bag.put(element, count + 1);
                      }
                    }
                    new MaterializingAsyncConsumer().run();
                  }

                  @Override
                  public void error(@NotNull final Exception error) {
                    consumeError(error);
                  }
                });
          } else {
            new MaterializingAsyncConsumer().run();
          }
        }
      }
    }

    private class MaterializingAsyncConsumer extends CancellableIndexedAsyncConsumer<E> implements
        Task {

      private String taskID;

      @Override
      public void cancellableAccept(final int size, final int index, final E element) {
        nextIndex = index + 1;
        final HashMap<Object, Integer> elementsBag = SymmetricDiffListAsyncMaterializer.ImmaterialState.this.elementsBag;
        final Integer count = elementsBag.get(element);
        if (isWrapped) {
          if (count == null) {
            final int wrappedIndex = elements.size();
            elements.add(element);
            consumeElement(wrappedIndex, element);
          } else {
            final int decCount = count - 1;
            if (decCount == 0) {
              elementsBag.remove(element);
            } else {
              elementsBag.put(element, decCount);
            }
          }
        } else if (count != null) {
          final int wrappedIndex = elements.size();
          elements.add(element);
          consumeElement(wrappedIndex, element);
          final int decCount = count - 1;
          if (decCount == 0) {
            elementsBag.remove(element);
          } else {
            elementsBag.put(element, decCount);
          }
        }
        if (!elementsConsumers.isEmpty()) {
          taskID = getTaskID();
          context.scheduleAfter(this);
        }
      }

      @Override
      public void cancellableComplete(final int size) throws Exception {
        if (isWrapped) {
          nextIndex = 0;
          isWrapped = false;
          taskID = getTaskID();
          context.scheduleAfter(this);
        } else {
          final List<E> materialized = decorateFunction.apply(elements);
          setState(new ListToListAsyncMaterializer<E>(materialized));
          consumeComplete(elements.size());
        }
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
      @SuppressWarnings("unchecked")
      public void run() {
        if (isWrapped) {
          wrapped.materializeElement(nextIndex, this);
        } else {
          ((ListAsyncMaterializer<E>) elementsMaterializer).materializeElement(nextIndex, this);
        }
      }

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        return isWrapped ? wrapped.weightElement() : elementsMaterializer.weightElement();
      }
    }

    private class MaterializingContainsElementAsyncConsumer extends
        CancellableIndexedAsyncConsumer<E> implements Task {

      private final AsyncConsumer<Boolean> consumer;
      private final Object element;

      private int index;
      private E lastElement;
      private int lastIndex = -1;
      private String taskID;

      private MaterializingContainsElementAsyncConsumer(@NotNull final Object element,
          @NotNull final AsyncConsumer<Boolean> consumer) {
        this.element = element;
        this.consumer = consumer;
      }

      @Override
      public void cancellableAccept(final int size, final int index, final E element) {
        lastIndex = index;
        lastElement = element;
      }

      @Override
      public void cancellableComplete(final int size) throws Exception {
        if (lastIndex < index) {
          consumer.accept(false);
        } else if (element.equals(lastElement)) {
          consumer.accept(true);
        } else {
          ++index;
          taskID = getTaskID();
          context.scheduleAfter(this);
        }
      }

      @Override
      public void error(@NotNull final Exception error) throws Exception {
        consumer.error(error);
      }

      @Override
      public void run() {
        materializeUntil(index, true, this);
      }

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        return weightElements();
      }
    }

    private class MaterializingContainsNullAsyncConsumer extends
        CancellableIndexedAsyncConsumer<E> implements Task {

      private final AsyncConsumer<Boolean> consumer;

      private int index;
      private E lastElement;
      private int lastIndex = -1;
      private String taskID;

      private MaterializingContainsNullAsyncConsumer(
          @NotNull final AsyncConsumer<Boolean> consumer) {
        this.consumer = consumer;
      }

      @Override
      public void cancellableAccept(final int size, final int index, final E element) {
        lastIndex = index;
        lastElement = element;
      }

      @Override
      public void cancellableComplete(final int size) throws Exception {
        if (lastIndex < index) {
          consumer.accept(false);
        } else if (lastElement == null) {
          consumer.accept(true);
        } else {
          ++index;
          taskID = getTaskID();
          context.scheduleAfter(this);
        }
      }

      @Override
      public void error(@NotNull final Exception error) throws Exception {
        consumer.error(error);
      }

      @Override
      public void run() {
        materializeUntil(index, true, this);
      }

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        return weightElements();
      }
    }
  }
}
