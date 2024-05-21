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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.collection.internal.future.AsyncConsumer;
import sparx.collection.internal.future.IndexedAsyncConsumer;
import sparx.util.Require;

public class DiffListAsyncMaterializer<E> implements ListAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(DiffListAsyncMaterializer.class.getName());

  private final HashMap<Integer, ArrayList<IndexedAsyncConsumer<E>>> elementsConsumers = new HashMap<Integer, ArrayList<IndexedAsyncConsumer<E>>>(
      2);
  private final ListAsyncMaterializer<?> elementsMaterializer;
  private final AtomicBoolean isMaterialized = new AtomicBoolean(false);
  private final ListAsyncMaterializer<E> wrapped;

  private ListAsyncMaterializer<E> state;

  public DiffListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      @NotNull final ListAsyncMaterializer<?> elementsMaterializer) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
    this.elementsMaterializer = Require.notNull(elementsMaterializer, "elementsMaterializer");
  }

  @Override
  public boolean cancel(final boolean mayInterruptIfRunning) {
    return wrapped.cancel(mayInterruptIfRunning);
  }

  @Override
  public boolean knownEmpty() {
    return false;
  }

  @Override
  public boolean isCancelled() {
    return wrapped.isCancelled();
  }

  @Override
  public boolean isDone() {
    return wrapped.isCancelled() || isMaterialized.get();
  }

  @Override
  public void materializeContains(final Object element,
      @NotNull final AsyncConsumer<Boolean> consumer) {
    materialized(new StateConsumer<E>() {
      @Override
      public void accept(@NotNull final ListAsyncMaterializer<E> state) {
        state.materializeContains(element, consumer);
      }
    });
  }

  @Override
  public void materializeElement(final int index, @NotNull final IndexedAsyncConsumer<E> consumer) {
    materialized(new StateConsumer<E>() {
      @Override
      public void accept(@NotNull final ListAsyncMaterializer<E> state) {
        state.materializeElement(index, consumer);
      }
    });
  }

  @Override
  public void materializeElements(@NotNull final AsyncConsumer<List<E>> consumer) {
    materialized(new StateConsumer<E>() {
      @Override
      public void accept(@NotNull final ListAsyncMaterializer<E> state) {
        state.materializeElements(consumer);
      }
    });
  }

  @Override
  public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
    materialized(new StateConsumer<E>() {
      @Override
      public void accept(@NotNull final ListAsyncMaterializer<E> state) {
        state.materializeEmpty(consumer);
      }
    });
  }

  @Override
  public void materializeOrdered(@NotNull final IndexedAsyncConsumer<E> consumer) {
    materialized(new StateConsumer<E>() {
      @Override
      public void accept(@NotNull final ListAsyncMaterializer<E> state) {
        state.materializeOrdered(consumer);
      }
    });
  }

  @Override
  public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
    materialized(new StateConsumer<E>() {
      @Override
      public void accept(@NotNull final ListAsyncMaterializer<E> state) {
        state.materializeSize(consumer);
      }
    });
  }

  @Override
  public void materializeUnordered(@NotNull final IndexedAsyncConsumer<E> consumer) {
    materialized(new StateConsumer<E>() {
      @Override
      public void accept(@NotNull final ListAsyncMaterializer<E> state) {
        state.materializeUnordered(consumer);
      }
    });
  }

  private void materialized(@NotNull final StateConsumer<E> consumer) {
    final ListAsyncMaterializer<E> wrapped = this.wrapped;
    wrapped.materializeEmpty(new AsyncConsumer<Boolean>() {
      @Override
      public void accept(final Boolean empty) {
        if (state != null) {
          consumer.accept(state);
        } else if (empty) {
          isMaterialized.set(true);
          consumer.accept(state = wrapped);
        } else {
          consumer.accept(state = new ImmaterialState());
        }
      }

      @Override
      public void error(@NotNull final Exception error) {
        isMaterialized.set(true);
        consumer.accept(state = new FailedListAsyncMaterializer<E>(-1, -1, error));
      }
    });
  }

  private interface StateConsumer<E> {

    void accept(@NotNull ListAsyncMaterializer<E> state);
  }

  private class ImmaterialState extends AbstractListAsyncMaterializer<E> {

    private final ElementsCache<E> elementsCache = new ElementsCache<E>(-1);

    private int nextIndex;

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
      return false;
    }

    @Override
    public boolean knownEmpty() {
      return false;
    }

    @Override
    public boolean isCancelled() {
      return wrapped.isCancelled() || elementsMaterializer.isCancelled();
    }

    @Override
    public boolean isDone() {
      return isCancelled() || isMaterialized.get();
    }

    @Override
    public void materializeContains(final Object element,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      elementsMaterializer.materializeContains(element, new AsyncConsumer<Boolean>() {
        @Override
        public void accept(final Boolean contains) throws Exception {
          if (contains) {
            consumer.accept(false);
          } else {
            elementsMaterializer.materializeContains(element, new AsyncConsumer<Boolean>() {
              @Override
              public void accept(final Boolean contains) throws Exception {
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
        public void error(@NotNull Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeElement(final int index,
        @NotNull final IndexedAsyncConsumer<E> consumer) {
      final ElementsCache<E> elementsCache = this.elementsCache;
      if (elementsCache.hasElement(index)) {
        safeConsume(consumer, elementsCache.knownSize(), index, elementsCache.getElement(index),
            LOGGER);
      } else {
        final int originalIndex = index;
        materializeUntil(index, new IndexedAsyncConsumer<E>() {
          @Override
          public void accept(final int size, final int index, final E param) throws Exception {
            if (originalIndex == index) {
              consumer.accept(size, index, param);
            }
          }

          @Override
          public void complete(final int size) throws Exception {
            if (originalIndex >= size) {
              consumer.complete(size);
            }
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
      materializeUntil(Integer.MAX_VALUE, new IndexedAsyncConsumer<E>() {
        @Override
        public void accept(final int size, final int index, final E param) {
        }

        @Override
        public void complete(final int size) throws Exception {
          consumer.accept(elementsCache.getElements());
        }

        @Override
        public void error(final int index, @NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
      materializeUntil(0, new IndexedAsyncConsumer<E>() {
        @Override
        public void accept(final int size, final int index, final E param) {
        }

        @Override
        public void complete(final int size) throws Exception {
          consumer.accept(size == 0);
        }

        @Override
        public void error(final int index, @NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeOrdered(@NotNull final IndexedAsyncConsumer<E> consumer) {
      materializeUntil(Integer.MAX_VALUE, consumer);
    }

    @Override
    public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
      materializeUntil(Integer.MAX_VALUE, new IndexedAsyncConsumer<E>() {
        @Override
        public void accept(final int size, final int index, final E param) {
        }

        @Override
        public void complete(final int size) throws Exception {
          consumer.accept(size);
        }

        @Override
        public void error(final int index, @NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeUnordered(@NotNull final IndexedAsyncConsumer<E> consumer) {
      materializeUntil(Integer.MAX_VALUE, consumer);
    }

    private void consumeComplete(final int size) {
      final HashMap<Integer, ArrayList<IndexedAsyncConsumer<E>>> elementsConsumers = DiffListAsyncMaterializer.this.elementsConsumers;
      for (final ArrayList<IndexedAsyncConsumer<E>> consumers : elementsConsumers.values()) {
        for (final IndexedAsyncConsumer<E> consumer : consumers) {
          safeConsumeComplete(consumer, size, LOGGER);
        }
      }
      elementsConsumers.clear();
    }

    private void consumeElement(final int index, final E element) {
      final HashMap<Integer, ArrayList<IndexedAsyncConsumer<E>>> elementsConsumers = DiffListAsyncMaterializer.this.elementsConsumers;
      final Iterator<Entry<Integer, ArrayList<IndexedAsyncConsumer<E>>>> entries = elementsConsumers.entrySet()
          .iterator();
      while (entries.hasNext()) {
        final Entry<Integer, ArrayList<IndexedAsyncConsumer<E>>> entry = entries.next();
        final int key = entry.getKey();
        if (key < index) {
          final Iterator<IndexedAsyncConsumer<E>> consumers = entry.getValue().iterator();
          while (consumers.hasNext()) {
            if (!safeConsume(consumers.next(), -1, index, element, LOGGER)) {
              consumers.remove();
            }
          }
          if (entry.getValue().isEmpty()) {
            entries.remove();
          }
        } else if (key == index) {
          for (final IndexedAsyncConsumer<E> consumer : entry.getValue()) {
            if (safeConsume(consumer, -1, index, element, LOGGER)) {
              safeConsumeComplete(consumer, index + 1, LOGGER);
            }
          }
          entries.remove();
        }
      }
    }

    private void consumeError(final int index, @NotNull final Exception error) {
      final HashMap<Integer, ArrayList<IndexedAsyncConsumer<E>>> elementsConsumers = DiffListAsyncMaterializer.this.elementsConsumers;
      for (final ArrayList<IndexedAsyncConsumer<E>> consumers : elementsConsumers.values()) {
        for (final IndexedAsyncConsumer<E> consumer : consumers) {
          safeConsumeError(consumer, index, error, LOGGER);
        }
      }
      elementsConsumers.clear();
    }

    private void materializeUntil(final int index,
        @NotNull final IndexedAsyncConsumer<E> consumer) {
      final ElementsCache<E> elementsCache = this.elementsCache;
      if (elementsCache.hasElement(index)) {
        final int size = index + 1;
        for (int i = 0; i < size; ++i) {
          if (!safeConsume(consumer, -1, i, elementsCache.getElement(i), LOGGER)) {
            return;
          }
        }
        safeConsumeComplete(consumer, size, LOGGER);
      } else {
        final int size = elementsCache.numElements();
        for (int i = 0; i < size; ++i) {
          if (!safeConsume(consumer, -1, i, elementsCache.getElement(i), LOGGER)) {
            return;
          }
        }
        final HashMap<Integer, ArrayList<IndexedAsyncConsumer<E>>> elementsConsumers = DiffListAsyncMaterializer.this.elementsConsumers;
        final boolean needsRun = elementsConsumers.isEmpty();
        ArrayList<IndexedAsyncConsumer<E>> indexConsumers = elementsConsumers.get(index);
        if (indexConsumers == null) {
          elementsConsumers.put(index, indexConsumers = new ArrayList<IndexedAsyncConsumer<E>>(2));
        }
        indexConsumers.add(consumer);
        if (needsRun) {
          final int originalIndex = index;
          wrapped.materializeElement(nextIndex, new IndexedAsyncConsumer<E>() {
            @Override
            public void accept(final int size, final int index, final E element) {
              final IndexedAsyncConsumer<E> elementConsumer = this;
              elementsMaterializer.materializeContains(element, new AsyncConsumer<Boolean>() {
                @Override
                public void accept(final Boolean contains) {
                  materialized(new StateConsumer<E>() {
                    @Override
                    public void accept(@NotNull final ListAsyncMaterializer<E> ignored) {
                      ++nextIndex;
                      if (!contains) {
                        final int index = elementsCache.numElements();
                        elementsCache.addElement(-1, index, element);
                        consumeElement(index, element);
                      }
                      if (nextIndex < originalIndex) {
                        wrapped.materializeElement(nextIndex, elementConsumer);
                      } else {
                        consumeComplete(nextIndex);
                      }
                    }
                  });
                }

                @Override
                public void error(@NotNull final Exception error) {
                  materialized(new StateConsumer<E>() {
                    @Override
                    public void accept(@NotNull final ListAsyncMaterializer<E> ignored) {
                      isMaterialized.set(true);
                      final int index = elementsCache.numElements() - 1;
                      state = new FailedListAsyncMaterializer<E>(-1, index, error);
                      consumeError(index, error);
                    }
                  });
                }
              });
            }

            @Override
            public void complete(final int size) {
              isMaterialized.set(true);
              final ElementsCache<E> elementsCache = ImmaterialState.this.elementsCache;
              state = new ListToListAsyncMaterializer<E>(elementsCache.getElements());
              consumeComplete(elementsCache.numElements());
            }

            @Override
            public void error(final int ignored, @NotNull final Exception error) {
              final int index = elementsCache.numElements() - 1;
              state = new FailedListAsyncMaterializer<E>(-1, index, error);
              consumeError(index, error);
            }
          });
        }
      }
    }
  }
}
