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

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sparx.logging.Log.LogLevel;

public class LogMessageFactory {

  private static final LogBuilder<?> NULL_BUILDER = new LogBuilder<Object>() {
    @Override
    public String build(final Object tag) {
      return null;
    }
  };
  private static final LogBuilder<StackTraceElement[]> STACK_BUILDER = new LogBuilder<StackTraceElement[]>() {
    @Override
    public StackTraceElement[] build(final Object tag) {
      return Thread.currentThread().getStackTrace();
    }
  };
  private static final LogBuilder<WeakReference<Object>> TAG_BUILDER = new LogBuilder<WeakReference<Object>>() {
    @Override
    public WeakReference<Object> build(final Object tag) {
      return new WeakReference<Object>(tag);
    }
  };
  private static final LogBuilder<Thread> THREAD_BUILDER = new LogBuilder<Thread>() {
    @Override
    public Thread build(final Object tag) {
      return Thread.currentThread();
    }
  };
  private static final LogBuilder<Long> TIME_BUILDER = new LogBuilder<Long>() {
    @Override
    public Long build(final Object tag) {
      return System.currentTimeMillis();
    }
  };

  private static final HashMap<Class<?>, NameExtractor> EXTRACTORS = new HashMap<Class<?>, NameExtractor>() {{
    final NameExtractor toStringExtractor = new NameExtractor() {
      @Override
      public String getName(Object tag) {
        return tag.toString();
      }
    };
    put(Class.class, new NameExtractor() {
      @Override
      public String getName(Object tag) {
        return ((Class<?>) tag).getName();
      }
    });
    put(Thread.class, new NameExtractor() {
      @Override
      public String getName(Object tag) {
        return ((Thread) tag).getName();
      }
    });
    put(Byte.class, toStringExtractor);
    put(Short.class, toStringExtractor);
    put(Integer.class, toStringExtractor);
    put(Long.class, toStringExtractor);
    put(Float.class, toStringExtractor);
    put(Double.class, toStringExtractor);
    put(BigInteger.class, toStringExtractor);
    put(BigDecimal.class, toStringExtractor);
    put(Number.class, toStringExtractor);
    put(Character.class, toStringExtractor);
    put(CharSequence.class, toStringExtractor);
    put(String.class, toStringExtractor);
  }};

  @SuppressWarnings("unchecked")
  private static <T> LogBuilder<T> nullBuilder() {
    return (LogBuilder<T>) NULL_BUILDER;
  }

  private final LogBuilder<StackTraceElement[]> stackBuilder;
  private final LogBuilder<WeakReference<Object>> tagBuilder;
  private final LogBuilder<Thread> threadBuilder;
  private final LogBuilder<Long> timestampBuilder;

  public LogMessageFactory() {
    this(LogMessageFactory.<WeakReference<Object>>nullBuilder(),
        LogMessageFactory.<StackTraceElement[]>nullBuilder(),
        LogMessageFactory.<Thread>nullBuilder(), TIME_BUILDER);
  }

  private LogMessageFactory(@NotNull final LogBuilder<WeakReference<Object>> tagBuilder,
      @NotNull final LogBuilder<StackTraceElement[]> stackBuilder,
      @NotNull final LogBuilder<Thread> threadBuilder,
      @NotNull final LogBuilder<Long> timestampBuilder) {
    this.tagBuilder = tagBuilder;
    this.stackBuilder = stackBuilder;
    this.threadBuilder = threadBuilder;
    this.timestampBuilder = timestampBuilder;
  }

  public @NotNull LogMessageFactory includeCallingThread(final boolean include) {
    if (include) {
      if (threadBuilder != THREAD_BUILDER) {
        return new LogMessageFactory(tagBuilder, stackBuilder, THREAD_BUILDER, timestampBuilder);
      }
    } else if (threadBuilder == THREAD_BUILDER) {
      return new LogMessageFactory(tagBuilder, stackBuilder,
          LogMessageFactory.<Thread>nullBuilder(), timestampBuilder);
    }
    return this;
  }

  public @NotNull LogMessageFactory includeCallStack(final boolean include) {
    if (include) {
      if (stackBuilder != STACK_BUILDER) {
        return new LogMessageFactory(tagBuilder, STACK_BUILDER, threadBuilder, timestampBuilder);
      }
    } else if (stackBuilder == STACK_BUILDER) {
      return new LogMessageFactory(tagBuilder,
          LogMessageFactory.<StackTraceElement[]>nullBuilder(), threadBuilder, timestampBuilder);
    }
    return this;
  }

  public @NotNull LogMessageFactory includeTag(final boolean include) {
    if (include) {
      if (tagBuilder != TAG_BUILDER) {
        return new LogMessageFactory(TAG_BUILDER, stackBuilder, threadBuilder, timestampBuilder);
      }
    } else if (tagBuilder == TAG_BUILDER) {
      return new LogMessageFactory(LogMessageFactory.<WeakReference<Object>>nullBuilder(),
          stackBuilder, threadBuilder, timestampBuilder);
    }
    return this;
  }

  public @NotNull LogMessageFactory includeTimestamp(final boolean include) {
    if (include) {
      if (timestampBuilder != TIME_BUILDER) {
        return new LogMessageFactory(tagBuilder, stackBuilder, threadBuilder, TIME_BUILDER);
      }
    } else if (timestampBuilder == TIME_BUILDER) {
      return new LogMessageFactory(tagBuilder, stackBuilder, threadBuilder,
          LogMessageFactory.<Long>nullBuilder());
    }
    return this;
  }

  public @NotNull LogMessage create(@NotNull final LogLevel level, @Nullable final Object tag,
      @Nullable final String msgFmt, @Nullable final Object... args) {
    final String tagName;
    if (tag == null) {
      tagName = "UNKNOWN";
    } else {
      final NameExtractor extractor = EXTRACTORS.get(tag.getClass());
      if (extractor == null) {
        final Class<?> tagClass = tag.getClass();
        if (tagClass.isArray()) {
          final StringBuilder stringBuilder = new StringBuilder("[");
          final int length = Array.getLength(tag);
          for (int i = 0; i < length; ++i) {
            if (stringBuilder.length() > 1) {
              stringBuilder.append(", ");
            }
            stringBuilder.append(Array.get(tag, i));
          }
          stringBuilder.append(']');
          tagName = stringBuilder.toString();
        } else {
          tagName = tagClass.getName();
        }
      } else {
        tagName = extractor.getName(tag);
      }
    }
    final String formattedText;
    if (msgFmt == null) {
      formattedText = Arrays.deepToString(args);
    } else {
      if (args == null) {
        formattedText = msgFmt;
      } else {
        formattedText = String.format(msgFmt, args);
      }
    }
    return new DefaultLogMessage(level, tagName, formattedText, tagBuilder.build(tag),
        stackBuilder.build(tag), threadBuilder.build(tag), timestampBuilder.build(tag));
  }

  private interface LogBuilder<T> {

    T build(Object tag);
  }

  private interface NameExtractor {

    String getName(Object tag);
  }

  private static class DefaultLogMessage implements LogMessage {

    private final String formattedText;
    private final LogLevel level;
    private final StackTraceElement[] stack;
    private final String tagName;
    private final WeakReference<Object> tagRef;
    private final Thread thread;
    private final long timestamp;

    private DefaultLogMessage(@NotNull final LogLevel level, @NotNull final String tagName,
        @NotNull final String formattedText, final WeakReference<Object> tagRef,
        final StackTraceElement[] stack, final Thread thread, final long timestamp) {
      this.tagRef = tagRef;
      this.tagName = tagName;
      this.formattedText = formattedText;
      this.level = level;
      this.stack = stack;
      this.thread = thread;
      this.timestamp = timestamp;
    }

    @Override
    public @Nullable Thread callingThread() {
      return thread;
    }

    @Override
    public @Nullable StackTraceElement[] callStack() {
      return stack;
    }

    @Override
    public @NotNull String formattedText() {
      return formattedText;
    }

    @Override
    public @NotNull LogLevel level() {
      return level;
    }

    @Override
    public @Nullable Object tag() {
      return (tagRef != null) ? tagRef.get() : null;
    }

    @Override
    public @NotNull String tagName() {
      return tagName;
    }

    @Override
    public long timestamp() {
      return timestamp;
    }
  }
}
