package sparx.concurrent;

import static java.lang.Boolean.parseBoolean;

import java.util.Properties;
import org.jetbrains.annotations.NotNull;
import sparx.logging.Log.LogCollector;
import sparx.logging.Log.LogLevel;
import sparx.logging.LogMessage;
import sparx.logging.LogMessageFactory;

public class FutureLogCollector extends ReadOnlyFuture<LogMessage> implements LogCollector {

  private static final String PROP_PREFIX = FutureLogCollector.class.getName();
  public static final String INCLUDE_CALL_STACK_PROP = PROP_PREFIX + ".includeStack";
  public static final String INCLUDE_CALLING_THREAD_PROP = PROP_PREFIX + ".includeThread";
  public static final String INCLUDE_TAG_PROP = PROP_PREFIX + ".includeTag";
  public static final String INCLUDE_TIMESTAMP_PROP = PROP_PREFIX + ".includeTimestamp";

  private final LogMessageFactory factory;

  public FutureLogCollector(@NotNull final Properties properties) {
    super(VarFuture.<LogMessage>create());
    factory = new LogMessageFactory()
        .includeTag(
            parseBoolean(properties.getProperty(INCLUDE_TAG_PROP, Boolean.FALSE.toString())))
        .includeCallingThread(parseBoolean(
            properties.getProperty(INCLUDE_CALLING_THREAD_PROP, Boolean.TRUE.toString())))
        .includeCallStack(
            parseBoolean(properties.getProperty(INCLUDE_CALL_STACK_PROP, Boolean.FALSE.toString())))
        .includeTimestamp(
            parseBoolean(properties.getProperty(INCLUDE_TIMESTAMP_PROP, Boolean.TRUE.toString())));
  }

  @Override
  public void log(@NotNull final LogLevel level, final Object tag, final String msgFmt,
      final Object... args) {
    super.wrapped().set(factory.create(level, tag, msgFmt, args));
  }

  @Override
  protected @NotNull StreamingFuture<LogMessage> wrapped() {
    return this;
  }
}
