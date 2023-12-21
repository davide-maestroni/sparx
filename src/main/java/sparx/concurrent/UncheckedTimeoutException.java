package sparx.concurrent;

public class UncheckedTimeoutException extends RuntimeException {

  public UncheckedTimeoutException() {
  }

  public UncheckedTimeoutException(Throwable cause) {
    super(cause);
  }
}
