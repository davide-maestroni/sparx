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
package sparx.concurrent;

import java.util.Collection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sparx.function.Function;

public abstract class ReadOnlyStreamScopeFuture<V, F extends SignalFuture<V>> extends
    StreamScopeFuture<V, F> {

  private static <R> R fail(@NotNull final String name) {
    throw new UnsupportedOperationException(name);
  }

  @Override
  public void clear() {
    fail("clear");
  }

  @Override
  public void close() {
    fail("close");
  }

  @Override
  public void compute(@NotNull final Function<? super V, ? extends V> function) {
    fail("compute");
  }

  @Override
  public boolean fail(@NotNull final Exception error) {
    return fail("fail");
  }

  @Override
  public boolean isReadOnly() {
    return true;
  }

  @Override
  public void set(final V value) {
    fail("set");
  }

  @Override
  public void setBulk(@NotNull final Collection<V> values) {
    fail("setBulk");
  }

  @Override
  public void setBulk(@Nullable final V... values) {
    fail("setBulk");
  }
}
