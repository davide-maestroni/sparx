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

import java.util.Iterator;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ContextTask;
import sparx.concurrent.ExecutionContext;
import sparx.util.DequeArrayList;
import sparx.util.SizeOverflowException;
import sparx.util.annotation.Positive;
import sparx.util.function.Function;

public class SlidingWindowIteratorFutureMaterializer<E, I extends Iterator<E>> extends
    AbstractIteratorFutureMaterializer<I> {

  private static final Logger LOGGER = Logger.getLogger(
      SlidingWindowIteratorFutureMaterializer.class.getName());

  private final int knownSize;

  public SlidingWindowIteratorFutureMaterializer(
      @NotNull final IteratorFutureMaterializer<E> wrapped, @Positive final int maxSize,
      @Positive final int step, @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final Function<? super DequeArrayList<E>, ? extends I> mapper) {
    super(context);
    setState(
        new ImmaterialState(wrapped, maxSize, 0, step, null, context, cancelException, mapper));
    knownSize = safeZie(wrapped.knownSize(), step);
  }

  public SlidingWindowIteratorFutureMaterializer(
      @NotNull final IteratorFutureMaterializer<E> wrapped, @Positive final int size,
      @Positive final int step, final E padding, @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final Function<? super DequeArrayList<E>, ? extends I> mapper) {
    super(context);
    setState(
        new ImmaterialState(wrapped, size, size, step, padding, context, cancelException, mapper));
    knownSize = safeZie(wrapped.knownSize(), step);
  }

  private static int safeZie(final int knownSize, final int step) {
    if (knownSize > 0) {
      if (knownSize < step) {
        return 1;
      }
      return SizeOverflowException.safeCast((knownSize + (step >> 1)) / step);
    }
    return -1;
  }

  @Override
  public int knownSize() {
    return knownSize;
  }

  private class ImmaterialState extends ProgressiveIteratorFutureMaterializerState<E, I> {

    private final DequeArrayList<E> elements;
    private final Function<? super DequeArrayList<E>, ? extends I> mapper;
    private final int maxSize;
    private final int size;
    private final int skip;
    private final int step;
    private final E padding;
    private final IteratorFutureMaterializer<E> wrapped;

    public ImmaterialState(@NotNull final IteratorFutureMaterializer<E> wrapped, final int maxSize,
        final int size, final int step, final E padding, @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Function<? super DequeArrayList<E>, ? extends I> mapper) {
      super(SlidingWindowIteratorFutureMaterializer.this, wrapped, context, cancelException,
          LOGGER);
      this.wrapped = wrapped;
      this.maxSize = maxSize;
      this.size = size;
      this.step = step;
      this.padding = padding;
      this.mapper = mapper;
      skip = Math.max(0, step - maxSize);
      elements = new DequeArrayList<E>(maxSize);
    }

    @Override
    boolean addElement(final E element) {
      return true;
    }

    @Override
    I mapElement(final E element) throws Exception {
      final DequeArrayList<E> clone = elements.clone();
      while (clone.size() < size) {
        clone.add(padding);
      }
      return mapper.apply(clone);
    }

    @Override
    void materializeNext() {
      final IteratorFutureMaterializer<E> wrapped = this.wrapped;
      final DequeArrayList<E> elements = this.elements;
      final int maxSize = this.maxSize;
      final int skip = this.skip;
      if (elements.isEmpty()) {
        wrapped.materializeNextWhile(new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) throws Exception {
            if (elements.isEmpty()) {
              setDone(EmptyIteratorFutureMaterializer.<I>instance());
              setComplete();
            } else if (setNextElement(null)) {
              final String taskID = context.currentTaskID();
              context.scheduleAfter(new ContextTask(context) {
                @Override
                protected void runWithContext() {
                  materializeNext();
                }

                @Override
                public @NotNull String taskID() {
                  return taskID != null ? taskID : "";
                }

                @Override
                public int weight() {
                  return skip > 0 ? wrapped.weightSkip() : wrapped.weightNextWhile();
                }
              });
            }
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            elements.add(element);
            if (elements.size() < maxSize) {
              return true;
            }
            if (setNextElement(null)) {
              final String taskID = context.currentTaskID();
              context.scheduleAfter(new ContextTask(context) {
                @Override
                protected void runWithContext() {
                  materializeNext();
                }

                @Override
                public @NotNull String taskID() {
                  return taskID != null ? taskID : "";
                }

                @Override
                public int weight() {
                  return skip > 0 ? wrapped.weightSkip() : wrapped.weightNextWhile();
                }
              });
            }
            return false;
          }

          @Override
          public void error(@NotNull final Exception error) {
            setNextError(error);
          }
        });
      } else if (skip > 0) {
        elements.clear();
        wrapped.materializeSkip(skip, new CancellableFutureConsumer<Integer>() {
          @Override
          public void cancellableAccept(final Integer skipped) throws Exception {
            if (skipped < skip) {
              setDone(EmptyIteratorFutureMaterializer.<I>instance());
              setComplete();
            } else {
              final String taskID = context.currentTaskID();
              context.scheduleAfter(new ContextTask(context) {
                @Override
                protected void runWithContext() {
                  materializeNext();
                }

                @Override
                public @NotNull String taskID() {
                  return taskID != null ? taskID : "";
                }

                @Override
                public int weight() {
                  return wrapped.weightNextWhile();
                }
              });
            }
          }

          @Override
          public void error(@NotNull final Exception error) {
            setNextError(error);
          }
        });
      } else {
        wrapped.materializeNextWhile(new CancellableIndexedFuturePredicate<E>() {
          private int count;

          @Override
          public void cancellableComplete(final int size) throws Exception {
            if (elements.isEmpty() || elements.size() <= (step - count)) {
              setDone(EmptyIteratorFutureMaterializer.<I>instance());
              setComplete();
            } else {
              for (int i = count; i < step; ++i) {
                elements.removeFirst();
              }
              if (setNextElement(null)) {
                final String taskID = context.currentTaskID();
                context.scheduleAfter(new ContextTask(context) {
                  @Override
                  protected void runWithContext() {
                    materializeNext();
                  }

                  @Override
                  public @NotNull String taskID() {
                    return taskID != null ? taskID : "";
                  }

                  @Override
                  public int weight() {
                    return wrapped.weightNextWhile();
                  }
                });
              }
            }
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            elements.removeFirst();
            elements.add(element);
            if (++count < step) {
              return true;
            }
            if (setNextElement(null)) {
              final String taskID = context.currentTaskID();
              context.scheduleAfter(new ContextTask(context) {
                @Override
                protected void runWithContext() {
                  materializeNext();
                }

                @Override
                public @NotNull String taskID() {
                  return taskID != null ? taskID : "";
                }

                @Override
                public int weight() {
                  return wrapped.weightNextWhile();
                }
              });
            }
            return false;
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
