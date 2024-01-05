package sparx.config;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;
import sparx.util.UncheckedException;
import sparx.logging.Log;
import sparx.util.ImmutableList;

public class SparxConfig {

  private static final List<String> CONFIG_LOCATIONS = ImmutableList.of("sparx.properties",
      "sparx/sparx.properties", "sparx/config.properties");

  private static final HashMap<String, ConfigModule> modules = new HashMap<String, ConfigModule>();
  private static final Object mutex = new Object();

  private static boolean initialized = false;

  private SparxConfig() {
  }

  public static void addModule(@NotNull final String prefix, @NotNull final ConfigModule module) {
    synchronized (mutex) {
      final ConfigModule oldModule = modules.put(prefix, module);
      if (oldModule != null && !oldModule.equals(module)) {
        try {
          oldModule.reset();
        } catch (final Exception e) {
          throw UncheckedException.throwUnchecked(e);
        }
      }
    }
  }

  public static void removeModule(@NotNull final String prefix) {
    synchronized (mutex) {
      final ConfigModule oldModule = modules.remove(prefix);
      if (oldModule != null) {
        try {
          oldModule.reset();
        } catch (final Exception e) {
          throw UncheckedException.throwUnchecked(e);
        }
      }
    }
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
        "Could not find any config file in the following locations: " + CONFIG_LOCATIONS);
  }

  public static void initFromConfigFile(@NotNull final String filePath) {
    final File file = new File(filePath);
    if (file.exists()) {
      initFromConfigFile(file.toURI());
    } else {
      final URL fileUrl = SparxConfig.class.getClassLoader().getResource(filePath);
      if (fileUrl != null) {
        initFromConfigFile(fileUrl);
      } else {
        throw new ExceptionInInitializerError(
            "Could not find any config file in the following location: " + filePath);
      }
    }
  }

  public static void initFromConfigFile(@NotNull final URI fileUri) {
    try {
      initFromConfigFile(fileUri.toURL());
    } catch (final MalformedURLException e) {
      throw new ExceptionInInitializerError(e);
    }
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
      initialized = true;
      for (final Entry<String, ConfigModule> entry : modules.entrySet()) {
        final String match = entry.getKey();
        final String prefix = match.endsWith(".") ? match : match + ".";
        final Enumeration<Object> keys = properties.keys();
        while (keys.hasMoreElements()) {
          final String key = keys.nextElement().toString();
          if (key.equals(match) || key.startsWith(prefix)) {
            try {
              entry.getValue().configure(properties);
              break;
            } catch (final Exception e) {
              throw UncheckedException.throwUnchecked(e);
            }
          }
        }
      }
      Log.dbg(SparxConfig.class,
          "Sparx configuration successfully initialized with properties: %s", properties);
    }
  }

  public static void reset() {
    synchronized (mutex) {
      if (initialized) {
        initialized = false;
        for (final ConfigModule module : modules.values()) {
          try {
            module.reset();
          } catch (final Exception e) {
            throw UncheckedException.throwUnchecked(e);
          }
        }
        Log.dbg(SparxConfig.class, "Sparx configuration successfully reset to default");
      }
    }
  }

  public interface ConfigModule {

    void configure(@NotNull Properties properties) throws Exception;

    void reset() throws Exception;
  }
}
