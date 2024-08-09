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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;
import sparx.util.function.Function;

public class IntersectListAsyncMaterializer<E> extends ProgressiveListAsyncMaterializer<E, E> {

  private static final Logger LOGGER = Logger.getLogger(
      IntersectListAsyncMaterializer.class.getName());

  public IntersectListAsyncMaterializer(@NotNull final ListAsyncMaterializer<E> wrapped,
      @NotNull final ListAsyncMaterializer<?> elementsMaterializer,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException,
      @NotNull final Function<List<E>, List<E>> decorateFunction) {
    super(new AtomicInteger(STATUS_RUNNING));
    setState(new ImmaterialState(wrapped, elementsMaterializer, context, cancelException,
        decorateFunction));
  }

  private class ImmaterialState extends ProgressiveListAsyncMaterializer<E, E>.ImmaterialState {

    private final ListAsyncMaterializer<?> elementsMaterializer;
    private final ListAsyncMaterializer<E> wrapped;

    private HashMap<Object, Integer> elementsBag;
    private int nextIndex;

    public ImmaterialState(@NotNull final ListAsyncMaterializer<E> wrapped,
        @NotNull final ListAsyncMaterializer<?> elementsMaterializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException,
        @NotNull final Function<List<E>, List<E>> decorateFunction) {
      super(wrapped, context, cancelException, decorateFunction, LOGGER);
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
    @SuppressWarnings("unchecked")
    void materializeNext() {
      if (elementsBag == null) {
        ((ListAsyncMaterializer<Object>) elementsMaterializer).materializeElements(
            new CancellableAsyncConsumer<List<Object>>() {
              @Override
              public void cancellableAccept(final List<Object> elements) {
                final HashMap<Object, Integer> bag = elementsBag = new HashMap<Object, Integer>();
                for (final Object element : elements) {
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
      wrapped.materializeNextWhile(nextIndex, new CancellableIndexedAsyncPredicate<E>() {
        @Override
        public void cancellableComplete(final int size) throws Exception {
          setComplete();
        }

        @Override
        public boolean cancellableTest(final int size, final int index, final E element) {
          nextIndex = index + 1;
          final HashMap<Object, Integer> elementsBag = ImmaterialState.this.elementsBag;
          final Integer count = elementsBag.get(element);
          if (count != null) {
            final int decCount = count - 1;
            if (decCount == 0) {
              elementsBag.remove(element);
            } else {
              elementsBag.put(element, decCount);
            }
            return setNextElement(element);
          }
          return true;
        }

        @Override
        public void error(@NotNull final Exception error) {
          setError(error);
        }
      });
    }
  }
}
