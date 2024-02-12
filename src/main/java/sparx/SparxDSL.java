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
package sparx;

import java.util.ArrayList;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.concurrent.Receiver;
import sparx.concurrent.Signal;
import sparx.concurrent.SignalFuture;
import sparx.concurrent.StreamingFuture;
import sparx.concurrent.TupleFuture;
import sparx.concurrent.VarFuture;
import sparx.concurrent.tuple.CoupleFuture;
import sparx.concurrent.tuple.TripleFuture;
import sparx.util.Nothing;
import sparx.util.UncheckedException;
import sparx.util.function.Action;
import sparx.util.function.BinaryFunction;
import sparx.util.function.Consumer;
import sparx.util.function.Function;
import sparx.util.function.Predicate;
import sparx.util.tuple.Couple;
import sparx.util.tuple.Triple;

public class SparxDSL {

  /*
   take(int)
   takeWhile
   takeUntil
   keep(int)
   keepWhile
   keepUntil
   skip(int)
   skipWhile
   skipUntil
   drop(int)
   dropWhile
   dropUntil
   filter
   map(v -> r) <= Future<V> => Future<R>
   any
   all
   none
   notAll ??? // at least one fail
   //  take(int1, int2, ...), ..., map(v1 -> r1, v2 -> r2, ...) ???
   flatten  <= Future<Collection> => Future<V>
   combineMap(missingValue, onFailure) <= TupleFuture => Future<Tuple>
   concatMap(concurrency, v => future) <= Future<V> => Future<R>
   mergeMap
   switchMap
   exceptionallyMap
   race
   spread(future -> future1, future -> future2, ...) <= Future => TupleFuture
   weakSubscribe
   autoUnsubscribe // when receiver future is done unsubscribe
   doOnce
   doForEach
   doFinally
   doExceptionally
   do* // then(until(), do()) ?????
   repeat* ?????
   until ???
   while ???
   [submitTo/submit](context, <function>) <= TupleFuture // all futures are inputs
   withHistory
   */

  public static @NotNull <V, F extends TupleFuture<V, ?>, U> Function<F, StreamingFuture<U>> callInContext(
      @NotNull final ExecutionContext context,
      @NotNull final Function<? super F, ? extends SignalFuture<U>> function) {
    return null;
  }

  public static @NotNull <V, F extends TupleFuture<V, ?>> Function<F, StreamingFuture<Nothing>> runInContext(
      @NotNull final ExecutionContext context, @NotNull final Consumer<? super F> consumer) {
    return null;
  }

  public static @NotNull <V, V1 extends V, V2 extends V> Function<CoupleFuture<V, V1, V2>, StreamingFuture<Couple<V, V1, V2>>> combineLatestBinary(
      V1 firstDefaultValue, V2 secondDefaultValue) {
    return null;
  }

  public static @NotNull <V, V1 extends V, V2 extends V, V3 extends V> Function<TripleFuture<V, V1, V2, V3>, StreamingFuture<Triple<V, V1, V2, V3>>> combineLatestTernary(
      V1 firstDefaultValue, V2 secondDefaultValue, V3 thirdDefaultValue) {
    return null;
  }

  public static @NotNull <V, V1 extends V, V2 extends V> Function<CoupleFuture<V, V1, V2>, StreamingFuture<Couple<V, V1, V2>>> joinBinary() {
    return null;
  }

  public static @NotNull <V, V1 extends V, V2 extends V, V3 extends V> Function<TripleFuture<V, V1, V2, V3>, StreamingFuture<Triple<V, V1, V2, V3>>> joinTernary() {
    return null;
  }

  public static @NotNull <V, V1 extends V, V2 extends V> Function<CoupleFuture<V, V1, V2>, StreamingFuture<V>> mergeBinary() {
    return null;
  }

  public static @NotNull <V, V1 extends V, V2 extends V, V3 extends V> Function<TripleFuture<V, V1, V2, V3>, StreamingFuture<V>> mergeTernary() {
    return null;
  }

  public static @NotNull <V, U> Function<Signal<V>, StreamingFuture<U>> concatMap(
      Function<? super V, ? extends SignalFuture<U>> function) {
    return null;
  }

  public static @NotNull <V, U> Function<Signal<V>, StreamingFuture<U>> concatMap(
      int maxConcurrency, Function<? super V, ? extends SignalFuture<U>> function) {
    return null;
  }

  public static @NotNull <V> Function<Signal<V>, StreamingFuture<V>> doFinally(
      Action action) {
    return null;
  }

  public static @NotNull <V> Function<Signal<V>, StreamingFuture<Nothing>> doOnce(
      final Consumer<? super V> consumer) {
    return new Function<Signal<V>, StreamingFuture<Nothing>>() {
      @Override
      public StreamingFuture<Nothing> apply(final Signal<V> signal) {
        final VarFuture<Nothing> future = VarFuture.create();
        signal.subscribe(new Receiver<V>() {
          @Override
          public void close() {
            future.close();
          }

          @Override
          public boolean fail(@NotNull final Exception error) {
            return future.fail(error);
          }

          @Override
          public void set(final V value) {
            try {
              consumer.accept(value);
              future.close();
            } catch (final Exception e) {
              future.fail(e);
            } finally {
              signal.unsubscribe(this);
            }
          }

          @Override
          public void setBulk(@NotNull final Collection<V> values) {
            if (!values.isEmpty()) {
              try {
                consumer.accept(values.iterator().next());
                future.close();
              } catch (final Exception e) {
                future.fail(e);
              } finally {
                signal.unsubscribe(this);
              }
            }
          }
        });
        return future.readOnly();
      }
    };
  }

  public static @NotNull <V> Function<SignalFuture<V>, StreamingFuture<Nothing>> doUntil(
      Predicate<? super V> predicate) {
    return null;
  }

  public static @NotNull <V> Function<SignalFuture<V>, StreamingFuture<Nothing>> doUntilFuture(
      Function<? super V, ? extends SignalFuture<Boolean>> function) {
    return null;
  }

  public static @NotNull <V> Function<SignalFuture<V>, StreamingFuture<Nothing>> doWhen(
      Consumer<? super V> consumer) {
    return null;
  }

  public static @NotNull <V> Function<SignalFuture<V>, StreamingFuture<Nothing>> doWhile(
      Predicate<? super V> predicate) {
    return null;
  }

  public static @NotNull <V> Function<SignalFuture<V>, StreamingFuture<Nothing>> doWhileFuture(
      Function<? super V, ? extends SignalFuture<Boolean>> function) {
    return null;
  }

  public static @NotNull <V> Function<SignalFuture<V>, StreamingFuture<Nothing>> repeatUntil(
      Predicate<? super StreamingFuture<V>> predicate) {
    return null;
  }

  public static @NotNull <V> Function<SignalFuture<V>, StreamingFuture<Nothing>> repeatUntilFuture(
      Function<? super StreamingFuture<V>, ? extends SignalFuture<Boolean>> function) {
    return null;
  }

  public static @NotNull <V> Function<SignalFuture<V>, StreamingFuture<Nothing>> repeatWhile(
      Predicate<? super StreamingFuture<V>> predicate) {
    return null;
  }

  public static @NotNull <V> Function<SignalFuture<V>, StreamingFuture<Nothing>> repeatWhileFuture(
      Function<? super StreamingFuture<V>, ? extends SignalFuture<Boolean>> function) {
    return null;
  }

  public static @NotNull <V> Function<Signal<V>, StreamingFuture<V>> buffer(
      final int maxSize) {
    return new Function<Signal<V>, StreamingFuture<V>>() {
      @Override
      public StreamingFuture<V> apply(final Signal<V> signal) {
        final VarFuture<V> future = VarFuture.create();
        if (maxSize == 1) {
          signal.subscribe(new Receiver<V>() {

            @Override
            public boolean fail(@NotNull final Exception error) {
              return future.fail(error);
            }

            @Override
            public void set(final V value) {
              future.set(value);
            }

            @Override
            public void setBulk(@NotNull final Collection<V> values) {
              for (final V value : values) {
                future.set(value);
              }
            }

            @Override
            public void close() {
              future.close();
            }
          });
        } else {
          signal.subscribe(new Receiver<V>() {

            private final ArrayList<V> buffer = new ArrayList<V>(maxSize);

            @Override
            public boolean fail(@NotNull final Exception error) {
              return future.fail(error);
            }

            @Override
            public void set(final V value) {
              final ArrayList<V> buffer = this.buffer;
              buffer.add(value);
              if (buffer.size() == maxSize) {
                future.setBulk(buffer);
                buffer.clear();
              }
            }

            @Override
            public void setBulk(@NotNull final Collection<V> values) {
              final ArrayList<V> buffer = this.buffer;
              for (final V value : values) {
                buffer.add(value);
                if (buffer.size() == maxSize) {
                  future.setBulk(buffer);
                  buffer.clear();
                }
              }
            }

            @Override
            public void close() {
              final ArrayList<V> buffer = this.buffer;
              if (!buffer.isEmpty()) {
                future.setBulk(buffer);
              }
              future.close();
            }
          });
        }
        return future.readOnly();
      }
    };
  }

  public static @NotNull <V, U> Function<Signal<V>, StreamingFuture<U>> map(
      @NotNull final Function<? super V, ? extends U> function) {
    return new Function<Signal<V>, StreamingFuture<U>>() {
      @Override
      public StreamingFuture<U> apply(final Signal<V> signal) {
        final VarFuture<U> future = VarFuture.create();
        signal.subscribe(new Receiver<V>() {
          @Override
          public boolean fail(@NotNull final Exception error) {
            return future.fail(error);
          }

          @Override
          public void set(final V value) {
            try {
              future.set(function.apply(value));
            } catch (final Exception e) {
              throw UncheckedException.throwUnchecked(e);
            }
          }

          @Override
          public void setBulk(@NotNull final Collection<V> values) {
            try {
              final ArrayList<U> outputs = new ArrayList<U>(values.size());
              for (final V value : values) {
                outputs.add(function.apply(value));
              }
              future.setBulk(outputs);
            } catch (final Exception e) {
              throw UncheckedException.throwUnchecked(e);
            }
          }

          @Override
          public void close() {
            future.close();
          }
        });
        return future.readOnly();
      }
    };
  }

  public static @NotNull <V, U> Function<Signal<V>, StreamingFuture<U>> foldLeft(
      final U identity, @NotNull final BinaryFunction<? super U, ? super V, ? extends U> function) {
    return new Function<Signal<V>, StreamingFuture<U>>() {
      @Override
      public StreamingFuture<U> apply(final Signal<V> signal) {
        final VarFuture<U> future = VarFuture.create();
        signal.subscribe(new Receiver<V>() {

          private U accumulated = identity;

          @Override
          public boolean fail(@NotNull final Exception error) {
            return future.fail(error);
          }

          @Override
          public void set(final V value) {
            try {
              accumulated = function.apply(accumulated, value);
            } catch (final Exception e) {
              throw UncheckedException.throwUnchecked(e);
            }
          }

          @Override
          public void setBulk(@NotNull final Collection<V> values) {
            try {
              for (final V value : values) {
                accumulated = function.apply(accumulated, value);
              }
            } catch (final Exception e) {
              throw UncheckedException.throwUnchecked(e);
            }
          }

          @Override
          public void close() {
            future.set(accumulated);
            future.close();
          }
        });
        return future.readOnly();
      }
    };
  }
}
