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

import static sparx.internal.future.AsyncConsumers.safeConsume;
import static sparx.internal.future.AsyncConsumers.safeConsumeComplete;
import static sparx.internal.future.AsyncConsumers.safeConsumeError;

import java.util.ArrayList;
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
import sparx.internal.util.ElementsCache;
import sparx.util.IndexOverflowException;
import sparx.util.SizeOverflowException;
import sparx.util.annotation.Positive;
import sparx.util.function.Function;

public class SlidingWindowListAsyncMaterializer<E, L extends List<E>> extends
    AbstractListAsyncMaterializer<L> {

  private static final Logger LOGGER = Logger.getLogger(
      SlidingWindowListAsyncMaterializer.class.getName());

  private final int knownSize;

  public SlidingWindowListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      @Positive final int maxSize, @Positive final int step,
      @NotNull final Splitter<E, ? extends L> splitter, @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final Function<List<L>, List<L>> decorateFunction) {
    super(new AtomicInteger(STATUS_RUNNING));
    knownSize = safeSize(wrapped.knownSize(), step);
    setState(new ImmaterialState(wrapped, maxSize, step, splitter, context, cancelException,
        decorateFunction));
  }

  private static int safeSize(final int wrappedSize, final int step) {
    if (wrappedSize >= 0) {
      if (wrappedSize < step) {
        return 1;
      }
      return SizeOverflowException.safeCast((wrappedSize + step - 1) / step);
    }
    return -1;
  }

  @Override
  public int knownSize() {
    return knownSize;
  }

  public interface Splitter<E, L extends List<E>> {

    @NotNull
    L getChunk(@NotNull ListAsyncMaterializer<E> materializer, int start, int end);

    void getElements(@NotNull L chunk, @NotNull AsyncConsumer<List<E>> consumer);
  }

  private class ImmaterialState implements ListAsyncMaterializer<L> {

    private final AtomicReference<CancellationException> cancelException;
    private final Splitter<E, ? extends L> splitter;
    private final ExecutionContext context;
    private final Function<List<L>, List<L>> decorateFunction;
    private final ElementsCache<L> elements = new ElementsCache<L>(knownSize);
    private final ArrayList<AsyncConsumer<List<L>>> elementsConsumers = new ArrayList<AsyncConsumer<List<L>>>(
        2);
    private final int maxSize;
    private final int step;
    private final ListAsyncMaterializer<E> wrapped;

    private int wrappedSize;

    public ImmaterialState(@NotNull final ListAsyncMaterializer<E> wrapped, final int maxSize,
        final int step, @NotNull final Splitter<E, ? extends L> splitter,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Function<List<L>, List<L>> decorateFunction) {
      this.wrapped = wrapped;
      this.maxSize = maxSize;
      this.step = step;
      this.splitter = splitter;
      this.context = context;
      this.cancelException = cancelException;
      this.decorateFunction = decorateFunction;
      wrappedSize = wrapped.knownSize();
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
    @SuppressWarnings("unchecked")
    public void materializeContains(final Object element,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      final ElementsCache<L> elements = this.elements;
      if (wrappedSize >= 0) {
        final int size = wrappedSize;
        final long maxSize = this.maxSize;
        final long step = this.step;
        final Splitter<E, ? extends L> splitter = this.splitter;
        final ListAsyncMaterializer<E> wrapped = this.wrapped;
        final int endIndex = (int) Math.min(size, maxSize);
        if (!elements.has(0)) {
          final L chunk = splitter.getChunk(wrapped, 0, endIndex);
          elements.set(0, chunk);
        }
        ((Splitter<E, L>) splitter).getElements(elements.get(0),
            new CancellableAsyncConsumer<List<E>>() {
              private int index;
              private int startIndex;

              @Override
              public void cancellableAccept(final List<E> chunkElements) throws Exception {
                if (chunkElements.equals(element)) {
                  consumer.accept(true);
                } else {
                  final int index = ++this.index;
                  final int startIndex = (int) Math.min(size, this.startIndex + step);
                  if (startIndex < size) {
                    final int endIndex = (int) Math.min(size, startIndex + maxSize);
                    if (!elements.has(index)) {
                      final L chunk = splitter.getChunk(wrapped, startIndex, endIndex);
                      elements.set(index, chunk);
                    }
                    this.startIndex = startIndex;
                    ((Splitter<E, L>) splitter).getElements(elements.get(index), this);
                  } else {
                    consumer.accept(false);
                  }
                }
              }

              @Override
              public void error(@NotNull final Exception error) throws Exception {
                consumer.error(error);
              }
            });
      } else {
        wrapped.materializeSize(new CancellableAsyncConsumer<Integer>() {
          @Override
          public void cancellableAccept(final Integer size) {
            wrappedSize = size;
            elements.setSize(safeSize(wrappedSize, step));
            materializeContains(element, consumer);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
    }

    @Override
    public void materializeDone(@NotNull final AsyncConsumer<List<L>> consumer) {
      safeConsumeError(consumer, new UnsupportedOperationException(), LOGGER);
    }

    @Override
    public void materializeElement(final int index,
        @NotNull final IndexedAsyncConsumer<L> consumer) {
      if (index < 0) {
        safeConsumeError(consumer, new IndexOutOfBoundsException(Integer.toString(index)), LOGGER);
      } else {
        final ElementsCache<L> elements = this.elements;
        if (elements.has(index)) {
          safeConsume(consumer, safeSize(wrappedSize, step), index, elements.get(index), LOGGER);
        } else if (wrappedSize >= 0) {
          final int size = wrappedSize;
          final long startIndex = (long) index * step;
          if (startIndex >= size) {
            safeConsumeComplete(consumer, safeSize(wrappedSize, step), LOGGER);
            return;
          }
          final L chunk;
          if (!elements.has(index)) {
            final int maxSize = this.maxSize;
            final int endIndex = (int) Math.min(size, startIndex + maxSize);
            chunk = splitter.getChunk(wrapped, (int) startIndex, endIndex);
            elements.set(index, chunk);
          } else {
            chunk = elements.get(index);
          }
          safeConsume(consumer, safeSize(wrappedSize, step), index, chunk, LOGGER);
        } else {
          wrapped.materializeSize(new CancellableAsyncConsumer<Integer>() {
            @Override
            public void cancellableAccept(final Integer size) {
              wrappedSize = size;
              elements.setSize(safeSize(wrappedSize, step));
              materializeElement(index, consumer);
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
    public void materializeElements(@NotNull final AsyncConsumer<List<L>> consumer) {
      final ArrayList<AsyncConsumer<List<L>>> elementsConsumers = this.elementsConsumers;
      elementsConsumers.add(consumer);
      if (elementsConsumers.size() == 1) {
        if (wrappedSize >= 0) {
          materializeElements();
        } else {
          wrapped.materializeSize(new CancellableAsyncConsumer<Integer>() {
            @Override
            public void cancellableAccept(final Integer size) {
              elements.setSize(safeSize(wrappedSize = size, step));
              materializeElements();
            }

            @Override
            public void error(@NotNull final Exception error) {
              setError(error);
            }
          });
        }
      }
    }

    @Override
    public void materializeEmpty(@NotNull final AsyncConsumer<Boolean> consumer) {
      wrapped.materializeEmpty(new CancellableAsyncConsumer<Boolean>() {
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

    @Override
    public void materializeHasElement(final int index,
        @NotNull final AsyncConsumer<Boolean> consumer) {
      if (index < 0) {
        safeConsume(consumer, false, LOGGER);
      } else {
        final ElementsCache<L> elements = this.elements;
        if (elements.has(index)) {
          safeConsume(consumer, true, LOGGER);
        } else if (wrappedSize >= 0) {
          final int size = wrappedSize;
          final long startIndex = (long) index * step;
          safeConsume(consumer, startIndex < size, LOGGER);
        } else {
          wrapped.materializeSize(new CancellableAsyncConsumer<Integer>() {
            @Override
            public void cancellableAccept(final Integer size) {
              elements.setSize(safeSize(wrappedSize = size, step));
              final long startIndex = Math.min(Integer.MAX_VALUE, (long) index * step);
              safeConsume(consumer, startIndex < size, LOGGER);
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
    public void materializeNextWhile(final int index,
        @NotNull final IndexedAsyncPredicate<L> predicate) {
      if (wrappedSize >= 0) {
        final int size = wrappedSize;
        final long maxSize = this.maxSize;
        final long step = this.step;
        final int elementsSize = safeSize(size, this.step);
        final ElementsCache<L> elements = this.elements;
        final Splitter<E, ? extends L> splitter = this.splitter;
        final ListAsyncMaterializer<E> wrapped = this.wrapped;
        final int startIndex = IndexOverflowException.safeCast(
            Math.min(index, elementsSize) * step);
        for (int i = startIndex, n = index; i < size; i += (int) step, ++n) {
          final int endIndex = (int) Math.min(size, i + maxSize);
          if (!elements.has(n)) {
            final L chunk = splitter.getChunk(wrapped, i, endIndex);
            elements.set(n, chunk);
            if (!safeConsume(predicate, elementsSize, n, chunk, LOGGER)) {
              return;
            }
          } else if (!safeConsume(predicate, elementsSize, n, elements.get(n), LOGGER)) {
            return;
          }
        }
        safeConsumeComplete(predicate, elementsSize, LOGGER);
      } else {
        wrapped.materializeSize(new CancellableAsyncConsumer<Integer>() {
          @Override
          public void cancellableAccept(final Integer size) {
            wrappedSize = size;
            elements.setSize(safeSize(wrappedSize, step));
            materializeNextWhile(index, predicate);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            predicate.error(error);
          }
        });
      }
    }

    @Override
    public void materializePrevWhile(final int index,
        @NotNull final IndexedAsyncPredicate<L> predicate) {
      if (wrappedSize >= 0) {
        final int size = wrappedSize;
        final long maxSize = this.maxSize;
        final long step = this.step;
        final int elementsSize = safeSize(size, this.step);
        final ElementsCache<L> elements = this.elements;
        final Splitter<E, ? extends L> splitter = this.splitter;
        final ListAsyncMaterializer<E> wrapped = this.wrapped;
        final int cappedIndex = Math.min(index, elementsSize - 1);
        final int startIndex = IndexOverflowException.safeCast(cappedIndex * step);
        for (int i = startIndex, n = cappedIndex; i >= 0; i -= (int) step, --n) {
          final int endIndex = (int) Math.min(size, i + maxSize);
          if (!elements.has(n)) {
            final L chunk = splitter.getChunk(wrapped, i, endIndex);
            elements.set(n, chunk);
            if (!safeConsume(predicate, elementsSize, n, chunk, LOGGER)) {
              return;
            }
          } else if (!safeConsume(predicate, elementsSize, n, elements.get(n), LOGGER)) {
            return;
          }
        }
        safeConsumeComplete(predicate, elementsSize, LOGGER);
      } else {
        wrapped.materializeSize(new CancellableAsyncConsumer<Integer>() {
          @Override
          public void cancellableAccept(final Integer size) {
            wrappedSize = size;
            elements.setSize(safeSize(wrappedSize, step));
            materializePrevWhile(index, predicate);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            predicate.error(error);
          }
        });
      }
    }

    @Override
    public void materializeSize(@NotNull final AsyncConsumer<Integer> consumer) {
      if (wrappedSize >= 0) {
        safeConsume(consumer, safeSize(wrappedSize, step), LOGGER);
      } else {
        wrapped.materializeSize(new CancellableAsyncConsumer<Integer>() {
          @Override
          public void cancellableAccept(final Integer size) {
            final int knownSize = safeSize(wrappedSize = size, step);
            elements.setSize(knownSize);
            safeConsume(consumer, knownSize, LOGGER);
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
      return wrappedSize < 0 ? wrapped.weightSize() : 1;
    }

    @Override
    public int weightElement() {
      return wrappedSize < 0 ? wrapped.weightSize() : 1;
    }

    @Override
    public int weightElements() {
      if (elementsConsumers.isEmpty()) {
        return wrappedSize < 0 ? wrapped.weightSize() : 1;
      }
      return 1;
    }

    @Override
    public int weightEmpty() {
      return wrapped.weightEmpty();
    }

    @Override
    public int weightHasElement() {
      return wrappedSize < 0 ? wrapped.weightSize() : 1;
    }

    @Override
    public int weightNextWhile() {
      return wrappedSize < 0 ? wrapped.weightSize() : 1;
    }

    @Override
    public int weightPrevWhile() {
      return wrappedSize < 0 ? wrapped.weightSize() : 1;
    }

    @Override
    public int weightSize() {
      return wrappedSize < 0 ? wrapped.weightSize() : 1;
    }

    private void consumeElements(@NotNull final List<L> elements) {
      final ArrayList<AsyncConsumer<List<L>>> elementsConsumers = this.elementsConsumers;
      for (final AsyncConsumer<List<L>> elementsConsumer : elementsConsumers) {
        safeConsume(elementsConsumer, elements, LOGGER);
      }
      elementsConsumers.clear();
    }

    private void consumeError(@NotNull final Exception error) {
      final ArrayList<AsyncConsumer<List<L>>> elementsConsumers = this.elementsConsumers;
      for (final AsyncConsumer<List<L>> elementsConsumer : elementsConsumers) {
        safeConsumeError(elementsConsumer, error, LOGGER);
      }
      elementsConsumers.clear();
    }

    private void materializeElements() {
      final int size = wrappedSize;
      final long maxSize = this.maxSize;
      final int step = this.step;
      final ElementsCache<L> elements = this.elements;
      final Splitter<E, ? extends L> splitter = this.splitter;
      final ListAsyncMaterializer<E> wrapped = this.wrapped;
      for (int i = 0, n = 0; i < size; i += step, ++n) {
        final int endIndex = (int) Math.min(size, i + maxSize);
        if (!elements.has(n)) {
          final L chunk = splitter.getChunk(wrapped, i, endIndex);
          elements.set(n, chunk);
        }
      }
      try {
        final List<L> materialized = decorateFunction.apply(elements.toList());
        setState(new ListToListAsyncMaterializer<L>(materialized, context));
        consumeElements(materialized);
      } catch (final Exception e) {
        if (e instanceof InterruptedException) {
          Thread.currentThread().interrupt();
        }
        setError(e);
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
