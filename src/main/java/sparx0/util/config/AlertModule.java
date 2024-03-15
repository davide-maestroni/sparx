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
import java.util.Properties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sparx0.util.config.SparxConfig.ConfigModule;
import sparx0.util.logging.alert.Alerts;
import sparx0.util.logging.alert.Alerts.Alert;
import sparx0.util.logging.alert.BackpressureAlert;
import sparx0.util.logging.alert.ExecutionContextTaskAlert;
import sparx0.util.logging.alert.HistorySizeAlert;
import sparx0.util.logging.alert.JoinAlert;
import sparx0.util.logging.alert.SchedulerQueueAlert;
import sparx0.util.logging.alert.SchedulerWorkerAlert;

public class AlertModule implements ConfigModule {

  private static final String PROP_PREFIX = "sparx0";
  public static final String ALERTS_PROP_PREFIX = PROP_PREFIX + ".alerts";
  public static final String ENABLE_PROP_SUFFIX = ".enable";

  private AlertModule() {
  }

  public static void addModule() {
    SparxConfig.addModule(ALERTS_PROP_PREFIX, new AlertModule());
  }

  private static @Nullable Alerts.Alert<?> instantiateAlert(
      @Nullable final String alertName,
      @NotNull final Properties properties) throws Exception {
    if (BackpressureAlert.class.getName().equals(alertName)) {
      return new BackpressureAlert(properties);
    }
    if (ExecutionContextTaskAlert.class.getName().equals(alertName)) {
      return new ExecutionContextTaskAlert();
    }
    if (HistorySizeAlert.class.getName().equals(alertName)) {
      return new HistorySizeAlert(properties);
    }
    if (JoinAlert.class.getName().equals(alertName)) {
      return new JoinAlert(properties);
    }
    if (SchedulerQueueAlert.class.getName().equals(alertName)) {
      return new SchedulerQueueAlert(properties);
    }
    if (SchedulerWorkerAlert.class.getName().equals(alertName)) {
      return new SchedulerWorkerAlert(properties);
    }
    if (alertName != null) {
      final Class<?> alertClass = Class.forName(alertName);
      try {
        final Constructor<?> constructor = alertClass.getConstructor(Properties.class);
        return (Alert<?>) constructor.newInstance(properties);
      } catch (final NoSuchMethodException ignored) {
        final Constructor<?> constructor = alertClass.getConstructor();
        return (Alert<?>) constructor.newInstance();
      }
    }
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void configure(@NotNull final Properties properties) throws Exception {
    final String alerts = properties.getProperty(ALERTS_PROP_PREFIX);
    if (alerts != null) {
      final String[] alertNames = alerts.split("\\s*,\\s*");
      for (final String alertName : alertNames) {
        final String enabled = properties.getProperty(alertName + ENABLE_PROP_SUFFIX, "false");
        if (Boolean.parseBoolean(enabled)) {
          final Alert<?> alert = instantiateAlert(alertName, properties);
          if (alert != null) {
            Alerts.enable((Class<Alert<Object>>) alert.getClass(), (Alert<Object>) alert);
          }
        }
      }
    }
  }

  @Override
  public void reset() {
    Alerts.resetDefaults();
  }
}
