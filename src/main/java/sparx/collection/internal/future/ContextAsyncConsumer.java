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
package sparx.collection.internal.future;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.concurrent.ExecutionContext.Task;
import sparx.util.Require;

public class ContextAsyncConsumer<P> implements AsyncConsumer<P> {

  private static final Logger LOGGER = Logger.getLogger(ContextAsyncConsumer.class.getName());

  private final ExecutionContext context;
  private final Logger logger;
  private final AsyncConsumer<P> wrapped;

  public ContextAsyncConsumer(@NotNull final ExecutionContext context,
      @NotNull final AsyncConsumer<P> wrapped) {
    this(context, wrapped, LOGGER);
  }

  public ContextAsyncConsumer(@NotNull final ExecutionContext context,
      @NotNull final AsyncConsumer<P> wrapped, @NotNull final Logger logger) {
    this.context = Require.notNull(context, "context");
    this.wrapped = Require.notNull(wrapped, "wrapped");
    this.logger = Require.notNull(logger, "logger");
  }

  @Override
  public void accept(final P param) throws Exception {
    context.scheduleAfter(new Task() {
      @Override
      public @NotNull String taskID() {
        return "";
      }

      @Override
      public int weight() {
        return 1;
      }

      @Override
      public void run() {
        try {
          wrapped.accept(param);
        } catch (final Exception e) {
          try {
            wrapped.error(e);
          } catch (final Exception ex) {
            logger.log(Level.SEVERE, "Ignored exception", ex);
          }
        }
      }
    });
  }

  @Override
  public void error(@NotNull final Exception error) throws Exception {
    context.scheduleAfter(new Task() {
      @Override
      public @NotNull String taskID() {
        return "";
      }

      @Override
      public int weight() {
        return 1;
      }

      @Override
      public void run() {
        try {
          wrapped.error(error);
        } catch (final Exception e) {
          logger.log(Level.SEVERE, "Ignored exception", e);
        }
      }
    });
  }
}
