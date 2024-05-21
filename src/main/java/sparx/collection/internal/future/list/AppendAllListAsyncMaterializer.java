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
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.collection.internal.future.AsyncConsumer;
import sparx.collection.internal.future.ContextAsyncConsumer;
import sparx.collection.internal.future.ContextIndexedAsyncConsumer;
import sparx.collection.internal.future.IndexedAsyncConsumer;
import sparx.concurrent.ExecutionContext;
import sparx.util.IndexOverflowException;
import sparx.util.Require;
import sparx.util.SizeOverflowException;
import sparx.util.function.BinaryFunction;

public class AppendAllListAsyncMaterializer<E> extends AbstractListAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      AppendAllListAsyncMaterializer.class.getName());

  private final Object stateLock = new Object();

  private volatile ListAsyncMaterializer<E> state;

  public AppendAllListAsyncMaterializer(@NotNull final ExecutionContext context,
      @NotNull final ListAsyncMaterializer<E> wrapped,
      @NotNull final ListAsyncMaterializer<E> elementsMaterializer,
      @NotNull final BinaryFunction<List<E>, List<E>, List<E>> appendFunction) {
    state = new ImmaterialState(Require.notNull(context, "context"),
        Require.notNull(wrapped, "wrapped"),
        Require.notNull(elementsMaterializer, "elementsMaterializer"),
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
  public boolean cancel(final boolean mayInterruptIfRunning) {
    synchronized (stateLock) {
      return state.cancel(mayInterruptIfRunning);
    }
  }

  @Override
  public boolean knownEmpty() {
    return false;
  }

  @Override
  public boolean isCancelled() {
    return state.isCancelled();
  }

  @Override
  public boolean isDone() {
    return state.isDone();
  }

  @Override
  public void materializeContains(final Object element,
      @NotNull final AsyncConsumer<Boolean> consumer) {
    state.materializeContains(element, consumer);
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
  public void materializeOrdered(@NotNull final IndexedAsyncConsumer<E> consumer) {
    state.materializeOrdered(consumer);
  }

  @Override
  public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
    state.materializeSize(consumer);
  }

  @Override
  public void materializeUnordered(@NotNull final IndexedAsyncConsumer<E> consumer) {
    state.materializeUnordered(consumer);
  }

  private class ImmaterialState extends AbstractListAsyncMaterializer<E> {

    private final BinaryFunction<List<E>, List<E>, List<E>> appendFunction;
    private final ExecutionContext context;
    private final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = new ArrayList<AsyncConsumer<List<E>>>(
        2);
    private final ListAsyncMaterializer<E> elementsMaterializer;
    private final ListAsyncMaterializer<E> wrapped;

    private List<E> elements;
    private int elementsSize = -1;
    private int wrappedSize = -1;

    public ImmaterialState(@NotNull final ExecutionContext context,
        @NotNull final ListAsyncMaterializer<E> wrapped,
        @NotNull final ListAsyncMaterializer<E> elementsMaterializer,
        @NotNull final BinaryFunction<List<E>, List<E>, List<E>> appendFunction) {
      this.context = context;
      this.wrapped = wrapped;
      this.elementsMaterializer = elementsMaterializer;
      this.appendFunction = appendFunction;
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
      if (wrapped.cancel(mayInterruptIfRunning) || elementsMaterializer.cancel(
          mayInterruptIfRunning)) {
        final int size = safeSize(wrappedSize, elementsSize);
        state = new ScheduledListAsyncMaterializer<E>(context,
            new CancelledListAsyncMaterializer<E>(size), size);
        return true;
      }
      return false;
    }

    @Override
    public boolean knownEmpty() {
      return wrapped.knownEmpty() && elementsMaterializer.knownEmpty();
    }

    @Override
    public boolean isCancelled() {
      return wrapped.isCancelled() || elementsMaterializer.isCancelled();
    }

    @Override
    public boolean isDone() {
      return isCancelled() || (wrapped.isDone() && elementsMaterializer.isDone());
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
            elementsMaterializer.materializeContains(element,
                new ContextAsyncConsumer<Boolean>(context, consumer, LOGGER));
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
        @NotNull final IndexedAsyncConsumer<E> consumer) {
      wrapped.materializeElement(index, new IndexedAsyncConsumer<E>() {
        @Override
        public void accept(final int size, final int index, final E param) throws Exception {
          final int knownSize = safeSize(wrappedSize = Math.max(wrappedSize, size), elementsSize);
          consumer.accept(knownSize, index, param);
        }

        @Override
        public void complete(final int size) {
          wrappedSize = size;
          final int originalIndex = index;
          elementsMaterializer.materializeElement(index - size,
              new ContextIndexedAsyncConsumer<E>(context, new IndexedAsyncConsumer<E>() {
                @Override
                public void accept(final int size, final int index, final E param)
                    throws Exception {
                  final int knownSize = safeSize(wrappedSize,
                      elementsSize = Math.max(elementsSize, size));
                  consumer.accept(knownSize, originalIndex, param);
                }

                @Override
                public void complete(final int size) throws Exception {
                  consumer.complete(
                      safeSize(wrappedSize, elementsSize = Math.max(elementsSize, size)));
                }

                @Override
                public void error(final int index, @NotNull final Exception error)
                    throws Exception {
                  consumer.error(originalIndex, error);
                }
              }, LOGGER));
        }

        @Override
        public void error(final int index, @NotNull final Exception error) throws Exception {
          consumer.error(index, error);
        }
      });
    }

    @Override
    public void materializeElements(@NotNull final AsyncConsumer<List<E>> consumer) {
      wrapped.materializeEmpty(new AsyncConsumer<Boolean>() {
        @Override
        public void accept(final Boolean param) throws Exception {
          if (elements != null) {
            consumer.accept(elements);
          } else {
            final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = ImmaterialState.this.elementsConsumers;
            elementsConsumers.add(consumer);
            if (elementsConsumers.size() == 1) {
              wrapped.materializeElements(new AsyncConsumer<List<E>>() {
                @Override
                public void accept(final List<E> param) {
                  wrappedSize = param.size();
                  final List<E> wrappedElements = param;
                  elementsMaterializer.materializeElements(
                      new ContextAsyncConsumer<List<E>>(context, new AsyncConsumer<List<E>>() {
                        @Override
                        public void accept(final List<E> param) throws Exception {
                          elementsSize = param.size();
                          elements = appendFunction.apply(wrappedElements, param);
                          setState(new ScheduledListAsyncMaterializer<E>(context,
                              new ListToListAsyncMaterializer<E>(elements), elements.size()));
                          consumeElements(elements);
                        }

                        @Override
                        public void error(@NotNull final Exception error) {
                          consumeError(error);
                        }
                      }, LOGGER));
                }

                @Override
                public void error(@NotNull final Exception error) {
                  consumeError(error);
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
    public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
      wrapped.materializeEmpty(new AsyncConsumer<Boolean>() {
        @Override
        public void accept(final Boolean empty) throws Exception {
          if (!empty) {
            consumer.accept(false);
          } else {
            elementsMaterializer.materializeEmpty(
                new ContextAsyncConsumer<Boolean>(context, consumer, LOGGER));
          }
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeOrdered(@NotNull final IndexedAsyncConsumer<E> consumer) {
      wrapped.materializeOrdered(new IndexedAsyncConsumer<E>() {
        @Override
        public void accept(final int size, final int index, final E param) throws Exception {
          final int knownSize = safeSize(wrappedSize = Math.max(wrappedSize, size), elementsSize);
          consumer.accept(knownSize, index, param);
        }

        @Override
        public void complete(final int size) {
          wrappedSize = size;
          elementsMaterializer.materializeOrdered(
              new ContextIndexedAsyncConsumer<E>(context, new IndexedAsyncConsumer<E>() {
                @Override
                public void accept(final int size, final int index, final E param)
                    throws Exception {
                  final int knownSize = safeSize(wrappedSize,
                      elementsSize = Math.max(elementsSize, size));
                  final int knownIndex = safeIndex(wrappedSize, index);
                  consumer.accept(knownSize, knownIndex, param);
                }

                @Override
                public void complete(final int size) throws Exception {
                  consumer.complete(
                      safeSize(wrappedSize, elementsSize = Math.max(elementsSize, size)));
                }

                @Override
                public void error(final int index, @NotNull Exception error) throws Exception {
                  consumer.error(safeIndex(wrappedSize, index), error);
                }
              }, LOGGER));
        }

        @Override
        public void error(final int index, @NotNull final Exception error) throws Exception {
          consumer.error(index, error);
        }
      });
    }

    @Override
    public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
      wrapped.materializeSize(new AsyncConsumer<Integer>() {
        @Override
        public void accept(final Integer size) {
          wrappedSize = size;
          elementsMaterializer.materializeSize(
              new ContextAsyncConsumer<Integer>(context, new AsyncConsumer<Integer>() {
                @Override
                public void accept(final Integer size) throws Exception {
                  consumer.accept(safeSize(wrappedSize, elementsSize = size));
                }

                @Override
                public void error(@NotNull final Exception error) throws Exception {
                  consumer.error(error);
                }
              }, LOGGER));
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeUnordered(@NotNull final IndexedAsyncConsumer<E> consumer) {
      wrapped.materializeUnordered(new IndexedAsyncConsumer<E>() {
        @Override
        public void accept(final int size, final int index, final E param) throws Exception {
          final int knownSize = safeSize(wrappedSize = Math.max(wrappedSize, size), elementsSize);
          consumer.accept(knownSize, index, param);
        }

        @Override
        public void complete(final int size) {
          if (size < 0) {
            wrapped.materializeSize(new AsyncConsumer<Integer>() {
              @Override
              public void accept(final Integer param) {
                complete(param);
              }

              @Override
              public void error(@NotNull final Exception error) throws Exception {
                consumer.error(-1, error);
              }
            });
          } else {
            wrappedSize = size;
            elementsMaterializer.materializeUnordered(
                new ContextIndexedAsyncConsumer<E>(context, new IndexedAsyncConsumer<E>() {
                  @Override
                  public void accept(final int size, final int index, final E param)
                      throws Exception {
                    final int knownSize = safeSize(wrappedSize,
                        elementsSize = Math.max(elementsSize, size));
                    final int knownIndex = safeIndex(wrappedSize, index);
                    consumer.accept(knownSize, knownIndex, param);
                  }

                  @Override
                  public void complete(final int size) throws Exception {
                    consumer.complete(
                        safeSize(wrappedSize, elementsSize = Math.max(elementsSize, size)));
                  }

                  @Override
                  public void error(final int index, @NotNull Exception error) throws Exception {
                    consumer.error(safeIndex(wrappedSize, index), error);
                  }
                }, LOGGER));
          }
        }

        @Override
        public void error(final int index, @NotNull final Exception error) throws Exception {
          consumer.error(index, error);
        }
      });
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

    private void setState(@NotNull final ListAsyncMaterializer<E> newState) {
      synchronized (stateLock) {
        state = newState;
      }
    }
  }
}
