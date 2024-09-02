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

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CancellationException;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ContextTask;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.AsyncConsumer;
import sparx.internal.future.IndexedAsyncConsumer;
import sparx.internal.future.IndexedAsyncPredicate;

public class CollectionToIteratorAsyncMaterializer<E> implements IteratorAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      CollectionToIteratorAsyncMaterializer.class.getName());

  private final ExecutionContext context;
  private final Collection<E> elements;
  private final Iterator<E> iterator;

  private int index;

  public CollectionToIteratorAsyncMaterializer(@NotNull final Collection<E> elements,
      @NotNull final ExecutionContext context) {
    this.elements = elements;
    this.context = context;
    iterator = elements.iterator();
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
    return elements.size();
  }

  @Override
  public void materializeCancel(@NotNull final CancellationException exception) {
  }

  @Override
  public void materializeHasNext(@NotNull final AsyncConsumer<Boolean> consumer) {
    safeConsume(consumer, iterator.hasNext(), LOGGER);
  }

  @Override
  public void materializeNext(@NotNull final IndexedAsyncConsumer<E> consumer) {
    final Iterator<E> iterator = this.iterator;
    if (iterator.hasNext()) {
      final int i = index++;
      safeConsume(consumer, elements.size() - i, i, iterator.next(), LOGGER);
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
      final Collection<E> elements = this.elements;
      final Iterator<E> iterator = this.iterator;
      while (iterator.hasNext()) {
        final int i = index++;
        if (!safeConsume(predicate, elements.size() - i, i, iterator.next(), LOGGER)) {
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
      final Iterator<E> iterator = this.iterator;
      for (int i = 0; i < count && iterator.hasNext(); ++i) {
        ++skipped;
        iterator.next();
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
    return Math.min(context.minThroughput(), elements.size() - index);
  }

  @Override
  public int weightSkip(final int count) {
    return Math.max(1, count);
  }

  private @NotNull String getTaskID() {
    final String taskID = context.currentTaskID();
    return taskID != null ? taskID : "";
  }

  private class NextTask extends ContextTask {

    private final IndexedAsyncPredicate<E> predicate;
    private final int throughput;

    private String taskID;

    private NextTask(@NotNull final IndexedAsyncPredicate<E> predicate, final int throughput) {
      super(context);
      this.predicate = predicate;
      this.throughput = throughput;
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

    @Override
    protected void runWithContext() {
      final int throughput = this.throughput;
      final IndexedAsyncPredicate<E> predicate = this.predicate;
      final Collection<E> elements = CollectionToIteratorAsyncMaterializer.this.elements;
      final Iterator<E> iterator = CollectionToIteratorAsyncMaterializer.this.iterator;
      final int size = elements.size();
      for (int n = 0; n < throughput && iterator.hasNext(); ++n) {
        final int i = index++;
        if (!safeConsume(predicate, size - i, i, iterator.next(), LOGGER)) {
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
  }
}
