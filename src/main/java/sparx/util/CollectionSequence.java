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

public interface CollectionSequence<E> extends Collection<E>, Sequence<E> {

  @Override
  @NotNull CollectionSequence<Boolean> all(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull <F> CollectionSequence<F> as();

  @Override
  @NotNull CollectionSequence<Integer> count();

  @Override
  @NotNull CollectionSequence<Integer> count(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull CollectionSequence<Integer> countNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull CollectionSequence<E> drop(int maxElements);

  @Override
  @NotNull CollectionSequence<E> dropUntil(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull CollectionSequence<E> dropWhile(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull CollectionSequence<E> dropRight(int maxElements);

  @Override
  @NotNull CollectionSequence<E> dropRightUntil(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull CollectionSequence<E> dropRightWhile(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull CollectionSequence<Boolean> endsWith(@NotNull Iterable<?> elements);

  @Override
  @NotNull CollectionSequence<Boolean> exists(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull CollectionSequence<E> filter(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull CollectionSequence<E> filterNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull CollectionSequence<E> findAny(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull CollectionSequence<E> findAnyNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull CollectionSequence<E> findFirst(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull CollectionSequence<E> findFirstNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull CollectionSequence<Integer> findIndexOf(Object element);

  @Override
  @NotNull CollectionSequence<Integer> findIndexWhere(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull CollectionSequence<Integer> findIndexOfSlice(@NotNull Iterable<?> elements);

  @Override
  @NotNull CollectionSequence<E> findLast(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull CollectionSequence<Integer> findLastIndexOf(Object element);

  @Override
  @NotNull CollectionSequence<Integer> findLastIndexWhere(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull CollectionSequence<Integer> findLastIndexOfSlice(@NotNull Iterable<?> elements);

  @Override
  @NotNull CollectionSequence<E> findLastNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull <F> CollectionSequence<F> flatMap(
      @NotNull Function<? super E, ? extends Iterable<F>> mapper);

  @Override
  @NotNull <F> CollectionSequence<F> foldLeft(F identity,
      @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

  @Override
  @NotNull <F> CollectionSequence<F> foldRight(F identity,
      @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

  @Override
  @NotNull CollectionSequence<? extends CollectionSequence<E>> group(int maxSize);

  @Override
  @NotNull CollectionSequence<? extends CollectionSequence<E>> group(int size, E filler);

  @Override
  @NotNull CollectionSequence<Boolean> includes(Object element);

  @Override
  @NotNull CollectionSequence<Boolean> includesAll(@NotNull Iterable<?> elements);

  @Override
  @NotNull CollectionSequence<Boolean> includesSlice(@NotNull Iterable<?> elements);

  @Override
  @NotNull <F> CollectionSequence<F> map(@NotNull Function<? super E, F> mapper);

  @Override
  @NotNull CollectionSequence<E> mapExceptionally(
      @NotNull Function<? super Throwable, ? extends E> mapper);

  @Override
  @NotNull CollectionSequence<E> max(@NotNull Comparator<? super E> comparator);

  @Override
  @NotNull CollectionSequence<E> min(@NotNull Comparator<? super E> comparator);

  @Override
  @NotNull CollectionSequence<Boolean> notAll(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull CollectionSequence<Boolean> nonExists(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull CollectionSequence<E> peek(@NotNull Consumer<? super E> consumer);

  @Override
  @NotNull CollectionSequence<E> plus(E element);

  @Override
  @NotNull CollectionSequence<E> plusAll(@NotNull Iterable<E> elements);

  @Override
  @NotNull <F extends E> CollectionSequence<F> reduceLeft(
      @NotNull BinaryFunction<? super E, ? super E, F> operation);

  @Override
  @NotNull <F extends E> CollectionSequence<F> reduceRight(
      @NotNull BinaryFunction<? super E, ? super E, F> operation);

  @Override
  @NotNull CollectionSequence<E> removeAt(int index);

  @Override
  @NotNull CollectionSequence<E> removeEach(E element);

  @Override
  @NotNull CollectionSequence<E> removeFirst(E element);

  @Override
  @NotNull CollectionSequence<E> removeLast(E element);

  @Override
  @NotNull CollectionSequence<E> removeSegment(int from, int maxSize);

  @Override
  @NotNull CollectionSequence<E> removeSlice(int from, int until);

  @Override
  @NotNull CollectionSequence<E> replaceAt(int index, E element);

  @Override
  @NotNull CollectionSequence<E> replaceEach(E element, E replacement);

  @Override
  @NotNull CollectionSequence<E> replaceFirst(E current, E replacement);

  @Override
  @NotNull CollectionSequence<E> replaceLast(E current, E replacement);

  @Override
  @NotNull CollectionSequence<E> replaceSegment(int from, @NotNull Iterable<? extends E> patch,
      int maxSize);

  @Override
  @NotNull CollectionSequence<E> replaceSlice(int from, @NotNull Iterable<? extends E> patch,
      int until);

  @Override
  @NotNull CollectionSequence<E> slice(int from, int until);

  @Override
  @NotNull CollectionSequence<Boolean> startsWith(@NotNull Iterable<?> elements);

  @Override
  @NotNull <F extends Number> CollectionSequence<F> sum(@NotNull Function<? super E, F> mapper);

  @Override
  @NotNull CollectionSequence<E> take(int maxElements);

  @Override
  @NotNull CollectionSequence<E> takeUntil(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull CollectionSequence<E> takeWhile(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull CollectionSequence<E> takeRight(int maxElements);

  @Override
  @NotNull CollectionSequence<E> takeRightUntil(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull CollectionSequence<E> takeRightWhile(@NotNull Predicate<? super E> predicate);
}
