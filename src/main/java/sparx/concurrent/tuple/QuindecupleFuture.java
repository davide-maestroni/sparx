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
import sparx.tuple.Quindecuple;
import sparx.util.ImmutableList;
import sparx.util.Require;

///////////////////////////////////////////////
// WARNING: GENERATED CODE - DO NOT MODIFY!! //
///////////////////////////////////////////////

public class QuindecupleFuture<V, V1 extends V, V2 extends V, V3 extends V, V4 extends V, V5 extends V, V6 extends V, V7 extends V, V8 extends V, V9 extends V, V10 extends V, V11 extends V, V12 extends V, V13 extends V, V14 extends V, V15 extends V> extends
    StreamContextTupleFuture<V, QuindecupleFuture<V, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15>> implements
    Quindecuple<StreamingFuture<? extends V>, StreamingFuture<V1>, StreamingFuture<V2>, StreamingFuture<V3>, StreamingFuture<V4>, StreamingFuture<V5>, StreamingFuture<V6>, StreamingFuture<V7>, StreamingFuture<V8>, StreamingFuture<V9>, StreamingFuture<V10>, StreamingFuture<V11>, StreamingFuture<V12>, StreamingFuture<V13>, StreamingFuture<V14>, StreamingFuture<V15>> {

  public static @NotNull <V, V1 extends V, V2 extends V, V3 extends V, V4 extends V, V5 extends V, V6 extends V, V7 extends V, V8 extends V, V9 extends V, V10 extends V, V11 extends V, V12 extends V, V13 extends V, V14 extends V, V15 extends V> QuindecupleFuture<V, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15> of(
      @NotNull final StreamingFuture<V1> first,
      @NotNull final StreamingFuture<V2> second,
      @NotNull final StreamingFuture<V3> third,
      @NotNull final StreamingFuture<V4> fourth,
      @NotNull final StreamingFuture<V5> fifth,
      @NotNull final StreamingFuture<V6> sixth,
      @NotNull final StreamingFuture<V7> seventh,
      @NotNull final StreamingFuture<V8> eighth,
      @NotNull final StreamingFuture<V9> ninth,
      @NotNull final StreamingFuture<V10> tenth,
      @NotNull final StreamingFuture<V11> eleventh,
      @NotNull final StreamingFuture<V12> twelfth,
      @NotNull final StreamingFuture<V13> thirteenth,
      @NotNull final StreamingFuture<V14> fourteenth,
      @NotNull final StreamingFuture<V15> fifteenth) { 
    return new QuindecupleFuture<V, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15>(
        Require.notNull(first, "first"),
        Require.notNull(second, "second"),
        Require.notNull(third, "third"),
        Require.notNull(fourth, "fourth"),
        Require.notNull(fifth, "fifth"),
        Require.notNull(sixth, "sixth"),
        Require.notNull(seventh, "seventh"),
        Require.notNull(eighth, "eighth"),
        Require.notNull(ninth, "ninth"),
        Require.notNull(tenth, "tenth"),
        Require.notNull(eleventh, "eleventh"),
        Require.notNull(twelfth, "twelfth"),
        Require.notNull(thirteenth, "thirteenth"),
        Require.notNull(fourteenth, "fourteenth"),
        Require.notNull(fifteenth, "fifteenth")
    );
  }

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
  private final StreamingFuture<V11> eleventh;
  private final StreamingFuture<V12> twelfth;
  private final StreamingFuture<V13> thirteenth;
  private final StreamingFuture<V14> fourteenth;
  private final StreamingFuture<V15> fifteenth;
  private final List<StreamingFuture<? extends V>> futures;

  private QuindecupleFuture(
      @NotNull final StreamingFuture<V1> first,
      @NotNull final StreamingFuture<V2> second,
      @NotNull final StreamingFuture<V3> third,
      @NotNull final StreamingFuture<V4> fourth,
      @NotNull final StreamingFuture<V5> fifth,
      @NotNull final StreamingFuture<V6> sixth,
      @NotNull final StreamingFuture<V7> seventh,
      @NotNull final StreamingFuture<V8> eighth,
      @NotNull final StreamingFuture<V9> ninth,
      @NotNull final StreamingFuture<V10> tenth,
      @NotNull final StreamingFuture<V11> eleventh,
      @NotNull final StreamingFuture<V12> twelfth,
      @NotNull final StreamingFuture<V13> thirteenth,
      @NotNull final StreamingFuture<V14> fourteenth,
      @NotNull final StreamingFuture<V15> fifteenth) { 
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
    this.eleventh = eleventh;
    this.twelfth = twelfth;
    this.thirteenth = thirteenth;
    this.fourteenth = fourteenth;
    this.fifteenth = fifteenth;
    this.futures = ImmutableList.of(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth);
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
  public @NotNull StreamingFuture<V11> getEleventh() {
    return eleventh;
  }


  @Override
  public @NotNull StreamingFuture<V12> getTwelfth() {
    return twelfth;
  }


  @Override
  public @NotNull StreamingFuture<V13> getThirteenth() {
    return thirteenth;
  }


  @Override
  public @NotNull StreamingFuture<V14> getFourteenth() {
    return fourteenth;
  }

  @Override
  public @NotNull StreamingFuture<V15> getFifteenth() {
    return fifteenth;
  }

  @Override
  public @NotNull List<StreamingFuture<? extends V>> asList() {
    return futures;
  }

  @Override
  public @NotNull QuindecupleFuture<V, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15> readOnly() {
    return this;
  }

  @Override
  protected @NotNull QuindecupleFuture<V, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15> createProxy() {
    return new QuindecupleFuture<V, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15>(
        proxyFuture(getFirst()),
        proxyFuture(getSecond()),
        proxyFuture(getThird()),
        proxyFuture(getFourth()),
        proxyFuture(getFifth()),
        proxyFuture(getSixth()),
        proxyFuture(getSeventh()),
        proxyFuture(getEighth()),
        proxyFuture(getNinth()),
        proxyFuture(getTenth()),
        proxyFuture(getEleventh()),
        proxyFuture(getTwelfth()),
        proxyFuture(getThirteenth()),
        proxyFuture(getFourteenth()),
        proxyFuture(getFifteenth())
    );
  }

  @Override
  protected void subscribeProxy(@NotNull final QuindecupleFuture<V, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15> proxyFuture) {
    connectProxy(proxyFuture.getFirst());
    connectProxy(proxyFuture.getSecond());
    connectProxy(proxyFuture.getThird());
    connectProxy(proxyFuture.getFourth());
    connectProxy(proxyFuture.getFifth());
    connectProxy(proxyFuture.getSixth());
    connectProxy(proxyFuture.getSeventh());
    connectProxy(proxyFuture.getEighth());
    connectProxy(proxyFuture.getNinth());
    connectProxy(proxyFuture.getTenth());
    connectProxy(proxyFuture.getEleventh());
    connectProxy(proxyFuture.getTwelfth());
    connectProxy(proxyFuture.getThirteenth());
    connectProxy(proxyFuture.getFourteenth());
    connectProxy(proxyFuture.getFifteenth());
  }
}
