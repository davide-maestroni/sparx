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
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.FutureGroup.Group;
import sparx.concurrent.FutureGroup.GroupReceiver;
import sparx.concurrent.FutureGroup.Registration;
import sparx.concurrent.Scheduler.Task;
import sparx.function.Consumer;
import sparx.function.Function;
import sparx.logging.Log;
import sparx.logging.alert.Alerts;
import sparx.logging.alert.ExecutionContextTaskAlert;
import sparx.util.Nothing;
import sparx.util.Requires;

class ExecutionScope implements ExecutionContext {

  private static final ExecutionContextTaskAlert taskAlert = Alerts.executionContextTaskAlert();

  private final Scheduler scheduler;

  ExecutionScope(@NotNull final Scheduler scheduler) {
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
    taskAlert.notifyCall(function);
    final CallFuture<V, F, U> task = new CallFuture<V, F, U>(scheduler, future, function, weight);
    scheduler.scheduleAfter(task);
    return task.readOnly();
  }

  @Override
  public @NotNull <V, F extends TupleFuture<V, ?>> StreamingFuture<Nothing> run(
      @NotNull final F future, @NotNull final Consumer<F> consumer, final int weight) {
    taskAlert.notifyRun(consumer);
    final RunFuture<V, F> task = new RunFuture<V, F>(scheduler, future, consumer, weight);
    scheduler.scheduleAfter(task);
    return task.readOnly();
  }

  private static abstract class ScopeFuture<U> extends VarFuture<U> implements Group, Task {

    private static final int IDLE = 0;
    private static final int RUNNING = 1;
    private static final int DONE = 2;
    private static final int FAILED = 3;

    private final HashSet<Receiver<?>> receivers = new HashSet<Receiver<?>>();
    private final Scheduler scheduler;
    private final AtomicInteger status = new AtomicInteger(IDLE);
    private final String taskID = toString();
    private final int weight;

    private GroupStatus groupStatus = new RunningStatus();

    ScopeFuture(@NotNull final Scheduler scheduler, final int weight) {
      this.scheduler = scheduler;
      this.weight = Requires.positive(weight, "weight");
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
      return fail(new FutureCancellationException(mayInterruptIfRunning));
    }

    @Override
    public void close() {
      if (status.compareAndSet(RUNNING, DONE)) {
        scheduler.scheduleAfter(new GroupTask() {
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
    public @NotNull Registration onCreate(@NotNull final StreamingFuture<?> future) {
      final ScopeRegistration registration = new ScopeRegistration(future);
      scheduler.scheduleAfter(new GroupTask() {
        @Override
        public void run() {
          groupStatus.onCreate(future, registration);
        }
      });
      return registration;
    }

    @Override
    public @NotNull <R, V extends R> GroupReceiver<R> onSubscribe(
        @NotNull final StreamingFuture<V> future, @NotNull final Scheduler scheduler,
        @NotNull final Receiver<R> receiver) {
      final ScopeGroupReceiver<R> groupReceiver = new ScopeGroupReceiver<R>(future, scheduler,
          receiver);
      this.scheduler.scheduleAfter(new GroupTask() {
        @Override
        public void run() {
          groupStatus.onSubscribe(groupReceiver);
        }
      });
      return groupReceiver;
    }

    @Override
    public void onTask(@NotNull final Task task) {
      scheduler.scheduleAfter(task);
    }

    @Override
    public @NotNull String taskID() {
      return taskID;
    }

    @Override
    public int weight() {
      return weight;
    }

    @Override
    public void run() {
      if (status.compareAndSet(IDLE, RUNNING)) {
        FutureGroup.pushGroup(this);
        try {
          innerRun();
        } catch (final Exception e) {
          fail(e);
        } finally {
          FutureGroup.popGroup();
        }
      }
    }

    protected void complete() {
      scheduler.scheduleAfter(new GroupTask() {
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
          groupStatus.onFail(error);
        }
      };
      if (FutureCancellationException.class.equals(error.getClass())) {
        final Scheduler scheduler = this.scheduler;
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

    private interface GroupStatus {

      void onCreate(@NotNull StreamingFuture<?> future, @NotNull Registration registration);

      void onFail(@NotNull Exception error);

      void onSubscribe(@NotNull GroupReceiver<?> groupReceiver);
    }

    private static class CancelledStatus implements GroupStatus {

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
      public void onSubscribe(@NotNull final GroupReceiver<?> groupReceiver) {
        groupReceiver.fail(failureException);
        groupReceiver.onUnsubscribe();
      }
    }

    private class RunningStatus implements GroupStatus {

      @Override
      public void onCreate(@NotNull final StreamingFuture<?> future,
          @NotNull final Registration registration) {
        receivers.add(future);
      }

      @Override
      @SuppressWarnings("unchecked")
      public void onFail(@NotNull final Exception error) {
        groupStatus = new CancelledStatus(error);
        final HashSet<Receiver<?>> receivers = ScopeFuture.this.receivers;
        for (final Receiver<?> receiver : receivers) {
          if (ScopeGroupReceiver.class.equals(receiver.getClass())) {
            ((ScopeGroupReceiver<Object>) receiver).failAndUnsubscribe(error);
          } else {
            receiver.fail(error);
          }
        }
        receivers.clear();
      }

      @Override
      public void onSubscribe(@NotNull final GroupReceiver<?> groupReceiver) {
        receivers.add(groupReceiver);
      }
    }

    private class ScopeGroupReceiver<R> implements GroupReceiver<R> {

      private final StreamingFuture<?> future;
      private final Scheduler futureScheduler;
      private final Receiver<R> wrapped;

      private Receiver<R> status = new RunningStatus();

      private ScopeGroupReceiver(@NotNull final StreamingFuture<?> future,
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
        scheduler.scheduleAfter(new GroupTask() {
          @Override
          public void run() {
            removeReceiver(ScopeGroupReceiver.this);
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
        FutureGroup.pushGroup(ScopeFuture.this);
        try {
          wrapped.fail(error);
        } catch (final RuntimeException e) {
          Log.err(ExecutionScope.class, "Uncaught exception: %s", Log.printable(e));
        } finally {
          FutureGroup.popGroup();
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
        this.future = Requires.notNull(future, "future");
      }

      @Override
      public void cancel() {
        scheduler.scheduleAfter(new GroupTask() {
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

    private abstract class GroupTask implements Task {

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        return 1;
      }
    }

    private abstract class ScopeTask extends GroupTask {

      @Override
      public void run() {
        FutureGroup.pushGroup(ScopeFuture.this);
        try {
          runInScope();
        } catch (final RuntimeException e) {
          fail(e);
        } finally {
          FutureGroup.popGroup();
        }
      }

      protected abstract void runInScope();
    }
  }

  private static class CallFuture<V, F extends TupleFuture<V, ?>, U> extends ScopeFuture<U> {

    private final Function<F, ? extends SignalFuture<U>> function;
    private final F future;

    private CallFuture(@NotNull final Scheduler scheduler, @NotNull final F future,
        @NotNull final Function<F, ? extends SignalFuture<U>> function, final int weight) {
      super(scheduler, weight);
      this.future = Requires.notNull(future, "future");
      this.function = Requires.notNull(function, "function");
    }

    @Override
    protected void innerRun() throws Exception {
      function.apply(future).subscribe(this);
    }
  }

  private static class RunFuture<V, F extends TupleFuture<V, ?>> extends ScopeFuture<Nothing> {

    private final Consumer<F> consumer;
    private final F future;

    private RunFuture(@NotNull final Scheduler scheduler, @NotNull final F future,
        @NotNull final Consumer<F> consumer, final int weight) {
      super(scheduler, weight);
      this.future = Requires.notNull(future, "future");
      this.consumer = Requires.notNull(consumer, "consumer");
    }

    @Override
    protected void innerRun() throws Exception {
      consumer.accept(future);
      complete();
    }
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
}
