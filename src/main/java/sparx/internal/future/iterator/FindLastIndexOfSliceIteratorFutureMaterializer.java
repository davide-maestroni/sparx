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

import java.util.Collections;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.list.ListFutureMaterializer;
import sparx.util.DequeueList;

public class FindLastIndexOfSliceIteratorFutureMaterializer<E> extends
    ImmediateIteratorFutureMaterializer<E, Integer> {

  private static final Logger LOGGER = Logger.getLogger(
      FindLastIndexOfSliceIteratorFutureMaterializer.class.getName());

  public FindLastIndexOfSliceIteratorFutureMaterializer(
      @NotNull final IteratorFutureMaterializer<E> wrapped,
      @NotNull final ListFutureMaterializer<Object> elementsMaterializer,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    super(context);
    setState(new ImmaterialState(wrapped, elementsMaterializer, cancelException));
  }

  @Override
  public boolean isMaterializedAtOnce() {
    return true;
  }

  private class ImmaterialState extends
      ImmediateIteratorFutureMaterializer<E, Integer>.ImmaterialState {

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
    public boolean isMaterializedAtOnce() {
      return true;
    }

    @Override
    public int weightElements() {
      return isMaterializing() ? 1 : wrapped.weightNext();
    }

    @Override
    void materialize() {
      new MaterializingFutureConsumer().run();
    }

    private @NotNull String getTaskID() {
      final String taskID = context.currentTaskID();
      return taskID != null ? taskID : "";
    }

    private class MaterializingFutureConsumer extends CancellableIndexedFutureConsumer<E> {

      private final DequeueList<E> buffer = new DequeueList<E>();
      private int elementsIndex;
      private int index;
      private int lastIndex = -1;
      private String taskID;

      private final CancellableIndexedFuturePredicate<Object> elementsPredicate = new CancellableIndexedFuturePredicate<Object>() {
        @Override
        public void cancellableComplete(final int size) {
          lastIndex = index++;
          elementsIndex = 0;
          buffer.removeFirst();
          schedule();
        }

        @Override
        public boolean cancellableTest(final int size, final int index, final Object element) {
          final E buffered = buffer.get(index);
          if (element == null ? buffered == null : element.equals(buffered)) {
            if (buffer.size() > index + 1) {
              return true;
            }
            elementsIndex = index + 1;
            MaterializingFutureConsumer.this.run();
          } else {
            ++MaterializingFutureConsumer.this.index;
            elementsIndex = 0;
            buffer.removeFirst();
            schedule();
          }
          return false;
        }

        @Override
        public void error(@NotNull final Exception error) {
          setError(error);
        }
      };

      @Override
      public void cancellableAccept(final int size, final int index, final E element) {
        buffer.add(element);
        schedule();
      }

      @Override
      public void cancellableComplete(final int size) {
        elementsMaterializer.materializeHasElement(elementsIndex,
            new CancellableFutureConsumer<Boolean>() {
              @Override
              public void cancellableAccept(final Boolean hasElement) {
                if (!hasElement) {
                  lastIndex = index;
                }
                if (lastIndex >= 0) {
                  setDone(new ElementToIteratorFutureMaterializer<Integer>(lastIndex));
                  consumeElements(Collections.singletonList(lastIndex));
                } else {
                  setDone(EmptyIteratorFutureMaterializer.<Integer>instance());
                  consumeElements(Collections.<Integer>emptyList());
                }
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
      protected void runWithContext() {
        if (elementsIndex >= buffer.size()) {
          wrapped.materializeNext(this);
        } else {
          elementsMaterializer.materializeNextWhile(elementsIndex, elementsPredicate);
        }
      }

      private void schedule() {
        taskID = getTaskID();
        context.scheduleAfter(this);
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
}
