package sparx.concurrent;

import org.jetbrains.annotations.NotNull;
import sparx.concurrent.FutureGroup.Registration;

class DummyRegistration implements Registration {

  private final static DummyRegistration INSTANCE = new DummyRegistration();

  static @NotNull DummyRegistration instance() {
    return INSTANCE;
  }

  private DummyRegistration() {
  }

  @Override
  public void cancel() {
  }

  @Override
  public void onUncaughtError(@NotNull final Exception error) {
  }
}
