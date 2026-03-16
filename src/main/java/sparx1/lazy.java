/*
 * Copyright 2026 Davide Maestroni
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
package sparx1;

import java.util.Comparator;
import sparx1.util.ZipEntry;
import sparx1.util.annotation.NotNegative;
import sparx1.util.annotation.NotNull;
import sparx1.util.annotation.Nullable;
import sparx1.util.annotation.Positive;
import sparx1.util.function.Action;
import sparx1.util.function.BinaryFunction;
import sparx1.util.function.Consumer;
import sparx1.util.function.Function;
import sparx1.util.function.IndexedConsumer;
import sparx1.util.function.IndexedFunction;
import sparx1.util.function.IndexedPredicate;
import sparx1.util.function.Predicate;
import sparx1.util.function.Supplier;

public class lazy {

  private lazy() {
  }

  static int getKnownSize(final java.lang.Iterable<?> elements) {
//    if (elements instanceof List) {
//      return ((List<?>) elements).knownSize();
//    }
    if (elements instanceof LazyIterator) {
      return ((LazyIterator<?>) elements).knownSize();
    }
    if (elements instanceof java.util.Collection) {
      return ((java.util.Collection<?>) elements).size();
    }
    return -1;
  }

  public abstract static class Iterator<E> extends AbstractIterator<E, Iterator<E>> implements
      itf.Iterator<E, Iterator<E>> {

    public static @NotNull <E> Iterator<E> of() {
      return LazyIterator.emptyIterator();
    }

    @Override
    public abstract @NotNull Iterator<E> append(@Nullable E element);

    @Override
    public abstract @NotNull Iterator<E> appendAll(
        @NotNull java.lang.Iterable<? extends E> elements);

    @Override
    public abstract @NotNull <F> Iterator<F> apply(
        @NotNull Function<? super Iterator<E>, java.lang.Iterable<F>> function);

    @Override
    public abstract @NotNull <F> Iterator<F> cast();

    @Override
    public abstract @NotNull Iterator<Integer> count();

    @Override
    public abstract @NotNull Iterator<E> diff(@NotNull java.lang.Iterable<?> elements);

    @Override
    public abstract @NotNull Iterator<E> distinct();

    @Override
    public abstract @NotNull <K> Iterator<E> distinctBy(
        @NotNull Function<? super E, K> keyExtractor);

    @Override
    public abstract @NotNull <K> Iterator<E> distinctBy(
        @NotNull IndexedFunction<? super E, K> keyExtractor);

    @Override
    public abstract @NotNull Iterator<E> doAfter(@NotNull Action action);

    @Override
    public abstract @NotNull Iterator<E> doFinally(@NotNull Action action);

    @Override
    public abstract @NotNull Iterator<E> dropFirst(int maxElements);

    @Override
    public abstract @NotNull Iterator<E> dropFirstWhile(
        @NotNull IndexedPredicate<? super E> condition);

    @Override
    public abstract @NotNull Iterator<E> dropFirstWhile(@NotNull Predicate<? super E> condition);

    @Override
    public abstract @NotNull Iterator<E> dropLast(int maxElements);

    @Override
    public abstract @NotNull Iterator<Boolean> endsWith(@NotNull java.lang.Iterable<?> elements);

    @Override
    public abstract @NotNull Iterator<Boolean> exists(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    public abstract @NotNull Iterator<Boolean> exists(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @Override
    public abstract @NotNull Iterator<Boolean> existsForward(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    public abstract @NotNull Iterator<Boolean> existsForward(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @Override
    public abstract @NotNull Iterator<E> filter(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    public abstract @NotNull Iterator<E> filter(@NotNull Predicate<? super E> predicate);

    @Override
    public abstract @NotNull Iterator<E> filterWhile(@NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    public abstract @NotNull Iterator<E> filterWhile(@NotNull Predicate<? super E> condition,
        @NotNull Predicate<? super E> predicate);

    @Override
    public abstract @NotNull Iterator<E> find(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    public abstract @NotNull Iterator<E> find(@NotNull Predicate<? super E> predicate);

    @Override
    public abstract @NotNull Iterator<E> findFirst(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    public abstract @NotNull Iterator<E> findFirst(@NotNull Predicate<? super E> predicate);

    @Override
    public abstract @NotNull Iterator<Integer> findFirstIndex(
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    public abstract @NotNull Iterator<Integer> findFirstIndex(
        @NotNull Predicate<? super E> predicate);

    @Override
    public abstract @NotNull Iterator<Integer> findFirstIndexOf(@Nullable Object element);

    @Override
    public abstract @NotNull Iterator<Integer> findFirstIndexOfSequence(
        @NotNull java.lang.Iterable<?> elements);

    @Override
    public abstract @NotNull Iterator<Integer> findIndex(
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    public abstract @NotNull Iterator<Integer> findIndex(@NotNull Predicate<? super E> predicate);

    @Override
    public abstract @NotNull Iterator<Integer> findIndexOf(@Nullable Object element);

    @Override
    public abstract @NotNull Iterator<E> findLast(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    public abstract @NotNull Iterator<E> findLast(@NotNull Predicate<? super E> predicate);

    @Override
    public abstract @NotNull Iterator<Integer> findLastIndex(
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    public abstract @NotNull Iterator<Integer> findLastIndex(
        @NotNull Predicate<? super E> predicate);

    @Override
    public abstract @NotNull Iterator<Integer> findLastIndexOf(@Nullable Object element);

    @Override
    public abstract @NotNull Iterator<Integer> findLastIndexOfSequence(
        @NotNull java.lang.Iterable<?> elements);

    @Override
    public abstract @NotNull <F> Iterator<F> flatMap(
        @NotNull Function<? super E, ? extends java.lang.Iterable<F>> mapper);

    @Override
    public abstract @NotNull <F> Iterator<F> flatMap(
        @NotNull IndexedFunction<? super E, ? extends java.lang.Iterable<F>> mapper);

    @Override
    public abstract @NotNull Iterator<E> flatMapWhile(
        @NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedFunction<? super E, ? extends java.lang.Iterable<? extends E>> mapper);

    @Override
    public abstract @NotNull Iterator<E> flatMapWhile(@NotNull Predicate<? super E> condition,
        @NotNull Function<? super E, ? extends java.lang.Iterable<? extends E>> mapper);

    @Override
    public abstract @NotNull <F> Iterator<F> fold(F identity,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    public abstract @NotNull <F> Iterator<F> foldForward(F identity,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    public abstract @NotNull <F> Iterator<F> foldWhile(F identity,
        @NotNull Predicate<? super F> condition,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    public abstract @NotNull <F> Iterator<F> foldWhileForward(F identity,
        @NotNull Predicate<? super F> condition,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    public abstract @NotNull Iterator<Boolean> includes(@Nullable Object element);

    @Override
    public abstract @NotNull Iterator<Boolean> includesAll(@NotNull java.lang.Iterable<?> elements);

    @Override
    public abstract @NotNull Iterator<Boolean> includesSequence(
        @NotNull java.lang.Iterable<?> elements);

    @Override
    public abstract @NotNull Iterator<E> insert(@Nullable E element);

    @Override
    public abstract @NotNull Iterator<E> insertAfter(int numElements, @Nullable E element);

    @Override
    public abstract @NotNull Iterator<E> insertAll(
        @NotNull java.lang.Iterable<? extends E> elements);

    @Override
    public abstract @NotNull Iterator<E> insertAllAfter(int numElements,
        @NotNull java.lang.Iterable<? extends E> elements);

    @Override
    public abstract @NotNull Iterator<E> interleave(
        @NotNull java.lang.Iterable<? extends E> elements);

    @Override
    public abstract @NotNull Iterator<E> interleaveInner(
        @NotNull java.lang.Iterable<? extends E> elements);

    @Override
    public abstract @NotNull Iterator<E> interleaveInnerWithPadding(
        @NotNull java.lang.Iterable<? extends E> elements, E paddingLeft, E paddingRight);

    @Override
    public abstract @NotNull Iterator<E> interleaveWithPadding(
        @NotNull java.lang.Iterable<? extends E> elements, E paddingLeft, E paddingRight);

    @Override
    public abstract @NotNull Iterator<E> intersect(@NotNull java.lang.Iterable<?> elements);

    @Override
    public abstract @NotNull Iterator<E> iterator();

    @Override
    public abstract @NotNull <F> Iterator<F> map(@NotNull Function<? super E, F> mapper);

    @Override
    public abstract @NotNull <F> Iterator<F> map(@NotNull IndexedFunction<? super E, F> mapper);

    @Override
    public abstract @NotNull <F> Iterator<F> mapForward(@NotNull Function<? super E, F> mapper);

    @Override
    public abstract @NotNull <F> Iterator<F> mapForward(
        @NotNull IndexedFunction<? super E, F> mapper);

    @Override
    public abstract @NotNull Iterator<E> mapWhile(@NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    public abstract @NotNull Iterator<E> mapWhile(@NotNull Predicate<? super E> condition,
        @NotNull Function<? super E, ? extends E> mapper);

    @Override
    public abstract @NotNull Iterator<E> mapWhileForward(
        @NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    public abstract @NotNull Iterator<E> mapWhileForward(@NotNull Predicate<? super E> condition,
        @NotNull Function<? super E, ? extends E> mapper);

    @Override
    public abstract @NotNull Iterator<E> materialize();

    @Override
    public abstract @NotNull Iterator<E> max(@NotNull Comparator<? super E> comparator);

    @Override
    public abstract @NotNull Iterator<E> min(@NotNull Comparator<? super E> comparator);

    @Override
    public abstract @NotNull Iterator<E> minus(@Nullable E element);

    @Override
    public abstract @NotNull Iterator<E> minusAll(
        @NotNull java.lang.Iterable<? extends E> elements);

    @Override
    public abstract @NotNull Iterator<E> minusFirst(@Nullable E element);

    @Override
    public abstract @NotNull Iterator<E> minusLast(@Nullable E element);

    @Override
    public abstract @NotNull Iterator<Boolean> notExists(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    public abstract @NotNull Iterator<Boolean> notExists(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @Override
    public abstract @NotNull Iterator<Boolean> notExistsForward(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    public abstract @NotNull Iterator<Boolean> notExistsForward(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @Override
    public abstract @NotNull Iterator<E> orElse(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    public abstract @NotNull Iterator<E> orElseGet(
        @NotNull Supplier<? extends java.lang.Iterable<? extends E>> supplier);

    @Override
    public abstract @NotNull Iterator<E> peek(@NotNull Consumer<? super E> consumer);

    @Override
    public abstract @NotNull Iterator<E> peek(@NotNull IndexedConsumer<? super E> consumer);

    @Override
    public abstract @NotNull Iterator<E> peekExceptionally(
        @NotNull Consumer<? super Throwable> consumer);

    @Override
    public abstract @NotNull Iterator<E> peekExceptionally(
        @NotNull IndexedConsumer<? super Throwable> consumer);

    @Override
    public abstract @NotNull Iterator<E> plus(@Nullable E element);

    @Override
    public abstract @NotNull Iterator<E> plusAll(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    public abstract @NotNull Iterator<E> reduce(
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    public abstract @NotNull Iterator<E> reduceForward(
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    public abstract @NotNull Iterator<E> reduceWhile(@NotNull Predicate<? super E> condition,
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    public abstract @NotNull Iterator<E> reduceWhileForward(@NotNull Predicate<? super E> condition,
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    public abstract @NotNull Iterator<E> removeFirst(
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    public abstract @NotNull Iterator<E> removeFirst(@NotNull Predicate<? super E> predicate);

    @Override
    public abstract @NotNull Iterator<E> removeFirstSequence(
        @NotNull java.lang.Iterable<?> elements);

    @Override
    public abstract @NotNull Iterator<E> removeLast(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    public abstract @NotNull Iterator<E> removeLast(@NotNull Predicate<? super E> predicate);

    @Override
    public abstract @NotNull Iterator<E> removeLastSequence(
        @NotNull java.lang.Iterable<?> elements);

    @Override
    public abstract @NotNull Iterator<E> removeSequence(@NotNull java.lang.Iterable<?> elements);

    @Override
    public abstract @NotNull Iterator<E> removeSlice(int start, int end);

    @Override
    public abstract @NotNull Iterator<E> replaceFirstSequence(
        @NotNull java.lang.Iterable<?> elements,
        @NotNull Function<? super java.lang.Iterable<E>, java.lang.Iterable<? extends E>> mapper);

    @Override
    public abstract @NotNull Iterator<E> replaceLastSequence(
        @NotNull java.lang.Iterable<?> elements,
        @NotNull Function<? super java.lang.Iterable<E>, java.lang.Iterable<? extends E>> mapper);

    @Override
    public abstract @NotNull Iterator<E> replaceSequence(@NotNull java.lang.Iterable<?> elements,
        @NotNull Function<? super java.lang.Iterable<E>, java.lang.Iterable<? extends E>> mapper);

    @Override
    public abstract @NotNull Iterator<E> replaceSequence(@NotNull java.lang.Iterable<?> elements,
        @NotNull IndexedFunction<? super java.lang.Iterable<E>, java.lang.Iterable<? extends E>> mapper);

    @Override
    public abstract @NotNull Iterator<E> replaceSlice(int start, int end,
        @NotNull java.lang.Iterable<? extends E> patch);

    @Override
    public abstract @NotNull Iterator<E> resizeTo(@NotNegative int numElements, E padding);

    @Override
    public abstract int skip(int maxElements);

    @Override
    public abstract @NotNull Iterator<E> slice(int start);

    @Override
    public abstract @NotNull Iterator<E> slice(int start, int end);

    @Override
    public abstract @NotNull Iterator<? extends Iterator<E>> slidingWindow(@Positive int maxSize,
        @Positive int step);

    @Override
    public abstract @NotNull Iterator<? extends Iterator<E>> slidingWindowWithPadding(
        @Positive int size, @Positive int step, E padding);

    @Override
    public abstract @NotNull Iterator<Boolean> startsWith(@NotNull java.lang.Iterable<?> elements);

    @Override
    public abstract @NotNull <X extends Throwable> Iterator<E> switchExceptionally(
        @NotNull Class<X> exceptionType,
        @NotNull Function<? super X, ? extends java.lang.Iterable<? extends E>> mapper);

    @Override
    public abstract @NotNull <X extends Throwable> Iterator<E> switchExceptionally(
        @NotNull Class<X> exceptionType,
        @NotNull IndexedFunction<? super X, ? extends java.lang.Iterable<? extends E>> mapper);

    @Override
    public abstract @NotNull Iterator<E> switchExceptionally(
        @NotNull Function<? super Throwable, ? extends java.lang.Iterable<? extends E>> mapper);

    @Override
    public abstract @NotNull Iterator<E> switchExceptionally(
        @NotNull IndexedFunction<? super Throwable, ? extends java.lang.Iterable<? extends E>> mapper);

    @Override
    public abstract @NotNull Iterator<E> symmetricDiff(
        @NotNull java.lang.Iterable<? extends E> elements);

    @Override
    public abstract @NotNull Iterator<E> takeFirst(int maxElements);

    @Override
    public abstract @NotNull Iterator<E> takeFirstWhile(
        @NotNull IndexedPredicate<? super E> condition);

    @Override
    public abstract @NotNull Iterator<E> takeFirstWhile(@NotNull Predicate<? super E> condition);

    @Override
    public abstract @NotNull Iterator<E> takeLast(int maxElements);

    @Override
    public @NotNull Iterator<E> toIterator() {
      return this;
    }

    @Override
    public abstract @NotNull Iterator<E> union(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    public abstract @NotNull <F> Iterator<ZipEntry<E, F>> zip(
        @NotNull java.lang.Iterable<F> elements);

    @Override
    public abstract @NotNull <F> Iterator<ZipEntry<E, F>> zipWithPadding(
        @NotNull java.lang.Iterable<F> elements, E paddingLeft, F paddingRight);
  }
}
