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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.concurrent.ExecutionContext.Task;
import sparx.internal.future.AsyncConsumer;
import sparx.internal.future.ContextAsyncConsumer;
import sparx.internal.future.ContextIndexedAsyncConsumer;
import sparx.internal.future.IndexedAsyncConsumer;

public class SwitchListAsyncMaterializer<E> implements ListAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      SwitchListAsyncMaterializer.class.getName());

  private final ExecutionContext fromContext;
  private final String fromTaskID;
  private final ExecutionContext toContext;
  private final String toTaskID;
  private final ListAsyncMaterializer<E> wrapped;

  public SwitchListAsyncMaterializer(@NotNull final ExecutionContext fromContext,
      @NotNull final String fromTaskID, @NotNull final ExecutionContext toContext,
      @NotNull final String toTaskID, @NotNull final ListAsyncMaterializer<E> wrapped) {
    this.fromContext = fromContext;
    this.fromTaskID = fromTaskID;
    this.toContext = toContext;
    this.toTaskID = toTaskID;
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
  public int knownSize() {
    return wrapped.knownSize();
  }

  @Override
  public void materializeCancel(final boolean mayInterruptIfRunning) {
    fromContext.scheduleAfter(new Task() {
      @Override
      public void run() {
        try {
          wrapped.materializeCancel(mayInterruptIfRunning);
        } catch (final Exception e) {
          LOGGER.log(Level.SEVERE, "Ignored exception", e);
        }
      }

      @Override
      public @NotNull String taskID() {
        return fromTaskID;
      }

      @Override
      public int weight() {
        return 1;
      }
    });
  }

  @Override
  public void materializeContains(final Object element,
      @NotNull final AsyncConsumer<Boolean> consumer) {
    final ContextAsyncConsumer<Boolean> switchConsumer = new ContextAsyncConsumer<Boolean>(
        toContext, toTaskID, consumer, LOGGER);
    fromContext.scheduleAfter(new Task() {
      @Override
      public void run() {
        try {
          wrapped.materializeContains(element, switchConsumer);
        } catch (final Exception e) {
          safeConsumeError(switchConsumer, e, LOGGER);
        }
      }

      @Override
      public @NotNull String taskID() {
        return fromTaskID;
      }

      @Override
      public int weight() {
        return 1;
      }
    });
  }

  @Override
  public void materializeEach(@NotNull final IndexedAsyncConsumer<E> consumer) {
    final ContextIndexedAsyncConsumer<E> switchConsumer = new ContextIndexedAsyncConsumer<E>(
        toContext, toTaskID, consumer, LOGGER);
    fromContext.scheduleAfter(new Task() {
      @Override
      public void run() {
        try {
          wrapped.materializeEach(switchConsumer);
        } catch (final Exception e) {
          safeConsumeError(switchConsumer, -1, e, LOGGER);
        }
      }

      @Override
      public @NotNull String taskID() {
        return fromTaskID;
      }

      @Override
      public int weight() {
        return 1;
      }
    });
  }

  @Override
  public void materializeElement(final int index, @NotNull final IndexedAsyncConsumer<E> consumer) {
    final ContextIndexedAsyncConsumer<E> switchConsumer = new ContextIndexedAsyncConsumer<E>(
        toContext, toTaskID, consumer, LOGGER);
    fromContext.scheduleAfter(new Task() {
      @Override
      public void run() {
        try {
          wrapped.materializeElement(index, switchConsumer);
        } catch (final Exception e) {
          safeConsumeError(switchConsumer, index, e, LOGGER);
        }
      }

      @Override
      public @NotNull String taskID() {
        return fromTaskID;
      }

      @Override
      public int weight() {
        return 1;
      }
    });
  }

  @Override
  public void materializeElements(@NotNull final AsyncConsumer<List<E>> consumer) {
    final ContextAsyncConsumer<List<E>> switchConsumer = new ContextAsyncConsumer<List<E>>(
        toContext, toTaskID, consumer, LOGGER);
    fromContext.scheduleAfter(new Task() {
      @Override
      public void run() {
        try {
          wrapped.materializeElements(switchConsumer);
        } catch (final Exception e) {
          safeConsumeError(switchConsumer, e, LOGGER);
        }
      }

      @Override
      public @NotNull String taskID() {
        return fromTaskID;
      }

      @Override
      public int weight() {
        return 1;
      }
    });
  }

  @Override
  public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
    final ContextAsyncConsumer<Boolean> switchConsumer = new ContextAsyncConsumer<Boolean>(
        toContext, toTaskID, consumer, LOGGER);
    fromContext.scheduleAfter(new Task() {
      @Override
      public void run() {
        try {
          wrapped.materializeEmpty(switchConsumer);
        } catch (final Exception e) {
          safeConsumeError(switchConsumer, e, LOGGER);
        }
      }

      @Override
      public @NotNull String taskID() {
        return fromTaskID;
      }

      @Override
      public int weight() {
        return 1;
      }
    });
  }

  @Override
  public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
    final ContextAsyncConsumer<Integer> switchConsumer = new ContextAsyncConsumer<Integer>(
        toContext, toTaskID, consumer, LOGGER);
    fromContext.scheduleAfter(new Task() {
      @Override
      public void run() {
        try {
          wrapped.materializeSize(switchConsumer);
        } catch (final Exception e) {
          safeConsumeError(switchConsumer, e, LOGGER);
        }
      }

      @Override
      public @NotNull String taskID() {
        return fromTaskID;
      }

      @Override
      public int weight() {
        return 1;
      }
    });
  }
}
