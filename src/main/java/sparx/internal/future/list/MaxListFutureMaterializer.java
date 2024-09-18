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

import static sparx.internal.future.FutureConsumers.safeConsumeError;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.FutureConsumer;
import sparx.internal.future.IndexedFutureConsumer;
import sparx.internal.future.IndexedFuturePredicate;

public class MaxListFutureMaterializer<E> extends AbstractListFutureMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(MaxListFutureMaterializer.class.getName());

  public MaxListFutureMaterializer(@NotNull final ListFutureMaterializer<E> wrapped,
      @NotNull final Comparator<? super E> comparator, @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    super(context, new AtomicInteger(STATUS_RUNNING));
    setState(new ImmaterialState(wrapped, comparator, cancelException));
  }

  @Override
  public boolean isMaterializedAtOnce() {
    return true;
  }

  @Override
  public int knownSize() {
    return -1;
  }

  private interface StateConsumer<E> {

    void accept(@NotNull ListFutureMaterializer<E> state);
  }

  private class ImmaterialState implements ListFutureMaterializer<E> {

    private final AtomicReference<CancellationException> cancelException;
    private final Comparator<? super E> comparator;
    private final ArrayList<StateConsumer<E>> stateConsumers = new ArrayList<StateConsumer<E>>(2);
    private final ListFutureMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final ListFutureMaterializer<E> wrapped,
        @NotNull final Comparator<? super E> comparator,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      this.wrapped = wrapped;
      this.comparator = comparator;
      this.cancelException = cancelException;
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
      return true;
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
      consumeState(setCancelled(exception));
    }

    @Override
    public void materializeContains(final Object element,
        @NotNull final FutureConsumer<Boolean> consumer) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<E> state) {
          state.materializeContains(element, consumer);
        }
      });
    }

    @Override
    public void materializeElement(final int index,
        @NotNull final IndexedFutureConsumer<E> consumer) {
      if (index < 0) {
        safeConsumeError(consumer, new IndexOutOfBoundsException(Integer.toString(index)), LOGGER);
      } else {
        materialized(new StateConsumer<E>() {
          @Override
          public void accept(@NotNull final ListFutureMaterializer<E> state) {
            state.materializeElement(index, consumer);
          }
        });
      }
    }

    @Override
    public void materializeElements(@NotNull final FutureConsumer<List<E>> consumer) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<E> state) {
          state.materializeElements(consumer);
        }
      });
    }

    @Override
    public void materializeEmpty(@NotNull final FutureConsumer<Boolean> consumer) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<E> state) {
          state.materializeEmpty(consumer);
        }
      });
    }

    @Override
    public void materializeHasElement(final int index,
        @NotNull final FutureConsumer<Boolean> consumer) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<E> state) {
          state.materializeHasElement(index, consumer);
        }
      });
    }

    @Override
    public void materializeNextWhile(final int index,
        @NotNull final IndexedFuturePredicate<E> predicate) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<E> state) {
          state.materializeNextWhile(index, predicate);
        }
      });
    }

    @Override
    public void materializePrevWhile(final int index,
        @NotNull final IndexedFuturePredicate<E> predicate) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<E> state) {
          state.materializePrevWhile(index, predicate);
        }
      });
    }

    @Override
    public void materializeSize(@NotNull final FutureConsumer<Integer> consumer) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<E> state) {
          state.materializeSize(consumer);
        }
      });
    }

    @Override
    public int weightContains() {
      return weightElements();
    }

    @Override
    public int weightElement() {
      return weightElements();
    }

    @Override
    public int weightElements() {
      return stateConsumers.isEmpty() ? wrapped.weightNextWhile() : 1;
    }

    @Override
    public int weightEmpty() {
      return weightElements();
    }

    @Override
    public int weightHasElement() {
      return weightElements();
    }

    @Override
    public int weightNextWhile() {
      return weightElements();
    }

    @Override
    public int weightPrevWhile() {
      return weightElements();
    }

    @Override
    public int weightSize() {
      return weightElements();
    }

    private void consumeState(@NotNull final ListFutureMaterializer<E> state) {
      final ArrayList<StateConsumer<E>> stateConsumers = this.stateConsumers;
      for (final StateConsumer<E> stateConsumer : stateConsumers) {
        stateConsumer.accept(state);
      }
      stateConsumers.clear();
    }

    private void materialized(@NotNull final StateConsumer<E> consumer) {
      final ArrayList<StateConsumer<E>> stateConsumers = this.stateConsumers;
      stateConsumers.add(consumer);
      if (stateConsumers.size() == 1) {
        wrapped.materializeNextWhile(0, new CancellableIndexedFuturePredicate<E>() {
          private E max;

          @Override
          public void cancellableComplete(final int size) {
            if (size == 0) {
              setState();
            } else {
              setState(max);
            }
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element) {
            if (index == 0 || comparator.compare(element, max) > 0) {
              max = element;
            }
            return true;
          }

          @Override
          public void error(@NotNull final Exception error) {
            final CancellationException exception = cancelException.get();
            if (exception != null) {
              consumeState(setCancelled(exception));
            } else {
              consumeState(setFailed(error));
            }
          }
        });
      }
    }

    private void setState() {
      consumeState(setDone(EmptyListFutureMaterializer.<E>instance()));
    }

    private void setState(final E element) {
      consumeState(setDone(new ElementToListFutureMaterializer<E>(element)));
    }
  }
}
