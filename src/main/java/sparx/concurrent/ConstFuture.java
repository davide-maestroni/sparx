package sparx.concurrent;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sparx.function.Action;
import sparx.function.Consumer;
import sparx.logging.Log;
import sparx.util.LiveIterator;

public class ConstFuture<V> extends ReadOnlyStreamGroupFuture<V, StreamingFuture<V>> implements
    StreamingFuture<V> {

  private static final ConstFuture<?> EMPTY_FUTURE = new ConstFuture<Object>(emptyList()) {

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
        Log.err(ConstFuture.class, "Uncaught exception: %s", Log.printable(e));
      }
      return DummySubscription.instance();
    }

    @Override
    public @NotNull Subscription subscribe(@Nullable final Consumer<? super Object> onValueConsumer,
        @Nullable final Consumer<? super Collection<Object>> onValuesConsumer,
        @Nullable final Consumer<Exception> onErrorConsumer, @Nullable final Action onCloseAction) {
      if (onCloseAction != null) {
        try {
          onCloseAction.run();
        } catch (final Exception e) {
          Log.err(ConstFuture.class, "Uncaught exception: %s", Log.printable(e));
        }
      }
      return DummySubscription.instance();
    }
  };

  @SuppressWarnings("unchecked")
  public static @NotNull <V> ConstFuture<V> of() {
    return (ConstFuture<V>) EMPTY_FUTURE;
  }

  public static @NotNull <V> ConstFuture<V> of(final V value) {
    return new ConstFuture<V>(singletonList(value)) {
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

  public static @NotNull <V> ConstFuture<V> ofBulk(@NotNull final Collection<V> values) {
    if (values.isEmpty()) {
      return of();
    } else if (values.size() == 1) {
      return of(values.iterator().next());
    }
    return new ConstFuture<V>(unmodifiableList(new ArrayList<V>(values)));
  }

  private final List<V> values;

  private ConstFuture(@NotNull final List<V> values) {
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
      Log.err(ConstFuture.class, "Uncaught exception: %s", Log.printable(e));
    }
    return DummySubscription.instance();
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
  public @NotNull LiveIterator<V> iterator() {
    return new ConstIterator<V>(values);
  }

  @Override
  public @NotNull LiveIterator<V> iterator(final long timeout, @NotNull final TimeUnit unit) {
    return iterator();
  }

  @Override
  public @NotNull ConstFuture<V> readOnly() {
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
  public List<V> get() {
    return values;
  }

  @Override
  public List<V> get(final long timeout, @NotNull final TimeUnit unit) {
    return get();
  }

  private static class ConstIterator<V> implements LiveIterator<V> {

    private final Iterator<V> iterator;

    private ConstIterator(@NotNull final Collection<V> values) {
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
