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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sparx.concurrent.FutureScope.Registration;
import sparx.concurrent.FutureScope.Scope;
import sparx.concurrent.FutureScope.ScopeReceiver;
import sparx.concurrent.Scheduler.Task;
import sparx.concurrent.execution.*;
import sparx.concurrent.history.FutureHistory;
import sparx.function.*;
import sparx.logging.Log;
import sparx.logging.alert.Alerts;
import sparx.logging.alert.ExecutionContextTaskAlert;
import sparx.tuple.*;
import sparx.util.Nothing;
import sparx.util.Require;

class ExecutionScope implements ExecutionContext {

  private static final ExecutionContextTaskAlert taskAlert = Alerts.executionContextTaskAlert();

  private final ExecutionContext context;
  private final FutureRegistry registry;
  private final Scheduler scheduler;
  private final Map<String, Object> objects;

  ExecutionScope(@NotNull final ExecutionContext context,
      @NotNull final Map<String, Object> objects, @NotNull final Scheduler scheduler,
      @NotNull final FutureRegistry registry) {
    this.context = Require.notNull(context, "context");
    this.objects = Require.notNull(objects, "objects");
    this.scheduler = Require.notNull(scheduler, "scheduler");
    this.registry = Require.notNull(registry, "registry");
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
    Require.notNull(action, "action");
    final ScopeFuture<Nothing> future = new ScopeFuture<Nothing>() {
      @Override
      protected void innerRun() throws Exception {
        action.run();
        complete();
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new NullaryFunctionFuture<P, Nothing>(future);
  }

  @Override
  public @NotNull <P, R> NullaryFuture<P, R> submit(
      @NotNull final Supplier<? extends Signal<R>> supplier) {
    Require.notNull(supplier, "supplier");
    final ScopeFuture<R> future = new ScopeFuture<R>() {
      @Override
      protected void innerRun() throws Exception {
        supplier.get().subscribe(this);
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new NullaryFunctionFuture<P, R>(future);
  }

  @Override
  public @NotNull <P> UnaryFuture<P, Nothing> submit(
      @NotNull final Consumer<? super StreamingFuture<P>> consumer) {
    Require.notNull(consumer, "consumer");
    // TODO: add to scope?
    final VarFuture<P> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P>keepAll()));
    final ScopeFuture<Nothing> future = new ScopeFuture<Nothing>() {
      @Override
      protected void innerRun() throws Exception {
        consumer.accept(first);
        complete();
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new UnaryFunctionFuture<P, Nothing>(future, first);
  }

  @Override
  public @NotNull <P, R> UnaryFuture<P, R> submit(
      @NotNull final Function<? super StreamingFuture<P>, ? extends Signal<R>> function) {
    Require.notNull(function, "function");
    final VarFuture<P> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P>keepAll()));
    final ScopeFuture<R> future = new ScopeFuture<R>() {
      @Override
      protected void innerRun() throws Exception {
        function.apply(first).subscribe(this);
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new UnaryFunctionFuture<P, R>(future, first);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P> BinaryFuture<P, P1, P2, Nothing> submit(
      @NotNull final BinaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>> consumer) {
    Require.notNull(consumer, "consumer");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final ScopeFuture<Nothing> future = new ScopeFuture<Nothing>() {
      @Override
      protected void innerRun() throws Exception {
        consumer.accept(first, second);
        complete();
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new BinaryFunctionFuture<P, P1, P2, Nothing>(future, first, second);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, R> BinaryFuture<P, P1, P2, R> submit(
      @NotNull final BinaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? extends Signal<R>> function) {
    Require.notNull(function, "function");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final ScopeFuture<R> future = new ScopeFuture<R>() {
      @Override
      protected void innerRun() throws Exception {
        function.apply(first, second).subscribe(this);
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new BinaryFunctionFuture<P, P1, P2, R>(future, first, second);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P> TernaryFuture<P, P1, P2, P3, Nothing> submit(
      @NotNull final TernaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>> consumer) {
    Require.notNull(consumer, "consumer");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final VarFuture<P3> third = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P3>keepAll()));
    final ScopeFuture<Nothing> future = new ScopeFuture<Nothing>() {
      @Override
      protected void innerRun() throws Exception {
        consumer.accept(first, second, third);
        complete();
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new TernaryFunctionFuture<P, P1, P2, P3, Nothing>(future, first, second, third);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, R> TernaryFuture<P, P1, P2, P3, R> submit(
      @NotNull final TernaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? extends Signal<R>> function) {
    Require.notNull(function, "function");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final VarFuture<P3> third = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P3>keepAll()));
    final ScopeFuture<R> future = new ScopeFuture<R>() {
      @Override
      protected void innerRun() throws Exception {
        function.apply(first, second, third).subscribe(this);
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new TernaryFunctionFuture<P, P1, P2, P3, R>(future, first, second, third);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P> QuaternaryFuture<P, P1, P2, P3, P4, Nothing> submit(
      @NotNull final QuaternaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>> consumer) {
    Require.notNull(consumer, "consumer");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final VarFuture<P3> third = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P3>keepAll()));
    final VarFuture<P4> fourth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P4>keepAll()));
    final ScopeFuture<Nothing> future = new ScopeFuture<Nothing>() {
      @Override
      protected void innerRun() throws Exception {
        consumer.accept(first, second, third, fourth);
        complete();
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new QuaternaryFunctionFuture<P, P1, P2, P3, P4, Nothing>(future, first, second, third, fourth);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, R> QuaternaryFuture<P, P1, P2, P3, P4, R> submit(
      @NotNull final QuaternaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? extends Signal<R>> function) {
    Require.notNull(function, "function");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final VarFuture<P3> third = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P3>keepAll()));
    final VarFuture<P4> fourth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P4>keepAll()));
    final ScopeFuture<R> future = new ScopeFuture<R>() {
      @Override
      protected void innerRun() throws Exception {
        function.apply(first, second, third, fourth).subscribe(this);
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new QuaternaryFunctionFuture<P, P1, P2, P3, P4, R>(future, first, second, third, fourth);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P> QuinaryFuture<P, P1, P2, P3, P4, P5, Nothing> submit(
      @NotNull final QuinaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>> consumer) {
    Require.notNull(consumer, "consumer");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final VarFuture<P3> third = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P3>keepAll()));
    final VarFuture<P4> fourth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P4>keepAll()));
    final VarFuture<P5> fifth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P5>keepAll()));
    final ScopeFuture<Nothing> future = new ScopeFuture<Nothing>() {
      @Override
      protected void innerRun() throws Exception {
        consumer.accept(first, second, third, fourth, fifth);
        complete();
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new QuinaryFunctionFuture<P, P1, P2, P3, P4, P5, Nothing>(future, first, second, third, fourth, fifth);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, R> QuinaryFuture<P, P1, P2, P3, P4, P5, R> submit(
      @NotNull final QuinaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? extends Signal<R>> function) {
    Require.notNull(function, "function");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final VarFuture<P3> third = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P3>keepAll()));
    final VarFuture<P4> fourth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P4>keepAll()));
    final VarFuture<P5> fifth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P5>keepAll()));
    final ScopeFuture<R> future = new ScopeFuture<R>() {
      @Override
      protected void innerRun() throws Exception {
        function.apply(first, second, third, fourth, fifth).subscribe(this);
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new QuinaryFunctionFuture<P, P1, P2, P3, P4, P5, R>(future, first, second, third, fourth, fifth);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P> SenaryFuture<P, P1, P2, P3, P4, P5, P6, Nothing> submit(
      @NotNull final SenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>> consumer) {
    Require.notNull(consumer, "consumer");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final VarFuture<P3> third = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P3>keepAll()));
    final VarFuture<P4> fourth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P4>keepAll()));
    final VarFuture<P5> fifth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P5>keepAll()));
    final VarFuture<P6> sixth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P6>keepAll()));
    final ScopeFuture<Nothing> future = new ScopeFuture<Nothing>() {
      @Override
      protected void innerRun() throws Exception {
        consumer.accept(first, second, third, fourth, fifth, sixth);
        complete();
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new SenaryFunctionFuture<P, P1, P2, P3, P4, P5, P6, Nothing>(future, first, second, third, fourth, fifth, sixth);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, R> SenaryFuture<P, P1, P2, P3, P4, P5, P6, R> submit(
      @NotNull final SenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? extends Signal<R>> function) {
    Require.notNull(function, "function");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final VarFuture<P3> third = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P3>keepAll()));
    final VarFuture<P4> fourth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P4>keepAll()));
    final VarFuture<P5> fifth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P5>keepAll()));
    final VarFuture<P6> sixth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P6>keepAll()));
    final ScopeFuture<R> future = new ScopeFuture<R>() {
      @Override
      protected void innerRun() throws Exception {
        function.apply(first, second, third, fourth, fifth, sixth).subscribe(this);
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new SenaryFunctionFuture<P, P1, P2, P3, P4, P5, P6, R>(future, first, second, third, fourth, fifth, sixth);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P> SeptenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, Nothing> submit(
      @NotNull final SeptenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>> consumer) {
    Require.notNull(consumer, "consumer");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final VarFuture<P3> third = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P3>keepAll()));
    final VarFuture<P4> fourth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P4>keepAll()));
    final VarFuture<P5> fifth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P5>keepAll()));
    final VarFuture<P6> sixth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P6>keepAll()));
    final VarFuture<P7> seventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P7>keepAll()));
    final ScopeFuture<Nothing> future = new ScopeFuture<Nothing>() {
      @Override
      protected void innerRun() throws Exception {
        consumer.accept(first, second, third, fourth, fifth, sixth, seventh);
        complete();
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new SeptenaryFunctionFuture<P, P1, P2, P3, P4, P5, P6, P7, Nothing>(future, first, second, third, fourth, fifth, sixth, seventh);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, R> SeptenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, R> submit(
      @NotNull final SeptenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? extends Signal<R>> function) {
    Require.notNull(function, "function");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final VarFuture<P3> third = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P3>keepAll()));
    final VarFuture<P4> fourth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P4>keepAll()));
    final VarFuture<P5> fifth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P5>keepAll()));
    final VarFuture<P6> sixth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P6>keepAll()));
    final VarFuture<P7> seventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P7>keepAll()));
    final ScopeFuture<R> future = new ScopeFuture<R>() {
      @Override
      protected void innerRun() throws Exception {
        function.apply(first, second, third, fourth, fifth, sixth, seventh).subscribe(this);
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new SeptenaryFunctionFuture<P, P1, P2, P3, P4, P5, P6, P7, R>(future, first, second, third, fourth, fifth, sixth, seventh);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P> OctonaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, Nothing> submit(
      @NotNull final OctonaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>> consumer) {
    Require.notNull(consumer, "consumer");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final VarFuture<P3> third = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P3>keepAll()));
    final VarFuture<P4> fourth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P4>keepAll()));
    final VarFuture<P5> fifth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P5>keepAll()));
    final VarFuture<P6> sixth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P6>keepAll()));
    final VarFuture<P7> seventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P7>keepAll()));
    final VarFuture<P8> eighth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P8>keepAll()));
    final ScopeFuture<Nothing> future = new ScopeFuture<Nothing>() {
      @Override
      protected void innerRun() throws Exception {
        consumer.accept(first, second, third, fourth, fifth, sixth, seventh, eighth);
        complete();
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new OctonaryFunctionFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, Nothing>(future, first, second, third, fourth, fifth, sixth, seventh, eighth);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, R> OctonaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, R> submit(
      @NotNull final OctonaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? extends Signal<R>> function) {
    Require.notNull(function, "function");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final VarFuture<P3> third = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P3>keepAll()));
    final VarFuture<P4> fourth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P4>keepAll()));
    final VarFuture<P5> fifth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P5>keepAll()));
    final VarFuture<P6> sixth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P6>keepAll()));
    final VarFuture<P7> seventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P7>keepAll()));
    final VarFuture<P8> eighth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P8>keepAll()));
    final ScopeFuture<R> future = new ScopeFuture<R>() {
      @Override
      protected void innerRun() throws Exception {
        function.apply(first, second, third, fourth, fifth, sixth, seventh, eighth).subscribe(this);
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new OctonaryFunctionFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, R>(future, first, second, third, fourth, fifth, sixth, seventh, eighth);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P> NonaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, Nothing> submit(
      @NotNull final NonaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>> consumer) {
    Require.notNull(consumer, "consumer");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final VarFuture<P3> third = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P3>keepAll()));
    final VarFuture<P4> fourth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P4>keepAll()));
    final VarFuture<P5> fifth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P5>keepAll()));
    final VarFuture<P6> sixth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P6>keepAll()));
    final VarFuture<P7> seventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P7>keepAll()));
    final VarFuture<P8> eighth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P8>keepAll()));
    final VarFuture<P9> ninth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P9>keepAll()));
    final ScopeFuture<Nothing> future = new ScopeFuture<Nothing>() {
      @Override
      protected void innerRun() throws Exception {
        consumer.accept(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth);
        complete();
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new NonaryFunctionFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, Nothing>(future, first, second, third, fourth, fifth, sixth, seventh, eighth, ninth);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, R> NonaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, R> submit(
      @NotNull final NonaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? extends Signal<R>> function) {
    Require.notNull(function, "function");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final VarFuture<P3> third = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P3>keepAll()));
    final VarFuture<P4> fourth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P4>keepAll()));
    final VarFuture<P5> fifth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P5>keepAll()));
    final VarFuture<P6> sixth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P6>keepAll()));
    final VarFuture<P7> seventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P7>keepAll()));
    final VarFuture<P8> eighth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P8>keepAll()));
    final VarFuture<P9> ninth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P9>keepAll()));
    final ScopeFuture<R> future = new ScopeFuture<R>() {
      @Override
      protected void innerRun() throws Exception {
        function.apply(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth).subscribe(this);
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new NonaryFunctionFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, R>(future, first, second, third, fourth, fifth, sixth, seventh, eighth, ninth);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P> DenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, Nothing> submit(
      @NotNull final DenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>> consumer) {
    Require.notNull(consumer, "consumer");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final VarFuture<P3> third = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P3>keepAll()));
    final VarFuture<P4> fourth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P4>keepAll()));
    final VarFuture<P5> fifth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P5>keepAll()));
    final VarFuture<P6> sixth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P6>keepAll()));
    final VarFuture<P7> seventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P7>keepAll()));
    final VarFuture<P8> eighth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P8>keepAll()));
    final VarFuture<P9> ninth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P9>keepAll()));
    final VarFuture<P10> tenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P10>keepAll()));
    final ScopeFuture<Nothing> future = new ScopeFuture<Nothing>() {
      @Override
      protected void innerRun() throws Exception {
        consumer.accept(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth);
        complete();
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new DenaryFunctionFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, Nothing>(future, first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, R> DenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, R> submit(
      @NotNull final DenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? extends Signal<R>> function) {
    Require.notNull(function, "function");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final VarFuture<P3> third = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P3>keepAll()));
    final VarFuture<P4> fourth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P4>keepAll()));
    final VarFuture<P5> fifth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P5>keepAll()));
    final VarFuture<P6> sixth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P6>keepAll()));
    final VarFuture<P7> seventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P7>keepAll()));
    final VarFuture<P8> eighth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P8>keepAll()));
    final VarFuture<P9> ninth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P9>keepAll()));
    final VarFuture<P10> tenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P10>keepAll()));
    final ScopeFuture<R> future = new ScopeFuture<R>() {
      @Override
      protected void innerRun() throws Exception {
        function.apply(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth).subscribe(this);
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new DenaryFunctionFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, R>(future, first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P> UndenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, Nothing> submit(
      @NotNull final UndenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>> consumer) {
    Require.notNull(consumer, "consumer");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final VarFuture<P3> third = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P3>keepAll()));
    final VarFuture<P4> fourth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P4>keepAll()));
    final VarFuture<P5> fifth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P5>keepAll()));
    final VarFuture<P6> sixth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P6>keepAll()));
    final VarFuture<P7> seventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P7>keepAll()));
    final VarFuture<P8> eighth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P8>keepAll()));
    final VarFuture<P9> ninth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P9>keepAll()));
    final VarFuture<P10> tenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P10>keepAll()));
    final VarFuture<P11> eleventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P11>keepAll()));
    final ScopeFuture<Nothing> future = new ScopeFuture<Nothing>() {
      @Override
      protected void innerRun() throws Exception {
        consumer.accept(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh);
        complete();
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new UndenaryFunctionFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, Nothing>(future, first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, R> UndenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, R> submit(
      @NotNull final UndenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? extends Signal<R>> function) {
    Require.notNull(function, "function");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final VarFuture<P3> third = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P3>keepAll()));
    final VarFuture<P4> fourth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P4>keepAll()));
    final VarFuture<P5> fifth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P5>keepAll()));
    final VarFuture<P6> sixth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P6>keepAll()));
    final VarFuture<P7> seventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P7>keepAll()));
    final VarFuture<P8> eighth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P8>keepAll()));
    final VarFuture<P9> ninth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P9>keepAll()));
    final VarFuture<P10> tenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P10>keepAll()));
    final VarFuture<P11> eleventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P11>keepAll()));
    final ScopeFuture<R> future = new ScopeFuture<R>() {
      @Override
      protected void innerRun() throws Exception {
        function.apply(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh).subscribe(this);
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new UndenaryFunctionFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, R>(future, first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P> DuodenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, Nothing> submit(
      @NotNull final DuodenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>> consumer) {
    Require.notNull(consumer, "consumer");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final VarFuture<P3> third = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P3>keepAll()));
    final VarFuture<P4> fourth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P4>keepAll()));
    final VarFuture<P5> fifth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P5>keepAll()));
    final VarFuture<P6> sixth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P6>keepAll()));
    final VarFuture<P7> seventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P7>keepAll()));
    final VarFuture<P8> eighth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P8>keepAll()));
    final VarFuture<P9> ninth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P9>keepAll()));
    final VarFuture<P10> tenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P10>keepAll()));
    final VarFuture<P11> eleventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P11>keepAll()));
    final VarFuture<P12> twelfth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P12>keepAll()));
    final ScopeFuture<Nothing> future = new ScopeFuture<Nothing>() {
      @Override
      protected void innerRun() throws Exception {
        consumer.accept(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth);
        complete();
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new DuodenaryFunctionFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, Nothing>(future, first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, R> DuodenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, R> submit(
      @NotNull final DuodenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? extends Signal<R>> function) {
    Require.notNull(function, "function");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final VarFuture<P3> third = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P3>keepAll()));
    final VarFuture<P4> fourth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P4>keepAll()));
    final VarFuture<P5> fifth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P5>keepAll()));
    final VarFuture<P6> sixth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P6>keepAll()));
    final VarFuture<P7> seventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P7>keepAll()));
    final VarFuture<P8> eighth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P8>keepAll()));
    final VarFuture<P9> ninth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P9>keepAll()));
    final VarFuture<P10> tenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P10>keepAll()));
    final VarFuture<P11> eleventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P11>keepAll()));
    final VarFuture<P12> twelfth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P12>keepAll()));
    final ScopeFuture<R> future = new ScopeFuture<R>() {
      @Override
      protected void innerRun() throws Exception {
        function.apply(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth).subscribe(this);
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new DuodenaryFunctionFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, R>(future, first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P> TerdenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, Nothing> submit(
      @NotNull final TerdenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>> consumer) {
    Require.notNull(consumer, "consumer");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final VarFuture<P3> third = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P3>keepAll()));
    final VarFuture<P4> fourth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P4>keepAll()));
    final VarFuture<P5> fifth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P5>keepAll()));
    final VarFuture<P6> sixth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P6>keepAll()));
    final VarFuture<P7> seventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P7>keepAll()));
    final VarFuture<P8> eighth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P8>keepAll()));
    final VarFuture<P9> ninth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P9>keepAll()));
    final VarFuture<P10> tenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P10>keepAll()));
    final VarFuture<P11> eleventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P11>keepAll()));
    final VarFuture<P12> twelfth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P12>keepAll()));
    final VarFuture<P13> thirteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P13>keepAll()));
    final ScopeFuture<Nothing> future = new ScopeFuture<Nothing>() {
      @Override
      protected void innerRun() throws Exception {
        consumer.accept(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth);
        complete();
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new TerdenaryFunctionFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, Nothing>(future, first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, R> TerdenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, R> submit(
      @NotNull final TerdenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? extends Signal<R>> function) {
    Require.notNull(function, "function");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final VarFuture<P3> third = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P3>keepAll()));
    final VarFuture<P4> fourth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P4>keepAll()));
    final VarFuture<P5> fifth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P5>keepAll()));
    final VarFuture<P6> sixth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P6>keepAll()));
    final VarFuture<P7> seventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P7>keepAll()));
    final VarFuture<P8> eighth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P8>keepAll()));
    final VarFuture<P9> ninth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P9>keepAll()));
    final VarFuture<P10> tenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P10>keepAll()));
    final VarFuture<P11> eleventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P11>keepAll()));
    final VarFuture<P12> twelfth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P12>keepAll()));
    final VarFuture<P13> thirteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P13>keepAll()));
    final ScopeFuture<R> future = new ScopeFuture<R>() {
      @Override
      protected void innerRun() throws Exception {
        function.apply(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth).subscribe(this);
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new TerdenaryFunctionFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, R>(future, first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P> QuaterdenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, Nothing> submit(
      @NotNull final QuaterdenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>> consumer) {
    Require.notNull(consumer, "consumer");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final VarFuture<P3> third = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P3>keepAll()));
    final VarFuture<P4> fourth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P4>keepAll()));
    final VarFuture<P5> fifth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P5>keepAll()));
    final VarFuture<P6> sixth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P6>keepAll()));
    final VarFuture<P7> seventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P7>keepAll()));
    final VarFuture<P8> eighth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P8>keepAll()));
    final VarFuture<P9> ninth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P9>keepAll()));
    final VarFuture<P10> tenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P10>keepAll()));
    final VarFuture<P11> eleventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P11>keepAll()));
    final VarFuture<P12> twelfth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P12>keepAll()));
    final VarFuture<P13> thirteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P13>keepAll()));
    final VarFuture<P14> fourteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P14>keepAll()));
    final ScopeFuture<Nothing> future = new ScopeFuture<Nothing>() {
      @Override
      protected void innerRun() throws Exception {
        consumer.accept(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth);
        complete();
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new QuaterdenaryFunctionFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, Nothing>(future, first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, R> QuaterdenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, R> submit(
      @NotNull final QuaterdenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? extends Signal<R>> function) {
    Require.notNull(function, "function");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final VarFuture<P3> third = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P3>keepAll()));
    final VarFuture<P4> fourth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P4>keepAll()));
    final VarFuture<P5> fifth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P5>keepAll()));
    final VarFuture<P6> sixth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P6>keepAll()));
    final VarFuture<P7> seventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P7>keepAll()));
    final VarFuture<P8> eighth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P8>keepAll()));
    final VarFuture<P9> ninth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P9>keepAll()));
    final VarFuture<P10> tenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P10>keepAll()));
    final VarFuture<P11> eleventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P11>keepAll()));
    final VarFuture<P12> twelfth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P12>keepAll()));
    final VarFuture<P13> thirteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P13>keepAll()));
    final VarFuture<P14> fourteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P14>keepAll()));
    final ScopeFuture<R> future = new ScopeFuture<R>() {
      @Override
      protected void innerRun() throws Exception {
        function.apply(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth).subscribe(this);
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new QuaterdenaryFunctionFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, R>(future, first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P> QuindenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, Nothing> submit(
      @NotNull final QuindenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? super StreamingFuture<P15>> consumer) {
    Require.notNull(consumer, "consumer");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final VarFuture<P3> third = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P3>keepAll()));
    final VarFuture<P4> fourth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P4>keepAll()));
    final VarFuture<P5> fifth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P5>keepAll()));
    final VarFuture<P6> sixth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P6>keepAll()));
    final VarFuture<P7> seventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P7>keepAll()));
    final VarFuture<P8> eighth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P8>keepAll()));
    final VarFuture<P9> ninth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P9>keepAll()));
    final VarFuture<P10> tenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P10>keepAll()));
    final VarFuture<P11> eleventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P11>keepAll()));
    final VarFuture<P12> twelfth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P12>keepAll()));
    final VarFuture<P13> thirteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P13>keepAll()));
    final VarFuture<P14> fourteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P14>keepAll()));
    final VarFuture<P15> fifteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P15>keepAll()));
    final ScopeFuture<Nothing> future = new ScopeFuture<Nothing>() {
      @Override
      protected void innerRun() throws Exception {
        consumer.accept(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth);
        complete();
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new QuindenaryFunctionFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, Nothing>(future, first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, R> QuindenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, R> submit(
      @NotNull final QuindenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? super StreamingFuture<P15>, ? extends Signal<R>> function) {
    Require.notNull(function, "function");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final VarFuture<P3> third = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P3>keepAll()));
    final VarFuture<P4> fourth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P4>keepAll()));
    final VarFuture<P5> fifth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P5>keepAll()));
    final VarFuture<P6> sixth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P6>keepAll()));
    final VarFuture<P7> seventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P7>keepAll()));
    final VarFuture<P8> eighth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P8>keepAll()));
    final VarFuture<P9> ninth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P9>keepAll()));
    final VarFuture<P10> tenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P10>keepAll()));
    final VarFuture<P11> eleventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P11>keepAll()));
    final VarFuture<P12> twelfth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P12>keepAll()));
    final VarFuture<P13> thirteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P13>keepAll()));
    final VarFuture<P14> fourteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P14>keepAll()));
    final VarFuture<P15> fifteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P15>keepAll()));
    final ScopeFuture<R> future = new ScopeFuture<R>() {
      @Override
      protected void innerRun() throws Exception {
        function.apply(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth).subscribe(this);
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new QuindenaryFunctionFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, R>(future, first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, P16 extends P> SexdenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, Nothing> submit(
      @NotNull final SexdenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? super StreamingFuture<P15>, ? super StreamingFuture<P16>> consumer) {
    Require.notNull(consumer, "consumer");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final VarFuture<P3> third = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P3>keepAll()));
    final VarFuture<P4> fourth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P4>keepAll()));
    final VarFuture<P5> fifth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P5>keepAll()));
    final VarFuture<P6> sixth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P6>keepAll()));
    final VarFuture<P7> seventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P7>keepAll()));
    final VarFuture<P8> eighth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P8>keepAll()));
    final VarFuture<P9> ninth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P9>keepAll()));
    final VarFuture<P10> tenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P10>keepAll()));
    final VarFuture<P11> eleventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P11>keepAll()));
    final VarFuture<P12> twelfth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P12>keepAll()));
    final VarFuture<P13> thirteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P13>keepAll()));
    final VarFuture<P14> fourteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P14>keepAll()));
    final VarFuture<P15> fifteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P15>keepAll()));
    final VarFuture<P16> sixteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P16>keepAll()));
    final ScopeFuture<Nothing> future = new ScopeFuture<Nothing>() {
      @Override
      protected void innerRun() throws Exception {
        consumer.accept(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth, sixteenth);
        complete();
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new SexdenaryFunctionFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, Nothing>(future, first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth, sixteenth);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, P16 extends P, R> SexdenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, R> submit(
      @NotNull final SexdenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? super StreamingFuture<P15>, ? super StreamingFuture<P16>, ? extends Signal<R>> function) {
    Require.notNull(function, "function");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final VarFuture<P3> third = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P3>keepAll()));
    final VarFuture<P4> fourth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P4>keepAll()));
    final VarFuture<P5> fifth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P5>keepAll()));
    final VarFuture<P6> sixth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P6>keepAll()));
    final VarFuture<P7> seventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P7>keepAll()));
    final VarFuture<P8> eighth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P8>keepAll()));
    final VarFuture<P9> ninth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P9>keepAll()));
    final VarFuture<P10> tenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P10>keepAll()));
    final VarFuture<P11> eleventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P11>keepAll()));
    final VarFuture<P12> twelfth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P12>keepAll()));
    final VarFuture<P13> thirteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P13>keepAll()));
    final VarFuture<P14> fourteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P14>keepAll()));
    final VarFuture<P15> fifteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P15>keepAll()));
    final VarFuture<P16> sixteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P16>keepAll()));
    final ScopeFuture<R> future = new ScopeFuture<R>() {
      @Override
      protected void innerRun() throws Exception {
        function.apply(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth, sixteenth).subscribe(this);
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new SexdenaryFunctionFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, R>(future, first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth, sixteenth);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, P16 extends P, P17 extends P> SeptendenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, Nothing> submit(
      @NotNull final SeptendenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? super StreamingFuture<P15>, ? super StreamingFuture<P16>, ? super StreamingFuture<P17>> consumer) {
    Require.notNull(consumer, "consumer");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final VarFuture<P3> third = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P3>keepAll()));
    final VarFuture<P4> fourth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P4>keepAll()));
    final VarFuture<P5> fifth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P5>keepAll()));
    final VarFuture<P6> sixth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P6>keepAll()));
    final VarFuture<P7> seventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P7>keepAll()));
    final VarFuture<P8> eighth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P8>keepAll()));
    final VarFuture<P9> ninth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P9>keepAll()));
    final VarFuture<P10> tenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P10>keepAll()));
    final VarFuture<P11> eleventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P11>keepAll()));
    final VarFuture<P12> twelfth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P12>keepAll()));
    final VarFuture<P13> thirteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P13>keepAll()));
    final VarFuture<P14> fourteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P14>keepAll()));
    final VarFuture<P15> fifteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P15>keepAll()));
    final VarFuture<P16> sixteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P16>keepAll()));
    final VarFuture<P17> seventeenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P17>keepAll()));
    final ScopeFuture<Nothing> future = new ScopeFuture<Nothing>() {
      @Override
      protected void innerRun() throws Exception {
        consumer.accept(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth, sixteenth, seventeenth);
        complete();
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new SeptendenaryFunctionFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, Nothing>(future, first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth, sixteenth, seventeenth);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, P16 extends P, P17 extends P, R> SeptendenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, R> submit(
      @NotNull final SeptendenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? super StreamingFuture<P15>, ? super StreamingFuture<P16>, ? super StreamingFuture<P17>, ? extends Signal<R>> function) {
    Require.notNull(function, "function");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final VarFuture<P3> third = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P3>keepAll()));
    final VarFuture<P4> fourth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P4>keepAll()));
    final VarFuture<P5> fifth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P5>keepAll()));
    final VarFuture<P6> sixth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P6>keepAll()));
    final VarFuture<P7> seventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P7>keepAll()));
    final VarFuture<P8> eighth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P8>keepAll()));
    final VarFuture<P9> ninth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P9>keepAll()));
    final VarFuture<P10> tenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P10>keepAll()));
    final VarFuture<P11> eleventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P11>keepAll()));
    final VarFuture<P12> twelfth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P12>keepAll()));
    final VarFuture<P13> thirteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P13>keepAll()));
    final VarFuture<P14> fourteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P14>keepAll()));
    final VarFuture<P15> fifteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P15>keepAll()));
    final VarFuture<P16> sixteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P16>keepAll()));
    final VarFuture<P17> seventeenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P17>keepAll()));
    final ScopeFuture<R> future = new ScopeFuture<R>() {
      @Override
      protected void innerRun() throws Exception {
        function.apply(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth, sixteenth, seventeenth).subscribe(this);
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new SeptendenaryFunctionFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, R>(future, first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth, sixteenth, seventeenth);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, P16 extends P, P17 extends P, P18 extends P> OctodenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, Nothing> submit(
      @NotNull final OctodenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? super StreamingFuture<P15>, ? super StreamingFuture<P16>, ? super StreamingFuture<P17>, ? super StreamingFuture<P18>> consumer) {
    Require.notNull(consumer, "consumer");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final VarFuture<P3> third = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P3>keepAll()));
    final VarFuture<P4> fourth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P4>keepAll()));
    final VarFuture<P5> fifth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P5>keepAll()));
    final VarFuture<P6> sixth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P6>keepAll()));
    final VarFuture<P7> seventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P7>keepAll()));
    final VarFuture<P8> eighth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P8>keepAll()));
    final VarFuture<P9> ninth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P9>keepAll()));
    final VarFuture<P10> tenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P10>keepAll()));
    final VarFuture<P11> eleventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P11>keepAll()));
    final VarFuture<P12> twelfth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P12>keepAll()));
    final VarFuture<P13> thirteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P13>keepAll()));
    final VarFuture<P14> fourteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P14>keepAll()));
    final VarFuture<P15> fifteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P15>keepAll()));
    final VarFuture<P16> sixteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P16>keepAll()));
    final VarFuture<P17> seventeenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P17>keepAll()));
    final VarFuture<P18> eighteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P18>keepAll()));
    final ScopeFuture<Nothing> future = new ScopeFuture<Nothing>() {
      @Override
      protected void innerRun() throws Exception {
        consumer.accept(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth, sixteenth, seventeenth, eighteenth);
        complete();
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new OctodenaryFunctionFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, Nothing>(future, first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth, sixteenth, seventeenth, eighteenth);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, P16 extends P, P17 extends P, P18 extends P, R> OctodenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, R> submit(
      @NotNull final OctodenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? super StreamingFuture<P15>, ? super StreamingFuture<P16>, ? super StreamingFuture<P17>, ? super StreamingFuture<P18>, ? extends Signal<R>> function) {
    Require.notNull(function, "function");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final VarFuture<P3> third = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P3>keepAll()));
    final VarFuture<P4> fourth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P4>keepAll()));
    final VarFuture<P5> fifth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P5>keepAll()));
    final VarFuture<P6> sixth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P6>keepAll()));
    final VarFuture<P7> seventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P7>keepAll()));
    final VarFuture<P8> eighth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P8>keepAll()));
    final VarFuture<P9> ninth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P9>keepAll()));
    final VarFuture<P10> tenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P10>keepAll()));
    final VarFuture<P11> eleventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P11>keepAll()));
    final VarFuture<P12> twelfth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P12>keepAll()));
    final VarFuture<P13> thirteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P13>keepAll()));
    final VarFuture<P14> fourteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P14>keepAll()));
    final VarFuture<P15> fifteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P15>keepAll()));
    final VarFuture<P16> sixteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P16>keepAll()));
    final VarFuture<P17> seventeenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P17>keepAll()));
    final VarFuture<P18> eighteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P18>keepAll()));
    final ScopeFuture<R> future = new ScopeFuture<R>() {
      @Override
      protected void innerRun() throws Exception {
        function.apply(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth, sixteenth, seventeenth, eighteenth).subscribe(this);
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new OctodenaryFunctionFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, R>(future, first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth, sixteenth, seventeenth, eighteenth);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, P16 extends P, P17 extends P, P18 extends P, P19 extends P> NovemdenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, Nothing> submit(
      @NotNull final NovemdenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? super StreamingFuture<P15>, ? super StreamingFuture<P16>, ? super StreamingFuture<P17>, ? super StreamingFuture<P18>, ? super StreamingFuture<P19>> consumer) {
    Require.notNull(consumer, "consumer");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final VarFuture<P3> third = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P3>keepAll()));
    final VarFuture<P4> fourth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P4>keepAll()));
    final VarFuture<P5> fifth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P5>keepAll()));
    final VarFuture<P6> sixth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P6>keepAll()));
    final VarFuture<P7> seventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P7>keepAll()));
    final VarFuture<P8> eighth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P8>keepAll()));
    final VarFuture<P9> ninth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P9>keepAll()));
    final VarFuture<P10> tenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P10>keepAll()));
    final VarFuture<P11> eleventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P11>keepAll()));
    final VarFuture<P12> twelfth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P12>keepAll()));
    final VarFuture<P13> thirteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P13>keepAll()));
    final VarFuture<P14> fourteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P14>keepAll()));
    final VarFuture<P15> fifteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P15>keepAll()));
    final VarFuture<P16> sixteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P16>keepAll()));
    final VarFuture<P17> seventeenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P17>keepAll()));
    final VarFuture<P18> eighteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P18>keepAll()));
    final VarFuture<P19> nineteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P19>keepAll()));
    final ScopeFuture<Nothing> future = new ScopeFuture<Nothing>() {
      @Override
      protected void innerRun() throws Exception {
        consumer.accept(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth, sixteenth, seventeenth, eighteenth, nineteenth);
        complete();
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new NovemdenaryFunctionFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, Nothing>(future, first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth, sixteenth, seventeenth, eighteenth, nineteenth);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, P16 extends P, P17 extends P, P18 extends P, P19 extends P, R> NovemdenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, R> submit(
      @NotNull final NovemdenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? super StreamingFuture<P15>, ? super StreamingFuture<P16>, ? super StreamingFuture<P17>, ? super StreamingFuture<P18>, ? super StreamingFuture<P19>, ? extends Signal<R>> function) {
    Require.notNull(function, "function");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final VarFuture<P3> third = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P3>keepAll()));
    final VarFuture<P4> fourth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P4>keepAll()));
    final VarFuture<P5> fifth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P5>keepAll()));
    final VarFuture<P6> sixth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P6>keepAll()));
    final VarFuture<P7> seventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P7>keepAll()));
    final VarFuture<P8> eighth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P8>keepAll()));
    final VarFuture<P9> ninth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P9>keepAll()));
    final VarFuture<P10> tenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P10>keepAll()));
    final VarFuture<P11> eleventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P11>keepAll()));
    final VarFuture<P12> twelfth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P12>keepAll()));
    final VarFuture<P13> thirteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P13>keepAll()));
    final VarFuture<P14> fourteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P14>keepAll()));
    final VarFuture<P15> fifteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P15>keepAll()));
    final VarFuture<P16> sixteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P16>keepAll()));
    final VarFuture<P17> seventeenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P17>keepAll()));
    final VarFuture<P18> eighteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P18>keepAll()));
    final VarFuture<P19> nineteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P19>keepAll()));
    final ScopeFuture<R> future = new ScopeFuture<R>() {
      @Override
      protected void innerRun() throws Exception {
        function.apply(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth, sixteenth, seventeenth, eighteenth, nineteenth).subscribe(this);
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new NovemdenaryFunctionFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, R>(future, first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth, sixteenth, seventeenth, eighteenth, nineteenth);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, P16 extends P, P17 extends P, P18 extends P, P19 extends P, P20 extends P> VigenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, Nothing> submit(
      @NotNull final VigenaryConsumer<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? super StreamingFuture<P15>, ? super StreamingFuture<P16>, ? super StreamingFuture<P17>, ? super StreamingFuture<P18>, ? super StreamingFuture<P19>, ? super StreamingFuture<P20>> consumer) {
    Require.notNull(consumer, "consumer");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final VarFuture<P3> third = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P3>keepAll()));
    final VarFuture<P4> fourth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P4>keepAll()));
    final VarFuture<P5> fifth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P5>keepAll()));
    final VarFuture<P6> sixth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P6>keepAll()));
    final VarFuture<P7> seventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P7>keepAll()));
    final VarFuture<P8> eighth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P8>keepAll()));
    final VarFuture<P9> ninth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P9>keepAll()));
    final VarFuture<P10> tenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P10>keepAll()));
    final VarFuture<P11> eleventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P11>keepAll()));
    final VarFuture<P12> twelfth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P12>keepAll()));
    final VarFuture<P13> thirteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P13>keepAll()));
    final VarFuture<P14> fourteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P14>keepAll()));
    final VarFuture<P15> fifteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P15>keepAll()));
    final VarFuture<P16> sixteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P16>keepAll()));
    final VarFuture<P17> seventeenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P17>keepAll()));
    final VarFuture<P18> eighteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P18>keepAll()));
    final VarFuture<P19> nineteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P19>keepAll()));
    final VarFuture<P20> twentieth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P20>keepAll()));
    final ScopeFuture<Nothing> future = new ScopeFuture<Nothing>() {
      @Override
      protected void innerRun() throws Exception {
        consumer.accept(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth, sixteenth, seventeenth, eighteenth, nineteenth, twentieth);
        complete();
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new VigenaryFunctionFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, Nothing>(future, first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth, sixteenth, seventeenth, eighteenth, nineteenth, twentieth);
  }

  @Override
  public @NotNull <P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, P16 extends P, P17 extends P, P18 extends P, P19 extends P, P20 extends P, R> VigenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, R> submit(
      @NotNull final VigenaryFunction<? super StreamingFuture<P1>, ? super StreamingFuture<P2>, ? super StreamingFuture<P3>, ? super StreamingFuture<P4>, ? super StreamingFuture<P5>, ? super StreamingFuture<P6>, ? super StreamingFuture<P7>, ? super StreamingFuture<P8>, ? super StreamingFuture<P9>, ? super StreamingFuture<P10>, ? super StreamingFuture<P11>, ? super StreamingFuture<P12>, ? super StreamingFuture<P13>, ? super StreamingFuture<P14>, ? super StreamingFuture<P15>, ? super StreamingFuture<P16>, ? super StreamingFuture<P17>, ? super StreamingFuture<P18>, ? super StreamingFuture<P19>, ? super StreamingFuture<P20>, ? extends Signal<R>> function) {
    Require.notNull(function, "function");
    final VarFuture<P1> first = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P1>keepAll()));
    final VarFuture<P2> second = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P2>keepAll()));
    final VarFuture<P3> third = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P3>keepAll()));
    final VarFuture<P4> fourth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P4>keepAll()));
    final VarFuture<P5> fifth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P5>keepAll()));
    final VarFuture<P6> sixth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P6>keepAll()));
    final VarFuture<P7> seventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P7>keepAll()));
    final VarFuture<P8> eighth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P8>keepAll()));
    final VarFuture<P9> ninth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P9>keepAll()));
    final VarFuture<P10> tenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P10>keepAll()));
    final VarFuture<P11> eleventh = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P11>keepAll()));
    final VarFuture<P12> twelfth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P12>keepAll()));
    final VarFuture<P13> thirteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P13>keepAll()));
    final VarFuture<P14> fourteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P14>keepAll()));
    final VarFuture<P15> fifteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P15>keepAll()));
    final VarFuture<P16> sixteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P16>keepAll()));
    final VarFuture<P17> seventeenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P17>keepAll()));
    final VarFuture<P18> eighteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P18>keepAll()));
    final VarFuture<P19> nineteenth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P19>keepAll()));
    final VarFuture<P20> twentieth = VarFuture.create(
        FutureHistory.untilSubscribe(FutureHistory.<P20>keepAll()));
    final ScopeFuture<R> future = new ScopeFuture<R>() {
      @Override
      protected void innerRun() throws Exception {
        function.apply(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth, sixteenth, seventeenth, eighteenth, nineteenth, twentieth).subscribe(this);
      }
    };
    registry.register(future);
    scheduler.scheduleAfter(future);
    return new VigenaryFunctionFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, R>(future, first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth, sixteenth, seventeenth, eighteenth, nineteenth, twentieth);
  }

  interface FutureRegistry {

    void register(@NotNull StreamingFuture<?> future);
  }

  private static class ChunkIterator<E> implements Iterator<List<E>> {

    private final Iterator<E> iterator;
    private final int maxSize;

    private ChunkIterator(@NotNull final Collection<E> collection, final int maxSize) {
      this.iterator = collection.iterator();
      this.maxSize = maxSize;
    }

    @Override
    public boolean hasNext() {
      return iterator.hasNext();
    }

    @Override
    public List<E> next() {
      final int maxSize = this.maxSize;
      final Iterator<E> iterator = this.iterator;
      final ArrayList<E> chunk = new ArrayList<E>(maxSize);
      while (iterator.hasNext() && chunk.size() < maxSize) {
        chunk.add(iterator.next());
      }
      return Collections.unmodifiableList(chunk);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove");
    }
  }

  private static class NullaryFunctionFuture<P, R> extends ReadOnlyFuture<R> implements
      NullaryFuture<P, R> {

    public NullaryFunctionFuture(@NotNull final StreamingFuture<R> future) {
      super(future);
    }

    @Override
    public @NotNull Empty<StreamingFuture<? extends P>> parameters() {
      return Tuples.asTuple();
    }
  }

  private static class UnaryFunctionFuture<P, R> extends ReadOnlyFuture<R> implements
      UnaryFuture<P, R> {

    private final Single<StreamingFuture<? extends P>> tuple;

    public UnaryFunctionFuture(@NotNull final StreamingFuture<R> future,
        @NotNull final StreamingFuture<P> first) {
      super(future);
      tuple = Tuples.<StreamingFuture<? extends P>>asTuple(first);
    }

    @Override
    public @NotNull Single<StreamingFuture<? extends P>> parameters() {
      return tuple;
    }
  }

  private static class BinaryFunctionFuture<P, P1 extends P, P2 extends P, R> extends ReadOnlyFuture<R> implements
      BinaryFuture<P, P1, P2, R> {

    private final Couple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>> tuple;

    public BinaryFunctionFuture(@NotNull final StreamingFuture<R> future,
        @NotNull final StreamingFuture<P1> first,
        @NotNull final StreamingFuture<P2> second) {
      super(future);
      tuple = Tuples.asTuple(first, second);
    }

    @Override
    public @NotNull Couple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>> parameters() {
      return tuple;
    }
  }

  private static class TernaryFunctionFuture<P, P1 extends P, P2 extends P, P3 extends P, R> extends ReadOnlyFuture<R> implements
      TernaryFuture<P, P1, P2, P3, R> {

    private final Triple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>> tuple;

    public TernaryFunctionFuture(@NotNull final StreamingFuture<R> future,
        @NotNull final StreamingFuture<P1> first,
        @NotNull final StreamingFuture<P2> second,
        @NotNull final StreamingFuture<P3> third) {
      super(future);
      tuple = Tuples.asTuple(first, second, third);
    }

    @Override
    public @NotNull Triple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>> parameters() {
      return tuple;
    }
  }

  private static class QuaternaryFunctionFuture<P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, R> extends ReadOnlyFuture<R> implements
      QuaternaryFuture<P, P1, P2, P3, P4, R> {

    private final Quadruple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>, StreamingFuture<P4>> tuple;

    public QuaternaryFunctionFuture(@NotNull final StreamingFuture<R> future,
        @NotNull final StreamingFuture<P1> first,
        @NotNull final StreamingFuture<P2> second,
        @NotNull final StreamingFuture<P3> third,
        @NotNull final StreamingFuture<P4> fourth) {
      super(future);
      tuple = Tuples.asTuple(first, second, third, fourth);
    }

    @Override
    public @NotNull Quadruple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>, StreamingFuture<P4>> parameters() {
      return tuple;
    }
  }

  private static class QuinaryFunctionFuture<P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, R> extends ReadOnlyFuture<R> implements
      QuinaryFuture<P, P1, P2, P3, P4, P5, R> {

    private final Quintuple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>, StreamingFuture<P4>, StreamingFuture<P5>> tuple;

    public QuinaryFunctionFuture(@NotNull final StreamingFuture<R> future,
        @NotNull final StreamingFuture<P1> first,
        @NotNull final StreamingFuture<P2> second,
        @NotNull final StreamingFuture<P3> third,
        @NotNull final StreamingFuture<P4> fourth,
        @NotNull final StreamingFuture<P5> fifth) {
      super(future);
      tuple = Tuples.asTuple(first, second, third, fourth, fifth);
    }

    @Override
    public @NotNull Quintuple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>, StreamingFuture<P4>, StreamingFuture<P5>> parameters() {
      return tuple;
    }
  }

  private static class SenaryFunctionFuture<P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, R> extends ReadOnlyFuture<R> implements
      SenaryFuture<P, P1, P2, P3, P4, P5, P6, R> {

    private final Sextuple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>, StreamingFuture<P4>, StreamingFuture<P5>, StreamingFuture<P6>> tuple;

    public SenaryFunctionFuture(@NotNull final StreamingFuture<R> future,
        @NotNull final StreamingFuture<P1> first,
        @NotNull final StreamingFuture<P2> second,
        @NotNull final StreamingFuture<P3> third,
        @NotNull final StreamingFuture<P4> fourth,
        @NotNull final StreamingFuture<P5> fifth,
        @NotNull final StreamingFuture<P6> sixth) {
      super(future);
      tuple = Tuples.asTuple(first, second, third, fourth, fifth, sixth);
    }

    @Override
    public @NotNull Sextuple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>, StreamingFuture<P4>, StreamingFuture<P5>, StreamingFuture<P6>> parameters() {
      return tuple;
    }
  }

  private static class SeptenaryFunctionFuture<P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, R> extends ReadOnlyFuture<R> implements
      SeptenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, R> {

    private final Septuple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>, StreamingFuture<P4>, StreamingFuture<P5>, StreamingFuture<P6>, StreamingFuture<P7>> tuple;

    public SeptenaryFunctionFuture(@NotNull final StreamingFuture<R> future,
        @NotNull final StreamingFuture<P1> first,
        @NotNull final StreamingFuture<P2> second,
        @NotNull final StreamingFuture<P3> third,
        @NotNull final StreamingFuture<P4> fourth,
        @NotNull final StreamingFuture<P5> fifth,
        @NotNull final StreamingFuture<P6> sixth,
        @NotNull final StreamingFuture<P7> seventh) {
      super(future);
      tuple = Tuples.asTuple(first, second, third, fourth, fifth, sixth, seventh);
    }

    @Override
    public @NotNull Septuple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>, StreamingFuture<P4>, StreamingFuture<P5>, StreamingFuture<P6>, StreamingFuture<P7>> parameters() {
      return tuple;
    }
  }

  private static class OctonaryFunctionFuture<P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, R> extends ReadOnlyFuture<R> implements
      OctonaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, R> {

    private final Octuple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>, StreamingFuture<P4>, StreamingFuture<P5>, StreamingFuture<P6>, StreamingFuture<P7>, StreamingFuture<P8>> tuple;

    public OctonaryFunctionFuture(@NotNull final StreamingFuture<R> future,
        @NotNull final StreamingFuture<P1> first,
        @NotNull final StreamingFuture<P2> second,
        @NotNull final StreamingFuture<P3> third,
        @NotNull final StreamingFuture<P4> fourth,
        @NotNull final StreamingFuture<P5> fifth,
        @NotNull final StreamingFuture<P6> sixth,
        @NotNull final StreamingFuture<P7> seventh,
        @NotNull final StreamingFuture<P8> eighth) {
      super(future);
      tuple = Tuples.asTuple(first, second, third, fourth, fifth, sixth, seventh, eighth);
    }

    @Override
    public @NotNull Octuple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>, StreamingFuture<P4>, StreamingFuture<P5>, StreamingFuture<P6>, StreamingFuture<P7>, StreamingFuture<P8>> parameters() {
      return tuple;
    }
  }

  private static class NonaryFunctionFuture<P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, R> extends ReadOnlyFuture<R> implements
      NonaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, R> {

    private final Nonuple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>, StreamingFuture<P4>, StreamingFuture<P5>, StreamingFuture<P6>, StreamingFuture<P7>, StreamingFuture<P8>, StreamingFuture<P9>> tuple;

    public NonaryFunctionFuture(@NotNull final StreamingFuture<R> future,
        @NotNull final StreamingFuture<P1> first,
        @NotNull final StreamingFuture<P2> second,
        @NotNull final StreamingFuture<P3> third,
        @NotNull final StreamingFuture<P4> fourth,
        @NotNull final StreamingFuture<P5> fifth,
        @NotNull final StreamingFuture<P6> sixth,
        @NotNull final StreamingFuture<P7> seventh,
        @NotNull final StreamingFuture<P8> eighth,
        @NotNull final StreamingFuture<P9> ninth) {
      super(future);
      tuple = Tuples.asTuple(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth);
    }

    @Override
    public @NotNull Nonuple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>, StreamingFuture<P4>, StreamingFuture<P5>, StreamingFuture<P6>, StreamingFuture<P7>, StreamingFuture<P8>, StreamingFuture<P9>> parameters() {
      return tuple;
    }
  }

  private static class DenaryFunctionFuture<P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, R> extends ReadOnlyFuture<R> implements
      DenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, R> {

    private final Decuple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>, StreamingFuture<P4>, StreamingFuture<P5>, StreamingFuture<P6>, StreamingFuture<P7>, StreamingFuture<P8>, StreamingFuture<P9>, StreamingFuture<P10>> tuple;

    public DenaryFunctionFuture(@NotNull final StreamingFuture<R> future,
        @NotNull final StreamingFuture<P1> first,
        @NotNull final StreamingFuture<P2> second,
        @NotNull final StreamingFuture<P3> third,
        @NotNull final StreamingFuture<P4> fourth,
        @NotNull final StreamingFuture<P5> fifth,
        @NotNull final StreamingFuture<P6> sixth,
        @NotNull final StreamingFuture<P7> seventh,
        @NotNull final StreamingFuture<P8> eighth,
        @NotNull final StreamingFuture<P9> ninth,
        @NotNull final StreamingFuture<P10> tenth) {
      super(future);
      tuple = Tuples.asTuple(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth);
    }

    @Override
    public @NotNull Decuple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>, StreamingFuture<P4>, StreamingFuture<P5>, StreamingFuture<P6>, StreamingFuture<P7>, StreamingFuture<P8>, StreamingFuture<P9>, StreamingFuture<P10>> parameters() {
      return tuple;
    }
  }

  private static class UndenaryFunctionFuture<P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, R> extends ReadOnlyFuture<R> implements
      UndenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, R> {

    private final Undecuple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>, StreamingFuture<P4>, StreamingFuture<P5>, StreamingFuture<P6>, StreamingFuture<P7>, StreamingFuture<P8>, StreamingFuture<P9>, StreamingFuture<P10>, StreamingFuture<P11>> tuple;

    public UndenaryFunctionFuture(@NotNull final StreamingFuture<R> future,
        @NotNull final StreamingFuture<P1> first,
        @NotNull final StreamingFuture<P2> second,
        @NotNull final StreamingFuture<P3> third,
        @NotNull final StreamingFuture<P4> fourth,
        @NotNull final StreamingFuture<P5> fifth,
        @NotNull final StreamingFuture<P6> sixth,
        @NotNull final StreamingFuture<P7> seventh,
        @NotNull final StreamingFuture<P8> eighth,
        @NotNull final StreamingFuture<P9> ninth,
        @NotNull final StreamingFuture<P10> tenth,
        @NotNull final StreamingFuture<P11> eleventh) {
      super(future);
      tuple = Tuples.asTuple(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh);
    }

    @Override
    public @NotNull Undecuple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>, StreamingFuture<P4>, StreamingFuture<P5>, StreamingFuture<P6>, StreamingFuture<P7>, StreamingFuture<P8>, StreamingFuture<P9>, StreamingFuture<P10>, StreamingFuture<P11>> parameters() {
      return tuple;
    }
  }

  private static class DuodenaryFunctionFuture<P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, R> extends ReadOnlyFuture<R> implements
      DuodenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, R> {

    private final Duodecuple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>, StreamingFuture<P4>, StreamingFuture<P5>, StreamingFuture<P6>, StreamingFuture<P7>, StreamingFuture<P8>, StreamingFuture<P9>, StreamingFuture<P10>, StreamingFuture<P11>, StreamingFuture<P12>> tuple;

    public DuodenaryFunctionFuture(@NotNull final StreamingFuture<R> future,
        @NotNull final StreamingFuture<P1> first,
        @NotNull final StreamingFuture<P2> second,
        @NotNull final StreamingFuture<P3> third,
        @NotNull final StreamingFuture<P4> fourth,
        @NotNull final StreamingFuture<P5> fifth,
        @NotNull final StreamingFuture<P6> sixth,
        @NotNull final StreamingFuture<P7> seventh,
        @NotNull final StreamingFuture<P8> eighth,
        @NotNull final StreamingFuture<P9> ninth,
        @NotNull final StreamingFuture<P10> tenth,
        @NotNull final StreamingFuture<P11> eleventh,
        @NotNull final StreamingFuture<P12> twelfth) {
      super(future);
      tuple = Tuples.asTuple(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth);
    }

    @Override
    public @NotNull Duodecuple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>, StreamingFuture<P4>, StreamingFuture<P5>, StreamingFuture<P6>, StreamingFuture<P7>, StreamingFuture<P8>, StreamingFuture<P9>, StreamingFuture<P10>, StreamingFuture<P11>, StreamingFuture<P12>> parameters() {
      return tuple;
    }
  }

  private static class TerdenaryFunctionFuture<P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, R> extends ReadOnlyFuture<R> implements
      TerdenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, R> {

    private final Tredecuple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>, StreamingFuture<P4>, StreamingFuture<P5>, StreamingFuture<P6>, StreamingFuture<P7>, StreamingFuture<P8>, StreamingFuture<P9>, StreamingFuture<P10>, StreamingFuture<P11>, StreamingFuture<P12>, StreamingFuture<P13>> tuple;

    public TerdenaryFunctionFuture(@NotNull final StreamingFuture<R> future,
        @NotNull final StreamingFuture<P1> first,
        @NotNull final StreamingFuture<P2> second,
        @NotNull final StreamingFuture<P3> third,
        @NotNull final StreamingFuture<P4> fourth,
        @NotNull final StreamingFuture<P5> fifth,
        @NotNull final StreamingFuture<P6> sixth,
        @NotNull final StreamingFuture<P7> seventh,
        @NotNull final StreamingFuture<P8> eighth,
        @NotNull final StreamingFuture<P9> ninth,
        @NotNull final StreamingFuture<P10> tenth,
        @NotNull final StreamingFuture<P11> eleventh,
        @NotNull final StreamingFuture<P12> twelfth,
        @NotNull final StreamingFuture<P13> thirteenth) {
      super(future);
      tuple = Tuples.asTuple(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth);
    }

    @Override
    public @NotNull Tredecuple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>, StreamingFuture<P4>, StreamingFuture<P5>, StreamingFuture<P6>, StreamingFuture<P7>, StreamingFuture<P8>, StreamingFuture<P9>, StreamingFuture<P10>, StreamingFuture<P11>, StreamingFuture<P12>, StreamingFuture<P13>> parameters() {
      return tuple;
    }
  }

  private static class QuaterdenaryFunctionFuture<P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, R> extends ReadOnlyFuture<R> implements
      QuaterdenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, R> {

    private final Quattuordecuple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>, StreamingFuture<P4>, StreamingFuture<P5>, StreamingFuture<P6>, StreamingFuture<P7>, StreamingFuture<P8>, StreamingFuture<P9>, StreamingFuture<P10>, StreamingFuture<P11>, StreamingFuture<P12>, StreamingFuture<P13>, StreamingFuture<P14>> tuple;

    public QuaterdenaryFunctionFuture(@NotNull final StreamingFuture<R> future,
        @NotNull final StreamingFuture<P1> first,
        @NotNull final StreamingFuture<P2> second,
        @NotNull final StreamingFuture<P3> third,
        @NotNull final StreamingFuture<P4> fourth,
        @NotNull final StreamingFuture<P5> fifth,
        @NotNull final StreamingFuture<P6> sixth,
        @NotNull final StreamingFuture<P7> seventh,
        @NotNull final StreamingFuture<P8> eighth,
        @NotNull final StreamingFuture<P9> ninth,
        @NotNull final StreamingFuture<P10> tenth,
        @NotNull final StreamingFuture<P11> eleventh,
        @NotNull final StreamingFuture<P12> twelfth,
        @NotNull final StreamingFuture<P13> thirteenth,
        @NotNull final StreamingFuture<P14> fourteenth) {
      super(future);
      tuple = Tuples.asTuple(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth);
    }

    @Override
    public @NotNull Quattuordecuple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>, StreamingFuture<P4>, StreamingFuture<P5>, StreamingFuture<P6>, StreamingFuture<P7>, StreamingFuture<P8>, StreamingFuture<P9>, StreamingFuture<P10>, StreamingFuture<P11>, StreamingFuture<P12>, StreamingFuture<P13>, StreamingFuture<P14>> parameters() {
      return tuple;
    }
  }

  private static class QuindenaryFunctionFuture<P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, R> extends ReadOnlyFuture<R> implements
      QuindenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, R> {

    private final Quindecuple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>, StreamingFuture<P4>, StreamingFuture<P5>, StreamingFuture<P6>, StreamingFuture<P7>, StreamingFuture<P8>, StreamingFuture<P9>, StreamingFuture<P10>, StreamingFuture<P11>, StreamingFuture<P12>, StreamingFuture<P13>, StreamingFuture<P14>, StreamingFuture<P15>> tuple;

    public QuindenaryFunctionFuture(@NotNull final StreamingFuture<R> future,
        @NotNull final StreamingFuture<P1> first,
        @NotNull final StreamingFuture<P2> second,
        @NotNull final StreamingFuture<P3> third,
        @NotNull final StreamingFuture<P4> fourth,
        @NotNull final StreamingFuture<P5> fifth,
        @NotNull final StreamingFuture<P6> sixth,
        @NotNull final StreamingFuture<P7> seventh,
        @NotNull final StreamingFuture<P8> eighth,
        @NotNull final StreamingFuture<P9> ninth,
        @NotNull final StreamingFuture<P10> tenth,
        @NotNull final StreamingFuture<P11> eleventh,
        @NotNull final StreamingFuture<P12> twelfth,
        @NotNull final StreamingFuture<P13> thirteenth,
        @NotNull final StreamingFuture<P14> fourteenth,
        @NotNull final StreamingFuture<P15> fifteenth) {
      super(future);
      tuple = Tuples.asTuple(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth);
    }

    @Override
    public @NotNull Quindecuple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>, StreamingFuture<P4>, StreamingFuture<P5>, StreamingFuture<P6>, StreamingFuture<P7>, StreamingFuture<P8>, StreamingFuture<P9>, StreamingFuture<P10>, StreamingFuture<P11>, StreamingFuture<P12>, StreamingFuture<P13>, StreamingFuture<P14>, StreamingFuture<P15>> parameters() {
      return tuple;
    }
  }

  private static class SexdenaryFunctionFuture<P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, P16 extends P, R> extends ReadOnlyFuture<R> implements
      SexdenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, R> {

    private final Sexdecuple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>, StreamingFuture<P4>, StreamingFuture<P5>, StreamingFuture<P6>, StreamingFuture<P7>, StreamingFuture<P8>, StreamingFuture<P9>, StreamingFuture<P10>, StreamingFuture<P11>, StreamingFuture<P12>, StreamingFuture<P13>, StreamingFuture<P14>, StreamingFuture<P15>, StreamingFuture<P16>> tuple;

    public SexdenaryFunctionFuture(@NotNull final StreamingFuture<R> future,
        @NotNull final StreamingFuture<P1> first,
        @NotNull final StreamingFuture<P2> second,
        @NotNull final StreamingFuture<P3> third,
        @NotNull final StreamingFuture<P4> fourth,
        @NotNull final StreamingFuture<P5> fifth,
        @NotNull final StreamingFuture<P6> sixth,
        @NotNull final StreamingFuture<P7> seventh,
        @NotNull final StreamingFuture<P8> eighth,
        @NotNull final StreamingFuture<P9> ninth,
        @NotNull final StreamingFuture<P10> tenth,
        @NotNull final StreamingFuture<P11> eleventh,
        @NotNull final StreamingFuture<P12> twelfth,
        @NotNull final StreamingFuture<P13> thirteenth,
        @NotNull final StreamingFuture<P14> fourteenth,
        @NotNull final StreamingFuture<P15> fifteenth,
        @NotNull final StreamingFuture<P16> sixteenth) {
      super(future);
      tuple = Tuples.asTuple(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth, sixteenth);
    }

    @Override
    public @NotNull Sexdecuple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>, StreamingFuture<P4>, StreamingFuture<P5>, StreamingFuture<P6>, StreamingFuture<P7>, StreamingFuture<P8>, StreamingFuture<P9>, StreamingFuture<P10>, StreamingFuture<P11>, StreamingFuture<P12>, StreamingFuture<P13>, StreamingFuture<P14>, StreamingFuture<P15>, StreamingFuture<P16>> parameters() {
      return tuple;
    }
  }

  private static class SeptendenaryFunctionFuture<P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, P16 extends P, P17 extends P, R> extends ReadOnlyFuture<R> implements
      SeptendenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, R> {

    private final Septendecuple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>, StreamingFuture<P4>, StreamingFuture<P5>, StreamingFuture<P6>, StreamingFuture<P7>, StreamingFuture<P8>, StreamingFuture<P9>, StreamingFuture<P10>, StreamingFuture<P11>, StreamingFuture<P12>, StreamingFuture<P13>, StreamingFuture<P14>, StreamingFuture<P15>, StreamingFuture<P16>, StreamingFuture<P17>> tuple;

    public SeptendenaryFunctionFuture(@NotNull final StreamingFuture<R> future,
        @NotNull final StreamingFuture<P1> first,
        @NotNull final StreamingFuture<P2> second,
        @NotNull final StreamingFuture<P3> third,
        @NotNull final StreamingFuture<P4> fourth,
        @NotNull final StreamingFuture<P5> fifth,
        @NotNull final StreamingFuture<P6> sixth,
        @NotNull final StreamingFuture<P7> seventh,
        @NotNull final StreamingFuture<P8> eighth,
        @NotNull final StreamingFuture<P9> ninth,
        @NotNull final StreamingFuture<P10> tenth,
        @NotNull final StreamingFuture<P11> eleventh,
        @NotNull final StreamingFuture<P12> twelfth,
        @NotNull final StreamingFuture<P13> thirteenth,
        @NotNull final StreamingFuture<P14> fourteenth,
        @NotNull final StreamingFuture<P15> fifteenth,
        @NotNull final StreamingFuture<P16> sixteenth,
        @NotNull final StreamingFuture<P17> seventeenth) {
      super(future);
      tuple = Tuples.asTuple(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth, sixteenth, seventeenth);
    }

    @Override
    public @NotNull Septendecuple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>, StreamingFuture<P4>, StreamingFuture<P5>, StreamingFuture<P6>, StreamingFuture<P7>, StreamingFuture<P8>, StreamingFuture<P9>, StreamingFuture<P10>, StreamingFuture<P11>, StreamingFuture<P12>, StreamingFuture<P13>, StreamingFuture<P14>, StreamingFuture<P15>, StreamingFuture<P16>, StreamingFuture<P17>> parameters() {
      return tuple;
    }
  }

  private static class OctodenaryFunctionFuture<P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, P16 extends P, P17 extends P, P18 extends P, R> extends ReadOnlyFuture<R> implements
      OctodenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, R> {

    private final Octodecuple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>, StreamingFuture<P4>, StreamingFuture<P5>, StreamingFuture<P6>, StreamingFuture<P7>, StreamingFuture<P8>, StreamingFuture<P9>, StreamingFuture<P10>, StreamingFuture<P11>, StreamingFuture<P12>, StreamingFuture<P13>, StreamingFuture<P14>, StreamingFuture<P15>, StreamingFuture<P16>, StreamingFuture<P17>, StreamingFuture<P18>> tuple;

    public OctodenaryFunctionFuture(@NotNull final StreamingFuture<R> future,
        @NotNull final StreamingFuture<P1> first,
        @NotNull final StreamingFuture<P2> second,
        @NotNull final StreamingFuture<P3> third,
        @NotNull final StreamingFuture<P4> fourth,
        @NotNull final StreamingFuture<P5> fifth,
        @NotNull final StreamingFuture<P6> sixth,
        @NotNull final StreamingFuture<P7> seventh,
        @NotNull final StreamingFuture<P8> eighth,
        @NotNull final StreamingFuture<P9> ninth,
        @NotNull final StreamingFuture<P10> tenth,
        @NotNull final StreamingFuture<P11> eleventh,
        @NotNull final StreamingFuture<P12> twelfth,
        @NotNull final StreamingFuture<P13> thirteenth,
        @NotNull final StreamingFuture<P14> fourteenth,
        @NotNull final StreamingFuture<P15> fifteenth,
        @NotNull final StreamingFuture<P16> sixteenth,
        @NotNull final StreamingFuture<P17> seventeenth,
        @NotNull final StreamingFuture<P18> eighteenth) {
      super(future);
      tuple = Tuples.asTuple(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth, sixteenth, seventeenth, eighteenth);
    }

    @Override
    public @NotNull Octodecuple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>, StreamingFuture<P4>, StreamingFuture<P5>, StreamingFuture<P6>, StreamingFuture<P7>, StreamingFuture<P8>, StreamingFuture<P9>, StreamingFuture<P10>, StreamingFuture<P11>, StreamingFuture<P12>, StreamingFuture<P13>, StreamingFuture<P14>, StreamingFuture<P15>, StreamingFuture<P16>, StreamingFuture<P17>, StreamingFuture<P18>> parameters() {
      return tuple;
    }
  }

  private static class NovemdenaryFunctionFuture<P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, P16 extends P, P17 extends P, P18 extends P, P19 extends P, R> extends ReadOnlyFuture<R> implements
      NovemdenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, R> {

    private final Novemdecuple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>, StreamingFuture<P4>, StreamingFuture<P5>, StreamingFuture<P6>, StreamingFuture<P7>, StreamingFuture<P8>, StreamingFuture<P9>, StreamingFuture<P10>, StreamingFuture<P11>, StreamingFuture<P12>, StreamingFuture<P13>, StreamingFuture<P14>, StreamingFuture<P15>, StreamingFuture<P16>, StreamingFuture<P17>, StreamingFuture<P18>, StreamingFuture<P19>> tuple;

    public NovemdenaryFunctionFuture(@NotNull final StreamingFuture<R> future,
        @NotNull final StreamingFuture<P1> first,
        @NotNull final StreamingFuture<P2> second,
        @NotNull final StreamingFuture<P3> third,
        @NotNull final StreamingFuture<P4> fourth,
        @NotNull final StreamingFuture<P5> fifth,
        @NotNull final StreamingFuture<P6> sixth,
        @NotNull final StreamingFuture<P7> seventh,
        @NotNull final StreamingFuture<P8> eighth,
        @NotNull final StreamingFuture<P9> ninth,
        @NotNull final StreamingFuture<P10> tenth,
        @NotNull final StreamingFuture<P11> eleventh,
        @NotNull final StreamingFuture<P12> twelfth,
        @NotNull final StreamingFuture<P13> thirteenth,
        @NotNull final StreamingFuture<P14> fourteenth,
        @NotNull final StreamingFuture<P15> fifteenth,
        @NotNull final StreamingFuture<P16> sixteenth,
        @NotNull final StreamingFuture<P17> seventeenth,
        @NotNull final StreamingFuture<P18> eighteenth,
        @NotNull final StreamingFuture<P19> nineteenth) {
      super(future);
      tuple = Tuples.asTuple(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth, sixteenth, seventeenth, eighteenth, nineteenth);
    }

    @Override
    public @NotNull Novemdecuple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>, StreamingFuture<P4>, StreamingFuture<P5>, StreamingFuture<P6>, StreamingFuture<P7>, StreamingFuture<P8>, StreamingFuture<P9>, StreamingFuture<P10>, StreamingFuture<P11>, StreamingFuture<P12>, StreamingFuture<P13>, StreamingFuture<P14>, StreamingFuture<P15>, StreamingFuture<P16>, StreamingFuture<P17>, StreamingFuture<P18>, StreamingFuture<P19>> parameters() {
      return tuple;
    }
  }

  private static class VigenaryFunctionFuture<P, P1 extends P, P2 extends P, P3 extends P, P4 extends P, P5 extends P, P6 extends P, P7 extends P, P8 extends P, P9 extends P, P10 extends P, P11 extends P, P12 extends P, P13 extends P, P14 extends P, P15 extends P, P16 extends P, P17 extends P, P18 extends P, P19 extends P, P20 extends P, R> extends ReadOnlyFuture<R> implements
      VigenaryFuture<P, P1, P2, P3, P4, P5, P6, P7, P8, P9, P10, P11, P12, P13, P14, P15, P16, P17, P18, P19, P20, R> {

    private final Viguple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>, StreamingFuture<P4>, StreamingFuture<P5>, StreamingFuture<P6>, StreamingFuture<P7>, StreamingFuture<P8>, StreamingFuture<P9>, StreamingFuture<P10>, StreamingFuture<P11>, StreamingFuture<P12>, StreamingFuture<P13>, StreamingFuture<P14>, StreamingFuture<P15>, StreamingFuture<P16>, StreamingFuture<P17>, StreamingFuture<P18>, StreamingFuture<P19>, StreamingFuture<P20>> tuple;

    public VigenaryFunctionFuture(@NotNull final StreamingFuture<R> future,
        @NotNull final StreamingFuture<P1> first,
        @NotNull final StreamingFuture<P2> second,
        @NotNull final StreamingFuture<P3> third,
        @NotNull final StreamingFuture<P4> fourth,
        @NotNull final StreamingFuture<P5> fifth,
        @NotNull final StreamingFuture<P6> sixth,
        @NotNull final StreamingFuture<P7> seventh,
        @NotNull final StreamingFuture<P8> eighth,
        @NotNull final StreamingFuture<P9> ninth,
        @NotNull final StreamingFuture<P10> tenth,
        @NotNull final StreamingFuture<P11> eleventh,
        @NotNull final StreamingFuture<P12> twelfth,
        @NotNull final StreamingFuture<P13> thirteenth,
        @NotNull final StreamingFuture<P14> fourteenth,
        @NotNull final StreamingFuture<P15> fifteenth,
        @NotNull final StreamingFuture<P16> sixteenth,
        @NotNull final StreamingFuture<P17> seventeenth,
        @NotNull final StreamingFuture<P18> eighteenth,
        @NotNull final StreamingFuture<P19> nineteenth,
        @NotNull final StreamingFuture<P20> twentieth) {
      super(future);
      tuple = Tuples.asTuple(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth, sixteenth, seventeenth, eighteenth, nineteenth, twentieth);
    }

    @Override
    public @NotNull Viguple<StreamingFuture<? extends P>, StreamingFuture<P1>, StreamingFuture<P2>, StreamingFuture<P3>, StreamingFuture<P4>, StreamingFuture<P5>, StreamingFuture<P6>, StreamingFuture<P7>, StreamingFuture<P8>, StreamingFuture<P9>, StreamingFuture<P10>, StreamingFuture<P11>, StreamingFuture<P12>, StreamingFuture<P13>, StreamingFuture<P14>, StreamingFuture<P15>, StreamingFuture<P16>, StreamingFuture<P17>, StreamingFuture<P18>, StreamingFuture<P19>, StreamingFuture<P20>> parameters() {
      return tuple;
    }
  }

  private abstract class ScopeFuture<U> extends VarFuture<U> implements Scope, Task {

    private static final int IDLE = 0;
    private static final int RUNNING = 1;
    private static final int DONE = 2;
    private static final int FAILED = 3;

    private final HashSet<StreamingFuture<?>> futures = new HashSet<StreamingFuture<?>>();
    private final HashSet<ExecutionScopeReceiver<?>> receivers = new HashSet<ExecutionScopeReceiver<?>>();
    private final AtomicInteger status = new AtomicInteger(IDLE);

    private ScopeStatus scopeStatus = new RunningStatus();

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
      return fail(new FutureCancellationException(mayInterruptIfRunning));
    }

    @Override
    public void close() {
      if (status.compareAndSet(RUNNING, DONE)) {
        scheduler.scheduleAfter(new ContextTask() {
          @Override
          public void run() {
            if (receivers.isEmpty() && futures.isEmpty()) {
              ScopeFuture.super.close();
            }
          }
        });
      }
    }

    @Override
    public @NotNull <R, V extends R> ScopeReceiver<R> decorateReceiver(
        @NotNull final StreamingFuture<V> future, @NotNull final Scheduler scheduler,
        @NotNull final Receiver<R> receiver) {
      final ExecutionScopeReceiver<R> scopeReceiver = new ExecutionScopeReceiver<R>(
          Require.notNull(future, "future"), Require.notNull(scheduler, "scheduler"),
          Require.notNull(receiver, "receiver"));
      ExecutionScope.this.scheduler.scheduleAfter(new ContextTask() {
        @Override
        public void run() {
          scopeStatus.onSubscribe(scopeReceiver);
        }
      });
      return scopeReceiver;
    }

    @Override
    public @Nullable ExecutionContext executionContext() {
      return context;
    }

    @Override
    public boolean fail(@NotNull final Exception error) {
      Require.notNull(error, "error");
      if (!status.compareAndSet(IDLE, FAILED)) {
        if (status.compareAndSet(RUNNING, FAILED)) {
          innerFail(error);
          return super.fail(error);
        }
        return false;
      }
      return super.fail(error);
    }

    @Override
    public @NotNull Registration registerFuture(@NotNull final StreamingFuture<?> future) {
      final ScopeRegistration registration = new ScopeRegistration(
          Require.notNull(future, "future"));
      scheduler.scheduleAfter(new ContextTask() {
        @Override
        public void run() {
          scopeStatus.onCreate(future, registration);
        }
      });
      return registration;
    }

    @Override
    public Object restoreObject(@NotNull final String name) {
      return objects.get(name);
    }

    @Override
    public void run() {
      if (status.compareAndSet(IDLE, RUNNING)) {
        FutureScope.pushScope(this);
        try {
          innerRun();
        } catch (final Exception e) {
          fail(e);
        } finally {
          FutureScope.popScope();
        }
      }
    }

    @Override
    public void runTask(@NotNull final Task task) {
      scheduler.scheduleAfter(task);
    }

    @Override
    public void storeObject(@NotNull final String name, final Object object) {
      if (object == null) {
        objects.remove(name);
      } else {
        objects.put(name, object);
      }
    }

    @Override
    public @NotNull String taskID() {
      return super.taskID();
    }

    @Override
    public int weight() {
      return 1;
    }

    protected void complete() {
      scheduler.scheduleAfter(new ContextTask() {
        @Override
        public void run() {
          if (receivers.isEmpty() && futures.isEmpty()) {
            ScopeFuture.super.close();
          }
        }
      });
    }

    protected abstract void innerRun() throws Exception;

    private void innerFail(@NotNull final Exception error) {
      final ScopeTask task = new ScopeTask() {
        @Override
        protected void runInScope() {
          scopeStatus.onFail(error);
        }
      };
      if (FutureCancellationException.class.equals(error.getClass())) {
        final Scheduler scheduler = ExecutionScope.this.scheduler;
        if (((FutureCancellationException) error).mayInterruptIfRunning()) {
          scheduler.interruptTask(toString());
        }
        scheduler.scheduleBefore(task);
      } else {
        scheduler.scheduleAfter(task);
      }
    }

    private void removeFuture(@NotNull final StreamingFuture<?> future) {
      final HashSet<StreamingFuture<?>> futures = ScopeFuture.this.futures;
      futures.remove(future);
      if (receivers.isEmpty() && futures.isEmpty()) {
        ScopeFuture.super.close();
      }
    }

    private void removeReceiver(@NotNull final ExecutionScopeReceiver<?> receiver) {
      final HashSet<ExecutionScopeReceiver<?>> receivers = ScopeFuture.this.receivers;
      receivers.remove(receiver);
      if (receivers.isEmpty() && futures.isEmpty()) {
        ScopeFuture.super.close();
      }
    }

    private class CancelledStatus extends ScopeStatus {

      private final Exception failureException;

      private CancelledStatus(@NotNull final Exception error) {
        failureException = error;
      }

      @Override
      public void onCreate(@NotNull final StreamingFuture<?> future,
          @NotNull final Registration registration) {
        future.fail(failureException);
        registration.cancel();
      }

      @Override
      public void onFail(@NotNull final Exception error) {
        Log.wrn(ExecutionScope.class,
            "Exception was thrown: %s\nbut it was shadowed by: %s", Log.printable(error),
            Log.printable(failureException));
      }

      @Override
      public void onSubscribe(@NotNull final ExecutionScopeReceiver<?> scopeReceiver) {
        scopeReceiver.fail(failureException);
        scopeReceiver.onUnsubscribe();
      }
    }

    private abstract class ContextTask implements Task {

      @Override
      public @NotNull String taskID() {
        return ScopeFuture.this.taskID();
      }

      @Override
      public int weight() {
        return 1;
      }
    }

    private class ExecutionScopeReceiver<R> implements ScopeReceiver<R> {

      private final StreamingFuture<?> future;
      private final Scheduler futureScheduler;
      private final Receiver<R> wrapped;

      private Receiver<R> status = new RunningStatus();

      private ExecutionScopeReceiver(@NotNull final StreamingFuture<?> future,
          @NotNull final Scheduler futureScheduler, @NotNull final Receiver<R> wrapped) {
        this.future = future;
        this.futureScheduler = futureScheduler;
        this.wrapped = wrapped;
      }

      @Override
      public void close() {
        status.close();
      }

      @Override
      public boolean fail(@NotNull final Exception error) {
        return status.fail(error);
      }

      @Override
      public boolean isConsumer() {
        return true;
      }

      @Override
      public void onReceiverError(@NotNull final Exception error) {
        innerFail(error);
      }

      @Override
      public void onUnsubscribe() {
        status = new DoneStatus();
        futureScheduler.pause();
        scheduler.scheduleAfter(new ContextTask() {
          @Override
          public void run() {
            removeReceiver(ExecutionScopeReceiver.this);
            futureScheduler.resume();
          }
        });
      }

      @Override
      public void set(final R value) {
        status.set(value);
      }

      @Override
      public void setBulk(@NotNull final Collection<R> values) {
        final int throughput = scheduler.minThroughput();
        if (throughput == 1) {
          for (final R value : values) {
            status.set(value);
          }
        } else {
          final ChunkIterator<R> chunkIterator = new ChunkIterator<R>(values, throughput);
          while (chunkIterator.hasNext()) {
            final List<R> chunk = chunkIterator.next();
            if (chunk.size() == 1) {
              status.set(chunk.get(0));
            } else {
              status.setBulk(values);
            }
          }
        }
      }

      private void failAndUnsubscribe(@NotNull final Exception error) {
        FutureScope.pushScope(ScopeFuture.this);
        try {
          wrapped.fail(error);
        } catch (final RuntimeException e) {
          Log.err(ExecutionScope.class, "Uncaught exception: %s", Log.printable(e));
        } finally {
          FutureScope.popScope();
        }
        future.unsubscribe(wrapped);
      }

      private class DoneStatus implements Receiver<R> {

        @Override
        public void close() {
        }

        @Override
        public boolean fail(@NotNull final Exception error) {
          return false;
        }

        @Override
        public void set(final R value) {
        }

        @Override
        public void setBulk(@NotNull final Collection<R> values) {
        }
      }

      private abstract class ReceiverTask extends ScopeTask {

        private ReceiverTask() {
          futureScheduler.pause();
        }

        @Override
        protected void runInScope() {
          try {
            runInScheduler();
          } finally {
            futureScheduler.resume();
          }
        }

        protected abstract void runInScheduler();
      }

      private class RunningStatus implements Receiver<R> {

        @Override
        public void close() {
          status = new DoneStatus();
          scheduler.scheduleAfter(new ReceiverTask() {
            @Override
            protected void runInScheduler() {
              wrapped.close();
            }
          });
        }

        @Override
        public boolean fail(@NotNull final Exception error) {
          Require.notNull(error, "error");
          status = new DoneStatus();
          scheduler.scheduleAfter(new ReceiverTask() {
            @Override
            protected void runInScheduler() {
              wrapped.fail(error);
            }
          });
          return true;
        }

        @Override
        public void set(final R value) {
          scheduler.scheduleAfter(new ReceiverTask() {
            @Override
            protected void runInScheduler() {
              wrapped.set(value);
            }
          });
        }

        @Override
        public void setBulk(@NotNull final Collection<R> values) {
          Require.notNull(values, "values");
          scheduler.scheduleAfter(new ReceiverTask() {
            @Override
            protected void runInScheduler() {
              wrapped.setBulk(values);
            }
          });
        }
      }
    }

    private class RunningStatus extends ScopeStatus {

      @Override
      public void onCreate(@NotNull final StreamingFuture<?> future,
          @NotNull final Registration registration) {
        futures.add(future);
      }

      @Override
      public void onFail(@NotNull final Exception error) {
        scopeStatus = new CancelledStatus(error);
        final HashSet<ExecutionScopeReceiver<?>> receivers = ScopeFuture.this.receivers;
        for (final ExecutionScopeReceiver<?> receiver : receivers) {
          receiver.failAndUnsubscribe(error);
        }
        receivers.clear();
        final HashSet<StreamingFuture<?>> futures = ScopeFuture.this.futures;
        for (final StreamingFuture<?> future : futures) {
          if (!future.isReadOnly()) {
            future.fail(error);
          } else {
            future.cancel(false);
          }
        }
        futures.clear();
      }

      @Override
      public void onSubscribe(@NotNull final ExecutionScopeReceiver<?> scopeReceiver) {
        receivers.add(scopeReceiver);
      }
    }

    private class ScopeRegistration implements Registration {

      private final StreamingFuture<?> future;

      private ScopeRegistration(@NotNull final StreamingFuture<?> future) {
        this.future = future;
      }

      @Override
      public void cancel() {
        scheduler.scheduleAfter(new ContextTask() {
          @Override
          public void run() {
            removeFuture(future);
          }
        });
      }

      @Override
      public void onUncaughtError(@NotNull final Exception error) {
        innerFail(error);
      }
    }

    private abstract class ScopeTask extends ContextTask {

      @Override
      public void run() {
        FutureScope.pushScope(ScopeFuture.this);
        try {
          runInScope();
        } catch (final RuntimeException e) {
          fail(e);
        } finally {
          FutureScope.popScope();
        }
      }

      protected abstract void runInScope();
    }

    private abstract class ScopeStatus {

      abstract void onCreate(@NotNull StreamingFuture<?> future,
          @NotNull Registration registration);

      abstract void onFail(@NotNull Exception error);

      abstract void onSubscribe(@NotNull ExecutionScopeReceiver<?> scopeReceiver);
    }
  }
}
