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
import sparx.logging.Log;
import sparx.util.ImmutableList;
import sparx.util.Require;
import sparx.util.UncheckedException;

public class SparxConfig {

  private static final List<String> CONFIG_LOCATIONS = ImmutableList.of("sparx.properties",
      "sparx/sparx.properties", "sparx/config.properties");

  private static final Object lock = new Object();
  private static final HashMap<String, ConfigModule> modules = new HashMap<String, ConfigModule>();

  private static boolean initialized = false;

  private SparxConfig() {
  }

  public static void addModule(@NotNull final String prefix, @NotNull final ConfigModule module) {
    synchronized (lock) {
      final ConfigModule oldModule = modules.put(prefix, Require.notNull(module, "module"));
      if (oldModule != null && !oldModule.equals(module)) {
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
    Require.notNull(properties, "properties");
    synchronized (lock) {
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

  public static void removeModule(@NotNull final String prefix) {
    synchronized (lock) {
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

  public static void reset() {
    synchronized (lock) {
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
