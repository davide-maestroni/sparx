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
import static sparx.internal.future.FutureConsumers.safeConsumeComplete;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CancellationException;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ContextTask;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.FutureConsumer;
import sparx.internal.future.IndexedFutureConsumer;
import sparx.internal.future.IndexedFuturePredicate;

public class IteratorToIteratorFutureMaterializer<E> implements IteratorFutureMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      IteratorToIteratorFutureMaterializer.class.getName());

  private final ExecutionContext context;
  private final Iterator<E> elements;

  private int index;

  public IteratorToIteratorFutureMaterializer(@NotNull final Iterator<E> elements,
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
    return -1;
  }

  @Override
  public void materializeCancel(@NotNull final CancellationException exception) {
  }

  @Override
  public void materializeElements(@NotNull final FutureConsumer<List<E>> consumer) {
    final ArrayList<E> elements = new ArrayList<E>();
    final Iterator<E> iterator = this.elements;
    while (iterator.hasNext()) {
      elements.add(iterator.next());
    }
    safeConsume(consumer, elements, LOGGER);
  }

  @Override
  public void materializeHasNext(@NotNull final FutureConsumer<Boolean> consumer) {
    safeConsume(consumer, elements.hasNext(), LOGGER);
  }

  @Override
  public void materializeIterator(@NotNull final FutureConsumer<Iterator<E>> consumer) {
    safeConsume(consumer, new Iterator<E>() {
      @Override
      public boolean hasNext() {
        return elements.hasNext();
      }

      @Override
      public E next() {
        if (!elements.hasNext()) {
          throw new NoSuchElementException();
        }
        ++index;
        return elements.next();
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("remove");
      }
    }, LOGGER);
  }

  @Override
  public void materializeNext(@NotNull final IndexedFutureConsumer<E> consumer) {
    if (elements.hasNext()) {
      safeConsume(consumer, -1, index++, elements.next(), LOGGER);
    } else {
      safeConsumeComplete(consumer, 0, LOGGER);
    }
  }

  @Override
  public void materializeNextWhile(@NotNull final IndexedFuturePredicate<E> predicate) {
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
  public void materializeSkip(final int count, @NotNull final FutureConsumer<Integer> consumer) {
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
  public int weightElements() {
    return 0;
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
    final String taskID = context.currentTaskID();
    return taskID != null ? taskID : "";
  }

  private class NextTask extends ContextTask {

    private final IndexedFuturePredicate<E> predicate;
    private final int throughput;

    private String taskID;

    private NextTask(@NotNull final IndexedFuturePredicate<E> predicate, final int throughput) {
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
      return throughput;
    }

    @Override
    protected void runWithContext() {
      final int throughput = this.throughput;
      final IndexedFuturePredicate<E> predicate = this.predicate;
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
  }
}
