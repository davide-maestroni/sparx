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
package sparx.collection.internal.list;

import java.util.Iterator;
import org.jetbrains.annotations.NotNull;
import sparx.collection.ListMaterializer;
import sparx.util.IndexOverflowException;
import sparx.util.Require;

public class ReverseListMaterializer<E> implements ListMaterializer<E> {

  private final ListMaterializer<E> wrapped;

  public ReverseListMaterializer(@NotNull final ListMaterializer<E> wrapped) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    final ListMaterializer<E> wrapped = this.wrapped;
    final long wrappedIndex = (long) wrapped.materializeSize() - index - 1;
    return wrappedIndex < Integer.MAX_VALUE && wrapped.canMaterializeElement((int) wrappedIndex);
  }

  @Override
  public int knownSize() {
    return wrapped.knownSize();
  }

  @Override
  public E materializeElement(final int index) {
    final ListMaterializer<E> wrapped = this.wrapped;
    final long wrappedIndex = (long) wrapped.materializeSize() - index - 1;
    return wrapped.materializeElement(IndexOverflowException.safeCast(wrappedIndex));
  }

  @Override
  public boolean materializeEmpty() {
    return wrapped.materializeEmpty();
  }

  @Override
  public @NotNull Iterator<E> materializeIterator() {
    return new ReverseIterator();
  }

  @Override
  public int materializeSize() {
    return wrapped.materializeSize();
  }

  private class ReverseIterator implements Iterator<E> {

    private int pos = wrapped.materializeSize() - 1;

    @Override
    public boolean hasNext() {
      return wrapped.canMaterializeElement(pos);
    }

    @Override
    public E next() {
      return wrapped.materializeElement(pos--);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove");
    }
  }
}
