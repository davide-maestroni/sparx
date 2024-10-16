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
import java.util.HashSet;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.FutureConsumer;
import sparx.util.annotation.Positive;

public class IncludesAllIteratorFutureMaterializer<E> extends
    ImmediateIteratorFutureMaterializer<E, Boolean> {

  private static final Logger LOGGER = Logger.getLogger(
      IncludesAllIteratorFutureMaterializer.class.getName());

  public IncludesAllIteratorFutureMaterializer(@NotNull final IteratorFutureMaterializer<E> wrapped,
      @NotNull final IteratorFutureMaterializer<Object> elementsMaterializer,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    super(context, new AtomicInteger(STATUS_RUNNING));
    setState(new ImmaterialState(wrapped, elementsMaterializer, cancelException));
  }

  @Override
  public int knownSize() {
    return 1;
  }

  private class ImmaterialState extends
      ImmediateIteratorFutureMaterializer<E, Boolean>.ImmaterialState {

    private final AtomicReference<CancellationException> cancelException;
    private final IteratorFutureMaterializer<Object> elementsMaterializer;
    private final IteratorFutureMaterializer<E> wrapped;

    public ImmaterialState(@NotNull final IteratorFutureMaterializer<E> wrapped,
        @NotNull final IteratorFutureMaterializer<Object> elementsMaterializer,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      super(wrapped, LOGGER);
      this.wrapped = wrapped;
      this.elementsMaterializer = elementsMaterializer;
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
        setDone(EmptyIteratorFutureMaterializer.<Boolean>instance());
        consumeElements(Collections.<Boolean>emptyList());
        safeConsume(consumer, 1, LOGGER);
      } else {
        super.materializeSkip(count, consumer);
      }
    }

    @Override
    public int weightElements() {
      return isMaterializing() ? 1 : (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightNextWhile() + elementsMaterializer.weightNextWhile());
    }

    @Override
    public int weightSkip() {
      return isMaterializing() ? weightElements() : 1;
    }

    @Override
    void materialize() {
      final HashSet<Object> elements = new HashSet<Object>();
      elementsMaterializer.materializeNextWhile(new CancellableIndexedFuturePredicate<Object>() {
        @Override
        public void cancellableComplete(final int size) {
          if (elements.isEmpty()) {
            setDone(new ElementToIteratorFutureMaterializer<Boolean>(true));
            consumeElements(Collections.singletonList(true));
          } else {
            wrapped.materializeNextWhile(new CancellableIndexedFuturePredicate<E>() {
              @Override
              public void cancellableComplete(final int size) {
                setDone(new ElementToIteratorFutureMaterializer<Boolean>(false));
                consumeElements(Collections.singletonList(false));
              }

              @Override
              public boolean cancellableTest(final int size, final int index, final E element) {
                if (elements.remove(element) && elements.isEmpty()) {
                  setDone(new ElementToIteratorFutureMaterializer<Boolean>(true));
                  consumeElements(Collections.singletonList(true));
                  return false;
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

        @Override
        public boolean cancellableTest(final int size, final int index, final Object element) {
          elements.add(element);
          return true;
        }

        @Override
        public void error(@NotNull final Exception error) {
          setError(error);
        }
      });
    }

    private void setError(@NotNull final Exception error) {
      final CancellationException exception = cancelException.get();
      if (exception != null) {
        setCancelled(exception);
        consumeError(exception);
      } else {
        setFailed(error);
        consumeError(error);
      }
    }
  }
}
