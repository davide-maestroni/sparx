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

import java.util.Collection;
import java.util.Comparator;
import org.jetbrains.annotations.NotNull;
import sparx.util.function.BinaryFunction;
import sparx.util.function.Function;
import sparx.util.function.IndexedFunction;
import sparx.util.function.IndexedPredicate;
import sparx.util.function.Predicate;
import sparx.util.function.Supplier;

public interface SetSequence<E> extends Collection<E>, Sequence<E> {

  @Override
  @NotNull
  SetSequence<Boolean> all(@NotNull IndexedPredicate<? super E> predicate);

  @Override
  @NotNull
  SetSequence<Boolean> all(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  <F> SetSequence<F> as();

  @Override
  @NotNull
  SetSequence<Integer> count();

  @Override
  @NotNull
  SetSequence<Integer> count(@NotNull IndexedPredicate<? super E> predicate);

  @Override
  @NotNull
  SetSequence<Integer> count(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  SetSequence<E> diff(@NotNull Iterable<?> elements);

  @Override
  @NotNull
  SetSequence<E> drop(int maxElements);

  @Override
  @NotNull
  SetSequence<E> dropRight(int maxElements);

  @Override
  @NotNull
  SetSequence<E> dropRightWhile(@NotNull IndexedPredicate<? super E> predicate);

  @Override
  @NotNull
  SetSequence<E> dropRightWhile(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  SetSequence<E> dropWhile(@NotNull IndexedPredicate<? super E> predicate);

  @Override
  @NotNull
  SetSequence<E> dropWhile(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  SetSequence<Boolean> endsWith(@NotNull Iterable<?> elements);

  @Override
  @NotNull
  SetSequence<Boolean> exists(@NotNull IndexedPredicate<? super E> predicate);

  @Override
  @NotNull
  SetSequence<Boolean> exists(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  SetSequence<E> filter(@NotNull IndexedPredicate<? super E> predicate);

  @Override
  @NotNull
  SetSequence<E> filter(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  SetSequence<E> findAny(@NotNull IndexedPredicate<? super E> predicate);

  @Override
  @NotNull
  SetSequence<E> findAny(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  SetSequence<E> findFirst(@NotNull IndexedPredicate<? super E> predicate);

  @Override
  @NotNull
  SetSequence<E> findFirst(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  SetSequence<Integer> findIndexOf(Object element);

  @Override
  @NotNull
  SetSequence<Integer> findIndexWhere(@NotNull IndexedPredicate<? super E> predicate);

  @Override
  @NotNull
  SetSequence<Integer> findIndexWhere(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  SetSequence<Integer> findIndexOfSlice(@NotNull Iterable<?> elements);

  @Override
  @NotNull
  SetSequence<E> findLast(@NotNull IndexedPredicate<? super E> predicate);

  @Override
  @NotNull
  SetSequence<E> findLast(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  SetSequence<Integer> findLastIndexOf(Object element);

  @Override
  @NotNull
  SetSequence<Integer> findLastIndexWhere(@NotNull IndexedPredicate<? super E> predicate);

  @Override
  @NotNull
  SetSequence<Integer> findLastIndexWhere(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  SetSequence<Integer> findLastIndexOfSlice(@NotNull Iterable<?> elements);

  @Override
  @NotNull
  <F> SetSequence<F> flatMap(@NotNull Function<? super E, ? extends Iterable<F>> mapper);

  @Override
  @NotNull
  <F> SetSequence<F> flatMap(@NotNull IndexedFunction<? super E, ? extends Iterable<F>> mapper);

  @Override
  @NotNull
  SetSequence<E> flatMapAfter(int numElements,
      @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

  @Override
  @NotNull
  SetSequence<E> flatMapAfter(int numElements,
      @NotNull IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper);

  @Override
  @NotNull
  SetSequence<E> flatMapFirstWhere(@NotNull IndexedPredicate<? super E> predicate,
      @NotNull IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper);

  @Override
  @NotNull
  SetSequence<E> flatMapFirstWhere(@NotNull Predicate<? super E> predicate,
      @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

  @Override
  @NotNull
  SetSequence<E> flatMapLastWhere(@NotNull IndexedPredicate<? super E> predicate,
      @NotNull IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper);

  @Override
  @NotNull
  SetSequence<E> flatMapLastWhere(@NotNull Predicate<? super E> predicate,
      @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

  @Override
  @NotNull
  SetSequence<E> flatMapWhere(@NotNull IndexedPredicate<? super E> predicate,
      @NotNull IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper);

  @Override
  @NotNull
  SetSequence<E> flatMapWhere(@NotNull Predicate<? super E> predicate,
      @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

  @Override
  @NotNull
  <F> SetSequence<F> fold(F identity,
      @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

  @Override
  @NotNull
  <F> SetSequence<F> foldLeft(F identity,
      @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

  @Override
  @NotNull
  <F> SetSequence<F> foldRight(F identity,
      @NotNull BinaryFunction<? super E, ? super F, ? extends F> operation);

  @Override
  @NotNull
  SetSequence<? extends SetSequence<E>> group(int maxSize);

  @Override
  @NotNull
  SetSequence<? extends SetSequence<E>> group(int size, E padding);

  @Override
  @NotNull
  SetSequence<Boolean> includes(Object element);

  @Override
  @NotNull
  SetSequence<Boolean> includesAll(@NotNull Iterable<?> elements);

  @Override
  @NotNull
  SetSequence<Boolean> includesSlice(@NotNull Iterable<?> elements);

  @Override
  @NotNull
  SetSequence<E> intersect(@NotNull Iterable<?> elements);

  @Override
  @NotNull
  <F> SetSequence<F> map(@NotNull Function<? super E, F> mapper);

  @Override
  @NotNull
  <F> SetSequence<F> map(@NotNull IndexedFunction<? super E, F> mapper);

  @Override
  @NotNull
  SetSequence<E> mapAfter(int numElements, @NotNull Function<? super E, ? extends E> mapper);

  @Override
  @NotNull
  SetSequence<E> mapAfter(int numElements, @NotNull IndexedFunction<? super E, ? extends E> mapper);

  @Override
  @NotNull
  SetSequence<E> mapFirstWhere(@NotNull IndexedPredicate<? super E> predicate,
      @NotNull IndexedFunction<? super E, ? extends E> mapper);

  @Override
  @NotNull
  SetSequence<E> mapFirstWhere(@NotNull Predicate<? super E> predicate,
      @NotNull Function<? super E, ? extends E> mapper);

  @Override
  @NotNull
  SetSequence<E> mapLastWhere(@NotNull IndexedPredicate<? super E> predicate,
      @NotNull IndexedFunction<? super E, ? extends E> mapper);

  @Override
  @NotNull
  SetSequence<E> mapLastWhere(@NotNull Predicate<? super E> predicate,
      @NotNull Function<? super E, ? extends E> mapper);

  @Override
  @NotNull
  SetSequence<E> mapWhere(@NotNull IndexedPredicate<? super E> predicate,
      @NotNull IndexedFunction<? super E, ? extends E> mapper);

  @Override
  @NotNull
  SetSequence<E> mapWhere(@NotNull Predicate<? super E> predicate,
      @NotNull Function<? super E, ? extends E> mapper);

  @Override
  @NotNull
  SetSequence<E> max(@NotNull Comparator<? super E> comparator);

  @Override
  @NotNull
  SetSequence<E> min(@NotNull Comparator<? super E> comparator);

  @Override
  @NotNull
  SetSequence<Boolean> notAll(@NotNull IndexedPredicate<? super E> predicate);

  @Override
  @NotNull
  SetSequence<Boolean> notAll(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  SetSequence<Boolean> notExists(@NotNull IndexedPredicate<? super E> predicate);

  @Override
  @NotNull
  SetSequence<Boolean> notExists(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  SetSequence<E> orElse(@NotNull Iterable<E> elements);

  @Override
  @NotNull
  SetSequence<E> orElseGet(@NotNull Supplier<? extends Iterable<? extends E>> supplier);

  @Override
  @NotNull
  SetSequence<E> plus(E element);

  @Override
  @NotNull
  SetSequence<E> plusAll(@NotNull Iterable<E> elements);

  @Override
  @NotNull
  SetSequence<E> reduce(@NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

  @Override
  @NotNull
  SetSequence<E> reduceLeft(@NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

  @Override
  @NotNull
  SetSequence<E> reduceRight(@NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

  @Override
  @NotNull
  SetSequence<E> removeAfter(int numElements);

  @Override
  @NotNull
  SetSequence<E> removeEach(E element);

  @Override
  @NotNull
  SetSequence<E> removeFirst(E element);

  @Override
  @NotNull
  SetSequence<E> removeFirstWhere(@NotNull IndexedPredicate<? super E> predicate);

  @Override
  @NotNull
  SetSequence<E> removeFirstWhere(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  SetSequence<E> removeLast(E element);

  @Override
  @NotNull
  SetSequence<E> removeLastWhere(@NotNull IndexedPredicate<? super E> predicate);

  @Override
  @NotNull
  SetSequence<E> removeLastWhere(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  SetSequence<E> removeSlice(int start, int end);

  @Override
  @NotNull
  SetSequence<E> removeWhere(@NotNull IndexedPredicate<? super E> predicate);

  @Override
  @NotNull
  SetSequence<E> removeWhere(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  SetSequence<E> replaceAfter(int numElements, E replacement);

  @Override
  @NotNull
  SetSequence<E> replaceEach(E element, E replacement);

  @Override
  @NotNull
  SetSequence<E> replaceFirst(E element, E replacement);

  @Override
  @NotNull
  SetSequence<E> replaceFirstWhere(@NotNull IndexedPredicate<? super E> predicate, E replacement);

  @Override
  @NotNull
  SetSequence<E> replaceFirstWhere(@NotNull Predicate<? super E> predicate, E replacement);

  @Override
  @NotNull
  SetSequence<E> replaceLast(E element, E replacement);

  @Override
  @NotNull
  SetSequence<E> replaceLastWhere(@NotNull IndexedPredicate<? super E> predicate, E replacement);

  @Override
  @NotNull
  SetSequence<E> replaceLastWhere(@NotNull Predicate<? super E> predicate, E replacement);

  @Override
  @NotNull
  SetSequence<E> replaceSlice(int start, int end, @NotNull Iterable<? extends E> patch);

  @Override
  @NotNull
  SetSequence<E> replaceWhere(@NotNull IndexedPredicate<? super E> predicate, E replacement);

  @Override
  @NotNull
  SetSequence<E> replaceWhere(@NotNull Predicate<? super E> predicate, E replacement);

  @Override
  @NotNull
  SetSequence<E> resizeTo(int numElements, E padding);

  @Override
  @NotNull
  SetSequence<E> slice(int start, int end);

  @Override
  @NotNull
  SetSequence<Boolean> startsWith(@NotNull Iterable<?> elements);

  @Override
  @NotNull
  SetSequence<E> take(int maxElements);

  @Override
  @NotNull
  SetSequence<E> takeRight(int maxElements);

  @Override
  @NotNull
  SetSequence<E> takeRightWhile(@NotNull IndexedPredicate<? super E> predicate);

  @Override
  @NotNull
  SetSequence<E> takeRightWhile(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  SetSequence<E> takeWhile(@NotNull IndexedPredicate<? super E> predicate);

  @Override
  @NotNull
  SetSequence<E> takeWhile(@NotNull Predicate<? super E> predicate);

  @Override
  @NotNull
  SetSequence<E> union(@NotNull Iterable<? extends E> elements);
}
