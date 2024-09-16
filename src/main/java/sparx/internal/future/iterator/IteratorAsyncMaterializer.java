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
package sparx.internal.future.iterator;

import java.util.Iterator;
import java.util.concurrent.CancellationException;
import org.jetbrains.annotations.NotNull;
import sparx.internal.future.AsyncConsumer;
import sparx.internal.future.IndexedAsyncConsumer;
import sparx.internal.future.IndexedAsyncPredicate;

public interface IteratorAsyncMaterializer<E> {

  boolean isCancelled();

  boolean isDone();

  boolean isFailed();

  boolean isSucceeded();

  boolean isMaterializedAtOnce();

  int knownSize();

  void materializeCancel(@NotNull CancellationException exception);

  void materializeElements(@NotNull AsyncConsumer<Iterator<E>> consumer);

  void materializeHasNext(@NotNull AsyncConsumer<Boolean> consumer);

  void materializeNext(@NotNull IndexedAsyncConsumer<E> consumer);

  void materializeNextWhile(@NotNull IndexedAsyncPredicate<E> predicate);

  void materializeSkip(int count, @NotNull AsyncConsumer<Integer> consumer);

  int weightElements();

  int weightHasNext();

  int weightNext();

  int weightNextWhile();

  int weightSkip(int count);
}
