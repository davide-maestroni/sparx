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

import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import sparx.util.LiveIterator;

public class EmptyLiveIterator<E> implements LiveIterator<E> {

  private static final EmptyLiveIterator<?> INSTANCE = new EmptyLiveIterator<Object>();

  @SuppressWarnings("unchecked")
  public static @NotNull <E> EmptyLiveIterator<E> instance() {
    return (EmptyLiveIterator<E>) INSTANCE;
  }

  private EmptyLiveIterator() {
  }

  @Override
  public boolean hasNext() {
    return false;
  }

  @Override
  public boolean hasNext(final long timeout, @NotNull final TimeUnit unit) {
    return false;
  }

  @Override
  public E next() {
    throw new NoSuchElementException();
  }

  @Override
  public E next(final long timeout, @NotNull final TimeUnit unit) {
    throw new NoSuchElementException();
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("remove");
  }
}
