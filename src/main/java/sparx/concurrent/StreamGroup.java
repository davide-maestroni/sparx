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
import java.util.Map.Entry;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.FutureGroup.Group;
import sparx.concurrent.FutureGroup.GroupReceiver;
import sparx.concurrent.FutureGroup.Registration;
import sparx.concurrent.Scheduler.Task;
import sparx.logging.Log;
import sparx.util.Requires;

class StreamGroup<U> implements Group, GroupReceiver<U> {

  private final Group group;
  private final HashMap<Receiver<?>, Scheduler> receivers = new HashMap<Receiver<?>, Scheduler>();
  private final Scheduler scheduler = Scheduler.trampoline();

  private GroupStatus status = new RunningStatus();

  StreamGroup(@NotNull final FutureGroup.Group group) {
    this.group = Requires.notNull(group, "hooks");
  }

  @Override
  public @NotNull FutureGroup.Registration onCreate(@NotNull final StreamingFuture<?> future) {
    final StreamRegistration registration = new StreamRegistration(group.onCreate(future),
        future);
    scheduler.scheduleAfter(new Task() {
      @Override
      public int weight() {
        return 1;
      }

      @Override
      public void run() {
        status.onCreate(future, registration);
      }
    });
    return registration;
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull <R, V extends R> FutureGroup.GroupReceiver<R> onSubscribe(
      @NotNull final StreamingFuture<V> future, @NotNull final Scheduler scheduler,
      @NotNull final Receiver<R> receiver) {
    if (receiver == this) {
      return (GroupReceiver<R>) this;
    }

    final StreamGroupReceiver<R> hookReceiver = new StreamGroupReceiver<R>(
        group.onSubscribe(future, scheduler, new StreamReceiver<R>(receiver)), future, receiver);
    this.scheduler.scheduleAfter(new Task() {
      @Override
      public int weight() {
        return 1;
      }

      @Override
      public void run() {
        status.onSubscribe(hookReceiver, scheduler);
      }
    });
    return hookReceiver;
  }

  @Override
  public void onTask(@NotNull final Task task) {
    group.onTask(task);
  }

  @Override
  public boolean isSink() {
    return false;
  }

  @Override
  public void onUnsubscribe() {
  }

  @Override
  public void onUncaughtError(@NotNull final Exception error) {
  }

  @Override
  public void close() {
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

  private void innerFail(@NotNull final Exception error) {
    final Task task = new Task() {
      @Override
      public int weight() {
        return 1;
      }

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

  private interface GroupStatus {

    void onCreate(@NotNull StreamingFuture<?> future,
        @NotNull FutureGroup.Registration registration);

    void onFail(@NotNull Exception error);

    void onSubscribe(@NotNull FutureGroup.GroupReceiver<?> groupReceiver,
        @NotNull Scheduler scheduler);
  }

  private static class CancelledStatus implements GroupStatus {

    private final Exception failureException;

    private CancelledStatus(@NotNull final Exception error) {
      failureException = error;
    }

    @Override
    public void onCreate(@NotNull final StreamingFuture<?> future,
        @NotNull final FutureGroup.Registration registration) {
      future.fail(failureException);
      registration.cancel();
    }

    @Override
    public void onFail(@NotNull final Exception error) {
      Log.wrn(StreamGroup.class, "Exception was thrown: %s\nbut it was shadowed by: %s",
          Log.printable(error), Log.printable(failureException));
    }

    @Override
    public void onSubscribe(@NotNull final FutureGroup.GroupReceiver<?> groupReceiver,
        @NotNull final Scheduler scheduler) {
      groupReceiver.fail(failureException);
      groupReceiver.onUnsubscribe();
    }
  }

  private class RunningStatus implements GroupStatus {

    @Override
    public void onCreate(@NotNull final StreamingFuture<?> future,
        @NotNull final FutureGroup.Registration registration) {
      receivers.put(future, null);
    }

    @Override
    public void onFail(@NotNull final Exception error) {
      status = new CancelledStatus(error);
      final HashMap<Receiver<?>, Scheduler> receivers = StreamGroup.this.receivers;
      for (final Entry<Receiver<?>, Scheduler> entry : receivers.entrySet()) {
        final Receiver<?> receiver = entry.getKey();
        if (StreamGroupReceiver.class.equals(receiver.getClass())) {
          entry.getValue().scheduleAfter(new Task() {
            @Override
            public int weight() {
              return 1;
            }

            @Override
            @SuppressWarnings("unchecked")
            public void run() {
              ((StreamGroupReceiver<Object>) receiver).failAndUnsubscribe(error);
            }
          });
        } else {
          receiver.fail(error);
        }
      }
      receivers.clear();
    }

    @Override
    public void onSubscribe(@NotNull final FutureGroup.GroupReceiver<?> groupReceiver,
        @NotNull final Scheduler scheduler) {
      receivers.put(groupReceiver, scheduler);
    }
  }

  private class StreamRegistration implements Registration {

    private final StreamingFuture<?> future;
    private final Registration wrapped;

    private StreamRegistration(@NotNull final Registration wrapped,
        @NotNull final StreamingFuture<?> future) {
      this.wrapped = Requires.notNull(wrapped, "wrapped");
      this.future = Requires.notNull(future, "future");
    }

    @Override
    public void cancel() {
      scheduler.scheduleAfter(new Task() {
        @Override
        public int weight() {
          return 1;
        }

        @Override
        public void run() {
          receivers.remove(future);
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
      FutureGroup.pushGroup(StreamGroup.this);
      try {
        wrapped.close();
      } finally {
        FutureGroup.popGroup();
      }
    }

    @Override
    public boolean fail(@NotNull final Exception error) {
      FutureGroup.pushGroup(StreamGroup.this);
      try {
        return wrapped.fail(error);
      } finally {
        FutureGroup.popGroup();
      }
    }

    @Override
    public void set(final R value) {
      FutureGroup.pushGroup(StreamGroup.this);
      try {
        wrapped.set(value);
      } finally {
        FutureGroup.popGroup();
      }
    }

    @Override
    public void setBulk(@NotNull final Collection<R> values) {
      FutureGroup.pushGroup(StreamGroup.this);
      try {
        wrapped.setBulk(values);
      } finally {
        FutureGroup.popGroup();
      }
    }
  }

  private class StreamGroupReceiver<R> implements GroupReceiver<R> {

    private final StreamingFuture<?> future;
    private final Receiver<R> receiver;
    private final GroupReceiver<R> wrapped;

    private Receiver<R> status = new RunningStatus();

    private StreamGroupReceiver(@NotNull final FutureGroup.GroupReceiver<R> wrapped,
        @NotNull final StreamingFuture<?> future, @NotNull final Receiver<R> receiver) {
      this.wrapped = wrapped;
      this.future = future;
      this.receiver = receiver;
    }

    @Override
    public boolean isSink() {
      return true;
    }

    @Override
    public void onUnsubscribe() {
      status = new DoneStatus();
      scheduler.scheduleAfter(new Task() {
        @Override
        public int weight() {
          return 1;
        }

        @Override
        public void run() {
          receivers.remove(receiver);
          wrapped.onUnsubscribe();
        }
      });
    }

    @Override
    public void onUncaughtError(@NotNull final Exception error) {
      innerFail(error);
      wrapped.onUncaughtError(error);
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

    private void failAndUnsubscribe(@NotNull final Exception error) {
      FutureGroup.pushGroup(StreamGroup.this);
      try {
        wrapped.fail(error);
      } catch (final RuntimeException e) {
        Log.err(StreamGroup.class, "Uncaught exception: %s", Log.printable(e));
      } finally {
        FutureGroup.popGroup();
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
}
