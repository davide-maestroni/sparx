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

import static sparx.internal.future.AsyncConsumers.safeConsume;
import static sparx.internal.future.AsyncConsumers.safeConsumeComplete;

import java.util.Iterator;
import java.util.concurrent.CancellationException;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.concurrent.ExecutionContext.Task;
import sparx.internal.future.AsyncConsumer;
import sparx.internal.future.IndexedAsyncConsumer;
import sparx.internal.future.IndexedAsyncPredicate;

public class IteratorToIteratorAsyncMaterializer<E> implements IteratorAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      IteratorToIteratorAsyncMaterializer.class.getName());

  private final ExecutionContext context;
  private final Iterator<E> elements;

  private int index;

  public IteratorToIteratorAsyncMaterializer(@NotNull final Iterator<E> elements,
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
  public int knownSize() {
    return -1;
  }

  @Override
  public void materializeCancel(@NotNull final CancellationException exception) {
  }

  @Override
  public void materializeHasNext(@NotNull final AsyncConsumer<Boolean> consumer) {
    safeConsume(consumer, elements.hasNext(), LOGGER);
  }

  @Override
  public void materializeNext(@NotNull final IndexedAsyncConsumer<E> consumer) {
    if (elements.hasNext()) {
      safeConsume(consumer, -1, index++, elements.next(), LOGGER);
    } else {
      safeConsumeComplete(consumer, 0, LOGGER);
    }
  }

  @Override
  public void materializeNextWhile(@NotNull final IndexedAsyncPredicate<E> predicate) {
    final int throughput = context.minThroughput();
    if (throughput < Integer.MAX_VALUE) {
      new NextTask(predicate, throughput).run();
    } else {
      final Iterator<E> elements = this.elements;
      while (elements.hasNext()) {
        if (!safeConsume(predicate, -1, index++, elements.next(), LOGGER)) {
          return;
        }
      }
      safeConsumeComplete(predicate, 0, LOGGER);
    }
  }

  @Override
  public void materializeSkip(final int count, @NotNull final AsyncConsumer<Integer> consumer) {
    if (count <= 0) {
      safeConsume(consumer, 0, LOGGER);
    } else {
      int skipped = 0;
      final Iterator<E> elements = this.elements;
      for (int i = 0; i < count && elements.hasNext(); ++i) {
        ++skipped;
        elements.next();
      }
      index += skipped;
      safeConsume(consumer, skipped, LOGGER);
    }
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
  public int weightSkip(final int count) {
    return Math.max(1, count);
  }

  private @NotNull String getTaskID() {
    final String taskID = context.currentTaskID();
    return taskID != null ? taskID : "";
  }

  private class NextTask implements Task {

    private final IndexedAsyncPredicate<E> predicate;
    private final int throughput;

    private String taskID;

    private NextTask(@NotNull final IndexedAsyncPredicate<E> predicate, final int throughput) {
      this.predicate = predicate;
      this.throughput = throughput;
    }

    @Override
    public void run() {
      final int throughput = this.throughput;
      final IndexedAsyncPredicate<E> predicate = this.predicate;
      final Iterator<E> iterator = elements;
      for (int n = 0; n < throughput && iterator.hasNext(); ++n) {
        if (!safeConsume(predicate, -1, index++, iterator.next(), LOGGER)) {
          return;
        }
      }
      if (!iterator.hasNext()) {
        safeConsumeComplete(predicate, 0, LOGGER);
      } else {
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
      return throughput;
    }
  }
}
