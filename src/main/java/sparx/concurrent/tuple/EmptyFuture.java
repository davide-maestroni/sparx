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
package sparx.concurrent.tuple;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sparx.concurrent.EmptyLiveIterator;
import sparx.concurrent.Receiver;
import sparx.concurrent.Scheduler;
import sparx.concurrent.Scheduler.Task;
import sparx.concurrent.StreamingFuture;
import sparx.function.Action;
import sparx.function.Consumer;
import sparx.logging.Log;
import sparx.tuple.Empty;
import sparx.util.ImmutableList;
import sparx.util.LiveIterator;
import sparx.util.Nothing;
import sparx.util.Require;

public class EmptyFuture extends StreamScopeTupleFuture<Nothing, EmptyFuture> implements
    Empty<StreamingFuture<? extends Nothing>> {

  private static final Subscription DUMMY_SUBSCRIPTION = new Subscription() {
    @Override
    public void cancel() {
    }
  };
  private static final EmptyFuture INSTANCE = new EmptyFuture();

  private EmptyFuture() {
  }

  public static @NotNull EmptyFuture instance() {
    return INSTANCE;
  }

  @Override
  public @NotNull List<StreamingFuture<? extends Nothing>> asList() {
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
  public @NotNull EmptyFuture readOnly() {
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
  public void unsubscribe(@NotNull final Receiver<?> receiver) {
  }

  @Override
  protected @NotNull EmptyFuture createProxy() {
    return new ProxyEmptyFuture();
  }

  @Override
  protected void subscribeProxy(@NotNull final EmptyFuture proxyFuture) {
    ((ProxyEmptyFuture) proxyFuture).connect();
  }

  private static class ProxyEmptyFuture extends EmptyFuture {

    private final Scheduler scheduler = Scheduler.trampoline();
    private final String taskID = toString();

    private ProxyEmptyFuture() {
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
