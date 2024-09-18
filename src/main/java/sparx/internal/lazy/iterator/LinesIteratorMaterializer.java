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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;
import sparx.util.UncheckedException;

public class LinesIteratorMaterializer extends AutoSkipIteratorMaterializer<String> {

  private final BufferedReader reader;

  private boolean hasNext;
  private String next;
  private int pos;

  public LinesIteratorMaterializer(@NotNull final BufferedReader reader) {
    this.reader = reader;
  }

  @Override
  public int knownSize() {
    return -1;
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
  public int materializeSkip(final int count) {
    final int skipped = super.materializeSkip(count);
    pos += skipped;
    return skipped;
  }
}
