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

public class FutureContext {

  private static final DummyFutureContext DUMMY_CONTEXT = new DummyFutureContext();
  private static final ThreadLocal<ArrayDeque<Context>> localContext = new ThreadLocal<ArrayDeque<Context>>() {

    @Override
    protected ArrayDeque<Context> initialValue() {
      final ArrayDeque<Context> contexts = new ArrayDeque<Context>();
      contexts.push(DUMMY_CONTEXT);
      return contexts;
    }
  };

  public static @NotNull Context currentContext() {
    return localContext.get().peek();
  }

  static void popContext() {
    final ArrayDeque<Context> contexts = localContext.get();
    if (contexts.size() > 1) {
      contexts.pop();
    }
  }

  static void pushContext(@NotNull final Context context) {
    localContext.get().push(Require.notNull(context, "context"));
  }

  public interface Context {

    @NotNull <R, V extends R> ContextReceiver<R> decorateReceiver(
        @NotNull StreamingFuture<V> future, @NotNull Scheduler scheduler,
        @NotNull Receiver<R> receiver);

    @Nullable ExecutionContext executionContext();

    @NotNull Registration registerFuture(@NotNull StreamingFuture<?> future);

    Object restoreValue(@NotNull String name);

    void runTask(@NotNull Task task);

    void storeValue(@NotNull String name, Object value);
  }

  public interface ContextReceiver<V> extends Receiver<V> {

    boolean isConsumer();

    void onReceiverError(@NotNull Exception error);

    void onUnsubscribe();
  }

  public interface Registration {

    void cancel();

    void onUncaughtError(@NotNull Exception error);
  }

  private static class DummyFutureContext implements Context {

    @Override
    public @NotNull <R, V extends R> ContextReceiver<R> decorateReceiver(
        @NotNull final StreamingFuture<V> future, @NotNull final Scheduler scheduler,
        @NotNull final Receiver<R> receiver) {
      return new StandardContextReceiver<R>(future, receiver);
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
    public Object restoreValue(@NotNull final String name) {
      throw new UnsupportedOperationException("restoreValue");
    }

    @Override
    public void runTask(@NotNull final Task task) {
      task.run();
    }

    @Override
    public void storeValue(@NotNull final String name, final Object value) {
      throw new UnsupportedOperationException("storeValue");
    }
  }
}
