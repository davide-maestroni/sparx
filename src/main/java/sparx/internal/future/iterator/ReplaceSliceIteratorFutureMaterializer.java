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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ContextTask;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.FutureConsumer;
import sparx.internal.future.IndexedFutureConsumer;
import sparx.internal.future.IndexedFuturePredicate;
import sparx.util.DequeueList;
import sparx.util.annotation.Positive;
import sparx.util.function.BinaryFunction;

public class ReplaceSliceIteratorFutureMaterializer<E> extends
    AbstractIteratorFutureMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      ReplaceSliceIteratorFutureMaterializer.class.getName());

  private final boolean isMaterializedAtOnce;

  public ReplaceSliceIteratorFutureMaterializer(
      @NotNull final IteratorFutureMaterializer<E> wrapped, final int start, final int end,
      @NotNull final IteratorFutureMaterializer<E> elementsMaterializer,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final BinaryFunction<List<E>, List<E>, List<E>> prependFunction) {
    super(context);
    if (start >= 0) {
      isMaterializedAtOnce = false;
      if (end >= 0) {
        setState(new MaterialState(wrapped, start, Math.max(0, end - start), elementsMaterializer,
            context, cancelException, prependFunction));
      } else {
        setState(
            new PendingState(wrapped, start, end, elementsMaterializer, context, cancelException,
                prependFunction));
      }
    } else {
      isMaterializedAtOnce =
          wrapped.isMaterializedAtOnce() && elementsMaterializer.isMaterializedAtOnce();
      setState(
          new ImmaterialState(wrapped, start, end, elementsMaterializer, context, cancelException));
    }
  }

  @Override
  public boolean isMaterializedAtOnce() {
    return isMaterializedAtOnce || super.isMaterializedAtOnce();
  }

  @Override
  public int knownSize() {
    return -1;
  }

  private class ImmaterialState implements IteratorFutureMaterializer<E> {

    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final ArrayList<FutureConsumer<List<E>>> elementsConsumers = new ArrayList<FutureConsumer<List<E>>>(
        2);
    private final int end;
    private final ArrayList<FutureConsumer<IteratorFutureMaterializer<E>>> materializerConsumers = new ArrayList<FutureConsumer<IteratorFutureMaterializer<E>>>(
        2);
    private final IteratorFutureMaterializer<E> patchMaterializer;
    private final int start;
    private final IteratorFutureMaterializer<E> wrapped;

    public ImmaterialState(@NotNull final IteratorFutureMaterializer<E> wrapped, final int start,
        final int end, @NotNull final IteratorFutureMaterializer<E> patchMaterializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      this.wrapped = wrapped;
      this.start = start;
      this.end = end;
      this.patchMaterializer = patchMaterializer;
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
      wrapped.materializeCancel(exception);
      patchMaterializer.materializeCancel(exception);
      setCancelled(exception);
      consumeError(exception);
    }

    @Override
    public void materializeElements(@NotNull final FutureConsumer<List<E>> consumer) {
      final ArrayList<FutureConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      elementsConsumers.add(consumer);
      if (elementsConsumers.size() == 1) {
        materialize(new FutureConsumer<IteratorFutureMaterializer<E>>() {
          @Override
          public void accept(final IteratorFutureMaterializer<E> materializer) {
            materializer.materializeElements(new FutureConsumer<List<E>>() {
              @Override
              public void accept(final List<E> elements) {
                setDone(new ListToIteratorFutureMaterializer<E>(elements, context));
                consumeElements(elements);
              }

              @Override
              public void error(@NotNull final Exception error) {
                setError(error);
              }
            });
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
      materialize(new FutureConsumer<IteratorFutureMaterializer<E>>() {
        @Override
        public void accept(final IteratorFutureMaterializer<E> materializer) {
          materializer.materializeHasNext(consumer);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeIterator(@NotNull final FutureConsumer<Iterator<E>> consumer) {
      materializeElements(new FutureConsumer<List<E>>() {
        @Override
        public void accept(final List<E> elements) {
          getState().materializeIterator(consumer);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeNext(@NotNull final IndexedFutureConsumer<E> consumer) {
      materialize(new FutureConsumer<IteratorFutureMaterializer<E>>() {
        @Override
        public void accept(final IteratorFutureMaterializer<E> materializer) {
          materializer.materializeNext(consumer);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public void materializeNextWhile(@NotNull final IndexedFuturePredicate<E> predicate) {
      materialize(new FutureConsumer<IteratorFutureMaterializer<E>>() {
        @Override
        public void accept(final IteratorFutureMaterializer<E> materializer) {
          materializer.materializeNextWhile(predicate);
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
      materialize(new FutureConsumer<IteratorFutureMaterializer<E>>() {
        @Override
        public void accept(final IteratorFutureMaterializer<E> materializer) {
          materializer.materializeSkip(count, consumer);
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      });
    }

    @Override
    public int weightElements() {
      return elementsConsumers.isEmpty() ? 1 : weightMaterialize();
    }

    @Override
    public int weightHasNext() {
      return weightMaterialize();
    }

    @Override
    public int weightNext() {
      return weightMaterialize();
    }

    @Override
    public int weightNextWhile() {
      return weightMaterialize();
    }

    @Override
    public int weightSkip() {
      return weightMaterialize();
    }

    private void consumeElements(@NotNull final List<E> elements) {
      final ArrayList<FutureConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      for (final FutureConsumer<List<E>> elementsConsumer : elementsConsumers) {
        safeConsume(elementsConsumer, elements, LOGGER);
      }
      elementsConsumers.clear();
    }

    private void consumeError(@NotNull final Exception error) {
      final ArrayList<FutureConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      for (final FutureConsumer<List<E>> elementsConsumer : elementsConsumers) {
        safeConsumeError(elementsConsumer, error, LOGGER);
      }
      elementsConsumers.clear();
    }

    private void materialize(
        @NotNull final FutureConsumer<IteratorFutureMaterializer<E>> consumer) {
      final ArrayList<FutureConsumer<IteratorFutureMaterializer<E>>> materializerConsumers = this.materializerConsumers;
      materializerConsumers.add(consumer);
      if (materializerConsumers.size() == 1) {
        wrapped.materializeElements(new CancellableFutureConsumer<List<E>>() {
          @Override
          public void cancellableAccept(final List<E> elements) {
            final int size = elements.size();
            int materializedStart = start;
            if (materializedStart < 0) {
              materializedStart = Math.max(0, size + materializedStart);
            }
            int materializedEnd = end;
            if (materializedEnd < 0) {
              materializedEnd = Math.max(0, size + materializedEnd);
            } else {
              materializedEnd = Math.min(size, materializedEnd);
            }
            final IteratorFutureMaterializer<E> materializer;
            if (materializedStart >= materializedEnd) {
              materializer = new ListToIteratorFutureMaterializer<E>(elements, context);
            } else {
              final DequeueList<E> materialized = new DequeueList<E>();
              materialized.addAll(elements.subList(0, materializedStart));
              if (materializedEnd < size) {
                materialized.addAll(elements.subList(materializedEnd, size));
              }
              materializer = new DequeueToIteratorFutureMaterializer<E>(materialized, context);
            }
            final IteratorFutureMaterializer<E> newState = setState(
                new InsertAllAfterIteratorFutureMaterializer<E>(materializer, materializedStart,
                    patchMaterializer, context, cancelException));
            for (FutureConsumer<IteratorFutureMaterializer<E>> consumer : materializerConsumers) {
              safeConsume(consumer, newState, LOGGER);
            }
            materializerConsumers.clear();
          }

          @Override
          public void error(@NotNull final Exception error) {
            final IteratorFutureMaterializer<E> newState;
            final CancellationException exception = cancelException.get();
            if (exception != null) {
              newState = setCancelled(exception);
            } else {
              newState = setFailed(error);
            }
            for (FutureConsumer<IteratorFutureMaterializer<E>> consumer : materializerConsumers) {
              safeConsume(consumer, newState, LOGGER);
            }
            materializerConsumers.clear();
          }
        });
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

    private int weightMaterialize() {
      return materializerConsumers.isEmpty() ? 1 : wrapped.weightElements();
    }
  }

  private class MaterialState extends ProgressiveIteratorFutureMaterializerState<E, E> {

    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final IteratorFutureMaterializer<E> patchMaterializer;
    private final int length;
    private final BinaryFunction<List<E>, List<E>, List<E>> prependFunction;
    private final int start;
    private final IteratorFutureMaterializer<E> wrapped;

    private int index;
    private IteratorFutureMaterializer<E> elementsMaterializer;

    private MaterialState(@NotNull final IteratorFutureMaterializer<E> wrapped, final int start,
        final int length, @NotNull final IteratorFutureMaterializer<E> patchMaterializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final BinaryFunction<List<E>, List<E>, List<E>> prependFunction) {
      super(ReplaceSliceIteratorFutureMaterializer.this, wrapped, context, cancelException, LOGGER);
      this.wrapped = wrapped;
      this.start = start;
      this.length = length;
      this.patchMaterializer = patchMaterializer;
      this.context = context;
      this.cancelException = cancelException;
      this.prependFunction = prependFunction;
    }

    @Override
    public void materializeCancel(@NotNull final CancellationException exception) {
      patchMaterializer.materializeCancel(exception);
      super.materializeCancel(exception);
    }

    @Override
    boolean addElement(final E element) {
      return true;
    }

    @Override
    E mapElement(final E element) {
      return element;
    }

    @Override
    void materializeNext() {
      if (elementsMaterializer != null) {
        elementsMaterializer.materializeNextWhile(new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) {
            setComplete();
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            return setNextElement(element);
          }

          @Override
          public void error(@NotNull final Exception error) {
            setNextError(error);
          }
        });
      } else {
        wrapped.materializeNextWhile(new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) {
            setComplete();
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            if (MaterialState.this.index++ == start) {
              final String taskID = context.currentTaskID();
              context.scheduleAfter(new ContextTask(context) {
                @Override
                protected void runWithContext() {
                  elementsMaterializer = new InsertAllIteratorFutureMaterializer<E>(wrapped,
                      patchMaterializer, context, cancelException, prependFunction);
                  if (length == 1) {
                    materializeUntilConsumed();
                  } else {
                    wrapped.materializeSkip(length - 1, new CancellableFutureConsumer<Integer>() {
                      @Override
                      public void cancellableAccept(final Integer skipped) {
                        materializeUntilConsumed();
                      }

                      @Override
                      public void error(@NotNull final Exception error) {
                        setNextError(error);
                      }
                    });
                  }
                }

                @Override
                public @NotNull String taskID() {
                  return taskID != null ? taskID : "";
                }

                @Override
                public int weight() {
                  return length == 1 ? (int) Math.min(Integer.MAX_VALUE,
                      (long) wrapped.weightNextWhile() + patchMaterializer.weightNextWhile())
                      : (int) Math.min(Integer.MAX_VALUE,
                          (long) wrapped.weightSkip() + wrapped.weightNextWhile()
                              + patchMaterializer.weightNextWhile());
                }
              });
              return false;
            }
            return setNextElement(element);
          }

          @Override
          public void error(@NotNull final Exception error) {
            setNextError(error);
          }
        });
      }
    }
  }

  private class PendingState extends ProgressiveIteratorFutureMaterializerState<E, E> {

    private final DequeueList<E> cachedElements = new DequeueList<E>();
    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final int end;
    private final IteratorFutureMaterializer<E> patchMaterializer;
    private final BinaryFunction<List<E>, List<E>, List<E>> prependFunction;
    private final int start;
    private final IteratorFutureMaterializer<E> wrapped;

    private IteratorFutureMaterializer<E> elementsMaterializer;
    private int index;

    private PendingState(@NotNull final IteratorFutureMaterializer<E> wrapped, final int start,
        final int end, @NotNull final IteratorFutureMaterializer<E> patchMaterializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final BinaryFunction<List<E>, List<E>, List<E>> prependFunction) {
      super(ReplaceSliceIteratorFutureMaterializer.this, wrapped, context, cancelException, LOGGER);
      this.wrapped = wrapped;
      this.start = start;
      this.end = end;
      this.patchMaterializer = patchMaterializer;
      this.context = context;
      this.cancelException = cancelException;
      this.prependFunction = prependFunction;
    }

    @Override
    public void materializeCancel(@NotNull final CancellationException exception) {
      patchMaterializer.materializeCancel(exception);
      super.materializeCancel(exception);
    }

    @Override
    boolean addElement(final E element) {
      return true;
    }

    @Override
    E mapElement(final E element) {
      return element;
    }

    @Override
    void materializeNext() {
      if (elementsMaterializer != null) {
        elementsMaterializer.materializeNextWhile(new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) {
            setComplete();
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            return setNextElement(element);
          }

          @Override
          public void error(@NotNull final Exception error) {
            setNextError(error);
          }
        });
      } else {
        final DequeueList<E> cachedElements = this.cachedElements;
        wrapped.materializeNextWhile(new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) {
            if (!cachedElements.isEmpty()) {
              final int endIndex = Math.max(0, index + end - 1);
              for (int i = 0; i < endIndex; ++i) {
                cachedElements.removeFirst();
              }
              elementsMaterializer = new InsertAllIteratorFutureMaterializer<E>(
                  new DequeueToIteratorFutureMaterializer<E>(cachedElements, context,
                      currentIndex()), patchMaterializer, context, cancelException,
                  prependFunction);
            } else {
              elementsMaterializer = patchMaterializer;
            }
            materializeUntilConsumed();
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            if (PendingState.this.index++ >= start) {
              cachedElements.add(element);
              return true;
            }
            return setNextElement(element);
          }

          @Override
          public void error(@NotNull final Exception error) {
            setNextError(error);
          }
        });
      }
    }
  }
}
