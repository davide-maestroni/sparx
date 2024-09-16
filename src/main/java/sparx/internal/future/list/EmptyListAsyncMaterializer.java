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

import static sparx.internal.future.AsyncConsumers.safeConsume;
import static sparx.internal.future.AsyncConsumers.safeConsumeComplete;
import static sparx.internal.future.AsyncConsumers.safeConsumeError;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.internal.future.AsyncConsumer;
import sparx.internal.future.IndexedAsyncConsumer;
import sparx.internal.future.IndexedAsyncPredicate;

public class EmptyListAsyncMaterializer<E> implements ListAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(EmptyListAsyncMaterializer.class.getName());

  private final List<E> elements;

  public EmptyListAsyncMaterializer(@NotNull final List<E> empty) {
    if (!empty.isEmpty()) {
      throw new IllegalArgumentException("'element' must be empty, but size is: " + empty.size());
    }
    this.elements = empty;
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
  public void materializeContains(final Object element,
      @NotNull final AsyncConsumer<Boolean> consumer) {
    safeConsume(consumer, false, LOGGER);
  }

  @Override
  public void materializeElement(final int index, @NotNull final IndexedAsyncConsumer<E> consumer) {
    if (index < 0) {
      safeConsumeError(consumer, new IndexOutOfBoundsException(Integer.toString(index)), LOGGER);
    } else {
      safeConsumeComplete(consumer, 0, LOGGER);
    }
  }

  @Override
  public void materializeElements(@NotNull final AsyncConsumer<List<E>> consumer) {
    safeConsume(consumer, elements, LOGGER);
  }

  @Override
  public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
    safeConsume(consumer, true, LOGGER);
  }

  @Override
  public void materializeHasElement(final int index,
      @NotNull final AsyncConsumer<Boolean> consumer) {
    safeConsume(consumer, false, LOGGER);
  }

  @Override
  public void materializeNextWhile(final int index,
      @NotNull final IndexedAsyncPredicate<E> predicate) {
    safeConsumeComplete(predicate, 0, LOGGER);
  }

  @Override
  public void materializePrevWhile(final int index,
      @NotNull final IndexedAsyncPredicate<E> predicate) {
    safeConsumeComplete(predicate, 0, LOGGER);
  }

  @Override
  public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
    safeConsume(consumer, 0, LOGGER);
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
