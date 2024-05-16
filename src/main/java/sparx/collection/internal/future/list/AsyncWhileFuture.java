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

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import sparx.collection.internal.future.IndexedAsyncConsumer;
import sparx.util.Require;
import sparx.util.function.IndexedConsumer;
import sparx.util.function.IndexedPredicate;

public class AsyncWhileFuture<E> implements Future<Void> {

  private final AtomicBoolean isDone = new AtomicBoolean(false);
  private final ListAsyncMaterializer<E> materializer;

  private Exception error;

  public AsyncWhileFuture(@NotNull final ListAsyncMaterializer<E> materializer,
      @NotNull final IndexedPredicate<? super E> predicate) {
    Require.notNull(predicate, "predicate");
    this.materializer = materializer;
    materializer.materializeElement(0, new IndexedAsyncConsumer<E>() {
      @Override
      public void accept(final int size, final int index, final E param) throws Exception {
        if (predicate.test(index, param)) {
          materializer.materializeElement(index + 1, this);
        } else {
          synchronized (isDone) {
            isDone.set(true);
            isDone.notifyAll();
          }
        }
      }

      @Override
      public void complete(final int size) {
        synchronized (isDone) {
          isDone.set(true);
          isDone.notifyAll();
        }
      }

      @Override
      public void error(final int index, @NotNull final Exception error) {
        synchronized (isDone) {
          AsyncWhileFuture.this.error = error;
          isDone.set(true);
          isDone.notifyAll();
        }
      }
    });
  }

  public AsyncWhileFuture(@NotNull final ListAsyncMaterializer<E> materializer,
      @NotNull final IndexedPredicate<? super E> condition,
      @NotNull final IndexedConsumer<? super E> consumer) {
    Require.notNull(condition, "condition");
    Require.notNull(consumer, "consumer");
    this.materializer = materializer;
    materializer.materializeElement(0, new IndexedAsyncConsumer<E>() {
      @Override
      public void accept(final int size, final int index, final E param) throws Exception {
        if (condition.test(index, param)) {
          consumer.accept(index, param);
          materializer.materializeElement(index + 1, this);
        } else {
          synchronized (isDone) {
            isDone.set(true);
            isDone.notifyAll();
          }
        }
      }

      @Override
      public void complete(final int size) {
        synchronized (isDone) {
          isDone.set(true);
          isDone.notifyAll();
        }
      }

      @Override
      public void error(final int index, @NotNull final Exception error) {
        synchronized (isDone) {
          AsyncWhileFuture.this.error = error;
          isDone.set(true);
          isDone.notifyAll();
        }
      }
    });
  }

  @Override
  public boolean cancel(final boolean mayInterruptIfRunning) {
    return materializer.cancel(mayInterruptIfRunning);
  }

  @Override
  public boolean isCancelled() {
    return materializer.isCancelled();
  }

  @Override
  public boolean isDone() {
    return isDone.get();
  }

  @Override
  public Void get() throws InterruptedException, ExecutionException {
    synchronized (isDone) {
      while (!isDone.get()) {
        isDone.wait();
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
    synchronized (isDone) {
      long timeoutMillis = unit.toMillis(timeout);
      while (!isDone.get() && timeoutMillis > 0) {
        isDone.wait(timeoutMillis);
        timeoutMillis -= System.currentTimeMillis() - startTimeMillis;
      }
      if (!isDone.get()) {
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
