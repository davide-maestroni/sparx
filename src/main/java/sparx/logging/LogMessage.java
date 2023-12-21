package sparx.logging;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface LogMessage {

  @Nullable
  Thread callingThread();

  @Nullable
  StackTraceElement[] callStack();

  @NotNull
  String formattedText();

  @NotNull
  Log.LogLevel level();

  @Nullable
  Object tag();

  @NotNull
  String tagName();

  long timestamp();
}
