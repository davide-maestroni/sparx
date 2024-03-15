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
package sparx0;

import org.jetbrains.annotations.NotNull;
import sparx0.concurrent.Receiver;
import sparx0.concurrent.StreamingFuture;
import sparx0.concurrent.VarFuture;
import sparx0.concurrent.history.SignalHistory;
import sparx0.concurrent.tuple.CoupleFuture;
import sparx0.concurrent.tuple.SingleFuture;
import sparx0.concurrent.tuple.TripleFuture;
import sparx0.util.function.BinaryFunction;
import sparx0.util.function.Consumer;

public class Sparx {

  public static @NotNull <V> StreamingFuture<V> future() {
    return VarFuture.create();
  }

  public static @NotNull <V> StreamingFuture<V> future(@NotNull final SignalHistory<V> history) {
    return VarFuture.create(history);
  }

  public static @NotNull <V> SingleFuture<V> tupleFuture(@NotNull StreamingFuture<V> first) {
    return SingleFuture.of(first);
  }

  public static @NotNull <V, V1 extends V, V2 extends V> CoupleFuture<V, V1, V2> tupleFuture(
      @NotNull StreamingFuture<V1> first, @NotNull StreamingFuture<V2> second) {
    return CoupleFuture.of(first, second);
  }

  public static @NotNull <V, V1 extends V, V2 extends V, V3 extends V> TripleFuture<V, V1, V2, V3> tupleFuture(
      @NotNull StreamingFuture<V1> first, @NotNull StreamingFuture<V2> second,
      @NotNull StreamingFuture<V3> third) {
    return TripleFuture.of(first, second, third);
  }

  public static @NotNull <V> StreamingFuture<V> generator(
      @NotNull Consumer<? super Receiver<V>> consumer) {
    return null;
  }

  public static @NotNull <V> StreamingFuture<V> generator(V initialValue,
      @NotNull BinaryFunction<? super V, ? super Receiver<V>, V> consumer) {
    return null;
  }
}