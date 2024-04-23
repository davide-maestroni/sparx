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
package sparx.collection.internal.iterator;

import java.util.NoSuchElementException;
import sparx.util.Require;

public class RepeatIteratorMaterializer<E> implements IteratorMaterializer<E> {

  private final E element;
  private final int times;

  private int pos;

  public RepeatIteratorMaterializer(final int times, final E element) {
    this.times = Require.positive(times, "times");
    this.element = element;
  }

  @Override
  public int knownSize() {
    return times;
  }

  @Override
  public boolean materializeHasNext() {
    return pos < times;
  }

  @Override
  public E materializeNext() {
    if (pos >= times) {
      throw new NoSuchElementException();
    }
    ++pos;
    return element;
  }

  @Override
  public int materializeSkip(final int count) {
    final int skipped = Math.max(0, Math.min(count, times - pos));
    pos += skipped;
    return skipped;
  }
}
