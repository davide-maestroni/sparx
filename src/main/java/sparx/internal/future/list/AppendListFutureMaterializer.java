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
import static sparx.internal.future.FutureConsumers.safeConsumeError;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.FutureConsumer;
import sparx.internal.future.IndexedFutureConsumer;
import sparx.internal.future.IndexedFuturePredicate;
import sparx.util.SizeOverflowException;
import sparx.util.annotation.NotNegative;
import sparx.util.function.BinaryFunction;

public class AppendListFutureMaterializer<E> extends AbstractListFutureMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      AppendListFutureMaterializer.class.getName());

  private final int knownSize;
  private final boolean isMaterializedAtOnce;

  public AppendListFutureMaterializer(@NotNull final ListFutureMaterializer<E> wrapped,
      final E element, @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final BinaryFunction<List<E>, E, List<E>> appendFunction) {
    super(context);
    knownSize = safeSize(wrapped.knownSize());
    isMaterializedAtOnce = wrapped.isMaterializedAtOnce();
    setState(new ImmaterialState(wrapped, element, context, cancelException, appendFunction));
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

    private final BinaryFunction<List<E>, E, List<E>> appendFunction;
    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final E element;
    private final ArrayList<FutureConsumer<List<E>>> elementsConsumers = new ArrayList<FutureConsumer<List<E>>>(
        2);
    private final ListFutureMaterializer<E> wrapped;

    private int wrappedSize;

    public ImmaterialState(@NotNull final ListFutureMaterializer<E> wrapped, final E element,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final BinaryFunction<List<E>, E, List<E>> appendFunction) {
      this.wrapped = wrapped;
      this.element = element;
      this.context = context;
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
      final E appended = this.element;
      if (element == appended || (element != null && element.equals(appended))) {
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
    public void materializeElement(@NotNegative final int index,
        @NotNull final IndexedFutureConsumer<E> consumer) {
      wrapped.materializeElement(index, new CancellableIndexedFutureConsumer<E>() {
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

    @Override
    public void materializeElements(@NotNull final FutureConsumer<List<E>> consumer) {
      final ArrayList<FutureConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      elementsConsumers.add(consumer);
      if (elementsConsumers.size() == 1) {
        wrapped.materializeElements(new CancellableFutureConsumer<List<E>>() {
          @Override
          public void cancellableAccept(final List<E> elements) throws Exception {
            final List<E> materialized = appendFunction.apply(elements, element);
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
    public void materializeHasElement(@NotNegative final int index,
        @NotNull final FutureConsumer<Boolean> consumer) {
      if (index < wrappedSize || index < safeSize(wrappedSize)) {
        safeConsume(consumer, true, LOGGER);
      } else if (wrappedSize >= 0) {
        safeConsume(consumer, false, LOGGER);
      } else {
        wrapped.materializeElement(index, new CancellableIndexedFutureConsumer<E>() {
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
    public void materializeNextWhile(@NotNegative final int index,
        @NotNull final IndexedFuturePredicate<E> predicate) {
      wrapped.materializeNextWhile(index, new CancellableIndexedFuturePredicate<E>() {
        @Override
        public void cancellableComplete(final int size) throws Exception {
          final int knownSize = safeSize(wrappedSize = size);
          if (index > size || predicate.test(knownSize, size, element)) {
            predicate.complete(knownSize);
          }
        }

        @Override
        public boolean cancellableTest(final int size, final int index, final E element)
            throws Exception {
          final int knownSize = safeSize(wrappedSize = Math.max(wrappedSize, size));
          return predicate.test(knownSize, index, element);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          predicate.error(error);
        }
      });
    }

    @Override
    public void materializePrevWhile(@NotNegative final int index,
        @NotNull final IndexedFuturePredicate<E> predicate) {
      if (wrappedSize >= 0) {
        if (index >= wrappedSize) {
          if (safeConsume(predicate, safeSize(wrappedSize), wrappedSize, element, LOGGER)) {
            wrapped.materializePrevWhile(wrappedSize - 1,
                new CancellableIndexedFuturePredicate<E>() {
                  @Override
                  public void cancellableComplete(final int size) throws Exception {
                    final int knownSize = safeSize(wrappedSize = Math.max(wrappedSize, size));
                    predicate.complete(knownSize);
                  }

                  @Override
                  public boolean cancellableTest(final int size, final int index, final E element)
                      throws Exception {
                    final int knownSize = safeSize(wrappedSize = Math.max(wrappedSize, size));
                    return predicate.test(knownSize, index, element);
                  }

                  @Override
                  public void error(@NotNull final Exception error) throws Exception {
                    predicate.error(error);
                  }
                });
          }
        } else {
          wrapped.materializePrevWhile(index, new CancellableIndexedFuturePredicate<E>() {
            @Override
            public void cancellableComplete(final int size) throws Exception {
              final int knownSize = safeSize(wrappedSize = Math.max(wrappedSize, size));
              predicate.complete(knownSize);
            }

            @Override
            public boolean cancellableTest(final int size, final int index, final E element)
                throws Exception {
              final int knownSize = safeSize(wrappedSize = Math.max(wrappedSize, size));
              return predicate.test(knownSize, index, element);
            }

            @Override
            public void error(@NotNull final Exception error) throws Exception {
              predicate.error(error);
            }
          });
        }
      } else {
        wrapped.materializeSize(new CancellableFutureConsumer<Integer>() {
          @Override
          public void cancellableAccept(final Integer size) {
            wrappedSize = size;
            materializePrevWhile(index, predicate);
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
      return wrappedSize < 0 ? wrapped.weightElement() : 1;
    }

    @Override
    public int weightNextWhile() {
      return wrapped.weightNextWhile();
    }

    @Override
    public int weightPrevWhile() {
      if (wrappedSize < 0) {
        return (int) Math.min(Integer.MAX_VALUE,
            (long) wrapped.weightSize() + wrapped.weightPrevWhile());
      }
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
