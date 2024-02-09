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
package sparx.concurrent.logging;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.Receiver;
import sparx.logging.Log;
import sparx.logging.Log.LogLevel;
import sparx.logging.LogMessage;
import sparx.logging.LogTemplate;
import sparx.logging.LogTemplate.VariableResolver;

public class JavaLogPrinter implements Receiver<LogMessage> {

  private static final String PROP_PREFIX = JavaLogPrinter.class.getName();
  public static final String MESSAGE_TEMPLATE_PROP = PROP_PREFIX + ".template";
  public static final String VAR_PREFIX_PROP = PROP_PREFIX + ".varPrefix";
  public static final String VAR_SUFFIX_PROP = PROP_PREFIX + ".varSuffix";

  private static final String DEFAULT_TEMPLATE = "<%1$stag_name%2$s> %1$smessage%2$s";
  private static final HashMap<LogLevel, Level> LEVEL_MAPPING = new HashMap<LogLevel, Level>() {{
    put(LogLevel.DEBUG, Level.INFO);
    put(LogLevel.WARNING, Level.WARNING);
    put(LogLevel.ERROR, Level.SEVERE);
    put(LogLevel.DISABLED, Level.OFF);
  }};
  private static final Map<String, VariableResolver> RESOLVERS = LogTemplate.defaultResolvers();

  private final String template;
  private final String varPrefix;
  private final String varSuffix;

  // TODO: constructor

  public JavaLogPrinter(@NotNull final Properties properties) {
    varPrefix = properties.getProperty(VAR_PREFIX_PROP, "{");
    varSuffix = properties.getProperty(VAR_SUFFIX_PROP, "}");
    template = properties.getProperty(MESSAGE_TEMPLATE_PROP,
        String.format(DEFAULT_TEMPLATE, varPrefix, varSuffix));
  }

  @Override
  public void close() {
  }

  @Override
  public boolean fail(@NotNull final Exception error) {
    return false;
  }

  @Override
  public void set(@NotNull final LogMessage value) {
    Logger.getLogger(value.tagName()).log(recordFromMessage(value));
  }

  @Override
  public void setBulk(@NotNull final Collection<LogMessage> values) {
    for (final LogMessage message : values) {
      set(message);
    }
  }

  @SuppressWarnings("deprecation")
  private @NotNull LogRecord recordFromMessage(@NotNull final LogMessage message) {
    final Object tag = message.tag();
    final Thread thread = message.callingThread();
    final StackTraceElement caller = Log.getCallerElement(message.callStack());
    final long timestamp = message.timestamp();
    final LogRecord record = new LogRecord(LEVEL_MAPPING.get(message.level()),
        LogTemplate.fillTemplate(template, varPrefix, varSuffix, RESOLVERS, message));
    if (tag != null) {
      record.setSourceClassName(tag.getClass().getName());
    }
    if (thread != null) {
      record.setThreadID((int) thread.getId());
    }
    if (caller != null) {
      record.setSourceClassName(caller.getClassName());
      record.setSourceMethodName(caller.getMethodName());
    }
    if (timestamp > 0) {
      record.setMillis(timestamp);
    }
    return record;
  }
}
