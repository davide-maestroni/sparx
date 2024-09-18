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

public class ReplaceSliceListFutureMaterializer<E> extends AbstractListFutureMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      ReplaceSliceListFutureMaterializer.class.getName());

  private final boolean isMaterializedAtOnce;
  private final int knownSize;

  public ReplaceSliceListFutureMaterializer(@NotNull final ListFutureMaterializer<E> wrapped,
      final int start, final int end, @NotNull final ListFutureMaterializer<E> elementsMaterializer,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    super(context, new AtomicInteger(STATUS_RUNNING));
    isMaterializedAtOnce = wrapped.isMaterializedAtOnce();
    final int knownSize = wrapped.knownSize();
    if (knownSize >= 0) {
      int materializedStart = start;
      if (materializedStart < 0) {
        materializedStart = Math.max(0, knownSize + materializedStart);
      } else {
        materializedStart = Math.min(knownSize, materializedStart);
      }
      int materializedEnd = end;
      if (materializedEnd < 0) {
        materializedEnd = Math.max(0, knownSize + materializedEnd);
      } else {
        materializedEnd = Math.min(knownSize, materializedEnd);
      }
      final int materializedLength = Math.max(0, materializedEnd - materializedStart);
      final int elementsSize = elementsMaterializer.knownSize();
      this.knownSize = elementsSize >= 0 ? SizeOverflowException.safeCast(
          (long) knownSize - materializedLength + elementsSize) : -1;
      setState(new MaterialState(wrapped, materializedStart, materializedLength, knownSize,
          elementsMaterializer, context, cancelException));
    } else {
      this.knownSize = -1;
      setState(
          new ImmaterialState(wrapped, start, end, elementsMaterializer, context, cancelException));
    }
  }

  @Override
  public boolean isMaterializedAtOnce() {
    return isMaterializedAtOnce || super.isMaterializedAtOnce();
  }

  @Override
  public int knownSize() {
    return knownSize;
  }

  private interface StateConsumer<E> {

    void accept(@NotNull ListFutureMaterializer<E> state);
  }

  private class ImmaterialState implements ListFutureMaterializer<E> {

    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final ListFutureMaterializer<E> elementsMaterializer;
    private final int end;
    private final int start;
    private final ListFutureMaterializer<E> wrapped;

    private int wrappedSize;

    public ImmaterialState(@NotNull final ListFutureMaterializer<E> wrapped, final int start,
        final int end, @NotNull final ListFutureMaterializer<E> elementsMaterializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      this.wrapped = wrapped;
      this.start = start;
      this.end = end;
      this.elementsMaterializer = elementsMaterializer;
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
      return -1;
    }

    @Override
    public void materializeCancel(@NotNull final CancellationException exception) {
      wrapped.materializeCancel(exception);
      setCancelled(exception);
    }

    @Override
    public void materializeContains(final Object element,
        @NotNull final FutureConsumer<Boolean> consumer) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<E> state) {
          state.materializeContains(element, consumer);
        }
      });
    }

    @Override
    public void materializeElement(final int index,
        @NotNull final IndexedFutureConsumer<E> consumer) {
      if (index < 0) {
        safeConsumeError(consumer, new IndexOutOfBoundsException(Integer.toString(index)), LOGGER);
      } else {
        materialized(new StateConsumer<E>() {
          @Override
          public void accept(@NotNull final ListFutureMaterializer<E> state) {
            state.materializeElement(index, consumer);
          }
        });
      }
    }

    @Override
    public void materializeElements(@NotNull final FutureConsumer<List<E>> consumer) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<E> state) {
          state.materializeElements(consumer);
        }
      });
    }

    @Override
    public void materializeEmpty(@NotNull final FutureConsumer<Boolean> consumer) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<E> state) {
          state.materializeEmpty(consumer);
        }
      });
    }

    @Override
    public void materializeHasElement(final int index,
        @NotNull final FutureConsumer<Boolean> consumer) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<E> state) {
          state.materializeHasElement(index, consumer);
        }
      });
    }

    @Override
    public void materializeNextWhile(final int index,
        @NotNull final IndexedFuturePredicate<E> predicate) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<E> state) {
          state.materializeNextWhile(index, predicate);
        }
      });
    }

    @Override
    public void materializePrevWhile(final int index,
        @NotNull final IndexedFuturePredicate<E> predicate) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<E> state) {
          state.materializePrevWhile(index, predicate);
        }
      });
    }

    @Override
    public void materializeSize(@NotNull final FutureConsumer<Integer> consumer) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<E> state) {
          state.materializeSize(consumer);
        }
      });
    }

    @Override
    public int weightContains() {
      return weightElements();
    }

    @Override
    public int weightElement() {
      return weightElements();
    }

    @Override
    public int weightElements() {
      return wrappedSize < 0 ? wrapped.weightSize() : 1;
    }

    @Override
    public int weightEmpty() {
      return weightElements();
    }

    @Override
    public int weightHasElement() {
      return weightElements();
    }

    @Override
    public int weightNextWhile() {
      return weightElements();
    }

    @Override
    public int weightPrevWhile() {
      return weightElements();
    }

    @Override
    public int weightSize() {
      return weightElements();
    }

    private void materialized(@NotNull final StateConsumer<E> consumer) {
      if (wrappedSize < 0) {
        wrapped.materializeSize(new CancellableFutureConsumer<Integer>() {
          @Override
          public void cancellableAccept(final Integer size) {
            wrappedSize = size;
            materialized(consumer);
          }

          @Override
          public void error(@NotNull final Exception error) {
            final CancellationException exception = cancelException.get();
            if (exception != null) {
              consumer.accept(setCancelled(exception));
            } else {
              consumer.accept(setFailed(error));
            }
          }
        });
      } else if (getState() == this) {
        int materializedStart = start;
        if (materializedStart < 0) {
          materializedStart = Math.max(0, wrappedSize + materializedStart);
        } else {
          materializedStart = Math.min(wrappedSize, materializedStart);
        }
        int materializedEnd = end;
        if (materializedEnd < 0) {
          materializedEnd = Math.max(0, wrappedSize + materializedEnd);
        } else {
          materializedEnd = Math.min(wrappedSize, materializedEnd);
        }
        final int materializedLength = Math.max(0, materializedEnd - materializedStart);
        consumer.accept(setState(
            new MaterialState(wrapped, materializedStart, materializedLength, wrappedSize,
                elementsMaterializer, context, cancelException)));
      } else {
        consumer.accept(getState());
      }
    }
  }

  private class MaterialState implements ListFutureMaterializer<E> {

    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final ArrayList<FutureConsumer<List<E>>> elementsConsumers = new ArrayList<FutureConsumer<List<E>>>(
        2);
    private final ListFutureMaterializer<E> elementsMaterializer;
    private final int knownSize;
    private final int length;
    private final int start;
    private final ListFutureMaterializer<E> wrapped;
    private final int wrappedSize;

    private int elementsSize;

    public MaterialState(@NotNull final ListFutureMaterializer<E> wrapped, final int start,
        final int length, final int wrappedSize,
        @NotNull final ListFutureMaterializer<E> elementsMaterializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      this.wrapped = wrapped;
      this.start = start;
      this.length = length;
      this.wrappedSize = wrappedSize;
      this.elementsMaterializer = elementsMaterializer;
      this.context = context;
      this.cancelException = cancelException;
      elementsSize = elementsMaterializer.knownSize();
      knownSize = safeSize(elementsSize);
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
      elementsMaterializer.materializeContains(element, new CancellableFutureConsumer<Boolean>() {
        @Override
        public void cancellableAccept(final Boolean contains) throws Exception {
          if (contains) {
            consumer.accept(true);
          } else if (length == 0) {
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
          } else {
            if (element == null) {
              final int end = start + length;
              wrapped.materializeNextWhile(start > 0 ? 0 : end,
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
                      if (index == start - 1) {
                        wrapped.materializeNextWhile(end, this);
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
              final int end = start + length;
              wrapped.materializeNextWhile(start > 0 ? 0 : end,
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
                      if (index == start - 1) {
                        wrapped.materializeNextWhile(end, this);
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
      } else {
        final int knownSize = safeSize(elementsSize);
        if (knownSize >= 0 && index >= knownSize) {
          safeConsumeComplete(consumer, knownSize, LOGGER);
        } else if (index >= start) {
          if (elementsSize >= 0 && index > IndexOverflowException.safeCast(
              (long) start + elementsSize)) {
            final int originalIndex = index;
            wrapped.materializeElement(
                IndexOverflowException.safeCast((long) index - elementsSize + length),
                new CancellableIndexedFutureConsumer<E>() {
                  @Override
                  public void cancellableAccept(final int size, final int index, final E element)
                      throws Exception {
                    consumer.accept(safeSize(elementsSize), originalIndex, element);
                  }

                  @Override
                  public void cancellableComplete(final int size) throws Exception {
                    consumer.complete(safeSize(elementsSize));
                  }

                  @Override
                  public void error(@NotNull final Exception error) throws Exception {
                    consumer.error(error);
                  }
                });
          } else {
            final int originalIndex = index;
            elementsMaterializer.materializeElement(index - start,
                new CancellableIndexedFutureConsumer<E>() {
                  @Override
                  public void cancellableAccept(final int size, final int index, final E element)
                      throws Exception {
                    final int knownSize = safeSize(elementsSize = Math.max(elementsSize, size));
                    consumer.accept(knownSize, originalIndex, element);
                  }

                  @Override
                  public void cancellableComplete(final int size) {
                    elementsSize = size;
                    wrapped.materializeElement(
                        IndexOverflowException.safeCast((long) originalIndex - size + length),
                        new CancellableIndexedFutureConsumer<E>() {
                          @Override
                          public void cancellableAccept(final int size, final int index,
                              final E element) throws Exception {
                            consumer.accept(safeSize(elementsSize), originalIndex, element);
                          }

                          @Override
                          public void cancellableComplete(final int size) throws Exception {
                            consumer.complete(safeSize(elementsSize));
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
        } else {
          wrapped.materializeElement(index, new CancellableIndexedFutureConsumer<E>() {
            @Override
            public void cancellableAccept(final int size, final int index, final E element)
                throws Exception {
              consumer.accept(safeSize(elementsSize), index, element);
            }

            @Override
            public void cancellableComplete(final int size) throws Exception {
              consumer.complete(safeSize(elementsSize));
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
    public void materializeElements(@NotNull final FutureConsumer<List<E>> consumer) {
      final ArrayList<FutureConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      elementsConsumers.add(consumer);
      if (elementsConsumers.size() == 1) {
        if (start == 0) {
          elementsMaterializer.materializeElements(new CancellableFutureConsumer<List<E>>() {
            @Override
            public void cancellableAccept(final List<E> patch) {
              elementsSize = patch.size();
              final ArrayList<E> elements = new ArrayList<E>(patch);
              wrapped.materializeNextWhile(length, new CancellableIndexedFuturePredicate<E>() {
                @Override
                public void cancellableComplete(final int size) {
                  setComplete(elements);
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

            @Override
            public void error(@NotNull final Exception error) {
              setError(error);
            }
          });
        } else {
          wrapped.materializeNextWhile(0, new CancellableIndexedFuturePredicate<E>() {
            private final ArrayList<E> elements = new ArrayList<E>();

            @Override
            public void cancellableComplete(final int size) {
              setComplete(elements);
            }

            @Override
            public boolean cancellableTest(final int size, final int index, final E element) {
              elements.add(element);
              if (index == start - 1) {
                elementsMaterializer.materializeElements(new CancellableFutureConsumer<List<E>>() {
                  @Override
                  public void cancellableAccept(final List<E> patch) {
                    elementsSize = patch.size();
                    elements.addAll(patch);
                    wrapped.materializeNextWhile(start + length,
                        new CancellableIndexedFuturePredicate<E>() {
                          @Override
                          public void cancellableComplete(final int size) {
                            setComplete(elements);
                          }

                          @Override
                          public boolean cancellableTest(final int size, final int index,
                              final E element) {
                            elements.add(element);
                            return true;
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
    public void materializeEmpty(@NotNull final FutureConsumer<Boolean> consumer) {
      final int knownSize = safeSize(elementsSize);
      if (knownSize >= 0) {
        safeConsume(consumer, knownSize == 0, LOGGER);
      } else {
        materializeHasElement(0, consumer);
      }
    }

    @Override
    public void materializeHasElement(final int index,
        @NotNull final FutureConsumer<Boolean> consumer) {
      if (index < 0) {
        safeConsume(consumer, false, LOGGER);
      } else if (index < start) {
        safeConsume(consumer, true, LOGGER);
      } else {
        final int knownSize = safeSize(elementsSize);
        if (knownSize >= 0) {
          safeConsume(consumer, index < knownSize, LOGGER);
        } else {
          elementsMaterializer.materializeElement(index - start,
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
                  safeConsume(consumer,
                      IndexOverflowException.safeCast((long) index - size + length) < wrappedSize,
                      LOGGER);
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
    public void materializeNextWhile(final int index,
        @NotNull final IndexedFuturePredicate<E> predicate) {
      if (index < start) {
        wrapped.materializeNextWhile(index, new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) throws Exception {
            predicate.complete(knownSize);
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            final boolean next = predicate.test(safeSize(elementsSize), index, element);
            if (index == start - 1 && next) {
              materializeNextWhile(start, predicate);
              return false;
            }
            return next;
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            predicate.error(error);
          }
        });
      } else if (elementsSize >= 0 && index >= IndexOverflowException.safeCast(
          (long) start + elementsSize)) {
        final int offset = elementsSize - length;
        wrapped.materializeNextWhile(index - offset, new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) throws Exception {
            predicate.complete(knownSize);
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            return predicate.test(safeSize(elementsSize),
                IndexOverflowException.safeCast((long) index + offset), element);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            predicate.error(error);
          }
        });
      } else {
        elementsMaterializer.materializeNextWhile(index - start,
            new CancellableIndexedFuturePredicate<E>() {
              @Override
              public void cancellableComplete(final int size) {
                elementsSize = size;
                materializeNextWhile(IndexOverflowException.safeCast((long) start + size),
                    predicate);
              }

              @Override
              public boolean cancellableTest(final int size, final int index, final E element)
                  throws Exception {
                final int knownSize = safeSize(elementsSize = Math.max(elementsSize, size));
                return predicate.test(knownSize,
                    IndexOverflowException.safeCast((long) index + start), element);
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
        @NotNull final IndexedFuturePredicate<E> predicate) {
      if (index < start) {
        wrapped.materializePrevWhile(index, new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) throws Exception {
            predicate.complete(safeSize(elementsSize));
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            return predicate.test(safeSize(elementsSize), index, element);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            predicate.error(error);
          }
        });
      } else if (elementsSize >= 0) {
        if (index < IndexOverflowException.safeCast((long) start + elementsSize)) {
          elementsMaterializer.materializePrevWhile(index - start,
              new CancellableIndexedFuturePredicate<E>() {
                @Override
                public void cancellableComplete(final int size) throws Exception {
                  elementsSize = Math.max(elementsSize, size);
                  if (start == 0) {
                    predicate.complete(safeSize(elementsSize));
                  } else {
                    materializePrevWhile(start - 1, predicate);
                  }
                }

                @Override
                public boolean cancellableTest(final int size, final int index, final E element)
                    throws Exception {
                  return predicate.test(safeSize(elementsSize = Math.max(elementsSize, size)),
                      IndexOverflowException.safeCast((long) index + start), element);
                }

                @Override
                public void error(@NotNull final Exception error) throws Exception {
                  predicate.error(error);
                }
              });
        } else {
          final int end = start + length;
          final int offset = elementsSize - length;
          wrapped.materializePrevWhile(index - offset, new CancellableIndexedFuturePredicate<E>() {
            @Override
            public void cancellableComplete(final int size) throws Exception {
              predicate.complete(safeSize(elementsSize));
            }

            @Override
            public boolean cancellableTest(final int size, final int index, final E element)
                throws Exception {
              final boolean next = predicate.test(safeSize(elementsSize),
                  IndexOverflowException.safeCast((long) index + offset), element);
              if (index == end && next) {
                materializePrevWhile(
                    IndexOverflowException.safeCast((long) start + elementsSize - 1), predicate);
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
      final int knownSize = safeSize(elementsSize);
      if (knownSize >= 0) {
        safeConsume(consumer, knownSize, LOGGER);
      } else {
        elementsMaterializer.materializeSize(new CancellableFutureConsumer<Integer>() {
          @Override
          public void cancellableAccept(final Integer size) throws Exception {
            consumer.accept(safeSize(elementsSize = size));
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
          (long) wrapped.weightNextWhile() + elementsMaterializer.weightContains());
    }

    @Override
    public int weightElement() {
      return (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightElement() + elementsMaterializer.weightElement());
    }

    @Override
    public int weightElements() {
      return elementsConsumers.isEmpty() ? (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightNextWhile() + elementsMaterializer.weightElements()) : 1;
    }

    @Override
    public int weightEmpty() {
      return elementsSize < 0 && start == 0 ? elementsMaterializer.weightElement() : 1;
    }

    @Override
    public int weightHasElement() {
      return elementsSize < 0 ? elementsMaterializer.weightElement() : 1;
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
      return elementsSize < 0 ? elementsMaterializer.weightSize() : 1;
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

    private int safeSize(final int elementsSize) {
      if (elementsSize >= 0) {
        return SizeOverflowException.safeCast((long) wrappedSize - length + elementsSize);
      }
      return -1;
    }

    private void setComplete(@NotNull final List<E> elements) {
      setDone(new ListToListFutureMaterializer<E>(elements, context));
      consumeElements(elements);
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
