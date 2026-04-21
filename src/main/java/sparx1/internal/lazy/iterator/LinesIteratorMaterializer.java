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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import sparx1.util.UncheckedException;
import sparx1.util.annotation.NotNull;
import sparx1.util.annotation.Positive;

public class LinesIteratorMaterializer extends AbstractIteratorMaterializer<String> {

  private final BufferedReader reader;

  private boolean hasNext;
  private String next;
  private int pos;

  public LinesIteratorMaterializer(final @NotNull BufferedReader reader) {
    this.reader = reader;
  }

  @Override
  public int currentKnownSize() {
    return -1;
  }

  @Override
  public boolean isSizeKnown() {
    return false;
  }

  @Override
  public boolean materializeHasNext() {
    if (hasNext) {
      return true;
    }
    try {
      next = reader.readLine();
    } catch (final IOException e) {
      throw UncheckedException.toUnchecked(e);
    }
    hasNext = next != null;
    return hasNext;
  }

  @Override
  public String materializeNext() {
    if (!materializeHasNext()) {
      throw new NoSuchElementException(Integer.toString(pos - 1));
    }
    hasNext = false;
    final String next = this.next;
    this.next = null;
    return next;
  }

  @Override
  public int materializeSkip(final @Positive int count) {
    final int skipped = super.materializeSkip(count);
    pos += skipped;
    return skipped;
  }
}
