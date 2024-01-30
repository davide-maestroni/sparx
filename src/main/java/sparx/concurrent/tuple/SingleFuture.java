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

import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.StreamingFuture;
import sparx.tuple.Single;
import sparx.util.Require;

public class SingleFuture<V> extends StreamScopeTupleFuture<V, SingleFuture<V>> implements
    Single<StreamingFuture<? extends V>> {

  private final StreamingFuture<V> first;
  private final List<StreamingFuture<? extends V>> futures;

  private SingleFuture(@NotNull final StreamingFuture<V> first) {
    this.first = first;
    this.futures = Collections.<StreamingFuture<? extends V>>singletonList(first);
  }

  public static @NotNull <V> SingleFuture<V> of(@NotNull final StreamingFuture<V> first) {
    return new SingleFuture<V>(Require.notNull(first, "first"));
  }

  @Override
  public @NotNull StreamingFuture<V> getFirst() {
    return first;
  }

  @Override
  public @NotNull List<StreamingFuture<? extends V>> asList() {
    return futures;
  }

  @Override
  public @NotNull SingleFuture<V> readOnly() {
    return this;
  }

  @Override
  protected @NotNull SingleFuture<V> createProxy() {
    return new SingleFuture<V>(proxyFuture(getFirst()));
  }

  @Override
  protected void subscribeProxy(@NotNull final SingleFuture<V> proxyFuture) {
    connectProxy(proxyFuture.getFirst());
  }
}
