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

import java.util.concurrent.Executor;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.backpressure.BackpressureStrategy;
import sparx.function.Consumer;
import sparx.function.Function;
import sparx.util.Nothing;
import sparx.util.Requires;

public class ExecutorContext implements ExecutionContext {

  private final Scheduler scheduler;

  public static @NotNull ExecutorContext of(@NotNull final Executor executor) {
    return of(executor, Integer.MAX_VALUE);
  }

  public static @NotNull ExecutorContext of(@NotNull final Executor executor,
      @NotNull final BackpressureStrategy backpressureStrategy) {
    return of(executor, Integer.MAX_VALUE, backpressureStrategy);
  }

  public static @NotNull ExecutorContext of(@NotNull final Executor executor,
      final int minThroughput) {
    // TODO: handle executor rejection: wrap executor and make every scope fail?
    return new ExecutorContext(Scheduler.of(executor, minThroughput));
  }

  public static @NotNull ExecutorContext of(@NotNull final Executor executor,
      final int minThroughput, @NotNull final BackpressureStrategy backpressureStrategy) {
    // TODO: handle executor rejection: wrap executor and make every scope fail?
    return new ExecutorContext(Scheduler.of(executor, minThroughput, backpressureStrategy));
  }

  public static @NotNull ExecutorContext trampoline() {
    return new ExecutorContext(Scheduler.trampoline());
  }

  public static @NotNull ExecutorContext trampoline(
      @NotNull final BackpressureStrategy backpressureStrategy) {
    return new ExecutorContext(Scheduler.trampoline(backpressureStrategy));
  }

  ExecutorContext(@NotNull final Scheduler scheduler) {
    this.scheduler = Requires.notNull(scheduler, "scheduler");
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
      @NotNull final F future, @NotNull final Function<F, ? extends SignalFuture<U>> function,
      final int weight) {
    return new ExecutionScope(scheduler).call(future, function, weight);
  }

  @Override
  public @NotNull <V, F extends TupleFuture<V, ?>> StreamingFuture<Nothing> run(
      @NotNull final F future, @NotNull final Consumer<F> consumer, final int weight) {
    return new ExecutionScope(scheduler).run(future, consumer, weight);
  }
}
