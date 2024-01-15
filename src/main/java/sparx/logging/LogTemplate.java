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
package sparx.logging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import org.jetbrains.annotations.NotNull;

public class LogTemplate {

  public static final String UNKNOWN = "UNKNOWN";

  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
  private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat(
      "yyyy-MM-dd'T'HH:mm:ss.SSS");
  private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");

  static {
    final TimeZone utc = TimeZone.getTimeZone("UTC");
    DATE_FORMAT.setTimeZone(utc);
    DATETIME_FORMAT.setTimeZone(utc);
    TIME_FORMAT.setTimeZone(utc);
  }

  public static @NotNull Map<String, VariableResolver> defaultResolvers() {
    return new HashMap<String, VariableResolver>() {{
      put("tag", new CachedVariableResolver() {
        @Override
        String innerResolve(@NotNull final LogMessage message) {
          final Object tag = message.tag();
          return tag != null ? tag.toString() : UNKNOWN;
        }
      });
      put("tag_name", new DefaultVariableResolver() {
        @Override
        public String resolve(@NotNull final LogMessage message) {
          return message.tagName();
        }
      });
      put("level", new DefaultVariableResolver() {
        @Override
        public String resolve(@NotNull final LogMessage message) {
          return message.level().name();
        }
      });
      put("message", new DefaultVariableResolver() {
        @Override
        public String resolve(@NotNull final LogMessage message) {
          return message.formattedText();
        }
      });
      put("timestamp", new CachedVariableResolver() {
        @Override
        String innerResolve(@NotNull final LogMessage message) {
          return Long.toString(message.timestamp());
        }
      });
      put("iso_date", new CachedVariableResolver() {
        @Override
        String innerResolve(@NotNull final LogMessage message) {
          return DATE_FORMAT.format(new Date(message.timestamp()));
        }
      });
      put("iso_time", new CachedVariableResolver() {
        @Override
        String innerResolve(@NotNull final LogMessage message) {
          return TIME_FORMAT.format(new Date(message.timestamp()));
        }
      });
      put("iso_datetime", new CachedVariableResolver() {
        @Override
        String innerResolve(@NotNull final LogMessage message) {
          return DATETIME_FORMAT.format(new Date(message.timestamp()));
        }
      });
      put("thread", new CachedVariableResolver() {
        @Override
        String innerResolve(@NotNull final LogMessage message) {
          final Thread thread = message.callingThread();
          return thread != null ? thread.toString() : UNKNOWN;
        }
      });
      put("thread_name", new CachedVariableResolver() {
        @Override
        String innerResolve(@NotNull final LogMessage message) {
          final Thread thread = message.callingThread();
          return thread != null ? thread.getName() : UNKNOWN;
        }
      });
      put("thread_id", new CachedVariableResolver() {
        @Override
        String innerResolve(@NotNull final LogMessage message) {
          final Thread thread = message.callingThread();
          return thread != null ? Long.toString(thread.getId()) : UNKNOWN;
        }
      });
      put("thread_priority", new CachedVariableResolver() {
        @Override
        String innerResolve(@NotNull final LogMessage message) {
          final Thread thread = message.callingThread();
          return thread != null ? Integer.toString(thread.getPriority()) : UNKNOWN;
        }
      });
      put("thread_daemon", new CachedVariableResolver() {
        @Override
        String innerResolve(@NotNull final LogMessage message) {
          final Thread thread = message.callingThread();
          return thread != null ? Boolean.toString(thread.isDaemon()) : UNKNOWN;
        }
      });
      put("thread_group", new CachedVariableResolver() {
        @Override
        String innerResolve(@NotNull final LogMessage message) {
          final Thread thread = message.callingThread();
          if (thread != null) {
            final ThreadGroup group = thread.getThreadGroup();
            return group != null ? group.getName() : UNKNOWN;
          }
          return UNKNOWN;
        }
      });
      put("call_stack", new CachedVariableResolver() {
        @Override
        String innerResolve(@NotNull final LogMessage message) {
          final StackTraceElement[] stack = message.callStack();
          if (stack != null) {
            final StringBuilder builder = new StringBuilder();
            for (final StackTraceElement element : stack) {
              if (builder.length() > 0) {
                builder.append("\n");
              }
              builder.append(element.toString());
            }
            return builder.toString();
          }
          return UNKNOWN;
        }
      });
      put("call_class", new CachedVariableResolver() {
        @Override
        String innerResolve(@NotNull final LogMessage message) {
          final StackTraceElement caller = Log.getCallerElement(message.callStack());
          if (caller != null) {
            return caller.getClassName();
          }
          return UNKNOWN;
        }
      });
      put("call_file", new CachedVariableResolver() {
        @Override
        String innerResolve(@NotNull final LogMessage message) {
          final StackTraceElement caller = Log.getCallerElement(message.callStack());
          if (caller != null) {
            return caller.getFileName();
          }
          return UNKNOWN;
        }
      });
      put("call_line", new CachedVariableResolver() {
        @Override
        String innerResolve(@NotNull final LogMessage message) {
          final StackTraceElement caller = Log.getCallerElement(message.callStack());
          if (caller != null) {
            return Integer.toString(caller.getLineNumber());
          }
          return UNKNOWN;
        }
      });
      put("call_method", new CachedVariableResolver() {
        @Override
        String innerResolve(@NotNull final LogMessage message) {
          final StackTraceElement caller = Log.getCallerElement(message.callStack());
          if (caller != null) {
            return caller.getMethodName();
          }
          return UNKNOWN;
        }
      });
    }};
  }

  public static @NotNull String fillTemplate(@NotNull final String template,
      @NotNull final String varPrefix, @NotNull final String varSuffix,
      @NotNull final Map<String, VariableResolver> resolvers, @NotNull final LogMessage message) {
    final StringBuilder builder = new StringBuilder(template.length());
    try {
      int offset = 0;
      while (true) {
        final int start = template.indexOf(varPrefix, offset);
        if (start < 0) {
          break;
        }
        final int end = template.indexOf(varSuffix, start + varPrefix.length());
        if (end < 0) {
          break;
        }
        final VariableResolver resolver = resolvers.get(
            template.substring(start + varPrefix.length(), end));
        if (resolver != null) {
          builder.append(template, offset, start).append(resolver.resolve(message));
          offset = end + varSuffix.length();
        } else {
          offset = end + varSuffix.length();
          builder.append(template, start, offset);
        }
      }
    } finally {
      for (final VariableResolver resolver : resolvers.values()) {
        resolver.reset();
      }
    }
    return builder.toString();
  }

  private LogTemplate() {
  }

  public interface VariableResolver {

    void reset();

    String resolve(@NotNull LogMessage message);
  }

  private static abstract class CachedVariableResolver implements VariableResolver {

    private String cached;

    @Override
    public void reset() {
      cached = null;
    }

    @Override
    public String resolve(@NotNull final LogMessage message) {
      if (cached == null) {
        cached = innerResolve(message);
      }
      return cached;
    }

    abstract String innerResolve(@NotNull LogMessage message);
  }

  private static abstract class DefaultVariableResolver implements VariableResolver {

    @Override
    public void reset() {
    }
  }
}
