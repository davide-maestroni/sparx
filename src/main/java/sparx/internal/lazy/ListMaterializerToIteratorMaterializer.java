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
package sparx.internal.lazy;

import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;
import sparx.internal.lazy.iterator.IteratorMaterializer;
import sparx.internal.lazy.list.ListMaterializer;
import sparx.util.annotation.Positive;

public class ListMaterializerToIteratorMaterializer<E> implements IteratorMaterializer<E> {

  private final ListMaterializer<E> wrapped;

  private int pos;

  public ListMaterializerToIteratorMaterializer(@NotNull final ListMaterializer<E> wrapped) {
    this.wrapped = wrapped;
  }

  @Override
  public int knownSize() {
    final int knownSize = wrapped.knownSize();
    if (knownSize >= 0) {
      return knownSize - pos;
    }
    return -1;
  }

  @Override
  public boolean materializeHasNext() {
    return wrapped.canMaterializeElement(pos);
  }

  @Override
  public E materializeNext() {
    try {
      return wrapped.materializeElement(pos++);
    } catch (final IndexOutOfBoundsException ignored) {
      throw new NoSuchElementException();
    }
  }

  @Override
  public int materializeSkip(@Positive final int count) {
    final ListMaterializer<E> wrapped = this.wrapped;
    final long wrappedIndex = (long) count + pos;
    if (wrappedIndex < Integer.MAX_VALUE && wrapped.canMaterializeElement((int) wrappedIndex)) {
      pos += count;
      return count;
    }
    final int skipped = Math.min(count, wrapped.materializeSize() - pos);
    pos += skipped;
    return skipped;
  }

  public @NotNull ListMaterializer<E> materializer() {
    return wrapped;
  }
}
