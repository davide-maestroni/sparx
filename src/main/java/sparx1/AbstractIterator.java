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
package sparx1;

import java.io.IOException;
import sparx1.itf.Iterator;
import sparx1.util.UncheckedException;
import sparx1.util.annotation.NotNull;

abstract class AbstractIterator<E, T extends AbstractIterator<E, T>> implements Iterator<E, T> {

  @Override
  public E first() {
    return next();
  }

  @Override
  public boolean isEmpty() {
    return !hasNext();
  }

  @Override
  public boolean isNotEmpty() {
    return hasNext();
  }

  @Override
  public boolean isOrdered() {
    return true;
  }

  @Override
  public boolean isTraversableAgain() {
    return false;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return toString(new StringBuilder(), ", ", "[", "]").toString();
  }

  @Override
  public @NotNull <A extends Appendable> A toString(final @NotNull A appendable) {
    try {
      while (hasNext()) {
        appendable.append(String.valueOf(next()));
      }
    } catch (final IOException e) {
      throw UncheckedException.throwUnchecked(e);
    }
    return appendable;
  }

  @Override
  public @NotNull <A extends Appendable> A toString(final @NotNull A appendable,
      final @NotNull String separator) {
    try {
      if (hasNext()) {
        appendable.append(String.valueOf(next()));
        while (hasNext()) {
          appendable.append(separator);
          appendable.append(String.valueOf(next()));
        }
      }
    } catch (final IOException e) {
      throw UncheckedException.throwUnchecked(e);
    }
    return appendable;
  }

  @Override
  public @NotNull <A extends Appendable> A toString(final @NotNull A appendable,
      final @NotNull String separator, final @NotNull String prefix, final @NotNull String suffix) {
    try {
      appendable.append(prefix);
      if (hasNext()) {
        appendable.append(String.valueOf(next()));
        while (hasNext()) {
          appendable.append(separator);
          appendable.append(String.valueOf(next()));
        }
      }
      appendable.append(suffix);
    } catch (final IOException e) {
      throw UncheckedException.throwUnchecked(e);
    }
    return appendable;
  }
}
