////
// WARNING: GENERATED CODE - DO NOT MODIFY!!
////
package sparx.concurrent;

import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import sparx.tuple.Quintuple;
import sparx.util.Requires;

public class QuintupleFuture<V, V1 extends V, V2 extends V, V3 extends V, V4 extends V, V5 extends V> extends
    TupleStreamGroupFuture<V, QuintupleFuture<V, V1, V2, V3, V4, V5>> implements
    Quintuple<StreamingFuture<? extends V>, StreamingFuture<V1>, StreamingFuture<V2>, StreamingFuture<V3>, StreamingFuture<V4>, StreamingFuture<V5>> {

  public static @NotNull <V, V1 extends V, V2 extends V, V3 extends V, V4 extends V, V5 extends V> QuintupleFuture<V, V1, V2, V3, V4, V5> of(
      @NotNull final StreamingFuture<V1> first,
      @NotNull final StreamingFuture<V2> second,
      @NotNull final StreamingFuture<V3> third,
      @NotNull final StreamingFuture<V4> fourth,
      @NotNull final StreamingFuture<V5> fifth) { 
    return new QuintupleFuture<V, V1, V2, V3, V4, V5>(
        Requires.notNull(first, "first"),
        Requires.notNull(second, "second"),
        Requires.notNull(third, "third"),
        Requires.notNull(fourth, "fourth"),
        Requires.notNull(fifth, "fifth"));
  }

  private final StreamingFuture<V1> first;
  private final StreamingFuture<V2> second;
  private final StreamingFuture<V3> third;
  private final StreamingFuture<V4> fourth;
  private final StreamingFuture<V5> fifth;
  private final List<StreamingFuture<? extends V>> futures;

  private QuintupleFuture(
      @NotNull final StreamingFuture<V1> first,
      @NotNull final StreamingFuture<V2> second,
      @NotNull final StreamingFuture<V3> third,
      @NotNull final StreamingFuture<V4> fourth,
      @NotNull final StreamingFuture<V5> fifth) { 
    this.first = first;
    this.second = second;
    this.third = third;
    this.fourth = fourth;
    this.fifth = fifth;
    this.futures = Arrays.asList(first, second, third, fourth, fifth);
  }

  @Override
  public @NotNull QuintupleFuture<V, V1, V2, V3, V4, V5> readOnly() {
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
  public @NotNull StreamingFuture<V5> getFifth() {
    return fifth;
  }

  @Override
  public @NotNull List<StreamingFuture<? extends V>> asList() {
    return futures;
  }
}
