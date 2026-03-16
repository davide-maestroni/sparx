/*
 * Copyright 2026 Davide Maestroni
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
package sparx1.internal.lazy.iterator;

import java.util.NoSuchElementException;
import sparx1.internal.lazy.IteratorMaterializer;
import sparx1.util.annotation.NotNull;

public class InterleaveWithPaddingIteratorMaterializer<E> extends AbstractIteratorMaterializer<E> {

  private final IteratorMaterializer<E> elementsMaterializer;
  private final E paddingLeft;
  private final E paddingRight;
  private final IteratorMaterializer<E> wrapped;

  private boolean isWrapped = true;

  public InterleaveWithPaddingIteratorMaterializer(final @NotNull IteratorMaterializer<E> wrapped,
      final @NotNull IteratorMaterializer<E> elementsMaterializer, final E paddingLeft,
      final E paddingRight) {
    this.wrapped = wrapped;
    this.elementsMaterializer = elementsMaterializer;
    this.paddingLeft = paddingLeft;
    this.paddingRight = paddingRight;
  }

  @Override
  public int currentKnownSize() {
    final int knownSize = wrapped.currentKnownSize();
    if (knownSize >= 0) {
      final int elementsSize = elementsMaterializer.currentKnownSize();
      if (elementsSize >= 0) {
        return Math.max(knownSize, elementsSize) * 2;
      }
    }
    return -1;
  }

  @Override
  public boolean materializeHasNext() {
    return !isWrapped || wrapped.materializeHasNext() || elementsMaterializer.materializeHasNext();
  }

  @Override
  public E materializeNext() {
    final boolean isWrapped = this.isWrapped;
    final E next;
    if (isWrapped) {
      final IteratorMaterializer<E> wrapped = this.wrapped;
      if (wrapped.materializeHasNext()) {
        next = wrapped.materializeNext();
      } else if (elementsMaterializer.materializeHasNext()) {
        next = paddingLeft;
      } else {
        throw new NoSuchElementException();
      }
    } else {
      final IteratorMaterializer<E> elementsMaterializer = this.elementsMaterializer;
      if (elementsMaterializer.materializeHasNext()) {
        next = elementsMaterializer.materializeNext();
      } else {
        next = paddingRight;
      }
    }
    this.isWrapped = !isWrapped;
    return next;
  }
}
