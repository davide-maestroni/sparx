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
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import sparx.internal.future.AsyncConsumer;
import sparx.internal.future.IndexedAsyncConsumer;

abstract class AbstractListAsyncMaterializer<E> implements ListAsyncMaterializer<E> {

  static final int STATUS_CANCELLED = 2;
  static final int STATUS_DONE = 1;
  static final int STATUS_RUNNING = 0;

  final AtomicInteger status;

  private ListAsyncMaterializer<E> state;

  AbstractListAsyncMaterializer(@NotNull final AtomicInteger status) {
    this.status = status;
  }

  @Override
  public boolean isCancelled() {
    return status.get() == STATUS_CANCELLED;
  }

  @Override
  public boolean isDone() {
    return status.get() != STATUS_RUNNING;
  }

  @Override
  public void materializeCancel(final boolean mayInterruptIfRunning) {
    state.materializeCancel(mayInterruptIfRunning);
  }

  @Override
  public void materializeContains(final Object element,
      @NotNull final AsyncConsumer<Boolean> consumer) {
    state.materializeContains(element, consumer);
  }

  @Override
  public void materializeDone(@NotNull final AsyncConsumer<List<E>> consumer) {
    state.materializeElements(new AsyncConsumer<List<E>>() {
      @Override
      public void accept(final List<E> elements) throws Exception {
        setState(state, STATUS_DONE);
        consumer.accept(elements);
      }

      @Override
      public void error(@NotNull final Exception error) throws Exception {
        consumer.error(error);
      }
    });
  }

  @Override
  public void materializeEach(@NotNull final IndexedAsyncConsumer<E> consumer) {
    state.materializeEach(consumer);
  }

  @Override
  public void materializeElement(final int index, @NotNull final IndexedAsyncConsumer<E> consumer) {
    state.materializeElement(index, consumer);
  }

  @Override
  public void materializeElements(@NotNull final AsyncConsumer<List<E>> consumer) {
    state.materializeElements(consumer);
  }

  @Override
  public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
    state.materializeEmpty(consumer);
  }

  @Override
  public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
    state.materializeSize(consumer);
  }

  @Override
  public int weightElement() {
    return state.weightElement();
  }

  @Override
  public int weightElements() {
    return state.weightElements();
  }

  @Override
  public int weightSize() {
    return state.weightSize();
  }

  @NotNull
  final ListAsyncMaterializer<E> getState() {
    return state;
  }

  @NotNull
  final ListAsyncMaterializer<E> setState(@NotNull final ListAsyncMaterializer<E> newState,
      final int statusCode) {
    if (status.compareAndSet(STATUS_RUNNING, statusCode)) {
      state = newState;
    }
    return state;
  }
}
