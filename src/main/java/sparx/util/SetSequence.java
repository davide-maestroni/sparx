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
package sparx.util;

import java.util.Collection;
import java.util.Comparator;
import org.jetbrains.annotations.NotNull;
import sparx.util.function.BinaryFunction;
import sparx.util.function.Consumer;
import sparx.util.function.Function;
import sparx.util.function.Predicate;

public interface SetSequence<E> extends Collection<E>, Sequence<E> {

  @Override
  @NotNull SetSequence<Boolean> all(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull SetSequence<Boolean> exists(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull <F> SetSequence<F> as();

  @Override
  @NotNull SetSequence<Integer> count();

  @Override
  @NotNull SetSequence<Integer> count(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull SetSequence<Integer> countNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull SetSequence<E> drop(int maxElements);

  @Override
  @NotNull SetSequence<E> dropUntil(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull SetSequence<E> dropWhile(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull SetSequence<E> dropRight(int maxElements);

  @Override
  @NotNull SetSequence<E> dropRightUntil(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull SetSequence<E> dropRightWhile(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull SetSequence<Boolean> endsWith(@NotNull Iterable<?> elements);

  @Override
  @NotNull SetSequence<E> filter(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull SetSequence<E> filterNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull SetSequence<E> findAny(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull SetSequence<E> findAnyNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull SetSequence<E> findFirst(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull SetSequence<E> findFirstNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull SetSequence<Integer> findIndexOf(Object element);

  @Override
  @NotNull SetSequence<Integer> findIndexWhere(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull SetSequence<Integer> findIndexOfSlice(@NotNull Iterable<?> elements);

  @Override
  @NotNull SetSequence<E> findLast(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull SetSequence<Integer> findLastIndexOf(Object element);

  @Override
  @NotNull SetSequence<Integer> findLastIndexWhere(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull SetSequence<Integer> findLastIndexOfSlice(@NotNull Iterable<?> elements);

  @Override
  @NotNull SetSequence<E> findLastNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull <F> SetSequence<F> flatMap(
      @NotNull Function<? super E, ? extends Iterable<F>> mapper);

  @Override
  @NotNull <F> SetSequence<F> foldLeft(F identity,
      @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

  @Override
  @NotNull <F> SetSequence<F> foldRight(F identity,
      @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

  @Override
  @NotNull SetSequence<? extends SetSequence<E>> group(int maxSize);

  @Override
  @NotNull SetSequence<? extends SetSequence<E>> group(int size, E filler);

  @Override
  @NotNull SetSequence<Boolean> includes(Object element);

  @Override
  @NotNull SetSequence<Boolean> includesAll(@NotNull Iterable<?> elements);

  @Override
  @NotNull SetSequence<Boolean> includesSlice(@NotNull Iterable<?> elements);

  @Override
  @NotNull <F> SetSequence<F> map(@NotNull Function<? super E, F> mapper);

  @Override
  @NotNull SetSequence<E> mapExceptionally(
      @NotNull Function<? super Throwable, ? extends E> mapper);

  @Override
  @NotNull SetSequence<E> max(@NotNull Comparator<? super E> comparator);

  @Override
  @NotNull SetSequence<E> min(@NotNull Comparator<? super E> comparator);

  @Override
  @NotNull SetSequence<Boolean> nonExists(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull SetSequence<Boolean> notAll(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull SetSequence<E> peek(@NotNull Consumer<? super E> consumer);

  @Override
  @NotNull SetSequence<E> plus(E element);

  @Override
  @NotNull SetSequence<E> plusAll(@NotNull Iterable<E> elements);

  @Override
  @NotNull <F extends E> SetSequence<F> reduceLeft(
      @NotNull BinaryFunction<? super E, ? super E, F> operation);

  @Override
  @NotNull <F extends E> SetSequence<F> reduceRight(
      @NotNull BinaryFunction<? super E, ? super E, F> operation);

  @Override
  @NotNull SetSequence<E> removeAt(int index);

  @Override
  @NotNull SetSequence<E> removeEach(E element);

  @Override
  @NotNull SetSequence<E> removeFirst(E element);

  @Override
  @NotNull SetSequence<E> removeLast(E element);

  @Override
  @NotNull SetSequence<E> removeSegment(int from, int maxSize);

  @Override
  @NotNull SetSequence<E> removeSlice(int from, int until);

  @Override
  @NotNull SetSequence<E> replaceAt(int index, E element);

  @Override
  @NotNull SetSequence<E> replaceEach(E element, E replacement);

  @Override
  @NotNull SetSequence<E> replaceFirst(E current, E replacement);

  @Override
  @NotNull SetSequence<E> replaceLast(E current, E replacement);

  @Override
  @NotNull SetSequence<E> replaceSegment(int from, @NotNull Iterable<? extends E> patch,
      int maxSize);

  @Override
  @NotNull SetSequence<E> replaceSlice(int from, @NotNull Iterable<? extends E> patch,
      int until);

  @Override
  @NotNull SetSequence<E> slice(int from, int until);

  @Override
  @NotNull SetSequence<Boolean> startsWith(@NotNull Iterable<?> elements);

  @Override
  @NotNull <F extends Number> SetSequence<F> sum(@NotNull Function<? super E, F> mapper);

  @Override
  @NotNull SetSequence<E> take(int maxElements);

  @Override
  @NotNull SetSequence<E> takeUntil(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull SetSequence<E> takeWhile(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull SetSequence<E> takeRight(int maxElements);

  @Override
  @NotNull SetSequence<E> takeRightUntil(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull SetSequence<E> takeRightWhile(@NotNull Predicate<? super E> predicate);
}