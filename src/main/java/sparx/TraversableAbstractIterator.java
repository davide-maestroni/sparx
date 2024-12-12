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

import java.io.IOException;
import java.util.Iterator;
import org.jetbrains.annotations.NotNull;
import sparx.itf.Traversable;
import sparx.util.UncheckedException;

public abstract class TraversableAbstractIterator<E> implements Iterator<E>, Traversable<E> {

  @Override
  public @NotNull <A extends Appendable> A toString(@NotNull final A appendable) {
    try {
      while (hasNext()) {
        final E element = next();
        appendable.append(element == null ? null : element.toString());
      }
    } catch (final IOException e) {
      throw UncheckedException.throwUnchecked(e);
    }
    return appendable;
  }

  @Override
  public @NotNull <A extends Appendable> A toString(@NotNull final A appendable,
      @NotNull final String separator) {
    try {
      if (hasNext()) {
        final E element = next();
        appendable.append(element == null ? null : element.toString());
        while (hasNext()) {
          appendable.append(separator);
          appendable.append(element == null ? null : element.toString());
        }
      }
    } catch (final IOException e) {
      throw UncheckedException.throwUnchecked(e);
    }
    return appendable;
  }

  @Override
  public @NotNull <A extends Appendable> A toString(@NotNull final A appendable,
      @NotNull final String separator, @NotNull final String prefix, @NotNull final String suffix) {
    try {
      if (hasNext()) {
        final E element = next();
        appendable.append(prefix);
        appendable.append(element == null ? null : element.toString());
        while (hasNext()) {
          appendable.append(separator);
          appendable.append(element == null ? null : element.toString());
        }
        appendable.append(suffix);
      }
    } catch (final IOException e) {
      throw UncheckedException.throwUnchecked(e);
    }
    return appendable;
  }
}
