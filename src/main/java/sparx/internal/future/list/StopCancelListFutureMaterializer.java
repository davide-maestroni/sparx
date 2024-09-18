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
import java.util.concurrent.CancellationException;
import org.jetbrains.annotations.NotNull;
import sparx.internal.future.FutureConsumer;
import sparx.internal.future.IndexedFutureConsumer;
import sparx.internal.future.IndexedFuturePredicate;

public class StopCancelListFutureMaterializer<E> implements ListFutureMaterializer<E> {

  private final ListFutureMaterializer<E> wrapped;

  public StopCancelListFutureMaterializer(@NotNull final ListFutureMaterializer<E> wrapped) {
    this.wrapped = wrapped;
  }

  @Override
  public boolean isCancelled() {
    return wrapped.isCancelled();
  }

  @Override
  public boolean isDone() {
    return wrapped.isDone();
  }

  @Override
  public boolean isFailed() {
    return wrapped.isFailed();
  }

  @Override
  public boolean isSucceeded() {
    return wrapped.isSucceeded();
  }

  @Override
  public boolean isMaterializedAtOnce() {
    return wrapped.isMaterializedAtOnce();
  }

  @Override
  public int knownSize() {
    return wrapped.knownSize();
  }

  @Override
  public void materializeCancel(@NotNull final CancellationException exception) {
  }

  @Override
  public void materializeContains(final Object element,
      @NotNull final FutureConsumer<Boolean> consumer) {
    wrapped.materializeContains(element, consumer);
  }

  @Override
  public void materializeElement(final int index,
      @NotNull final IndexedFutureConsumer<E> consumer) {
    wrapped.materializeElement(index, consumer);
  }

  @Override
  public void materializeElements(@NotNull final FutureConsumer<List<E>> consumer) {
    wrapped.materializeElements(consumer);
  }

  @Override
  public void materializeEmpty(@NotNull final FutureConsumer<Boolean> consumer) {
    wrapped.materializeEmpty(consumer);
  }

  @Override
  public void materializeHasElement(final int index,
      @NotNull final FutureConsumer<Boolean> consumer) {
    wrapped.materializeHasElement(index, consumer);
  }

  @Override
  public void materializeNextWhile(final int index,
      @NotNull final IndexedFuturePredicate<E> predicate) {
    wrapped.materializeNextWhile(index, predicate);
  }

  @Override
  public void materializePrevWhile(final int index,
      @NotNull final IndexedFuturePredicate<E> predicate) {
    wrapped.materializePrevWhile(index, predicate);
  }

  @Override
  public void materializeSize(@NotNull final FutureConsumer<Integer> consumer) {
    wrapped.materializeSize(consumer);
  }

  @Override
  public int weightContains() {
    return wrapped.weightContains();
  }

  @Override
  public int weightElement() {
    return wrapped.weightElement();
  }

  @Override
  public int weightElements() {
    return wrapped.weightElements();
  }

  @Override
  public int weightEmpty() {
    return wrapped.weightEmpty();
  }

  @Override
  public int weightHasElement() {
    return wrapped.weightHasElement();
  }

  @Override
  public int weightNextWhile() {
    return wrapped.weightNextWhile();
  }

  @Override
  public int weightPrevWhile() {
    return wrapped.weightPrevWhile();
  }

  @Override
  public int weightSize() {
    return wrapped.weightSize();
  }
}
