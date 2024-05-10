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
import org.jetbrains.annotations.NotNull;
import sparx.collection.internal.future.AsyncConsumer;
import sparx.collection.internal.future.IndexedAsyncConsumer;
import sparx.util.Require;

public class ListToListAsyncMaterializer<E> implements ListAsyncMaterializer<E> {

  private ListAsyncMaterializer<E> state;

  public ListToListAsyncMaterializer(@NotNull final List<E> elements) {
    this.state = new RunningState(Require.notNull(elements, "elements"));
  }

  @Override
  public int knownSize() {
    return state.knownSize();
  }

  @Override
  public void materializeContains(final Object element,
      @NotNull final AsyncConsumer<Boolean> consumer) {
    state.materializeContains(element, consumer);
  }

  @Override
  public void materializeElement(final int index, @NotNull final IndexedAsyncConsumer<E> consumer) {
    state.materializeElement(index, consumer);
  }

  @Override
  public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
    state.materializeEmpty(consumer);
  }

  @Override
  public void materializeOrdered(@NotNull final IndexedAsyncConsumer<E> consumer) {
    state.materializeOrdered(consumer);
  }

  @Override
  public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
    state.materializeSize(consumer);
  }

  @Override
  public void materializeUnordered(@NotNull final IndexedAsyncConsumer<E> consumer) {
    state.materializeUnordered(consumer);
  }

  private class RunningState implements ListAsyncMaterializer<E> {

    private final List<E> elements;

    private RunningState(@NotNull final List<E> elements) {
      this.elements = elements;
    }

    @Override
    public int knownSize() {
      return elements.size();
    }

    @Override
    @SuppressWarnings("SuspiciousMethodCalls")
    public void materializeContains(final Object element,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      boolean contains;
      try {
        contains = elements.contains(element);
      } catch (final Exception e) {
        state = new FailedListAsyncMaterializer<E>(safeSize(), -1, e);
        try {
          consumer.error(e);
        } catch (final Exception ex) {
          // TODO
        }
        return;
      }
      try {
        consumer.accept(contains);
      } catch (final Exception e) {
        // TODO
      }
    }

    @Override
    public void materializeElement(final int index,
        @NotNull final IndexedAsyncConsumer<E> consumer) {
      final List<E> elements = this.elements;
      if (index < 0) {
        try {
          consumer.error(index, new IndexOutOfBoundsException(Integer.toString(index)));
        } catch (final Exception e) {
          // TODO
        }
      } else {
        try {
          final int size = elements.size();
          if (index >= size) {
            try {
              consumer.complete(size);
            } catch (final Exception e) {
              // TODO
            }
          } else {
            final E element = elements.get(index);
            try {
              consumer.accept(size, index, element);
            } catch (final Exception e) {
              // TODO
            }
          }
        } catch (final Exception e) {
          state = new FailedListAsyncMaterializer<E>(safeSize(), index, e);
          try {
            consumer.error(index, e);
          } catch (final Exception ex) {
            // TODO
          }
        }
      }
    }

    @Override
    public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
      boolean empty;
      try {
        empty = elements.isEmpty();
      } catch (final Exception e) {
        state = new FailedListAsyncMaterializer<E>(safeSize(), -1, e);
        try {
          consumer.error(e);
        } catch (final Exception ex) {
          // TODO
        }
        return;
      }
      try {
        consumer.accept(empty);
      } catch (final Exception e) {
        // TODO
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
            // TODO
            return;
          }
          ++i;
        }
        try {
          consumer.complete(size);
        } catch (final Exception e) {
          // TODO
        }
      } catch (final Exception e) {
        state = new FailedListAsyncMaterializer<E>(safeSize(), i, e);
        try {
          consumer.error(i, e);
        } catch (final Exception ex) {
          // TODO
        }
      }
    }

    @Override
    public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
      int size;
      try {
        size = elements.size();
      } catch (final Exception e) {
        state = new FailedListAsyncMaterializer<E>(safeSize(), -1, e);
        try {
          consumer.error(e);
        } catch (final Exception ex) {
          // TODO
        }
        return;
      }
      try {
        consumer.accept(size);
      } catch (final Exception e) {
        // TODO
      }
    }

    @Override
    public void materializeUnordered(@NotNull final IndexedAsyncConsumer<E> consumer) {
      materializeOrdered(consumer);
    }

    private int safeSize() {
      try {
        return elements.size();
      } catch (final Exception ignored) {
        return -1;
      }
    }
  }
}
