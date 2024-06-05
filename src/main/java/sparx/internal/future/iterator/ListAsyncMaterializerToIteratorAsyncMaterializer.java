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

import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.internal.future.AsyncConsumer;
import sparx.internal.future.IndexedAsyncConsumer;
import sparx.internal.future.list.ListAsyncMaterializer;

public class ListAsyncMaterializerToIteratorAsyncMaterializer<E> implements
    IteratorAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      ListAsyncMaterializerToIteratorAsyncMaterializer.class.getName());

  private final ListAsyncMaterializer<E> materializer;

  private int index;

  public ListAsyncMaterializerToIteratorAsyncMaterializer(
      @NotNull final ListAsyncMaterializer<E> materializer) {
    this.materializer = materializer;
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
    return materializer.knownSize();
  }

  @Override
  public void materializeCancel(final boolean mayInterruptIfRunning) {
  }

  @Override
  public void materializeHasNext(@NotNull final AsyncConsumer<Boolean> consumer) {
    materializer.materializeElement(index, new IndexedAsyncConsumer<E>() {
      @Override
      public void accept(final int size, final int index, final E element) throws Exception {
        consumer.accept(true);
      }

      @Override
      public void complete(final int size) throws Exception {
        consumer.accept(false);
      }

      @Override
      public void error(final int index, @NotNull final Exception error) throws Exception {
        consumer.error(error);
      }
    });
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
      public void error(final int index, @NotNull final Exception error) throws Exception {
        consumer.error(index, error);
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
}
