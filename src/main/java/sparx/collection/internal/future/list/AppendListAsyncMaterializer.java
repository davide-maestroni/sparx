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
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.collection.internal.future.AsyncConsumer;
import sparx.collection.internal.future.IndexedAsyncConsumer;
import sparx.util.Require;
import sparx.util.SizeOverflowException;

public class AppendListAsyncMaterializer<E> implements ListAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      AppendListAsyncMaterializer.class.getName());

  private final E element;
  private final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = new ArrayList<AsyncConsumer<List<E>>>(
      2);
  private final AtomicBoolean isMaterialized = new AtomicBoolean(false);
  private final ListAsyncMaterializer<E> wrapped;

  private ListAsyncMaterializer<E> state;

  public AppendListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      final E element) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
    this.element = element;
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
    return isCancelled() || isMaterialized.get();
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
          consumer.accept(state = new ElementToListAsyncMaterializer<E>(element));
        } else {
          consumer.accept(state = new ImmaterialState());
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

  private class ImmaterialState extends AbstractListAsyncMaterializer<E> {

    private final ElementsCache<E> elementsCache = new ElementsCache<E>(knownSize());

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
      return false;
    }

    @Override
    public int knownSize() {
      return safeSize(wrapped.knownSize());
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
      final E appended = AppendListAsyncMaterializer.this.element;
      if (element == appended || (element != null && element.equals(appended))) {
        safeConsume(consumer, true, LOGGER);
      } else {
        wrapped.materializeContains(element, consumer);
      }
    }

    @Override
    public void materializeElement(final int index,
        @NotNull final IndexedAsyncConsumer<E> consumer) {
      final ElementsCache<E> elementsCache = this.elementsCache;
      if (elementsCache.hasElement(index)) {
        safeConsume(consumer, elementsCache.knownSize(), index, elementsCache.getElement(index),
            LOGGER);
      } else {
        wrapped.materializeElement(index, new IndexedAsyncConsumer<E>() {
          @Override
          public void accept(final int size, final int index, final E param) throws Exception {
            final int materialSize = safeSize(size);
            final List<E> elements = elementsCache.addElement(materialSize, index, param);
            if (elements != null) {
              isMaterialized.set(true);
              state = new ListToListAsyncMaterializer<E>(elements);
            }
            consumer.accept(materialSize, index, param);
          }

          @Override
          public void complete(final int size) throws Exception {
            if (size == index) {
              final int materialSize = safeSize(size);
              final List<E> elements = elementsCache.addElement(materialSize, size, element);
              if (elements != null) {
                isMaterialized.set(true);
                state = new ListToListAsyncMaterializer<E>(elements);
              }
              consumer.accept(materialSize, size, element);
            } else {
              consumer.complete(safeSize(size));
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
        safeConsume(consumer, elementsCache.getElements(), LOGGER);
      } else {
        final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = AppendListAsyncMaterializer.this.elementsConsumers;
        elementsConsumers.add(consumer);
        if (elementsConsumers.size() == 1) {
          wrapped.materializeOrdered(new IndexedAsyncConsumer<E>() {
            @Override
            public void accept(final int size, final int index, final E param) {
              elementsCache.addElement(safeSize(size), index, param);
            }

            @Override
            public void complete(final int size) {
              final List<E> elements = elementsCache.addElement(safeSize(size), size, element);
              state = new ListToListAsyncMaterializer<E>(elements);
              isMaterialized.set(true);
              consumeElements(elements);
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
      safeConsume(consumer, false, LOGGER);
    }

    @Override
    public void materializeOrdered(@NotNull final IndexedAsyncConsumer<E> consumer) {
      wrapped.materializeOrdered(new IndexedAsyncConsumer<E>() {
        @Override
        public void accept(final int size, final int index, final E param) throws Exception {
          final int materialSize = safeSize(size);
          final List<E> elements = elementsCache.addElement(materialSize, index, param);
          if (elements != null) {
            isMaterialized.set(true);
            state = new ListToListAsyncMaterializer<E>(elements);
          }
          consumer.accept(materialSize, index, param);
        }

        @Override
        public void complete(final int size) throws Exception {
          final int materialSize = safeSize(size);
          final List<E> elements = elementsCache.addElement(materialSize, size, element);
          if (elements != null) {
            isMaterialized.set(true);
            state = new ListToListAsyncMaterializer<E>(elements);
          }
          consumer.accept(materialSize, size, element);
          consumer.complete(materialSize);
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
        public void accept(final Integer size) throws Exception {
          consumer.accept(safeSize(size));
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
          final int materialSize = safeSize(size);
          final List<E> elements = elementsCache.addElement(materialSize, index, param);
          if (elements != null) {
            isMaterialized.set(true);
            state = new ListToListAsyncMaterializer<E>(elements);
          }
          consumer.accept(materialSize, index, param);
        }

        @Override
        public void complete(final int size) throws Exception {
          final int materialSize = safeSize(size);
          final List<E> elements = elementsCache.addElement(materialSize, size, element);
          if (elements != null) {
            isMaterialized.set(true);
            state = new ListToListAsyncMaterializer<E>(elements);
          }
          consumer.accept(materialSize, size, element);
          consumer.complete(materialSize);
        }

        @Override
        public void error(final int index, @NotNull final Exception error) throws Exception {
          consumer.error(index, error);
        }
      });
    }

    private void consumeElements(@NotNull final List<E> elements) {
      final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = AppendListAsyncMaterializer.this.elementsConsumers;
      for (final AsyncConsumer<List<E>> elementsConsumer : elementsConsumers) {
        safeConsume(elementsConsumer, elements, LOGGER);
      }
      elementsConsumers.clear();
    }

    private void consumeError(@NotNull final Exception error) {
      final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = AppendListAsyncMaterializer.this.elementsConsumers;
      for (final AsyncConsumer<List<E>> elementsConsumer : elementsConsumers) {
        safeConsumeError(elementsConsumer, error, LOGGER);
      }
      elementsConsumers.clear();
    }

    private int safeSize(final int wrappedSize) {
      if (wrappedSize >= 0) {
        return SizeOverflowException.safeCast((long) wrappedSize + 1);
      }
      return wrappedSize;
    }
  }
}
