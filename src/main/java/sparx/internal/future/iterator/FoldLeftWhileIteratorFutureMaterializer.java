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

import static sparx.internal.future.FutureConsumers.safeConsume;

import java.util.Collections;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.FutureConsumer;
import sparx.util.annotation.Positive;
import sparx.util.function.BinaryFunction;
import sparx.util.function.Predicate;

public class FoldLeftWhileIteratorFutureMaterializer<E, F> extends
    ImmediateIteratorFutureMaterializer<E, F> {

  private static final Logger LOGGER = Logger.getLogger(
      FoldLeftWhileIteratorFutureMaterializer.class.getName());

  public FoldLeftWhileIteratorFutureMaterializer(
      @NotNull final IteratorFutureMaterializer<E> wrapped, final F identity,
      @NotNull final Predicate<? super F> predicate,
      @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    super(context, new AtomicInteger(STATUS_RUNNING));
    setState(new ImmaterialState(wrapped, identity, predicate, operation, cancelException));
  }

  @Override
  public int knownSize() {
    return 1;
  }

  private class ImmaterialState extends ImmediateIteratorFutureMaterializer<E, F>.ImmaterialState {

    private final AtomicReference<CancellationException> cancelException;
    private final F identity;
    private final BinaryFunction<? super F, ? super E, ? extends F> operation;
    private final Predicate<? super F> predicate;
    private final IteratorFutureMaterializer<E> wrapped;

    public ImmaterialState(@NotNull final IteratorFutureMaterializer<E> wrapped, final F identity,
        @NotNull final Predicate<? super F> predicate,
        @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      super(wrapped, LOGGER);
      this.wrapped = wrapped;
      this.identity = identity;
      this.predicate = predicate;
      this.operation = operation;
      this.cancelException = cancelException;
    }

    @Override
    public int knownSize() {
      return 1;
    }

    @Override
    public void materializeSkip(@Positive final int count,
        @NotNull final FutureConsumer<Integer> consumer) {
      if (!isMaterializing()) {
        setDone(EmptyIteratorFutureMaterializer.<F>instance());
        consumeElements(Collections.<F>emptyList());
        safeConsume(consumer, 1, LOGGER);
      } else {
        super.materializeSkip(count, consumer);
      }
    }

    @Override
    public int weightElements() {
      return isMaterializing() ? 1 : wrapped.weightSkip();
    }

    @Override
    public int weightSkip() {
      return isMaterializing() ? weightElements() : 1;
    }

    @Override
    void materialize() {
      wrapped.materializeNextWhile(new CancellableIndexedFuturePredicate<E>() {
        private F current = identity;

        @Override
        public void cancellableComplete(final int size) {
          setDone(new ElementToIteratorFutureMaterializer<F>(current));
          consumeElements(Collections.singletonList(current));
        }

        @Override
        public boolean cancellableTest(final int size, final int index, final E element)
            throws Exception {
          if (predicate.test(current)) {
            current = operation.apply(current, element);
            return true;
          }
          setDone(new ElementToIteratorFutureMaterializer<F>(current));
          consumeElements(Collections.singletonList(current));
          return false;
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
