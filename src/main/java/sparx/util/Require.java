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
package sparx.util;

import java.util.Collection;
import org.jetbrains.annotations.NotNull;

public class Require {

  private Require() {
  }

  public static @NotNull <E, T extends Collection<E>> T notContainsNull(final T ref,
      final String name) {
    if (ref == null) {
      throw new NullPointerException("'" + name + "' must not be null!");
    }
    if (ref.contains(null)) {
      throw new NullPointerException("'" + name + "' must not contain null elements!");
    }
    return ref;
  }

  public static @NotNull <T> T notNull(final T ref, final String name) {
    if (ref == null) {
      throw new NullPointerException("'" + name + "' must not be null!");
    }
    return ref;
  }

  public static int positive(final int value, final String name) {
    if (value < 1) {
      throw new IllegalArgumentException("'" + name + "' must be positive, but is " + value);
    }
    return value;
  }

  public static long positive(final long value, final String name) {
    if (value < 1) {
      throw new IllegalArgumentException("'" + name + "' must be positive, but is " + value);
    }
    return value;
  }
}
