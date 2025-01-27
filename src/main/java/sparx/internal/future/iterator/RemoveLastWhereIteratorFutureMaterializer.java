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
import sparx.concurrent.ExecutionContext;
import sparx.util.DequeArrayList;
import sparx.util.function.BinaryFunction;
import sparx.util.function.IndexedPredicate;

public class RemoveLastWhereIteratorFutureMaterializer<E> extends
    AbstractIteratorFutureMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      RemoveLastWhereIteratorFutureMaterializer.class.getName());

  public RemoveLastWhereIteratorFutureMaterializer(
      @NotNull final IteratorFutureMaterializer<E> wrapped,
      @NotNull final IndexedPredicate<? super E> predicate, @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final BinaryFunction<List<E>, List<E>, List<E>> prependFunction) {
    super(context);
    setState(new ImmaterialState(wrapped, predicate, context, cancelException, prependFunction));
  }

  @Override
  public int knownSize() {
    return -1;
  }

  private class ImmaterialState extends ProgressiveIteratorFutureMaterializerState<E, E> {

    private final AtomicReference<CancellationException> cancelException;
    private final ExecutionContext context;
    private final DequeArrayList<E> cachedElements = new DequeArrayList<E>();
    private final IndexedPredicate<? super E> predicate;
    private final BinaryFunction<List<E>, List<E>, List<E>> prependFunction;
    private final IteratorFutureMaterializer<E> wrapped;

    private IteratorFutureMaterializer<E> elementsMaterializer;
    private int wrappedIndex;

    public ImmaterialState(@NotNull final IteratorFutureMaterializer<E> wrapped,
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final BinaryFunction<List<E>, List<E>, List<E>> prependFunction) {
      super(RemoveLastWhereIteratorFutureMaterializer.this, wrapped, context, cancelException,
          LOGGER);
      this.wrapped = wrapped;
      this.predicate = predicate;
      this.context = context;
      this.cancelException = cancelException;
      this.prependFunction = prependFunction;
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
        final DequeArrayList<E> cachedElements = this.cachedElements;
        wrapped.materializeNextWhile(new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) {
            if (!cachedElements.isEmpty()) {
              cachedElements.removeFirst();
              elementsMaterializer = new InsertAllIteratorFutureMaterializer<E>(
                  new DequeToIteratorFutureMaterializer<E>(cachedElements, context), wrapped,
                  context, cancelException, prependFunction, currentIndex());
              materializeUntilConsumed();
            } else {
              setComplete();
            }
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element)
              throws Exception {
            final int wrappedIndex = ImmaterialState.this.wrappedIndex++;
            if (predicate.test(wrappedIndex, element)) {
              if (cachedElements.isEmpty()) {
                cachedElements.add(element);
                return true;
              }
              final boolean next = setNextElements(cachedElements);
              cachedElements.clear();
              cachedElements.add(element);
              return next;
            } else if (!cachedElements.isEmpty()) {
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

    @Override
    int weightNextElements() {
      return elementsMaterializer != null ? elementsMaterializer.weightNextWhile()
          : wrapped.weightNextWhile();
    }
  }
}
