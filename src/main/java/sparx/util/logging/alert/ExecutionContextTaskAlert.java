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
package sparx.util.logging.alert;

import java.lang.reflect.Constructor;
import org.jetbrains.annotations.NotNull;
import sparx.util.logging.Log;
import sparx.util.logging.alert.Alerts.Alert;

public class ExecutionContextTaskAlert implements Alert<Object> {

  private static void checkConstructors(@NotNull final Class<?> taskClass) {
    final Constructor<?>[] ctors = taskClass.getConstructors();
    final Constructor<?>[] declaredCtors = taskClass.getDeclaredConstructors();
    if (ctors.length + declaredCtors.length != 1
        || (ctors.length > 0 && ctors[0].getParameterTypes().length != 0)
        || (declaredCtors.length > 0 && declaredCtors[0].getParameterTypes().length != 0)) {
      Log.wrn(ExecutionContextTaskAlert.class,
          "Execution context task might not be serializable, only one default constructor should be declared: %s\nPlease consider avoiding referencing external objects or modifying the task implementation.",
          taskClass);
    }
  }

  @Override
  public void disable() {
  }

  @Override
  public void notify(final int state, final Object function) {
    checkConstructors(function.getClass());
  }
}
