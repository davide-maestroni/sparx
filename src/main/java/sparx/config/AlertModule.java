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

import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import sparx.config.SparxConfig.ConfigModule;
import sparx.logging.alert.Alerts;

public class AlertModule implements ConfigModule {

  private static final String PROP_PREFIX = "sparx";
  public static final String ALERT_PROP_PREFIX = PROP_PREFIX + ".alert";
  public static final String BACKPRESSURE_PROP_PREFIX = ALERT_PROP_PREFIX + ".backpressure";
  public static final String BACKPRESSURE_ENABLE_PROP = BACKPRESSURE_PROP_PREFIX + ".enable";
  public static final String BACKPRESSURE_INTERVAL_PROP = BACKPRESSURE_PROP_PREFIX + ".interval";
  public static final String BACKPRESSURE_INTERVAL_UNIT_PROP =
      BACKPRESSURE_PROP_PREFIX + ".intervalUnit";
  public static final String BACKPRESSURE_TIMEOUT_PROP = BACKPRESSURE_PROP_PREFIX + ".timeout";
  public static final String BACKPRESSURE_TIMEOUT_UNIT_PROP =
      BACKPRESSURE_PROP_PREFIX + ".timeoutUnit";
  public static final String JOIN_PROP_PREFIX = ALERT_PROP_PREFIX + ".join";
  public static final String JOIN_ENABLE_PROP = JOIN_PROP_PREFIX + ".enable";
  public static final String JOIN_INTERVAL_PROP = JOIN_PROP_PREFIX + ".interval";
  public static final String JOIN_INTERVAL_UNIT_PROP = JOIN_PROP_PREFIX + ".intervalUnit";
  public static final String JOIN_TIMEOUT_PROP = JOIN_PROP_PREFIX + ".timeout";
  public static final String JOIN_TIMEOUT_UNIT_PROP = JOIN_PROP_PREFIX + ".timeoutUnit";
  public static final String SERIALIZABLE_TASK_PROP_PREFIX =
      ALERT_PROP_PREFIX + ".serializableTask";
  public static final String SERIALIZABLE_TASK_ENABLE_PROP =
      SERIALIZABLE_TASK_PROP_PREFIX + ".enable";
  public static final String SCHEDULER_QUEUE_PROP_PREFIX = ALERT_PROP_PREFIX + ".schedulerQueue";
  public static final String SCHEDULER_QUEUE_ENABLE_PROP = SCHEDULER_QUEUE_PROP_PREFIX + ".enable";
  public static final String SCHEDULER_QUEUE_COUNT_PROP = SCHEDULER_QUEUE_PROP_PREFIX + ".maxCount";
  public static final String SCHEDULER_WORKER_PROP_PREFIX = ALERT_PROP_PREFIX + ".schedulerWorker";
  public static final String SCHEDULER_WORKER_ENABLE_PROP =
      SCHEDULER_WORKER_PROP_PREFIX + ".enable";
  public static final String SCHEDULER_WORKER_INTERVAL_PROP =
      SCHEDULER_WORKER_PROP_PREFIX + ".interval";
  public static final String SCHEDULER_WORKER_INTERVAL_UNIT_PROP =
      SCHEDULER_WORKER_PROP_PREFIX + ".intervalUnit";
  public static final String SCHEDULER_WORKER_TIMEOUT_PROP =
      SCHEDULER_WORKER_PROP_PREFIX + ".timeout";
  public static final String SCHEDULER_WORKER_TIMEOUT_UNIT_PROP =
      SCHEDULER_WORKER_PROP_PREFIX + ".timeoutUnit";

  public static void addModule() {
    SparxConfig.addModule(ALERT_PROP_PREFIX, new AlertModule());
  }

  private static void configureBackpressure(@NotNull final Properties properties) {
    final String enabled = properties.getProperty(BACKPRESSURE_ENABLE_PROP, "false");
    if (Boolean.parseBoolean(enabled)) {
      final long interval;
      final TimeUnit intervalUnit;
      final String intervalProp = properties.getProperty(BACKPRESSURE_INTERVAL_PROP);
      if (intervalProp == null) {
        interval = 10;
        intervalUnit = TimeUnit.SECONDS;
      } else {
        interval = Long.parseLong(intervalProp);
        intervalUnit = TimeUnit.valueOf(
            properties.getProperty(BACKPRESSURE_INTERVAL_UNIT_PROP, TimeUnit.SECONDS.name()));
      }
      final long timeout;
      final TimeUnit timeoutUnit;
      final String timeoutProp = properties.getProperty(BACKPRESSURE_TIMEOUT_PROP);
      if (intervalProp == null) {
        timeout = 10;
        timeoutUnit = TimeUnit.SECONDS;
      } else {
        timeout = Long.parseLong(timeoutProp);
        timeoutUnit = TimeUnit.valueOf(
            properties.getProperty(BACKPRESSURE_TIMEOUT_UNIT_PROP, TimeUnit.SECONDS.name()));
      }
      Alerts.enableBackpressureAlert(interval, intervalUnit, timeout, timeoutUnit);
    } else {
      Alerts.disableBackpressureAlert();
    }
  }

  private static void configureJoin(@NotNull final Properties properties) {
    final String enabled = properties.getProperty(JOIN_ENABLE_PROP, "false");
    if (Boolean.parseBoolean(enabled)) {
      final long interval;
      final TimeUnit intervalUnit;
      final String intervalProp = properties.getProperty(JOIN_INTERVAL_PROP);
      if (intervalProp == null) {
        interval = 10;
        intervalUnit = TimeUnit.SECONDS;
      } else {
        interval = Long.parseLong(intervalProp);
        intervalUnit = TimeUnit.valueOf(
            properties.getProperty(JOIN_INTERVAL_UNIT_PROP, TimeUnit.SECONDS.name()));
      }
      final long timeout;
      final TimeUnit timeoutUnit;
      final String timeoutProp = properties.getProperty(JOIN_TIMEOUT_PROP);
      if (intervalProp == null) {
        timeout = 1;
        timeoutUnit = TimeUnit.MINUTES;
      } else {
        timeout = Long.parseLong(timeoutProp);
        timeoutUnit = TimeUnit.valueOf(
            properties.getProperty(JOIN_TIMEOUT_UNIT_PROP, TimeUnit.SECONDS.name()));
      }
      Alerts.enableJoinAlert(interval, intervalUnit, timeout, timeoutUnit);
    } else {
      Alerts.disableJoinAlert();
    }
  }

  private static void configureSerializableTask(@NotNull final Properties properties) {
    final String enabled = properties.getProperty(SERIALIZABLE_TASK_ENABLE_PROP, "false");
    if (Boolean.parseBoolean(enabled)) {
      Alerts.enableExecutionContextTaskAlert();
    } else {
      Alerts.disableExecutionContextTaskAlert();
    }
  }

  private static void configureSchedulerQueue(@NotNull final Properties properties) {
    final String enabled = properties.getProperty(SCHEDULER_QUEUE_ENABLE_PROP, "false");
    if (Boolean.parseBoolean(enabled)) {
      final int maxCount = Integer.parseInt(
          properties.getProperty(SCHEDULER_QUEUE_COUNT_PROP, "1000"));
      Alerts.enableSchedulerQueueAlert(maxCount);
    } else {
      Alerts.disableSchedulerQueueAlert();
    }
  }

  private static void configureSchedulerWorker(@NotNull final Properties properties) {
    final String enabled = properties.getProperty(SCHEDULER_WORKER_ENABLE_PROP, "false");
    if (Boolean.parseBoolean(enabled)) {
      final long interval;
      final TimeUnit intervalUnit;
      final String intervalProp = properties.getProperty(SCHEDULER_WORKER_INTERVAL_PROP);
      if (intervalProp == null) {
        interval = 10;
        intervalUnit = TimeUnit.SECONDS;
      } else {
        interval = Long.parseLong(intervalProp);
        intervalUnit = TimeUnit.valueOf(
            properties.getProperty(SCHEDULER_WORKER_INTERVAL_UNIT_PROP, TimeUnit.SECONDS.name()));
      }
      final long timeout;
      final TimeUnit timeoutUnit;
      final String timeoutProp = properties.getProperty(SCHEDULER_WORKER_TIMEOUT_PROP);
      if (intervalProp == null) {
        timeout = 1;
        timeoutUnit = TimeUnit.MINUTES;
      } else {
        timeout = Long.parseLong(timeoutProp);
        timeoutUnit = TimeUnit.valueOf(
            properties.getProperty(SCHEDULER_WORKER_TIMEOUT_UNIT_PROP, TimeUnit.SECONDS.name()));
      }
      Alerts.enableSchedulerWorkerAlert(interval, intervalUnit, timeout, timeoutUnit);
    } else {
      Alerts.disableSchedulerWorkerAlert();
    }
  }

  @Override
  public void configure(@NotNull final Properties properties) {
    configureBackpressure(properties);
    configureSerializableTask(properties);
    configureJoin(properties);
    configureSchedulerQueue(properties);
    configureSchedulerWorker(properties);
  }

  @Override
  public void reset() {
    Alerts.resetDefaults();
  }
}
