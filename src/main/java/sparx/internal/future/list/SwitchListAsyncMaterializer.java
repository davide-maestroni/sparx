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

import static sparx.internal.future.AsyncConsumers.safeConsumeError;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ContextTask;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.AsyncConsumer;
import sparx.internal.future.ContextAsyncConsumer;
import sparx.internal.future.ContextIndexedAsyncConsumer;
import sparx.internal.future.IndexedAsyncConsumer;
import sparx.internal.future.IndexedAsyncPredicate;

public class SwitchListAsyncMaterializer<E> implements ListAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      SwitchListAsyncMaterializer.class.getName());

  private final ExecutionContext fromContext;
  private final String fromTaskID;
  private final ExecutionContext toContext;
  private final ListAsyncMaterializer<E> wrapped;

  public SwitchListAsyncMaterializer(@NotNull final ExecutionContext fromContext,
      @NotNull final String fromTaskID, @NotNull final ExecutionContext toContext,
      @NotNull final ListAsyncMaterializer<E> wrapped) {
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
  public void materializeContains(final Object element,
      @NotNull final AsyncConsumer<Boolean> consumer) {
    final ContextAsyncConsumer<Boolean> switchConsumer = new ContextAsyncConsumer<Boolean>(
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
          wrapped.materializeContains(element, switchConsumer);
        } catch (final Exception e) {
          safeConsumeError(switchConsumer, e, LOGGER);
        }
      }
    });
  }

  @Override
  public void materializeElement(final int index, @NotNull final IndexedAsyncConsumer<E> consumer) {
    final ContextIndexedAsyncConsumer<E> switchConsumer = new ContextIndexedAsyncConsumer<E>(
        toContext, getTaskID(), consumer, LOGGER);
    fromContext.scheduleAfter(new ContextTask(fromContext) {
      @Override
      public @NotNull String taskID() {
        return fromTaskID;
      }

      @Override
      public int weight() {
        return wrapped.weightElement();
      }

      @Override
      protected void runWithContext() {
        try {
          wrapped.materializeElement(index, switchConsumer);
        } catch (final Exception e) {
          safeConsumeError(switchConsumer, e, LOGGER);
        }
      }
    });
  }

  @Override
  public void materializeElements(@NotNull final AsyncConsumer<List<E>> consumer) {
    final ContextAsyncConsumer<List<E>> switchConsumer = new ContextAsyncConsumer<List<E>>(
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
  public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
    final ContextAsyncConsumer<Boolean> switchConsumer = new ContextAsyncConsumer<Boolean>(
        toContext, getTaskID(), consumer, LOGGER);
    fromContext.scheduleAfter(new ContextTask(fromContext) {
      @Override
      public @NotNull String taskID() {
        return fromTaskID;
      }

      @Override
      public int weight() {
        return wrapped.weightEmpty();
      }

      @Override
      protected void runWithContext() {
        try {
          wrapped.materializeEmpty(switchConsumer);
        } catch (final Exception e) {
          safeConsumeError(switchConsumer, e, LOGGER);
        }
      }
    });
  }

  @Override
  public void materializeHasElement(final int index,
      @NotNull final AsyncConsumer<Boolean> consumer) {
    final ContextAsyncConsumer<Boolean> switchConsumer = new ContextAsyncConsumer<Boolean>(
        toContext, getTaskID(), consumer, LOGGER);
    fromContext.scheduleAfter(new ContextTask(fromContext) {
      @Override
      public @NotNull String taskID() {
        return fromTaskID;
      }

      @Override
      public int weight() {
        return wrapped.weightHasElement();
      }

      @Override
      protected void runWithContext() {
        try {
          wrapped.materializeHasElement(index, switchConsumer);
        } catch (final Exception e) {
          safeConsumeError(switchConsumer, e, LOGGER);
        }
      }
    });
  }

  @Override
  public void materializeNextWhile(final int index,
      @NotNull final IndexedAsyncPredicate<E> predicate) {
    final NextIndexedAsyncConsumer nextConsumer = new NextIndexedAsyncConsumer(toContext,
        getTaskID(), predicate, LOGGER);
    final ContextIndexedAsyncConsumer<E> switchConsumer = nextConsumer.switchConsumer();
    fromContext.scheduleAfter(new ContextTask(fromContext) {
      @Override
      public @NotNull String taskID() {
        return fromTaskID;
      }

      @Override
      public int weight() {
        return wrapped.weightElement();
      }

      @Override
      protected void runWithContext() {
        try {
          wrapped.materializeElement(index, switchConsumer);
        } catch (final Exception e) {
          safeConsumeError(switchConsumer, e, LOGGER);
        }
      }
    });
  }

  @Override
  public void materializePrevWhile(final int index,
      @NotNull final IndexedAsyncPredicate<E> predicate) {
    final PrevIndexedAsyncConsumer nextConsumer = new PrevIndexedAsyncConsumer(toContext,
        getTaskID(), predicate, LOGGER);
    final ContextIndexedAsyncConsumer<E> switchConsumer = nextConsumer.switchConsumer();
    fromContext.scheduleAfter(new ContextTask(fromContext) {
      @Override
      public @NotNull String taskID() {
        return fromTaskID;
      }

      @Override
      public int weight() {
        return wrapped.weightElement();
      }

      @Override
      protected void runWithContext() {
        try {
          wrapped.materializeElement(index, switchConsumer);
        } catch (final Exception e) {
          safeConsumeError(switchConsumer, e, LOGGER);
        }
      }
    });
  }

  @Override
  public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
    final ContextAsyncConsumer<Integer> switchConsumer = new ContextAsyncConsumer<Integer>(
        toContext, getTaskID(), consumer, LOGGER);
    fromContext.scheduleAfter(new ContextTask(fromContext) {
      @Override
      public @NotNull String taskID() {
        return fromTaskID;
      }

      @Override
      public int weight() {
        return wrapped.weightSize();
      }

      @Override
      protected void runWithContext() {
        try {
          wrapped.materializeSize(switchConsumer);
        } catch (final Exception e) {
          safeConsumeError(switchConsumer, e, LOGGER);
        }
      }
    });
  }

  @Override
  public int weightContains() {
    return 1;
  }

  @Override
  public int weightElement() {
    return 1;
  }

  @Override
  public int weightElements() {
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
    return 1;
  }

  @Override
  public int weightPrevWhile() {
    return 1;
  }

  @Override
  public int weightSize() {
    return 1;
  }

  private @NotNull String getTaskID() {
    final String taskID = toContext.currentTaskID();
    return taskID != null ? taskID : "";
  }

  private class NextIndexedAsyncConsumer implements IndexedAsyncConsumer<E> {

    private final ContextIndexedAsyncConsumer<E> switchConsumer;
    private final IndexedAsyncPredicate<E> predicate;

    public NextIndexedAsyncConsumer(@NotNull final ExecutionContext context,
        @NotNull final String taskID, @NotNull final IndexedAsyncPredicate<E> predicate,
        @NotNull final Logger logger) {
      this.predicate = predicate;
      switchConsumer = new ContextIndexedAsyncConsumer<E>(context, taskID, this, logger);
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
            return wrapped.weightElement();
          }

          @Override
          protected void runWithContext() {
            try {
              wrapped.materializeElement(index + 1, switchConsumer);
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

    private @NotNull ContextIndexedAsyncConsumer<E> switchConsumer() {
      return switchConsumer;
    }
  }

  private class PrevIndexedAsyncConsumer implements IndexedAsyncConsumer<E> {

    private final ContextIndexedAsyncConsumer<E> switchConsumer;
    private final IndexedAsyncPredicate<E> predicate;

    public PrevIndexedAsyncConsumer(@NotNull final ExecutionContext context,
        @NotNull final String taskID, @NotNull final IndexedAsyncPredicate<E> predicate,
        @NotNull final Logger logger) {
      this.predicate = predicate;
      switchConsumer = new ContextIndexedAsyncConsumer<E>(context, taskID, this, logger);
    }

    @Override
    public void accept(final int size, final int index, final E element) throws Exception {
      if (predicate.test(size, index, element)) {
        if (index == 0) {
          predicate.complete(size);
        } else {
          fromContext.scheduleAfter(new ContextTask(fromContext) {
            @Override
            public @NotNull String taskID() {
              return fromTaskID;
            }

            @Override
            public int weight() {
              return wrapped.weightElement();
            }

            @Override
            protected void runWithContext() {
              try {
                wrapped.materializeElement(index - 1, switchConsumer);
              } catch (final Exception e) {
                safeConsumeError(switchConsumer, e, LOGGER);
              }
            }
          });
        }
      }
    }

    @Override
    public void complete(final int size) {
      fromContext.scheduleAfter(new ContextTask(fromContext) {
        @Override
        public @NotNull String taskID() {
          return fromTaskID;
        }

        @Override
        public int weight() {
          return wrapped.weightElement();
        }

        @Override
        protected void runWithContext() {
          try {
            wrapped.materializeElement(size - 1, switchConsumer);
          } catch (final Exception e) {
            safeConsumeError(switchConsumer, e, LOGGER);
          }
        }
      });
    }

    @Override
    public void error(@NotNull final Exception error) throws Exception {
      predicate.error(error);
    }

    private @NotNull ContextIndexedAsyncConsumer<E> switchConsumer() {
      return switchConsumer;
    }
  }
}
