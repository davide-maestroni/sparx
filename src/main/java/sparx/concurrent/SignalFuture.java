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
import sparx.function.Action;
import sparx.function.Consumer;
import sparx.function.Function;
import sparx.util.LiveIterator;

public interface SignalFuture<V> extends Future<List<V>>, Receiver<V>, Iterable<V> {

  // TODO:
  //  generator?

  void clear();

  void compute(@NotNull Function<? super V, ? extends V> function);

  V getCurrent();

  V getCurrentOr(V defaultValue);

  boolean isReadOnly();

  @NotNull SignalFuture<V> readOnly();

  void setBulk(@NotNull V... values);

  @NotNull Subscription subscribe(@NotNull Receiver<? super V> receiver);

  @NotNull Subscription subscribe(@Nullable Consumer<? super V> onValueConsumer,
      @Nullable Consumer<? super Collection<V>> onValuesConsumer,
      @Nullable Consumer<Exception> onErrorConsumer,
      @Nullable Action onCloseAction);

  void unsubscribe(@NotNull Receiver<?> receiver);

  @Override
  @NotNull LiveIterator<V> iterator();

  @NotNull LiveIterator<V> iterator(long timeout, @NotNull TimeUnit unit);

  interface Subscription {

    void cancel();
  }
}
