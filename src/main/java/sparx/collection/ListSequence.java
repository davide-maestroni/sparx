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
package sparx.collection;

import java.util.Comparator;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import sparx.util.function.BinaryFunction;
import sparx.util.function.Consumer;
import sparx.util.function.Function;
import sparx.util.function.Predicate;

public interface ListSequence<E> extends CollectionSequence<E>, List<E> {

  @Override
  @NotNull ListSequence<Boolean> all(@NotNull Predicate<? super E> predicate);

  @NotNull ListSequence<E> append(E element);

  @NotNull ListSequence<E> appendAll(@NotNull Iterable<E> elements);

  @Override
  @NotNull <F> ListSequence<F> as();

  @Override
  @NotNull ListSequence<Integer> count();

  @Override
  @NotNull ListSequence<Integer> count(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull ListSequence<Integer> countNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull ListSequence<E> drop(int maxElements);

  @Override
  @NotNull ListSequence<E> dropUntil(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull ListSequence<E> dropWhile(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull ListSequence<E> dropRight(int maxElements);

  @Override
  @NotNull ListSequence<E> dropRightUntil(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull ListSequence<E> dropRightWhile(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull ListSequence<Boolean> endsWith(@NotNull Iterable<?> elements);

  @Override
  @NotNull ListSequence<Boolean> exists(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull ListSequence<E> filter(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull ListSequence<E> filterNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull ListSequence<E> findAny(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull ListSequence<E> findAnyNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull ListSequence<E> findFirst(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull ListSequence<E> findFirstNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull ListSequence<Integer> findIndexOf(Object element);

  @Override
  @NotNull ListSequence<Integer> findIndexOf(int minIndex, Object element);

  @Override
  @NotNull ListSequence<Integer> findIndexWhere(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull ListSequence<Integer> findIndexWhere(int minIndex,
      @NotNull Predicate<? super E> predicate);

  @Override
  @NotNull ListSequence<Integer> findIndexWhereNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull ListSequence<Integer> findIndexWhereNot(int minIndex,
      @NotNull Predicate<? super E> predicate);

  @Override
  @NotNull ListSequence<Integer> findIndexOfSlice(@NotNull Iterable<?> elements);

  @Override
  @NotNull ListSequence<Integer> findIndexOfSlice(int minIndex, @NotNull Iterable<?> elements);

  @Override
  @NotNull ListSequence<E> findLast(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull ListSequence<Integer> findLastIndexOf(Object element);

  @Override
  @NotNull ListSequence<Integer> findLastIndexOf(int maxIndex, Object element);

  @Override
  @NotNull ListSequence<Integer> findLastIndexWhere(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull ListSequence<Integer> findLastIndexWhere(int maxIndex,
      @NotNull Predicate<? super E> predicate);

  @Override
  @NotNull ListSequence<Integer> findLastIndexWhereNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull ListSequence<Integer> findLastIndexWhereNot(int maxIndex,
      @NotNull Predicate<? super E> predicate);

  @Override
  @NotNull ListSequence<Integer> findLastIndexOfSlice(@NotNull Iterable<?> elements);

  @Override
  @NotNull ListSequence<Integer> findLastIndexOfSlice(int maxIndex, @NotNull Iterable<?> elements);

  @Override
  @NotNull ListSequence<E> findLastNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull <F> ListSequence<F> flatMap(@NotNull Function<? super E, ? extends Iterable<F>> mapper);

  @Override
  @NotNull <F> ListSequence<F> foldLeft(F identity,
      @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

  @Override
  @NotNull <F> ListSequence<F> foldRight(F identity,
      @NotNull BinaryFunction<? super E, ? super F, ? extends F> operation);

  @Override
  @NotNull ListSequence<? extends ListSequence<E>> group(int maxSize);

  @Override
  @NotNull ListSequence<? extends ListSequence<E>> group(int size, E filler);

  @Override
  @NotNull ListSequence<Boolean> includes(Object element);

  @Override
  @NotNull ListSequence<Boolean> includesAll(@NotNull Iterable<?> elements);

  @Override
  @NotNull ListSequence<Boolean> includesSlice(@NotNull Iterable<?> elements);

  @NotNull ListSequence<E> insertAllAt(int index, @NotNull Iterable<? extends E> elements);

  @NotNull ListSequence<E> insertAt(int index, E element);

  @Override
  @NotNull <F> ListSequence<F> map(@NotNull Function<? super E, F> mapper);

  @Override
  @NotNull ListSequence<E> max(@NotNull Comparator<? super E> comparator);

  @Override
  @NotNull ListSequence<E> min(@NotNull Comparator<? super E> comparator);

  @Override
  @NotNull ListSequence<Boolean> notAll(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull ListSequence<Boolean> notExists(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull ListSequence<E> peek(@NotNull Consumer<? super E> consumer);

  @Override
  @NotNull ListSequence<E> plus(E element);

  @Override
  @NotNull ListSequence<E> plusAll(@NotNull Iterable<E> elements);

  @NotNull ListSequence<E> prepend(E element);

  @NotNull ListSequence<E> prependAll(@NotNull Iterable<E> elements);

  @Override
  @NotNull ListSequence<E> reduceLeft(
      @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

  @Override
  @NotNull ListSequence<E> reduceRight(
      @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

  @Override
  @NotNull ListSequence<E> removeAt(int index);

  @Override
  @NotNull ListSequence<E> removeEach(E element);

  @Override
  @NotNull ListSequence<E> removeFirst(E element);

  @Override
  @NotNull ListSequence<E> removeFirstWhere(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull ListSequence<E> removeFirstWhere(int minIndex, @NotNull Predicate<? super E> predicate);

  @Override
  @NotNull ListSequence<E> removeFirstWhereNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull ListSequence<E> removeFirstWhereNot(int minIndex,
      @NotNull Predicate<? super E> predicate);

  @Override
  @NotNull ListSequence<E> removeLast(E element);

  @Override
  @NotNull ListSequence<E> removeLastWhere(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull ListSequence<E> removeLastWhereNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull ListSequence<E> removeSegment(int start, int maxSize);

  @Override
  @NotNull ListSequence<E> removeSlice(int start, int end);

  @Override
  @NotNull ListSequence<E> removeWhere(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull ListSequence<E> removeWhereNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull ListSequence<E> replaceAt(int index, E element);

  @Override
  @NotNull ListSequence<E> replaceEach(E element, E replacement);

  @Override
  @NotNull ListSequence<E> replaceFirst(E element, E replacement);

  @Override
  @NotNull ListSequence<E> replaceLast(E element, E replacement);

  @Override
  @NotNull ListSequence<E> replaceSegment(int start, @NotNull Iterable<? extends E> patch,
      int maxSize);

  @Override
  @NotNull ListSequence<E> replaceSlice(int start, @NotNull Iterable<? extends E> patch, int end);

  @NotNull ListSequence<E> reverse();

  @Override
  @NotNull ListSequence<E> slice(int from, int until);

  @Override
  @NotNull ListSequence<Boolean> startsWith(@NotNull Iterable<?> elements);

  @NotNull ListSequence<E> sorted(@NotNull Comparator<? super E> comparator);

  @Override
  @NotNull ListSequence<E> take(int maxElements);

  @Override
  @NotNull ListSequence<E> takeUntil(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull ListSequence<E> takeWhile(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull ListSequence<E> takeRight(int maxElements);

  @Override
  @NotNull ListSequence<E> takeRightUntil(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull ListSequence<E> takeRightWhile(@NotNull Predicate<? super E> predicate);
}
