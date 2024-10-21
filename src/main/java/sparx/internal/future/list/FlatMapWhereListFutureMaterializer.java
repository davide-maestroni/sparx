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

import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.iterator.IteratorFutureMaterializer;
import sparx.util.function.IndexedFunction;
import sparx.util.function.IndexedPredicate;

public class FlatMapWhereListFutureMaterializer<E> extends ProgressiveListFutureMaterializer<E, E> {

  private static final Logger LOGGER = Logger.getLogger(
      FlatMapWhereListFutureMaterializer.class.getName());

  public FlatMapWhereListFutureMaterializer(@NotNull final ListFutureMaterializer<E> wrapped,
      @NotNull final IndexedPredicate<? super E> predicate,
      @NotNull final IndexedFunction<? super E, ? extends IteratorFutureMaterializer<E>> mapper,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    super(context);
    setState(new ImmaterialState(wrapped, predicate, mapper, context, cancelException));
  }

  private class ImmaterialState extends ProgressiveListFutureMaterializer<E, E>.ImmaterialState {

    private final ExecutionContext context;
    private final IndexedFunction<? super E, ? extends IteratorFutureMaterializer<E>> mapper;
    private final IndexedPredicate<? super E> predicate;
    private final ListFutureMaterializer<E> wrapped;

    private IteratorFutureMaterializer<E> elementsMaterializer;
    private int nextIndex;

    public ImmaterialState(@NotNull final ListFutureMaterializer<E> wrapped,
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends IteratorFutureMaterializer<E>> mapper,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      super(wrapped, context, cancelException, LOGGER);
      this.wrapped = wrapped;
      this.predicate = predicate;
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
    public int weightElements() {
      return needsMaterializing() ? elementsMaterializer != null
          ? elementsMaterializer.weightNextWhile() : wrapped.weightElement() : 1;
    }

    @Override
    void materializeNext() {
      new MaterializingFutureConsumer().run();
    }

    private @NotNull String getTaskID() {
      final String taskID = context.currentTaskID();
      return taskID != null ? taskID : "";
    }

    private class MaterializingFutureConsumer extends CancellableIndexedFutureConsumer<E> {

      private String taskID;

      private final CancellableIndexedFuturePredicate<E> elementsPredicate = new CancellableIndexedFuturePredicate<E>() {
        @Override
        public void cancellableComplete(final int size) {
          elementsMaterializer = null;
          schedule();
        }

        @Override
        public boolean cancellableTest(final int size, final int index, final E element) {
          return setNextElement(element);
        }

        @Override
        public void error(@NotNull final Exception error) {
          setError(error);
        }
      };

      @Override
      public void cancellableAccept(final int size, final int index, final E element)
          throws Exception {
        ++nextIndex;
        if (predicate.test(index, element)) {
          final IteratorFutureMaterializer<E> materializer = mapper.apply(index, element);
          if (materializer.knownSize() == 0) {
            schedule();
          } else {
            (elementsMaterializer = materializer).materializeNextWhile(elementsPredicate);
          }
        } else if (setNextElement(element)) {
          schedule();
        }
      }

      @Override
      public void cancellableComplete(final int size) throws Exception {
        setComplete();
      }

      @Override
      public void error(@NotNull final Exception error) {
        setError(error);
      }

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        return wrapped.weightElement();
      }

      @Override
      protected void runWithContext() {
        if (elementsMaterializer != null) {
          elementsMaterializer.materializeNextWhile(elementsPredicate);
        } else {
          wrapped.materializeElement(nextIndex, this);
        }
      }

      private void schedule() {
        taskID = getTaskID();
        context.scheduleAfter(this);
      }
    }
  }
}
