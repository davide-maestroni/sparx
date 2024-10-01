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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.FutureConsumer;
import sparx.internal.future.IndexedFutureConsumer;
import sparx.internal.future.IndexedFuturePredicate;
import sparx.util.IndexOverflowException;
import sparx.util.annotation.NotNegative;
import sparx.util.annotation.Positive;
import sparx.util.function.BinaryFunction;

public class RemoveAfterListFutureMaterializer<E> extends AbstractListFutureMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      RemoveAfterListFutureMaterializer.class.getName());

  private final int knownSize;

  public RemoveAfterListFutureMaterializer(@NotNull final ListFutureMaterializer<E> wrapped,
      @Positive final int numElements, @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final BinaryFunction<List<E>, Integer, List<E>> removeFunction) {
    this(wrapped, numElements, new AtomicInteger(STATUS_RUNNING), context, cancelException,
        removeFunction);
  }

  RemoveAfterListFutureMaterializer(@NotNull final ListFutureMaterializer<E> wrapped,
      @Positive final int numElements, @NotNull final AtomicInteger status,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final BinaryFunction<List<E>, Integer, List<E>> removeFunction) {
    super(context, status);
    knownSize = safeSize(numElements, wrapped.knownSize());
    setState(new ImmaterialState(wrapped, numElements, context, cancelException, removeFunction));
  }

  private static int safeSize(final int numElements, final int wrappedSize) {
    if (wrappedSize >= 0) {
      if (numElements < wrappedSize) {
        return wrappedSize - 1;
      }
      return wrappedSize;
    }
    return -1;
  }

  @Override
  public int knownSize() {
    return knownSize;
  }

  private class ImmaterialState implements ListFutureMaterializer<E> {

    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final ArrayList<FutureConsumer<List<E>>> elementsConsumers = new ArrayList<FutureConsumer<List<E>>>(
        2);
    private final int numElements;
    private final BinaryFunction<List<E>, Integer, List<E>> removeFunction;
    private final ListFutureMaterializer<E> wrapped;

    private int wrappedSize;

    public ImmaterialState(@NotNull final ListFutureMaterializer<E> wrapped, final int numElements,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final BinaryFunction<List<E>, Integer, List<E>> removeFunction) {
      this.wrapped = wrapped;
      this.numElements = numElements;
      this.context = context;
      this.cancelException = cancelException;
      this.removeFunction = removeFunction;
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
      if (element == null) {
        wrapped.materializeNextWhile(numElements == 0 ? 1 : 0,
            new CancellableIndexedFuturePredicate<E>() {
              @Override
              public void cancellableComplete(final int size) throws Exception {
                consumer.accept(false);
              }

              @Override
              public boolean cancellableTest(final int size, final int index, final E element)
                  throws Exception {
                if (element == null) {
                  consumer.accept(true);
                  return false;
                }
                if (index == numElements - 1) {
                  wrapped.materializeNextWhile((int) Math.min(Integer.MAX_VALUE, (long) index + 2),
                      this);
                  return false;
                }
                return true;
              }

              @Override
              public void error(@NotNull final Exception error) throws Exception {
                consumer.error(error);
              }
            });
      } else {
        final Object other = element;
        wrapped.materializeNextWhile(numElements == 0 ? 1 : 0,
            new CancellableIndexedFuturePredicate<E>() {
              @Override
              public void cancellableComplete(final int size) throws Exception {
                consumer.accept(false);
              }

              @Override
              public boolean cancellableTest(final int size, final int index, final E element)
                  throws Exception {
                if (other.equals(element)) {
                  consumer.accept(true);
                  return false;
                }
                if (index == numElements - 1) {
                  wrapped.materializeNextWhile((int) Math.min(Integer.MAX_VALUE, (long) index + 2),
                      this);
                  return false;
                }
                return true;
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
      if (index < numElements) {
        wrapped.materializeElement(index, new CancellableIndexedFutureConsumer<E>() {
          @Override
          public void cancellableAccept(final int size, final int index, final E element)
              throws Exception {
            final int knownSize = safeSize(numElements, wrappedSize = Math.max(wrappedSize, size));
            consumer.accept(knownSize, index, element);
          }

          @Override
          public void cancellableComplete(final int size) throws Exception {
            consumer.complete(wrappedSize = size);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      } else {
        final int originalIndex = index;
        wrapped.materializeElement((int) Math.min(Integer.MAX_VALUE, (long) index + 1),
            new CancellableIndexedFutureConsumer<E>() {
              @Override
              public void cancellableAccept(final int size, final int index, final E element)
                  throws Exception {
                final int knownSize = safeSize(numElements,
                    wrappedSize = Math.max(wrappedSize, size));
                consumer.accept(knownSize, originalIndex, element);
              }

              @Override
              public void cancellableComplete(final int size) throws Exception {
                consumer.complete(safeSize(numElements, wrappedSize = size));
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
            final List<E> materialized = removeFunction.apply(elements, numElements);
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
      if (wrappedSize == 0 || (wrappedSize == 1 && numElements == 0)) {
        safeConsume(consumer, true, LOGGER);
      } else if (wrappedSize > 0) {
        safeConsume(consumer, false, LOGGER);
      } else if (numElements == 0) {
        wrapped.materializeHasElement(1, new CancellableFutureConsumer<Boolean>() {
          @Override
          public void cancellableAccept(final Boolean hasElement) throws Exception {
            consumer.accept(!hasElement);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      } else {
        wrapped.materializeEmpty(new CancellableFutureConsumer<Boolean>() {
          @Override
          public void cancellableAccept(final Boolean empty) throws Exception {
            consumer.accept(empty);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
    }

    @Override
    public void materializeHasElement(@NotNegative final int index,
        @NotNull final FutureConsumer<Boolean> consumer) {
      if (index < safeSize(numElements, wrappedSize)) {
        safeConsume(consumer, true, LOGGER);
      } else if (wrappedSize >= 0) {
        safeConsume(consumer, false, LOGGER);
      } else if (index < numElements) {
        wrapped.materializeHasElement(index, new CancellableFutureConsumer<Boolean>() {
          @Override
          public void cancellableAccept(final Boolean hasElement) throws Exception {
            consumer.accept(hasElement);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      } else {
        wrapped.materializeHasElement(IndexOverflowException.safeCast((long) index + 1),
            new CancellableFutureConsumer<Boolean>() {
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
    public void materializeNextWhile(int index,
        @NotNull final IndexedFuturePredicate<E> predicate) {
      if (index >= numElements && index < Integer.MAX_VALUE) {
        ++index;
      }
      wrapped.materializeNextWhile(index, new CancellableIndexedFuturePredicate<E>() {
        @Override
        public void cancellableComplete(final int size) throws Exception {
          predicate.complete(safeSize(numElements, wrappedSize = size));
        }

        @Override
        public boolean cancellableTest(final int size, final int index, final E element)
            throws Exception {
          final int knownSize = safeSize(numElements, wrappedSize = Math.max(wrappedSize, size));
          if (index < numElements) {
            if (index == numElements - 1) {
              if (predicate.test(knownSize, index, element)) {
                wrapped.materializeNextWhile((int) Math.min(Integer.MAX_VALUE, (long) index + 2),
                    this);
              }
              return false;
            }
            return predicate.test(knownSize, index, element);
          } else if (index > numElements) {
            return predicate.test(knownSize, index - 1, element);
          }
          return true;
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          predicate.error(error);
        }
      });
    }

    @Override
    public void materializePrevWhile(int index,
        @NotNull final IndexedFuturePredicate<E> predicate) {
      if (index >= numElements && index < Integer.MAX_VALUE) {
        ++index;
      }
      wrapped.materializePrevWhile(index, new CancellableIndexedFuturePredicate<E>() {
        @Override
        public void cancellableComplete(final int size) throws Exception {
          final int knownSize = safeSize(numElements, wrappedSize = Math.max(wrappedSize, size));
          predicate.complete(knownSize);
        }

        @Override
        public boolean cancellableTest(final int size, final int index, final E element)
            throws Exception {
          final int knownSize = safeSize(numElements, wrappedSize = Math.max(wrappedSize, size));
          if (index < numElements) {
            return predicate.test(knownSize, index, element);
          } else if (index > numElements) {
            if (index == numElements + 1) {
              if (predicate.test(knownSize, index - 1, element)) {
                if (index == 1) {
                  predicate.complete(knownSize);
                } else {
                  wrapped.materializePrevWhile(index - 2, this);
                }
              }
              return false;
            }
            return predicate.test(knownSize, index - 1, element);
          }
          return true;
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          predicate.error(error);
        }
      });
    }

    @Override
    public void materializeSize(@NotNull final FutureConsumer<Integer> consumer) {
      if (wrappedSize >= 0) {
        safeConsume(consumer, safeSize(numElements, wrappedSize), LOGGER);
      } else {
        wrapped.materializeSize(new CancellableFutureConsumer<Integer>() {
          @Override
          public void cancellableAccept(final Integer size) throws Exception {
            consumer.accept(safeSize(numElements, wrappedSize = size));
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
      return wrapped.weightNextWhile();
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
      return wrappedSize < 0 ? numElements > 0 ? wrapped.weightEmpty() : wrapped.weightHasElement()
          : 1;
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
