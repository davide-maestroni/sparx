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
package sparx.internal.future.iterator;

import java.util.HashSet;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.util.function.IndexedFunction;

public class DistinctByIteratorFutureMaterializer<E, K> extends
    ProgressiveIteratorFutureMaterializer<E, E> {

  private static final Logger LOGGER = Logger.getLogger(
      DistinctByIteratorFutureMaterializer.class.getName());

  public DistinctByIteratorFutureMaterializer(@NotNull final IteratorFutureMaterializer<E> wrapped,
      @NotNull final IndexedFunction<? super E, K> keyExtractor,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    super(context, new AtomicInteger(STATUS_RUNNING));
    setState(new ImmaterialState(wrapped, keyExtractor, context, cancelException));
  }

  private class ImmaterialState extends
      ProgressiveIteratorFutureMaterializer<E, E>.ImmaterialState {

    private final IndexedFunction<? super E, K> keyExtractor;
    private final HashSet<K> keys = new HashSet<K>();

    private int wrappedIndex;

    public ImmaterialState(@NotNull final IteratorFutureMaterializer<E> wrapped,
        @NotNull final IndexedFunction<? super E, K> keyExtractor,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      super(wrapped, context, cancelException, LOGGER);
      this.keyExtractor = keyExtractor;
    }

    @Override
    boolean addElement(final E element) throws Exception {
      final K key = keyExtractor.apply(wrappedIndex++, element);
      return keys.add(key);
    }

    @Override
    E mapElement(final E element) {
      return element;
    }
  }
}
