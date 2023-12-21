////
// WARNING: GENERATED CODE - DO NOT MODIFY!!
////
package sparx.concurrent;

import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import sparx.tuple.Sextuple;
import sparx.util.Requires;

public class SextupleFuture<V, V1 extends V, V2 extends V, V3 extends V, V4 extends V, V5 extends V, V6 extends V> extends
    TupleStreamGroupFuture<V, SextupleFuture<V, V1, V2, V3, V4, V5, V6>> implements
    Sextuple<StreamingFuture<? extends V>, StreamingFuture<V1>, StreamingFuture<V2>, StreamingFuture<V3>, StreamingFuture<V4>, StreamingFuture<V5>, StreamingFuture<V6>> {

  public static @NotNull <V, V1 extends V, V2 extends V, V3 extends V, V4 extends V, V5 extends V, V6 extends V> SextupleFuture<V, V1, V2, V3, V4, V5, V6> of(
      @NotNull final StreamingFuture<V1> first,
      @NotNull final StreamingFuture<V2> second,
      @NotNull final StreamingFuture<V3> third,
      @NotNull final StreamingFuture<V4> fourth,
      @NotNull final StreamingFuture<V5> fifth,
      @NotNull final StreamingFuture<V6> sixth) { 
    return new SextupleFuture<V, V1, V2, V3, V4, V5, V6>(
        Requires.notNull(first, "first"),
        Requires.notNull(second, "second"),
        Requires.notNull(third, "third"),
        Requires.notNull(fourth, "fourth"),
        Requires.notNull(fifth, "fifth"),
        Requires.notNull(sixth, "sixth"));
  }

  private final StreamingFuture<V1> first;
  private final StreamingFuture<V2> second;
  private final StreamingFuture<V3> third;
  private final StreamingFuture<V4> fourth;
  private final StreamingFuture<V5> fifth;
  private final StreamingFuture<V6> sixth;
  private final List<StreamingFuture<? extends V>> futures;

  private SextupleFuture(
      @NotNull final StreamingFuture<V1> first,
      @NotNull final StreamingFuture<V2> second,
      @NotNull final StreamingFuture<V3> third,
      @NotNull final StreamingFuture<V4> fourth,
      @NotNull final StreamingFuture<V5> fifth,
      @NotNull final StreamingFuture<V6> sixth) { 
    this.first = first;
    this.second = second;
    this.third = third;
    this.fourth = fourth;
    this.fifth = fifth;
    this.sixth = sixth;
    this.futures = Arrays.asList(first, second, third, fourth, fifth, sixth);
  }

  @Override
  public @NotNull SextupleFuture<V, V1, V2, V3, V4, V5, V6> readOnly() {
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
  public @NotNull StreamingFuture<V6> getSixth() {
    return sixth;
  }

  @Override
  public @NotNull List<StreamingFuture<? extends V>> asList() {
    return futures;
  }
}
