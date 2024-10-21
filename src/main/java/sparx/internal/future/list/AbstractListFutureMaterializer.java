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
import static sparx.internal.future.FutureConsumers.safeConsumeError;

import java.util.ArrayList;
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
import sparx.util.annotation.NotNegative;

public abstract class AbstractListFutureMaterializer<E> implements ListFutureMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      AbstractListFutureMaterializer.class.getName());

  protected static final int STATUS_CANCELLED = 3;
  protected static final int STATUS_FAILED = 2;
  protected static final int STATUS_DONE = 1;
  protected static final int STATUS_RUNNING = 0;

  final ExecutionContext context;

  private final AtomicInteger status = new AtomicInteger(STATUS_RUNNING);

  private CancellationException cancelException;
  private ListFutureMaterializer<E> state;

  public AbstractListFutureMaterializer(@NotNull final ExecutionContext context) {
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
  public void materializeContains(final Object element,
      @NotNull final FutureConsumer<Boolean> consumer) {
    state.materializeContains(element, consumer);
  }

  @Override
  public void materializeElement(@NotNegative final int index,
      @NotNull final IndexedFutureConsumer<E> consumer) {
    state.materializeElement(index, consumer);
  }

  @Override
  public void materializeElements(@NotNull final FutureConsumer<List<E>> consumer) {
    state.materializeElements(consumer);
  }

  @Override
  public void materializeEmpty(@NotNull final FutureConsumer<Boolean> consumer) {
    state.materializeEmpty(consumer);
  }

  @Override
  public void materializeHasElement(@NotNegative final int index,
      @NotNull final FutureConsumer<Boolean> consumer) {
    state.materializeHasElement(index, consumer);
  }

  @Override
  public void materializeNextWhile(@NotNegative final int index,
      @NotNull final IndexedFuturePredicate<E> predicate) {
    state.materializeNextWhile(index, predicate);
  }

  @Override
  public void materializePrevWhile(@NotNegative final int index,
      @NotNull final IndexedFuturePredicate<E> predicate) {
    state.materializePrevWhile(index, predicate);
  }

  @Override
  public void materializeSize(@NotNull final FutureConsumer<Integer> consumer) {
    state.materializeSize(consumer);
  }

  @Override
  public int weightContains() {
    return state.weightContains();
  }

  @Override
  public int weightElement() {
    return state.weightElement();
  }

  @Override
  public int weightElements() {
    return state.weightElements();
  }

  @Override
  public int weightHasElement() {
    return state.weightHasElement();
  }

  @Override
  public int weightEmpty() {
    return state.weightEmpty();
  }

  @Override
  public int weightNextWhile() {
    return state.weightNextWhile();
  }

  @Override
  public int weightPrevWhile() {
    return state.weightPrevWhile();
  }

  @Override
  public int weightSize() {
    return state.weightSize();
  }

  @NotNull
  protected final ListFutureMaterializer<E> getState() {
    return state;
  }

  @NotNull
  protected final ListFutureMaterializer<E> setCancelled(
      @NotNull final CancellationException exception) {
    return setState(new CancelledListFutureMaterializer<E>(exception), STATUS_CANCELLED);
  }

  @NotNull
  protected final ListFutureMaterializer<E> setDone(
      @NotNull final ListFutureMaterializer<E> newState) {
    return setState(newState, STATUS_DONE);
  }

  @NotNull
  protected final ListFutureMaterializer<E> setFailed(@NotNull final Exception error) {
    return setState(new FailedListFutureMaterializer<E>(error), STATUS_FAILED);
  }

  @NotNull
  protected final ListFutureMaterializer<E> setState(
      @NotNull final ListFutureMaterializer<E> newState) {
    return setState(newState, STATUS_RUNNING);
  }

  @NotNull
  private ListFutureMaterializer<E> setState(@NotNull final ListFutureMaterializer<E> newState,
      final int statusCode) {
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
      final CancellationException cancelException = AbstractListFutureMaterializer.this.cancelException;
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
      final CancellationException cancelException = AbstractListFutureMaterializer.this.cancelException;
      if (cancelException != null) {
        error(cancelException);
      } else {
        cancellableAccept(size, index, param);
      }
    }

    @Override
    public void complete(final int size) throws Exception {
      final CancellationException cancelException = AbstractListFutureMaterializer.this.cancelException;
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
      final CancellationException cancelException = AbstractListFutureMaterializer.this.cancelException;
      if (cancelException != null) {
        error(cancelException);
      } else {
        cancellableComplete(size);
      }
    }

    @Override
    public boolean test(final int size, final int index, final P param) throws Exception {
      final CancellationException cancelException = AbstractListFutureMaterializer.this.cancelException;
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
      final CancellationException cancelException = AbstractListFutureMaterializer.this.cancelException;
      if (cancelException != null) {
        error(cancelException);
      } else {
        cancellableAccept(param);
      }
    }

    @Override
    public void accept(final int size, final int index, final P2 param) throws Exception {
      final CancellationException cancelException = AbstractListFutureMaterializer.this.cancelException;
      if (cancelException != null) {
        error(cancelException);
      } else {
        cancellableAccept(size, index, param);
      }
    }

    @Override
    public void complete(final int size) throws Exception {
      final CancellationException cancelException = AbstractListFutureMaterializer.this.cancelException;
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

  protected class WrappingState implements ListFutureMaterializer<E> {

    private final AtomicReference<CancellationException> cancelException;
    private final ArrayList<FutureConsumer<List<E>>> elementsConsumers = new ArrayList<FutureConsumer<List<E>>>(
        2);
    private final int knownSize;
    private final ListFutureMaterializer<E> wrapped;

    private int wrappedSize;

    protected WrappingState(@NotNull final ListFutureMaterializer<E> wrapped,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      this.wrapped = wrapped;
      this.cancelException = cancelException;
      wrappedSize = knownSize = wrapped.knownSize();
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
    public void materializeContains(final Object element,
        @NotNull final FutureConsumer<Boolean> consumer) {
      wrapped.materializeContains(element, new CancellableFutureConsumer<Boolean>() {
        @Override
        public void cancellableAccept(final Boolean contains) throws Exception {
          consumer.accept(contains);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeElement(@NotNegative final int index,
        @NotNull final IndexedFutureConsumer<E> consumer) {
      wrapped.materializeElement(index, new CancellableIndexedFutureConsumer<E>() {
        @Override
        public void cancellableAccept(final int size, final int index, final E element)
            throws Exception {
          wrappedSize = Math.max(wrappedSize, size);
          consumer.accept(size, index, element);
        }

        @Override
        public void cancellableComplete(final int size) throws Exception {
          wrappedSize = size;
          consumer.complete(size);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeElements(@NotNull final FutureConsumer<List<E>> consumer) {
      final ArrayList<FutureConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      elementsConsumers.add(consumer);
      if (elementsConsumers.size() == 1) {
        wrapped.materializeElements(new CancellableFutureConsumer<List<E>>() {
          @Override
          public void cancellableAccept(final List<E> elements) {
            setDone(new ListToListFutureMaterializer<E>(elements, context));
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
    public void materializeEmpty(@NotNull final FutureConsumer<Boolean> consumer) {
      if (wrappedSize >= 0) {
        safeConsume(consumer, wrappedSize == 0, LOGGER);
      } else {
        wrapped.materializeEmpty(new CancellableFutureConsumer<Boolean>() {
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
    public void materializeHasElement(@NotNegative final int index,
        @NotNull final FutureConsumer<Boolean> consumer) {
      if (wrappedSize >= 0) {
        safeConsume(consumer, index < wrappedSize, LOGGER);
      } else {
        wrapped.materializeHasElement(index, new CancellableFutureConsumer<Boolean>() {
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
    public void materializeNextWhile(@NotNegative final int index,
        @NotNull final IndexedFuturePredicate<E> predicate) {
      wrapped.materializeNextWhile(index, new CancellableIndexedFuturePredicate<E>() {
        @Override
        public void cancellableComplete(final int size) throws Exception {
          wrappedSize = size;
          predicate.complete(size);
        }

        @Override
        public boolean cancellableTest(final int size, final int index, final E element)
            throws Exception {
          wrappedSize = Math.max(wrappedSize, size);
          return predicate.test(size, index, element);
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
      wrapped.materializePrevWhile(index, new CancellableIndexedFuturePredicate<E>() {
        @Override
        public void cancellableComplete(final int size) throws Exception {
          wrappedSize = Math.max(wrappedSize, size);
          predicate.complete(size);
        }

        @Override
        public boolean cancellableTest(final int size, final int index, final E element)
            throws Exception {
          wrappedSize = Math.max(wrappedSize, size);
          return predicate.test(size, index, element);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          predicate.error(error);
        }
      });
    }

    @Override
    public void materializeSize(@NotNull final FutureConsumer<Integer> consumer) {
      if (wrappedSize >= 0) {
        safeConsume(consumer, wrappedSize, LOGGER);
      } else {
        wrapped.materializeSize(new CancellableFutureConsumer<Integer>() {
          @Override
          public void cancellableAccept(final Integer size) throws Exception {
            wrappedSize = size;
            consumer.accept(size);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
    }

    @Override
    public int weightContains() {
      return wrapped.weightContains();
    }

    @Override
    public int weightElement() {
      return wrapped.weightElement();
    }

    @Override
    public int weightElements() {
      return elementsConsumers.isEmpty() ? wrapped.weightElements() : 1;
    }

    @Override
    public int weightEmpty() {
      return wrappedSize < 0 ? wrapped.weightEmpty() : 1;
    }

    @Override
    public int weightHasElement() {
      return wrappedSize < 0 ? wrapped.weightHasElement() : 1;
    }

    @Override
    public int weightSize() {
      return wrappedSize < 0 ? wrapped.weightSize() : 1;
    }

    @Override
    public int weightNextWhile() {
      return wrapped.weightNextWhile();
    }

    @Override
    public int weightPrevWhile() {
      return wrapped.weightPrevWhile();
    }
  }
}
