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
package sparx.concurrent;

import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.FutureGroup.GroupReceiver;
import sparx.logging.Log;
import sparx.util.Requires;

class StandardGroupReceiver<V> implements GroupReceiver<V> {

  private final StreamingFuture<?> future;
  private final Receiver<V> receiver;

  StandardGroupReceiver(@NotNull final StreamingFuture<?> future,
      @NotNull final Receiver<V> receiver) {
    this.future = Requires.notNull(future, "future");
    this.receiver = Requires.notNull(receiver, "receiver");
  }

  @Override
  public boolean isSink() {
    return true;
  }

  @Override
  public void onUnsubscribe() {
  }

  @Override
  public void onUncaughtError(@NotNull final Exception error) {
    Log.err(GroupReceiver.class,
        "Uncaught exception, the throwing receiver will be automatically unsubscribed: %s",
        Log.printable(error));
    future.unsubscribe(receiver);
  }

  @Override
  public void close() {
    receiver.close();
  }

  @Override
  public boolean fail(@NotNull final Exception error) {
    return receiver.fail(error);
  }

  @Override
  public void set(final V value) {
    receiver.set(value);
  }

  @Override
  public void setBulk(@NotNull final Collection<V> values) {
    receiver.setBulk(values);
  }
}
