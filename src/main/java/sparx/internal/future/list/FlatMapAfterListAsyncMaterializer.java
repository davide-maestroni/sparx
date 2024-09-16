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
import static sparx.internal.future.AsyncConsumers.safeConsumeError;

import java.util.ArrayList;
import java.util.Collections;
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
import sparx.internal.future.IndexedAsyncPredicate;
import sparx.util.IndexOverflowException;
import sparx.util.SizeOverflowException;
import sparx.util.annotation.NotNegative;
import sparx.util.function.Function;
import sparx.util.function.IndexedFunction;

public class FlatMapAfterListAsyncMaterializer<E> extends AbstractListAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      FlatMapAfterListAsyncMaterializer.class.getName());

  public FlatMapAfterListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      @NotNegative final int numElements,
      @NotNull final IndexedFunction<? super E, ? extends ListAsyncMaterializer<E>> mapper,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final Function<List<E>, List<E>> decorateFunction) {
    this(wrapped, numElements, mapper, new AtomicInteger(STATUS_RUNNING), context, cancelException,
        decorateFunction);
  }

  FlatMapAfterListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      @NotNegative final int numElements,
      @NotNull final IndexedFunction<? super E, ? extends ListAsyncMaterializer<E>> mapper,
      @NotNull final AtomicInteger status, @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final Function<List<E>, List<E>> decorateFunction) {
    super(context, status);
    setState(new ImmaterialState(wrapped, numElements, mapper, context, cancelException,
        decorateFunction));
  }

  @Override
  public int knownSize() {
    return -1;
  }

  private class ImmaterialState implements ListAsyncMaterializer<E> {

    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final Function<List<E>, List<E>> decorateFunction;
    private final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = new ArrayList<AsyncConsumer<List<E>>>(
        2);
    private final IndexedFunction<? super E, ? extends ListAsyncMaterializer<E>> mapper;
    private final int numElements;
    private final ListAsyncMaterializer<E> wrapped;

    private ListAsyncMaterializer<E> elementsMaterializer;
    private int elementsSize = -1;
    private int wrappedSize;

    public ImmaterialState(@NotNull final ListAsyncMaterializer<E> wrapped, final int numElements,
        @NotNull final IndexedFunction<? super E, ? extends ListAsyncMaterializer<E>> mapper,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Function<List<E>, List<E>> decorateFunction) {
      this.wrapped = wrapped;
      this.numElements = numElements;
      this.mapper = mapper;
      this.context = context;
      this.cancelException = cancelException;
      this.decorateFunction = decorateFunction;
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
      return -1;
    }

    @Override
    public void materializeCancel(@NotNull final CancellationException exception) {
      wrapped.materializeCancel(exception);
      final ListAsyncMaterializer<E> elementsMaterializer = this.elementsMaterializer;
      if (elementsMaterializer != null) {
        elementsMaterializer.materializeCancel(exception);
      }
      setCancelled(exception);
      consumeError(exception);
    }

    @Override
    public void materializeContains(final Object element,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      if (element == null) {
        new MaterializingContainsNullAsyncPredicate(consumer).run();
      } else {
        new MaterializingContainsElementAsyncPredicate(element, consumer).run();
      }
    }

    @Override
    public void materializeElement(final int index,
        @NotNull final IndexedAsyncConsumer<E> consumer) {
      if (index < 0) {
        safeConsumeError(consumer, new IndexOutOfBoundsException(Integer.toString(index)), LOGGER);
      } else if (index >= numElements) {
        if (wrappedSize >= 0) {
          if (numElements >= wrappedSize) {
            wrapped.materializeElement(index, new CancellableIndexedAsyncConsumer<E>() {
              @Override
              public void cancellableAccept(final int size, final int index, final E element)
                  throws Exception {
                consumer.accept(size, index, element);
              }

              @Override
              public void cancellableComplete(final int size) throws Exception {
                consumer.complete(size);
              }

              @Override
              public void error(@NotNull final Exception error) throws Exception {
                consumer.error(error);
              }
            });
          } else if (elementsSize >= 0) {
            final int originalIndex = index;
            if (index < SizeOverflowException.safeCast((long) numElements + elementsSize)) {
              elementsMaterializer.materializeElement(index - numElements,
                  new CancellableIndexedAsyncConsumer<E>() {
                    @Override
                    public void cancellableAccept(final int size, final int index, final E element)
                        throws Exception {
                      consumer.accept(safeSize(), originalIndex, element);
                    }

                    @Override
                    public void error(@NotNull final Exception error) throws Exception {
                      consumer.error(error);
                    }
                  });
            } else {
              wrapped.materializeElement(
                  IndexOverflowException.safeCast((long) index - elementsSize + 1),
                  new CancellableIndexedAsyncConsumer<E>() {
                    @Override
                    public void cancellableAccept(final int size, final int index, final E element)
                        throws Exception {
                      consumer.accept(safeSize(), originalIndex, element);
                    }

                    @Override
                    public void cancellableComplete(final int size) throws Exception {
                      consumer.complete(safeSize());
                    }

                    @Override
                    public void error(@NotNull final Exception error) throws Exception {
                      consumer.error(error);
                    }
                  });
            }
          } else {
            materialized(new AsyncConsumer<ListAsyncMaterializer<E>>() {
              @Override
              public void accept(final ListAsyncMaterializer<E> materializer) {
                final int originalIndex = index;
                materializer.materializeElement(index - numElements,
                    new CancellableIndexedAsyncConsumer<E>() {
                      @Override
                      public void cancellableAccept(final int size, final int index,
                          final E element) throws Exception {
                        elementsSize = Math.max(elementsSize, size);
                        consumer.accept(safeSize(), originalIndex, element);
                      }

                      @Override
                      public void cancellableComplete(final int size) {
                        elementsSize = size;
                        materializeElement(index, consumer);
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
        wrapped.materializeElement(index, new CancellableIndexedAsyncConsumer<E>() {
          @Override
          public void cancellableAccept(final int size, final int index, final E element)
              throws Exception {
            wrappedSize = Math.max(wrappedSize, size);
            consumer.accept(safeSize(), index, element);
          }

          @Override
          public void cancellableComplete(final int size) throws Exception {
            wrappedSize = size;
            consumer.complete(size);
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
          public void cancellableAccept(final List<E> wrappedElements) throws Exception {
            wrappedSize = wrappedElements.size();
            if (numElements >= wrappedSize) {
              final List<E> materialized = decorateFunction.apply(wrappedElements);
              setDone(new ListToListAsyncMaterializer<E>(materialized, context));
              consumeElements(materialized);
            } else {
              if (elementsMaterializer == null) {
                elementsMaterializer = mapper.apply(numElements, wrappedElements.get(numElements));
                elementsSize = elementsMaterializer.knownSize();
              }
              elementsMaterializer.materializeElements(new CancellableAsyncConsumer<List<E>>() {
                @Override
                public void cancellableAccept(final List<E> mappedElements) throws Exception {
                  elementsSize = mappedElements.size();
                  final ArrayList<E> elements = new ArrayList<E>(safeSize());
                  elements.addAll(wrappedElements.subList(0, Math.min(numElements, wrappedSize)));
                  elements.addAll(mappedElements);
                  elements.addAll(
                      wrappedElements.subList(Math.min(numElements + 1, wrappedSize), wrappedSize));
                  final List<E> materialized = decorateFunction.apply(elements);
                  setDone(new ListToListAsyncMaterializer<E>(materialized, context));
                  consumeElements(materialized);
                }

                @Override
                public void error(@NotNull final Exception error) {
                  setError(error);
                }
              });
            }
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
      } else if (numElements == 0 && wrappedSize == 1) {
        materialized(new AsyncConsumer<ListAsyncMaterializer<E>>() {
          @Override
          public void accept(final ListAsyncMaterializer<E> materializer) throws Exception {
            if (materializer == wrapped) {
              consumer.accept(false);
            } else {
              materializer.materializeEmpty(new CancellableAsyncConsumer<Boolean>() {
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
      } else if (wrappedSize > 0) {
        safeConsume(consumer, false, LOGGER);
      } else {
        wrapped.materializeSize(new CancellableAsyncConsumer<Integer>() {
          @Override
          public void cancellableAccept(final Integer size) {
            wrappedSize = size;
            materializeEmpty(consumer);
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
      } else if (index >= numElements) {
        if (wrappedSize >= 0) {
          if (numElements >= wrappedSize) {
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
          } else if (elementsSize >= 0) {
            if (index < SizeOverflowException.safeCast((long) numElements + elementsSize)) {
              elementsMaterializer.materializeHasElement(index - numElements,
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
            } else {
              wrapped.materializeHasElement(
                  IndexOverflowException.safeCast((long) index - elementsSize + 1),
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
            materialized(new AsyncConsumer<ListAsyncMaterializer<E>>() {
              @Override
              public void accept(final ListAsyncMaterializer<E> materializer) {
                materializer.materializeElement(index - numElements,
                    new CancellableIndexedAsyncConsumer<E>() {
                      @Override
                      public void cancellableAccept(final int size, final int index,
                          final E element) throws Exception {
                        elementsSize = Math.max(elementsSize, size);
                        consumer.accept(true);
                      }

                      @Override
                      public void cancellableComplete(final int size) {
                        elementsSize = size;
                        materializeHasElement(index, consumer);
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
    }

    @Override
    public void materializeNextWhile(final int index,
        @NotNull final IndexedAsyncPredicate<E> predicate) {
      new MaterializingNextAsyncPredicate(predicate, index).run();
    }

    @Override
    public void materializePrevWhile(final int index,
        @NotNull final IndexedAsyncPredicate<E> predicate) {
      new MaterializingPrevAsyncPredicate(predicate, index).run();
    }

    @Override
    public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
      if (wrappedSize >= 0) {
        if (elementsSize >= 0) {
          safeConsume(consumer, safeSize(), LOGGER);
        } else if (numElements >= wrappedSize) {
          safeConsume(consumer, wrappedSize, LOGGER);
        } else {
          materialized(new AsyncConsumer<ListAsyncMaterializer<E>>() {
            @Override
            public void accept(final ListAsyncMaterializer<E> materializer) {
              materializer.materializeSize(new CancellableAsyncConsumer<Integer>() {
                @Override
                public void accept(final Integer size) throws Exception {
                  elementsSize = size;
                  consumer.accept(safeSize());
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
        wrapped.materializeSize(new CancellableAsyncConsumer<Integer>() {
          @Override
          public void cancellableAccept(final Integer size) throws Exception {
            wrappedSize = size;
            if (numElements < wrappedSize) {
              materializeSize(consumer);
            } else {
              consumer.accept(size);
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
      return elementsMaterializer != null ? (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightNextWhile() + elementsMaterializer.weightContains())
          : wrapped.weightNextWhile();
    }

    @Override
    public int weightElement() {
      final ListAsyncMaterializer<E> wrapped = this.wrapped;
      final int weight = wrapped.weightElement();
      if (wrappedSize >= 0) {
        if (numElements >= wrappedSize) {
          return weight;
        } else if (elementsSize >= 0) {
          return Math.max(weight, elementsMaterializer.weightElement());
        }
        return Math.max(weight, weightElements());
      }
      return Math.max(weight, wrapped.weightSize());
    }

    @Override
    public int weightElements() {
      return elementsConsumers.isEmpty() ? elementsMaterializer != null ? (int) Math.min(
          Integer.MAX_VALUE,
          (long) wrapped.weightElements() + elementsMaterializer.weightElements())
          : wrapped.weightElements() : 1;
    }

    @Override
    public int weightEmpty() {
      if (wrappedSize == 0) {
        return 1;
      } else if (numElements == 0 && wrappedSize == 1) {
        final ListAsyncMaterializer<E> wrapped = this.wrapped;
        return (int) Math.min(Integer.MAX_VALUE,
            (long) wrapped.weightElement() + wrapped.weightEmpty());
      } else if (wrappedSize > 0) {
        return 1;
      }
      final ListAsyncMaterializer<E> wrapped = this.wrapped;
      return (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightSize() + wrapped.weightElement());
    }

    @Override
    public int weightHasElement() {
      final ListAsyncMaterializer<E> wrapped = this.wrapped;
      final int weight = wrapped.weightHasElement();
      if (wrappedSize >= 0) {
        if (numElements >= wrappedSize) {
          return weight;
        } else if (elementsSize >= 0) {
          return Math.max(weight, elementsMaterializer.weightHasElement());
        }
        return Math.max(weight, wrapped.weightElement());
      }
      return Math.max(weight, wrapped.weightSize());
    }

    @Override
    public int weightNextWhile() {
      final ListAsyncMaterializer<E> elementsMaterializer = this.elementsMaterializer;
      return elementsMaterializer != null ? (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightNextWhile() + elementsMaterializer.weightNextWhile())
          : wrapped.weightNextWhile();
    }

    @Override
    public int weightPrevWhile() {
      final ListAsyncMaterializer<E> elementsMaterializer = this.elementsMaterializer;
      if (elementsMaterializer != null) {
        if (elementsSize >= 0) {
          return (int) Math.min(Integer.MAX_VALUE,
              (long) wrapped.weightPrevWhile() + elementsMaterializer.weightPrevWhile());
        } else {
          return (int) Math.min(Integer.MAX_VALUE,
              (long) wrapped.weightPrevWhile() + elementsMaterializer.weightSize()
                  + elementsMaterializer.weightPrevWhile());
        }
      }
      return wrapped.weightPrevWhile();
    }

    @Override
    public int weightSize() {
      if (wrappedSize >= 0) {
        return elementsSize < 0 && numElements < wrappedSize ? wrapped.weightElement() : 1;
      }
      final ListAsyncMaterializer<E> wrapped = this.wrapped;
      return (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightSize() + wrapped.weightElement());
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

    private void materialized(@NotNull final AsyncConsumer<ListAsyncMaterializer<E>> consumer) {
      if (elementsMaterializer != null) {
        safeConsume(consumer, elementsMaterializer, LOGGER);
      } else {
        wrapped.materializeElement(numElements, new CancellableIndexedAsyncConsumer<E>() {
          @Override
          public void cancellableAccept(final int size, final int index, final E element)
              throws Exception {
            if (elementsMaterializer == null) {
              elementsMaterializer = mapper.apply(index, element);
              elementsSize = elementsMaterializer.knownSize();
              consumer.accept(elementsMaterializer);
            } else {
              consumer.accept(elementsMaterializer);
            }
          }

          @Override
          public void cancellableComplete(final int size) throws Exception {
            setState(new WrappingState(wrapped, cancelException));
            consumer.accept(new EmptyListAsyncMaterializer<E>(
                decorateFunction.apply(Collections.<E>emptyList())));
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
    }

    private int safeSize() {
      final int wrappedSize = this.wrappedSize;
      final long elementsSize = this.elementsSize;
      if (wrappedSize >= 0 && elementsSize >= 0) {
        if (numElements < wrappedSize) {
          return SizeOverflowException.safeCast(wrappedSize + elementsSize - 1);
        }
        return wrappedSize;
      }
      return -1;
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

    private class MaterializingContainsElementAsyncPredicate extends
        CancellableIndexedAsyncPredicate<E> implements Task {

      private final AsyncConsumer<Boolean> consumer;
      private final Object element;

      private int index;
      private String taskID;

      private MaterializingContainsElementAsyncPredicate(@NotNull final Object element,
          @NotNull final AsyncConsumer<Boolean> consumer) {
        this.element = element;
        this.consumer = consumer;
      }

      @Override
      public void cancellableComplete(final int size) throws Exception {
        consumer.accept(false);
      }

      @Override
      public boolean cancellableTest(final int size, final int index, final E element)
          throws Exception {
        if (index == numElements) {
          if (elementsMaterializer == null) {
            try {
              elementsMaterializer = mapper.apply(index, element);
              elementsSize = elementsMaterializer.knownSize();
            } catch (final Exception e) {
              setError(e);
              throw e;
            }
          }
          this.index = index + 1;
          elementsMaterializer.materializeContains(this.element,
              new CancellableAsyncConsumer<Boolean>() {
                @Override
                public void cancellableAccept(final Boolean contains) throws Exception {
                  if (contains) {
                    consumer.accept(true);
                  } else {
                    schedule();
                  }
                }

                @Override
                public void error(@NotNull final Exception error) throws Exception {
                  consumer.error(error);
                }
              });
          return false;
        } else if (this.element.equals(element)) {
          consumer.accept(true);
          return false;
        }
        return true;
      }

      @Override
      public void error(@NotNull final Exception error) throws Exception {
        consumer.error(error);
      }

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        return wrapped.weightNextWhile();
      }

      @Override
      protected void runWithContext() {
        wrapped.materializeNextWhile(index, this);
      }

      private void schedule() {
        taskID = getTaskID();
        context.scheduleAfter(this);
      }
    }

    private class MaterializingContainsNullAsyncPredicate extends
        CancellableIndexedAsyncPredicate<E> implements Task {

      private final AsyncConsumer<Boolean> consumer;

      private int index;
      private String taskID;

      private MaterializingContainsNullAsyncPredicate(
          @NotNull final AsyncConsumer<Boolean> consumer) {
        this.consumer = consumer;
      }

      @Override
      public void cancellableComplete(final int size) throws Exception {
        consumer.accept(false);
      }

      @Override
      public boolean cancellableTest(final int size, final int index, final E element)
          throws Exception {
        if (index == numElements) {
          if (elementsMaterializer == null) {
            try {
              elementsMaterializer = mapper.apply(index, element);
              elementsSize = elementsMaterializer.knownSize();
            } catch (final Exception e) {
              setError(e);
              throw e;
            }
          }
          this.index = index + 1;
          elementsMaterializer.materializeContains(null, new CancellableAsyncConsumer<Boolean>() {
            @Override
            public void cancellableAccept(final Boolean contains) throws Exception {
              if (contains) {
                consumer.accept(true);
              } else {
                schedule();
              }
            }

            @Override
            public void error(@NotNull final Exception error) throws Exception {
              consumer.error(error);
            }
          });
          return false;
        } else if (element == null) {
          consumer.accept(true);
          return false;
        }
        return true;
      }

      @Override
      public void error(@NotNull final Exception error) throws Exception {
        consumer.error(error);
      }

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        return wrapped.weightNextWhile();
      }

      @Override
      protected void runWithContext() {
        wrapped.materializeNextWhile(index, this);
      }

      private void schedule() {
        taskID = getTaskID();
        context.scheduleAfter(this);
      }
    }

    private class MaterializingNextAsyncPredicate extends
        CancellableIndexedAsyncPredicate<E> implements Task {

      private final IndexedAsyncPredicate<E> predicate;

      private int index;
      private int wrappedIndex;
      private boolean isWrapped;
      private String taskID;

      private MaterializingNextAsyncPredicate(@NotNull final IndexedAsyncPredicate<E> predicate,
          final int index) {
        this.predicate = predicate;
        this.index = wrappedIndex = index;
        isWrapped = index <= numElements;
      }

      @Override
      public void cancellableComplete(final int size) throws Exception {
        if (isWrapped) {
          wrappedSize = size;
          predicate.complete(safeSize());
        } else {
          elementsSize = size;
          isWrapped = true;
          wrappedIndex = IndexOverflowException.safeCast((long) index - size + 1);
          schedule();
        }
      }

      @Override
      public boolean cancellableTest(final int size, final int index, final E element)
          throws Exception {
        if (isWrapped) {
          wrappedSize = Math.max(wrappedSize, size);
          if (index == numElements) {
            if (elementsMaterializer == null) {
              elementsMaterializer = mapper.apply(index, element);
              elementsSize = elementsMaterializer.knownSize();
            }
            isWrapped = false;
            elementsMaterializer.materializeNextWhile(0, this);
            return false;
          }
        } else {
          elementsSize = Math.max(elementsSize, size);
        }
        return predicate.test(safeSize(), this.index++, element);
      }

      @Override
      public void error(@NotNull final Exception error) {
        setError(error);
      }

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        return wrapped.weightNextWhile();
      }

      @Override
      protected void runWithContext() {
        if (isWrapped) {
          wrapped.materializeNextWhile(wrappedIndex, this);
        } else {
          materialized(new AsyncConsumer<ListAsyncMaterializer<E>>() {
            @Override
            public void accept(final ListAsyncMaterializer<E> elementsMaterializer) {
              isWrapped = false;
              elementsMaterializer.materializeNextWhile(index - numElements,
                  MaterializingNextAsyncPredicate.this);
            }

            @Override
            public void error(@NotNull final Exception error) throws Exception {
              predicate.error(error);
            }
          });
        }
      }

      private void schedule() {
        taskID = getTaskID();
        context.scheduleAfter(this);
      }
    }

    private class MaterializingPrevAsyncPredicate extends
        CancellableIndexedAsyncPredicate<E> implements Task {

      private final IndexedAsyncPredicate<E> predicate;

      private int index;
      private int wrappedIndex;
      private boolean isWrapped;
      private String taskID;

      private MaterializingPrevAsyncPredicate(@NotNull final IndexedAsyncPredicate<E> predicate,
          final int index) {
        this.predicate = predicate;
        this.index = wrappedIndex = index;
        isWrapped = index <= numElements;
      }

      @Override
      public void cancellableComplete(final int size) throws Exception {
        if (isWrapped) {
          wrappedSize = Math.max(wrappedSize, size);
          predicate.complete(safeSize());
        } else {
          elementsSize = Math.max(elementsSize, size);
          if (index < 0) {
            predicate.complete(safeSize());
          } else {
            isWrapped = true;
            wrappedIndex = index;
            schedule();
          }
        }
      }

      @Override
      public boolean cancellableTest(final int size, final int index, final E element)
          throws Exception {
        if (isWrapped) {
          wrappedSize = Math.max(wrappedSize, size);
          if (index == numElements) {
            if (elementsMaterializer == null) {
              elementsMaterializer = mapper.apply(index, element);
              elementsSize = elementsMaterializer.knownSize();
            }
            if (elementsSize >= 0) {
              isWrapped = false;
              elementsMaterializer.materializePrevWhile(
                  Math.min(elementsSize - 1, this.index - numElements), this);
            } else {
              elementsMaterializer.materializeSize(new CancellableAsyncConsumer<Integer>() {
                @Override
                public void cancellableAccept(final Integer size) {
                  elementsSize = size;
                  isWrapped = false;
                  elementsMaterializer.materializePrevWhile(size - 1,
                      MaterializingPrevAsyncPredicate.this);
                }

                @Override
                public void error(@NotNull final Exception error) throws Exception {
                  predicate.error(error);
                }
              });
            }
            return false;
          }
          if (index > numElements && elementsSize >= 0) {
            this.index = IndexOverflowException.safeCast((long) index + elementsSize - 1);
          }
        } else {
          elementsSize = Math.max(elementsSize, size);
        }
        return predicate.test(safeSize(), this.index--, element);
      }

      @Override
      public void error(@NotNull final Exception error) {
        setError(error);
      }

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        return wrapped.weightNextWhile();
      }

      @Override
      protected void runWithContext() {
        if (isWrapped) {
          wrapped.materializePrevWhile(wrappedIndex, this);
        } else {
          materialized(new AsyncConsumer<ListAsyncMaterializer<E>>() {
            @Override
            public void accept(final ListAsyncMaterializer<E> elementsMaterializer) {
              if (elementsSize >= 0) {
                if (index < SizeOverflowException.safeCast((long) numElements + elementsSize)) {
                  isWrapped = false;
                  elementsMaterializer.materializePrevWhile(index - numElements,
                      MaterializingPrevAsyncPredicate.this);
                } else {
                  isWrapped = true;
                  wrapped.materializePrevWhile(
                      (int) Math.min(Integer.MAX_VALUE, (long) index - elementsSize + 1),
                      MaterializingPrevAsyncPredicate.this);
                }
              } else {
                elementsMaterializer.materializeSize(new CancellableAsyncConsumer<Integer>() {
                  @Override
                  public void cancellableAccept(final Integer size) {
                    elementsSize = size;
                    schedule();
                  }

                  @Override
                  public void error(@NotNull final Exception error) throws Exception {
                    predicate.error(error);
                  }
                });
              }
            }

            @Override
            public void error(@NotNull final Exception error) throws Exception {
              predicate.error(error);
            }
          });
        }
      }

      private void schedule() {
        taskID = getTaskID();
        context.scheduleAfter(this);
      }
    }
  }
}
