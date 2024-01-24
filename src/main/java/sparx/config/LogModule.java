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
package sparx.config;

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
import sparx.concurrent.ExecutorContext;
import sparx.concurrent.Receiver;
import sparx.concurrent.StreamingFuture;
import sparx.concurrent.TripleFuture;
import sparx.concurrent.ValFuture;
import sparx.concurrent.VarFuture;
import sparx.concurrent.logging.ConsoleLogPrinter;
import sparx.concurrent.logging.JavaLogPrinter;
import sparx.concurrent.logging.LogCollectorFuture;
import sparx.config.SparxConfig.ConfigModule;
import sparx.function.Consumer;
import sparx.logging.Log;
import sparx.logging.Log.LogCollector;
import sparx.logging.Log.LogLevel;
import sparx.logging.LogMessage;
import sparx.tuple.Couple;
import sparx.tuple.Tuples;
import sparx.util.Nothing;

public class LogModule implements ConfigModule {

  private static final String PROP_PREFIX = "sparx";
  public static final String LOGGING_PROP_PREFIX = PROP_PREFIX + ".logging";
  public static final String LOG_FORMATTER_PROP = LOGGING_PROP_PREFIX + ".formatter";
  public static final String LOG_LEVEL_PROP = LOGGING_PROP_PREFIX + ".level";
  public static final String LOG_PRINTERS_PROP = LOGGING_PROP_PREFIX + ".printers";
  public static final String LOG_THREADS_PROP = LOGGING_PROP_PREFIX + ".threads";
  public static final String LOG_PRINTER_THREAD_PREFIX = "sparx-log-printer-";

  private final HashSet<ExecutorService> executors = new HashSet<ExecutorService>();
  private final Object lock = new Object();

  public static void addModule() {
    SparxConfig.addModule(LOGGING_PROP_PREFIX, new LogModule());
  }

  private static @Nullable LogCollector instantiateLogFormatter(
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
    synchronized (lock) {
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
                for (final String printerName : printerNames) {
                  final VarFuture<Nothing> ready = VarFuture.create();
                  ExecutorContext.of(executor)
                      .run(TripleFuture.of(future, ready,
                              ValFuture.of(Tuples.asTuple(printerName, properties))),
                          new LogPrintersSetup());
                  ready.get();
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
    synchronized (lock) {
      final HashSet<ExecutorService> executors = this.executors;
      for (final ExecutorService executor : executors) {
        executor.shutdown();
      }
      executors.clear();
      Log.reset();
    }
  }

  private static class LogPrintersSetup implements
      Consumer<TripleFuture<Object, LogMessage, Nothing, Couple<Object, String, Properties>>> {

    @Override
    public void accept(
        final TripleFuture<Object, LogMessage, Nothing, Couple<Object, String, Properties>> input) {
      input.getThird().subscribe(
          new Receiver<Couple<Object, String, Properties>>() {
            @Override
            public boolean fail(@NotNull final Exception error) {
              return input.getSecond().fail(error);
            }

            @Override
            public void set(
                final Couple<Object, String, Properties> value) {
              try {
                final Receiver<LogMessage> logPrinter = instantiateLogPrinter(
                    value.getFirst(), value.getSecond());
                if (logPrinter != null) {
                  input.getFirst().subscribe(logPrinter);
                }
              } catch (final Exception e) {
                input.getSecond().fail(e);
              }
            }

            @Override
            public void setBulk(
                @NotNull final Collection<Couple<Object, String, Properties>> values) {
              for (final Couple<Object, String, Properties> value : values) {
                set(value);
              }
            }

            @Override
            public void close() {
              input.getSecond().close();
            }
          });
    }
  }
}
