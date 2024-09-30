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
package sparx.internal.future.iterator;

import static sparx.internal.future.FutureConsumers.safeConsume;
import static sparx.internal.future.FutureConsumers.safeConsumeError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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
import sparx.internal.future.list.ListFutureMaterializer;
import sparx.util.DequeueList;
import sparx.util.annotation.Positive;

public class EndsWithIteratorFutureMaterializer<E> extends
    AbstractIteratorFutureMaterializer<Boolean> {

  private static final Logger LOGGER = Logger.getLogger(
      EndsWithIteratorFutureMaterializer.class.getName());

  public EndsWithIteratorFutureMaterializer(@NotNull final IteratorFutureMaterializer<E> wrapped,
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

  private class ImmaterialState implements IteratorFutureMaterializer<Boolean> {

    private final AtomicReference<CancellationException> cancelException;
    private final ArrayList<FutureConsumer<List<Boolean>>> elementsConsumers = new ArrayList<FutureConsumer<List<Boolean>>>(
        2);
    private final ListFutureMaterializer<Object> elementsMaterializer;
    private final IteratorFutureMaterializer<E> wrapped;

    public ImmaterialState(@NotNull final IteratorFutureMaterializer<E> wrapped,
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
      return true;
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
      setCancelled(exception);
      consumeError(exception);
    }

    @Override
    public void materializeElements(@NotNull final FutureConsumer<List<Boolean>> consumer) {
      final ArrayList<FutureConsumer<List<Boolean>>> elementsConsumers = this.elementsConsumers;
      elementsConsumers.add(consumer);
      if (elementsConsumers.size() == 1) {
        elementsMaterializer.materializeSize(new CancellableFutureConsumer<Integer>() {
          @Override
          public void cancellableAccept(final Integer size) {
            if (size == 0) {
              setDone(new ElementToIteratorFutureMaterializer<Boolean>(true));
              consumeElements(Collections.singletonList(true));
            } else {
              new MaterializingFutureConsumer(size).schedule();
            }
          }

          @Override
          public void error(@NotNull final Exception error) {
            setError(error);
          }
        });
      }
    }

    @Override
    public void materializeHasNext(@NotNull final FutureConsumer<Boolean> consumer) {
      materializeElements(new FutureConsumer<List<Boolean>>() {
        @Override
        public void accept(final List<Boolean> elements) {
          getState().materializeHasNext(consumer);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeIterator(@NotNull final FutureConsumer<Iterator<Boolean>> consumer) {
      materializeElements(new FutureConsumer<List<Boolean>>() {
        @Override
        public void accept(final List<Boolean> elements) {
          getState().materializeIterator(consumer);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeNext(@NotNull final IndexedFutureConsumer<Boolean> consumer) {
      materializeElements(new FutureConsumer<List<Boolean>>() {
        @Override
        public void accept(final List<Boolean> elements) {
          getState().materializeNext(consumer);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeNextWhile(@NotNull final IndexedFuturePredicate<Boolean> predicate) {
      materializeElements(new FutureConsumer<List<Boolean>>() {
        @Override
        public void accept(final List<Boolean> elements) {
          getState().materializeNextWhile(predicate);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          predicate.error(error);
        }
      });
    }

    @Override
    public void materializeSkip(@Positive final int count,
        @NotNull final FutureConsumer<Integer> consumer) {
      if (elementsConsumers.isEmpty()) {
        setDone(EmptyIteratorFutureMaterializer.<Boolean>instance());
        consumeElements(Collections.<Boolean>emptyList());
        safeConsume(consumer, 1, LOGGER);
      } else {
        materializeElements(new FutureConsumer<List<Boolean>>() {
          @Override
          public void accept(final List<Boolean> elements) {
            getState().materializeSkip(count, consumer);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
    }

    @Override
    public int weightElements() {
      return elementsConsumers.isEmpty() ? wrapped.weightElements() : 1;
    }

    @Override
    public int weightHasNext() {
      return weightElements();
    }

    @Override
    public int weightNext() {
      return weightElements();
    }

    @Override
    public int weightNextWhile() {
      return weightElements();
    }

    @Override
    public int weightSkip() {
      return weightElements();
    }

    private void consumeElements(@NotNull final List<Boolean> elements) {
      final ArrayList<FutureConsumer<List<Boolean>>> elementsConsumers = this.elementsConsumers;
      for (final FutureConsumer<List<Boolean>> elementsConsumer : elementsConsumers) {
        safeConsume(elementsConsumer, elements, LOGGER);
      }
      elementsConsumers.clear();
    }

    private void consumeError(@NotNull final Exception error) {
      final ArrayList<FutureConsumer<List<Boolean>>> elementsConsumers = this.elementsConsumers;
      for (final FutureConsumer<List<Boolean>> elementsConsumer : elementsConsumers) {
        safeConsumeError(elementsConsumer, error, LOGGER);
      }
      elementsConsumers.clear();
    }

    private @NotNull String getTaskID() {
      final String taskID = context.currentTaskID();
      return taskID != null ? taskID : "";
    }

    private void setError(@NotNull final Exception error) {
      final CancellationException exception = cancelException.get();
      if (exception != null) {
        setCancelled(exception);
        consumeError(exception);
      } else {
        setFailed(error);
        consumeError(error);
      }
    }

    private class MaterializingFutureConsumer extends CancellableIndexedFuturePredicate<E> {

      private final DequeueList<Object> elements = new DequeueList<Object>();
      private final int elementsSize;

      private String taskID;

      private MaterializingFutureConsumer(final int elementsSize) {
        this.elementsSize = elementsSize;
      }

      @Override
      public void cancellableComplete(final int size) {
        if (elements.size() < elementsSize) {
          setDone(new ElementToIteratorFutureMaterializer<Boolean>(false));
          consumeElements(Collections.singletonList(false));
        } else {
          elementsMaterializer.materializeNextWhile(0,
              new CancellableIndexedFuturePredicate<Object>() {
                @Override
                public void cancellableComplete(final int size) {
                  setDone(new ElementToIteratorFutureMaterializer<Boolean>(true));
                  consumeElements(Collections.singletonList(true));
                }

                @Override
                public boolean cancellableTest(final int size, final int index,
                    final Object element) {
                  if (element == null ? null != elements.get(index)
                      : !element.equals(elements.get(index))) {
                    setDone(new ElementToIteratorFutureMaterializer<Boolean>(false));
                    consumeElements(Collections.singletonList(false));
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
      public boolean cancellableTest(final int size, final int index, final E element) {
        elements.add(element);
        if (elements.size() > elementsSize) {
          elements.removeFirst();
        }
        return true;
      }

      @Override
      public void error(@NotNull final Exception error) {
        setError(error);
      }

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        return (int) Math.min(Integer.MAX_VALUE,
            (long) wrapped.weightNextWhile() + elementsMaterializer.weightNextWhile());
      }

      @Override
      protected void runWithContext() {
        wrapped.materializeNextWhile(this);
      }

      private void schedule() {
        taskID = getTaskID();
        context.scheduleAfter(this);
      }
    }
  }
}
