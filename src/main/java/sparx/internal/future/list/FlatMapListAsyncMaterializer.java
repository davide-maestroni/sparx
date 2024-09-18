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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.concurrent.ExecutionContext.Task;
import sparx.internal.future.iterator.IteratorAsyncMaterializer;
import sparx.util.function.IndexedFunction;

public class FlatMapListAsyncMaterializer<E, F> extends ProgressiveListAsyncMaterializer<E, F> {

  private static final Logger LOGGER = Logger.getLogger(
      FlatMapListAsyncMaterializer.class.getName());

  public FlatMapListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      @NotNull final IndexedFunction<? super E, ? extends IteratorAsyncMaterializer<F>> mapper,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    super(context, new AtomicInteger(STATUS_RUNNING));
    setState(new ImmaterialState(wrapped, mapper, context, cancelException));
  }

  private class ImmaterialState extends ProgressiveListAsyncMaterializer<E, F>.ImmaterialState {

    private final ExecutionContext context;
    private final IndexedFunction<? super E, ? extends IteratorAsyncMaterializer<F>> mapper;
    private final ListAsyncMaterializer<E> wrapped;

    private IteratorAsyncMaterializer<F> elementsMaterializer;
    private int nextIndex;

    public ImmaterialState(@NotNull final ListAsyncMaterializer<E> wrapped,
        @NotNull final IndexedFunction<? super E, ? extends IteratorAsyncMaterializer<F>> mapper,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      super(wrapped, context, cancelException, LOGGER);
      this.wrapped = wrapped;
      this.mapper = mapper;
      this.context = context;
    }

    @Override
    public void materializeCancel(@NotNull final CancellationException exception) {
      final IteratorAsyncMaterializer<F> elementsMaterializer = this.elementsMaterializer;
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
      new MaterializingAsyncConsumer().run();
    }

    private @NotNull String getTaskID() {
      final String taskID = context.currentTaskID();
      return taskID != null ? taskID : "";
    }

    private class MaterializingAsyncConsumer extends CancellableIndexedAsyncConsumer<E> implements
        Task {

      private String taskID;

      private final CancellableIndexedAsyncPredicate<F> elementsPredicate = new CancellableIndexedAsyncPredicate<F>() {
        @Override
        public void cancellableComplete(final int size) {
          elementsMaterializer = null;
          schedule();
        }

        @Override
        public boolean cancellableTest(final int size, final int index, final F element) {
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
        final IteratorAsyncMaterializer<F> materializer = mapper.apply(index, element);
        if (materializer.knownSize() == 0) {
          schedule();
        } else {
          (elementsMaterializer = materializer).materializeNextWhile(elementsPredicate);
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
        final IteratorAsyncMaterializer<F> elementsMaterializer = ImmaterialState.this.elementsMaterializer;
        return elementsMaterializer != null ? elementsMaterializer.weightNextWhile()
            : wrapped.weightElement();
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
