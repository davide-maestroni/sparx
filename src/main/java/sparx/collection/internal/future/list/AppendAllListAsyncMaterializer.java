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
package sparx.collection.internal.future.list;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.collection.internal.future.AsyncConsumer;
import sparx.collection.internal.future.IndexedAsyncConsumer;
import sparx.util.IndexOverflowException;
import sparx.util.Require;
import sparx.util.SizeOverflowException;
import sparx.util.function.BinaryFunction;

public class AppendAllListAsyncMaterializer<E> extends AbstractListAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      AppendAllListAsyncMaterializer.class.getName());

  private static final int STATUS_CANCELLED = 2;
  private static final int STATUS_DONE = 1;
  private static final int STATUS_RUNNING = 0;

  private final int knownSize;
  private final AtomicInteger status = new AtomicInteger(STATUS_RUNNING);

  private ListAsyncMaterializer<E> state;

  public AppendAllListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      @NotNull final ListAsyncMaterializer<E> elementsMaterializer,
      @NotNull final AtomicBoolean isCancelled,
      @NotNull final BinaryFunction<List<E>, List<E>, List<E>> appendFunction) {
    knownSize = safeSize(wrapped.knownSize(), elementsMaterializer.knownSize());
    state = new ImmaterialState(wrapped, elementsMaterializer,
        Require.notNull(isCancelled, "isCancelled"),
        Require.notNull(appendFunction, "appendFunction"));
  }

  private static int safeIndex(final int wrappedSize, final int elementsIndex) {
    if (wrappedSize >= 0) {
      return IndexOverflowException.safeCast((long) wrappedSize + elementsIndex);
    }
    return -1;
  }

  private static int safeSize(final int wrappedSize, final int elementsSize) {
    if (wrappedSize >= 0 && elementsSize > 0) {
      return SizeOverflowException.safeCast((long) wrappedSize + elementsSize);
    }
    return -1;
  }

  @Override
  public boolean isCancelled() {
    return status.get() == STATUS_CANCELLED;
  }

  @Override
  public boolean isDone() {
    return status.get() != STATUS_RUNNING;
  }

  @Override
  public int knownSize() {
    return knownSize;
  }

  @Override
  public void materializeCancel(final boolean mayInterruptIfRunning) {
    state.materializeCancel(mayInterruptIfRunning);
  }

  @Override
  public void materializeContains(final Object element,
      @NotNull final AsyncConsumer<Boolean> consumer) {
    state.materializeContains(element, consumer);
  }

  @Override
  public void materializeEach(@NotNull final IndexedAsyncConsumer<E> consumer) {
    state.materializeEach(consumer);
  }

  @Override
  public void materializeElement(final int index, @NotNull final IndexedAsyncConsumer<E> consumer) {
    state.materializeElement(index, consumer);
  }

  @Override
  public void materializeElements(@NotNull final AsyncConsumer<List<E>> consumer) {
    state.materializeElements(consumer);
  }

  @Override
  public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
    state.materializeEmpty(consumer);
  }

  @Override
  public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
    state.materializeSize(consumer);
  }

  private class ImmaterialState extends AbstractListAsyncMaterializer<E> {

    private final BinaryFunction<List<E>, List<E>, List<E>> appendFunction;
    private final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = new ArrayList<AsyncConsumer<List<E>>>(
        2);
    private final ListAsyncMaterializer<E> elementsMaterializer;
    private final AtomicBoolean isCancelled;
    private final ListAsyncMaterializer<E> wrapped;

    private int elementsSize = -1;
    private int wrappedSize = -1;

    public ImmaterialState(@NotNull final ListAsyncMaterializer<E> wrapped,
        @NotNull final ListAsyncMaterializer<E> elementsMaterializer,
        @NotNull final AtomicBoolean isCancelled,
        @NotNull final BinaryFunction<List<E>, List<E>, List<E>> appendFunction) {
      this.wrapped = wrapped;
      this.elementsMaterializer = elementsMaterializer;
      this.isCancelled = isCancelled;
      this.appendFunction = appendFunction;
    }

    @Override
    public boolean isCancelled() {
      return status.get() == STATUS_CANCELLED;
    }

    @Override
    public boolean isDone() {
      return status.get() != STATUS_RUNNING;
    }

    @Override
    public int knownSize() {
      return knownSize;
    }

    @Override
    public void materializeCancel(final boolean mayInterruptIfRunning) {
      wrapped.materializeCancel(mayInterruptIfRunning);
      elementsMaterializer.materializeCancel(mayInterruptIfRunning);
      setState(new CancelledListAsyncMaterializer<E>(), STATUS_CANCELLED);
    }

    @Override
    public void materializeContains(final Object element,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      wrapped.materializeContains(element, new AsyncConsumer<Boolean>() {
        @Override
        public void accept(final Boolean contains) throws Exception {
          if (contains) {
            consumer.accept(true);
          } else {
            elementsMaterializer.materializeContains(element, consumer);
          }
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeEach(@NotNull final IndexedAsyncConsumer<E> consumer) {
      wrapped.materializeEach(new IndexedAsyncConsumer<E>() {
        @Override
        public void accept(final int size, final int index, final E element) throws Exception {
          final int knownSize = safeSize(wrappedSize = Math.max(wrappedSize, size), elementsSize);
          consumer.accept(knownSize, index, element);
        }

        @Override
        public void complete(final int size) {
          wrappedSize = size;
          elementsMaterializer.materializeEach(new IndexedAsyncConsumer<E>() {
            @Override
            public void accept(final int size, final int index, final E element) throws Exception {
              final int knownSize = safeSize(wrappedSize,
                  elementsSize = Math.max(elementsSize, size));
              final int knownIndex = safeIndex(wrappedSize, index);
              consumer.accept(knownSize, knownIndex, element);
            }

            @Override
            public void complete(final int size) throws Exception {
              consumer.complete(safeSize(wrappedSize, elementsSize = Math.max(elementsSize, size)));
            }

            @Override
            public void error(final int index, @NotNull Exception error) throws Exception {
              consumer.error(safeIndex(wrappedSize, index), error);
            }
          });
        }

        @Override
        public void error(final int index, @NotNull final Exception error) throws Exception {
          consumer.error(index, error);
        }
      });
    }

    @Override
    public void materializeElement(final int index,
        @NotNull final IndexedAsyncConsumer<E> consumer) {
      if (index < 0) {
        safeConsumeError(consumer, index, new IndexOutOfBoundsException(Integer.toString(index)),
            LOGGER);
      } else {
        wrapped.materializeElement(index, new IndexedAsyncConsumer<E>() {
          @Override
          public void accept(final int size, final int index, final E element) throws Exception {
            final int knownSize = safeSize(wrappedSize = Math.max(wrappedSize, size), elementsSize);
            consumer.accept(knownSize, index, element);
          }

          @Override
          public void complete(final int size) {
            wrappedSize = size;
            final int originalIndex = index;
            elementsMaterializer.materializeElement(index - size, new IndexedAsyncConsumer<E>() {
              @Override
              public void accept(final int size, final int index, final E element)
                  throws Exception {
                final int knownSize = safeSize(wrappedSize,
                    elementsSize = Math.max(elementsSize, size));
                consumer.accept(knownSize, originalIndex, element);
              }

              @Override
              public void complete(final int size) throws Exception {
                consumer.complete(
                    safeSize(wrappedSize, elementsSize = Math.max(elementsSize, size)));
              }

              @Override
              public void error(final int index, @NotNull final Exception error) throws Exception {
                consumer.error(originalIndex, error);
              }
            });
          }

          @Override
          public void error(final int index, @NotNull final Exception error) throws Exception {
            consumer.error(index, error);
          }
        });
      }
    }

    @Override
    public void materializeElements(@NotNull final AsyncConsumer<List<E>> consumer) {
      final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      elementsConsumers.add(consumer);
      if (elementsConsumers.size() == 1) {
        wrapped.materializeElements(new AsyncConsumer<List<E>>() {
          @Override
          public void accept(final List<E> elements) {
            wrappedSize = elements.size();
            final List<E> wrappedElements = elements;
            elementsMaterializer.materializeElements(new AsyncConsumer<List<E>>() {
              @Override
              public void accept(final List<E> elements) {
                try {
                  final List<E> materialized = appendFunction.apply(wrappedElements, elements);
                  setState(new ListToListAsyncMaterializer<E>(materialized), STATUS_DONE);
                  consumeElements(materialized);
                } catch (final Exception e) {
                  if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                  }
                  if (isCancelled.get()) {
                    setState(new CancelledListAsyncMaterializer<E>(), STATUS_CANCELLED);
                    consumeError(new CancellationException());
                  } else {
                    setState(new FailedListAsyncMaterializer<E>(e), STATUS_DONE);
                    consumeError(e);
                  }
                }
              }

              @Override
              public void error(@NotNull final Exception error) {
                if (isCancelled.get()) {
                  setState(new CancelledListAsyncMaterializer<E>(), STATUS_CANCELLED);
                  consumeError(new CancellationException());
                } else {
                  setState(new FailedListAsyncMaterializer<E>(error), STATUS_DONE);
                  consumeError(error);
                }
              }
            });
          }

          @Override
          public void error(@NotNull final Exception error) {
            if (isCancelled.get()) {
              setState(new CancelledListAsyncMaterializer<E>(), STATUS_CANCELLED);
              consumeError(new CancellationException());
            } else {
              setState(new FailedListAsyncMaterializer<E>(error), STATUS_DONE);
              consumeError(error);
            }
          }
        });
      }
    }

    @Override
    public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
      wrapped.materializeEmpty(new AsyncConsumer<Boolean>() {
        @Override
        public void accept(final Boolean empty) throws Exception {
          if (!empty) {
            consumer.accept(false);
          } else {
            elementsMaterializer.materializeEmpty(consumer);
          }
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
      if (wrappedSize >= 0) {
        if (elementsSize >= 0) {
          safeConsume(consumer, safeSize(wrappedSize, elementsSize), LOGGER);
        } else {
          elementsMaterializer.materializeSize(new AsyncConsumer<Integer>() {
            @Override
            public void accept(final Integer size) throws Exception {
              consumer.accept(safeSize(wrappedSize, elementsSize = size));
            }

            @Override
            public void error(@NotNull final Exception error) throws Exception {
              consumer.error(error);
            }
          });
        }
      } else if (elementsSize >= 0) {
        wrapped.materializeSize(new AsyncConsumer<Integer>() {
          @Override
          public void accept(final Integer size) throws Exception {
            consumer.accept(safeSize(wrappedSize = size, elementsSize));
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      } else {
        wrapped.materializeSize(new AsyncConsumer<Integer>() {
          @Override
          public void accept(final Integer size) throws Exception {
            wrappedSize = size;
            if (elementsSize >= 0) {
              consumer.accept(safeSize(wrappedSize, elementsSize));
            } else {
              elementsMaterializer.materializeSize(new AsyncConsumer<Integer>() {
                @Override
                public void accept(final Integer size) throws Exception {
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

    private void setState(@NotNull final ListAsyncMaterializer<E> newState, final int statusCode) {
      if (status.compareAndSet(STATUS_RUNNING, statusCode)) {
        state = newState;
      }
    }
  }
}
