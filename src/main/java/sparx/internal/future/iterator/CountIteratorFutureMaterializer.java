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
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.FutureConsumer;
import sparx.util.annotation.Positive;

public class CountIteratorFutureMaterializer<E> extends
    ImmediateIteratorFutureMaterializer<E, Integer> {

  private static final Logger LOGGER = Logger.getLogger(
      CountIteratorFutureMaterializer.class.getName());

  public CountIteratorFutureMaterializer(@NotNull final IteratorFutureMaterializer<E> wrapped,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    super(context);
    setState(new ImmaterialState(wrapped, cancelException));
  }

  @Override
  public int knownSize() {
    return 1;
  }

  private class ImmaterialState extends
      ImmediateIteratorFutureMaterializer<E, Integer>.ImmaterialState {

    private final AtomicReference<CancellationException> cancelException;
    private final IteratorFutureMaterializer<E> wrapped;

    public ImmaterialState(@NotNull final IteratorFutureMaterializer<E> wrapped,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      super(wrapped, LOGGER);
      this.wrapped = wrapped;
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
        setDone(EmptyIteratorFutureMaterializer.<Integer>instance());
        consumeElements(Collections.<Integer>emptyList());
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
      wrapped.materializeSkip(Integer.MAX_VALUE, new CancellableFutureConsumer<Integer>() {
        @Override
        public void cancellableAccept(final Integer skipped) {
          setDone(new ElementToIteratorFutureMaterializer<Integer>(skipped));
          consumeElements(Collections.singletonList(skipped));
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
