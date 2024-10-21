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

import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;
import sparx.concurrent.ExecutionContext;

public class WrappingIteratorFutureMaterializer<E> extends AbstractIteratorFutureMaterializer<E> {

  private final boolean isMaterializedAtOnce;
  private final int knownSize;

  public WrappingIteratorFutureMaterializer(@NotNull final IteratorFutureMaterializer<E> wrapped,
      @NotNull final ExecutionContext context,
      @NotNull final AtomicReference<CancellationException> cancelException) {
    super(context);
    isMaterializedAtOnce = wrapped.isMaterializedAtOnce();
    knownSize = wrapped.knownSize();
    setState(new WrappingState(wrapped, cancelException));
  }

  @Override
  public boolean isMaterializedAtOnce() {
    return isMaterializedAtOnce;
  }

  @Override
  public int knownSize() {
    return knownSize;
  }
}
