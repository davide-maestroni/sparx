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

import static sparx1.util.function.Functions.toIndexedFunction;
import static sparx1.util.function.Functions.toIndexedPredicate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Comparator;
import sparx1.internal.lazy.iterator.ArrayToIteratorMaterializer;
import sparx1.internal.lazy.iterator.CharSequenceToIteratorMaterializer;
import sparx1.internal.lazy.iterator.DoubleArrayToIteratorMaterializer;
import sparx1.internal.lazy.iterator.ElementToIteratorMaterializer;
import sparx1.internal.lazy.iterator.FloatArrayToIteratorMaterializer;
import sparx1.internal.lazy.iterator.GeneratorToIteratorMaterializer;
import sparx1.internal.lazy.iterator.IntArrayToIteratorMaterializer;
import sparx1.internal.lazy.iterator.IteratorToIteratorMaterializer;
import sparx1.internal.lazy.iterator.LinesIteratorMaterializer;
import sparx1.internal.lazy.iterator.LongArrayToIteratorMaterializer;
import sparx1.internal.lazy.iterator.LoopToIteratorMaterializer;
import sparx1.internal.lazy.iterator.RepeatIteratorMaterializer;
import sparx1.util.Require;
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

    public static @NotNull <E> Iterator<E> of(final E el0) {
      return new LazyIterator<E>(new ElementToIteratorMaterializer<E>(el0));
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> Iterator<E> of(final E el0, final E el1) {
      return new LazyIterator<E>(new ArrayToIteratorMaterializer<E>(el0, el1));
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> Iterator<E> of(final E el0, final E el1, final E el2) {
      return new LazyIterator<E>(new ArrayToIteratorMaterializer<E>(el0, el1, el2));
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> Iterator<E> of(final E el0, final E el1, final E el2, final E el3) {
      return new LazyIterator<E>(new ArrayToIteratorMaterializer<E>(el0, el1, el2, el3));
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> Iterator<E> of(final E el0, final E el1, final E el2, final E el3,
        final E el4) {
      return new LazyIterator<E>(new ArrayToIteratorMaterializer<E>(el0, el1, el2, el3, el4));
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> Iterator<E> of(final E el0, final E el1, final E el2, final E el3,
        final E el4, final E el5) {
      return new LazyIterator<E>(new ArrayToIteratorMaterializer<E>(el0, el1, el2, el3, el4, el5));
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> Iterator<E> of(final E el0, final E el1, final E el2, final E el3,
        final E el4, final E el5, final E el6) {
      return new LazyIterator<E>(
          new ArrayToIteratorMaterializer<E>(el0, el1, el2, el3, el4, el5, el6));
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> Iterator<E> of(final E el0, final E el1, final E el2, final E el3,
        final E el4, final E el5, final E el6, final E el7) {
      return new LazyIterator<E>(
          new ArrayToIteratorMaterializer<E>(el0, el1, el2, el3, el4, el5, el6, el7));
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> Iterator<E> of(final E el0, final E el1, final E el2, final E el3,
        final E el4, final E el5, final E el6, final E el7, final E el8) {
      return new LazyIterator<E>(
          new ArrayToIteratorMaterializer<E>(el0, el1, el2, el3, el4, el5, el6, el7, el8));
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> Iterator<E> of(final E el0, final E el1, final E el2, final E el3,
        final E el4, final E el5, final E el6, final E el7, final E el8, final E el9) {
      return new LazyIterator<E>(
          new ArrayToIteratorMaterializer<E>(el0, el1, el2, el3, el4, el5, el6, el7, el8, el9));
    }

    public static @NotNull <E> Iterator<E> ofArray(final E... elements) {
      if (elements == null || elements.length == 0) {
        return LazyIterator.emptyIterator();
      }
      return new LazyIterator<E>(
          new ArrayToIteratorMaterializer<E>(Arrays.copyOf(elements, elements.length)));
    }

    public static @NotNull Iterator<Character> ofChars(final @NotNull CharSequence chars) {
      return new LazyIterator<Character>(
          new CharSequenceToIteratorMaterializer(Require.notNull(chars, "chars")));
    }

    public static @NotNull Iterator<Double> ofDoubles(final double... elements) {
      if (elements == null || elements.length == 0) {
        return LazyIterator.emptyIterator();
      }
      return new LazyIterator<Double>(
          new DoubleArrayToIteratorMaterializer(Arrays.copyOf(elements, elements.length)));
    }

    public static @NotNull Iterator<Float> ofFloats(final float... elements) {
      if (elements == null || elements.length == 0) {
        return LazyIterator.emptyIterator();
      }
      return new LazyIterator<Float>(
          new FloatArrayToIteratorMaterializer(Arrays.copyOf(elements, elements.length)));
    }

    public static @NotNull <G, E> Iterator<E> ofGenerator(final @NotNull G generator,
        final @NotNull IndexedPredicate<? super G> hasNextPredicate,
        final @NotNull IndexedFunction<? super G, ? extends E> nextFunction) {
      return new LazyIterator<E>(
          new GeneratorToIteratorMaterializer<G, E>(Require.notNull(generator, "generator"),
              Require.notNull(hasNextPredicate, "hasNextPredicate"),
              Require.notNull(nextFunction, "nextFunction")));
    }

    public static @NotNull <G, E> Iterator<E> ofGenerator(final @NotNull G generator,
        final @NotNull Predicate<? super G> hasNextPredicate,
        final @NotNull Function<? super G, ? extends E> nextFunction) {
      return new LazyIterator<E>(
          new GeneratorToIteratorMaterializer<G, E>(Require.notNull(generator, "generator"),
              toIndexedPredicate(hasNextPredicate, "hasNextPredicate"),
              toIndexedFunction(nextFunction, "nextFunction")));
    }

    public static @NotNull Iterator<Integer> ofInts(final int... elements) {
      if (elements == null || elements.length == 0) {
        return LazyIterator.emptyIterator();
      }
      return new LazyIterator<Integer>(
          new IntArrayToIteratorMaterializer(Arrays.copyOf(elements, elements.length)));
    }

    private static @NotNull Iterator<String> ofLines(final @NotNull BufferedReader reader) {
      return new LazyIterator<String>(
          new LinesIteratorMaterializer(Require.notNull(reader, "reader")));
    }

    public static @NotNull Iterator<String> ofLines(final @NotNull File file,
        final @Nullable Charset charset) throws FileNotFoundException {
      return ofLines(new FileInputStream(file), charset);
    }

    private static @NotNull Iterator<String> ofLines(final @NotNull InputStream inputStream,
        final @Nullable Charset charset) {
      return ofLines(new BufferedReader(new InputStreamReader(inputStream,
          charset != null ? charset : Charset.defaultCharset())));
    }

    public static @NotNull Iterator<String> ofLines(final @NotNull Reader reader) {
      if (reader instanceof BufferedReader) {
        return ofLines((BufferedReader) reader);
      }
      return ofLines(new BufferedReader(reader));
    }

    public static @NotNull Iterator<Long> ofLongs(final long... elements) {
      if (elements == null || elements.length == 0) {
        return LazyIterator.emptyIterator();
      }
      return new LazyIterator<Long>(
          new LongArrayToIteratorMaterializer(Arrays.copyOf(elements, elements.length)));
    }

    public static @NotNull <E> Iterator<E> ofLoop(final E initialValue,
        final @NotNull IndexedPredicate<? super E> predicate,
        final @NotNull IndexedFunction<? super E, ? extends E> update) {
      return new LazyIterator<E>(
          new LoopToIteratorMaterializer<E>(initialValue, Require.notNull(predicate, "predicate"),
              Require.notNull(update, "update")));
    }

    public static @NotNull <E> Iterator<E> ofLoop(final E initialValue,
        final @NotNull Predicate<? super E> predicate,
        final @NotNull Function<? super E, ? extends E> update) {
      return new LazyIterator<E>(new LoopToIteratorMaterializer<E>(initialValue,
          toIndexedPredicate(predicate, "predicate"), toIndexedFunction(update, "update")));
    }

    public static @NotNull <E> Iterator<E> times(final @NotNegative int count, final E element) {
      if (count == 0) {
        return LazyIterator.emptyIterator();
      }
      return new LazyIterator<E>(
          new RepeatIteratorMaterializer<E>(Require.notNegative(count, "count"), element));
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> Iterator<E> wrap(final @NotNull Iterable<? extends E> elements) {
      if (elements instanceof Iterator) {
        return (Iterator<E>) elements;
      }
      return LazyIterator.wrappedIterator(Require.notNull(elements, "elements"));
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> Iterator<E> wrap(
        final @NotNull java.util.Iterator<? extends E> elements) {
      if (elements instanceof Iterator) {
        return (Iterator<E>) elements;
      }
      return new LazyIterator<E>(
          new IteratorToIteratorMaterializer<E>(Require.notNull(elements, "elements")));
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
    public abstract @NotNull Iterator<Iterator<E>> partition(@Positive int numPartitions,
        @NotNull Function<? super E, Integer> indexExtractor);

    @Override
    public abstract @NotNull Iterator<Iterator<E>> partition(@Positive int numPartitions,
        @NotNull IndexedFunction<? super E, Integer> indexExtractor);

    @Override
    public abstract @NotNull <K> Iterator<ZipEntry<K, Iterator<E>>> partitionZip(
        @NotNull Function<? super E, K> keyExtractor);

    @Override
    public abstract @NotNull <K> Iterator<ZipEntry<K, Iterator<E>>> partitionZip(
        @NotNull IndexedFunction<? super E, K> keyExtractor);

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
    public abstract @NotNull Iterator<E> removeSlice(int start);

    @Override
    public abstract @NotNull Iterator<E> removeSlice(int start, int end);

    @Override
    public abstract @NotNull Iterator<E> replaceFirstSequence(
        @NotNull java.lang.Iterable<?> elements,
        @NotNull Function<? super java.util.List<E>, java.lang.Iterable<? extends E>> mapper);

    @Override
    public abstract @NotNull Iterator<E> replaceLastSequence(
        @NotNull java.lang.Iterable<?> elements,
        @NotNull Function<? super java.util.List<E>, java.lang.Iterable<? extends E>> mapper);

    @Override
    public abstract @NotNull Iterator<E> replaceSequence(@NotNull java.lang.Iterable<?> elements,
        @NotNull Function<? super java.util.List<E>, java.lang.Iterable<? extends E>> mapper);

    @Override
    public abstract @NotNull Iterator<E> replaceSequence(@NotNull java.lang.Iterable<?> elements,
        @NotNull IndexedFunction<? super java.util.List<E>, java.lang.Iterable<? extends E>> mapper);

    @Override
    public abstract @NotNull Iterator<E> replaceSlice(int start, int end,
        @NotNull java.lang.Iterable<? extends E> patch);

    @Override
    public abstract @NotNull Iterator<E> replaceSlice(int start,
        @NotNull Iterable<? extends E> patch);

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

  public abstract static class List<E> extends AbstractList<E, List<E>> implements
      itf.List<E, List<E>> {

    @Override
    public abstract @NotNull List<E> append(@Nullable E element);

    @Override
    public abstract @NotNull List<E> appendAll(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    public abstract @NotNull <F> List<F> apply(
        @NotNull Function<? super List<E>, java.lang.Iterable<F>> function);

    @Override
    public abstract @NotNull <F> List<F> cast();

    @Override
    public abstract @NotNull List<E> clone();

    @Override
    public abstract @NotNull List<E> clone(@NotNull Function<? super E, ? extends E> cloner);

    @Override
    public abstract @NotNull List<Integer> count();

    @Override
    public abstract @NotNull List<E> diff(@NotNull java.lang.Iterable<?> elements);

    @Override
    public abstract @NotNull List<E> distinct();

    @Override
    public abstract @NotNull <K> List<E> distinctBy(@NotNull Function<? super E, K> keyExtractor);

    @Override
    public abstract @NotNull <K> List<E> distinctBy(
        @NotNull IndexedFunction<? super E, K> keyExtractor);

    @Override
    public abstract @NotNull List<E> dropFirst(int maxElements);

    @Override
    public abstract @NotNull List<E> dropFirstWhile(@NotNull IndexedPredicate<? super E> condition);

    @Override
    public abstract @NotNull List<E> dropFirstWhile(@NotNull Predicate<? super E> condition);

    @Override
    public abstract @NotNull List<E> dropLast(int maxElements);

    @Override
    public abstract @NotNull List<E> dropLastWhile(@NotNull IndexedPredicate<? super E> condition);

    @Override
    public abstract @NotNull List<E> dropLastWhile(@NotNull Predicate<? super E> condition);

    @Override
    public abstract @NotNull List<Boolean> endsWith(@NotNull java.lang.Iterable<?> elements);

    @Override
    public abstract @NotNull List<Boolean> exists(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    public abstract @NotNull List<Boolean> exists(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @Override
    public abstract @NotNull List<Boolean> existsBackward(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    public abstract @NotNull List<Boolean> existsBackward(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @Override
    public abstract @NotNull List<Boolean> existsForward(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    public abstract @NotNull List<Boolean> existsForward(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @Override
    public abstract @NotNull List<E> filter(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    public abstract @NotNull List<E> filter(@NotNull Predicate<? super E> predicate);

    @Override
    public abstract @NotNull List<E> filterWhile(@NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    public abstract @NotNull List<E> filterWhile(@NotNull Predicate<? super E> condition,
        @NotNull Predicate<? super E> predicate);

    @Override
    public abstract @NotNull List<E> find(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    public abstract @NotNull List<E> find(@NotNull Predicate<? super E> predicate);

    @Override
    public abstract @NotNull List<E> findFirst(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    public abstract @NotNull List<E> findFirst(@NotNull Predicate<? super E> predicate);

    @Override
    public abstract @NotNull List<Integer> findFirstIndex(
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    public abstract @NotNull List<Integer> findFirstIndex(@NotNull Predicate<? super E> predicate);

    @Override
    public abstract @NotNull List<Integer> findFirstIndexOf(@Nullable Object element);

    @Override
    public abstract @NotNull List<Integer> findFirstIndexOfSequence(
        @NotNull java.lang.Iterable<?> elements);

    @Override
    public abstract @NotNull List<Integer> findIndex(
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    public abstract @NotNull List<Integer> findIndex(@NotNull Predicate<? super E> predicate);

    @Override
    public abstract @NotNull List<Integer> findIndexOf(@Nullable Object element);

    @Override
    public abstract @NotNull List<E> findLast(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    public abstract @NotNull List<E> findLast(@NotNull Predicate<? super E> predicate);

    @Override
    public abstract @NotNull List<Integer> findLastIndex(
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    public abstract @NotNull List<Integer> findLastIndex(@NotNull Predicate<? super E> predicate);

    @Override
    public abstract @NotNull List<Integer> findLastIndexOf(@Nullable Object element);

    @Override
    public abstract @NotNull List<Integer> findLastIndexOfSequence(
        @NotNull java.lang.Iterable<?> elements);

    @Override
    public abstract @NotNull <F> List<F> flatMap(
        @NotNull Function<? super E, ? extends java.lang.Iterable<F>> mapper);

    @Override
    public abstract @NotNull <F> List<F> flatMap(
        @NotNull IndexedFunction<? super E, ? extends java.lang.Iterable<F>> mapper);

    @Override
    public abstract @NotNull List<E> flatMapWhile(@NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedFunction<? super E, ? extends java.lang.Iterable<? extends E>> mapper);

    @Override
    public abstract @NotNull List<E> flatMapWhile(@NotNull Predicate<? super E> condition,
        @NotNull Function<? super E, ? extends java.lang.Iterable<? extends E>> mapper);

    @Override
    public abstract @NotNull <F> List<F> fold(F identity,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    public abstract @NotNull <F> List<F> foldBackward(F identity,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    public abstract @NotNull <F> List<F> foldForward(F identity,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    public abstract @NotNull <F> List<F> foldWhile(F identity,
        @NotNull Predicate<? super F> condition,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    public abstract @NotNull <F> List<F> foldWhileBackward(F identity,
        @NotNull Predicate<? super F> condition,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    public abstract @NotNull <F> List<F> foldWhileForward(F identity,
        @NotNull Predicate<? super F> condition,
        @NotNull BinaryFunction<? super F, ? super E, ? extends F> operation);

    @Override
    public abstract @NotNull List<Boolean> includes(@Nullable Object element);

    @Override
    public abstract @NotNull List<Boolean> includesAll(@NotNull java.lang.Iterable<?> elements);

    @Override
    public abstract @NotNull List<Boolean> includesSequence(
        @NotNull java.lang.Iterable<?> elements);

    public abstract @NotNull List<E> insertAfter(int numElements, E element);

    public abstract @NotNull List<E> insertAllAfter(int numElements,
        @NotNull java.lang.Iterable<? extends E> elements);

    @Override
    public abstract @NotNull List<E> interleave(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    public abstract @NotNull List<E> interleaveInner(
        @NotNull java.lang.Iterable<? extends E> elements);

    @Override
    public abstract @NotNull List<E> interleaveInnerWithPadding(
        @NotNull java.lang.Iterable<? extends E> elements, E paddingLeft, E paddingRight);

    @Override
    public abstract @NotNull List<E> interleaveWithPadding(
        @NotNull java.lang.Iterable<? extends E> elements, E paddingLeft, E paddingRight);

    @Override
    public abstract @NotNull List<E> intersect(@NotNull java.lang.Iterable<?> elements);

    @Override
    public abstract @NotNull ListIterator<E> listIterator();

    @Override
    public abstract @NotNull ListIterator<E> listIterator(int index);

    @Override
    public abstract @NotNull <F> List<F> map(@NotNull Function<? super E, F> mapper);

    @Override
    public abstract @NotNull <F> List<F> map(@NotNull IndexedFunction<? super E, F> mapper);

    @Override
    public abstract @NotNull <F> List<F> mapBackward(@NotNull Function<? super E, F> mapper);

    @Override
    public abstract @NotNull <F> List<F> mapBackward(@NotNull IndexedFunction<? super E, F> mapper);

    @Override
    public abstract @NotNull <F> List<F> mapForward(@NotNull Function<? super E, F> mapper);

    @Override
    public abstract @NotNull <F> List<F> mapForward(@NotNull IndexedFunction<? super E, F> mapper);

    @Override
    public abstract @NotNull List<E> mapWhile(@NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    public abstract @NotNull List<E> mapWhile(@NotNull Predicate<? super E> condition,
        @NotNull Function<? super E, ? extends E> mapper);

    @Override
    public abstract @NotNull <F> List<F> mapWhileBackward(
        @NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedFunction<? super E, F> mapper);

    @Override
    public abstract @NotNull <F> List<F> mapWhileBackward(@NotNull Predicate<? super E> condition,
        @NotNull Function<? super E, F> mapper);

    @Override
    public abstract @NotNull List<E> mapWhileForward(@NotNull IndexedPredicate<? super E> condition,
        @NotNull IndexedFunction<? super E, ? extends E> mapper);

    @Override
    public abstract @NotNull List<E> mapWhileForward(@NotNull Predicate<? super E> condition,
        @NotNull Function<? super E, ? extends E> mapper);

    @Override
    public abstract @NotNull List<E> materialize();

    @Override
    public abstract @NotNull List<E> max(@NotNull Comparator<? super E> comparator);

    @Override
    public abstract @NotNull List<E> min(@NotNull Comparator<? super E> comparator);

    @Override
    public abstract @NotNull List<E> minus(@Nullable E element);

    @Override
    public abstract @NotNull List<E> minusAll(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    public abstract @NotNull List<E> minusFirst(@Nullable E element);

    @Override
    public abstract @NotNull List<E> minusLast(@Nullable E element);

    @Override
    public abstract @NotNull List<Boolean> notExists(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    public abstract @NotNull List<Boolean> notExists(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @Override
    public abstract @NotNull List<Boolean> notExistsBackward(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    public abstract @NotNull List<Boolean> notExistsBackward(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @Override
    public abstract @NotNull List<Boolean> notExistsForward(boolean whenEmpty,
        @NotNull IndexedPredicate<? super E> predicate);

    @Override
    public abstract @NotNull List<Boolean> notExistsForward(boolean whenEmpty,
        @NotNull Predicate<? super E> predicate);

    @Override
    public abstract @NotNull List<E> orElse(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    public abstract @NotNull List<E> orElseGet(
        @NotNull Supplier<? extends java.lang.Iterable<? extends E>> supplier);

    @Override
    public abstract @NotNull Iterator<List<E>> partition(@Positive int numPartitions,
        @NotNull Function<? super E, Integer> indexExtractor);

    @Override
    public abstract @NotNull Iterator<List<E>> partition(@Positive int numPartitions,
        @NotNull IndexedFunction<? super E, Integer> indexExtractor);

    @Override
    public abstract @NotNull <K> Iterator<ZipEntry<K, List<E>>> partitionZip(
        @NotNull Function<? super E, K> keyExtractor);

    @Override
    public abstract @NotNull <K> Iterator<ZipEntry<K, List<E>>> partitionZip(
        @NotNull IndexedFunction<? super E, K> keyExtractor);

    @Override
    public abstract @NotNull List<E> plus(@Nullable E element);

    @Override
    public abstract @NotNull List<E> plusAll(@NotNull java.lang.Iterable<? extends E> elements);

    public abstract @NotNull List<E> prepend(E element);

    public abstract @NotNull List<E> prependAll(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    public abstract @NotNull List<E> reduce(
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    public abstract @NotNull List<E> reduceBackward(
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    public abstract @NotNull List<E> reduceForward(
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    public abstract @NotNull List<E> reduceWhile(@NotNull Predicate<? super E> condition,
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    public abstract @NotNull List<E> reduceWhileBackward(@NotNull Predicate<? super E> condition,
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    public abstract @NotNull List<E> reduceWhileForward(@NotNull Predicate<? super E> condition,
        @NotNull BinaryFunction<? super E, ? super E, ? extends E> operation);

    @Override
    public abstract @NotNull List<E> removeFirst(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    public abstract @NotNull List<E> removeFirst(@NotNull Predicate<? super E> predicate);

    @Override
    public abstract @NotNull List<E> removeFirstSequence(@NotNull java.lang.Iterable<?> elements);

    @Override
    public abstract @NotNull List<E> removeLast(@NotNull IndexedPredicate<? super E> predicate);

    @Override
    public abstract @NotNull List<E> removeLast(@NotNull Predicate<? super E> predicate);

    @Override
    public abstract @NotNull List<E> removeLastSequence(@NotNull java.lang.Iterable<?> elements);

    @Override
    public abstract @NotNull List<E> removeSequence(@NotNull java.lang.Iterable<?> elements);

    @Override
    public abstract @NotNull List<E> removeSlice(int start);

    @Override
    public abstract @NotNull List<E> removeSlice(int start, int end);

    public abstract @NotNull List<E> replaceFirstSequence(@NotNull java.lang.Iterable<?> elements,
        @NotNull Function<? super java.util.List<E>, java.lang.Iterable<? extends E>> mapper);

    public abstract @NotNull List<E> replaceLastSequence(@NotNull java.lang.Iterable<?> elements,
        @NotNull Function<? super java.util.List<E>, java.lang.Iterable<? extends E>> mapper);

    public abstract @NotNull List<E> replaceSequence(@NotNull java.lang.Iterable<?> elements,
        @NotNull Function<? super java.util.List<E>, java.lang.Iterable<? extends E>> mapper);

    public abstract @NotNull List<E> replaceSequence(@NotNull java.lang.Iterable<?> elements,
        @NotNull IndexedFunction<? super java.util.List<E>, java.lang.Iterable<? extends E>> mapper);

    @Override
    public abstract @NotNull List<E> replaceSlice(int start, int end,
        @NotNull java.lang.Iterable<? extends E> patch);

    @Override
    public abstract @NotNull List<E> replaceSlice(int start,
        @NotNull java.lang.Iterable<? extends E> patch);

    @Override
    public abstract @NotNull List<E> resizeTo(@NotNegative int numElements, E padding);

    public abstract @NotNull List<E> reverse();

    @Override
    public abstract @NotNull List<E> slice(int start);

    @Override
    public abstract @NotNull List<E> slice(int start, int end);

    @Override
    public abstract @NotNull List<List<E>> slidingWindow(@Positive int maxSize, @Positive int step);

    @Override
    public abstract @NotNull List<List<E>> slidingWindowWithPadding(@Positive int size,
        @Positive int step, E padding);

    public abstract @NotNull List<E> sorted(@NotNull Comparator<? super E> comparator);

    @Override
    public abstract @NotNull List<Boolean> startsWith(@NotNull java.lang.Iterable<?> elements);

    @Override
    public abstract @NotNull List<E> symmetricDiff(
        @NotNull java.lang.Iterable<? extends E> elements);

    @Override
    public abstract @NotNull List<E> takeFirst(int maxElements);

    @Override
    public abstract @NotNull List<E> takeFirstWhile(@NotNull IndexedPredicate<? super E> condition);

    @Override
    public abstract @NotNull List<E> takeFirstWhile(@NotNull Predicate<? super E> condition);

    @Override
    public abstract @NotNull List<E> takeLast(int maxElements);

    @Override
    public abstract @NotNull List<E> takeLastWhile(@NotNull IndexedPredicate<? super E> condition);

    @Override
    public abstract @NotNull List<E> takeLastWhile(@NotNull Predicate<? super E> condition);

    @Override
    public abstract @NotNull List<E> union(@NotNull java.lang.Iterable<? extends E> elements);

    @Override
    public abstract @NotNull <F> List<ZipEntry<E, F>> zip(@NotNull java.lang.Iterable<F> elements);

    @Override
    public abstract @NotNull <F> List<ZipEntry<E, F>> zipWithPadding(
        @NotNull java.lang.Iterable<F> elements, E paddingLeft, F paddingRight);
  }

  public abstract static class ListIterator<E> implements itf.ListIterator<E, ListIterator<E>> {

  }
}
