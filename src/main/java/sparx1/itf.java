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

public class itf {

  private itf() {
  }

  public interface Collection<E, T extends Collection<E, T>> extends java.util.Collection<E>,
      Iterable<E, T> {

    @Override
    @NotNull
    <F> Collection<F, ? extends Collection<F, ?>> apply(
        @NotNull Function<? super T, java.lang.Iterable<F>> function);

    @Override
    @NotNull
    <F> Collection<F, ? extends Collection<F, ?>> cast();

    @NotNull
    Collection<E, T> clone();

    @NotNull
    Collection<E, T> clone(@NotNull Function<? super E, ? extends E> cloner);

    @Override
    @NotNull
    Collection<Integer, ? extends Collection<Integer, ?>> count();

    @Override
    @NotNull
    Collection<E, T> diff(@NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    Collection<E, T> distinct();

    @Override
    @NotNull
    <K> Collection<E, T> distinctBy(@NotNull Function<? super E, K> keyExtractor);

    @Override
    @NotNull
    <K> Collection<E, T> distinctBy(@NotNull IndexedFunction<? super E, K> keyExtractor);

    @Override
    @NotNull
    Collection<E, T> dropFirst(int maxElements);

    @Override
    @NotNull
    Collection<E, T> dropFirstWhile(@NotNull IndexedPredicate<? super E> condition);

    @Override
    @NotNull
    Collection<E, T> dropFirstWhile(@NotNull Predicate<? super E> condition);

    @Override
    @NotNull
    Collection<E, T> dropLast(int maxElements);

    @NotNull
    Collection<E, T> dropLastWhile(@NotNull IndexedPredicate<? super E> condition);

    @NotNull
    Collection<E, T> dropLastWhile(@NotNull Predicate<? super E> condition);

    @Override
    @NotNull
    Collection<Boolean, ? extends Collection<Boolean, ?>> endsWith(
        @NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    Collection<Boolean, ? extends Collection<Boolean, ?>> exists(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Collection<Boolean, ? extends Collection<Boolean, ?>> exists(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @NotNull
    Collection<Boolean, ? extends Collection<Boolean, ?>> existsBackward(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @NotNull
    Collection<Boolean, ? extends Collection<Boolean, ?>> existsBackward(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Collection<Boolean, ? extends Collection<Boolean, ?>> existsForward(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Collection<Boolean, ? extends Collection<Boolean, ?>> existsForward(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Collection<E, T> filter(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Collection<E, T> filter(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Collection<E, T> filterWhile(@NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Collection<E, T> filterWhile(@NotNull Predicate<? super E> condition,
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Collection<E, T> find(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Collection<E, T> find(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Collection<E, T> findFirst(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Collection<E, T> findFirst(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Collection<Integer, ? extends Collection<Integer, ?>> findFirstIndex(
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Collection<Integer, ? extends Collection<Integer, ?>> findFirstIndex(
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Collection<Integer, ? extends Collection<Integer, ?>> findFirstIndexOf(
        @Nullable Object element);

    @Override
    @NotNull
    Collection<Integer, ? extends Collection<Integer, ?>> findFirstIndexOfSequence(
        @NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    Collection<Integer, ? extends Collection<Integer, ?>> findIndex(
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Collection<Integer, ? extends Collection<Integer, ?>> findIndex(
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Collection<Integer, ? extends Collection<Integer, ?>> findIndexOf(@Nullable Object element);

    @Override
    @NotNull
    Collection<E, T> findLast(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Collection<E, T> findLast(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Collection<Integer, ? extends Collection<Integer, ?>> findLastIndex(
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Collection<Integer, ? extends Collection<Integer, ?>> findLastIndex(
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Collection<Integer, ? extends Collection<Integer, ?>> findLastIndexOf(@Nullable Object element);

    @Override
    @NotNull
    Collection<Integer, ? extends Collection<Integer, ?>> findLastIndexOfSequence(
        @NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    <F> Collection<F, ? extends Collection<F, ?>> flatMap(
        @NotNull Function<? super E, ? extends java.lang.Iterable<F>> mapper);

    @Override
    @NotNull
    <F> Collection<F, ? extends Collection<F, ?>> flatMap(
        @NotNull IndexedFunction<? super E, ? extends java.lang.Iterable<F>> mapper);

    @Override
    @NotNull
    Collection<E, T> flatMapWhile(@NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedFunction<? super E, ? extends java.lang.Iterable<? extends E>> mapper);

    @Override
    @NotNull
    Collection<E, T> flatMapWhile(@NotNull Predicate<? super E> condition,
        @NotNull Function<? super E, ? extends java.lang.Iterable<? extends E>> mapper);

    @Override
    @NotNull
    <F> Collection<F, ? extends Collection<F, ?>> fold(F identity,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @NotNull
    <F> Collection<F, ? extends Collection<F, ?>> foldBackward(F identity,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    @NotNull
    <F> Collection<F, ? extends Collection<F, ?>> foldForward(F identity,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    @NotNull
    <F> Collection<F, ? extends Collection<F, ?>> foldWhile(F identity,
        @NotNull Predicate<? super F> condition,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @NotNull
    <F> Collection<F, ? extends Collection<F, ?>> foldWhileBackward(F identity,
        @NotNull Predicate<? super F> condition,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    @NotNull
    <F> Collection<F, ? extends Collection<F, ?>> foldWhileForward(F identity,
        @NotNull Predicate<? super F> condition,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    @NotNull
    Collection<Boolean, ? extends Collection<Boolean, ?>> includes(@Nullable Object element);

    @Override
    @NotNull
    Collection<Boolean, ? extends Collection<Boolean, ?>> includesAll(
        @NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    Collection<Boolean, ? extends Collection<Boolean, ?>> includesSequence(
        @NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    Collection<E, T> interleave(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    Collection<E, T> interleaveInner(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    Collection<E, T> interleaveInnerWithPadding(@NotNull java.lang.Iterable<? extends E> elements,
        E paddingLeft, E paddingRight);

    @Override
    @NotNull
    Collection<E, T> interleaveWithPadding(@NotNull java.lang.Iterable<? extends E> elements,
        E paddingLeft, E paddingRight);

    @Override
    @NotNull
    Collection<E, T> intersect(@NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    <F> Collection<F, ? extends Collection<F, ?>> map(@NotNull Function<? super E, F> mapper);

    @Override
    @NotNull
    <F> Collection<F, ? extends Collection<F, ?>> map(
        @NotNull IndexedFunction<? super E, F> mapper);

    @NotNull
    <F> Collection<F, ? extends Collection<F, ?>> mapBackward(
        @NotNull Function<? super E, F> mapper);

    @NotNull
    <F> Collection<F, ? extends Collection<F, ?>> mapBackward(
        @NotNull IndexedFunction<? super E, F> mapper);

    @Override
    @NotNull
    <F> Collection<F, ? extends Collection<F, ?>> mapForward(
        @NotNull Function<? super E, F> mapper);

    @Override
    @NotNull
    <F> Collection<F, ? extends Collection<F, ?>> mapForward(
        @NotNull IndexedFunction<? super E, F> mapper);

    @Override
    @NotNull
    Collection<E, ? extends Collection<E, ?>> mapWhile(
        @NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Collection<E, ? extends Collection<E, ?>> mapWhile(@NotNull Predicate<? super E> condition,
        @NotNull Function<? super E, ? extends E> mapper);

    @NotNull
    <F> Collection<F, ? extends Collection<F, ?>> mapWhileBackward(
        @NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedFunction<? super E, F> mapper);

    @NotNull
    <F> Collection<F, ? extends Collection<F, ?>> mapWhileBackward(
        @NotNull Predicate<? super E> condition, @NotNull Function<? super E, F> mapper);

    @Override
    @NotNull
    Collection<E, ? extends Collection<E, ?>> mapWhileForward(
        @NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Collection<E, ? extends Collection<E, ?>> mapWhileForward(
        @NotNull Predicate<? super E> condition, @NotNull Function<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Collection<E, T> materialize();

    @Override
    @NotNull
    Collection<E, T> max(@NotNull Comparator<? super E> comparator);

    @Override
    @NotNull
    Collection<E, T> min(@NotNull Comparator<? super E> comparator);

    @Override
    @NotNull
    Collection<E, T> minus(@Nullable E element);

    @Override
    @NotNull
    Collection<E, T> minusAll(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    Collection<E, T> minusFirst(@Nullable E element);

    @Override
    @NotNull
    Collection<E, T> minusLast(@Nullable E element);

    @Override
    @NotNull
    Collection<Boolean, ? extends Collection<Boolean, ?>> notExists(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Collection<Boolean, ? extends Collection<Boolean, ?>> notExists(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @NotNull
    Collection<Boolean, ? extends Collection<Boolean, ?>> notExistsBackward(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @NotNull
    Collection<Boolean, ? extends Collection<Boolean, ?>> notExistsBackward(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Collection<Boolean, ? extends Collection<Boolean, ?>> notExistsForward(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Collection<Boolean, ? extends Collection<Boolean, ?>> notExistsForward(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Collection<E, T> orElse(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    Collection<E, T> orElseGet(
        @NotNull Supplier<? extends java.lang.Iterable<? extends E>> supplier);

    @Override
    @NotNull
    Iterator<? extends Collection<E, T>, ? extends Iterator<? extends Collection<E, T>, ?>> partition(
        @Positive int numPartitions, @NotNull Function<? super E, Integer> indexExtractor);

    @Override
    @NotNull
    Iterator<? extends Collection<E, T>, ? extends Iterator<? extends Collection<E, T>, ?>> partition(
        @Positive int numPartitions, @NotNull IndexedFunction<? super E, Integer> indexExtractor);

    @Override
    @NotNull
    <K> Iterator<? extends ZipEntry<K, ? extends Collection<E, T>>, ? extends Iterator<? extends ZipEntry<K, ? extends Collection<E, T>>, ?>> partitionZip(
        @NotNull Function<? super E, K> keyExtractor);

    @Override
    @NotNull
    <K> Iterator<? extends ZipEntry<K, ? extends Collection<E, T>>, ? extends Iterator<? extends ZipEntry<K, ? extends Collection<E, T>>, ?>> partitionZip(
        @NotNull IndexedFunction<? super E, K> keyExtractor);

    @Override
    @NotNull
    Collection<E, T> plus(@Nullable E element);

    @Override
    @NotNull
    Collection<E, T> plusAll(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    Collection<E, T> reduce(@NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @NotNull
    Collection<E, T> reduceBackward(
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    Collection<E, T> reduceForward(
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    Collection<E, T> reduceWhile(@NotNull Predicate<? super E> condition,
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @NotNull
    Collection<E, T> reduceWhileBackward(@NotNull Predicate<? super E> condition,
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    Collection<E, T> reduceWhileForward(@NotNull Predicate<? super E> condition,
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    Collection<E, T> removeFirst(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Collection<E, T> removeFirst(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Collection<E, T> removeFirstSequence(@NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    Collection<E, T> removeLast(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Collection<E, T> removeLast(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Collection<E, T> removeLastSequence(@NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    Collection<E, T> removeSequence(@NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    Collection<E, T> removeSlice(int start);

    @Override
    @NotNull
    Collection<E, T> removeSlice(int start, int end);

    @NotNull
    Collection<E, T> replaceFirstSequence(@NotNull java.lang.Iterable<?> elements,
        @NotNull Function<? super java.util.List<E>, java.lang.Iterable<? extends E>> mapper);

    @NotNull
    Collection<E, T> replaceLastSequence(@NotNull java.lang.Iterable<?> elements,
        @NotNull Function<? super java.util.List<E>, java.lang.Iterable<? extends E>> mapper);

    @NotNull
    Collection<E, T> replaceSequence(@NotNull java.lang.Iterable<?> elements,
        @NotNull Function<? super java.util.List<E>, java.lang.Iterable<? extends E>> mapper);

    @NotNull
    Collection<E, T> replaceSequence(@NotNull java.lang.Iterable<?> elements,
        @NotNull IndexedFunction<? super java.util.List<E>, java.lang.Iterable<? extends E>> mapper);

    @Override
    @NotNull
    Collection<E, T> replaceSlice(int start, int end,
        @NotNull java.lang.Iterable<? extends E> patch);

    @Override
    @NotNull
    Collection<E, T> replaceSlice(int start, @NotNull java.lang.Iterable<? extends E> patch);

    @Override
    @NotNull
    Collection<E, T> resizeTo(@NotNegative int numElements, E padding);

    @Override
    @NotNull
    Collection<E, T> slice(int start);

    @Override
    @NotNull
    Collection<E, T> slice(int start, int end);

    @Override
    @NotNull
    Collection<? extends Collection<E, T>, ? extends Collection<? extends Collection<E, T>, ?>> slidingWindow(
        @Positive int maxSize, @Positive int step);

    @Override
    @NotNull
    Collection<? extends Collection<E, T>, ? extends Collection<? extends Collection<E, T>, ?>> slidingWindowWithPadding(
        @Positive int size, @Positive int step, E padding);

    @Override
    @NotNull
    Collection<Boolean, ? extends Collection<Boolean, ?>> startsWith(
        @NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    Collection<E, T> symmetricDiff(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    Collection<E, T> takeFirst(int maxElements);

    @Override
    @NotNull
    Collection<E, T> takeFirstWhile(@NotNull IndexedPredicate<? super E> condition);

    @Override
    @NotNull
    Collection<E, T> takeFirstWhile(@NotNull Predicate<? super E> condition);

    @Override
    @NotNull
    Collection<E, T> takeLast(int maxElements);

    @NotNull
    Collection<E, T> takeLastWhile(@NotNull IndexedPredicate<? super E> condition);

    @NotNull
    Collection<E, T> takeLastWhile(@NotNull Predicate<? super E> condition);

    @Override
    @NotNull
    Collection<E, T> union(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    <F> Collection<ZipEntry<E, F>, ? extends Collection<ZipEntry<E, F>, ?>> zip(
        @NotNull java.lang.Iterable<F> elements);

    @Override
    @NotNull
    <F> Collection<ZipEntry<E, F>, ? extends Collection<ZipEntry<E, F>, ?>> zipWithPadding(
        @NotNull java.lang.Iterable<F> elements, E paddingLeft, F paddingRight);
  }

  public interface Future<E, T> extends java.util.concurrent.Future<T> {

    @NotNull
    java.util.concurrent.Future<?> doForFuture(@NotNull Consumer<? super E> elementConsumer);

    @NotNull
    java.util.concurrent.Future<?> doForFuture(@NotNull Consumer<? super E> elementConsumer,
        @NotNull Action endAction);

    @NotNull
    java.util.concurrent.Future<?> doForFuture(@NotNull Consumer<? super E> elementConsumer,
        @NotNull Action endAction, @NotNull Consumer<? super Throwable> errorConsumer);

    @NotNull
    java.util.concurrent.Future<?> doForFuture(@NotNull IndexedConsumer<? super E> elementConsumer);

    @NotNull
    java.util.concurrent.Future<?> doForFuture(@NotNull IndexedConsumer<? super E> elementConsumer,
        @NotNull Consumer<? super Integer> endConsumer);

    @NotNull
    java.util.concurrent.Future<?> doForFuture(@NotNull IndexedConsumer<? super E> elementConsumer,
        @NotNull Consumer<? super Integer> endConsumer,
        @NotNull IndexedConsumer<? super Throwable> errorConsumer);

    @NotNull
    java.util.concurrent.Future<?> doWhileFuture(
        @NotNull IndexedPredicate<? super E> elementPredicate);

    @NotNull
    java.util.concurrent.Future<?> doWhileFuture(
        @NotNull IndexedPredicate<? super E> elementPredicate,
        @NotNull Consumer<? super Integer> endConsumer);

    @NotNull
    java.util.concurrent.Future<?> doWhileFuture(
        @NotNull IndexedPredicate<? super E> elementPredicate,
        @NotNull Consumer<? super Integer> endConsumer,
        @NotNull IndexedConsumer<? super Throwable> errorConsumer);

    @NotNull
    java.util.concurrent.Future<?> doWhileFuture(@NotNull Predicate<? super E> elementPredicate);

    @NotNull
    java.util.concurrent.Future<?> doWhileFuture(@NotNull Predicate<? super E> elementPredicate,
        @NotNull Action endAction);

    @NotNull
    java.util.concurrent.Future<?> doWhileFuture(@NotNull Predicate<? super E> elementPredicate,
        @NotNull Action endAction, @NotNull Consumer<? super Throwable> errorConsumer);

    @NotNull
    java.util.concurrent.Future<?> getFuture();

    @NotNull
    java.util.concurrent.Future<?> getFuture(@NotNull Action endAction);

    @NotNull
    java.util.concurrent.Future<?> getFuture(@NotNull Action endAction,
        @NotNull Consumer<? super Throwable> errorConsumer);

    boolean isFailed();

    boolean isSucceeded();
  }

  public interface Iterable<E, T extends Iterable<E, T>> extends java.lang.Iterable<E> {

    @NotNull
    <F> Iterable<F, ? extends Iterable<F, ?>> apply(
        @NotNull Function<? super T, java.lang.Iterable<F>> function);

    @NotNull
    <F> Iterable<F, ? extends Iterable<F, ?>> cast();

    @NotNull
    Iterable<Integer, ? extends Iterable<Integer, ?>> count();

    @NotNull
    Iterable<E, T> diff(@NotNull java.lang.Iterable<?> elements);

    @NotNull
    Iterable<E, T> distinct();

    @NotNull
    <K> Iterable<E, T> distinctBy(@NotNull Function<? super E, K> keyExtractor);

    @NotNull
    <K> Iterable<E, T> distinctBy(@NotNull IndexedFunction<? super E, K> keyExtractor);

    void doFor(@NotNull Consumer<? super E> elementConsumer);

    void doFor(@NotNull Consumer<? super E> elementConsumer, @NotNull Action endAction);

    void doFor(@NotNull Consumer<? super E> elementConsumer, @NotNull Action endAction,
        @NotNull Consumer<? super Throwable> errorConsumer);

    void doFor(@NotNull IndexedConsumer<? super E> elementConsumer);

    void doFor(@NotNull IndexedConsumer<? super E> elementConsumer,
        @NotNull Consumer<? super Integer> endConsumer);

    void doFor(@NotNull IndexedConsumer<? super E> elementConsumer,
        @NotNull Consumer<? super Integer> endConsumer,
        @NotNull IndexedConsumer<? super Throwable> errorConsumer);

    void doWhile(@NotNull IndexedPredicate<? super E> elementPredicate);

    void doWhile(@NotNull IndexedPredicate<? super E> elementPredicate,
        @NotNull Consumer<? super Integer> endConsumer);

    void doWhile(@NotNull IndexedPredicate<? super E> elementPredicate,
        @NotNull Consumer<? super Integer> endConsumer,
        @NotNull IndexedConsumer<? super Throwable> errorConsumer);

    void doWhile(@NotNull Predicate<? super E> elementPredicate);

    void doWhile(@NotNull Predicate<? super E> elementPredicate, @NotNull Action endAction);

    void doWhile(@NotNull Predicate<? super E> elementPredicate, @NotNull Action endAction,
        @NotNull Consumer<? super Throwable> errorConsumer);

    @NotNull
    Iterable<E, T> dropFirst(int maxElements);

    @NotNull
    Iterable<E, T> dropFirstWhile(@NotNull IndexedPredicate<? super E> condition);

    @NotNull
    Iterable<E, T> dropFirstWhile(@NotNull Predicate<? super E> condition);

    @NotNull
    Iterable<E, T> dropLast(int maxElements);

    @NotNull
    Iterable<Boolean, ? extends Iterable<Boolean, ?>> endsWith(
        @NotNull java.lang.Iterable<?> elements);

    @NotNull
    Iterable<Boolean, ? extends Iterable<Boolean, ?>> exists(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @NotNull
    Iterable<Boolean, ? extends Iterable<Boolean, ?>> exists(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @NotNull
    Iterable<Boolean, ? extends Iterable<Boolean, ?>> existsForward(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @NotNull
    Iterable<Boolean, ? extends Iterable<Boolean, ?>> existsForward(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @NotNull
    Iterable<E, T> filter(@NotNull IndexedPredicate<? super E> predicate);

    @NotNull
    Iterable<E, T> filter(@NotNull Predicate<? super E> predicate);

    @NotNull
    Iterable<E, T> filterWhile(@NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedPredicate<? super E> predicate);

    @NotNull
    Iterable<E, T> filterWhile(@NotNull Predicate<? super E> condition,
        @NotNull Predicate<? super E> predicate);

    @NotNull
    Iterable<E, T> find(@NotNull IndexedPredicate<? super E> predicate);

    @NotNull
    Iterable<E, T> find(@NotNull Predicate<? super E> predicate);

    @NotNull
    Iterable<E, T> findFirst(@NotNull IndexedPredicate<? super E> predicate);

    @NotNull
    Iterable<E, T> findFirst(@NotNull Predicate<? super E> predicate);

    @NotNull
    Iterable<E, T> findLast(@NotNull IndexedPredicate<? super E> predicate);

    @NotNull
    Iterable<E, T> findLast(@NotNull Predicate<? super E> predicate);

    @NotNull
    Iterable<Integer, ? extends Iterable<Integer, ?>> findFirstIndex(
        @NotNull IndexedPredicate<? super E> predicate);

    @NotNull
    Iterable<Integer, ? extends Iterable<Integer, ?>> findFirstIndex(
        @NotNull Predicate<? super E> predicate);

    @NotNull
    Iterable<Integer, ? extends Iterable<Integer, ?>> findIndex(
        @NotNull IndexedPredicate<? super E> predicate);

    @NotNull
    Iterable<Integer, ? extends Iterable<Integer, ?>> findIndex(
        @NotNull Predicate<? super E> predicate);

    @NotNull
    Iterable<Integer, ? extends Iterable<Integer, ?>> findLastIndex(
        @NotNull IndexedPredicate<? super E> predicate);

    @NotNull
    Iterable<Integer, ? extends Iterable<Integer, ?>> findLastIndex(
        @NotNull Predicate<? super E> predicate);

    @NotNull
    Iterable<Integer, ? extends Iterable<Integer, ?>> findFirstIndexOf(@Nullable Object element);

    @NotNull
    Iterable<Integer, ? extends Iterable<Integer, ?>> findIndexOf(@Nullable Object element);

    @NotNull
    Iterable<Integer, ? extends Iterable<Integer, ?>> findLastIndexOf(@Nullable Object element);

    @NotNull
    Iterable<Integer, ? extends Iterable<Integer, ?>> findFirstIndexOfSequence(
        @NotNull java.lang.Iterable<?> elements);

    @NotNull
    Iterable<Integer, ? extends Iterable<Integer, ?>> findLastIndexOfSequence(
        @NotNull java.lang.Iterable<?> elements);

    E first();

    @NotNull
    <F> Iterable<F, ? extends Iterable<F, ?>> flatMap(
        @NotNull Function<? super E, ? extends java.lang.Iterable<F>> mapper);

    @NotNull
    <F> Iterable<F, ? extends Iterable<F, ?>> flatMap(
        @NotNull IndexedFunction<? super E, ? extends java.lang.Iterable<F>> mapper);

    @NotNull
    Iterable<E, T> flatMapWhile(@NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedFunction<? super E, ? extends java.lang.Iterable<? extends E>> mapper);

    @NotNull
    Iterable<E, T> flatMapWhile(@NotNull Predicate<? super E> condition,
        @NotNull Function<? super E, ? extends java.lang.Iterable<? extends E>> mapper);

    @NotNull
    <F> Iterable<F, ? extends Iterable<F, ?>> fold(F identity,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @NotNull
    <F> Iterable<F, ? extends Iterable<F, ?>> foldForward(F identity,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @NotNull
    <F> Iterable<F, ? extends Iterable<F, ?>> foldWhile(F identity,
        @NotNull Predicate<? super F> condition,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @NotNull
    <F> Iterable<F, ? extends Iterable<F, ?>> foldWhileForward(F identity,
        @NotNull Predicate<? super F> condition,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @NotNull
    Iterable<Boolean, ? extends Iterable<Boolean, ?>> includes(@Nullable Object element);

    @NotNull
    Iterable<Boolean, ? extends Iterable<Boolean, ?>> includesAll(
        @NotNull java.lang.Iterable<?> elements);

    @NotNull
    Iterable<Boolean, ? extends Iterable<Boolean, ?>> includesSequence(
        @NotNull java.lang.Iterable<?> elements);

    @NotNull
    Iterable<E, T> interleave(@NotNull java.lang.Iterable<? extends E> elements);

    @NotNull
    Iterable<E, T> interleaveInner(@NotNull java.lang.Iterable<? extends E> elements);

    @NotNull
    Iterable<E, T> interleaveInnerWithPadding(@NotNull java.lang.Iterable<? extends E> elements,
        E paddingLeft, E paddingRight);

    @NotNull
    Iterable<E, T> interleaveWithPadding(@NotNull java.lang.Iterable<? extends E> elements,
        E paddingLeft, E paddingRight);

    @NotNull
    Iterable<E, T> intersect(@NotNull java.lang.Iterable<?> elements);

    boolean isDefinite();

    boolean isEmpty();

    boolean isLazy();

    boolean isMutable();

    boolean isNotEmpty();

    boolean isOrdered();

    boolean isSorted();

    boolean isTraversableAgain();

    @Override
    @NotNull
    Iterator<E, ? extends Iterator<E, ?>> iterator();

    E last();

    @NotNull
    <F> Iterable<F, ? extends Iterable<F, ?>> map(@NotNull Function<? super E, F> mapper);

    @NotNull
    <F> Iterable<F, ? extends Iterable<F, ?>> map(@NotNull IndexedFunction<? super E, F> mapper);

    @NotNull
    <F> Iterable<F, ? extends Iterable<F, ?>> mapForward(@NotNull Function<? super E, F> mapper);

    @NotNull
    <F> Iterable<F, ? extends Iterable<F, ?>> mapForward(
        @NotNull IndexedFunction<? super E, F> mapper);

    @NotNull
    Iterable<E, ? extends Iterable<E, ?>> mapWhile(@NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @NotNull
    Iterable<E, ? extends Iterable<E, ?>> mapWhile(@NotNull Predicate<? super E> condition,
        @NotNull Function<? super E, ? extends E> mapper);

    @NotNull
    Iterable<E, ? extends Iterable<E, ?>> mapWhileForward(
        @NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @NotNull
    Iterable<E, ? extends Iterable<E, ?>> mapWhileForward(@NotNull Predicate<? super E> condition,
        @NotNull Function<? super E, ? extends E> mapper);

    @NotNull
    Iterable<E, T> materialize();

    @NotNull
    Iterable<E, T> max(@NotNull Comparator<? super E> comparator);

    @NotNull
    Iterable<E, T> min(@NotNull Comparator<? super E> comparator);

    @NotNull
    Iterable<E, T> minus(@Nullable E element);

    @NotNull
    Iterable<E, T> minusAll(@NotNull java.lang.Iterable<? extends E> elements);

    @NotNull
    Iterable<E, T> minusFirst(@Nullable E element);

    @NotNull
    Iterable<E, T> minusLast(@Nullable E element);

    @NotNull
    Iterable<Boolean, ? extends Iterable<Boolean, ?>> notExists(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @NotNull
    Iterable<Boolean, ? extends Iterable<Boolean, ?>> notExists(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @NotNull
    Iterable<Boolean, ? extends Iterable<Boolean, ?>> notExistsForward(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @NotNull
    Iterable<Boolean, ? extends Iterable<Boolean, ?>> notExistsForward(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @NotNull
    Iterable<E, T> orElse(@NotNull java.lang.Iterable<? extends E> elements);

    @NotNull
    Iterable<E, T> orElseGet(@NotNull Supplier<? extends java.lang.Iterable<? extends E>> supplier);

    @NotNull
    Iterator<? extends Iterable<E, T>, ? extends Iterator<? extends Iterable<E, T>, ?>> partition(
        @Positive int numPartitions, @NotNull Function<? super E, Integer> indexExtractor);

    @NotNull
    Iterator<? extends Iterable<E, T>, ? extends Iterator<? extends Iterable<E, T>, ?>> partition(
        @Positive int numPartitions, @NotNull IndexedFunction<? super E, Integer> indexExtractor);

    @NotNull
    <K> Iterator<? extends ZipEntry<K, ? extends Iterable<E, T>>, ? extends Iterator<? extends ZipEntry<K, ? extends Iterable<E, T>>, ?>> partitionZip(
        @NotNull Function<? super E, K> keyExtractor);

    @NotNull
    <K> Iterator<? extends ZipEntry<K, ? extends Iterable<E, T>>, ? extends Iterator<? extends ZipEntry<K, ? extends Iterable<E, T>>, ?>> partitionZip(
        @NotNull IndexedFunction<? super E, K> keyExtractor);

    @NotNull
    Iterable<E, T> plus(@Nullable E element);

    @NotNull
    Iterable<E, T> plusAll(@NotNull java.lang.Iterable<? extends E> elements);

    @NotNull
    Iterable<E, T> reduce(@NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @NotNull
    Iterable<E, T> reduceForward(
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @NotNull
    Iterable<E, T> reduceWhile(@NotNull Predicate<? super E> condition,
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @NotNull
    Iterable<E, T> reduceWhileForward(@NotNull Predicate<? super E> condition,
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @NotNull
    Iterable<E, T> removeFirst(@NotNull IndexedPredicate<? super E> predicate);

    @NotNull
    Iterable<E, T> removeFirst(@NotNull Predicate<? super E> predicate);

    @NotNull
    Iterable<E, T> removeFirstSequence(@NotNull java.lang.Iterable<?> elements);

    @NotNull
    Iterable<E, T> removeLast(@NotNull IndexedPredicate<? super E> predicate);

    @NotNull
    Iterable<E, T> removeLast(@NotNull Predicate<? super E> predicate);

    @NotNull
    Iterable<E, T> removeLastSequence(@NotNull java.lang.Iterable<?> elements);

    @NotNull
    Iterable<E, T> removeSequence(@NotNull java.lang.Iterable<?> elements);

    @NotNull
    Iterable<E, T> removeSlice(int start);

    @NotNull
    Iterable<E, T> removeSlice(int start, int end);

    @NotNull
    Iterable<E, T> replaceFirstSequence(@NotNull java.lang.Iterable<?> elements,
        @NotNull Function<? super java.util.List<E>, java.lang.Iterable<? extends E>> mapper);

    @NotNull
    Iterable<E, T> replaceLastSequence(@NotNull java.lang.Iterable<?> elements,
        @NotNull Function<? super java.util.List<E>, java.lang.Iterable<? extends E>> mapper);

    @NotNull
    Iterable<E, T> replaceSequence(@NotNull java.lang.Iterable<?> elements,
        @NotNull Function<? super java.util.List<E>, java.lang.Iterable<? extends E>> mapper);

    @NotNull
    Iterable<E, T> replaceSequence(@NotNull java.lang.Iterable<?> elements,
        @NotNull IndexedFunction<? super java.util.List<E>, java.lang.Iterable<? extends E>> mapper);

    @NotNull
    Iterable<E, T> replaceSlice(int start, int end, @NotNull java.lang.Iterable<? extends E> patch);

    @NotNull
    Iterable<E, T> replaceSlice(int start, @NotNull java.lang.Iterable<? extends E> patch);

    @NotNull
    Iterable<E, T> resizeTo(@NotNegative int numElements, E padding);

    int size();

    @NotNull
    Iterable<E, T> slice(int start);

    @NotNull
    Iterable<E, T> slice(int start, int end);

    @NotNull
    Iterable<? extends Iterable<E, T>, ? extends Iterable<? extends Iterable<E, T>, ?>> slidingWindow(
        @Positive int maxSize, @Positive int step);

    @NotNull
    Iterable<? extends Iterable<E, T>, ? extends Iterable<? extends Iterable<E, T>, ?>> slidingWindowWithPadding(
        @Positive int size, @Positive int step, E padding);

    @NotNull
    Iterable<Boolean, ? extends Iterable<Boolean, ?>> startsWith(
        @NotNull java.lang.Iterable<?> elements);

    @NotNull
    Iterable<E, T> symmetricDiff(@NotNull java.lang.Iterable<? extends E> elements);

    @NotNull
    Iterable<E, T> takeFirst(int maxElements);

    @NotNull
    Iterable<E, T> takeFirstWhile(@NotNull IndexedPredicate<? super E> condition);

    @NotNull
    Iterable<E, T> takeFirstWhile(@NotNull Predicate<? super E> condition);

    @NotNull
    Iterable<E, T> takeLast(int maxElements);

    @NotNull
    Object[] toArray();

    @NotNull
    <R> R[] toArray(@NotNull R[] array);

    @NotNull
    Iterator<E, ? extends Iterator<E, ?>> toIterator();

    @NotNull
    <A extends Appendable> A toString(@NotNull A appendable);

    @NotNull
    <A extends Appendable> A toString(@NotNull A appendable, @NotNull String separator);

    @NotNull
    <A extends Appendable> A toString(@NotNull A appendable, @NotNull String separator,
        @NotNull String prefix, @NotNull String suffix);

    @NotNull
    Iterable<E, T> union(@NotNull java.lang.Iterable<? extends E> elements);

    @NotNull
    <F> Iterable<ZipEntry<E, F>, ? extends Iterable<ZipEntry<E, F>, ?>> zip(
        @NotNull java.lang.Iterable<F> elements);

    @NotNull
    <F> Iterable<ZipEntry<E, F>, ? extends Iterable<ZipEntry<E, F>, ?>> zipWithPadding(
        @NotNull java.lang.Iterable<F> elements, E paddingLeft, F paddingRight);

    /*
      TODO:
      to++
     */
  }

  public interface Iterator<E, T extends Iterator<E, T>> extends java.util.Iterator<E>,
      Iterable<E, T> {

    @NotNull
    Iterator<E, T> append(@Nullable E element);

    @NotNull
    Iterator<E, T> appendAll(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    <F> Iterator<F, ? extends Iterator<F, ?>> apply(
        @NotNull Function<? super T, java.lang.Iterable<F>> function);

    @Override
    @NotNull
    <F> Iterator<F, ? extends Iterator<F, ?>> cast();

    @Override
    @NotNull
    Iterator<Integer, ? extends Iterator<Integer, ?>> count();

    @Override
    @NotNull
    Iterator<E, T> diff(@NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    Iterator<E, T> distinct();

    @Override
    @NotNull
    <K> Iterator<E, T> distinctBy(@NotNull Function<? super E, K> keyExtractor);

    @Override
    @NotNull
    <K> Iterator<E, T> distinctBy(@NotNull IndexedFunction<? super E, K> keyExtractor);

    @NotNull
    Iterator<E, T> doAfter(@NotNull Action action);

    @NotNull
    Iterator<E, T> doFinally(@NotNull Action action);

    @Override
    @NotNull
    Iterator<E, T> dropFirst(int maxElements);

    @Override
    @NotNull
    Iterator<E, T> dropFirstWhile(@NotNull IndexedPredicate<? super E> condition);

    @Override
    @NotNull
    Iterator<E, T> dropFirstWhile(@NotNull Predicate<? super E> condition);

    @Override
    @NotNull
    Iterator<E, T> dropLast(int maxElements);

    @Override
    @NotNull
    Iterator<Boolean, ? extends Iterator<Boolean, ?>> endsWith(
        @NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    Iterator<Boolean, ? extends Iterator<Boolean, ?>> exists(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<Boolean, ? extends Iterator<Boolean, ?>> exists(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<Boolean, ? extends Iterator<Boolean, ?>> existsForward(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<Boolean, ? extends Iterator<Boolean, ?>> existsForward(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<E, T> filter(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<E, T> filter(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<E, T> filterWhile(@NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<E, T> filterWhile(@NotNull Predicate<? super E> condition,
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<E, T> find(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<E, T> find(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<E, T> findFirst(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<E, T> findFirst(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<Integer, ? extends Iterator<Integer, ?>> findFirstIndex(
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<Integer, ? extends Iterator<Integer, ?>> findFirstIndex(
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<Integer, ? extends Iterator<Integer, ?>> findFirstIndexOf(@Nullable Object element);

    @Override
    @NotNull
    Iterator<Integer, ? extends Iterator<Integer, ?>> findFirstIndexOfSequence(
        @NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    Iterator<Integer, ? extends Iterator<Integer, ?>> findIndex(
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<Integer, ? extends Iterator<Integer, ?>> findIndex(
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<Integer, ? extends Iterator<Integer, ?>> findIndexOf(@Nullable Object element);

    @Override
    @NotNull
    Iterator<E, T> findLast(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<E, T> findLast(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<Integer, ? extends Iterator<Integer, ?>> findLastIndex(
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<Integer, ? extends Iterator<Integer, ?>> findLastIndex(
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<Integer, ? extends Iterator<Integer, ?>> findLastIndexOf(@Nullable Object element);

    @Override
    @NotNull
    Iterator<Integer, ? extends Iterator<Integer, ?>> findLastIndexOfSequence(
        @NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    <F> Iterator<F, ? extends Iterator<F, ?>> flatMap(
        @NotNull Function<? super E, ? extends java.lang.Iterable<F>> mapper);

    @Override
    @NotNull
    <F> Iterator<F, ? extends Iterator<F, ?>> flatMap(
        @NotNull IndexedFunction<? super E, ? extends java.lang.Iterable<F>> mapper);

    @Override
    @NotNull
    Iterator<E, T> flatMapWhile(@NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedFunction<? super E, ? extends java.lang.Iterable<? extends E>> mapper);

    @Override
    @NotNull
    Iterator<E, T> flatMapWhile(@NotNull Predicate<? super E> condition,
        @NotNull Function<? super E, ? extends java.lang.Iterable<? extends E>> mapper);

    @Override
    @NotNull
    <F> Iterator<F, ? extends Iterator<F, ?>> fold(F identity,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    @NotNull
    <F> Iterator<F, ? extends Iterator<F, ?>> foldForward(F identity,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    @NotNull
    <F> Iterator<F, ? extends Iterator<F, ?>> foldWhile(F identity,
        @NotNull Predicate<? super F> condition,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    @NotNull
    <F> Iterator<F, ? extends Iterator<F, ?>> foldWhileForward(F identity,
        @NotNull Predicate<? super F> condition,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    @NotNull
    Iterator<Boolean, ? extends Iterator<Boolean, ?>> includes(@Nullable Object element);

    @Override
    @NotNull
    Iterator<Boolean, ? extends Iterator<Boolean, ?>> includesAll(
        @NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    Iterator<Boolean, ? extends Iterator<Boolean, ?>> includesSequence(
        @NotNull java.lang.Iterable<?> elements);

    @NotNull
    Iterator<E, T> insert(@Nullable E element);

    @NotNull
    Iterator<E, T> insertAfter(int numElements, @Nullable E element);

    @NotNull
    Iterator<E, T> insertAll(@NotNull java.lang.Iterable<? extends E> elements);

    @NotNull
    Iterator<E, T> insertAllAfter(int numElements,
        @NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    Iterator<E, T> interleave(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    Iterator<E, T> interleaveInner(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    Iterator<E, T> interleaveInnerWithPadding(@NotNull java.lang.Iterable<? extends E> elements,
        E paddingLeft, E paddingRight);

    @Override
    @NotNull
    Iterator<E, T> interleaveWithPadding(@NotNull java.lang.Iterable<? extends E> elements,
        E paddingLeft, E paddingRight);

    @Override
    @NotNull
    Iterator<E, T> intersect(@NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    <F> Iterator<F, ? extends Iterator<F, ?>> map(@NotNull Function<? super E, F> mapper);

    @Override
    @NotNull
    <F> Iterator<F, ? extends Iterator<F, ?>> map(@NotNull IndexedFunction<? super E, F> mapper);

    @Override
    @NotNull
    <F> Iterator<F, ? extends Iterator<F, ?>> mapForward(@NotNull Function<? super E, F> mapper);

    @Override
    @NotNull
    <F> Iterator<F, ? extends Iterator<F, ?>> mapForward(
        @NotNull IndexedFunction<? super E, F> mapper);

    @Override
    @NotNull
    Iterator<E, ? extends Iterator<E, ?>> mapWhile(@NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Iterator<E, ? extends Iterator<E, ?>> mapWhile(@NotNull Predicate<? super E> condition,
        @NotNull Function<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Iterator<E, ? extends Iterator<E, ?>> mapWhileForward(
        @NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Iterator<E, ? extends Iterator<E, ?>> mapWhileForward(@NotNull Predicate<? super E> condition,
        @NotNull Function<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Iterator<E, T> materialize();

    @Override
    @NotNull
    Iterator<E, T> max(@NotNull Comparator<? super E> comparator);

    @Override
    @NotNull
    Iterator<E, T> min(@NotNull Comparator<? super E> comparator);

    @Override
    @NotNull
    Iterator<E, T> minus(@Nullable E element);

    @Override
    @NotNull
    Iterator<E, T> minusAll(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    Iterator<E, T> minusFirst(@Nullable E element);

    @Override
    @NotNull
    Iterator<E, T> minusLast(@Nullable E element);

    @Override
    @NotNull
    Iterator<Boolean, ? extends Iterator<Boolean, ?>> notExists(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<Boolean, ? extends Iterator<Boolean, ?>> notExists(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<Boolean, ? extends Iterator<Boolean, ?>> notExistsForward(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<Boolean, ? extends Iterator<Boolean, ?>> notExistsForward(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<E, T> orElse(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    Iterator<E, T> orElseGet(@NotNull Supplier<? extends java.lang.Iterable<? extends E>> supplier);

    @Override
    @NotNull
    Iterator<? extends Iterator<E, T>, ? extends Iterator<? extends Iterator<E, T>, ?>> partition(
        @Positive int numPartitions, @NotNull Function<? super E, Integer> indexExtractor);

    @Override
    @NotNull
    Iterator<? extends Iterator<E, T>, ? extends Iterator<? extends Iterator<E, T>, ?>> partition(
        @Positive int numPartitions, @NotNull IndexedFunction<? super E, Integer> indexExtractor);

    @Override
    @NotNull
    <K> Iterator<? extends ZipEntry<K, ? extends Iterator<E, T>>, ? extends Iterator<? extends ZipEntry<K, ? extends Iterator<E, T>>, ?>> partitionZip(
        @NotNull Function<? super E, K> keyExtractor);

    @Override
    @NotNull
    <K> Iterator<? extends ZipEntry<K, ? extends Iterator<E, T>>, ? extends Iterator<? extends ZipEntry<K, ? extends Iterator<E, T>>, ?>> partitionZip(
        @NotNull IndexedFunction<? super E, K> keyExtractor);

    @NotNull
    Iterator<E, T> peek(@NotNull Consumer<? super E> consumer);

    @NotNull
    Iterator<E, T> peek(@NotNull IndexedConsumer<? super E> consumer);

    @NotNull
    Iterator<E, T> peekExceptionally(@NotNull Consumer<? super Throwable> consumer);

    @NotNull
    Iterator<E, T> peekExceptionally(@NotNull IndexedConsumer<? super Throwable> consumer);

    @Override
    @NotNull
    Iterator<E, T> plus(@Nullable E element);

    @Override
    @NotNull
    Iterator<E, T> plusAll(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    Iterator<E, T> reduce(@NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    Iterator<E, T> reduceForward(
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    Iterator<E, T> reduceWhile(@NotNull Predicate<? super E> condition,
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    Iterator<E, T> reduceWhileForward(@NotNull Predicate<? super E> condition,
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    Iterator<E, T> removeFirst(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<E, T> removeFirst(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<E, T> removeFirstSequence(@NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    Iterator<E, T> removeLast(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<E, T> removeLast(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Iterator<E, T> removeLastSequence(@NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    Iterator<E, T> removeSequence(@NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    Iterator<E, T> removeSlice(int start);

    @Override
    @NotNull
    Iterator<E, T> removeSlice(int start, int end);

    @NotNull
    Iterator<E, T> replaceFirstSequence(@NotNull java.lang.Iterable<?> elements,
        @NotNull Function<? super java.util.List<E>, java.lang.Iterable<? extends E>> mapper);

    @NotNull
    Iterator<E, T> replaceLastSequence(@NotNull java.lang.Iterable<?> elements,
        @NotNull Function<? super java.util.List<E>, java.lang.Iterable<? extends E>> mapper);

    @NotNull
    Iterator<E, T> replaceSequence(@NotNull java.lang.Iterable<?> elements,
        @NotNull Function<? super java.util.List<E>, java.lang.Iterable<? extends E>> mapper);

    @NotNull
    Iterator<E, T> replaceSequence(@NotNull java.lang.Iterable<?> elements,
        @NotNull IndexedFunction<? super java.util.List<E>, java.lang.Iterable<? extends E>> mapper);

    @Override
    @NotNull
    Iterator<E, T> replaceSlice(int start, int end, @NotNull java.lang.Iterable<? extends E> patch);

    @Override
    @NotNull
    Iterator<E, T> replaceSlice(int start, @NotNull java.lang.Iterable<? extends E> patch);

    @Override
    @NotNull
    Iterator<E, T> resizeTo(@NotNegative int numElements, E padding);

    int skip(int maxElements);

    @Override
    @NotNull
    Iterator<E, T> slice(int start);

    @Override
    @NotNull
    Iterator<E, T> slice(int start, int end);

    @Override
    @NotNull
    Iterator<? extends Iterator<E, T>, ? extends Iterator<? extends Iterator<E, T>, ?>> slidingWindow(
        @Positive int maxSize, @Positive int step);

    @Override
    @NotNull
    Iterator<? extends Iterator<E, T>, ? extends Iterator<? extends Iterator<E, T>, ?>> slidingWindowWithPadding(
        @Positive int size, @Positive int step, E padding);

    @Override
    @NotNull
    Iterator<Boolean, ? extends Iterator<Boolean, ?>> startsWith(
        @NotNull java.lang.Iterable<?> elements);

    @NotNull
    <X extends Throwable> Iterator<E, T> switchExceptionally(@NotNull Class<X> exceptionType,
        @NotNull Function<? super X, ? extends java.lang.Iterable<? extends E>> mapper);

    @NotNull
    <X extends Throwable> Iterator<E, T> switchExceptionally(@NotNull Class<X> exceptionType,
        @NotNull IndexedFunction<? super X, ? extends java.lang.Iterable<? extends E>> mapper);

    @NotNull
    Iterator<E, T> switchExceptionally(
        @NotNull Function<? super Throwable, ? extends java.lang.Iterable<? extends E>> mapper);

    @NotNull
    Iterator<E, T> switchExceptionally(
        @NotNull IndexedFunction<? super Throwable, ? extends java.lang.Iterable<? extends E>> mapper);

    @Override
    @NotNull
    Iterator<E, T> symmetricDiff(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    Iterator<E, T> takeFirst(int maxElements);

    @Override
    @NotNull
    Iterator<E, T> takeFirstWhile(@NotNull IndexedPredicate<? super E> condition);

    @Override
    @NotNull
    Iterator<E, T> takeFirstWhile(@NotNull Predicate<? super E> condition);

    @Override
    @NotNull
    Iterator<E, T> takeLast(int maxElements);

    @Override
    @NotNull
    Iterator<E, T> union(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    <F> Iterator<ZipEntry<E, F>, ? extends Iterator<ZipEntry<E, F>, ?>> zip(
        @NotNull java.lang.Iterable<F> elements);

    @Override
    @NotNull
    <F> Iterator<ZipEntry<E, F>, ? extends Iterator<ZipEntry<E, F>, ?>> zipWithPadding(
        @NotNull java.lang.Iterable<F> elements, E paddingLeft, F paddingRight);
  }

  public interface List<E, T extends List<E, T>> extends Collection<E, T>, java.util.List<E> {

    @NotNull
    List<E, T> append(@Nullable E element);

    @NotNull
    List<E, T> appendAll(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    <F> List<F, ? extends List<F, ?>> apply(
        @NotNull Function<? super T, java.lang.Iterable<F>> function);

    @Override
    @NotNull
    <F> List<F, ? extends List<F, ?>> cast();

    @Override
    @NotNull
    List<E, T> clone();

    @Override
    @NotNull
    List<E, T> clone(@NotNull Function<? super E, ? extends E> cloner);

    @Override
    @NotNull
    List<Integer, ? extends List<Integer, ?>> count();

    @Override
    @NotNull
    List<E, T> diff(@NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    List<E, T> distinct();

    @Override
    @NotNull
    <K> List<E, T> distinctBy(@NotNull Function<? super E, K> keyExtractor);

    @Override
    @NotNull
    <K> List<E, T> distinctBy(@NotNull IndexedFunction<? super E, K> keyExtractor);

    @Override
    @NotNull
    List<E, T> dropFirst(int maxElements);

    @Override
    @NotNull
    List<E, T> dropFirstWhile(@NotNull IndexedPredicate<? super E> condition);

    @Override
    @NotNull
    List<E, T> dropFirstWhile(@NotNull Predicate<? super E> condition);

    @Override
    @NotNull
    List<E, T> dropLast(int maxElements);

    @Override
    @NotNull
    List<E, T> dropLastWhile(@NotNull IndexedPredicate<? super E> condition);

    @Override
    @NotNull
    List<E, T> dropLastWhile(@NotNull Predicate<? super E> condition);

    @Override
    @NotNull
    List<Boolean, ? extends List<Boolean, ?>> endsWith(@NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    List<Boolean, ? extends List<Boolean, ?>> exists(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    List<Boolean, ? extends List<Boolean, ?>> exists(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    List<Boolean, ? extends List<Boolean, ?>> existsBackward(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    List<Boolean, ? extends List<Boolean, ?>> existsBackward(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    List<Boolean, ? extends List<Boolean, ?>> existsForward(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    List<Boolean, ? extends List<Boolean, ?>> existsForward(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    List<E, T> filter(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    List<E, T> filter(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    List<E, T> filterWhile(@NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    List<E, T> filterWhile(@NotNull Predicate<? super E> condition,
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    List<E, T> find(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    List<E, T> find(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    List<E, T> findFirst(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    List<E, T> findFirst(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    List<Integer, ? extends List<Integer, ?>> findFirstIndex(
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    List<Integer, ? extends List<Integer, ?>> findFirstIndex(
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    List<Integer, ? extends List<Integer, ?>> findFirstIndexOf(@Nullable Object element);

    @Override
    @NotNull
    List<Integer, ? extends List<Integer, ?>> findFirstIndexOfSequence(
        @NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    List<Integer, ? extends List<Integer, ?>> findIndex(
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    List<Integer, ? extends List<Integer, ?>> findIndex(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    List<Integer, ? extends List<Integer, ?>> findIndexOf(@Nullable Object element);

    @Override
    @NotNull
    List<E, T> findLast(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    List<E, T> findLast(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    List<Integer, ? extends List<Integer, ?>> findLastIndex(
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    List<Integer, ? extends List<Integer, ?>> findLastIndex(
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    List<Integer, ? extends List<Integer, ?>> findLastIndexOf(@Nullable Object element);

    @Override
    @NotNull
    List<Integer, ? extends List<Integer, ?>> findLastIndexOfSequence(
        @NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    <F> List<F, ? extends List<F, ?>> flatMap(
        @NotNull Function<? super E, ? extends java.lang.Iterable<F>> mapper);

    @Override
    @NotNull
    <F> List<F, ? extends List<F, ?>> flatMap(
        @NotNull IndexedFunction<? super E, ? extends java.lang.Iterable<F>> mapper);

    @Override
    @NotNull
    List<E, T> flatMapWhile(@NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedFunction<? super E, ? extends java.lang.Iterable<? extends E>> mapper);

    @Override
    @NotNull
    List<E, T> flatMapWhile(@NotNull Predicate<? super E> condition,
        @NotNull Function<? super E, ? extends java.lang.Iterable<? extends E>> mapper);

    @Override
    @NotNull
    <F> List<F, ? extends List<F, ?>> fold(F identity,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    @NotNull
    <F> List<F, ? extends List<F, ?>> foldBackward(F identity,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    @NotNull
    <F> List<F, ? extends List<F, ?>> foldForward(F identity,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    @NotNull
    <F> List<F, ? extends List<F, ?>> foldWhile(F identity, @NotNull Predicate<? super F> condition,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    @NotNull
    <F> List<F, ? extends List<F, ?>> foldWhileBackward(F identity,
        @NotNull Predicate<? super F> condition,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    @NotNull
    <F> List<F, ? extends List<F, ?>> foldWhileForward(F identity,
        @NotNull Predicate<? super F> condition,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    @NotNull
    List<Boolean, ? extends List<Boolean, ?>> includes(@Nullable Object element);

    @Override
    @NotNull
    List<Boolean, ? extends List<Boolean, ?>> includesAll(@NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    List<Boolean, ? extends List<Boolean, ?>> includesSequence(
        @NotNull java.lang.Iterable<?> elements);

    @NotNull
    List<E, T> insertAfter(int numElements, E element);

    @NotNull
    List<E, T> insertAllAfter(int numElements, @NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    List<E, T> interleave(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    List<E, T> interleaveInner(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    List<E, T> interleaveInnerWithPadding(@NotNull java.lang.Iterable<? extends E> elements,
        E paddingLeft, E paddingRight);

    @Override
    @NotNull
    List<E, T> interleaveWithPadding(@NotNull java.lang.Iterable<? extends E> elements,
        E paddingLeft, E paddingRight);

    @Override
    @NotNull
    List<E, T> intersect(@NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    ListIterator<E, ? extends ListIterator<E, ?>> listIterator();

    @Override
    @NotNull
    ListIterator<E, ? extends ListIterator<E, ?>> listIterator(int index);

    @Override
    @NotNull
    <F> List<F, ? extends List<F, ?>> map(@NotNull Function<? super E, F> mapper);

    @Override
    @NotNull
    <F> List<F, ? extends List<F, ?>> map(@NotNull IndexedFunction<? super E, F> mapper);

    @Override
    @NotNull
    <F> List<F, ? extends List<F, ?>> mapBackward(@NotNull Function<? super E, F> mapper);

    @Override
    @NotNull
    <F> List<F, ? extends List<F, ?>> mapBackward(@NotNull IndexedFunction<? super E, F> mapper);

    @Override
    @NotNull
    <F> List<F, ? extends List<F, ?>> mapForward(@NotNull Function<? super E, F> mapper);

    @Override
    @NotNull
    <F> List<F, ? extends List<F, ?>> mapForward(@NotNull IndexedFunction<? super E, F> mapper);

    @Override
    @NotNull
    List<E, ? extends List<E, ?>> mapWhile(@NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    @NotNull
    List<E, ? extends List<E, ?>> mapWhile(@NotNull Predicate<? super E> condition,
        @NotNull Function<? super E, ? extends E> mapper);

    @Override
    @NotNull
    <F> List<F, ? extends List<F, ?>> mapWhileBackward(
        @NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedFunction<? super E, F> mapper);

    @Override
    @NotNull
    <F> List<F, ? extends List<F, ?>> mapWhileBackward(@NotNull Predicate<? super E> condition,
        @NotNull Function<? super E, F> mapper);

    @Override
    @NotNull
    List<E, ? extends List<E, ?>> mapWhileForward(@NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    @NotNull
    List<E, ? extends List<E, ?>> mapWhileForward(@NotNull Predicate<? super E> condition,
        @NotNull Function<? super E, ? extends E> mapper);

    @Override
    @NotNull
    List<E, T> materialize();

    @Override
    @NotNull
    List<E, T> max(@NotNull Comparator<? super E> comparator);

    @Override
    @NotNull
    List<E, T> min(@NotNull Comparator<? super E> comparator);

    @Override
    @NotNull
    List<E, T> minus(@Nullable E element);

    @Override
    @NotNull
    List<E, T> minusAll(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    List<E, T> minusFirst(@Nullable E element);

    @Override
    @NotNull
    List<E, T> minusLast(@Nullable E element);

    @Override
    @NotNull
    List<Boolean, ? extends List<Boolean, ?>> notExists(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    List<Boolean, ? extends List<Boolean, ?>> notExists(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    List<Boolean, ? extends List<Boolean, ?>> notExistsBackward(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    List<Boolean, ? extends List<Boolean, ?>> notExistsBackward(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    List<Boolean, ? extends List<Boolean, ?>> notExistsForward(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    List<Boolean, ? extends List<Boolean, ?>> notExistsForward(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    List<E, T> orElse(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    List<E, T> orElseGet(@NotNull Supplier<? extends java.lang.Iterable<? extends E>> supplier);

    @Override
    @NotNull
    Iterator<? extends List<E, T>, ? extends Iterator<? extends List<E, T>, ?>> partition(
        @Positive int numPartitions, @NotNull Function<? super E, Integer> indexExtractor);

    @Override
    @NotNull
    Iterator<? extends List<E, T>, ? extends Iterator<? extends List<E, T>, ?>> partition(
        @Positive int numPartitions, @NotNull IndexedFunction<? super E, Integer> indexExtractor);

    @Override
    @NotNull
    <K> Iterator<? extends ZipEntry<K, ? extends List<E, T>>, ? extends Iterator<? extends ZipEntry<K, ? extends List<E, T>>, ?>> partitionZip(
        @NotNull Function<? super E, K> keyExtractor);

    @Override
    @NotNull
    <K> Iterator<? extends ZipEntry<K, ? extends List<E, T>>, ? extends Iterator<? extends ZipEntry<K, ? extends List<E, T>>, ?>> partitionZip(
        @NotNull IndexedFunction<? super E, K> keyExtractor);

    @Override
    @NotNull
    List<E, T> plus(@Nullable E element);

    @Override
    @NotNull
    List<E, T> plusAll(@NotNull java.lang.Iterable<? extends E> elements);

    @NotNull
    List<E, T> prepend(E element);

    @NotNull
    List<E, T> prependAll(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    List<E, T> reduce(@NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    List<E, T> reduceBackward(@NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    List<E, T> reduceForward(@NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    List<E, T> reduceWhile(@NotNull Predicate<? super E> condition,
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    List<E, T> reduceWhileBackward(@NotNull Predicate<? super E> condition,
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    List<E, T> reduceWhileForward(@NotNull Predicate<? super E> condition,
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    List<E, T> removeFirst(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    List<E, T> removeFirst(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    List<E, T> removeFirstSequence(@NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    List<E, T> removeLast(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    List<E, T> removeLast(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    List<E, T> removeLastSequence(@NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    List<E, T> removeSequence(@NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    List<E, T> removeSlice(int start);

    @Override
    @NotNull
    List<E, T> removeSlice(int start, int end);

    @NotNull
    List<E, T> replaceFirstSequence(@NotNull java.lang.Iterable<?> elements,
        @NotNull Function<? super java.util.List<E>, java.lang.Iterable<? extends E>> mapper);

    @NotNull
    List<E, T> replaceLastSequence(@NotNull java.lang.Iterable<?> elements,
        @NotNull Function<? super java.util.List<E>, java.lang.Iterable<? extends E>> mapper);

    @NotNull
    List<E, T> replaceSequence(@NotNull java.lang.Iterable<?> elements,
        @NotNull Function<? super java.util.List<E>, java.lang.Iterable<? extends E>> mapper);

    @NotNull
    List<E, T> replaceSequence(@NotNull java.lang.Iterable<?> elements,
        @NotNull IndexedFunction<? super java.util.List<E>, java.lang.Iterable<? extends E>> mapper);

    @Override
    @NotNull
    List<E, T> replaceSlice(int start, int end, @NotNull java.lang.Iterable<? extends E> patch);

    @Override
    @NotNull
    List<E, T> replaceSlice(int start, @NotNull java.lang.Iterable<? extends E> patch);

    @Override
    @NotNull
    List<E, T> resizeTo(@NotNegative int numElements, E padding);

    @NotNull
    List<E, T> reverse();

    @Override
    @NotNull
    List<E, T> slice(int start);

    @Override
    @NotNull
    List<E, T> slice(int start, int end);

    @Override
    @NotNull
    List<? extends List<E, T>, ? extends List<? extends List<E, T>, ?>> slidingWindow(
        @Positive int maxSize, @Positive int step);

    @Override
    @NotNull
    List<? extends List<E, T>, ? extends List<? extends List<E, T>, ?>> slidingWindowWithPadding(
        @Positive int size, @Positive int step, E padding);

    @NotNull
    List<E, T> sorted(@NotNull Comparator<? super E> comparator);

    @Override
    @NotNull
    List<Boolean, ? extends List<Boolean, ?>> startsWith(@NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    List<E, T> symmetricDiff(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    List<E, T> takeFirst(int maxElements);

    @Override
    @NotNull
    List<E, T> takeFirstWhile(@NotNull IndexedPredicate<? super E> condition);

    @Override
    @NotNull
    List<E, T> takeFirstWhile(@NotNull Predicate<? super E> condition);

    @Override
    @NotNull
    List<E, T> takeLast(int maxElements);

    @Override
    @NotNull
    List<E, T> takeLastWhile(@NotNull IndexedPredicate<? super E> condition);

    @Override
    @NotNull
    List<E, T> takeLastWhile(@NotNull Predicate<? super E> condition);

    @Override
    @NotNull
    List<E, T> union(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    <F> List<ZipEntry<E, F>, ? extends List<ZipEntry<E, F>, ?>> zip(
        @NotNull java.lang.Iterable<F> elements);

    @Override
    @NotNull
    <F> List<ZipEntry<E, F>, ? extends List<ZipEntry<E, F>, ?>> zipWithPadding(
        @NotNull java.lang.Iterable<F> elements, E paddingLeft, F paddingRight);
  }

  public interface ListIterator<E, T extends ListIterator<E, T>> extends java.util.ListIterator<E>,
      Iterator<E, T> {

    @Override
    @NotNull
    ListIterator<E, T> append(@Nullable E element);

    @Override
    @NotNull
    ListIterator<E, T> appendAll(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    <F> ListIterator<F, ? extends ListIterator<F, ?>> apply(
        @NotNull Function<? super T, java.lang.Iterable<F>> function);

    @Override
    @NotNull
    <F> ListIterator<F, ? extends ListIterator<F, ?>> cast();

    @NotNull
    ListIterator<E, T> clone();

    @NotNull
    ListIterator<E, T> clone(@NotNull Function<? super E, ? extends E> cloner);

    @Override
    @NotNull
    ListIterator<Integer, ? extends ListIterator<Integer, ?>> count();

    @Override
    @NotNull
    ListIterator<E, T> diff(@NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    ListIterator<E, T> distinct();

    @Override
    @NotNull
    <K> ListIterator<E, T> distinctBy(@NotNull Function<? super E, K> keyExtractor);

    @Override
    @NotNull
    <K> ListIterator<E, T> distinctBy(@NotNull IndexedFunction<? super E, K> keyExtractor);

    @Override
    @NotNull
    ListIterator<E, T> doAfter(@NotNull Action action);

    @Override
    @NotNull
    ListIterator<E, T> doFinally(@NotNull Action action);

    @Override
    @NotNull
    ListIterator<E, T> dropFirst(int maxElements);

    @Override
    @NotNull
    ListIterator<E, T> dropFirstWhile(@NotNull IndexedPredicate<? super E> condition);

    @Override
    @NotNull
    ListIterator<E, T> dropFirstWhile(@NotNull Predicate<? super E> condition);

    @Override
    @NotNull
    ListIterator<E, T> dropLast(int maxElements);

    @NotNull
    ListIterator<E, T> dropLastWhile(@NotNull IndexedPredicate<? super E> condition);

    @NotNull
    ListIterator<E, T> dropLastWhile(@NotNull Predicate<? super E> condition);

    @Override
    @NotNull
    ListIterator<Boolean, ? extends ListIterator<Boolean, ?>> endsWith(
        @NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    ListIterator<Boolean, ? extends ListIterator<Boolean, ?>> exists(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<Boolean, ? extends ListIterator<Boolean, ?>> exists(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @NotNull
    ListIterator<Boolean, ? extends ListIterator<Boolean, ?>> existsBackward(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @NotNull
    ListIterator<Boolean, ? extends ListIterator<Boolean, ?>> existsBackward(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<Boolean, ? extends ListIterator<Boolean, ?>> existsForward(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<Boolean, ? extends ListIterator<Boolean, ?>> existsForward(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<E, T> filter(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<E, T> filter(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<E, T> filterWhile(@NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<E, T> filterWhile(@NotNull Predicate<? super E> condition,
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<E, T> find(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<E, T> find(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<E, T> findFirst(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<E, T> findFirst(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<Integer, ? extends ListIterator<Integer, ?>> findFirstIndex(
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<Integer, ? extends ListIterator<Integer, ?>> findFirstIndex(
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<Integer, ? extends ListIterator<Integer, ?>> findFirstIndexOf(
        @Nullable Object element);

    @Override
    @NotNull
    ListIterator<Integer, ? extends ListIterator<Integer, ?>> findFirstIndexOfSequence(
        @NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    ListIterator<Integer, ? extends ListIterator<Integer, ?>> findIndex(
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<Integer, ? extends ListIterator<Integer, ?>> findIndex(
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<Integer, ? extends ListIterator<Integer, ?>> findIndexOf(@Nullable Object element);

    @Override
    @NotNull
    ListIterator<E, T> findLast(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<E, T> findLast(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<Integer, ? extends ListIterator<Integer, ?>> findLastIndex(
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<Integer, ? extends ListIterator<Integer, ?>> findLastIndex(
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<Integer, ? extends ListIterator<Integer, ?>> findLastIndexOf(
        @Nullable Object element);

    @Override
    @NotNull
    ListIterator<Integer, ? extends ListIterator<Integer, ?>> findLastIndexOfSequence(
        @NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    <F> ListIterator<F, ? extends ListIterator<F, ?>> flatMap(
        @NotNull Function<? super E, ? extends java.lang.Iterable<F>> mapper);

    @Override
    @NotNull
    <F> ListIterator<F, ? extends ListIterator<F, ?>> flatMap(
        @NotNull IndexedFunction<? super E, ? extends java.lang.Iterable<F>> mapper);

    @Override
    @NotNull
    ListIterator<E, T> flatMapWhile(@NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedFunction<? super E, ? extends java.lang.Iterable<? extends E>> mapper);

    @Override
    @NotNull
    ListIterator<E, T> flatMapWhile(@NotNull Predicate<? super E> condition,
        @NotNull Function<? super E, ? extends java.lang.Iterable<? extends E>> mapper);

    @Override
    @NotNull
    <F> ListIterator<F, ? extends ListIterator<F, ?>> fold(F identity,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @NotNull
    <F> ListIterator<F, ? extends ListIterator<F, ?>> foldBackward(F identity,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    @NotNull
    <F> ListIterator<F, ? extends ListIterator<F, ?>> foldForward(F identity,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    @NotNull
    <F> ListIterator<F, ? extends ListIterator<F, ?>> foldWhile(F identity,
        @NotNull Predicate<? super F> condition,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @NotNull
    <F> ListIterator<F, ? extends ListIterator<F, ?>> foldWhileBackward(F identity,
        @NotNull Predicate<? super F> condition,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    @NotNull
    <F> ListIterator<F, ? extends ListIterator<F, ?>> foldWhileForward(F identity,
        @NotNull Predicate<? super F> condition,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    @NotNull
    ListIterator<Boolean, ? extends ListIterator<Boolean, ?>> includes(@Nullable Object element);

    @Override
    @NotNull
    ListIterator<Boolean, ? extends ListIterator<Boolean, ?>> includesAll(
        @NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    ListIterator<Boolean, ? extends ListIterator<Boolean, ?>> includesSequence(
        @NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    ListIterator<E, T> insert(@Nullable E element);

    @Override
    @NotNull
    ListIterator<E, T> insertAfter(int numElements, @Nullable E element);

    @Override
    @NotNull
    ListIterator<E, T> insertAll(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    ListIterator<E, T> insertAllAfter(int numElements,
        @NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    ListIterator<E, T> interleave(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    ListIterator<E, T> interleaveInner(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    ListIterator<E, T> interleaveInnerWithPadding(@NotNull java.lang.Iterable<? extends E> elements,
        E paddingLeft, E paddingRight);

    @Override
    @NotNull
    ListIterator<E, T> interleaveWithPadding(@NotNull java.lang.Iterable<? extends E> elements,
        E paddingLeft, E paddingRight);

    @Override
    @NotNull
    ListIterator<E, T> intersect(@NotNull java.lang.Iterable<?> elements);

    @NotNull
    ListIterator<E, ? extends ListIterator<E, ?>> listIterator();

    @Override
    @NotNull
    <F> ListIterator<F, ? extends ListIterator<F, ?>> map(@NotNull Function<? super E, F> mapper);

    @Override
    @NotNull
    <F> ListIterator<F, ? extends ListIterator<F, ?>> map(
        @NotNull IndexedFunction<? super E, F> mapper);

    @NotNull
    <F> ListIterator<F, ? extends ListIterator<F, ?>> mapBackward(
        @NotNull Function<? super E, F> mapper);

    @NotNull
    <F> ListIterator<F, ? extends ListIterator<F, ?>> mapBackward(
        @NotNull IndexedFunction<? super E, F> mapper);

    @Override
    @NotNull
    <F> ListIterator<F, ? extends ListIterator<F, ?>> mapForward(
        @NotNull Function<? super E, F> mapper);

    @Override
    @NotNull
    <F> ListIterator<F, ? extends ListIterator<F, ?>> mapForward(
        @NotNull IndexedFunction<? super E, F> mapper);

    @Override
    @NotNull
    ListIterator<E, ? extends ListIterator<E, ?>> mapWhile(
        @NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    @NotNull
    ListIterator<E, ? extends ListIterator<E, ?>> mapWhile(@NotNull Predicate<? super E> condition,
        @NotNull Function<? super E, ? extends E> mapper);

    @NotNull
    <F> ListIterator<F, ? extends ListIterator<F, ?>> mapWhileBackward(
        @NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedFunction<? super E, F> mapper);

    @NotNull
    <F> ListIterator<F, ? extends ListIterator<F, ?>> mapWhileBackward(
        @NotNull Predicate<? super E> condition, @NotNull Function<? super E, F> mapper);

    @Override
    @NotNull
    ListIterator<E, ? extends ListIterator<E, ?>> mapWhileForward(
        @NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    @NotNull
    ListIterator<E, ? extends ListIterator<E, ?>> mapWhileForward(
        @NotNull Predicate<? super E> condition, @NotNull Function<? super E, ? extends E> mapper);

    @Override
    @NotNull
    ListIterator<E, T> materialize();

    @Override
    @NotNull
    ListIterator<E, T> max(@NotNull Comparator<? super E> comparator);

    @Override
    @NotNull
    ListIterator<E, T> min(@NotNull Comparator<? super E> comparator);

    @Override
    @NotNull
    ListIterator<E, T> minus(@Nullable E element);

    @Override
    @NotNull
    ListIterator<E, T> minusAll(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    ListIterator<E, T> minusFirst(@Nullable E element);

    @Override
    @NotNull
    ListIterator<E, T> minusLast(@Nullable E element);

    @NotNull
    ListIterator<E, T> moveBy(int maxElements);

    @NotNull
    ListIterator<E, T> moveTo(int index);

    @NotNull
    List<E, ? extends List<E, ?>> nextList();

    @Override
    @NotNull
    ListIterator<Boolean, ? extends ListIterator<Boolean, ?>> notExists(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<Boolean, ? extends ListIterator<Boolean, ?>> notExists(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @NotNull
    ListIterator<Boolean, ? extends ListIterator<Boolean, ?>> notExistsBackward(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @NotNull
    ListIterator<Boolean, ? extends ListIterator<Boolean, ?>> notExistsBackward(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<Boolean, ? extends ListIterator<Boolean, ?>> notExistsForward(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<Boolean, ? extends ListIterator<Boolean, ?>> notExistsForward(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<E, T> orElse(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    ListIterator<E, T> orElseGet(
        @NotNull Supplier<? extends java.lang.Iterable<? extends E>> supplier);

    @Override
    @NotNull
    Iterator<? extends ListIterator<E, T>, ? extends Iterator<? extends ListIterator<E, T>, ?>> partition(
        @Positive int numPartitions, @NotNull Function<? super E, Integer> indexExtractor);

    @Override
    @NotNull
    Iterator<? extends ListIterator<E, T>, ? extends Iterator<? extends ListIterator<E, T>, ?>> partition(
        @Positive int numPartitions, @NotNull IndexedFunction<? super E, Integer> indexExtractor);

    @Override
    @NotNull
    <K> Iterator<? extends ZipEntry<K, ? extends ListIterator<E, T>>, ? extends Iterator<? extends ZipEntry<K, ? extends ListIterator<E, T>>, ?>> partitionZip(
        @NotNull Function<? super E, K> keyExtractor);

    @Override
    @NotNull
    <K> Iterator<? extends ZipEntry<K, ? extends ListIterator<E, T>>, ? extends Iterator<? extends ZipEntry<K, ? extends ListIterator<E, T>>, ?>> partitionZip(
        @NotNull IndexedFunction<? super E, K> keyExtractor);

    @Override
    @NotNull
    ListIterator<E, T> peek(@NotNull Consumer<? super E> consumer);

    @Override
    @NotNull
    ListIterator<E, T> peek(@NotNull IndexedConsumer<? super E> consumer);

    @Override
    @NotNull
    ListIterator<E, T> peekExceptionally(@NotNull Consumer<? super Throwable> consumer);

    @Override
    @NotNull
    ListIterator<E, T> peekExceptionally(@NotNull IndexedConsumer<? super Throwable> consumer);

    @Override
    @NotNull
    ListIterator<E, T> plus(@Nullable E element);

    @Override
    @NotNull
    ListIterator<E, T> plusAll(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    ListIterator<E, T> reduce(@NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @NotNull
    ListIterator<E, T> reduceBackward(
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    ListIterator<E, T> reduceForward(
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    ListIterator<E, T> reduceWhile(@NotNull Predicate<? super E> condition,
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @NotNull
    ListIterator<E, T> reduceWhileBackward(@NotNull Predicate<? super E> condition,
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    ListIterator<E, T> reduceWhileForward(@NotNull Predicate<? super E> condition,
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    ListIterator<E, T> removeFirst(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<E, T> removeFirst(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<E, T> removeFirstSequence(@NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    ListIterator<E, T> removeLast(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<E, T> removeLast(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    ListIterator<E, T> removeLastSequence(@NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    ListIterator<E, T> removeSequence(@NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    ListIterator<E, T> removeSlice(int start);

    @Override
    @NotNull
    ListIterator<E, T> removeSlice(int start, int end);

    @NotNull
    ListIterator<E, T> replaceFirstSequence(@NotNull java.lang.Iterable<?> elements,
        @NotNull Function<? super java.util.List<E>, java.lang.Iterable<? extends E>> mapper);

    @NotNull
    ListIterator<E, T> replaceLastSequence(@NotNull java.lang.Iterable<?> elements,
        @NotNull Function<? super java.util.List<E>, java.lang.Iterable<? extends E>> mapper);

    @NotNull
    ListIterator<E, T> replaceSequence(@NotNull java.lang.Iterable<?> elements,
        @NotNull Function<? super java.util.List<E>, java.lang.Iterable<? extends E>> mapper);

    @NotNull
    ListIterator<E, T> replaceSequence(@NotNull java.lang.Iterable<?> elements,
        @NotNull IndexedFunction<? super java.util.List<E>, java.lang.Iterable<? extends E>> mapper);

    @Override
    @NotNull
    ListIterator<E, T> replaceSlice(int start, int end,
        @NotNull java.lang.Iterable<? extends E> patch);

    @Override
    @NotNull
    ListIterator<E, T> replaceSlice(int start, @NotNull java.lang.Iterable<? extends E> patch);

    @Override
    @NotNull
    ListIterator<E, T> resizeTo(@NotNegative int numElements, E padding);

    @Override
    int skip(int maxElements);

    @Override
    @NotNull
    ListIterator<E, T> slice(int start);

    @Override
    @NotNull
    ListIterator<E, T> slice(int start, int end);

    @Override
    @NotNull
    ListIterator<? extends ListIterator<E, T>, ? extends ListIterator<? extends ListIterator<E, T>, ?>> slidingWindow(
        @Positive int maxSize, @Positive int step);

    @Override
    @NotNull
    ListIterator<? extends ListIterator<E, T>, ? extends ListIterator<? extends ListIterator<E, T>, ?>> slidingWindowWithPadding(
        @Positive int size, @Positive int step, E padding);

    @Override
    @NotNull
    ListIterator<Boolean, ? extends ListIterator<Boolean, ?>> startsWith(
        @NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    <X extends Throwable> ListIterator<E, T> switchExceptionally(@NotNull Class<X> exceptionType,
        @NotNull Function<? super X, ? extends java.lang.Iterable<? extends E>> mapper);

    @Override
    @NotNull
    <X extends Throwable> ListIterator<E, T> switchExceptionally(@NotNull Class<X> exceptionType,
        @NotNull IndexedFunction<? super X, ? extends java.lang.Iterable<? extends E>> mapper);

    @Override
    @NotNull
    ListIterator<E, T> switchExceptionally(
        @NotNull Function<? super Throwable, ? extends java.lang.Iterable<? extends E>> mapper);

    @Override
    @NotNull
    ListIterator<E, T> switchExceptionally(
        @NotNull IndexedFunction<? super Throwable, ? extends java.lang.Iterable<? extends E>> mapper);

    @Override
    @NotNull
    ListIterator<E, T> symmetricDiff(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    ListIterator<E, T> takeFirst(int maxElements);

    @Override
    @NotNull
    ListIterator<E, T> takeFirstWhile(@NotNull IndexedPredicate<? super E> condition);

    @Override
    @NotNull
    ListIterator<E, T> takeFirstWhile(@NotNull Predicate<? super E> condition);

    @Override
    @NotNull
    ListIterator<E, T> takeLast(int maxElements);

    @NotNull
    ListIterator<E, T> takeLastWhile(@NotNull IndexedPredicate<? super E> condition);

    @NotNull
    ListIterator<E, T> takeLastWhile(@NotNull Predicate<? super E> condition);

    @Override
    @NotNull
    ListIterator<E, T> union(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    <F> ListIterator<ZipEntry<E, F>, ? extends ListIterator<ZipEntry<E, F>, ?>> zip(
        @NotNull java.lang.Iterable<F> elements);

    @Override
    @NotNull
    <F> ListIterator<ZipEntry<E, F>, ? extends ListIterator<ZipEntry<E, F>, ?>> zipWithPadding(
        @NotNull java.lang.Iterable<F> elements, E paddingLeft, F paddingRight);
  }

  public interface Set<E, T extends Set<E, T>> extends Collection<E, T>, java.util.Set<E> {

    @Override
    @NotNull
    <F> Set<F, ? extends Set<F, ?>> apply(
        @NotNull Function<? super T, java.lang.Iterable<F>> function);

    @Override
    @NotNull
    <F> Set<F, ? extends Set<F, ?>> cast();

    @Override
    @NotNull
    Set<E, T> clone();

    @Override
    @NotNull
    Set<E, T> clone(@NotNull Function<? super E, ? extends E> cloner);

    @Override
    @NotNull
    Set<Integer, ? extends Set<Integer, ?>> count();

    @Override
    @NotNull
    Set<E, T> diff(@NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    Set<E, T> distinct();

    @Override
    @NotNull
    <K> Set<E, T> distinctBy(@NotNull Function<? super E, K> keyExtractor);

    @Override
    @NotNull
    <K> Set<E, T> distinctBy(@NotNull IndexedFunction<? super E, K> keyExtractor);

    @Override
    @NotNull
    Set<E, T> dropFirst(int maxElements);

    @Override
    @NotNull
    Set<E, T> dropFirstWhile(@NotNull IndexedPredicate<? super E> condition);

    @Override
    @NotNull
    Set<E, T> dropFirstWhile(@NotNull Predicate<? super E> condition);

    @Override
    @NotNull
    Set<E, T> dropLast(int maxElements);

    @Override
    @NotNull
    Set<E, T> dropLastWhile(@NotNull IndexedPredicate<? super E> condition);

    @Override
    @NotNull
    Set<E, T> dropLastWhile(@NotNull Predicate<? super E> condition);

    @Override
    @NotNull
    Set<Boolean, ? extends Set<Boolean, ?>> endsWith(@NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    Set<Boolean, ? extends Set<Boolean, ?>> exists(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Set<Boolean, ? extends Set<Boolean, ?>> exists(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Set<Boolean, ? extends Set<Boolean, ?>> existsBackward(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Set<Boolean, ? extends Set<Boolean, ?>> existsBackward(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Set<Boolean, ? extends Set<Boolean, ?>> existsForward(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Set<Boolean, ? extends Set<Boolean, ?>> existsForward(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Set<E, T> filter(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Set<E, T> filter(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Set<E, T> filterWhile(@NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Set<E, T> filterWhile(@NotNull Predicate<? super E> condition,
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Set<E, T> find(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Set<E, T> find(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Set<E, T> findFirst(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Set<E, T> findFirst(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Set<Integer, ? extends Set<Integer, ?>> findFirstIndex(
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Set<Integer, ? extends Set<Integer, ?>> findFirstIndex(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Set<Integer, ? extends Set<Integer, ?>> findFirstIndexOf(@Nullable Object element);

    @Override
    @NotNull
    Set<Integer, ? extends Set<Integer, ?>> findFirstIndexOfSequence(
        @NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    Set<Integer, ? extends Set<Integer, ?>> findIndex(
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Set<Integer, ? extends Set<Integer, ?>> findIndex(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Set<Integer, ? extends Set<Integer, ?>> findIndexOf(@Nullable Object element);

    @Override
    @NotNull
    Set<E, T> findLast(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Set<E, T> findLast(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Set<Integer, ? extends Set<Integer, ?>> findLastIndex(
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Set<Integer, ? extends Set<Integer, ?>> findLastIndex(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Set<Integer, ? extends Set<Integer, ?>> findLastIndexOf(@Nullable Object element);

    @Override
    @NotNull
    Set<Integer, ? extends Set<Integer, ?>> findLastIndexOfSequence(
        @NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    <F> Set<F, ? extends Set<F, ?>> flatMap(
        @NotNull Function<? super E, ? extends java.lang.Iterable<F>> mapper);

    @Override
    @NotNull
    <F> Set<F, ? extends Set<F, ?>> flatMap(
        @NotNull IndexedFunction<? super E, ? extends java.lang.Iterable<F>> mapper);

    @Override
    @NotNull
    Set<E, T> flatMapWhile(@NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedFunction<? super E, ? extends java.lang.Iterable<? extends E>> mapper);

    @Override
    @NotNull
    Set<E, T> flatMapWhile(@NotNull Predicate<? super E> condition,
        @NotNull Function<? super E, ? extends java.lang.Iterable<? extends E>> mapper);

    @Override
    @NotNull
    <F> Set<F, ? extends Set<F, ?>> fold(F identity,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    @NotNull
    <F> Set<F, ? extends Set<F, ?>> foldBackward(F identity,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    @NotNull
    <F> Set<F, ? extends Set<F, ?>> foldForward(F identity,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    @NotNull
    <F> Set<F, ? extends Set<F, ?>> foldWhile(F identity, @NotNull Predicate<? super F> condition,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    @NotNull
    <F> Set<F, ? extends Set<F, ?>> foldWhileBackward(F identity,
        @NotNull Predicate<? super F> condition,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    @NotNull
    <F> Set<F, ? extends Set<F, ?>> foldWhileForward(F identity,
        @NotNull Predicate<? super F> condition,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    @NotNull
    Set<Boolean, ? extends Set<Boolean, ?>> includes(@Nullable Object element);

    @Override
    @NotNull
    Set<Boolean, ? extends Set<Boolean, ?>> includesAll(@NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    Set<Boolean, ? extends Set<Boolean, ?>> includesSequence(
        @NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    Set<E, T> interleave(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    Set<E, T> interleaveInner(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    Set<E, T> interleaveInnerWithPadding(@NotNull java.lang.Iterable<? extends E> elements,
        E paddingLeft, E paddingRight);

    @Override
    @NotNull
    Set<E, T> interleaveWithPadding(@NotNull java.lang.Iterable<? extends E> elements,
        E paddingLeft, E paddingRight);

    @Override
    @NotNull
    Set<E, T> intersect(@NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    <F> Set<F, ? extends Set<F, ?>> map(@NotNull Function<? super E, F> mapper);

    @Override
    @NotNull
    <F> Set<F, ? extends Set<F, ?>> map(@NotNull IndexedFunction<? super E, F> mapper);

    @Override
    @NotNull
    <F> Set<F, ? extends Set<F, ?>> mapBackward(@NotNull Function<? super E, F> mapper);

    @Override
    @NotNull
    <F> Set<F, ? extends Set<F, ?>> mapBackward(@NotNull IndexedFunction<? super E, F> mapper);

    @Override
    @NotNull
    <F> Set<F, ? extends Set<F, ?>> mapForward(@NotNull Function<? super E, F> mapper);

    @Override
    @NotNull
    <F> Set<F, ? extends Set<F, ?>> mapForward(@NotNull IndexedFunction<? super E, F> mapper);

    @Override
    @NotNull
    Set<E, ? extends Set<E, ?>> mapWhile(@NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Set<E, ? extends Set<E, ?>> mapWhile(@NotNull Predicate<? super E> condition,
        @NotNull Function<? super E, ? extends E> mapper);

    @Override
    @NotNull
    <F> Set<F, ? extends Set<F, ?>> mapWhileBackward(@NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedFunction<? super E, F> mapper);

    @Override
    @NotNull
    <F> Set<F, ? extends Set<F, ?>> mapWhileBackward(@NotNull Predicate<? super E> condition,
        @NotNull Function<? super E, F> mapper);

    @Override
    @NotNull
    Set<E, ? extends Set<E, ?>> mapWhileForward(@NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Set<E, ? extends Set<E, ?>> mapWhileForward(@NotNull Predicate<? super E> condition,
        @NotNull Function<? super E, ? extends E> mapper);

    @Override
    @NotNull
    Set<E, T> materialize();

    @Override
    @NotNull
    Set<E, T> max(@NotNull Comparator<? super E> comparator);

    @Override
    @NotNull
    Set<E, T> min(@NotNull Comparator<? super E> comparator);

    @Override
    @NotNull
    Set<E, T> minus(@Nullable E element);

    @Override
    @NotNull
    Set<E, T> minusAll(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    Set<E, T> minusFirst(@Nullable E element);

    @Override
    @NotNull
    Set<E, T> minusLast(@Nullable E element);

    @Override
    @NotNull
    Set<Boolean, ? extends Set<Boolean, ?>> notExists(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Set<Boolean, ? extends Set<Boolean, ?>> notExists(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Set<Boolean, ? extends Set<Boolean, ?>> notExistsBackward(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Set<Boolean, ? extends Set<Boolean, ?>> notExistsBackward(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Set<Boolean, ? extends Set<Boolean, ?>> notExistsForward(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Set<Boolean, ? extends Set<Boolean, ?>> notExistsForward(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Set<E, T> orElse(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    Set<E, T> orElseGet(@NotNull Supplier<? extends java.lang.Iterable<? extends E>> supplier);

    @Override
    @NotNull
    Iterator<? extends Set<E, T>, ? extends Iterator<? extends Set<E, T>, ?>> partition(
        @Positive int numPartitions, @NotNull Function<? super E, Integer> indexExtractor);

    @Override
    @NotNull
    Iterator<? extends Set<E, T>, ? extends Iterator<? extends Set<E, T>, ?>> partition(
        @Positive int numPartitions, @NotNull IndexedFunction<? super E, Integer> indexExtractor);

    @Override
    @NotNull
    <K> Iterator<? extends ZipEntry<K, ? extends Set<E, T>>, ? extends Iterator<? extends ZipEntry<K, ? extends Set<E, T>>, ?>> partitionZip(
        @NotNull Function<? super E, K> keyExtractor);

    @Override
    @NotNull
    <K> Iterator<? extends ZipEntry<K, ? extends Set<E, T>>, ? extends Iterator<? extends ZipEntry<K, ? extends Set<E, T>>, ?>> partitionZip(
        @NotNull IndexedFunction<? super E, K> keyExtractor);

    @Override
    @NotNull
    Set<E, T> plus(@Nullable E element);

    @Override
    @NotNull
    Set<E, T> plusAll(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    Set<E, T> reduce(@NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    Set<E, T> reduceBackward(@NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    Set<E, T> reduceForward(@NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    Set<E, T> reduceWhile(@NotNull Predicate<? super E> condition,
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    Set<E, T> reduceWhileBackward(@NotNull Predicate<? super E> condition,
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    Set<E, T> reduceWhileForward(@NotNull Predicate<? super E> condition,
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    @NotNull
    Set<E, T> removeFirst(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Set<E, T> removeFirst(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Set<E, T> removeFirstSequence(@NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    Set<E, T> removeLast(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    @NotNull
    Set<E, T> removeLast(@NotNull Predicate<? super E> predicate);

    @Override
    @NotNull
    Set<E, T> removeLastSequence(@NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    Set<E, T> removeSequence(@NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    Set<E, T> removeSlice(int start);

    @Override
    @NotNull
    Set<E, T> removeSlice(int start, int end);

    @NotNull
    Set<E, T> replaceFirstSequence(@NotNull java.lang.Iterable<?> elements,
        @NotNull Function<? super java.util.List<E>, java.lang.Iterable<? extends E>> mapper);

    @NotNull
    Set<E, T> replaceLastSequence(@NotNull java.lang.Iterable<?> elements,
        @NotNull Function<? super java.util.List<E>, java.lang.Iterable<? extends E>> mapper);

    @NotNull
    Set<E, T> replaceSequence(@NotNull java.lang.Iterable<?> elements,
        @NotNull Function<? super java.util.List<E>, java.lang.Iterable<? extends E>> mapper);

    @NotNull
    Set<E, T> replaceSequence(@NotNull java.lang.Iterable<?> elements,
        @NotNull IndexedFunction<? super java.util.List<E>, java.lang.Iterable<? extends E>> mapper);

    @Override
    @NotNull
    Set<E, T> replaceSlice(int start, int end, @NotNull java.lang.Iterable<? extends E> patch);

    @Override
    @NotNull
    Set<E, T> replaceSlice(int start, @NotNull java.lang.Iterable<? extends E> patch);

    @Override
    @NotNull
    Set<E, T> resizeTo(@NotNegative int numElements, E padding);

    @Override
    @NotNull
    Set<E, T> slice(int start);

    @Override
    @NotNull
    Set<E, T> slice(int start, int end);

    @Override
    @NotNull
    Set<? extends Set<E, T>, ? extends Set<? extends Set<E, T>, ?>> slidingWindow(
        @Positive int maxSize, @Positive int step);

    @Override
    @NotNull
    Set<? extends Set<E, T>, ? extends Set<? extends Set<E, T>, ?>> slidingWindowWithPadding(
        @Positive int size, @Positive int step, E padding);

    @Override
    @NotNull
    Set<Boolean, ? extends Set<Boolean, ?>> startsWith(@NotNull java.lang.Iterable<?> elements);

    @Override
    @NotNull
    Set<E, T> symmetricDiff(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    Set<E, T> takeFirst(int maxElements);

    @Override
    @NotNull
    Set<E, T> takeFirstWhile(@NotNull IndexedPredicate<? super E> condition);

    @Override
    @NotNull
    Set<E, T> takeFirstWhile(@NotNull Predicate<? super E> condition);

    @Override
    @NotNull
    Set<E, T> takeLast(int maxElements);

    @Override
    @NotNull
    Set<E, T> takeLastWhile(@NotNull IndexedPredicate<? super E> condition);

    @Override
    @NotNull
    Set<E, T> takeLastWhile(@NotNull Predicate<? super E> condition);

    @Override
    @NotNull
    Set<E, T> union(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    @NotNull
    <F> Set<ZipEntry<E, F>, ? extends Set<ZipEntry<E, F>, ?>> zip(
        @NotNull java.lang.Iterable<F> elements);

    @Override
    @NotNull
    <F> Set<ZipEntry<E, F>, ? extends Set<ZipEntry<E, F>, ?>> zipWithPadding(
        @NotNull java.lang.Iterable<F> elements, E paddingLeft, F paddingRight);
  }

  public interface Tro<E> extends Iterable<E, Tro<E>> {

    @NotNull
    <F> Tro<F> apply(@NotNull Function<? super Tro<E>, java.lang.Iterable<F>> function);

    @NotNull
    <F> Tro<F> cast();

    @Override
    @NotNull
    Tro<Integer> count();
  }
}
