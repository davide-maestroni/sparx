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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.collection.internal.future.AsyncConsumer;
import sparx.collection.internal.future.IndexedAsyncConsumer;

public abstract class AbstractListAsyncMaterializer<E> implements ListAsyncMaterializer<E> {

  protected <T> boolean safeConsume(@NotNull final IndexedAsyncConsumer<T> consumer, final int size,
      final int index, final T value, @NotNull final Logger logger) {
    try {
      consumer.accept(size, index, value);
    } catch (final Exception error) {
      if (error instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      try {
        consumer.error(index, error);
      } catch (final Exception e) {
        if (e instanceof InterruptedException) {
          Thread.currentThread().interrupt();
        }
        logger.log(Level.SEVERE, "Ignored exception", e);
      }
      return false;
    }
    return true;
  }

  protected <T> void safeConsume(@NotNull final AsyncConsumer<T> consumer, final T value,
      @NotNull final Logger logger) {
    try {
      consumer.accept(value);
    } catch (final Exception error) {
      if (error instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      try {
        consumer.error(error);
      } catch (final Exception e) {
        if (e instanceof InterruptedException) {
          Thread.currentThread().interrupt();
        }
        logger.log(Level.SEVERE, "Ignored exception", e);
      }
    }
  }

  protected void safeConsumeComplete(@NotNull final IndexedAsyncConsumer<?> consumer,
      final int size, @NotNull final Logger logger) {
    try {
      consumer.complete(size);
    } catch (final Exception e) {
      if (e instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      logger.log(Level.SEVERE, "Ignored exception", e);
    }
  }

  protected void safeConsumeError(@NotNull final AsyncConsumer<?> consumer,
      @NotNull final Exception error, @NotNull final Logger logger) {
    try {
      consumer.error(error);
    } catch (final Exception e) {
      if (e instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      logger.log(Level.SEVERE, "Ignored exception", e);
    }
  }

  protected void safeConsumeError(@NotNull final IndexedAsyncConsumer<?> consumer, final int index,
      @NotNull final Exception error, @NotNull final Logger logger) {
    try {
      consumer.error(index, error);
    } catch (final Exception e) {
      if (e instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      logger.log(Level.SEVERE, "Ignored exception", e);
    }
  }
}
