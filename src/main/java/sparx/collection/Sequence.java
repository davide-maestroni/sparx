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
import org.jetbrains.annotations.NotNull;
import sparx.util.function.BinaryFunction;
import sparx.util.function.Consumer;
import sparx.util.function.Function;
import sparx.util.function.Predicate;
import sparx.util.function.Supplier;

public interface Sequence<E> extends Iterable<E> {

  @NotNull Sequence<Boolean> all(@NotNull Predicate<? super E> predicate);

  <T> T apply(@NotNull Function<? super Sequence<E>, T> mapper); // TODO: cannot inherit!!!

  @NotNull <F> Sequence<F> as();

  @NotNull Sequence<Integer> count();

  @NotNull Sequence<Integer> count(@NotNull Predicate<? super E> predicate);

  @NotNull Sequence<Integer> countNot(@NotNull Predicate<? super E> predicate);

  void doFor(@NotNull Consumer<? super E> consumer);

  void doUntil(@NotNull Predicate<? super E> condition, @NotNull Consumer<? super E> consumer);

  void doUntil(@NotNull Predicate<? super E> predicate);

  void doWhile(@NotNull Predicate<? super E> condition, @NotNull Consumer<? super E> consumer);

  void doWhile(@NotNull Predicate<? super E> predicate);

  @NotNull Sequence<E> drop(int maxElements);

  @NotNull Sequence<E> dropRight(int maxElements);

  @NotNull Sequence<E> dropRightUntil(@NotNull Predicate<? super E> predicate);

  @NotNull Sequence<E> dropRightWhile(@NotNull Predicate<? super E> predicate);

  @NotNull Sequence<E> dropUntil(@NotNull Predicate<? super E> predicate);

  @NotNull Sequence<E> dropWhile(@NotNull Predicate<? super E> predicate);

  @NotNull Sequence<Boolean> endsWith(@NotNull Iterable<?> elements);

  @NotNull Sequence<Boolean> exists(@NotNull Predicate<? super E> predicate);

  @NotNull Sequence<E> filter(@NotNull Predicate<? super E> predicate);

  @NotNull Sequence<E> filterNot(@NotNull Predicate<? super E> predicate);

  @NotNull Sequence<E> findAny(@NotNull Predicate<? super E> predicate);

  @NotNull Sequence<E> findAnyNot(@NotNull Predicate<? super E> predicate);

  @NotNull Sequence<E> findFirst(@NotNull Predicate<? super E> predicate);

  @NotNull Sequence<E> findFirst(int minIndex, @NotNull Predicate<? super E> predicate);

  @NotNull Sequence<E> findFirstNot(@NotNull Predicate<? super E> predicate);

  @NotNull Sequence<E> findFirstNot(int minIndex, @NotNull Predicate<? super E> predicate);

  @NotNull Sequence<Integer> findIndexOf(Object element);

  @NotNull Sequence<Integer> findIndexOf(int minIndex, Object element);

  @NotNull Sequence<Integer> findIndexWhere(@NotNull Predicate<? super E> predicate);

  @NotNull Sequence<Integer> findIndexWhere(int minIndex, @NotNull Predicate<? super E> predicate);

  @NotNull Sequence<Integer> findIndexWhereNot(@NotNull Predicate<? super E> predicate);

  @NotNull Sequence<Integer> findIndexWhereNot(int minIndex,
      @NotNull Predicate<? super E> predicate);

  @NotNull Sequence<Integer> findIndexOfSlice(@NotNull Iterable<?> elements);

  @NotNull Sequence<Integer> findIndexOfSlice(int minIndex, @NotNull Iterable<?> elements);

  @NotNull Sequence<E> findLast(@NotNull Predicate<? super E> predicate);

  @NotNull Sequence<E> findLast(int maxIndex, @NotNull Predicate<? super E> predicate);

  @NotNull Sequence<Integer> findLastIndexOf(Object element);

  @NotNull Sequence<Integer> findLastIndexOf(int maxIndex, Object element);

  @NotNull Sequence<Integer> findLastIndexWhere(@NotNull Predicate<? super E> predicate);

  @NotNull Sequence<Integer> findLastIndexWhere(int maxIndex,
      @NotNull Predicate<? super E> predicate);

  @NotNull Sequence<Integer> findLastIndexWhereNot(@NotNull Predicate<? super E> predicate);

  @NotNull Sequence<Integer> findLastIndexWhereNot(int maxIndex,
      @NotNull Predicate<? super E> predicate);

  @NotNull Sequence<Integer> findLastIndexOfSlice(@NotNull Iterable<?> elements);

  @NotNull Sequence<Integer> findLastIndexOfSlice(int maxIndex, @NotNull Iterable<?> elements);

  @NotNull Sequence<E> findLastNot(@NotNull Predicate<? super E> predicate);

  @NotNull Sequence<E> findLastNot(int maxIndex, @NotNull Predicate<? super E> predicate);

  E first();

  @NotNull <F> Sequence<F> flatMap(@NotNull Function<? super E, ? extends Iterable<F>> mapper);

  @NotNull Sequence<E> flatMapAt(int index,
      @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

  @NotNull <F> Sequence<F> fold(F identity,
      @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

  @NotNull <F> Sequence<F> foldLeft(F identity,
      @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

  @NotNull <F> Sequence<F> foldRight(F identity,
      @NotNull BinaryFunction<? super E, ? super F, ? extends F> operation);

  @NotNull Sequence<? extends Sequence<E>> group(int maxSize);

  @NotNull Sequence<? extends Sequence<E>> group(int size, E filler);

  @NotNull Sequence<Boolean> includes(Object element);

  @NotNull Sequence<Boolean> includesAll(@NotNull Iterable<?> elements);

  @NotNull Sequence<Boolean> includesSlice(@NotNull Iterable<?> elements);

  boolean isEmpty();

  E last();

  @NotNull <F> Sequence<F> map(@NotNull Function<? super E, F> mapper);

  @NotNull Sequence<E> max(@NotNull Comparator<? super E> comparator);

  @NotNull Sequence<E> min(@NotNull Comparator<? super E> comparator);

  @NotNull Sequence<Boolean> notAll(@NotNull Predicate<? super E> predicate);

  boolean notEmpty();

  @NotNull Sequence<Boolean> notExists(@NotNull Predicate<? super E> predicate);

  @NotNull Sequence<E> orElse(@NotNull Iterable<E> elements);

  @NotNull Sequence<E> orElseGet(@NotNull Supplier<? extends Iterable<? extends E>> supplier);

  @NotNull Sequence<E> peek(@NotNull Consumer<? super E> consumer);

  @NotNull Sequence<E> plus(E element);

  @NotNull Sequence<E> plusAll(@NotNull Iterable<E> elements);

  @NotNull Sequence<E> reduce(
      @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

  @NotNull Sequence<E> reduceLeft(
      @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

  @NotNull Sequence<E> reduceRight(
      @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

  @NotNull Sequence<E> removeAt(int index);

  @NotNull Sequence<E> removeEach(E element);

  @NotNull Sequence<E> removeFirst(E element);

  @NotNull Sequence<E> removeFirst(int minIndex, E element);

  @NotNull Sequence<E> removeFirstWhere(@NotNull Predicate<? super E> predicate);

  @NotNull Sequence<E> removeFirstWhere(int minIndex, @NotNull Predicate<? super E> predicate);

  @NotNull Sequence<E> removeFirstWhereNot(@NotNull Predicate<? super E> predicate);

  @NotNull Sequence<E> removeFirstWhereNot(int minIndex, @NotNull Predicate<? super E> predicate);

  @NotNull Sequence<E> removeLast(E element);

  @NotNull Sequence<E> removeLast(int maxIndex, E element);

  @NotNull Sequence<E> removeLastWhere(@NotNull Predicate<? super E> predicate);

  @NotNull Sequence<E> removeLastWhere(int maxIndex, @NotNull Predicate<? super E> predicate);

  @NotNull Sequence<E> removeLastWhereNot(@NotNull Predicate<? super E> predicate);

  @NotNull Sequence<E> removeLastWhereNot(int maxIndex, @NotNull Predicate<? super E> predicate);

  @NotNull Sequence<E> removeSegment(int start, int maxSize);

  @NotNull Sequence<E> removeSlice(int start, int end);

  @NotNull Sequence<E> removeWhere(@NotNull Predicate<? super E> predicate);

  @NotNull Sequence<E> removeWhereNot(@NotNull Predicate<? super E> predicate);

  @NotNull Sequence<E> replaceAt(int index, E element);

  @NotNull Sequence<E> replaceEach(E element, E replacement);

  @NotNull Sequence<E> replaceFirst(E element, E replacement);

  @NotNull Sequence<E> replaceFirst(int minIndex, E element, E replacement);

  @NotNull Sequence<E> replaceLast(E element, E replacement);

  @NotNull Sequence<E> replaceLast(int maxIndex, E element, E replacement);

  @NotNull Sequence<E> replaceSegment(int start, @NotNull Iterable<? extends E> patch, int maxSize);

  @NotNull Sequence<E> replaceSlice(int start, @NotNull Iterable<? extends E> patch, int end);

  int size();

  @NotNull Sequence<E> slice(int from, int until);

  @NotNull Sequence<Boolean> startsWith(@NotNull Iterable<?> elements);

  @NotNull Sequence<E> take(int maxElements);

  @NotNull Sequence<E> takeUntil(@NotNull Predicate<? super E> predicate);

  @NotNull Sequence<E> takeWhile(@NotNull Predicate<? super E> predicate);

  @NotNull Sequence<E> takeRight(int maxElements);

  @NotNull Sequence<E> takeRightUntil(@NotNull Predicate<? super E> predicate);

  @NotNull Sequence<E> takeRightWhile(@NotNull Predicate<? super E> predicate);

  // TODO: hasMemory, isSorted, etc.

  // TODO: diff? intersect? union? (removeAll? retainAll?)
  // TODO: sliding?? split??

  // TODO: toArray, groupBy, toString(StringBuilder/StringJoiner), collect
  // TODO: zip, merge, combine

  // TODO: combinations
}
