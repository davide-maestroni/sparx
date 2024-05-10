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

import org.jetbrains.annotations.NotNull;
import sparx.collection.internal.future.AsyncConsumer;
import sparx.collection.internal.future.IndexedAsyncConsumer;

public class ElementToListAsyncMaterializer<E> implements ListAsyncMaterializer<E> {

  private final E element;

  public ElementToListAsyncMaterializer(final E element) {
    this.element = element;
  }

  @Override
  public int knownSize() {
    return 1;
  }

  @Override
  public void materializeContains(final Object element,
      @NotNull final AsyncConsumer<Boolean> consumer) {
    try {
      consumer.accept(element == this.element || (element != null && element.equals(this.element)));
    } catch (final Exception e) {
      // TODO
    }
  }

  @Override
  public void materializeElement(final int index, @NotNull final IndexedAsyncConsumer<E> consumer) {
    if (index < 0) {
      try {
        consumer.error(index, new IndexOutOfBoundsException(Integer.toString(index)));
      } catch (final Exception e) {
        // TODO
      }
    } else if (index != 0) {
      try {
        consumer.complete(1);
      } catch (final Exception e) {
        // TODO
      }
    } else {
      try {
        consumer.accept(1, 0, element);
      } catch (final Exception e) {
        // TODO
      }
    }
  }

  @Override
  public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
    try {
      consumer.accept(false);
    } catch (final Exception e) {
      // TODO
    }
  }

  @Override
  public void materializeOrdered(@NotNull final IndexedAsyncConsumer<E> consumer) {
    try {
      consumer.accept(1, 0, element);
    } catch (final Exception e) {
      // TODO
      return;
    }
    try {
      consumer.complete(1);
    } catch (final Exception e) {
      // TODO
    }
  }

  @Override
  public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
    try {
      consumer.accept(1);
    } catch (final Exception e) {
      // TODO
    }
  }

  @Override
  public void materializeUnordered(@NotNull final IndexedAsyncConsumer<E> consumer) {
    materializeOrdered(consumer);
  }
}
