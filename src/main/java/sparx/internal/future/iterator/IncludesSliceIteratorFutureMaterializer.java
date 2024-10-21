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

import java.util.Collections;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.FutureConsumer;
import sparx.internal.future.IndexedFutureConsumer;
import sparx.internal.future.list.ListFutureMaterializer;
import sparx.util.DequeueList;
import sparx.util.annotation.Positive;

public class IncludesSliceIteratorFutureMaterializer<E> extends
    ImmediateIteratorFutureMaterializer<E, Boolean> {

  private static final Logger LOGGER = Logger.getLogger(
      IncludesSliceIteratorFutureMaterializer.class.getName());

  public IncludesSliceIteratorFutureMaterializer(
      @NotNull final IteratorFutureMaterializer<E> wrapped,
      @NotNull final ListFutureMaterializer<Object> elementsMaterializer,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    super(context);
    setState(new ImmaterialState(wrapped, elementsMaterializer, cancelException));
  }

  @Override
  public int knownSize() {
    return 1;
  }

  private class ImmaterialState extends
      ImmediateIteratorFutureMaterializer<E, Boolean>.ImmaterialState {

    private final AtomicReference<CancellationException> cancelException;
    private final ListFutureMaterializer<Object> elementsMaterializer;
    private final IteratorFutureMaterializer<E> wrapped;

    public ImmaterialState(@NotNull final IteratorFutureMaterializer<E> wrapped,
        @NotNull final ListFutureMaterializer<Object> elementsMaterializer,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      super(wrapped, LOGGER);
      this.wrapped = wrapped;
      this.elementsMaterializer = elementsMaterializer;
      this.cancelException = cancelException;
    }

    @Override
    public int knownSize() {
      return 1;
    }

    @Override
    public void materializeSkip(@Positive final int count,
        @NotNull final FutureConsumer<Integer> consumer) {
      if (!isMaterializing()) {
        setDone(EmptyIteratorFutureMaterializer.<Boolean>instance());
        consumeElements(Collections.<Boolean>emptyList());
        safeConsume(consumer, 1, LOGGER);
      } else {
        super.materializeSkip(count, consumer);
      }
    }

    @Override
    public int weightElements() {
      return isMaterializing() ? 1 : wrapped.weightSkip();
    }

    @Override
    public int weightSkip() {
      return isMaterializing() ? weightElements() : 1;
    }

    @Override
    void materialize() {
      new MaterializingFutureConsumer().run();
    }

    private @NotNull String getTaskID() {
      final String taskID = context.currentTaskID();
      return taskID != null ? taskID : "";
    }

    private class MaterializingFutureConsumer extends
        CancellableMultiFutureConsumer<Boolean, Object> {

      private final DequeueList<Object> buffer = new DequeueList<Object>();

      private int elementsIndex;
      private boolean isWrapped = true;
      private String taskID;

      @Override
      public void cancellableAccept(final Boolean hasNext) {
        final boolean includes = !hasNext;
        setDone(new ElementToIteratorFutureMaterializer<Boolean>(includes));
        consumeElements(Collections.singletonList(includes));
      }

      @Override
      public void cancellableAccept(final int size, final int index, final Object element) {
        if (isWrapped) {
          buffer.add(element);
          isWrapped = false;
          schedule();
        } else {
          final Object wrappedElement = buffer.get(elementsIndex++);
          if (element == wrappedElement || (element != null && element.equals(wrappedElement))) {
            isWrapped = buffer.size() <= elementsIndex;
          } else {
            buffer.removeFirst();
            elementsIndex = 0;
            isWrapped = buffer.isEmpty();
          }
          schedule();
        }
      }

      @Override
      public void cancellableComplete(final int size) {
        if (isWrapped) {
          elementsMaterializer.materializeHasElement(elementsIndex, this);
        } else {
          setDone(new ElementToIteratorFutureMaterializer<Boolean>(true));
          consumeElements(Collections.singletonList(true));
        }
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

      @Override
      public @NotNull String taskID() {
        return taskID;
      }

      @Override
      public int weight() {
        return elementsIndex >= buffer.size() ? wrapped.weightNext()
            : elementsMaterializer.weightNextWhile();
      }

      @Override
      @SuppressWarnings("unchecked")
      protected void runWithContext() {
        if (isWrapped) {
          wrapped.materializeNext((IndexedFutureConsumer<E>) this);
        } else {
          elementsMaterializer.materializeElement(elementsIndex, this);
        }
      }

      private void schedule() {
        taskID = getTaskID();
        context.scheduleAfter(this);
      }
    }
  }
}
