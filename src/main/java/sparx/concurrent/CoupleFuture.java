////
// WARNING: GENERATED CODE - DO NOT MODIFY!!
////
package sparx.concurrent;

import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import sparx.tuple.Couple;
import sparx.util.Requires;

public class CoupleFuture<V, V1 extends V, V2 extends V> extends
    TupleStreamGroupFuture<V, CoupleFuture<V, V1, V2>> implements
    Couple<StreamingFuture<? extends V>, StreamingFuture<V1>, StreamingFuture<V2>> {

  public static @NotNull <V, V1 extends V, V2 extends V> CoupleFuture<V, V1, V2> of(
      @NotNull final StreamingFuture<V1> first,
      @NotNull final StreamingFuture<V2> second) { 
    return new CoupleFuture<V, V1, V2>(
        Requires.notNull(first, "first"),
        Requires.notNull(second, "second"));
  }

  private final StreamingFuture<V1> first;
  private final StreamingFuture<V2> second;
  private final List<StreamingFuture<? extends V>> futures;

  private CoupleFuture(
      @NotNull final StreamingFuture<V1> first,
      @NotNull final StreamingFuture<V2> second) { 
    this.first = first;
    this.second = second;
    this.futures = Arrays.asList(first, second);
  }

  @Override
  public @NotNull CoupleFuture<V, V1, V2> readOnly() {
    return this;
  }

  @Override
  public @NotNull StreamingFuture<V1> getFirst() {
    return first;
  }

  @Override
  public @NotNull StreamingFuture<V2> getSecond() {
    return second;
  }

  @Override
  public @NotNull List<StreamingFuture<? extends V>> asList() {
    return futures;
  }
}
