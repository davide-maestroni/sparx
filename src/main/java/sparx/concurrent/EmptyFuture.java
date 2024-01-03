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
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sparx.function.Action;
import sparx.function.Consumer;
import sparx.logging.Log;
import sparx.tuple.Empty;
import sparx.util.ImmutableList;
import sparx.util.LiveIterator;
import sparx.util.Nothing;

public class EmptyFuture extends TupleStreamGroupFuture<Nothing, EmptyFuture> implements
    Empty<StreamingFuture<? extends Nothing>> {

  private static final EmptyFuture INSTANCE = new EmptyFuture();

  public static @NotNull EmptyFuture instance() {
    return INSTANCE;
  }

  private EmptyFuture() {
  }

  @Override
  public @NotNull Subscription subscribe(@NotNull final Receiver<? super Nothing> receiver) {
    try {
      receiver.close();
    } catch (final RuntimeException e) {
      Log.err(EmptyFuture.class, "Uncaught exception: %s", Log.printable(e));
    }
    return DummySubscription.instance();
  }

  @Override
  public @NotNull Subscription subscribe(@Nullable final Consumer<? super Nothing> onValueConsumer,
      @Nullable final Consumer<? super Collection<Nothing>> onValuesConsumer,
      @Nullable final Consumer<Exception> onErrorConsumer, @Nullable final Action onCloseAction) {
    if (onCloseAction != null) {
      try {
        onCloseAction.run();
      } catch (final Exception e) {
        Log.err(EmptyFuture.class, "Uncaught exception: %s", Log.printable(e));
      }
    }
    return DummySubscription.instance();
  }

  @Override
  public void unsubscribe(@NotNull final Receiver<?> receiver) {
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
  public boolean cancel(final boolean mayInterruptIfRunning) {
    return false;
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
  public List<Nothing> get() throws InterruptedException, ExecutionException {
    return ImmutableList.of();
  }

  @Override
  public List<Nothing> get(final long timeout, @NotNull final TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    return get();
  }

  @Override
  public @NotNull List<StreamingFuture<? extends Nothing>> asList() {
    return ImmutableList.of();
  }
}
