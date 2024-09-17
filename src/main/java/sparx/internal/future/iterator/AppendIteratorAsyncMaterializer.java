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

import static sparx.internal.future.AsyncConsumers.safeConsume;
import static sparx.internal.future.AsyncConsumers.safeConsumeComplete;
import static sparx.internal.future.AsyncConsumers.safeConsumeError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.AsyncConsumer;
import sparx.internal.future.IndexedAsyncConsumer;
import sparx.internal.future.IndexedAsyncPredicate;
import sparx.util.IndexOverflowException;
import sparx.util.SizeOverflowException;
import sparx.util.function.BinaryFunction;

public class AppendIteratorAsyncMaterializer<E> extends AbstractIteratorAsyncMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      AppendIteratorAsyncMaterializer.class.getName());

  private final int knownSize;
  private final boolean isMaterializedAtOnce;

  public AppendIteratorAsyncMaterializer(@NotNull final IteratorAsyncMaterializer<E> wrapped,
      final E element, @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final BinaryFunction<List<E>, E, List<E>> appendFunction) {
    super(context, new AtomicInteger(STATUS_RUNNING));
    knownSize = safeSize(wrapped.knownSize());
    isMaterializedAtOnce = wrapped.isMaterializedAtOnce();
    setState(new ImmaterialState(wrapped, element, context, cancelException, appendFunction));
  }

  private static int safeSize(final int wrappedSize) {
    if (wrappedSize >= 0) {
      return SizeOverflowException.safeCast((long) wrappedSize + 1);
    }
    return -1;
  }

  private static int safeIndex(final int wrappedIndex) {
    return IndexOverflowException.safeCast((long) wrappedIndex + 1);
  }

  @Override
  public boolean isMaterializedAtOnce() {
    return isMaterializedAtOnce || super.isMaterializedAtOnce();
  }

  @Override
  public int knownSize() {
    return knownSize;
  }

  private class ImmaterialState implements IteratorAsyncMaterializer<E> {

    private final BinaryFunction<List<E>, E, List<E>> appendFunction;
    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final E element;
    private final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = new ArrayList<AsyncConsumer<List<E>>>(
        2);
    private final IteratorAsyncMaterializer<E> wrapped;

    private boolean consumed;
    private int lastIndex;

    public ImmaterialState(@NotNull final IteratorAsyncMaterializer<E> wrapped, final E element,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final BinaryFunction<List<E>, E, List<E>> appendFunction) {
      this.wrapped = wrapped;
      this.element = element;
      this.context = context;
      this.cancelException = cancelException;
      this.appendFunction = appendFunction;
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
      return wrapped.isMaterializedAtOnce();
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
    public void materializeElements(@NotNull final AsyncConsumer<List<E>> consumer) {
      final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      elementsConsumers.add(consumer);
      if (elementsConsumers.size() == 1) {
        wrapped.materializeElements(new CancellableAsyncConsumer<List<E>>() {
          @Override
          public void cancellableAccept(final List<E> elements) throws Exception {
            final List<E> materialized;
            if (consumed) {
              materialized = elements;
            } else {
              setDone();
              materialized = appendFunction.apply(elements, element);
            }
            consumeElements(materialized);
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

    @Override
    public void materializeHasNext(@NotNull final AsyncConsumer<Boolean> consumer) {
      safeConsume(consumer, !consumed, LOGGER);
    }

    @Override
    public void materializeNext(@NotNull final IndexedAsyncConsumer<E> consumer) {
      if (consumed) {
        safeConsumeComplete(consumer, 0, LOGGER);
      } else {
        wrapped.materializeNext(new CancellableIndexedAsyncConsumer<E>() {
          @Override
          public void cancellableAccept(final int size, final int index, final E element)
              throws Exception {
            lastIndex = index;
            consumer.accept(safeSize(size), index, element);
          }

          @Override
          public void cancellableComplete(final int size) throws Exception {
            if (!consumed) {
              setDone();
              consumer.accept(1, safeIndex(lastIndex), element);
            } else {
              consumer.complete(0);
            }
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
    }

    @Override
    public void materializeNextWhile(@NotNull final IndexedAsyncPredicate<E> predicate) {
      wrapped.materializeNextWhile(new CancellableIndexedAsyncPredicate<E>() {
        @Override
        public void cancellableComplete(final int size) throws Exception {
          if (consumed || predicate.test(1, size, element)) {
            setDone();
            predicate.complete(0);
          }
        }

        @Override
        public boolean cancellableTest(final int size, final int index, final E element)
            throws Exception {
          lastIndex = index;
          return predicate.test(safeSize(size), index, element);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          predicate.error(error);
        }
      });
    }

    @Override
    public void materializeSkip(final int count, @NotNull final AsyncConsumer<Integer> consumer) {
      if (consumed) {
        safeConsume(consumer, 0, LOGGER);
      } else {
        wrapped.materializeSkip(count, new CancellableAsyncConsumer<Integer>() {
          @Override
          public void cancellableAccept(final Integer skipped) throws Exception {
            if (skipped < count && !consumed) {
              setDone();
              consumer.accept(skipped + 1);
            } else {
              consumer.accept(skipped);
            }
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
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
      return consumed ? 1 : wrapped.weightNext();
    }

    @Override
    public int weightNextWhile() {
      return consumed ? 1 : wrapped.weightNextWhile();
    }

    @Override
    public int weightSkip() {
      return consumed ? 1 : wrapped.weightSkip();
    }

    private void consumeElements(@NotNull final List<E> elements) {
      final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      for (final AsyncConsumer<List<E>> elementsConsumer : elementsConsumers) {
        safeConsume(elementsConsumer, elements, LOGGER);
      }
      elementsConsumers.clear();
    }

    private void consumeError(@NotNull final Exception error) {
      final ArrayList<AsyncConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      for (final AsyncConsumer<List<E>> elementsConsumer : elementsConsumers) {
        safeConsumeError(elementsConsumer, error, LOGGER);
      }
      elementsConsumers.clear();
    }

    private void setDone() {
      consumed = true;
      AppendIteratorAsyncMaterializer.this.setDone(
          new ListToIteratorAsyncMaterializer<E>(Collections.<E>emptyList(), context));
    }
  }
}
