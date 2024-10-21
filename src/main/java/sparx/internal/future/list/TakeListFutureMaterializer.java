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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.FutureConsumer;
import sparx.internal.future.IndexedFutureConsumer;
import sparx.internal.future.IndexedFuturePredicate;
import sparx.util.annotation.NotNegative;
import sparx.util.annotation.Positive;

public class TakeListFutureMaterializer<E> extends AbstractListFutureMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(TakeListFutureMaterializer.class.getName());

  private final boolean isMaterializedAtOnce;
  private final int knownSize;

  public TakeListFutureMaterializer(@NotNull final ListFutureMaterializer<E> wrapped,
      @Positive final int maxElements, @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    super(context);
    isMaterializedAtOnce = wrapped.isMaterializedAtOnce();
    knownSize = Math.min(wrapped.knownSize(), maxElements);
    setState(new ImmaterialState(wrapped, maxElements, context, cancelException));
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
    private final ArrayList<FutureConsumer<List<E>>> elementsConsumers = new ArrayList<FutureConsumer<List<E>>>(
        2);
    private final int maxElements;
    private final ListFutureMaterializer<E> wrapped;

    private int wrappedSize;

    public ImmaterialState(@NotNull final ListFutureMaterializer<E> wrapped, final int maxElements,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      this.wrapped = wrapped;
      this.maxElements = maxElements;
      this.context = context;
      this.cancelException = cancelException;
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
      return Math.min(wrapped.knownSize(), maxElements);
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
      final int last = maxElements - 1;
      if (element == null) {
        wrapped.materializeNextWhile(0, new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) throws Exception {
            wrappedSize = size;
            consumer.accept(false);
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            wrappedSize = Math.max(wrappedSize, size);
            if (element == null) {
              consumer.accept(true);
              return false;
            }
            if (index == last) {
              consumer.accept(false);
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
        wrapped.materializeNextWhile(0, new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) throws Exception {
            wrappedSize = size;
            consumer.accept(false);
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            wrappedSize = Math.max(wrappedSize, size);
            if (other.equals(element)) {
              consumer.accept(true);
              return false;
            }
            if (index == last) {
              consumer.accept(false);
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
      final int knownSize = safeSize(wrappedSize);
      if (knownSize >= 0 && index >= knownSize) {
        safeConsumeComplete(consumer, knownSize, LOGGER);
      } else {
        wrapped.materializeElement(index, new CancellableIndexedFutureConsumer<E>() {
          @Override
          public void cancellableAccept(final int size, final int index, final E element)
              throws Exception {
            final int knownSize = safeSize(wrappedSize = Math.max(wrappedSize, size));
            if (index < maxElements) {
              consumer.accept(knownSize, index, element);
            } else if (knownSize >= 0) {
              safeConsumeComplete(consumer, knownSize, LOGGER);
            } else {
              wrapped.materializeSize(new CancellableFutureConsumer<Integer>() {
                @Override
                public void cancellableAccept(final Integer size) throws Exception {
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
          public void cancellableComplete(final int size) throws Exception {
            final int knownSize = safeSize(wrappedSize = size);
            consumer.complete(knownSize);
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
        final ArrayList<E> elements = new ArrayList<E>();
        final int last = maxElements - 1;
        wrapped.materializeNextWhile(0, new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) {
            if (elements.isEmpty()) {
              setDone(EmptyListFutureMaterializer.<E>instance());
              consumeElements(Collections.<E>emptyList());
            } else {
              setDone(new ListToListFutureMaterializer<E>(elements, context));
              consumeElements(elements);
            }
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element) {
            wrappedSize = Math.max(wrappedSize, size);
            elements.add(element);
            if (index == last) {
              cancellableComplete(wrappedSize);
              return false;
            }
            return true;
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
      final int knownSize = safeSize(wrappedSize);
      if (knownSize >= 0) {
        safeConsume(consumer, knownSize == 0, LOGGER);
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
      if (index < 0 || index >= maxElements) {
        safeConsume(consumer, false, LOGGER);
      } else {
        final int knownSize = safeSize(wrappedSize);
        if (knownSize >= 0 && index < knownSize) {
          safeConsume(consumer, true, LOGGER);
        } else {
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
        }
      }
    }

    @Override
    public void materializeNextWhile(@NotNegative final int index,
        @NotNull final IndexedFuturePredicate<E> predicate) {
      if (index >= maxElements) {
        final int knownSize = safeSize(wrappedSize);
        if (knownSize >= 0) {
          safeConsumeComplete(predicate, knownSize, LOGGER);
        } else {
          wrapped.materializeSize(new CancellableFutureConsumer<Integer>() {
            @Override
            public void cancellableAccept(final Integer size) throws Exception {
              predicate.complete(safeSize(wrappedSize = size));
            }

            @Override
            public void error(@NotNull final Exception error) throws Exception {
              predicate.error(error);
            }
          });
        }
      } else {
        final int last = maxElements - 1;
        wrapped.materializeNextWhile(index, new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) throws Exception {
            predicate.complete(safeSize(wrappedSize = size));
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            final int knownSize = safeSize(wrappedSize = Math.max(wrappedSize, size));
            final boolean next = predicate.test(knownSize, index, element);
            if (next && index == last) {
              if (knownSize >= 0) {
                safeConsumeComplete(predicate, knownSize, LOGGER);
              } else {
                wrapped.materializeSize(new CancellableFutureConsumer<Integer>() {
                  @Override
                  public void cancellableAccept(final Integer size) throws Exception {
                    predicate.complete(safeSize(wrappedSize = size));
                  }

                  @Override
                  public void error(@NotNull final Exception error) throws Exception {
                    predicate.error(error);
                  }
                });
              }
              return false;
            }
            return next;
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            predicate.error(error);
          }
        });
      }
    }

    @Override
    public void materializePrevWhile(@NotNegative final int index,
        @NotNull final IndexedFuturePredicate<E> predicate) {
      wrapped.materializePrevWhile(Math.min(index, maxElements - 1),
          new CancellableIndexedFuturePredicate<E>() {
            @Override
            public void cancellableComplete(final int size) throws Exception {
              predicate.complete(safeSize(wrappedSize = Math.max(wrappedSize, size)));
            }

            @Override
            public boolean cancellableTest(final int size, final int index, final E element)
                throws Exception {
              final int knownSize = safeSize(wrappedSize = Math.max(wrappedSize, size));
              final boolean next = predicate.test(knownSize, index, element);
              if (next && index == 0) {
                safeConsumeComplete(predicate, knownSize, LOGGER);
                return false;
              }
              return next;
            }

            @Override
            public void error(@NotNull final Exception error) throws Exception {
              predicate.error(error);
            }
          });
    }

    @Override
    public void materializeSize(@NotNull final FutureConsumer<Integer> consumer) {
      final int knownSize = safeSize(wrappedSize);
      if (knownSize >= 0) {
        safeConsume(consumer, knownSize, LOGGER);
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
      return wrapped.weightNextWhile();
    }

    @Override
    public int weightElement() {
      return wrapped.weightElement();
    }

    @Override
    public int weightElements() {
      return elementsConsumers.isEmpty() ? wrapped.weightNextWhile() : 1;
    }

    @Override
    public int weightEmpty() {
      return wrappedSize < 0 ? wrapped.weightEmpty() : 1;
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

    private int safeSize(final int knownSize) {
      if (knownSize >= 0) {
        return Math.min(maxElements, knownSize);
      }
      return -1;
    }
  }
}
