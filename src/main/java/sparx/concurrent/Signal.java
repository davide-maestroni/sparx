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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sparx.function.Action;
import sparx.function.Consumer;

public interface Signal<V> {

  V getCurrent();

  V getCurrentOr(V defaultValue);

  @NotNull Subscription subscribe(@NotNull Receiver<? super V> receiver);

  @NotNull Subscription subscribe(@Nullable Consumer<? super V> onValueConsumer,
      @Nullable Consumer<? super Collection<V>> onValuesConsumer,
      @Nullable Consumer<Exception> onErrorConsumer,
      @Nullable Action onCloseAction);

  void unsubscribe(@NotNull Receiver<?> receiver);

  interface Subscription {

    void cancel();
  }
}