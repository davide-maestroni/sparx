package sparx.concurrent;

public class UncheckedInterruptedException extends RuntimeException {

  public UncheckedInterruptedException(final InterruptedException cause) {
    super(cause);
    Thread.currentThread().interrupt();  //set the flag back to true
  }
}
