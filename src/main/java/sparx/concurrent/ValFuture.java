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
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sparx.function.Action;
import sparx.function.Consumer;
import sparx.logging.Log;
import sparx.util.ImmutableList;
import sparx.util.LiveIterator;
import sparx.util.Requires;

public abstract class ValFuture<V> extends
    ReadOnlyStreamGroupFuture<V, StreamingFuture<V>> implements StreamingFuture<V> {

  @SuppressWarnings("unchecked")
  public static @NotNull <V> ValFuture<V> of() {
    return (ValFuture<V>) ValuesFuture.EMPTY_FUTURE;
  }

  public static @NotNull <V> ValFuture<V> of(final V value) {
    return new ValuesFuture<V>(ImmutableList.of(value)) {
      @Override
      public V getCurrent() {
        return get().get(0);
      }

      @Override
      public V getCurrentOr(final V defaultValue) {
        return getCurrent();
      }
    };
  }

  public static @NotNull <V> ValFuture<V> ofBulk(@NotNull final Collection<V> values) {
    if (values.isEmpty()) {
      return of();
    } else if (values.size() == 1) {
      return of(values.iterator().next());
    }
    return new ValuesFuture<V>(ImmutableList.ofElementsIn(values));
  }

  public static @NotNull <V> ValFuture<V> ofBulk(@NotNull final V... values) {
    if (values.length == 0) {
      return of();
    } else if (values.length == 1) {
      return of(values[0]);
    }
    return new ValuesFuture<V>(ImmutableList.of(values));
  }

  public static @NotNull <V> ValFuture<V> ofFailure(@NotNull final Exception error) {
    return new FailureFuture<V>(Requires.notNull(error, "error"));
  }

  private ValFuture() {
  }

  @Override
  public @NotNull ValFuture<V> readOnly() {
    return this;
  }

  @Override
  public @NotNull Subscription subscribe(@Nullable final Consumer<? super V> onValueConsumer,
      @Nullable final Consumer<? super Collection<V>> onValuesConsumer,
      @Nullable final Consumer<Exception> onErrorConsumer, @Nullable final Action onCloseAction) {
    return subscribe(new FunctionalReceiver<V>(onValueConsumer, onValuesConsumer, onErrorConsumer,
        onCloseAction));
  }

  @Override
  public void unsubscribe(@NotNull final Receiver<?> receiver) {
  }

  @Override
  public @NotNull LiveIterator<V> iterator(final long timeout, @NotNull final TimeUnit unit) {
    return iterator();
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
  public abstract List<V> get() throws ExecutionException;

  @Override
  public List<V> get(final long timeout, @NotNull final TimeUnit unit) throws ExecutionException {
    return get();
  }

  @Override
  protected @NotNull StreamingFuture<V> createFuture() {
    return new VarFuture<V>();
  }

  @Override
  protected void subscribeFuture(@NotNull final StreamingFuture<V> future) {
    subscribe(future);
  }

  private static class FailureFuture<V> extends ValFuture<V> {

    private final Exception error;
    private final FailureIterator<V> iterator;

    private FailureFuture(@NotNull final Exception error) {
      this.error = error;
      this.iterator = new FailureIterator<V>(error);
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
        Log.err(ValFuture.class, "Uncaught exception: %s", Log.printable(e));
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
          Log.err(ValFuture.class, "Uncaught exception: %s", Log.printable(e));
        }
      }
      return DummySubscription.instance();
    }

    @Override
    public @NotNull LiveIterator<V> iterator() {
      return iterator;
    }

    @Override
    public List<V> get() throws ExecutionException {
      throw new ExecutionException(error);
    }

    private static class FailureIterator<V> implements LiveIterator<V> {

      private final Exception error;

      private FailureIterator(@NotNull final Exception error) {
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

  private static class ValuesFuture<V> extends ValFuture<V> {

    private static final ValuesFuture<?> EMPTY_FUTURE = new ValuesFuture<Object>(
        ImmutableList.of()) {

      @Override
      public Object getCurrent() {
        throw new NoSuchElementException();
      }

      @Override
      public Object getCurrentOr(final Object defaultValue) {
        return defaultValue;
      }

      @Override
      public @NotNull Subscription subscribe(@NotNull final Receiver<? super Object> receiver) {
        try {
          receiver.close();
        } catch (final RuntimeException e) {
          Log.err(ValFuture.class, "Uncaught exception: %s", Log.printable(e));
        }
        return DummySubscription.instance();
      }

      @Override
      public @NotNull Subscription subscribe(
          @Nullable final Consumer<? super Object> onValueConsumer,
          @Nullable final Consumer<? super Collection<Object>> onValuesConsumer,
          @Nullable final Consumer<Exception> onErrorConsumer,
          @Nullable final Action onCloseAction) {
        if (onCloseAction != null) {
          try {
            onCloseAction.run();
          } catch (final Exception e) {
            Log.err(ValFuture.class, "Uncaught exception: %s", Log.printable(e));
          }
        }
        return DummySubscription.instance();
      }
    };

    private final List<V> values;

    private ValuesFuture(@NotNull final List<V> values) {
      this.values = values;
    }

    @Override
    public V getCurrent() {
      if (values.isEmpty()) {
        throw new NoSuchElementException();
      }
      return values.get(values.size() - 1);
    }

    @Override
    public V getCurrentOr(final V defaultValue) {
      if (values.isEmpty()) {
        return defaultValue;
      }
      return values.get(values.size() - 1);
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull Subscription subscribe(@NotNull final Receiver<? super V> receiver) {
      final List<V> values = this.values;
      try {
        if (values.size() == 1) {
          receiver.set(values.get(0));
        } else if (!values.isEmpty()) {
          ((Receiver<V>) receiver).setBulk(values);
        }
        receiver.close();
      } catch (final RuntimeException e) {
        Log.err(ValFuture.class, "Uncaught exception: %s", Log.printable(e));
      }
      return DummySubscription.instance();
    }

    @Override
    public @NotNull LiveIterator<V> iterator() {
      return new ValuesIterator<V>(values);
    }

    @Override
    public List<V> get() {
      return values;
    }

    private static class ValuesIterator<V> implements LiveIterator<V> {

      private final Iterator<V> iterator;

      private ValuesIterator(@NotNull final Collection<V> values) {
        iterator = values.iterator();
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
        return iterator.hasNext();
      }

      @Override
      public V next() {
        return iterator.next();
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("remove");
      }
    }
  }
}
