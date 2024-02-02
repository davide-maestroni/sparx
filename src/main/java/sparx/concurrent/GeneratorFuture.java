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
import java.util.Iterator;
import java.util.LinkedList;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.Scheduler.Task;
import sparx.function.Function;
import sparx.function.Predicate;
import sparx.function.Supplier;
import sparx.logging.Log;
import sparx.util.Require;

public class GeneratorFuture<V> extends GeneratorScopeFuture<V> {

  private GeneratorFuture(@NotNull final Supplier<? extends SignalFuture<V>> supplier) {
    super(new PullFuture<V>(supplier));
  }

  public static @NotNull <V> GeneratorFuture<V> of(@NotNull final Iterable<? extends V> iterable) {
    return of(iterable.iterator());
  }

  public static @NotNull <V> GeneratorFuture<V> of(@NotNull final Iterator<? extends V> iterator) {
    Require.notNull(iterator, "iterator");
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
    return new GeneratorFuture<V>(Require.notNull(supplier, "supplier"));
  }

  public static @NotNull <V> GeneratorFuture<V> ofDeferred(
      @NotNull final Supplier<? extends SignalFuture<V>> supplier) {
    Require.notNull(supplier, "supplier");
    return of(new Supplier<SignalFuture<V>>() {
      @Override
      public SignalFuture<V> get() throws Exception {
        return status.get();
      }

      private Supplier<SignalFuture<V>> status = new Supplier<SignalFuture<V>>() {
        @Override
        public SignalFuture<V> get() throws Exception {
          status = new Supplier<SignalFuture<V>>() {
            @Override
            public SignalFuture<V> get() {
              return null;
            }
          };
          return supplier.get();
        }
      };
    });
  }

  public static @NotNull <V> GeneratorFuture<V> ofLoop(final V initialValue,
      @NotNull final Predicate<? super V> predicate,
      @NotNull final Function<? super V, ? extends V> function) {
    Require.notNull(predicate, "predicate");
    Require.notNull(function, "function");
    return of(new Supplier<SignalFuture<V>>() {
      private V value = initialValue;

      @Override
      public SignalFuture<V> get() throws Exception {
        return status.get();
      }

      private Supplier<SignalFuture<V>> status = new Supplier<SignalFuture<V>>() {
        @Override
        public SignalFuture<V> get() throws Exception {
          if (predicate.test(value)) {
            status = new Supplier<SignalFuture<V>>() {
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

  @Override
  protected @NotNull StreamingFuture<V> wrapped() {
    return this;
  }

  @Override
  @NotNull <U> GeneratingFuture<U> createGeneratingFuture(
      @NotNull final Supplier<? extends SignalFuture<U>> supplier) {
    return new GeneratorFuture<U>(Require.notNull(supplier, "supplier"));
  }

  private static class PullFuture<V> extends VarFuture<V> {

    private final LinkedList<V> pendingValues = new LinkedList<V>();
    private final GeneratorReceiver receiver = new GeneratorReceiver();
    private final Supplier<? extends SignalFuture<V>> supplier;
    private final String taskID = toString();

    private boolean pullFromIterator = false;
    private boolean pullFromJoin = false;
    private boolean pullFromReceiver = false;

    private PullFuture(@NotNull final Supplier<? extends SignalFuture<V>> supplier) {
      this.supplier = supplier;
    }

    @Override
    protected void pullFromIterator() {
      scheduler().scheduleBefore(new PullTask() {
        @Override
        public void run() {
          final boolean wasPull = isPull();
          pullFromIterator = true;
          if (!wasPull) {
            internalPull();
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
            internalPull();
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
    protected void pullFromReceiver() {
      scheduler().scheduleAfter(new PullTask() {
        @Override
        public void run() {
          if (hasConsumers()) {
            final boolean wasPull = isPull();
            pullFromReceiver = true;
            if (!wasPull) {
              internalPull();
            }
          } else {
            pullFromReceiver = false;
          }
        }
      });
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

    private void internalPull() {
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
        scheduler().scheduleAfter(new PullTask() {
          @Override
          public void run() {
            internalPull();
          }
        });
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
            consumeValues();
          }
        });
      }

      @Override
      public void setBulk(@NotNull final Collection<V> values) {
        scheduler().scheduleAfter(new PullTask() {
          @Override
          public void run() {
            pendingValues.addAll(values);
            consumeValues();
          }
        });
      }
    }

    private abstract class PullTask implements Task {

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        return 1;
      }
    }
  }
}
