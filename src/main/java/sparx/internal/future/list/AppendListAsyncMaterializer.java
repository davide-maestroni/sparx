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
import static sparx.internal.future.AsyncConsumers.safeConsumeError;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.internal.future.AsyncConsumer;
import sparx.internal.future.IndexedAsyncConsumer;
import sparx.util.SizeOverflowException;
import sparx.util.function.BinaryFunction;

public class AppendListAsyncMaterializer<E> extends AbstractListAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      AppendListAsyncMaterializer.class.getName());

  private final int knownSize;
  private final boolean isMaterializedAtOnce;

  public AppendListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      final E element, @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final BinaryFunction<List<E>, E, List<E>> appendFunction) {
    super(new AtomicInteger(STATUS_RUNNING));
    knownSize = safeSize(wrapped.knownSize());
    isMaterializedAtOnce = wrapped.isMaterializedAtOnce();
    setState(new ImmaterialState(wrapped, element, cancelException, appendFunction));
  }

  private static int safeSize(final int wrappedSize) {
    if (wrappedSize >= 0) {
      return SizeOverflowException.safeCast((long) wrappedSize + 1);
    }
    return -1;
  }

  @Override
  public int knownSize() {
    return knownSize;
  }

  @Override
  public boolean isMaterializedAtOnce() {
    return isMaterializedAtOnce || super.isMaterializedAtOnce();
  }

  private class ImmaterialState implements ListAsyncMaterializer<E> {

    private final BinaryFunction<List<E>, E, List<E>> appendFunction;
    private final AtomicReference<CancellationException> cancelException;
    private final E element;
    private final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = new ArrayList<AsyncConsumer<List<E>>>(
        2);
    private final ListAsyncMaterializer<E> wrapped;

    private int wrappedSize;

    public ImmaterialState(@NotNull final ListAsyncMaterializer<E> wrapped, final E element,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final BinaryFunction<List<E>, E, List<E>> appendFunction) {
      this.wrapped = wrapped;
      this.element = element;
      this.cancelException = cancelException;
      this.appendFunction = appendFunction;
      wrappedSize = wrapped.knownSize();
    }

    @Override
    public boolean isCancelled() {
      return false;
    }

    @Override
    public boolean isDone() {
      return false;
    }

    @Override
    public boolean isFailed() {
      return false;
    }

    @Override
    public boolean isMaterializedAtOnce() {
      return wrapped.isMaterializedAtOnce();
    }

    @Override
    public int knownSize() {
      return knownSize;
    }

    @Override
    public void materializeCancel(@NotNull final CancellationException exception) {
      wrapped.materializeCancel(exception);
      setCancelled(exception);
      consumeError(exception);
    }

    @Override
    public void materializeContains(final Object element,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      final E appended = this.element;
      if (element == appended || (element != null && element.equals(appended))) {
        safeConsume(consumer, true, LOGGER);
      } else {
        wrapped.materializeContains(element, new CancellableAsyncConsumer<Boolean>() {
          @Override
          public void cancellableAccept(final Boolean contains) throws Exception {
            consumer.accept(contains);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
    }

    @Override
    public void materializeDone(@NotNull final AsyncConsumer<List<E>> consumer) {
      safeConsumeError(consumer, new UnsupportedOperationException(), LOGGER);
    }

    @Override
    public void materializeEach(@NotNull final IndexedAsyncConsumer<E> consumer) {
      wrapped.materializeEach(new CancellableIndexedAsyncConsumer<E>() {
        @Override
        public void cancellableAccept(final int size, final int index, final E element)
            throws Exception {
          final int knownSize = safeSize(wrappedSize = Math.max(wrappedSize, size));
          consumer.accept(knownSize, index, element);
        }

        @Override
        public void cancellableComplete(final int size) throws Exception {
          final int knownSize = safeSize(wrappedSize = size);
          consumer.accept(knownSize, size, element);
          consumer.complete(knownSize);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeElement(final int index,
        @NotNull final IndexedAsyncConsumer<E> consumer) {
      if (index < 0) {
        safeConsumeError(consumer, new IndexOutOfBoundsException(Integer.toString(index)), LOGGER);
      } else {
        wrapped.materializeElement(index, new CancellableIndexedAsyncConsumer<E>() {
          @Override
          public void cancellableAccept(final int size, final int index, final E element)
              throws Exception {
            final int knownSize = safeSize(wrappedSize = Math.max(wrappedSize, size));
            consumer.accept(knownSize, index, element);
          }

          @Override
          public void cancellableComplete(final int size) throws Exception {
            wrappedSize = size;
            if (size == index) {
              consumer.accept(safeSize(size), size, element);
            } else {
              consumer.complete(safeSize(size));
            }
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
    }

    @Override
    public void materializeElements(@NotNull final AsyncConsumer<List<E>> consumer) {
      final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      elementsConsumers.add(consumer);
      if (elementsConsumers.size() == 1) {
        wrapped.materializeElements(new CancellableAsyncConsumer<List<E>>() {
          @Override
          public void cancellableAccept(final List<E> elements) throws Exception {
            final List<E> materialized = appendFunction.apply(elements, element);
            setState(new ListToListAsyncMaterializer<E>(materialized));
            consumeElements(materialized);
          }

          @Override
          public void error(@NotNull final Exception error) {
            final CancellationException exception = cancelException.get();
            if (exception != null) {
              setCancelled(exception);
              consumeError(exception);
            } else {
              setFailed(error);
              consumeError(error);
            }
          }
        });
      }
    }

    @Override
    public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
      safeConsume(consumer, false, LOGGER);
    }

    @Override
    public void materializeHasElement(final int index,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      if (index < 0) {
        safeConsume(consumer, false, LOGGER);
      } else if (index < wrappedSize || index < safeSize(wrappedSize)) {
        safeConsume(consumer, true, LOGGER);
      } else if (wrappedSize >= 0) {
        safeConsume(consumer, false, LOGGER);
      } else {
        wrapped.materializeElement(index, new CancellableIndexedAsyncConsumer<E>() {
          @Override
          public void cancellableAccept(final int size, final int index, final E element)
              throws Exception {
            wrappedSize = Math.max(wrappedSize, size);
            consumer.accept(true);
          }

          @Override
          public void cancellableComplete(final int size) throws Exception {
            wrappedSize = size;
            consumer.accept(size == index);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
    }

    @Override
    public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
      if (wrappedSize >= 0) {
        safeConsume(consumer, safeSize(wrappedSize), LOGGER);
      } else {
        wrapped.materializeSize(new CancellableAsyncConsumer<Integer>() {
          @Override
          public void cancellableAccept(final Integer size) throws Exception {
            consumer.accept(safeSize(wrappedSize = size));
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
    }

    @Override
    public int weightContains() {
      return wrapped.weightContains();
    }

    @Override
    public int weightEach() {
      return wrapped.weightEach();
    }

    @Override
    public int weightElement() {
      return wrapped.weightElement();
    }

    @Override
    public int weightElements() {
      return elementsConsumers.isEmpty() ? wrapped.weightElements() : 1;
    }

    @Override
    public int weightEmpty() {
      return 1;
    }

    @Override
    public int weightHasElement() {
      if (wrappedSize >= 0) {
        return 1;
      }
      return wrapped.weightElement();
    }

    @Override
    public int weightSize() {
      if (wrappedSize >= 0) {
        return 1;
      }
      return wrapped.weightSize();
    }

    private void consumeElements(@NotNull final List<E> elements) {
      final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      for (final AsyncConsumer<List<E>> elementsConsumer : elementsConsumers) {
        safeConsume(elementsConsumer, elements, LOGGER);
      }
      elementsConsumers.clear();
    }

    private void consumeError(@NotNull final Exception error) {
      final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      for (final AsyncConsumer<List<E>> elementsConsumer : elementsConsumers) {
        safeConsumeError(elementsConsumer, error, LOGGER);
      }
      elementsConsumers.clear();
    }
  }
}
