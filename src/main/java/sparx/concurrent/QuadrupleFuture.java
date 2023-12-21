////
// WARNING: GENERATED CODE - DO NOT MODIFY!!
////
package sparx.concurrent;

import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import sparx.tuple.Quadruple;
import sparx.util.Requires;

public class QuadrupleFuture<V, V1 extends V, V2 extends V, V3 extends V, V4 extends V> extends
    TupleStreamGroupFuture<V, QuadrupleFuture<V, V1, V2, V3, V4>> implements
    Quadruple<StreamingFuture<? extends V>, StreamingFuture<V1>, StreamingFuture<V2>, StreamingFuture<V3>, StreamingFuture<V4>> {

  public static @NotNull <V, V1 extends V, V2 extends V, V3 extends V, V4 extends V> QuadrupleFuture<V, V1, V2, V3, V4> of(
      @NotNull final StreamingFuture<V1> first,
      @NotNull final StreamingFuture<V2> second,
      @NotNull final StreamingFuture<V3> third,
      @NotNull final StreamingFuture<V4> fourth) { 
    return new QuadrupleFuture<V, V1, V2, V3, V4>(
        Requires.notNull(first, "first"),
        Requires.notNull(second, "second"),
        Requires.notNull(third, "third"),
        Requires.notNull(fourth, "fourth"));
  }

  private final StreamingFuture<V1> first;
  private final StreamingFuture<V2> second;
  private final StreamingFuture<V3> third;
  private final StreamingFuture<V4> fourth;
  private final List<StreamingFuture<? extends V>> futures;

  private QuadrupleFuture(
      @NotNull final StreamingFuture<V1> first,
      @NotNull final StreamingFuture<V2> second,
      @NotNull final StreamingFuture<V3> third,
      @NotNull final StreamingFuture<V4> fourth) { 
    this.first = first;
    this.second = second;
    this.third = third;
    this.fourth = fourth;
    this.futures = Arrays.asList(first, second, third, fourth);
  }

  @Override
  public @NotNull QuadrupleFuture<V, V1, V2, V3, V4> readOnly() {
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
  public @NotNull StreamingFuture<V3> getThird() {
    return third;
  }

  @Override
  public @NotNull StreamingFuture<V4> getFourth() {
    return fourth;
  }

  @Override
  public @NotNull List<StreamingFuture<? extends V>> asList() {
    return futures;
  }
}
