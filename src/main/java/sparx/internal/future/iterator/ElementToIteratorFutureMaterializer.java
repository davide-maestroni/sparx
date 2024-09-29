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
import static sparx.internal.future.FutureConsumers.safeConsumeComplete;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CancellationException;
import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import sparx.internal.future.FutureConsumer;
import sparx.internal.future.IndexedFutureConsumer;
import sparx.internal.future.IndexedFuturePredicate;
import sparx.util.annotation.Positive;

public class ElementToIteratorFutureMaterializer<E> implements IteratorFutureMaterializer<E> {

  private static final Logger LOGGER = Logger.getLogger(
      ElementToIteratorFutureMaterializer.class.getName());

  private final E element;

  private boolean consumed;

  public ElementToIteratorFutureMaterializer(final E element) {
    this.element = element;
  }

  @Override
  public boolean isCancelled() {
    return false;
  }

  @Override
  public boolean isDone() {
    return true;
  }

  @Override
  public boolean isFailed() {
    return false;
  }

  @Override
  public boolean isMaterializedAtOnce() {
    return true;
  }

  @Override
  public boolean isSucceeded() {
    return true;
  }

  @Override
  public int knownSize() {
    return 1;
  }

  @Override
  public void materializeCancel(@NotNull final CancellationException exception) {
  }

  @Override
  public void materializeElements(@NotNull final FutureConsumer<List<E>> consumer) {
    if (consumed) {
      safeConsume(consumer, Collections.<E>emptyList(), LOGGER);
    } else {
      consumed = true;
      safeConsume(consumer, Collections.singletonList(element), LOGGER);
    }
  }

  @Override
  public void materializeHasNext(@NotNull final FutureConsumer<Boolean> consumer) {
    safeConsume(consumer, !consumed, LOGGER);
  }

  @Override
  public void materializeIterator(@NotNull final FutureConsumer<Iterator<E>> consumer) {
    safeConsume(consumer, new Iterator<E>() {
      @Override
      public boolean hasNext() {
        return !consumed;
      }

      @Override
      public E next() {
        if (consumed) {
          throw new NoSuchElementException();
        }
        consumed = true;
        return element;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("remove");
      }
    }, LOGGER);
  }

  @Override
  public void materializeNext(@NotNull final IndexedFutureConsumer<E> consumer) {
    if (consumed) {
      safeConsumeComplete(consumer, 0, LOGGER);
    } else {
      consumed = true;
      safeConsume(consumer, 1, 0, element, LOGGER);
    }
  }

  @Override
  public void materializeNextWhile(@NotNull final IndexedFuturePredicate<E> predicate) {
    if (!consumed) {
      consumed = true;
      if (!safeConsume(predicate, 1, 0, element, LOGGER)) {
        return;
      }
    }
    safeConsumeComplete(predicate, 0, LOGGER);
  }

  @Override
  public void materializeSkip(@Positive final int count,
      @NotNull final FutureConsumer<Integer> consumer) {
    if (consumed) {
      safeConsume(consumer, 0, LOGGER);
    } else {
      consumed = true;
      safeConsume(consumer, 1, LOGGER);
    }
  }

  @Override
  public int weightElements() {
    return 1;
  }

  @Override
  public int weightHasNext() {
    return 1;
  }

  @Override
  public int weightNext() {
    return 1;
  }

  @Override
  public int weightNextWhile() {
    return consumed ? 1 : 2;
  }

  @Override
  public int weightSkip() {
    return 1;
  }
}
