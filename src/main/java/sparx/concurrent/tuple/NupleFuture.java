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
import java.util.Iterator;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.StreamingFuture;
import sparx.concurrent.TupleFuture;
import sparx.tuple.Tuple;
import sparx.util.ImmutableList;

public class NupleFuture<V> extends StreamScopeTupleFuture<V, NupleFuture<V>> implements
    Tuple<StreamingFuture<? extends V>> {

  private final List<? extends StreamingFuture<? extends V>> futures;

  private NupleFuture(@NotNull final List<? extends StreamingFuture<? extends V>> futures) {
    this.futures = futures;
  }

  public static @NotNull <V> NupleFuture<V> of(
      @NotNull final Collection<? extends StreamingFuture<V>> futures) {
    return new NupleFuture<V>(ImmutableList.ofElementsIn(futures));
  }

  public static @NotNull <V> TupleFuture<V, ?> tupleOf(
      @NotNull final Collection<? extends StreamingFuture<V>> futures) {
    final int size = futures.size();
    if (size == 0) {
      return EmptyFuture.instance();
    } else if (size == 1) {
      return SingleFuture.of(futures.iterator().next());
    } else if (size == 2) {
      final Iterator<? extends StreamingFuture<V>> iterator = futures.iterator();
      return CoupleFuture.of(
          iterator.next(),
          iterator.next()
      );
    } else if (size == 3) {
      final Iterator<? extends StreamingFuture<V>> iterator = futures.iterator();
      return TripleFuture.of(
          iterator.next(),
          iterator.next(),
          iterator.next()
      );
    } else if (size == 4) {
      final Iterator<? extends StreamingFuture<V>> iterator = futures.iterator();
      return QuadrupleFuture.of(
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next()
      );
    } else if (size == 5) {
      final Iterator<? extends StreamingFuture<V>> iterator = futures.iterator();
      return QuintupleFuture.of(
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next()
      );
    } else if (size == 6) {
      final Iterator<? extends StreamingFuture<V>> iterator = futures.iterator();
      return SextupleFuture.of(
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next()
      );
    } else if (size == 7) {
      final Iterator<? extends StreamingFuture<V>> iterator = futures.iterator();
      return SeptupleFuture.of(
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next()
      );
    } else if (size == 8) {
      final Iterator<? extends StreamingFuture<V>> iterator = futures.iterator();
      return OctupleFuture.of(
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next()
      );
    } else if (size == 9) {
      final Iterator<? extends StreamingFuture<V>> iterator = futures.iterator();
      return NonupleFuture.of(
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next()
      );
    } else if (size == 10) {
      final Iterator<? extends StreamingFuture<V>> iterator = futures.iterator();
      return DecupleFuture.of(
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next()
      );
    } else if (size == 11) {
      final Iterator<? extends StreamingFuture<V>> iterator = futures.iterator();
      return UndecupleFuture.of(
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next()
      );
    } else if (size == 12) {
      final Iterator<? extends StreamingFuture<V>> iterator = futures.iterator();
      return DuodecupleFuture.of(
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next()
      );
    } else if (size == 13) {
      final Iterator<? extends StreamingFuture<V>> iterator = futures.iterator();
      return TredecupleFuture.of(
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next()
      );
    } else if (size == 14) {
      final Iterator<? extends StreamingFuture<V>> iterator = futures.iterator();
      return QuattuordecupleFuture.of(
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next()
      );
    } else if (size == 15) {
      final Iterator<? extends StreamingFuture<V>> iterator = futures.iterator();
      return QuindecupleFuture.of(
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next()
      );
    } else if (size == 16) {
      final Iterator<? extends StreamingFuture<V>> iterator = futures.iterator();
      return SexdecupleFuture.of(
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next()
      );
    } else if (size == 17) {
      final Iterator<? extends StreamingFuture<V>> iterator = futures.iterator();
      return SeptendecupleFuture.of(
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next()
      );
    } else if (size == 18) {
      final Iterator<? extends StreamingFuture<V>> iterator = futures.iterator();
      return OctodecupleFuture.of(
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next()
      );
    } else if (size == 19) {
      final Iterator<? extends StreamingFuture<V>> iterator = futures.iterator();
      return NovemdecupleFuture.of(
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next()
      );
    } else if (size == 20) {
      final Iterator<? extends StreamingFuture<V>> iterator = futures.iterator();
      return VigupleFuture.of(
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next(),
          iterator.next()
      );
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
