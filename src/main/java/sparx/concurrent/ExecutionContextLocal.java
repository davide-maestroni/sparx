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
package sparx.concurrent;

import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.FutureContext.Context;
import sparx.util.Require;

public class ExecutionContextLocal<T> {

  private final String contextLocalName;

  public ExecutionContextLocal() {
    contextLocalName = "sparx.context.local." + UUID.randomUUID();
  }

  public ExecutionContextLocal(@NotNull final String name) {
    contextLocalName = Require.notNull(name, "name");
  }

  @SuppressWarnings("unchecked")
  public T get() {
    final Context context = FutureContext.currentContext();
    if (context.executionContext() == null) {
      throw new IllegalStateException("no execution context");
    }
    return (T) context.restoreValue(contextLocalName);
  }

  public @NotNull String name() {
    return contextLocalName;
  }

  public void set(final T value) {
    final Context context = FutureContext.currentContext();
    if (context.executionContext() == null) {
      throw new IllegalStateException("no execution context");
    }
    context.storeValue(contextLocalName, value);
  }
}
