package sparx.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.jetbrains.annotations.NotNull;
import sparx.util.Requires;

class PrintableThrowable implements Printable {

  private final Throwable wrapped;
  private volatile String message;

  PrintableThrowable(@NotNull final Throwable wrapped) {
    this.wrapped = Requires.notNull(wrapped, "wrapped");
  }

  @Override
  public String toString() {
    if (message == null) {
      final StringWriter sw = new StringWriter();
      final PrintWriter pw = new PrintWriter(sw);
      wrapped.printStackTrace(pw);
      message = sw.toString();
    }
    return message;
  }
}
