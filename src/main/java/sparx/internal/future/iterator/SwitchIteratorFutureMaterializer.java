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

import static sparx.internal.future.FutureConsumers.safeConsumeError;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ContextTask;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.ContextFutureConsumer;
import sparx.internal.future.ContextIndexedFutureConsumer;
import sparx.internal.future.FutureConsumer;
import sparx.internal.future.IndexedFutureConsumer;
import sparx.internal.future.IndexedFuturePredicate;

public class SwitchIteratorFutureMaterializer<E> implements IteratorFutureMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      SwitchIteratorFutureMaterializer.class.getName());

  private final ExecutionContext fromContext;
  private final String fromTaskID;
  private final ExecutionContext toContext;
  private final IteratorFutureMaterializer<E> wrapped;

  public SwitchIteratorFutureMaterializer(@NotNull final ExecutionContext fromContext,
      @NotNull final String fromTaskID, @NotNull final ExecutionContext toContext,
      @NotNull final IteratorFutureMaterializer<E> wrapped) {
    this.fromContext = fromContext;
    this.fromTaskID = fromTaskID;
    this.toContext = toContext;
    this.wrapped = wrapped;
  }

  @Override
  public boolean isCancelled() {
    return wrapped.isCancelled();
  }

  @Override
  public boolean isDone() {
    return wrapped.isDone();
  }

  @Override
  public boolean isFailed() {
    return wrapped.isFailed();
  }

  @Override
  public boolean isMaterializedAtOnce() {
    return wrapped.isMaterializedAtOnce();
  }

  @Override
  public boolean isSucceeded() {
    return wrapped.isSucceeded();
  }

  @Override
  public int knownSize() {
    return wrapped.knownSize();
  }

  @Override
  public void materializeCancel(@NotNull final CancellationException exception) {
    fromContext.scheduleBefore(new ContextTask(fromContext) {
      @Override
      public @NotNull String taskID() {
        return fromTaskID;
      }

      @Override
      protected void runWithContext() {
        try {
          wrapped.materializeCancel(exception);
        } catch (final Exception e) {
          LOGGER.log(Level.SEVERE, "Ignored exception", e);
        }
      }
    });
  }

  @Override
  public void materializeElements(@NotNull final FutureConsumer<List<E>> consumer) {
    final ContextFutureConsumer<List<E>> switchConsumer = new ContextFutureConsumer<List<E>>(
        toContext, getTaskID(), consumer, LOGGER);
    fromContext.scheduleAfter(new ContextTask(fromContext) {
      @Override
      public @NotNull String taskID() {
        return fromTaskID;
      }

      @Override
      public int weight() {
        return wrapped.weightElements();
      }

      @Override
      protected void runWithContext() {
        try {
          wrapped.materializeElements(switchConsumer);
        } catch (final Exception e) {
          safeConsumeError(switchConsumer, e, LOGGER);
        }
      }
    });
  }

  @Override
  public void materializeHasNext(@NotNull final FutureConsumer<Boolean> consumer) {
    final ContextFutureConsumer<Boolean> switchConsumer = new ContextFutureConsumer<Boolean>(
        toContext, getTaskID(), consumer, LOGGER);
    fromContext.scheduleAfter(new ContextTask(fromContext) {
      @Override
      public @NotNull String taskID() {
        return fromTaskID;
      }

      @Override
      public int weight() {
        return wrapped.weightElements();
      }

      @Override
      protected void runWithContext() {
        try {
          wrapped.materializeHasNext(switchConsumer);
        } catch (final Exception e) {
          safeConsumeError(switchConsumer, e, LOGGER);
        }
      }
    });
  }

  @Override
  public void materializeIterator(@NotNull final FutureConsumer<Iterator<E>> consumer) {
    final ContextFutureConsumer<Iterator<E>> switchConsumer = new ContextFutureConsumer<Iterator<E>>(
        toContext, getTaskID(), consumer, LOGGER);
    fromContext.scheduleAfter(new ContextTask(fromContext) {
      @Override
      public @NotNull String taskID() {
        return fromTaskID;
      }

      @Override
      public int weight() {
        return wrapped.weightElements();
      }

      @Override
      protected void runWithContext() {
        try {
          wrapped.materializeIterator(switchConsumer);
        } catch (final Exception e) {
          safeConsumeError(switchConsumer, e, LOGGER);
        }
      }
    });
  }

  @Override
  public void materializeNext(@NotNull final IndexedFutureConsumer<E> consumer) {
    final ContextIndexedFutureConsumer<E> switchConsumer = new ContextIndexedFutureConsumer<E>(
        toContext, getTaskID(), consumer, LOGGER);
    fromContext.scheduleAfter(new ContextTask(fromContext) {
      @Override
      public @NotNull String taskID() {
        return fromTaskID;
      }

      @Override
      public int weight() {
        return wrapped.weightElements();
      }

      @Override
      protected void runWithContext() {
        try {
          wrapped.materializeNext(switchConsumer);
        } catch (final Exception e) {
          safeConsumeError(switchConsumer, e, LOGGER);
        }
      }
    });
  }

  @Override
  public void materializeNextWhile(@NotNull final IndexedFuturePredicate<E> predicate) {
    final NextIndexedFutureConsumer nextConsumer = new NextIndexedFutureConsumer(toContext,
        getTaskID(), predicate, LOGGER);
    final ContextIndexedFutureConsumer<E> switchConsumer = nextConsumer.switchConsumer();
    fromContext.scheduleAfter(new ContextTask(fromContext) {
      @Override
      public @NotNull String taskID() {
        return fromTaskID;
      }

      @Override
      public int weight() {
        return wrapped.weightNext();
      }

      @Override
      protected void runWithContext() {
        try {
          wrapped.materializeNext(switchConsumer);
        } catch (final Exception e) {
          safeConsumeError(switchConsumer, e, LOGGER);
        }
      }
    });
  }

  @Override
  public void materializeSkip(final int count, @NotNull final FutureConsumer<Integer> consumer) {
    final ContextFutureConsumer<Integer> switchConsumer = new ContextFutureConsumer<Integer>(
        toContext, getTaskID(), consumer, LOGGER);
    fromContext.scheduleAfter(new ContextTask(fromContext) {
      @Override
      public @NotNull String taskID() {
        return fromTaskID;
      }

      @Override
      public int weight() {
        return wrapped.weightElements();
      }

      @Override
      protected void runWithContext() {
        try {
          wrapped.materializeSkip(count, switchConsumer);
        } catch (final Exception e) {
          safeConsumeError(switchConsumer, e, LOGGER);
        }
      }
    });
  }

  @Override
  public int weightElements() {
    return 1;
  }

  @Override
  public int weightHasNext() {
    return 1;
  }

  @Override
  public int weightNext() {
    return 1;
  }

  @Override
  public int weightNextWhile() {
    return 1;
  }

  @Override
  public int weightSkip() {
    return 1;
  }

  private @NotNull String getTaskID() {
    final String taskID = toContext.currentTaskID();
    return taskID != null ? taskID : "";
  }

  private class NextIndexedFutureConsumer implements IndexedFutureConsumer<E> {

    private final ContextIndexedFutureConsumer<E> switchConsumer;
    private final IndexedFuturePredicate<E> predicate;

    public NextIndexedFutureConsumer(@NotNull final ExecutionContext context,
        @NotNull final String taskID, @NotNull final IndexedFuturePredicate<E> predicate,
        @NotNull final Logger logger) {
      this.predicate = predicate;
      switchConsumer = new ContextIndexedFutureConsumer<E>(context, taskID, this, logger);
    }

    @Override
    public void accept(final int size, final int index, final E element) throws Exception {
      if (predicate.test(size, index, element)) {
        fromContext.scheduleAfter(new ContextTask(fromContext) {
          @Override
          public @NotNull String taskID() {
            return fromTaskID;
          }

          @Override
          public int weight() {
            return wrapped.weightNext();
          }

          @Override
          protected void runWithContext() {
            try {
              wrapped.materializeNext(switchConsumer);
            } catch (final Exception e) {
              safeConsumeError(switchConsumer, e, LOGGER);
            }
          }
        });
      }
    }

    @Override
    public void complete(final int size) throws Exception {
      predicate.complete(size);
    }

    @Override
    public void error(@NotNull final Exception error) throws Exception {
      predicate.error(error);
    }

    private @NotNull ContextIndexedFutureConsumer<E> switchConsumer() {
      return switchConsumer;
    }
  }
}
