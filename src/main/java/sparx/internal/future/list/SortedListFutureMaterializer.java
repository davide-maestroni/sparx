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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.FutureConsumer;
import sparx.internal.future.IndexedFutureConsumer;
import sparx.internal.future.IndexedFuturePredicate;
import sparx.util.annotation.NotNegative;

public class SortedListFutureMaterializer<E> extends AbstractListFutureMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      SortedListFutureMaterializer.class.getName());

  private final int knownSize;

  public SortedListFutureMaterializer(@NotNull final ListFutureMaterializer<E> wrapped,
      @NotNull final Comparator<? super E> comparator, @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    super(context);
    setState(new ImmaterialState(wrapped, comparator, context, cancelException));
    knownSize = wrapped.knownSize();
  }

  @Override
  public int knownSize() {
    return knownSize;
  }

  private interface StateConsumer<E> {

    void accept(@NotNull ListFutureMaterializer<E> state);
  }

  private class ImmaterialState implements ListFutureMaterializer<E> {

    private final AtomicReference<CancellationException> cancelException;
    private final Comparator<? super E> comparator;
    private final ExecutionContext context;
    private final ArrayList<StateConsumer<E>> stateConsumers = new ArrayList<StateConsumer<E>>(2);
    private final ListFutureMaterializer<E> wrapped;

    private int wrappedSize;

    private ImmaterialState(@NotNull final ListFutureMaterializer<E> wrapped,
        @NotNull final Comparator<? super E> comparator, @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      this.wrapped = wrapped;
      this.comparator = comparator;
      this.context = context;
      this.cancelException = cancelException;
      wrappedSize = wrapped.knownSize();
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
      return wrappedSize;
    }

    @Override
    public void materializeCancel(@NotNull final CancellationException exception) {
      wrapped.materializeCancel(exception);
      consumeState(setCancelled(exception));
    }

    @Override
    public void materializeContains(final Object element,
        @NotNull final FutureConsumer<Boolean> consumer) {
      wrapped.materializeContains(element, new CancellableFutureConsumer<Boolean>() {
        @Override
        public void cancellableAccept(final Boolean contains) throws Exception {
          consumer.accept(contains);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeElement(@NotNegative final int index,
        @NotNull final IndexedFutureConsumer<E> consumer) {
      if (wrappedSize >= 0 && index >= wrappedSize) {
        safeConsumeComplete(consumer, wrappedSize, LOGGER);
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
      if (wrappedSize >= 0) {
        safeConsume(consumer, wrappedSize == 0, LOGGER);
      } else {
        wrapped.materializeEmpty(new CancellableFutureConsumer<Boolean>() {
          @Override
          public void cancellableAccept(final Boolean empty) throws Exception {
            consumer.accept(empty);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
    }

    @Override
    public void materializeHasElement(@NotNegative final int index,
        @NotNull final FutureConsumer<Boolean> consumer) {
      if (wrappedSize >= 0) {
        safeConsume(consumer, index < wrappedSize, LOGGER);
      } else {
        wrapped.materializeHasElement(index, new CancellableFutureConsumer<Boolean>() {
          @Override
          public void cancellableAccept(final Boolean hasElement) throws Exception {
            consumer.accept(hasElement);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
    }

    @Override
    public void materializeNextWhile(@NotNegative final int index,
        @NotNull final IndexedFuturePredicate<E> predicate) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final ListFutureMaterializer<E> state) {
          state.materializeNextWhile(index, predicate);
        }
      });
    }

    @Override
    public void materializePrevWhile(@NotNegative final int index,
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
      if (wrappedSize >= 0) {
        safeConsume(consumer, wrappedSize, LOGGER);
      } else {
        wrapped.materializeSize(new CancellableFutureConsumer<Integer>() {
          @Override
          public void cancellableAccept(final Integer size) throws Exception {
            consumer.accept(wrappedSize = size);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
    }

    @Override
    public int weightContains() {
      return wrapped.weightContains();
    }

    @Override
    public int weightElement() {
      return weightElements();
    }

    @Override
    public int weightElements() {
      return stateConsumers.isEmpty() ? wrapped.weightElements() : 1;
    }

    @Override
    public int weightHasElement() {
      return wrappedSize < 0 ? wrapped.weightHasElement() : 1;
    }

    @Override
    public int weightEmpty() {
      return wrappedSize < 0 ? wrapped.weightEmpty() : 1;
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
      return wrappedSize < 0 ? wrapped.weightSize() : 1;
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
        wrapped.materializeElements(new CancellableFutureConsumer<List<E>>() {
          @Override
          @SuppressWarnings("unchecked")
          public void cancellableAccept(final List<E> elements) {
            final Object[] array = elements.toArray();
            if (array.length == 0) {
              consumeState(setDone(EmptyListFutureMaterializer.<E>instance()));
            } else if (array.length == 1) {
              consumeState(setDone(new ElementToListFutureMaterializer<E>((E) array[0])));
            } else {
              Arrays.sort(array, (Comparator<? super Object>) comparator);
              final List<E> materialized = (List<E>) Arrays.asList(array);
              consumeState(setDone(new ListToListFutureMaterializer<E>(materialized, context)));
            }
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
  }
}
