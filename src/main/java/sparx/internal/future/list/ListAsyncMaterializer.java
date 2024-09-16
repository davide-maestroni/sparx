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
package sparx.internal.future.list;

import java.util.List;
import org.jetbrains.annotations.NotNull;
import sparx.internal.future.AsyncConsumer;
import sparx.internal.future.CollectionAsyncMaterializer;
import sparx.internal.future.IndexedAsyncConsumer;
import sparx.internal.future.IndexedAsyncPredicate;
import sparx.util.annotation.NotNegative;

public interface ListAsyncMaterializer<E> extends CollectionAsyncMaterializer<E> {

  void materializeElement(int index, @NotNull IndexedAsyncConsumer<E> consumer);

  void materializeElements(@NotNull AsyncConsumer<List<E>> consumer);

  void materializeHasElement(int index, @NotNull AsyncConsumer<Boolean> consumer);

  void materializeNextWhile(@NotNegative int index, @NotNull IndexedAsyncPredicate<E> predicate);

  void materializePrevWhile(@NotNegative int index, @NotNull IndexedAsyncPredicate<E> predicate);

  int weightElement();

  int weightElements();

  int weightHasElement();

  int weightNextWhile();

  int weightPrevWhile();
}
