/*
 * Copyright 2026 Davide Maestroni
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
package sparx1.internal.lazy.iterator;

import sparx1.internal.lazy.IteratorMaterializer;
import sparx1.util.UncheckedException;
import sparx1.util.annotation.NotNull;
import sparx1.util.annotation.Positive;
import sparx1.util.function.Supplier;

public class SuppliedIteratorMaterializer<E> extends StatefulIteratorMaterializer<E> {

  public SuppliedIteratorMaterializer(
      final @NotNull Supplier<IteratorMaterializer<E>> materializerSupplier) {
    setState(new InitialState(materializerSupplier));
  }

  private class InitialState implements IteratorMaterializer<E> {

    private final Supplier<IteratorMaterializer<E>> materializerSupplier;

    private InitialState(final @NotNull Supplier<IteratorMaterializer<E>> materializerSupplier) {
      this.materializerSupplier = materializerSupplier;
    }

    @Override
    public int currentKnownSize() {
      return -1;
    }

    @Override
    public boolean materializeHasNext() {
      try {
        return setState(materializerSupplier.get()).materializeHasNext();
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public E materializeNext() {
      try {
        return setState(materializerSupplier.get()).materializeNext();
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public int materializeSkip(final @Positive int count) {
      try {
        return setState(materializerSupplier.get()).materializeSkip(count);
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }
  }
}
