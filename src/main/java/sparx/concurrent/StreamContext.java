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
import sparx.concurrent.FutureContext.Context;
import sparx.concurrent.FutureContext.ContextReceiver;
import sparx.concurrent.FutureContext.Registration;
import sparx.concurrent.Scheduler.Task;
import sparx.logging.Log;
import sparx.util.Require;

class StreamContext<U> implements Context, ContextReceiver<U> {

  private final Context context;
  private final HashSet<StreamingFuture<?>> futures = new HashSet<StreamingFuture<?>>();
  private final HashMap<StreamContextReceiver<?>, Scheduler> receivers = new HashMap<StreamContextReceiver<?>, Scheduler>();
  private final Scheduler scheduler = Scheduler.trampoline();
  private final String taskID = toString();

  private ContextStatus status = new RunningStatus();

  StreamContext(@NotNull final Context context) {
    this.context = Require.notNull(context, "hooks");
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <R, V extends R> ContextReceiver<R> decorateReceiver(
      @NotNull final StreamingFuture<V> future, @NotNull final Scheduler scheduler,
      @NotNull final Receiver<R> receiver) {
    if (receiver == this) {
      return (ContextReceiver<R>) this;
    }

    final StreamContextReceiver<R> contextReceiver = new StreamContextReceiver<R>(
        context.decorateReceiver(future, scheduler, new StreamReceiver<R>(receiver)), future,
        receiver);
    this.scheduler.scheduleAfter(new ContextTask() {
      @Override
      public void run() {
        status.onSubscribe(contextReceiver, scheduler);
      }
    });
    return contextReceiver;
  }

  @Override
  public @Nullable ExecutionContext executionContext() {
    return context.executionContext();
  }

  @Override
  public @NotNull FutureContext.Registration registerFuture(
      @NotNull final StreamingFuture<?> future) {
    final StreamRegistration registration = new StreamRegistration(context.registerFuture(future),
        future);
    scheduler.scheduleAfter(new ContextTask() {
      @Override
      public void run() {
        status.onCreate(future, registration);
      }
    });
    return registration;
  }

  @Override
  public Object restoreValue(@NotNull final String name) {
    return context.restoreValue(name);
  }

  @Override
  public void runTask(@NotNull final Task task) {
    context.runTask(task);
  }

  @Override
  public void storeValue(@NotNull final String name, final Object value) {
    context.storeValue(name, value);
  }

  @Override
  public boolean isConsumer() {
    return false;
  }

  @Override
  public void onReceiverError(@NotNull final Exception error) {
  }

  @Override
  public void onUnsubscribe() {
  }

  @Override
  public void close() {
    innerClose();
  }

  @Override
  public boolean fail(@NotNull final Exception error) {
    innerFail(error);
    return true;
  }

  @Override
  public void set(final U value) {
  }

  @Override
  public void setBulk(@NotNull final Collection<U> values) {
  }

  private void innerClose() {
    scheduler.scheduleAfter(new ContextTask() {
      @Override
      public void run() {
        status.onClose();
      }
    });
  }

  private void innerFail(@NotNull final Exception error) {
    final ContextTask task = new ContextTask() {
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

  private abstract class ContextStatus {

    abstract void onClose();

    abstract void onCreate(@NotNull StreamingFuture<?> future, @NotNull Registration registration);

    abstract void onFail(@NotNull Exception error);

    abstract void onSubscribe(@NotNull StreamContextReceiver<?> contextReceiver,
        @NotNull Scheduler scheduler);
  }

  private class CancelledStatus extends ContextStatus {

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
      Log.wrn(StreamContext.class, "Exception was thrown: %s\nbut it was shadowed by: %s",
          Log.printable(error), Log.printable(failureException));
    }

    @Override
    public void onSubscribe(@NotNull final StreamContextReceiver<?> contextReceiver,
        @NotNull final Scheduler scheduler) {
      contextReceiver.fail(failureException);
      contextReceiver.onUnsubscribe();
    }
  }

  private class RunningStatus extends ContextStatus {

    @Override
    public void onClose() {
      final HashMap<StreamContextReceiver<?>, Scheduler> receivers = StreamContext.this.receivers;
      for (final Entry<StreamContextReceiver<?>, Scheduler> entry : receivers.entrySet()) {
        final StreamContextReceiver<?> receiver = entry.getKey();
        entry.getValue().scheduleAfter(new ContextTask() {
          @Override
          public void run() {
            receiver.closeAndUnsubscribe();
          }
        });
      }
      receivers.clear();
      final HashSet<StreamingFuture<?>> futures = StreamContext.this.futures;
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
      final HashMap<StreamContextReceiver<?>, Scheduler> receivers = StreamContext.this.receivers;
      for (final Entry<StreamContextReceiver<?>, Scheduler> entry : receivers.entrySet()) {
        final StreamContextReceiver<?> receiver = entry.getKey();
        entry.getValue().scheduleAfter(new ContextTask() {
          @Override
          public void run() {
            receiver.failAndUnsubscribe(error);
          }
        });
      }
      receivers.clear();
      final HashSet<StreamingFuture<?>> futures = StreamContext.this.futures;
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
    public void onSubscribe(@NotNull final StreamContextReceiver<?> contextReceiver,
        @NotNull final Scheduler scheduler) {
      receivers.put(contextReceiver, scheduler);
    }
  }

  private class StreamRegistration implements Registration {

    private final StreamingFuture<?> future;
    private final Registration wrapped;

    private StreamRegistration(@NotNull final Registration wrapped,
        @NotNull final StreamingFuture<?> future) {
      this.wrapped = Require.notNull(wrapped, "wrapped");
      this.future = Require.notNull(future, "future");
    }

    @Override
    public void cancel() {
      scheduler.scheduleAfter(new ContextTask() {
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

  private class StreamReceiver<R> implements Receiver<R> {

    private final Receiver<R> wrapped;

    private StreamReceiver(@NotNull final Receiver<R> wrapped) {
      this.wrapped = wrapped;
    }

    @Override
    public void close() {
      FutureContext.pushContext(StreamContext.this);
      try {
        wrapped.close();
      } finally {
        FutureContext.popContext();
      }
    }

    @Override
    public boolean fail(@NotNull final Exception error) {
      FutureContext.pushContext(StreamContext.this);
      try {
        return wrapped.fail(error);
      } finally {
        FutureContext.popContext();
      }
    }

    @Override
    public void set(final R value) {
      FutureContext.pushContext(StreamContext.this);
      try {
        wrapped.set(value);
      } finally {
        FutureContext.popContext();
      }
    }

    @Override
    public void setBulk(@NotNull final Collection<R> values) {
      FutureContext.pushContext(StreamContext.this);
      try {
        wrapped.setBulk(values);
      } finally {
        FutureContext.popContext();
      }
    }
  }

  private class StreamContextReceiver<R> implements ContextReceiver<R> {

    private final StreamingFuture<?> future;
    private final Receiver<R> receiver;
    private final ContextReceiver<R> wrapped;

    private Receiver<R> status = new RunningStatus();

    private StreamContextReceiver(@NotNull final ContextReceiver<R> wrapped,
        @NotNull final StreamingFuture<?> future, @NotNull final Receiver<R> receiver) {
      this.wrapped = wrapped;
      this.future = future;
      this.receiver = receiver;
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
      scheduler.scheduleAfter(new ContextTask() {
        @Override
        public void run() {
          receivers.remove(StreamContextReceiver.this);
          wrapped.onUnsubscribe();
        }
      });
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
      status.setBulk(values);
    }

    private void closeAndUnsubscribe() {
      FutureContext.pushContext(StreamContext.this);
      try {
        wrapped.close();
      } catch (final RuntimeException e) {
        Log.err(StreamContext.class, "Uncaught exception: %s", Log.printable(e));
      } finally {
        FutureContext.popContext();
      }
      future.unsubscribe(receiver);
    }

    private void failAndUnsubscribe(@NotNull final Exception error) {
      FutureContext.pushContext(StreamContext.this);
      try {
        wrapped.fail(error);
      } catch (final RuntimeException e) {
        Log.err(StreamContext.class, "Uncaught exception: %s", Log.printable(e));
      } finally {
        FutureContext.popContext();
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
}
