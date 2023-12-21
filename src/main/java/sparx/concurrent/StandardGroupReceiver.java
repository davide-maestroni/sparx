package sparx.concurrent;

import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.FutureGroup.GroupReceiver;
import sparx.logging.Log;
import sparx.util.Requires;

class StandardGroupReceiver<V> implements GroupReceiver<V> {

  private final StreamingFuture<?> future;
  private final Receiver<V> receiver;

  StandardGroupReceiver(@NotNull final StreamingFuture<?> future,
      @NotNull final Receiver<V> receiver) {
    this.future = Requires.notNull(future, "future");
    this.receiver = Requires.notNull(receiver, "receiver");
  }

  @Override
  public boolean isSink() {
    return true;
  }

  @Override
  public void onUnsubscribe() {
  }

  @Override
  public void onUncaughtError(@NotNull final Exception error) {
    Log.err(GroupReceiver.class,
        "Uncaught exception, the throwing receiver will be automatically unsubscribed: %s",
        Log.printable(error));
    future.unsubscribe(receiver);
  }

  @Override
  public void close() {
    receiver.close();
  }

  @Override
  public boolean fail(@NotNull final Exception error) {
    return receiver.fail(error);
  }

  @Override
  public void set(final V value) {
    receiver.set(value);
  }

  @Override
  public void setBulk(@NotNull final Collection<V> values) {
    receiver.setBulk(values);
  }
}
