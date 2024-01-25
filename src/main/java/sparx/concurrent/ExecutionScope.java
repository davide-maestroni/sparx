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
import sparx.concurrent.FutureContext.Context;
import sparx.concurrent.FutureContext.ContextReceiver;
import sparx.concurrent.FutureContext.Registration;
import sparx.concurrent.Scheduler.Task;
import sparx.function.Consumer;
import sparx.function.Function;
import sparx.logging.Log;
import sparx.logging.alert.Alerts;
import sparx.logging.alert.ExecutionContextTaskAlert;
import sparx.util.Nothing;
import sparx.util.Require;

class ExecutionScope implements ExecutionContext {

  private static final ExecutionContextTaskAlert taskAlert = Alerts.executionContextTaskAlert();

  private final ExecutionContext context;
  private final FutureRegistry registry;
  private final Scheduler scheduler;
  private final Map<String, Object> values;

  ExecutionScope(@NotNull final ExecutionContext context, @NotNull final Map<String, Object> values,
      @NotNull final Scheduler scheduler, @NotNull final FutureRegistry registry) {
    this.context = Require.notNull(context, "context");
    this.values = Require.notNull(values, "values");
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
  public @NotNull <V, F extends TupleFuture<V, ?>, U> StreamingFuture<U> call(
      @NotNull final F future,
      @NotNull final Function<? super F, ? extends SignalFuture<U>> function) {
    taskAlert.notifyCall(function);
    final CallFuture<V, F, U> task = new CallFuture<V, F, U>(future, function);
    registry.register(task);
    scheduler.scheduleAfter(task);
    return task.readOnly();
  }

  @Override
  public @NotNull <V, F extends TupleFuture<V, ?>> StreamingFuture<Nothing> run(
      @NotNull final F future, @NotNull final Consumer<? super F> consumer) {
    taskAlert.notifyRun(consumer);
    final RunFuture<V, F> task = new RunFuture<V, F>(future, consumer);
    registry.register(task);
    scheduler.scheduleAfter(task);
    return task.readOnly();
  }

  public interface FutureRegistry {

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

  private interface ContextStatus {

    void onCreate(@NotNull StreamingFuture<?> future, @NotNull Registration registration);

    void onFail(@NotNull Exception error);

    void onSubscribe(@NotNull ContextReceiver<?> contextReceiver);
  }

  private abstract class ScopeFuture<U> extends VarFuture<U> implements Context, Task {

    private static final int IDLE = 0;
    private static final int RUNNING = 1;
    private static final int DONE = 2;
    private static final int FAILED = 3;

    private final HashSet<Receiver<?>> receivers = new HashSet<Receiver<?>>();
    private final AtomicInteger status = new AtomicInteger(IDLE);
    private final String taskID = toString();

    private ContextStatus contextStatus = new RunningStatus();

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
            if (receivers.isEmpty()) {
              ScopeFuture.super.close();
            }
          }
        });
      }
    }

    @Override
    public boolean fail(@NotNull final Exception error) {
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
    public @Nullable ExecutionContext executionContext() {
      return context;
    }

    @Override
    public @NotNull Registration onCreate(@NotNull final StreamingFuture<?> future) {
      final ScopeRegistration registration = new ScopeRegistration(future);
      scheduler.scheduleAfter(new ContextTask() {
        @Override
        public void run() {
          contextStatus.onCreate(future, registration);
        }
      });
      return registration;
    }

    @Override
    public @NotNull <R, V extends R> ContextReceiver<R> onSubscribe(
        @NotNull final StreamingFuture<V> future, @NotNull final Scheduler scheduler,
        @NotNull final Receiver<R> receiver) {
      final ScopeContextReceiver<R> contextReceiver = new ScopeContextReceiver<R>(future, scheduler,
          receiver);
      ExecutionScope.this.scheduler.scheduleAfter(new ContextTask() {
        @Override
        public void run() {
          contextStatus.onSubscribe(contextReceiver);
        }
      });
      return contextReceiver;
    }

    @Override
    public void onTask(@NotNull final Task task) {
      scheduler.scheduleAfter(task);
    }

    @Override
    public Object restoreValue(@NotNull final String name) {
      return values.get(name);
    }

    @Override
    public void storeValue(@NotNull final String name, final Object value) {
      if (value == null) {
        values.remove(name);
      } else {
        values.put(name, value);
      }
    }

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
      if (status.compareAndSet(IDLE, RUNNING)) {
        FutureContext.pushContext(this);
        try {
          innerRun();
        } catch (final Exception e) {
          fail(e);
        } finally {
          FutureContext.popContext();
        }
      }
    }

    protected void complete() {
      scheduler.scheduleAfter(new ContextTask() {
        @Override
        public void run() {
          if (receivers.isEmpty()) {
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
          contextStatus.onFail(error);
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

    private void removeReceiver(@NotNull final Receiver<?> receiver) {
      final HashSet<Receiver<?>> receivers = ScopeFuture.this.receivers;
      receivers.remove(receiver);
      if (receivers.isEmpty()) {
        ScopeFuture.super.close();
      }
    }

    private class CancelledStatus implements ContextStatus {

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
      public void onSubscribe(@NotNull final ContextReceiver<?> contextReceiver) {
        contextReceiver.fail(failureException);
        contextReceiver.onUnsubscribe();
      }
    }

    private class RunningStatus implements ContextStatus {

      @Override
      public void onCreate(@NotNull final StreamingFuture<?> future,
          @NotNull final Registration registration) {
        receivers.add(future);
      }

      @Override
      @SuppressWarnings("unchecked")
      public void onFail(@NotNull final Exception error) {
        contextStatus = new CancelledStatus(error);
        final HashSet<Receiver<?>> receivers = ScopeFuture.this.receivers;
        for (final Receiver<?> receiver : receivers) {
          if (ScopeContextReceiver.class.equals(receiver.getClass())) {
            ((ScopeContextReceiver<Object>) receiver).failAndUnsubscribe(error);
          } else {
            receiver.fail(error);
          }
        }
        receivers.clear();
      }

      @Override
      public void onSubscribe(@NotNull final ContextReceiver<?> contextReceiver) {
        receivers.add(contextReceiver);
      }
    }

    private class ScopeContextReceiver<R> implements ContextReceiver<R> {

      private final StreamingFuture<?> future;
      private final Scheduler futureScheduler;
      private final Receiver<R> wrapped;

      private Receiver<R> status = new RunningStatus();

      private ScopeContextReceiver(@NotNull final StreamingFuture<?> future,
          @NotNull final Scheduler futureScheduler,
          @NotNull final Receiver<R> wrapped) {
        this.future = future;
        this.futureScheduler = futureScheduler;
        this.wrapped = wrapped;
      }

      @Override
      public boolean isSink() {
        return true;
      }

      @Override
      public void onUnsubscribe() {
        status = new DoneStatus();
        futureScheduler.pause();
        scheduler.scheduleAfter(new ContextTask() {
          @Override
          public void run() {
            removeReceiver(ScopeContextReceiver.this);
            futureScheduler.resume();
          }
        });
      }

      @Override
      public void onUncaughtError(@NotNull final Exception error) {
        innerFail(error);
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
        FutureContext.pushContext(ScopeFuture.this);
        try {
          wrapped.fail(error);
        } catch (final RuntimeException e) {
          Log.err(ExecutionScope.class, "Uncaught exception: %s", Log.printable(e));
        } finally {
          FutureContext.popContext();
        }
        future.unsubscribe(wrapped);
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

      private class DoneStatus implements Receiver<R> {

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

        @Override
        public void close() {
        }
      }

      private class RunningStatus implements Receiver<R> {

        @Override
        public boolean fail(@NotNull final Exception error) {
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
          scheduler.scheduleAfter(new ReceiverTask() {
            @Override
            protected void runInScheduler() {
              wrapped.setBulk(values);
            }
          });
        }

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
      }
    }

    private class ScopeRegistration implements Registration {

      private final StreamingFuture<?> future;

      private ScopeRegistration(@NotNull final StreamingFuture<?> future) {
        this.future = Require.notNull(future, "future");
      }

      @Override
      public void cancel() {
        scheduler.scheduleAfter(new ContextTask() {
          @Override
          public void run() {
            removeReceiver(future);
          }
        });
      }

      @Override
      public void onUncaughtError(@NotNull final Exception error) {
        innerFail(error);
      }
    }

    private abstract class ContextTask implements Task {

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        return 1;
      }
    }

    private abstract class ScopeTask extends ContextTask {

      @Override
      public void run() {
        FutureContext.pushContext(ScopeFuture.this);
        try {
          runInScope();
        } catch (final RuntimeException e) {
          fail(e);
        } finally {
          FutureContext.popContext();
        }
      }

      protected abstract void runInScope();
    }
  }

  private class CallFuture<V, F extends TupleFuture<V, ?>, U> extends ScopeFuture<U> {

    private final Function<? super F, ? extends SignalFuture<U>> function;
    private final F future;

    private CallFuture(@NotNull final F future,
        @NotNull final Function<? super F, ? extends SignalFuture<U>> function) {
      this.future = Require.notNull(future, "future");
      this.function = Require.notNull(function, "function");
    }

    @Override
    protected void innerRun() throws Exception {
      function.apply(future).subscribe(this);
    }
  }

  private class RunFuture<V, F extends TupleFuture<V, ?>> extends ScopeFuture<Nothing> {

    private final Consumer<? super F> consumer;
    private final F future;

    private RunFuture(@NotNull final F future, @NotNull final Consumer<? super F> consumer) {
      this.future = Require.notNull(future, "future");
      this.consumer = Require.notNull(consumer, "consumer");
    }

    @Override
    protected void innerRun() throws Exception {
      consumer.accept(future);
      complete();
    }
  }
}
