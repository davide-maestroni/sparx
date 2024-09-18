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
package sparx.internal.lazy.iterator;

import org.jetbrains.annotations.NotNull;
import sparx.util.UncheckedException;
import sparx.util.function.IndexedFunction;
import sparx.util.function.IndexedPredicate;

public class GeneratorToIteratorMaterializer<G, E> extends AutoSkipIteratorMaterializer<E> {

  private final G generator;
  private final IndexedPredicate<? super G> hasNextPredicate;
  private final IndexedFunction<? super G, ? extends E> nextFunction;

  private int pos;

  public GeneratorToIteratorMaterializer(@NotNull final G generator,
      @NotNull final IndexedPredicate<? super G> hasNextPredicate,
      @NotNull final IndexedFunction<? super G, ? extends E> nextFunction) {
    this.generator = generator;
    this.hasNextPredicate = hasNextPredicate;
    this.nextFunction = nextFunction;
  }

  @Override
  public int knownSize() {
    return -1;
  }

  @Override
  public boolean materializeHasNext() {
    try {
      return hasNextPredicate.test(pos, generator);
    } catch (final Exception e) {
      throw UncheckedException.throwUnchecked(e);
    }
  }

  @Override
  public E materializeNext() {
    try {
      return nextFunction.apply(pos++, generator);
    } catch (final Exception e) {
      throw UncheckedException.throwUnchecked(e);
    }
  }
}
