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
import org.jetbrains.annotations.NotNull;
import sparx.collection.internal.future.AsyncConsumer;
import sparx.collection.internal.future.IndexedAsyncConsumer;
import sparx.util.Require;

public class CountListAsyncMaterializer<E> implements ListAsyncMaterializer<Integer> {

  private static final ElementToListAsyncMaterializer<Integer> ZERO_STATE = new ElementToListAsyncMaterializer<Integer>(
      0);

  private final AtomicBoolean isMaterialized = new AtomicBoolean(false);
  private final ArrayList<StateConsumer> stateConsumers = new ArrayList<StateConsumer>(2);
  private final ListAsyncMaterializer<E> wrapped;

  private ListAsyncMaterializer<Integer> state;

  public CountListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
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
    materialized(new StateConsumer() {
      @Override
      public void accept(@NotNull final ListAsyncMaterializer<Integer> state) {
        state.materializeContains(element, consumer);
      }
    });
  }

  @Override
  public void materializeElement(final int index,
      @NotNull final IndexedAsyncConsumer<Integer> consumer) {
    materialized(new StateConsumer() {
      @Override
      public void accept(@NotNull final ListAsyncMaterializer<Integer> state) {
        state.materializeElement(index, consumer);
      }
    });
  }

  @Override
  public void materializeElements(@NotNull final AsyncConsumer<List<Integer>> consumer) {
    materialized(new StateConsumer() {
      @Override
      public void accept(@NotNull final ListAsyncMaterializer<Integer> state) {
        state.materializeElements(consumer);
      }
    });
  }

  @Override
  public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
    materialized(new StateConsumer() {
      @Override
      public void accept(@NotNull final ListAsyncMaterializer<Integer> state) {
        state.materializeEmpty(consumer);
      }
    });
  }

  @Override
  public void materializeOrdered(@NotNull final IndexedAsyncConsumer<Integer> consumer) {
    materialized(new StateConsumer() {
      @Override
      public void accept(@NotNull final ListAsyncMaterializer<Integer> state) {
        state.materializeOrdered(consumer);
      }
    });
  }

  @Override
  public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
    materialized(new StateConsumer() {
      @Override
      public void accept(@NotNull final ListAsyncMaterializer<Integer> state) {
        state.materializeSize(consumer);
      }
    });
  }

  @Override
  public void materializeUnordered(@NotNull final IndexedAsyncConsumer<Integer> consumer) {
    materialized(new StateConsumer() {
      @Override
      public void accept(@NotNull final ListAsyncMaterializer<Integer> state) {
        state.materializeUnordered(consumer);
      }
    });
  }

  private void consumeState(@NotNull final ListAsyncMaterializer<Integer> state) {
    final ArrayList<StateConsumer> stateConsumers = this.stateConsumers;
    for (final StateConsumer stateConsumer : stateConsumers) {
      stateConsumer.accept(state);
    }
    stateConsumers.clear();
  }

  private void materialized(@NotNull final StateConsumer consumer) {
    final ListAsyncMaterializer<E> wrapped = this.wrapped;
    wrapped.materializeEmpty(new AsyncConsumer<Boolean>() {
      @Override
      public void accept(final Boolean empty) {
        if (state != null) {
          consumer.accept(state);
        } else if (empty) {
          isMaterialized.set(true);
          consumer.accept(state = ZERO_STATE);
        } else {
          final ArrayList<StateConsumer> stateConsumers = CountListAsyncMaterializer.this.stateConsumers;
          stateConsumers.add(consumer);
          if (stateConsumers.size() == 1) {
            wrapped.materializeSize(new AsyncConsumer<Integer>() {
              @Override
              public void accept(final Integer param) {
                isMaterialized.set(true);
                consumeState(state = new ElementToListAsyncMaterializer<Integer>(param));
              }

              @Override
              public void error(@NotNull final Exception error) {
                isMaterialized.set(true);
                consumeState(state = new FailedListAsyncMaterializer<Integer>(1, 0, error));
              }
            });
          }
        }
      }

      @Override
      public void error(@NotNull final Exception error) {
        isMaterialized.set(true);
        consumer.accept(state = new FailedListAsyncMaterializer<Integer>(1, 0, error));
      }
    });
  }

  private interface StateConsumer {

    void accept(@NotNull ListAsyncMaterializer<Integer> state);
  }
}
