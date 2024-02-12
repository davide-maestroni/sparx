/*
 * Copyright 2024 Davide Maestroni
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sparx.util.logging;

import java.util.Arrays;
import java.util.HashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sparx.util.Require;

///////////////////////////////////////////////
// WARNING: GENERATED CODE - DO NOT MODIFY!!
// - templating engine: Mustache 0.9.11
// - template path: template/logging/Log.mustache
///////////////////////////////////////////////

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
    Log.collector = Require.notNull(collector, "collector");
  }

  public static @NotNull LogLevel getLevel() {
    return level;
  }

  public static void setLevel(@NotNull final LogLevel level) {
    Log.level = Require.notNull(level, "level");
  }

  public static @Nullable StackTraceElement getCallerElement(
      @Nullable final StackTraceElement[] stackTrace) {
    if (stackTrace != null) {
      for (int i = 0; i < stackTrace.length - 1; ++i) {
        final StackTraceElement element = stackTrace[i];
        if ((element != null) && element.getClassName().equals(LOG_CLASS_NAME)
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

  public static void dbg(@Nullable final Object tag, @Nullable final String msg) {
    if (level == LogLevel.DEBUG) {
      collector.log(LogLevel.DEBUG, tag, msg);
    }
  }

  public static void dbg(@Nullable final Object tag, @Nullable final String msgFmt,
      final Object firstArg) {
    if (level == LogLevel.DEBUG) {
      collector.log(LogLevel.DEBUG, tag, msgFmt, firstArg);
    }
  }

  public static void dbg(@Nullable final Object tag, @Nullable final String msgFmt,
      final Object firstArg, final Object secondArg) {
    if (level == LogLevel.DEBUG) {
      collector.log(LogLevel.DEBUG, tag, msgFmt, firstArg, secondArg);
    }
  }

  public static void dbg(@Nullable final Object tag, @Nullable final String msgFmt,
      final Object firstArg, final Object secondArg, final Object thirdArg) {
    if (level == LogLevel.DEBUG) {
      collector.log(LogLevel.DEBUG, tag, msgFmt, firstArg, secondArg, thirdArg);
    }
  }

  public static void dbg(@Nullable final Object tag, @Nullable final String msgFmt,
      final Object firstArg, final Object secondArg, final Object thirdArg, final Object fourthArg) {
    if (level == LogLevel.DEBUG) {
      collector.log(LogLevel.DEBUG, tag, msgFmt, firstArg, secondArg, thirdArg, fourthArg);
    }
  }

  public static void dbg(@Nullable final Object tag, @Nullable final String msgFmt,
      final Object firstArg, final Object secondArg, final Object thirdArg, final Object fourthArg, final Object fifthArg) {
    if (level == LogLevel.DEBUG) {
      collector.log(LogLevel.DEBUG, tag, msgFmt, firstArg, secondArg, thirdArg, fourthArg, fifthArg);
    }
  }

  public static void dbg(@Nullable final Object tag, @Nullable final String msgFmt,
      @Nullable final Object... args) {
    if (level == LogLevel.DEBUG) {
      collector.log(LogLevel.DEBUG, tag, msgFmt, args);
    }
  }

  public static void err(@Nullable final Object tag, @Nullable final String msg) {
    if (level.ordinal() <= LogLevel.ERROR.ordinal()) {
      collector.log(LogLevel.ERROR, tag, msg);
    }
  }

  public static void err(@Nullable final Object tag, @Nullable final String msgFmt,
      final Object firstArg) {
    if (level.ordinal() <= LogLevel.ERROR.ordinal()) {
      collector.log(LogLevel.ERROR, tag, msgFmt, firstArg);
    }
  }

  public static void err(@Nullable final Object tag, @Nullable final String msgFmt,
      final Object firstArg, final Object secondArg) {
    if (level.ordinal() <= LogLevel.ERROR.ordinal()) {
      collector.log(LogLevel.ERROR, tag, msgFmt, firstArg, secondArg);
    }
  }

  public static void err(@Nullable final Object tag, @Nullable final String msgFmt,
      final Object firstArg, final Object secondArg, final Object thirdArg) {
    if (level.ordinal() <= LogLevel.ERROR.ordinal()) {
      collector.log(LogLevel.ERROR, tag, msgFmt, firstArg, secondArg, thirdArg);
    }
  }

  public static void err(@Nullable final Object tag, @Nullable final String msgFmt,
      final Object firstArg, final Object secondArg, final Object thirdArg, final Object fourthArg) {
    if (level.ordinal() <= LogLevel.ERROR.ordinal()) {
      collector.log(LogLevel.ERROR, tag, msgFmt, firstArg, secondArg, thirdArg, fourthArg);
    }
  }

  public static void err(@Nullable final Object tag, @Nullable final String msgFmt,
      final Object firstArg, final Object secondArg, final Object thirdArg, final Object fourthArg, final Object fifthArg) {
    if (level.ordinal() <= LogLevel.ERROR.ordinal()) {
      collector.log(LogLevel.ERROR, tag, msgFmt, firstArg, secondArg, thirdArg, fourthArg, fifthArg);
    }
  }

  public static void err(@Nullable final Object tag, @Nullable final String msgFmt,
      @Nullable final Object... args) {
    if (level.ordinal() <= LogLevel.ERROR.ordinal()) {
      collector.log(LogLevel.ERROR, tag, msgFmt, args);
    }
  }

  public static void wrn(@Nullable final Object tag, @Nullable final String msg) {
    if (level.ordinal() <= LogLevel.WARNING.ordinal()) {
      collector.log(LogLevel.WARNING, tag, msg);
    }
  }

  public static void wrn(@Nullable final Object tag, @Nullable final String msgFmt,
      final Object firstArg) {
    if (level.ordinal() <= LogLevel.WARNING.ordinal()) {
      collector.log(LogLevel.WARNING, tag, msgFmt, firstArg);
    }
  }

  public static void wrn(@Nullable final Object tag, @Nullable final String msgFmt,
      final Object firstArg, final Object secondArg) {
    if (level.ordinal() <= LogLevel.WARNING.ordinal()) {
      collector.log(LogLevel.WARNING, tag, msgFmt, firstArg, secondArg);
    }
  }

  public static void wrn(@Nullable final Object tag, @Nullable final String msgFmt,
      final Object firstArg, final Object secondArg, final Object thirdArg) {
    if (level.ordinal() <= LogLevel.WARNING.ordinal()) {
      collector.log(LogLevel.WARNING, tag, msgFmt, firstArg, secondArg, thirdArg);
    }
  }

  public static void wrn(@Nullable final Object tag, @Nullable final String msgFmt,
      final Object firstArg, final Object secondArg, final Object thirdArg, final Object fourthArg) {
    if (level.ordinal() <= LogLevel.WARNING.ordinal()) {
      collector.log(LogLevel.WARNING, tag, msgFmt, firstArg, secondArg, thirdArg, fourthArg);
    }
  }

  public static void wrn(@Nullable final Object tag, @Nullable final String msgFmt,
      final Object firstArg, final Object secondArg, final Object thirdArg, final Object fourthArg, final Object fifthArg) {
    if (level.ordinal() <= LogLevel.WARNING.ordinal()) {
      collector.log(LogLevel.WARNING, tag, msgFmt, firstArg, secondArg, thirdArg, fourthArg, fifthArg);
    }
  }

  public static void wrn(@Nullable final Object tag, @Nullable final String msgFmt,
      @Nullable final Object... args) {
    if (level.ordinal() <= LogLevel.WARNING.ordinal()) {
      collector.log(LogLevel.WARNING, tag, msgFmt, args);
    }
  }

  private Log() {
  }

  public interface LogCollector {

    void log(@NotNull LogLevel level, @Nullable Object tag, @Nullable String msgFmt,
        @Nullable Object... args);
  }

  public enum LogLevel {
    DEBUG,
    WARNING,
    ERROR,
    DISABLED
  }

  private static class DummyCollector implements LogCollector {

    @Override
    public void log(@NotNull final LogLevel level, @Nullable final Object tag,
        @Nullable final String msgFmt, @Nullable final Object... args) {
    }
  }
}
