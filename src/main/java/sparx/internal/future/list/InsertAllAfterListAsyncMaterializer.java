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
import sparx.util.IndexOverflowException;
import sparx.util.SizeOverflowException;
import sparx.util.annotation.Positive;
import sparx.util.function.TernaryFunction;

public class InsertAllAfterListAsyncMaterializer<E> extends AbstractListAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      InsertAllAfterListAsyncMaterializer.class.getName());

  private final int knownSize;

  public InsertAllAfterListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      @Positive final int numElements, @NotNull final ListAsyncMaterializer<E> elementsMaterializer,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final TernaryFunction<List<E>, Integer, List<E>, List<E>> insertAllFunction) {
    super(new AtomicInteger(STATUS_RUNNING));
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

  private class ImmaterialState implements ListAsyncMaterializer<E> {

    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = new ArrayList<AsyncConsumer<List<E>>>(
        2);
    private final ListAsyncMaterializer<E> elementsMaterializer;
    private final TernaryFunction<List<E>, Integer, List<E>, List<E>> insertAllFunction;
    private final int numElements;
    private final ListAsyncMaterializer<E> wrapped;

    private int elementsSize;
    private int wrappedSize;

    public ImmaterialState(@NotNull final ListAsyncMaterializer<E> wrapped, final int numElements,
        @NotNull final ListAsyncMaterializer<E> elementsMaterializer,
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
        @NotNull final AsyncConsumer<Boolean> consumer) {
      wrapped.materializeContains(element, new CancellableAsyncConsumer<Boolean>() {
        @Override
        public void cancellableAccept(final Boolean contains) throws Exception {
          if (contains) {
            consumer.accept(true);
          } else {
            if (wrappedSize >= 0) {
              if (numElements <= wrappedSize) {
                elementsMaterializer.materializeContains(element,
                    new CancellableAsyncConsumer<Boolean>() {
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
                  new CancellableAsyncConsumer<Boolean>() {
                    @Override
                    public void cancellableAccept(final Boolean hasElement) throws Exception {
                      if (hasElement) {
                        elementsMaterializer.materializeContains(element,
                            new CancellableAsyncConsumer<Boolean>() {
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
    public void materializeDone(@NotNull final AsyncConsumer<List<E>> consumer) {
      safeConsumeError(consumer, new UnsupportedOperationException(), LOGGER);
    }

    @Override
    public void materializeElement(final int index,
        @NotNull final IndexedAsyncConsumer<E> consumer) {
      if (index < 0) {
        safeConsumeError(consumer, new IndexOutOfBoundsException(Integer.toString(index)), LOGGER);
      } else if (index < numElements) {
        wrapped.materializeElement(index, new CancellableIndexedAsyncConsumer<E>() {
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
              new CancellableIndexedAsyncConsumer<E>() {
                @Override
                public void cancellableAccept(final int size, final int index, final E element)
                    throws Exception {
                  consumer.accept(safeSize(numElements, wrappedSize,
                      elementsSize = Math.max(elementsSize, size)), originalIndex, element);
                }

                @Override
                public void cancellableComplete(final int size) {
                  final int wrappedIndex = index - elementsSize;
                  final int knownSize = safeSize(numElements, wrappedSize, elementsSize = size);
                  if (wrappedIndex < knownSize) {
                    wrapped.materializeElement(wrappedIndex,
                        new CancellableIndexedAsyncConsumer<E>() {
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
                    safeConsumeComplete(consumer, knownSize, LOGGER);
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
        wrapped.materializeElement(numElements - 1, new CancellableIndexedAsyncConsumer<E>() {
          @Override
          public void cancellableAccept(final int size, final int index, final E element) {
            elementsMaterializer.materializeElement(originalIndex - numElements,
                new CancellableIndexedAsyncConsumer<E>() {
                  @Override
                  public void cancellableAccept(final int size, final int index, final E element)
                      throws Exception {
                    consumer.accept(safeSize(numElements, wrappedSize,
                        elementsSize = Math.max(elementsSize, size)), originalIndex, element);
                  }

                  @Override
                  public void cancellableComplete(final int size) {
                    final int wrappedIndex = index - elementsSize;
                    final int knownSize = safeSize(numElements, wrappedSize, elementsSize = size);
                    if (wrappedIndex < knownSize) {
                      wrapped.materializeElement(wrappedIndex,
                          new CancellableIndexedAsyncConsumer<E>() {
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
                      safeConsumeComplete(consumer, knownSize, LOGGER);
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
    public void materializeElements(@NotNull final AsyncConsumer<List<E>> consumer) {
      final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      elementsConsumers.add(consumer);
      if (elementsConsumers.size() == 1) {
        wrapped.materializeElements(new CancellableAsyncConsumer<List<E>>() {
          @Override
          public void cancellableAccept(final List<E> elements) {
            wrappedSize = elements.size();
            final List<E> wrappedElements = elements;
            elementsMaterializer.materializeElements(new CancellableAsyncConsumer<List<E>>() {
              @Override
              public void cancellableAccept(final List<E> elements) throws Exception {
                final List<E> materialized = insertAllFunction.apply(wrappedElements, numElements,
                    elements);
                setState(new ListToListAsyncMaterializer<E>(materialized, context));
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
    public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
      if (wrappedSize == 0) {
        safeConsume(consumer, true, LOGGER);
      } else if (wrappedSize > 0) {
        safeConsume(consumer, false, LOGGER);
      } else {
        wrapped.materializeEmpty(new CancellableAsyncConsumer<Boolean>() {
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
    public void materializeHasElement(final int index,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      if (index < 0) {
        safeConsume(consumer, false, LOGGER);
      } else if (index < wrappedSize || index < safeSize(numElements, wrappedSize, elementsSize)) {
        safeConsume(consumer, true, LOGGER);
      } else if (wrappedSize >= 0 && elementsSize >= 0) {
        safeConsume(consumer, false, LOGGER);
      } else if (index < numElements) {
        if (wrappedSize >= 0) {
          safeConsume(consumer, index < wrappedSize, LOGGER);
        } else {
          wrapped.materializeHasElement(index, new CancellableAsyncConsumer<Boolean>() {
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
              new CancellableIndexedAsyncConsumer<E>() {
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
        wrapped.materializeHasElement(numElements - 1, new CancellableAsyncConsumer<Boolean>() {
          @Override
          public void cancellableAccept(final Boolean hasElement) throws Exception {
            if (hasElement) {
              elementsMaterializer.materializeElement(index - numElements,
                  new CancellableIndexedAsyncConsumer<E>() {
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
    public void materializeNextWhile(final int index,
        @NotNull final IndexedAsyncPredicate<E> predicate) {
      final boolean initIsWrapped;
      final long initOffset;
      final ListAsyncMaterializer<E> materializer;
      if (index < numElements) {
        initIsWrapped = true;
        initOffset = 0;
        materializer = wrapped;
      } else {
        initIsWrapped = false;
        initOffset = numElements;
        materializer = elementsMaterializer;
      }
      materializer.materializeNextWhile((int) (index - initOffset),
          new CancellableIndexedAsyncPredicate<E>() {
            private boolean isWrapped = initIsWrapped;
            private long offset = initOffset;

            @Override
            public void cancellableComplete(final int size) throws Exception {
              if (isWrapped) {
                wrappedSize = size;
                if (size == numElements) {
                  offset = size;
                  elementsMaterializer.materializeNextWhile(0, this);
                } else {
                  predicate.complete(safeSize(numElements, size, elementsSize));
                }
              } else {
                isWrapped = true;
                offset = elementsSize = size;
                wrapped.materializeNextWhile(numElements, this);
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
                if (index == numElements - 1 && next) {
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
    public void materializePrevWhile(final int index,
        @NotNull final IndexedAsyncPredicate<E> predicate) {
      if (index < numElements) {
        wrapped.materializePrevWhile(index, new CancellableIndexedAsyncPredicate<E>() {
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
        final ListAsyncMaterializer<E> materializer;
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
            new CancellableIndexedAsyncPredicate<E>() {
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
        elementsMaterializer.materializeSize(new CancellableAsyncConsumer<Integer>() {
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
    public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
      if (wrappedSize >= 0) {
        if (numElements > wrappedSize) {
          safeConsume(consumer, wrappedSize, LOGGER);
        } else {
          if (elementsSize >= 0) {
            safeConsume(consumer, safeSize(numElements, wrappedSize, elementsSize), LOGGER);
          } else {
            elementsMaterializer.materializeSize(new CancellableAsyncConsumer<Integer>() {
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
        wrapped.materializeSize(new CancellableAsyncConsumer<Integer>() {
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
      final ListAsyncMaterializer<E> wrapped = this.wrapped;
      return (int) Math.min(Integer.MAX_VALUE,
          (long) (wrappedSize < 0 ? wrapped.weightHasElement() : 1) + wrapped.weightContains()
              + elementsMaterializer.weightContains());
    }

    @Override
    public int weightElement() {
      final ListAsyncMaterializer<E> wrapped = this.wrapped;
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