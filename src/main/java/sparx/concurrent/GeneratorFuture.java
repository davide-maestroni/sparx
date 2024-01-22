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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.Scheduler.Task;
import sparx.function.Function;
import sparx.function.Predicate;
import sparx.function.Supplier;
import sparx.logging.Log;
import sparx.tuple.Couple;
import sparx.util.ImmutableList;
import sparx.util.Requires;

public class GeneratorFuture<V> extends GeneratorStreamGroupFuture<V> {

  public static @NotNull <V> GeneratorFuture<V> forLoop(final V initialValue,
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

  public static @NotNull <V> GeneratorFuture<V> of(@NotNull final Iterable<? extends V> iterable) {
    return of(iterable.iterator());
  }

  public static @NotNull <V> GeneratorFuture<V> of(@NotNull final Iterator<? extends V> iterator) {
    return of(new Supplier<SignalFuture<V>>() {
      @Override
      public SignalFuture<V> get() {
        if (iterator.hasNext()) {
          return ValFuture.of(iterator.next());
        }
        return null;
      }
    });
  }

  public static @NotNull <V> GeneratorFuture<V> of(
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

  private GeneratorFuture(@NotNull final PullFuture<V> future) {
    super(future);
  }

  @Override
  protected @NotNull StreamingFuture<V> wrapped() {
    return this;
  }

  @Override
  void pull(@NotNull final Receiver<V> receiver) {
    ((PullFuture<V>) super.wrapped()).pull(receiver);
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

    private void pull(@NotNull final Receiver<V> receiver) {
      scheduler().scheduleAfter(new PullTask() {
        @Override
        public void run() {
          final LinkedList<V> pendingValues = PullFuture.this.pendingValues;
          if (!pendingValues.isEmpty()) {
            receiver.setBulk(ImmutableList.ofElementsIn(pendingValues));
            pendingValues.clear();
          } else if (isPull()) {
            PullFuture.this.receiver.addTempReceiver(receiver);
          } else {
            try {
              final SignalFuture<V> future = supplier.get();
              if (future == null) {
                close();
              } else {
                final GeneratorReceiver generatorReceiver = PullFuture.this.receiver;
                generatorReceiver.addTempReceiver(receiver);
                future.subscribe(generatorReceiver);
              }
            } catch (final Exception e) {
              Log.err(GeneratorFuture.class, "Failed to generate new values: %s", Log.printable(e));
              fail(e);
            }
          }
        }
      });
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

      private final ArrayList<Receiver<V>> receivers = new ArrayList<Receiver<V>>();

      @Override
      public void close() {
      }

      @Override
      public boolean fail(@NotNull final Exception error) {
        scheduler().scheduleAfter(new PullTask() {
          @Override
          public void run() {
            PullFuture.this.fail(error);
          }
        });
        return true;
      }

      @Override
      public void set(final V value) {
        scheduler().scheduleAfter(new PullTask() {
          @Override
          public void run() {
            pendingValues.add(value);
            for (final Receiver<V> receiver : receivers) {
              receiver.setBulk(pendingValues);
            }
            consumeValues();
            if (!receivers.isEmpty()) {
              receivers.clear();
              pendingValues.clear();
            }
          }
        });
      }

      @Override
      public void setBulk(@NotNull final Collection<V> values) {
        scheduler().scheduleAfter(new PullTask() {
          @Override
          public void run() {
            pendingValues.addAll(values);
            for (final Receiver<V> receiver : receivers) {
              receiver.setBulk(pendingValues);
            }
            consumeValues();
            if (!receivers.isEmpty()) {
              receivers.clear();
              pendingValues.clear();
            }
          }
        });
      }

      private void addTempReceiver(@NotNull final Receiver<V> receiver) {
        receivers.add(receiver);
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
