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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sparx.concurrent.FutureContext.Context;
import sparx.util.Require;

public class ContextLocal {

  private ContextLocal() {
  }

  public static @Nullable ExecutionContext currentContext() {
    return FutureContext.currentContext().executionContext();
  }

  public static @NotNull <V> LocalValue<V> getValue(@NotNull final String name) {
    final Context context = FutureContext.currentContext();
    @SuppressWarnings("unchecked") final LocalValue<V> value =
        (LocalValue<V>) context.restoreValue(Require.notNull(name, "name"));
    return (value == null) ? new LocalValue<V>(context, name) : value;
  }

  public static class LocalValue<V> {

    private final ExecutionContext executionContext;
    private final Context context;
    private final String name;

    private V value;

    private LocalValue(@NotNull final Context context, @NotNull final String name) {
      this.executionContext = context.executionContext();
      this.context = context;
      this.name = name;
    }

    public V get() {
      return value;
    }

    public void set(final V value) {
      if (FutureContext.currentContext().executionContext() != executionContext) {
        throw new InvalidContextException();
      }
      if (value == null) {
        context.storeValue(name, null);
      } else {
        this.value = value;
        context.storeValue(name, this);
      }
    }
  }
}
