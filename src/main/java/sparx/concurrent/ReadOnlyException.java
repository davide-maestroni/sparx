package sparx.concurrent;

public class ReadOnlyException extends IllegalStateException {

  public ReadOnlyException() {
    super("the future is read-only");
  }
}
