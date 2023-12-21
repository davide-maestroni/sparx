package sparx.concurrent;

import org.jetbrains.annotations.NotNull;
import sparx.function.Consumer;
import sparx.function.Function;
import sparx.function.Predicate;
import sparx.util.Nothing;

public interface ExecutionContext {

  int minThroughput();

  int pendingCount();

  @NotNull <V, F extends TupleFuture<V, ?>, U> StreamingFuture<U> call(@NotNull F future,
      @NotNull Function<F, ? extends SignalFuture<U>> function, int weight);

  @NotNull <V, F extends TupleFuture<V, ?>> StreamingFuture<Nothing> run(@NotNull F future,
      @NotNull Consumer<F> consumer, int weight);

  @NotNull <V, F extends TupleFuture<V, ?>> StreamingFuture<Boolean> test(@NotNull F future,
      @NotNull Predicate<F> predicate, int weight);
}
