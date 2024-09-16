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
import static sparx.internal.future.AsyncConsumers.safeConsumeComplete;
import static sparx.internal.future.AsyncConsumers.safeConsumeError;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.concurrent.ExecutionContext.Task;
import sparx.internal.future.AsyncConsumer;
import sparx.internal.future.IndexedAsyncConsumer;
import sparx.internal.future.IndexedAsyncPredicate;
import sparx.util.annotation.NotNegative;

public class ListToListAsyncMaterializer<E> implements ListAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      ListToListAsyncMaterializer.class.getName());

  private final ExecutionContext context;
  private final List<E> elements;

  public ListToListAsyncMaterializer(@NotNull final List<E> elements,
      @NotNull final ExecutionContext context) {
    this.elements = elements;
    this.context = context;
  }

  @Override
  public boolean isCancelled() {
    return false;
  }

  @Override
  public boolean isDone() {
    return true;
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
    return true;
  }

  @Override
  public int knownSize() {
    return elements.size();
  }

  @Override
  public void materializeCancel(@NotNull final CancellationException exception) {
  }

  @Override
  @SuppressWarnings("SuspiciousMethodCalls")
  public void materializeContains(final Object element,
      @NotNull final AsyncConsumer<Boolean> consumer) {
    safeConsume(consumer, elements.contains(element), LOGGER);
  }

  @Override
  public void materializeElement(final int index, @NotNull final IndexedAsyncConsumer<E> consumer) {
    final List<E> elements = this.elements;
    if (index < 0) {
      safeConsumeError(consumer, new IndexOutOfBoundsException(Integer.toString(index)), LOGGER);
    } else {
      final int size = elements.size();
      if (index >= size) {
        safeConsumeComplete(consumer, size, LOGGER);
      } else {
        safeConsume(consumer, size, index, elements.get(index), LOGGER);
      }
    }
  }

  @Override
  public void materializeElements(@NotNull final AsyncConsumer<List<E>> consumer) {
    safeConsume(consumer, elements, LOGGER);
  }

  @Override
  public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
    safeConsume(consumer, elements.isEmpty(), LOGGER);
  }

  @Override
  public void materializeHasElement(final int index,
      @NotNull final AsyncConsumer<Boolean> consumer) {
    safeConsume(consumer, index >= 0 && index < elements.size(), LOGGER);
  }

  @Override
  public void materializeNextWhile(@NotNegative final int index,
      @NotNull final IndexedAsyncPredicate<E> predicate) {
    final int throughput = context.minThroughput();
    if (throughput < Integer.MAX_VALUE) {
      new NextTask(predicate, index, throughput).run();
    } else {
      final List<E> elements = this.elements;
      final int size = elements.size();
      for (int i = index; i < size; ++i) {
        if (!safeConsume(predicate, size, i, elements.get(i), LOGGER)) {
          return;
        }
      }
      safeConsumeComplete(predicate, size, LOGGER);
    }
  }

  @Override
  public void materializePrevWhile(@NotNegative final int index,
      @NotNull final IndexedAsyncPredicate<E> predicate) {
    final List<E> elements = this.elements;
    final int size = elements.size();
    final int throughput = context.minThroughput();
    if (throughput < Integer.MAX_VALUE) {
      new PrevTask(predicate, Math.min(index, size - 1), throughput).run();
    } else {
      for (int i = Math.min(index, size - 1); i >= 0; --i) {
        final E element = elements.get(i);
        if (!safeConsume(predicate, size, i, element, LOGGER)) {
          return;
        }
      }
      safeConsumeComplete(predicate, size, LOGGER);
    }
  }

  @Override
  public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
    safeConsume(consumer, elements.size(), LOGGER);
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
    return Math.min(context.minThroughput(), elements.size());
  }

  @Override
  public int weightPrevWhile() {
    return Math.min(context.minThroughput(), elements.size());
  }

  @Override
  public int weightSize() {
    return 1;
  }

  private @NotNull String getTaskID() {
    final String taskID = context.currentTaskID();
    return taskID != null ? taskID : "";
  }

  private class NextTask implements Task {

    private final IndexedAsyncPredicate<E> predicate;
    private final int throughput;

    private int index;
    private String taskID;

    private NextTask(@NotNull final IndexedAsyncPredicate<E> predicate, final int index,
        final int throughput) {
      this.predicate = predicate;
      this.index = index;
      this.throughput = throughput;
    }

    @Override
    public void run() {
      final int throughput = this.throughput;
      final IndexedAsyncPredicate<E> predicate = this.predicate;
      final List<E> elements = ListToListAsyncMaterializer.this.elements;
      final int size = elements.size();
      int i = index;
      for (int n = 0; n < throughput && i < size; ++n, ++i) {
        if (!safeConsume(predicate, size, i, elements.get(i), LOGGER)) {
          return;
        }
      }
      if (i == size) {
        safeConsumeComplete(predicate, size, LOGGER);
      } else {
        index = i;
        taskID = getTaskID();
        context.scheduleAfter(this);
      }
    }

    @Override
    public @NotNull
    final String taskID() {
      return taskID;
    }

    @Override
    public int weight() {
      return Math.min(throughput, elements.size() - index);
    }
  }

  private class PrevTask implements Task {

    private final IndexedAsyncPredicate<E> predicate;
    private final int throughput;

    private int index;
    private String taskID;

    private PrevTask(@NotNull final IndexedAsyncPredicate<E> predicate, final int index,
        final int throughput) {
      this.predicate = predicate;
      this.index = index;
      this.throughput = throughput;
    }

    @Override
    public void run() {
      final int throughput = this.throughput;
      final IndexedAsyncPredicate<E> predicate = this.predicate;
      final List<E> elements = ListToListAsyncMaterializer.this.elements;
      final int size = elements.size();
      int i = index;
      for (int n = 0; n < throughput && i >= 0; ++n, --i) {
        if (!safeConsume(predicate, size, i, elements.get(i), LOGGER)) {
          return;
        }
      }
      if (i < 0) {
        safeConsumeComplete(predicate, size, LOGGER);
      } else {
        index = i;
        taskID = getTaskID();
        context.scheduleAfter(this);
      }
    }

    @Override
    public @NotNull
    final String taskID() {
      return taskID;
    }

    @Override
    public int weight() {
      return Math.min(throughput, index + 1);
    }
  }
}
