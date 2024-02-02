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
import sparx.tuple.Couple;
import sparx.util.ImmutableList;
import sparx.util.Require;

///////////////////////////////////////////////
// WARNING: GENERATED CODE - DO NOT MODIFY!! //
///////////////////////////////////////////////

public class CoupleFuture<V, V1 extends V, V2 extends V> extends
    StreamScopeTupleFuture<V, CoupleFuture<V, V1, V2>> implements
    Couple<StreamingFuture<? extends V>, StreamingFuture<V1>, StreamingFuture<V2>> {

  private final StreamingFuture<V1> first;
  private final StreamingFuture<V2> second;
  private final List<StreamingFuture<? extends V>> futures;

  private CoupleFuture(
      @NotNull final StreamingFuture<V1> first,
      @NotNull final StreamingFuture<V2> second) { 
    this.first = first;
    this.second = second;
    this.futures = ImmutableList.of(first, second);
  }

  public static @NotNull <V, V1 extends V, V2 extends V> CoupleFuture<V, V1, V2> of(
      @NotNull final StreamingFuture<V1> first,
      @NotNull final StreamingFuture<V2> second) { 
    return new CoupleFuture<V, V1, V2>(
        Require.notNull(first, "first"),
        Require.notNull(second, "second")
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
  public @NotNull List<StreamingFuture<? extends V>> asList() {
    return futures;
  }

  @Override
  public @NotNull CoupleFuture<V, V1, V2> readOnly() {
    return this;
  }

  @Override
  protected @NotNull CoupleFuture<V, V1, V2> createPaused() {
    return new CoupleFuture<V, V1, V2>(
        pauseFuture(getFirst()),
        pauseFuture(getSecond())
    );
  }

  @Override
  protected void resumePaused(@NotNull final CoupleFuture<V, V1, V2> pausedFuture) {
    resumeFuture(pausedFuture.getFirst());
    resumeFuture(pausedFuture.getSecond());
  }
}
