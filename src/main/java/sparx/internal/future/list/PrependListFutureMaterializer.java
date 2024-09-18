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

import static sparx.internal.future.FutureConsumers.safeConsume;
import static sparx.internal.future.FutureConsumers.safeConsumeComplete;
import static sparx.internal.future.FutureConsumers.safeConsumeError;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.FutureConsumer;
import sparx.internal.future.IndexedFutureConsumer;
import sparx.internal.future.IndexedFuturePredicate;
import sparx.util.IndexOverflowException;
import sparx.util.SizeOverflowException;
import sparx.util.function.BinaryFunction;

public class PrependListFutureMaterializer<E> extends AbstractListFutureMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      PrependListFutureMaterializer.class.getName());

  private final int knownSize;
  private final boolean isMaterializedAtOnce;

  public PrependListFutureMaterializer(@NotNull final ListFutureMaterializer<E> wrapped,
      final E element, @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final BinaryFunction<List<E>, E, List<E>> prependFunction) {
    super(context, new AtomicInteger(STATUS_RUNNING));
    knownSize = safeSize(wrapped.knownSize());
    isMaterializedAtOnce = wrapped.isMaterializedAtOnce();
    setState(new ImmaterialState(wrapped, element, context, cancelException, prependFunction));
  }

  private static int safeSize(final int wrappedSize) {
    if (wrappedSize >= 0) {
      return SizeOverflowException.safeCast((long) wrappedSize + 1);
    }
    return -1;
  }

  @Override
  public boolean isMaterializedAtOnce() {
    return isMaterializedAtOnce || super.isMaterializedAtOnce();
  }

  @Override
  public int knownSize() {
    return knownSize;
  }

  private class ImmaterialState implements ListFutureMaterializer<E> {

    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final E element;
    private final ArrayList<FutureConsumer<List<E>>> elementsConsumers = new ArrayList<FutureConsumer<List<E>>>(
        2);
    private final BinaryFunction<List<E>, E, List<E>> prependFunction;
    private final ListFutureMaterializer<E> wrapped;

    private int wrappedSize;

    public ImmaterialState(@NotNull final ListFutureMaterializer<E> wrapped, final E element,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final BinaryFunction<List<E>, E, List<E>> prependFunction) {
      this.wrapped = wrapped;
      this.element = element;
      this.context = context;
      this.cancelException = cancelException;
      this.prependFunction = prependFunction;
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
    public boolean isSucceeded() {
      return false;
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
        @NotNull final FutureConsumer<Boolean> consumer) {
      final E prepended = this.element;
      if (element == prepended || (element != null && element.equals(prepended))) {
        safeConsume(consumer, true, LOGGER);
      } else {
        wrapped.materializeContains(element, new CancellableFutureConsumer<Boolean>() {
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
    public void materializeElement(final int index,
        @NotNull final IndexedFutureConsumer<E> consumer) {
      if (index < 0) {
        safeConsumeError(consumer, new IndexOutOfBoundsException(Integer.toString(index)), LOGGER);
      } else if (index == 0) {
        safeConsume(consumer, safeSize(wrappedSize), 0, element, LOGGER);
      } else {
        final int originalIndex = index;
        wrapped.materializeElement(index - 1, new CancellableIndexedFutureConsumer<E>() {
          @Override
          public void cancellableAccept(final int size, final int index, final E element)
              throws Exception {
            final int knownSize = safeSize(wrappedSize = Math.max(wrappedSize, size));
            consumer.accept(knownSize, originalIndex, element);
          }

          @Override
          public void cancellableComplete(final int size) throws Exception {
            consumer.complete(safeSize(wrappedSize = size));
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
    }

    @Override
    public void materializeElements(@NotNull final FutureConsumer<List<E>> consumer) {
      final ArrayList<FutureConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      elementsConsumers.add(consumer);
      if (elementsConsumers.size() == 1) {
        wrapped.materializeElements(new CancellableFutureConsumer<List<E>>() {
          @Override
          public void cancellableAccept(final List<E> elements) throws Exception {
            final List<E> materialized = prependFunction.apply(elements, element);
            setDone(new ListToListFutureMaterializer<E>(materialized, context));
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
    public void materializeEmpty(@NotNull final FutureConsumer<Boolean> consumer) {
      safeConsume(consumer, false, LOGGER);
    }

    @Override
    public void materializeHasElement(final int index,
        @NotNull final FutureConsumer<Boolean> consumer) {
      if (index < 0) {
        safeConsume(consumer, false, LOGGER);
      } else if (index == 0 || index <= wrappedSize) {
        safeConsume(consumer, true, LOGGER);
      } else if (wrappedSize >= 0) {
        safeConsume(consumer, false, LOGGER);
      } else {
        wrapped.materializeHasElement(index - 1, new CancellableFutureConsumer<Boolean>() {
          @Override
          public void cancellableAccept(final Boolean hasElement) throws Exception {
            consumer.accept(hasElement);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
    }

    @Override
    public void materializeNextWhile(final int index,
        @NotNull final IndexedFuturePredicate<E> predicate) {
      if (index == 0 && !safeConsume(predicate, safeSize(wrappedSize), 0, element, LOGGER)) {
        return;
      }
      wrapped.materializeNextWhile(Math.max(0, index - 1),
          new CancellableIndexedFuturePredicate<E>() {
            @Override
            public void cancellableComplete(final int size) throws Exception {
              predicate.complete(safeSize(wrappedSize = size));
            }

            @Override
            public boolean cancellableTest(final int size, final int index, final E element)
                throws Exception {
              return predicate.test(safeSize(wrappedSize = size),
                  IndexOverflowException.safeCast((long) index + 1), element);
            }

            @Override
            public void error(@NotNull final Exception error) throws Exception {
              predicate.error(error);
            }
          });
    }

    @Override
    public void materializePrevWhile(final int index,
        @NotNull final IndexedFuturePredicate<E> predicate) {
      if (index == 0) {
        if (safeConsume(predicate, safeSize(wrappedSize), 0, element, LOGGER)) {
          safeConsumeComplete(predicate, safeSize(wrappedSize), LOGGER);
        }
      } else {
        wrapped.materializePrevWhile(index - 1, new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) throws Exception {
            if (predicate.test(safeSize(wrappedSize = Math.max(wrappedSize, size)), 0, element)) {
              predicate.complete(safeSize(wrappedSize));
            }
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            return predicate.test(safeSize(wrappedSize = Math.max(wrappedSize, size)),
                IndexOverflowException.safeCast((long) index + 1), element);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            predicate.error(error);
          }
        });
      }
    }

    @Override
    public void materializeSize(@NotNull final FutureConsumer<Integer> consumer) {
      if (wrappedSize >= 0) {
        safeConsume(consumer, safeSize(wrappedSize), LOGGER);
      } else {
        wrapped.materializeSize(new CancellableFutureConsumer<Integer>() {
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
      return wrappedSize < 0 ? wrapped.weightHasElement() : 1;
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
      return wrappedSize < 0 ? wrapped.weightSize() : 1;
    }

    private void consumeElements(@NotNull final List<E> elements) {
      final ArrayList<FutureConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      for (final FutureConsumer<List<E>> elementsConsumer : elementsConsumers) {
        safeConsume(elementsConsumer, elements, LOGGER);
      }
      elementsConsumers.clear();
    }

    private void consumeError(@NotNull final Exception error) {
      final ArrayList<FutureConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      for (final FutureConsumer<List<E>> elementsConsumer : elementsConsumers) {
        safeConsumeError(elementsConsumer, error, LOGGER);
      }
      elementsConsumers.clear();
    }
  }
}
