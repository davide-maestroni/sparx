package sparx.concurrent;

import org.jetbrains.annotations.NotNull;
import sparx.concurrent.SignalFuture.Subscription;

class DummySubscription implements Subscription {

  private final static DummySubscription INSTANCE = new DummySubscription();

  static @NotNull DummySubscription instance() {
    return INSTANCE;
  }

  private DummySubscription() {
  }

  @Override
  public void cancel() {
  }
}
