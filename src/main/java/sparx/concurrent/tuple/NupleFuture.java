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
package sparx.concurrent.tuple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.StreamingFuture;
import sparx.concurrent.TupleFuture;
import sparx.function.Function;
import sparx.tuple.Tuple;
import sparx.util.ImmutableList;
import sparx.util.UncheckedException;

///////////////////////////////////////////////
// WARNING: GENERATED CODE - DO NOT MODIFY!! //
///////////////////////////////////////////////

public class NupleFuture<V> extends StreamScopeTupleFuture<V, NupleFuture<V>> implements
    Tuple<StreamingFuture<? extends V>> {

  private static final HashMap<Integer, Function<Iterator<StreamingFuture<?>>, TupleFuture<?, ?>>> factories =
      new HashMap<Integer, Function<Iterator<StreamingFuture<?>>, TupleFuture<?, ?>>>() {
        {
          put(1, new Function<Iterator<StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<StreamingFuture<?>> input) {
              return SingleFuture.of(input.next());
            }
          });
          put(2, new Function<Iterator<StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<StreamingFuture<?>> input) {
              return CoupleFuture.of(
                  input.next(),
                  input.next()
              );
            }
          });
          put(3, new Function<Iterator<StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<StreamingFuture<?>> input) {
              return TripleFuture.of(
                  input.next(),
                  input.next(),
                  input.next()
              );
            }
          });
          put(4, new Function<Iterator<StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<StreamingFuture<?>> input) {
              return QuadrupleFuture.of(
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next()
              );
            }
          });
          put(5, new Function<Iterator<StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<StreamingFuture<?>> input) {
              return QuintupleFuture.of(
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next()
              );
            }
          });
          put(6, new Function<Iterator<StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<StreamingFuture<?>> input) {
              return SextupleFuture.of(
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next()
              );
            }
          });
          put(7, new Function<Iterator<StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<StreamingFuture<?>> input) {
              return SeptupleFuture.of(
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next()
              );
            }
          });
          put(8, new Function<Iterator<StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<StreamingFuture<?>> input) {
              return OctupleFuture.of(
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next()
              );
            }
          });
          put(9, new Function<Iterator<StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<StreamingFuture<?>> input) {
              return NonupleFuture.of(
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next()
              );
            }
          });
          put(10, new Function<Iterator<StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<StreamingFuture<?>> input) {
              return DecupleFuture.of(
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next()
              );
            }
          });
          put(11, new Function<Iterator<StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<StreamingFuture<?>> input) {
              return UndecupleFuture.of(
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next()
              );
            }
          });
          put(12, new Function<Iterator<StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<StreamingFuture<?>> input) {
              return DuodecupleFuture.of(
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next()
              );
            }
          });
          put(13, new Function<Iterator<StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<StreamingFuture<?>> input) {
              return TredecupleFuture.of(
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next()
              );
            }
          });
          put(14, new Function<Iterator<StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<StreamingFuture<?>> input) {
              return QuattuordecupleFuture.of(
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next()
              );
            }
          });
          put(15, new Function<Iterator<StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<StreamingFuture<?>> input) {
              return QuindecupleFuture.of(
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next()
              );
            }
          });
          put(16, new Function<Iterator<StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<StreamingFuture<?>> input) {
              return SexdecupleFuture.of(
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next()
              );
            }
          });
          put(17, new Function<Iterator<StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<StreamingFuture<?>> input) {
              return SeptendecupleFuture.of(
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next()
              );
            }
          });
          put(18, new Function<Iterator<StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<StreamingFuture<?>> input) {
              return OctodecupleFuture.of(
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next()
              );
            }
          });
          put(19, new Function<Iterator<StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<StreamingFuture<?>> input) {
              return NovemdecupleFuture.of(
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next()
              );
            }
          });
          put(20, new Function<Iterator<StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<StreamingFuture<?>> input) {
              return VigupleFuture.of(
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next(),
                  input.next()
              );
            }
          });
        }
      };

  private final List<? extends StreamingFuture<? extends V>> futures;

  private NupleFuture(@NotNull final List<? extends StreamingFuture<? extends V>> futures) {
    this.futures = futures;
  }

  public static @NotNull <V> NupleFuture<V> of(
      @NotNull final Collection<? extends StreamingFuture<V>> futures) {
    return new NupleFuture<V>(ImmutableList.ofElementsIn(futures));
  }

  @SuppressWarnings("unchecked")
  public static @NotNull <V> TupleFuture<V, ?> tupleOf(
      @NotNull final Collection<? extends StreamingFuture<V>> futures) {
    if (futures.isEmpty()) {
      return EmptyFuture.instance();
    } else {
      final Function<Iterator<StreamingFuture<?>>, TupleFuture<?, ?>> factory =
          factories.get(futures.size());
      if (factory != null) {
        try {
          return (TupleFuture<V, ?>) factory.apply(
              (Iterator<StreamingFuture<?>>) futures.iterator());
        } catch (final Exception e) {
          throw UncheckedException.throwUnchecked(e);
        }
      }
    }
    return new NupleFuture<V>(ImmutableList.ofElementsIn(futures));
  }

  @Override
  @SuppressWarnings("unchecked")
  public @NotNull List<StreamingFuture<? extends V>> asList() {
    return (List<StreamingFuture<? extends V>>) futures;
  }

  @Override
  public @NotNull NupleFuture<V> readOnly() {
    return this;
  }

  @Override
  protected @NotNull NupleFuture<V> createProxy() {
    final ArrayList<StreamingFuture<? extends V>> proxies = new ArrayList<StreamingFuture<? extends V>>(
        futures.size());
    for (final StreamingFuture<? extends V> future : futures) {
      proxies.add(proxyFuture(future));
    }
    return new NupleFuture<V>(ImmutableList.ofElementsIn(proxies));
  }

  @Override
  protected void subscribeProxy(@NotNull final NupleFuture<V> proxyFuture) {
    for (final StreamingFuture<? extends V> future : proxyFuture.asList()) {
      connectProxy(future);
    }
  }
}
