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

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;

public class DiffIteratorFutureMaterializer<E> extends ProgressiveIteratorFutureMaterializer<E, E> {

  private static final Logger LOGGER = Logger.getLogger(
      DiffIteratorFutureMaterializer.class.getName());

  public DiffIteratorFutureMaterializer(@NotNull final IteratorFutureMaterializer<E> wrapped,
      @NotNull final IteratorFutureMaterializer<?> elementsMaterializer,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    super(context, new AtomicInteger(STATUS_RUNNING));
    setState(new ImmaterialState(wrapped, elementsMaterializer, context, cancelException));
  }

  private class ImmaterialState extends
      ProgressiveIteratorFutureMaterializer<E, E>.ImmaterialState {

    private final IteratorFutureMaterializer<?> elementsMaterializer;

    private HashMap<Object, Integer> elementsBag;

    public ImmaterialState(@NotNull final IteratorFutureMaterializer<E> wrapped,
        @NotNull final IteratorFutureMaterializer<?> elementsMaterializer,
        @NotNull final ExecutionContext context,
        @NotNull final AtomicReference<CancellationException> cancelException) {
      super(wrapped, context, cancelException, LOGGER);
      this.elementsMaterializer = elementsMaterializer;
    }

    @Override
    boolean addElement(final E element) {
      final HashMap<Object, Integer> elementsBag = ImmaterialState.this.elementsBag;
      final Integer count = elementsBag.get(element);
      if (count == null) {
        return true;
      }
      final int decCount = count - 1;
      if (decCount == 0) {
        elementsBag.remove(element);
      } else {
        elementsBag.put(element, decCount);
      }
      return false;
    }

    @Override
    E mapElement(final E element) {
      return element;
    }

    @SuppressWarnings("unchecked")
    void materializeUntilConsumed() {
      if (elementsBag == null) {
        ((IteratorFutureMaterializer<Object>) elementsMaterializer).materializeElements(
            new CancellableFutureConsumer<List<Object>>() {
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
                setNextError(error);
              }
            });
      } else {
        super.materializeUntilConsumed();
      }
    }

    @Override
    int weightUntilConsumed() {
      if (elementsBag == null) {
        return (int) Math.min(Integer.MAX_VALUE,
            (long) elementsMaterializer.weightElements() + super.weightUntilConsumed());
      }
      return super.weightUntilConsumed();
    }
  }
}