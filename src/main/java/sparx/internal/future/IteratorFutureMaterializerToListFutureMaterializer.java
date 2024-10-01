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
package sparx.internal.future;

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
import sparx.internal.future.iterator.IteratorFutureMaterializer;
import sparx.internal.future.list.AbstractListFutureMaterializer;
import sparx.internal.future.list.ListFutureMaterializer;
import sparx.internal.future.list.ListToListFutureMaterializer;
import sparx.util.annotation.NotNegative;

public class IteratorFutureMaterializerToListFutureMaterializer<E> extends
    AbstractListFutureMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      IteratorFutureMaterializerToListFutureMaterializer.class.getName());

  private final int knownSize;

  public IteratorFutureMaterializerToListFutureMaterializer(@NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final IteratorFutureMaterializer<E> materializer) {
    super(context, new AtomicInteger(STATUS_RUNNING));
    knownSize = materializer.knownSize();
    setState(new ImmaterialState(materializer, context, cancelException));
  }

  @Override
  public int knownSize() {
    return knownSize;
  }

  private class ImmaterialState implements ListFutureMaterializer<E> {

    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final ArrayList<E> elements = new ArrayList<E>();
    private final HashMap<Integer, ArrayList<IndexedFutureConsumer<E>>> elementsConsumers = new HashMap<Integer, ArrayList<IndexedFutureConsumer<E>>>(
        2);
    private final IteratorFutureMaterializer<E> materializer;

    private ImmaterialState(@NotNull final IteratorFutureMaterializer<E> materializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      this.materializer = materializer;
      this.context = context;
      this.cancelException = cancelException;
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
      materializer.materializeCancel(exception);
      setCancelled(exception);
      consumeError(exception);
    }

    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    public void materializeContains(final Object element,
        @NotNull final FutureConsumer<Boolean> consumer) {
      if (element == null) {
        if (elements.contains(null)) {
          safeConsume(consumer, true, LOGGER);
        } else {
          materializeUntil(elements.size(), new IndexedFutureConsumer<E>() {
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
          safeConsume(consumer, true, LOGGER);
        } else {
          final Object other = element;
          materializeUntil(elements.size(), new IndexedFutureConsumer<E>() {
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
    public void materializeElement(@NotNegative final int index,
        @NotNull final IndexedFutureConsumer<E> consumer) {
      final ArrayList<E> elements = this.elements;
      if (elements.size() > index) {
        safeConsume(consumer, -1, index, elements.get(index), LOGGER);
      } else {
        materializeUntil(index, consumer);
      }
    }

    @Override
    public void materializeElements(@NotNull final FutureConsumer<List<E>> consumer) {
      materializeUntil(Integer.MAX_VALUE, new IndexedFutureConsumer<E>() {
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
    public void materializeEmpty(@NotNull final FutureConsumer<Boolean> consumer) {
      materializeUntil(0, new IndexedFutureConsumer<E>() {
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
    public void materializeHasElement(@NotNegative final int index,
        @NotNull final FutureConsumer<Boolean> consumer) {
      final ArrayList<E> elements = this.elements;
      if (elements.size() > index) {
        safeConsume(consumer, true, LOGGER);
      } else {
        materializeUntil(index, new IndexedFutureConsumer<E>() {
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

    @Override
    public void materializeNextWhile(@NotNegative final int index,
        @NotNull final IndexedFuturePredicate<E> predicate) {
      int i = index;
      while (i < elements.size()) {
        if (!safeConsume(predicate, -1, i, elements.get(i), LOGGER)) {
          return;
        }
        ++i;
      }
      materializeUntil(i, new IndexedFutureConsumer<E>() {
        @Override
        public void accept(final int size, final int index, final E element) throws Exception {
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
    public void materializePrevWhile(@NotNegative final int index,
        @NotNull final IndexedFuturePredicate<E> predicate) {
      final int size = elements.size();
      if (index < size) {
        for (int i = index; i >= 0; --i) {
          if (!safeConsume(predicate, -1, i, elements.get(i), LOGGER)) {
            return;
          }
        }
        safeConsumeComplete(predicate, -1, LOGGER);
      } else {
        materializeUntil(index, new IndexedFutureConsumer<E>() {
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
      materializeUntil(Integer.MAX_VALUE, new IndexedFutureConsumer<E>() {
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
      return materializer.weightNextWhile();
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

    boolean addElementConsumer(final int index, @NotNull final IndexedFutureConsumer<E> consumer) {
      final HashMap<Integer, ArrayList<IndexedFutureConsumer<E>>> elementsConsumers = this.elementsConsumers;
      final boolean isFirst = elementsConsumers.isEmpty();
      ArrayList<IndexedFutureConsumer<E>> indexConsumers = elementsConsumers.get(index);
      if (indexConsumers == null) {
        elementsConsumers.put(index, indexConsumers = new ArrayList<IndexedFutureConsumer<E>>(2));
      }
      indexConsumers.add(consumer);
      return isFirst;
    }

    private void consumeComplete(final int size) {
      final HashMap<Integer, ArrayList<IndexedFutureConsumer<E>>> elementsConsumers = this.elementsConsumers;
      for (final ArrayList<IndexedFutureConsumer<E>> consumers : elementsConsumers.values()) {
        for (final IndexedFutureConsumer<E> consumer : consumers) {
          safeConsumeComplete(consumer, size, LOGGER);
        }
      }
      elementsConsumers.clear();
    }

    private void consumeElement(final int index, final E element) {
      final ArrayList<IndexedFutureConsumer<E>> consumers = elementsConsumers.remove(index);
      if (consumers != null) {
        for (final IndexedFutureConsumer<E> consumer : consumers) {
          safeConsume(consumer, -1, index, element, LOGGER);
        }
      }
    }

    private void consumeError(@NotNull final Exception error) {
      final HashMap<Integer, ArrayList<IndexedFutureConsumer<E>>> elementsConsumers = this.elementsConsumers;
      for (final ArrayList<IndexedFutureConsumer<E>> consumers : elementsConsumers.values()) {
        for (final IndexedFutureConsumer<E> consumer : consumers) {
          safeConsumeError(consumer, error, LOGGER);
        }
      }
      elementsConsumers.clear();
    }

    private void materializeUntil(final int index,
        @NotNull final IndexedFutureConsumer<E> consumer) {
      final ArrayList<E> elements = this.elements;
      if (elements.size() > index) {
        safeConsume(consumer, -1, index, elements.get(index), LOGGER);
      } else if (addElementConsumer(index, consumer)) {
        materializer.materializeNextWhile(new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) {
            setDone(new ListToListFutureMaterializer<E>(elements, context));
            consumeComplete(elements.size());
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element) {
            final int wrappedIndex = elements.size();
            elements.add(element);
            consumeElement(wrappedIndex, element);
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
}
