package sparx.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.jetbrains.annotations.NotNull;
import sparx.util.Requires;

public class PrintableException extends Exception {

  private volatile String message;

  PrintableException(@NotNull final Throwable cause) {
    super(Requires.notNull(cause, "cause"));
  }

  @Override
  public String toString() {
    if (message == null) {
      final StringWriter sw = new StringWriter();
      final PrintWriter pw = new PrintWriter(sw);
      printStackTrace(pw);
      message = sw.toString();
    }
    return message;
  }
}
