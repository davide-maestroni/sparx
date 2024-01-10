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
package sparx.concurrent;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import sparx.tuple.Undecuple;
import sparx.util.ImmutableList;
import sparx.util.Requires;

///////////////////////////////////////////////
// WARNING: GENERATED CODE - DO NOT MODIFY!! //
///////////////////////////////////////////////

public class UndecupleFuture<V, V1 extends V, V2 extends V, V3 extends V, V4 extends V, V5 extends V, V6 extends V, V7 extends V, V8 extends V, V9 extends V, V10 extends V, V11 extends V> extends
    TupleStreamGroupFuture<V, UndecupleFuture<V, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11>> implements
    Undecuple<StreamingFuture<? extends V>, StreamingFuture<V1>, StreamingFuture<V2>, StreamingFuture<V3>, StreamingFuture<V4>, StreamingFuture<V5>, StreamingFuture<V6>, StreamingFuture<V7>, StreamingFuture<V8>, StreamingFuture<V9>, StreamingFuture<V10>, StreamingFuture<V11>> {

  public static @NotNull <V, V1 extends V, V2 extends V, V3 extends V, V4 extends V, V5 extends V, V6 extends V, V7 extends V, V8 extends V, V9 extends V, V10 extends V, V11 extends V> UndecupleFuture<V, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11> of(
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
      @NotNull final StreamingFuture<V11> eleventh) { 
    return new UndecupleFuture<V, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11>(
        Requires.notNull(first, "first"),
        Requires.notNull(second, "second"),
        Requires.notNull(third, "third"),
        Requires.notNull(fourth, "fourth"),
        Requires.notNull(fifth, "fifth"),
        Requires.notNull(sixth, "sixth"),
        Requires.notNull(seventh, "seventh"),
        Requires.notNull(eighth, "eighth"),
        Requires.notNull(ninth, "ninth"),
        Requires.notNull(tenth, "tenth"),
        Requires.notNull(eleventh, "eleventh")
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
  private final List<StreamingFuture<? extends V>> futures;

  private UndecupleFuture(
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
      @NotNull final StreamingFuture<V11> eleventh) { 
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
    this.futures = ImmutableList.of(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth, eleventh);
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
  public @NotNull List<StreamingFuture<? extends V>> asList() {
    return futures;
  }

  @Override
  public @NotNull UndecupleFuture<V, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11> readOnly() {
    return this;
  }

  @Override
  protected @NotNull UndecupleFuture<V, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11> createFuture() {
    return new UndecupleFuture<V, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11>(
        new VarFuture<V1>(),
        new VarFuture<V2>(),
        new VarFuture<V3>(),
        new VarFuture<V4>(),
        new VarFuture<V5>(),
        new VarFuture<V6>(),
        new VarFuture<V7>(),
        new VarFuture<V8>(),
        new VarFuture<V9>(),
        new VarFuture<V10>(),
        new VarFuture<V11>()
    );
  }

  @Override
  protected void subscribeFuture(@NotNull final UndecupleFuture<V, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11> future) {
    getFirst().subscribe(future.getFirst());
    getSecond().subscribe(future.getSecond());
    getThird().subscribe(future.getThird());
    getFourth().subscribe(future.getFourth());
    getFifth().subscribe(future.getFifth());
    getSixth().subscribe(future.getSixth());
    getSeventh().subscribe(future.getSeventh());
    getEighth().subscribe(future.getEighth());
    getNinth().subscribe(future.getNinth());
    getTenth().subscribe(future.getTenth());
    getEleventh().subscribe(future.getEleventh());
  }
}
