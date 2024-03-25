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
package sparx;

import java.util.Iterator;
import org.jetbrains.annotations.NotNull;
import sparx.collection.ListMaterializer;
import sparx.util.Require;
import sparx.util.UncheckedException;
import sparx.util.function.Consumer;

class PeekListMaterializer<E> implements ListMaterializer<E> {

  private final Consumer<? super E> consumer;
  private final ListMaterializer<E> wrapped;

  PeekListMaterializer(@NotNull final ListMaterializer<E> wrapped,
      @NotNull final Consumer<? super E> consumer) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
    this.consumer = Require.notNull(consumer, "consumer");
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    return wrapped.canMaterializeElement(index);
  }

  @Override
  public int knownSize() {
    return wrapped.knownSize();
  }

  @Override
  public E materializeElement(final int index) {
    final E element = wrapped.materializeElement(index);
    try {
      consumer.accept(element);
    } catch (final Exception e) {
      throw UncheckedException.throwUnchecked(e);
    }
    return element;
  }

  @Override
  public boolean materializeEmpty() {
    return wrapped.materializeEmpty();
  }

  @Override
  public @NotNull Iterator<E> materializeIterator() {
    return new ListMaterializerIterator<E>(this);
  }

  @Override
  public int materializeSize() {
    return wrapped.materializeSize();
  }
}
