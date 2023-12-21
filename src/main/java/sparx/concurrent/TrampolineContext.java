package sparx.concurrent;

import org.jetbrains.annotations.NotNull;

public class TrampolineContext extends ExecutorContext {

  public static @NotNull TrampolineContext create() {
    return new TrampolineContext();
  }

  TrampolineContext() {
    super(Scheduler.trampoline());
  }
}
