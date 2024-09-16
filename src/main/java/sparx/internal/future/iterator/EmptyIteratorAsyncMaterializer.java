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

import static sparx.internal.future.AsyncConsumers.safeConsume;
import static sparx.internal.future.AsyncConsumers.safeConsumeComplete;

import java.util.Iterator;
import java.util.concurrent.CancellationException;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.internal.future.AsyncConsumer;
import sparx.internal.future.IndexedAsyncConsumer;
import sparx.internal.future.IndexedAsyncPredicate;
import sparx.lazy;

public class EmptyIteratorAsyncMaterializer<E> implements IteratorAsyncMaterializer<E> {

  private static final EmptyIteratorAsyncMaterializer<?> INSTANCE = new EmptyIteratorAsyncMaterializer<Object>();
  private static final Logger LOGGER = Logger.getLogger(
      EmptyIteratorAsyncMaterializer.class.getName());

  @SuppressWarnings("unchecked")
  public static @NotNull <E> IteratorAsyncMaterializer<E> instance() {
    return (IteratorAsyncMaterializer<E>) INSTANCE;
  }

  @Override
  public boolean isCancelled() {
    return false;
  }

  @Override
  public boolean isDone() {
    return true;
  }

  @Override
  public boolean isFailed() {
    return false;
  }

  @Override
  public boolean isMaterializedAtOnce() {
    return true;
  }

  @Override
  public boolean isSucceeded() {
    return true;
  }

  @Override
  public int knownSize() {
    return 0;
  }

  @Override
  public void materializeCancel(@NotNull final CancellationException exception) {
  }

  @Override
  public void materializeElements(@NotNull final AsyncConsumer<Iterator<E>> consumer) {
    safeConsume(consumer, lazy.Iterator.<E>of(), LOGGER);
  }

  @Override
  public void materializeHasNext(@NotNull final AsyncConsumer<Boolean> consumer) {
    safeConsume(consumer, false, LOGGER);
  }

  @Override
  public void materializeNext(@NotNull final IndexedAsyncConsumer<E> consumer) {
    safeConsumeComplete(consumer, 0, LOGGER);
  }

  @Override
  public void materializeNextWhile(@NotNull final IndexedAsyncPredicate<E> predicate) {
    safeConsumeComplete(predicate, 0, LOGGER);
  }

  @Override
  public void materializeSkip(final int count, @NotNull final AsyncConsumer<Integer> consumer) {
    safeConsume(consumer, 0, LOGGER);
  }

  @Override
  public int weightElements() {
    return 1;
  }

  @Override
  public int weightHasNext() {
    return 1;
  }

  @Override
  public int weightNext() {
    return 1;
  }

  @Override
  public int weightNextWhile() {
    return 1;
  }

  @Override
  public int weightSkip(final int count) {
    return 1;
  }
}
