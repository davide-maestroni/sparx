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

import static sparx.internal.future.FutureConsumers.safeConsumeError;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.internal.future.FutureConsumer;
import sparx.internal.future.IndexedFutureConsumer;
import sparx.internal.future.IndexedFuturePredicate;

public class FailedIteratorFutureMaterializer<E> implements IteratorFutureMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      FailedIteratorFutureMaterializer.class.getName());

  private final Exception error;

  public FailedIteratorFutureMaterializer(@NotNull final Exception error) {
    this.error = error;
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
    return true;
  }

  @Override
  public boolean isMaterializedAtOnce() {
    return true;
  }

  @Override
  public boolean isSucceeded() {
    return false;
  }

  @Override
  public int knownSize() {
    return -1;
  }

  @Override
  public void materializeCancel(@NotNull final CancellationException exception) {
  }

  @Override
  public void materializeElements(@NotNull final FutureConsumer<List<E>> consumer) {
    safeConsumeError(consumer, error, LOGGER);
  }

  @Override
  public void materializeHasNext(@NotNull final FutureConsumer<Boolean> consumer) {
    safeConsumeError(consumer, error, LOGGER);
  }

  @Override
  public void materializeIterator(@NotNull final FutureConsumer<Iterator<E>> consumer) {
    safeConsumeError(consumer, error, LOGGER);
  }

  @Override
  public void materializeNext(@NotNull final IndexedFutureConsumer<E> consumer) {
    safeConsumeError(consumer, error, LOGGER);
  }

  @Override
  public void materializeNextWhile(@NotNull final IndexedFuturePredicate<E> predicate) {
    safeConsumeError(predicate, error, LOGGER);
  }

  @Override
  public void materializeSkip(final int count, @NotNull final FutureConsumer<Integer> consumer) {
    safeConsumeError(consumer, error, LOGGER);
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
  public int weightSkip() {
    return 1;
  }
}
