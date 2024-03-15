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
import sparx0.util.tuple.Decuple;

///////////////////////////////////////////////
// WARNING: GENERATED CODE - DO NOT MODIFY!!
// - templating engine: Mustache 0.9.11
// - template path: template/concurrent/TupleFuture.mustache
///////////////////////////////////////////////

public class DecupleFuture<V, V1 extends V, V2 extends V, V3 extends V, V4 extends V, V5 extends V, V6 extends V, V7 extends V, V8 extends V, V9 extends V, V10 extends V> extends
    StreamScopeTupleFuture<V, DecupleFuture<V, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10>> implements
    Decuple<StreamingFuture<? extends V>, StreamingFuture<V1>, StreamingFuture<V2>, StreamingFuture<V3>, StreamingFuture<V4>, StreamingFuture<V5>, StreamingFuture<V6>, StreamingFuture<V7>, StreamingFuture<V8>, StreamingFuture<V9>, StreamingFuture<V10>> {

  private final StreamingFuture<V1> first;
  private final StreamingFuture<V2> second;
  private final StreamingFuture<V3> third;
  private final StreamingFuture<V4> fourth;
  private final StreamingFuture<V5> fifth;
  private final StreamingFuture<V6> sixth;
  private final StreamingFuture<V7> seventh;
  private final StreamingFuture<V8> eighth;
  private final StreamingFuture<V9> ninth;
  private final StreamingFuture<V10> tenth;
  private final List<StreamingFuture<? extends V>> futures;

  private DecupleFuture(
      @NotNull final StreamingFuture<V1> first,
      @NotNull final StreamingFuture<V2> second,
      @NotNull final StreamingFuture<V3> third,
      @NotNull final StreamingFuture<V4> fourth,
      @NotNull final StreamingFuture<V5> fifth,
      @NotNull final StreamingFuture<V6> sixth,
      @NotNull final StreamingFuture<V7> seventh,
      @NotNull final StreamingFuture<V8> eighth,
      @NotNull final StreamingFuture<V9> ninth,
      @NotNull final StreamingFuture<V10> tenth) { 
    this.first = first;
    this.second = second;
    this.third = third;
    this.fourth = fourth;
    this.fifth = fifth;
    this.sixth = sixth;
    this.seventh = seventh;
    this.eighth = eighth;
    this.ninth = ninth;
    this.tenth = tenth;
    this.futures = ImmutableList.of(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth);
  }

  public static @NotNull <V, V1 extends V, V2 extends V, V3 extends V, V4 extends V, V5 extends V, V6 extends V, V7 extends V, V8 extends V, V9 extends V, V10 extends V> DecupleFuture<V, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10> of(
      @NotNull final StreamingFuture<V1> first,
      @NotNull final StreamingFuture<V2> second,
      @NotNull final StreamingFuture<V3> third,
      @NotNull final StreamingFuture<V4> fourth,
      @NotNull final StreamingFuture<V5> fifth,
      @NotNull final StreamingFuture<V6> sixth,
      @NotNull final StreamingFuture<V7> seventh,
      @NotNull final StreamingFuture<V8> eighth,
      @NotNull final StreamingFuture<V9> ninth,
      @NotNull final StreamingFuture<V10> tenth) { 
    return new DecupleFuture<V, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10>(
        Require.notNull(first, "first"),
        Require.notNull(second, "second"),
        Require.notNull(third, "third"),
        Require.notNull(fourth, "fourth"),
        Require.notNull(fifth, "fifth"),
        Require.notNull(sixth, "sixth"),
        Require.notNull(seventh, "seventh"),
        Require.notNull(eighth, "eighth"),
        Require.notNull(ninth, "ninth"),
        Require.notNull(tenth, "tenth")
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
  public @NotNull StreamingFuture<V9> getNinth() {
    return ninth;
  }

  @Override
  public @NotNull StreamingFuture<V10> getTenth() {
    return tenth;
  }

  @Override
  public @NotNull List<StreamingFuture<? extends V>> asList() {
    return futures;
  }

  @Override
  public @NotNull DecupleFuture<V, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10> readOnly() {
    return this;
  }

  @Override
  protected @NotNull DecupleFuture<V, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10> createPaused() {
    return new DecupleFuture<V, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10>(
        pauseFuture(getFirst()),
        pauseFuture(getSecond()),
        pauseFuture(getThird()),
        pauseFuture(getFourth()),
        pauseFuture(getFifth()),
        pauseFuture(getSixth()),
        pauseFuture(getSeventh()),
        pauseFuture(getEighth()),
        pauseFuture(getNinth()),
        pauseFuture(getTenth())
    );
  }

  @Override
  protected void resumePaused(@NotNull final DecupleFuture<V, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10> pausedFuture) {
    resumeFuture(pausedFuture.getFirst());
    resumeFuture(pausedFuture.getSecond());
    resumeFuture(pausedFuture.getThird());
    resumeFuture(pausedFuture.getFourth());
    resumeFuture(pausedFuture.getFifth());
    resumeFuture(pausedFuture.getSixth());
    resumeFuture(pausedFuture.getSeventh());
    resumeFuture(pausedFuture.getEighth());
    resumeFuture(pausedFuture.getNinth());
    resumeFuture(pausedFuture.getTenth());
  }
}