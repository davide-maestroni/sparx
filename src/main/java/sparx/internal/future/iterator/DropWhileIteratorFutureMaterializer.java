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
import sparx.util.function.IndexedPredicate;

public class DropWhileIteratorFutureMaterializer<E> extends AbstractIteratorFutureMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      DropWhileIteratorFutureMaterializer.class.getName());

  private final boolean isMaterializedAtOnce;

  public DropWhileIteratorFutureMaterializer(@NotNull final IteratorFutureMaterializer<E> wrapped,
      @NotNull final IndexedPredicate<? super E> predicate, @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    super(context, new AtomicInteger(STATUS_RUNNING));
    isMaterializedAtOnce = wrapped.isMaterializedAtOnce();
    setState(new ImmaterialState(wrapped, predicate, context, cancelException));
  }

  @Override
  public boolean isMaterializedAtOnce() {
    return isMaterializedAtOnce || super.isMaterializedAtOnce();
  }

  @Override
  public int knownSize() {
    return -1;
  }

  private class ImmaterialState implements IteratorFutureMaterializer<E> {

    private final ArrayList<FutureConsumer<DequeueList<E>>> bufferConsumers = new ArrayList<FutureConsumer<DequeueList<E>>>(
        2);
    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final ArrayList<FutureConsumer<List<E>>> elementsConsumers = new ArrayList<FutureConsumer<List<E>>>(
        2);
    private final IndexedPredicate<? super E> predicate;
    private final IteratorFutureMaterializer<E> wrapped;

    private DequeueList<E> buffer;
    private int index;

    public ImmaterialState(@NotNull final IteratorFutureMaterializer<E> wrapped,
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      this.wrapped = wrapped;
      this.predicate = predicate;
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
      wrapped.materializeCancel(exception);
      setCancelled(exception);
      consumeError(exception);
    }

    @Override
    public void materializeElements(@NotNull final FutureConsumer<List<E>> consumer) {
      if (buffer == null) {
        materializeBuffer(new FutureConsumer<DequeueList<E>>() {
          @Override
          public void accept(final DequeueList<E> buffer) {
            materializeElements(consumer);
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
              final DequeueList<E> materialized = new DequeueList<E>(
                  Math.max(1, buffer.size() + elements.size()));
              materialized.addAll(buffer);
              materialized.addAll(elements);
              if (materialized.isEmpty()) {
                setDone(EmptyIteratorFutureMaterializer.<E>instance());
                consumeElements(Collections.<E>emptyList());
              } else {
                setDone(new DequeueToIteratorFutureMaterializer<E>(materialized, context));
                consumeElements(materialized);
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
      if (buffer == null) {
        materializeBuffer(new FutureConsumer<DequeueList<E>>() {
          @Override
          public void accept(final DequeueList<E> buffer) throws Exception {
            if (!buffer.isEmpty()) {
              consumer.accept(true);
            } else {
              materializeHasNext(consumer);
            }
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      } else if (!buffer.isEmpty()) {
        safeConsume(consumer, true, LOGGER);
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
      if (buffer == null) {
        materializeBuffer(new FutureConsumer<DequeueList<E>>() {
          @Override
          public void accept(final DequeueList<E> buffer) throws Exception {
            if (!buffer.isEmpty()) {
              consumer.accept(-1, index++, buffer.removeFirst());
            } else {
              materializeNext(consumer);
            }
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      } else if (!buffer.isEmpty()) {
        safeConsume(consumer, -1, index++, buffer.removeFirst(), LOGGER);
      } else {
        wrapped.materializeNext(new CancellableIndexedFutureConsumer<E>() {
          @Override
          public void cancellableAccept(final int size, final int index, final E element)
              throws Exception {
            consumer.accept(size, ImmaterialState.this.index++, element);
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
      if (buffer == null) {
        materializeBuffer(new FutureConsumer<DequeueList<E>>() {
          @Override
          public void accept(final DequeueList<E> buffer) throws Exception {
            if (buffer.isEmpty() || predicate.test(-1, index++, buffer.removeFirst())) {
              materializeNextWhile(predicate);
            }
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            predicate.error(error);
          }
        });
      } else if (!buffer.isEmpty()) {
        if (safeConsume(predicate, -1, index++, buffer.removeFirst(), LOGGER)) {
          materializeNextWhile(predicate);
        }
      } else {
        wrapped.materializeNextWhile(new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) throws Exception {
            predicate.complete(0);
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            return predicate.test(size, ImmaterialState.this.index++, element);
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
      if (buffer == null) {
        materializeBuffer(new FutureConsumer<DequeueList<E>>() {
          @Override
          public void accept(final DequeueList<E> buffer) throws Exception {
            if (buffer.isEmpty()) {
              materializeSkip(count, consumer);
            } else {
              buffer.clear();
              if (count == 1) {
                consumer.accept(1);
              } else {
                materializeSkip(count - 1, new FutureConsumer<Integer>() {
                  @Override
                  public void accept(final Integer skipped) throws Exception {
                    consumer.accept(skipped + 1);
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
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      } else if (!buffer.isEmpty()) {
        buffer.clear();
        if (count == 1) {
          safeConsume(consumer, 1, LOGGER);
        } else {
          materializeSkip(count - 1, new FutureConsumer<Integer>() {
            @Override
            public void accept(final Integer skipped) throws Exception {
              consumer.accept(skipped + 1);
            }

            @Override
            public void error(@NotNull final Exception error) throws Exception {
              consumer.error(error);
            }
          });
        }
      } else {
        wrapped.materializeSkip(count, new CancellableFutureConsumer<Integer>() {
          @Override
          public void cancellableAccept(final Integer skipped) throws Exception {
            consumer.accept(skipped);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
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
      return (int) Math.min(Integer.MAX_VALUE, weightBuffer() + wrapped.weightSkip());
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
          private int wrappedIndex;

          @Override
          public void cancellableComplete(final int size) {
            setDone(EmptyIteratorFutureMaterializer.<E>instance());
            for (final FutureConsumer<DequeueList<E>> consumer : bufferConsumers) {
              safeConsume(consumer, buffer = new DequeueList<E>(1), LOGGER);
            }
            bufferConsumers.clear();
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            if (!predicate.test(wrappedIndex++, element)) {
              final DequeueList<E> buffer = ImmaterialState.this.buffer = new DequeueList<E>(1);
              buffer.add(element);
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
      return buffer == null && bufferConsumers.isEmpty() ? wrapped.weightNextWhile() : 0;
    }
  }
}