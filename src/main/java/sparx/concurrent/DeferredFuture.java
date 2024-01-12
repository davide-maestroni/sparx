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
import sparx.function.Supplier;
import sparx.util.LiveIterator;
import sparx.util.Requires;
import sparx.util.UncheckedException;

public class DeferredFuture<V> extends ReadOnlyStreamGroupFuture<V, StreamingFuture<V>> implements
    StreamingFuture<V> {

  private final Object lock = new Object();

  private Supplier<StreamingFuture<V>> supplier;

  public static @NotNull <V> DeferredFuture<V> of(
      @NotNull final Supplier<StreamingFuture<V>> supplier) {
    return new DeferredFuture<V>(supplier);
  }

  private DeferredFuture(@NotNull final Supplier<StreamingFuture<V>> supplier) {
    Requires.notNull(supplier, "supplier");
    this.supplier = new Supplier<StreamingFuture<V>>() {
      @Override
      public StreamingFuture<V> get() throws Exception {
        final StreamingFuture<V> future = supplier.get();
        DeferredFuture.this.supplier = new Supplier<StreamingFuture<V>>() {
          @Override
          public StreamingFuture<V> get() {
            return future;
          }
        };
        return future;
      }
    };
  }

  @Override
  public V getCurrent() {
    return getFuture().getCurrent();
  }

  @Override
  public V getCurrentOr(final V defaultValue) {
    return getFuture().getCurrentOr(defaultValue);
  }

  @Override
  public @NotNull Subscription subscribe(@NotNull final Receiver<? super V> receiver) {
    return getFuture().subscribe(receiver);
  }

  @Override
  public @NotNull Subscription subscribe(@Nullable final Consumer<? super V> onValueConsumer,
      @Nullable final Consumer<? super Collection<V>> onValuesConsumer,
      @Nullable final Consumer<Exception> onErrorConsumer, @Nullable final Action onCloseAction) {
    return getFuture().subscribe(onValueConsumer, onValuesConsumer, onErrorConsumer, onCloseAction);
  }

  @Override
  public void unsubscribe(@NotNull final Receiver<?> receiver) {
    getFuture().unsubscribe(receiver);
  }

  @Override
  public @NotNull LiveIterator<V> iterator() {
    return getFuture().iterator();
  }

  @Override
  public @NotNull LiveIterator<V> iterator(final long timeout, @NotNull final TimeUnit unit) {
    return getFuture().iterator(timeout, unit);
  }

  @Override
  public @NotNull StreamingFuture<V> readOnly() {
    return this;
  }

  @Override
  public boolean cancel(final boolean mayInterruptIfRunning) {
    return getFuture().cancel(mayInterruptIfRunning);
  }

  @Override
  public boolean isCancelled() {
    return getFuture().isCancelled();
  }

  @Override
  public boolean isDone() {
    return getFuture().isDone();
  }

  @Override
  public List<V> get() throws InterruptedException, ExecutionException {
    return getFuture().get();
  }

  @Override
  public List<V> get(final long timeout, @NotNull final TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    return getFuture().get(timeout, unit);
  }

  @Override
  protected @NotNull StreamingFuture<V> createFuture() {
    return new VarFuture<V>();
  }

  @Override
  protected void subscribeFuture(@NotNull final StreamingFuture<V> future) {
    subscribe(future);
  }

  private @NotNull StreamingFuture<V> getFuture() {
    try {
      synchronized (lock) {
        return supplier.get();
      }
    } catch (final Exception e) {
      throw UncheckedException.throwUnchecked(e);
    }
  }
}
