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

import java.util.Collections;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.util.function.IndexedPredicate;

public class FindLastIndexIteratorFutureMaterializer<E> extends
    ImmediateIteratorFutureMaterializer<E, Integer> {

  private static final Logger LOGGER = Logger.getLogger(
      FindLastIndexIteratorFutureMaterializer.class.getName());

  public FindLastIndexIteratorFutureMaterializer(
      @NotNull final IteratorFutureMaterializer<E> wrapped,
      @NotNull final IndexedPredicate<? super E> predicate, @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    super(context, new AtomicInteger(STATUS_RUNNING));
    setState(new ImmaterialState(wrapped, predicate, cancelException));
  }

  @Override
  public boolean isMaterializedAtOnce() {
    return true;
  }

  private class ImmaterialState extends
      ImmediateIteratorFutureMaterializer<E, Integer>.ImmaterialState {

    private final AtomicReference<CancellationException> cancelException;
    private final IndexedPredicate<? super E> predicate;
    private final IteratorFutureMaterializer<E> wrapped;

    public ImmaterialState(@NotNull final IteratorFutureMaterializer<E> wrapped,
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      super(wrapped, LOGGER);
      this.wrapped = wrapped;
      this.predicate = predicate;
      this.cancelException = cancelException;
    }

    @Override
    public boolean isMaterializedAtOnce() {
      return true;
    }

    @Override
    void materialize() {
      wrapped.materializeNextWhile(new CancellableIndexedFuturePredicate<E>() {
        private int index = -1;
        private int wrappedIndex;

        @Override
        public void cancellableComplete(final int size) {
          if (index >= 0) {
            setDone(new ElementToIteratorFutureMaterializer<Integer>(index));
            consumeElements(Collections.singletonList(index));
          } else {
            setDone(EmptyIteratorFutureMaterializer.<Integer>instance());
            consumeElements(Collections.<Integer>emptyList());
          }
        }

        @Override
        public boolean cancellableTest(final int size, final int index, final E element)
            throws Exception {
          if (predicate.test(wrappedIndex++, element)) {
            this.index = wrappedIndex - 1;
          }
          return true;
        }

        @Override
        public void error(@NotNull final Exception error) {
          final CancellationException exception = cancelException.get();
          if (exception != null) {
            setCancelled(exception);
            consumeError(exception);
          } else {
            setFailed(error);
            consumeError(error);
          }
        }
      });
    }
  }
}