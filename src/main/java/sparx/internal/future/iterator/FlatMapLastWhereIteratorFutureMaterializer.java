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
package sparx.internal.future.iterator;

import static sparx.internal.future.FutureConsumers.safeConsume;
import static sparx.internal.future.FutureConsumers.safeConsumeError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.FutureConsumer;
import sparx.internal.future.IndexedFutureConsumer;
import sparx.internal.future.IndexedFuturePredicate;
import sparx.util.DequeueList;
import sparx.util.annotation.Positive;
import sparx.util.function.BinaryFunction;
import sparx.util.function.IndexedFunction;
import sparx.util.function.IndexedPredicate;

public class FlatMapLastWhereIteratorFutureMaterializer<E> extends
    AbstractIteratorFutureMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      FlatMapLastWhereIteratorFutureMaterializer.class.getName());

  public FlatMapLastWhereIteratorFutureMaterializer(
      @NotNull final IteratorFutureMaterializer<E> wrapped,
      @NotNull final IndexedPredicate<? super E> predicate,
      @NotNull final IndexedFunction<? super E, ? extends IteratorFutureMaterializer<E>> mapper,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final BinaryFunction<List<E>, List<E>, List<E>> prependFunction) {
    super(context);
    setState(
        new ImmaterialState(wrapped, predicate, mapper, context, cancelException, prependFunction));
  }

  @Override
  public int knownSize() {
    return -1;
  }

  private class ImmaterialState implements IteratorFutureMaterializer<E> {

    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final ArrayList<FutureConsumer<List<E>>> elementsConsumers = new ArrayList<FutureConsumer<List<E>>>(
        2);
    private final IndexedFunction<? super E, ? extends IteratorFutureMaterializer<E>> mapper;
    private final DequeueList<FutureConsumer<DequeueList<E>>> nextElementConsumers = new DequeueList<FutureConsumer<DequeueList<E>>>(
        2);
    private final DequeueList<E> cachedElements = new DequeueList<E>();
    private final DequeueList<E> nextElements = new DequeueList<E>(1);
    private final IndexedPredicate<? super E> predicate;
    private final BinaryFunction<List<E>, List<E>, List<E>> prependFunction;
    private final IteratorFutureMaterializer<E> wrapped;

    private IteratorFutureMaterializer<E> elementsMaterializer;
    private int index;
    private int wrappedIndex;

    public ImmaterialState(@NotNull final IteratorFutureMaterializer<E> wrapped,
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends IteratorFutureMaterializer<E>> mapper,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final BinaryFunction<List<E>, List<E>, List<E>> prependFunction) {
      this.wrapped = wrapped;
      this.predicate = predicate;
      this.mapper = mapper;
      this.context = context;
      this.cancelException = cancelException;
      this.prependFunction = prependFunction;
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
    public void materializeElements(@NotNull final FutureConsumer<List<E>> consumer) {
      final ArrayList<FutureConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      elementsConsumers.add(consumer);
      if (elementsConsumers.size() == 1) {
        final DequeueList<E> elements = new DequeueList<E>();
        materializeNext(new FutureConsumer<DequeueList<E>>() {
          @Override
          public void accept(final DequeueList<E> nextElements) {
            if (nextElements.isEmpty()) {
              if (elements.isEmpty()) {
                setDone(EmptyIteratorFutureMaterializer.<E>instance());
                consumeElements(Collections.<E>emptyList());
              } else {
                setDone(new DequeueToIteratorFutureMaterializer<E>(elements, context, index));
                consumeElements(elements);
              }
            } else {
              elements.addAll(nextElements);
              nextElements.clear();
              materializeNext(this);
            }
          }

          @Override
          public void error(@NotNull final Exception error) {
            setError(error);
          }
        });
      }
    }

    @Override
    public void materializeHasNext(@NotNull final FutureConsumer<Boolean> consumer) {
      materializeNext(new FutureConsumer<DequeueList<E>>() {
        @Override
        public void accept(final DequeueList<E> nextElements) throws Exception {
          if (nextElements.isEmpty()) {
            setDone(EmptyIteratorFutureMaterializer.<E>instance());
            consumer.accept(false);
          } else {
            consumer.accept(true);
          }
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeIterator(@NotNull final FutureConsumer<Iterator<E>> consumer) {
      materializeElements(new FutureConsumer<List<E>>() {
        @Override
        public void accept(final List<E> elements) {
          getState().materializeIterator(consumer);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeNext(@NotNull final IndexedFutureConsumer<E> consumer) {
      materializeNext(new FutureConsumer<DequeueList<E>>() {
        @Override
        public void accept(final DequeueList<E> nextElements) throws Exception {
          if (nextElements.isEmpty()) {
            setDone(EmptyIteratorFutureMaterializer.<E>instance());
            consumer.complete(0);
          } else {
            consumer.accept(-1, ImmaterialState.this.index++, nextElements.removeFirst());
          }
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeNextWhile(@NotNull final IndexedFuturePredicate<E> predicate) {
      materializeNext(new FutureConsumer<DequeueList<E>>() {
        @Override
        public void accept(final DequeueList<E> nextElements) throws Exception {
          if (nextElements.isEmpty()) {
            setDone(EmptyIteratorFutureMaterializer.<E>instance());
            predicate.complete(0);
          } else if (predicate.test(-1, ImmaterialState.this.index++, nextElements.removeFirst())) {
            materializeNext(this);
          }
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          predicate.error(error);
        }
      });
    }

    @Override
    public void materializeSkip(@Positive final int count,
        @NotNull final FutureConsumer<Integer> consumer) {
      materializeNext(new FutureConsumer<DequeueList<E>>() {
        private int skipped;

        @Override
        public void accept(final DequeueList<E> nextElements) throws Exception {
          if (nextElements.isEmpty()) {
            setDone(EmptyIteratorFutureMaterializer.<E>instance());
            consumer.accept(skipped);
          } else {
            while (skipped < count && !nextElements.isEmpty()) {
              nextElements.removeFirst();
              ++index;
              ++skipped;
            }
            if (skipped < count) {
              materializeNext(this);
            } else {
              consumer.accept(skipped);
            }
          }
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public int weightElements() {
      return elementsConsumers.isEmpty() ? weightNextElements() : 1;
    }

    @Override
    public int weightHasNext() {
      return weightNextElements();
    }

    @Override
    public int weightNext() {
      return weightNextElements();
    }

    @Override
    public int weightNextWhile() {
      return weightNextElements();
    }

    @Override
    public int weightSkip() {
      return weightNextElements();
    }

    private void consumeElements(@NotNull final List<E> elements) {
      final ArrayList<FutureConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      for (final FutureConsumer<List<E>> elementsConsumer : elementsConsumers) {
        safeConsume(elementsConsumer, elements, LOGGER);
      }
      elementsConsumers.clear();
    }

    private void consumeError(@NotNull final Exception error) {
      final ArrayList<FutureConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      for (final FutureConsumer<List<E>> elementsConsumer : elementsConsumers) {
        safeConsumeError(elementsConsumer, error, LOGGER);
      }
      elementsConsumers.clear();
    }

    private void materializeNext(@NotNull final FutureConsumer<DequeueList<E>> consumer) {
      final DequeueList<FutureConsumer<DequeueList<E>>> elementConsumers = this.nextElementConsumers;
      elementConsumers.add(consumer);
      if (elementConsumers.size() == 1) {
        materializeUntilConsumed();
      }
    }

    private void materializeUntilConsumed() {
      final DequeueList<E> nextElements = this.nextElements;
      final DequeueList<FutureConsumer<DequeueList<E>>> elementConsumers = this.nextElementConsumers;
      if (!nextElements.isEmpty()) {
        while (!elementConsumers.isEmpty()) {
          if (nextElements.isEmpty()) {
            materializeUntilConsumed();
            return;
          }
          safeConsume(elementConsumers.getFirst(), nextElements, LOGGER);
          elementConsumers.removeFirst();
        }
      } else if (elementsMaterializer != null) {
        elementsMaterializer.materializeNextWhile(new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) {
            for (final FutureConsumer<DequeueList<E>> consumer : elementConsumers) {
              safeConsume(consumer, nextElements, LOGGER);
            }
            elementConsumers.clear();
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element) {
            nextElements.add(element);
            while (!elementConsumers.isEmpty()) {
              if (nextElements.isEmpty()) {
                return true;
              }
              safeConsume(elementConsumers.getFirst(), nextElements, LOGGER);
              elementConsumers.removeFirst();
            }
            return false;
          }

          @Override
          public void error(@NotNull final Exception error) {
            for (final FutureConsumer<DequeueList<E>> consumer : elementConsumers) {
              safeConsumeError(consumer, error, LOGGER);
            }
            elementConsumers.clear();
          }
        });
      } else {
        final DequeueList<E> cachedElements = this.cachedElements;
        wrapped.materializeNextWhile(new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) throws Exception {
            if (!cachedElements.isEmpty()) {
              final IteratorFutureMaterializer<E> materializer = mapper.apply(
                  wrappedIndex - cachedElements.size(), cachedElements.removeFirst());
              elementsMaterializer = new InsertAllIteratorFutureMaterializer<E>(
                  new DequeueToIteratorFutureMaterializer<E>(cachedElements, context), materializer,
                  context, cancelException, prependFunction, ImmaterialState.this.index);
              materializeUntilConsumed();
            } else {
              for (final FutureConsumer<DequeueList<E>> consumer : elementConsumers) {
                safeConsume(consumer, nextElements, LOGGER);
              }
              elementConsumers.clear();
            }
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            final int wrappedIndex = ImmaterialState.this.wrappedIndex++;
            if (predicate.test(wrappedIndex, element)) {
              if (cachedElements.isEmpty()) {
                cachedElements.add(element);
                return true;
              } else {
                nextElements.addAll(cachedElements);
                cachedElements.clear();
                cachedElements.add(element);
                while (!elementConsumers.isEmpty()) {
                  if (nextElements.isEmpty()) {
                    return true;
                  }
                  safeConsume(elementConsumers.getFirst(), nextElements, LOGGER);
                  elementConsumers.removeFirst();
                }
              }
            } else if (!cachedElements.isEmpty()) {
              cachedElements.add(element);
              return true;
            } else {
              nextElements.add(element);
              while (!elementConsumers.isEmpty()) {
                if (nextElements.isEmpty()) {
                  return true;
                }
                safeConsume(elementConsumers.getFirst(), nextElements, LOGGER);
                elementConsumers.removeFirst();
              }
            }
            return false;
          }

          @Override
          public void error(@NotNull final Exception error) {
            for (final FutureConsumer<DequeueList<E>> consumer : elementConsumers) {
              safeConsumeError(consumer, error, LOGGER);
            }
            elementConsumers.clear();
          }
        });
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

    private int weightNextElements() {
      return nextElementConsumers.isEmpty() ? wrapped.weightNextWhile() : 1;
    }
  }
}
