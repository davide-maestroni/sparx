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

import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import sparx.collection.internal.future.AsyncConsumer;
import sparx.collection.internal.future.IndexedAsyncConsumer;

public class EmptyListAsyncMaterializer<E> implements ListAsyncMaterializer<E> {

  private static final EmptyListAsyncMaterializer<?> INSTANCE = new EmptyListAsyncMaterializer<Object>();

  @SuppressWarnings("unchecked")
  public static @NotNull <E> EmptyListAsyncMaterializer<E> instance() {
    return (EmptyListAsyncMaterializer<E>) INSTANCE;
  }

  @Override
  public boolean cancel(final boolean mayInterruptIfRunning) {
    return false;
  }

  @Override
  public int knownSize() {
    return 0;
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
  public void materialize(@NotNull final AsyncConsumer<List<E>> consumer) {
    try {
      consumer.accept(Collections.<E>emptyList());
    } catch (final Exception e) {
      // TODO
    }

  }

  @Override
  public void materializeContains(final Object element,
      @NotNull final AsyncConsumer<Boolean> consumer) {
    try {
      consumer.accept(false);
    } catch (final Exception e) {
      // TODO
    }
  }

  @Override
  public void materializeElement(final int index, @NotNull final IndexedAsyncConsumer<E> consumer) {
    try {
      if (index < 0) {
        consumer.error(index, new IndexOutOfBoundsException(Integer.toString(index)));
      } else {
        consumer.complete(0);
      }
    } catch (final Exception e) {
      // TODO
    }
  }

  @Override
  public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
    try {
      consumer.accept(true);
    } catch (final Exception e) {
      // TODO
    }
  }

  @Override
  public void materializeOrdered(@NotNull final IndexedAsyncConsumer<E> consumer) {
    try {
      consumer.complete(0);
    } catch (final Exception e) {
      // TODO
    }
  }

  @Override
  public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
    try {
      consumer.accept(0);
    } catch (final Exception e) {
      // TODO
    }
  }

  @Override
  public void materializeUnordered(@NotNull final IndexedAsyncConsumer<E> consumer) {
    try {
      consumer.complete(0);
    } catch (final Exception e) {
      // TODO
    }
  }
}
