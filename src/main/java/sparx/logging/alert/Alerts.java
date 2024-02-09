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

import java.util.ArrayList;
import java.util.HashMap;
import org.jetbrains.annotations.NotNull;

public class Alerts {

  private static final Alert<?> DISABLED = new Alert<Object>() {
    @Override
    public void disable() {
    }

    @Override
    public void notify(final int state, final Object payload) {
    }
  };

  private static final HashMap<Class<?>, DynamicAlert<?>> alerts = new HashMap<Class<?>, DynamicAlert<?>>();

  private Alerts() {
  }

  public static <P, A extends Alert<P>> void disable(@NotNull final Class<A> type) {
    final DynamicAlert<?> dynamicAlert;
    synchronized (alerts) {
      dynamicAlert = alerts.get(type);
    }
    if (dynamicAlert != null) {
      dynamicAlert.disable();
    }
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <P, A extends Alert<P>> Alert<P> enable(@NotNull final Class<A> type,
      @NotNull final A alert) {
    DynamicAlert<P> dynamicAlert;
    synchronized (alerts) {
      dynamicAlert = (DynamicAlert<P>) alerts.get(type);
      if (dynamicAlert == null) {
        dynamicAlert = new DynamicAlert<P>();
        alerts.put(type, dynamicAlert);
      }
    }
    dynamicAlert.enable(alert);
    return dynamicAlert;
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <P, A extends Alert<P>> Alert<P> get(@NotNull final Class<A> type) {
    DynamicAlert<P> dynamicAlert;
    synchronized (alerts) {
      dynamicAlert = (DynamicAlert<P>) alerts.get(type);
      if (dynamicAlert == null) {
        dynamicAlert = new DynamicAlert<P>();
        alerts.put(type, dynamicAlert);
      }
    }
    return dynamicAlert;
  }

  public static void resetDefaults() {
    final ArrayList<DynamicAlert<?>> dynamicAlerts;
    synchronized (alerts) {
      dynamicAlerts = new ArrayList<DynamicAlert<?>>(alerts.values());
    }
    for (final DynamicAlert<?> dynamicAlert : dynamicAlerts) {
      dynamicAlert.disable();
    }
  }

  public interface Alert<P> {

    void disable();

    void notify(int state, P payload);
  }

  @SuppressWarnings("unchecked")
  private static class DynamicAlert<P> implements Alert<P> {

    private volatile Alert<P> alert = (Alert<P>) DISABLED;

    @Override
    public void disable() {
      final Alert<P> oldAlert = alert;
      alert = (Alert<P>) DISABLED;
      oldAlert.disable();
    }

    @Override
    public void notify(final int state, final P payload) {
      alert.notify(state, payload);
    }

    private void enable(@NotNull final Alert<P> alert) {
      final Alert<P> oldAlert = this.alert;
      this.alert = alert;
      oldAlert.disable();
    }
  }
}
