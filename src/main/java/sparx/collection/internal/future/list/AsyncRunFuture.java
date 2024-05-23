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
package sparx.collection.internal.future.list;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import sparx.collection.internal.future.AsyncConsumer;
import sparx.concurrent.ExecutionContext;
import sparx.concurrent.ExecutionContext.Task;
import sparx.util.Require;

public class AsyncRunFuture<E> implements Future<Void> {

  private static final int STATUS_CANCELLED = 2;
  private static final int STATUS_DONE = 1;
  private static final int STATUS_RUNNING = 0;

  private final ExecutionContext context;
  private final AtomicInteger status = new AtomicInteger(STATUS_RUNNING);
  private final String taskID;

  private Exception error;

  public AsyncRunFuture(@NotNull final ExecutionContext context, @NotNull final String taskID,
      @NotNull final ListAsyncMaterializer<E> materializer) {
    this.context = context;
    this.taskID = Require.notNull(taskID, "taskID");
    context.scheduleAfter(new Task() {
      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        return 1;
      }

      @Override
      public void run() {
        materializer.materializeElements(new AsyncConsumer<List<E>>() {
          @Override
          public void accept(final List<E> param) {
            synchronized (status) {
              if (isCancelled()) {
                status.notifyAll();
                throw new CancellationException();
              }
              status.compareAndSet(STATUS_RUNNING, STATUS_DONE);
              status.notifyAll();
            }
          }

          @Override
          public void error(@NotNull final Exception error) {
            synchronized (status) {
              if (isCancelled()) {
                status.notifyAll();
                throw new CancellationException();
              }
              AsyncRunFuture.this.error = error;
              status.compareAndSet(STATUS_RUNNING, STATUS_DONE);
              status.notifyAll();
            }
          }
        });
      }
    });
  }

  @Override
  public boolean cancel(final boolean mayInterruptIfRunning) {
    if (status.compareAndSet(STATUS_RUNNING, STATUS_CANCELLED)) {
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
    synchronized (status) {
      while (!isDone()) {
        status.wait();
      }
      final Exception error = this.error;
      if (error instanceof InterruptedException) {
        throw (InterruptedException) error;
      } else if (error instanceof CancellationException) {
        throw (CancellationException) error;
      } else if (error != null) {
        throw new ExecutionException(error);
      }
    }
    return null;
  }

  @Override
  public Void get(final long timeout, @NotNull final TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    final long startTimeMillis = System.currentTimeMillis();
    synchronized (status) {
      long timeoutMillis = unit.toMillis(timeout);
      while (!isDone() && timeoutMillis > 0) {
        status.wait(timeoutMillis);
        timeoutMillis -= System.currentTimeMillis() - startTimeMillis;
      }
      if (!isDone()) {
        throw new TimeoutException("timeout after " + unit.toMillis(timeout) + " ms");
      }
      final Exception error = this.error;
      if (error instanceof InterruptedException) {
        throw (InterruptedException) error;
      } else if (error instanceof CancellationException) {
        throw (CancellationException) error;
      } else if (error != null) {
        throw new ExecutionException(error);
      }
    }
    return null;
  }
}
