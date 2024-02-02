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
import sparx.tuple.Octuple;
import sparx.util.ImmutableList;
import sparx.util.Require;

///////////////////////////////////////////////
// WARNING: GENERATED CODE - DO NOT MODIFY!! //
///////////////////////////////////////////////

public class OctupleFuture<V, V1 extends V, V2 extends V, V3 extends V, V4 extends V, V5 extends V, V6 extends V, V7 extends V, V8 extends V> extends
    StreamScopeTupleFuture<V, OctupleFuture<V, V1, V2, V3, V4, V5, V6, V7, V8>> implements
    Octuple<StreamingFuture<? extends V>, StreamingFuture<V1>, StreamingFuture<V2>, StreamingFuture<V3>, StreamingFuture<V4>, StreamingFuture<V5>, StreamingFuture<V6>, StreamingFuture<V7>, StreamingFuture<V8>> {

  private final StreamingFuture<V1> first;
  private final StreamingFuture<V2> second;
  private final StreamingFuture<V3> third;
  private final StreamingFuture<V4> fourth;
  private final StreamingFuture<V5> fifth;
  private final StreamingFuture<V6> sixth;
  private final StreamingFuture<V7> seventh;
  private final StreamingFuture<V8> eighth;
  private final List<StreamingFuture<? extends V>> futures;

  private OctupleFuture(
      @NotNull final StreamingFuture<V1> first,
      @NotNull final StreamingFuture<V2> second,
      @NotNull final StreamingFuture<V3> third,
      @NotNull final StreamingFuture<V4> fourth,
      @NotNull final StreamingFuture<V5> fifth,
      @NotNull final StreamingFuture<V6> sixth,
      @NotNull final StreamingFuture<V7> seventh,
      @NotNull final StreamingFuture<V8> eighth) { 
    this.first = first;
    this.second = second;
    this.third = third;
    this.fourth = fourth;
    this.fifth = fifth;
    this.sixth = sixth;
    this.seventh = seventh;
    this.eighth = eighth;
    this.futures = ImmutableList.of(first, second, third, fourth, fifth, sixth, seventh, eighth);
  }

  public static @NotNull <V, V1 extends V, V2 extends V, V3 extends V, V4 extends V, V5 extends V, V6 extends V, V7 extends V, V8 extends V> OctupleFuture<V, V1, V2, V3, V4, V5, V6, V7, V8> of(
      @NotNull final StreamingFuture<V1> first,
      @NotNull final StreamingFuture<V2> second,
      @NotNull final StreamingFuture<V3> third,
      @NotNull final StreamingFuture<V4> fourth,
      @NotNull final StreamingFuture<V5> fifth,
      @NotNull final StreamingFuture<V6> sixth,
      @NotNull final StreamingFuture<V7> seventh,
      @NotNull final StreamingFuture<V8> eighth) { 
    return new OctupleFuture<V, V1, V2, V3, V4, V5, V6, V7, V8>(
        Require.notNull(first, "first"),
        Require.notNull(second, "second"),
        Require.notNull(third, "third"),
        Require.notNull(fourth, "fourth"),
        Require.notNull(fifth, "fifth"),
        Require.notNull(sixth, "sixth"),
        Require.notNull(seventh, "seventh"),
        Require.notNull(eighth, "eighth")
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
  public @NotNull StreamingFuture<V7> getSeventh() {
    return seventh;
  }

  @Override
  public @NotNull StreamingFuture<V8> getEighth() {
    return eighth;
  }

  @Override
  public @NotNull List<StreamingFuture<? extends V>> asList() {
    return futures;
  }

  @Override
  public @NotNull OctupleFuture<V, V1, V2, V3, V4, V5, V6, V7, V8> readOnly() {
    return this;
  }

  @Override
  protected @NotNull OctupleFuture<V, V1, V2, V3, V4, V5, V6, V7, V8> createPaused() {
    return new OctupleFuture<V, V1, V2, V3, V4, V5, V6, V7, V8>(
        pauseFuture(getFirst()),
        pauseFuture(getSecond()),
        pauseFuture(getThird()),
        pauseFuture(getFourth()),
        pauseFuture(getFifth()),
        pauseFuture(getSixth()),
        pauseFuture(getSeventh()),
        pauseFuture(getEighth())
    );
  }

  @Override
  protected void resumePaused(@NotNull final OctupleFuture<V, V1, V2, V3, V4, V5, V6, V7, V8> pausedFuture) {
    resumeFuture(pausedFuture.getFirst());
    resumeFuture(pausedFuture.getSecond());
    resumeFuture(pausedFuture.getThird());
    resumeFuture(pausedFuture.getFourth());
    resumeFuture(pausedFuture.getFifth());
    resumeFuture(pausedFuture.getSixth());
    resumeFuture(pausedFuture.getSeventh());
    resumeFuture(pausedFuture.getEighth());
  }
}
