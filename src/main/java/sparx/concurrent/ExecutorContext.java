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
package sparx.concurrent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionScope.FutureRegistry;
import sparx.concurrent.backpressure.BackpressureStrategy;
import sparx.function.Consumer;
import sparx.function.Function;
import sparx.logging.Log;
import sparx.util.Nothing;
import sparx.util.Require;
import sparx.util.UncheckedException;
import sparx.util.UncheckedException.UncheckedInterruptedException;

public class ExecutorContext implements ExecutionContext {

  private static final FutureRegistry DUMMY_REGISTRY = new FutureRegistry() {
    @Override
    public void register(@NotNull final StreamingFuture<?> future) {
    }
  };

  private final FutureRegistry registry;
  private final Scheduler scheduler;
  private final HashMap<String, Object> values = new HashMap<String, Object>();

  public static @NotNull ExecutorContext of(@NotNull final Executor executor) {
    return of(executor, Integer.MAX_VALUE);
  }

  public static @NotNull ExecutorContext of(@NotNull final Executor executor,
      @NotNull final BackpressureStrategy backpressureStrategy) {
    return of(executor, Integer.MAX_VALUE, backpressureStrategy);
  }

  public static @NotNull ExecutorContext of(@NotNull final Executor executor,
      final int minThroughput) {
    final FailingExecutor failingExecutor = new FailingExecutor(executor);
    final Scheduler scheduler = Scheduler.of(failingExecutor, minThroughput);
    return new ExecutorContext(scheduler, failingExecutor);
  }

  public static @NotNull ExecutorContext of(@NotNull final Executor executor,
      final int minThroughput, @NotNull final BackpressureStrategy backpressureStrategy) {
    final FailingExecutor failingExecutor = new FailingExecutor(executor);
    final Scheduler scheduler = Scheduler.of(failingExecutor, minThroughput, backpressureStrategy);
    return new ExecutorContext(scheduler, failingExecutor);
  }

  public static @NotNull ExecutorContext trampoline() {
    return new ExecutorContext(Scheduler.trampoline(), DUMMY_REGISTRY);
  }

  public static @NotNull ExecutorContext trampoline(
      @NotNull final BackpressureStrategy backpressureStrategy) {
    return new ExecutorContext(Scheduler.trampoline(backpressureStrategy), DUMMY_REGISTRY);
  }

  ExecutorContext(@NotNull final Scheduler scheduler, @NotNull final FutureRegistry registry) {
    this.scheduler = Require.notNull(scheduler, "scheduler");
    this.registry = registry;
  }

  @Override
  public int minThroughput() {
    return scheduler.minThroughput();
  }

  @Override
  public int pendingCount() {
    return scheduler.pendingCount();
  }

  @Override
  public @NotNull <V, F extends TupleFuture<V, ?>, U> StreamingFuture<U> call(
      @NotNull final F future,
      @NotNull final Function<? super F, ? extends SignalFuture<U>> function) {
    return new ExecutionScope(this, values, scheduler, registry).call(future, function);
  }

  @Override
  public @NotNull <V, F extends TupleFuture<V, ?>> StreamingFuture<Nothing> run(
      @NotNull final F future, @NotNull final Consumer<? super F> consumer) {
    return new ExecutionScope(this, values, scheduler, registry).run(future, consumer);
  }

  private static class FailingExecutor implements Executor, FutureRegistry {

    private final WeakHashMap<StreamingFuture<?>, Void> futures = new WeakHashMap<StreamingFuture<?>, Void>();
    private final Object lock = new Object();
    private final Executor wrapped;

    private FailingExecutor(@NotNull final Executor wrapped) {
      this.wrapped = wrapped;
    }

    @Override
    public void execute(@NotNull final Runnable command) {
      try {
        wrapped.execute(command);
      } catch (final UncheckedInterruptedException e) {
        throw e;
      } catch (final RuntimeException e) {
        final ArrayList<StreamingFuture<?>> toCancel;
        synchronized (lock) {
          final WeakHashMap<StreamingFuture<?>, Void> futures = this.futures;
          toCancel = new ArrayList<StreamingFuture<?>>(futures.keySet());
          futures.clear();
        }
        for (final StreamingFuture<?> future : toCancel) {
          try {
            future.fail(e);
          } catch (final RuntimeException ex) {
            Log.err(ExecutorContext.class, "Uncaught exception: %s", Log.printable(ex));
          }
        }
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public void register(@NotNull final StreamingFuture<?> future) {
      synchronized (lock) {
        futures.put(future, null);
      }
    }
  }
}
