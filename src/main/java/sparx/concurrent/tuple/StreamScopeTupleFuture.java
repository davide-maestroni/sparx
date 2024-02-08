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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sparx.concurrent.FunctionalReceiver;
import sparx.concurrent.LiveIterator;
import sparx.concurrent.ReadOnlyStreamScopeFuture;
import sparx.concurrent.Receiver;
import sparx.concurrent.StreamingFuture;
import sparx.concurrent.TupleFuture;
import sparx.function.Action;
import sparx.function.Consumer;
import sparx.util.ImmutableList;
import sparx.util.Nothing;
import sparx.util.Require;
import sparx.util.UncheckedException;

abstract class StreamScopeTupleFuture<V, F extends TupleFuture<V, F>> extends
    ReadOnlyStreamScopeFuture<Nothing, F> implements TupleFuture<V, F> {

  private final Map<Receiver<?>, TupleSubscription> receivers = Collections.synchronizedMap(
      new WeakHashMap<Receiver<?>, TupleSubscription>());

  @Override
  public boolean cancel(final boolean mayInterruptIfRunning) {
    for (final StreamingFuture<? extends V> future : asList()) {
      if (!future.cancel(mayInterruptIfRunning)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public List<Nothing> get() throws InterruptedException, ExecutionException {
    for (final StreamingFuture<? extends V> future : asList()) {
      future.get();
    }
    return ImmutableList.of();
  }

  @Override
  public List<Nothing> get(final long timeout, @NotNull final TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    for (final StreamingFuture<? extends V> future : asList()) {
      future.get(timeout, unit);
    }
    return ImmutableList.of();
  }

  @Override
  public Nothing getCurrent() {
    throw new NoSuchElementException();
  }

  @Override
  public Nothing getCurrentOr(final Nothing defaultValue) {
    return defaultValue;
  }

  @Override
  public boolean isCancelled() {
    for (final StreamingFuture<? extends V> future : asList()) {
      if (!future.isCancelled()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean isDone() {
    for (final StreamingFuture<? extends V> future : asList()) {
      if (!future.isDone()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public @NotNull LiveIterator<Nothing> iterator() {
    return new IndefiniteIterator();
  }

  @Override
  public @NotNull LiveIterator<Nothing> iterator(final long timeout, @NotNull final TimeUnit unit) {
    return new TimeoutIterator(unit.toMillis(Require.positive(timeout, "timeout")));
  }

  @Override
  public @NotNull LiveIterator<Nothing> iteratorNext() {
    return iterator();
  }

  @Override
  public @NotNull LiveIterator<Nothing> iteratorNext(final long timeout,
      @NotNull final TimeUnit unit) {
    return iterator(timeout, unit);
  }

  @Override
  public @NotNull Subscription subscribe(@NotNull final Receiver<? super Nothing> receiver) {
    final Collection<StreamingFuture<? extends V>> futures = asList();
    final TupleReceiver<V> tupleReceiver = new TupleReceiver<V>(
        Require.notNull(receiver, "receiver"), futures.size());
    final ArrayList<Subscription> subscriptions = new ArrayList<Subscription>(futures.size());
    for (final StreamingFuture<? extends V> future : futures) {
      subscriptions.add(future.subscribe(tupleReceiver));
    }
    final TupleSubscription subscription = new TupleSubscription(subscriptions);
    receivers.put(receiver, subscription);
    return subscription;
  }

  @Override
  public @NotNull Subscription subscribe(
      @Nullable final Consumer<? super Nothing> onValueConsumer,
      @Nullable final Consumer<? super Collection<Nothing>> onBulkConsumer,
      @Nullable final Consumer<Exception> onErrorConsumer, @Nullable final Action onCloseAction) {
    return subscribe(new FunctionalReceiver<Nothing>(onValueConsumer, onBulkConsumer,
        onErrorConsumer, onCloseAction));
  }

  @Override
  public @NotNull Subscription subscribeNext(@NotNull final Receiver<? super Nothing> receiver) {
    final Collection<StreamingFuture<? extends V>> futures = asList();
    final TupleReceiver<V> tupleReceiver = new TupleReceiver<V>(
        Require.notNull(receiver, "receiver"), futures.size());
    final ArrayList<Subscription> subscriptions = new ArrayList<Subscription>(futures.size());
    for (final StreamingFuture<? extends V> future : futures) {
      subscriptions.add(future.subscribeNext(tupleReceiver));
    }
    final TupleSubscription subscription = new TupleSubscription(subscriptions);
    receivers.put(receiver, subscription);
    return subscription;
  }

  @Override
  public @NotNull Subscription subscribeNext(
      @Nullable final Consumer<? super Nothing> onValueConsumer,
      @Nullable final Consumer<? super Collection<Nothing>> onBulkConsumer,
      @Nullable final Consumer<Exception> onErrorConsumer, @Nullable final Action onCloseAction) {
    return subscribeNext(new FunctionalReceiver<Nothing>(onValueConsumer, onBulkConsumer,
        onErrorConsumer, onCloseAction));
  }

  @Override
  public void unsubscribe(@NotNull final Receiver<?> receiver) {
    final TupleSubscription subscription = receivers.get(receiver);
    if (subscription != null) {
      subscription.cancel();
    }
  }

  private static class TupleReceiver<V> implements Receiver<V> {

    private final Semaphore count;
    private final AtomicBoolean done = new AtomicBoolean(false);
    private final Receiver<? super Nothing> receiver;

    private TupleReceiver(@NotNull final Receiver<? super Nothing> receiver, final int size) {
      this.receiver = receiver;
      this.count = new Semaphore(size);
    }

    @Override
    public void close() {
      if (count.tryAcquire() && done.compareAndSet(false, true)) {
        receiver.close();
      }
    }

    @Override
    public boolean fail(@NotNull final Exception error) {
      return done.compareAndSet(false, true) && receiver.fail(error);
    }

    @Override
    public void set(final V value) {
    }

    @Override
    public void setBulk(@NotNull final Collection<V> values) {
    }
  }

  private static class TupleSubscription implements Subscription {

    private final Collection<Subscription> subscriptions;

    private TupleSubscription(@NotNull final Collection<Subscription> subscriptions) {
      this.subscriptions = subscriptions;
    }

    @Override
    public void cancel() {
      for (final Subscription subscription : subscriptions) {
        subscription.cancel();
      }
    }
  }

  private class IndefiniteIterator implements LiveIterator<Nothing> {

    @Override
    public boolean hasNext() {
      for (final StreamingFuture<? extends V> future : asList()) {
        try {
          future.get();
        } catch (final Exception e) {
          throw UncheckedException.throwUnchecked(e);
        }
      }
      return false;
    }

    @Override
    public boolean hasNext(final long timeout, @NotNull final TimeUnit unit) {
      long timeoutMillis = unit.toMillis(Require.positive(timeout, "timeout"));
      for (final StreamingFuture<? extends V> future : asList()) {
        try {
          final long start = System.currentTimeMillis();
          future.get(timeoutMillis, TimeUnit.MILLISECONDS);
          timeoutMillis -= System.currentTimeMillis() - start;
        } catch (final Exception e) {
          throw UncheckedException.throwUnchecked(e);
        }
      }
      return false;
    }

    @Override
    public Nothing next() {
      throw new NoSuchElementException();
    }

    @Override
    public Nothing next(final long timeout, @NotNull final TimeUnit unit) {
      throw new NoSuchElementException();
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove");
    }
  }

  private class TimeoutIterator implements LiveIterator<Nothing> {

    private long totalTimeoutMillis;

    private TimeoutIterator(final long totalTimeoutMillis) {
      this.totalTimeoutMillis = totalTimeoutMillis;
    }

    @Override
    public boolean hasNext() {
      final long startTime = System.currentTimeMillis();
      try {
        long timeoutMillis = totalTimeoutMillis;
        for (final StreamingFuture<? extends V> future : asList()) {
          try {
            final long start = System.currentTimeMillis();
            future.get(timeoutMillis, TimeUnit.MILLISECONDS);
            timeoutMillis -= System.currentTimeMillis() - start;
          } catch (final Exception e) {
            throw UncheckedException.throwUnchecked(e);
          }
        }
      } finally {
        totalTimeoutMillis -= System.currentTimeMillis() - startTime;
      }
      return false;
    }

    @Override
    public boolean hasNext(final long timeout, @NotNull final TimeUnit unit) {
      final long startTime = System.currentTimeMillis();
      long timeoutMillis = Math.min(unit.toMillis(Require.positive(timeout, "timeout")),
          totalTimeoutMillis);
      try {
        for (final StreamingFuture<? extends V> future : asList()) {
          try {
            final long start = System.currentTimeMillis();
            future.get(timeoutMillis, TimeUnit.MILLISECONDS);
            timeoutMillis -= System.currentTimeMillis() - start;
          } catch (final Exception e) {
            throw UncheckedException.throwUnchecked(e);
          }
        }
      } finally {
        totalTimeoutMillis -= System.currentTimeMillis() - startTime;
      }
      return false;
    }

    @Override
    public Nothing next() {
      throw new NoSuchElementException();
    }

    @Override
    public Nothing next(final long timeout, @NotNull final TimeUnit unit) {
      throw new NoSuchElementException();
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove");
    }
  }
}
