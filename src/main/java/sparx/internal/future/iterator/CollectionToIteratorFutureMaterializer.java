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
import java.util.Collection;
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
import sparx.util.annotation.Positive;

public class CollectionToIteratorFutureMaterializer<E> implements IteratorFutureMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      CollectionToIteratorFutureMaterializer.class.getName());

  private final ExecutionContext context;
  private final Collection<E> elements;
  private final Iterator<E> iterator;
  private final int offset;

  private int pos;

  public CollectionToIteratorFutureMaterializer(@NotNull final Collection<E> elements,
      @NotNull final ExecutionContext context) {
    this(elements, context, 0);
  }

  public CollectionToIteratorFutureMaterializer(@NotNull final Collection<E> elements,
      @NotNull final ExecutionContext context, final int offset) {
    this.elements = elements;
    this.context = context;
    this.offset = offset;
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
  public void materializeElements(@NotNull final FutureConsumer<List<E>> consumer) {
    final ArrayList<E> elements = new ArrayList<E>();
    final Iterator<E> iterator = this.iterator;
    while (iterator.hasNext()) {
      elements.add(iterator.next());
    }
    safeConsume(consumer, elements, LOGGER);
  }

  @Override
  public void materializeHasNext(@NotNull final FutureConsumer<Boolean> consumer) {
    safeConsume(consumer, iterator.hasNext(), LOGGER);
  }

  @Override
  public void materializeIterator(@NotNull final FutureConsumer<Iterator<E>> consumer) {
    safeConsume(consumer, new Iterator<E>() {
      @Override
      public boolean hasNext() {
        return iterator.hasNext();
      }

      @Override
      public E next() {
        if (!iterator.hasNext()) {
          throw new NoSuchElementException();
        }
        ++pos;
        return iterator.next();
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("remove");
      }
    }, LOGGER);
  }

  @Override
  public void materializeNext(@NotNull final IndexedFutureConsumer<E> consumer) {
    final Iterator<E> iterator = this.iterator;
    if (iterator.hasNext()) {
      final int i = pos++;
      safeConsume(consumer, elements.size() - i, offset + i, iterator.next(), LOGGER);
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
      final Collection<E> elements = this.elements;
      final Iterator<E> iterator = this.iterator;
      while (iterator.hasNext()) {
        final int i = pos++;
        if (!safeConsume(predicate, elements.size() - i, offset + i, iterator.next(), LOGGER)) {
          return;
        }
      }
      safeConsumeComplete(predicate, 0, LOGGER);
    }
  }

  @Override
  public void materializeSkip(@Positive final int count,
      @NotNull final FutureConsumer<Integer> consumer) {
    int skipped = 0;
    final Iterator<E> iterator = this.iterator;
    for (int i = 0; i < count && iterator.hasNext(); ++i) {
      ++skipped;
      iterator.next();
    }
    pos += skipped;
    safeConsume(consumer, skipped, LOGGER);
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
    return Math.min(context.minThroughput(), elements.size() - pos);
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
      return Math.min(throughput, elements.size() - pos);
    }

    @Override
    protected void runWithContext() {
      final int throughput = this.throughput;
      final IndexedFuturePredicate<E> predicate = this.predicate;
      final Collection<E> elements = CollectionToIteratorFutureMaterializer.this.elements;
      final Iterator<E> iterator = CollectionToIteratorFutureMaterializer.this.iterator;
      final int size = elements.size();
      for (int n = 0; n < throughput && iterator.hasNext(); ++n) {
        final int i = pos++;
        if (!safeConsume(predicate, size - i, offset + i, iterator.next(), LOGGER)) {
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
