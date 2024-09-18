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

import static sparx.internal.future.FutureConsumers.safeConsume;
import static sparx.internal.future.FutureConsumers.safeConsumeComplete;
import static sparx.internal.future.FutureConsumers.safeConsumeError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.FutureConsumer;
import sparx.internal.future.IndexedFutureConsumer;
import sparx.internal.future.IndexedFuturePredicate;

abstract class ProgressiveListFutureMaterializer<E, F> extends AbstractListFutureMaterializer<F> {

  ProgressiveListFutureMaterializer(@NotNull final ExecutionContext context,
      @NotNull final AtomicInteger status) {
    super(context, status);
  }

  @Override
  public int knownSize() {
    return -1;
  }

  abstract class ImmaterialState implements ListFutureMaterializer<F> {

    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final ArrayList<F> elements = new ArrayList<F>();
    private final HashMap<Integer, ArrayList<IndexedFutureConsumer<F>>> elementsConsumers = new HashMap<Integer, ArrayList<IndexedFutureConsumer<F>>>(
        2);
    private final Logger logger;
    private final ListFutureMaterializer<E> wrapped;

    public ImmaterialState(@NotNull final ListFutureMaterializer<E> wrapped,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Logger logger) {
      this.wrapped = wrapped;
      this.context = context;
      this.cancelException = cancelException;
      this.logger = logger;
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
    public boolean isSucceeded() {
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
    @SuppressWarnings("SuspiciousMethodCalls")
    public void materializeContains(final Object element,
        @NotNull final FutureConsumer<Boolean> consumer) {
      if (element == null) {
        if (elements.contains(null)) {
          safeConsume(consumer, true, logger);
        } else {
          materializeUntil(elements.size(), new IndexedFutureConsumer<F>() {
            @Override
            public void accept(final int size, final int index, final F element) throws Exception {
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
                addElementConsumer(i, this);
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
          safeConsume(consumer, true, logger);
        } else {
          final Object other = element;
          materializeUntil(elements.size(), new IndexedFutureConsumer<F>() {
            @Override
            public void accept(final int size, final int index, final F element) throws Exception {
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
                addElementConsumer(i, this);
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
    public void materializeElement(final int index,
        @NotNull final IndexedFutureConsumer<F> consumer) {
      if (index < 0) {
        safeConsumeError(consumer, new IndexOutOfBoundsException(Integer.toString(index)), logger);
      } else {
        final ArrayList<F> elements = this.elements;
        if (elements.size() > index) {
          safeConsume(consumer, -1, index, elements.get(index), logger);
        } else {
          materializeUntil(index, consumer);
        }
      }
    }

    @Override
    public void materializeElements(@NotNull final FutureConsumer<List<F>> consumer) {
      materializeUntil(Integer.MAX_VALUE, new IndexedFutureConsumer<F>() {
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
    }

    @Override
    public void materializeEmpty(@NotNull final FutureConsumer<Boolean> consumer) {
      materializeUntil(0, new IndexedFutureConsumer<F>() {
        @Override
        public void accept(final int size, final int index, final F element) throws Exception {
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
        @NotNull final FutureConsumer<Boolean> consumer) {
      if (index < 0) {
        safeConsume(consumer, false, logger);
      } else {
        final ArrayList<F> elements = this.elements;
        if (elements.size() > index) {
          safeConsume(consumer, true, logger);
        } else {
          materializeUntil(index, new IndexedFutureConsumer<F>() {
            @Override
            public void accept(final int size, final int index, final F element) throws Exception {
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
        @NotNull final IndexedFuturePredicate<F> predicate) {
      int i = index;
      while (i < elements.size()) {
        if (!safeConsume(predicate, -1, i, elements.get(i), logger)) {
          return;
        }
        ++i;
      }
      materializeUntil(i, new IndexedFutureConsumer<F>() {
        @Override
        public void accept(final int size, final int index, final F element) throws Exception {
          int i = index;
          while (i < elements.size()) {
            if (!predicate.test(size, i, elements.get(i))) {
              return;
            }
            ++i;
          }
          addElementConsumer(i, this);
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
        @NotNull final IndexedFuturePredicate<F> predicate) {
      final int size = elements.size();
      if (index < size) {
        for (int i = index; i >= 0; --i) {
          if (!safeConsume(predicate, -1, i, elements.get(i), logger)) {
            return;
          }
        }
        safeConsumeComplete(predicate, -1, logger);
      } else {
        materializeUntil(index, new IndexedFutureConsumer<F>() {
          @Override
          public void accept(final int size, final int index, final F element) throws Exception {
            for (int i = index; i >= 0; --i) {
              if (!predicate.test(size, i, elements.get(i))) {
                return;
              }
            }
            predicate.complete(size);
          }

          @Override
          public void complete(final int size) throws Exception {
            for (int i = elements.size() - 1; i >= 0; --i) {
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
    public void materializeSize(@NotNull final FutureConsumer<Integer> consumer) {
      materializeUntil(Integer.MAX_VALUE, new IndexedFutureConsumer<F>() {
        @Override
        public void accept(final int size, final int index, final F element) {
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
    public int weightEmpty() {
      return weightElements();
    }

    @Override
    public int weightHasElement() {
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

    boolean addElementConsumer(final int index, @NotNull final IndexedFutureConsumer<F> consumer) {
      final HashMap<Integer, ArrayList<IndexedFutureConsumer<F>>> elementsConsumers = this.elementsConsumers;
      final boolean isFirst = elementsConsumers.isEmpty();
      ArrayList<IndexedFutureConsumer<F>> indexConsumers = elementsConsumers.get(index);
      if (indexConsumers == null) {
        elementsConsumers.put(index, indexConsumers = new ArrayList<IndexedFutureConsumer<F>>(2));
      }
      indexConsumers.add(consumer);
      return isFirst;
    }

    abstract void materializeNext();

    boolean needsMaterializing() {
      return elementsConsumers.isEmpty();
    }

    void setComplete() throws Exception {
      setDone(new ListToListFutureMaterializer<F>(elements, context));
      consumeComplete(elements.size());
    }

    void setError(@NotNull final Exception error) {
      final CancellationException exception = cancelException.get();
      if (exception != null) {
        setCancelled(exception);
        consumeError(exception);
      } else {
        setFailed(error);
        consumeError(error);
      }
    }

    boolean setNextElement(final F element) {
      final int wrappedIndex = elements.size();
      elements.add(element);
      consumeElement(wrappedIndex, element);
      return !elementsConsumers.isEmpty();
    }

    private void consumeComplete(final int size) {
      final HashMap<Integer, ArrayList<IndexedFutureConsumer<F>>> elementsConsumers = this.elementsConsumers;
      for (final ArrayList<IndexedFutureConsumer<F>> consumers : elementsConsumers.values()) {
        for (final IndexedFutureConsumer<F> consumer : consumers) {
          safeConsumeComplete(consumer, size, logger);
        }
      }
      elementsConsumers.clear();
    }

    private void consumeElement(final int index, final F element) {
      final ArrayList<IndexedFutureConsumer<F>> consumers = elementsConsumers.remove(index);
      if (consumers != null) {
        for (final IndexedFutureConsumer<F> consumer : consumers) {
          safeConsume(consumer, -1, index, element, logger);
        }
      }
    }

    private void consumeError(@NotNull final Exception error) {
      final HashMap<Integer, ArrayList<IndexedFutureConsumer<F>>> elementsConsumers = this.elementsConsumers;
      for (final ArrayList<IndexedFutureConsumer<F>> consumers : elementsConsumers.values()) {
        for (final IndexedFutureConsumer<F> consumer : consumers) {
          safeConsumeError(consumer, error, logger);
        }
      }
      elementsConsumers.clear();
    }

    private void materializeUntil(final int index,
        @NotNull final IndexedFutureConsumer<F> consumer) {
      final ArrayList<F> elements = this.elements;
      if (elements.size() > index) {
        safeConsume(consumer, -1, index, elements.get(index), logger);
      } else if (addElementConsumer(index, consumer)) {
        materializeNext();
      }
    }
  }
}

