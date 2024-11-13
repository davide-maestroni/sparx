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
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ContextTask;
import sparx.concurrent.ExecutionContext;
import sparx.util.DequeueList;
import sparx.util.function.TernaryFunction;

public class RemoveSliceIteratorFutureMaterializer<E> extends
    AbstractIteratorFutureMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      RemoveSliceIteratorFutureMaterializer.class.getName());

  private final boolean isMaterializedAtOnce;

  public RemoveSliceIteratorFutureMaterializer(@NotNull final IteratorFutureMaterializer<E> wrapped,
      final int start, final int end, @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final TernaryFunction<List<E>, Integer, Integer, List<E>> removeSliceFunction) {
    super(context);
    if (start >= 0) {
      isMaterializedAtOnce = false;
      if (end >= 0) {
        setState(
            new MaterialState(wrapped, start, Math.max(0, end - start), context, cancelException));
      } else {
        setState(new PendingState(wrapped, start, end, context, cancelException));
      }
    } else {
      isMaterializedAtOnce = wrapped.isMaterializedAtOnce();
      setState(new ImmaterialState(wrapped, start, end, cancelException, removeSliceFunction));
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

  private class ImmaterialState extends ImmediateIteratorFutureMaterializerState<E, E> {

    private final AtomicReference<CancellationException> cancelException;
    private final int end;
    private final TernaryFunction<List<E>, Integer, Integer, List<E>> removeSliceFunction;
    private final int start;
    private final IteratorFutureMaterializer<E> wrapped;

    public ImmaterialState(@NotNull final IteratorFutureMaterializer<E> wrapped, final int start,
        final int end, @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final TernaryFunction<List<E>, Integer, Integer, List<E>> removeSliceFunction) {
      super(RemoveSliceIteratorFutureMaterializer.this, wrapped, LOGGER);
      this.wrapped = wrapped;
      this.start = start;
      this.end = end;
      this.cancelException = cancelException;
      this.removeSliceFunction = removeSliceFunction;
    }

    @Override
    public int weightElements() {
      return isMaterializing() ? 1 : wrapped.weightElements();
    }

    @Override
    void materialize() {
      wrapped.materializeElements(new CancellableFutureConsumer<List<E>>() {
        @Override
        public void cancellableAccept(final List<E> elements) throws Exception {
          final List<E> materialized = removeSliceFunction.apply(elements, start, end);
          setDone(new ListToIteratorFutureMaterializer<E>(materialized, context));
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

  private class MaterialState extends ProgressiveIteratorFutureMaterializerState<E, E> {

    private final ExecutionContext context;
    private final int length;
    private final int start;
    private final IteratorFutureMaterializer<E> wrapped;

    private int index;

    private MaterialState(@NotNull final IteratorFutureMaterializer<E> wrapped, final int start,
        final int length, @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      super(RemoveSliceIteratorFutureMaterializer.this, wrapped, context, cancelException, LOGGER);
      this.wrapped = wrapped;
      this.start = start;
      this.length = length;
      this.context = context;
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
      wrapped.materializeNextWhile(new CancellableIndexedFuturePredicate<E>() {
        @Override
        public void cancellableComplete(final int size) {
          setComplete();
        }

        @Override
        public boolean cancellableTest(final int size, final int index, final E element)
            throws Exception {
          if (MaterialState.this.index++ == start) {
            if (length == 1) {
              return true;
            }
            final String taskID = context.currentTaskID();
            context.scheduleAfter(new ContextTask(context) {
              @Override
              protected void runWithContext() {
                final int toSkip = length - 1;
                wrapped.materializeSkip(toSkip, new CancellableFutureConsumer<Integer>() {
                  @Override
                  public void cancellableAccept(final Integer skipped) {
                    if (skipped < toSkip) {
                      setComplete();
                    } else {
                      materializeNext();
                    }
                  }

                  @Override
                  public void error(@NotNull final Exception error) {
                    setNextError(error);
                  }
                });
              }

              @Override
              public @NotNull String taskID() {
                return taskID != null ? taskID : "";
              }

              @Override
              public int weight() {
                return (int) Math.min(Integer.MAX_VALUE,
                    (long) wrapped.weightSkip() + wrapped.weightNextWhile());
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

  private class PendingState extends ProgressiveIteratorFutureMaterializerState<E, E> {

    private final DequeueList<E> cachedElements = new DequeueList<E>();
    private final ExecutionContext context;
    private final int end;
    private final int start;
    private final IteratorFutureMaterializer<E> wrapped;

    private IteratorFutureMaterializer<E> elementsMaterializer;
    private int index;

    private PendingState(@NotNull final IteratorFutureMaterializer<E> wrapped, final int start,
        final int end, @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      super(RemoveSliceIteratorFutureMaterializer.this, wrapped, context, cancelException, LOGGER);
      this.wrapped = wrapped;
      this.start = start;
      this.end = end;
      this.context = context;
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
              elementsMaterializer = new DequeueToIteratorFutureMaterializer<E>(cachedElements,
                  context, currentIndex());
              materializeUntilConsumed();
            } else {
              setComplete();
            }
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
