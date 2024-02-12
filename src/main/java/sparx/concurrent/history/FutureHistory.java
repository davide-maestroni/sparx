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
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import sparx.util.ImmutableList;
import sparx.util.Require;
import sparx.util.UncheckedException;
import sparx.util.function.Function;
import sparx.util.logging.alert.Alerts;
import sparx.util.logging.alert.Alerts.Alert;
import sparx.util.logging.alert.HistorySizeAlert;

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
    Require.notNull(initialStrategy, "initialStrategy");
    Require.notNull(finalStrategy, "finalStrategy");
    if (initialStrategy == finalStrategy) {
      return initialStrategy;
    }
    return new SwitchOnSubscribeStrategy<V>(initialStrategy, finalStrategy);
  }

  public static @NotNull <V> HistoryStrategy<V> untilSubscribe(
      @NotNull final HistoryStrategy<V> initialStrategy) {
    return switchOnSubscribe(initialStrategy, FutureHistory.<V>noHistory());
  }

  public static @NotNull <V> HistoryStrategy<V> withDropping() {
    return null;
  }

  public enum DropIteration {
    ASCENDING(new Function<Deque<?>, Iterator<?>>() {
      @Override
      public Iterator<?> apply(final Deque<?> deque) {
        return deque.iterator();
      }
    }),
    DESCENDING(new Function<Deque<?>, Iterator<?>>() {
      @Override
      public Iterator<?> apply(final Deque<?> deque) {
        return deque.descendingIterator();
      }
    });

    private final Function<Deque<?>, Iterator<?>> function;

    DropIteration(@NotNull final Function<Deque<?>, Iterator<?>> function) {
      this.function = function;
    }

    @SuppressWarnings("unchecked")
    public @NotNull <E> Iterator<E> iterator(@NotNull final Deque<E> deque) {
      try {
        return (Iterator<E>) function.apply(deque);
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }

  public enum DropTermination {

  }

  public interface DropStrategy<V> {

    @NotNull DropIteration getIteration();

    @NotNull DropTermination getTermination();

    void onClear();

    void onClose();

    void onPush(V value);

    void onPushBulk(@NotNull List<V> values);

    void onRemove(int index, V value);
  }

  private static class KeepAllStrategy<V> implements HistoryStrategy<V> {

    private static final Alert<Void> historyAlert = Alerts.get(HistorySizeAlert.class);

    private final ArrayList<V> history = new ArrayList<V>();

    @Override
    public void onClear() {
      history.clear();
    }

    @Override
    public void onClose() {
    }

    @Override
    public void onPush(final V value) {
      final ArrayList<V> history = this.history;
      history.add(value);
      historyAlert.notify(history.size(), null);
    }

    @Override
    public void onPushBulk(@NotNull final List<V> values) {
      final ArrayList<V> history = this.history;
      history.addAll(values);
      historyAlert.notify(history.size(), null);
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
