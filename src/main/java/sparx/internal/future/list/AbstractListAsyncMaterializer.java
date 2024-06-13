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
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import sparx.internal.future.AsyncConsumer;
import sparx.internal.future.IndexedAsyncConsumer;

abstract class AbstractListAsyncMaterializer<E> implements ListAsyncMaterializer<E> {

  static final int STATUS_CANCELLED = 3;
  static final int STATUS_FAILED = 2;
  static final int STATUS_DONE = 1;
  static final int STATUS_RUNNING = 0;

  final AtomicInteger status;

  private CancellationException cancelException;
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
  public boolean isFailed() {
    return status.get() == STATUS_FAILED;
  }

  @Override
  public boolean isMaterializedAtOnce() {
    return isDone();
  }

  @Override
  public void materializeCancel(@NotNull final CancellationException exception) {
    cancelException = exception;
    state.materializeCancel(exception);
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
  public int weightContains() {
    return state.weightContains();
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
  public int weightEmpty() {
    return state.weightEmpty();
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
  final ListAsyncMaterializer<E> setCancelled(@NotNull final CancellationException exception) {
    return setState(new CancelledListAsyncMaterializer<E>(exception), STATUS_CANCELLED);
  }

  @NotNull
  final ListAsyncMaterializer<E> setFailed(@NotNull final Exception error) {
    return setState(new FailedListAsyncMaterializer<E>(error), STATUS_FAILED);
  }

  @NotNull
  final ListAsyncMaterializer<E> setState(@NotNull final ListAsyncMaterializer<E> newState) {
    return setState(newState, STATUS_RUNNING);
  }

  @NotNull
  private ListAsyncMaterializer<E> setState(@NotNull final ListAsyncMaterializer<E> newState,
      final int statusCode) {
    if (status.compareAndSet(STATUS_RUNNING, statusCode)) {
      state = newState;
    }
    return state;
  }

  protected abstract class CancellableAsyncConsumer<P> implements AsyncConsumer<P> {

    @Override
    public void accept(final P param) throws Exception {
      final CancellationException cancelException = AbstractListAsyncMaterializer.this.cancelException;
      if (cancelException != null) {
        error(cancelException);
      } else {
        cancellableAccept(param);
      }
    }

    public void cancellableAccept(final P param) throws Exception {
    }
  }

  protected abstract class CancellableIndexedAsyncConsumer<P> implements IndexedAsyncConsumer<P> {

    @Override
    public void accept(final int size, final int index, final P param) throws Exception {
      final CancellationException cancelException = AbstractListAsyncMaterializer.this.cancelException;
      if (cancelException != null) {
        error(cancelException);
      } else {
        cancellableAccept(size, index, param);
      }
    }

    @Override
    public void complete(final int size) throws Exception {
      final CancellationException cancelException = AbstractListAsyncMaterializer.this.cancelException;
      if (cancelException != null) {
        error(cancelException);
      } else {
        cancellableComplete(size);
      }
    }

    public void cancellableAccept(final int size, final int index, final P param) throws Exception {
    }

    public void cancellableComplete(final int size) throws Exception {
    }
  }

  protected abstract class CancellableMultiAsyncConsumer<P1, P2> implements AsyncConsumer<P1>,
      IndexedAsyncConsumer<P2> {

    @Override
    public void accept(final P1 param) throws Exception {
      final CancellationException cancelException = AbstractListAsyncMaterializer.this.cancelException;
      if (cancelException != null) {
        error(cancelException);
      } else {
        cancellableAccept(param);
      }
    }

    @Override
    public void accept(final int size, final int index, final P2 param) throws Exception {
      final CancellationException cancelException = AbstractListAsyncMaterializer.this.cancelException;
      if (cancelException != null) {
        error(cancelException);
      } else {
        cancellableAccept(size, index, param);
      }
    }

    @Override
    public void complete(final int size) throws Exception {
      final CancellationException cancelException = AbstractListAsyncMaterializer.this.cancelException;
      if (cancelException != null) {
        error(cancelException);
      } else {
        cancellableComplete(size);
      }
    }

    public void cancellableAccept(final P1 param) throws Exception {
    }

    public void cancellableAccept(final int size, final int index, final P2 param)
        throws Exception {
    }

    public void cancellableComplete(final int size) throws Exception {
    }
  }
}
