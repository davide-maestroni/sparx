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

import static sparx.internal.future.FutureConsumers.safeConsume;
import static sparx.internal.future.FutureConsumers.safeConsumeError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.FutureConsumer;
import sparx.internal.future.IndexedFutureConsumer;
import sparx.internal.future.IndexedFuturePredicate;
import sparx.internal.util.ElementsCache;
import sparx.util.function.IndexedFunction;

public class MapListFutureMaterializer<E, F> extends AbstractListFutureMaterializer<F> {

  private static final Logger LOGGER = Logger.getLogger(MapListFutureMaterializer.class.getName());

  private final int knownSize;

  public MapListFutureMaterializer(@NotNull final ListFutureMaterializer<E> wrapped,
      @NotNull final IndexedFunction<? super E, ? extends F> mapper,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    super(context, new AtomicInteger(STATUS_RUNNING));
    knownSize = wrapped.knownSize();
    setState(new ImmaterialState(wrapped, mapper, context, cancelException));
  }

  @Override
  public int knownSize() {
    return knownSize;
  }

  private class ImmaterialState implements ListFutureMaterializer<F> {

    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final ElementsCache<F> elements = new ElementsCache<F>(knownSize);
    private final ArrayList<FutureConsumer<List<F>>> elementsConsumers = new ArrayList<FutureConsumer<List<F>>>(
        2);
    private final IndexedFunction<? super E, ? extends F> mapper;
    private final ListFutureMaterializer<E> wrapped;

    private int wrappedSize = knownSize;

    public ImmaterialState(@NotNull final ListFutureMaterializer<E> wrapped,
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
    public void materializeContains(final Object element,
        @NotNull final FutureConsumer<Boolean> consumer) {
      final ElementsCache<F> elements = this.elements;
      int i = 0;
      if (element == null) {
        while (elements.has(i)) {
          if (elements.get(i) == null) {
            safeConsume(consumer, true, LOGGER);
            return;
          }
          ++i;
        }
        wrapped.materializeNextWhile(i, new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) throws Exception {
            wrappedSize = size;
            consumer.accept(false);
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            final ElementsCache<F> elements = ImmaterialState.this.elements;
            elements.setSize(wrappedSize = Math.max(wrappedSize, size));
            final F mapped;
            if (!elements.has(index)) {
              try {
                mapped = mapper.apply(index, element);
              } catch (final Exception e) {
                setError(e);
                throw e;
              }
              elements.set(index, mapped);
            } else {
              mapped = elements.get(index);
            }
            if (mapped == null) {
              consumer.accept(true);
              return false;
            }
            return true;
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      } else {
        while (elements.has(i)) {
          if (element.equals(elements.get(i))) {
            safeConsume(consumer, true, LOGGER);
            return;
          }
          ++i;
        }
        final Object other = element;
        wrapped.materializeNextWhile(i, new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) throws Exception {
            wrappedSize = size;
            consumer.accept(false);
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            final ElementsCache<F> elements = ImmaterialState.this.elements;
            elements.setSize(wrappedSize = Math.max(wrappedSize, size));
            final F mapped;
            if (!elements.has(index)) {
              try {
                mapped = mapper.apply(index, element);
              } catch (final Exception e) {
                setError(e);
                throw e;
              }
              elements.set(index, mapped);
            } else {
              mapped = elements.get(index);
            }
            if (other.equals(mapped)) {
              consumer.accept(true);
              return false;
            }
            return true;
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
    }

    @Override
    public void materializeElement(final int index,
        @NotNull final IndexedFutureConsumer<F> consumer) {
      if (index < 0) {
        safeConsumeError(consumer, new IndexOutOfBoundsException(Integer.toString(index)), LOGGER);
      } else {
        final ElementsCache<F> elements = this.elements;
        if (elements.has(index)) {
          safeConsume(consumer, knownSize, index, elements.get(index), LOGGER);
        } else {
          wrapped.materializeElement(index, new CancellableIndexedFutureConsumer<E>() {
            @Override
            public void cancellableAccept(final int size, final int index, final E element)
                throws Exception {
              final ElementsCache<F> elements = ImmaterialState.this.elements;
              elements.setSize(wrappedSize = Math.max(wrappedSize, size));
              final F mapped;
              if (!elements.has(index)) {
                try {
                  mapped = mapper.apply(index, element);
                } catch (final Exception e) {
                  setError(e);
                  throw e;
                }
                elements.set(index, mapped);
              } else {
                mapped = elements.get(index);
              }
              consumer.accept(wrappedSize, index, mapped);
            }

            @Override
            public void cancellableComplete(final int size) throws Exception {
              elements.setSize(wrappedSize = size);
              consumer.complete(size);
            }

            @Override
            public void error(@NotNull final Exception error) throws Exception {
              consumer.error(error);
            }
          });
        }
      }
    }

    @Override
    public void materializeElements(@NotNull final FutureConsumer<List<F>> consumer) {
      final ArrayList<FutureConsumer<List<F>>> elementsConsumers = this.elementsConsumers;
      elementsConsumers.add(consumer);
      if (elementsConsumers.size() == 1) {
        final ElementsCache<F> elements = this.elements;
        int i = 0;
        while (elements.has(i)) {
          ++i;
        }
        wrapped.materializeNextWhile(i, new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) {
            wrappedSize = size;
            final List<F> materialized = elements.toList();
            if (materialized.isEmpty()) {
              setDone(EmptyListFutureMaterializer.<F>instance());
              consumeElements(Collections.<F>emptyList());
            } else {
              setDone(new ListToListFutureMaterializer<F>(materialized, context));
              consumeElements(materialized);
            }
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            final ElementsCache<F> elements = ImmaterialState.this.elements;
            elements.setSize(wrappedSize = Math.max(wrappedSize, size));
            if (!elements.has(index)) {
              elements.set(index, mapper.apply(index, element));
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
    public void materializeEmpty(@NotNull final FutureConsumer<Boolean> consumer) {
      if (elements.count() > 0) {
        safeConsume(consumer, false, LOGGER);
      } else if (wrappedSize >= 0) {
        safeConsume(consumer, wrappedSize == 0, LOGGER);
      } else {
        wrapped.materializeEmpty(new CancellableFutureConsumer<Boolean>() {
          @Override
          public void cancellableAccept(final Boolean empty) throws Exception {
            consumer.accept(empty);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
    }

    @Override
    public void materializeHasElement(final int index,
        @NotNull final FutureConsumer<Boolean> consumer) {
      if (index < 0) {
        safeConsume(consumer, false, LOGGER);
      } else if (wrappedSize >= 0) {
        safeConsume(consumer, index < wrappedSize, LOGGER);
      } else {
        wrapped.materializeHasElement(index, new CancellableFutureConsumer<Boolean>() {
          @Override
          public void cancellableAccept(final Boolean hasElement) throws Exception {
            consumer.accept(hasElement);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
    }

    @Override
    public void materializeNextWhile(final int index,
        @NotNull final IndexedFuturePredicate<F> predicate) {
      final ElementsCache<F> elements = this.elements;
      int i = index;
      while (elements.has(i)) {
        if (!safeConsume(predicate, wrappedSize, i, elements.get(i), LOGGER)) {
          return;
        }
        ++i;
      }
      wrapped.materializeNextWhile(i, new CancellableIndexedFuturePredicate<E>() {
        @Override
        public void cancellableComplete(final int size) throws Exception {
          predicate.complete(wrappedSize = size);
        }

        @Override
        public boolean cancellableTest(final int size, final int index, final E element)
            throws Exception {
          final ElementsCache<F> elements = ImmaterialState.this.elements;
          elements.setSize(wrappedSize = Math.max(wrappedSize, size));
          final F mapped;
          if (!elements.has(index)) {
            try {
              mapped = mapper.apply(index, element);
            } catch (final Exception e) {
              setError(e);
              throw e;
            }
            elements.set(index, mapped);
          } else {
            mapped = elements.get(index);
          }
          return predicate.test(wrappedSize, index, mapped);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          predicate.error(error);
        }
      });
    }

    @Override
    public void materializePrevWhile(final int index,
        @NotNull final IndexedFuturePredicate<F> predicate) {
      final ElementsCache<F> elements = this.elements;
      int i = index;
      while (elements.has(i)) {
        if (!safeConsume(predicate, wrappedSize, i, elements.get(i), LOGGER)) {
          return;
        }
        --i;
      }
      wrapped.materializePrevWhile(i, new CancellableIndexedFuturePredicate<E>() {
        @Override
        public void cancellableComplete(final int size) throws Exception {
          predicate.complete(wrappedSize = Math.max(wrappedSize, size));
        }

        @Override
        public boolean cancellableTest(final int size, final int index, final E element)
            throws Exception {
          final ElementsCache<F> elements = ImmaterialState.this.elements;
          elements.setSize(wrappedSize = Math.max(wrappedSize, size));
          final F mapped;
          if (!elements.has(index)) {
            try {
              mapped = mapper.apply(index, element);
            } catch (final Exception e) {
              setError(e);
              throw e;
            }
            elements.set(index, mapped);
          } else {
            mapped = elements.get(index);
          }
          return predicate.test(wrappedSize, index, mapped);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          predicate.error(error);
        }
      });
    }

    @Override
    public void materializeSize(@NotNull final FutureConsumer<Integer> consumer) {
      if (wrappedSize >= 0) {
        safeConsume(consumer, wrappedSize, LOGGER);
      } else {
        wrapped.materializeSize(new CancellableFutureConsumer<Integer>() {
          @Override
          public void cancellableAccept(final Integer size) throws Exception {
            elements.setSize(size);
            consumer.accept(size);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
    }

    @Override
    public int weightContains() {
      return wrapped.weightNextWhile();
    }

    @Override
    public int weightElement() {
      return wrapped.weightElement();
    }

    @Override
    public int weightElements() {
      return elementsConsumers.isEmpty() ? wrapped.weightNextWhile() : 1;
    }

    @Override
    public int weightEmpty() {
      return wrappedSize < 0 ? wrapped.weightEmpty() : 1;
    }

    @Override
    public int weightHasElement() {
      return wrappedSize < 0 ? wrapped.weightHasElement() : 1;
    }

    @Override
    public int weightNextWhile() {
      return wrapped.weightNextWhile();
    }

    @Override
    public int weightPrevWhile() {
      return wrapped.weightPrevWhile();
    }

    @Override
    public int weightSize() {
      return wrappedSize < 0 ? wrapped.weightSize() : 1;
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
