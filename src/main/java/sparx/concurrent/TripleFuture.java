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
///////////////////////////////////////////////
// WARNING: GENERATED CODE - DO NOT MODIFY!! //
///////////////////////////////////////////////
package sparx.concurrent;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import sparx.tuple.Triple;
import sparx.util.ImmutableList;
import sparx.util.Requires;

public class TripleFuture<V, V1 extends V, V2 extends V, V3 extends V> extends
    TupleStreamGroupFuture<V, TripleFuture<V, V1, V2, V3>> implements
    Triple<StreamingFuture<? extends V>, StreamingFuture<V1>, StreamingFuture<V2>, StreamingFuture<V3>> {

  public static @NotNull <V, V1 extends V, V2 extends V, V3 extends V> TripleFuture<V, V1, V2, V3> of(
      @NotNull final StreamingFuture<V1> first,
      @NotNull final StreamingFuture<V2> second,
      @NotNull final StreamingFuture<V3> third) { 
    return new TripleFuture<V, V1, V2, V3>(
        Requires.notNull(first, "first"),
        Requires.notNull(second, "second"),
        Requires.notNull(third, "third"));
  }

  private final StreamingFuture<V1> first;
  private final StreamingFuture<V2> second;
  private final StreamingFuture<V3> third;
  private final List<StreamingFuture<? extends V>> futures;

  @SuppressWarnings("unchecked")
  private TripleFuture(
      @NotNull final StreamingFuture<V1> first,
      @NotNull final StreamingFuture<V2> second,
      @NotNull final StreamingFuture<V3> third) { 
    this.first = first;
    this.second = second;
    this.third = third;
    this.futures = ImmutableList.of(first, second, third);
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
  public @NotNull List<StreamingFuture<? extends V>> asList() {
    return futures;
  }

  @Override
  public @NotNull TripleFuture<V, V1, V2, V3> readOnly() {
    return this;
  }
}
