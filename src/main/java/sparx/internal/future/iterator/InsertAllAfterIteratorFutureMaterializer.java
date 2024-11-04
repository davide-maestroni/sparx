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

import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ContextTask;
import sparx.concurrent.ExecutionContext;
import sparx.util.SizeOverflowException;
import sparx.util.annotation.Positive;

public class InsertAllAfterIteratorFutureMaterializer<E> extends
    ProgressiveIteratorFutureMaterializer<E, E> {

  private static final Logger LOGGER = Logger.getLogger(
      InsertAllAfterIteratorFutureMaterializer.class.getName());

  private final boolean isMaterializedAtOnce;
  private final int knownSize;

  public InsertAllAfterIteratorFutureMaterializer(
      @NotNull final IteratorFutureMaterializer<E> wrapped, @Positive final int numElements,
      @NotNull final IteratorFutureMaterializer<E> elementsMaterializer,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    super(context);
    knownSize = safeSize(wrapped.knownSize(), elementsMaterializer.knownSize(), numElements);
    isMaterializedAtOnce =
        wrapped.isMaterializedAtOnce() && elementsMaterializer.isMaterializedAtOnce();
    setState(
        new ImmaterialState(wrapped, numElements, elementsMaterializer, context, cancelException));
  }

  private static int safeSize(final int wrappedSize, final int elementsSize, final int numElement) {
    if (wrappedSize >= numElement && elementsSize >= 0) {
      if (elementsSize > 0) {
        return SizeOverflowException.safeCast((long) wrappedSize + elementsSize);
      }
      return wrappedSize;
    }
    return -1;
  }

  @Override
  public boolean isMaterializedAtOnce() {
    return isMaterializedAtOnce || super.isMaterializedAtOnce();
  }

  @Override
  public int knownSize() {
    return knownSize;
  }

  private class ImmaterialState extends
      ProgressiveIteratorFutureMaterializer<E, E>.ImmaterialState {

    private final ExecutionContext context;
    private final IteratorFutureMaterializer<E> elementsMaterializer;
    private final int numElements;
    private final IteratorFutureMaterializer<E> wrapped;

    private int wrappedIndex;

    public ImmaterialState(@NotNull final IteratorFutureMaterializer<E> wrapped,
        @Positive final int numElements,
        @NotNull final IteratorFutureMaterializer<E> elementsMaterializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      super(wrapped, context, cancelException, LOGGER);
      this.wrapped = wrapped;
      this.numElements = numElements;
      this.elementsMaterializer = elementsMaterializer;
      this.context = context;
    }

    @Override
    public boolean isMaterializedAtOnce() {
      return wrapped.isMaterializedAtOnce() && elementsMaterializer.isMaterializedAtOnce();
    }

    @Override
    public int knownSize() {
      return knownSize;
    }

    @Override
    public void materializeCancel(@NotNull final CancellationException exception) {
      elementsMaterializer.materializeCancel(exception);
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
      if (wrappedIndex == numElements) {
        elementsMaterializer.materializeNextWhile(new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) {
            ++wrappedIndex;
            final String taskID = context.currentTaskID();
            context.scheduleAfter(new ContextTask(context) {
              @Override
              protected void runWithContext() {
                materializeUntilConsumed();
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
            ++wrappedIndex;
            final boolean next = setNextElement(element);
            if (next) {
              if (wrappedIndex == numElements) {
                final String taskID = context.currentTaskID();
                context.scheduleAfter(new ContextTask(context) {
                  @Override
                  protected void runWithContext() {
                    materializeUntilConsumed();
                  }

                  @Override
                  public @NotNull String taskID() {
                    return taskID != null ? taskID : "";
                  }

                  @Override
                  public int weight() {
                    return elementsMaterializer.weightNextWhile();
                  }
                });
                return false;
              }
            }
            return next;
          }

          @Override
          public void error(@NotNull final Exception error) {
            setNextError(error);
          }
        });
      }
    }

    @Override
    int weightNextElements() {
      return (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightNextWhile() + elementsMaterializer.weightNextWhile());
    }
  }
}
