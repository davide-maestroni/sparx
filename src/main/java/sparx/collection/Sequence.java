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
import sparx.util.function.IndexedConsumer;
import sparx.util.function.IndexedFunction;
import sparx.util.function.IndexedPredicate;
import sparx.util.function.Predicate;
import sparx.util.function.Supplier;

public interface Sequence<E> extends Iterable<E> {

  @NotNull
  Sequence<Boolean> all(@NotNull IndexedPredicate<? super E> predicate);

  @NotNull
  Sequence<Boolean> all(@NotNull Predicate<? super E> predicate);

  <T> T apply(@NotNull Function<? super Sequence<E>, T> mapper); // TODO: cannot inherit!!!

  @NotNull
  <F> Sequence<F> as();

  @NotNull
  Sequence<Integer> count();

  @NotNull
  Sequence<Integer> count(@NotNull IndexedPredicate<? super E> predicate);

  @NotNull
  Sequence<Integer> count(@NotNull Predicate<? super E> predicate);

  @NotNull
  Sequence<E> diff(@NotNull Iterable<?> elements);

  void doFor(@NotNull Consumer<? super E> consumer);

  void doFor(@NotNull IndexedConsumer<? super E> consumer);

  void doWhile(@NotNull IndexedPredicate<? super E> predicate);

  void doWhile(@NotNull IndexedPredicate<? super E> condition,
      @NotNull IndexedConsumer<? super E> consumer);

  void doWhile(@NotNull Predicate<? super E> predicate);

  void doWhile(@NotNull Predicate<? super E> condition, @NotNull Consumer<? super E> consumer);

  @NotNull
  Sequence<E> drop(int maxElements);

  @NotNull
  Sequence<E> dropRight(int maxElements);

  @NotNull
  Sequence<E> dropRightWhile(@NotNull IndexedPredicate<? super E> predicate);

  @NotNull
  Sequence<E> dropRightWhile(@NotNull Predicate<? super E> predicate);

  @NotNull
  Sequence<E> dropWhile(@NotNull IndexedPredicate<? super E> predicate);

  @NotNull
  Sequence<E> dropWhile(@NotNull Predicate<? super E> predicate);

  @NotNull
  Sequence<Boolean> endsWith(@NotNull Iterable<?> elements);

  // TODO: enumerate()

  @NotNull
  Sequence<Boolean> exists(@NotNull IndexedPredicate<? super E> predicate);

  @NotNull
  Sequence<Boolean> exists(@NotNull Predicate<? super E> predicate);

  @NotNull
  Sequence<E> filter(@NotNull IndexedPredicate<? super E> predicate);

  @NotNull
  Sequence<E> filter(@NotNull Predicate<? super E> predicate);

  @NotNull
  Sequence<E> findAny(@NotNull IndexedPredicate<? super E> predicate);

  @NotNull
  Sequence<E> findAny(@NotNull Predicate<? super E> predicate);

  @NotNull
  Sequence<E> findFirst(@NotNull IndexedPredicate<? super E> predicate);

  @NotNull
  Sequence<E> findFirst(@NotNull Predicate<? super E> predicate);

  @NotNull
  Sequence<Integer> findIndexOf(Object element);

  @NotNull
  Sequence<Integer> findIndexWhere(@NotNull IndexedPredicate<? super E> predicate);

  @NotNull
  Sequence<Integer> findIndexWhere(@NotNull Predicate<? super E> predicate);

  @NotNull
  Sequence<Integer> findIndexOfSlice(@NotNull Iterable<?> elements);

  @NotNull
  Sequence<E> findLast(@NotNull IndexedPredicate<? super E> predicate);

  @NotNull
  Sequence<E> findLast(@NotNull Predicate<? super E> predicate);

  @NotNull
  Sequence<Integer> findLastIndexOf(Object element);

  @NotNull
  Sequence<Integer> findLastIndexWhere(@NotNull IndexedPredicate<? super E> predicate);

  @NotNull
  Sequence<Integer> findLastIndexWhere(@NotNull Predicate<? super E> predicate);

  @NotNull
  Sequence<Integer> findLastIndexOfSlice(@NotNull Iterable<?> elements);

  E first();

  @NotNull
  <F> Sequence<F> flatMap(@NotNull Function<? super E, ? extends Iterable<F>> mapper);

  @NotNull
  <F> Sequence<F> flatMap(@NotNull IndexedFunction<? super E, ? extends Iterable<F>> mapper);

  @NotNull
  Sequence<E> flatMapAfter(int numElements,
      @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

  @NotNull
  Sequence<E> flatMapAfter(int numElements,
      @NotNull IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper);

  @NotNull
  Sequence<E> flatMapFirstWhere(@NotNull IndexedPredicate<? super E> predicate,
      @NotNull IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper);

  @NotNull
  Sequence<E> flatMapFirstWhere(@NotNull Predicate<? super E> predicate,
      @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

  @NotNull
  Sequence<E> flatMapLastWhere(@NotNull IndexedPredicate<? super E> predicate,
      @NotNull IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper);

  @NotNull
  Sequence<E> flatMapLastWhere(@NotNull Predicate<? super E> predicate,
      @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

  @NotNull
  Sequence<E> flatMapWhere(@NotNull IndexedPredicate<? super E> predicate,
      @NotNull IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper);

  @NotNull
  Sequence<E> flatMapWhere(@NotNull Predicate<? super E> predicate,
      @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

  @NotNull
  <F> Sequence<F> fold(F identity,
      @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

  @NotNull
  <F> Sequence<F> foldLeft(F identity,
      @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

  @NotNull
  <F> Sequence<F> foldRight(F identity,
      @NotNull BinaryFunction<? super E, ? super F, ? extends F> operation);

  @NotNull
  Sequence<? extends Sequence<E>> group(int maxSize);

  @NotNull
  Sequence<? extends Sequence<E>> group(int size, E padding);

  @NotNull
  Sequence<Boolean> includes(Object element);

  @NotNull
  Sequence<Boolean> includesAll(@NotNull Iterable<?> elements);

  @NotNull
  Sequence<Boolean> includesSlice(@NotNull Iterable<?> elements);

  @NotNull
  Sequence<E> intersect(@NotNull Iterable<?> elements);

  boolean isEmpty();

  E last();

  @NotNull
  <F> Sequence<F> map(@NotNull Function<? super E, F> mapper);

  @NotNull
  <F> Sequence<F> map(@NotNull IndexedFunction<? super E, F> mapper);

  @NotNull
  Sequence<E> mapAfter(int numElements, @NotNull Function<? super E, ? extends E> mapper);

  @NotNull
  Sequence<E> mapAfter(int numElements, @NotNull IndexedFunction<? super E, ? extends E> mapper);

  @NotNull
  Sequence<E> mapFirstWhere(@NotNull IndexedPredicate<? super E> predicate,
      @NotNull IndexedFunction<? super E, ? extends E> mapper);

  @NotNull
  Sequence<E> mapFirstWhere(@NotNull Predicate<? super E> predicate,
      @NotNull Function<? super E, ? extends E> mapper);

  @NotNull
  Sequence<E> mapLastWhere(@NotNull IndexedPredicate<? super E> predicate,
      @NotNull IndexedFunction<? super E, ? extends E> mapper);

  @NotNull
  Sequence<E> mapLastWhere(@NotNull Predicate<? super E> predicate,
      @NotNull Function<? super E, ? extends E> mapper);

  @NotNull
  Sequence<E> mapWhere(@NotNull IndexedPredicate<? super E> predicate,
      @NotNull IndexedFunction<? super E, ? extends E> mapper);

  @NotNull
  Sequence<E> mapWhere(@NotNull Predicate<? super E> predicate,
      @NotNull Function<? super E, ? extends E> mapper);

  @NotNull
  Sequence<E> max(@NotNull Comparator<? super E> comparator);

  @NotNull
  Sequence<E> min(@NotNull Comparator<? super E> comparator);

  @NotNull
  Sequence<Boolean> notAll(@NotNull IndexedPredicate<? super E> predicate);

  @NotNull
  Sequence<Boolean> notAll(@NotNull Predicate<? super E> predicate);

  boolean notEmpty();

  @NotNull
  Sequence<Boolean> notExists(@NotNull IndexedPredicate<? super E> predicate);

  @NotNull
  Sequence<Boolean> notExists(@NotNull Predicate<? super E> predicate);

  @NotNull
  Sequence<E> orElse(@NotNull Iterable<E> elements);

  @NotNull
  Sequence<E> orElseGet(@NotNull Supplier<? extends Iterable<? extends E>> supplier);

  @NotNull
  Sequence<E> plus(E element);

  @NotNull
  Sequence<E> plusAll(@NotNull Iterable<E> elements);

  @NotNull
  Sequence<E> reduce(@NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

  @NotNull
  Sequence<E> reduceLeft(@NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

  @NotNull
  Sequence<E> reduceRight(@NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

  @NotNull
  Sequence<E> removeAfter(int numElements);

  @NotNull
  Sequence<E> removeEach(E element);

  @NotNull
  Sequence<E> removeFirst(E element);

  @NotNull
  Sequence<E> removeFirstWhere(@NotNull IndexedPredicate<? super E> predicate);

  @NotNull
  Sequence<E> removeFirstWhere(@NotNull Predicate<? super E> predicate);

  @NotNull
  Sequence<E> removeLast(E element);

  @NotNull
  Sequence<E> removeLastWhere(@NotNull IndexedPredicate<? super E> predicate);

  @NotNull
  Sequence<E> removeLastWhere(@NotNull Predicate<? super E> predicate);

  @NotNull
  Sequence<E> removeSlice(int start, int end);

  @NotNull
  Sequence<E> removeWhere(@NotNull IndexedPredicate<? super E> predicate);

  @NotNull
  Sequence<E> removeWhere(@NotNull Predicate<? super E> predicate);

  @NotNull
  Sequence<E> replaceAfter(int numElements, E replacement);

  @NotNull
  Sequence<E> replaceEach(E element, E replacement);

  @NotNull
  Sequence<E> replaceFirst(E element, E replacement);

  @NotNull
  Sequence<E> replaceFirstWhere(@NotNull IndexedPredicate<? super E> predicate, E replacement);

  @NotNull
  Sequence<E> replaceFirstWhere(@NotNull Predicate<? super E> predicate, E replacement);

  @NotNull
  Sequence<E> replaceLast(E element, E replacement);

  @NotNull
  Sequence<E> replaceLastWhere(@NotNull IndexedPredicate<? super E> predicate, E replacement);

  @NotNull
  Sequence<E> replaceLastWhere(@NotNull Predicate<? super E> predicate, E replacement);

  @NotNull
  Sequence<E> replaceSlice(int start, int end, @NotNull Iterable<? extends E> patch);

  @NotNull
  Sequence<E> replaceWhere(@NotNull IndexedPredicate<? super E> predicate, E replacement);

  @NotNull
  Sequence<E> replaceWhere(@NotNull Predicate<? super E> predicate, E replacement);

  @NotNull
  Sequence<E> resizeTo(int numElements, E padding);

  int size();

  @NotNull
  Sequence<E> slice(int start, int end);

  @NotNull
  Sequence<Boolean> startsWith(@NotNull Iterable<?> elements);

  @NotNull
  Sequence<E> take(int maxElements);

  @NotNull
  Sequence<E> takeRight(int maxElements);

  @NotNull
  Sequence<E> takeRightWhile(@NotNull IndexedPredicate<? super E> predicate);

  @NotNull
  Sequence<E> takeRightWhile(@NotNull Predicate<? super E> predicate);

  @NotNull
  Sequence<E> takeWhile(@NotNull IndexedPredicate<? super E> predicate);

  @NotNull
  Sequence<E> takeWhile(@NotNull Predicate<? super E> predicate);

  @NotNull
  Sequence<E> union(@NotNull Iterable<? extends E> elements);

  // TODO: isMemoized, isSorted, etc.
  // TODO: sliding?? split??

  // TODO: toArray, groupBy, toString(StringBuilder/StringJoiner), collect
  // TODO: zip, merge, combine

  // TODO: combinations
}
