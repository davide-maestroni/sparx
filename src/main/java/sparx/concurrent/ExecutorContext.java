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
import sparx.concurrent.execution.*;
import sparx.util.Nothing;
import sparx.util.Require;
import sparx.util.UncheckedException;
import sparx.util.UncheckedException.UncheckedInterruptedException;
import sparx.util.function.*;
import sparx.util.logging.Log;

///////////////////////////////////////////////
// WARNING: GENERATED CODE - DO NOT MODIFY!!
// - templating engine: Mustache 0.9.11
// - template path: template/concurrent/ExecutorContext.mustache
///////////////////////////////////////////////

public class ExecutorContext implements ExecutionContext {

  private static final FutureRegistry DUMMY_REGISTRY = new FutureRegistry() {
    @Override
    public void register(@NotNull final StreamingFuture<?> future) {
    }
  };

  private final FutureRegistry registry;
  private final Scheduler scheduler;
  private final HashMap<String, Object> objects = new HashMap<String, Object>();

  private ExecutorContext(@NotNull final Scheduler scheduler,
      @NotNull final FutureRegistry registry) {
    this.scheduler = scheduler;
    this.registry = registry;
  }

  public static @NotNull ExecutorContext of(@NotNull final Executor executor) {
    return of(executor, Integer.MAX_VALUE);
  }

  public static @NotNull ExecutorContext of(@NotNull final Executor executor,
      @NotNull final BackpressureStrategy backpressureStrategy) {
    return of(executor, Integer.MAX_VALUE, backpressureStrategy);
  }

  public static @NotNull ExecutorContext of(@NotNull final Executor executor,
      final int minThroughput) {
    final FailingExecutor failingExecutor = new FailingExecutor(
        Require.notNull(executor, "executor"));
    final Scheduler scheduler = Scheduler.of(failingExecutor, minThroughput);
    return new ExecutorContext(scheduler, failingExecutor);
  }

  public static @NotNull ExecutorContext of(@NotNull final Executor executor,
      final int minThroughput, @NotNull final BackpressureStrategy backpressureStrategy) {
    final FailingExecutor failingExecutor = new FailingExecutor(
        Require.notNull(executor, "executor"));
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

  @Override
  public int minThroughput() {
    return scheduler.minThroughput();
  }

  @Override
  public int pendingCount() {
    return scheduler.pendingCount();
  }

  @Override
  public @NotNull <P> NullaryFuture<P, Nothing> submit(@NotNull final Action action) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(action);
  }

  @Override
  public @NotNull <P, R> NullaryFuture<P, R> submit(
      @NotNull final Supplier<? extends Signal<R>> supplier) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(supplier);
  }

  @Override
  public @NotNull <P> UnaryFuture<P, Nothing> submit(
      @NotNull final Consumer<? super StreamingFuture<P>> consumer) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(consumer);
  }

  @Override
  public @NotNull <P, R> UnaryFuture<P, R> submit(
      @NotNull final Function<? super StreamingFuture<P>, ? extends Signal<R>> function) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(function);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P> BinaryFuture<P, P1, P2, Nothing> submit(
      @NotNull final BinaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>> consumer) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(consumer);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, R> BinaryFuture<P, P1, P2, R> submit(
      @NotNull final BinaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? extends Signal<R>> function) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(function);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P> TernaryFuture<P, P1, P2, P3, Nothing> submit(
      @NotNull final TernaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>> consumer) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(consumer);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, R> TernaryFuture<P, P1, P2, P3, R> submit(
      @NotNull final TernaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? extends Signal<R>> function) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(function);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P> QuaternaryFuture<P, P1, P2, P3, P4, Nothing> submit(
      @NotNull final QuaternaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>> consumer) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(consumer);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, R> QuaternaryFuture<P, P1, P2, P3, P4, R> submit(
      @NotNull final QuaternaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? extends Signal<R>> function) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(function);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P> QuinaryFuture<P, P1, P2, P3, P4, P5, Nothing> submit(
      @NotNull final QuinaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>> consumer) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(consumer);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, R> QuinaryFuture<P, P1, P2, P3, P4, P5, R> submit(
      @NotNull final QuinaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? extends Signal<R>> function) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(function);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P> SenaryFuture<P, P1, P2, P3, P4, P5, P6, Nothing> submit(
      @NotNull final SenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>> consumer) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(consumer);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, R> SenaryFuture<P, P1, P2, P3, P4, P5, P6, R> submit(
      @NotNull final SenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? extends Signal<R>> function) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(function);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P> SeptenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, Nothing> submit(
      @NotNull final SeptenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>> consumer) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(consumer);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, R> SeptenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, R> submit(
      @NotNull final SeptenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? extends Signal<R>> function) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(function);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P> OctonaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, Nothing> submit(
      @NotNull final OctonaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>> consumer) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(consumer);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, R> OctonaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, R> submit(
      @NotNull final OctonaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? extends Signal<R>> function) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(function);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P> NonaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, Nothing> submit(
      @NotNull final NonaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>> consumer) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(consumer);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, R> NonaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, R> submit(
      @NotNull final NonaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? extends Signal<R>> function) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(function);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P> DenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, Nothing> submit(
      @NotNull final DenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>> consumer) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(consumer);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, R> DenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, R> submit(
      @NotNull final DenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? extends Signal<R>> function) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(function);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P> UndenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, Nothing> submit(
      @NotNull final UndenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>> consumer) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(consumer);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, R> UndenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, R> submit(
      @NotNull final UndenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? extends Signal<R>> function) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(function);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P> DuodenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, Nothing> submit(
      @NotNull final DuodenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>> consumer) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(consumer);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, R> DuodenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, R> submit(
      @NotNull final DuodenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? extends Signal<R>> function) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(function);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P> TredenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, Nothing> submit(
      @NotNull final TredenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>> consumer) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(consumer);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, R> TredenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, R> submit(
      @NotNull final TredenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? extends Signal<R>> function) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(function);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P> QuattuordenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, Nothing> submit(
      @NotNull final QuattuordenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>> consumer) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(consumer);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, R> QuattuordenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, R> submit(
      @NotNull final QuattuordenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? extends Signal<R>> function) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(function);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P> QuindenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, Nothing> submit(
      @NotNull final QuindenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? super StreamingFuture<P15>> consumer) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(consumer);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, R> QuindenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, R> submit(
      @NotNull final QuindenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? super StreamingFuture<P15>, ? extends Signal<R>> function) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(function);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, P16 extends P> SexdenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, Nothing> submit(
      @NotNull final SexdenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? super StreamingFuture<P15>, ? super StreamingFuture<P16>> consumer) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(consumer);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, P16 extends P, R> SexdenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, R> submit(
      @NotNull final SexdenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? super StreamingFuture<P15>, ? super StreamingFuture<P16>, ? extends Signal<R>> function) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(function);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, P16 extends P, P17 extends P> SeptendenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, Nothing> submit(
      @NotNull final SeptendenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? super StreamingFuture<P15>, ? super StreamingFuture<P16>, ? super StreamingFuture<P17>> consumer) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(consumer);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, P16 extends P, P17 extends P, R> SeptendenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, R> submit(
      @NotNull final SeptendenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? super StreamingFuture<P15>, ? super StreamingFuture<P16>, ? super StreamingFuture<P17>, ? extends Signal<R>> function) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(function);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, P16 extends P, P17 extends P, P18 extends P> OctodenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, Nothing> submit(
      @NotNull final OctodenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? super StreamingFuture<P15>, ? super StreamingFuture<P16>, ? super StreamingFuture<P17>, ? super StreamingFuture<P18>> consumer) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(consumer);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, P16 extends P, P17 extends P, P18 extends P, R> OctodenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, R> submit(
      @NotNull final OctodenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? super StreamingFuture<P15>, ? super StreamingFuture<P16>, ? super StreamingFuture<P17>, ? super StreamingFuture<P18>, ? extends Signal<R>> function) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(function);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, P16 extends P, P17 extends P, P18 extends P, P19 extends P> NovemdenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, Nothing> submit(
      @NotNull final NovemdenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? super StreamingFuture<P15>, ? super StreamingFuture<P16>, ? super StreamingFuture<P17>, ? super StreamingFuture<P18>, ? super StreamingFuture<P19>> consumer) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(consumer);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, P16 extends P, P17 extends P, P18 extends P, P19 extends P, R> NovemdenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, R> submit(
      @NotNull final NovemdenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? super StreamingFuture<P15>, ? super StreamingFuture<P16>, ? super StreamingFuture<P17>, ? super StreamingFuture<P18>, ? super StreamingFuture<P19>, ? extends Signal<R>> function) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(function);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, P16 extends P, P17 extends P, P18 extends P, P19 extends P, P20 extends P> VigenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, Nothing> submit(
      @NotNull final VigenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? super StreamingFuture<P15>, ? super StreamingFuture<P16>, ? super StreamingFuture<P17>, ? super StreamingFuture<P18>, ? super StreamingFuture<P19>, ? super StreamingFuture<P20>> consumer) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(consumer);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, P16 extends P, P17 extends P, P18 extends P, P19 extends P, P20 extends P, R> VigenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, R> submit(
      @NotNull final VigenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? super StreamingFuture<P15>, ? super StreamingFuture<P16>, ? super StreamingFuture<P17>, ? super StreamingFuture<P18>, ? super StreamingFuture<P19>, ? super StreamingFuture<P20>, ? extends Signal<R>> function) {
    return new ExecutionScope(this, objects, scheduler, registry).submit(function);
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
