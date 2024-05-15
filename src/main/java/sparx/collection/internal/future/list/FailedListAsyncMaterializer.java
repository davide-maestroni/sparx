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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.collection.internal.future.AsyncConsumer;
import sparx.collection.internal.future.IndexedAsyncConsumer;

public class FailedListAsyncMaterializer<E> implements ListAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      FailedListAsyncMaterializer.class.getName());

  private final Exception error;
  private final int index;
  private final int size;

  public FailedListAsyncMaterializer(final int size, final int index,
      @NotNull final Exception error) {
    this.size = size;
    this.index = index;
    this.error = error;
  }

  @Override
  public boolean cancel(final boolean mayInterruptIfRunning) {
    return false;
  }

  @Override
  public int knownSize() {
    return size;
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
  public void materializeContains(final Object element,
      @NotNull final AsyncConsumer<Boolean> consumer) {
    try {
      consumer.error(error);
    } catch (final Exception e) {
      LOGGER.log(Level.SEVERE, "Ignored exception", e);
    }
  }

  @Override
  public void materializeElement(final int index, @NotNull final IndexedAsyncConsumer<E> consumer) {
    try {
      consumer.error(this.index, error);
    } catch (final Exception e) {
      LOGGER.log(Level.SEVERE, "Ignored exception", e);
    }
  }

  @Override
  public void materializeElements(@NotNull final AsyncConsumer<List<E>> consumer) {
    try {
      consumer.error(error);
    } catch (final Exception e) {
      LOGGER.log(Level.SEVERE, "Ignored exception", e);
    }
  }

  @Override
  public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
    try {
      consumer.error(error);
    } catch (final Exception e) {
      LOGGER.log(Level.SEVERE, "Ignored exception", e);
    }
  }

  @Override
  public void materializeOrdered(@NotNull final IndexedAsyncConsumer<E> consumer) {
    try {
      consumer.error(index, error);
    } catch (final Exception e) {
      LOGGER.log(Level.SEVERE, "Ignored exception", e);
    }
  }

  @Override
  public void materializeUnordered(@NotNull final IndexedAsyncConsumer<E> consumer) {
    try {
      consumer.error(index, error);
    } catch (final Exception e) {
      LOGGER.log(Level.SEVERE, "Ignored exception", e);
    }
  }

  @Override
  public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
    try {
      consumer.error(error);
    } catch (final Exception e) {
      LOGGER.log(Level.SEVERE, "Ignored exception", e);
    }
  }
}
