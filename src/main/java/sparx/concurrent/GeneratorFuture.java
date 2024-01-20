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
import java.util.LinkedList;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.Scheduler.Task;
import sparx.function.Function;
import sparx.function.Predicate;
import sparx.function.Supplier;
import sparx.logging.Log;
import sparx.tuple.Couple;
import sparx.util.Requires;

public class GeneratorFuture<V> extends GeneratorStreamGroupFuture<V> {

  public static @NotNull <V> StreamingFuture<V> forLoop(final V initialValue,
      @NotNull final Predicate<? super V> predicate,
      @NotNull final Function<? super V, ? extends V> function) {
    return of(new Supplier<SignalFuture<V>>() {
      private V value = initialValue;

      @Override
      public SignalFuture<V> get() throws Exception {
        return supplier.get();
      }

      private Supplier<SignalFuture<V>> supplier = new Supplier<SignalFuture<V>>() {
        @Override
        public SignalFuture<V> get() throws Exception {
          if (predicate.test(value)) {
            supplier = new Supplier<SignalFuture<V>>() {
              @Override
              public SignalFuture<V> get() throws Exception {
                if (predicate.test(value)) {
                  value = function.apply(value);
                  return ValFuture.of(value);
                }
                return null;
              }
            };
            return ValFuture.of(value);
          }
          return null;
        }
      };
    });
  }

  // TODO: of(Iterable), of(Iterator)

  public static @NotNull <V> StreamingFuture<V> of(
      @NotNull final Supplier<? extends SignalFuture<V>> supplier) {
    return new GeneratorFuture<V>(new PullFuture<V>(supplier));
  }

  public static @NotNull <V> StreamingFuture<V> of(final V initialValue,
      @NotNull final Function<? super V, Couple<Object, ? extends V, ? extends SignalFuture<V>>> function) {
    return of(new Supplier<SignalFuture<V>>() {
      private V value = initialValue;

      @Override
      public SignalFuture<V> get() throws Exception {
        final Couple<Object, ? extends V, ? extends SignalFuture<V>> couple = function.apply(value);
        if (couple != null) {
          value = couple.getFirst();
          return couple.getSecond();
        }
        return null;
      }
    });
  }

  private GeneratorFuture(@NotNull final StreamingFuture<V> future) {
    super(future);
  }

  private static class PullFuture<V> extends VarFuture<V> {

    private final Supplier<? extends SignalFuture<V>> supplier;
    private final LinkedList<V> pendingValues = new LinkedList<V>();
    private final GeneratorReceiver receiver = new GeneratorReceiver();

    private boolean pullFromIterator = false;
    private boolean pullFromJoin = false;
    private boolean pullFromReceiver = false;

    private PullFuture(@NotNull final Supplier<? extends SignalFuture<V>> supplier) {
      this.supplier = Requires.notNull(supplier, "supplier");
    }

    @Override
    protected void pullFromIterator() {
      scheduler().scheduleBefore(new PullTask() {
        @Override
        public void run() {
          final boolean wasPull = isPull();
          pullFromIterator = true;
          if (!wasPull) {
            pull();
          }
        }
      });
    }

    @Override
    protected void pullFromJoinStart() {
      scheduler().scheduleBefore(new PullTask() {
        @Override
        public void run() {
          final boolean wasPull = isPull();
          pullFromJoin = true;
          if (!wasPull) {
            pull();
          }
        }
      });
    }

    @Override
    protected void pullFromJoinStop() {
      scheduler().scheduleBefore(new PullTask() {
        @Override
        public void run() {
          pullFromJoin = false;
        }
      });
    }

    @Override
    protected void pullFromExistingReceiver() {
      scheduler().scheduleAfter(new PullTask() {
        @Override
        public void run() {
          if (hasSinks()) {
            pullFromNewReceiver();
          } else {
            pullFromReceiver = false;
          }
        }
      });
    }

    @Override
    protected void pullFromNewReceiver() {
      final boolean wasPull = isPull();
      pullFromReceiver = true;
      if (!wasPull) {
        pull();
      }
    }

    private void consumeValues() {
      final LinkedList<V> pendingValues = this.pendingValues;
      if (!pendingValues.isEmpty()) {
        if (pullFromJoin || pullFromReceiver) {
          setBulk(pendingValues);
          pendingValues.clear();
          pullFromReceiver = false;
          pullFromIterator = false;
        } else if (pullFromIterator) {
          set(pendingValues.removeFirst());
          pullFromIterator = false;
        }
      }
    }

    private boolean isPull() {
      return pullFromJoin || pullFromReceiver || pullFromIterator;
    }

    private void pull() {
      consumeValues();
      if (isPull()) {
        try {
          final SignalFuture<V> future = supplier.get();
          if (future == null) {
            close();
          } else {
            future.subscribe(receiver);
          }
        } catch (final Exception e) {
          Log.err(GeneratorFuture.class, "Failed to generate new values: %s", Log.printable(e));
          fail(e);
        }
      }
    }

    private class GeneratorReceiver implements Receiver<V> {

      @Override
      public void close() {
      }

      @Override
      public boolean fail(@NotNull final Exception error) {
        return PullFuture.this.fail(error);
      }

      @Override
      public void set(final V value) {
        pendingValues.add(value);
        consumeValues();
      }

      @Override
      public void setBulk(@NotNull final Collection<V> values) {
        pendingValues.addAll(values);
        consumeValues();
      }
    }

    private abstract class PullTask implements Task {

      @Override
      public @NotNull String taskID() {
        return PullFuture.super.taskID();
      }

      @Override
      public int weight() {
        return 1;
      }
    }
  }
}
