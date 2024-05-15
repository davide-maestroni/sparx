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
import sparx.util.Require;

public class ListToListAsyncMaterializer<E> implements ListAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      ListToListAsyncMaterializer.class.getName());

  private final List<E> elements;

  public ListToListAsyncMaterializer(@NotNull final List<E> elements) {
    this.elements = Require.notNull(elements, "elements");
  }

  @Override
  public boolean cancel(final boolean mayInterruptIfRunning) {
    return false;
  }

  @Override
  public int knownSize() {
    try {
      return elements.size();
    } catch (final Exception e) {
      LOGGER.log(Level.SEVERE, "Ignored exception", e);
    }
    return -1;
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
  @SuppressWarnings("SuspiciousMethodCalls")
  public void materializeContains(final Object element,
      @NotNull final AsyncConsumer<Boolean> consumer) {
    boolean contains;
    try {
      contains = elements.contains(element);
    } catch (final Exception e) {
      try {
        consumer.error(e);
      } catch (final Exception ex) {
        LOGGER.log(Level.SEVERE, "Ignored exception", e);
      }
      return;
    }
    try {
      consumer.accept(contains);
    } catch (final Exception e) {
      LOGGER.log(Level.SEVERE, "Ignored exception", e);
    }
  }

  @Override
  public void materializeElement(final int index, @NotNull final IndexedAsyncConsumer<E> consumer) {
    final List<E> elements = this.elements;
    if (index < 0) {
      try {
        consumer.error(index, new IndexOutOfBoundsException(Integer.toString(index)));
      } catch (final Exception e) {
        LOGGER.log(Level.SEVERE, "Ignored exception", e);
      }
    } else {
      try {
        final int size = elements.size();
        if (index >= size) {
          try {
            consumer.complete(size);
          } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Ignored exception", e);
          }
        } else {
          final E element = elements.get(index);
          try {
            consumer.accept(size, index, element);
          } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Ignored exception", e);
          }
        }
      } catch (final Exception e) {
        try {
          consumer.error(index, e);
        } catch (final Exception ex) {
          LOGGER.log(Level.SEVERE, "Ignored exception", e);
        }
      }
    }
  }

  @Override
  public void materializeElements(@NotNull final AsyncConsumer<List<E>> consumer) {
    try {
      consumer.accept(elements);
    } catch (final Exception e) {
      LOGGER.log(Level.SEVERE, "Ignored exception", e);
    }
  }

  @Override
  public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
    boolean empty;
    try {
      empty = elements.isEmpty();
    } catch (final Exception e) {
      try {
        consumer.error(e);
      } catch (final Exception ex) {
        LOGGER.log(Level.SEVERE, "Ignored exception", e);
      }
      return;
    }
    try {
      consumer.accept(empty);
    } catch (final Exception e) {
      LOGGER.log(Level.SEVERE, "Ignored exception", e);
    }
  }

  @Override
  public void materializeOrdered(@NotNull final IndexedAsyncConsumer<E> consumer) {
    int i = 0;
    try {
      final List<E> elements = this.elements;
      final int size = elements.size();
      while (i < size) {
        try {
          consumer.accept(size, i, elements.get(i));
        } catch (final Exception e) {
          LOGGER.log(Level.SEVERE, "Ignored exception", e);
          return;
        }
        ++i;
      }
      try {
        consumer.complete(size);
      } catch (final Exception e) {
        LOGGER.log(Level.SEVERE, "Ignored exception", e);
      }
    } catch (final Exception e) {
      try {
        consumer.error(i, e);
      } catch (final Exception ex) {
        LOGGER.log(Level.SEVERE, "Ignored exception", e);
      }
    }
  }

  @Override
  public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
    int size;
    try {
      size = elements.size();
    } catch (final Exception e) {
      try {
        consumer.error(e);
      } catch (final Exception ex) {
        LOGGER.log(Level.SEVERE, "Ignored exception", e);
      }
      return;
    }
    try {
      consumer.accept(size);
    } catch (final Exception e) {
      LOGGER.log(Level.SEVERE, "Ignored exception", e);
    }
  }

  @Override
  public void materializeUnordered(@NotNull final IndexedAsyncConsumer<E> consumer) {
    materializeOrdered(consumer);
  }
}
