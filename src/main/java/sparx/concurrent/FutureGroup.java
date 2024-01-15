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
import sparx.util.Requires;

public class FutureGroup {

  private static final DummyFutureGroup DUMMY_GROUP = new DummyFutureGroup();
  private static final ThreadLocal<ArrayDeque<Group>> localGroup = new ThreadLocal<ArrayDeque<Group>>() {

    @Override
    protected ArrayDeque<Group> initialValue() {
      final ArrayDeque<Group> groups = new ArrayDeque<Group>();
      groups.push(DUMMY_GROUP);
      return groups;
    }
  };

  public static @NotNull Group currentGroup() {
    return localGroup.get().peek();
  }

  static void popGroup() {
    final ArrayDeque<Group> groups = localGroup.get();
    if (groups.size() > 1) {
      groups.pop();
    }
  }

  static void pushGroup(@NotNull final Group group) {
    localGroup.get().push(Requires.notNull(group, "group"));
  }

  public interface Group {

    @Nullable ExecutionContext executionContext();

    @NotNull Registration onCreate(@NotNull StreamingFuture<?> future);

    @NotNull <R, V extends R> GroupReceiver<R> onSubscribe(
        @NotNull StreamingFuture<V> future, @NotNull Scheduler scheduler,
        @NotNull Receiver<R> receiver);

    void onTask(@NotNull Task task);

    Object restoreValue(@NotNull String name);

    void storeValue(@NotNull String name, Object value);
  }

  public interface GroupReceiver<V> extends Receiver<V> {

    boolean isSink();

    void onUnsubscribe();

    void onUncaughtError(@NotNull Exception error);
  }

  public interface Registration {

    void cancel();

    void onUncaughtError(@NotNull Exception error);
  }

  private static class DummyFutureGroup implements Group {

    @Override
    public @Nullable ExecutionContext executionContext() {
      return null;
    }

    @Override
    public @NotNull Registration onCreate(@NotNull final StreamingFuture<?> future) {
      return DummyRegistration.instance();
    }

    @Override
    public @NotNull <R, V extends R> FutureGroup.GroupReceiver<R> onSubscribe(
        @NotNull final StreamingFuture<V> future, @NotNull final Scheduler scheduler,
        @NotNull final Receiver<R> receiver) {
      return new StandardGroupReceiver<R>(future, receiver);
    }

    @Override
    public void onTask(@NotNull final Task task) {
      task.run();
    }

    @Override
    public Object restoreValue(@NotNull final String name) {
      throw new UnsupportedOperationException("restoreValue");
    }

    @Override
    public void storeValue(@NotNull final String name, final Object value) {
      throw new UnsupportedOperationException("storeValue");
    }
  }
}
