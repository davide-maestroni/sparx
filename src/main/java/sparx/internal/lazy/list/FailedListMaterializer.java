package sparx.internal.lazy.list;/*
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

import java.util.Iterator;
import org.jetbrains.annotations.NotNull;
import sparx.util.UncheckedException;

public class FailedListMaterializer<E> implements ListMaterializer<E> {

  private final Exception error;

  public FailedListMaterializer(@NotNull final Exception error) {
    this.error = error;
  }

  @Override
  public boolean canMaterializeElement(final int index) {
    return fail();
  }

  @Override
  public E materializeElement(final int index) {
    return fail();
  }

  @Override
  public int knownSize() {
    return fail();
  }

  @Override
  public boolean materializeContains(final Object element) {
    return fail();
  }

  @Override
  public int materializeElements() {
    return fail();
  }

  @Override
  public boolean materializeEmpty() {
    return fail();
  }

  @Override
  public @NotNull Iterator<E> materializeIterator() {
    return fail();
  }

  @Override
  public int materializeSize() {
    return fail();
  }

  private <T> T fail() {
    throw UncheckedException.throwUnchecked(error);
  }
}
