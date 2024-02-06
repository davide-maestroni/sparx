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

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import sparx.util.ImmutableList;
import sparx.util.Require;

public class FutureHistory {

  private static final NoHistoryStrategy<?> NO_HISTORY = new NoHistoryStrategy<Object>();

  private FutureHistory() {
  }

  public static @NotNull <V> HistoryStrategy<V> keepAll() {
    return new KeepAllStrategy<V>();
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <V> HistoryStrategy<V> noHistory() {
    return (HistoryStrategy<V>) NO_HISTORY;
  }

  public static @NotNull <V> HistoryStrategy<V> switchOnSubscribe(
      @NotNull final HistoryStrategy<V> initialStrategy,
      @NotNull final HistoryStrategy<V> finalStrategy) {
    return new SwitchOnSubscribeStrategy<V>(Require.notNull(initialStrategy, "initialStrategy"),
        Require.notNull(finalStrategy, "finalStrategy"));
  }

  public static @NotNull <V> HistoryStrategy<V> untilSubscribe(
      @NotNull final HistoryStrategy<V> initialStrategy) {
    return new SwitchOnSubscribeStrategy<V>(Require.notNull(initialStrategy, "initialStrategy"),
        FutureHistory.<V>noHistory());
  }

  private static class KeepAllStrategy<V> implements HistoryStrategy<V> {

    private final ArrayList<V> history = new ArrayList<V>();

    @Override
    public void onClear() {
      history.clear();
    }

    @Override
    public void onClose() {
    }

    // TODO: alert
    @Override
    public void onPush(final V value) {
      history.add(value);
    }

    @Override
    public void onPushBulk(@NotNull final List<V> values) {
      history.addAll(values);
    }

    @Override
    public @NotNull List<V> onSubscribe() {
      return history;
    }
  }

  private static class NoHistoryStrategy<V> implements HistoryStrategy<V> {

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

  private static class SwitchOnSubscribeStrategy<V> implements HistoryStrategy<V> {

    private final HistoryStrategy<V> strategy;
    private HistoryStrategy<V> status;

    private SwitchOnSubscribeStrategy(@NotNull final HistoryStrategy<V> initialStrategy,
        @NotNull final HistoryStrategy<V> finalStrategy) {
      status = initialStrategy;
      strategy = finalStrategy;
    }

    @Override
    public void onClear() {
      status.onClear();
    }

    @Override
    public void onClose() {
      status.onClose();
    }

    @Override
    public void onPush(final V value) {
      status.onPush(value);
    }

    @Override
    public void onPushBulk(@NotNull final List<V> values) {
      status.onPushBulk(values);
    }

    @Override
    public @NotNull List<V> onSubscribe() {
      final List<V> values = status.onSubscribe();
      status = strategy;
      return values;
    }
  }
}
