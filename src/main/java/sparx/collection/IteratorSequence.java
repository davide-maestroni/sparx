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
import java.util.Iterator;
import org.jetbrains.annotations.NotNull;
import sparx.util.function.Action;
import sparx.util.function.BinaryFunction;
import sparx.util.function.Consumer;
import sparx.util.function.Function;
import sparx.util.function.Predicate;

public interface IteratorSequence<E> extends Sequence<E>, Iterator<E> {

  @Override
  @NotNull IteratorSequence<Boolean> all(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull IteratorSequence<Boolean> exists(@NotNull Predicate<? super E> predicate);

  @NotNull IteratorSequence<E> append(E element);

  @NotNull IteratorSequence<E> appendAll(@NotNull Iterable<E> elements);

  @Override
  @NotNull <F> IteratorSequence<F> as();

  @Override
  @NotNull IteratorSequence<Integer> count();

  @Override
  @NotNull IteratorSequence<Integer> count(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull IteratorSequence<Integer> countNot(@NotNull Predicate<? super E> predicate);

  @NotNull IteratorSequence<E> doFinally(@NotNull Action action);

  @Override
  @NotNull IteratorSequence<E> drop(int maxElements);

  @Override
  @NotNull IteratorSequence<E> dropUntil(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull IteratorSequence<E> dropWhile(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull IteratorSequence<E> dropRight(int maxElements);

  @Override
  @NotNull IteratorSequence<E> dropRightUntil(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull IteratorSequence<E> dropRightWhile(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull IteratorSequence<Boolean> endsWith(@NotNull Iterable<?> elements);

  @Override
  @NotNull IteratorSequence<E> filter(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull IteratorSequence<E> filterNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull IteratorSequence<E> findAny(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull IteratorSequence<E> findAnyNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull IteratorSequence<E> findFirst(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull IteratorSequence<E> findFirstNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull IteratorSequence<Integer> findIndexOf(Object element);

  @Override
  @NotNull IteratorSequence<Integer> findIndexWhere(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull IteratorSequence<Integer> findIndexWhereNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull IteratorSequence<Integer> findIndexOfSlice(@NotNull Iterable<?> elements);

  @Override
  @NotNull IteratorSequence<E> findLast(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull IteratorSequence<Integer> findLastIndexOf(Object element);

  @Override
  @NotNull IteratorSequence<Integer> findLastIndexWhere(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull IteratorSequence<Integer> findLastIndexWhereNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull IteratorSequence<Integer> findLastIndexOfSlice(@NotNull Iterable<?> elements);

  @Override
  @NotNull IteratorSequence<E> findLastNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull <F> IteratorSequence<F> flatMap(
      @NotNull Function<? super E, ? extends Iterable<F>> mapper);

  @Override
  @NotNull IteratorSequence<E> flatMapAfter(int numElements,
      @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

  @NotNull <T extends Throwable> IteratorSequence<E> flatMapExceptionally(
      @NotNull Class<T> exceptionType,
      @NotNull Function<? super T, ? extends Iterable<? extends E>> mapper);

  @NotNull IteratorSequence<E> flatMapExceptionally(
      @NotNull Function<? super Throwable, ? extends Iterable<? extends E>> mapper);

  @Override
  @NotNull IteratorSequence<E> flatMapWhere(@NotNull Predicate<? super E> predicate,
      @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

  @Override
  @NotNull IteratorSequence<E> flatMapWhereNot(@NotNull Predicate<? super E> predicate,
      @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

  @Override
  @NotNull IteratorSequence<E> flatMapFirstWhere(@NotNull Predicate<? super E> predicate,
      @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

  @Override
  @NotNull IteratorSequence<E> flatMapFirstWhereNot(@NotNull Predicate<? super E> predicate,
      @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

  @Override
  @NotNull IteratorSequence<E> flatMapLastWhere(@NotNull Predicate<? super E> predicate,
      @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

  @Override
  @NotNull IteratorSequence<E> flatMapLastWhereNot(@NotNull Predicate<? super E> predicate,
      @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

  @Override
  @NotNull <F> IteratorSequence<F> fold(F identity,
      @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

  @Override
  @NotNull <F> IteratorSequence<F> foldLeft(F identity,
      @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

  @Override
  @NotNull <F> IteratorSequence<F> foldRight(F identity,
      @NotNull BinaryFunction<? super E, ? super F, ? extends F> operation);

  @Override
  @NotNull IteratorSequence<? extends IteratorSequence<E>> group(int maxSize);

  @Override
  @NotNull IteratorSequence<? extends IteratorSequence<E>> group(int size, E filler);

  @Override
  @NotNull IteratorSequence<Boolean> includes(Object element);

  @Override
  @NotNull IteratorSequence<Boolean> includesAll(@NotNull Iterable<?> elements);

  @Override
  @NotNull IteratorSequence<Boolean> includesSlice(@NotNull Iterable<?> elements);

  @NotNull IteratorSequence<E> insert(E element);

  @NotNull IteratorSequence<E> insertAll(@NotNull Iterable<E> elements);

  @NotNull IteratorSequence<E> insertAllAt(int index, @NotNull Iterable<? extends E> patch);

  @NotNull IteratorSequence<E> insertAt(int index, E element);

  @Override
  @NotNull <F> IteratorSequence<F> map(@NotNull Function<? super E, F> mapper);

  @Override
  @NotNull IteratorSequence<E> mapAfter(int numElements,
      @NotNull Function<? super E, ? extends E> mapper);

  @Override
  @NotNull IteratorSequence<E> mapFirstWhere(@NotNull Predicate<? super E> predicate,
      @NotNull Function<? super E, ? extends E> mapper);

  @Override
  @NotNull IteratorSequence<E> mapFirstWhereNot(@NotNull Predicate<? super E> predicate,
      @NotNull Function<? super E, ? extends E> mapper);

  @Override
  @NotNull IteratorSequence<E> mapLastWhere(@NotNull Predicate<? super E> predicate,
      @NotNull Function<? super E, ? extends E> mapper);

  @Override
  @NotNull IteratorSequence<E> mapLastWhereNot(@NotNull Predicate<? super E> predicate,
      @NotNull Function<? super E, ? extends E> mapper);

  @Override
  @NotNull IteratorSequence<E> mapWhere(@NotNull Predicate<? super E> predicate,
      @NotNull Function<? super E, ? extends E> mapper);

  @Override
  @NotNull IteratorSequence<E> mapWhereNot(@NotNull Predicate<? super E> predicate,
      @NotNull Function<? super E, ? extends E> mapper);

  @NotNull <T extends Throwable> IteratorSequence<E> mapExceptionally(
      @NotNull Class<T> exceptionType, @NotNull Function<? super T, E> mapper);

  @NotNull IteratorSequence<E> mapExceptionally(@NotNull Function<? super Throwable, E> mapper);

  @Override
  @NotNull IteratorSequence<E> max(@NotNull Comparator<? super E> comparator);

  @Override
  @NotNull IteratorSequence<E> min(@NotNull Comparator<? super E> comparator);

  @Override
  @NotNull IteratorSequence<Boolean> notExists(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull IteratorSequence<Boolean> notAll(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull IteratorSequence<E> peek(@NotNull Consumer<? super E> consumer);

  @Override
  @NotNull IteratorSequence<E> plus(E element);

  @Override
  @NotNull IteratorSequence<E> plusAll(@NotNull Iterable<E> elements);

  @Override
  @NotNull IteratorSequence<E> reduce(
      @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

  @Override
  @NotNull IteratorSequence<E> reduceLeft(
      @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

  @Override
  @NotNull IteratorSequence<E> reduceRight(
      @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

  @Override
  @NotNull IteratorSequence<E> removeAfter(int numElements);

  @Override
  @NotNull IteratorSequence<E> removeEach(E element);

  @Override
  @NotNull IteratorSequence<E> removeFirst(E element);

  @Override
  @NotNull IteratorSequence<E> removeFirstWhere(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull IteratorSequence<E> removeFirstWhereNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull IteratorSequence<E> removeLast(E element); // TODO: ???

  @Override
  @NotNull IteratorSequence<E> removeLastWhere(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull IteratorSequence<E> removeLastWhereNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull IteratorSequence<E> removeSegment(int start, int maxSize);

  @Override
  @NotNull IteratorSequence<E> removeSlice(int start, int end);

  @Override
  @NotNull IteratorSequence<E> removeWhere(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull IteratorSequence<E> removeWhereNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull IteratorSequence<E> replaceAfter(int numElements, E replacement);

  @Override
  @NotNull IteratorSequence<E> replaceEach(E element, E replacement);

  @Override
  @NotNull IteratorSequence<E> replaceFirst(E element, E replacement);

  @Override
  @NotNull IteratorSequence<E> replaceLast(E element, E replacement); // TODO: ???

  @Override
  @NotNull IteratorSequence<E> replaceSegment(int start, @NotNull Iterable<? extends E> patch,
      int maxSize);

  @Override
  @NotNull IteratorSequence<E> replaceSlice(int start, @NotNull Iterable<? extends E> patch,
      int end);

  @Override
  @NotNull IteratorSequence<E> slice(int from, int until);

  @Override
  @NotNull IteratorSequence<Boolean> startsWith(@NotNull Iterable<?> elements);

  @Override
  @NotNull IteratorSequence<E> take(int maxElements);

  @Override
  @NotNull IteratorSequence<E> takeUntil(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull IteratorSequence<E> takeWhile(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull IteratorSequence<E> takeRight(int maxElements);

  @Override
  @NotNull IteratorSequence<E> takeRightUntil(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull IteratorSequence<E> takeRightWhile(@NotNull Predicate<? super E> predicate);
}