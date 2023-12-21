package sparx.concurrent;

import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import sparx.tuple.Single;
import sparx.util.Requires;

public class SingleFuture<V> extends TupleStreamGroupFuture<V, SingleFuture<V>> implements
    Single<StreamingFuture<? extends V>> {

  public static @NotNull <V> SingleFuture<V> of(@NotNull final StreamingFuture<V> first) {
    return new SingleFuture<V>(Requires.notNull(first, "first"));
  }

  private final StreamingFuture<V> first;
  private final List<StreamingFuture<? extends V>> futures;

  private SingleFuture(@NotNull final StreamingFuture<V> first) {
    this.first = first;
    this.futures = Collections.<StreamingFuture<? extends V>>singletonList(first);
  }

  @Override
  public @NotNull SingleFuture<V> readOnly() {
    return this;
  }

  @Override
  public @NotNull StreamingFuture<V> getFirst() {
    return first;
  }

  @Override
  public @NotNull List<StreamingFuture<? extends V>> asList() {
    return futures;
  }
}
