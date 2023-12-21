package sparx.concurrent;

import org.jetbrains.annotations.NotNull;

public class UncheckedException extends RuntimeException {

  public static void throwUnchecked(final Throwable error) {
    if (error instanceof RuntimeException) {
      throw (RuntimeException) error;
    } else if (error instanceof Error) {
      throw (Error) error;
    }
    throw new UncheckedException(error);
  }

  public static @NotNull UncheckedException toUnchecked(final Throwable error) {
    return (error instanceof UncheckedException) ? (UncheckedException) error
        : new UncheckedException(error);
  }

  public UncheckedException(final Throwable cause) {
    super(cause);
  }
}
