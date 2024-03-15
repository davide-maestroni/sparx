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
package sparx0.util.config;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sparx0.concurrent.ExecutorContext;
import sparx0.concurrent.Receiver;
import sparx0.concurrent.StreamingFuture;
import sparx0.concurrent.execution.TernaryFuture;
import sparx0.concurrent.logging.ConsoleLogPrinter;
import sparx0.concurrent.logging.JavaLogPrinter;
import sparx0.concurrent.logging.LogCollectorFuture;
import sparx0.util.Nothing;
import sparx0.util.config.SparxConfig.ConfigModule;
import sparx0.util.function.TernaryConsumer;
import sparx0.util.logging.Log;
import sparx0.util.logging.Log.LogCollector;
import sparx0.util.logging.Log.LogLevel;
import sparx0.util.logging.LogMessage;
import sparx0.util.tuple.Couple;
import sparx0.util.tuple.Tuples;

public class LogModule implements ConfigModule {

  private static final String PROP_PREFIX = "sparx0";
  public static final String LOGGING_PROP_PREFIX = PROP_PREFIX + ".logging";
  public static final String LOG_FORMATTER_PROP = LOGGING_PROP_PREFIX + ".formatter";
  public static final String LOG_LEVEL_PROP = LOGGING_PROP_PREFIX + ".level";
  public static final String LOG_PRINTERS_PROP = LOGGING_PROP_PREFIX + ".printers";
  public static final String LOG_THREADS_PROP = LOGGING_PROP_PREFIX + ".threads";
  public static final String LOG_PRINTER_THREAD_PREFIX = "sparx-log-printer-";

  private final HashSet<ExecutorService> executors = new HashSet<ExecutorService>();

  private LogModule() {
  }

  public static void addModule() {
    SparxConfig.addModule(LOGGING_PROP_PREFIX, new LogModule());
  }

  private static @Nullable Log.LogCollector instantiateLogFormatter(
      @Nullable final String formatterName,
      @NotNull final Properties properties) throws Exception {
    if (LogCollectorFuture.class.getName().equals(formatterName)) {
      return new LogCollectorFuture(properties);
    }
    if (formatterName != null) {
      final Class<?> formatterClass = Class.forName(formatterName);
      try {
        final Constructor<?> constructor = formatterClass.getConstructor(Properties.class);
        return (LogCollector) constructor.newInstance(properties);
      } catch (final NoSuchMethodException ignored) {
        final Constructor<?> constructor = formatterClass.getConstructor();
        return (LogCollector) constructor.newInstance();
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private static @Nullable Receiver<LogMessage> instantiateLogPrinter(
      @Nullable final String printerName,
      @NotNull final Properties properties) throws Exception {
    if (ConsoleLogPrinter.class.getName().equals(printerName)) {
      return new ConsoleLogPrinter(properties);
    }
    if (JavaLogPrinter.class.getName().equals(printerName)) {
      return new JavaLogPrinter(properties);
    }
    if (printerName != null) {
      final Class<?> formatterClass = Class.forName(printerName);
      try {
        final Constructor<?> constructor = formatterClass.getConstructor(Properties.class);
        return (Receiver<LogMessage>) constructor.newInstance(properties);
      } catch (final NoSuchMethodException ignored) {
        final Constructor<?> constructor = formatterClass.getConstructor();
        return (Receiver<LogMessage>) constructor.newInstance();
      }
    }
    return null;
  }

  @Override
  public void configure(@NotNull final Properties properties) throws Exception {
    final HashSet<ExecutorService> executors = this.executors;
    synchronized (executors) {
      final String levelName = properties.getProperty(LOG_LEVEL_PROP);
      if (levelName != null) {
        Log.setLevel(LogLevel.valueOf(levelName.toUpperCase()));
      }
      final LogCollector logCollector = instantiateLogFormatter(
          properties.getProperty(LOG_FORMATTER_PROP), properties);
      if (logCollector != null) {
        Log.setCollector(logCollector);
        if (logCollector instanceof StreamingFuture) {
          final String printers = properties.getProperty(LOG_PRINTERS_PROP);
          if (printers != null) {
            final String[] printerNames = printers.split("\\s*,\\s*");
            if (printerNames.length > 0) {
              final int threads = Integer.parseInt(properties.getProperty(LOG_THREADS_PROP, "1"));
              @SuppressWarnings("unchecked") final StreamingFuture<LogMessage> future = (StreamingFuture<LogMessage>) logCollector;
              if (threads < 1) {
                for (final String printerName : printerNames) {
                  final Receiver<LogMessage> logPrinter = instantiateLogPrinter(printerName,
                      properties);
                  if (logPrinter != null) {
                    future.subscribe(logPrinter);
                  }
                }
              } else {
                final ExecutorService executor;
                if (threads == 1) {
                  executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
                    @Override
                    public Thread newThread(@NotNull final Runnable r) {
                      final Thread thread = new Thread(r, LOG_PRINTER_THREAD_PREFIX + "0");
                      thread.setPriority(Thread.MIN_PRIORITY);
                      return thread;
                    }
                  });
                } else {
                  executor = Executors.newFixedThreadPool(threads, new ThreadFactory() {
                    private final AtomicInteger count = new AtomicInteger(0);

                    @Override
                    public Thread newThread(@NotNull final Runnable r) {
                      final Thread thread = new Thread(r,
                          LOG_PRINTER_THREAD_PREFIX + count.getAndIncrement());
                      thread.setPriority(Thread.MIN_PRIORITY);
                      return thread;
                    }
                  });
                }
                executors.add(executor);
                final LogPrintersSetup setup = new LogPrintersSetup();
                for (final String printerName : printerNames) {
                  final TernaryFuture<Object, Couple<Object, String, Properties>, LogMessage, Nothing, Nothing> result =
                      ExecutorContext.of(executor).submit(setup);
                  future.subscribe(result.parameters().getSecond());
                  result.parameters().getFirst().set(Tuples.asTuple(printerName, properties));
                  result.parameters().getThird().get();
                }
              }
            }
          }
        }
      }
    }
  }

  @Override
  public void reset() {
    final HashSet<ExecutorService> executors = this.executors;
    synchronized (executors) {
      for (final ExecutorService executor : executors) {
        executor.shutdown();
      }
      executors.clear();
      Log.reset();
    }
  }

  private static class LogPrintersSetup implements
      TernaryConsumer<StreamingFuture<Couple<Object, String, Properties>>, StreamingFuture<LogMessage>, StreamingFuture<Nothing>> {

    @Override
    public void accept(final StreamingFuture<Couple<Object, String, Properties>> configFuture,
        final StreamingFuture<LogMessage> collectorFuture,
        final StreamingFuture<Nothing> readyFuture) {
      configFuture.subscribe(new Receiver<Couple<Object, String, Properties>>() {
        @Override
        public void close() {
          readyFuture.close();
        }

        @Override
        public boolean fail(@NotNull final Exception error) {
          return readyFuture.fail(error);
        }

        @Override
        public void set(
            final Couple<Object, String, Properties> value) {
          try {
            final Receiver<LogMessage> logPrinter = instantiateLogPrinter(
                value.getFirst(), value.getSecond());
            if (logPrinter != null) {
              collectorFuture.subscribe(logPrinter);
            }
          } catch (final Exception e) {
            readyFuture.fail(e);
          }
        }

        @Override
        public void setBulk(
            @NotNull final Collection<Couple<Object, String, Properties>> values) {
          for (final Couple<Object, String, Properties> value : values) {
            set(value);
          }
        }
      });
    }
  }
}
