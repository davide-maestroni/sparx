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
package sparx.internal.future.list;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.util.function.Function;
import sparx.util.function.IndexedFunction;

public class DistinctByListAsyncMaterializer<E, K> extends ProgressiveListAsyncMaterializer<E, E> {

  private static final Logger LOGGER = Logger.getLogger(
      DistinctByListAsyncMaterializer.class.getName());

  public DistinctByListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      @NotNull final IndexedFunction<? super E, K> keyExtractor,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final Function<List<E>, List<E>> decorateFunction) {
    super(context, new AtomicInteger(STATUS_RUNNING));
    setState(
        new ImmaterialState(wrapped, keyExtractor, context, cancelException, decorateFunction));
  }

  private class ImmaterialState extends ProgressiveListAsyncMaterializer<E, E>.ImmaterialState {

    private final IndexedFunction<? super E, K> keyExtractor;
    private final HashSet<K> keys = new HashSet<K>();
    private final ListAsyncMaterializer<E> wrapped;

    private int nextIndex;

    public ImmaterialState(@NotNull final ListAsyncMaterializer<E> wrapped,
        @NotNull final IndexedFunction<? super E, K> keyExtractor,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Function<List<E>, List<E>> decorateFunction) {
      super(wrapped, context, cancelException, decorateFunction, LOGGER);
      this.wrapped = wrapped;
      this.keyExtractor = keyExtractor;
    }

    @Override
    public int weightElements() {
      return needsMaterializing() ? wrapped.weightNextWhile() : 1;
    }

    @Override
    void materializeNext() {
      wrapped.materializeNextWhile(nextIndex, new CancellableIndexedAsyncPredicate<E>() {
        @Override
        public void cancellableComplete(final int size) throws Exception {
          setComplete();
        }

        @Override
        public boolean cancellableTest(final int size, final int index, final E element)
            throws Exception {
          nextIndex = index + 1;
          if (keys.add(keyExtractor.apply(index, element))) {
            return setNextElement(element);
          }
          return true;
        }

        @Override
        public void error(@NotNull final Exception error) {
          setError(error);
        }
      });
    }
  }
}
