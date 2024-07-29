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
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.AsyncConsumer;
import sparx.internal.future.IndexedAsyncConsumer;
import sparx.internal.future.IndexedAsyncPredicate;
import sparx.util.function.Function;

public class IntersectListAsyncMaterializer<E> extends AbstractListAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      IntersectListAsyncMaterializer.class.getName());

  public IntersectListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
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
    @SuppressWarnings("SuspiciousMethodCalls")
    public void materializeContains(final Object element,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      if (element == null) {
        if (elements.contains(null)) {
          safeConsume(consumer, true, LOGGER);
        } else {
          materializeUntil(elements.size(), new IndexedAsyncConsumer<E>() {
            @Override
            public void accept(final int size, final int index, final E element) throws Exception {
              if (element == null) {
                consumer.accept(true);
              } else {
                int i = index;
                while (i < elements.size()) {
                  if (elements.get(i) == null) {
                    consumer.accept(true);
                    return;
                  }
                  ++i;
                }
                materializeUntil(i, this);
              }
            }

            @Override
            public void complete(final int size) throws Exception {
              consumer.accept(false);
            }

            @Override
            public void error(@NotNull final Exception error) throws Exception {
              consumer.error(error);
            }
          });
        }
      } else {
        if (elements.contains(element)) {
          safeConsume(consumer, true, LOGGER);
        } else {
          final Object other = element;
          materializeUntil(elements.size(), new IndexedAsyncConsumer<E>() {
            @Override
            public void accept(final int size, final int index, final E element) throws Exception {
              if (other.equals(element)) {
                consumer.accept(true);
              } else {
                int i = index;
                while (i < elements.size()) {
                  if (other.equals(elements.get(i))) {
                    consumer.accept(true);
                    return;
                  }
                  ++i;
                }
                materializeUntil(i, this);
              }
            }

            @Override
            public void complete(final int size) throws Exception {
              consumer.accept(false);
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
    public void materializeDone(@NotNull final AsyncConsumer<List<E>> consumer) {
      safeConsumeError(consumer, new UnsupportedOperationException(), LOGGER);
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
          materializeUntil(index, consumer);
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
        public void accept(final int size, final int index, final E element) throws Exception {
          consumer.accept(false);
        }

        @Override
        public void complete(final int size) throws Exception {
          consumer.accept(true);
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
          materializeUntil(index, new IndexedAsyncConsumer<E>() {
            @Override
            public void accept(final int size, final int index, final E element) throws Exception {
              consumer.accept(true);
            }

            @Override
            public void complete(final int size) throws Exception {
              consumer.accept(false);
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
    public void materializeNextWhile(final int index,
        @NotNull final IndexedAsyncPredicate<E> predicate) {
      int i = index;
      while (i < elements.size()) {
        if (!safeConsume(predicate, -1, i, elements.get(i), LOGGER)) {
          return;
        }
        ++i;
      }
      materializeUntil(i, new IndexedAsyncConsumer<E>() {
        @Override
        public void accept(final int size, final int index, final E element) throws Exception {
          int i = index;
          while (i < elements.size()) {
            if (!predicate.test(size, i, elements.get(i))) {
              return;
            }
            ++i;
          }
          materializeUntil(i, this);
        }

        @Override
        public void complete(final int size) throws Exception {
          predicate.complete(size);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          predicate.error(error);
        }
      });
    }

    @Override
    public void materializePrevWhile(final int index,
        @NotNull final IndexedAsyncPredicate<E> predicate) {
      final int size = elements.size();
      if (index < size) {
        for (int i = index; i >= 0; --i) {
          if (!safeConsume(predicate, -1, i, elements.get(i), LOGGER)) {
            return;
          }
        }
        safeConsumeComplete(predicate, -1, LOGGER);
      } else {
        materializeUntil(index, new IndexedAsyncConsumer<E>() {
          @Override
          public void accept(final int size, final int index, final E element) throws Exception {
            for (int i = index; i >= 0; --i) {
              if (!predicate.test(size, i, elements.get(i))) {
                return;
              }
            }
            predicate.complete(size);
          }

          @Override
          public void complete(final int size) throws Exception {
            for (int i = index; i >= 0; --i) {
              if (!predicate.test(size, i, elements.get(i))) {
                return;
              }
            }
            predicate.complete(size);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            predicate.error(error);
          }
        });
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
    public int weightElement() {
      return weightElements();
    }

    @Override
    public int weightElements() {
      if (elementsConsumers.isEmpty()) {
        if (elementsBag == null) {
          return (int) Math.min(Integer.MAX_VALUE,
              (long) wrapped.weightNextWhile() + elementsMaterializer.weightElements());
        }
        return wrapped.weightNextWhile();
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
    public int weightNextWhile() {
      return weightElements();
    }

    @Override
    public int weightPrevWhile() {
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
      final ArrayList<IndexedAsyncConsumer<E>> consumers = elementsConsumers.remove(index);
      if (consumers != null) {
        for (final IndexedAsyncConsumer<E> consumer : consumers) {
          safeConsume(consumer, -1, index, element, LOGGER);
        }
      }
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

    @SuppressWarnings("unchecked")
    private void materializeUntil(final int index,
        @NotNull final IndexedAsyncConsumer<E> consumer) {
      final ArrayList<E> elements = this.elements;
      if (elements.size() > index) {
        safeConsume(consumer, -1, index, elements.get(index), LOGGER);
      } else {
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
                    materializeUntilConsumed();
                  }

                  @Override
                  public void error(@NotNull final Exception error) {
                    consumeError(error);
                  }
                });
          } else {
            materializeUntilConsumed();
          }
        }
      }
    }

    private void materializeUntilConsumed() {
      wrapped.materializeNextWhile(nextIndex, new CancellableIndexedAsyncPredicate<E>() {
        @Override
        public void cancellableComplete(final int size) throws Exception {
          final List<E> materialized = decorateFunction.apply(elements);
          setState(new ListToListAsyncMaterializer<E>(materialized, context));
          consumeComplete(elements.size());
        }

        @Override
        public boolean cancellableTest(final int size, final int index, final E element) {
          nextIndex = index + 1;
          final HashMap<Object, Integer> elementsBag = ImmaterialState.this.elementsBag;
          final Integer count = elementsBag.get(element);
          if (count != null) {
            final int decCount = count - 1;
            if (decCount == 0) {
              elementsBag.remove(element);
            } else {
              elementsBag.put(element, decCount);
            }
            final int wrappedIndex = elements.size();
            elements.add(element);
            consumeElement(wrappedIndex, element);
          }
          return !elementsConsumers.isEmpty();
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
      });
    }
  }
}
