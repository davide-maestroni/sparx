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
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.FutureConsumer;
import sparx.internal.future.IndexedFutureConsumer;
import sparx.internal.future.IndexedFuturePredicate;
import sparx.util.DequeueList;
import sparx.util.annotation.Positive;

public class DropRightIteratorFutureMaterializer<E> extends AbstractIteratorFutureMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      DropRightIteratorFutureMaterializer.class.getName());

  private final int knownSize;

  public DropRightIteratorFutureMaterializer(@NotNull final IteratorFutureMaterializer<E> wrapped,
      @Positive final int maxElements, @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    this(wrapped, maxElements, new AtomicInteger(STATUS_RUNNING), context, cancelException);
  }

  // TODO: needed?
  DropRightIteratorFutureMaterializer(@NotNull final IteratorFutureMaterializer<E> wrapped,
      @Positive final int maxElements, @NotNull final AtomicInteger status,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    super(context, status);
    final int wrappedSize = wrapped.knownSize();
    knownSize = wrappedSize >= 0 ? Math.max(0, wrappedSize - maxElements) : -1;
    setState(new ImmaterialState(wrapped, maxElements, context, cancelException));
  }

  @Override
  public int knownSize() {
    return knownSize;
  }

  private class ImmaterialState implements IteratorFutureMaterializer<E> {

    private final DequeueList<E> buffer;
    private final ArrayList<FutureConsumer<DequeueList<E>>> bufferConsumers = new ArrayList<FutureConsumer<DequeueList<E>>>(
        2);
    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final ArrayList<FutureConsumer<List<E>>> elementsConsumers = new ArrayList<FutureConsumer<List<E>>>(
        2);
    private final int maxElements;
    private final IteratorFutureMaterializer<E> wrapped;

    private int index;

    public ImmaterialState(@NotNull final IteratorFutureMaterializer<E> wrapped,
        final int maxElements, @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      this.wrapped = wrapped;
      this.maxElements = maxElements;
      this.context = context;
      this.cancelException = cancelException;
      buffer = new DequeueList<E>(Math.min(64, maxElements));
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
      return knownSize;
    }

    @Override
    public void materializeCancel(@NotNull final CancellationException exception) {
      wrapped.materializeCancel(exception);
      setCancelled(exception);
      consumeError(exception);
    }

    @Override
    public void materializeElements(@NotNull final FutureConsumer<List<E>> consumer) {
      if (buffer.isEmpty()) {
        materializeBuffer(new FutureConsumer<DequeueList<E>>() {
          @Override
          public void accept(final DequeueList<E> buffer) throws Exception {
            if (buffer.isEmpty()) {
              consumer.accept(Collections.<E>emptyList());
            } else {
              materializeElements(consumer);
            }
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      } else {
        final ArrayList<FutureConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
        elementsConsumers.add(consumer);
        if (elementsConsumers.size() == 1) {
          wrapped.materializeElements(new CancellableFutureConsumer<List<E>>() {
            @Override
            public void cancellableAccept(final List<E> elements) {
              final int size = elements.size();
              if (size == 0) {
                setDone(EmptyIteratorFutureMaterializer.<E>instance());
                consumeElements(Collections.<E>emptyList());
              } else {
                final DequeueList<E> materialized = new DequeueList<E>(size);
                if (size > maxElements) {
                  materialized.addAll(buffer);
                  materialized.addAll(elements.subList(0, size - maxElements));
                } else {
                  materialized.addAll(buffer.subList(0, size));
                }
                if (materialized.isEmpty()) {
                  setDone(EmptyIteratorFutureMaterializer.<E>instance());
                  consumeElements(Collections.<E>emptyList());
                } else {
                  setDone(new DequeueToIteratorFutureMaterializer<E>(materialized, context));
                  consumeElements(materialized);
                }
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
          });
        }
      }
    }

    @Override
    public void materializeHasNext(@NotNull final FutureConsumer<Boolean> consumer) {
      if (buffer.isEmpty()) {
        materializeBuffer(new FutureConsumer<DequeueList<E>>() {
          @Override
          public void accept(final DequeueList<E> buffer) throws Exception {
            if (buffer.isEmpty()) {
              consumer.accept(false);
            } else {
              materializeHasNext(consumer);
            }
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      } else {
        wrapped.materializeHasNext(new CancellableFutureConsumer<Boolean>() {
          @Override
          public void cancellableAccept(final Boolean hasNext) throws Exception {
            consumer.accept(hasNext);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
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
      if (buffer.isEmpty()) {
        materializeBuffer(new FutureConsumer<DequeueList<E>>() {
          @Override
          public void accept(final DequeueList<E> buffer) throws Exception {
            if (buffer.isEmpty()) {
              consumer.complete(0);
            } else {
              materializeNext(consumer);
            }
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      } else {
        wrapped.materializeNext(new CancellableIndexedFutureConsumer<E>() {
          @Override
          public void cancellableAccept(final int size, final int index, final E element)
              throws Exception {
            buffer.add(element);
            consumer.accept(size, ImmaterialState.this.index++, buffer.removeFirst());
          }

          @Override
          public void cancellableComplete(final int size) throws Exception {
            consumer.complete(0);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
    }

    @Override
    public void materializeNextWhile(@NotNull final IndexedFuturePredicate<E> predicate) {
      if (buffer.isEmpty()) {
        materializeBuffer(new FutureConsumer<DequeueList<E>>() {
          @Override
          public void accept(final DequeueList<E> buffer) throws Exception {
            if (buffer.isEmpty()) {
              predicate.complete(0);
            } else {
              materializeNextWhile(predicate);
            }
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            predicate.error(error);
          }
        });
      } else {
        wrapped.materializeNextWhile(new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) throws Exception {
            predicate.complete(0);
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            buffer.add(element);
            return predicate.test(size, ImmaterialState.this.index++, buffer.removeFirst());
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            predicate.error(error);
          }
        });
      }
    }

    @Override
    public void materializeSkip(@Positive final int count,
        @NotNull final FutureConsumer<Integer> consumer) {
      materializeNextWhile(new IndexedFuturePredicate<E>() {
        private int skipped;

        @Override
        public void complete(final int size) throws Exception {
          consumer.accept(skipped);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }

        @Override
        public boolean test(final int size, final int index, final E element) throws Exception {
          if (++skipped >= count) {
            consumer.accept(skipped);
            return false;
          }
          return true;
        }
      });
    }

    @Override
    public int weightElements() {
      return elementsConsumers.isEmpty() ? (int) Math.min(Integer.MAX_VALUE,
          weightBuffer() + wrapped.weightElements()) : 1;
    }

    @Override
    public int weightHasNext() {
      return (int) Math.min(Integer.MAX_VALUE, weightBuffer() + wrapped.weightHasNext());
    }

    @Override
    public int weightNext() {
      return (int) Math.min(Integer.MAX_VALUE, weightBuffer() + wrapped.weightNext());
    }

    @Override
    public int weightNextWhile() {
      return (int) Math.min(Integer.MAX_VALUE, weightBuffer() + wrapped.weightNextWhile());
    }

    @Override
    public int weightSkip() {
      return weightNextWhile();
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

    private void materializeBuffer(@NotNull final FutureConsumer<DequeueList<E>> consumer) {
      final ArrayList<FutureConsumer<DequeueList<E>>> bufferConsumers = this.bufferConsumers;
      bufferConsumers.add(consumer);
      if (bufferConsumers.size() == 1) {
        wrapped.materializeNextWhile(new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) {
            buffer.clear();
            setDone(EmptyIteratorFutureMaterializer.<E>instance());
            for (final FutureConsumer<DequeueList<E>> consumer : bufferConsumers) {
              safeConsume(consumer, buffer, LOGGER);
            }
            bufferConsumers.clear();
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element) {
            buffer.add(element);
            if (buffer.size() >= maxElements) {
              for (final FutureConsumer<DequeueList<E>> consumer : bufferConsumers) {
                safeConsume(consumer, buffer, LOGGER);
              }
              bufferConsumers.clear();
              return false;
            }
            return true;
          }

          @Override
          public void error(@NotNull final Exception error) {
            final CancellationException exception = cancelException.get();
            if (exception != null) {
              setCancelled(exception);
              for (final FutureConsumer<DequeueList<E>> consumer : bufferConsumers) {
                safeConsumeError(consumer, exception, LOGGER);
              }
            } else {
              setFailed(error);
              for (final FutureConsumer<DequeueList<E>> consumer : bufferConsumers) {
                safeConsumeError(consumer, error, LOGGER);
              }
            }
            bufferConsumers.clear();
          }
        });
      }
    }

    private long weightBuffer() {
      return buffer.isEmpty() && bufferConsumers.isEmpty() ? wrapped.weightNextWhile() : 0;
    }
  }
}
