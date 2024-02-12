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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sparx.concurrent.FutureScope.Registration;
import sparx.concurrent.FutureScope.Scope;
import sparx.concurrent.FutureScope.ScopeReceiver;
import sparx.concurrent.Scheduler.Task;
import sparx.util.Require;
import sparx.util.logging.Log;

class StreamScope<U> implements Scope, ScopeReceiver<U> {

  private final HashSet<StreamingFuture<?>> futures = new HashSet<StreamingFuture<?>>();
  private final HashMap<StreamScopeReceiver<?>, Scheduler> receivers = new HashMap<StreamScopeReceiver<?>, Scheduler>();
  private final Scheduler scheduler = Scheduler.trampoline();
  private final Scope scope;
  private final String taskID = toString();

  private ScopeStatus status = new RunningStatus();

  StreamScope(@NotNull final Scope scope) {
    this.scope = Require.notNull(scope, "scope");
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <R, V extends R> ScopeReceiver<R> decorateReceiver(
      @NotNull final StreamingFuture<V> future, @NotNull final Scheduler scheduler,
      @NotNull final Receiver<R> receiver) {
    if (receiver == this) {
      return (ScopeReceiver<R>) this;
    }

    final StreamScopeReceiver<R> scopeReceiver = new StreamScopeReceiver<R>(
        scope.decorateReceiver(future, scheduler, new StreamReceiver<R>(receiver)), future,
        receiver);
    this.scheduler.scheduleAfter(new StreamTask() {
      @Override
      public void run() {
        status.onSubscribe(scopeReceiver, scheduler);
      }
    });
    return scopeReceiver;
  }

  @Override
  public void close() {
    innerClose();
  }

  @Override
  public @Nullable ExecutionContext executionContext() {
    return scope.executionContext();
  }

  @Override
  public boolean fail(@NotNull final Exception error) {
    innerFail(error);
    return true;
  }

  @Override
  public boolean isConsumer() {
    return false;
  }

  @Override
  public void onReceiverError(@NotNull final Exception error) {
    innerFail(error);
  }

  @Override
  public void onUnsubscribe() {
  }

  @Override
  public @NotNull Registration registerFuture(@NotNull final StreamingFuture<?> future) {
    final StreamRegistration registration = new StreamRegistration(scope.registerFuture(future),
        future);
    scheduler.scheduleAfter(new StreamTask() {
      @Override
      public void run() {
        status.onCreate(future, registration);
      }
    });
    return registration;
  }

  @Override
  public Object restoreObject(@NotNull final String name) {
    return scope.restoreObject(name);
  }

  @Override
  public void runTask(@NotNull final Task task) {
    scope.runTask(task);
  }

  @Override
  public void set(final U value) {
  }

  @Override
  public void setBulk(@NotNull final Collection<U> values) {
  }

  @Override
  public void storeObject(@NotNull final String name, final Object object) {
    scope.storeObject(name, object);
  }

  private void innerClose() {
    scheduler.scheduleAfter(new StreamTask() {
      @Override
      public void run() {
        status.onClose();
      }
    });
  }

  private void innerFail(@NotNull final Exception error) {
    final StreamTask task = new StreamTask() {
      @Override
      public void run() {
        status.onFail(error);
      }
    };
    if (FutureCancellationException.class.equals(error.getClass())) {
      scheduler.scheduleBefore(task);
    } else {
      scheduler.scheduleAfter(task);
    }
  }

  private class CancelledStatus extends ScopeStatus {

    private final Exception failureException;

    private CancelledStatus(@NotNull final Exception error) {
      failureException = error;
    }

    @Override
    public void onClose() {
    }

    @Override
    public void onCreate(@NotNull final StreamingFuture<?> future,
        @NotNull final Registration registration) {
      future.fail(failureException);
      registration.cancel();
    }

    @Override
    public void onFail(@NotNull final Exception error) {
      Log.wrn(StreamScope.class, "Exception was thrown: %s\nbut it was shadowed by: %s",
          Log.printable(error), Log.printable(failureException));
    }

    @Override
    public void onSubscribe(@NotNull final StreamScopeReceiver<?> scopeReceiver,
        @NotNull final Scheduler scheduler) {
      scopeReceiver.fail(failureException);
      scopeReceiver.onUnsubscribe();
    }
  }

  private class RunningStatus extends ScopeStatus {

    @Override
    public void onClose() {
      final HashMap<StreamScopeReceiver<?>, Scheduler> receivers = StreamScope.this.receivers;
      for (final Entry<StreamScopeReceiver<?>, Scheduler> entry : receivers.entrySet()) {
        final StreamScopeReceiver<?> receiver = entry.getKey();
        entry.getValue().scheduleAfter(new StreamTask() {
          @Override
          public void run() {
            receiver.closeAndUnsubscribe();
          }
        });
      }
      receivers.clear();
      final HashSet<StreamingFuture<?>> futures = StreamScope.this.futures;
      for (final StreamingFuture<?> future : futures) {
        if (!future.isReadOnly()) {
          future.close();
        }
      }
      futures.clear();
    }

    @Override
    public void onCreate(@NotNull final StreamingFuture<?> future,
        @NotNull final Registration registration) {
      futures.add(future);
    }

    @Override
    public void onFail(@NotNull final Exception error) {
      status = new CancelledStatus(error);
      final HashMap<StreamScopeReceiver<?>, Scheduler> receivers = StreamScope.this.receivers;
      for (final Entry<StreamScopeReceiver<?>, Scheduler> entry : receivers.entrySet()) {
        final StreamScopeReceiver<?> receiver = entry.getKey();
        entry.getValue().scheduleAfter(new StreamTask() {
          @Override
          public void run() {
            receiver.failAndUnsubscribe(error);
          }
        });
      }
      receivers.clear();
      final HashSet<StreamingFuture<?>> futures = StreamScope.this.futures;
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
    public void onSubscribe(@NotNull final StreamScopeReceiver<?> scopeReceiver,
        @NotNull final Scheduler scheduler) {
      receivers.put(scopeReceiver, scheduler);
    }
  }

  private abstract class ScopeStatus {

    abstract void onClose();

    abstract void onCreate(@NotNull StreamingFuture<?> future, @NotNull Registration registration);

    abstract void onFail(@NotNull Exception error);

    abstract void onSubscribe(@NotNull StreamScopeReceiver<?> scopeReceiver,
        @NotNull Scheduler scheduler);
  }

  private class StreamReceiver<R> implements Receiver<R> {

    private final Receiver<R> wrapped;

    private StreamReceiver(@NotNull final Receiver<R> wrapped) {
      this.wrapped = wrapped;
    }

    @Override
    public void close() {
      FutureScope.pushScope(StreamScope.this);
      try {
        wrapped.close();
      } finally {
        FutureScope.popScope();
      }
    }

    @Override
    public boolean fail(@NotNull final Exception error) {
      FutureScope.pushScope(StreamScope.this);
      try {
        return wrapped.fail(error);
      } finally {
        FutureScope.popScope();
      }
    }

    @Override
    public void set(final R value) {
      FutureScope.pushScope(StreamScope.this);
      try {
        wrapped.set(value);
      } finally {
        FutureScope.popScope();
      }
    }

    @Override
    public void setBulk(@NotNull final Collection<R> values) {
      FutureScope.pushScope(StreamScope.this);
      try {
        wrapped.setBulk(values);
      } finally {
        FutureScope.popScope();
      }
    }
  }

  private class StreamRegistration implements Registration {

    private final StreamingFuture<?> future;
    private final Registration wrapped;

    private StreamRegistration(@NotNull final Registration wrapped,
        @NotNull final StreamingFuture<?> future) {
      this.wrapped = wrapped;
      this.future = future;
    }

    @Override
    public void cancel() {
      scheduler.scheduleAfter(new StreamTask() {
        @Override
        public void run() {
          futures.remove(future);
          wrapped.cancel();
        }
      });
    }

    @Override
    public void onUncaughtError(@NotNull final Exception error) {
      innerFail(error);
    }
  }

  private class StreamScopeReceiver<R> implements ScopeReceiver<R> {

    private final StreamingFuture<?> future;
    private final Receiver<R> receiver;
    private final ScopeReceiver<R> wrapped;

    private Receiver<R> status = new RunningStatus();

    private StreamScopeReceiver(@NotNull final ScopeReceiver<R> wrapped,
        @NotNull final StreamingFuture<?> future, @NotNull final Receiver<R> receiver) {
      this.wrapped = wrapped;
      this.future = future;
      this.receiver = receiver;
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
      wrapped.onReceiverError(error);
    }

    @Override
    public void onUnsubscribe() {
      status = new DoneStatus();
      scheduler.scheduleAfter(new StreamTask() {
        @Override
        public void run() {
          receivers.remove(StreamScopeReceiver.this);
          wrapped.onUnsubscribe();
        }
      });
    }

    @Override
    public void set(final R value) {
      status.set(value);
    }

    @Override
    public void setBulk(@NotNull final Collection<R> values) {
      status.setBulk(values);
    }

    private void closeAndUnsubscribe() {
      FutureScope.pushScope(StreamScope.this);
      try {
        wrapped.close();
      } catch (final RuntimeException e) {
        Log.err(StreamScope.class, "Uncaught exception: %s", Log.printable(e));
      } finally {
        FutureScope.popScope();
      }
      future.unsubscribe(receiver);
    }

    private void failAndUnsubscribe(@NotNull final Exception error) {
      FutureScope.pushScope(StreamScope.this);
      try {
        wrapped.fail(error);
      } catch (final RuntimeException e) {
        Log.err(StreamScope.class, "Uncaught exception: %s", Log.printable(e));
      } finally {
        FutureScope.popScope();
      }
      future.unsubscribe(receiver);
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
        return wrapped.fail(error);
      }

      @Override
      public void set(final R value) {
        wrapped.set(value);
      }

      @Override
      public void setBulk(@NotNull final Collection<R> values) {
        wrapped.setBulk(values);
      }

      @Override
      public void close() {
        status = new DoneStatus();
        wrapped.close();
      }
    }
  }

  private abstract class StreamTask implements Task {

    @Override
    public @NotNull String taskID() {
      return taskID;
    }

    @Override
    public int weight() {
      return 1;
    }
  }
}
