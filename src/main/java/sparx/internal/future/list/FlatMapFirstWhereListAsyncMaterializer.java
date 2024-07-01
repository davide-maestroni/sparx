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

import static sparx.internal.future.AsyncConsumers.safeConsume;
import static sparx.internal.future.AsyncConsumers.safeConsumeError;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.concurrent.ExecutionContext.Task;
import sparx.internal.future.AsyncConsumer;
import sparx.internal.future.IndexedAsyncConsumer;
import sparx.util.function.Function;
import sparx.util.function.IndexedFunction;
import sparx.util.function.IndexedPredicate;

public class FlatMapFirstWhereListAsyncMaterializer<E> extends AbstractListAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      FlatMapFirstWhereListAsyncMaterializer.class.getName());

  public FlatMapFirstWhereListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      @NotNull final IndexedPredicate<? super E> predicate,
      @NotNull final IndexedFunction<? super E, ? extends ListAsyncMaterializer<E>> mapper,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final Function<List<E>, List<E>> decorateFunction) {
    super(new AtomicInteger(STATUS_RUNNING));
    setState(new ImmaterialState(wrapped, predicate, mapper, context, cancelException,
        decorateFunction));
  }

  @Override
  public int knownSize() {
    return -1;
  }

  private interface StateConsumer<E> {

    void accept(@NotNull ListAsyncMaterializer<E> state);
  }

  private class ImmaterialState implements ListAsyncMaterializer<E> {

    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final Function<List<E>, List<E>> decorateFunction;
    private final IndexedFunction<? super E, ? extends ListAsyncMaterializer<E>> mapper;
    private final IndexedPredicate<? super E> predicate;
    private final ArrayList<StateConsumer<E>> stateConsumers = new ArrayList<StateConsumer<E>>(2);
    private final ListAsyncMaterializer<E> wrapped;

    private int testedIndex = -1;

    public ImmaterialState(@NotNull final ListAsyncMaterializer<E> wrapped,
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends ListAsyncMaterializer<E>> mapper,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Function<List<E>, List<E>> decorateFunction) {
      this.wrapped = wrapped;
      this.predicate = predicate;
      this.mapper = mapper;
      this.context = context;
      this.cancelException = cancelException;
      this.decorateFunction = decorateFunction;
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
        @NotNull final AsyncConsumer<Boolean> consumer) {
      if (element == null) {
        new MaterializingContainsNullAsyncConsumer(consumer).run();
      } else {
        new MaterializingContainsElementAsyncConsumer(element, consumer).run();
      }
    }

    @Override
    public void materializeDone(@NotNull final AsyncConsumer<List<E>> consumer) {
      safeConsumeError(consumer, new UnsupportedOperationException(), LOGGER);
    }

    @Override
    public void materializeEach(@NotNull final IndexedAsyncConsumer<E> consumer) {
      materialized(new StateConsumer<E>() {
        @Override
        public void accept(@NotNull final ListAsyncMaterializer<E> state) {
          state.materializeEach(consumer);
        }
      });
    }

    @Override
    public void materializeElement(final int index,
        @NotNull final IndexedAsyncConsumer<E> consumer) {
      if (index < 0) {
        safeConsumeError(consumer, new IndexOutOfBoundsException(Integer.toString(index)), LOGGER);
      } else if (index <= testedIndex) {
        wrapped.materializeElement(index, new CancellableIndexedAsyncConsumer<E>() {
          @Override
          public void cancellableAccept(final int size, final int index, final E element)
              throws Exception {
            consumer.accept(-1, index, element);
          }

          @Override
          public void cancellableComplete(final int size) throws Exception {
            consumer.complete(size);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      } else {
        wrapped.materializeElement(testedIndex + 1,
            new MaterializingElementAsyncConsumer(index, consumer));
      }
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
      if (testedIndex >= 0) {
        safeConsume(consumer, false, LOGGER);
      } else {
        wrapped.materializeElement(0, new CancellableIndexedAsyncConsumer<E>() {
          @Override
          public void cancellableAccept(final int size, final int index, final E element)
              throws Exception {
            if (testedIndex < index) {
              try {
                if (predicate.test(index, element)) {
                  final ListAsyncMaterializer<E> state = setState(
                      new FlatMapAfterListAsyncMaterializer<E>(wrapped, index, mapper, status,
                          context, cancelException, decorateFunction));
                  consumeState(state);
                  state.materializeEmpty(consumer);
                  return;
                }
              } catch (final Exception e) {
                setError(e);
                throw e;
              }
            }
            consumer.accept(false);
          }

          @Override
          public void cancellableComplete(final int size) throws Exception {
            consumer.accept(true);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
    }

    @Override
    public void materializeHasElement(final int index,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      if (index < 0) {
        safeConsumeError(consumer, new IndexOutOfBoundsException(Integer.toString(index)), LOGGER);
      } else if (index <= testedIndex) {
        safeConsume(consumer, true, LOGGER);
      } else {
        wrapped.materializeElement(testedIndex + 1,
            new MaterializingHasElementAsyncConsumer(index, consumer));
      }
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
    public int weightContains() {
      return wrapped.weightElement();
    }

    @Override
    public int weightEach() {
      return weightElements();
    }

    @Override
    public int weightElement() {
      return wrapped.weightElement();
    }

    @Override
    public int weightElements() {
      return stateConsumers.isEmpty() ? wrapped.weightElement() : 1;
    }

    @Override
    public int weightEmpty() {
      return testedIndex < 0 ? wrapped.weightElement() : 1;
    }

    @Override
    public int weightHasElement() {
      return wrapped.weightElement();
    }

    @Override
    public int weightSize() {
      return wrapped.weightElement();
    }

    private void consumeState(@NotNull final ListAsyncMaterializer<E> state) {
      final ArrayList<StateConsumer<E>> stateConsumers = this.stateConsumers;
      for (final StateConsumer<E> stateConsumer : stateConsumers) {
        stateConsumer.accept(state);
      }
      stateConsumers.clear();
    }

    private @NotNull String getTaskID() {
      final String taskID = context.currentTaskID();
      return taskID != null ? taskID : "";
    }

    private void materialized(@NotNull final StateConsumer<E> consumer) {
      final ArrayList<StateConsumer<E>> stateConsumers = this.stateConsumers;
      stateConsumers.add(consumer);
      if (stateConsumers.size() == 1) {
        wrapped.materializeElement(testedIndex + 1, new MaterializingAsyncConsumer());
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

    private class MaterializingAsyncConsumer extends CancellableIndexedAsyncConsumer<E> implements
        Task {

      private int index;
      private String taskID;

      @Override
      public void cancellableAccept(final int size, final int index, final E element)
          throws Exception {
        if (testedIndex < index) {
          if (predicate.test(index, element)) {
            consumeState(setState(
                new FlatMapAfterListAsyncMaterializer<E>(wrapped, index, mapper, status, context,
                    cancelException, decorateFunction)));
            return;
          }
          testedIndex = index;
        }
        if (!stateConsumers.isEmpty()) {
          this.index = index + 1;
          taskID = getTaskID();
          context.scheduleAfter(this);
        }
      }

      @Override
      public void cancellableComplete(final int size) {
        consumeState(setState(wrapped));
      }

      @Override
      public void error(@NotNull final Exception error) {
        setError(error);
      }

      @Override
      public void run() {
        wrapped.materializeElement(index, this);
      }

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        return wrapped.weightElement();
      }
    }

    private class MaterializingContainsElementAsyncConsumer extends
        CancellableIndexedAsyncConsumer<E> implements Task {

      private final AsyncConsumer<Boolean> consumer;
      private final Object element;

      private int index;
      private String taskID;

      private MaterializingContainsElementAsyncConsumer(@NotNull final Object element,
          @NotNull final AsyncConsumer<Boolean> consumer) {
        this.element = element;
        this.consumer = consumer;
      }

      @Override
      public void cancellableAccept(final int size, final int index, final E element)
          throws Exception {
        if (testedIndex < index) {
          try {
            if (predicate.test(index, element)) {
              final ListAsyncMaterializer<E> state = setState(
                  new FlatMapAfterListAsyncMaterializer<E>(wrapped, index, mapper, status, context,
                      cancelException, decorateFunction));
              consumeState(state);
              if (index == 0) {
                state.materializeContains(this.element, consumer);
              } else {
                new DropListAsyncMaterializer<E>(state, index, context, cancelException,
                    decorateFunction).materializeContains(this.element, consumer);
              }
              return;
            }
          } catch (final Exception e) {
            setError(e);
            throw e;
          }
          testedIndex = index;
        }
        if (this.element.equals(element)) {
          consumer.accept(true);
        } else {
          this.index = index + 1;
          taskID = getTaskID();
          context.scheduleAfter(this);
        }
      }

      @Override
      public void cancellableComplete(final int size) throws Exception {
        consumeState(setState(wrapped));
        consumer.accept(false);
      }

      @Override
      public void error(@NotNull final Exception error) throws Exception {
        consumer.error(error);
      }

      @Override
      public void run() {
        wrapped.materializeElement(index, this);
      }

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        final ListAsyncMaterializer<E> wrapped = ImmaterialState.this.wrapped;
        return (int) Math.min(Integer.MAX_VALUE,
            (long) wrapped.weightElement() + wrapped.weightContains());
      }
    }

    private class MaterializingContainsNullAsyncConsumer extends
        CancellableIndexedAsyncConsumer<E> implements Task {

      private final AsyncConsumer<Boolean> consumer;

      private int index;
      private String taskID;

      private MaterializingContainsNullAsyncConsumer(
          @NotNull final AsyncConsumer<Boolean> consumer) {
        this.consumer = consumer;
      }

      @Override
      public void cancellableAccept(final int size, final int index, final E element)
          throws Exception {
        if (testedIndex < index) {
          try {
            if (predicate.test(index, element)) {
              final ListAsyncMaterializer<E> state = setState(
                  new FlatMapAfterListAsyncMaterializer<E>(wrapped, index, mapper, status, context,
                      cancelException, decorateFunction));
              consumeState(state);
              if (index == 0) {
                state.materializeContains(null, consumer);
              } else {
                new DropListAsyncMaterializer<E>(state, index, context, cancelException,
                    decorateFunction).materializeContains(null, consumer);
              }
              return;
            }
          } catch (final Exception e) {
            setError(e);
            throw e;
          }
          testedIndex = index;
        }
        if (element == null) {
          consumer.accept(true);
        } else {
          this.index = index + 1;
          taskID = getTaskID();
          context.scheduleAfter(this);
        }
      }

      @Override
      public void cancellableComplete(final int size) throws Exception {
        consumeState(setState(wrapped));
        consumer.accept(false);
      }

      @Override
      public void error(@NotNull final Exception error) throws Exception {
        consumer.error(error);
      }

      @Override
      public void run() {
        wrapped.materializeElement(index, this);
      }

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        final ListAsyncMaterializer<E> wrapped = ImmaterialState.this.wrapped;
        return (int) Math.min(Integer.MAX_VALUE,
            (long) wrapped.weightElement() + wrapped.weightContains());
      }
    }

    private class MaterializingElementAsyncConsumer extends
        CancellableIndexedAsyncConsumer<E> implements Task {

      private final IndexedAsyncConsumer<E> consumer;
      private final int index;

      private String taskID;

      private MaterializingElementAsyncConsumer(final int index,
          @NotNull final IndexedAsyncConsumer<E> consumer) {
        this.index = index;
        this.consumer = consumer;
      }

      @Override
      public void cancellableAccept(final int size, final int index, final E element)
          throws Exception {
        if (testedIndex < index) {
          try {
            if (predicate.test(index, element)) {
              final ListAsyncMaterializer<E> state = setState(
                  new FlatMapAfterListAsyncMaterializer<E>(wrapped, index, mapper, status, context,
                      cancelException, decorateFunction));
              consumeState(state);
              state.materializeElement(this.index, consumer);
              return;
            }
          } catch (final Exception e) {
            setError(e);
            throw e;
          }
          testedIndex = index;
        }
        if (this.index == index) {
          consumer.accept(-1, index, element);
        } else {
          taskID = getTaskID();
          context.scheduleAfter(this);
        }
      }

      @Override
      public void cancellableComplete(final int size) throws Exception {
        consumeState(setState(wrapped));
        consumer.complete(size);
      }

      @Override
      public void error(@NotNull final Exception error) throws Exception {
        consumer.error(error);
      }

      @Override
      public void run() {
        wrapped.materializeElement(testedIndex + 1, this);
      }

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        final long weight = wrapped.weightElement();
        return (int) Math.min(Integer.MAX_VALUE, weight << 1);
      }
    }

    private class MaterializingHasElementAsyncConsumer extends
        CancellableIndexedAsyncConsumer<E> implements Task {

      private final AsyncConsumer<Boolean> consumer;
      private final int index;

      private String taskID;

      private MaterializingHasElementAsyncConsumer(final int index,
          @NotNull final AsyncConsumer<Boolean> consumer) {
        this.index = index;
        this.consumer = consumer;
      }

      @Override
      public void cancellableAccept(final int size, final int index, final E element)
          throws Exception {
        if (testedIndex < index) {
          try {
            if (predicate.test(index, element)) {
              final ListAsyncMaterializer<E> state = setState(
                  new FlatMapAfterListAsyncMaterializer<E>(wrapped, index, mapper, status, context,
                      cancelException, decorateFunction));
              consumeState(state);
              state.materializeHasElement(this.index, consumer);
              return;
            }
          } catch (final Exception e) {
            setError(e);
            throw e;
          }
          testedIndex = index;
        }
        if (this.index == index) {
          consumer.accept(true);
        } else {
          taskID = getTaskID();
          context.scheduleAfter(this);
        }
      }

      @Override
      public void cancellableComplete(final int size) {
        consumeState(setState(wrapped));
      }

      @Override
      public void error(@NotNull final Exception error) throws Exception {
        consumer.error(error);
      }

      @Override
      public void run() {
        wrapped.materializeElement(testedIndex + 1, this);
      }

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        final ListAsyncMaterializer<E> wrapped = ImmaterialState.this.wrapped;
        return (int) Math.min(Integer.MAX_VALUE,
            (long) wrapped.weightElement() + wrapped.weightHasElement());
      }
    }
  }
}
