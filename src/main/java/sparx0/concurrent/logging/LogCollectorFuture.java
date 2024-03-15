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
package sparx0.concurrent.logging;

import static java.lang.Boolean.parseBoolean;

import java.util.Properties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sparx0.concurrent.ReadOnlyFuture;
import sparx0.concurrent.VarFuture;
import sparx0.util.Require;
import sparx0.util.logging.Log.LogCollector;
import sparx0.util.logging.Log.LogLevel;
import sparx0.util.logging.LogMessage;
import sparx0.util.logging.LogMessageFactory;

public class LogCollectorFuture extends ReadOnlyFuture<LogMessage> implements LogCollector {

  private static final String PROP_PREFIX = LogCollectorFuture.class.getName();
  public static final String INCLUDE_CALL_STACK_PROP = PROP_PREFIX + ".includeStack";
  public static final String INCLUDE_CALLING_THREAD_PROP = PROP_PREFIX + ".includeThread";
  public static final String INCLUDE_TAG_PROP = PROP_PREFIX + ".includeTag";
  public static final String INCLUDE_TIMESTAMP_PROP = PROP_PREFIX + ".includeTimestamp";

  private final LogMessageFactory factory;

  public LogCollectorFuture(@NotNull final LogMessageFactory factory) {
    super(VarFuture.<LogMessage>create());
    this.factory = Require.notNull(factory, "factory");
  }

  public LogCollectorFuture(@NotNull final Properties properties) {
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
  public void log(@NotNull final LogLevel level, @Nullable final Object tag,
      @Nullable final String msgFmt, @Nullable final Object... args) {
    wrapped().set(factory.create(level, tag, msgFmt, args));
  }
}