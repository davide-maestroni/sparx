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
package sparx.internal.future;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ContextTask;
import sparx.concurrent.ExecutionContext;

public class ContextFutureConsumer<P> implements FutureConsumer<P> {

  private final ExecutionContext context;
  private final Logger logger;
  private final String taskID;
  private final FutureConsumer<P> wrapped;

  public ContextFutureConsumer(@NotNull final ExecutionContext context,
      @NotNull final String taskID,
      @NotNull final FutureConsumer<P> wrapped, @NotNull final Logger logger) {
    this.context = context;
    this.taskID = taskID;
    this.wrapped = wrapped;
    this.logger = logger;
  }

  @Override
  public void accept(final P param) {
    context.scheduleAfter(new ContextTask(context) {
      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      protected void runWithContext() {
        try {
          wrapped.accept(param);
        } catch (final Exception error) {
          try {
            wrapped.error(error);
          } catch (final Exception e) {
            if (e instanceof InterruptedException) {
              Thread.currentThread().interrupt();
            }
            logger.log(Level.SEVERE, "Ignored exception", e);
          }
        }
      }
    });
  }

  @Override
  public void error(@NotNull final Exception error) {
    context.scheduleAfter(new ContextTask(context) {
      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      protected void runWithContext() {
        try {
          wrapped.error(error);
        } catch (final Exception e) {
          if (e instanceof InterruptedException) {
            Thread.currentThread().interrupt();
          }
          logger.log(Level.SEVERE, "Ignored exception", e);
        }
      }
    });
  }
}
