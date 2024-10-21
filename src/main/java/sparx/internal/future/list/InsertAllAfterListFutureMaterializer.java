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
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.FutureConsumer;
import sparx.internal.future.IndexedFutureConsumer;
import sparx.internal.future.IndexedFuturePredicate;
import sparx.util.IndexOverflowException;
import sparx.util.SizeOverflowException;
import sparx.util.annotation.NotNegative;
import sparx.util.annotation.Positive;
import sparx.util.function.TernaryFunction;

public class InsertAllAfterListFutureMaterializer<E> extends AbstractListFutureMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      InsertAllAfterListFutureMaterializer.class.getName());

  private final int knownSize;

  public InsertAllAfterListFutureMaterializer(@NotNull final ListFutureMaterializer<E> wrapped,
      @Positive final int numElements,
      @NotNull final ListFutureMaterializer<E> elementsMaterializer,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final TernaryFunction<List<E>, Integer, List<E>, List<E>> insertAllFunction) {
    super(context);
    knownSize = safeSize(numElements, wrapped.knownSize(), elementsMaterializer.knownSize());
    setState(
        new ImmaterialState(wrapped, numElements, elementsMaterializer, context, cancelException,
            insertAllFunction));
  }

  private static int safeSize(final int numElements, final int wrappedSize,
      final int elementsSize) {
    if (wrappedSize >= 0) {
      if (numElements <= wrappedSize && elementsSize > 0) {
        return SizeOverflowException.safeCast((long) wrappedSize + elementsSize);
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
    private final ListFutureMaterializer<E> elementsMaterializer;
    private final TernaryFunction<List<E>, Integer, List<E>, List<E>> insertAllFunction;
    private final int numElements;
    private final ListFutureMaterializer<E> wrapped;

    private int elementsSize;
    private int wrappedSize;

    public ImmaterialState(@NotNull final ListFutureMaterializer<E> wrapped, final int numElements,
        @NotNull final ListFutureMaterializer<E> elementsMaterializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull TernaryFunction<List<E>, Integer, List<E>, List<E>> insertAllFunction) {
      this.wrapped = wrapped;
      this.numElements = numElements;
      this.elementsMaterializer = elementsMaterializer;
      this.context = context;
      this.cancelException = cancelException;
      this.insertAllFunction = insertAllFunction;
      wrappedSize = wrapped.knownSize();
      elementsSize = elementsMaterializer.knownSize();
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
      elementsMaterializer.materializeCancel(exception);
      setCancelled(exception);
      consumeError(exception);
    }

    @Override
    public void materializeContains(final Object element,
        @NotNull final FutureConsumer<Boolean> consumer) {
      wrapped.materializeContains(element, new CancellableFutureConsumer<Boolean>() {
        @Override
        public void cancellableAccept(final Boolean contains) throws Exception {
          if (contains) {
            consumer.accept(true);
          } else {
            if (wrappedSize >= 0) {
              if (numElements <= wrappedSize) {
                elementsMaterializer.materializeContains(element,
                    new CancellableFutureConsumer<Boolean>() {
                      @Override
                      public void cancellableAccept(final Boolean contains) throws Exception {
                        consumer.accept(contains);
                      }

                      @Override
                      public void error(@NotNull final Exception error) throws Exception {
                        consumer.error(error);
                      }
                    });
              } else {
                consumer.accept(false);
              }
            } else {
              wrapped.materializeHasElement(numElements - 1,
                  new CancellableFutureConsumer<Boolean>() {
                    @Override
                    public void cancellableAccept(final Boolean hasElement) throws Exception {
                      if (hasElement) {
                        elementsMaterializer.materializeContains(element,
                            new CancellableFutureConsumer<Boolean>() {
                              @Override
                              public void cancellableAccept(final Boolean contains)
                                  throws Exception {
                                consumer.accept(contains);
                              }

                              @Override
                              public void error(@NotNull final Exception error) throws Exception {
                                consumer.error(error);
                              }
                            });
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
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeElement(@NotNegative final int index,
        @NotNull final IndexedFutureConsumer<E> consumer) {
      if (index < numElements) {
        wrapped.materializeElement(index, new CancellableIndexedFutureConsumer<E>() {
          @Override
          public void cancellableAccept(final int size, final int index, final E element)
              throws Exception {
            final int knownSize = safeSize(numElements, wrappedSize = Math.max(wrappedSize, size),
                elementsSize);
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
      } else if (wrappedSize >= 0) {
        if (numElements <= wrappedSize) {
          final int originalIndex = index;
          elementsMaterializer.materializeElement(index - numElements,
              new CancellableIndexedFutureConsumer<E>() {
                @Override
                public void cancellableAccept(final int size, final int index, final E element)
                    throws Exception {
                  consumer.accept(safeSize(numElements, wrappedSize,
                      elementsSize = Math.max(elementsSize, size)), originalIndex, element);
                }

                @Override
                public void cancellableComplete(final int size) throws Exception {
                  final int wrappedIndex = index - size;
                  final int knownSize = safeSize(numElements, wrappedSize, elementsSize = size);
                  if (wrappedIndex < knownSize) {
                    wrapped.materializeElement(wrappedIndex,
                        new CancellableIndexedFutureConsumer<E>() {
                          @Override
                          public void cancellableAccept(final int size, final int index,
                              final E element) throws Exception {
                            consumer.accept(knownSize, originalIndex, element);
                          }

                          @Override
                          public void cancellableComplete(final int size) throws Exception {
                            consumer.complete(knownSize);
                          }

                          @Override
                          public void error(@NotNull final Exception error) throws Exception {
                            consumer.error(error);
                          }
                        });
                  } else {
                    consumer.complete(knownSize);
                  }
                }

                @Override
                public void error(@NotNull final Exception error) throws Exception {
                  consumer.error(error);
                }
              });
        } else {
          safeConsumeComplete(consumer, safeSize(numElements, wrappedSize, elementsSize), LOGGER);
        }
      } else {
        final int originalIndex = index;
        wrapped.materializeElement(numElements - 1, new CancellableIndexedFutureConsumer<E>() {
          @Override
          public void cancellableAccept(final int size, final int index, final E element) {
            elementsMaterializer.materializeElement(originalIndex - numElements,
                new CancellableIndexedFutureConsumer<E>() {
                  @Override
                  public void cancellableAccept(final int size, final int index, final E element)
                      throws Exception {
                    consumer.accept(safeSize(numElements, wrappedSize,
                        elementsSize = Math.max(elementsSize, size)), originalIndex, element);
                  }

                  @Override
                  public void cancellableComplete(final int size) throws Exception {
                    final int wrappedIndex = originalIndex - elementsSize;
                    final int knownSize = safeSize(numElements, wrappedSize, elementsSize = size);
                    if (knownSize < 0 || wrappedIndex < knownSize) {
                      wrapped.materializeElement(wrappedIndex,
                          new CancellableIndexedFutureConsumer<E>() {
                            @Override
                            public void cancellableAccept(final int size, final int index,
                                final E element) throws Exception {
                              consumer.accept(knownSize, originalIndex, element);
                            }

                            @Override
                            public void cancellableComplete(final int size) throws Exception {
                              consumer.complete(knownSize);
                            }

                            @Override
                            public void error(@NotNull final Exception error) throws Exception {
                              consumer.error(error);
                            }
                          });
                    } else {
                      consumer.complete(knownSize);
                    }
                  }

                  @Override
                  public void error(@NotNull final Exception error) throws Exception {
                    consumer.error(error);
                  }
                });
          }

          @Override
          public void cancellableComplete(final int size) throws Exception {
            final int knownSize = safeSize(numElements, wrappedSize = size, elementsSize);
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
        wrapped.materializeElements(new CancellableFutureConsumer<List<E>>() {
          @Override
          public void cancellableAccept(final List<E> elements) {
            wrappedSize = elements.size();
            final List<E> wrappedElements = elements;
            elementsMaterializer.materializeElements(new CancellableFutureConsumer<List<E>>() {
              @Override
              public void cancellableAccept(final List<E> elements) throws Exception {
                final List<E> materialized = insertAllFunction.apply(wrappedElements, numElements,
                    elements);
                setDone(new ListToListFutureMaterializer<E>(materialized, context));
                consumeElements(materialized);
              }

              @Override
              public void error(@NotNull final Exception error) {
                setError(error);
              }
            });
          }

          @Override
          public void error(@NotNull final Exception error) {
            setError(error);
          }
        });
      }
    }

    @Override
    public void materializeEmpty(@NotNull final FutureConsumer<Boolean> consumer) {
      if (wrappedSize == 0) {
        safeConsume(consumer, true, LOGGER);
      } else if (wrappedSize > 0) {
        safeConsume(consumer, false, LOGGER);
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
      if (index < wrappedSize || index < safeSize(numElements, wrappedSize, elementsSize)) {
        safeConsume(consumer, true, LOGGER);
      } else if (wrappedSize >= 0 && elementsSize >= 0) {
        safeConsume(consumer, false, LOGGER);
      } else if (index < numElements) {
        if (wrappedSize >= 0) {
          safeConsume(consumer, index < wrappedSize, LOGGER);
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
      } else if (wrappedSize >= 0) {
        if (numElements <= wrappedSize) {
          elementsMaterializer.materializeElement(index - numElements,
              new CancellableIndexedFutureConsumer<E>() {
                @Override
                public void cancellableAccept(final int size, final int index, final E element)
                    throws Exception {
                  elementsSize = Math.max(elementsSize, size);
                  consumer.accept(true);
                }

                @Override
                public void cancellableComplete(final int size) throws Exception {
                  elementsSize = size;
                  consumer.accept(index < safeSize(numElements, wrappedSize, elementsSize));
                }

                @Override
                public void error(@NotNull final Exception error) throws Exception {
                  consumer.error(error);
                }
              });
        } else {
          safeConsume(consumer, false, LOGGER);
        }
      } else {
        wrapped.materializeHasElement(numElements - 1, new CancellableFutureConsumer<Boolean>() {
          @Override
          public void cancellableAccept(final Boolean hasElement) throws Exception {
            if (hasElement) {
              elementsMaterializer.materializeElement(index - numElements,
                  new CancellableIndexedFutureConsumer<E>() {
                    @Override
                    public void cancellableAccept(final int size, final int index, final E element)
                        throws Exception {
                      elementsSize = Math.max(elementsSize, size);
                      consumer.accept(true);
                    }

                    @Override
                    public void cancellableComplete(final int size) {
                      elementsSize = size;
                      wrapped.materializeHasElement(index - size,
                          new CancellableFutureConsumer<Boolean>() {
                            @Override
                            public void cancellableAccept(final Boolean hasElement)
                                throws Exception {
                              consumer.accept(hasElement);
                            }

                            @Override
                            public void error(@NotNull final Exception error) throws Exception {
                              consumer.error(error);
                            }
                          });
                    }

                    @Override
                    public void error(@NotNull final Exception error) throws Exception {
                      consumer.error(error);
                    }
                  });
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
    public void materializeNextWhile(@NotNegative final int index,
        @NotNull final IndexedFuturePredicate<E> predicate) {
      final boolean initIsWrapped;
      final long initOffset;
      final ListFutureMaterializer<E> materializer;
      if (index < numElements) {
        initIsWrapped = true;
        initOffset = 0;
        materializer = wrapped;
      } else {
        initIsWrapped = false;
        initOffset = numElements;
        materializer = elementsMaterializer;
      }
      final int prevIndex = numElements - 1;
      materializer.materializeNextWhile((int) (index - initOffset),
          new CancellableIndexedFuturePredicate<E>() {
            private boolean isWrapped = initIsWrapped;
            private long offset = initOffset;

            @Override
            public void cancellableComplete(final int size) throws Exception {
              if (isWrapped) {
                wrappedSize = size;
                if (size == numElements) {
                  isWrapped = false;
                  offset = size;
                  elementsMaterializer.materializeNextWhile(0, this);
                } else {
                  predicate.complete(safeSize(numElements, size, elementsSize));
                }
              } else {
                elementsSize = size;
                if (wrappedSize == numElements) {
                  predicate.complete(safeSize(numElements, wrappedSize, elementsSize));
                } else {
                  isWrapped = true;
                  offset = size;
                  wrapped.materializeNextWhile(Math.max(numElements, index - size), this);
                }
              }
            }

            @Override
            public boolean cancellableTest(final int size, final int index, final E element)
                throws Exception {
              if (isWrapped) {
                final int knownSize = safeSize(numElements,
                    wrappedSize = Math.max(wrappedSize, size), elementsSize);
                final boolean next = predicate.test(knownSize,
                    IndexOverflowException.safeCast(offset + index), element);
                if (index == prevIndex && next) {
                  isWrapped = false;
                  offset = numElements;
                  elementsMaterializer.materializeNextWhile(0, this);
                  return false;
                }
                return next;
              }
              final int knownSize = safeSize(numElements, wrappedSize,
                  elementsSize = Math.max(elementsSize, size));
              return predicate.test(knownSize, IndexOverflowException.safeCast(offset + index),
                  element);
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
      if (index < numElements) {
        wrapped.materializePrevWhile(index, new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) throws Exception {
            final int knownSize = safeSize(numElements, wrappedSize = Math.max(wrappedSize, size),
                elementsSize);
            predicate.complete(knownSize);
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            final int knownSize = safeSize(numElements, wrappedSize = Math.max(wrappedSize, size),
                elementsSize);
            return predicate.test(knownSize, index, element);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            predicate.error(error);
          }
        });
      } else if (elementsSize >= 0) {
        final boolean initIsWrapped;
        final long initOffset;
        final ListFutureMaterializer<E> materializer;
        if (index < SizeOverflowException.safeCast((long) numElements + elementsSize)) {
          initIsWrapped = false;
          initOffset = numElements;
          materializer = elementsMaterializer;
        } else {
          initIsWrapped = true;
          initOffset = elementsSize;
          materializer = wrapped;
        }
        materializer.materializePrevWhile((int) (index - initOffset),
            new CancellableIndexedFuturePredicate<E>() {
              private boolean isWrapped = initIsWrapped;
              private long offset = initOffset;

              @Override
              public void cancellableComplete(final int size) throws Exception {
                if (isWrapped) {
                  predicate.complete(
                      safeSize(numElements, wrappedSize = Math.max(wrappedSize, size),
                          elementsSize));
                } else {
                  elementsSize = Math.max(elementsSize, size);
                  isWrapped = true;
                  offset = 0;
                  wrapped.materializePrevWhile(numElements - 1, this);
                }
              }

              @Override
              public boolean cancellableTest(final int size, final int index, final E element)
                  throws Exception {
                if (isWrapped) {
                  final int knownSize = safeSize(numElements,
                      wrappedSize = Math.max(wrappedSize, size), elementsSize);
                  final boolean next = predicate.test(knownSize,
                      IndexOverflowException.safeCast(offset + index), element);
                  if (index == numElements && next) {
                    isWrapped = false;
                    offset = numElements;
                    elementsMaterializer.materializePrevWhile(elementsSize, this);
                    return false;
                  }
                  return next;
                }
                final int knownSize = safeSize(numElements, wrappedSize,
                    elementsSize = Math.max(elementsSize, size));
                return predicate.test(knownSize, IndexOverflowException.safeCast(offset + index),
                    element);
              }

              @Override
              public void error(@NotNull final Exception error) throws Exception {
                predicate.error(error);
              }
            });
      } else {
        elementsMaterializer.materializeSize(new CancellableFutureConsumer<Integer>() {
          @Override
          public void cancellableAccept(final Integer size) {
            elementsSize = size;
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
        if (numElements > wrappedSize) {
          safeConsume(consumer, wrappedSize, LOGGER);
        } else {
          if (elementsSize >= 0) {
            safeConsume(consumer, safeSize(numElements, wrappedSize, elementsSize), LOGGER);
          } else {
            elementsMaterializer.materializeSize(new CancellableFutureConsumer<Integer>() {
              @Override
              public void cancellableAccept(final Integer size) throws Exception {
                consumer.accept(safeSize(numElements, wrappedSize, elementsSize = size));
              }

              @Override
              public void error(@NotNull final Exception error) throws Exception {
                consumer.error(error);
              }
            });
          }
        }
      } else {
        wrapped.materializeSize(new CancellableFutureConsumer<Integer>() {
          @Override
          public void cancellableAccept(final Integer size) {
            wrappedSize = size;
            materializeSize(consumer);
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
      final ListFutureMaterializer<E> wrapped = this.wrapped;
      return (int) Math.min(Integer.MAX_VALUE,
          ((long) wrapped.weightContains() * 2) + (wrappedSize < 0 ? wrapped.weightHasElement() : 1)
              + elementsMaterializer.weightContains());
    }

    @Override
    public int weightElement() {
      final ListFutureMaterializer<E> wrapped = this.wrapped;
      return (int) Math.min(Integer.MAX_VALUE,
          (long) (wrappedSize < 0 ? wrapped.weightHasElement() : 1) + wrapped.weightElement()
              + elementsMaterializer.weightElement());
    }

    @Override
    public int weightElements() {
      if (elementsConsumers.isEmpty()) {
        return (int) Math.min(Integer.MAX_VALUE,
            (long) wrapped.weightElements() + elementsMaterializer.weightElements());
      }
      return 1;
    }

    @Override
    public int weightEmpty() {
      return wrappedSize < 0 ? wrapped.weightEmpty() : 1;
    }

    @Override
    public int weightHasElement() {
      return (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightHasElement() + elementsMaterializer.weightElement());
    }

    @Override
    public int weightNextWhile() {
      return (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightNextWhile() + elementsMaterializer.weightNextWhile());
    }

    @Override
    public int weightPrevWhile() {
      return (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightPrevWhile() + elementsMaterializer.weightPrevWhile());
    }

    @Override
    public int weightSize() {
      if (wrappedSize >= 0) {
        if (numElements > wrappedSize) {
          return 1;
        }
        return elementsSize < 1 ? elementsMaterializer.weightSize() : 1;
      }
      return (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightSize() + (elementsSize < 1 ? elementsMaterializer.weightSize() : 1));
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
