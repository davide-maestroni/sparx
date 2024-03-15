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
package sparx0.concurrent.tuple;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sparx0.concurrent.EmptyLiveIterator;
import sparx0.concurrent.LiveIterator;
import sparx0.concurrent.Receiver;
import sparx0.concurrent.Scheduler;
import sparx0.concurrent.Scheduler.Task;
import sparx0.concurrent.StreamingFuture;
import sparx0.util.ImmutableList;
import sparx0.util.Nothing;
import sparx0.util.Require;
import sparx0.util.function.Action;
import sparx0.util.function.Consumer;
import sparx0.util.logging.Log;
import sparx0.util.tuple.Empty;

public class EmptyFuture<V> extends StreamScopeTupleFuture<V, EmptyFuture<V>> implements
    Empty<StreamingFuture<? extends V>> {

  private static final Subscription DUMMY_SUBSCRIPTION = new Subscription() {
    @Override
    public void cancel() {
    }
  };
  private static final EmptyFuture<?> INSTANCE = new EmptyFuture<Object>();

  private EmptyFuture() {
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <V> EmptyFuture<V> instance() {
    return (EmptyFuture<V>) INSTANCE;
  }

  @Override
  public @NotNull List<StreamingFuture<? extends V>> asList() {
    return ImmutableList.of();
  }

  @Override
  public boolean cancel(final boolean mayInterruptIfRunning) {
    return false;
  }

  @Override
  public List<Nothing> get() {
    return ImmutableList.of();
  }

  @Override
  public List<Nothing> get(final long timeout, @NotNull final TimeUnit unit) {
    return get();
  }

  @Override
  public boolean isCancelled() {
    return false;
  }

  @Override
  public boolean isDone() {
    return true;
  }

  @Override
  public @NotNull LiveIterator<Nothing> iterator() {
    return EmptyLiveIterator.instance();
  }

  @Override
  public @NotNull LiveIterator<Nothing> iterator(final long timeout, @NotNull final TimeUnit unit) {
    return iterator();
  }

  @Override
  public @NotNull LiveIterator<Nothing> iteratorNext() {
    return iterator();
  }

  @Override
  public @NotNull LiveIterator<Nothing> iteratorNext(long timeout, @NotNull TimeUnit unit) {
    return iterator();
  }

  @Override
  public @NotNull EmptyFuture<V> readOnly() {
    return this;
  }

  @Override
  public @NotNull Subscription subscribe(@NotNull final Receiver<? super Nothing> receiver) {
    try {
      receiver.close();
    } catch (final RuntimeException e) {
      Log.err(EmptyFuture.class, "Uncaught exception: %s", Log.printable(e));
    }
    return DUMMY_SUBSCRIPTION;
  }

  @Override
  public @NotNull Subscription subscribe(@Nullable final Consumer<? super Nothing> onValueConsumer,
      @Nullable final Consumer<? super Collection<Nothing>> onBulkConsumer,
      @Nullable final Consumer<Exception> onErrorConsumer, @Nullable final Action onCloseAction) {
    if (onCloseAction != null) {
      try {
        onCloseAction.run();
      } catch (final Exception e) {
        Log.err(EmptyFuture.class, "Uncaught exception: %s", Log.printable(e));
      }
    }
    return DUMMY_SUBSCRIPTION;
  }

  @Override
  public @NotNull Subscription subscribeNext(@NotNull final Receiver<? super Nothing> receiver) {
    return subscribe(receiver);
  }

  @Override
  public @NotNull Subscription subscribeNext(
      @Nullable final Consumer<? super Nothing> onValueConsumer,
      @Nullable final Consumer<? super Collection<Nothing>> onBulkConsumer,
      @Nullable final Consumer<Exception> onErrorConsumer, @Nullable final Action onCloseAction) {
    return subscribe(onValueConsumer, onBulkConsumer, onErrorConsumer, onCloseAction);
  }

  @Override
  public void unsubscribe(@NotNull final Receiver<?> receiver) {
  }

  @Override
  protected @NotNull EmptyFuture<V> createPaused() {
    return new PausedEmptyFuture<V>();
  }

  @Override
  protected void resumePaused(@NotNull final EmptyFuture<V> pausedFuture) {
    ((PausedEmptyFuture<V>) pausedFuture).connect();
  }

  private static class PausedEmptyFuture<V> extends EmptyFuture<V> {

    private final Scheduler scheduler = Scheduler.trampoline();
    private final String taskID = toString();

    private PausedEmptyFuture() {
      scheduler.pause();
    }

    @Override
    public @NotNull Subscription subscribe(@NotNull final Receiver<? super Nothing> receiver) {
      Require.notNull(receiver, "receiver");
      scheduler.scheduleAfter(new Task() {
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
          INSTANCE.subscribe(receiver);
        }
      });
      return DUMMY_SUBSCRIPTION;
    }

    @Override
    public @NotNull Subscription subscribe(
        @Nullable final Consumer<? super Nothing> onValueConsumer,
        @Nullable final Consumer<? super Collection<Nothing>> onBulkConsumer,
        @Nullable final Consumer<Exception> onErrorConsumer, @Nullable final Action onCloseAction) {
      scheduler.scheduleAfter(new Task() {
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
          INSTANCE.subscribe(onValueConsumer, onBulkConsumer, onErrorConsumer, onCloseAction);
        }
      });
      return DUMMY_SUBSCRIPTION;
    }

    private void connect() {
      scheduler.resume();
    }
  }
}
