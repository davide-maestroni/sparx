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
import org.jetbrains.annotations.NotNull;

public class CancelledIteratorAsyncMaterializer<E> extends FailedIteratorAsyncMaterializer<E> {

  public CancelledIteratorAsyncMaterializer(@NotNull final CancellationException exception) {
    super(exception);
  }

  @Override
  public boolean isCancelled() {
    return true;
  }

  @Override
  public boolean isFailed() {
    return false;
  }
}
