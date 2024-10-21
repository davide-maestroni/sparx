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

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;

public class SymmetricDiffListFutureMaterializer<E> extends
    ProgressiveListFutureMaterializer<E, E> {

  private static final Logger LOGGER = Logger.getLogger(
      SymmetricDiffListFutureMaterializer.class.getName());

  public SymmetricDiffListFutureMaterializer(@NotNull final ListFutureMaterializer<E> wrapped,
      @NotNull final ListFutureMaterializer<E> elementsMaterializer,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    super(context);
    setState(new ImmaterialState(wrapped, elementsMaterializer, context, cancelException));
  }

  private class ImmaterialState extends ProgressiveListFutureMaterializer<E, E>.ImmaterialState {

    private final ListFutureMaterializer<E> elementsMaterializer;
    private final ListFutureMaterializer<E> wrapped;

    private HashMap<E, Integer> elementsBag;
    private boolean isWrapped = true;
    private int nextIndex;

    public ImmaterialState(@NotNull final ListFutureMaterializer<E> wrapped,
        @NotNull final ListFutureMaterializer<E> elementsMaterializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      super(wrapped, context, cancelException, LOGGER);
      this.wrapped = wrapped;
      this.elementsMaterializer = elementsMaterializer;
    }

    @Override
    public void materializeCancel(@NotNull final CancellationException exception) {
      elementsMaterializer.materializeCancel(exception);
      super.materializeCancel(exception);
    }

    @Override
    public int weightElements() {
      if (needsMaterializing()) {
        if (elementsBag == null) {
          return (int) Math.min(Integer.MAX_VALUE,
              (long) wrapped.weightNextWhile() + elementsMaterializer.weightElements());
        }
        return wrapped.weightNextWhile();
      }
      return 1;
    }

    @Override
    void materializeNext() {
      if (elementsBag == null) {
        elementsMaterializer.materializeElements(new CancellableFutureConsumer<List<E>>() {
          @Override
          public void cancellableAccept(final List<E> elements) {
            final HashMap<E, Integer> bag = elementsBag = new HashMap<E, Integer>();
            for (final E element : elements) {
              final Integer count = bag.get(element);
              if (count == null) {
                bag.put(element, 1);
              } else {
                bag.put(element, count + 1);
              }
            }
            materializeUntilConsumed();
          }

          @Override
          public void error(@NotNull final Exception error) {
            setError(error);
          }
        });
      } else {
        materializeUntilConsumed();
      }
    }

    private void materializeUntilConsumed() {
      if (isWrapped) {
        wrapped.materializeNextWhile(nextIndex, new CancellableIndexedFuturePredicate<E>() {
          @Override
          public void cancellableComplete(final int size) {
            isWrapped = false;
            nextIndex = 0;
            materializeUntilConsumed();
          }

          @Override
          public boolean cancellableTest(final int size, final int index, final E element) {
            nextIndex = index + 1;
            final HashMap<E, Integer> elementsBag = ImmaterialState.this.elementsBag;
            final Integer count = elementsBag.get(element);
            if (count == null) {
              return setNextElement(element);
            }
            final int decCount = count - 1;
            if (decCount == 0) {
              elementsBag.remove(element);
            } else {
              elementsBag.put(element, decCount);
            }
            return true;
          }

          @Override
          public void error(@NotNull final Exception error) {
            setError(error);
          }
        });
      } else {
        elementsMaterializer.materializeNextWhile(nextIndex,
            new CancellableIndexedFuturePredicate<E>() {
              @Override
              public void cancellableComplete(final int size) throws Exception {
                setComplete();
              }

              @Override
              public boolean cancellableTest(final int size, final int index, final E element) {
                nextIndex = index + 1;
                final HashMap<E, Integer> elementsBag = ImmaterialState.this.elementsBag;
                final Integer count = elementsBag.get(element);
                boolean next = true;
                if (count != null) {
                  final int decCount = count - 1;
                  if (decCount == 0) {
                    elementsBag.remove(element);
                  } else {
                    elementsBag.put(element, decCount);
                  }
                  next = setNextElement(element);
                }
                return next;
              }

              @Override
              public void error(@NotNull final Exception error) {
                setError(error);
              }
            });
      }
    }
  }
}
