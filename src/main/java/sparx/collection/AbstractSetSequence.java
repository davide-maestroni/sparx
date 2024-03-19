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
package sparx.collection;

import java.util.AbstractSet;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import sparx.util.Require;

public abstract class AbstractSetSequence<E> extends AbstractSet<E> implements SetSequence<E> {

  private final ListMaterializer<E> materializer;

  public AbstractSetSequence(@NotNull final ListMaterializer<E> materializer) {
    this.materializer = Require.notNull(materializer, "materializer");
  }

  @Override
  public boolean addAll(@NotNull final Collection<? extends E> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  @Override
  public E head() {
    return materializer.materializeElement(0);
  }

  @Override
  public boolean isEmpty() {
    return materializer.materializeEmpty();
  }

  @Override
  public boolean notEmpty() {
    return !isEmpty();
  }

  @Override
  public boolean remove(final Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAll(@NotNull final Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean retainAll(@NotNull final Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int size() {
    return materializer.materializeSize();
  }

  @Override
  public E tail() {
    return materializer.materializeElement(size() - 1);
  }
}
