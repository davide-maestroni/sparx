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
import sparx.util.function.BinaryFunction;

public class AppendAllListAsyncMaterializer<E> extends AbstractListAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      AppendAllListAsyncMaterializer.class.getName());

  private final int knownSize;
  private final boolean isMaterializedAtOnce;

  public AppendAllListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      @NotNull final ListAsyncMaterializer<E> elementsMaterializer,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final BinaryFunction<List<E>, List<E>, List<E>> appendFunction) {
    super(context, new AtomicInteger(STATUS_RUNNING));
    knownSize = safeSize(wrapped.knownSize(), elementsMaterializer.knownSize());
    isMaterializedAtOnce =
        wrapped.isMaterializedAtOnce() && elementsMaterializer.isMaterializedAtOnce();
    setState(new ImmaterialState(wrapped, elementsMaterializer, context, cancelException,
        appendFunction));
  }

  private static int safeSize(final int wrappedSize, final int elementsSize) {
    if (wrappedSize >= 0 && elementsSize >= 0) {
      if (elementsSize > 0) {
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

  @Override
  public boolean isMaterializedAtOnce() {
    return isMaterializedAtOnce || super.isMaterializedAtOnce();
  }

  private class ImmaterialState implements ListAsyncMaterializer<E> {

    private final BinaryFunction<List<E>, List<E>, List<E>> appendFunction;
    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = new ArrayList<AsyncConsumer<List<E>>>(
        2);
    private final ListAsyncMaterializer<E> elementsMaterializer;
    private final ListAsyncMaterializer<E> wrapped;

    private int elementsSize;
    private int wrappedSize;

    public ImmaterialState(@NotNull final ListAsyncMaterializer<E> wrapped,
        @NotNull final ListAsyncMaterializer<E> elementsMaterializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final BinaryFunction<List<E>, List<E>, List<E>> appendFunction) {
      this.wrapped = wrapped;
      this.elementsMaterializer = elementsMaterializer;
      this.context = context;
      this.cancelException = cancelException;
      this.appendFunction = appendFunction;
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
        @NotNull final AsyncConsumer<Boolean> consumer) {
      wrapped.materializeContains(element, new CancellableAsyncConsumer<Boolean>() {
        @Override
        public void cancellableAccept(final Boolean contains) throws Exception {
          if (contains) {
            consumer.accept(true);
          } else {
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
      } else {
        final int knownSize = safeSize(wrappedSize, elementsSize);
        if (knownSize >= 0 && index >= knownSize) {
          safeConsumeComplete(consumer, knownSize, LOGGER);
        } else if (wrappedSize >= 0 && index >= wrappedSize) {
          final int originalIndex = index;
          elementsMaterializer.materializeElement(index - wrappedSize,
              new CancellableIndexedAsyncConsumer<E>() {
                @Override
                public void cancellableAccept(final int size, final int index, final E element)
                    throws Exception {
                  final int knownSize = safeSize(wrappedSize,
                      elementsSize = Math.max(elementsSize, size));
                  consumer.accept(knownSize, originalIndex, element);
                }

                @Override
                public void cancellableComplete(final int size) throws Exception {
                  final int knownSize = safeSize(wrappedSize, elementsSize = size);
                  consumer.complete(knownSize);
                }

                @Override
                public void error(@NotNull final Exception error) throws Exception {
                  consumer.error(error);
                }
              });
        } else {
          wrapped.materializeElement(index, new CancellableIndexedAsyncConsumer<E>() {
            @Override
            public void cancellableAccept(final int size, final int index, final E element)
                throws Exception {
              final int knownSize = safeSize(wrappedSize = Math.max(wrappedSize, size),
                  elementsSize);
              consumer.accept(knownSize, index, element);
            }

            @Override
            public void cancellableComplete(final int size) {
              wrappedSize = size;
              final int originalIndex = index;
              elementsMaterializer.materializeElement(index - size,
                  new CancellableIndexedAsyncConsumer<E>() {
                    @Override
                    public void cancellableAccept(final int size, final int index, final E element)
                        throws Exception {
                      final int knownSize = safeSize(wrappedSize,
                          elementsSize = Math.max(elementsSize, size));
                      consumer.accept(knownSize, originalIndex, element);
                    }

                    @Override
                    public void cancellableComplete(final int size) throws Exception {
                      final int knownSize = safeSize(wrappedSize, elementsSize = size);
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
                final List<E> materialized = appendFunction.apply(wrappedElements, elements);
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
        if (elementsSize >= 0) {
          safeConsume(consumer, elementsSize == 0, LOGGER);
        } else {
          elementsMaterializer.materializeEmpty(new CancellableAsyncConsumer<Boolean>() {
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
        wrapped.materializeEmpty(new CancellableAsyncConsumer<Boolean>() {
          @Override
          public void cancellableAccept(final Boolean empty) throws Exception {
            if (!empty) {
              consumer.accept(false);
            } else if (elementsSize == 0) {
              consumer.accept(true);
            } else {
              elementsMaterializer.materializeEmpty(new CancellableAsyncConsumer<Boolean>() {
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
        @NotNull final AsyncConsumer<Boolean> consumer) {
      if (index < 0) {
        safeConsume(consumer, false, LOGGER);
      } else if (index < wrappedSize || index < safeSize(wrappedSize, elementsSize)) {
        safeConsume(consumer, true, LOGGER);
      } else if (wrappedSize >= 0) {
        if (elementsSize >= 0) {
          safeConsume(consumer, false, LOGGER);
        } else {
          elementsMaterializer.materializeHasElement(index - wrappedSize,
              new CancellableAsyncConsumer<Boolean>() {
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
      } else {
        wrapped.materializeElement(index, new CancellableIndexedAsyncConsumer<E>() {
          @Override
          public void cancellableAccept(final int size, final int index, final E element)
              throws Exception {
            wrappedSize = Math.max(wrappedSize, size);
            consumer.accept(true);
          }

          @Override
          public void cancellableComplete(final int size) throws Exception {
            wrappedSize = size;
            if (index < safeSize(wrappedSize, elementsSize)) {
              consumer.accept(true);
            } else {
              elementsMaterializer.materializeHasElement(index - size,
                  new CancellableAsyncConsumer<Boolean>() {
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
        @NotNull final IndexedAsyncPredicate<E> predicate) {
      wrapped.materializeNextWhile(index, new CancellableIndexedAsyncPredicate<E>() {
        @Override
        public void cancellableComplete(final int size) {
          wrappedSize = size;
          elementsMaterializer.materializeNextWhile(Math.max(0, index - size),
              new CancellableIndexedAsyncPredicate<E>() {
                @Override
                public void cancellableComplete(final int size) throws Exception {
                  final int knownSize = safeSize(wrappedSize, elementsSize = size);
                  predicate.complete(knownSize);
                }

                @Override
                public boolean cancellableTest(final int size, final int index, final E element)
                    throws Exception {
                  final int knownSize = safeSize(wrappedSize,
                      elementsSize = Math.max(elementsSize, size));
                  return predicate.test(knownSize,
                      IndexOverflowException.safeCast((long) wrappedSize + index), element);
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
          final int knownSize = safeSize(wrappedSize = Math.max(wrappedSize, size), elementsSize);
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
        @NotNull final IndexedAsyncPredicate<E> predicate) {
      if (wrappedSize >= 0) {
        if (index >= wrappedSize) {
          elementsMaterializer.materializePrevWhile(index - wrappedSize,
              new CancellableIndexedAsyncPredicate<E>() {
                @Override
                public void cancellableComplete(final int size) {
                  elementsSize = size;
                  wrapped.materializePrevWhile(index, new CancellableIndexedAsyncPredicate<E>() {
                    @Override
                    public void cancellableComplete(final int size) throws Exception {
                      final int knownSize = safeSize(wrappedSize = Math.max(wrappedSize, size),
                          elementsSize);
                      predicate.complete(knownSize);
                    }

                    @Override
                    public boolean cancellableTest(final int size, final int index, final E element)
                        throws Exception {
                      final int knownSize = safeSize(wrappedSize = Math.max(wrappedSize, size),
                          elementsSize);
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
                  final int knownSize = safeSize(wrappedSize,
                      elementsSize = Math.max(elementsSize, size));
                  return predicate.test(knownSize,
                      IndexOverflowException.safeCast((long) wrappedSize + index), element);
                }

                @Override
                public void error(@NotNull final Exception error) throws Exception {
                  predicate.error(error);
                }
              });
        } else {
          wrapped.materializePrevWhile(index, new CancellableIndexedAsyncPredicate<E>() {
            @Override
            public void cancellableComplete(final int size) throws Exception {
              final int knownSize = safeSize(wrappedSize = Math.max(wrappedSize, size),
                  elementsSize);
              predicate.complete(knownSize);
            }

            @Override
            public boolean cancellableTest(final int size, final int index, final E element)
                throws Exception {
              final int knownSize = safeSize(wrappedSize = Math.max(wrappedSize, size),
                  elementsSize);
              return predicate.test(knownSize, index, element);
            }

            @Override
            public void error(@NotNull final Exception error) throws Exception {
              predicate.error(error);
            }
          });
        }
      } else {
        wrapped.materializeSize(new CancellableAsyncConsumer<Integer>() {
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
    public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
      if (wrappedSize >= 0) {
        if (elementsSize >= 0) {
          safeConsume(consumer, safeSize(wrappedSize, elementsSize), LOGGER);
        } else {
          elementsMaterializer.materializeSize(new CancellableAsyncConsumer<Integer>() {
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
        wrapped.materializeSize(new CancellableAsyncConsumer<Integer>() {
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
        wrapped.materializeSize(new CancellableAsyncConsumer<Integer>() {
          @Override
          public void cancellableAccept(final Integer size) throws Exception {
            wrappedSize = size;
            if (elementsSize >= 0) {
              consumer.accept(safeSize(wrappedSize, elementsSize));
            } else {
              elementsMaterializer.materializeSize(new CancellableAsyncConsumer<Integer>() {
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
          (long) wrapped.weightElement() + elementsMaterializer.weightHasElement());
    }

    @Override
    public int weightNextWhile() {
      return (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightNextWhile() + elementsMaterializer.weightNextWhile());
    }

    @Override
    public int weightPrevWhile() {
      if (wrappedSize >= 0) {
        return (int) Math.min(Integer.MAX_VALUE,
            (long) wrapped.weightPrevWhile() + elementsMaterializer.weightPrevWhile());
      }
      return (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightSize() + wrapped.weightPrevWhile()
              + elementsMaterializer.weightPrevWhile());
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
