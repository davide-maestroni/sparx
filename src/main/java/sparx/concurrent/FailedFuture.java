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
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sparx.function.Action;
import sparx.function.Consumer;
import sparx.logging.Log;
import sparx.util.LiveIterator;
import sparx.util.Requires;

public class FailedFuture<V> extends ReadOnlyStreamGroupFuture<V, StreamingFuture<V>> implements
    StreamingFuture<V> {

  public static @NotNull <V> FailedFuture<V> of(@NotNull final Exception error) {
    return new FailedFuture<V>(Requires.notNull(error, "error"));
  }

  private final Exception error;
  private final FailedIterator<V> iterator;

  private FailedFuture(@NotNull final Exception error) {
    this.error = error;
    this.iterator = new FailedIterator<V>(error);
  }

  @Override
  public V getCurrent() {
    throw new NoSuchElementException();
  }

  @Override
  public V getCurrentOr(final V defaultValue) {
    return defaultValue;
  }

  @Override
  public @NotNull Subscription subscribe(@NotNull final Receiver<? super V> receiver) {
    try {
      receiver.fail(error);
    } catch (final RuntimeException e) {
      Log.err(FailedFuture.class, "Uncaught exception: %s", Log.printable(e));
    }
    return DummySubscription.instance();
  }

  @Override
  public @NotNull Subscription subscribe(@Nullable final Consumer<? super V> onValueConsumer,
      @Nullable final Consumer<? super Collection<V>> onValuesConsumer,
      @Nullable final Consumer<Exception> onErrorConsumer, @Nullable final Action onCloseAction) {
    if (onErrorConsumer != null) {
      try {
        onErrorConsumer.accept(error);
      } catch (final Exception e) {
        Log.err(FailedFuture.class, "Uncaught exception: %s", Log.printable(e));
      }
    }
    return DummySubscription.instance();
  }

  @Override
  public void unsubscribe(@NotNull final Receiver<?> receiver) {
  }

  @Override
  public @NotNull LiveIterator<V> iterator() {
    return iterator;
  }

  @Override
  public @NotNull LiveIterator<V> iterator(final long timeout, @NotNull final TimeUnit unit) {
    return iterator();
  }

  @Override
  public @NotNull FailedFuture<V> readOnly() {
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
  public List<V> get() throws ExecutionException {
    throw new ExecutionException(error);
  }

  @Override
  public List<V> get(final long timeout, @NotNull final TimeUnit unit) throws ExecutionException {
    return get();
  }

  private static class FailedIterator<V> implements LiveIterator<V> {

    private final Exception error;

    private FailedIterator(@NotNull final Exception error) {
      this.error = error;
    }

    @Override
    public boolean hasNext(final long timeout, @NotNull final TimeUnit unit) {
      return hasNext();
    }

    @Override
    public V next(final long timeout, @NotNull final TimeUnit unit) {
      return next();
    }

    @Override
    public boolean hasNext() {
      throw new IllegalStateException(error);
    }

    @Override
    public V next() {
      throw new IllegalStateException(error);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove");
    }
  }
}
