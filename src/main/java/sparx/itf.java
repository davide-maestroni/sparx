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
package sparx;

import java.util.Comparator;
import org.jetbrains.annotations.NotNull;
import sparx.util.function.Action;
import sparx.util.function.BinaryFunction;
import sparx.util.function.Consumer;
import sparx.util.function.Function;
import sparx.util.function.IndexedConsumer;
import sparx.util.function.IndexedFunction;
import sparx.util.function.IndexedPredicate;
import sparx.util.function.Predicate;
import sparx.util.function.Supplier;

class itf {

  private itf() {
  }

  // TODO: equals, clone, Serializable

  public interface Collection<E> extends java.util.Collection<E>, Sequence<E> {

    @Override
    <T> T apply(@NotNull Function<? super Sequence<E>, T> mapper);

    @Override
    @NotNull
    <F> Collection<F> as();

    @Override
    @NotNull
    Collection<Integer> count();

    @Override
    @NotNull
    Collection<Integer> count(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Collection<Integer> count(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Collection<E> diff(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    Collection<E> drop(int maxElements);

    @Override
    @NotNull
    Collection<E> dropRight(int maxElements);

    @Override
    @NotNull
    Collection<E> dropRightWhile(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Collection<E> dropRightWhile(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Collection<E> dropWhile(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Collection<E> dropWhile(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Collection<Boolean> each(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Collection<Boolean> each(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Collection<Boolean> endsWith(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    Collection<Boolean> exists(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Collection<Boolean> exists(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Collection<E> filter(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Collection<E> filter(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Collection<E> findAny(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Collection<E> findAny(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Collection<E> findFirst(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Collection<E> findFirst(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Collection<Integer> findIndexOf(Object element);

    @Override
    @NotNull
    Collection<Integer> findIndexOfSlice(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    Collection<Integer> findIndexWhere(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Collection<Integer> findIndexWhere(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Collection<E> findLast(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Collection<E> findLast(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Collection<Integer> findLastIndexOf(Object element);

    @Override
    @NotNull
    Collection<Integer> findLastIndexOfSlice(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    Collection<Integer> findLastIndexWhere(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Collection<Integer> findLastIndexWhere(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    <F> Collection<F> flatMap(@NotNull Function<? super E, ? extends Iterable<F>> mapper);

    @Override
    @NotNull
    <F> Collection<F> flatMap(@NotNull IndexedFunction<? super E, ? extends Iterable<F>> mapper);

    @Override
    @NotNull
    Collection<E> flatMapAfter(int numElements,
        @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    Collection<E> flatMapAfter(int numElements,
        @NotNull IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    Collection<E> flatMapFirstWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    Collection<E> flatMapFirstWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    Collection<E> flatMapLastWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    Collection<E> flatMapLastWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    Collection<E> flatMapWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    Collection<E> flatMapWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    <F> Collection<F> fold(F identity,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    @NotNull
    <F> Collection<F> foldLeft(F identity,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    @NotNull
    <F> Collection<F> foldRight(F identity,
        @NotNull BinaryFunction<? super E, ? super F, ? extends F> operation);

    @Override
    @NotNull
    Iterator<E> iterator();

    @Override
    @NotNull
    Collection<? extends Sequence<E>> group(int maxSize);

    @Override
    @NotNull
    Collection<? extends Sequence<E>> group(int size, E padding);

    @Override
    @NotNull
    Collection<Boolean> includes(Object element);

    @Override
    @NotNull
    Collection<Boolean> includesAll(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    Collection<Boolean> includesSlice(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    Collection<E> intersect(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    <F> Collection<F> map(@NotNull Function<? super E, F> mapper);

    @Override
    @NotNull
    <F> Collection<F> map(@NotNull IndexedFunction<? super E, F> mapper);

    @Override
    @NotNull
    Collection<E> mapAfter(int numElements, @NotNull Function<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Collection<E> mapAfter(int numElements,
        @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Collection<E> mapFirstWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Collection<E> mapFirstWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Collection<E> mapLastWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Collection<E> mapLastWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Collection<E> mapWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Collection<E> mapWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Collection<E> max(@NotNull Comparator<? super E> comparator);

    @Override
    @NotNull
    Collection<E> min(@NotNull Comparator<? super E> comparator);

    @Override
    @NotNull
    Collection<Boolean> none(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Collection<Boolean> none(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Collection<Boolean> notAll(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Collection<Boolean> notAll(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Collection<E> orElse(@NotNull Iterable<E> elements);

    @Override
    @NotNull
    Collection<E> orElseGet(@NotNull Supplier<? extends Iterable<? extends E>> supplier);

    @Override
    @NotNull
    Collection<E> plus(E element);

    @Override
    @NotNull
    Collection<E> plusAll(@NotNull Iterable<E> elements);

    @Override
    @NotNull
    Sequence<E> reduce(@NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    Collection<E> reduceLeft(@NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    Collection<E> reduceRight(@NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    Collection<E> removeAfter(int numElements);

    @Override
    @NotNull
    Collection<E> removeEach(E element);

    @Override
    @NotNull
    Collection<E> removeFirst(E element);

    @Override
    @NotNull
    Collection<E> removeFirstWhere(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Collection<E> removeFirstWhere(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Collection<E> removeLast(E element);

    @Override
    @NotNull
    Collection<E> removeLastWhere(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Collection<E> removeLastWhere(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Collection<E> removeSlice(int start, int end);

    @Override
    @NotNull
    Collection<E> removeWhere(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Collection<E> removeWhere(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Collection<E> replaceAfter(int numElements, E replacement);

    @Override
    @NotNull
    Collection<E> replaceEach(E element, E replacement);

    @Override
    @NotNull
    Collection<E> replaceFirst(E element, E replacement);

    @Override
    @NotNull
    Collection<E> replaceFirstWhere(@NotNull IndexedPredicate<? super E> predicate, E replacement);

    @Override
    @NotNull
    Collection<E> replaceFirstWhere(@NotNull Predicate<? super E> predicate, E replacement);

    @Override
    @NotNull
    Collection<E> replaceLast(E element, E replacement);

    @Override
    @NotNull
    Collection<E> replaceLastWhere(@NotNull IndexedPredicate<? super E> predicate, E replacement);

    @Override
    @NotNull
    Collection<E> replaceLastWhere(@NotNull Predicate<? super E> predicate, E replacement);

    @Override
    @NotNull
    Collection<E> replaceSlice(int start, int end, @NotNull Iterable<? extends E> patch);

    @Override
    @NotNull
    Collection<E> replaceWhere(@NotNull IndexedPredicate<? super E> predicate, E replacement);

    @Override
    @NotNull
    Collection<E> replaceWhere(@NotNull Predicate<? super E> predicate, E replacement);

    @Override
    @NotNull
    Collection<E> resizeTo(int numElements, E padding);

    @Override
    @NotNull
    Collection<E> slice(int start);

    @Override
    @NotNull
    Collection<E> slice(int start, int end);

    @Override
    @NotNull
    Collection<Boolean> startsWith(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    Collection<E> take(int maxElements);

    @Override
    @NotNull
    Collection<E> takeRight(int maxElements);

    @Override
    @NotNull
    Collection<E> takeRightWhile(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Collection<E> takeRightWhile(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Collection<E> takeWhile(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Collection<E> takeWhile(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Collection<E> union(@NotNull Iterable<? extends E> elements);
  }

  public interface Future<E, T> extends java.util.concurrent.Future<T> {

    boolean isFailed();

    @NotNull
    java.util.concurrent.Future<?> nonBlockingFor(@NotNull Consumer<? super E> consumer);

    @NotNull
    java.util.concurrent.Future<?> nonBlockingFor(@NotNull IndexedConsumer<? super E> consumer);

    @NotNull
    java.util.concurrent.Future<?> nonBlockingGet();

    @NotNull
    java.util.concurrent.Future<?> nonBlockingWhile(@NotNull IndexedPredicate<? super E> predicate);

    @NotNull
    java.util.concurrent.Future<?> nonBlockingWhile(@NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedConsumer<? super E> consumer);

    @NotNull
    java.util.concurrent.Future<?> nonBlockingWhile(@NotNull Predicate<? super E> predicate);

    @NotNull
    java.util.concurrent.Future<?> nonBlockingWhile(@NotNull Predicate<? super E> condition,
        @NotNull Consumer<? super E> consumer);
  }

  public interface Iterator<E> extends java.util.Iterator<E>, Stream<E> {

    @Override
    @NotNull
    Iterator<E> append(E element);

    @Override
    @NotNull
    Iterator<E> appendAll(@NotNull Iterable<? extends E> elements);

    @Override
    @NotNull
    <F> Iterator<F> as();

    @Override
    @NotNull
    Iterator<Integer> count();

    @Override
    @NotNull
    Iterator<Integer> count(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<Integer> count(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<E> diff(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    Iterator<E> drop(int maxElements);

    @Override
    @NotNull
    Iterator<E> dropRight(int maxElements);

    @Override
    @NotNull
    Iterator<E> dropRightWhile(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<E> dropRightWhile(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<E> dropWhile(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<E> dropWhile(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<Boolean> each(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<Boolean> each(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<Boolean> endsWith(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    Iterator<Boolean> exists(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<Boolean> exists(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<E> filter(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<E> filter(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<E> findAny(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<E> findAny(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<E> findFirst(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<E> findFirst(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<Integer> findIndexOf(Object element);

    @Override
    @NotNull
    Iterator<Integer> findIndexOfSlice(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    Iterator<Integer> findIndexWhere(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<Integer> findIndexWhere(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<E> findLast(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<E> findLast(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<Integer> findLastIndexOf(Object element);

    @Override
    @NotNull
    Iterator<Integer> findLastIndexOfSlice(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    Iterator<Integer> findLastIndexWhere(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<Integer> findLastIndexWhere(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    <F> Iterator<F> flatMap(@NotNull Function<? super E, ? extends Iterable<F>> mapper);

    @Override
    @NotNull
    <F> Iterator<F> flatMap(@NotNull IndexedFunction<? super E, ? extends Iterable<F>> mapper);

    @Override
    @NotNull
    Iterator<E> flatMapAfter(int numElements,
        @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    Iterator<E> flatMapAfter(int numElements,
        @NotNull IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    Iterator<E> flatMapFirstWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    Iterator<E> flatMapFirstWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    Iterator<E> flatMapLastWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    Iterator<E> flatMapLastWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    Iterator<E> flatMapWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    Iterator<E> flatMapWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    <F> Iterator<F> fold(F identity,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    @NotNull
    <F> Iterator<F> foldLeft(F identity,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    @NotNull
    <F> Iterator<F> foldRight(F identity,
        @NotNull BinaryFunction<? super E, ? super F, ? extends F> operation);

    @Override
    @NotNull
    Iterator<? extends Iterator<E>> group(int maxSize);

    @Override
    @NotNull
    Iterator<? extends Iterator<E>> group(int size, E padding);

    @Override
    @NotNull
    Iterator<Boolean> includes(Object element);

    @Override
    @NotNull
    Iterator<Boolean> includesAll(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    Iterator<Boolean> includesSlice(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    Iterator<E> insert(E element);

    @Override
    @NotNull
    Iterator<E> insertAfter(int numElements, E element);

    @Override
    @NotNull
    Iterator<E> insertAll(@NotNull Iterable<? extends E> elements);

    @Override
    @NotNull
    Iterator<E> insertAllAfter(int numElements, @NotNull Iterable<? extends E> elements);

    @Override
    @NotNull
    Iterator<E> intersect(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    Iterator<E> iterator();

    @Override
    @NotNull
    <F> Iterator<F> map(@NotNull Function<? super E, F> mapper);

    @Override
    @NotNull
    <F> Iterator<F> map(@NotNull IndexedFunction<? super E, F> mapper);

    @Override
    @NotNull
    Iterator<E> mapAfter(int numElements, @NotNull Function<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Iterator<E> mapAfter(int numElements, @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Iterator<E> mapFirstWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Iterator<E> mapFirstWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Iterator<E> mapLastWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Iterator<E> mapLastWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Iterator<E> mapWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Iterator<E> mapWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Iterator<E> max(@NotNull Comparator<? super E> comparator);

    @Override
    @NotNull
    Iterator<E> min(@NotNull Comparator<? super E> comparator);

    @Override
    @NotNull
    Iterator<Boolean> none(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<Boolean> none(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<Boolean> notAll(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<Boolean> notAll(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<E> orElse(@NotNull Iterable<E> elements);

    @Override
    @NotNull
    Iterator<E> orElseGet(@NotNull Supplier<? extends Iterable<? extends E>> supplier);

    @NotNull
    Iterator<E> peek(@NotNull Consumer<? super E> consumer);

    @NotNull
    Iterator<E> peek(@NotNull IndexedConsumer<? super E> consumer);

    @NotNull
    Iterator<E> peekExceptionally(@NotNull Consumer<? super Throwable> consumer);

    @NotNull
    Iterator<E> peekExceptionally(@NotNull IndexedConsumer<? super Throwable> consumer);

    @Override
    @NotNull
    Iterator<E> plus(E element);

    @Override
    @NotNull
    Iterator<E> plusAll(@NotNull Iterable<E> elements);

    @Override
    @NotNull
    Iterator<E> reduce(@NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    Iterator<E> reduceLeft(@NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    Iterator<E> reduceRight(@NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    Iterator<E> removeAfter(int numElements);

    @Override
    @NotNull
    Iterator<E> removeEach(E element);

    @Override
    @NotNull
    Iterator<E> removeFirst(E element);

    @Override
    @NotNull
    Iterator<E> removeFirstWhere(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<E> removeFirstWhere(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<E> removeLast(E element);

    @Override
    @NotNull
    Iterator<E> removeLastWhere(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<E> removeLastWhere(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<E> removeSlice(int start, int end);

    @Override
    @NotNull
    Iterator<E> removeWhere(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<E> removeWhere(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<E> replaceAfter(int numElements, E replacement);

    @Override
    @NotNull
    Iterator<E> replaceEach(E element, E replacement);

    @Override
    @NotNull
    Iterator<E> replaceFirst(E element, E replacement);

    @Override
    @NotNull
    Iterator<E> replaceFirstWhere(@NotNull IndexedPredicate<? super E> predicate, E replacement);

    @Override
    @NotNull
    Iterator<E> replaceFirstWhere(@NotNull Predicate<? super E> predicate, E replacement);

    @Override
    @NotNull
    Iterator<E> replaceLast(E element, E replacement);

    @Override
    @NotNull
    Iterator<E> replaceLastWhere(@NotNull IndexedPredicate<? super E> predicate, E replacement);

    @Override
    @NotNull
    Iterator<E> replaceLastWhere(@NotNull Predicate<? super E> predicate, E replacement);

    @Override
    @NotNull
    Iterator<E> replaceSlice(int start, int end, @NotNull Iterable<? extends E> patch);

    @Override
    @NotNull
    Iterator<E> replaceWhere(@NotNull IndexedPredicate<? super E> predicate, E replacement);

    @Override
    @NotNull
    Iterator<E> replaceWhere(@NotNull Predicate<? super E> predicate, E replacement);

    @Override
    @NotNull
    Iterator<E> resizeTo(int numElements, E padding);

    @NotNull
    Iterator<E> runFinally(@NotNull Action action);

    @Override
    @NotNull
    Iterator<E> slice(int start);

    @Override
    @NotNull
    Iterator<E> slice(int start, int end);

    @NotNull
    Iterator<? extends Iterator<E>> slidingWindow(int maxSize, int step);

    @NotNull
    Iterator<? extends Iterator<E>> slidingWindow(int size, int step, E padding);

    @Override
    @NotNull
    Iterator<Boolean> startsWith(@NotNull Iterable<?> elements);

    @NotNull
    <T extends Throwable> Iterator<E> switchExceptionally(@NotNull Class<T> exceptionType,
        @NotNull Function<? super T, ? extends Iterable<? extends E>> mapper);

    @NotNull
    <T extends Throwable> Iterator<E> switchExceptionally(@NotNull Class<T> exceptionType,
        @NotNull IndexedFunction<? super T, ? extends Iterable<? extends E>> mapper);

    @NotNull
    Iterator<E> switchExceptionally(
        @NotNull Function<? super Throwable, ? extends Iterable<? extends E>> mapper);

    @NotNull
    Iterator<E> switchExceptionally(
        @NotNull IndexedFunction<? super Throwable, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    Iterator<E> take(int maxElements);

    @Override
    @NotNull
    Iterator<E> takeRight(int maxElements);

    @Override
    @NotNull
    Iterator<E> takeRightWhile(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<E> takeRightWhile(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<E> takeWhile(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<E> takeWhile(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<E> union(@NotNull Iterable<? extends E> elements);
  }

  public interface List<E> extends Collection<E>, java.util.List<E>, Sequence<E> {

    @NotNull
    List<E> append(E element);

    @NotNull
    List<E> appendAll(@NotNull Iterable<? extends E> elements);

    @Override
    @NotNull
    <F> List<F> as();

    @Override
    @NotNull
    List<Integer> count();

    @Override
    @NotNull
    List<Integer> count(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    List<Integer> count(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    List<E> diff(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    List<E> drop(int maxElements);

    @Override
    @NotNull
    List<E> dropRight(int maxElements);

    @Override
    @NotNull
    List<E> dropRightWhile(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    List<E> dropRightWhile(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    List<E> dropWhile(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    List<E> dropWhile(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    List<Boolean> each(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    List<Boolean> each(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    List<Boolean> endsWith(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    List<Boolean> exists(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    List<Boolean> exists(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    List<E> filter(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    List<E> filter(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    List<E> findAny(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    List<E> findAny(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    List<E> findFirst(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    List<E> findFirst(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    List<Integer> findIndexOf(Object element);

    @Override
    @NotNull
    List<Integer> findIndexOfSlice(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    List<Integer> findIndexWhere(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    List<Integer> findIndexWhere(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    List<E> findLast(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    List<E> findLast(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    List<Integer> findLastIndexOf(Object element);

    @Override
    @NotNull
    List<Integer> findLastIndexOfSlice(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    List<Integer> findLastIndexWhere(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    List<Integer> findLastIndexWhere(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    <F> List<F> flatMap(@NotNull Function<? super E, ? extends Iterable<F>> mapper);

    @Override
    @NotNull
    <F> List<F> flatMap(@NotNull IndexedFunction<? super E, ? extends Iterable<F>> mapper);

    @Override
    @NotNull
    List<E> flatMapAfter(int numElements,
        @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    List<E> flatMapAfter(int numElements,
        @NotNull IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    List<E> flatMapFirstWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    List<E> flatMapFirstWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    List<E> flatMapLastWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    List<E> flatMapLastWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    List<E> flatMapWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    List<E> flatMapWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    <F> List<F> fold(F identity,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    @NotNull
    <F> List<F> foldLeft(F identity,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    @NotNull
    <F> List<F> foldRight(F identity,
        @NotNull BinaryFunction<? super E, ? super F, ? extends F> operation);

    @Override
    @NotNull
    List<? extends List<E>> group(int maxSize);

    @Override
    @NotNull
    List<? extends List<E>> group(int size, E padding);

    @Override
    @NotNull
    List<Boolean> includes(Object element);

    @Override
    @NotNull
    List<Boolean> includesAll(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    List<Boolean> includesSlice(@NotNull Iterable<?> elements);

    @NotNull
    List<E> insertAfter(int numElements, E element);

    @NotNull
    List<E> insertAllAfter(int numElements, @NotNull Iterable<? extends E> elements);

    @Override
    @NotNull
    List<E> intersect(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    ListIterator<E> listIterator();

    @Override
    @NotNull
    ListIterator<E> listIterator(int index);

    @Override
    @NotNull
    <F> List<F> map(@NotNull Function<? super E, F> mapper);

    @Override
    @NotNull
    <F> List<F> map(@NotNull IndexedFunction<? super E, F> mapper);

    @Override
    @NotNull
    List<E> mapAfter(int numElements, @NotNull Function<? super E, ? extends E> mapper);

    @Override
    @NotNull
    List<E> mapAfter(int numElements, @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    @NotNull
    List<E> mapFirstWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    @NotNull
    List<E> mapFirstWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends E> mapper);

    @Override
    @NotNull
    List<E> mapLastWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    @NotNull
    List<E> mapLastWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends E> mapper);

    @Override
    @NotNull
    List<E> mapWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    @NotNull
    List<E> mapWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends E> mapper);

    @Override
    @NotNull
    List<E> max(@NotNull Comparator<? super E> comparator);

    @Override
    @NotNull
    List<E> min(@NotNull Comparator<? super E> comparator);

    @Override
    @NotNull
    List<Boolean> none(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    List<Boolean> none(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    List<Boolean> notAll(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    List<Boolean> notAll(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    List<E> orElse(@NotNull Iterable<E> elements);

    @Override
    @NotNull
    List<E> orElseGet(@NotNull Supplier<? extends Iterable<? extends E>> supplier);

    @Override
    @NotNull
    List<E> plus(E element);

    @Override
    @NotNull
    List<E> plusAll(@NotNull Iterable<E> elements);

    @NotNull
    List<E> prepend(E element);

    @NotNull
    List<E> prependAll(@NotNull Iterable<? extends E> elements);

    @Override
    @NotNull
    List<E> reduce(@NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    List<E> reduceLeft(@NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    List<E> reduceRight(@NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    List<E> removeAfter(int numElements);

    @Override
    @NotNull
    List<E> removeEach(E element);

    @Override
    @NotNull
    List<E> removeFirst(E element);

    @Override
    @NotNull
    List<E> removeFirstWhere(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    List<E> removeFirstWhere(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    List<E> removeLast(E element);

    @Override
    @NotNull
    List<E> removeLastWhere(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    List<E> removeLastWhere(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    List<E> removeSlice(int start, int end);

    @Override
    @NotNull
    List<E> removeWhere(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    List<E> removeWhere(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    List<E> replaceAfter(int numElements, E replacement);

    @Override
    @NotNull
    List<E> replaceEach(E element, E replacement);

    @Override
    @NotNull
    List<E> replaceFirst(E element, E replacement);

    @Override
    @NotNull
    List<E> replaceFirstWhere(@NotNull IndexedPredicate<? super E> predicate, E replacement);

    @Override
    @NotNull
    List<E> replaceFirstWhere(@NotNull Predicate<? super E> predicate, E replacement);

    @Override
    @NotNull
    List<E> replaceLast(E element, E replacement);

    @Override
    @NotNull
    List<E> replaceLastWhere(@NotNull IndexedPredicate<? super E> predicate, E replacement);

    @Override
    @NotNull
    List<E> replaceLastWhere(@NotNull Predicate<? super E> predicate, E replacement);

    @Override
    @NotNull
    List<E> replaceSlice(int start, int end, @NotNull Iterable<? extends E> patch);

    @Override
    @NotNull
    List<E> replaceWhere(@NotNull IndexedPredicate<? super E> predicate, E replacement);

    @Override
    @NotNull
    List<E> replaceWhere(@NotNull Predicate<? super E> predicate, E replacement);

    @Override
    @NotNull
    List<E> resizeTo(int numElements, E padding);

    @NotNull
    List<E> reverse();

    @Override
    @NotNull
    List<E> slice(int start);

    @Override
    @NotNull
    List<E> slice(int start, int end);

    @Override
    @NotNull
    List<Boolean> startsWith(@NotNull Iterable<?> elements);

    @NotNull
    List<E> sorted(@NotNull Comparator<? super E> comparator);

    @Override
    @NotNull
    List<E> take(int maxElements);

    @Override
    @NotNull
    List<E> takeRight(int maxElements);

    @Override
    @NotNull
    List<E> takeRightWhile(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    List<E> takeRightWhile(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    List<E> takeWhile(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    List<E> takeWhile(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    List<E> union(@NotNull Iterable<? extends E> elements);
  }

  public interface ListIterator<E> extends java.util.ListIterator<E>, Stream<E> {

    @Override
    @NotNull
    ListIterator<E> append(E element);

    @Override
    @NotNull
    ListIterator<E> appendAll(@NotNull Iterable<? extends E> elements);

    @Override
    @NotNull
    <F> ListIterator<F> as();

    @Override
    @NotNull
    ListIterator<Integer> count();

    @Override
    @NotNull
    ListIterator<Integer> count(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<Integer> count(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<E> diff(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    ListIterator<E> drop(int maxElements);

    @Override
    @NotNull
    ListIterator<E> dropRight(int maxElements);

    @Override
    @NotNull
    ListIterator<E> dropRightWhile(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<E> dropRightWhile(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<E> dropWhile(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<E> dropWhile(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<Boolean> each(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<Boolean> each(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<Boolean> endsWith(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    ListIterator<Boolean> exists(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<Boolean> exists(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<E> filter(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<E> filter(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<E> findAny(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<E> findAny(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<E> findFirst(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<E> findFirst(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<Integer> findIndexOf(Object element);

    @Override
    @NotNull
    ListIterator<Integer> findIndexOfSlice(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    ListIterator<Integer> findIndexWhere(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<Integer> findIndexWhere(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<E> findLast(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<E> findLast(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<Integer> findLastIndexOf(Object element);

    @Override
    @NotNull
    ListIterator<Integer> findLastIndexOfSlice(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    ListIterator<Integer> findLastIndexWhere(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<Integer> findLastIndexWhere(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    <F> ListIterator<F> flatMap(@NotNull Function<? super E, ? extends Iterable<F>> mapper);

    @Override
    @NotNull
    <F> ListIterator<F> flatMap(@NotNull IndexedFunction<? super E, ? extends Iterable<F>> mapper);

    @Override
    @NotNull
    ListIterator<E> flatMapAfter(int numElements,
        @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    ListIterator<E> flatMapAfter(int numElements,
        @NotNull IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    ListIterator<E> flatMapFirstWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    ListIterator<E> flatMapFirstWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    ListIterator<E> flatMapLastWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    ListIterator<E> flatMapLastWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    ListIterator<E> flatMapWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    ListIterator<E> flatMapWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    <F> ListIterator<F> fold(F identity,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    @NotNull
    <F> ListIterator<F> foldLeft(F identity,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    @NotNull
    <F> ListIterator<F> foldRight(F identity,
        @NotNull BinaryFunction<? super E, ? super F, ? extends F> operation);

    @Override
    @NotNull
    ListIterator<? extends ListIterator<E>> group(int maxSize);

    @Override
    @NotNull
    ListIterator<? extends ListIterator<E>> group(int size, E padding);

    @Override
    @NotNull
    ListIterator<Boolean> includes(Object element);

    @Override
    @NotNull
    ListIterator<Boolean> includesAll(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    ListIterator<Boolean> includesSlice(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    ListIterator<E> insert(E element);

    @Override
    @NotNull
    ListIterator<E> insertAfter(int numElements, E element);

    @Override
    @NotNull
    ListIterator<E> insertAll(@NotNull Iterable<? extends E> elements);

    @Override
    @NotNull
    ListIterator<E> insertAllAfter(int numElements, @NotNull Iterable<? extends E> elements);

    @Override
    @NotNull
    ListIterator<E> intersect(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    Iterator<E> iterator();

    @Override
    @NotNull
    <F> ListIterator<F> map(@NotNull Function<? super E, F> mapper);

    @Override
    @NotNull
    <F> ListIterator<F> map(@NotNull IndexedFunction<? super E, F> mapper);

    @Override
    @NotNull
    ListIterator<E> mapAfter(int numElements, @NotNull Function<? super E, ? extends E> mapper);

    @Override
    @NotNull
    ListIterator<E> mapAfter(int numElements,
        @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    @NotNull
    ListIterator<E> mapFirstWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    @NotNull
    ListIterator<E> mapFirstWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends E> mapper);

    @Override
    @NotNull
    ListIterator<E> mapLastWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    @NotNull
    ListIterator<E> mapLastWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends E> mapper);

    @Override
    @NotNull
    ListIterator<E> mapWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    @NotNull
    ListIterator<E> mapWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends E> mapper);

    @Override
    @NotNull
    ListIterator<E> max(@NotNull Comparator<? super E> comparator);

    @Override
    @NotNull
    ListIterator<E> min(@NotNull Comparator<? super E> comparator);

    @NotNull
    ListIterator<E> moveBy(int maxElements);

    @NotNull
    ListIterator<E> moveTo(int index);

    @Override
    @NotNull
    ListIterator<Boolean> none(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<Boolean> none(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<Boolean> notAll(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<Boolean> notAll(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<E> orElse(@NotNull Iterable<E> elements);

    @Override
    @NotNull
    ListIterator<E> orElseGet(@NotNull Supplier<? extends Iterable<? extends E>> supplier);

    @Override
    @NotNull
    ListIterator<E> plus(E element);

    @Override
    @NotNull
    ListIterator<E> plusAll(@NotNull Iterable<E> elements);

    @Override
    @NotNull
    ListIterator<E> reduce(@NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    ListIterator<E> reduceLeft(
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    ListIterator<E> reduceRight(
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    ListIterator<E> removeAfter(int numElements);

    @Override
    @NotNull
    ListIterator<E> removeEach(E element);

    @Override
    @NotNull
    ListIterator<E> removeFirst(E element);

    @Override
    @NotNull
    ListIterator<E> removeFirstWhere(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<E> removeFirstWhere(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<E> removeLast(E element);

    @Override
    @NotNull
    ListIterator<E> removeLastWhere(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<E> removeLastWhere(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<E> removeSlice(int start, int end);

    @Override
    @NotNull
    ListIterator<E> removeWhere(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<E> removeWhere(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<E> replaceAfter(int numElements, E replacement);

    @Override
    @NotNull
    ListIterator<E> replaceEach(E element, E replacement);

    @Override
    @NotNull
    ListIterator<E> replaceFirst(E element, E replacement);

    @Override
    @NotNull
    ListIterator<E> replaceFirstWhere(@NotNull IndexedPredicate<? super E> predicate,
        E replacement);

    @Override
    @NotNull
    ListIterator<E> replaceFirstWhere(@NotNull Predicate<? super E> predicate, E replacement);

    @Override
    @NotNull
    ListIterator<E> replaceLast(E element, E replacement);

    @Override
    @NotNull
    ListIterator<E> replaceLastWhere(@NotNull IndexedPredicate<? super E> predicate, E replacement);

    @Override
    @NotNull
    ListIterator<E> replaceLastWhere(@NotNull Predicate<? super E> predicate, E replacement);

    @Override
    @NotNull
    ListIterator<E> replaceSlice(int start, int end, @NotNull Iterable<? extends E> patch);

    @Override
    @NotNull
    ListIterator<E> replaceWhere(@NotNull IndexedPredicate<? super E> predicate, E replacement);

    @Override
    @NotNull
    ListIterator<E> replaceWhere(@NotNull Predicate<? super E> predicate, E replacement);

    @Override
    @NotNull
    ListIterator<E> resizeTo(int numElements, E padding);

    @NotNull
    ListIterator<E> reverse();

    @Override
    @NotNull
    ListIterator<E> slice(int start);

    @Override
    @NotNull
    ListIterator<E> slice(int start, int end);

    @Override
    @NotNull
    ListIterator<Boolean> startsWith(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    ListIterator<E> take(int maxElements);

    @Override
    @NotNull
    ListIterator<E> takeRight(int maxElements);

    @Override
    @NotNull
    ListIterator<E> takeRightWhile(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<E> takeRightWhile(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<E> takeWhile(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<E> takeWhile(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<E> union(@NotNull Iterable<? extends E> elements);
  }

  // TODO: Option???

  public interface Sequence<E> extends Iterable<E> {

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
    Sequence<Boolean> each(@NotNull IndexedPredicate<? super E> predicate);

    @NotNull
    Sequence<Boolean> each(@NotNull Predicate<? super E> predicate);

    @NotNull
    Sequence<Boolean> endsWith(@NotNull Iterable<?> elements);

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
    Sequence<Boolean> none(@NotNull IndexedPredicate<? super E> predicate);

    @NotNull
    Sequence<Boolean> none(@NotNull Predicate<? super E> predicate);

    @NotNull
    Sequence<Boolean> notAll(@NotNull IndexedPredicate<? super E> predicate);

    @NotNull
    Sequence<Boolean> notAll(@NotNull Predicate<? super E> predicate);

    boolean notEmpty();

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
    Sequence<E> slice(int start);

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
    // TODO: split??

    // TODO: toArray, toString(StringBuilder/StringJoiner), collect
    // TODO: zip, merge, combine

    // TODO: combinations
  }

  public interface Set<E> extends java.util.Set<E>, Sequence<E> {

    @Override
    @NotNull
    <F> Set<F> as();

    @Override
    @NotNull
    Set<Integer> count();

    @Override
    @NotNull
    Set<Integer> count(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Set<Integer> count(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Set<E> diff(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    Set<E> drop(int maxElements);

    @Override
    @NotNull
    Set<E> dropRight(int maxElements);

    @Override
    @NotNull
    Set<E> dropRightWhile(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Set<E> dropRightWhile(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Set<E> dropWhile(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Set<E> dropWhile(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Set<Boolean> each(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Set<Boolean> each(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Set<Boolean> endsWith(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    Set<Boolean> exists(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Set<Boolean> exists(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Set<E> filter(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Set<E> filter(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Set<E> findAny(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Set<E> findAny(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Set<E> findFirst(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Set<E> findFirst(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Set<Integer> findIndexOf(Object element);

    @Override
    @NotNull
    Set<Integer> findIndexOfSlice(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    Set<Integer> findIndexWhere(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Set<Integer> findIndexWhere(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Set<E> findLast(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Set<E> findLast(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Set<Integer> findLastIndexOf(Object element);

    @Override
    @NotNull
    Set<Integer> findLastIndexOfSlice(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    Set<Integer> findLastIndexWhere(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Set<Integer> findLastIndexWhere(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    <F> Set<F> flatMap(@NotNull Function<? super E, ? extends Iterable<F>> mapper);

    @Override
    @NotNull
    <F> Set<F> flatMap(@NotNull IndexedFunction<? super E, ? extends Iterable<F>> mapper);

    @Override
    @NotNull
    Set<E> flatMapAfter(int numElements,
        @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    Set<E> flatMapAfter(int numElements,
        @NotNull IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    Set<E> flatMapFirstWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    Set<E> flatMapFirstWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    Set<E> flatMapLastWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    Set<E> flatMapLastWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    Set<E> flatMapWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    Set<E> flatMapWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    <F> Set<F> fold(F identity,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    @NotNull
    <F> Set<F> foldLeft(F identity,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    @NotNull
    <F> Set<F> foldRight(F identity,
        @NotNull BinaryFunction<? super E, ? super F, ? extends F> operation);

    @Override
    @NotNull
    Set<? extends Set<E>> group(int maxSize);

    @Override
    @NotNull
    Set<? extends Set<E>> group(int size, E padding);

    @Override
    @NotNull
    Set<Boolean> includes(Object element);

    @Override
    @NotNull
    Set<Boolean> includesAll(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    Set<Boolean> includesSlice(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    Set<E> intersect(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    <F> Set<F> map(@NotNull Function<? super E, F> mapper);

    @Override
    @NotNull
    <F> Set<F> map(@NotNull IndexedFunction<? super E, F> mapper);

    @Override
    @NotNull
    Set<E> mapAfter(int numElements, @NotNull Function<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Set<E> mapAfter(int numElements, @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Set<E> mapFirstWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Set<E> mapFirstWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Set<E> mapLastWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Set<E> mapLastWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Set<E> mapWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Set<E> mapWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Set<E> max(@NotNull Comparator<? super E> comparator);

    @Override
    @NotNull
    Set<E> min(@NotNull Comparator<? super E> comparator);

    @Override
    @NotNull
    Set<Boolean> none(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Set<Boolean> none(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Set<Boolean> notAll(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Set<Boolean> notAll(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Set<E> orElse(@NotNull Iterable<E> elements);

    @Override
    @NotNull
    Set<E> orElseGet(@NotNull Supplier<? extends Iterable<? extends E>> supplier);

    @Override
    @NotNull
    Set<E> plus(E element);

    @Override
    @NotNull
    Set<E> plusAll(@NotNull Iterable<E> elements);

    @Override
    @NotNull
    Set<E> reduce(@NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    Set<E> reduceLeft(@NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    Set<E> reduceRight(@NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    Set<E> removeAfter(int numElements);

    @Override
    @NotNull
    Set<E> removeEach(E element);

    @Override
    @NotNull
    Set<E> removeFirst(E element);

    @Override
    @NotNull
    Set<E> removeFirstWhere(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Set<E> removeFirstWhere(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Set<E> removeLast(E element);

    @Override
    @NotNull
    Set<E> removeLastWhere(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Set<E> removeLastWhere(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Set<E> removeSlice(int start, int end);

    @Override
    @NotNull
    Set<E> removeWhere(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Set<E> removeWhere(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Set<E> replaceAfter(int numElements, E replacement);

    @Override
    @NotNull
    Set<E> replaceEach(E element, E replacement);

    @Override
    @NotNull
    Set<E> replaceFirst(E element, E replacement);

    @Override
    @NotNull
    Set<E> replaceFirstWhere(@NotNull IndexedPredicate<? super E> predicate, E replacement);

    @Override
    @NotNull
    Set<E> replaceFirstWhere(@NotNull Predicate<? super E> predicate, E replacement);

    @Override
    @NotNull
    Set<E> replaceLast(E element, E replacement);

    @Override
    @NotNull
    Set<E> replaceLastWhere(@NotNull IndexedPredicate<? super E> predicate, E replacement);

    @Override
    @NotNull
    Set<E> replaceLastWhere(@NotNull Predicate<? super E> predicate, E replacement);

    @Override
    @NotNull
    Set<E> replaceSlice(int start, int end, @NotNull Iterable<? extends E> patch);

    @Override
    @NotNull
    Set<E> replaceWhere(@NotNull IndexedPredicate<? super E> predicate, E replacement);

    @Override
    @NotNull
    Set<E> replaceWhere(@NotNull Predicate<? super E> predicate, E replacement);

    @Override
    @NotNull
    Set<E> resizeTo(int numElements, E padding);

    @Override
    @NotNull
    Set<E> slice(int start);

    @Override
    @NotNull
    Set<E> slice(int start, int end);

    @Override
    @NotNull
    Set<Boolean> startsWith(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    Set<E> take(int maxElements);

    @Override
    @NotNull
    Set<E> takeRight(int maxElements);

    @Override
    @NotNull
    Set<E> takeRightWhile(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Set<E> takeRightWhile(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Set<E> takeWhile(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Set<E> takeWhile(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Set<E> union(@NotNull Iterable<? extends E> elements);
  }

  public interface Stream<E> extends java.util.Iterator<E>, Sequence<E> {

    @NotNull
    Stream<E> append(E element);

    @NotNull
    Stream<E> appendAll(@NotNull Iterable<? extends E> elements);

    @Override
    @NotNull
    <F> Stream<F> as();

    @Override
    @NotNull
    Stream<Integer> count();

    @Override
    @NotNull
    Stream<Integer> count(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Stream<Integer> count(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Stream<E> diff(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    Stream<E> drop(int maxElements);

    @Override
    @NotNull
    Stream<E> dropRight(int maxElements);

    @Override
    @NotNull
    Stream<E> dropRightWhile(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Stream<E> dropRightWhile(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Stream<E> dropWhile(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Stream<E> dropWhile(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Stream<Boolean> each(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Stream<Boolean> each(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Stream<Boolean> endsWith(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    Stream<Boolean> exists(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Stream<Boolean> exists(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Stream<E> filter(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Stream<E> filter(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Stream<E> findAny(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Stream<E> findAny(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Stream<E> findFirst(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Stream<E> findFirst(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Stream<Integer> findIndexOf(Object element);

    @Override
    @NotNull
    Stream<Integer> findIndexOfSlice(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    Stream<Integer> findIndexWhere(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Stream<Integer> findIndexWhere(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Stream<E> findLast(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Stream<E> findLast(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Stream<Integer> findLastIndexOf(Object element);

    @Override
    @NotNull
    Stream<Integer> findLastIndexOfSlice(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    Stream<Integer> findLastIndexWhere(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Stream<Integer> findLastIndexWhere(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    <F> Stream<F> flatMap(@NotNull Function<? super E, ? extends Iterable<F>> mapper);

    @Override
    @NotNull
    <F> Stream<F> flatMap(@NotNull IndexedFunction<? super E, ? extends Iterable<F>> mapper);

    @Override
    @NotNull
    Stream<E> flatMapAfter(int numElements,
        @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    Stream<E> flatMapAfter(int numElements,
        @NotNull IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    Stream<E> flatMapFirstWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    Stream<E> flatMapFirstWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    Stream<E> flatMapLastWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    Stream<E> flatMapLastWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    Stream<E> flatMapWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    Stream<E> flatMapWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends Iterable<? extends E>> mapper);

    @Override
    @NotNull
    <F> Stream<F> fold(F identity,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    @NotNull
    <F> Stream<F> foldLeft(F identity,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    @NotNull
    <F> Stream<F> foldRight(F identity,
        @NotNull BinaryFunction<? super E, ? super F, ? extends F> operation);

    @Override
    @NotNull
    Stream<? extends Stream<E>> group(int maxSize);

    @Override
    @NotNull
    Stream<? extends Stream<E>> group(int size, E padding);

    @Override
    @NotNull
    Stream<Boolean> includes(Object element);

    @Override
    @NotNull
    Stream<Boolean> includesAll(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    Stream<Boolean> includesSlice(@NotNull Iterable<?> elements);

    @NotNull
    Stream<E> insert(E element);

    @NotNull
    Stream<E> insertAfter(int numElements, E element);

    @NotNull
    Stream<E> insertAll(@NotNull Iterable<? extends E> elements);

    @NotNull
    Stream<E> insertAllAfter(int numElements, @NotNull Iterable<? extends E> elements);

    @Override
    @NotNull
    Stream<E> intersect(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    Stream<E> iterator();

    @Override
    @NotNull
    <F> Stream<F> map(@NotNull Function<? super E, F> mapper);

    @Override
    @NotNull
    <F> Stream<F> map(@NotNull IndexedFunction<? super E, F> mapper);

    @Override
    @NotNull
    Stream<E> mapAfter(int numElements, @NotNull Function<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Stream<E> mapAfter(int numElements, @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Stream<E> mapFirstWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Stream<E> mapFirstWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Stream<E> mapLastWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Stream<E> mapLastWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Stream<E> mapWhere(@NotNull IndexedPredicate<? super E> predicate,
        @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Stream<E> mapWhere(@NotNull Predicate<? super E> predicate,
        @NotNull Function<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Stream<E> max(@NotNull Comparator<? super E> comparator);

    @Override
    @NotNull
    Stream<E> min(@NotNull Comparator<? super E> comparator);

    @Override
    @NotNull
    Stream<Boolean> none(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Stream<Boolean> none(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Stream<Boolean> notAll(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Stream<Boolean> notAll(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Stream<E> orElse(@NotNull Iterable<E> elements);

    @Override
    @NotNull
    Stream<E> orElseGet(@NotNull Supplier<? extends Iterable<? extends E>> supplier);

    @Override
    @NotNull
    Stream<E> plus(E element);

    @Override
    @NotNull
    Stream<E> plusAll(@NotNull Iterable<E> elements);

    @Override
    @NotNull
    Stream<E> reduce(@NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    Stream<E> reduceLeft(@NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    Stream<E> reduceRight(@NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    Stream<E> removeAfter(int numElements);

    @Override
    @NotNull
    Stream<E> removeEach(E element);

    @Override
    @NotNull
    Stream<E> removeFirst(E element);

    @Override
    @NotNull
    Stream<E> removeFirstWhere(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Stream<E> removeFirstWhere(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Stream<E> removeLast(E element);

    @Override
    @NotNull
    Stream<E> removeLastWhere(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Stream<E> removeLastWhere(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Stream<E> removeSlice(int start, int end);

    @Override
    @NotNull
    Stream<E> removeWhere(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Stream<E> removeWhere(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Stream<E> replaceAfter(int numElements, E replacement);

    @Override
    @NotNull
    Stream<E> replaceEach(E element, E replacement);

    @Override
    @NotNull
    Stream<E> replaceFirst(E element, E replacement);

    @Override
    @NotNull
    Stream<E> replaceFirstWhere(@NotNull IndexedPredicate<? super E> predicate, E replacement);

    @Override
    @NotNull
    Stream<E> replaceFirstWhere(@NotNull Predicate<? super E> predicate, E replacement);

    @Override
    @NotNull
    Stream<E> replaceLast(E element, E replacement);

    @Override
    @NotNull
    Stream<E> replaceLastWhere(@NotNull IndexedPredicate<? super E> predicate, E replacement);

    @Override
    @NotNull
    Stream<E> replaceLastWhere(@NotNull Predicate<? super E> predicate, E replacement);

    @Override
    @NotNull
    Stream<E> replaceSlice(int start, int end, @NotNull Iterable<? extends E> patch);

    @Override
    @NotNull
    Stream<E> replaceWhere(@NotNull IndexedPredicate<? super E> predicate, E replacement);

    @Override
    @NotNull
    Stream<E> replaceWhere(@NotNull Predicate<? super E> predicate, E replacement);

    @Override
    @NotNull
    Stream<E> resizeTo(int numElements, E padding);

    @Override
    @NotNull
    Stream<E> slice(int start);

    @Override
    @NotNull
    Stream<E> slice(int start, int end);

    @Override
    @NotNull
    Stream<Boolean> startsWith(@NotNull Iterable<?> elements);

    @Override
    @NotNull
    Stream<E> take(int maxElements);

    @Override
    @NotNull
    Stream<E> takeRight(int maxElements);

    @Override
    @NotNull
    Stream<E> takeRightWhile(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Stream<E> takeRightWhile(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Stream<E> takeWhile(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Stream<E> takeWhile(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Stream<E> union(@NotNull Iterable<? extends E> elements);
  }
}
