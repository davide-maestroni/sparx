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
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.internal.future.AsyncConsumer;
import sparx.internal.future.IndexedAsyncConsumer;
import sparx.internal.future.IndexedAsyncPredicate;
import sparx.internal.future.list.ListAsyncMaterializer;
import sparx.lazy;

public class ListAsyncMaterializerToIteratorAsyncMaterializer<E> implements
    IteratorAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      ListAsyncMaterializerToIteratorAsyncMaterializer.class.getName());

  private final ArrayList<IndexedAsyncPredicate<E>> elementsPredicates = new ArrayList<IndexedAsyncPredicate<E>>(
      2);
  private final ListAsyncMaterializer<E> materializer;

  private CancellationException cancelException;
  private int index;

  public ListAsyncMaterializerToIteratorAsyncMaterializer(
      @NotNull final ListAsyncMaterializer<E> materializer) {
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
  public boolean isFailed() {
    return materializer.isFailed();
  }

  @Override
  public boolean isMaterializedAtOnce() {
    return materializer.isMaterializedAtOnce();
  }

  @Override
  public boolean isSucceeded() {
    return materializer.isSucceeded();
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
  public void materializeElements(@NotNull final AsyncConsumer<Iterator<E>> consumer) {
    materializer.materializeElements(new AsyncConsumer<List<E>>() {
      @Override
      public void accept(final List<E> list) throws Exception {
        consumer.accept(lazy.Iterator.wrap(list).drop(index));
      }

      @Override
      public void error(@NotNull final Exception error) throws Exception {
        consumer.error(error);
      }
    });
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
  public void materializeNextWhile(@NotNull final IndexedAsyncPredicate<E> predicate) {
    final ArrayList<IndexedAsyncPredicate<E>> elementsPredicates = this.elementsPredicates;
    elementsPredicates.add(predicate);
    if (elementsPredicates.size() == 1) {
      materializer.materializeNextWhile(index, new IndexedAsyncPredicate<E>() {
        @Override
        public void complete(final int size) {
          for (final IndexedAsyncPredicate<E> predicate : elementsPredicates) {
            safeConsumeComplete(predicate, 0, LOGGER);
          }
          elementsPredicates.clear();
        }

        @Override
        public void error(@NotNull final Exception error) {
          final Exception exception;
          if (cancelException != null) {
            exception = cancelException;
          } else {
            exception = error;
          }
          for (final IndexedAsyncPredicate<E> predicate : elementsPredicates) {
            safeConsumeError(predicate, exception, LOGGER);
          }
          elementsPredicates.clear();
        }

        @Override
        public boolean test(final int size, final int index, final E element) {
          ListAsyncMaterializerToIteratorAsyncMaterializer.this.index = index;
          final Iterator<IndexedAsyncPredicate<E>> iterator = elementsPredicates.iterator();
          while (iterator.hasNext()) {
            if (!safeConsume(iterator.next(), size >= 0 ? size - index : -1, index, element,
                LOGGER)) {
              iterator.remove();
            }
          }
          return !elementsPredicates.isEmpty();
        }
      });
    }
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
  public int weightElements() {
    return materializer.weightElements();
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
  public int weightNextWhile() {
    return materializer.weightNextWhile();
  }

  @Override
  public int weightSkip(final int count) {
    return materializer.weightSize();
  }
}
