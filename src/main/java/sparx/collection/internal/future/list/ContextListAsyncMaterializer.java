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

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.collection.internal.future.AsyncConsumer;
import sparx.collection.internal.future.IndexedAsyncConsumer;
import sparx.concurrent.ExecutionContext;
import sparx.concurrent.ExecutionContext.Task;
import sparx.util.Require;

public class ContextListAsyncMaterializer<E> extends AbstractListAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      ContextListAsyncMaterializer.class.getName());

  private static final int STATUS_CANCELLED = 2;
  private static final int STATUS_DONE = 1;
  private static final int STATUS_RUNNING = 0;

  private final ExecutionContext executionContext;
  private final String taskID = toString();
  private final AtomicInteger status = new AtomicInteger(STATUS_RUNNING);

  private ListAsyncMaterializer<E> wrapped;

  public ContextListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      @NotNull final ExecutionContext executionContext) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
    this.executionContext = Require.notNull(executionContext, "executionContext");
  }

  @Override
  public boolean cancel(final boolean mayInterruptIfRunning) {
    if (status.compareAndSet(STATUS_RUNNING, STATUS_CANCELLED)) {
      if (mayInterruptIfRunning) {
        executionContext.interruptTask(taskID);
      }
      executionContext.scheduleBefore(new Task() {
        @Override
        public @NotNull String taskID() {
          return taskID;
        }

        @Override
        public int weight() {
          return 1;
        }

        @Override
        public void run() {
          wrapped.cancel(mayInterruptIfRunning);
          wrapped = new CancelledListAsyncMaterializer<E>(wrapped.knownSize());
        }
      });
      return true;
    }
    return false;
  }

  @Override
  public int knownSize() {
    return wrapped.knownSize();
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
  public void materializeContains(final Object element,
      @NotNull final AsyncConsumer<Boolean> consumer) {
    executionContext.scheduleAfter(new Task() {
      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        return 1;
      }

      @Override
      public void run() {
        wrapped.materializeContains(element, consumer);
      }
    });
  }

  @Override
  public void materializeElement(final int index, @NotNull final IndexedAsyncConsumer<E> consumer) {
    executionContext.scheduleAfter(new Task() {
      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        return 1;
      }

      @Override
      public void run() {
        wrapped.materializeElement(index, consumer);
      }
    });
  }

  @Override
  public void materializeElements(@NotNull final AsyncConsumer<List<E>> consumer) {
    executionContext.scheduleAfter(new Task() {
      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        return wrapped.knownSize();
      }

      @Override
      public void run() {
        if (isCancelled()) {
          safeConsumeError(consumer, new CancellationException(), LOGGER);
        } else {
          wrapped.materializeElements(new AsyncConsumer<List<E>>() {
            @Override
            public void accept(final List<E> param) throws Exception {
              if (status.compareAndSet(STATUS_RUNNING, STATUS_DONE)) {
                consumer.accept(param);
              }
            }

            @Override
            public void error(@NotNull final Exception error) throws Exception {
              consumer.error(error);
            }
          });
        }
      }
    });
  }

  @Override
  public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
    executionContext.scheduleAfter(new Task() {
      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        return 1;
      }

      @Override
      public void run() {
        wrapped.materializeEmpty(consumer);
      }
    });
  }

  @Override
  public void materializeOrdered(@NotNull final IndexedAsyncConsumer<E> consumer) {
    executionContext.scheduleAfter(new Task() {
      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        return wrapped.knownSize();
      }

      @Override
      public void run() {
        wrapped.materializeOrdered(consumer);
      }
    });
  }

  @Override
  public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
    executionContext.scheduleAfter(new Task() {
      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        return 1;
      }

      @Override
      public void run() {
        wrapped.materializeSize(consumer);
      }
    });
  }

  @Override
  public void materializeUnordered(@NotNull final IndexedAsyncConsumer<E> consumer) {
    executionContext.scheduleAfter(new Task() {
      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        return wrapped.knownSize();
      }

      @Override
      public void run() {
        wrapped.materializeUnordered(consumer);
      }
    });
  }
}
