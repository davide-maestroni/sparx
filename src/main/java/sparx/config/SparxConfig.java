package sparx.config;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sparx.concurrent.ConstFuture;
import sparx.concurrent.ExecutorContext;
import sparx.concurrent.LogCollectorFuture;
import sparx.concurrent.Receiver;
import sparx.concurrent.StreamingFuture;
import sparx.concurrent.TripleFuture;
import sparx.concurrent.UncheckedInterruptedException;
import sparx.concurrent.VarFuture;
import sparx.function.Consumer;
import sparx.logging.Log;
import sparx.logging.Log.LogCollector;
import sparx.logging.Log.LogLevel;
import sparx.logging.LogMessage;
import sparx.logging.printer.ConsoleLogPrinter;
import sparx.logging.printer.JavaLogPrinter;
import sparx.tuple.Couple;
import sparx.tuple.Tuples;
import sparx.util.Nothing;

public class SparxConfig {

  // TODO: alerts

  private static final String PROP_PREFIX = "sparx";
  private static final String LOGGING_PROP_PREFIX = PROP_PREFIX + ".logging";
  public static final String LOG_FORMATTER_PROP = LOGGING_PROP_PREFIX + ".formatter";
  public static final String LOG_LEVEL_PROP = LOGGING_PROP_PREFIX + ".level";
  public static final String LOG_PRINTERS_PROP = LOGGING_PROP_PREFIX + ".printers";
  public static final String LOG_THREADS_PROP = LOGGING_PROP_PREFIX + ".threads";

  private static final List<String> CONFIG_LOCATIONS = Arrays.asList("sparx.properties",
      "sparx/sparx.properties", "sparx/config.properties");
  public static final String LOG_PRINTER_THREAD_PREFIX = "sparx-log-printer-";

  private static final HashSet<ExecutorService> executors = new HashSet<ExecutorService>();
  private static final Object mutex = new Object();

  private static boolean initialized = false;

  private SparxConfig() {
  }

  public static void initFromConfigFile() {
    URL fileUrl;
    for (final String location : CONFIG_LOCATIONS) {
      fileUrl = SparxConfig.class.getClassLoader().getResource(location);
      if (fileUrl != null) {
        initFromConfigFile(fileUrl);
        return;
      }
    }
    throw new ExceptionInInitializerError(
        "Could not find the config file in the following locations: " + CONFIG_LOCATIONS);
  }

  public static void initFromConfigFile(@NotNull final URL fileUrl) {
    try {
      final Properties properties = new Properties();
      properties.load(fileUrl.openStream());
      initFromProperties(properties);
    } catch (final IOException e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  public static void initFromProperties(@NotNull final Properties properties) {
    synchronized (mutex) {
      reset();
      try {
        initialized = true;
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
                                ConstFuture.of(Tuples.asTuple(printerName, properties))),
                            new Consumer<TripleFuture<Object, LogMessage, Nothing, Couple<Object, String, Properties>>>() {
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
                                        } catch (final RuntimeException e) {
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
                            }, 1);
                    ready.get();
                  }
                }
              }
            }
          }
        }
        Log.dbg(SparxConfig.class,
            "Sparx configuration successfully initialized with properties: %s", properties);
      } catch (final InterruptedException e) {
        reset();
        throw new UncheckedInterruptedException(e);
      } catch (final Throwable t) {
        reset();
        throw new ExceptionInInitializerError(t);
      }
    }
  }

  public static void reset() {
    synchronized (mutex) {
      if (initialized) {
        final HashSet<ExecutorService> executors = SparxConfig.executors;
        for (final ExecutorService executor : executors) {
          executor.shutdown();
        }
        executors.clear();
        Log.dbg(SparxConfig.class, "Sparx configuration successfully reset");
        Log.reset();
        initialized = false;
      }
    }
  }

  private static LogCollector instantiateLogFormatter(@Nullable final String formatterName,
      @NotNull final Properties properties) {
    if (LogCollectorFuture.class.getName().equals(formatterName)) {
      return new LogCollectorFuture(properties);
    }
    if (formatterName != null) {
      try {
        final Class<?> formatterClass = Class.forName(formatterName);
        try {
          final Constructor<?> constructor = formatterClass.getConstructor(Properties.class);
          try {
            return (LogCollector) constructor.newInstance(properties);
          } catch (final Exception e) {
            throw new ExceptionInInitializerError(e);
          }
        } catch (final NoSuchMethodException e) {
          try {
            final Constructor<?> constructor = formatterClass.getConstructor();
            try {
              return (LogCollector) constructor.newInstance();
            } catch (final Exception ex) {
              throw new ExceptionInInitializerError(ex);
            }
          } catch (final NoSuchMethodException ex) {
            throw new ExceptionInInitializerError(ex);
          }
        }
      } catch (final ClassNotFoundException e) {
        throw new ExceptionInInitializerError(e);
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private static Receiver<LogMessage> instantiateLogPrinter(@Nullable final String printerName,
      @NotNull final Properties properties) {
    if (ConsoleLogPrinter.class.getName().equals(printerName)) {
      return new ConsoleLogPrinter(properties);
    }
    if (JavaLogPrinter.class.getName().equals(printerName)) {
      return new JavaLogPrinter(properties);
    }
    if (printerName != null) {
      try {
        final Class<?> formatterClass = Class.forName(printerName);
        try {
          final Constructor<?> constructor = formatterClass.getConstructor(Properties.class);
          try {
            return (Receiver<LogMessage>) constructor.newInstance(properties);
          } catch (final Exception e) {
            throw new ExceptionInInitializerError(e);
          }
        } catch (final NoSuchMethodException e) {
          try {
            final Constructor<?> constructor = formatterClass.getConstructor();
            try {
              return (Receiver<LogMessage>) constructor.newInstance();
            } catch (final Exception ex) {
              throw new ExceptionInInitializerError(ex);
            }
          } catch (final NoSuchMethodException ex) {
            throw new ExceptionInInitializerError(ex);
          }
        }
      } catch (final ClassNotFoundException e) {
        throw new ExceptionInInitializerError(e);
      }
    }
    return null;
  }
}
