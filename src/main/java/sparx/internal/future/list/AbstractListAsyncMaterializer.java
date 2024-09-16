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
import static sparx.internal.future.AsyncConsumers.safeConsumeError;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ContextTask;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.AsyncConsumer;
import sparx.internal.future.IndexedAsyncConsumer;
import sparx.internal.future.IndexedAsyncPredicate;

public abstract class AbstractListAsyncMaterializer<E> implements ListAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      AbstractListAsyncMaterializer.class.getName());

  protected static final int STATUS_CANCELLED = 3;
  protected static final int STATUS_FAILED = 2;
  protected static final int STATUS_DONE = 1;
  protected static final int STATUS_RUNNING = 0;

  final ExecutionContext context;
  final AtomicInteger status;

  private CancellationException cancelException;
  private ListAsyncMaterializer<E> state;

  public AbstractListAsyncMaterializer(@NotNull final ExecutionContext context,
      @NotNull final AtomicInteger status) {
    this.context = context;
    this.status = status;
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
      @NotNull final AsyncConsumer<Boolean> consumer) {
    state.materializeContains(element, consumer);
  }

  @Override
  public void materializeElement(final int index, @NotNull final IndexedAsyncConsumer<E> consumer) {
    state.materializeElement(index, consumer);
  }

  @Override
  public void materializeElements(@NotNull final AsyncConsumer<List<E>> consumer) {
    state.materializeElements(consumer);
  }

  @Override
  public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
    state.materializeEmpty(consumer);
  }

  @Override
  public void materializeHasElement(final int index,
      @NotNull final AsyncConsumer<Boolean> consumer) {
    state.materializeHasElement(index, consumer);
  }

  @Override
  public void materializeNextWhile(final int index,
      @NotNull final IndexedAsyncPredicate<E> predicate) {
    state.materializeNextWhile(index, predicate);
  }

  @Override
  public void materializePrevWhile(final int index,
      @NotNull final IndexedAsyncPredicate<E> predicate) {
    state.materializePrevWhile(index, predicate);
  }

  @Override
  public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
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
  protected final ListAsyncMaterializer<E> getState() {
    return state;
  }

  @NotNull
  protected final ListAsyncMaterializer<E> setCancelled(
      @NotNull final CancellationException exception) {
    return setState(new CancelledListAsyncMaterializer<E>(exception), STATUS_CANCELLED);
  }

  @NotNull
  protected final ListAsyncMaterializer<E> setDone(
      @NotNull final ListAsyncMaterializer<E> newState) {
    return setState(newState, STATUS_DONE);
  }

  @NotNull
  protected final ListAsyncMaterializer<E> setFailed(@NotNull final Exception error) {
    return setState(new FailedListAsyncMaterializer<E>(error), STATUS_FAILED);
  }

  @NotNull
  protected final ListAsyncMaterializer<E> setState(
      @NotNull final ListAsyncMaterializer<E> newState) {
    return setState(newState, STATUS_RUNNING);
  }

  @NotNull
  private ListAsyncMaterializer<E> setState(@NotNull final ListAsyncMaterializer<E> newState,
      final int statusCode) {
    if (status.compareAndSet(STATUS_RUNNING, statusCode)) {
      state = newState;
    }
    return state;
  }

  protected abstract class CancellableAsyncConsumer<P> extends ContextTask implements
      AsyncConsumer<P> {

    protected CancellableAsyncConsumer() {
      super(context);
    }

    @Override
    public void accept(final P param) throws Exception {
      final CancellationException cancelException = AbstractListAsyncMaterializer.this.cancelException;
      if (cancelException != null) {
        error(cancelException);
      } else {
        cancellableAccept(param);
      }
    }

    public void cancellableAccept(final P param) throws Exception {
    }
  }

  protected abstract class CancellableIndexedAsyncConsumer<P> extends ContextTask implements
      IndexedAsyncConsumer<P> {

    protected CancellableIndexedAsyncConsumer() {
      super(context);
    }

    @Override
    public void accept(final int size, final int index, final P param) throws Exception {
      final CancellationException cancelException = AbstractListAsyncMaterializer.this.cancelException;
      if (cancelException != null) {
        error(cancelException);
      } else {
        cancellableAccept(size, index, param);
      }
    }

    @Override
    public void complete(final int size) throws Exception {
      final CancellationException cancelException = AbstractListAsyncMaterializer.this.cancelException;
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

  protected abstract class CancellableIndexedAsyncPredicate<P> extends ContextTask implements
      IndexedAsyncPredicate<P> {

    protected CancellableIndexedAsyncPredicate() {
      super(context);
    }

    @Override
    public void complete(final int size) throws Exception {
      final CancellationException cancelException = AbstractListAsyncMaterializer.this.cancelException;
      if (cancelException != null) {
        error(cancelException);
      } else {
        cancellableComplete(size);
      }
    }

    @Override
    public boolean test(final int size, final int index, final P param) throws Exception {
      final CancellationException cancelException = AbstractListAsyncMaterializer.this.cancelException;
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

  protected abstract class CancellableMultiAsyncConsumer<P1, P2> extends ContextTask implements
      AsyncConsumer<P1>, IndexedAsyncConsumer<P2> {

    protected CancellableMultiAsyncConsumer() {
      super(context);
    }

    @Override
    public void accept(final P1 param) throws Exception {
      final CancellationException cancelException = AbstractListAsyncMaterializer.this.cancelException;
      if (cancelException != null) {
        error(cancelException);
      } else {
        cancellableAccept(param);
      }
    }

    @Override
    public void accept(final int size, final int index, final P2 param) throws Exception {
      final CancellationException cancelException = AbstractListAsyncMaterializer.this.cancelException;
      if (cancelException != null) {
        error(cancelException);
      } else {
        cancellableAccept(size, index, param);
      }
    }

    @Override
    public void complete(final int size) throws Exception {
      final CancellationException cancelException = AbstractListAsyncMaterializer.this.cancelException;
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

  protected class WrappingState implements ListAsyncMaterializer<E> {

    private final AtomicReference<CancellationException> cancelException;
    private final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = new ArrayList<AsyncConsumer<List<E>>>(
        2);
    private final int knownSize;
    private final ListAsyncMaterializer<E> wrapped;

    private int wrappedSize;

    protected WrappingState(@NotNull final ListAsyncMaterializer<E> wrapped,
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
      final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      for (final AsyncConsumer<List<E>> consumer : elementsConsumers) {
        safeConsumeError(consumer, exception, LOGGER);
      }
      elementsConsumers.clear();
    }

    @Override
    public void materializeContains(final Object element,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      wrapped.materializeContains(element, new CancellableAsyncConsumer<Boolean>() {
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
    public void materializeElement(final int index,
        @NotNull final IndexedAsyncConsumer<E> consumer) {
      if (index < 0) {
        safeConsumeError(consumer, new IndexOutOfBoundsException(Integer.toString(index)), LOGGER);
      } else {
        wrapped.materializeElement(index, new CancellableIndexedAsyncConsumer<E>() {
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
    }

    @Override
    public void materializeElements(@NotNull final AsyncConsumer<List<E>> consumer) {
      final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      elementsConsumers.add(consumer);
      if (elementsConsumers.size() == 1) {
        wrapped.materializeElements(new CancellableAsyncConsumer<List<E>>() {
          @Override
          public void cancellableAccept(final List<E> elements) {
            setDone(new ListToListAsyncMaterializer<E>(elements, context));
            for (final AsyncConsumer<List<E>> consumer : elementsConsumers) {
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
            for (final AsyncConsumer<List<E>> consumer : elementsConsumers) {
              safeConsumeError(consumer, error, LOGGER);
            }
            elementsConsumers.clear();
          }
        });
      }
    }

    @Override
    public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
      if (wrappedSize >= 0) {
        safeConsume(consumer, wrappedSize == 0, LOGGER);
      } else {
        wrapped.materializeEmpty(new CancellableAsyncConsumer<Boolean>() {
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
    public void materializeHasElement(final int index,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      if (index < 0) {
        safeConsume(consumer, false, LOGGER);
      } else if (wrappedSize >= 0) {
        safeConsume(consumer, index < wrappedSize, LOGGER);
      } else {
        wrapped.materializeHasElement(index, new CancellableAsyncConsumer<Boolean>() {
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
    public void materializeNextWhile(final int index,
        @NotNull final IndexedAsyncPredicate<E> predicate) {
      wrapped.materializeNextWhile(index, new CancellableIndexedAsyncPredicate<E>() {
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
    public void materializePrevWhile(final int index,
        @NotNull final IndexedAsyncPredicate<E> predicate) {
      wrapped.materializePrevWhile(index, new CancellableIndexedAsyncPredicate<E>() {
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
    public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
      if (wrappedSize >= 0) {
        safeConsume(consumer, wrappedSize, LOGGER);
      } else {
        wrapped.materializeSize(new CancellableAsyncConsumer<Integer>() {
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
