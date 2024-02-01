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
package sparx.concurrent.tuple;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.StreamingFuture;
import sparx.tuple.Sextuple;
import sparx.util.ImmutableList;
import sparx.util.Require;

///////////////////////////////////////////////
// WARNING: GENERATED CODE - DO NOT MODIFY!! //
///////////////////////////////////////////////

public class SextupleFuture<V, V1 extends V, V2 extends V, V3 extends V, V4 extends V, V5 extends V, V6 extends V> extends
    StreamScopeTupleFuture<V, SextupleFuture<V, V1, V2, V3, V4, V5, V6>> implements
    Sextuple<StreamingFuture<? extends V>, StreamingFuture<V1>, StreamingFuture<V2>, StreamingFuture<V3>, StreamingFuture<V4>, StreamingFuture<V5>, StreamingFuture<V6>> {

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
    this.futures = ImmutableList.of(first, second, third, fourth, fifth, sixth);
  }

  public static @NotNull <V, V1 extends V, V2 extends V, V3 extends V, V4 extends V, V5 extends V, V6 extends V> SextupleFuture<V, V1, V2, V3, V4, V5, V6> of(
      @NotNull final StreamingFuture<V1> first,
      @NotNull final StreamingFuture<V2> second,
      @NotNull final StreamingFuture<V3> third,
      @NotNull final StreamingFuture<V4> fourth,
      @NotNull final StreamingFuture<V5> fifth,
      @NotNull final StreamingFuture<V6> sixth) { 
    return new SextupleFuture<V, V1, V2, V3, V4, V5, V6>(
        Require.notNull(first, "first"),
        Require.notNull(second, "second"),
        Require.notNull(third, "third"),
        Require.notNull(fourth, "fourth"),
        Require.notNull(fifth, "fifth"),
        Require.notNull(sixth, "sixth")
    );
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

  @Override
  public @NotNull SextupleFuture<V, V1, V2, V3, V4, V5, V6> readOnly() {
    return this;
  }

  @Override
  protected @NotNull SextupleFuture<V, V1, V2, V3, V4, V5, V6> createProxy() {
    return new SextupleFuture<V, V1, V2, V3, V4, V5, V6>(
        proxyFuture(getFirst()),
        proxyFuture(getSecond()),
        proxyFuture(getThird()),
        proxyFuture(getFourth()),
        proxyFuture(getFifth()),
        proxyFuture(getSixth())
    );
  }

  @Override
  protected void subscribeProxy(@NotNull final SextupleFuture<V, V1, V2, V3, V4, V5, V6> proxyFuture) {
    connectProxy(proxyFuture.getFirst());
    connectProxy(proxyFuture.getSecond());
    connectProxy(proxyFuture.getThird());
    connectProxy(proxyFuture.getFourth());
    connectProxy(proxyFuture.getFifth());
    connectProxy(proxyFuture.getSixth());
  }
}
