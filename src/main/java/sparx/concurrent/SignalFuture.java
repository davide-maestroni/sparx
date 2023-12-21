package sparx.concurrent;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sparx.function.Action;
import sparx.function.Consumer;
import sparx.function.Function;
import sparx.util.LiveIterator;

public interface SignalFuture<V> extends Future<List<V>>, Receiver<V>, Iterable<V> {

  // TODO:
  //  state (in execution context)?
  //  lazy? f.pipe(lazy(...))
  //  pull?
  //  generator?
  //  FailedFuture (ConstFuture??)

  void clear();

  void compute(@NotNull Function<? super V, ? extends V> function);

  V getCurrent();

  V getCurrentOr(V defaultValue);

  boolean isReadOnly();

  @NotNull SignalFuture<V> readOnly();

  @NotNull Subscription subscribe(@NotNull Receiver<? super V> receiver);

  @NotNull Subscription subscribe(@Nullable Consumer<? super V> onValueConsumer,
      @Nullable Consumer<? super Collection<V>> onValuesConsumer,
      @Nullable Consumer<Exception> onErrorConsumer,
      @Nullable Action onCloseAction);

  void unsubscribe(@NotNull Receiver<?> receiver);

  @Override
  @NotNull LiveIterator<V> iterator();

  @NotNull LiveIterator<V> iterator(long timeout, @NotNull TimeUnit unit);

  interface Subscription {

    void cancel();
  }
}
