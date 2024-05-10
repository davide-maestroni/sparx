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

import java.util.concurrent.CancellationException;
import org.jetbrains.annotations.NotNull;
import sparx.collection.internal.future.AsyncConsumer;
import sparx.collection.internal.future.IndexedAsyncConsumer;
import sparx.concurrent.ExecutionContext;
import sparx.concurrent.ExecutionContext.Task;
import sparx.util.Require;

public class ContextListAsyncMaterializer<E> implements ListAsyncMaterializer<E> {

  private final ExecutionContext executionContext;
  private final String taskID = toString();

  private ListAsyncMaterializer<E> materializer;

  public ContextListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> materializer,
      @NotNull final ExecutionContext executionContext) {
    this.materializer = Require.notNull(materializer, "materializer");
    this.executionContext = Require.notNull(executionContext, "executionContext");
  }

  @Override
  public boolean cancel(final boolean mayInterruptIfRunning) {
    boolean interrupted = false;
    if (mayInterruptIfRunning) {
      interrupted = executionContext.interruptTask(taskID);
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
        materializer.cancel(mayInterruptIfRunning);
        materializer = new FailedListAsyncMaterializer<E>(materializer.knownSize(), -1,
            new CancellationException());
      }
    });
    return interrupted;
  }

  @Override
  public int knownSize() {
    return materializer.knownSize();
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
        materializer.materializeContains(element, consumer);
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
        materializer.materializeElement(index, consumer);
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
        materializer.materializeEmpty(consumer);
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
        return Math.max(1, materializer.knownSize());
      }

      @Override
      public void run() {
        materializer.materializeOrdered(consumer);
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
        materializer.materializeSize(consumer);
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
        return Math.max(1, materializer.knownSize());
      }

      @Override
      public void run() {
        materializer.materializeUnordered(consumer);
      }
    });
  }
}
