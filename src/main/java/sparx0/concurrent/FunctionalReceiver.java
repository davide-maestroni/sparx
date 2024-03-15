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
package sparx0.concurrent;

import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sparx0.util.UncheckedException;
import sparx0.util.function.Action;
import sparx0.util.function.Consumer;
import sparx0.util.logging.Log;

public class FunctionalReceiver<V> implements Receiver<V> {

  private static final Action EMPTY_ACTION = new Action() {
    @Override
    public void run() {
    }
  };
  private static final Consumer<?> EMPTY_CONSUMER = new Consumer<Object>() {
    @Override
    public void accept(final Object param) {
    }
  };

  private final Consumer<? super Collection<V>> onBulkConsumer;
  private final Action onCloseAction;
  private final Consumer<Exception> onErrorConsumer;
  private final Consumer<? super V> onValueConsumer;

  @SuppressWarnings("unchecked")
  public FunctionalReceiver(@Nullable final Consumer<? super V> onValueConsumer,
      @Nullable final Consumer<? super Collection<V>> onBulkConsumer,
      @Nullable final Consumer<Exception> onErrorConsumer, @Nullable final Action onCloseAction) {
    if (onValueConsumer != null) {
      this.onValueConsumer = onValueConsumer;
    } else {
      this.onValueConsumer = (Consumer<? super V>) EMPTY_CONSUMER;
    }
    if (onBulkConsumer != null) {
      this.onBulkConsumer = onBulkConsumer;
    } else {
      this.onBulkConsumer = (Consumer<? super Collection<V>>) EMPTY_CONSUMER;
    }
    if (onErrorConsumer != null) {
      this.onErrorConsumer = onErrorConsumer;
    } else {
      this.onErrorConsumer = (Consumer<Exception>) EMPTY_CONSUMER;
    }
    this.onCloseAction = onCloseAction != null ? onCloseAction : EMPTY_ACTION;
    if (((onValueConsumer == null) || (onBulkConsumer == null))
        && (onValueConsumer != onBulkConsumer)) {
      if (onValueConsumer != null) {
        Log.wrn(FunctionalReceiver.class,
            "Bulk values handler implementation is missing: single value is handled by '%s'\nPlease consider providing a proper consumer.",
            onValueConsumer);
      } else {
        Log.wrn(FunctionalReceiver.class,
            "Single value handler implementation is missing: bulk values are handled by '%s'\nPlease consider providing a proper consumer.",
            onBulkConsumer);
      }
    }
  }

  @Override
  public void close() {
    try {
      onCloseAction.run();
    } catch (final Exception e) {
      throw UncheckedException.throwUnchecked(e);
    }
  }

  @Override
  public boolean fail(@NotNull final Exception error) {
    try {
      onErrorConsumer.accept(error);
      return true;
    } catch (final Exception e) {
      throw UncheckedException.throwUnchecked(e);
    }
  }

  @Override
  public void set(final V value) {
    try {
      onValueConsumer.accept(value);
    } catch (final Exception e) {
      throw UncheckedException.throwUnchecked(e);
    }
  }

  @Override
  public void setBulk(@NotNull final Collection<V> values) {
    try {
      onBulkConsumer.accept(values);
    } catch (final Exception e) {
      throw UncheckedException.throwUnchecked(e);
    }
  }
}