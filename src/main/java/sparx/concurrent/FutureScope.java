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
import org.jetbrains.annotations.Nullable;
import sparx.concurrent.Scheduler.Task;
import sparx.util.Require;

public class FutureScope {

  private static final DummyScope DUMMY_SCOPE = new DummyScope();
  private static final ThreadLocal<ArrayDeque<Scope>> localScopes = new ThreadLocal<ArrayDeque<Scope>>() {

    @Override
    protected ArrayDeque<Scope> initialValue() {
      final ArrayDeque<Scope> scopes = new ArrayDeque<Scope>();
      scopes.push(DUMMY_SCOPE);
      return scopes;
    }
  };

  public static @Nullable ExecutionContext currentContext() {
    return currentScope().executionContext();
  }

  @SuppressWarnings("unchecked")
  public static <T> T restoreObject(@NotNull final String name) {
    return (T) currentScope().restoreObject(name);
  }

  public static <T> void storeObject(@NotNull final String name, final T object) {
    currentScope().storeObject(name, object);
  }

  static @NotNull Scope currentScope() {
    return localScopes.get().peek();
  }

  static void popScope() {
    final ArrayDeque<Scope> scopes = localScopes.get();
    if (scopes.size() > 1) {
      scopes.pop();
    }
  }

  static void pushScope(@NotNull final Scope scope) {
    localScopes.get().push(Require.notNull(scope, "scope"));
  }

  interface Registration {

    void cancel();

    void onUncaughtError(@NotNull Exception error);
  }

  interface Scope {

    @NotNull <R, V extends R> ScopeReceiver<R> decorateReceiver(
        @NotNull StreamingFuture<V> future, @NotNull Scheduler scheduler,
        @NotNull Receiver<R> receiver);

    @Nullable ExecutionContext executionContext();

    @NotNull Registration registerFuture(@NotNull StreamingFuture<?> future);

    Object restoreObject(@NotNull String name);

    void runTask(@NotNull Task task);

    void storeObject(@NotNull String name, Object object);
  }

  interface ScopeReceiver<V> extends Receiver<V> {

    boolean isConsumer();

    void onReceiverError(@NotNull Exception error);

    void onUnsubscribe();
  }

  private static class DummyScope implements Scope {

    @Override
    public @NotNull <R, V extends R> ScopeReceiver<R> decorateReceiver(
        @NotNull final StreamingFuture<V> future, @NotNull final Scheduler scheduler,
        @NotNull final Receiver<R> receiver) {
      return new StandardScopeReceiver<R>(future, receiver);
    }

    @Override
    public @Nullable ExecutionContext executionContext() {
      return null;
    }

    @Override
    public @NotNull Registration registerFuture(@NotNull final StreamingFuture<?> future) {
      return DummyRegistration.instance();
    }

    @Override
    public Object restoreObject(@NotNull final String name) {
      throw new UnsupportedOperationException("restoreValue");
    }

    @Override
    public void runTask(@NotNull final Task task) {
      task.run();
    }

    @Override
    public void storeObject(@NotNull final String name, final Object object) {
      throw new UnsupportedOperationException("storeValue");
    }
  }
}
