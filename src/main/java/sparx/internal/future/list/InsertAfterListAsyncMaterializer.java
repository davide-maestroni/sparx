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
import sparx.internal.future.AsyncConsumer;
import sparx.internal.future.IndexedAsyncConsumer;
import sparx.util.IndexOverflowException;
import sparx.util.SizeOverflowException;
import sparx.util.function.TernaryFunction;

public class InsertAfterListAsyncMaterializer<E> extends AbstractListAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      InsertAfterListAsyncMaterializer.class.getName());

  private final int knownSize;

  // numElements: positive
  public InsertAfterListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      final int numElements, final E element,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final TernaryFunction<List<E>, Integer, E, List<E>> insertFunction) {
    super(new AtomicInteger(STATUS_RUNNING));
    knownSize = safeSize(numElements, wrapped.knownSize());
    setState(new ImmaterialState(wrapped, numElements, element, cancelException, insertFunction));
  }

  private static int safeSize(final int numElements, final int wrappedSize) {
    if (wrappedSize >= 0) {
      if (numElements <= wrappedSize) {
        return SizeOverflowException.safeCast((long) wrappedSize + 1);
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
    private final E element;
    private final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = new ArrayList<AsyncConsumer<List<E>>>(
        2);
    private final TernaryFunction<List<E>, Integer, E, List<E>> insertFunction;
    private final int numElements;
    private final ListAsyncMaterializer<E> wrapped;

    private int wrappedSize;

    public ImmaterialState(@NotNull final ListAsyncMaterializer<E> wrapped, final int numElements,
        final E element, @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final TernaryFunction<List<E>, Integer, E, List<E>> insertFunction) {
      this.wrapped = wrapped;
      this.numElements = numElements;
      this.element = element;
      this.cancelException = cancelException;
      this.insertFunction = insertFunction;
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
        @NotNull final AsyncConsumer<Boolean> consumer) {
      wrapped.materializeContains(element, new CancellableAsyncConsumer<Boolean>() {
        @Override
        public void cancellableAccept(final Boolean contains) throws Exception {
          if (contains) {
            consumer.accept(contains);
          } else {
            if (wrappedSize >= 0) {
              final E inserted = ImmaterialState.this.element;
              consumer.accept(numElements <= wrappedSize && (element == inserted || (element != null
                  && element.equals(inserted))));
            } else {
              wrapped.materializeSize(new CancellableAsyncConsumer<Integer>() {
                @Override
                public void cancellableAccept(final Integer size) throws Exception {
                  wrappedSize = size;
                  final E inserted = ImmaterialState.this.element;
                  consumer.accept(numElements <= size && (element == inserted || (element != null
                      && element.equals(inserted))));
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
    public void materializeEach(@NotNull final IndexedAsyncConsumer<E> consumer) {
      wrapped.materializeEach(new CancellableIndexedAsyncConsumer<E>() {
        @Override
        public void cancellableAccept(final int size, final int index, final E element)
            throws Exception {
          final int knownSize = safeSize(numElements, wrappedSize = Math.max(wrappedSize, size));
          if (index < numElements) {
            consumer.accept(knownSize, index, element);
          } else {
            if (index == numElements) {
              consumer.accept(knownSize, index, element);
            }
            consumer.accept(knownSize, IndexOverflowException.safeCast((long) index + 1), element);
          }
        }

        @Override
        public void cancellableComplete(final int size) throws Exception {
          final int knownSize = safeSize(numElements, wrappedSize = size);
          consumer.complete(knownSize);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
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
      } else if (index == numElements) {
        if (wrappedSize >= 0) {
          final int size = safeSize(numElements, wrappedSize);
          if (index < size) {
            safeConsume(consumer, size, index, element, LOGGER);
          } else {
            safeConsumeComplete(consumer, size, LOGGER);
          }
        } else {
          wrapped.materializeSize(new CancellableAsyncConsumer<Integer>() {
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
        }
      } else {
        final int originalIndex = index;
        wrapped.materializeElement(IndexOverflowException.safeCast((long) index + 1),
            new CancellableIndexedAsyncConsumer<E>() {
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
    public void materializeElements(@NotNull final AsyncConsumer<List<E>> consumer) {
      final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      elementsConsumers.add(consumer);
      if (elementsConsumers.size() == 1) {
        wrapped.materializeElements(new CancellableAsyncConsumer<List<E>>() {
          @Override
          public void cancellableAccept(final List<E> elements) throws Exception {
            final List<E> materialized = insertFunction.apply(elements, numElements, element);
            setState(new ListToListAsyncMaterializer<E>(materialized));
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
    public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
      if (wrappedSize == 0) {
        safeConsume(consumer, numElements != 0, LOGGER);
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
      } else if (index < wrappedSize || index < safeSize(numElements, wrappedSize)) {
        safeConsume(consumer, true, LOGGER);
      } else if (wrappedSize >= 0) {
        safeConsume(consumer, false, LOGGER);
      } else {
        wrapped.materializeSize(new CancellableAsyncConsumer<Integer>() {
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
      }
    }

    @Override
    public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
      if (wrappedSize >= 0) {
        safeConsume(consumer, safeSize(numElements, wrappedSize), LOGGER);
      } else {
        wrapped.materializeSize(new CancellableAsyncConsumer<Integer>() {
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
      return (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightContains() + wrapped.weightSize());
    }

    @Override
    public int weightEach() {
      return wrapped.weightEach();
    }

    @Override
    public int weightElement() {
      return (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightElement() + wrapped.weightSize());
    }

    @Override
    public int weightElements() {
      return elementsConsumers.isEmpty() ? wrapped.weightElements() : 1;
    }

    @Override
    public int weightEmpty() {
      return wrapped.weightEmpty();
    }

    @Override
    public int weightHasElement() {
      return wrapped.weightSize();
    }

    @Override
    public int weightSize() {
      return wrapped.weightSize();
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
  }
}
