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
import sparx.function.Function;

public class ReadOnlyFuture<V> extends DecoratedFuture<V> {

  private static <R> R fail() {
    throw new ReadOnlyException();
  }

  public ReadOnlyFuture(@NotNull final StreamingFuture<V> future) {
    super(future);
  }

  @Override
  public void clear() {
    fail();
  }

  @Override
  public void compute(@NotNull final Function<? super V, ? extends V> function) {
    fail();
  }

  @Override
  public @NotNull StreamingFuture<V> readOnly() {
    return this;
  }

  @Override
  public boolean fail(@NotNull final Exception error) {
    return fail();
  }

  @Override
  public boolean isReadOnly() {
    return true;
  }

  @Override
  public void set(final V value) {
    fail();
  }

  @Override
  public void setBulk(@NotNull final Collection<V> values) {
    fail();
  }

  @Override
  public void close() {
    fail();
  }
}
