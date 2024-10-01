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

public class TakeRightListFutureMaterializer<E> extends AbstractListFutureMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      TakeRightListFutureMaterializer.class.getName());

  private final int knownSize;

  public TakeRightListFutureMaterializer(@NotNull final ListFutureMaterializer<E> wrapped,
      @Positive final int maxElements, @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    this(wrapped, maxElements, new AtomicInteger(STATUS_RUNNING), context, cancelException);
  }

  TakeRightListFutureMaterializer(@NotNull final ListFutureMaterializer<E> wrapped,
      @Positive final int maxElements, @NotNull final AtomicInteger status,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    super(context, status);
    final int wrappedSize = wrapped.knownSize();
    knownSize = wrappedSize >= 0 ? Math.max(0, wrappedSize - maxElements) : -1;
    setState(new ImmaterialState(wrapped, maxElements, context, cancelException));
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

    public ImmaterialState(@NotNull final ListFutureMaterializer<E> wrapped,
        @Positive final int maxElements, @NotNull final ExecutionContext context,
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
      if (wrappedSize < 0) {
        wrapped.materializeSize(new CancellableFutureConsumer<Integer>() {
          @Override
          public void cancellableAccept(final Integer size) {
            wrappedSize = size;
            materializeContains(element, consumer);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      } else {
        final int start = Math.max(0, wrappedSize - maxElements);
        if (element == null) {
          wrapped.materializeNextWhile(start, new CancellableIndexedFuturePredicate<E>() {
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
              return true;
            }

            @Override
            public void error(@NotNull final Exception error) throws Exception {
              consumer.error(error);
            }
          });
        } else {
          final Object other = element;
          wrapped.materializeNextWhile(start, new CancellableIndexedFuturePredicate<E>() {
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
              return true;
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
    public void materializeElement(@NotNegative final int index,
        @NotNull final IndexedFutureConsumer<E> consumer) {
      if (index < 0) {
        safeConsumeError(consumer, new IndexOutOfBoundsException(Integer.toString(index)), LOGGER);
      } else {
        if (wrappedSize < 0) {
          wrapped.materializeSize(new CancellableFutureConsumer<Integer>() {
            @Override
            public void cancellableAccept(final Integer size) {
              wrappedSize = size;
              materializeElement(index, consumer);
            }

            @Override
            public void error(@NotNull final Exception error) throws Exception {
              consumer.error(error);
            }
          });
        } else {
          final int knownSize = Math.min(wrappedSize, maxElements);
          if (index >= knownSize) {
            safeConsumeComplete(consumer, knownSize, LOGGER);
          } else {
            final int originalIndex = index;
            wrapped.materializeElement(IndexOverflowException.safeCast(
                    (long) Math.max(0, wrappedSize - maxElements) + index),
                new CancellableIndexedFutureConsumer<E>() {
                  @Override
                  public void cancellableAccept(final int size, final int index, final E element)
                      throws Exception {
                    consumer.accept(Math.min(wrappedSize, maxElements), originalIndex, element);
                  }

                  @Override
                  public void cancellableComplete(final int size) throws Exception {
                    consumer.complete(Math.min(wrappedSize, maxElements));
                  }

                  @Override
                  public void error(@NotNull final Exception error) throws Exception {
                    consumer.error(error);
                  }
                });
          }
        }
      }
    }

    @Override
    public void materializeElements(@NotNull final FutureConsumer<List<E>> consumer) {
      final ArrayList<FutureConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      elementsConsumers.add(consumer);
      if (elementsConsumers.size() == 1) {
        if (wrappedSize < 0) {
          wrapped.materializeSize(new CancellableFutureConsumer<Integer>() {
            @Override
            public void cancellableAccept(final Integer size) {
              wrappedSize = size;
              materialize();
            }

            @Override
            public void error(@NotNull final Exception error) {
              setError(error);
            }
          });
        } else {
          materialize();
        }
      }
    }

    @Override
    public void materializeEmpty(@NotNull final FutureConsumer<Boolean> consumer) {
      if (wrappedSize >= 0) {
        safeConsume(consumer, wrappedSize == 0, LOGGER);
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
      if (wrappedSize < 0) {
        wrapped.materializeSize(new CancellableFutureConsumer<Integer>() {
          @Override
          public void cancellableAccept(final Integer size) {
            wrappedSize = size;
            materializeHasElement(index, consumer);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      } else {
        safeConsume(consumer, index < Math.min(wrappedSize, maxElements), LOGGER);
      }
    }

    @Override
    public void materializeNextWhile(@NotNegative final int index,
        @NotNull final IndexedFuturePredicate<E> predicate) {
      if (wrappedSize < 0) {
        wrapped.materializeSize(new CancellableFutureConsumer<Integer>() {
          @Override
          public void cancellableAccept(final Integer size) {
            wrappedSize = size;
            materializeNextWhile(index, predicate);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            predicate.error(error);
          }
        });
      } else {
        final int knownSize = Math.min(wrappedSize, maxElements);
        if (index >= knownSize) {
          safeConsumeComplete(predicate, knownSize, LOGGER);
        } else {
          final int start = Math.max(0, wrappedSize - maxElements);
          wrapped.materializeNextWhile(IndexOverflowException.safeCast((long) start + index),
              new CancellableIndexedFuturePredicate<E>() {
                @Override
                public void cancellableComplete(final int size) throws Exception {
                  predicate.complete(knownSize);
                }

                @Override
                public boolean cancellableTest(final int size, final int index, final E element)
                    throws Exception {
                  return predicate.test(knownSize, index - start, element);
                }

                @Override
                public void error(@NotNull final Exception error) throws Exception {
                  predicate.error(error);
                }
              });
        }
      }
    }

    @Override
    public void materializePrevWhile(@NotNegative final int index,
        @NotNull final IndexedFuturePredicate<E> predicate) {
      if (wrappedSize < 0) {
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
      } else {
        final int knownSize = Math.min(wrappedSize, maxElements);
        final int start = Math.max(0, wrappedSize - maxElements);
        wrapped.materializePrevWhile(start + Math.min(index, knownSize - 1),
            new CancellableIndexedFuturePredicate<E>() {
              @Override
              public void cancellableComplete(final int size) throws Exception {
                predicate.complete(knownSize);
              }

              @Override
              public boolean cancellableTest(final int size, final int index, final E element)
                  throws Exception {
                return predicate.test(knownSize, index - start, element);
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
        safeConsume(consumer, Math.min(wrappedSize, maxElements), LOGGER);
      } else {
        wrapped.materializeSize(new CancellableFutureConsumer<Integer>() {
          @Override
          public void cancellableAccept(final Integer size) throws Exception {
            consumer.accept(Math.min(wrappedSize = size, maxElements));
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
      if (wrappedSize < 0) {
        return (int) Math.min(Integer.MAX_VALUE,
            (long) wrapped.weightSize() + wrapped.weightNextWhile());
      }
      return wrapped.weightNextWhile();
    }

    @Override
    public int weightElement() {
      if (wrappedSize < 0) {
        return (int) Math.min(Integer.MAX_VALUE,
            (long) wrapped.weightSize() + wrapped.weightElement());
      }
      return wrapped.weightElement();
    }

    @Override
    public int weightElements() {
      if (elementsConsumers.isEmpty()) {
        if (wrappedSize < 0) {
          return (int) Math.min(Integer.MAX_VALUE,
              (long) wrapped.weightSize() + wrapped.weightNextWhile());
        }
        return wrapped.weightNextWhile();
      }
      return 1;
    }

    @Override
    public int weightEmpty() {
      return wrappedSize < 0 ? wrapped.weightEmpty() : 1;
    }

    @Override
    public int weightHasElement() {
      return wrappedSize < 0 ? wrapped.weightSize() : 1;
    }

    @Override
    public int weightNextWhile() {
      if (wrappedSize < 0) {
        return (int) Math.min(Integer.MAX_VALUE,
            (long) wrapped.weightSize() + wrapped.weightNextWhile());
      }
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

    private void materialize() {
      if (wrappedSize <= maxElements) {
        wrapped.materializeElements(new CancellableFutureConsumer<List<E>>() {
          @Override
          public void cancellableAccept(final List<E> elements) {
            setDone(new ListToListFutureMaterializer<E>(elements, context));
            consumeElements(elements);
          }

          @Override
          public void error(@NotNull final Exception error) {
            setError(error);
          }
        });
      } else {
        final int start = Math.max(0, wrappedSize - maxElements);
        wrapped.materializeNextWhile(start, new CancellableIndexedFuturePredicate<E>() {
          private final ArrayList<E> elements = new ArrayList<E>();

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
            elements.add(element);
            return true;
          }

          @Override
          public void error(@NotNull final Exception error) {
            setError(error);
          }
        });
      }
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
