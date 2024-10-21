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
import static sparx.internal.future.FutureConsumers.safeConsumeError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.FutureConsumer;
import sparx.internal.future.IndexedFutureConsumer;
import sparx.internal.future.IndexedFuturePredicate;
import sparx.util.DequeueList;
import sparx.util.annotation.Positive;
import sparx.util.function.IndexedFunction;

public class MapIteratorFutureMaterializer<E, F> extends AbstractIteratorFutureMaterializer<F> {

  private static final Logger LOGGER = Logger.getLogger(
      MapIteratorFutureMaterializer.class.getName());

  private final int knownSize;

  public MapIteratorFutureMaterializer(@NotNull final IteratorFutureMaterializer<E> wrapped,
      @NotNull final IndexedFunction<? super E, ? extends F> mapper,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    super(context);
    knownSize = wrapped.knownSize();
    setState(new ImmaterialState(wrapped, mapper, context, cancelException));
  }

  @Override
  public int knownSize() {
    return knownSize;
  }

  private class ImmaterialState implements IteratorFutureMaterializer<F> {

    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final ArrayList<FutureConsumer<List<F>>> elementsConsumers = new ArrayList<FutureConsumer<List<F>>>(
        2);
    final IndexedFunction<? super E, ? extends F> mapper;
    private final IteratorFutureMaterializer<E> wrapped;

    private int wrappedIndex;

    public ImmaterialState(@NotNull final IteratorFutureMaterializer<E> wrapped,
        @NotNull final IndexedFunction<? super E, ? extends F> mapper,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      this.wrapped = wrapped;
      this.mapper = mapper;
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
      return knownSize;
    }

    @Override
    public void materializeCancel(@NotNull final CancellationException exception) {
      wrapped.materializeCancel(exception);
      setCancelled(exception);
      consumeError(exception);
    }

    @Override
    public void materializeElements(@NotNull final FutureConsumer<List<F>> consumer) {
      final ArrayList<FutureConsumer<List<F>>> elementsConsumers = this.elementsConsumers;
      elementsConsumers.add(consumer);
      if (elementsConsumers.size() == 1) {
        final DequeueList<F> elements = new DequeueList<F>();
        wrapped.materializeNextWhile(new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) {
            if (elements.isEmpty()) {
              setDone(EmptyIteratorFutureMaterializer.<F>instance());
              consumeElements(Collections.<F>emptyList());
            } else {
              setDone(new DequeueToIteratorFutureMaterializer<F>(elements, context, wrappedIndex));
              consumeElements(elements);
            }
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            elements.add(getMapped(element));
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
    public void materializeHasNext(@NotNull final FutureConsumer<Boolean> consumer) {
      wrapped.materializeHasNext(new CancellableFutureConsumer<Boolean>() {
        @Override
        public void cancellableAccept(final Boolean hasNext) throws Exception {
          consumer.accept(hasNext);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeIterator(@NotNull final FutureConsumer<Iterator<F>> consumer) {
      materializeElements(new FutureConsumer<List<F>>() {
        @Override
        public void accept(final List<F> elements) {
          getState().materializeIterator(consumer);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeNext(@NotNull final IndexedFutureConsumer<F> consumer) {
      wrapped.materializeNext(new CancellableIndexedFutureConsumer<E>() {
        @Override
        public void cancellableAccept(final int size, final int index, final E element)
            throws Exception {
          final int currentIndex = wrappedIndex;
          final F mapped = getMapped(element);
          consumer.accept(size, currentIndex, mapped);
        }

        @Override
        public void cancellableComplete(final int size) throws Exception {
          setDone(EmptyIteratorFutureMaterializer.<F>instance());
          consumer.complete(0);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeNextWhile(@NotNull final IndexedFuturePredicate<F> predicate) {
      wrapped.materializeNextWhile(new CancellableIndexedFuturePredicate<E>() {
        @Override
        public void cancellableComplete(final int size) throws Exception {
          setDone(EmptyIteratorFutureMaterializer.<F>instance());
          predicate.complete(0);
        }

        @Override
        public boolean cancellableTest(final int size, final int index, final E element)
            throws Exception {
          final int currentIndex = wrappedIndex;
          final F mapped = getMapped(element);
          return predicate.test(size, currentIndex, mapped);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          predicate.error(error);
        }
      });
    }

    @Override
    public void materializeSkip(@Positive final int count,
        @NotNull final FutureConsumer<Integer> consumer) {
      wrapped.materializeSkip(count, new CancellableFutureConsumer<Integer>() {
        @Override
        public void cancellableAccept(final Integer skipped) throws Exception {
          wrappedIndex += skipped;
          consumer.accept(skipped);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public int weightElements() {
      return elementsConsumers.isEmpty() ? wrapped.weightElements() : 1;
    }

    @Override
    public int weightHasNext() {
      return 1;
    }

    @Override
    public int weightNext() {
      return wrapped.weightNext();
    }

    @Override
    public int weightNextWhile() {
      return wrapped.weightNextWhile();
    }

    @Override
    public int weightSkip() {
      return wrapped.weightSkip();
    }

    private void consumeElements(@NotNull final List<F> elements) {
      final ArrayList<FutureConsumer<List<F>>> elementsConsumers = this.elementsConsumers;
      for (final FutureConsumer<List<F>> elementsConsumer : elementsConsumers) {
        safeConsume(elementsConsumer, elements, LOGGER);
      }
      elementsConsumers.clear();
    }

    private void consumeError(@NotNull final Exception error) {
      final ArrayList<FutureConsumer<List<F>>> elementsConsumers = this.elementsConsumers;
      for (final FutureConsumer<List<F>> elementsConsumer : elementsConsumers) {
        safeConsumeError(elementsConsumer, error, LOGGER);
      }
      elementsConsumers.clear();
    }

    private F getMapped(final E element) throws Exception {
      try {
        return mapper.apply(wrappedIndex++, element);
      } catch (final Exception e) {
        setError(e);
        throw e;
      }
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
