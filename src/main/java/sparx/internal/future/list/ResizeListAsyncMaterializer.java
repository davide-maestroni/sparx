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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.AsyncConsumer;
import sparx.internal.future.IndexedAsyncConsumer;
import sparx.internal.future.IndexedAsyncPredicate;
import sparx.util.annotation.Positive;
import sparx.util.function.Function;
import sparx.util.function.TernaryFunction;

public class ResizeListAsyncMaterializer<E> extends AbstractListAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      ResizeListAsyncMaterializer.class.getName());

  private final int size;

  public ResizeListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      @Positive final int numElements, final E padding, @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final TernaryFunction<List<E>, Integer, E, List<E>> resizeFunction,
      @NotNull final Function<List<E>, List<E>> decorateFunction) {
    super(new AtomicInteger(STATUS_RUNNING));
    setState(
        new ImmaterialState(wrapped, numElements, padding, context, cancelException, resizeFunction,
            decorateFunction));
    size = numElements;
  }

  @Override
  public int knownSize() {
    return size;
  }

  private class ImmaterialState implements ListAsyncMaterializer<E> {

    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final Function<List<E>, List<E>> decorateFunction;
    private final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = new ArrayList<AsyncConsumer<List<E>>>(
        2);
    private final int numElements;
    private final E padding;
    private final TernaryFunction<List<E>, Integer, E, List<E>> resizeFunction;
    private final ListAsyncMaterializer<E> wrapped;

    private int wrappedSize;

    private ImmaterialState(@NotNull final ListAsyncMaterializer<E> wrapped,
        @Positive final int numElements, final E padding, @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final TernaryFunction<List<E>, Integer, E, List<E>> resizeFunction,
        @NotNull final Function<List<E>, List<E>> decorateFunction) {
      this.wrapped = wrapped;
      this.numElements = numElements;
      this.padding = padding;
      this.context = context;
      this.cancelException = cancelException;
      this.resizeFunction = resizeFunction;
      this.decorateFunction = decorateFunction;
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
      return false;
    }

    @Override
    public int knownSize() {
      return numElements;
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
      wrapped.materializeContains(element, new CancellableAsyncConsumer<Boolean>() {
        @Override
        public void cancellableAccept(final Boolean contains) throws Exception {
          if (contains) {
            consumer.accept(true);
          } else if (wrappedSize >= 0) {
            if (numElements > wrappedSize) {
              consumer.accept(padding == element || (element != null && element.equals(padding)));
            } else {
              consumer.accept(false);
            }
          } else {
            wrapped.materializeSize(new CancellableAsyncConsumer<Integer>() {
              @Override
              public void cancellableAccept(final Integer size) throws Exception {
                wrappedSize = size;
                if (numElements > size) {
                  consumer.accept(
                      padding == element || (element != null && element.equals(padding)));
                } else {
                  consumer.accept(false);
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
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeDone(@NotNull final AsyncConsumer<List<E>> consumer) {
      safeConsumeError(consumer, new UnsupportedOperationException(), LOGGER);
    }

    @Override
    public void materializeElement(final int index,
        @NotNull final IndexedAsyncConsumer<E> consumer) {
      if (index < 0) {
        safeConsumeError(consumer, new IndexOutOfBoundsException(Integer.toString(index)), LOGGER);
      } else if (index >= numElements) {
        safeConsumeComplete(consumer, numElements, LOGGER);
      } else if (wrappedSize >= 0 && index >= wrappedSize) {
        safeConsume(consumer, numElements, index, padding, LOGGER);
      } else {
        wrapped.materializeElement(index, new CancellableIndexedAsyncConsumer<E>() {
          @Override
          public void cancellableComplete(final int size) throws Exception {
            wrappedSize = size;
            consumer.accept(numElements, index, padding);
          }

          @Override
          public void cancellableAccept(final int size, final int index, final E element)
              throws Exception {
            wrappedSize = Math.max(wrappedSize, size);
            consumer.accept(numElements, index, element);
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
        if (wrappedSize >= 0 && numElements >= wrappedSize) {
          wrapped.materializeElements(new CancellableAsyncConsumer<List<E>>() {
            @Override
            public void cancellableAccept(final List<E> elements) throws Exception {
              final List<E> materialized = resizeFunction.apply(elements, numElements, padding);
              setState(new ListToListAsyncMaterializer<E>(materialized, context));
              consumeElements(materialized);
            }

            @Override
            public void error(@NotNull final Exception error) {
              setError(error);
            }
          });
        } else {
          final int last = numElements - 1;
          wrapped.materializeNextWhile(0, new CancellableIndexedAsyncPredicate<E>() {
            private final ArrayList<E> elements = new ArrayList<E>();

            @Override
            public void cancellableComplete(final int size) throws Exception {
              final List<E> materialized = resizeFunction.apply(elements, numElements, padding);
              setState(new ListToListAsyncMaterializer<E>(materialized, context));
              consumeElements(materialized);
            }

            @Override
            public boolean cancellableTest(final int size, final int index, final E element)
                throws Exception {
              elements.add(element);
              if (index == last) {
                final List<E> materialized = decorateFunction.apply(elements);
                setState(new ListToListAsyncMaterializer<E>(materialized, context));
                consumeElements(materialized);
                return false;
              }
              return true;
            }

            @Override
            public void error(@NotNull final Exception error) {
              setError(error);
            }
          });
        }
      }
    }

    @Override
    public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
      safeConsume(consumer, false, LOGGER);
    }

    @Override
    public void materializeHasElement(final int index,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      safeConsume(consumer, index >= 0 && index < numElements, LOGGER);
    }

    @Override
    public void materializeNextWhile(final int index,
        @NotNull final IndexedAsyncPredicate<E> predicate) {
      if (index >= numElements) {
        safeConsumeComplete(predicate, numElements, LOGGER);
      } else {
        wrapped.materializeNextWhile(index, new CancellableIndexedAsyncPredicate<E>() {
          @Override
          public void cancellableComplete(final int size) throws Exception {
            for (int i = size; i < numElements; ++i) {
              if (!predicate.test(numElements, i, padding)) {
                return;
              }
            }
            predicate.complete(numElements);
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            return predicate.test(numElements, index, element);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            predicate.error(error);
          }
        });
      }
    }

    @Override
    public void materializePrevWhile(final int index,
        @NotNull final IndexedAsyncPredicate<E> predicate) {
      if (wrappedSize >= 0) {
        for (int i = Math.min(index, numElements); i >= wrappedSize; --i) {
          if (!safeConsume(predicate, numElements, i, padding, LOGGER)) {
            return;
          }
        }
        wrapped.materializePrevWhile(index, new CancellableIndexedAsyncPredicate<E>() {
          @Override
          public void cancellableComplete(final int size) throws Exception {
            predicate.complete(numElements);
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            return predicate.test(numElements, index, element);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            predicate.error(error);
          }
        });
      } else {
        wrapped.materializePrevWhile(index, new CancellableIndexedAsyncPredicate<E>() {
          private boolean first = true;

          @Override
          public void cancellableComplete(final int size) throws Exception {
            if (handleFirst(-1)) {
              predicate.complete(numElements);
            }
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            return handleFirst(index) && predicate.test(numElements, index, element);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            predicate.error(error);
          }

          private boolean handleFirst(final int index) throws Exception {
            if (first) {
              first = false;
              for (int i = numElements - 1; i > index; --i) {
                if (!predicate.test(numElements, i, padding)) {
                  return false;
                }
              }
            }
            return true;
          }
        });
      }
    }

    @Override
    public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
      safeConsume(consumer, numElements, LOGGER);
    }

    @Override
    public int weightContains() {
      if (wrappedSize < 0) {
        return (int) Math.min(Integer.MAX_VALUE,
            (long) wrapped.weightContains() + wrapped.weightSize());
      }
      return wrapped.weightContains();
    }

    @Override
    public int weightElement() {
      return wrapped.weightElement();
    }

    @Override
    public int weightElements() {
      return elementsConsumers.isEmpty() ? wrappedSize >= 0 && numElements >= wrappedSize
          ? wrapped.weightElements() : wrapped.weightNextWhile() : 1;
    }

    @Override
    public int weightHasElement() {
      return 1;
    }

    @Override
    public int weightEmpty() {
      return 1;
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
      return 1;
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

    private void setError(@NotNull final Exception error) {
      final CancellationException exception = cancelException.get();
      if (exception != null) {
        setCancelled(exception);
        consumeError(exception);
      } else {
        setFailed(error);
        consumeError(error);
      }
    }
  }
}
