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

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.FutureConsumer;
import sparx.util.annotation.Positive;
import sparx.util.function.BinaryFunction;
import sparx.util.function.Predicate;

public class FoldRightWhileIteratorFutureMaterializer<E, F> extends
    ImmediateIteratorFutureMaterializer<E, F> {

  private static final Logger LOGGER = Logger.getLogger(
      FoldRightWhileIteratorFutureMaterializer.class.getName());

  public FoldRightWhileIteratorFutureMaterializer(
      @NotNull final IteratorFutureMaterializer<E> wrapped, final F identity,
      @NotNull final Predicate<? super F> predicate,
      @NotNull final BinaryFunction<? super E, ? super F, ? extends F> operation,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    super(context);
    setState(new ImmaterialState(wrapped, identity, predicate, operation, cancelException));
  }

  @Override
  public int knownSize() {
    return 1;
  }

  private class ImmaterialState extends ImmediateIteratorFutureMaterializer<E, F>.ImmaterialState {

    private final AtomicReference<CancellationException> cancelException;
    private final F identity;
    private final BinaryFunction<? super E, ? super F, ? extends F> operation;
    private final Predicate<? super F> predicate;
    private final IteratorFutureMaterializer<E> wrapped;

    public ImmaterialState(@NotNull final IteratorFutureMaterializer<E> wrapped, final F identity,
        @NotNull final Predicate<? super F> predicate,
        @NotNull final BinaryFunction<? super E, ? super F, ? extends F> operation,
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
      return isMaterializing() ? 1 : wrapped.weightNextWhile();
    }

    @Override
    public int weightSkip() {
      return isMaterializing() ? weightElements() : 1;
    }

    @Override
    void materialize() {
      final ArrayList<E> elements = new ArrayList<E>();
      wrapped.materializeNextWhile(new CancellableIndexedFuturePredicate<E>() {
        @Override
        public void cancellableComplete(final int size) throws Exception {
          F current = identity;
          for (int i = elements.size() - 1; i >= 0 && predicate.test(current); --i) {
            current = operation.apply(elements.get(i), current);
          }
          setDone(new ElementToIteratorFutureMaterializer<F>(current));
          consumeElements(Collections.singletonList(current));
        }

        @Override
        public boolean cancellableTest(final int size, final int index, final E element) {
          elements.add(element);
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
