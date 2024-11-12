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
    super(context);
    setState(new ImmaterialState(wrapped, numElements, mapper, context, cancelException));
  }

  @Override
  public int knownSize() {
    return -1;
  }

  private class ImmaterialState extends ProgressiveIteratorFutureMaterializerState<E, E> {

    private final ExecutionContext context;
    private final IndexedFunction<? super E, ? extends IteratorFutureMaterializer<E>> mapper;
    private final int numElements;
    private final IteratorFutureMaterializer<E> wrapped;

    private IteratorFutureMaterializer<E> elementsMaterializer;
    private int wrappedIndex;

    public ImmaterialState(@NotNull final IteratorFutureMaterializer<E> wrapped,
        @Positive final int numElements,
        @NotNull final IndexedFunction<? super E, ? extends IteratorFutureMaterializer<E>> mapper,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      super(FlatMapAfterIteratorFutureMaterializer.this, wrapped, context, cancelException, LOGGER);
      this.wrapped = wrapped;
      this.numElements = numElements;
      this.mapper = mapper;
      this.context = context;
    }

    @Override
    public void materializeCancel(@NotNull final CancellationException exception) {
      final IteratorFutureMaterializer<E> elementsMaterializer = this.elementsMaterializer;
      if (elementsMaterializer != null) {
        elementsMaterializer.materializeCancel(exception);
      }
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
            ImmaterialState.this.elementsMaterializer = null;
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
            final int wrappedIndex = ImmaterialState.this.wrappedIndex++;
            if (wrappedIndex == numElements) {
              elementsMaterializer = mapper.apply(wrappedIndex, element);
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
            return setNextElement(element);
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
      return elementsMaterializer != null ? (int) Math.min(Integer.MAX_VALUE,
          (long) wrapped.weightNextWhile() + elementsMaterializer.weightNextWhile())
          : (int) Math.min(Integer.MAX_VALUE,
              (long) wrapped.weightNext() + wrapped.weightNextWhile());
    }
  }
}
