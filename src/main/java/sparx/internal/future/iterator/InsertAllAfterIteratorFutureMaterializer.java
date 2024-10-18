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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ContextTask;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.FutureConsumer;
import sparx.internal.future.IndexedFutureConsumer;
import sparx.internal.future.IndexedFuturePredicate;
import sparx.util.DequeueList;
import sparx.util.SizeOverflowException;
import sparx.util.annotation.Positive;

public class InsertAllAfterIteratorFutureMaterializer<E> extends
    AbstractIteratorFutureMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      InsertAllAfterIteratorFutureMaterializer.class.getName());

  private final int knownSize;
  private final boolean isMaterializedAtOnce;

  public InsertAllAfterIteratorFutureMaterializer(
      @NotNull final IteratorFutureMaterializer<E> wrapped, @Positive final int numElements,
      @NotNull final IteratorFutureMaterializer<E> elementsMaterializer,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    super(context, new AtomicInteger(STATUS_RUNNING));
    knownSize = safeSize(wrapped.knownSize(), elementsMaterializer.knownSize(), numElements);
    isMaterializedAtOnce =
        wrapped.isMaterializedAtOnce() && elementsMaterializer.isMaterializedAtOnce();
    setState(
        new ImmaterialState(wrapped, numElements, elementsMaterializer, context, cancelException));
  }

  private static int safeSize(final int wrappedSize, final int elementsSize, final int numElement) {
    if (wrappedSize >= numElement && elementsSize >= 0) {
      if (elementsSize > 0) {
        return SizeOverflowException.safeCast((long) wrappedSize + elementsSize);
      }
      return wrappedSize;
    }
    return -1;
  }

  @Override
  public boolean isMaterializedAtOnce() {
    return isMaterializedAtOnce || super.isMaterializedAtOnce();
  }

  @Override
  public int knownSize() {
    return knownSize;
  }

  private class ImmaterialState implements IteratorFutureMaterializer<E> {

    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final ArrayList<FutureConsumer<List<E>>> elementsConsumers = new ArrayList<FutureConsumer<List<E>>>(
        2);
    private final IteratorFutureMaterializer<E> elementsMaterializer;
    private final DequeueList<FutureConsumer<DequeueList<E>>> nextElementConsumers = new DequeueList<FutureConsumer<DequeueList<E>>>(
        2);
    private final DequeueList<E> nextElements = new DequeueList<E>(1);
    private final int numElements;
    private final IteratorFutureMaterializer<E> wrapped;

    private int index;
    private int wrappedIndex;

    public ImmaterialState(@NotNull final IteratorFutureMaterializer<E> wrapped,
        @Positive final int numElements,
        @NotNull final IteratorFutureMaterializer<E> elementsMaterializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      this.wrapped = wrapped;
      this.numElements = numElements;
      this.elementsMaterializer = elementsMaterializer;
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
      return wrapped.isMaterializedAtOnce() && elementsMaterializer.isMaterializedAtOnce();
    }

    @Override
    public boolean isSucceeded() {
      return false;
    }

    @Override
    public int knownSize() {
      return knownSize;
    }

    @Override
    public void materializeCancel(@NotNull final CancellationException exception) {
      wrapped.materializeCancel(exception);
      elementsMaterializer.materializeCancel(exception);
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
          consumer.accept(!nextElements.isEmpty());
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
      return elementsConsumers.isEmpty() ? (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightElements() + elementsMaterializer.weightElements()) : 1;
    }

    @Override
    public int weightHasNext() {
      return (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightHasNext() + elementsMaterializer.weightHasNext());
    }

    @Override
    public int weightNext() {
      return (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightNext() + elementsMaterializer.weightNext());
    }

    @Override
    public int weightNextWhile() {
      return (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightNextWhile() + elementsMaterializer.weightNextWhile());
    }

    @Override
    public int weightSkip() {
      return (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightSkip() + elementsMaterializer.weightSkip());
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
      } else if (wrappedIndex == numElements) {
        elementsMaterializer.materializeNextWhile(new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) {
            ++wrappedIndex;
            final String taskID = context.currentTaskID();
            context.scheduleAfter(new ContextTask(context) {
              @Override
              protected void runWithContext() {
                materializeUntilConsumed();
              }

              @Override
              public @NotNull String taskID() {
                return taskID != null ? taskID : "";
              }

              @Override
              public int weight() {
                return wrapped.weightNextWhile();
              }
            });
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
            while (!elementConsumers.isEmpty()) {
              safeConsumeError(elementConsumers.getFirst(), error, LOGGER);
              elementConsumers.removeFirst();
            }
          }
        });
      } else {
        wrapped.materializeNextWhile(new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) {
            for (final FutureConsumer<DequeueList<E>> consumer : elementConsumers) {
              safeConsume(consumer, nextElements, LOGGER);
            }
            elementConsumers.clear();
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element) {
            ++wrappedIndex;
            nextElements.add(element);
            while (!elementConsumers.isEmpty()) {
              if (nextElements.isEmpty()) {
                if (wrappedIndex == numElements) {
                  materializeUntilConsumed();
                  return false;
                }
                return true;
              }
              safeConsume(elementConsumers.getFirst(), nextElements, LOGGER);
              elementConsumers.removeFirst();
            }
            return false;
          }

          @Override
          public void error(@NotNull final Exception error) {
            while (!elementConsumers.isEmpty()) {
              safeConsumeError(elementConsumers.getFirst(), error, LOGGER);
              elementConsumers.removeFirst();
            }
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
  }
}
