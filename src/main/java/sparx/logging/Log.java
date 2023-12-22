package sparx.logging;

import java.util.Arrays;
import java.util.HashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sparx.util.Requires;

public class Log {

  private static final DummyCollector DUMMY_COLLECTOR = new DummyCollector();
  private static final String LOG_CLASS_NAME = Log.class.getName();
  private static final HashSet<String> LOG_METHOD_NAMES = new HashSet<String>(
      Arrays.asList("dbg", "dbgInvocation", "wrn", "wrnInvocation", "err"));

  private static volatile LogCollector collector = DUMMY_COLLECTOR;
  private static volatile LogLevel level = LogLevel.WARNING;

  public static @NotNull LogCollector getCollector() {
    return collector;
  }

  public static void setCollector(@NotNull final LogCollector collector) {
    Log.collector = Requires.notNull(collector, "collector");
  }

  public static @NotNull LogLevel getLevel() {
    return level;
  }

  public static void setLevel(@NotNull final LogLevel level) {
    Log.level = Requires.notNull(level, "level");
  }

  public static @Nullable StackTraceElement getCallerElement(
      final StackTraceElement[] stackTrace) {
    if (stackTrace != null) {
      for (int i = 0; i < stackTrace.length - 1; ++i) {
        final StackTraceElement element = stackTrace[i];
        if (element.getClassName().equals(LOG_CLASS_NAME)
            && LOG_METHOD_NAMES.contains(element.getMethodName())) {
          return stackTrace[i + 1];
        }
      }
    }
    return null;
  }

  public static @NotNull Printable printable(@NotNull final Throwable t) {
    return new PrintableThrowable(t);
  }

  public static void reset() {
    collector = DUMMY_COLLECTOR;
    level = LogLevel.WARNING;
  }

  public static void dbg(final Object tag, final String msg) {
    if (level == LogLevel.DEBUG) {
      collector.log(LogLevel.DEBUG, tag, msg);
    }
  }

  public static void dbg(final Object tag, final String msgFmt, final Object firstArg) {
    if (level == LogLevel.DEBUG) {
      collector.log(LogLevel.DEBUG, tag, msgFmt, firstArg);
    }
  }

  public static void dbg(final Object tag, final String msgFmt, final Object firstArg,
      final Object secondArg) {
    if (level == LogLevel.DEBUG) {
      collector.log(LogLevel.DEBUG, tag, msgFmt, firstArg, secondArg);
    }
  }

  public static void dbg(final Object tag, final String msgFmt, final Object firstArg,
      final Object secondArg, final Object thirdArg) {
    if (level == LogLevel.DEBUG) {
      collector.log(LogLevel.DEBUG, tag, msgFmt, firstArg, secondArg, thirdArg);
    }
  }

  public static void dbg(final Object tag, final String msgFmt, final Object firstArg,
      final Object secondArg, final Object thirdArg, final Object fourthArg) {
    if (level == LogLevel.DEBUG) {
      collector.log(LogLevel.DEBUG, tag, msgFmt, firstArg, secondArg, thirdArg, fourthArg);
    }
  }

  public static void dbg(final Object tag, final String msgFmt, final Object firstArg,
      final Object secondArg, final Object thirdArg, final Object fourthArg,
      final Object fifthArg) {
    if (level == LogLevel.DEBUG) {
      collector.log(LogLevel.DEBUG, tag, msgFmt, firstArg, secondArg, thirdArg, fourthArg,
          fifthArg);
    }
  }

  public static void dbg(final Object tag, final String msgFmt, final Object firstArg,
      final Object secondArg, final Object thirdArg, final Object fourthArg,
      final Object fifthArg, final Object sixthArg) {
    if (level == LogLevel.DEBUG) {
      collector.log(LogLevel.DEBUG, tag, msgFmt, firstArg, secondArg, thirdArg, fourthArg,
          fifthArg, sixthArg);
    }
  }

  public static void dbg(final Object tag, final String msgFmt, final Object firstArg,
      final Object secondArg, final Object thirdArg, final Object fourthArg,
      final Object fifthArg, final Object sixthArg, final Object seventhArg) {
    if (level == LogLevel.DEBUG) {
      collector.log(LogLevel.DEBUG, tag, msgFmt, firstArg, secondArg, thirdArg, fourthArg,
          fifthArg, sixthArg, seventhArg);
    }
  }

  public static void dbg(final Object tag, final String msgFmt, final Object firstArg,
      final Object secondArg, final Object thirdArg, final Object fourthArg,
      final Object fifthArg, final Object sixthArg, final Object seventhArg,
      final Object eighthArg) {
    if (level == LogLevel.DEBUG) {
      collector.log(LogLevel.DEBUG, tag, msgFmt, firstArg, secondArg, thirdArg, fourthArg,
          fifthArg, sixthArg, seventhArg, eighthArg);
    }
  }

  public static void dbg(final Object tag, final String msgFmt, final Object firstArg,
      final Object secondArg, final Object thirdArg, final Object fourthArg,
      final Object fifthArg, final Object sixthArg, final Object seventhArg,
      final Object eighthArg, final Object ninthArg) {
    if (level == LogLevel.DEBUG) {
      collector.log(LogLevel.DEBUG, tag, msgFmt, firstArg, secondArg, thirdArg, fourthArg,
          fifthArg, sixthArg, seventhArg, eighthArg, ninthArg);
    }
  }

  public static void dbg(final Object tag, final String msgFmt, final Object firstArg,
      final Object secondArg, final Object thirdArg, final Object fourthArg,
      final Object fifthArg, final Object sixthArg, final Object seventhArg,
      final Object eighthArg, final Object ninthArg, final Object tenthArg) {
    if (level == LogLevel.DEBUG) {
      collector.log(LogLevel.DEBUG, tag, msgFmt, firstArg, secondArg, thirdArg, fourthArg,
          fifthArg, sixthArg, seventhArg, eighthArg, ninthArg, tenthArg);
    }
  }

  public static void dbg(final Object tag, final String msgFmt, final Object... args) {
    if (level == LogLevel.DEBUG) {
      collector.log(LogLevel.DEBUG, tag, msgFmt, args);
    }
  }

  public static void err(final Object tag, final String msg) {
    if (level.ordinal() <= LogLevel.ERROR.ordinal()) {
      collector.log(LogLevel.ERROR, tag, msg);
    }
  }

  public static void err(final Object tag, final String msgFmt, final Object firstArg) {
    if (level.ordinal() <= LogLevel.ERROR.ordinal()) {
      collector.log(LogLevel.ERROR, tag, msgFmt, firstArg);
    }
  }

  public static void err(final Object tag, final String msgFmt, final Object firstArg,
      final Object secondArg) {
    if (level.ordinal() <= LogLevel.ERROR.ordinal()) {
      collector.log(LogLevel.ERROR, tag, msgFmt, firstArg, secondArg);
    }
  }

  public static void err(final Object tag, final String msgFmt, final Object firstArg,
      final Object secondArg, final Object thirdArg) {
    if (level.ordinal() <= LogLevel.ERROR.ordinal()) {
      collector.log(LogLevel.ERROR, tag, msgFmt, firstArg, secondArg, thirdArg);
    }
  }

  public static void err(final Object tag, final String msgFmt, final Object firstArg,
      final Object secondArg, final Object thirdArg, final Object fourthArg) {
    if (level.ordinal() <= LogLevel.ERROR.ordinal()) {
      collector.log(LogLevel.ERROR, tag, msgFmt, firstArg, secondArg, thirdArg, fourthArg);
    }
  }

  public static void err(final Object tag, final String msgFmt, final Object firstArg,
      final Object secondArg, final Object thirdArg, final Object fourthArg,
      final Object fifthArg) {
    if (level.ordinal() <= LogLevel.ERROR.ordinal()) {
      collector.log(LogLevel.ERROR, tag, msgFmt, firstArg, secondArg, thirdArg, fourthArg,
          fifthArg);
    }
  }

  public static void err(final Object tag, final String msgFmt, final Object firstArg,
      final Object secondArg, final Object thirdArg, final Object fourthArg,
      final Object fifthArg, final Object sixthArg) {
    if (level.ordinal() <= LogLevel.ERROR.ordinal()) {
      collector.log(LogLevel.ERROR, tag, msgFmt, firstArg, secondArg, thirdArg, fourthArg,
          fifthArg, sixthArg);
    }
  }

  public static void err(final Object tag, final String msgFmt, final Object firstArg,
      final Object secondArg, final Object thirdArg, final Object fourthArg,
      final Object fifthArg, final Object sixthArg, final Object seventhArg) {
    if (level.ordinal() <= LogLevel.ERROR.ordinal()) {
      collector.log(LogLevel.ERROR, tag, msgFmt, firstArg, secondArg, thirdArg, fourthArg,
          fifthArg, sixthArg, seventhArg);
    }
  }

  public static void err(final Object tag, final String msgFmt, final Object firstArg,
      final Object secondArg, final Object thirdArg, final Object fourthArg,
      final Object fifthArg, final Object sixthArg, final Object seventhArg,
      final Object eighthArg) {
    if (level.ordinal() <= LogLevel.ERROR.ordinal()) {
      collector.log(LogLevel.ERROR, tag, msgFmt, firstArg, secondArg, thirdArg, fourthArg,
          fifthArg, sixthArg, seventhArg, eighthArg);
    }
  }

  public static void err(final Object tag, final String msgFmt, final Object firstArg,
      final Object secondArg, final Object thirdArg, final Object fourthArg,
      final Object fifthArg, final Object sixthArg, final Object seventhArg,
      final Object eighthArg, final Object ninthArg) {
    if (level.ordinal() <= LogLevel.ERROR.ordinal()) {
      collector.log(LogLevel.ERROR, tag, msgFmt, firstArg, secondArg, thirdArg, fourthArg,
          fifthArg, sixthArg, seventhArg, eighthArg, ninthArg);
    }
  }

  public static void err(final Object tag, final String msgFmt, final Object firstArg,
      final Object secondArg, final Object thirdArg, final Object fourthArg,
      final Object fifthArg, final Object sixthArg, final Object seventhArg,
      final Object eighthArg, final Object ninthArg, final Object tenthArg) {
    if (level.ordinal() <= LogLevel.ERROR.ordinal()) {
      collector.log(LogLevel.ERROR, tag, msgFmt, firstArg, secondArg, thirdArg, fourthArg,
          fifthArg, sixthArg, seventhArg, eighthArg, ninthArg, tenthArg);
    }
  }

  public static void err(final Object tag, final String msgFmt, final Object... args) {
    if (level.ordinal() <= LogLevel.ERROR.ordinal()) {
      collector.log(LogLevel.ERROR, tag, msgFmt, args);
    }
  }

  public static void errInvocation(final Object tag, final String name, final String method,
      final Exception e) {
    // TODO: remove
    if (level.ordinal() <= LogLevel.ERROR.ordinal()) {
      collector.log(LogLevel.ERROR, tag, "Failed to invoke %s '%s' method: %s", name, method,
          Log.printable(e));
    }
  }

  public static void wrn(final Object tag, final String msg) {
    if (level.ordinal() <= LogLevel.WARNING.ordinal()) {
      collector.log(LogLevel.WARNING, tag, msg);
    }
  }

  public static void wrn(final Object tag, final String msgFmt, final Object firstArg) {
    if (level.ordinal() <= LogLevel.WARNING.ordinal()) {
      collector.log(LogLevel.WARNING, tag, msgFmt, firstArg);
    }
  }

  public static void wrn(final Object tag, final String msgFmt, final Object firstArg,
      final Object secondArg) {
    if (level.ordinal() <= LogLevel.WARNING.ordinal()) {
      collector.log(LogLevel.WARNING, tag, msgFmt, firstArg, secondArg);
    }
  }

  public static void wrn(final Object tag, final String msgFmt, final Object firstArg,
      final Object secondArg, final Object thirdArg) {
    if (level.ordinal() <= LogLevel.WARNING.ordinal()) {
      collector.log(LogLevel.WARNING, tag, msgFmt, firstArg, secondArg, thirdArg);
    }
  }

  public static void wrn(final Object tag, final String msgFmt, final Object firstArg,
      final Object secondArg, final Object thirdArg, final Object fourthArg) {
    if (level.ordinal() <= LogLevel.WARNING.ordinal()) {
      collector.log(LogLevel.WARNING, tag, msgFmt, firstArg, secondArg, thirdArg, fourthArg);
    }
  }

  public static void wrn(final Object tag, final String msgFmt, final Object firstArg,
      final Object secondArg, final Object thirdArg, final Object fourthArg,
      final Object fifthArg) {
    if (level.ordinal() <= LogLevel.WARNING.ordinal()) {
      collector.log(LogLevel.WARNING, tag, msgFmt, firstArg, secondArg, thirdArg, fourthArg,
          fifthArg);
    }
  }

  public static void wrn(final Object tag, final String msgFmt, final Object firstArg,
      final Object secondArg, final Object thirdArg, final Object fourthArg,
      final Object fifthArg, final Object sixthArg) {
    if (level.ordinal() <= LogLevel.WARNING.ordinal()) {
      collector.log(LogLevel.WARNING, tag, msgFmt, firstArg, secondArg, thirdArg, fourthArg,
          fifthArg, sixthArg);
    }
  }

  public static void wrn(final Object tag, final String msgFmt, final Object firstArg,
      final Object secondArg, final Object thirdArg, final Object fourthArg,
      final Object fifthArg, final Object sixthArg, final Object seventhArg) {
    if (level.ordinal() <= LogLevel.WARNING.ordinal()) {
      collector.log(LogLevel.WARNING, tag, msgFmt, firstArg, secondArg, thirdArg, fourthArg,
          fifthArg, sixthArg, seventhArg);
    }
  }

  public static void wrn(final Object tag, final String msgFmt, final Object firstArg,
      final Object secondArg, final Object thirdArg, final Object fourthArg,
      final Object fifthArg, final Object sixthArg, final Object seventhArg,
      final Object eighthArg) {
    if (level.ordinal() <= LogLevel.WARNING.ordinal()) {
      collector.log(LogLevel.WARNING, tag, msgFmt, firstArg, secondArg, thirdArg, fourthArg,
          fifthArg, sixthArg, seventhArg, eighthArg);
    }
  }

  public static void wrn(final Object tag, final String msgFmt, final Object firstArg,
      final Object secondArg, final Object thirdArg, final Object fourthArg,
      final Object fifthArg, final Object sixthArg, final Object seventhArg,
      final Object eighthArg, final Object ninthArg) {
    if (level.ordinal() <= LogLevel.WARNING.ordinal()) {
      collector.log(LogLevel.WARNING, tag, msgFmt, firstArg, secondArg, thirdArg, fourthArg,
          fifthArg, sixthArg, seventhArg, eighthArg, ninthArg);
    }
  }

  public static void wrn(final Object tag, final String msgFmt, final Object firstArg,
      final Object secondArg, final Object thirdArg, final Object fourthArg,
      final Object fifthArg, final Object sixthArg, final Object seventhArg,
      final Object eighthArg, final Object ninthArg, final Object tenthArg) {
    if (level.ordinal() <= LogLevel.WARNING.ordinal()) {
      collector.log(LogLevel.WARNING, tag, msgFmt, firstArg, secondArg, thirdArg, fourthArg,
          fifthArg, sixthArg, seventhArg, eighthArg, ninthArg, tenthArg);
    }
  }

  public static void wrn(final Object tag, final String msgFmt, final Object... args) {
    if (level.ordinal() <= LogLevel.WARNING.ordinal()) {
      collector.log(LogLevel.WARNING, tag, msgFmt, args);
    }
  }

  public static void wrnInvocation(final Object tag, final String name, final String method,
      final Exception e) {
    // TODO: remove
    if (level.ordinal() <= LogLevel.WARNING.ordinal()) {
      collector.log(LogLevel.WARNING, tag, "Failed to invoke %s '%s' method: %s", name, method,
          Log.printable(e));
    }
  }

  private Log() {
  }

  public interface LogCollector {

    void log(@NotNull LogLevel level, Object tag, String msgFmt, Object... args);
  }

  public enum LogLevel {
    DEBUG,
    WARNING,
    ERROR,
    DISABLED
  }

  private static class DummyCollector implements LogCollector {

    @Override
    public void log(@NotNull final LogLevel level, final Object tag, final String msgFmt,
        final Object... args) {
    }
  }
}
