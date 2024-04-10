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
import sparx.util.function.Supplier;

public interface ListSequence<E> extends CollectionSequence<E>, List<E> {

  @Override
  @NotNull
  ListSequence<Boolean> all(@NotNull Predicate<? super E> predicate);

  @NotNull
  ListSequence<E> append(E element);

  @NotNull
  ListSequence<E> appendAll(@NotNull Iterable<? extends E> elements);

  @Override
  @NotNull
  <F> ListSequence<F> as();

  @Override
  @NotNull
  ListSequence<Integer> count();

  @Override
  @NotNull
  ListSequence<Integer> count(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  ListSequence<Integer> countNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  ListSequence<E> drop(int maxElements);

  @Override
  @NotNull
  ListSequence<E> dropRight(int maxElements);

  @Override
  @NotNull
  ListSequence<E> dropRightWhile(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  ListSequence<E> dropRightWhileNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  ListSequence<E> dropWhile(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  ListSequence<E> dropWhileNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  ListSequence<Boolean> endsWith(@NotNull Iterable<?> elements);

  // TODO: enumerate()

  @Override
  @NotNull
  ListSequence<Boolean> exists(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  ListSequence<E> filter(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  ListSequence<E> filterNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  ListSequence<E> findAny(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  ListSequence<E> findAnyNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  ListSequence<E> findFirst(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  ListSequence<E> findFirstNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  ListSequence<Integer> findIndexOf(Object element);

  @Override
  @NotNull
  ListSequence<Integer> findIndexWhere(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  ListSequence<Integer> findIndexWhereNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  ListSequence<Integer> findIndexOfSlice(@NotNull Iterable<?> elements);

  @Override
  @NotNull
  ListSequence<E> findLast(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  ListSequence<Integer> findLastIndexOf(Object element);

  @Override
  @NotNull
  ListSequence<Integer> findLastIndexWhere(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  ListSequence<Integer> findLastIndexWhereNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  ListSequence<Integer> findLastIndexOfSlice(@NotNull Iterable<?> elements);

  @Override
  @NotNull
  ListSequence<E> findLastNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  <F> ListSequence<F> flatMap(@NotNull Function<? super E, ? extends Iterable<F>> mapper);

  @Override
  @NotNull
  ListSequence<E> flatMapAfter(int numElements,
      @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

  @Override
  @NotNull
  ListSequence<E> flatMapFirstWhere(@NotNull Predicate<? super E> predicate,
      @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

  @Override
  @NotNull
  ListSequence<E> flatMapFirstWhereNot(@NotNull Predicate<? super E> predicate,
      @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

  @Override
  @NotNull
  ListSequence<E> flatMapLastWhere(@NotNull Predicate<? super E> predicate,
      @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

  @Override
  @NotNull
  ListSequence<E> flatMapLastWhereNot(@NotNull Predicate<? super E> predicate,
      @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

  @Override
  @NotNull
  ListSequence<E> flatMapWhere(@NotNull Predicate<? super E> predicate,
      @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

  @Override
  @NotNull
  ListSequence<E> flatMapWhereNot(@NotNull Predicate<? super E> predicate,
      @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

  @Override
  @NotNull
  <F> ListSequence<F> fold(F identity,
      @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

  @Override
  @NotNull
  <F> ListSequence<F> foldLeft(F identity,
      @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

  @Override
  @NotNull
  <F> ListSequence<F> foldRight(F identity,
      @NotNull BinaryFunction<? super E, ? super F, ? extends F> operation);

  @Override
  @NotNull
  ListSequence<? extends ListSequence<E>> group(int maxSize);

  @Override
  @NotNull
  ListSequence<? extends ListSequence<E>> group(int size, E filler);

  @Override
  @NotNull
  ListSequence<Boolean> includes(Object element);

  @Override
  @NotNull
  ListSequence<Boolean> includesAll(@NotNull Iterable<?> elements);

  @Override
  @NotNull
  ListSequence<Boolean> includesSlice(@NotNull Iterable<?> elements);

  @NotNull
  ListSequence<E> insertAfter(int numElements, E element);

  @NotNull
  ListSequence<E> insertAllAfter(int numElements, @NotNull Iterable<? extends E> elements);

  @Override
  @NotNull
  <F> ListSequence<F> map(@NotNull Function<? super E, F> mapper);

  @Override
  @NotNull
  ListSequence<E> mapAfter(int numElements, @NotNull Function<? super E, ? extends E> mapper);

  @Override
  @NotNull
  ListSequence<E> mapFirstWhere(@NotNull Predicate<? super E> predicate,
      @NotNull Function<? super E, ? extends E> mapper);

  @Override
  @NotNull
  ListSequence<E> mapFirstWhereNot(@NotNull Predicate<? super E> predicate,
      @NotNull Function<? super E, ? extends E> mapper);

  @Override
  @NotNull
  ListSequence<E> mapLastWhere(@NotNull Predicate<? super E> predicate,
      @NotNull Function<? super E, ? extends E> mapper);

  @Override
  @NotNull
  ListSequence<E> mapLastWhereNot(@NotNull Predicate<? super E> predicate,
      @NotNull Function<? super E, ? extends E> mapper);

  @Override
  @NotNull
  ListSequence<E> mapWhere(@NotNull Predicate<? super E> predicate,
      @NotNull Function<? super E, ? extends E> mapper);

  @Override
  @NotNull
  ListSequence<E> mapWhereNot(@NotNull Predicate<? super E> predicate,
      @NotNull Function<? super E, ? extends E> mapper);

  @Override
  @NotNull
  ListSequence<E> max(@NotNull Comparator<? super E> comparator);

  @Override
  @NotNull
  ListSequence<E> min(@NotNull Comparator<? super E> comparator);

  @Override
  @NotNull
  ListSequence<Boolean> notAll(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  ListSequence<Boolean> notExists(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  ListSequence<E> orElse(@NotNull Iterable<E> elements);

  @Override
  @NotNull
  ListSequence<E> orElseGet(@NotNull Supplier<? extends Iterable<? extends E>> supplier);

  // TODO: padTo(size, element), padLeftTo(size, element), padRightTo(size, element)

  @Override
  @NotNull
  ListSequence<E> peek(@NotNull Consumer<? super E> consumer);

  @Override
  @NotNull
  ListSequence<E> plus(E element);

  @Override
  @NotNull
  ListSequence<E> plusAll(@NotNull Iterable<E> elements);

  @NotNull
  ListSequence<E> prepend(E element);

  @NotNull
  ListSequence<E> prependAll(@NotNull Iterable<? extends E> elements);

  @Override
  @NotNull
  ListSequence<E> reduce(@NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

  @Override
  @NotNull
  ListSequence<E> reduceLeft(@NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

  @Override
  @NotNull
  ListSequence<E> reduceRight(@NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

  @Override
  @NotNull
  ListSequence<E> removeAfter(int numElements);

  @Override
  @NotNull
  ListSequence<E> removeEach(E element);

  @Override
  @NotNull
  ListSequence<E> removeFirst(E element);

  @Override
  @NotNull
  ListSequence<E> removeFirstWhere(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  ListSequence<E> removeFirstWhereNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  ListSequence<E> removeLast(E element);

  @Override
  @NotNull
  ListSequence<E> removeLastWhere(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  ListSequence<E> removeLastWhereNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  ListSequence<E> removeSlice(int start, int end);

  @Override
  @NotNull
  ListSequence<E> removeWhere(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  ListSequence<E> removeWhereNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  ListSequence<E> replaceAfter(int numElements, E replacement);

  @Override
  @NotNull
  ListSequence<E> replaceEach(E element, E replacement);

  @Override
  @NotNull
  ListSequence<E> replaceFirst(E element, E replacement);

  @Override
  @NotNull
  ListSequence<E> replaceFirstWhere(@NotNull Predicate<? super E> predicate,
      E replacement);

  @Override
  @NotNull
  ListSequence<E> replaceFirstWhereNot(@NotNull Predicate<? super E> predicate, E replacement);

  @Override
  @NotNull
  ListSequence<E> replaceLast(E element, E replacement);

  @Override
  @NotNull
  ListSequence<E> replaceLastWhere(@NotNull Predicate<? super E> predicate, E replacement);

  @Override
  @NotNull
  ListSequence<E> replaceLastWhereNot(@NotNull Predicate<? super E> predicate, E replacement);

  @Override
  @NotNull
  ListSequence<E> replaceSlice(int start, int end, @NotNull Iterable<? extends E> patch);

  @Override
  @NotNull
  ListSequence<E> replaceWhere(@NotNull Predicate<? super E> predicate, E replacement);

  @NotNull
  ListSequence<E> reverse();

  @Override
  @NotNull
  ListSequence<E> slice(int start, int end);

  @Override
  @NotNull
  ListSequence<Boolean> startsWith(@NotNull Iterable<?> elements);

  @NotNull
  ListSequence<E> sorted(@NotNull Comparator<? super E> comparator);

  @Override
  @NotNull
  ListSequence<E> take(int maxElements);

  @Override
  @NotNull
  ListSequence<E> takeRight(int maxElements);

  @Override
  @NotNull
  ListSequence<E> takeRightWhile(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  ListSequence<E> takeRightWhileNot(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  ListSequence<E> takeWhile(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  ListSequence<E> takeWhileNot(@NotNull Predicate<? super E> predicate);
}
