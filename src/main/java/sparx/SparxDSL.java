package sparx;

import java.util.ArrayList;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.CoupleFuture;
import sparx.concurrent.ExecutionContext;
import sparx.concurrent.Receiver;
import sparx.concurrent.SignalFuture;
import sparx.concurrent.StreamingFuture;
import sparx.concurrent.TripleFuture;
import sparx.concurrent.TupleFuture;
import sparx.concurrent.UncheckedException;
import sparx.concurrent.VarFuture;
import sparx.function.Action;
import sparx.function.BiFunction;
import sparx.function.Consumer;
import sparx.function.Function;
import sparx.function.Predicate;
import sparx.tuple.Couple;
import sparx.tuple.Triple;
import sparx.util.Nothing;

public class SparxDSL {

  public static @NotNull <V, F extends TupleFuture<V, ?>> Function<F, StreamingFuture<Nothing>> inContext(
      ExecutionContext context, Consumer<? super F> consumer) {
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

  public static @NotNull <V, U> Function<SignalFuture<V>, StreamingFuture<U>> concatMap(
      Function<? super V, ? extends SignalFuture<U>> function) {
    return null;
  }

  public static @NotNull <V, U> Function<SignalFuture<V>, StreamingFuture<U>> concatMap(
      int maxConcurrency, Function<? super V, ? extends SignalFuture<U>> function) {
    return null;
  }

  public static @NotNull Function<SignalFuture<?>, StreamingFuture<Nothing>> doFinally(
      Action action) {
    return null;
  }

  public static @NotNull <V> Function<SignalFuture<V>, StreamingFuture<Nothing>> doOnce(
      Consumer<? super V> consumer) {
    return null;
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

  public static @NotNull <V> Function<SignalFuture<V>, StreamingFuture<V>> buffer(
      final int maxSize) {
    return new Function<SignalFuture<V>, StreamingFuture<V>>() {
      @Override
      public StreamingFuture<V> apply(final SignalFuture<V> input) {
        final VarFuture<V> future = VarFuture.create();
        input.subscribe(new Receiver<V>() {

          private final ArrayList<V> buffer = new ArrayList<V>();

          @Override
          public boolean fail(@NotNull final Exception error) {
            return future.fail(error);
          }

          @Override
          public void set(final V value) {
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
            if (!buffer.isEmpty()) {
              future.setBulk(buffer);
            }
            future.close();
          }
        });
        return future.readOnly();
      }
    };
  }

  public static @NotNull <V, U> Function<SignalFuture<V>, StreamingFuture<U>> map(
      @NotNull final Function<? super V, ? extends U> function) {
    return new Function<SignalFuture<V>, StreamingFuture<U>>() {
      @Override
      public StreamingFuture<U> apply(final SignalFuture<V> input) {
        final VarFuture<U> future = VarFuture.create();
        input.subscribe(new Receiver<V>() {
          @Override
          public boolean fail(@NotNull final Exception error) {
            return future.fail(error);
          }

          @Override
          public void set(final V value) {
            try {
              future.set(function.apply(value));
            } catch (final Exception e) {
              throw UncheckedException.toUnchecked(e);
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
              throw UncheckedException.toUnchecked(e);
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

  public static @NotNull <V, U> Function<SignalFuture<V>, StreamingFuture<U>> foldLeft(
      final U identity, @NotNull final BiFunction<? super U, ? super V, ? extends U> function) {
    return new Function<SignalFuture<V>, StreamingFuture<U>>() {
      @Override
      public StreamingFuture<U> apply(final SignalFuture<V> input) {
        final VarFuture<U> future = VarFuture.create();
        input.subscribe(new Receiver<V>() {

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
              throw UncheckedException.toUnchecked(e);
            }
          }

          @Override
          public void setBulk(@NotNull final Collection<V> values) {
            try {
              for (final V value : values) {
                accumulated = function.apply(accumulated, value);
              }
            } catch (final Exception e) {
              throw UncheckedException.toUnchecked(e);
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
