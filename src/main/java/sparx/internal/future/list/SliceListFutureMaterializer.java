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
import sparx.util.UncheckedException;

public class SliceListFutureMaterializer<E> extends AbstractListFutureMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      SliceListFutureMaterializer.class.getName());

  private final boolean isMaterializedAtOnce;
  private final int knownSize;

  public SliceListFutureMaterializer(@NotNull final ListFutureMaterializer<E> wrapped,
      final int start, final int end, @NotNull final ExecutionContext context,
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
      this.knownSize = materializedLength;
      if (materializedLength == 0) {
        try {
          setDone(EmptyListFutureMaterializer.<E>instance());
        } catch (final Exception e) {
          throw UncheckedException.throwUnchecked(e);
        }
      } else {
        setState(new MaterialState(wrapped, materializedStart, materializedLength, context,
            cancelException));
      }
    } else {
      this.knownSize = 1;
      setState(new ImmaterialState(wrapped, start, end, context, cancelException));
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
    private final int end;
    private final int start;
    private final ListFutureMaterializer<E> wrapped;

    private int wrappedSize;

    public ImmaterialState(@NotNull final ListFutureMaterializer<E> wrapped, final int start,
        final int end, @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      this.wrapped = wrapped;
      this.start = start;
      this.end = end;
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
        if (materializedLength == 0) {
          try {
            consumer.accept(setDone(EmptyListFutureMaterializer.<E>instance()));
          } catch (final Exception e) {
            final CancellationException exception = cancelException.get();
            if (exception != null) {
              consumer.accept(setCancelled(exception));
            } else {
              consumer.accept(setFailed(e));
            }
          }
        } else {
          consumer.accept(setState(
              new MaterialState(wrapped, materializedStart, materializedLength, context,
                  cancelException)));
        }
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
    private final int length;
    private final int start;
    private final ListFutureMaterializer<E> wrapped;

    public MaterialState(@NotNull final ListFutureMaterializer<E> wrapped, final int start,
        final int length, @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      this.wrapped = wrapped;
      this.start = start;
      this.length = length;
      this.context = context;
      this.cancelException = cancelException;
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
      return length;
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
      final int end = start + length - 1;
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
            return index < end;
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
            return index < end;
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
      } else {
        final int knownSize = length;
        if (index >= knownSize) {
          safeConsumeComplete(consumer, knownSize, LOGGER);
        } else {
          final int originalIndex = index;
          wrapped.materializeElement(IndexOverflowException.safeCast((long) index + start),
              new CancellableIndexedFutureConsumer<E>() {
                @Override
                public void cancellableAccept(final int size, final int index, final E element)
                    throws Exception {
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
        }
      }
    }

    @Override
    public void materializeElements(@NotNull final FutureConsumer<List<E>> consumer) {
      final ArrayList<FutureConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      elementsConsumers.add(consumer);
      if (elementsConsumers.size() == 1) {
        final ArrayList<E> elements = new ArrayList<E>();
        final int end = start + length - 1;
        wrapped.materializeNextWhile(start, new CancellableIndexedFuturePredicate<E>() {
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
            if (index < end) {
              return true;
            }
            cancellableComplete(length);
            return false;
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
      safeConsume(consumer, length == 0, LOGGER);
    }

    @Override
    public void materializeHasElement(final int index,
        @NotNull final FutureConsumer<Boolean> consumer) {
      safeConsume(consumer, index >= 0 && index < length, LOGGER);
    }

    @Override
    public void materializeNextWhile(final int index,
        @NotNull final IndexedFuturePredicate<E> predicate) {
      if (index >= length) {
        safeConsumeComplete(predicate, length, LOGGER);
      } else {
        final int end = start + length - 1;
        wrapped.materializeNextWhile(IndexOverflowException.safeCast((long) index + start),
            new CancellableIndexedFuturePredicate<E>() {
              @Override
              public void cancellableComplete(final int size) throws Exception {
                predicate.complete(length);
              }

              @Override
              public boolean cancellableTest(final int size, final int index, final E element)
                  throws Exception {
                final boolean next = predicate.test(length, index - start, element);
                if (index < end) {
                  return next;
                }
                if (next) {
                  cancellableComplete(length);
                }
                return false;
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
      wrapped.materializePrevWhile(Math.min(index, start + length - 1),
          new CancellableIndexedFuturePredicate<E>() {
            @Override
            public void cancellableComplete(final int size) throws Exception {
              predicate.complete(length);
            }

            @Override
            public boolean cancellableTest(final int size, final int index, final E element)
                throws Exception {
              final boolean next = predicate.test(length, index - start, element);
              if (index > start) {
                return next;
              }
              if (next) {
                cancellableComplete(length);
              }
              return false;
            }

            @Override
            public void error(@NotNull final Exception error) throws Exception {
              predicate.error(error);
            }
          });
    }

    @Override
    public void materializeSize(@NotNull final FutureConsumer<Integer> consumer) {
      safeConsume(consumer, length, LOGGER);
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
      return 1;
    }

    @Override
    public int weightHasElement() {
      return 1;
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
      return 1;
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
