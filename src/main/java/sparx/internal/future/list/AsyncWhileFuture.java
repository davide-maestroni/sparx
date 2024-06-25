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

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.concurrent.ExecutionContext.Task;
import sparx.internal.future.AsyncConsumer;
import sparx.internal.future.IndexedAsyncConsumer;
import sparx.util.DeadLockException;
import sparx.util.function.IndexedConsumer;
import sparx.util.function.IndexedPredicate;

public class AsyncWhileFuture<E> implements Future<Void> {

  private static final int STATUS_CANCELLED = 2;
  private static final int STATUS_DONE = 1;
  private static final int STATUS_RUNNING = 0;

  private final ExecutionContext context;
  private final AtomicInteger status = new AtomicInteger(STATUS_RUNNING);
  private final String taskID;

  private volatile Exception error;

  public AsyncWhileFuture(@NotNull final ExecutionContext context, @NotNull final String taskID,
      @NotNull final ListAsyncMaterializer<E> materializer,
      @NotNull final IndexedPredicate<? super E> predicate) {
    this.context = context;
    this.taskID = taskID;
    if (context.isCurrent()) {
      if (!materializer.isDone()) {
        throw new DeadLockException("cannot wait on the future own execution context");
      }
      materializer.materializeElements(new AsyncConsumer<List<E>>() {
        @Override
        public void accept(final java.util.List<E> elements) throws Exception {
          int i = 0;
          for (final E element : elements) {
            if (!predicate.test(i++, element)) {
              break;
            }
          }
          synchronized (status) {
            status.compareAndSet(STATUS_RUNNING, STATUS_DONE);
            status.notifyAll();
          }
        }

        @Override
        public void error(@NotNull final Exception error) {
          synchronized (status) {
            if (status.compareAndSet(STATUS_RUNNING, STATUS_DONE)) {
              AsyncWhileFuture.this.error = error;
            }
            status.notifyAll();
          }
        }
      });
    } else {
      context.scheduleAfter(new Task() {
        @Override
        public void run() {
          materializer.materializeElement(0, new IndexedAsyncConsumer<E>() {
            @Override
            public void accept(final int size, final int index, final E element) throws Exception {
              if (isCancelled()) {
                throw getCancelException();
              }
              if (predicate.test(index, element)) {
                materializer.materializeElement(index + 1, this);
              } else {
                synchronized (status) {
                  status.compareAndSet(STATUS_RUNNING, STATUS_DONE);
                  status.notifyAll();
                }
              }
            }

            @Override
            public void complete(final int size) {
              synchronized (status) {
                status.compareAndSet(STATUS_RUNNING, STATUS_DONE);
                status.notifyAll();
              }
            }

            @Override
            public void error(@NotNull final Exception error) {
              synchronized (status) {
                if (status.compareAndSet(STATUS_RUNNING, STATUS_DONE)) {
                  AsyncWhileFuture.this.error = error;
                }
                status.notifyAll();
              }
            }
          });
        }

        @Override
        public @NotNull String taskID() {
          return taskID;
        }

        @Override
        public int weight() {
          return materializer.weightElement();
        }
      });
    }
  }

  public AsyncWhileFuture(@NotNull final ExecutionContext context, @NotNull final String taskID,
      @NotNull final ListAsyncMaterializer<E> materializer,
      @NotNull final IndexedPredicate<? super E> condition,
      @NotNull final IndexedConsumer<? super E> consumer) {
    this.context = context;
    this.taskID = taskID;
    if (context.isCurrent()) {
      if (!materializer.isDone()) {
        throw new DeadLockException("cannot wait on the future own execution context");
      }
      materializer.materializeElements(new AsyncConsumer<List<E>>() {
        @Override
        public void accept(final java.util.List<E> elements) throws Exception {
          int i = 0;
          for (final E element : elements) {
            if (condition.test(i, element)) {
              consumer.accept(i++, element);
            } else {
              break;
            }
          }
          synchronized (status) {
            status.compareAndSet(STATUS_RUNNING, STATUS_DONE);
            status.notifyAll();
          }
        }

        @Override
        public void error(@NotNull final Exception error) {
          synchronized (status) {
            if (status.compareAndSet(STATUS_RUNNING, STATUS_DONE)) {
              AsyncWhileFuture.this.error = error;
            }
            status.notifyAll();
          }
        }
      });
    } else {
      context.scheduleAfter(new Task() {
        @Override
        public void run() {
          materializer.materializeElement(0, new IndexedAsyncConsumer<E>() {
            @Override
            public void accept(final int size, final int index, final E element) throws Exception {
              if (isCancelled()) {
                throw getCancelException();
              }
              if (condition.test(index, element)) {
                consumer.accept(index, element);
                materializer.materializeElement(index + 1, this);
              } else {
                synchronized (status) {
                  status.compareAndSet(STATUS_RUNNING, STATUS_DONE);
                  status.notifyAll();
                }
              }
            }

            @Override
            public void complete(final int size) {
              synchronized (status) {
                status.compareAndSet(STATUS_RUNNING, STATUS_DONE);
                status.notifyAll();
              }
            }

            @Override
            public void error(@NotNull final Exception error) {
              synchronized (status) {
                if (status.compareAndSet(STATUS_RUNNING, STATUS_DONE)) {
                  AsyncWhileFuture.this.error = error;
                }
                status.notifyAll();
              }
            }
          });
        }

        @Override
        public @NotNull String taskID() {
          return taskID;
        }

        @Override
        public int weight() {
          return materializer.weightElement();
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
    synchronized (status) {
      while (!isDone()) {
        status.wait();
      }
      if (isCancelled()) {
        throw getCancelException();
      }
      final Exception error = this.error;
      if (error instanceof InterruptedException) {
        throw (InterruptedException) error;
      } else if (error != null) {
        throw new ExecutionException(error);
      }
    }
    return null;
  }

  @Override
  public Void get(final long timeout, @NotNull final TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    if (context.isCurrent() && !isDone()) {
      throw new DeadLockException("cannot wait on the future own execution context");
    }
    final long startTimeMillis = System.currentTimeMillis();
    synchronized (status) {
      long timeoutMillis = unit.toMillis(timeout);
      while (!isDone() && timeoutMillis > 0) {
        status.wait(timeoutMillis);
        timeoutMillis -= System.currentTimeMillis() - startTimeMillis;
      }
      final int statusCode = status.get();
      if (statusCode == STATUS_RUNNING) {
        throw new TimeoutException("timeout after " + unit.toMillis(timeout) + " ms");
      }
      if (statusCode == STATUS_CANCELLED) {
        throw getCancelException();
      }
      final Exception error = this.error;
      if (error instanceof InterruptedException) {
        throw (InterruptedException) error;
      } else if (error != null) {
        throw new ExecutionException(error);
      }
    }
    return null;
  }

  private @NotNull CancellationException getCancelException() {
    return error instanceof CancellationException ? (CancellationException) error
        : new CancellationException();
  }
}
