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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sparx.concurrent.Scheduler.Task;
import sparx.function.Action;
import sparx.function.Consumer;

class ProxyFuture<V> extends DecoratedFuture<V> {

  private final Scheduler scheduler = Scheduler.trampoline();
  private final String taskID = toString();

  ProxyFuture(@NotNull final StreamingFuture<V> wrapped) {
    super(wrapped);
    scheduler.pause();
  }

  @Override
  public @NotNull StreamingFuture<V> readOnly() {
    return new ReadOnlyFuture<V>(this);
  }

  @Override
  public @NotNull Subscription subscribe(@NotNull final Receiver<? super V> receiver) {
    final ProxySubscription subscription = new ProxySubscription(receiver);
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
        wrapped().subscribe(receiver);
      }
    });
    return subscription;
  }

  @Override
  public @NotNull Subscription subscribe(@Nullable final Consumer<? super V> onValueConsumer,
      @Nullable final Consumer<? super Collection<V>> onBulkConsumer,
      @Nullable final Consumer<Exception> onErrorConsumer, @Nullable final Action onCloseAction) {
    return subscribe(new FunctionalReceiver<V>(onValueConsumer, onBulkConsumer, onErrorConsumer,
        onCloseAction));
  }

  @Override
  public @NotNull Subscription subscribeNext(@NotNull final Receiver<? super V> receiver) {
    final ProxySubscription subscription = new ProxySubscription(receiver);
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
        wrapped().subscribeNext(receiver);
      }
    });
    return subscription;
  }

  @Override
  public @NotNull Subscription subscribeNext(@Nullable final Consumer<? super V> onValueConsumer,
      @Nullable final Consumer<? super Collection<V>> onBulkConsumer,
      @Nullable final Consumer<Exception> onErrorConsumer, @Nullable final Action onCloseAction) {
    return subscribeNext(new FunctionalReceiver<V>(onValueConsumer, onBulkConsumer, onErrorConsumer,
        onCloseAction));
  }

  @Override
  public void unsubscribe(@NotNull final Receiver<?> receiver) {
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
        wrapped().unsubscribe(receiver);
      }
    });
  }

  void connect() {
    scheduler.resume();
  }

  private class ProxySubscription implements Subscription {

    private final Receiver<?> receiver;

    private ProxySubscription(@NotNull final Receiver<?> receiver) {
      this.receiver = receiver;
    }

    @Override
    public void cancel() {
      unsubscribe(receiver);
    }
  }
}
