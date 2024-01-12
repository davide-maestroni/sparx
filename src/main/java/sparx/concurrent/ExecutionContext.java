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

import org.jetbrains.annotations.NotNull;
import sparx.function.Consumer;
import sparx.function.Function;
import sparx.util.Nothing;

public interface ExecutionContext {

  int minThroughput();

  int pendingCount();

  @NotNull <V, F extends TupleFuture<V, ?>, U> StreamingFuture<U> call(@NotNull F future,
      @NotNull Function<F, ? extends SignalFuture<U>> function, int weight);

  @NotNull <V, F extends TupleFuture<V, ?>> StreamingFuture<Nothing> run(@NotNull F future,
      @NotNull Consumer<F> consumer, int weight);
}
