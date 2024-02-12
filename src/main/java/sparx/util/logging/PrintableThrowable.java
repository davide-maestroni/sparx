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
package sparx.util.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.jetbrains.annotations.NotNull;
import sparx.util.Require;

class PrintableThrowable implements Printable {

  private final Throwable wrapped;
  private volatile String message;

  PrintableThrowable(@NotNull final Throwable wrapped) {
    this.wrapped = Require.notNull(wrapped, "wrapped");
  }

  @Override
  public String toString() {
    if (message == null) {
      final StringWriter sw = new StringWriter();
      final PrintWriter pw = new PrintWriter(sw);
      wrapped.printStackTrace(pw);
      message = sw.toString();
    }
    return message;
  }
}
