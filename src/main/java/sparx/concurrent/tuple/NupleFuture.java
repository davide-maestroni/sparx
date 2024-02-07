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
// WARNING: GENERATED CODE - DO NOT MODIFY!!
// - templating engine: Mustache 0.9.11
// - template path: template/concurrent/NupleFuture.mustache
///////////////////////////////////////////////

public class NupleFuture<V> extends StreamScopeTupleFuture<V, NupleFuture<V>> implements
    Tuple<StreamingFuture<? extends V>> {

  private static final HashMap<Integer, Function<Iterator<? extends StreamingFuture<?>>, TupleFuture<?, ?>>> factories =
      new HashMap<Integer, Function<Iterator<? extends StreamingFuture<?>>, TupleFuture<?, ?>>>() {
        {
          put(1, new Function<Iterator<? extends StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<? extends StreamingFuture<?>> itr) {
              return SingleFuture.of(itr.next());
            }
          });
          put(2, new Function<Iterator<? extends StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<? extends StreamingFuture<?>> itr) {
              return CoupleFuture.of(
                  itr.next(),
                  itr.next()
              );
            }
          });
          put(3, new Function<Iterator<? extends StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<? extends StreamingFuture<?>> itr) {
              return TripleFuture.of(
                  itr.next(),
                  itr.next(),
                  itr.next()
              );
            }
          });
          put(4, new Function<Iterator<? extends StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<? extends StreamingFuture<?>> itr) {
              return QuadrupleFuture.of(
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next()
              );
            }
          });
          put(5, new Function<Iterator<? extends StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<? extends StreamingFuture<?>> itr) {
              return QuintupleFuture.of(
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next()
              );
            }
          });
          put(6, new Function<Iterator<? extends StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<? extends StreamingFuture<?>> itr) {
              return SextupleFuture.of(
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next()
              );
            }
          });
          put(7, new Function<Iterator<? extends StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<? extends StreamingFuture<?>> itr) {
              return SeptupleFuture.of(
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next()
              );
            }
          });
          put(8, new Function<Iterator<? extends StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<? extends StreamingFuture<?>> itr) {
              return OctupleFuture.of(
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next()
              );
            }
          });
          put(9, new Function<Iterator<? extends StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<? extends StreamingFuture<?>> itr) {
              return NonupleFuture.of(
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next()
              );
            }
          });
          put(10, new Function<Iterator<? extends StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<? extends StreamingFuture<?>> itr) {
              return DecupleFuture.of(
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next()
              );
            }
          });
          put(11, new Function<Iterator<? extends StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<? extends StreamingFuture<?>> itr) {
              return UndecupleFuture.of(
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next()
              );
            }
          });
          put(12, new Function<Iterator<? extends StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<? extends StreamingFuture<?>> itr) {
              return DuodecupleFuture.of(
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next()
              );
            }
          });
          put(13, new Function<Iterator<? extends StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<? extends StreamingFuture<?>> itr) {
              return TredecupleFuture.of(
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next()
              );
            }
          });
          put(14, new Function<Iterator<? extends StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<? extends StreamingFuture<?>> itr) {
              return QuattuordecupleFuture.of(
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next()
              );
            }
          });
          put(15, new Function<Iterator<? extends StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<? extends StreamingFuture<?>> itr) {
              return QuindecupleFuture.of(
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next()
              );
            }
          });
          put(16, new Function<Iterator<? extends StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<? extends StreamingFuture<?>> itr) {
              return SexdecupleFuture.of(
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next()
              );
            }
          });
          put(17, new Function<Iterator<? extends StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<? extends StreamingFuture<?>> itr) {
              return SeptendecupleFuture.of(
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next()
              );
            }
          });
          put(18, new Function<Iterator<? extends StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<? extends StreamingFuture<?>> itr) {
              return OctodecupleFuture.of(
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next()
              );
            }
          });
          put(19, new Function<Iterator<? extends StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<? extends StreamingFuture<?>> itr) {
              return NovemdecupleFuture.of(
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next()
              );
            }
          });
          put(20, new Function<Iterator<? extends StreamingFuture<?>>, TupleFuture<?, ?>>() {
            @Override
            public TupleFuture<?, ?> apply(final Iterator<? extends StreamingFuture<?>> itr) {
              return VigupleFuture.of(
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next(),
                  itr.next()
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
      final Function<Iterator<? extends StreamingFuture<?>>, TupleFuture<?, ?>> factory =
          factories.get(futures.size());
      if (factory != null) {
        try {
          return (TupleFuture<V, ?>) factory.apply(futures.iterator());
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
  protected @NotNull NupleFuture<V> createPaused() {
    final ArrayList<StreamingFuture<? extends V>> proxies = new ArrayList<StreamingFuture<? extends V>>(
        futures.size());
    for (final StreamingFuture<? extends V> future : futures) {
      proxies.add(pauseFuture(future));
    }
    return new NupleFuture<V>(ImmutableList.ofElementsIn(proxies));
  }

  @Override
  protected void resumePaused(@NotNull final NupleFuture<V> pausedFuture) {
    for (final StreamingFuture<? extends V> future : pausedFuture.asList()) {
      resumeFuture(future);
    }
  }
}
