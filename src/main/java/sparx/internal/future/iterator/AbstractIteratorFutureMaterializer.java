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
import sparx.util.annotation.Positive;

public abstract class AbstractIteratorFutureMaterializer<E> implements
    IteratorFutureMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      AbstractIteratorFutureMaterializer.class.getName());

  protected static final int STATUS_CANCELLED = 3;
  protected static final int STATUS_FAILED = 2;
  protected static final int STATUS_DONE = 1;
  protected static final int STATUS_RUNNING = 0;

  final ExecutionContext context;

  private final AtomicInteger status = new AtomicInteger(STATUS_RUNNING);

  private CancellationException cancelException;
  private IteratorFutureMaterializer<E> state;

  public AbstractIteratorFutureMaterializer(@NotNull final ExecutionContext context) {
    this.context = context;
  }

  @Override
  public boolean isCancelled() {
    return status.get() == STATUS_CANCELLED;
  }

  @Override
  public boolean isDone() {
    return status.get() != STATUS_RUNNING;
  }

  @Override
  public boolean isFailed() {
    return status.get() == STATUS_FAILED;
  }

  @Override
  public boolean isMaterializedAtOnce() {
    return isDone();
  }

  @Override
  public boolean isSucceeded() {
    return status.get() == STATUS_DONE;
  }

  @Override
  public void materializeCancel(@NotNull final CancellationException exception) {
    cancelException = exception;
    state.materializeCancel(exception);
  }

  @Override
  public void materializeElements(@NotNull final FutureConsumer<List<E>> consumer) {
    state.materializeElements(consumer);
  }

  @Override
  public void materializeHasNext(@NotNull final FutureConsumer<Boolean> consumer) {
    state.materializeHasNext(consumer);
  }

  @Override
  public void materializeIterator(@NotNull final FutureConsumer<Iterator<E>> consumer) {
    state.materializeIterator(consumer);
  }

  @Override
  public void materializeNext(@NotNull final IndexedFutureConsumer<E> consumer) {
    state.materializeNext(consumer);
  }

  @Override
  public void materializeNextWhile(@NotNull final IndexedFuturePredicate<E> predicate) {
    state.materializeNextWhile(predicate);
  }

  @Override
  public void materializeSkip(@Positive final int count,
      @NotNull final FutureConsumer<Integer> consumer) {
    state.materializeSkip(count, consumer);
  }

  @Override
  public int weightElements() {
    return state.weightElements();
  }

  @Override
  public int weightHasNext() {
    return state.weightHasNext();
  }

  @Override
  public int weightNext() {
    return state.weightNext();
  }

  @Override
  public int weightNextWhile() {
    return state.weightNextWhile();
  }

  @Override
  public int weightSkip() {
    return state.weightSkip();
  }

  @NotNull
  protected final IteratorFutureMaterializer<E> getState() {
    return state;
  }

  @NotNull
  protected final IteratorFutureMaterializer<E> setCancelled(
      @NotNull final CancellationException exception) {
    return setState(new CancelledIteratorFutureMaterializer<E>(exception), STATUS_CANCELLED);
  }

  @NotNull
  protected final IteratorFutureMaterializer<E> setDone(
      @NotNull final IteratorFutureMaterializer<E> newState) {
    return setState(newState, STATUS_DONE);
  }

  @NotNull
  protected final IteratorFutureMaterializer<E> setFailed(@NotNull final Exception error) {
    return setState(new FailedIteratorFutureMaterializer<E>(error), STATUS_FAILED);
  }

  @NotNull
  protected final IteratorFutureMaterializer<E> setState(
      @NotNull final IteratorFutureMaterializer<E> newState) {
    return setState(newState, STATUS_RUNNING);
  }

  @NotNull
  private IteratorFutureMaterializer<E> setState(
      @NotNull final IteratorFutureMaterializer<E> newState, final int statusCode) {
    if (status.compareAndSet(STATUS_RUNNING, statusCode)) {
      state = newState;
    }
    return state;
  }

  protected abstract class CancellableFutureConsumer<P> extends ContextTask implements
      FutureConsumer<P> {

    protected CancellableFutureConsumer() {
      super(context);
    }

    @Override
    public void accept(final P param) throws Exception {
      final CancellationException cancelException = AbstractIteratorFutureMaterializer.this.cancelException;
      if (cancelException != null) {
        error(cancelException);
      } else {
        cancellableAccept(param);
      }
    }

    public void cancellableAccept(final P param) throws Exception {
    }
  }

  protected abstract class CancellableIndexedFutureConsumer<P> extends ContextTask implements
      IndexedFutureConsumer<P> {

    protected CancellableIndexedFutureConsumer() {
      super(context);
    }

    @Override
    public void accept(final int size, final int index, final P param) throws Exception {
      final CancellationException cancelException = AbstractIteratorFutureMaterializer.this.cancelException;
      if (cancelException != null) {
        error(cancelException);
      } else {
        cancellableAccept(size, index, param);
      }
    }

    @Override
    public void complete(final int size) throws Exception {
      final CancellationException cancelException = AbstractIteratorFutureMaterializer.this.cancelException;
      if (cancelException != null) {
        error(cancelException);
      } else {
        cancellableComplete(size);
      }
    }

    public void cancellableAccept(final int size, final int index, final P param) throws Exception {
    }

    public void cancellableComplete(final int size) throws Exception {
    }
  }

  protected abstract class CancellableIndexedFuturePredicate<P> extends ContextTask implements
      IndexedFuturePredicate<P> {

    protected CancellableIndexedFuturePredicate() {
      super(context);
    }

    @Override
    public void complete(final int size) throws Exception {
      final CancellationException cancelException = AbstractIteratorFutureMaterializer.this.cancelException;
      if (cancelException != null) {
        error(cancelException);
      } else {
        cancellableComplete(size);
      }
    }

    @Override
    public boolean test(final int size, final int index, final P param) throws Exception {
      final CancellationException cancelException = AbstractIteratorFutureMaterializer.this.cancelException;
      if (cancelException != null) {
        error(cancelException);
        return false;
      } else {
        return cancellableTest(size, index, param);
      }
    }

    public void cancellableComplete(final int size) throws Exception {
    }

    public boolean cancellableTest(final int size, final int index, final P param)
        throws Exception {
      return false;
    }
  }

  protected abstract class CancellableMultiFutureConsumer<P1, P2> extends ContextTask implements
      FutureConsumer<P1>, IndexedFutureConsumer<P2> {

    protected CancellableMultiFutureConsumer() {
      super(context);
    }

    @Override
    public void accept(final P1 param) throws Exception {
      final CancellationException cancelException = AbstractIteratorFutureMaterializer.this.cancelException;
      if (cancelException != null) {
        error(cancelException);
      } else {
        cancellableAccept(param);
      }
    }

    @Override
    public void accept(final int size, final int index, final P2 param) throws Exception {
      final CancellationException cancelException = AbstractIteratorFutureMaterializer.this.cancelException;
      if (cancelException != null) {
        error(cancelException);
      } else {
        cancellableAccept(size, index, param);
      }
    }

    @Override
    public void complete(final int size) throws Exception {
      final CancellationException cancelException = AbstractIteratorFutureMaterializer.this.cancelException;
      if (cancelException != null) {
        error(cancelException);
      } else {
        cancellableComplete(size);
      }
    }

    public void cancellableAccept(final P1 param) throws Exception {
    }

    public void cancellableAccept(final int size, final int index, final P2 param)
        throws Exception {
    }

    public void cancellableComplete(final int size) throws Exception {
    }
  }

  protected abstract class CancellableMultiFuturePredicate<P1, P2> extends ContextTask implements
      FutureConsumer<P1>, IndexedFuturePredicate<P2> {

    protected CancellableMultiFuturePredicate() {
      super(context);
    }

    @Override
    public void accept(final P1 param) throws Exception {
      final CancellationException cancelException = AbstractIteratorFutureMaterializer.this.cancelException;
      if (cancelException != null) {
        error(cancelException);
      } else {
        cancellableAccept(param);
      }
    }

    @Override
    public void complete(final int size) throws Exception {
      final CancellationException cancelException = AbstractIteratorFutureMaterializer.this.cancelException;
      if (cancelException != null) {
        error(cancelException);
      } else {
        cancellableComplete(size);
      }
    }

    @Override
    public boolean test(final int size, final int index, final P2 param) throws Exception {
      final CancellationException cancelException = AbstractIteratorFutureMaterializer.this.cancelException;
      if (cancelException != null) {
        error(cancelException);
      } else {
        return cancellableTest(size, index, param);
      }
      return false;
    }

    public void cancellableAccept(final P1 param) throws Exception {
    }

    public void cancellableComplete(final int size) throws Exception {
    }

    public boolean cancellableTest(final int size, final int index, final P2 param)
        throws Exception {
      return false;
    }
  }

  protected class WrappingState implements IteratorFutureMaterializer<E> {

    private final AtomicReference<CancellationException> cancelException;
    private final ArrayList<FutureConsumer<List<E>>> elementsConsumers = new ArrayList<FutureConsumer<List<E>>>(
        2);
    private final int knownSize;
    private final IteratorFutureMaterializer<E> wrapped;

    private int index;

    protected WrappingState(@NotNull final IteratorFutureMaterializer<E> wrapped,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      this(wrapped, cancelException, 0);
    }

    protected WrappingState(@NotNull final IteratorFutureMaterializer<E> wrapped,
        @NotNull final AtomicReference<CancellationException> cancelException, final int offset) {
      this.wrapped = wrapped;
      this.cancelException = cancelException;
      this.index = offset;
      knownSize = wrapped.knownSize();
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
      return wrapped.isMaterializedAtOnce();
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
      final ArrayList<FutureConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      for (final FutureConsumer<List<E>> consumer : elementsConsumers) {
        safeConsumeError(consumer, exception, LOGGER);
      }
      elementsConsumers.clear();
    }

    @Override
    public void materializeElements(@NotNull final FutureConsumer<List<E>> consumer) {
      final ArrayList<FutureConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      elementsConsumers.add(consumer);
      if (elementsConsumers.size() == 1) {
        wrapped.materializeElements(new CancellableFutureConsumer<List<E>>() {
          @Override
          public void cancellableAccept(final List<E> elements) {
            setDone(new ListToIteratorFutureMaterializer<E>(elements, context));
            for (final FutureConsumer<List<E>> consumer : elementsConsumers) {
              safeConsume(consumer, elements, LOGGER);
            }
            elementsConsumers.clear();
          }

          @Override
          public void error(@NotNull Exception error) {
            final CancellationException exception = cancelException.get();
            if (exception != null) {
              setCancelled(exception);
              error = exception;
            } else {
              setFailed(error);
            }
            for (final FutureConsumer<List<E>> consumer : elementsConsumers) {
              safeConsumeError(consumer, error, LOGGER);
            }
            elementsConsumers.clear();
          }
        });
      }
    }

    @Override
    public void materializeHasNext(@NotNull final FutureConsumer<Boolean> consumer) {
      wrapped.materializeHasNext(new CancellableFutureConsumer<Boolean>() {
        @Override
        public void cancellableAccept(final Boolean hasNext) throws Exception {
          if (!hasNext) {
            setDone(EmptyIteratorFutureMaterializer.<E>instance());
          }
          consumer.accept(hasNext);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeIterator(@NotNull final FutureConsumer<Iterator<E>> consumer) {
      wrapped.materializeIterator(new CancellableFutureConsumer<Iterator<E>>() {
        @Override
        public void cancellableAccept(final Iterator<E> iterator) throws Exception {
          consumer.accept(iterator);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeNext(@NotNull final IndexedFutureConsumer<E> consumer) {
      wrapped.materializeNext(new CancellableIndexedFutureConsumer<E>() {
        @Override
        public void cancellableAccept(final int size, final int index, final E element)
            throws Exception {
          consumer.accept(size, WrappingState.this.index++, element);
        }

        @Override
        public void cancellableComplete(final int size) throws Exception {
          setDone(EmptyIteratorFutureMaterializer.<E>instance());
          consumer.complete(size);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeNextWhile(@NotNull final IndexedFuturePredicate<E> predicate) {
      wrapped.materializeNextWhile(new CancellableIndexedFuturePredicate<E>() {
        @Override
        public void cancellableComplete(final int size) throws Exception {
          setDone(EmptyIteratorFutureMaterializer.<E>instance());
          predicate.complete(size);
        }

        @Override
        public boolean cancellableTest(final int size, final int index, final E element)
            throws Exception {
          return predicate.test(size, WrappingState.this.index++, element);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          predicate.error(error);
        }
      });
    }

    @Override
    public void materializeSkip(final int count, @NotNull final FutureConsumer<Integer> consumer) {
      wrapped.materializeSkip(count, new CancellableFutureConsumer<Integer>() {
        @Override
        public void cancellableAccept(final Integer skipped) throws Exception {
          index += skipped;
          consumer.accept(skipped);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public int weightElements() {
      return elementsConsumers.isEmpty() ? wrapped.weightElements() : 1;
    }

    @Override
    public int weightHasNext() {
      return wrapped.weightHasNext();
    }

    @Override
    public int weightNext() {
      return wrapped.weightNext();
    }

    @Override
    public int weightNextWhile() {
      return wrapped.weightNextWhile();
    }

    @Override
    public int weightSkip() {
      return wrapped.weightSkip();
    }
  }
}
