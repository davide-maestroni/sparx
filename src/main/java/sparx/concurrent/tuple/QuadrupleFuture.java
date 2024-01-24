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
import sparx.concurrent.VarFuture;
import sparx.tuple.Quadruple;
import sparx.util.ImmutableList;
import sparx.util.Require;

///////////////////////////////////////////////
// WARNING: GENERATED CODE - DO NOT MODIFY!! //
///////////////////////////////////////////////

public class QuadrupleFuture<V, V1 extends V, V2 extends V, V3 extends V, V4 extends V> extends
    StreamGroupTupleFuture<V, QuadrupleFuture<V, V1, V2, V3, V4>> implements
    Quadruple<StreamingFuture<? extends V>, StreamingFuture<V1>, StreamingFuture<V2>, StreamingFuture<V3>, StreamingFuture<V4>> {

  public static @NotNull <V, V1 extends V, V2 extends V, V3 extends V, V4 extends V> QuadrupleFuture<V, V1, V2, V3, V4> of(
      @NotNull final StreamingFuture<V1> first,
      @NotNull final StreamingFuture<V2> second,
      @NotNull final StreamingFuture<V3> third,
      @NotNull final StreamingFuture<V4> fourth) { 
    return new QuadrupleFuture<V, V1, V2, V3, V4>(
        Require.notNull(first, "first"),
        Require.notNull(second, "second"),
        Require.notNull(third, "third"),
        Require.notNull(fourth, "fourth")
    );
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
    this.futures = ImmutableList.of(first, second, third, fourth);
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

  @Override
  public @NotNull QuadrupleFuture<V, V1, V2, V3, V4> readOnly() {
    return this;
  }

  @Override
  protected @NotNull QuadrupleFuture<V, V1, V2, V3, V4> createFuture() {
    return new QuadrupleFuture<V, V1, V2, V3, V4>(
        VarFuture.<V1>create(),
        VarFuture.<V2>create(),
        VarFuture.<V3>create(),
        VarFuture.<V4>create()
    );
  }

  @Override
  protected void subscribeFuture(@NotNull final QuadrupleFuture<V, V1, V2, V3, V4> future) {
    getFirst().subscribe(future.getFirst());
    getSecond().subscribe(future.getSecond());
    getThird().subscribe(future.getThird());
    getFourth().subscribe(future.getFourth());
  }
}
