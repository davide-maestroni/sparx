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
  @NotNull IteratorSequence<Integer> findIndexOfSlice(@NotNull Iterable<?> elements);

  @Override
  @NotNull IteratorSequence<E> findLast(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull IteratorSequence<Integer> findLastIndexOf(Object element);

  @Override
  @NotNull IteratorSequence<Integer> findLastIndexWhere(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull IteratorSequence<Integer> findLastIndexOfSlice(@NotNull Iterable<?> elements);

  @Override
  @NotNull IteratorSequence<E> findLastNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull <F> IteratorSequence<F> flatMap(
      @NotNull Function<? super E, ? extends Iterable<F>> mapper);

  @Override
  @NotNull <F> IteratorSequence<F> foldLeft(F identity,
      @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

  @Override
  @NotNull <F> IteratorSequence<F> foldRight(F identity,
      @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

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
  @NotNull IteratorSequence<E> mapExceptionally(
      @NotNull Function<? super Throwable, ? extends E> mapper);

  @Override
  @NotNull IteratorSequence<E> max(@NotNull Comparator<? super E> comparator);

  @Override
  @NotNull IteratorSequence<E> min(@NotNull Comparator<? super E> comparator);

  @Override
  @NotNull IteratorSequence<Boolean> nonExists(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull IteratorSequence<Boolean> notAll(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull IteratorSequence<E> peek(@NotNull Consumer<? super E> consumer);

  @Override
  @NotNull IteratorSequence<E> plus(E element);

  @Override
  @NotNull IteratorSequence<E> plusAll(@NotNull Iterable<E> elements);

  @Override
  @NotNull <F extends E> IteratorSequence<F> reduceLeft(
      @NotNull BinaryFunction<? super E, ? super E, F> operation);

  @Override
  @NotNull <F extends E> IteratorSequence<F> reduceRight(
      @NotNull BinaryFunction<? super E, ? super E, F> operation);

  @Override
  @NotNull IteratorSequence<E> removeAt(int index);

  @Override
  @NotNull IteratorSequence<E> removeEach(E element);

  @Override
  @NotNull IteratorSequence<E> removeFirst(E element);

  @Override
  @NotNull IteratorSequence<E> removeLast(E element);

  @Override
  @NotNull IteratorSequence<E> removeSegment(int from, int maxSize);

  @Override
  @NotNull IteratorSequence<E> removeSlice(int from, int until);

  @Override
  @NotNull IteratorSequence<E> replaceAt(int index, E element);

  @Override
  @NotNull IteratorSequence<E> replaceEach(E element, E replacement);

  @Override
  @NotNull IteratorSequence<E> replaceFirst(E current, E replacement);

  @Override
  @NotNull IteratorSequence<E> replaceLast(E current, E replacement);

  @Override
  @NotNull IteratorSequence<E> replaceSegment(int from, @NotNull Iterable<? extends E> patch,
      int maxSize);

  @Override
  @NotNull IteratorSequence<E> replaceSlice(int from, @NotNull Iterable<? extends E> patch,
      int until);

  @Override
  @NotNull IteratorSequence<E> slice(int from, int until);

  @Override
  @NotNull IteratorSequence<Boolean> startsWith(@NotNull Iterable<?> elements);

  @Override
  @NotNull <F extends Number> IteratorSequence<F> sum(@NotNull Function<? super E, F> mapper);

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