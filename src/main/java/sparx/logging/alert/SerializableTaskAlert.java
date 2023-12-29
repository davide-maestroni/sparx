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

import java.lang.reflect.Constructor;
import org.jetbrains.annotations.NotNull;
import sparx.function.Consumer;
import sparx.function.Function;
import sparx.logging.Log;

class SerializableTaskAlert implements ExecutionContextTaskAlert {

  private static void checkConstructors(@NotNull final Class<?> taskClass) {
    final Constructor<?>[] constructors = taskClass.getConstructors();
    if (constructors.length != 1 || constructors[0].getParameterTypes().length != 0) {
      Log.wrn(ExecutionContextTaskAlert.class,
          "Execution context task might not be serializable, only one default constructor should be declared: %s\nPlease consider avoiding referencing external objects or modifying the task implementation.",
          taskClass);
    }
  }

  SerializableTaskAlert() {
  }

  @Override
  public void notifyCall(@NotNull final Function<?, ?> function) {
    checkConstructors(function.getClass());
  }

  @Override
  public void notifyRun(@NotNull final Consumer<?> consumer) {
    checkConstructors(consumer.getClass());
  }
}
