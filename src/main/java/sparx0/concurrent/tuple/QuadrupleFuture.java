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
package sparx0.concurrent.tuple;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import sparx0.concurrent.StreamingFuture;
import sparx0.util.ImmutableList;
import sparx0.util.Require;
import sparx0.util.tuple.Quadruple;

///////////////////////////////////////////////
// WARNING: GENERATED CODE - DO NOT MODIFY!!
// - templating engine: Mustache 0.9.11
// - template path: template/concurrent/TupleFuture.mustache
///////////////////////////////////////////////

public class QuadrupleFuture<V, V1 extends V, V2 extends V, V3 extends V, V4 extends V> extends
    StreamScopeTupleFuture<V, QuadrupleFuture<V, V1, V2, V3, V4>> implements
    Quadruple<StreamingFuture<? extends V>, StreamingFuture<V1>, StreamingFuture<V2>, StreamingFuture<V3>, StreamingFuture<V4>> {

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
  protected @NotNull QuadrupleFuture<V, V1, V2, V3, V4> createPaused() {
    return new QuadrupleFuture<V, V1, V2, V3, V4>(
        pauseFuture(getFirst()),
        pauseFuture(getSecond()),
        pauseFuture(getThird()),
        pauseFuture(getFourth())
    );
  }

  @Override
  protected void resumePaused(@NotNull final QuadrupleFuture<V, V1, V2, V3, V4> pausedFuture) {
    resumeFuture(pausedFuture.getFirst());
    resumeFuture(pausedFuture.getSecond());
    resumeFuture(pausedFuture.getThird());
    resumeFuture(pausedFuture.getFourth());
  }
}