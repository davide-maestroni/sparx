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
import java.util.Comparator;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.FutureConsumer;

public class MaxIteratorFutureMaterializer<E> extends ImmediateIteratorFutureMaterializer<E, E> {

  private static final Logger LOGGER = Logger.getLogger(
      MaxIteratorFutureMaterializer.class.getName());

  public MaxIteratorFutureMaterializer(@NotNull final IteratorFutureMaterializer<E> wrapped,
      @NotNull final Comparator<? super E> comparator, @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    super(context);
    setState(new ImmaterialState(wrapped, comparator, cancelException));
  }

  @Override
  public boolean isMaterializedAtOnce() {
    return true;
  }

  private class ImmaterialState extends ImmediateIteratorFutureMaterializer<E, E>.ImmaterialState {

    private final AtomicReference<CancellationException> cancelException;
    private final Comparator<? super E> comparator;
    private final IteratorFutureMaterializer<E> wrapped;

    public ImmaterialState(@NotNull final IteratorFutureMaterializer<E> wrapped,
        @NotNull final Comparator<? super E> comparator,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      super(wrapped, LOGGER);
      this.wrapped = wrapped;
      this.comparator = comparator;
      this.cancelException = cancelException;
    }

    @Override
    public boolean isMaterializedAtOnce() {
      return true;
    }

    @Override
    public void materializeSkip(final int count, @NotNull final FutureConsumer<Integer> consumer) {
      wrapped.materializeHasNext(new CancellableFutureConsumer<Boolean>() {
        @Override
        public void cancellableAccept(final Boolean hasNext) throws Exception {
          setDone(EmptyIteratorFutureMaterializer.<E>instance());
          consumer.accept(hasNext ? 1 : 0);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public int weightElements() {
      return isMaterializing() ? 1 : wrapped.weightNextWhile();
    }

    @Override
    void materialize() {
      wrapped.materializeNextWhile(new CancellableIndexedFuturePredicate<E>() {
        private boolean first = true;
        private E max;

        @Override
        public void cancellableComplete(final int size) {
          if (first) {
            setDone(EmptyIteratorFutureMaterializer.<E>instance());
            consumeElements(Collections.<E>emptyList());
          } else {
            setDone(new ElementToIteratorFutureMaterializer<E>(max));
            consumeElements(Collections.singletonList(max));
          }
        }

        @Override
        public boolean cancellableTest(final int size, final int index, final E element) {
          if (first) {
            first = false;
            max = element;
          } else if (comparator.compare(element, max) > 0) {
            max = element;
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
