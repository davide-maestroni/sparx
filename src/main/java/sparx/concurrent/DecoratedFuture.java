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
import sparx.function.Function;
import sparx.util.LiveIterator;
import sparx.util.Requires;

public class DecoratedFuture<V> extends StreamGroupFuture<V, StreamingFuture<V>> implements
    StreamingFuture<V> {

  private final StreamingFuture<V> wrapped;

  public DecoratedFuture(@NotNull final StreamingFuture<V> wrapped) {
    this.wrapped = Requires.notNull(wrapped, "wrapped");
  }

  @Override
  public void clear() {
    wrapped.clear();
  }

  @Override
  public void compute(@NotNull final Function<? super V, ? extends V> function) {
    wrapped.compute(function);
  }

  @Override
  public V getCurrent() {
    return wrapped.getCurrent();
  }

  @Override
  public V getCurrentOr(final V defaultValue) {
    return wrapped.getCurrentOr(defaultValue);
  }

  @Override
  public boolean isReadOnly() {
    return wrapped.isReadOnly();
  }

  @Override
  public @NotNull StreamingFuture<V> readOnly() {
    return wrapped.readOnly();
  }

  @Override
  public @NotNull Subscription subscribe(@NotNull final Receiver<? super V> receiver) {
    return wrapped.subscribe(receiver);
  }

  @Override
  public @NotNull Subscription subscribe(@Nullable final Consumer<? super V> onValueConsumer,
      @Nullable final Consumer<? super Collection<V>> onValuesConsumer,
      @Nullable final Consumer<Exception> onErrorConsumer, @Nullable final Action onCloseAction) {
    return wrapped.subscribe(onValueConsumer, onValuesConsumer, onErrorConsumer, onCloseAction);
  }

  @Override
  public void unsubscribe(@NotNull final Receiver<?> receiver) {
    wrapped.unsubscribe(receiver);
  }

  @Override
  public @NotNull LiveIterator<V> iterator() {
    return wrapped.iterator();
  }

  @Override
  public @NotNull LiveIterator<V> iterator(long timeout, @NotNull final TimeUnit unit) {
    return wrapped.iterator(timeout, unit);
  }

  @Override
  public boolean cancel(final boolean mayInterruptIfRunning) {
    return wrapped.cancel(mayInterruptIfRunning);
  }

  @Override
  public boolean isCancelled() {
    return wrapped.isCancelled();
  }

  @Override
  public boolean isDone() {
    return wrapped.isDone();
  }

  @Override
  public List<V> get() throws InterruptedException, ExecutionException {
    return wrapped.get();
  }

  @Override
  public List<V> get(final long timeout, final @NotNull TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    return wrapped.get(timeout, unit);
  }

  @Override
  public boolean fail(@NotNull final Exception error) {
    return wrapped.fail(error);
  }

  @Override
  public void set(final V value) {
    wrapped.set(value);
  }

  @Override
  public void setBulk(@NotNull final Collection<V> values) {
    wrapped.setBulk(values);
  }

  @Override
  public void close() {
    wrapped.close();
  }

  protected @NotNull StreamingFuture<V> wrapped() {
    return wrapped;
  }
}
