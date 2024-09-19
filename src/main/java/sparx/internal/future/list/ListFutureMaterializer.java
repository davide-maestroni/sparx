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
import sparx.internal.future.CollectionFutureMaterializer;
import sparx.internal.future.FutureConsumer;
import sparx.internal.future.IndexedFutureConsumer;
import sparx.internal.future.IndexedFuturePredicate;
import sparx.util.annotation.NotNegative;

public interface ListFutureMaterializer<E> extends CollectionFutureMaterializer {

  void materializeElement(int index, @NotNull IndexedFutureConsumer<E> consumer);

  void materializeElements(@NotNull FutureConsumer<List<E>> consumer);

  void materializeHasElement(int index, @NotNull FutureConsumer<Boolean> consumer);

  void materializeNextWhile(@NotNegative int index, @NotNull IndexedFuturePredicate<E> predicate);

  void materializePrevWhile(@NotNegative int index, @NotNull IndexedFuturePredicate<E> predicate);

  int weightElement();

  int weightElements();

  int weightHasElement();

  int weightNextWhile();

  int weightPrevWhile();
}