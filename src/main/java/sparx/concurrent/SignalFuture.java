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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sparx.util.function.Action;
import sparx.util.function.Consumer;
import sparx.util.function.Function;

public interface SignalFuture<V> extends Future<List<V>>, Iterable<V>, Receiver<V>, Signal<V> {

  void clear();

  void compute(@NotNull Function<? super V, ? extends V> function);

  boolean isReadOnly();

  @Override
  @NotNull LiveIterator<V> iterator();

  @NotNull LiveIterator<V> iterator(long timeout, @NotNull TimeUnit unit);

  @NotNull LiveIterator<V> iteratorNext();

  @NotNull LiveIterator<V> iteratorNext(long timeout, @NotNull TimeUnit unit);

  @NotNull SignalFuture<V> readOnly();

  void setBulk(@Nullable V... values);

  // TODO: subscribe leak future if it fails or is closed

  @NotNull Subscription subscribe(@Nullable Consumer<? super V> onValueConsumer,
      @Nullable Consumer<? super Collection<V>> onBulkConsumer,
      @Nullable Consumer<Exception> onErrorConsumer,
      @Nullable Action onCloseAction);

  @NotNull Subscription subscribeNext(@Nullable Consumer<? super V> onValueConsumer,
      @Nullable Consumer<? super Collection<V>> onBulkConsumer,
      @Nullable Consumer<Exception> onErrorConsumer,
      @Nullable Action onCloseAction);
}
