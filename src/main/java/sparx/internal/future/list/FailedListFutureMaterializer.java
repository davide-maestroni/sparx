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

import static sparx.internal.future.FutureConsumers.safeConsumeError;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.internal.future.FutureConsumer;
import sparx.internal.future.IndexedFutureConsumer;
import sparx.internal.future.IndexedFuturePredicate;

public class FailedListFutureMaterializer<E> implements ListFutureMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      FailedListFutureMaterializer.class.getName());

  private final Exception error;

  public FailedListFutureMaterializer(@NotNull final Exception error) {
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
  public void materializeContains(final Object element,
      @NotNull final FutureConsumer<Boolean> consumer) {
    safeConsumeError(consumer, error, LOGGER);
  }

  @Override
  public void materializeElement(final int ignored,
      @NotNull final IndexedFutureConsumer<E> consumer) {
    safeConsumeError(consumer, error, LOGGER);
  }

  @Override
  public void materializeElements(@NotNull final FutureConsumer<List<E>> consumer) {
    safeConsumeError(consumer, error, LOGGER);
  }

  @Override
  public void materializeEmpty(@NotNull final FutureConsumer<Boolean> consumer) {
    safeConsumeError(consumer, error, LOGGER);
  }

  @Override
  public void materializeHasElement(final int index,
      @NotNull final FutureConsumer<Boolean> consumer) {
    safeConsumeError(consumer, error, LOGGER);
  }

  @Override
  public void materializeNextWhile(final int index,
      @NotNull final IndexedFuturePredicate<E> predicate) {
    safeConsumeError(predicate, error, LOGGER);
  }

  @Override
  public void materializePrevWhile(final int index,
      @NotNull final IndexedFuturePredicate<E> predicate) {
    safeConsumeError(predicate, error, LOGGER);
  }

  @Override
  public void materializeSize(@NotNull final FutureConsumer<Integer> consumer) {
    safeConsumeError(consumer, error, LOGGER);
  }

  @Override
  public int weightContains() {
    return 1;
  }

  @Override
  public int weightElement() {
    return 1;
  }

  @Override
  public int weightElements() {
    return 1;
  }

  @Override
  public int weightEmpty() {
    return 1;
  }

  @Override
  public int weightHasElement() {
    return 1;
  }

  @Override
  public int weightNextWhile() {
    return 1;
  }

  @Override
  public int weightPrevWhile() {
    return 1;
  }

  @Override
  public int weightSize() {
    return 1;
  }
}
