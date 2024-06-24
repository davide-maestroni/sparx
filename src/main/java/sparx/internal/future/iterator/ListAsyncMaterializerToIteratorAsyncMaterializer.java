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
import static sparx.internal.future.AsyncConsumers.safeConsumeError;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CancellationException;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.concurrent.ExecutionContext.Task;
import sparx.internal.future.AsyncConsumer;
import sparx.internal.future.IndexedAsyncConsumer;
import sparx.internal.future.list.ListAsyncMaterializer;

public class ListAsyncMaterializerToIteratorAsyncMaterializer<E> implements
    IteratorAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      ListAsyncMaterializerToIteratorAsyncMaterializer.class.getName());

  private final ExecutionContext context;
  private final ArrayList<IndexedAsyncConsumer<E>> elementsConsumers = new ArrayList<IndexedAsyncConsumer<E>>(
      2);
  private final ListAsyncMaterializer<E> materializer;

  private CancellationException cancelException;
  private int index;

  public ListAsyncMaterializerToIteratorAsyncMaterializer(@NotNull final ExecutionContext context,
      @NotNull final ListAsyncMaterializer<E> materializer) {
    this.context = context;
    this.materializer = materializer;
  }

  @Override
  public boolean isCancelled() {
    return materializer.isCancelled();
  }

  @Override
  public boolean isDone() {
    return materializer.isDone();
  }

  @Override
  public int knownSize() {
    return materializer.knownSize();
  }

  @Override
  public void materializeCancel(@NotNull final CancellationException exception) {
    cancelException = exception;
    materializer.materializeCancel(exception);
  }

  @Override
  public void materializeEach(@NotNull final IndexedAsyncConsumer<E> consumer) {
    final ArrayList<IndexedAsyncConsumer<E>> elementsConsumers = this.elementsConsumers;
    elementsConsumers.add(consumer);
    if (elementsConsumers.size() == 1) {
      materializer.materializeElement(index, new MaterializingAsyncConsumer());
    }
  }

  @Override
  public void materializeHasNext(@NotNull final AsyncConsumer<Boolean> consumer) {
    materializer.materializeHasElement(index, consumer);
  }

  @Override
  public void materializeNext(@NotNull final IndexedAsyncConsumer<E> consumer) {
    materializer.materializeElement(index++, new IndexedAsyncConsumer<E>() {
      @Override
      public void accept(final int size, final int index, final E element) throws Exception {
        consumer.accept(size, index, element);
      }

      @Override
      public void complete(final int size) throws Exception {
        consumer.complete(size);
      }

      @Override
      public void error(@NotNull final Exception error) throws Exception {
        consumer.error(error);
      }
    });
  }

  @Override
  public void materializeSkip(final int count, @NotNull final AsyncConsumer<Integer> consumer) {
    if (count <= 0) {
      safeConsume(consumer, 0, LOGGER);
    } else {
      materializer.materializeSize(new AsyncConsumer<Integer>() {
        @Override
        public void accept(final Integer size) throws Exception {
          final int skipped = Math.min(count, size - index);
          index += skipped;
          consumer.accept(skipped);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }
  }

  @Override
  public int weightHasNext() {
    return materializer.weightElement();
  }

  @Override
  public int weightNext() {
    return materializer.weightElement();
  }

  @Override
  public int weightSkip(final int count) {
    return materializer.weightSize();
  }

  private @NotNull String getTaskID() {
    final String taskID = context.currentTaskID();
    return taskID != null ? taskID : "";
  }

  private class MaterializingAsyncConsumer implements IndexedAsyncConsumer<E>, Task {

    private String taskID;

    @Override
    public void accept(final int size, final int index, final E element) {
      ListAsyncMaterializerToIteratorAsyncMaterializer.this.index = index + 1;
      final ArrayList<IndexedAsyncConsumer<E>> elementsConsumers = ListAsyncMaterializerToIteratorAsyncMaterializer.this.elementsConsumers;
      final Iterator<IndexedAsyncConsumer<E>> iterator = elementsConsumers.iterator();
      while (iterator.hasNext()) {
        if (!safeConsume(iterator.next(), -1, index, element, LOGGER)) {
          iterator.remove();
        }
      }
      if (!elementsConsumers.isEmpty()) {
        taskID = getTaskID();
        context.scheduleAfter(this);
      }
    }

    @Override
    public void complete(final int size) {
      final ArrayList<IndexedAsyncConsumer<E>> elementsConsumers = ListAsyncMaterializerToIteratorAsyncMaterializer.this.elementsConsumers;
      for (final IndexedAsyncConsumer<E> consumer : elementsConsumers) {
        safeConsumeComplete(consumer, size, LOGGER);
      }
      elementsConsumers.clear();
    }

    @Override
    public void error(@NotNull final Exception error) {
      final Exception exception;
      if (cancelException != null) {
        exception = cancelException;
      } else {
        exception = error;
      }
      final ArrayList<IndexedAsyncConsumer<E>> elementsConsumers = ListAsyncMaterializerToIteratorAsyncMaterializer.this.elementsConsumers;
      for (final IndexedAsyncConsumer<E> consumer : elementsConsumers) {
        safeConsumeError(consumer, exception, LOGGER);
      }
      elementsConsumers.clear();
    }

    @Override
    public void run() {
      materializer.materializeElement(index, this);
    }

    @Override
    public @NotNull String taskID() {
      return taskID;
    }

    @Override
    public int weight() {
      return materializer.weightElement();
    }
  }
}
