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
package sparx.logging.alert;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import sparx.function.Consumer;
import sparx.function.Function;

public class Alerts {

  private static volatile BackpressureAlert backpressureAlert;
  private static volatile ExecutionContextTaskAlert executionContextTaskAlert;
  private static volatile JoinAlert joinAlert;
  private static volatile SchedulerQueueAlert queueAlert;
  private static volatile SchedulerWorkerAlert workerAlert;

  private static final BackpressureAlert BACKPRESSURE_ALERT = new BackpressureAlert() {

    @Override
    public void notifyWaitStart(@NotNull final Thread currentThread) {
      backpressureAlert.notifyWaitStart(currentThread);
    }

    @Override
    public void notifyWaitStop(@NotNull final Thread currentThread) {
      backpressureAlert.notifyWaitStop(currentThread);
    }

    @Override
    public void turnOff() {
      throw new UnsupportedOperationException("turnOff");
    }
  };
  private static final ExecutionContextTaskAlert EXECUTION_CONTEXT_TASK_ALERT = new ExecutionContextTaskAlert() {

    @Override
    public void notifyCall(@NotNull final Function<?, ?> function) {
      executionContextTaskAlert.notifyCall(function);
    }

    @Override
    public void notifyRun(@NotNull final Consumer<?> consumer) {
      executionContextTaskAlert.notifyRun(consumer);
    }
  };
  private static final JoinAlert JOIN_ALERT = new JoinAlert() {
    @Override
    public void notifyJoinStart() {
      joinAlert.notifyJoinStart();
    }

    @Override
    public void notifyJoinStop() {
      joinAlert.notifyJoinStop();
    }

    @Override
    public void turnOff() {
      throw new UnsupportedOperationException("turnOff");
    }
  };
  private static final SchedulerQueueAlert SCHEDULER_QUEUE_ALERT = new SchedulerQueueAlert() {
    @Override
    public void notifyPendingTasks(final int beforeQueueCount, final int afterQueueCount) {
      queueAlert.notifyPendingTasks(beforeQueueCount, afterQueueCount);
    }
  };
  private static final SchedulerWorkerAlert SCHEDULER_WORKER_ALERT = new SchedulerWorkerAlert() {
    @Override
    public void notifyTaskStart(@NotNull final Thread currentThread) {
      workerAlert.notifyTaskStart(currentThread);
    }

    @Override
    public void notifyTaskStop(@NotNull final Thread currentThread) {
      workerAlert.notifyTaskStop(currentThread);
    }

    @Override
    public void turnOff() {
      throw new UnsupportedOperationException("turnOff");
    }
  };
  private static final Object mutex;

  private static ScheduledExecutorService alertsExecutorService;
  private static int enabledServiceClients;

  static {
    mutex = new Object();
    backpressureAlert = DummyBackpressureAlert.instance();
    joinAlert = DummyJoinAlert.instance();
    workerAlert = DummySchedulerWorkerAlert.instance();
    resetDefaults();
  }

  private Alerts() {
  }

  public static @NotNull BackpressureAlert backpressureAlert() {
    return BACKPRESSURE_ALERT;
  }

  public static @NotNull ExecutionContextTaskAlert executionContextTaskAlert() {
    return EXECUTION_CONTEXT_TASK_ALERT;
  }

  public static @NotNull JoinAlert joinAlert() {
    return JOIN_ALERT;
  }

  public static @NotNull SchedulerQueueAlert schedulerQueueAlert() {
    return SCHEDULER_QUEUE_ALERT;
  }

  public static @NotNull SchedulerWorkerAlert schedulerWorkerAlert() {
    return SCHEDULER_WORKER_ALERT;
  }

  public static void disableBackpressureAlert() {
    ScheduledExecutorService executorService = null;
    synchronized (mutex) {
      backpressureAlert.turnOff();
      backpressureAlert = DummyBackpressureAlert.instance();
      if (--enabledServiceClients == 0) {
        executorService = alertsExecutorService;
        alertsExecutorService = null;
      }
    }
    if (executorService != null) {
      executorService.shutdown();
    }
  }

  public static void disableExecutionContextTaskAlert() {
    executionContextTaskAlert = DummyExecutionContextTaskAlert.instance();
  }

  public static void disableJoinAlert() {
    ScheduledExecutorService executorService = null;
    synchronized (mutex) {
      joinAlert.turnOff();
      joinAlert = DummyJoinAlert.instance();
      if (--enabledServiceClients == 0) {
        executorService = alertsExecutorService;
        alertsExecutorService = null;
      }
    }
    if (executorService != null) {
      executorService.shutdown();
    }
  }

  public static void disableSchedulerQueueAlert() {
    queueAlert = DummySchedulerQueueAlert.instance();
  }

  public static void disableSchedulerWorkerAlert() {
    ScheduledExecutorService executorService = null;
    synchronized (mutex) {
      workerAlert.turnOff();
      workerAlert = DummySchedulerWorkerAlert.instance();
      if (--enabledServiceClients == 0) {
        executorService = alertsExecutorService;
        alertsExecutorService = null;
      }
    }
    if (executorService != null) {
      executorService.shutdown();
    }
  }

  public static void enableBackpressureAlert(final long interval,
      @NotNull final TimeUnit intervalUnit, final long timeout,
      @NotNull final TimeUnit timeoutUnit) {
    synchronized (mutex) {
      if (alertsExecutorService == null) {
        alertsExecutorService = createsExecutorService();
      }
      ++enabledServiceClients;
      backpressureAlert.turnOff();
      backpressureAlert = new WaitTimeoutAlert(Alerts.class, alertsExecutorService, interval,
          intervalUnit, timeout, timeoutUnit);
    }
  }

  public static void enableExecutionContextTaskAlert() {
    executionContextTaskAlert = new SerializableTaskAlert(Alerts.class);
  }

  public static void enableJoinAlert(final long interval,
      @NotNull final TimeUnit intervalUnit, final long timeout,
      @NotNull final TimeUnit timeoutUnit) {
    synchronized (mutex) {
      if (alertsExecutorService == null) {
        alertsExecutorService = createsExecutorService();
      }
      ++enabledServiceClients;
      joinAlert.turnOff();
      joinAlert = new AcquireTimeoutAlert(Alerts.class, alertsExecutorService, interval,
          intervalUnit, timeout, timeoutUnit);
    }
  }

  public static void enableSchedulerQueueAlert(final int maxCount) {
    queueAlert = new PendingTasksWorkerAlert(Alerts.class, maxCount);
  }

  public static void enableSchedulerWorkerAlert(final long interval,
      @NotNull final TimeUnit intervalUnit, final long timeout,
      @NotNull final TimeUnit timeoutUnit) {
    synchronized (mutex) {
      if (alertsExecutorService == null) {
        alertsExecutorService = createsExecutorService();
      }
      ++enabledServiceClients;
      workerAlert.turnOff();
      workerAlert = new WorkerTimeoutAlert(Alerts.class, alertsExecutorService, interval,
          intervalUnit, timeout, timeoutUnit);
    }
  }

  public static void resetDefaults() {
    executionContextTaskAlert = DummyExecutionContextTaskAlert.instance();
    queueAlert = DummySchedulerQueueAlert.instance();
    final ScheduledExecutorService executorService;
    synchronized (mutex) {
      backpressureAlert.turnOff();
      backpressureAlert = DummyBackpressureAlert.instance();
      joinAlert.turnOff();
      joinAlert = DummyJoinAlert.instance();
      workerAlert.turnOff();
      workerAlert = DummySchedulerWorkerAlert.instance();
      executorService = alertsExecutorService;
      alertsExecutorService = null;
    }
    if (executorService != null) {
      executorService.shutdown();
    }
  }

  private static @NotNull ScheduledExecutorService createsExecutorService() {
    return Executors.newSingleThreadScheduledExecutor(
        new ThreadFactory() {
          @Override
          public Thread newThread(@NotNull final Runnable r) {
            final Thread thread = new Thread(r, "sparx-alerts");
            thread.setPriority(Thread.MIN_PRIORITY);
            return thread;
          }
        });
  }
}
