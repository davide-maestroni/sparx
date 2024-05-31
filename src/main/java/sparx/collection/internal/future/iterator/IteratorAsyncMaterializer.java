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
package sparx.collection.internal.future.iterator;

import org.jetbrains.annotations.NotNull;
import sparx.collection.internal.future.AsyncConsumer;
import sparx.collection.internal.future.IndexedAsyncConsumer;

public interface IteratorAsyncMaterializer<E> {

  boolean isCancelled();

  boolean isDone();

  int knownSize();

  void materializeCancel(boolean mayInterruptIfRunning);

  void materializeHasNext(@NotNull AsyncConsumer<Boolean> consumer);

  void materializeNext(@NotNull IndexedAsyncConsumer<E> consumer);

  void materializeSkip(int count, @NotNull AsyncConsumer<Integer> consumer);
}
