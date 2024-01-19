package sparx;

import org.jetbrains.annotations.NotNull;
import sparx.concurrent.CoupleFuture;
import sparx.concurrent.Receiver;
import sparx.concurrent.SingleFuture;
import sparx.concurrent.StreamingFuture;
import sparx.concurrent.TripleFuture;
import sparx.concurrent.VarFuture;
import sparx.concurrent.history.HistoryStrategy;
import sparx.function.BinaryFunction;
import sparx.function.Consumer;

public class Sparx {

  public static @NotNull <V> StreamingFuture<V> future() {
    return VarFuture.create();
  }

  public static @NotNull <V> StreamingFuture<V> future(
      @NotNull final HistoryStrategy<V> historyStrategy) {
    return VarFuture.create(historyStrategy);
  }

  public static @NotNull <V> SingleFuture<V> tupleFuture(@NotNull StreamingFuture<V> first) {
    return SingleFuture.of(first);
  }

  public static @NotNull <V, V1 extends V, V2 extends V> CoupleFuture<V, V1, V2> tupleFuture(
      @NotNull StreamingFuture<V1> first, @NotNull StreamingFuture<V2> second) {
    return CoupleFuture.of(first, second);
  }

  public static @NotNull <V, V1 extends V, V2 extends V, V3 extends V> TripleFuture<V, V1, V2, V3> tupleFuture(
      @NotNull StreamingFuture<V1> first, @NotNull StreamingFuture<V2> second,
      @NotNull StreamingFuture<V3> third) {
    return TripleFuture.of(first, second, third);
  }

  public static @NotNull <V> StreamingFuture<V> generator(
      @NotNull Consumer<? super Receiver<V>> consumer) {
    return null;
  }

  public static @NotNull <V> StreamingFuture<V> generator(V initialValue,
      @NotNull BinaryFunction<? super V, ? super Receiver<V>, V> consumer) {
    return null;
  }
}
