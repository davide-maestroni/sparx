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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

public class Alerts {

  private static volatile JoinAlert joinTimeout = DummyJoinAlert.instance();
  private static volatile SchedulerAlert pendingTasks = DummySchedulerAlert.instance();
  private static volatile SchedulerAlert tasksTimeout = DummySchedulerAlert.instance();

  private static final JoinAlert joinAlert = new JoinAlert() {
    @Override
    public void notifyAcquire(@NotNull final Semaphore semaphore) throws InterruptedException {
      joinTimeout.notifyAcquire(semaphore);
    }

    @Override
    public boolean notifyTryAcquire(@NotNull final Semaphore semaphore, final long timeout,
        @NotNull final TimeUnit unit) throws InterruptedException {
      return joinTimeout.notifyTryAcquire(semaphore, timeout, unit);
    }
  };
  private static final SchedulerAlert schedulerAlert = new SchedulerAlert() {
    @Override
    public void notifyPendingTasks(final int beforeQueueCount, final int afterQueueCount) {
      pendingTasks.notifyPendingTasks(beforeQueueCount, afterQueueCount);
    }

    @Override
    public void notifyTaskStart(@NotNull final Thread currentThread) {
      tasksTimeout.notifyTaskStart(currentThread);
    }

    @Override
    public void notifyTaskStop(@NotNull final Thread currentThread) {
      tasksTimeout.notifyTaskStop(currentThread);
    }

    @Override
    public void close() {
    }
  };
  private static final Object mutex = new Object();

  private static ScheduledExecutorService alertsExecutorService;

  private Alerts() {
  }

  public static void disableAcquireTimeoutAlert() {
    joinTimeout = DummyJoinAlert.instance();
  }

  public static void disablePendingTasksAlert() {
    synchronized (mutex) {
      pendingTasks.close();
      pendingTasks = DummySchedulerAlert.instance();
    }
  }

  public static void disableTasksTimeoutAlert() {
    synchronized (mutex) {
      pendingTasks.close();
      pendingTasks = DummySchedulerAlert.instance();
      if (alertsExecutorService != null) {
        alertsExecutorService.shutdown();
        alertsExecutorService = null;
      }
    }
  }

  public static void enableAcquireTimeoutAlert(final long timeout,
      @NotNull final TimeUnit timeoutUnit) {
    joinTimeout = new AcquireTimeoutAlert(Alerts.class, timeout, timeoutUnit);
  }

  public static void enablePendingTasksAlert(final int maxCount) {
    synchronized (mutex) {
      pendingTasks.close();
      pendingTasks = new PendingTasksAlert(Alerts.class, maxCount);
    }
  }

  public static void enableTasksTimeoutAlert(final long interval,
      @NotNull final TimeUnit intervalUnit, final long timeout,
      @NotNull final TimeUnit timeoutUnit) {
    synchronized (mutex) {
      if (alertsExecutorService == null) {
        alertsExecutorService = createsExecutorService();
      }
      tasksTimeout.close();
      tasksTimeout = new TaskTimeoutAlert(Alerts.class, alertsExecutorService, interval,
          intervalUnit, timeout, timeoutUnit);
    }
  }

  public static void resetDefaults() {
    synchronized (mutex) {
      pendingTasks.close();
      pendingTasks = DummySchedulerAlert.instance();
      tasksTimeout.close();
      tasksTimeout = DummySchedulerAlert.instance();
      if (alertsExecutorService != null) {
        alertsExecutorService.shutdown();
        alertsExecutorService = null;
      }
    }
  }

  public static @NotNull JoinAlert joinAlert() {
    return joinAlert;
  }

  public static @NotNull SchedulerAlert schedulerAlert() {
    return schedulerAlert;
  }

  private static @NotNull ScheduledExecutorService createsExecutorService() {
    return Executors.newSingleThreadScheduledExecutor(
        new ThreadFactory() {
          @Override
          public Thread newThread(@NotNull final Runnable r) {
            final Thread thread = new Thread(r, "sparx-alert");
            thread.setPriority(Thread.MIN_PRIORITY);
            return thread;
          }
        });
  }
}
