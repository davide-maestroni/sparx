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
package sparx.internal.lazy.iterator;

import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;

public class EmptyIteratorMaterializer<E> implements IteratorMaterializer<E> {

  private static final EmptyIteratorMaterializer<?> INSTANCE = new EmptyIteratorMaterializer<Object>();

  private EmptyIteratorMaterializer() {
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <E> EmptyIteratorMaterializer<E> instance() {
    return (EmptyIteratorMaterializer<E>) INSTANCE;
  }

  @Override
  public int knownSize() {
    return 0;
  }

  @Override
  public boolean materializeHasNext() {
    return false;
  }

  @Override
  public E materializeNext() {
    throw new NoSuchElementException();
  }

  @Override
  public int materializeSkip(final int count) {
    return 0;
  }
}
