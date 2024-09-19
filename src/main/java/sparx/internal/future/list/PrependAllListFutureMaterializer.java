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
import sparx.util.SizeOverflowException;
import sparx.util.function.BinaryFunction;

public class PrependAllListFutureMaterializer<E> extends AbstractListFutureMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      PrependAllListFutureMaterializer.class.getName());

  private final int knownSize;
  private final boolean isMaterializedAtOnce;

  public PrependAllListFutureMaterializer(@NotNull final ListFutureMaterializer<E> wrapped,
      @NotNull final ListFutureMaterializer<E> elementsMaterializer,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final BinaryFunction<List<E>, List<E>, List<E>> prependFunction) {
    super(context, new AtomicInteger(STATUS_RUNNING));
    knownSize = safeSize(wrapped.knownSize(), elementsMaterializer.knownSize());
    isMaterializedAtOnce =
        wrapped.isMaterializedAtOnce() && elementsMaterializer.isMaterializedAtOnce();
    setState(new ImmaterialState(wrapped, elementsMaterializer, context, cancelException,
        prependFunction));
  }

  private static int safeSize(final int wrappedSize, final int elementsSize) {
    if (wrappedSize >= 0) {
      if (elementsSize > 0) {
        return SizeOverflowException.safeCast((long) wrappedSize + elementsSize);
      }
      return wrappedSize;
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
    private final ArrayList<FutureConsumer<List<E>>> elementsConsumers = new ArrayList<FutureConsumer<List<E>>>(
        2);
    private final ListFutureMaterializer<E> elementsMaterializer;
    private final ListFutureMaterializer<E> wrapped;
    private final BinaryFunction<List<E>, List<E>, List<E>> prependFunction;

    private int elementsSize;
    private int wrappedSize;

    public ImmaterialState(@NotNull final ListFutureMaterializer<E> wrapped,
        @NotNull final ListFutureMaterializer<E> elementsMaterializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final BinaryFunction<List<E>, List<E>, List<E>> prependFunction) {
      this.wrapped = wrapped;
      this.elementsMaterializer = elementsMaterializer;
      this.context = context;
      this.cancelException = cancelException;
      this.prependFunction = prependFunction;
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
      return wrapped.isMaterializedAtOnce() && elementsMaterializer.isMaterializedAtOnce();
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
          }
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeElement(final int index,
        @NotNull final IndexedFutureConsumer<E> consumer) {
      if (index < 0) {
        safeConsumeError(consumer, new IndexOutOfBoundsException(Integer.toString(index)), LOGGER);
      } else if (elementsSize >= 0 && index >= elementsSize) {
        final int originalIndex = index;
        wrapped.materializeElement(index - elementsSize, new CancellableIndexedFutureConsumer<E>() {
          @Override
          public void cancellableAccept(final int size, final int index, final E element)
              throws Exception {
            final int knownSize = safeSize(wrappedSize = Math.max(wrappedSize, size), elementsSize);
            consumer.accept(knownSize, originalIndex, element);
          }

          @Override
          public void cancellableComplete(final int size) throws Exception {
            consumer.complete(safeSize(wrappedSize = size, elementsSize));
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      } else {
        elementsMaterializer.materializeElement(index, new CancellableIndexedFutureConsumer<E>() {
          @Override
          public void cancellableAccept(final int size, final int index, final E element)
              throws Exception {
            final int knownSize = safeSize(wrappedSize,
                elementsSize = Math.max(elementsSize, size));
            consumer.accept(knownSize, index, element);
          }

          @Override
          public void cancellableComplete(final int size) {
            elementsSize = size;
            final int originalIndex = index;
            wrapped.materializeElement(index - size, new CancellableIndexedFutureConsumer<E>() {
              @Override
              public void cancellableAccept(final int size, final int index, final E element)
                  throws Exception {
                final int knownSize = safeSize(wrappedSize = Math.max(wrappedSize, size),
                    elementsSize);
                consumer.accept(knownSize, originalIndex, element);
              }

              @Override
              public void cancellableComplete(final int size) throws Exception {
                final int knownSize = safeSize(wrappedSize = size, elementsSize);
                consumer.complete(knownSize);
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
                final List<E> materialized = prependFunction.apply(wrappedElements, elements);
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
        if (elementsSize >= 0) {
          safeConsume(consumer, elementsSize == 0, LOGGER);
        } else {
          elementsMaterializer.materializeEmpty(new CancellableFutureConsumer<Boolean>() {
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
      } else if (wrappedSize > 0 || elementsSize > 0) {
        safeConsume(consumer, false, LOGGER);
      } else {
        wrapped.materializeEmpty(new CancellableFutureConsumer<Boolean>() {
          @Override
          public void cancellableAccept(final Boolean empty) throws Exception {
            if (!empty) {
              consumer.accept(false);
            } else if (elementsSize == 0) {
              consumer.accept(true);
            } else {
              elementsMaterializer.materializeEmpty(new CancellableFutureConsumer<Boolean>() {
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
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
    }

    @Override
    public void materializeHasElement(final int index,
        @NotNull final FutureConsumer<Boolean> consumer) {
      if (index < 0) {
        safeConsume(consumer, false, LOGGER);
      } else if (index < wrappedSize || index < elementsSize || index < safeSize(wrappedSize,
          elementsSize)) {
        safeConsume(consumer, true, LOGGER);
      } else if (wrappedSize >= 0) {
        if (elementsSize >= 0) {
          safeConsume(consumer, false, LOGGER);
        } else {
          elementsMaterializer.materializeHasElement(index,
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
      } else if (elementsSize >= 0) {
        wrapped.materializeHasElement(index - elementsSize,
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
      } else {
        elementsMaterializer.materializeElement(index, new CancellableIndexedFutureConsumer<E>() {
          @Override
          public void cancellableAccept(final int size, final int index, final E element)
              throws Exception {
            elementsSize = Math.max(elementsSize, size);
            consumer.accept(true);
          }

          @Override
          public void cancellableComplete(final int size) throws Exception {
            elementsSize = size;
            if (index < safeSize(wrappedSize, elementsSize)) {
              consumer.accept(false);
            } else {
              wrapped.materializeHasElement(index - size, new CancellableFutureConsumer<Boolean>() {
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
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
    }

    @Override
    public void materializeNextWhile(final int index,
        @NotNull final IndexedFuturePredicate<E> predicate) {
      elementsMaterializer.materializeNextWhile(index, new CancellableIndexedFuturePredicate<E>() {
        @Override
        public void cancellableComplete(final int size) {
          elementsSize = size;
          wrapped.materializeNextWhile(Math.max(0, index - size),
              new CancellableIndexedFuturePredicate<E>() {
                @Override
                public void cancellableComplete(final int size) throws Exception {
                  final int knownSize = safeSize(wrappedSize = size, elementsSize);
                  predicate.complete(knownSize);
                }

                @Override
                public boolean cancellableTest(final int size, final int index, final E element)
                    throws Exception {
                  final int knownSize = safeSize(wrappedSize = Math.max(wrappedSize, size),
                      elementsSize);
                  return predicate.test(knownSize,
                      IndexOverflowException.safeCast((long) elementsSize + index), element);
                }

                @Override
                public void error(@NotNull final Exception error) throws Exception {
                  predicate.error(error);
                }
              });
        }

        @Override
        public boolean cancellableTest(final int size, final int index, final E element)
            throws Exception {
          final int knownSize = safeSize(wrappedSize, elementsSize = Math.max(elementsSize, size));
          return predicate.test(knownSize, index, element);
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
      if (elementsSize >= 0) {
        if (index < elementsSize) {
          elementsMaterializer.materializePrevWhile(index,
              new CancellableIndexedFuturePredicate<E>() {
                @Override
                public void cancellableComplete(final int size) throws Exception {
                  final int knownSize = safeSize(wrappedSize, elementsSize);
                  predicate.complete(knownSize);
                }

                @Override
                public boolean cancellableTest(final int size, final int index, final E element)
                    throws Exception {
                  final int knownSize = safeSize(wrappedSize, elementsSize);
                  return predicate.test(knownSize, index, element);
                }

                @Override
                public void error(@NotNull final Exception error) throws Exception {
                  predicate.error(error);
                }
              });
        } else {
          wrapped.materializePrevWhile(Math.max(0, index - elementsSize),
              new CancellableIndexedFuturePredicate<E>() {
                @Override
                public void cancellableComplete(final int size) {
                  wrappedSize = Math.max(wrappedSize, size);
                  elementsMaterializer.materializePrevWhile(elementsSize - 1,
                      new CancellableIndexedFuturePredicate<E>() {
                        @Override
                        public void cancellableComplete(final int size) throws Exception {
                          final int knownSize = safeSize(wrappedSize, elementsSize);
                          predicate.complete(knownSize);
                        }

                        @Override
                        public boolean cancellableTest(final int size, final int index,
                            final E element) throws Exception {
                          final int knownSize = safeSize(wrappedSize, elementsSize);
                          return predicate.test(knownSize, index, element);
                        }

                        @Override
                        public void error(@NotNull final Exception error) throws Exception {
                          predicate.error(error);
                        }
                      });
                }

                @Override
                public boolean cancellableTest(final int size, final int index, final E element)
                    throws Exception {
                  final int knownSize = safeSize(wrappedSize = Math.max(wrappedSize, size),
                      elementsSize);
                  return predicate.test(knownSize,
                      IndexOverflowException.safeCast((long) elementsSize + index), element);
                }

                @Override
                public void error(@NotNull final Exception error) throws Exception {
                  predicate.error(error);
                }
              });
        }
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
        if (elementsSize >= 0) {
          safeConsume(consumer, safeSize(wrappedSize, elementsSize), LOGGER);
        } else {
          elementsMaterializer.materializeSize(new CancellableFutureConsumer<Integer>() {
            @Override
            public void cancellableAccept(final Integer size) throws Exception {
              consumer.accept(safeSize(wrappedSize, elementsSize = size));
            }

            @Override
            public void error(@NotNull final Exception error) throws Exception {
              consumer.error(error);
            }
          });
        }
      } else if (elementsSize >= 0) {
        wrapped.materializeSize(new CancellableFutureConsumer<Integer>() {
          @Override
          public void cancellableAccept(final Integer size) throws Exception {
            consumer.accept(safeSize(wrappedSize = size, elementsSize));
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      } else {
        wrapped.materializeSize(new CancellableFutureConsumer<Integer>() {
          @Override
          public void cancellableAccept(final Integer size) throws Exception {
            wrappedSize = size;
            if (elementsSize >= 0) {
              consumer.accept(safeSize(wrappedSize, elementsSize));
            } else {
              elementsMaterializer.materializeSize(new CancellableFutureConsumer<Integer>() {
                @Override
                public void cancellableAccept(final Integer size) throws Exception {
                  consumer.accept(safeSize(wrappedSize, elementsSize = size));
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
    }

    @Override
    public int weightContains() {
      return (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightContains() + elementsMaterializer.weightContains());
    }

    @Override
    public int weightElement() {
      return (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightElement() + elementsMaterializer.weightElement());
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
      if (wrappedSize > 0 || elementsSize > 0) {
        return 1;
      }
      if (wrappedSize == 0 && elementsSize < 0) {
        return elementsMaterializer.weightEmpty();
      }
      if (wrappedSize < 0 && elementsSize == 0) {
        return wrapped.weightEmpty();
      }
      return (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightEmpty() + elementsMaterializer.weightEmpty());
    }

    @Override
    public int weightHasElement() {
      if (wrappedSize >= 0 && elementsSize >= 0) {
        return 1;
      }
      if (wrappedSize >= 0) {
        return elementsMaterializer.weightHasElement();
      }
      if (elementsSize >= 0) {
        return wrapped.weightElement();
      }
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
      if (elementsSize >= 0) {
        final ListFutureMaterializer<E> elementsMaterializer = this.elementsMaterializer;
        return (int) Math.min(Integer.MAX_VALUE,
            (long) wrapped.weightPrevWhile() + elementsMaterializer.weightSize()
                + elementsMaterializer.weightPrevWhile());
      }
      return (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightPrevWhile() + elementsMaterializer.weightPrevWhile());
    }

    @Override
    public int weightSize() {
      if (wrappedSize >= 0 && elementsSize >= 0) {
        return 1;
      }
      if (wrappedSize >= 0) {
        return elementsMaterializer.weightSize();
      }
      if (elementsSize >= 0) {
        return wrapped.weightSize();
      }
      return (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightSize() + elementsMaterializer.weightSize());
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