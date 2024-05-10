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
import sparx.util.function.IndexedPredicate;

public class AllListAsyncMaterializer<E> implements ListAsyncMaterializer<Boolean> {

  private static final ElementToListAsyncMaterializer<Boolean> FALSE_STATE = new ElementToListAsyncMaterializer<Boolean>(
      false);
  private static final ElementToListAsyncMaterializer<Boolean> TRUE_STATE = new ElementToListAsyncMaterializer<Boolean>(
      true);

  private final IndexedPredicate<? super E> predicate;
  private final ListAsyncMaterializer<E> wrapped;

  private ListAsyncMaterializer<Boolean> state;

  public AllListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      @NotNull final IndexedPredicate<? super E> predicate) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
    this.predicate = Require.notNull(predicate, "predicate");
  }

  @Override
  public boolean cancel(final boolean mayInterruptIfRunning) {
    return wrapped.cancel(mayInterruptIfRunning);
  }

  @Override
  public int knownSize() {
    return 1;
  }

  @Override
  public void materializeContains(final Object element,
      @NotNull final AsyncConsumer<Boolean> consumer) {
    materialized(new StateConsumer() {
      @Override
      public void accept(@NotNull final ListAsyncMaterializer<Boolean> state) {
        state.materializeContains(element, consumer);
      }
    });
  }

  @Override
  public void materializeElement(final int index,
      @NotNull final IndexedAsyncConsumer<Boolean> consumer) {
    materialized(new StateConsumer() {
      @Override
      public void accept(@NotNull final ListAsyncMaterializer<Boolean> state) {
        state.materializeElement(index, consumer);
      }
    });
  }

  @Override
  public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
    materialized(new StateConsumer() {
      @Override
      public void accept(@NotNull final ListAsyncMaterializer<Boolean> state) {
        state.materializeEmpty(consumer);
      }
    });
  }

  @Override
  public void materializeOrdered(@NotNull final IndexedAsyncConsumer<Boolean> consumer) {
    materialized(new StateConsumer() {
      @Override
      public void accept(@NotNull final ListAsyncMaterializer<Boolean> state) {
        state.materializeOrdered(consumer);
      }
    });
  }

  @Override
  public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
    materialized(new StateConsumer() {
      @Override
      public void accept(@NotNull final ListAsyncMaterializer<Boolean> state) {
        state.materializeSize(consumer);
      }
    });
  }

  @Override
  public void materializeUnordered(@NotNull final IndexedAsyncConsumer<Boolean> consumer) {
    materialized(new StateConsumer() {
      @Override
      public void accept(@NotNull final ListAsyncMaterializer<Boolean> state) {
        state.materializeUnordered(consumer);
      }
    });
  }

  private void materialized(@NotNull final StateConsumer consumer) {
    final ListAsyncMaterializer<E> wrapped = this.wrapped;
    wrapped.materializeEmpty(new AsyncConsumer<Boolean>() {
      @Override
      public void accept(final Boolean empty) {
        if (state != null) {
          consumer.accept(state);
        } else if (empty) {
          consumer.accept(state = TRUE_STATE);
        } else {
          wrapped.materializeElement(0, new IndexedAsyncConsumer<E>() {
            @Override
            public void accept(final int size, final int index, final E param) {
              try {
                if (!predicate.test(index, param)) {
                  consumer.accept(state = FALSE_STATE);
                } else {
                  wrapped.materializeElement(index + 1, this);
                }
              } catch (final Exception e) {
                consumer.accept(state = new FailedListAsyncMaterializer<Boolean>(1, index, e));
              }
            }

            @Override
            public void complete(final int size) {
              consumer.accept(state = TRUE_STATE);
            }

            @Override
            public void error(final int index, @NotNull final Exception error) {
              consumer.accept(state = new FailedListAsyncMaterializer<Boolean>(1, index, error));
            }
          });
        }
      }

      @Override
      public void error(@NotNull final Exception error) {
        consumer.accept(state = new FailedListAsyncMaterializer<Boolean>(1, 0, error));
      }
    });
  }

  private interface StateConsumer {

    void accept(@NotNull ListAsyncMaterializer<Boolean> state);
  }
}