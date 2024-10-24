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

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.FutureConsumer;
import sparx.internal.future.IndexedFutureConsumer;
import sparx.internal.future.IndexedFuturePredicate;
import sparx.util.function.Supplier;

public class SuppliedIteratorFutureMaterializer<E> extends AbstractIteratorFutureMaterializer<E> {

  public SuppliedIteratorFutureMaterializer(
      @NotNull final Supplier<? extends IteratorFutureMaterializer<E>> supplier,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    super(context);
    setState(new ImmaterialState(supplier, context, cancelException));
  }

  @Override
  public int knownSize() {
    return -1;
  }

  private class ImmaterialState implements IteratorFutureMaterializer<E> {

    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final Supplier<? extends IteratorFutureMaterializer<E>> supplier;

    private ImmaterialState(
        @NotNull final Supplier<? extends IteratorFutureMaterializer<E>> supplier,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      this.supplier = supplier;
      this.context = context;
      this.cancelException = cancelException;
    }

    @Override
    public boolean isCancelled() {
      return false;
    }

    @Override
    public boolean isDone() {
      return false;
    }

    @Override
    public boolean isFailed() {
      return false;
    }

    @Override
    public boolean isMaterializedAtOnce() {
      return false;
    }

    @Override
    public boolean isSucceeded() {
      return false;
    }

    @Override
    public int knownSize() {
      return -1;
    }

    @Override
    public void materializeCancel(@NotNull final CancellationException exception) {
      setCancelled(exception);
    }

    @Override
    public void materializeElements(@NotNull final FutureConsumer<List<E>> consumer) {
      materialized().materializeElements(consumer);
    }

    @Override
    public void materializeHasNext(@NotNull final FutureConsumer<Boolean> consumer) {
      materialized().materializeHasNext(consumer);
    }

    @Override
    public void materializeIterator(@NotNull final FutureConsumer<java.util.Iterator<E>> consumer) {
      materialized().materializeIterator(consumer);
    }

    @Override
    public void materializeNext(@NotNull final IndexedFutureConsumer<E> consumer) {
      materialized().materializeNext(consumer);
    }

    @Override
    public void materializeNextWhile(@NotNull final IndexedFuturePredicate<E> predicate) {
      materialized().materializeNextWhile(predicate);
    }

    @Override
    public void materializeSkip(final int count, @NotNull final FutureConsumer<Integer> consumer) {
      materialized().materializeSkip(count, consumer);
    }

    @Override
    public int weightElements() {
      return 1;
    }

    @Override
    public int weightHasNext() {
      return weightElements();
    }

    @Override
    public int weightNext() {
      return weightElements();
    }

    @Override
    public int weightNextWhile() {
      return weightElements();
    }

    @Override
    public int weightSkip() {
      return weightElements();
    }

    private @NotNull IteratorFutureMaterializer<E> materialized() {
      try {
        return setState(new WrappingState(supplier.get(), cancelException));
      } catch (final Exception e) {
        if (e instanceof InterruptedException) {
          Thread.currentThread().interrupt();
        }
        final CancellationException exception = cancelException.get();
        if (exception != null) {
          return setCancelled(exception);
        } else {
          return setFailed(e);
        }
      }
    }
  }
}
