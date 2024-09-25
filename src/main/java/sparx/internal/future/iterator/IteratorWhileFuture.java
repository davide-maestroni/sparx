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

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ContextTask;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.FutureConsumer;
import sparx.internal.future.IndexedFuturePredicate;
import sparx.util.DeadLockException;
import sparx.util.function.IndexedConsumer;
import sparx.util.function.IndexedPredicate;

public class IteratorWhileFuture<E> implements Future<Void> {

  private static final int STATUS_CANCELLED = 2;
  private static final int STATUS_DONE = 1;
  private static final int STATUS_RUNNING = 0;

  private final AtomicReference<CancellationException> cancelException;
  private final ExecutionContext context;
  private final AtomicInteger status = new AtomicInteger(STATUS_RUNNING);
  private final String taskID;

  private volatile Exception error;

  public IteratorWhileFuture(@NotNull final ExecutionContext context, @NotNull final String taskID,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final IteratorFutureMaterializer<E> materializer,
      @NotNull final IndexedPredicate<? super E> predicate) {
    this.context = context;
    this.taskID = taskID;
    this.cancelException = cancelException;
    if (context.isCurrent() && materializer.isDone()) {
      materializer.materializeElements(new FutureConsumer<List<E>>() {
        @Override
        public void accept(final List<E> elements) throws Exception {
          int i = 0;
          for (final E element : elements) {
            if (!predicate.test(i++, element)) {
              break;
            }
          }
          synchronized (cancelException) {
            status.compareAndSet(STATUS_RUNNING, STATUS_DONE);
            cancelException.notifyAll();
          }
        }

        @Override
        public void error(@NotNull final Exception error) {
          synchronized (cancelException) {
            if (status.compareAndSet(STATUS_RUNNING, STATUS_DONE)) {
              IteratorWhileFuture.this.error = error;
            }
            cancelException.notifyAll();
          }
        }
      });
    } else {
      context.scheduleAfter(new ContextTask(context) {
        @Override
        public @NotNull String taskID() {
          return taskID;
        }

        @Override
        public int weight() {
          return materializer.weightNextWhile();
        }

        @Override
        protected void runWithContext() {
          materializer.materializeNextWhile(new IndexedFuturePredicate<E>() {
            @Override
            public void complete(final int size) {
              synchronized (cancelException) {
                status.compareAndSet(STATUS_RUNNING, STATUS_DONE);
                cancelException.notifyAll();
              }
            }

            @Override
            public boolean test(final int size, final int index, final E element) throws Exception {
              if (isCancelled()) {
                throw getCancelException();
              }
              if (predicate.test(index, element)) {
                return true;
              }
              synchronized (cancelException) {
                status.compareAndSet(STATUS_RUNNING, STATUS_DONE);
                cancelException.notifyAll();
              }
              return false;
            }

            @Override
            public void error(@NotNull final Exception error) {
              synchronized (cancelException) {
                if (status.compareAndSet(STATUS_RUNNING, STATUS_DONE)) {
                  IteratorWhileFuture.this.error = error;
                }
                cancelException.notifyAll();
              }
            }
          });
        }
      });
    }
  }

  public IteratorWhileFuture(@NotNull final ExecutionContext context, @NotNull final String taskID,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final IteratorFutureMaterializer<E> materializer,
      @NotNull final IndexedPredicate<? super E> condition,
      @NotNull final IndexedConsumer<? super E> consumer) {
    this.context = context;
    this.taskID = taskID;
    this.cancelException = cancelException;
    if (context.isCurrent() && materializer.isDone()) {
      materializer.materializeElements(new FutureConsumer<List<E>>() {
        @Override
        public void accept(final List<E> elements) throws Exception {
          int i = 0;
          for (final E element : elements) {
            if (condition.test(i, element)) {
              consumer.accept(i++, element);
            } else {
              break;
            }
          }
          synchronized (cancelException) {
            status.compareAndSet(STATUS_RUNNING, STATUS_DONE);
            cancelException.notifyAll();
          }
        }

        @Override
        public void error(@NotNull final Exception error) {
          synchronized (cancelException) {
            if (status.compareAndSet(STATUS_RUNNING, STATUS_DONE)) {
              IteratorWhileFuture.this.error = error;
            }
            cancelException.notifyAll();
          }
        }
      });
    } else {
      context.scheduleAfter(new ContextTask(context) {
        @Override
        public @NotNull String taskID() {
          return taskID;
        }

        @Override
        public int weight() {
          return materializer.weightNextWhile();
        }

        @Override
        protected void runWithContext() {
          materializer.materializeNextWhile(new IndexedFuturePredicate<E>() {
            @Override
            public void complete(final int size) {
              synchronized (cancelException) {
                status.compareAndSet(STATUS_RUNNING, STATUS_DONE);
                cancelException.notifyAll();
              }
            }

            @Override
            public boolean test(final int size, final int index, final E element) throws Exception {
              if (isCancelled()) {
                throw getCancelException();
              }
              if (condition.test(index, element)) {
                consumer.accept(index, element);
                return true;
              }
              synchronized (cancelException) {
                status.compareAndSet(STATUS_RUNNING, STATUS_DONE);
                cancelException.notifyAll();
              }
              return false;
            }

            @Override
            public void error(@NotNull final Exception error) {
              synchronized (cancelException) {
                if (status.compareAndSet(STATUS_RUNNING, STATUS_DONE)) {
                  IteratorWhileFuture.this.error = error;
                }
                cancelException.notifyAll();
              }
            }
          });
        }
      });
    }
  }

  @Override
  public boolean cancel(final boolean mayInterruptIfRunning) {
    if (status.compareAndSet(STATUS_RUNNING, STATUS_CANCELLED)) {
      error = new CancellationException();
      if (mayInterruptIfRunning) {
        context.interruptTask(taskID);
      }
      synchronized (cancelException) {
        cancelException.notifyAll();
      }
      return true;
    }
    return false;
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
  public Void get() throws InterruptedException, ExecutionException {
    if (context.isCurrent() && !isDone()) {
      throw new DeadLockException("cannot wait on the future own execution context");
    }
    synchronized (cancelException) {
      while (!isDone()) {
        cancelException.wait();
        final CancellationException exception = cancelException.get();
        if (exception != null) {
          throw new ExecutionException(exception);
        }
      }
      if (isCancelled()) {
        throw getCancelException();
      }
      final Exception error = this.error;
      if (error != null) {
        throw new ExecutionException(error);
      }
    }
    return null;
  }

  @Override
  public Void get(final long timeout, @NotNull final TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    if (timeout <= 0) {
      return get();
    }
    if (context.isCurrent() && !isDone()) {
      throw new DeadLockException("cannot wait on the future own execution context");
    }
    final long startTimeMillis = System.currentTimeMillis();
    synchronized (cancelException) {
      long timeoutMillis = unit.toMillis(timeout);
      while (!isDone() && timeoutMillis > 0) {
        cancelException.wait(timeoutMillis);
        timeoutMillis -= System.currentTimeMillis() - startTimeMillis;
        final CancellationException exception = cancelException.get();
        if (exception != null) {
          throw new ExecutionException(exception);
        }
      }
      final int statusCode = status.get();
      if (statusCode == STATUS_RUNNING) {
        throw new TimeoutException("timeout after " + unit.toMillis(timeout) + " ms");
      }
      if (statusCode == STATUS_CANCELLED) {
        throw getCancelException();
      }
      final Exception error = this.error;
      if (error != null) {
        throw new ExecutionException(error);
      }
    }
    return null;
  }

  private @NotNull CancellationException getCancelException() {
    return error instanceof CancellationException
        ? (CancellationException) new CancellationException().initCause(error)
        : new CancellationException();
  }
}
