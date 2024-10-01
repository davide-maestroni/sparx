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

import static sparx.internal.future.FutureConsumers.safeConsume;
import static sparx.internal.future.FutureConsumers.safeConsumeComplete;
import static sparx.internal.future.FutureConsumers.safeConsumeError;

import java.util.ArrayList;
import java.util.HashSet;
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
import sparx.util.annotation.NotNegative;

public class IncludesAllListFutureMaterializer<E> extends AbstractListFutureMaterializer<Boolean> {

  private static final Logger LOGGER = Logger.getLogger(
      IncludesAllListFutureMaterializer.class.getName());

  public IncludesAllListFutureMaterializer(@NotNull final ListFutureMaterializer<E> wrapped,
      @NotNull final ListFutureMaterializer<Object> elementsMaterializer,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    super(context, new AtomicInteger(STATUS_RUNNING));
    setState(new ImmaterialState(wrapped, elementsMaterializer, cancelException));
  }

  @Override
  public int knownSize() {
    return 1;
  }

  private interface StateConsumer {

    void accept(@NotNull ListFutureMaterializer<Boolean> state);
  }

  private class ImmaterialState implements ListFutureMaterializer<Boolean> {

    private final AtomicReference<CancellationException> cancelException;
    private final ListFutureMaterializer<Object> elementsMaterializer;
    private final ArrayList<StateConsumer> stateConsumers = new ArrayList<StateConsumer>(2);
    private final ListFutureMaterializer<E> wrapped;

    private ImmaterialState(@NotNull final ListFutureMaterializer<E> wrapped,
        @NotNull final ListFutureMaterializer<Object> elementsMaterializer,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      this.wrapped = wrapped;
      this.elementsMaterializer = elementsMaterializer;
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
      return false;
    }

    @Override
    public boolean isSucceeded() {
      return false;
    }

    @Override
    public int knownSize() {
      return 1;
    }

    @Override
    public void materializeCancel(@NotNull final CancellationException exception) {
      wrapped.materializeCancel(exception);
      consumeState(setCancelled(exception));
    }

    @Override
    public void materializeContains(final Object element,
        @NotNull final FutureConsumer<Boolean> consumer) {
      materialized(new StateConsumer() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<Boolean> state) {
          state.materializeContains(element, consumer);
        }
      });
    }

    @Override
    public void materializeElement(@NotNegative final int index,
        @NotNull final IndexedFutureConsumer<Boolean> consumer) {
      if (index > 1) {
        safeConsumeComplete(consumer, 1, LOGGER);
      } else {
        materialized(new StateConsumer() {
          @Override
          public void accept(@NotNull final ListFutureMaterializer<Boolean> state) {
            state.materializeElement(index, consumer);
          }
        });
      }
    }

    @Override
    public void materializeElements(@NotNull final FutureConsumer<List<Boolean>> consumer) {
      materialized(new StateConsumer() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<Boolean> state) {
          state.materializeElements(consumer);
        }
      });
    }

    @Override
    public void materializeEmpty(@NotNull final FutureConsumer<Boolean> consumer) {
      safeConsume(consumer, false, LOGGER);
    }

    @Override
    public void materializeHasElement(@NotNegative final int index,
        @NotNull final FutureConsumer<Boolean> consumer) {
      safeConsume(consumer, index == 0, LOGGER);
    }

    @Override
    public void materializeNextWhile(@NotNegative final int index,
        @NotNull final IndexedFuturePredicate<Boolean> predicate) {
      materialized(new StateConsumer() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<Boolean> state) {
          state.materializeNextWhile(index, predicate);
        }
      });
    }

    @Override
    public void materializePrevWhile(@NotNegative final int index,
        @NotNull final IndexedFuturePredicate<Boolean> predicate) {
      materialized(new StateConsumer() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<Boolean> state) {
          state.materializePrevWhile(index, predicate);
        }
      });
    }

    @Override
    public void materializeSize(@NotNull final FutureConsumer<Integer> consumer) {
      safeConsume(consumer, 1, LOGGER);
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
      if (stateConsumers.isEmpty()) {
        return (int) Math.min(Integer.MAX_VALUE,
            (long) wrapped.weightNextWhile() + elementsMaterializer.weightElements());
      }
      return 1;
    }

    @Override
    public int weightEmpty() {
      return 1;
    }

    @Override
    public int weightHasElement() {
      return 1;
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
      return 1;
    }

    private void consumeState(@NotNull final ListFutureMaterializer<Boolean> state) {
      final ArrayList<StateConsumer> stateConsumers = this.stateConsumers;
      for (final StateConsumer stateConsumer : stateConsumers) {
        stateConsumer.accept(state);
      }
      stateConsumers.clear();
    }

    private void materialized(@NotNull final StateConsumer consumer) {
      final ArrayList<StateConsumer> stateConsumers = this.stateConsumers;
      stateConsumers.add(consumer);
      if (stateConsumers.size() == 1) {
        elementsMaterializer.materializeElements(new CancellableFutureConsumer<List<Object>>() {
          @Override
          public void cancellableAccept(final List<Object> elements) {
            if (elements.isEmpty()) {
              setState(true);
            } else {
              final HashSet<Object> elementSet = new HashSet<Object>(elements);
              wrapped.materializeNextWhile(0, new CancellableIndexedFuturePredicate<E>() {
                @Override
                public void cancellableComplete(final int size) {
                  setState(false);
                }

                @Override
                public boolean cancellableTest(final int size, final int index, final E element) {
                  elementSet.remove(element);
                  if (elementSet.isEmpty()) {
                    setState(true);
                    return false;
                  }
                  return true;
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

    private void setError(@NotNull final Exception error) {
      final CancellationException exception = cancelException.get();
      if (exception != null) {
        consumeState(setCancelled(exception));
      } else {
        consumeState(setFailed(error));
      }
    }

    private void setState(final boolean matches) {
      consumeState(setDone(new ElementToListFutureMaterializer<Boolean>(matches)));
    }
  }
}
