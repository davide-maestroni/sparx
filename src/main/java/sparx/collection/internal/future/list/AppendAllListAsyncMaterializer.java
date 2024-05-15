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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.collection.internal.future.AsyncConsumer;
import sparx.collection.internal.future.IndexedAsyncConsumer;
import sparx.util.IndexOverflowException;
import sparx.util.Require;
import sparx.util.SizeOverflowException;

public class AppendAllListAsyncMaterializer<E> implements ListAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      AppendAllListAsyncMaterializer.class.getName());

  private final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = new ArrayList<AsyncConsumer<List<E>>>(
      2);
  private final ListAsyncMaterializer<E> elementsMaterializer;
  private final AtomicBoolean isMaterialized = new AtomicBoolean(false);
  private final ListAsyncMaterializer<E> wrapped;

  private ListAsyncMaterializer<E> state;

  public AppendAllListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      @NotNull final ListAsyncMaterializer<E> elementsMaterializer) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
    this.elementsMaterializer = Require.notNull(elementsMaterializer, "elementsMaterializer");
  }

  @Override
  public boolean cancel(final boolean mayInterruptIfRunning) {
    return wrapped.cancel(mayInterruptIfRunning);
  }

  @Override
  public int knownSize() {
    final int wrappedSize = wrapped.knownSize();
    if (wrappedSize >= 0) {
      return SizeOverflowException.safeCast((long) wrappedSize + 1);
    }
    return wrappedSize;
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
          consumer.accept(state = elementsMaterializer);
        } else {
          consumer.accept(state = new AppendState());
        }
      }

      @Override
      public void error(@NotNull final Exception error) {
        isMaterialized.set(true);
        consumer.accept(state = new FailedListAsyncMaterializer<E>(knownSize(), 0, error));
      }
    });
  }

  private interface StateConsumer<E> {

    void accept(@NotNull ListAsyncMaterializer<E> state);
  }

  private class AppendState implements ListAsyncMaterializer<E> {

    private final ElementsCache<E> elementsCache = new ElementsCache<E>(knownSize());

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
      return false;
    }

    @Override
    public int knownSize() {
      return safeSize(wrapped.knownSize(), elementsMaterializer.knownSize());
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
    public void materializeElement(final int index,
        @NotNull final IndexedAsyncConsumer<E> consumer) {
      final ElementsCache<E> elementsCache = this.elementsCache;
      if (elementsCache.hasElement(index)) {
        try {
          consumer.accept(elementsCache.getSize(), index, elementsCache.getElement(index));
        } catch (final Exception e) {
          LOGGER.log(Level.SEVERE, "Ignored exception", e);
        }
      } else {
        wrapped.materializeElement(index, new IndexedAsyncConsumer<E>() {
          @Override
          public void accept(final int size, final int index, final E param) throws Exception {
            final int materialSize = safeSize(size, elementsMaterializer.knownSize());
            if (elementsCache.add(materialSize, index, param) != null) {
              isMaterialized.set(true);
            }
            consumer.accept(materialSize, index, param);
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
                  consumer.error(index, error);
                }
              });
            } else {
              final int originalSize = size;
              final int originalIndex = index;
              elementsMaterializer.materializeElement(index - size, new IndexedAsyncConsumer<E>() {
                @Override
                public void accept(final int size, final int index, final E param)
                    throws Exception {
                  final int materialSize = safeSize(originalSize, size);
                  if (elementsCache.add(materialSize, originalIndex, param) != null) {
                    isMaterialized.set(true);
                  }
                  consumer.accept(materialSize, originalIndex, param);
                }

                @Override
                public void complete(final int size) throws Exception {
                  consumer.complete(safeSize(originalSize, size));
                }

                @Override
                public void error(final int index, @NotNull final Exception error)
                    throws Exception {
                  consumer.error(originalIndex, error);
                }
              });
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
      if (isMaterialized.get()) {
        try {
          consumer.accept(elementsCache.get(0));
        } catch (final Exception e) {
          LOGGER.log(Level.SEVERE, "Ignored exception", e);
        }
      } else {
        final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = AppendAllListAsyncMaterializer.this.elementsConsumers;
        elementsConsumers.add(consumer);
        if (elementsConsumers.size() == 1) {
          wrapped.materializeOrdered(new IndexedAsyncConsumer<E>() {
            @Override
            public void accept(final int size, final int index, final E param) {
              elementsCache.add(safeSize(size, elementsMaterializer.knownSize()), index, param);
            }

            @Override
            public void complete(final int size) {
              final int originalSize = size;
              elementsMaterializer.materializeOrdered(new IndexedAsyncConsumer<E>() {
                @Override
                public void accept(final int size, final int index, final E param) {
                  elementsCache.add(safeSize(originalSize, size), safeIndex(originalSize, index),
                      param);
                }

                @Override
                public void complete(final int size) {
                  isMaterialized.set(true);
                  consumeElements(elementsCache.get(safeSize(originalSize, size)));
                }

                @Override
                public void error(final int index, @NotNull final Exception error) {
                  consumeError(error);
                }
              });
            }

            @Override
            public void error(final int index, @NotNull final Exception error) {
              consumeError(error);
            }
          });
        }
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
            elementsMaterializer.materializeEmpty(new AsyncConsumer<Boolean>() {
              @Override
              public void accept(final Boolean param) throws Exception {
                consumer.accept(param);
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
    public void materializeOrdered(@NotNull final IndexedAsyncConsumer<E> consumer) {
      wrapped.materializeOrdered(new IndexedAsyncConsumer<E>() {
        @Override
        public void accept(final int size, final int index, final E param) throws Exception {
          final int materialSize = safeSize(size, elementsMaterializer.knownSize());
          if (elementsCache.add(materialSize, index, param) != null) {
            isMaterialized.set(true);
          }
          consumer.accept(materialSize, index, param);
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
            final int originalSize = size;
            elementsMaterializer.materializeOrdered(new IndexedAsyncConsumer<E>() {
              @Override
              public void accept(final int size, final int index, final E param) throws Exception {
                final int materialSize = safeSize(originalSize, size);
                final int materialIndex = safeIndex(originalSize, index);
                if (elementsCache.add(materialSize, materialIndex, param) != null) {
                  isMaterialized.set(true);
                }
                consumer.accept(materialSize, materialIndex, param);
              }

              @Override
              public void complete(final int size) throws Exception {
                consumer.complete(safeSize(originalSize, size));
              }

              @Override
              public void error(final int index, @NotNull Exception error) throws Exception {
                consumer.error(safeIndex(originalSize, index), error);
              }
            });
          }
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
          elementsMaterializer.materializeSize(new AsyncConsumer<Integer>() {
            @Override
            public void accept(final Integer param) throws Exception {
              consumer.accept(safeSize(size, param));
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

    @Override
    public void materializeUnordered(@NotNull final IndexedAsyncConsumer<E> consumer) {
      wrapped.materializeUnordered(new IndexedAsyncConsumer<E>() {
        @Override
        public void accept(final int size, final int index, final E param) throws Exception {
          final int materialSize = safeSize(size, elementsMaterializer.knownSize());
          if (elementsCache.add(materialSize, index, param) != null) {
            isMaterialized.set(true);
          }
          consumer.accept(materialSize, index, param);
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
            final int originalSize = size;
            elementsMaterializer.materializeUnordered(new IndexedAsyncConsumer<E>() {
              @Override
              public void accept(final int size, final int index, final E param) throws Exception {
                final int materialSize = safeSize(originalSize, size);
                final int materialIndex = safeIndex(originalSize, index);
                if (elementsCache.add(materialSize, materialIndex, param) != null) {
                  isMaterialized.set(true);
                }
                consumer.accept(materialSize, materialIndex, param);
              }

              @Override
              public void complete(final int size) throws Exception {
                consumer.complete(safeSize(originalSize, size));
              }

              @Override
              public void error(final int index, @NotNull Exception error) throws Exception {
                consumer.error(safeIndex(originalSize, index), error);
              }
            });
          }
        }

        @Override
        public void error(final int index, @NotNull final Exception error) throws Exception {
          consumer.error(index, error);
        }
      });
    }

    private void consumeElements(@NotNull final List<E> elements) {
      final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = AppendAllListAsyncMaterializer.this.elementsConsumers;
      for (final AsyncConsumer<List<E>> elementsConsumer : elementsConsumers) {
        try {
          elementsConsumer.accept(elements);
        } catch (final Exception e) {
          LOGGER.log(Level.SEVERE, "Ignored exception", e);
        }
      }
      elementsConsumers.clear();
    }

    private void consumeError(@NotNull final Exception error) {
      final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = AppendAllListAsyncMaterializer.this.elementsConsumers;
      for (final AsyncConsumer<List<E>> elementsConsumer : elementsConsumers) {
        try {
          elementsConsumer.error(error);
        } catch (final Exception e) {
          LOGGER.log(Level.SEVERE, "Ignored exception", e);
        }
      }
      elementsConsumers.clear();
    }

    private int safeIndex(final int wrappedSize, final int elementsSize) {
      if (wrappedSize >= 0 && elementsSize > 0) {
        return SizeOverflowException.safeCast((long) wrappedSize + elementsSize);
      }
      return -1;
    }

    private int safeSize(final int wrappedSize, final int elementsIndex) {
      if (wrappedSize >= 0) {
        return IndexOverflowException.safeCast((long) wrappedSize + elementsIndex);
      }
      return -1;
    }
  }
}
