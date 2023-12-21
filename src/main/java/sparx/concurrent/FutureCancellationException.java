package sparx.concurrent;

public class FutureCancellationException extends IllegalStateException {

  private final boolean mayInterruptIfRunning;

  FutureCancellationException(final boolean mayInterruptIfRunning) {
    this.mayInterruptIfRunning = mayInterruptIfRunning;
  }

  public boolean mayInterruptIfRunning() {
    return mayInterruptIfRunning;
  }
}
