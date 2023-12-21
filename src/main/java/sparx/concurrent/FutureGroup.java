package sparx.concurrent;

import java.util.ArrayDeque;
import org.jetbrains.annotations.NotNull;
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

    @NotNull Registration onCreate(@NotNull StreamingFuture<?> future);

    @NotNull <R, V extends R> GroupReceiver<R> onSubscribe(
        @NotNull StreamingFuture<V> future, @NotNull Scheduler scheduler,
        @NotNull Receiver<R> receiver);

    void onTask(@NotNull Task task);
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
  }
}
