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
package sparx.concurrent.history;

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import sparx.util.ImmutableList;
import sparx.util.Require;

public class Histories {

  private static final NoHistory<?> NO_HISTORY = new NoHistory<Object>();

  private Histories() {
  }

  public static @NotNull <V> SignalHistory<V> keepAll() {
    return new KeepAllHistory<V>();
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <V> SignalHistory<V> noHistory() {
    return (SignalHistory<V>) NO_HISTORY;
  }

  public static @NotNull <V> SignalHistory<V> switchOnSubscribe(
      @NotNull final SignalHistory<V> initialStrategy,
      @NotNull final SignalHistory<V> finalStrategy) {
    Require.notNull(initialStrategy, "initialStrategy");
    Require.notNull(finalStrategy, "finalStrategy");
    if (initialStrategy == finalStrategy) {
      return initialStrategy;
    }
    return new SwitchOnSubscribeHistory<V>(initialStrategy, finalStrategy);
  }

  public static @NotNull <V> SignalHistory<V> untilSubscribe(
      @NotNull final SignalHistory<V> initialStrategy) {
    return switchOnSubscribe(initialStrategy, Histories.<V>noHistory());
  }

  public static @NotNull <V> SignalHistory<V> dropAfter(final long timeout,
      @NotNull final TimeUnit unit) {
    return new DropAfterHistory<V>(timeout, unit);
  }

  public static @NotNull <V> SignalHistory<V> dropNewer(final int maxSize) {
    return new DropNewerHistory<V>(maxSize);
  }

  public static @NotNull <V> SignalHistory<V> dropNewerOrAfter(final int maxSize,
      final long timeout, @NotNull final TimeUnit unit) {
    return new DropNewerOrAfterHistory<V>(maxSize, timeout, unit);
  }

  public static @NotNull <V> SignalHistory<V> dropOlder(final int maxSize) {
    return new DropOlderHistory<V>(maxSize);
  }

  public static @NotNull <V> SignalHistory<V> dropOlderAfter(final int maxSize,
      final long timeout, @NotNull final TimeUnit unit) {
    return new DropOlderAndAfterHistory<V>(maxSize, timeout, unit);
  }

  public static @NotNull <V> SignalHistory<V> dropOlderOrAfter(final int maxSize,
      final long timeout, @NotNull final TimeUnit unit) {
    return new DropOlderOrAfterHistory<V>(maxSize, timeout, unit);
  }

  private static class NoHistory<V> implements SignalHistory<V> {

    @Override
    public void onClear() {
    }

    @Override
    public void onClose() {
    }

    @Override
    public void onPush(final V value) {
    }

    @Override
    public void onPushBulk(@NotNull final List<V> values) {
    }

    @Override
    public @NotNull List<V> onSubscribe() {
      return ImmutableList.of();
    }
  }
}
