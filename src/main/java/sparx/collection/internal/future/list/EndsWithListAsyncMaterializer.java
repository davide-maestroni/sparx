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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.collection.internal.future.AsyncConsumer;
import sparx.collection.internal.future.IndexedAsyncConsumer;
import sparx.util.Require;
import sparx.util.function.Function;

public class EndsWithListAsyncMaterializer<E> implements ListAsyncMaterializer<Boolean> {

  private static final Logger LOGGER = Logger.getLogger(
      EndsWithListAsyncMaterializer.class.getName());

  private static final int STATUS_CANCELLED = 2;
  private static final int STATUS_DONE = 1;
  private static final int STATUS_RUNNING = 0;

  private final AtomicInteger status = new AtomicInteger(STATUS_RUNNING);

  private ListAsyncMaterializer<Boolean> state;

  public EndsWithListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      @NotNull final ListAsyncMaterializer<Object> elementsMaterializer,
      @NotNull final AtomicBoolean isCancelled,
      @NotNull final Function<List<Boolean>, List<Boolean>> decorateFunction) {
    state = new ImmaterialState(Require.notNull(wrapped, "wrapped"),
        Require.notNull(elementsMaterializer, "elementsMaterializer"),
        Require.notNull(isCancelled, "isCancelled"),
        Require.notNull(decorateFunction, "decorateFunction"));
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
    return 1;
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
  public void materializeEach(@NotNull final IndexedAsyncConsumer<Boolean> consumer) {
    state.materializeEach(consumer);
  }

  @Override
  public void materializeElement(final int index,
      @NotNull final IndexedAsyncConsumer<Boolean> consumer) {
    state.materializeElement(index, consumer);
  }

  @Override
  public void materializeElements(@NotNull final AsyncConsumer<List<Boolean>> consumer) {
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

  private interface StateConsumer {

    void accept(@NotNull ListAsyncMaterializer<Boolean> state);
  }

  private class ImmaterialState extends AbstractListAsyncMaterializer<Boolean> {

    private final Function<List<Boolean>, List<Boolean>> decorateFunction;
    private final ListAsyncMaterializer<Object> elementsMaterializer;
    private final AtomicBoolean isCancelled;
    private final ArrayList<StateConsumer> stateConsumers = new ArrayList<StateConsumer>(2);
    private final ListAsyncMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final ListAsyncMaterializer<E> wrapped,
        @NotNull final ListAsyncMaterializer<Object> elementsMaterializer,
        @NotNull final AtomicBoolean isCancelled,
        @NotNull final Function<List<Boolean>, List<Boolean>> decorateFunction) {
      this.wrapped = wrapped;
      this.elementsMaterializer = elementsMaterializer;
      this.isCancelled = isCancelled;
      this.decorateFunction = decorateFunction;
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
      return 1;
    }

    @Override
    public void materializeCancel(final boolean mayInterruptIfRunning) {
      wrapped.materializeCancel(mayInterruptIfRunning);
      elementsMaterializer.materializeCancel(mayInterruptIfRunning);
      setState(new CancelledListAsyncMaterializer<Boolean>(), STATUS_CANCELLED);
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
    public void materializeEach(@NotNull final IndexedAsyncConsumer<Boolean> consumer) {
      materialized(new StateConsumer() {
        @Override
        public void accept(@NotNull final ListAsyncMaterializer<Boolean> state) {
          state.materializeEach(consumer);
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
    public void materializeElements(@NotNull final AsyncConsumer<List<Boolean>> consumer) {
      materialized(new StateConsumer() {
        @Override
        public void accept(@NotNull final ListAsyncMaterializer<Boolean> state) {
          state.materializeElements(consumer);
        }
      });
    }

    @Override
    public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
      safeConsume(consumer, false, LOGGER);
    }

    @Override
    public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
      safeConsume(consumer, 1, LOGGER);
    }

    private void materialized(@NotNull final StateConsumer consumer) {
      final ArrayList<StateConsumer> stateConsumers = this.stateConsumers;
      stateConsumers.add(consumer);
      if (stateConsumers.size() == 1) {
        final ListAsyncMaterializer<E> wrapped = this.wrapped;
        final ListAsyncMaterializer<Object> elementsMaterializer = this.elementsMaterializer;
        elementsMaterializer.materializeSize(new AsyncConsumer<Integer>() {
          @Override
          public void accept(final Integer elementsSize) throws Exception {
            if (elementsSize == 0) {
              setState(true);
            } else {
              wrapped.materializeSize(new AsyncConsumer<Integer>() {
                @Override
                public void accept(final Integer wrappedSize) throws Exception {
                  if (wrappedSize == 0) {
                    setState(false);
                  } else {
                    elementsMaterializer.materializeElement(elementsSize - 1,
                        new ComparingConsumer(wrappedSize));
                  }
                }

                @Override
                public void error(@NotNull final Exception error) {
                  setState(error);
                }
              });
            }
          }

          @Override
          public void error(@NotNull final Exception error) {
            setState(error);
          }
        });
      }
    }

    private void setState(final boolean endsWith) throws Exception {
      setState(new ListToListAsyncMaterializer<Boolean>(
          decorateFunction.apply(Collections.singletonList(endsWith))), STATUS_DONE);
    }

    private void setState(@NotNull final Exception error) {
      if (isCancelled.get()) {
        setState(new CancelledListAsyncMaterializer<Boolean>(), STATUS_CANCELLED);
      } else {
        setState(new FailedListAsyncMaterializer<Boolean>(error), STATUS_DONE);
      }
    }

    private void setState(@NotNull final ListAsyncMaterializer<Boolean> newState,
        final int statusCode) {
      final ListAsyncMaterializer<Boolean> state;
      if (status.compareAndSet(STATUS_RUNNING, statusCode)) {
        state = EndsWithListAsyncMaterializer.this.state = newState;
      } else {
        state = EndsWithListAsyncMaterializer.this.state;
      }
      final ArrayList<StateConsumer> stateConsumers = this.stateConsumers;
      for (final StateConsumer stateConsumer : stateConsumers) {
        stateConsumer.accept(state);
      }
      stateConsumers.clear();
    }

    private class ComparingConsumer implements IndexedAsyncConsumer<Object> {

      private int wrappedIndex;

      private ComparingConsumer(final int wrappedSize) {
        wrappedIndex = wrappedSize;
      }

      @Override
      public void accept(final int size, final int elementIndex, final Object element)
          throws Exception {
        if (wrappedIndex == 0) {
          setState(false);
        } else {
          final IndexedAsyncConsumer<Object> elementConsumer = this;
          if (element == null) {
            wrapped.materializeElement(--wrappedIndex, new IndexedAsyncConsumer<E>() {
              @Override
              public void accept(final int size, final int index, final E wrappedElement)
                  throws Exception {
                if (wrappedElement != null) {
                  setState(false);
                } else if (elementIndex == 0) {
                  setState(true);
                } else {
                  elementsMaterializer.materializeElement(elementIndex - 1, elementConsumer);
                }
              }

              @Override
              public void complete(final int size) {
              }

              @Override
              public void error(final int index, @NotNull final Exception error) {
                setState(error);
              }
            });
          } else {
            wrapped.materializeElement(--wrappedIndex, new IndexedAsyncConsumer<E>() {
              @Override
              public void accept(final int size, final int index, final E wrappedElement)
                  throws Exception {
                if (!element.equals(wrappedElement)) {
                  setState(false);
                } else if (elementIndex == 0) {
                  setState(true);
                } else {
                  elementsMaterializer.materializeElement(elementIndex - 1, elementConsumer);
                }
              }

              @Override
              public void complete(final int size) {
              }

              @Override
              public void error(final int index, @NotNull final Exception error) {
                setState(error);
              }
            });
          }
        }
      }

      @Override
      public void complete(final int size) {
      }

      @Override
      public void error(final int index, @NotNull final Exception error) {
        setState(error);
      }
    }
  }
}
