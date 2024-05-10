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

import org.jetbrains.annotations.NotNull;
import sparx.collection.internal.future.AsyncConsumer;
import sparx.collection.internal.future.IndexedAsyncConsumer;
import sparx.util.Require;
import sparx.util.SizeOverflowException;

public class AppendListAsyncMaterializer<E> implements ListAsyncMaterializer<E> {

  private final E element;
  private final ListAsyncMaterializer<E> wrapped;

  private ListAsyncMaterializer<E> state;

  public AppendListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      @NotNull final E element) {
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
  public void materializeDone(@NotNull final AsyncConsumer<Boolean> consumer) {
    wrapped.materializeDone(new AsyncConsumer<Boolean>() {
      @Override
      public void accept(final Boolean done) throws Exception {
        if (done) {
          consumer.accept(state != null);
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
          consumer.accept(state = new ElementToListAsyncMaterializer<E>(element));
        } else {
          consumer.accept(state = new AppendState());
        }
      }

      @Override
      public void error(@NotNull final Exception error) {
        consumer.accept(state = new FailedListAsyncMaterializer<E>(knownSize(), 0, error));
      }
    });
  }

  private interface StateConsumer<E> {

    void accept(@NotNull ListAsyncMaterializer<E> state);
  }

  private class AppendState implements ListAsyncMaterializer<E> {

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
      return false;
    }

    @Override
    public int knownSize() {
      return -1;
    }

    @Override
    public void materializeContains(final Object element,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      final E appended = AppendListAsyncMaterializer.this.element;
      if (element == appended || (element != null && element.equals(appended))) {
        try {
          consumer.accept(true);
        } catch (final Exception e) {
          // TODO
        }
      } else {
        wrapped.materializeContains(element, consumer);
      }
    }

    @Override
    public void materializeDone(@NotNull final AsyncConsumer<Boolean> consumer) {
      try {
        consumer.error(new UnsupportedOperationException());
      } catch (final Exception e) {
        // TODO
      }
    }

    @Override
    public void materializeElement(final int index,
        @NotNull final IndexedAsyncConsumer<E> consumer) {
      wrapped.materializeElement(index, new IndexedAsyncConsumer<E>() {
        @Override
        public void accept(final int size, final int index, final E param) throws Exception {
          consumer.accept(SizeOverflowException.safeCast((long) size + 1), index, param);
        }

        @Override
        public void complete(final int size) throws Exception {
          if (size == index) {
            consumer.accept(SizeOverflowException.safeCast((long) size + 1), size, element);
          } else if (size >= 0) {
            consumer.complete(SizeOverflowException.safeCast((long) size + 1));
          } else {
            consumer.complete(size);
          }
        }

        @Override
        public void error(final int index, @NotNull final Exception error) throws Exception {
          consumer.error(index, error);
        }
      });
    }

    @Override
    public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
      try {
        consumer.accept(false);
      } catch (final Exception e) {
        // TODO
      }
    }

    @Override
    public void materializeOrdered(@NotNull final IndexedAsyncConsumer<E> consumer) {
      wrapped.materializeOrdered(new IndexedAsyncConsumer<E>() {
        @Override
        public void accept(final int size, final int index, final E param) throws Exception {
          consumer.accept(SizeOverflowException.safeCast((long) size + 1), index, param);
        }

        @Override
        public void complete(final int size) throws Exception {
          final int appendSize = SizeOverflowException.safeCast((long) size + 1);
          consumer.accept(appendSize, size, element);
          consumer.complete(appendSize);
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
          consumer.accept(SizeOverflowException.safeCast((long) size + 1));
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
          consumer.accept(SizeOverflowException.safeCast((long) size + 1), index, param);
        }

        @Override
        public void complete(final int size) throws Exception {
          final int appendSize = SizeOverflowException.safeCast((long) size + 1);
          consumer.accept(appendSize, size, element);
          consumer.complete(appendSize);
        }

        @Override
        public void error(final int index, @NotNull final Exception error) throws Exception {
          consumer.error(index, error);
        }
      });
    }
  }
}
