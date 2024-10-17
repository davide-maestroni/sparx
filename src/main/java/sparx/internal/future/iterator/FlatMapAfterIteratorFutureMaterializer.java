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
import java.util.concurrent.atomic.AtomicInteger;
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

public class FlatMapAfterIteratorFutureMaterializer<E> extends
    AbstractIteratorFutureMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      FlatMapAfterIteratorFutureMaterializer.class.getName());

  public FlatMapAfterIteratorFutureMaterializer(
      @NotNull final IteratorFutureMaterializer<E> wrapped, @Positive final int numElements,
      @NotNull final IndexedFunction<? super E, ? extends IteratorFutureMaterializer<E>> mapper,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    super(context, new AtomicInteger(STATUS_RUNNING));
    setState(new ImmaterialState(wrapped, numElements, mapper, context, cancelException));
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
    private final ArrayList<FutureConsumer<IteratorFutureMaterializer<E>>> elementsMaterializerConsumers = new ArrayList<FutureConsumer<IteratorFutureMaterializer<E>>>(
        2);
    private final IndexedFunction<? super E, ? extends IteratorFutureMaterializer<E>> mapper;
    private final int numElements;
    private final IteratorFutureMaterializer<E> wrapped;

    private IteratorFutureMaterializer<E> elementsMaterializer;
    private int index;
    private int wrappedIndex;

    public ImmaterialState(@NotNull final IteratorFutureMaterializer<E> wrapped,
        @Positive final int numElements,
        @NotNull final IndexedFunction<? super E, ? extends IteratorFutureMaterializer<E>> mapper,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      this.wrapped = wrapped;
      this.numElements = numElements;
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
      return -1;
    }

    @Override
    public void materializeCancel(@NotNull final CancellationException exception) {
      wrapped.materializeCancel(exception);
      final IteratorFutureMaterializer<E> elementsMaterializer = this.elementsMaterializer;
      if (elementsMaterializer != null) {
        elementsMaterializer.materializeCancel(exception);
      }
      setCancelled(exception);
      consumeError(exception);
    }

    @Override
    public void materializeElements(@NotNull final FutureConsumer<List<E>> consumer) {
      final ArrayList<FutureConsumer<List<E>>> elementsConsumers = this.elementsConsumers;
      elementsConsumers.add(consumer);
      if (elementsConsumers.size() == 1) {
        final DequeueList<E> materialized = new DequeueList<E>();
        final CancellableIndexedFuturePredicate<E> predicate = new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) {
            setDone(new DequeueToIteratorFutureMaterializer<E>(materialized, context));
            consumeElements(materialized);
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element) {
            final CancellableIndexedFuturePredicate<E> predicate = this;
            materialized.add(element);
            if (++wrappedIndex == numElements) {
              materialized(new FutureConsumer<IteratorFutureMaterializer<E>>() {
                @Override
                public void accept(final IteratorFutureMaterializer<E> materializer) {
                  if (materializer == null) {
                    wrapped.materializeNextWhile(predicate);
                  } else {
                    materializer.materializeElements(new CancellableFutureConsumer<List<E>>() {
                      @Override
                      public void cancellableAccept(final List<E> elements) {
                        materialized.addAll(elements);
                        wrapped.materializeNextWhile(predicate);
                      }

                      @Override
                      public void error(@NotNull final Exception error) {
                        setError(error);
                      }
                    });
                  }
                }

                @Override
                public void error(@NotNull final Exception error) {
                  setError(error);
                }
              });
              return false;
            }
            return true;
          }

          @Override
          public void error(@NotNull final Exception error) {
            setError(error);
          }
        };
        if (wrappedIndex == numElements) {
          materialized(new FutureConsumer<IteratorFutureMaterializer<E>>() {
            @Override
            public void accept(final IteratorFutureMaterializer<E> materializer) throws Exception {
              if (materializer == null) {
                predicate.complete(0);
              } else {
                materializer.materializeElements(new CancellableFutureConsumer<List<E>>() {
                  @Override
                  public void cancellableAccept(final List<E> elements) {
                    materialized.addAll(elements);
                    wrapped.materializeNextWhile(predicate);
                  }

                  @Override
                  public void error(@NotNull final Exception error) {
                    setError(error);
                  }
                });
              }
            }

            @Override
            public void error(@NotNull final Exception error) {
              setError(error);
            }
          });
        }
        wrapped.materializeNextWhile(predicate);
      }
    }

    @Override
    public void materializeHasNext(@NotNull final FutureConsumer<Boolean> consumer) {
      if (wrappedIndex == numElements) {
        materialized(new FutureConsumer<IteratorFutureMaterializer<E>>() {
          @Override
          public void accept(final IteratorFutureMaterializer<E> materializer) throws Exception {
            if (materializer == null) {
              consumer.accept(false);
            } else {
              materializer.materializeHasNext(new CancellableFutureConsumer<Boolean>() {
                @Override
                public void cancellableAccept(final Boolean hasNext) throws Exception {
                  if (hasNext) {
                    consumer.accept(true);
                  } else {
                    if (elementsMaterializer != null) {
                      ++wrappedIndex;
                      elementsMaterializer = null;
                    }
                    materializeHasNext(consumer);
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
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      } else {
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
      if (wrappedIndex == numElements) {
        materialized(new FutureConsumer<IteratorFutureMaterializer<E>>() {
          @Override
          public void accept(final IteratorFutureMaterializer<E> materializer) throws Exception {
            if (materializer == null) {
              consumer.complete(0);
            } else {
              materializer.materializeNext(new CancellableIndexedFutureConsumer<E>() {
                @Override
                public void cancellableAccept(final int size, final int index, final E element)
                    throws Exception {
                  consumer.accept(-1, ImmaterialState.this.index++, element);
                }

                @Override
                public void cancellableComplete(final int size) {
                  if (elementsMaterializer != null) {
                    ++wrappedIndex;
                    elementsMaterializer = null;
                  }
                  materializeNext(consumer);
                }

                @Override
                public void error(@NotNull final Exception error) throws Exception {
                  consumer.error(error);
                }
              });
            }
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      } else {
        wrapped.materializeNext(new CancellableIndexedFutureConsumer<E>() {
          @Override
          public void cancellableAccept(final int size, final int index, final E element)
              throws Exception {
            ++wrappedIndex;
            consumer.accept(safeSize(size), ImmaterialState.this.index++, element);
          }

          @Override
          public void cancellableComplete(final int size) throws Exception {
            setDone(EmptyIteratorFutureMaterializer.<E>instance());
            consumer.complete(0);
          }

          @Override
          public void error(@NotNull final Exception error) throws Exception {
            consumer.error(error);
          }
        });
      }
    }

    @Override
    public void materializeNextWhile(@NotNull final IndexedFuturePredicate<E> predicate) {
      new NextWhileFuturePredicate(predicate).run();
    }

    @Override
    public void materializeSkip(@Positive final int count,
        @NotNull final FutureConsumer<Integer> consumer) {
      new SkipFutureConsumer(count, consumer).run();
    }

    @Override
    public int weightElements() {
      return elementsConsumers.isEmpty() ? elementsMaterializer != null ? (int) Math.min(
          Integer.MAX_VALUE,
          (long) wrapped.weightNextWhile() + elementsMaterializer.weightElements())
          : (int) Math.min(Integer.MAX_VALUE,
              (long) wrapped.weightNext() + wrapped.weightNextWhile()) : 1;
    }

    @Override
    public int weightHasNext() {
      return elementsMaterializer != null ? (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightHasNext() + elementsMaterializer.weightHasNext())
          : (int) Math.min(Integer.MAX_VALUE,
              (long) wrapped.weightNext() + wrapped.weightHasNext());
    }

    @Override
    public int weightNext() {
      return elementsMaterializer != null ? (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightNext() + elementsMaterializer.weightNext())
          : (int) Math.min(Integer.MAX_VALUE, (long) wrapped.weightNext() + wrapped.weightNext());
    }

    @Override
    public int weightNextWhile() {
      return elementsMaterializer != null ? (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightNextWhile() + elementsMaterializer.weightNextWhile())
          : (int) Math.min(Integer.MAX_VALUE,
              (long) wrapped.weightNext() + wrapped.weightNextWhile());
    }

    @Override
    public int weightSkip() {
      return elementsMaterializer != null ? (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightSkip() + elementsMaterializer.weightSkip())
          : (int) Math.min(Integer.MAX_VALUE, (long) wrapped.weightNext() + wrapped.weightSkip());
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

    private @NotNull String getTaskID() {
      final String taskID = context.currentTaskID();
      return taskID != null ? taskID : "";
    }

    private void materialized(
        @NotNull final FutureConsumer<IteratorFutureMaterializer<E>> consumer) {
      if (elementsMaterializer != null) {
        safeConsume(consumer, elementsMaterializer, LOGGER);
      } else {
        final ArrayList<FutureConsumer<IteratorFutureMaterializer<E>>> elementsMaterializerConsumers = this.elementsMaterializerConsumers;
        elementsMaterializerConsumers.add(consumer);
        if (elementsMaterializerConsumers.size() == 1) {
          wrapped.materializeNext(new CancellableIndexedFutureConsumer<E>() {
            @Override
            public void cancellableAccept(final int size, final int index, final E element)
                throws Exception {
              elementsMaterializer = mapper.apply(numElements, element);
              for (final FutureConsumer<IteratorFutureMaterializer<E>> consumer : elementsMaterializerConsumers) {
                safeConsume(consumer, elementsMaterializer, LOGGER);
              }
              elementsMaterializerConsumers.clear();
            }

            @Override
            public void cancellableComplete(final int size) {
              for (final FutureConsumer<IteratorFutureMaterializer<E>> consumer : elementsMaterializerConsumers) {
                safeConsume(consumer, null, LOGGER);
              }
              elementsMaterializerConsumers.clear();
            }

            @Override
            public void error(@NotNull final Exception error) {
              for (final FutureConsumer<IteratorFutureMaterializer<E>> consumer : elementsMaterializerConsumers) {
                safeConsumeError(consumer, error, LOGGER);
              }
              elementsMaterializerConsumers.clear();
            }
          });
        }
      }
    }

    private int safeSize(final int wrappedSize) {
      if (wrappedIndex > numElements) {
        return wrappedSize;
      }
      return -1;
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

    private class NextWhileFuturePredicate extends
        CancellableMultiFuturePredicate<IteratorFutureMaterializer<E>, E> {

      private final IndexedFuturePredicate<E> predicate;

      private boolean isWrapped;
      private String taskID;

      private NextWhileFuturePredicate(@NotNull final IndexedFuturePredicate<E> predicate) {
        this.predicate = predicate;
      }

      @Override
      public void cancellableAccept(final IteratorFutureMaterializer<E> materializer)
          throws Exception {
        if (materializer == null) {
          predicate.complete(0);
        } else {
          isWrapped = false;
          materializer.materializeNextWhile(this);
        }
      }

      @Override
      public void cancellableComplete(final int size) throws Exception {
        if (isWrapped) {
          setDone(EmptyIteratorFutureMaterializer.<E>instance());
          predicate.complete(0);
        } else {
          if (elementsMaterializer != null) {
            ++wrappedIndex;
            elementsMaterializer = null;
          }
          schedule();
        }
      }

      @Override
      public boolean cancellableTest(final int size, final int index, final E element)
          throws Exception {
        if (isWrapped) {
          if (++wrappedIndex == numElements) {
            if (predicate.test(safeSize(size), ImmaterialState.this.index++, element)) {
              schedule();
            }
            return false;
          }
        }
        return predicate.test(-1, ImmaterialState.this.index++, element);
      }

      @Override
      public void error(@NotNull final Exception error) throws Exception {
        predicate.error(error);
      }

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      protected void runWithContext() {
        if (wrappedIndex == numElements) {
          materialized(this);
        } else {
          isWrapped = true;
          wrapped.materializeNextWhile(this);
        }
      }

      private void schedule() {
        taskID = getTaskID();
        context.scheduleAfter(this);
      }
    }

    private class SkipFutureConsumer extends CancellableFutureConsumer<Integer> {

      private final FutureConsumer<Integer> consumer;
      private final int count;

      private boolean isWrapped;
      private int skipped;
      private String taskID;

      private final FutureConsumer<IteratorFutureMaterializer<E>> materializerConsumer = new FutureConsumer<IteratorFutureMaterializer<E>>() {
        @Override
        public void accept(final IteratorFutureMaterializer<E> materializer) throws Exception {
          if (materializer == null) {
            consumer.accept(skipped);
          } else {
            isWrapped = false;
            materializer.materializeSkip(count - skipped, SkipFutureConsumer.this);
          }
        }

        @Override
        public void error(@NotNull final Exception error) throws Exception {
          consumer.error(error);
        }
      };

      private SkipFutureConsumer(final int count, @NotNull final FutureConsumer<Integer> consumer) {
        this.count = count;
        this.consumer = consumer;
      }

      @Override
      public void cancellableAccept(final Integer skipped) throws Exception {
        index += skipped;
        this.skipped += skipped;
        if (isWrapped) {
          wrappedIndex += skipped;
          if (wrappedIndex > numElements || this.skipped >= count) {
            consumer.accept(this.skipped);
            return;
          }
        } else if (this.skipped >= count) {
          consumer.accept(this.skipped);
          return;
        } else {
          ++wrappedIndex;
        }
        schedule();
      }

      @Override
      public void error(@NotNull final Exception error) throws Exception {
        consumer.error(error);
      }

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      protected void runWithContext() {
        if (wrappedIndex == numElements) {
          materialized(materializerConsumer);
        } else {
          isWrapped = true;
          if (wrappedIndex < numElements) {
            wrapped.materializeSkip(Math.min(count - skipped, numElements - wrappedIndex), this);
          } else {
            wrapped.materializeSkip(count - skipped, this);
          }
        }
      }

      private void schedule() {
        taskID = getTaskID();
        context.scheduleAfter(this);
      }
    }
  }
}
