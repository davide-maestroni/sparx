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
import sparx0.util.tuple.Novemdecuple;

///////////////////////////////////////////////
// WARNING: GENERATED CODE - DO NOT MODIFY!!
// - templating engine: Mustache 0.9.11
// - template path: template/concurrent/TupleFuture.mustache
///////////////////////////////////////////////

public class NovemdecupleFuture<V, V1 extends V, V2 extends V, V3 extends V, V4 extends V, V5 extends V, V6 extends V, V7 extends V, V8 extends V, V9 extends V, V10 extends V, V11 extends V, V12 extends V, V13 extends V, V14 extends V, V15 extends V, V16 extends V, V17 extends V, V18 extends V, V19 extends V> extends
    StreamScopeTupleFuture<V, NovemdecupleFuture<V, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16, V17, V18, V19>> implements
    Novemdecuple<StreamingFuture<? extends V>, StreamingFuture<V1>, StreamingFuture<V2>, StreamingFuture<V3>, StreamingFuture<V4>, StreamingFuture<V5>, StreamingFuture<V6>, StreamingFuture<V7>, StreamingFuture<V8>, StreamingFuture<V9>, StreamingFuture<V10>, StreamingFuture<V11>, StreamingFuture<V12>, StreamingFuture<V13>, StreamingFuture<V14>, StreamingFuture<V15>, StreamingFuture<V16>, StreamingFuture<V17>, StreamingFuture<V18>, StreamingFuture<V19>> {

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
  private final StreamingFuture<V16> sixteenth;
  private final StreamingFuture<V17> seventeenth;
  private final StreamingFuture<V18> eighteenth;
  private final StreamingFuture<V19> nineteenth;
  private final List<StreamingFuture<? extends V>> futures;

  private NovemdecupleFuture(
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
      @NotNull final StreamingFuture<V15> fifteenth,
      @NotNull final StreamingFuture<V16> sixteenth,
      @NotNull final StreamingFuture<V17> seventeenth,
      @NotNull final StreamingFuture<V18> eighteenth,
      @NotNull final StreamingFuture<V19> nineteenth) { 
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
    this.sixteenth = sixteenth;
    this.seventeenth = seventeenth;
    this.eighteenth = eighteenth;
    this.nineteenth = nineteenth;
    this.futures = ImmutableList.of(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh, twelfth, thirteenth, fourteenth, fifteenth, sixteenth, seventeenth, eighteenth, nineteenth);
  }

  public static @NotNull <V, V1 extends V, V2 extends V, V3 extends V, V4 extends V, V5 extends V, V6 extends V, V7 extends V, V8 extends V, V9 extends V, V10 extends V, V11 extends V, V12 extends V, V13 extends V, V14 extends V, V15 extends V, V16 extends V, V17 extends V, V18 extends V, V19 extends V> NovemdecupleFuture<V, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16, V17, V18, V19> of(
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
      @NotNull final StreamingFuture<V15> fifteenth,
      @NotNull final StreamingFuture<V16> sixteenth,
      @NotNull final StreamingFuture<V17> seventeenth,
      @NotNull final StreamingFuture<V18> eighteenth,
      @NotNull final StreamingFuture<V19> nineteenth) { 
    return new NovemdecupleFuture<V, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16, V17, V18, V19>(
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
        Require.notNull(fifteenth, "fifteenth"),
        Require.notNull(sixteenth, "sixteenth"),
        Require.notNull(seventeenth, "seventeenth"),
        Require.notNull(eighteenth, "eighteenth"),
        Require.notNull(nineteenth, "nineteenth")
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
  public @NotNull StreamingFuture<V16> getSixteenth() {
    return sixteenth;
  }


  @Override
  public @NotNull StreamingFuture<V17> getSeventeenth() {
    return seventeenth;
  }


  @Override
  public @NotNull StreamingFuture<V18> getEighteenth() {
    return eighteenth;
  }

  @Override
  public @NotNull StreamingFuture<V19> getNineteenth() {
    return nineteenth;
  }

  @Override
  public @NotNull List<StreamingFuture<? extends V>> asList() {
    return futures;
  }

  @Override
  public @NotNull NovemdecupleFuture<V, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16, V17, V18, V19> readOnly() {
    return this;
  }

  @Override
  protected @NotNull NovemdecupleFuture<V, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16, V17, V18, V19> createPaused() {
    return new NovemdecupleFuture<V, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16, V17, V18, V19>(
        pauseFuture(getFirst()),
        pauseFuture(getSecond()),
        pauseFuture(getThird()),
        pauseFuture(getFourth()),
        pauseFuture(getFifth()),
        pauseFuture(getSixth()),
        pauseFuture(getSeventh()),
        pauseFuture(getEighth()),
        pauseFuture(getNinth()),
        pauseFuture(getTenth()),
        pauseFuture(getEleventh()),
        pauseFuture(getTwelfth()),
        pauseFuture(getThirteenth()),
        pauseFuture(getFourteenth()),
        pauseFuture(getFifteenth()),
        pauseFuture(getSixteenth()),
        pauseFuture(getSeventeenth()),
        pauseFuture(getEighteenth()),
        pauseFuture(getNineteenth())
    );
  }

  @Override
  protected void resumePaused(@NotNull final NovemdecupleFuture<V, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11, V12, V13, V14, V15, V16, V17, V18, V19> pausedFuture) {
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
    resumeFuture(pausedFuture.getEleventh());
    resumeFuture(pausedFuture.getTwelfth());
    resumeFuture(pausedFuture.getThirteenth());
    resumeFuture(pausedFuture.getFourteenth());
    resumeFuture(pausedFuture.getFifteenth());
    resumeFuture(pausedFuture.getSixteenth());
    resumeFuture(pausedFuture.getSeventeenth());
    resumeFuture(pausedFuture.getEighteenth());
    resumeFuture(pausedFuture.getNineteenth());
  }
}