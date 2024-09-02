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

import java.util.ArrayDeque;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext.Task;
import sparx.util.Require;

public abstract class ContextTask implements Task {

  private static final ThreadLocal<ArrayDeque<ExecutionContext>> contexts = new ThreadLocal<ArrayDeque<ExecutionContext>>() {
    @Override
    protected ArrayDeque<ExecutionContext> initialValue() {
      return new ArrayDeque<ExecutionContext>(2);
    }
  };

  private final ExecutionContext context;

  public ContextTask(@NotNull final ExecutionContext context) {
    this.context = Require.notNull(context, "context");
  }

  public static @NotNull ExecutionContext currentContext() {
    final ExecutionContext context = contexts.get().peek();
    if (context == null) {
      throw new IllegalStateException("code not running in context");
    }
    return context;
  }

  @Override
  public final void run() {
    final ArrayDeque<ExecutionContext> contexts = ContextTask.contexts.get();
    contexts.push(context);
    try {
      runWithContext();
    } finally {
      contexts.pop();
    }
  }

  @Override
  public @NotNull String taskID() {
    return "";
  }

  @Override
  public int weight() {
    return 1;
  }

  protected void runWithContext() {
  }
}
