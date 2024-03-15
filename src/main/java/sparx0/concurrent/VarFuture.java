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
package sparx0.concurrent;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.WeakHashMap;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sparx0.concurrent.FutureScope.Registration;
import sparx0.concurrent.FutureScope.Scope;
import sparx0.concurrent.FutureScope.ScopeReceiver;
import sparx0.concurrent.Scheduler.Task;
import sparx0.concurrent.history.Histories;
import sparx0.concurrent.history.SignalHistory;
import sparx0.util.ImmutableList;
import sparx0.util.Require;
import sparx0.util.UncheckedException;
import sparx0.util.function.Action;
import sparx0.util.function.Consumer;
import sparx0.util.function.Function;
import sparx0.util.function.Supplier;
import sparx0.util.logging.Log;
import sparx0.util.logging.alert.Alerts;
import sparx0.util.logging.alert.Alerts.Alert;
import sparx0.util.logging.alert.JoinAlert;

public class VarFuture<V> extends StreamScopeFuture<V, StreamingFuture<V>> implements
    StreamingFuture<V> {

  private static final int CLOSED = 0;
  private static final int RUNNING = 1;
  private static final int CANCELLED = 2;
  private static final int COMPLETING = 3;
  private static final Object UNSET = new Object();

  private static final Alert<Void> joinAlert = Alerts.get(JoinAlert.class);

  private final SignalHistory<V> history;
  private final WeakHashMap<FutureIterator<V>, Void> iterators = new WeakHashMap<FutureIterator<V>, Void>();
  private final HashMap<Receiver<?>, ScopeReceiver<V>> receivers = new HashMap<Receiver<?>, ScopeReceiver<V>>();
  private final Registration registration;
  private final Scheduler scheduler = Scheduler.trampoline();
  private final WeakHashMap<Semaphore, Void> semaphores = new WeakHashMap<Semaphore, Void>();
  private final AtomicInteger status = new AtomicInteger(RUNNING);
  private final String taskID = toString();

  private volatile Exception failureException;
  private Status innerStatus = new RunningStatus();
  private volatile Object lastValue = UNSET;
  private volatile StreamingFuture<V> readonly;
  private volatile Result<V> result;

  VarFuture() {
    this(Histories.<V>noHistory());
  }

  VarFuture(@NotNull final SignalHistory<V> history) {
    this.history = Require.notNull(history, "history");
    this.registration = FutureScope.currentScope().registerFuture(this);
  }

  public static @NotNull <V> VarFuture<V> create() {
    return new VarFuture<V>();
  }

  public static @NotNull <V> VarFuture<V> create(@NotNull final SignalHistory<V> history) {
    return new VarFuture<V>(history);
  }

  private static void logInvocationException(final String name, final String method,
      final Exception e) {
    Log.err(VarFuture.class, "Failed to invoke %s '%s' method: %s", name, method,
        Log.printable(e));
  }

  @Override
  public boolean cancel(final boolean mayInterruptIfRunning) {
    return fail(new FutureCancellationException(mayInterruptIfRunning));
  }

  @Override
  public void clear() {
    scheduler.scheduleAfter(new VarTask() {
      @Override
      public void run() {
        innerStatus.clear();
      }
    });
  }

  @Override
  public void close() {
    if (status.compareAndSet(RUNNING, COMPLETING)) {
      scheduler.scheduleAfter(new VarTask() {
        @Override
        public void run() {
          innerStatus.close();
        }
      });
    } else {
      Log.dbg(VarFuture.class, "Ignoring 'close' operation: future is already closed");
    }
  }

  @Override
  public void compute(@NotNull final Function<? super V, ? extends V> function) {
    Require.notNull(function, "function");
    final Scope scope = FutureScope.currentScope();
    scheduler.scheduleAfter(new VarTask() {
      @Override
      public void run() {
        innerStatus.compute(scope, function);
      }
    });
  }

  @Override
  public boolean fail(@NotNull final Exception error) {
    Require.notNull(error, "error");
    if (status.compareAndSet(RUNNING, COMPLETING)) {
      final Task task = new VarTask() {
        @Override
        public void run() {
          innerStatus.fail(error);
        }
      };
      if (FutureCancellationException.class.equals(error.getClass())) {
        final Scheduler scheduler = this.scheduler;
        if (((FutureCancellationException) error).mayInterruptIfRunning()) {
          scheduler.interruptTask(toString());
        }
        scheduler.scheduleBefore(task);
      } else {
        scheduler.scheduleAfter(task);
      }
      return true;
    }
    Log.dbg(VarFuture.class, "Ignoring 'fail' operation: future is already closed");
    return false;
  }

  @Override
  public List<V> get() throws InterruptedException, ExecutionException {
    final Result<V> result = this.result;
    if (result != null) {
      return result.get();
    }
    final Scheduler scheduler = this.scheduler;
    final GetTask task = new GetTask();
    scheduler.scheduleAfter(task);
    pullFromJoinStart();
    final Alert<Void> joinAlert = VarFuture.joinAlert;
    joinAlert.notify(JoinAlert.WAIT_START, null);
    try {
      task.acquire();
    } finally {
      joinAlert.notify(JoinAlert.WAIT_STOP, null);
      scheduler.scheduleBefore(new RemoveTask(task));
      pullFromJoinStop();
    }
    return this.result.get();
  }

  @Override
  public List<V> get(final long timeout, @NotNull final TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    final Result<V> result = this.result;
    if (result != null) {
      return result.get();
    }
    final Scheduler scheduler = this.scheduler;
    final GetTask task = new GetTask();
    scheduler.scheduleAfter(task);
    pullFromJoinStart();
    final Alert<Void> joinAlert = VarFuture.joinAlert;
    joinAlert.notify(JoinAlert.WAIT_START, null);
    try {
      if (!task.tryAcquire(timeout, unit)) {
        throw new TimeoutException();
      }
    } finally {
      joinAlert.notify(JoinAlert.WAIT_STOP, null);
      scheduler.scheduleBefore(new RemoveTask(task));
      pullFromJoinStop();
    }
    return this.result.get();
  }

  @Override
  public V getCurrent() {
    @SuppressWarnings("unchecked") final V value = (V) this.lastValue;
    if (value == UNSET) {
      throw new NoSuchElementException();
    }
    return value;
  }

  @Override
  public V getCurrentOr(final V defaultValue) {
    @SuppressWarnings("unchecked") final V value = (V) this.lastValue;
    if (value == UNSET) {
      return defaultValue;
    }
    return value;
  }

  @Override
  public boolean isCancelled() {
    return status.get() == CANCELLED
        && FutureCancellationException.class.equals(failureException.getClass());
  }

  @Override
  public boolean isDone() {
    final int status = this.status.get();
    return status != RUNNING && status != COMPLETING;
  }

  @Override
  public boolean isReadOnly() {
    return false;
  }

  @Override
  public @NotNull LiveIterator<V> iterator() {
    final IndefiniteIterator<V> iterator = new IndefiniteIterator<V>();
    scheduler.scheduleBefore(new IteratorTask(iterator));
    return iterator;
  }

  @Override
  public @NotNull LiveIterator<V> iterator(final long timeout, @NotNull final TimeUnit unit) {
    final TimeoutIterator<V> iterator = new TimeoutIterator<V>(
        unit.toMillis(Require.positive(timeout, "timeout")));
    scheduler.scheduleBefore(new IteratorTask(iterator));
    return iterator;
  }

  @Override
  public @NotNull LiveIterator<V> iteratorNext() {
    final IndefiniteIterator<V> iterator = new IndefiniteIterator<V>();
    scheduler.scheduleBefore(new IteratorNextTask(iterator));
    return iterator;
  }

  @Override
  public @NotNull LiveIterator<V> iteratorNext(final long timeout, @NotNull final TimeUnit unit) {
    final TimeoutIterator<V> iterator = new TimeoutIterator<V>(
        unit.toMillis(Require.positive(timeout, "timeout")));
    scheduler.scheduleBefore(new IteratorNextTask(iterator));
    return iterator;
  }

  @Override
  public @NotNull StreamingFuture<V> readOnly() {
    if (readonly == null) {
      readonly = new ReadOnlyFuture<V>(this);
    }
    return readonly;
  }

  @Override
  public void set(final V value) {
    scheduler.scheduleAfter(new VarTask() {
      @Override
      public void run() {
        innerStatus.set(value);
      }
    });
  }

  @Override
  public void setBulk(@NotNull final Collection<V> values) {
    if (!values.isEmpty()) {
      final List<V> valueList = ImmutableList.ofElementsIn(values);
      scheduler.scheduleAfter(new VarTask() {
        @Override
        public int weight() {
          return valueList.size();
        }

        @Override
        public void run() {
          innerStatus.setBulk(valueList);
        }
      });
    }
  }

  @Override
  public void setBulk(@Nullable final V... values) {
    if (values != null && values.length > 0) {
      final List<V> valueList = ImmutableList.of(values);
      scheduler.scheduleAfter(new VarTask() {
        @Override
        public int weight() {
          return valueList.size();
        }

        @Override
        public void run() {
          innerStatus.setBulk(valueList);
        }
      });
    }
  }

  @Override
  public @NotNull Subscription subscribe(@NotNull final Receiver<? super V> receiver) {
    final ScopeReceiver<? super V> scopeReceiver = FutureScope.currentScope()
        .decorateReceiver(this, scheduler, Require.notNull(receiver, "receiver"));
    scheduler.scheduleBefore(new VarTask() {
      @Override
      @SuppressWarnings("unchecked")
      public void run() {
        innerStatus.subscribe((Receiver<V>) receiver, (ScopeReceiver<V>) scopeReceiver);
      }
    });
    return new VarSubscription(receiver);
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
    final ScopeReceiver<? super V> scopeReceiver = FutureScope.currentScope()
        .decorateReceiver(this, scheduler, Require.notNull(receiver, "receiver"));
    scheduler.scheduleBefore(new VarTask() {
      @Override
      @SuppressWarnings("unchecked")
      public void run() {
        innerStatus.subscribeNext((Receiver<V>) receiver, (ScopeReceiver<V>) scopeReceiver);
      }
    });
    return new VarSubscription(receiver);
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
    scheduler.scheduleBefore(new VarTask() {
      @Override
      public void run() {
        final ScopeReceiver<V> scopeReceiver = receivers.remove(receiver);
        if (scopeReceiver != null) {
          scopeReceiver.onUnsubscribe();
        }
      }
    });
  }

  @Override
  protected @NotNull StreamingFuture<V> createPaused() {
    return pauseFuture(this);
  }

  protected boolean hasConsumers() {
    for (final ScopeReceiver<V> scopeReceiver : receivers.values()) {
      if (scopeReceiver.isConsumer()) {
        return true;
      }
    }
    return false;
  }

  protected void pullFromIterator() {
  }

  protected void pullFromJoinStart() {
  }

  protected void pullFromJoinStop() {
  }

  protected void pullFromReceiver() {
  }

  @Override
  protected void resumePaused(@NotNull final StreamingFuture<V> pausedFuture) {
    resumeFuture(pausedFuture);
  }

  protected @NotNull Scheduler scheduler() {
    return scheduler;
  }

  protected @NotNull String taskID() {
    return taskID;
  }

  private interface Result<V> extends Supplier<List<V>> {

    @Override
    List<V> get() throws ExecutionException;
  }

// Iterators

  private static abstract class FutureIterator<E> implements LiveIterator<E> {

    protected static final int IDLE = 0;
    protected static final int RUNNING = 1;
    protected static final int FAILED = 2;

    protected final ConcurrentLinkedQueue<ArrayDeque<E>> queue = new ConcurrentLinkedQueue<ArrayDeque<E>>();
    protected final Semaphore semaphore = new Semaphore(0);
    protected final AtomicInteger status = new AtomicInteger(RUNNING);

    protected volatile Exception failureException;

    @Override
    public E next(final long timeout, @NotNull final TimeUnit unit) {
      if (hasNext(timeout, unit)) {
        final ArrayDeque<E> elements = queue.peek();
        @SuppressWarnings("DataFlowIssue") final E element = elements.poll();
        if (elements.isEmpty()) {
          queue.remove();
        }
        return element;
      }
      throw new NoSuchElementException();
    }

    @Override
    public E next() {
      if (hasNext()) {
        final ArrayDeque<E> elements = queue.peek();
        @SuppressWarnings("DataFlowIssue") final E element = elements.poll();
        if (elements.isEmpty()) {
          queue.remove();
        }
        return element;
      }
      throw new NoSuchElementException();
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove");
    }

    private void add(@NotNull final E element) {
      final ArrayDeque<E> array = new ArrayDeque<E>(1);
      array.add(element);
      queue.add(array);
      release();
    }

    private void addAll(@NotNull final Collection<E> elements) {
      queue.add(new ArrayDeque<E>(elements));
      release();
    }

    private void end() {
      status.set(IDLE);
      release();
    }

    private void fail(@NotNull final Exception error) {
      this.failureException = error;
      status.set(FAILED);
      release();
    }

    private void release() {
      final Semaphore semaphore = this.semaphore;
      semaphore.drainPermits();
      semaphore.release();
    }
  }

  private class IndefiniteIterator<E> extends FutureIterator<E> {

    @Override
    public boolean hasNext() {
      final ConcurrentLinkedQueue<ArrayDeque<E>> queue = this.queue;
      final Semaphore semaphore = this.semaphore;
      final AtomicInteger iteratorStatus = status;
      final Alert<Void> joinAlert = VarFuture.joinAlert;
      boolean firstLoop = true;
      while (true) {
        if (!queue.isEmpty()) {
          return true;
        }
        final int status = iteratorStatus.get();
        if (status == IDLE) {
          return false;
        }
        if (status == FAILED) {
          throw UncheckedException.toUnchecked(failureException);
        }
        if (firstLoop) {
          firstLoop = false;
          pullFromIterator();
        }
        joinAlert.notify(JoinAlert.WAIT_START, null);
        try {
          semaphore.acquire();
        } catch (final InterruptedException e) {
          throw UncheckedException.toUnchecked(e);
        } finally {
          joinAlert.notify(JoinAlert.WAIT_STOP, null);
        }
      }
    }

    @Override
    public boolean hasNext(final long timeout, @NotNull final TimeUnit unit) {
      final long startTime = System.currentTimeMillis();
      long remainingTime = unit.toMillis(Require.positive(timeout, "timeout"));
      final ConcurrentLinkedQueue<ArrayDeque<E>> queue = this.queue;
      final Semaphore semaphore = this.semaphore;
      final AtomicInteger iteratorStatus = status;
      final Alert<Void> joinAlert = VarFuture.joinAlert;
      boolean firstLoop = true;
      while (remainingTime > 0) {
        if (!queue.isEmpty()) {
          return true;
        }
        final int status = iteratorStatus.get();
        if (status == IDLE) {
          return false;
        }
        if (status == FAILED) {
          throw UncheckedException.toUnchecked(failureException);
        }
        if (firstLoop) {
          firstLoop = false;
          pullFromIterator();
        }
        joinAlert.notify(JoinAlert.WAIT_START, null);
        try {
          if (!semaphore.tryAcquire(remainingTime, TimeUnit.MILLISECONDS)) {
            throw UncheckedException.timeout();
          }
        } catch (final InterruptedException e) {
          throw UncheckedException.toUnchecked(e);
        } finally {
          joinAlert.notify(JoinAlert.WAIT_STOP, null);
        }
        remainingTime -= System.currentTimeMillis() - startTime;
      }
      throw UncheckedException.timeout();
    }
  }

  private class TimeoutIterator<E> extends FutureIterator<E> {

    private long totalTimeoutMillis;

    private TimeoutIterator(final long totalTimeoutMillis) {
      this.totalTimeoutMillis = totalTimeoutMillis;
    }

    @Override
    public boolean hasNext() {
      final long startTime = System.currentTimeMillis();
      long remainingTime = totalTimeoutMillis;
      try {
        final ConcurrentLinkedQueue<ArrayDeque<E>> queue = this.queue;
        final Semaphore semaphore = this.semaphore;
        final AtomicInteger iteratorStatus = status;
        final Alert<Void> joinAlert = VarFuture.joinAlert;
        boolean firstLoop = true;
        while (remainingTime > 0) {
          if (!queue.isEmpty()) {
            return true;
          }
          final int status = iteratorStatus.get();
          if (status == IDLE) {
            return false;
          }
          if (status == FAILED) {
            throw UncheckedException.toUnchecked(failureException);
          }
          if (firstLoop) {
            firstLoop = false;
            pullFromIterator();
          }
          joinAlert.notify(JoinAlert.WAIT_START, null);
          try {
            if (!semaphore.tryAcquire(remainingTime, TimeUnit.MILLISECONDS)) {
              throw UncheckedException.timeout();
            }
          } catch (final InterruptedException e) {
            throw UncheckedException.toUnchecked(e);
          } finally {
            joinAlert.notify(JoinAlert.WAIT_STOP, null);
          }
          remainingTime -= System.currentTimeMillis() - startTime;
        }
        throw UncheckedException.timeout();
      } finally {
        totalTimeoutMillis -= System.currentTimeMillis() - startTime;
      }
    }

    @Override
    public boolean hasNext(final long timeout, @NotNull final TimeUnit unit) {
      final long startTime = System.currentTimeMillis();
      long remainingTime = Math.min(totalTimeoutMillis,
          unit.toMillis(Require.positive(timeout, "timeout")));
      try {
        final ConcurrentLinkedQueue<ArrayDeque<E>> queue = this.queue;
        final Semaphore semaphore = this.semaphore;
        final AtomicInteger iteratorStatus = status;
        final Alert<Void> joinAlert = VarFuture.joinAlert;
        boolean firstLoop = true;
        while (remainingTime > 0) {
          if (!queue.isEmpty()) {
            return true;
          }
          final int status = iteratorStatus.get();
          if (status == IDLE) {
            return false;
          }
          if (status == FAILED) {
            throw UncheckedException.toUnchecked(failureException);
          }
          if (firstLoop) {
            firstLoop = false;
            pullFromIterator();
          }
          joinAlert.notify(JoinAlert.WAIT_START, null);
          try {
            if (!semaphore.tryAcquire(remainingTime, TimeUnit.MILLISECONDS)) {
              throw UncheckedException.timeout();
            }
          } catch (final InterruptedException e) {
            throw UncheckedException.toUnchecked(e);
          } finally {
            joinAlert.notify(JoinAlert.WAIT_STOP, null);
          }
          remainingTime -= System.currentTimeMillis() - startTime;
        }
        throw UncheckedException.timeout();
      } finally {
        totalTimeoutMillis -= System.currentTimeMillis() - startTime;
      }
    }
  }

// Results

  private static class CancellationResult<V> implements Result<V> {

    private final CancellationException result;

    private CancellationResult() {
      result = new CancellationException();
    }

    @Override
    public List<V> get() {
      throw result;
    }
  }

  private static class FailureResult<V> implements Result<V> {

    private final ExecutionException result;

    private FailureResult(@NotNull final Exception error) {
      result = new ExecutionException(error);
    }

    @Override
    public List<V> get() throws ExecutionException {
      throw result;
    }
  }

  private static class ValueResult<V> implements Result<V> {

    private final List<V> result;

    private ValueResult(final V value) {
      if (value != UNSET) {
        result = ImmutableList.of(value);
      } else {
        result = ImmutableList.of();
      }
    }

    @Override
    public List<V> get() {
      return result;
    }
  }

// Subscriptions

  private class VarSubscription implements Subscription {

    private final Receiver<? super V> receiver;

    private VarSubscription(@NotNull final Receiver<? super V> receiver) {
      this.receiver = receiver;
    }

    @Override
    public void cancel() {
      VarFuture.this.unsubscribe(receiver);
    }
  }

// Status

  private class CancelledStatus extends Status {

    @Override
    void get(@NotNull final Semaphore semaphore) {
      semaphore.release();
    }

    @Override
    void iterator(@NotNull final FutureIterator<V> iterator) {
      super.iterator(iterator);
      iterator.fail(failureException);
    }

    @Override
    void iteratorNext(@NotNull final FutureIterator<V> iterator) {
      iterator.fail(failureException);
    }

    @Override
    void subscribe(@NotNull final Receiver<V> receiver,
        @NotNull final ScopeReceiver<V> scopeReceiver) {
      super.subscribe(receiver, scopeReceiver);
      subscribeNext(receiver, scopeReceiver);
    }

    @Override
    void subscribeNext(@NotNull final Receiver<V> receiver,
        @NotNull final ScopeReceiver<V> scopeReceiver) {
      try {
        scopeReceiver.fail(failureException);
      } catch (final RuntimeException e) {
        scopeReceiver.onReceiverError(e);
      }
      scopeReceiver.onUnsubscribe();
    }
  }

  private class ClosedStatus extends Status {

    @Override
    void get(@NotNull final Semaphore semaphore) {
      semaphore.release();
    }

    @Override
    @SuppressWarnings("unchecked")
    void iterator(@NotNull final FutureIterator<V> iterator) {
      super.iterator(iterator);
      if (lastValue != UNSET) {
        iterator.add((V) lastValue);
      }
      iterator.end();
    }

    @Override
    void iteratorNext(@NotNull final FutureIterator<V> iterator) {
      iterator.end();
    }

    @Override
    @SuppressWarnings("unchecked")
    void subscribe(@NotNull final Receiver<V> receiver,
        @NotNull final ScopeReceiver<V> scopeReceiver) {
      super.subscribe(receiver, scopeReceiver);
      try {
        if (scopeReceiver.isConsumer() && lastValue != UNSET) {
          scopeReceiver.set((V) lastValue);
        }
        scopeReceiver.close();
      } catch (final RuntimeException e) {
        scopeReceiver.onReceiverError(e);
      }
      scopeReceiver.onUnsubscribe();
    }

    @Override
    void subscribeNext(@NotNull final Receiver<V> receiver,
        @NotNull final ScopeReceiver<V> scopeReceiver) {
      try {
        scopeReceiver.close();
      } catch (final RuntimeException e) {
        scopeReceiver.onReceiverError(e);
      }
      scopeReceiver.onUnsubscribe();
    }
  }

  private class RunningStatus extends Status {

    @Override
    @SuppressWarnings("unchecked")
    public void close() {
      status.set(CLOSED);
      innerStatus = new ClosedStatus();
      result = new ValueResult<V>((V) lastValue);
      try {
        history.onClose();
      } catch (final RuntimeException e) {
        logInvocationException("history", "onClose", e);
      }
      final HashMap<Receiver<?>, ScopeReceiver<V>> receivers = VarFuture.this.receivers;
      for (final ScopeReceiver<V> scopeReceiver : receivers.values()) {
        try {
          scopeReceiver.close();
        } catch (final RuntimeException e) {
          scopeReceiver.onReceiverError(e);
        }
        scopeReceiver.onUnsubscribe();
      }
      receivers.clear();
      final WeakHashMap<FutureIterator<V>, Void> iterators = VarFuture.this.iterators;
      for (final FutureIterator<V> futureIterator : iterators.keySet()) {
        futureIterator.end();
      }
      iterators.clear();
      final WeakHashMap<Semaphore, Void> semaphores = VarFuture.this.semaphores;
      for (final Semaphore semaphore : semaphores.keySet()) {
        semaphore.release();
      }
      semaphores.clear();
      registration.cancel();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean fail(@NotNull final Exception error) {
      status.set(CANCELLED);
      innerStatus = new CancelledStatus();
      if (lastValue != UNSET) {
        try {
          history.onPush((V) lastValue);
        } catch (final RuntimeException e) {
          logInvocationException("history", "onSet", e);
        }
      }
      lastValue = UNSET;
      failureException = error;
      if (FutureCancellationException.class.equals(error.getClass())) {
        result = new CancellationResult<V>();
      } else {
        result = new FailureResult<V>(error);
      }
      final HashMap<Receiver<?>, ScopeReceiver<V>> receivers = VarFuture.this.receivers;
      final WeakHashMap<FutureIterator<V>, Void> iterators = VarFuture.this.iterators;
      final WeakHashMap<Semaphore, Void> semaphores = VarFuture.this.semaphores;
      for (final ScopeReceiver<V> scopeReceiver : receivers.values()) {
        try {
          scopeReceiver.fail(error);
        } catch (final RuntimeException e) {
          scopeReceiver.onReceiverError(e);
        }
        scopeReceiver.onUnsubscribe();
      }
      receivers.clear();
      for (final FutureIterator<V> futureIterator : iterators.keySet()) {
        futureIterator.fail(error);
      }
      iterators.clear();
      for (final Semaphore semaphore : semaphores.keySet()) {
        semaphore.release();
      }
      semaphores.clear();
      if (hasConsumers()) {
        registration.cancel();
      } else {
        registration.onUncaughtError(error);
      }
      return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void set(final V value) {
      if (lastValue != UNSET) {
        try {
          history.onPush((V) lastValue);
        } catch (final RuntimeException e) {
          logInvocationException("history", "onSet", e);
        }
      }
      lastValue = value;
      boolean firstSink = true;
      for (final Entry<Receiver<?>, ScopeReceiver<V>> entry : receivers.entrySet()) {
        final ScopeReceiver<V> scopeReceiver = entry.getValue();
        if (scopeReceiver.isConsumer()) {
          try {
            scopeReceiver.set(value);
          } catch (final RuntimeException e) {
            scopeReceiver.onReceiverError(e);
          }
          if (firstSink) {
            firstSink = false;
            pullFromReceiver();
          }
        }
      }
      for (final FutureIterator<V> futureIterator : iterators.keySet()) {
        futureIterator.add(value);
      }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setBulk(@NotNull final Collection<V> values) {
      if (lastValue != UNSET) {
        try {
          history.onPush((V) lastValue);
        } catch (final RuntimeException e) {
          logInvocationException("history", "onSet", e);
        }
      }
      final int lastIndex = values.size() - 1;
      final List<V> valuesList = (List<V>) values;
      lastValue = valuesList.get(lastIndex);
      try {
        history.onPushBulk(valuesList.subList(0, lastIndex));
      } catch (final RuntimeException e) {
        logInvocationException("history", "onSetBulk", e);
      }
      boolean firstSink = true;
      for (final Entry<Receiver<?>, ScopeReceiver<V>> entry : receivers.entrySet()) {
        final ScopeReceiver<V> scopeReceiver = entry.getValue();
        if (scopeReceiver.isConsumer()) {
          try {
            if (values.size() == 1) {
              scopeReceiver.set(valuesList.get(0));
            } else {
              scopeReceiver.setBulk(values);
            }
          } catch (final RuntimeException e) {
            scopeReceiver.onReceiverError(e);
          }
          if (firstSink) {
            firstSink = false;
            pullFromReceiver();
          }
        }
      }
      for (final FutureIterator<V> futureIterator : iterators.keySet()) {
        futureIterator.addAll(values);
      }
    }

    @Override
    void clear() {
      lastValue = UNSET;
      try {
        history.onClear();
      } catch (final RuntimeException e) {
        logInvocationException("history", "onClear", e);
      }
    }

    @Override
    void compute(@NotNull final Scope scope,
        @NotNull final Function<? super V, ? extends V> function) {
      if (lastValue != UNSET) {
        scheduler.pause();
        try {
          scope.runTask(new VarTask() {
            @Override
            @SuppressWarnings({"unchecked", "NonAtomicOperationOnVolatileField"})
            public void run() {
              try {
                lastValue = function.apply((V) lastValue);
              } catch (final Exception e) {
                Log.err(VarFuture.class, "Failed to compute next value: %s", Log.printable(e));
                innerStatus.fail(e);
              }
              scheduler.resume();
            }
          });
        } catch (final RuntimeException e) {
          logInvocationException("context", "onTask", e);
          innerStatus.fail(e);
          scheduler.resume();
        }
      }
    }

    @Override
    void get(@NotNull final Semaphore semaphore) {
      semaphores.put(semaphore, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    void iterator(@NotNull final FutureIterator<V> iterator) {
      super.iterator(iterator);
      if (lastValue != UNSET) {
        iterator.add((V) lastValue);
      }
      iterators.put(iterator, null);
    }

    @Override
    void iteratorNext(@NotNull final FutureIterator<V> iterator) {
      iterators.put(iterator, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    void subscribe(@NotNull final Receiver<V> receiver,
        @NotNull final ScopeReceiver<V> scopeReceiver) {
      final HashMap<Receiver<?>, ScopeReceiver<V>> receivers = VarFuture.this.receivers;
      if (!receivers.containsKey(receiver)) {
        super.subscribe(receiver, scopeReceiver);
        if (scopeReceiver.isConsumer() && lastValue != UNSET) {
          try {
            scopeReceiver.set((V) lastValue);
          } catch (final RuntimeException e) {
            scopeReceiver.onReceiverError(e);
          }
        }
        receivers.put(receiver, scopeReceiver);
        if (hasConsumers()) {
          pullFromReceiver();
        }
      }
    }

    @Override
    void subscribeNext(@NotNull final Receiver<V> receiver,
        @NotNull final ScopeReceiver<V> scopeReceiver) {
      final HashMap<Receiver<?>, ScopeReceiver<V>> receivers = VarFuture.this.receivers;
      if (!receivers.containsKey(receiver)) {
        receivers.put(receiver, scopeReceiver);
        if (hasConsumers()) {
          pullFromReceiver();
        }
      }
    }

    @Override
    void remove(@NotNull final Semaphore semaphore) {
      semaphores.remove(semaphore);
    }
  }

  private abstract class Status implements Receiver<V> {

    @Override
    public void close() {
      Log.dbg(VarFuture.class, "Ignoring 'close' operation: future is already closed");
    }

    @Override
    public boolean fail(@NotNull final Exception error) {
      Log.dbg(VarFuture.class, "Ignoring 'fail' operation: future is already closed");
      return false;
    }

    @Override
    public void set(final V value) {
      Log.dbg(VarFuture.class, "Ignoring 'set' operation: future is already closed");
    }

    @Override
    public void setBulk(@NotNull final Collection<V> values) {
      Log.dbg(VarFuture.class, "Ignoring 'setBulk' operation: future is already closed");
    }

    void clear() {
      Log.dbg(VarFuture.class, "Ignoring 'clear' operation: future is already closed");
    }

    void compute(@NotNull final Scope scope,
        @NotNull final Function<? super V, ? extends V> function) {
      Log.dbg(VarFuture.class, "Ignoring 'compute' operation: future is already closed");
    }

    void get(@NotNull final Semaphore semaphore) {
    }

    void iterator(@NotNull final FutureIterator<V> iterator) {
      try {
        final List<V> values = history.onSubscribe();
        if (!values.isEmpty()) {
          iterator.addAll(values);
        }
      } catch (final RuntimeException e) {
        logInvocationException("history", "onSubscribe", e);
      }
    }

    void iteratorNext(@NotNull final FutureIterator<V> iterator) {
    }

    void subscribe(@NotNull final Receiver<V> receiver,
        @NotNull final ScopeReceiver<V> scopeReceiver) {
      if (scopeReceiver.isConsumer()) {
        try {
          final List<V> values = history.onSubscribe();
          if (!values.isEmpty()) {
            try {
              if (values.size() == 1) {
                scopeReceiver.set(values.get(0));
              } else {
                scopeReceiver.setBulk(ImmutableList.ofElementsIn(values));
              }
            } catch (final RuntimeException e) {
              scopeReceiver.onReceiverError(e);
            }
          }
        } catch (final RuntimeException e) {
          logInvocationException("history", "onSubscribe", e);
        }
      }
    }

    void subscribeNext(@NotNull final Receiver<V> receiver,
        @NotNull final ScopeReceiver<V> scopeReceiver) {
    }

    void remove(@NotNull final Semaphore semaphore) {
    }
  }

// Tasks

  private class GetTask extends Semaphore implements Task {

    private GetTask() {
      super(0);
    }

    @Override
    public void run() {
      innerStatus.get(this);
    }

    @Override
    public @NotNull String taskID() {
      return VarFuture.this.taskID();
    }

    @Override
    public int weight() {
      return 1;
    }
  }

  private class IteratorTask extends VarTask {

    private final FutureIterator<V> iterator;

    private IteratorTask(@NotNull final FutureIterator<V> iterator) {
      this.iterator = iterator;
    }

    @Override
    public void run() {
      innerStatus.iterator(iterator);
    }
  }

  private class IteratorNextTask extends VarTask {

    private final FutureIterator<V> iterator;

    private IteratorNextTask(@NotNull final FutureIterator<V> iterator) {
      this.iterator = iterator;
    }

    @Override
    public void run() {
      innerStatus.iteratorNext(iterator);
    }
  }

  private class RemoveTask extends VarTask {

    private final Semaphore semaphore;

    private RemoveTask(@NotNull final Semaphore semaphore) {
      this.semaphore = semaphore;
    }

    @Override
    public void run() {
      innerStatus.remove(semaphore);
    }
  }

  private abstract class VarTask implements Task {

    @Override
    public @NotNull String taskID() {
      return VarFuture.this.taskID();
    }

    @Override
    public int weight() {
      return 1;
    }
  }
}