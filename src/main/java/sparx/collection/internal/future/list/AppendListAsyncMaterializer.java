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
package sparx.collection.internal.future.list;

import static sparx.collection.internal.future.AsyncConsumers.safeConsume;
import static sparx.collection.internal.future.AsyncConsumers.safeConsumeError;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.collection.internal.future.AsyncConsumer;
import sparx.collection.internal.future.IndexedAsyncConsumer;
import sparx.util.Require;
import sparx.util.SizeOverflowException;
import sparx.util.function.BinaryFunction;

public class AppendListAsyncMaterializer<E> extends AbstractListAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      AppendListAsyncMaterializer.class.getName());

  private final int knownSize;

  public AppendListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      final E element, @NotNull final AtomicBoolean isCancelled,
      @NotNull final BinaryFunction<List<E>, E, List<E>> appendFunction) {
    super(new AtomicInteger(STATUS_RUNNING));
    knownSize = safeSize(wrapped.knownSize());
    setState(new ImmaterialState(wrapped, element, Require.notNull(isCancelled, "isCancelled"),
        Require.notNull(appendFunction, "appendFunction")), STATUS_RUNNING);
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

  private class ImmaterialState implements ListAsyncMaterializer<E> {

    private final BinaryFunction<List<E>, E, List<E>> appendFunction;
    private final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = new ArrayList<AsyncConsumer<List<E>>>(
        2);
    private final E element;
    private final AtomicBoolean isCancelled;
    private final ListAsyncMaterializer<E> wrapped;

    private int wrappedSize = -1;

    public ImmaterialState(@NotNull final ListAsyncMaterializer<E> wrapped, final E element,
        @NotNull final AtomicBoolean isCancelled,
        @NotNull final BinaryFunction<List<E>, E, List<E>> appendFunction) {
      this.wrapped = wrapped;
      this.element = element;
      this.isCancelled = isCancelled;
      this.appendFunction = appendFunction;
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
    public int knownSize() {
      return knownSize;
    }

    @Override
    public void materializeCancel(final boolean mayInterruptIfRunning) {
      wrapped.materializeCancel(mayInterruptIfRunning);
      setState(new CancelledListAsyncMaterializer<E>(), STATUS_CANCELLED);
    }

    @Override
    public void materializeContains(final Object element,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      final E appended = this.element;
      if (element == appended || (element != null && element.equals(appended))) {
        safeConsume(consumer, true, LOGGER);
      } else {
        wrapped.materializeContains(element, new AsyncConsumer<Boolean>() {
          @Override
          public void accept(final Boolean contains) throws Exception {
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
    public void materializeEach(@NotNull final IndexedAsyncConsumer<E> consumer) {
      wrapped.materializeEach(new IndexedAsyncConsumer<E>() {
        @Override
        public void accept(final int size, final int index, final E element) throws Exception {
          final int knownSize = safeSize(wrappedSize = Math.max(wrappedSize, size));
          consumer.accept(knownSize, index, element);
        }

        @Override
        public void complete(final int size) throws Exception {
          final int knownSize = safeSize(wrappedSize = size);
          consumer.accept(knownSize, size, element);
          consumer.complete(knownSize);
        }

        @Override
        public void error(final int index, @NotNull final Exception error) throws Exception {
          consumer.error(index, error);
        }
      });
    }

    @Override
    public void materializeElement(final int index,
        @NotNull final IndexedAsyncConsumer<E> consumer) {
      if (index < 0) {
        safeConsumeError(consumer, index, new IndexOutOfBoundsException(Integer.toString(index)),
            LOGGER);
      } else {
        wrapped.materializeElement(index, new IndexedAsyncConsumer<E>() {
          @Override
          public void accept(final int size, final int index, final E element) throws Exception {
            final int knownSize = safeSize(wrappedSize = Math.max(wrappedSize, size));
            consumer.accept(knownSize, index, element);
          }

          @Override
          public void complete(final int size) throws Exception {
            wrappedSize = size;
            if (size == index) {
              consumer.accept(safeSize(size), size, element);
            } else {
              consumer.complete(safeSize(size));
            }
          }

          @Override
          public void error(final int index, @NotNull final Exception error) throws Exception {
            consumer.error(index, error);
          }
        });
      }
    }

    @Override
    public void materializeElements(@NotNull final AsyncConsumer<List<E>> consumer) {
      final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      elementsConsumers.add(consumer);
      if (elementsConsumers.size() == 1) {
        wrapped.materializeElements(new AsyncConsumer<List<E>>() {
          @Override
          public void accept(final List<E> elements) throws Exception {
            final List<E> materialized = appendFunction.apply(elements, element);
            setState(new ListToListAsyncMaterializer<E>(materialized), STATUS_DONE);
            consumeElements(materialized);
          }

          @Override
          public void error(@NotNull final Exception error) {
            if (isCancelled.get()) {
              setState(new CancelledListAsyncMaterializer<E>(), STATUS_CANCELLED);
              consumeError(new CancellationException());
            } else {
              setState(new FailedListAsyncMaterializer<E>(error), STATUS_DONE);
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
    public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
      if (wrappedSize >= 0) {
        safeConsume(consumer, safeSize(wrappedSize), LOGGER);
      } else {
        wrapped.materializeSize(new AsyncConsumer<Integer>() {
          @Override
          public void accept(final Integer size) throws Exception {
            consumer.accept(safeSize(wrappedSize = size));
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
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
