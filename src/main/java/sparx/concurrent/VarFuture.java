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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
import sparx.concurrent.FutureGroup.Group;
import sparx.concurrent.FutureGroup.GroupReceiver;
import sparx.concurrent.FutureGroup.Registration;
import sparx.concurrent.Scheduler.Task;
import sparx.function.Action;
import sparx.function.Consumer;
import sparx.function.Function;
import sparx.function.Supplier;
import sparx.logging.Log;
import sparx.util.LiveIterator;
import sparx.util.Requires;

public class VarFuture<V> extends StreamGroupFuture<V, StreamingFuture<V>> implements
    StreamingFuture<V> {

  private static final int CLOSED = 0;
  private static final int RUNNING = 1;
  private static final int CANCELLED = 2;
  private static final int COMPLETING = 3;
  private static final NoHistoryStrategy<?> NO_HISTORY = new NoHistoryStrategy<Object>();
  private static final Object UNSET = new Object();

  private final HistoryStrategy<V> historyStrategy;
  private final WeakHashMap<FutureIterator<V>, Void> iterators = new WeakHashMap<FutureIterator<V>, Void>();
  private final HashMap<Receiver<?>, GroupReceiver<V>> receivers = new HashMap<Receiver<?>, GroupReceiver<V>>();
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

  public static @NotNull <V> VarFuture<V> create() {
    return new VarFuture<V>();
  }

  public static @NotNull <V> VarFuture<V> create(
      @NotNull final HistoryStrategy<V> historyStrategy) {
    return new VarFuture<V>(historyStrategy);
  }

  @SuppressWarnings("unchecked")
  VarFuture() {
    this((HistoryStrategy<V>) NO_HISTORY);
  }

  VarFuture(@NotNull final HistoryStrategy<V> historyStrategy) {
    this.historyStrategy = Requires.notNull(historyStrategy, "historyStrategy");
    this.registration = FutureGroup.currentGroup().onCreate(this);
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
  public void compute(@NotNull final Function<? super V, ? extends V> function) {
    Requires.notNull(function, "function");
    final Group group = FutureGroup.currentGroup();
    scheduler.scheduleAfter(new VarTask() {
      @Override
      public void run() {
        innerStatus.compute(group, function);
      }
    });
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
  public boolean isReadOnly() {
    return false;
  }

  @Override
  public @NotNull StreamingFuture<V> readOnly() {
    if (readonly == null) {
      readonly = new ReadOnlyFuture<V>(this);
    }
    return readonly;
  }

  @Override
  public void setBulk(@NotNull final V... values) {
    if (values.length > 0 && !isDone()) {
      final List<V> valueList = Arrays.asList(values);
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
    final GroupReceiver<? super V> groupReceiver = FutureGroup.currentGroup()
        .onSubscribe(this, scheduler, Requires.notNull(receiver, "receiver"));
    // TODO: alert => serializable?
    scheduler.scheduleAfter(new VarTask() {
      @Override
      @SuppressWarnings("unchecked")
      public void run() {
        innerStatus.subscribe((Receiver<V>) receiver, (GroupReceiver<V>) groupReceiver);
      }
    });
    return new VarSubscription(receiver);
  }

  @Override
  public @NotNull Subscription subscribe(@Nullable final Consumer<? super V> onValueConsumer,
      @Nullable final Consumer<? super Collection<V>> onValuesConsumer,
      @Nullable final Consumer<Exception> onErrorConsumer, @Nullable final Action onCloseAction) {
    // TODO: alert => serializable?
    return subscribe(new FunctionalReceiver<V>(onValueConsumer, onValuesConsumer, onErrorConsumer,
        onCloseAction));
  }

  @Override
  public void unsubscribe(@NotNull final Receiver<?> receiver) {
    scheduler.scheduleBefore(new VarTask() {
      @Override
      public void run() {
        final GroupReceiver<V> groupReceiver = receivers.remove(receiver);
        if (groupReceiver != null) {
          groupReceiver.onUnsubscribe();
        }
      }
    });
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
        unit.toMillis(Requires.positive(timeout, "timeout")));
    scheduler.scheduleBefore(new IteratorTask(iterator));
    return iterator;
  }

  @Override
  public boolean cancel(final boolean mayInterruptIfRunning) {
    return fail(new FutureCancellationException(mayInterruptIfRunning));
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
  public List<V> get() throws InterruptedException, ExecutionException {
    final Result<V> result = this.result;
    if (result != null) {
      return result.get();
    }
    final GetTask task = new GetTask();
    scheduler.scheduleAfter(task);
    task.acquire();
    // TODO: alert => takes too long?
    return this.result.get();
  }

  @Override
  public List<V> get(final long timeout, @NotNull final TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    final Result<V> result = this.result;
    if (result != null) {
      return result.get();
    }
    final GetTask task = new GetTask();
    scheduler.scheduleAfter(task);
    // TODO: alert => timeout too long?
    if (!task.tryAcquire(timeout, unit)) {
      throw new TimeoutException();
    }
    return this.result.get();
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
    }
  }

  @Override
  public boolean fail(@NotNull final Exception error) {
    Requires.notNull(error, "error");
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
    return false;
  }

  @Override
  public void set(final V value) {
    if (!isDone()) {
      scheduler.scheduleAfter(new VarTask() {
        @Override
        public void run() {
          innerStatus.set(value);
        }
      });
    }
  }

  @Override
  public void setBulk(@NotNull final Collection<V> values) {
    if (!values.isEmpty() && !isDone()) {
      final List<V> valueList = Collections.unmodifiableList(new ArrayList<V>(values));
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

  public interface HistoryStrategy<V> {

    void onClear();

    void onClose();

    void onSet(V value);

    void onSetBulk(@NotNull List<V> values);

    @NotNull List<V> onSubscribe();
  }

  private interface Result<V> extends Supplier<List<V>> {

    @Override
    List<V> get() throws ExecutionException;
  }

// History

  private static class NoHistoryStrategy<V> implements HistoryStrategy<V> {

    @Override
    public void onClear() {
    }

    @Override
    public void onClose() {
    }

    @Override
    public void onSet(final V value) {
    }

    @Override
    public void onSetBulk(@NotNull final List<V> values) {
    }

    @Override
    public @NotNull List<V> onSubscribe() {
      return Collections.emptyList();
    }
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
      semaphore.release();
    }

    private void addAll(@NotNull final Collection<E> elements) {
      queue.add(new ArrayDeque<E>(elements));
      semaphore.release();
    }

    private void end() {
      status.set(IDLE);
      semaphore.release();
    }

    private void fail(@NotNull final Exception error) {
      this.failureException = error;
      status.set(FAILED);
      semaphore.release();
    }
  }

  private static class IndefiniteIterator<E> extends FutureIterator<E> {

    @Override
    public boolean hasNext(final long timeout, @NotNull final TimeUnit unit) {
      final long startTime = System.currentTimeMillis();
      long remainingTime = unit.toMillis(Requires.positive(timeout, "timeout"));
      final ConcurrentLinkedQueue<ArrayDeque<E>> queue = this.queue;
      final Semaphore semaphore = this.semaphore;
      final AtomicInteger iteratorStatus = status;
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
        try {
          // TODO: alert => timeout too long?
          if (!semaphore.tryAcquire(remainingTime, TimeUnit.MILLISECONDS)) {
            throw new UncheckedTimeoutException();
          }
        } catch (final InterruptedException e) {
          throw new UncheckedInterruptedException(e);
        }
        remainingTime -= System.currentTimeMillis() - startTime;
      }
      throw new UncheckedTimeoutException();
    }

    @Override
    public boolean hasNext() {
      final ConcurrentLinkedQueue<ArrayDeque<E>> queue = this.queue;
      final Semaphore semaphore = this.semaphore;
      final AtomicInteger iteratorStatus = status;
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
        try {
          semaphore.acquire();
          // TODO: alert => takes too long?
        } catch (final InterruptedException e) {
          throw new UncheckedInterruptedException(e);
        }
      }
    }
  }

  private static class TimeoutIterator<E> extends FutureIterator<E> {

    private long totalTimeoutMillis;

    private TimeoutIterator(final long totalTimeoutMillis) {
      this.totalTimeoutMillis = totalTimeoutMillis;
    }

    @Override
    public boolean hasNext(final long timeout, @NotNull final TimeUnit unit) {
      final long startTime = System.currentTimeMillis();
      long remainingTime = Math.min(totalTimeoutMillis,
          unit.toMillis(Requires.positive(timeout, "timeout")));
      try {
        final ConcurrentLinkedQueue<ArrayDeque<E>> queue = this.queue;
        final Semaphore semaphore = this.semaphore;
        final AtomicInteger iteratorStatus = status;
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
          try {
            // TODO: alert => timeout too long?
            if (!semaphore.tryAcquire(remainingTime, TimeUnit.MILLISECONDS)) {
              throw new UncheckedTimeoutException();
            }
          } catch (final InterruptedException e) {
            throw new UncheckedInterruptedException(e);
          }
          remainingTime -= System.currentTimeMillis() - startTime;
        }
        throw new UncheckedTimeoutException();
      } finally {
        totalTimeoutMillis -= System.currentTimeMillis() - startTime;
      }
    }

    @Override
    public boolean hasNext() {
      final long startTime = System.currentTimeMillis();
      long remainingTime = totalTimeoutMillis;
      try {
        final ConcurrentLinkedQueue<ArrayDeque<E>> queue = this.queue;
        final Semaphore semaphore = this.semaphore;
        final AtomicInteger iteratorStatus = status;
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
          try {
            // TODO: alert => timeout too long?
            if (!semaphore.tryAcquire(remainingTime, TimeUnit.MILLISECONDS)) {
              throw new UncheckedTimeoutException();
            }
          } catch (final InterruptedException e) {
            throw new UncheckedInterruptedException(e);
          }
          remainingTime -= System.currentTimeMillis() - startTime;
        }
        throw new UncheckedTimeoutException();
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

    private FailureResult(final Exception error) {
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
        result = Collections.singletonList(value);
      } else {
        result = Collections.emptyList();
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

  private abstract class Status implements Receiver<V> {

    @Override
    public boolean fail(@NotNull final Exception error) {
      return false;
    }

    @Override
    public void setBulk(@NotNull final Collection<V> values) {
    }

    @Override
    public void set(final V value) {
    }

    @Override
    public void close() {
    }

    void clear() {
    }

    void compute(@NotNull final Group group,
        @NotNull final Function<? super V, ? extends V> function) {
    }

    void get(@NotNull final Semaphore semaphore) {
    }

    void iterator(@NotNull final FutureIterator<V> iterator) {
      try {
        final List<V> values = historyStrategy.onSubscribe();
        if (!values.isEmpty()) {
          iterator.addAll(values);
        }
      } catch (final RuntimeException e) {
        Log.errInvocation(VarFuture.class, "history strategy", "onSubscribe", e);
      }
    }

    void subscribe(@NotNull final Receiver<V> receiver,
        @NotNull final GroupReceiver<V> groupReceiver) {
      try {
        final List<V> values = historyStrategy.onSubscribe();
        if (!values.isEmpty()) {
          try {
            if (values.size() == 1) {
              groupReceiver.set(values.get(0));
            } else {
              groupReceiver.setBulk(Collections.unmodifiableList(new ArrayList<V>(values)));
            }
          } catch (final RuntimeException e) {
            groupReceiver.onUncaughtError(e);
          }
        }
      } catch (final RuntimeException e) {
        Log.errInvocation(VarFuture.class, "history strategy", "onSubscribe", e);
      }
    }
  }

  private class CancelledStatus extends Status {

    @Override
    public void get(@NotNull final Semaphore semaphore) {
      semaphore.release();
    }

    @Override
    public void iterator(@NotNull final FutureIterator<V> iterator) {
      super.iterator(iterator);
      iterator.fail(failureException);
    }

    @Override
    void subscribe(@NotNull final Receiver<V> receiver,
        @NotNull final GroupReceiver<V> groupReceiver) {
      super.subscribe(receiver, groupReceiver);
      try {
        groupReceiver.fail(failureException);
      } catch (final RuntimeException e) {
        groupReceiver.onUncaughtError(e);
      }
      groupReceiver.onUnsubscribe();
    }
  }

  private class ClosedStatus extends Status {

    @Override
    public void get(@NotNull final Semaphore semaphore) {
      semaphore.release();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void iterator(@NotNull final FutureIterator<V> iterator) {
      super.iterator(iterator);
      if (lastValue != UNSET) {
        iterator.add((V) lastValue);
      }
      iterator.end();
    }

    @Override
    @SuppressWarnings("unchecked")
    void subscribe(@NotNull final Receiver<V> receiver,
        @NotNull final GroupReceiver<V> groupReceiver) {
      super.subscribe(receiver, groupReceiver);
      try {
        if (lastValue != UNSET) {
          groupReceiver.set((V) lastValue);
        }
        groupReceiver.close();
      } catch (final RuntimeException e) {
        groupReceiver.onUncaughtError(e);
      }
      groupReceiver.onUnsubscribe();
    }
  }

  private class RunningStatus extends Status {

    // TODO: set volatile Thread => cancel(true) (only set, setBulk??)

    @Override
    @SuppressWarnings("unchecked")
    public boolean fail(@NotNull final Exception error) {
      innerStatus = new CancelledStatus();
      if (lastValue != UNSET) {
        try {
          historyStrategy.onSet((V) lastValue);
        } catch (final RuntimeException e) {
          Log.errInvocation(VarFuture.class, "history strategy", "onSet", e);
        }
      }
      lastValue = UNSET;
      failureException = error;
      if (FutureCancellationException.class.equals(error.getClass())) {
        result = new CancellationResult<V>();
      } else {
        result = new FailureResult<V>(error);
      }
      final WeakHashMap<FutureIterator<V>, Void> iterators = VarFuture.this.iterators;
      final WeakHashMap<Semaphore, Void> semaphores = VarFuture.this.semaphores;
      if (isUncaught()) {
        for (final FutureIterator<V> futureIterator : iterators.keySet()) {
          futureIterator.fail(error);
        }
        iterators.clear();
        for (final Semaphore semaphore : semaphores.keySet()) {
          semaphore.release();
        }
        semaphores.clear();
        registration.onUncaughtError(error);
      } else {
        final HashMap<Receiver<?>, GroupReceiver<V>> receivers = VarFuture.this.receivers;
        for (final GroupReceiver<V> groupReceiver : receivers.values()) {
          try {
            groupReceiver.fail(error);
          } catch (final RuntimeException e) {
            groupReceiver.onUncaughtError(e);
          }
          groupReceiver.onUnsubscribe();
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
        registration.cancel();
      }
      status.set(CANCELLED);
      return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setBulk(@NotNull final Collection<V> values) {
      if (lastValue != UNSET) {
        try {
          historyStrategy.onSet((V) lastValue);
        } catch (final RuntimeException e) {
          Log.errInvocation(VarFuture.class, "history strategy", "onSet", e);
        }
      }
      final int lastIndex = values.size() - 1;
      final List<V> valuesList = (List<V>) values;
      lastValue = valuesList.get(lastIndex);
      try {
        historyStrategy.onSetBulk(valuesList.subList(0, lastIndex));
      } catch (final RuntimeException e) {
        Log.errInvocation(VarFuture.class, "history strategy", "onSetBulk", e);
      }
      for (final Entry<Receiver<?>, GroupReceiver<V>> entry : receivers.entrySet()) {
        final GroupReceiver<V> groupReceiver = entry.getValue();
        if (groupReceiver.isSink()) {
          try {
            if (values.size() == 1) {
              groupReceiver.set(valuesList.get(0));
            } else {
              groupReceiver.setBulk(values);
            }
          } catch (final RuntimeException e) {
            groupReceiver.onUncaughtError(e);
          }
        }
      }
      for (final FutureIterator<V> futureIterator : iterators.keySet()) {
        futureIterator.addAll(values);
      }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void set(final V value) {
      if (lastValue != UNSET) {
        try {
          historyStrategy.onSet((V) lastValue);
        } catch (final RuntimeException e) {
          Log.errInvocation(VarFuture.class, "history strategy", "onSet", e);
        }
      }
      lastValue = value;
      for (final Entry<Receiver<?>, GroupReceiver<V>> entry : receivers.entrySet()) {
        final GroupReceiver<V> groupReceiver = entry.getValue();
        if (groupReceiver.isSink()) {
          try {
            groupReceiver.set(value);
          } catch (final RuntimeException e) {
            groupReceiver.onUncaughtError(e);
          }
        }
      }
      for (final FutureIterator<V> futureIterator : iterators.keySet()) {
        futureIterator.add(value);
      }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void close() {
      innerStatus = new ClosedStatus();
      result = new ValueResult<V>((V) lastValue);
      try {
        historyStrategy.onClose();
      } catch (final RuntimeException e) {
        Log.errInvocation(VarFuture.class, "history strategy", "onClose", e);
      }
      final HashMap<Receiver<?>, GroupReceiver<V>> receivers = VarFuture.this.receivers;
      for (final GroupReceiver<V> groupReceiver : receivers.values()) {
        try {
          groupReceiver.close();
        } catch (final RuntimeException e) {
          groupReceiver.onUncaughtError(e);
        }
        groupReceiver.onUnsubscribe();
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
      status.set(CLOSED);
    }

    @Override
    public void clear() {
      lastValue = UNSET;
      try {
        historyStrategy.onClear();
      } catch (final RuntimeException e) {
        Log.errInvocation(VarFuture.class, "history strategy", "onClear", e);
      }
    }

    @Override
    public void compute(@NotNull final Group group,
        @NotNull final Function<? super V, ? extends V> function) {
      if (lastValue != UNSET) {
        scheduler.pause();
        try {
          group.onTask(new VarTask() {
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
          Log.errInvocation(VarFuture.class, "group", "onTask", e);
          scheduler.resume();
        }
      }
    }

    @Override
    public void get(@NotNull final Semaphore semaphore) {
      semaphores.put(semaphore, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void iterator(@NotNull final FutureIterator<V> iterator) {
      super.iterator(iterator);
      if (lastValue != UNSET) {
        iterator.add((V) lastValue);
      }
      iterators.put(iterator, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    void subscribe(@NotNull final Receiver<V> receiver,
        @NotNull final GroupReceiver<V> groupReceiver) {
      final HashMap<Receiver<?>, GroupReceiver<V>> receivers = VarFuture.this.receivers;
      if (!receivers.containsKey(receiver)) {
        super.subscribe(receiver, groupReceiver);
        if (lastValue != UNSET) {
          try {
            groupReceiver.set((V) lastValue);
          } catch (final RuntimeException e) {
            groupReceiver.onUncaughtError(e);
          }
        }
        receivers.put(receiver, groupReceiver);
      }
    }

    private boolean isUncaught() {
      for (final GroupReceiver<V> groupReceiver : receivers.values()) {
        if (groupReceiver.isSink()) {
          return false;
        }
      }
      return true;
    }
  }

// Tasks

  private class GetTask extends Semaphore implements Task {

    private GetTask() {
      super(0);
    }

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
      innerStatus.get(this);
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

  private abstract class VarTask implements Task {

    @Override
    public @NotNull String taskID() {
      return taskID;
    }

    @Override
    public int weight() {
      return 1;
    }
  }
}
