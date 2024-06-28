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
import sparx.concurrent.ExecutionContext.Task;
import sparx.internal.future.AsyncConsumer;
import sparx.internal.future.IndexedAsyncConsumer;
import sparx.util.SizeOverflowException;
import sparx.util.function.TernaryFunction;

public class InsertAllAfterListAsyncMaterializer<E> extends AbstractListAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      InsertAllAfterListAsyncMaterializer.class.getName());

  private final int knownSize;

  // numElements: positive
  public InsertAllAfterListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      final int numElements, @NotNull final ListAsyncMaterializer<E> elementsMaterializer,
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
            consumer.accept(contains);
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
              wrapped.materializeSize(new CancellableAsyncConsumer<Integer>() {
                @Override
                public void cancellableAccept(final Integer size) throws Exception {
                  wrappedSize = size;
                  if (numElements <= size) {
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
      new MaterializingEachAsyncConsumer(consumer).run();
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
                setState(new ListToListAsyncMaterializer<E>(materialized));
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
        if (numElements == 0) {
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
        } else {
          safeConsume(consumer, true, LOGGER);
        }
      } else if (wrappedSize > 0) {
        safeConsume(consumer, false, LOGGER);
      } else {
        wrapped.materializeEmpty(new CancellableAsyncConsumer<Boolean>() {
          @Override
          public void cancellableAccept(final Boolean empty) throws Exception {
            if (!empty) {
              consumer.accept(false);
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
      return (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightSize() + wrapped.weightContains()
              + elementsMaterializer.weightContains());
    }

    @Override
    public int weightElement() {
      return (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightSize() + wrapped.weightContains()
              + elementsMaterializer.weightContains());
    }

    @Override
    public int weightElements() {
      return (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightElements() + elementsMaterializer.weightElements());
    }

    @Override
    public int weightEmpty() {
      return (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightEmpty() + elementsMaterializer.weightEmpty());
    }

    @Override
    public int weightHasElement() {
      return (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightSize() + Math.max(wrapped.weightHasElement(),
              elementsMaterializer.weightElement()));
    }

    @Override
    public int weightSize() {
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

    private @NotNull String getTaskID() {
      final String taskID = context.currentTaskID();
      return taskID != null ? taskID : "";
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

    private class MaterializingEachAsyncConsumer extends
        CancellableIndexedAsyncConsumer<E> implements Task {

      private final IndexedAsyncConsumer<E> consumer;

      private int elementsIndex;
      private int index;
      private boolean isWrapped = numElements != 0;
      private String taskID;
      private int wrappedIndex;

      private MaterializingEachAsyncConsumer(@NotNull final IndexedAsyncConsumer<E> consumer) {
        this.consumer = consumer;
      }

      @Override
      public void cancellableAccept(final int size, final int index, final E element)
          throws Exception {
        if (isWrapped) {
          wrappedSize = Math.max(wrappedSize, size);
          wrappedIndex = index + 1;
        } else {
          elementsSize = Math.max(elementsSize, size);
          elementsIndex = index + 1;
        }
        consumer.accept(safeSize(numElements, wrappedSize, elementsSize), this.index++, element);
        taskID = getTaskID();
        context.scheduleAfter(this);
      }

      @Override
      public void cancellableComplete(final int size) throws Exception {
        if (isWrapped) {
          wrappedSize = size;
          if (index == numElements) {
            index = size;
            taskID = getTaskID();
            context.scheduleAfter(this);
          } else {
            consumer.complete(safeSize(numElements, wrappedSize, elementsSize));
          }
        } else {
          elementsSize = size;
          if (wrappedSize == numElements) {
            consumer.complete(safeSize(numElements, wrappedSize, elementsSize));
          } else {
            isWrapped = true;
            taskID = getTaskID();
            context.scheduleAfter(this);
          }
        }
      }

      @Override
      public void error(@NotNull final Exception error) throws Exception {
        consumer.error(error);
      }

      @Override
      public void run() {
        if (isWrapped) {
          wrapped.materializeElement(wrappedIndex, this);
        } else {
          elementsMaterializer.materializeElement(elementsIndex, this);
        }
      }

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        return Math.max(wrapped.weightElement(), elementsMaterializer.weightElement());
      }
    }
  }
}
