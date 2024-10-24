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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sparx.concurrent.ExecutionContext;
import sparx.internal.future.iterator.IteratorToIteratorFutureMaterializer;
import sparx.internal.future.list.ListToListFutureMaterializer;
import sparx.internal.lazy.ListMaterializerToIteratorMaterializer;
import sparx.internal.lazy.iterator.AppendAllIteratorMaterializer;
import sparx.internal.lazy.iterator.AppendIteratorMaterializer;
import sparx.internal.lazy.iterator.ArrayToIteratorMaterializer;
import sparx.internal.lazy.iterator.CharSequenceToIteratorMaterializer;
import sparx.internal.lazy.iterator.CollectionToIteratorMaterializer;
import sparx.internal.lazy.iterator.CountIteratorMaterializer;
import sparx.internal.lazy.iterator.CountWhereIteratorMaterializer;
import sparx.internal.lazy.iterator.DiffIteratorMaterializer;
import sparx.internal.lazy.iterator.DistinctByIteratorMaterializer;
import sparx.internal.lazy.iterator.DoubleArrayToIteratorMaterializer;
import sparx.internal.lazy.iterator.DropIteratorMaterializer;
import sparx.internal.lazy.iterator.DropRightIteratorMaterializer;
import sparx.internal.lazy.iterator.DropRightWhileIteratorMaterializer;
import sparx.internal.lazy.iterator.DropWhileIteratorMaterializer;
import sparx.internal.lazy.iterator.EachIteratorMaterializer;
import sparx.internal.lazy.iterator.ElementToIteratorMaterializer;
import sparx.internal.lazy.iterator.EmptyIteratorMaterializer;
import sparx.internal.lazy.iterator.EndsWithIteratorMaterializer;
import sparx.internal.lazy.iterator.ExistsIteratorMaterializer;
import sparx.internal.lazy.iterator.FinallyIteratorMaterializer;
import sparx.internal.lazy.iterator.FindFirstIteratorMaterializer;
import sparx.internal.lazy.iterator.FindIndexIteratorMaterializer;
import sparx.internal.lazy.iterator.FindIndexOfSliceIteratorMaterializer;
import sparx.internal.lazy.iterator.FindLastIndexIteratorMaterializer;
import sparx.internal.lazy.iterator.FindLastIndexOfSliceIteratorMaterializer;
import sparx.internal.lazy.iterator.FindLastIteratorMaterializer;
import sparx.internal.lazy.iterator.FlatMapAfterIteratorMaterializer;
import sparx.internal.lazy.iterator.FlatMapFirstWhereIteratorMaterializer;
import sparx.internal.lazy.iterator.FlatMapIteratorMaterializer;
import sparx.internal.lazy.iterator.FlatMapLastWhereIteratorMaterializer;
import sparx.internal.lazy.iterator.FlatMapWhereIteratorMaterializer;
import sparx.internal.lazy.iterator.FloatArrayToIteratorMaterializer;
import sparx.internal.lazy.iterator.FoldLeftIteratorMaterializer;
import sparx.internal.lazy.iterator.FoldLeftWhileIteratorMaterializer;
import sparx.internal.lazy.iterator.FoldRightIteratorMaterializer;
import sparx.internal.lazy.iterator.FoldRightWhileIteratorMaterializer;
import sparx.internal.lazy.iterator.GeneratorToIteratorMaterializer;
import sparx.internal.lazy.iterator.IncludesAllIteratorMaterializer;
import sparx.internal.lazy.iterator.IncludesSliceIteratorMaterializer;
import sparx.internal.lazy.iterator.InsertAfterIteratorMaterializer;
import sparx.internal.lazy.iterator.InsertAllAfterIteratorMaterializer;
import sparx.internal.lazy.iterator.InsertAllIteratorMaterializer;
import sparx.internal.lazy.iterator.InsertIteratorMaterializer;
import sparx.internal.lazy.iterator.IntArrayToIteratorMaterializer;
import sparx.internal.lazy.iterator.IntersectIteratorMaterializer;
import sparx.internal.lazy.iterator.IteratorMaterializer;
import sparx.internal.lazy.iterator.IteratorToIteratorMaterializer;
import sparx.internal.lazy.iterator.LinesIteratorMaterializer;
import sparx.internal.lazy.iterator.ListToIteratorMaterializer;
import sparx.internal.lazy.iterator.LongArrayToIteratorMaterializer;
import sparx.internal.lazy.iterator.LoopToIteratorMaterializer;
import sparx.internal.lazy.iterator.MapAfterIteratorMaterializer;
import sparx.internal.lazy.iterator.MapFirstWhereIteratorMaterializer;
import sparx.internal.lazy.iterator.MapIteratorMaterializer;
import sparx.internal.lazy.iterator.MapLastWhereIteratorMaterializer;
import sparx.internal.lazy.iterator.MaxIteratorMaterializer;
import sparx.internal.lazy.iterator.OrElseIteratorMaterializer;
import sparx.internal.lazy.iterator.PeekExceptionallyIteratorMaterializer;
import sparx.internal.lazy.iterator.PeekIteratorMaterializer;
import sparx.internal.lazy.iterator.ReduceLeftIteratorMaterializer;
import sparx.internal.lazy.iterator.ReduceRightIteratorMaterializer;
import sparx.internal.lazy.iterator.RemoveAfterIteratorMaterializer;
import sparx.internal.lazy.iterator.RemoveFirstWhereIteratorMaterializer;
import sparx.internal.lazy.iterator.RemoveLastWhereIteratorMaterializer;
import sparx.internal.lazy.iterator.RemoveSliceIteratorMaterializer;
import sparx.internal.lazy.iterator.RemoveWhereIteratorMaterializer;
import sparx.internal.lazy.iterator.RepeatIteratorMaterializer;
import sparx.internal.lazy.iterator.ReplaceSliceIteratorMaterializer;
import sparx.internal.lazy.iterator.ResizeIteratorMaterializer;
import sparx.internal.lazy.iterator.SliceIteratorMaterializer;
import sparx.internal.lazy.iterator.SlidingWindowIteratorMaterializer;
import sparx.internal.lazy.iterator.StartsWithIteratorMaterializer;
import sparx.internal.lazy.iterator.SwitchExceptionallyIteratorMaterializer;
import sparx.internal.lazy.iterator.SymmetricDiffIteratorMaterializer;
import sparx.internal.lazy.iterator.TakeIteratorMaterializer;
import sparx.internal.lazy.iterator.TakeRightIteratorMaterializer;
import sparx.internal.lazy.iterator.TakeRightWhileIteratorMaterializer;
import sparx.internal.lazy.iterator.TakeWhileIteratorMaterializer;
import sparx.internal.lazy.iterator.UnionIteratorMaterializer;
import sparx.internal.lazy.list.AppendAllListMaterializer;
import sparx.internal.lazy.list.AppendListMaterializer;
import sparx.internal.lazy.list.ArrayToListMaterializer;
import sparx.internal.lazy.list.CharSequenceToListMaterializer;
import sparx.internal.lazy.list.CollectionToListMaterializer;
import sparx.internal.lazy.list.CountListMaterializer;
import sparx.internal.lazy.list.CountWhereListMaterializer;
import sparx.internal.lazy.list.DiffListMaterializer;
import sparx.internal.lazy.list.DistinctByListMaterializer;
import sparx.internal.lazy.list.DoubleArrayToListMaterializer;
import sparx.internal.lazy.list.DropListMaterializer;
import sparx.internal.lazy.list.DropRightListMaterializer;
import sparx.internal.lazy.list.DropRightWhileListMaterializer;
import sparx.internal.lazy.list.DropWhileListMaterializer;
import sparx.internal.lazy.list.EachListMaterializer;
import sparx.internal.lazy.list.ElementToListMaterializer;
import sparx.internal.lazy.list.EmptyListMaterializer;
import sparx.internal.lazy.list.EndsWithListMaterializer;
import sparx.internal.lazy.list.ExistsListMaterializer;
import sparx.internal.lazy.list.FindFirstListMaterializer;
import sparx.internal.lazy.list.FindIndexListMaterializer;
import sparx.internal.lazy.list.FindIndexOfSliceListMaterializer;
import sparx.internal.lazy.list.FindLastIndexListMaterializer;
import sparx.internal.lazy.list.FindLastIndexOfSliceListMaterializer;
import sparx.internal.lazy.list.FindLastListMaterializer;
import sparx.internal.lazy.list.FlatMapAfterListMaterializer;
import sparx.internal.lazy.list.FlatMapFirstWhereListMaterializer;
import sparx.internal.lazy.list.FlatMapLastWhereListMaterializer;
import sparx.internal.lazy.list.FlatMapListMaterializer;
import sparx.internal.lazy.list.FlatMapWhereListMaterializer;
import sparx.internal.lazy.list.FloatArrayToListMaterializer;
import sparx.internal.lazy.list.FoldLeftListMaterializer;
import sparx.internal.lazy.list.FoldLeftWhileListMaterializer;
import sparx.internal.lazy.list.FoldRightListMaterializer;
import sparx.internal.lazy.list.FoldRightWhileListMaterializer;
import sparx.internal.lazy.list.IncludesAllListMaterializer;
import sparx.internal.lazy.list.IncludesSliceListMaterializer;
import sparx.internal.lazy.list.InsertAfterListMaterializer;
import sparx.internal.lazy.list.InsertAllAfterListMaterializer;
import sparx.internal.lazy.list.IntArrayToListMaterializer;
import sparx.internal.lazy.list.IntersectListMaterializer;
import sparx.internal.lazy.list.IteratorToListMaterializer;
import sparx.internal.lazy.list.ListMaterializer;
import sparx.internal.lazy.list.ListToListMaterializer;
import sparx.internal.lazy.list.LongArrayToListMaterializer;
import sparx.internal.lazy.list.MapAfterListMaterializer;
import sparx.internal.lazy.list.MapFirstWhereListMaterializer;
import sparx.internal.lazy.list.MapLastWhereListMaterializer;
import sparx.internal.lazy.list.MapListMaterializer;
import sparx.internal.lazy.list.MaxListMaterializer;
import sparx.internal.lazy.list.OrElseListMaterializer;
import sparx.internal.lazy.list.PrependAllListMaterializer;
import sparx.internal.lazy.list.PrependListMaterializer;
import sparx.internal.lazy.list.ReduceLeftListMaterializer;
import sparx.internal.lazy.list.ReduceRightListMaterializer;
import sparx.internal.lazy.list.RemoveAfterListMaterializer;
import sparx.internal.lazy.list.RemoveFirstWhereListMaterializer;
import sparx.internal.lazy.list.RemoveLastWhereListMaterializer;
import sparx.internal.lazy.list.RemoveSliceListMaterializer;
import sparx.internal.lazy.list.RemoveWhereListMaterializer;
import sparx.internal.lazy.list.RepeatListMaterializer;
import sparx.internal.lazy.list.ReplaceSliceListMaterializer;
import sparx.internal.lazy.list.ResizeListMaterializer;
import sparx.internal.lazy.list.ReverseListMaterializer;
import sparx.internal.lazy.list.SingleFlatMapListMaterializer;
import sparx.internal.lazy.list.SingleFlatMapWhereListMaterializer;
import sparx.internal.lazy.list.SingleMapListMaterializer;
import sparx.internal.lazy.list.SingleMapWhereListMaterializer;
import sparx.internal.lazy.list.SliceListMaterializer;
import sparx.internal.lazy.list.SlidingWindowListMaterializer;
import sparx.internal.lazy.list.SlidingWindowListMaterializer.Splitter;
import sparx.internal.lazy.list.SortedListMaterializer;
import sparx.internal.lazy.list.StartsWithListMaterializer;
import sparx.internal.lazy.list.SymmetricDiffListMaterializer;
import sparx.internal.lazy.list.TakeListMaterializer;
import sparx.internal.lazy.list.TakeRightListMaterializer;
import sparx.internal.lazy.list.TakeRightWhileListMaterializer;
import sparx.internal.lazy.list.TakeWhileListMaterializer;
import sparx.util.Require;
import sparx.util.UncheckedException;
import sparx.util.annotation.NotNegative;
import sparx.util.annotation.Positive;
import sparx.util.function.Action;
import sparx.util.function.BinaryFunction;
import sparx.util.function.Consumer;
import sparx.util.function.Function;
import sparx.util.function.IndexedConsumer;
import sparx.util.function.IndexedFunction;
import sparx.util.function.IndexedPredicate;
import sparx.util.function.Predicate;
import sparx.util.function.Supplier;

public class lazy extends Sparx {

  private static final IndexedFunction<?, ?> INDEXED_IDENTITY = new IndexedFunction<Object, Object>() {
    @Override
    public Object apply(final int index, final Object param) {
      return param;
    }
  };

  private lazy() {
  }

  static int getKnownSize(final Iterable<?> elements) {
    if (elements instanceof List) {
      return ((List<?>) elements).knownSize();
    }
    if (elements instanceof Iterator) {
      return ((Iterator<?>) elements).knownSize();
    }
    if (elements instanceof Collection) {
      return ((Collection<?>) elements).size();
    }
    return -1;
  }

  @NotNull
  @SuppressWarnings("unchecked")
  static <E> IndexedFunction<E, E> indexedIdentity() {
    return (IndexedFunction<E, E>) INDEXED_IDENTITY;
  }

  public static class Iterator<E> implements itf.Iterator<E> {

    private static final Iterator<?> EMPTY_ITERATOR = new Iterator<Object>(
        EmptyIteratorMaterializer.instance());
    private static final Function<? extends java.util.List<?>, ? extends Iterator<?>> FROM_JAVA_LIST = new Function<java.util.List<Object>, Iterator<Object>>() {
      @Override
      public Iterator<Object> apply(final java.util.List<Object> param) {
        return new Iterator<Object>(new ListToIteratorMaterializer<Object>(param));
      }
    };

    private final IteratorMaterializer<E> materializer;

    Iterator(@NotNull final IteratorMaterializer<E> materializer) {
      this.materializer = materializer;
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> Iterator<E> of() {
      return (Iterator<E>) EMPTY_ITERATOR;
    }

    public static @NotNull <E> Iterator<E> of(final E first) {
      return new Iterator<E>(new ElementToIteratorMaterializer<E>(first));
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> Iterator<E> of(final E first, final E second) {
      return new Iterator<E>(new ArrayToIteratorMaterializer<E>(first, second));
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> Iterator<E> of(final E first, final E second, final E third) {
      return new Iterator<E>(new ArrayToIteratorMaterializer<E>(first, second, third));
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> Iterator<E> of(final E first, final E second, final E third,
        final E fourth) {
      return new Iterator<E>(new ArrayToIteratorMaterializer<E>(first, second, third, fourth));
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> Iterator<E> of(final E first, final E second, final E third,
        final E fourth, final E fifth) {
      return new Iterator<E>(
          new ArrayToIteratorMaterializer<E>(first, second, third, fourth, fifth));
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> Iterator<E> of(final E first, final E second, final E third,
        final E fourth, final E fifth, final E sixth) {
      return new Iterator<E>(
          new ArrayToIteratorMaterializer<E>(first, second, third, fourth, fifth, sixth));
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> Iterator<E> of(final E first, final E second, final E third,
        final E fourth, final E fifth, final E sixth, final E seventh) {
      return new Iterator<E>(
          new ArrayToIteratorMaterializer<E>(first, second, third, fourth, fifth, sixth, seventh));
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> Iterator<E> of(final E first, final E second, final E third,
        final E fourth, final E fifth, final E sixth, final E seventh, final E eighth) {
      return new Iterator<E>(
          new ArrayToIteratorMaterializer<E>(first, second, third, fourth, fifth, sixth, seventh,
              eighth));
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> Iterator<E> of(final E first, final E second, final E third,
        final E fourth, final E fifth, final E sixth, final E seventh, final E eighth,
        final E ninth) {
      return new Iterator<E>(
          new ArrayToIteratorMaterializer<E>(first, second, third, fourth, fifth, sixth, seventh,
              eighth, ninth));
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> Iterator<E> of(final E first, final E second, final E third,
        final E fourth, final E fifth, final E sixth, final E seventh, final E eighth,
        final E ninth, final E tenth) {
      return new Iterator<E>(
          new ArrayToIteratorMaterializer<E>(first, second, third, fourth, fifth, sixth, seventh,
              eighth, ninth, tenth));
    }

    public static @NotNull <E> Iterator<E> ofArray(final E... elements) {
      if (elements == null) {
        return Iterator.of();
      }
      return new Iterator<E>(
          new ArrayToIteratorMaterializer<E>(Arrays.copyOf(elements, elements.length)));
    }

    public static @NotNull Iterator<Character> ofChars(@NotNull final CharSequence chars) {
      return new Iterator<Character>(
          new CharSequenceToIteratorMaterializer(Require.notNull(chars, "chars")));
    }

    public static @NotNull Iterator<Double> ofDoubles(final double... elements) {
      if (elements == null) {
        return Iterator.of();
      }
      return new Iterator<Double>(
          new DoubleArrayToIteratorMaterializer(Arrays.copyOf(elements, elements.length)));
    }

    public static @NotNull Iterator<Float> ofFloats(final float... elements) {
      if (elements == null) {
        return Iterator.of();
      }
      return new Iterator<Float>(
          new FloatArrayToIteratorMaterializer(Arrays.copyOf(elements, elements.length)));
    }

    public static @NotNull <G, E> Iterator<E> ofGenerator(@NotNull final G generator,
        @NotNull final IndexedPredicate<? super G> hasNextPredicate,
        @NotNull final IndexedFunction<? super G, ? extends E> nextFunction) {
      return new Iterator<E>(
          new GeneratorToIteratorMaterializer<G, E>(Require.notNull(generator, "generator"),
              Require.notNull(hasNextPredicate, "hasNextPredicate"),
              Require.notNull(nextFunction, "nextFunction")));
    }

    public static @NotNull <G, E> Iterator<E> ofGenerator(@NotNull final G generator,
        @NotNull final Predicate<? super G> hasNextPredicate,
        @NotNull final Function<? super G, ? extends E> nextFunction) {
      return new Iterator<E>(
          new GeneratorToIteratorMaterializer<G, E>(Require.notNull(generator, "generator"),
              toIndexedPredicate(Require.notNull(hasNextPredicate, "hasNextPredicate")),
              toIndexedFunction(Require.notNull(nextFunction, "nextFunction"))));
    }

    public static @NotNull Iterator<Integer> ofInts(final int... elements) {
      if (elements == null) {
        return Iterator.of();
      }
      return new Iterator<Integer>(
          new IntArrayToIteratorMaterializer(Arrays.copyOf(elements, elements.length)));
    }

    private static @NotNull Iterator<String> ofLines(@NotNull final BufferedReader reader) {
      return new Iterator<String>(new LinesIteratorMaterializer(Require.notNull(reader, "reader")));
    }

    public static @NotNull Iterator<String> ofLines(@NotNull final File file,
        @Nullable final Charset charset) throws FileNotFoundException {
      return ofLines(new FileInputStream(file), charset);
    }

    private static @NotNull Iterator<String> ofLines(@NotNull final InputStream inputStream,
        @Nullable final Charset charset) {
      return ofLines(new BufferedReader(new InputStreamReader(inputStream,
          charset != null ? charset : Charset.defaultCharset())));
    }

    public static @NotNull Iterator<String> ofLines(@NotNull final Reader reader) {
      if (reader instanceof BufferedReader) {
        return ofLines((BufferedReader) reader);
      }
      return ofLines(new BufferedReader(reader));
    }

    public static @NotNull Iterator<Long> ofLongs(final long... elements) {
      if (elements == null) {
        return Iterator.of();
      }
      return new Iterator<Long>(
          new LongArrayToIteratorMaterializer(Arrays.copyOf(elements, elements.length)));
    }

    public static @NotNull <E> Iterator<E> ofLoop(final E initialValue,
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends E> update) {
      return new Iterator<E>(
          new LoopToIteratorMaterializer<E>(initialValue, Require.notNull(predicate, "predicate"),
              Require.notNull(update, "update")));
    }

    public static @NotNull <E> Iterator<E> ofLoop(final E initialValue,
        @NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends E> update) {
      return new Iterator<E>(new LoopToIteratorMaterializer<E>(initialValue,
          toIndexedPredicate(Require.notNull(predicate, "predicate")),
          toIndexedFunction(Require.notNull(update, "update"))));
    }

    public static @NotNull <E> Iterator<E> times(final int count, final E element) {
      if (count == 0) {
        return Iterator.of();
      }
      return new Iterator<E>(
          new RepeatIteratorMaterializer<E>(Require.notNegative(count, "count"), element));
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> Iterator<E> wrap(@NotNull final Iterable<? extends E> elements) {
      if (elements instanceof Iterator) {
        return (Iterator<E>) elements;
      }
      return new Iterator<E>(getElementsMaterializer(Require.notNull(elements, "elements")));
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> Iterator<E> wrap(
        @NotNull final java.util.Iterator<? extends E> elements) {
      if (elements instanceof Iterator) {
        return (Iterator<E>) elements;
      }
      return new Iterator<E>(
          new IteratorToIteratorMaterializer<E>(Require.notNull(elements, "elements")));
    }

    @SuppressWarnings("unchecked")
    private static @NotNull <E> IteratorMaterializer<E> getElementsMaterializer(
        @NotNull final Iterable<? extends E> elements) {
      if (elements instanceof Iterator) {
        return ((Iterator<E>) elements).materializer;
      }
      if (elements instanceof List) {
        final ListMaterializer<E> materializer = ((List<E>) elements).materializer;
        if (materializer.knownSize() == 0) {
          return EmptyIteratorMaterializer.instance();
        }
        return new ListMaterializerToIteratorMaterializer<E>(materializer);
      }
      if (elements instanceof java.util.List) {
        final java.util.List<E> list = (java.util.List<E>) elements;
        if (list.isEmpty()) {
          return EmptyIteratorMaterializer.instance();
        }
        return new ListToIteratorMaterializer<E>(list);
      }
      if (elements instanceof Collection) {
        final Collection<E> collection = (Collection<E>) elements;
        if (collection.isEmpty()) {
          return EmptyIteratorMaterializer.instance();
        }
        return new CollectionToIteratorMaterializer<E>(collection);
      }
      return new IteratorToIteratorMaterializer<E>(elements.iterator());
    }

    private static @NotNull <E, F> IndexedFunction<E, IteratorMaterializer<F>> getElementToMaterializer(
        @NotNull final Function<? super E, ? extends Iterable<? extends F>> mapper) {
      return new IndexedFunction<E, IteratorMaterializer<F>>() {
        @Override
        public IteratorMaterializer<F> apply(final int index, final E element) throws Exception {
          return getElementsMaterializer(mapper.apply(element));
        }
      };
    }

    private static @NotNull <E, F> IndexedFunction<E, IteratorMaterializer<F>> getElementToMaterializer(
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends F>> mapper) {
      return new IndexedFunction<E, IteratorMaterializer<F>>() {
        @Override
        public IteratorMaterializer<F> apply(final int index, final E element) throws Exception {
          return getElementsMaterializer(mapper.apply(index, element));
        }
      };
    }

    private static @NotNull <E, T extends Throwable> IndexedFunction<Throwable, IteratorMaterializer<E>> getExceptionToMaterializer(
        @NotNull final Class<T> exceptionType,
        @NotNull final Function<? super T, ? extends Iterable<? extends E>> mapper) {
      return new IndexedFunction<Throwable, IteratorMaterializer<E>>() {
        @Override
        @SuppressWarnings("unchecked")
        public IteratorMaterializer<E> apply(final int index, final Throwable exception)
            throws Exception {
          if (exceptionType.isInstance(exception)) {
            return getElementsMaterializer(mapper.apply((T) exception));
          }
          if (exception instanceof Exception) {
            throw (Exception) exception;
          }
          throw UncheckedException.throwUnchecked(exception);
        }
      };
    }

    private static @NotNull <E, T extends Throwable> IndexedFunction<Throwable, IteratorMaterializer<E>> getExceptionToMaterializer(
        @NotNull final Class<T> exceptionType,
        @NotNull final IndexedFunction<? super T, ? extends Iterable<? extends E>> mapper) {
      return new IndexedFunction<Throwable, IteratorMaterializer<E>>() {
        @Override
        @SuppressWarnings("unchecked")
        public IteratorMaterializer<E> apply(final int index, final Throwable exception)
            throws Exception {
          if (exceptionType.isInstance(exception)) {
            return getElementsMaterializer(mapper.apply(index, (T) exception));
          }
          if (exception instanceof Exception) {
            throw (Exception) exception;
          }
          if (exception instanceof Error) {
            throw (Error) exception;
          }
          throw UncheckedException.throwUnchecked(exception);
        }
      };
    }

    @Override
    public @NotNull Iterator<E> append(final E element) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return new Iterator<E>(new ElementToIteratorMaterializer<E>(element));
      }
      return new Iterator<E>(new AppendIteratorMaterializer<E>(materializer, element));
    }

    @Override
    public @NotNull Iterator<E> appendAll(@NotNull final Iterable<? extends E> elements) {
      final IteratorMaterializer<E> materializer = this.materializer;
      final IteratorMaterializer<E> elementsMaterializer = getElementsMaterializer(
          Require.notNull(elements, "elements"));
      if (materializer.knownSize() == 0) {
        return new Iterator<E>(elementsMaterializer);
      }
      return new Iterator<E>(
          new AppendAllIteratorMaterializer<E>(materializer, elementsMaterializer));
    }

    @Override
    public <T> T apply(@NotNull final Function<? super itf.Sequence<E>, T> mapper) {
      return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <F> Iterator<F> as() {
      return (Iterator<F>) this;
    }

    @Override
    public @NotNull Iterator<Integer> count() {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return Iterator.of(0);
      }
      return new Iterator<Integer>(new CountIteratorMaterializer<E>(materializer));
    }

    @Override
    public @NotNull Iterator<Integer> countWhere(
        @NotNull final IndexedPredicate<? super E> predicate) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return Iterator.of(0);
      }
      return new Iterator<Integer>(new CountWhereIteratorMaterializer<E>(materializer,
          Require.notNull(predicate, "predicate")));
    }

    @Override
    public @NotNull Iterator<Integer> countWhere(@NotNull final Predicate<? super E> predicate) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return Iterator.of(0);
      }
      return new Iterator<Integer>(new CountWhereIteratorMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull Iterator<E> diff(@NotNull final Iterable<?> elements) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      if (getKnownSize(elements) == 0) {
        return iterator();
      }
      return new Iterator<E>(new DiffIteratorMaterializer<E>(materializer,
          getElementsMaterializer(Require.notNull(elements, "elements"))));
    }

    @Override
    public @NotNull Iterator<E> distinct() {
      return distinctBy(indexedIdentity());
    }

    @Override
    public @NotNull <K> Iterator<E> distinctBy(@NotNull final Function<? super E, K> keyExtractor) {
      final IteratorMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return this;
      }
      if (knownSize == 1) {
        return iterator();
      }
      return new Iterator<E>(new DistinctByIteratorMaterializer<E, K>(materializer,
          toIndexedFunction(Require.notNull(keyExtractor, "keyExtractor"))));
    }

    @Override
    public @NotNull <K> Iterator<E> distinctBy(
        @NotNull final IndexedFunction<? super E, K> keyExtractor) {
      final IteratorMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return this;
      }
      if (knownSize == 1) {
        return iterator();
      }
      return new Iterator<E>(new DistinctByIteratorMaterializer<E, K>(materializer,
          Require.notNull(keyExtractor, "keyExtractor")));
    }

    @Override
    public void doFor(@NotNull final Consumer<? super E> consumer) {
      try {
        final IteratorMaterializer<E> materializer = this.materializer;
        while (materializer.materializeHasNext()) {
          consumer.accept(materializer.materializeNext());
        }
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public void doFor(@NotNull final IndexedConsumer<? super E> consumer) {
      try {
        final IteratorMaterializer<E> materializer = this.materializer;
        int i = 0;
        while (materializer.materializeHasNext()) {
          consumer.accept(i++, materializer.materializeNext());
        }
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public void doWhile(@NotNull IndexedPredicate<? super E> predicate) {
      try {
        final IteratorMaterializer<E> materializer = this.materializer;
        int i = 0;
        while (materializer.materializeHasNext()) {
          if (!predicate.test(i++, materializer.materializeNext())) {
            break;
          }
        }
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public void doWhile(@NotNull final IndexedPredicate<? super E> condition,
        @NotNull final IndexedConsumer<? super E> consumer) {
      try {
        final IteratorMaterializer<E> materializer = this.materializer;
        int i = 0;
        while (materializer.materializeHasNext()) {
          final E next = materializer.materializeNext();
          if (!condition.test(i, next)) {
            break;
          }
          consumer.accept(i, next);
          ++i;
        }
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public void doWhile(@NotNull final Predicate<? super E> predicate) {
      try {
        final IteratorMaterializer<E> materializer = this.materializer;
        while (materializer.materializeHasNext()) {
          if (!predicate.test(materializer.materializeNext())) {
            break;
          }
        }
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public void doWhile(@NotNull final Predicate<? super E> condition,
        @NotNull final Consumer<? super E> consumer) {
      try {
        final IteratorMaterializer<E> materializer = this.materializer;
        while (materializer.materializeHasNext()) {
          final E next = materializer.materializeNext();
          if (!condition.test(next)) {
            break;
          }
          consumer.accept(next);
        }
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public @NotNull Iterator<E> drop(final int maxElements) {
      if (maxElements == Integer.MAX_VALUE) {
        return Iterator.of();
      }
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      if (maxElements <= 0) {
        return iterator();
      }
      return new Iterator<E>(new DropIteratorMaterializer<E>(materializer, maxElements));
    }

    @Override
    public @NotNull Iterator<E> dropRight(final int maxElements) {
      if (maxElements == Integer.MAX_VALUE) {
        return Iterator.of();
      }
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      if (maxElements <= 0) {
        return iterator();
      }
      return new Iterator<E>(new DropRightIteratorMaterializer<E>(materializer, maxElements));
    }

    @Override
    public @NotNull Iterator<E> dropRightWhile(
        @NotNull final IndexedPredicate<? super E> predicate) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new DropRightWhileIteratorMaterializer<E>(materializer,
          Require.notNull(predicate, "predicate")));
    }

    @Override
    public @NotNull Iterator<E> dropRightWhile(@NotNull final Predicate<? super E> predicate) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new DropRightWhileIteratorMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull Iterator<E> dropWhile(@NotNull final IndexedPredicate<? super E> predicate) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new DropWhileIteratorMaterializer<E>(materializer,
          Require.notNull(predicate, "predicate")));
    }

    @Override
    public @NotNull Iterator<E> dropWhile(@NotNull final Predicate<? super E> predicate) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new DropWhileIteratorMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull Iterator<Boolean> each(@NotNull final IndexedPredicate<? super E> predicate) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return Iterator.of(false);
      }
      return new Iterator<Boolean>(
          new EachIteratorMaterializer<E>(materializer, Require.notNull(predicate, "predicate"),
              false));
    }

    @Override
    public @NotNull Iterator<Boolean> each(@NotNull final Predicate<? super E> predicate) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return Iterator.of(false);
      }
      return new Iterator<Boolean>(new EachIteratorMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate")), false));
    }

    @Override
    public @NotNull Iterator<Boolean> endsWith(@NotNull final Iterable<?> elements) {
      if (getKnownSize(elements) == 0) {
        return Iterator.of(true);
      }
      return new Iterator<Boolean>(new EndsWithIteratorMaterializer<E>(materializer,
          List.getElementsMaterializer(Require.notNull(elements, "elements"))));
    }

    @Override
    public @NotNull Iterator<Boolean> exists(@NotNull final IndexedPredicate<? super E> predicate) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return Iterator.of(false);
      }
      return new Iterator<Boolean>(
          new ExistsIteratorMaterializer<E>(materializer, Require.notNull(predicate, "predicate"),
              false));
    }

    @Override
    public @NotNull Iterator<Boolean> exists(@NotNull final Predicate<? super E> predicate) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return Iterator.of(false);
      }
      return new Iterator<Boolean>(new ExistsIteratorMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate")), false));
    }

    @Override
    public @NotNull Iterator<E> filter(@NotNull final IndexedPredicate<? super E> predicate) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new RemoveWhereIteratorMaterializer<E>(materializer,
          negated(Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull Iterator<E> filter(@NotNull final Predicate<? super E> predicate) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new RemoveWhereIteratorMaterializer<E>(materializer,
          toNegatedIndexedPredicate(Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull Iterator<E> findAny(@NotNull final IndexedPredicate<? super E> predicate) {
      return findFirst(predicate);
    }

    @Override
    public @NotNull Iterator<E> findAny(@NotNull final Predicate<? super E> predicate) {
      return findFirst(predicate);
    }

    @Override
    public @NotNull Iterator<E> findFirst(@NotNull final IndexedPredicate<? super E> predicate) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new FindFirstIteratorMaterializer<E>(materializer,
          Require.notNull(predicate, "predicate")));
    }

    @Override
    public @NotNull Iterator<E> findFirst(@NotNull final Predicate<? super E> predicate) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new FindFirstIteratorMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull Iterator<Integer> findIndexOf(final Object element) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return Iterator.of();
      }
      return new Iterator<Integer>(
          new FindIndexIteratorMaterializer<E>(materializer, equalsElement(element)));
    }

    @Override
    public @NotNull Iterator<Integer> findIndexOfSlice(@NotNull final Iterable<?> elements) {
      return new Iterator<Integer>(new FindIndexOfSliceIteratorMaterializer<E>(materializer,
          List.getElementsMaterializer(Require.notNull(elements, "elements"))));
    }

    @Override
    public @NotNull Iterator<Integer> findIndexWhere(
        @NotNull final IndexedPredicate<? super E> predicate) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return Iterator.of();
      }
      return new Iterator<Integer>(new FindIndexIteratorMaterializer<E>(materializer,
          Require.notNull(predicate, "predicate")));
    }

    @Override
    public @NotNull Iterator<Integer> findIndexWhere(
        @NotNull final Predicate<? super E> predicate) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return Iterator.of();
      }
      return new Iterator<Integer>(new FindIndexIteratorMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull Iterator<E> findLast(@NotNull final IndexedPredicate<? super E> predicate) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new FindLastIteratorMaterializer<E>(materializer,
          Require.notNull(predicate, "predicate")));
    }

    @Override
    public @NotNull Iterator<E> findLast(@NotNull final Predicate<? super E> predicate) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new FindLastIteratorMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull Iterator<Integer> findLastIndexOf(final Object element) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return Iterator.of();
      }
      return new Iterator<Integer>(
          new FindLastIndexIteratorMaterializer<E>(materializer, equalsElement(element)));
    }

    @Override
    public @NotNull Iterator<Integer> findLastIndexOfSlice(@NotNull final Iterable<?> elements) {
      return new Iterator<Integer>(new FindLastIndexOfSliceIteratorMaterializer<E>(materializer,
          List.getElementsMaterializer(Require.notNull(elements, "elements"))));
    }

    @Override
    public @NotNull Iterator<Integer> findLastIndexWhere(
        @NotNull final IndexedPredicate<? super E> predicate) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return Iterator.of();
      }
      return new Iterator<Integer>(new FindLastIndexIteratorMaterializer<E>(materializer,
          Require.notNull(predicate, "predicate")));
    }

    @Override
    public @NotNull Iterator<Integer> findLastIndexWhere(
        @NotNull final Predicate<? super E> predicate) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return Iterator.of();
      }
      return new Iterator<Integer>(new FindLastIndexIteratorMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate"))));
    }

    @Override
    public E first() {
      return materializer.materializeNext();
    }

    @Override
    public @NotNull <F> Iterator<F> flatMap(
        @NotNull final Function<? super E, ? extends Iterable<F>> mapper) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return Iterator.of();
      }
      return new Iterator<F>(new FlatMapIteratorMaterializer<E, F>(materializer,
          getElementToMaterializer(Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull <F> Iterator<F> flatMap(
        @NotNull final IndexedFunction<? super E, ? extends Iterable<F>> mapper) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return Iterator.of();
      }
      return new Iterator<F>(new FlatMapIteratorMaterializer<E, F>(materializer,
          getElementToMaterializer(Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull Iterator<E> flatMapAfter(final int numElements,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      if (numElements < 0 || numElements == Integer.MAX_VALUE) {
        return iterator();
      }
      final IteratorMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return this;
      }
      if (knownSize > 0 && knownSize <= numElements) {
        return iterator();
      }
      return new Iterator<E>(new FlatMapAfterIteratorMaterializer<E>(materializer, numElements,
          getElementToMaterializer(Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull Iterator<E> flatMapAfter(final int numElements,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      if (numElements < 0 || numElements == Integer.MAX_VALUE) {
        return iterator();
      }
      final IteratorMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return this;
      }
      if (knownSize > 0 && knownSize <= numElements) {
        return iterator();
      }
      return new Iterator<E>(new FlatMapAfterIteratorMaterializer<E>(materializer, numElements,
          getElementToMaterializer(Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull Iterator<E> flatMapFirstWhere(
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new FlatMapFirstWhereIteratorMaterializer<E>(materializer,
          Require.notNull(predicate, "predicate"),
          getElementToMaterializer(Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull Iterator<E> flatMapFirstWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new FlatMapFirstWhereIteratorMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate")),
          getElementToMaterializer(Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull Iterator<E> flatMapLastWhere(
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new FlatMapLastWhereIteratorMaterializer<E>(materializer,
          Require.notNull(predicate, "predicate"),
          getElementToMaterializer(Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull Iterator<E> flatMapLastWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new FlatMapLastWhereIteratorMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate")),
          getElementToMaterializer(Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull Iterator<E> flatMapWhere(@NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new FlatMapWhereIteratorMaterializer<E>(materializer,
          Require.notNull(predicate, "predicate"),
          getElementToMaterializer(Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull Iterator<E> flatMapWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new FlatMapWhereIteratorMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate")),
          getElementToMaterializer(Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull <F> Iterator<F> fold(final F identity,
        @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
      return foldLeft(identity, operation);
    }

    @Override
    public @NotNull <F> Iterator<F> foldLeft(final F identity,
        @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return Iterator.of(identity);
      }
      return new Iterator<F>(new FoldLeftIteratorMaterializer<E, F>(materializer, identity,
          Require.notNull(operation, "operation")));
    }

    @Override
    public @NotNull <F> Iterator<F> foldLeftWhile(final F identity,
        @NotNull final Predicate<? super F> predicate,
        @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return Iterator.of(identity);
      }
      return new Iterator<F>(new FoldLeftWhileIteratorMaterializer<E, F>(materializer, identity,
          Require.notNull(predicate, "predicate"), Require.notNull(operation, "operation")));
    }

    @Override
    public @NotNull <F> Iterator<F> foldRight(final F identity,
        @NotNull final BinaryFunction<? super E, ? super F, ? extends F> operation) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return Iterator.of(identity);
      }
      return new Iterator<F>(new FoldRightIteratorMaterializer<E, F>(materializer, identity,
          Require.notNull(operation, "operation")));
    }

    @Override
    public @NotNull <F> Iterator<F> foldRightWhile(final F identity,
        @NotNull final Predicate<? super F> predicate,
        @NotNull final BinaryFunction<? super E, ? super F, ? extends F> operation) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return Iterator.of(identity);
      }
      return new Iterator<F>(new FoldRightWhileIteratorMaterializer<E, F>(materializer, identity,
          Require.notNull(predicate, "predicate"), Require.notNull(operation, "operation")));
    }

    @Override
    public boolean hasNext() {
      return materializer.materializeHasNext();
    }

    @Override
    public @NotNull Iterator<Boolean> includes(final Object element) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return Iterator.of(false);
      }
      return new Iterator<Boolean>(
          new ExistsIteratorMaterializer<E>(materializer, equalsElement(element), false));
    }

    @Override
    public @NotNull Iterator<Boolean> includesAll(@NotNull final Iterable<?> elements) {
      return new Iterator<Boolean>(new IncludesAllIteratorMaterializer<E>(materializer,
          Require.notNull(elements, "elements")));
    }

    @Override
    public @NotNull Iterator<Boolean> includesSlice(@NotNull final Iterable<?> elements) {
      return new Iterator<Boolean>(new IncludesSliceIteratorMaterializer<E>(materializer,
          List.getElementsMaterializer(Require.notNull(elements, "elements"))));
    }

    @Override
    public @NotNull Iterator<E> insert(final E element) {
      return new Iterator<E>(new InsertIteratorMaterializer<E>(materializer, element));
    }

    @Override
    public @NotNull Iterator<E> insertAfter(final int numElements, final E element) {
      if (numElements < 0 || numElements == Integer.MAX_VALUE) {
        return iterator();
      }
      if (numElements == 0) {
        return insert(element);
      }
      final IteratorMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return this;
      }
      if (knownSize > 0) {
        if (knownSize < numElements) {
          return iterator();
        }
        if (knownSize == numElements) {
          return append(element);
        }
      }
      return new Iterator<E>(
          new InsertAfterIteratorMaterializer<E>(materializer, numElements, element));
    }

    @Override
    public @NotNull Iterator<E> insertAll(@NotNull final Iterable<? extends E> elements) {
      return new Iterator<E>(new InsertAllIteratorMaterializer<E>(materializer,
          getElementsMaterializer(Require.notNull(elements, "elements"))));
    }

    @Override
    public @NotNull Iterator<E> insertAllAfter(final int numElements,
        @NotNull final Iterable<? extends E> elements) {
      if (numElements < 0 || numElements == Integer.MAX_VALUE) {
        return iterator();
      }
      if (numElements == 0) {
        return insertAll(elements);
      }
      final IteratorMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return this;
      }
      if (knownSize > 0) {
        if (knownSize < numElements) {
          return iterator();
        }
        if (knownSize == numElements) {
          return appendAll(elements);
        }
      }
      return new Iterator<E>(new InsertAllAfterIteratorMaterializer<E>(materializer, numElements,
          getElementsMaterializer(Require.notNull(elements, "elements"))));
    }

    @Override
    public @NotNull Iterator<E> intersect(@NotNull final Iterable<?> elements) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      if (getKnownSize(elements) == 0) {
        return Iterator.of();
      }
      return new Iterator<E>(new IntersectIteratorMaterializer<E>(materializer,
          getElementsMaterializer(Require.notNull(elements, "elements"))));
    }

    @Override
    public boolean isEmpty() {
      return !materializer.materializeHasNext();
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
      return this;
    }

    @Override
    public E last() {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (!materializer.materializeHasNext()) {
        throw new IndexOutOfBoundsException();
      }
      E next = null;
      while (materializer.materializeHasNext()) {
        next = materializer.materializeNext();
      }
      return next;
    }

    @Override
    public @NotNull <F> Iterator<F> map(@NotNull final Function<? super E, F> mapper) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return Iterator.of();
      }
      return new Iterator<F>(new MapIteratorMaterializer<E, F>(materializer,
          toIndexedFunction(Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull <F> Iterator<F> map(@NotNull final IndexedFunction<? super E, F> mapper) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return Iterator.of();
      }
      return new Iterator<F>(
          new MapIteratorMaterializer<E, F>(materializer, Require.notNull(mapper, "mapper")));
    }

    @Override
    public @NotNull Iterator<E> mapAfter(final int numElements,
        @NotNull final Function<? super E, ? extends E> mapper) {
      if (numElements < 0 || numElements == Integer.MAX_VALUE) {
        return iterator();
      }
      final IteratorMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return this;
      }
      if (knownSize > 0 && knownSize <= numElements) {
        return iterator();
      }
      return new Iterator<E>(new MapAfterIteratorMaterializer<E>(materializer, numElements,
          toIndexedFunction(Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull Iterator<E> mapAfter(final int numElements,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      if (numElements < 0 || numElements == Integer.MAX_VALUE) {
        return iterator();
      }
      final IteratorMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return this;
      }
      if (knownSize > 0 && knownSize <= numElements) {
        return iterator();
      }
      return new Iterator<E>(new MapAfterIteratorMaterializer<E>(materializer, numElements,
          Require.notNull(mapper, "mapper")));
    }

    @Override
    public @NotNull Iterator<E> mapFirstWhere(@NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new MapFirstWhereIteratorMaterializer<E>(materializer,
          Require.notNull(predicate, "predicate"), Require.notNull(mapper, "mapper")));
    }

    @Override
    public @NotNull Iterator<E> mapFirstWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends E> mapper) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new MapFirstWhereIteratorMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate")),
          toIndexedFunction(Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull Iterator<E> mapLastWhere(@NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new MapLastWhereIteratorMaterializer<E>(materializer,
          Require.notNull(predicate, "predicate"), Require.notNull(mapper, "mapper")));
    }

    @Override
    public @NotNull Iterator<E> mapLastWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends E> mapper) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new MapLastWhereIteratorMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate")),
          toIndexedFunction(Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull Iterator<E> mapWhere(@NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new MapIteratorMaterializer<E, E>(materializer,
          filteredMapper(Require.notNull(predicate, "predicate"),
              Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull Iterator<E> mapWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends E> mapper) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new MapIteratorMaterializer<E, E>(materializer,
          filteredMapper(Require.notNull(predicate, "predicate"),
              Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull Iterator<E> max(@NotNull final Comparator<? super E> comparator) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return Iterator.of();
      }
      return new Iterator<E>(
          new MaxIteratorMaterializer<E>(materializer, Require.notNull(comparator, "comparator")));
    }

    @Override
    public @NotNull Iterator<E> min(@NotNull final Comparator<? super E> comparator) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return Iterator.of();
      }
      return new Iterator<E>(new MaxIteratorMaterializer<E>(materializer,
          reversed(Require.notNull(comparator, "comparator"))));
    }

    @Override
    public E next() {
      return materializer.materializeNext();
    }

    @Override
    public @NotNull Iterator<Boolean> none(@NotNull final IndexedPredicate<? super E> predicate) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return Iterator.of(true);
      }
      return new Iterator<Boolean>(new EachIteratorMaterializer<E>(materializer,
          negated(Require.notNull(predicate, "predicate")), true));
    }

    @Override
    public @NotNull Iterator<Boolean> none(@NotNull final Predicate<? super E> predicate) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return Iterator.of(true);
      }
      return new Iterator<Boolean>(new EachIteratorMaterializer<E>(materializer,
          toNegatedIndexedPredicate(Require.notNull(predicate, "predicate")), true));
    }

    @Override
    public @NotNull Iterator<Boolean> notAll(@NotNull final IndexedPredicate<? super E> predicate) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return Iterator.of(true);
      }
      return new Iterator<Boolean>(new ExistsIteratorMaterializer<E>(materializer,
          negated(Require.notNull(predicate, "predicate")), true));
    }

    @Override
    public @NotNull Iterator<Boolean> notAll(@NotNull final Predicate<? super E> predicate) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return Iterator.of(true);
      }
      return new Iterator<Boolean>(new ExistsIteratorMaterializer<E>(materializer,
          toNegatedIndexedPredicate(Require.notNull(predicate, "predicate")), true));
    }

    @Override
    public boolean notEmpty() {
      return materializer.materializeHasNext();
    }

    @Override
    public @NotNull Iterator<E> orElse(@NotNull final Iterable<? extends E> elements) {
      final IteratorMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      final IteratorMaterializer<E> elementsMaterializer = getElementsMaterializer(
          Require.notNull(elements, "elements"));
      if (knownSize == 0) {
        return new Iterator<E>(elementsMaterializer);
      }
      return new Iterator<E>(new OrElseIteratorMaterializer<E>(materializer, elementsMaterializer));
    }

    @Override
    public @NotNull Iterator<E> orElseGet(
        @NotNull final Supplier<? extends Iterable<? extends E>> supplier) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return new Iterator<E>(new SuppliedMaterializer<E>(Require.notNull(supplier, "supplier")));
      }
      return new Iterator<E>(new OrElseIteratorMaterializer<E>(materializer,
          new SuppliedMaterializer<E>(Require.notNull(supplier, "supplier"))));
    }

    @Override
    public @NotNull Iterator<E> peek(@NotNull final Consumer<? super E> consumer) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new PeekIteratorMaterializer<E>(materializer,
          toIndexedConsumer(Require.notNull(consumer, "consumer"))));
    }

    @Override
    public @NotNull Iterator<E> peek(@NotNull final IndexedConsumer<? super E> consumer) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(
          new PeekIteratorMaterializer<E>(materializer, Require.notNull(consumer, "consumer")));
    }

    @Override
    public @NotNull Iterator<E> peekExceptionally(
        @NotNull final Consumer<? super Throwable> consumer) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new PeekExceptionallyIteratorMaterializer<E>(materializer,
          toIndexedConsumer(Require.notNull(consumer, "consumer"))));
    }

    @Override
    public @NotNull Iterator<E> peekExceptionally(
        @NotNull final IndexedConsumer<? super Throwable> consumer) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new PeekExceptionallyIteratorMaterializer<E>(materializer,
          Require.notNull(consumer, "consumer")));
    }

    @Override
    public @NotNull Iterator<E> plus(final E element) {
      return append(element);
    }

    @Override
    public @NotNull Iterator<E> plusAll(@NotNull final Iterable<? extends E> elements) {
      return appendAll(elements);
    }

    @Override
    public @NotNull Iterator<E> reduce(
        @NotNull final BinaryFunction<? super E, ? super E, ? extends E> operation) {
      return reduceLeft(operation);
    }

    @Override
    public @NotNull Iterator<E> reduceLeft(
        @NotNull final BinaryFunction<? super E, ? super E, ? extends E> operation) {
      final IteratorMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return this;
      }
      if (knownSize == 1) {
        return iterator();
      }
      return new Iterator<E>(new ReduceLeftIteratorMaterializer<E>(materializer,
          Require.notNull(operation, "operation")));
    }

    @Override
    public @NotNull Iterator<E> reduceRight(
        @NotNull final BinaryFunction<? super E, ? super E, ? extends E> operation) {
      final IteratorMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return this;
      }
      if (knownSize == 1) {
        return iterator();
      }
      return new Iterator<E>(new ReduceRightIteratorMaterializer<E>(materializer,
          Require.notNull(operation, "operation")));
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull Iterator<E> removeAfter(final int numElements) {
      if (numElements < 0 || numElements == Integer.MAX_VALUE) {
        return iterator();
      }
      final IteratorMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return this;
      }
      if (numElements == 0 && knownSize == 1) {
        return Iterator.of();
      }
      return new Iterator<E>(new RemoveAfterIteratorMaterializer<E>(materializer, numElements));
    }

    @Override
    public @NotNull Iterator<E> removeEach(final E element) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(
          new RemoveWhereIteratorMaterializer<E>(materializer, equalsElement(element)));
    }

    @Override
    public @NotNull Iterator<E> removeFirst(final E element) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(
          new RemoveFirstWhereIteratorMaterializer<E>(materializer, equalsElement(element)));
    }

    @Override
    public @NotNull Iterator<E> removeFirstWhere(
        @NotNull final IndexedPredicate<? super E> predicate) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new RemoveFirstWhereIteratorMaterializer<E>(materializer,
          Require.notNull(predicate, "predicate")));
    }

    @Override
    public @NotNull Iterator<E> removeFirstWhere(@NotNull final Predicate<? super E> predicate) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new RemoveFirstWhereIteratorMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull Iterator<E> removeLast(final E element) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(
          new RemoveLastWhereIteratorMaterializer<E>(materializer, equalsElement(element)));
    }

    @Override
    public @NotNull Iterator<E> removeLastWhere(
        @NotNull final IndexedPredicate<? super E> predicate) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new RemoveLastWhereIteratorMaterializer<E>(materializer,
          Require.notNull(predicate, "predicate")));
    }

    @Override
    public @NotNull Iterator<E> removeLastWhere(@NotNull final Predicate<? super E> predicate) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new RemoveLastWhereIteratorMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull Iterator<E> removeSlice(final int start, final int end) {
      if (end >= 0 && start >= end) {
        return iterator();
      }
      final IteratorMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return this;
      }
      if (knownSize > 0) {
        final int knownStart;
        if (start < 0) {
          knownStart = Math.max(0, knownSize + start);
        } else {
          knownStart = Math.min(knownSize, start);
        }
        final int knownEnd;
        if (end < 0) {
          knownEnd = Math.max(0, knownSize + end);
        } else {
          knownEnd = Math.min(knownSize, end);
        }
        if (knownStart >= knownEnd) {
          return iterator();
        }
        final int knownLength = knownEnd - knownStart;
        if (knownLength == 1) {
          return removeAfter(knownStart);
        }
        if (knownLength == knownSize) {
          return Iterator.of();
        }
      }
      return new Iterator<E>(new RemoveSliceIteratorMaterializer<E>(materializer, start, end));
    }

    @Override
    public @NotNull Iterator<E> removeWhere(@NotNull final IndexedPredicate<? super E> predicate) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new RemoveWhereIteratorMaterializer<E>(materializer,
          Require.notNull(predicate, "predicate")));
    }

    @Override
    public @NotNull Iterator<E> removeWhere(@NotNull final Predicate<? super E> predicate) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new RemoveWhereIteratorMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull Iterator<E> replaceAfter(final int numElements, final E replacement) {
      if (numElements < 0 || numElements == Integer.MAX_VALUE) {
        return iterator();
      }
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new MapAfterIteratorMaterializer<E>(materializer, numElements,
          replacementMapper(replacement)));
    }

    @Override
    public @NotNull Iterator<E> replaceEach(final E element, final E replacement) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new MapIteratorMaterializer<E, E>(materializer,
          filteredMapper(equalsElement(element), replacementMapper(replacement))));
    }

    @Override
    public @NotNull Iterator<E> replaceFirst(final E element, final E replacement) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(
          new MapFirstWhereIteratorMaterializer<E>(materializer, equalsElement(element),
              replacementMapper(replacement)));
    }

    @Override
    public @NotNull Iterator<E> replaceFirstWhere(
        @NotNull final IndexedPredicate<? super E> predicate, final E replacement) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new MapFirstWhereIteratorMaterializer<E>(materializer,
          Require.notNull(predicate, "predicate"), replacementMapper(replacement)));
    }

    @Override
    public @NotNull Iterator<E> replaceFirstWhere(@NotNull final Predicate<? super E> predicate,
        final E replacement) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new MapFirstWhereIteratorMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate")),
          replacementMapper(replacement)));
    }

    @Override
    public @NotNull Iterator<E> replaceLast(final E element, final E replacement) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(
          new MapLastWhereIteratorMaterializer<E>(materializer, equalsElement(element),
              replacementMapper(replacement)));
    }

    @Override
    public @NotNull Iterator<E> replaceLastWhere(
        @NotNull final IndexedPredicate<? super E> predicate, final E replacement) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new MapLastWhereIteratorMaterializer<E>(materializer,
          Require.notNull(predicate, "predicate"), replacementMapper(replacement)));
    }

    @Override
    public @NotNull Iterator<E> replaceLastWhere(@NotNull final Predicate<? super E> predicate,
        final E replacement) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new MapLastWhereIteratorMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate")),
          replacementMapper(replacement)));
    }

    @Override
    public @NotNull Iterator<E> replaceSlice(final int start, final int end,
        @NotNull final Iterable<? extends E> patch) {
      final IteratorMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize >= 0) {
        final int knownStart;
        if (start < 0) {
          knownStart = Math.max(0, knownSize + start);
        } else {
          knownStart = Math.min(knownSize, start);
        }
        final int knownEnd;
        if (end < 0) {
          knownEnd = Math.max(0, knownSize + end);
        } else {
          knownEnd = Math.min(knownSize, end);
        }
        if (knownStart >= knownEnd) {
          return insertAllAfter(knownStart, patch);
        }
        final int knownLength = knownEnd - knownStart;
        if (knownLength == knownSize) {
          return new Iterator<E>(getElementsMaterializer(Require.notNull(patch, "patch")));
        }
      }
      if (getKnownSize(patch) == 0) {
        return new Iterator<E>(new RemoveSliceIteratorMaterializer<E>(materializer, start, end));
      }
      return new Iterator<E>(new ReplaceSliceIteratorMaterializer<E>(materializer, start, end,
          getElementsMaterializer(Require.notNull(patch, "patch"))));
    }

    @Override
    public @NotNull Iterator<E> replaceWhere(@NotNull final IndexedPredicate<? super E> predicate,
        final E replacement) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new MapIteratorMaterializer<E, E>(materializer,
          filteredMapper(Require.notNull(predicate, "predicate"), replacementMapper(replacement))));
    }

    @Override
    public @NotNull Iterator<E> replaceWhere(@NotNull final Predicate<? super E> predicate,
        final E replacement) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new MapIteratorMaterializer<E, E>(materializer,
          filteredMapper(Require.notNull(predicate, "predicate"), replacementMapper(replacement))));
    }

    @Override
    public @NotNull Iterator<E> resizeTo(@NotNegative final int numElements, final E padding) {
      Require.notNegative(numElements, "numElements");
      if (numElements == 0) {
        return Iterator.of();
      }
      final IteratorMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize >= 0) {
        if (knownSize == 0) {
          return Iterator.times(numElements, padding);
        }
        if (knownSize == numElements) {
          return iterator();
        }
        if (knownSize > numElements) {
          return new Iterator<E>(new TakeIteratorMaterializer<E>(materializer, numElements));
        }
        return new Iterator<E>(new AppendAllIteratorMaterializer<E>(materializer,
            new RepeatIteratorMaterializer<E>(numElements - knownSize, padding)));
      }
      return new Iterator<E>(new ResizeIteratorMaterializer<E>(materializer, numElements, padding));
    }

    @Override
    public int skip(final int maxElements) {
      if (maxElements > 0) {
        return materializer.materializeSkip(maxElements);
      }
      return 0;
    }

    @Override
    public @NotNull Iterator<E> runFinally(@NotNull final Action action) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        try {
          action.run();
        } catch (final Exception e) {
          throw UncheckedException.throwUnchecked(e);
        }
        return this;
      }
      return new Iterator<E>(
          new FinallyIteratorMaterializer<E>(materializer, Require.notNull(action, "action")));
    }

    @Override
    public int size() {
      return materializer.materializeSkip(Integer.MAX_VALUE);
    }

    @Override
    public @NotNull Iterator<E> slice(final int start) {
      return slice(start, Integer.MAX_VALUE);
    }

    @Override
    public @NotNull Iterator<E> slice(final int start, final int end) {
      if (end == Integer.MAX_VALUE && start >= 0) {
        return drop(start);
      }
      if (start == 0 && end >= 0) {
        return take(end);
      }
      if ((start == end) || (end >= 0 && start >= end)) {
        return Iterator.of();
      }
      final IteratorMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return this;
      }
      if (knownSize > 0) {
        final int knownStart;
        if (start < 0) {
          knownStart = Math.max(0, knownSize + start);
        } else {
          knownStart = Math.min(knownSize, start);
        }
        final int knownEnd;
        if (end < 0) {
          knownEnd = Math.max(0, knownSize + end);
        } else {
          knownEnd = Math.min(knownSize, end);
        }
        if (knownStart >= knownEnd) {
          return Iterator.of();
        }
      }
      return new Iterator<E>(new SliceIteratorMaterializer<E>(materializer, start, end));
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull Iterator<? extends Iterator<E>> slidingWindow(@Positive final int maxSize,
        @Positive final int step) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return Iterator.of();
      }
      return new Iterator<Iterator<E>>(
          new SlidingWindowIteratorMaterializer<E, Iterator<E>>(materializer, maxSize, step,
              (Function<? super java.util.List<E>, ? extends Iterator<E>>) FROM_JAVA_LIST));
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull Iterator<? extends Iterator<E>> slidingWindowWithPadding(
        @Positive final int size, @Positive final int step, final E padding) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return Iterator.of();
      }
      return new Iterator<Iterator<E>>(
          new SlidingWindowIteratorMaterializer<E, Iterator<E>>(materializer, size, step, padding,
              (Function<? super java.util.List<E>, ? extends Iterator<E>>) FROM_JAVA_LIST));
    }

    @Override
    public @NotNull Iterator<Boolean> startsWith(@NotNull final Iterable<?> elements) {
      if (getKnownSize(elements) == 0) {
        return Iterator.of(true);
      }
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return Iterator.of(false);
      }
      return new Iterator<Boolean>(new StartsWithIteratorMaterializer<E>(materializer,
          getElementsMaterializer(Require.notNull(elements, "elements"))));

    }

    @Override
    public @NotNull <T extends Throwable> Iterator<E> switchExceptionally(
        @NotNull final Class<T> exceptionType,
        @NotNull final Function<? super T, ? extends Iterable<? extends E>> mapper) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new SwitchExceptionallyIteratorMaterializer<E>(materializer,
          getExceptionToMaterializer(Require.notNull(exceptionType, "exceptionType"),
              Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull <T extends Throwable> Iterator<E> switchExceptionally(
        @NotNull final Class<T> exceptionType,
        @NotNull final IndexedFunction<? super T, ? extends Iterable<? extends E>> mapper) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new SwitchExceptionallyIteratorMaterializer<E>(materializer,
          getExceptionToMaterializer(Require.notNull(exceptionType, "exceptionType"),
              Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull Iterator<E> switchExceptionally(
        @NotNull final Function<? super Throwable, ? extends Iterable<? extends E>> mapper) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new SwitchExceptionallyIteratorMaterializer<E>(materializer,
          getElementToMaterializer(Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull Iterator<E> switchExceptionally(
        @NotNull final IndexedFunction<? super Throwable, ? extends Iterable<? extends E>> mapper) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new SwitchExceptionallyIteratorMaterializer<E>(materializer,
          getElementToMaterializer(Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull Iterator<E> symmetricDiff(@NotNull final Iterable<? extends E> elements) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return Iterator.wrap(elements);
      }
      if (getKnownSize(elements) == 0) {
        return this;
      }
      return new Iterator<E>(new SymmetricDiffIteratorMaterializer<E>(materializer,
          List.getElementsMaterializer(Require.notNull(elements, "elements"))));
    }

    @Override
    public @NotNull Iterator<E> take(final int maxElements) {
      if (maxElements <= 0) {
        return Iterator.of();
      }
      if (maxElements == Integer.MAX_VALUE) {
        return iterator();
      }
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new TakeIteratorMaterializer<E>(materializer, maxElements));
    }

    @Override
    public @NotNull Iterator<E> takeRight(final int maxElements) {
      if (maxElements <= 0) {
        return Iterator.of();
      }
      if (maxElements == Integer.MAX_VALUE) {
        return iterator();
      }
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new TakeRightIteratorMaterializer<E>(materializer, maxElements));
    }

    @Override
    public @NotNull Iterator<E> takeRightWhile(
        @NotNull final IndexedPredicate<? super E> predicate) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new TakeRightWhileIteratorMaterializer<E>(materializer,
          Require.notNull(predicate, "predicate")));
    }

    @Override
    public @NotNull Iterator<E> takeRightWhile(@NotNull final Predicate<? super E> predicate) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new TakeRightWhileIteratorMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull Iterator<E> takeWhile(@NotNull final IndexedPredicate<? super E> predicate) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new TakeWhileIteratorMaterializer<E>(materializer,
          Require.notNull(predicate, "predicate")));
    }

    @Override
    public @NotNull Iterator<E> takeWhile(@NotNull final Predicate<? super E> predicate) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new Iterator<E>(new TakeWhileIteratorMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate"))));
    }

    // TODO: extra
    public @NotNull future.Iterator<E> toFuture(@NotNull final ExecutionContext context) {
      return new future.Iterator<E>(Require.notNull(context, "context"),
          new AtomicReference<CancellationException>(),
          new IteratorToIteratorFutureMaterializer<E>(this, context));
    }

    // TODO: extra
    public @NotNull List<E> toList() {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer instanceof ListMaterializerToIteratorMaterializer) {
        return new List<E>(
            ((ListMaterializerToIteratorMaterializer<E>) materializer).materializer());
      }
      return List.wrap(this);
    }

    @Override
    public @NotNull Iterator<E> union(@NotNull final Iterable<? extends E> elements) {
      final IteratorMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return Iterator.wrap(elements);
      }
      if (getKnownSize(elements) == 0) {
        return this;
      }
      return new Iterator<E>(new UnionIteratorMaterializer<E>(materializer,
          getElementsMaterializer(Require.notNull(elements, "elements"))));
    }

    int knownSize() {
      return materializer.knownSize();
    }

    private static class SuppliedMaterializer<E> implements IteratorMaterializer<E> {

      private volatile IteratorMaterializer<E> state;

      private SuppliedMaterializer(
          @NotNull final Supplier<? extends Iterable<? extends E>> supplier) {
        state = new ImmaterialState(supplier);
      }

      @Override
      public int knownSize() {
        return state.knownSize();
      }

      @Override
      public boolean materializeHasNext() {
        return state.materializeHasNext();
      }

      @Override
      public E materializeNext() {
        return state.materializeNext();
      }

      @Override
      public int materializeSkip(final int count) {
        return state.materializeSkip(count);
      }

      private class ImmaterialState implements IteratorMaterializer<E> {

        private final Supplier<? extends Iterable<? extends E>> supplier;

        private ImmaterialState(@NotNull final Supplier<? extends Iterable<? extends E>> supplier) {
          this.supplier = supplier;
        }

        @Override
        public int knownSize() {
          return -1;
        }

        @Override
        public boolean materializeHasNext() {
          try {
            final IteratorMaterializer<E> elementsMaterializer = getElementsMaterializer(
                supplier.get());
            return (state = elementsMaterializer).materializeHasNext();
          } catch (final Exception e) {
            throw UncheckedException.throwUnchecked(e);
          }
        }

        @Override
        public E materializeNext() {
          try {
            final IteratorMaterializer<E> elementsMaterializer = getElementsMaterializer(
                supplier.get());
            return (state = elementsMaterializer).materializeNext();
          } catch (final Exception e) {
            throw UncheckedException.throwUnchecked(e);
          }
        }

        @Override
        public int materializeSkip(final int count) {
          try {
            final IteratorMaterializer<E> elementsMaterializer = getElementsMaterializer(
                supplier.get());
            return (state = elementsMaterializer).materializeSkip(count);
          } catch (final Exception e) {
            throw UncheckedException.throwUnchecked(e);
          }
        }
      }
    }
  }

  public static class List<E> extends AbstractListSequence<E> implements itf.List<E> {

    private static final List<?> EMPTY_LIST = new List<Object>(EmptyListMaterializer.instance());
    private static final Splitter<?, ? extends List<?>> SPLITTER = new Splitter<Object, List<Object>>() {
      @Override
      public @NotNull List<Object> getChunk(@NotNull final ListMaterializer<Object> materializer,
          final int start, final int end) {
        return new List<Object>(materializer).slice(start, end);
      }
    };
    private static final List<Boolean> FALSE_LIST = new List<Boolean>(
        new ElementToListMaterializer<Boolean>(false));
    private static final List<?> NULL_LIST = new List<Object>(
        new ElementToListMaterializer<Object>(null));
    private static final List<Boolean> TRUE_LIST = new List<Boolean>(
        new ElementToListMaterializer<Boolean>(true));
    private static final List<Integer> ZERO_LIST = new List<Integer>(
        new ElementToListMaterializer<Integer>(0));

    private final ListMaterializer<E> materializer;

    List(@NotNull final ListMaterializer<E> materializer) {
      this.materializer = materializer;
    }

    public static @NotNull <E> List<E> from(@NotNull final Iterable<E> elements) {
      final ArrayList<E> list = new ArrayList<E>();
      for (final E element : elements) {
        list.add(element);
      }
      return new List<E>(new ListToListMaterializer<E>(list));
    }

    public static @NotNull <E> List<E> from(@NotNull final java.util.Iterator<E> elements) {
      final ArrayList<E> list = new ArrayList<E>();
      while (elements.hasNext()) {
        list.add(elements.next());
      }
      return new List<E>(new ListToListMaterializer<E>(list));
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> List<E> of() {
      return (List<E>) EMPTY_LIST;
    }

    public static @NotNull <E> List<E> of(final E first) {
      if (first == null) {
        return NULL_LIST.as();
      }
      if (Boolean.TRUE.equals(first)) {
        return TRUE_LIST.as();
      }
      if (Boolean.FALSE.equals(first)) {
        return FALSE_LIST.as();
      }
      if (Integer.valueOf(0).equals(first)) {
        return ZERO_LIST.as();
      }
      return new List<E>(new ElementToListMaterializer<E>(first));
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> List<E> of(final E first, final E second) {
      return new List<E>(new ArrayToListMaterializer<E>(first, second));
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> List<E> of(final E first, final E second, final E third) {
      return new List<E>(new ArrayToListMaterializer<E>(first, second, third));
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> List<E> of(final E first, final E second, final E third,
        final E fourth) {
      return new List<E>(new ArrayToListMaterializer<E>(first, second, third, fourth));
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> List<E> of(final E first, final E second, final E third,
        final E fourth, final E fifth) {
      return new List<E>(new ArrayToListMaterializer<E>(first, second, third, fourth, fifth));
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> List<E> of(final E first, final E second, final E third,
        final E fourth, final E fifth, final E sixth) {
      return new List<E>(
          new ArrayToListMaterializer<E>(first, second, third, fourth, fifth, sixth));
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> List<E> of(final E first, final E second, final E third,
        final E fourth, final E fifth, final E sixth, final E seventh) {
      return new List<E>(
          new ArrayToListMaterializer<E>(first, second, third, fourth, fifth, sixth, seventh));
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> List<E> of(final E first, final E second, final E third,
        final E fourth, final E fifth, final E sixth, final E seventh, final E eighth) {
      return new List<E>(
          new ArrayToListMaterializer<E>(first, second, third, fourth, fifth, sixth, seventh,
              eighth));
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> List<E> of(final E first, final E second, final E third,
        final E fourth, final E fifth, final E sixth, final E seventh, final E eighth,
        final E ninth) {
      return new List<E>(
          new ArrayToListMaterializer<E>(first, second, third, fourth, fifth, sixth, seventh,
              eighth, ninth));
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> List<E> of(final E first, final E second, final E third,
        final E fourth, final E fifth, final E sixth, final E seventh, final E eighth,
        final E ninth, final E tenth) {
      return new List<E>(
          new ArrayToListMaterializer<E>(first, second, third, fourth, fifth, sixth, seventh,
              eighth, ninth, tenth));
    }

    public static @NotNull <E> List<E> ofArray(final E... elements) {
      if (elements == null) {
        return List.of();
      }
      return new List<E>(new ArrayToListMaterializer<E>(Arrays.copyOf(elements, elements.length)));
    }

    public static @NotNull List<Character> ofChars(@NotNull final CharSequence chars) {
      return new List<Character>(
          new CharSequenceToListMaterializer(Require.notNull(chars, "chars")));
    }

    public static @NotNull List<Double> ofDoubles(final double... elements) {
      if (elements == null) {
        return List.of();
      }
      return new List<Double>(
          new DoubleArrayToListMaterializer(Arrays.copyOf(elements, elements.length)));
    }

    public static @NotNull List<Float> ofFloats(final float... elements) {
      if (elements == null) {
        return List.of();
      }
      return new List<Float>(
          new FloatArrayToListMaterializer(Arrays.copyOf(elements, elements.length)));
    }

    public static @NotNull List<Integer> ofInts(final int... elements) {
      if (elements == null) {
        return List.of();
      }
      return new List<Integer>(
          new IntArrayToListMaterializer(Arrays.copyOf(elements, elements.length)));
    }

    public static @NotNull List<Long> ofLongs(final long... elements) {
      if (elements == null) {
        return List.of();
      }
      return new List<Long>(
          new LongArrayToListMaterializer(Arrays.copyOf(elements, elements.length)));
    }

    public static @NotNull <E> List<E> times(final int count, final E element) {
      if (count == 0) {
        return List.of();
      }
      return new List<E>(
          new RepeatListMaterializer<E>(Require.notNegative(count, "count"), element));
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> List<E> wrap(@NotNull final Iterable<? extends E> elements) {
      if (elements instanceof List) {
        return (List<E>) elements;
      }
      return new List<E>(getElementsMaterializer(Require.notNull(elements, "elements")));
    }

    private static @NotNull <E> Splitter<E, List<E>> getSplitter(final int size, final E padding) {
      return new Splitter<E, List<E>>() {
        @Override
        public @NotNull List<E> getChunk(@NotNull final ListMaterializer<E> materializer,
            final int start, final int end) {
          final List<E> sliced = new List<E>(materializer).slice(start, end);
          final int paddingSize = size - (end - start);
          if (paddingSize > 0) {
            return sliced.appendAll(List.times(paddingSize, padding));
          }
          return sliced;
        }
      };
    }

    @SuppressWarnings("unchecked")
    private static @NotNull <E> ListMaterializer<E> getElementsMaterializer(
        @NotNull final Iterable<? extends E> elements) {
      if (elements instanceof List) {
        return ((List<E>) elements).materializer;
      }
      if (elements instanceof java.util.List) {
        final java.util.List<E> list = (java.util.List<E>) elements;
        final int size = list.size();
        if (size == 0) {
          return EmptyListMaterializer.instance();
        }
        if (size == 1) {
          return new ElementToListMaterializer<E>(list.get(0));
        }
        return new ListToListMaterializer<E>(list);
      }
      if (elements instanceof Collection) {
        final Collection<E> collection = (Collection<E>) elements;
        if (collection.isEmpty()) {
          return EmptyListMaterializer.instance();
        }
        return new CollectionToListMaterializer<E>(collection);
      }
      return new IteratorToListMaterializer<E>((java.util.Iterator<E>) elements.iterator());
    }

    private static @NotNull <E, F> IndexedFunction<E, ListMaterializer<F>> getElementToMaterializer(
        @NotNull final Function<? super E, ? extends Iterable<? extends F>> mapper) {
      return new IndexedFunction<E, ListMaterializer<F>>() {
        @Override
        public ListMaterializer<F> apply(final int index, final E element) throws Exception {
          return getElementsMaterializer(mapper.apply(element));
        }
      };
    }

    private static @NotNull <E, F> IndexedFunction<E, ListMaterializer<F>> getElementToMaterializer(
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends F>> mapper) {
      return new IndexedFunction<E, ListMaterializer<F>>() {
        @Override
        public ListMaterializer<F> apply(final int index, final E element) throws Exception {
          return getElementsMaterializer(mapper.apply(index, element));
        }
      };
    }

    @Override
    public @NotNull List<E> append(final E element) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return List.of(element);
      }
      return new List<E>(new AppendListMaterializer<E>(materializer, element));
    }

    @Override
    public @NotNull List<E> appendAll(@NotNull final Iterable<? extends E> elements) {
      final ListMaterializer<E> materializer = this.materializer;
      if (getKnownSize(elements) == 0) {
        return this;
      } else if (materializer.knownSize() == 0) {
        return new List<E>(getElementsMaterializer(Require.notNull(elements, "elements")));
      }
      return new List<E>(new AppendAllListMaterializer<E>(materializer,
          getElementsMaterializer(Require.notNull(elements, "elements"))));
    }

    @Override
    public <T> T apply(@NotNull final Function<? super itf.Sequence<E>, T> mapper) {
      return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <F> List<F> as() {
      return (List<F>) this;
    }

    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public List<E> clone() {
      materializer.materializeElements();
      return new List<E>(materializer);
    }

    @Override
    public boolean contains(final Object o) {
      return materializer.materializeContains(o);
    }

    @Override
    public @NotNull List<Integer> count() {
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return ZERO_LIST;
      }
      if (knownSize > 0) {
        return List.of(knownSize);
      }
      return new List<Integer>(new CountListMaterializer<E>(materializer));
    }

    @Override
    public @NotNull List<Integer> countWhere(@NotNull final IndexedPredicate<? super E> predicate) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return ZERO_LIST;
      }
      return new List<Integer>(
          new CountWhereListMaterializer<E>(materializer, Require.notNull(predicate, "predicate")));
    }

    @Override
    public @NotNull List<Integer> countWhere(@NotNull final Predicate<? super E> predicate) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return ZERO_LIST;
      }
      return new List<Integer>(new CountWhereListMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull List<E> diff(@NotNull final Iterable<?> elements) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      if (getKnownSize(elements) == 0) {
        return this;
      }
      return new List<E>(new DiffListMaterializer<E>(materializer,
          getElementsMaterializer(Require.notNull(elements, "elements"))));
    }

    @Override
    public @NotNull List<E> distinct() {
      return distinctBy(indexedIdentity());
    }

    @Override
    public @NotNull <K> List<E> distinctBy(@NotNull final Function<? super E, K> keyExtractor) {
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0 || knownSize == 1) {
        return this;
      }
      return new List<E>(new DistinctByListMaterializer<E, K>(materializer,
          toIndexedFunction(Require.notNull(keyExtractor, "keyExtractor"))));
    }

    @Override
    public @NotNull <K> List<E> distinctBy(
        @NotNull final IndexedFunction<? super E, K> keyExtractor) {
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0 || knownSize == 1) {
        return this;
      }
      return new List<E>(new DistinctByListMaterializer<E, K>(materializer,
          Require.notNull(keyExtractor, "keyExtractor")));
    }

    @Override
    public void doFor(@NotNull final Consumer<? super E> consumer) {
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return;
      }
      try {
        if (knownSize == 1) {
          consumer.accept(materializer.materializeElement(0));
        } else {
          int i = 0;
          while (materializer.canMaterializeElement(i)) {
            consumer.accept(materializer.materializeElement(i));
            ++i;
          }
        }
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public void doFor(@NotNull final IndexedConsumer<? super E> consumer) {
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return;
      }
      try {
        if (knownSize == 1) {
          consumer.accept(0, materializer.materializeElement(0));
        } else {
          int i = 0;
          while (materializer.canMaterializeElement(i)) {
            consumer.accept(i, materializer.materializeElement(i));
            ++i;
          }
        }
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public void doWhile(@NotNull final IndexedPredicate<? super E> predicate) {
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return;
      }
      try {
        if (knownSize == 1) {
          predicate.test(0, materializer.materializeElement(0));
        } else {
          int i = 0;
          while (materializer.canMaterializeElement(i)) {
            if (!predicate.test(i, materializer.materializeElement(i))) {
              break;
            }
            ++i;
          }
        }
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public void doWhile(@NotNull final IndexedPredicate<? super E> condition,
        @NotNull final IndexedConsumer<? super E> consumer) {
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return;
      }
      try {
        if (knownSize == 1) {
          final E element = materializer.materializeElement(0);
          if (condition.test(0, element)) {
            consumer.accept(0, element);
          }
        } else {
          int i = 0;
          while (materializer.canMaterializeElement(i)) {
            final E next = materializer.materializeElement(i);
            if (!condition.test(i, next)) {
              break;
            }
            consumer.accept(i, next);
            ++i;
          }
        }
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public void doWhile(@NotNull final Predicate<? super E> predicate) {
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return;
      }
      try {
        if (knownSize == 1) {
          predicate.test(materializer.materializeElement(0));
        } else {
          int i = 0;
          while (materializer.canMaterializeElement(i)) {
            if (!predicate.test(materializer.materializeElement(i))) {
              break;
            }
            ++i;
          }
        }
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public void doWhile(@NotNull final Predicate<? super E> condition,
        @NotNull final Consumer<? super E> consumer) {
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return;
      }
      try {
        if (knownSize == 1) {
          final E element = materializer.materializeElement(0);
          if (condition.test(element)) {
            consumer.accept(element);
          }
        } else {
          int i = 0;
          while (materializer.canMaterializeElement(i)) {
            final E next = materializer.materializeElement(i);
            if (!condition.test(next)) {
              break;
            }
            consumer.accept(next);
            ++i;
          }
        }
      } catch (final Exception e) {
        throw UncheckedException.throwUnchecked(e);
      }
    }

    @Override
    public @NotNull List<E> drop(final int maxElements) {
      if (maxElements == Integer.MAX_VALUE) {
        return List.of();
      }
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (maxElements <= 0 || knownSize == 0) {
        return this;
      }
      if (knownSize > 0 && maxElements >= knownSize) {
        return List.of();
      }
      return new List<E>(new DropListMaterializer<E>(materializer, maxElements));
    }

    @Override
    public @NotNull List<E> dropRight(final int maxElements) {
      if (maxElements == Integer.MAX_VALUE) {
        return List.of();
      }
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (maxElements <= 0 || knownSize == 0) {
        return this;
      }
      if (knownSize > 0 && maxElements >= knownSize) {
        return List.of();
      }
      return new List<E>(new DropRightListMaterializer<E>(materializer, maxElements));
    }

    @Override
    public @NotNull List<E> dropRightWhile(@NotNull final IndexedPredicate<? super E> predicate) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new List<E>(new DropRightWhileListMaterializer<E>(materializer,
          Require.notNull(predicate, "predicate")));
    }

    @Override
    public @NotNull List<E> dropRightWhile(@NotNull final Predicate<? super E> predicate) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new List<E>(new DropRightWhileListMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull List<E> dropWhile(@NotNull final IndexedPredicate<? super E> predicate) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new List<E>(
          new DropWhileListMaterializer<E>(materializer, Require.notNull(predicate, "predicate")));
    }

    @Override
    public @NotNull List<E> dropWhile(@NotNull final Predicate<? super E> predicate) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new List<E>(new DropWhileListMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull List<Boolean> each(@NotNull final IndexedPredicate<? super E> predicate) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return FALSE_LIST;
      }
      return new List<Boolean>(
          new EachListMaterializer<E>(materializer, Require.notNull(predicate, "predicate"),
              false));
    }

    @Override
    public @NotNull List<Boolean> each(@NotNull final Predicate<? super E> predicate) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return FALSE_LIST;
      }
      return new List<Boolean>(new EachListMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate")), false));
    }

    @Override
    public @NotNull List<Boolean> endsWith(@NotNull final Iterable<?> elements) {
      if (getKnownSize(elements) == 0) {
        return TRUE_LIST;
      }
      return new List<Boolean>(new EndsWithListMaterializer<E>(materializer,
          getElementsMaterializer(Require.notNull(elements, "elements"))));
    }

    @Override
    public @NotNull List<Boolean> exists(@NotNull final IndexedPredicate<? super E> predicate) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return FALSE_LIST;
      }
      return new List<Boolean>(
          new ExistsListMaterializer<E>(materializer, Require.notNull(predicate, "predicate"),
              false));
    }

    @Override
    public @NotNull List<Boolean> exists(@NotNull final Predicate<? super E> predicate) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return FALSE_LIST;
      }
      return new List<Boolean>(new ExistsListMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate")), false));
    }

    @Override
    public @NotNull List<E> filter(@NotNull final IndexedPredicate<? super E> predicate) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new List<E>(new RemoveWhereListMaterializer<E>(materializer,
          negated(Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull List<E> filter(@NotNull final Predicate<? super E> predicate) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new List<E>(new RemoveWhereListMaterializer<E>(materializer,
          toNegatedIndexedPredicate(Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull List<E> findAny(@NotNull final IndexedPredicate<? super E> predicate) {
      return findFirst(predicate);
    }

    @Override
    public @NotNull List<E> findAny(@NotNull final Predicate<? super E> predicate) {
      return findFirst(predicate);
    }

    @Override
    public @NotNull List<E> findFirst(@NotNull final IndexedPredicate<? super E> predicate) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new List<E>(
          new FindFirstListMaterializer<E>(materializer, Require.notNull(predicate, "predicate")));
    }

    @Override
    public @NotNull List<E> findFirst(@NotNull final Predicate<? super E> predicate) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new List<E>(new FindFirstListMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull List<Integer> findIndexOf(final Object element) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return List.of();
      }
      return new List<Integer>(
          new FindIndexListMaterializer<E>(materializer, equalsElement(element)));
    }

    @Override
    public @NotNull List<Integer> findIndexOfSlice(@NotNull final Iterable<?> elements) {
      return new List<Integer>(new FindIndexOfSliceListMaterializer<E>(materializer,
          getElementsMaterializer(Require.notNull(elements, "elements"))));
    }

    @Override
    public @NotNull List<Integer> findIndexWhere(
        @NotNull final IndexedPredicate<? super E> predicate) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return List.of();
      }
      return new List<Integer>(
          new FindIndexListMaterializer<E>(materializer, Require.notNull(predicate, "predicate")));
    }

    @Override
    public @NotNull List<Integer> findIndexWhere(@NotNull final Predicate<? super E> predicate) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return List.of();
      }
      return new List<Integer>(new FindIndexListMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull List<E> findLast(@NotNull final IndexedPredicate<? super E> predicate) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new List<E>(
          new FindLastListMaterializer<E>(materializer, Require.notNull(predicate, "predicate")));
    }

    @Override
    public @NotNull List<E> findLast(@NotNull final Predicate<? super E> predicate) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new List<E>(new FindLastListMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull List<Integer> findLastIndexOf(final Object element) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return List.of();
      }
      return new List<Integer>(
          new FindLastIndexListMaterializer<E>(materializer, equalsElement(element)));
    }

    @Override
    public @NotNull List<Integer> findLastIndexOfSlice(@NotNull final Iterable<?> elements) {
      if (getKnownSize(elements) == 0) {
        final int knownSize = materializer.knownSize();
        if (knownSize >= 0) {
          return List.of(knownSize);
        }
      }
      return new List<Integer>(new FindLastIndexOfSliceListMaterializer<E>(materializer,
          getElementsMaterializer(Require.notNull(elements, "elements"))));
    }

    @Override
    public @NotNull List<Integer> findLastIndexWhere(
        @NotNull final IndexedPredicate<? super E> predicate) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return List.of();
      }
      return new List<Integer>(new FindLastIndexListMaterializer<E>(materializer,
          Require.notNull(predicate, "predicate")));
    }

    @Override
    public @NotNull List<Integer> findLastIndexWhere(
        @NotNull final Predicate<? super E> predicate) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return List.of();
      }
      return new List<Integer>(new FindLastIndexListMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate"))));
    }

    @Override
    public E first() {
      return materializer.materializeElement(0);
    }

    @Override
    public @NotNull <F> List<F> flatMap(
        @NotNull final Function<? super E, ? extends Iterable<F>> mapper) {
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return List.of();
      }
      if (knownSize == 1) {
        return new List<F>(new SingleFlatMapListMaterializer<E, F>(materializer,
            getElementToMaterializer(Require.notNull(mapper, "mapper"))));
      }
      return new List<F>(new FlatMapListMaterializer<E, F>(materializer,
          toIndexedFunction(Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull <F> List<F> flatMap(
        @NotNull final IndexedFunction<? super E, ? extends Iterable<F>> mapper) {
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return List.of();
      }
      if (knownSize == 1) {
        return new List<F>(new SingleFlatMapListMaterializer<E, F>(materializer,
            getElementToMaterializer(Require.notNull(mapper, "mapper"))));
      }
      return new List<F>(
          new FlatMapListMaterializer<E, F>(materializer, Require.notNull(mapper, "mapper")));
    }

    @Override
    public @NotNull List<E> flatMapAfter(final int numElements,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      if (numElements < 0 || numElements == Integer.MAX_VALUE) {
        return this;
      }
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0 || (knownSize > 0 && knownSize <= numElements)) {
        return this;
      }
      if (numElements == 0 && knownSize == 1) {
        return new List<E>(new SingleFlatMapListMaterializer<E, E>(materializer,
            getElementToMaterializer(Require.notNull(mapper, "mapper"))));
      }
      return new List<E>(new FlatMapAfterListMaterializer<E>(materializer, numElements,
          getElementToMaterializer(Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull List<E> flatMapAfter(final int numElements,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      if (numElements < 0 || numElements == Integer.MAX_VALUE) {
        return this;
      }
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0 || (knownSize > 0 && knownSize <= numElements)) {
        return this;
      }
      if (numElements == 0 && knownSize == 1) {
        return new List<E>(new SingleFlatMapListMaterializer<E, E>(materializer,
            getElementToMaterializer(Require.notNull(mapper, "mapper"))));
      }
      return new List<E>(new FlatMapAfterListMaterializer<E>(materializer, numElements,
          getElementToMaterializer(Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull List<E> flatMapFirstWhere(@NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return List.of();
      }
      if (knownSize == 1) {
        return new List<E>(new SingleFlatMapWhereListMaterializer<E>(materializer,
            Require.notNull(predicate, "predicate"),
            getElementToMaterializer(Require.notNull(mapper, "mapper"))));
      }
      return new List<E>(new FlatMapFirstWhereListMaterializer<E>(materializer,
          Require.notNull(predicate, "predicate"),
          getElementToMaterializer(Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull List<E> flatMapFirstWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return List.of();
      }
      if (knownSize == 1) {
        return new List<E>(new SingleFlatMapWhereListMaterializer<E>(materializer,
            toIndexedPredicate(Require.notNull(predicate, "predicate")),
            getElementToMaterializer(Require.notNull(mapper, "mapper"))));
      }
      return new List<E>(new FlatMapFirstWhereListMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate")),
          getElementToMaterializer(Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull List<E> flatMapLastWhere(@NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return List.of();
      }
      if (knownSize == 1) {
        return new List<E>(new SingleFlatMapWhereListMaterializer<E>(materializer,
            Require.notNull(predicate, "predicate"),
            getElementToMaterializer(Require.notNull(mapper, "mapper"))));
      }
      return new List<E>(new FlatMapLastWhereListMaterializer<E>(materializer,
          Require.notNull(predicate, "predicate"),
          getElementToMaterializer(Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull List<E> flatMapLastWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return List.of();
      }
      if (knownSize == 1) {
        return new List<E>(new SingleFlatMapWhereListMaterializer<E>(materializer,
            toIndexedPredicate(Require.notNull(predicate, "predicate")),
            getElementToMaterializer(Require.notNull(mapper, "mapper"))));
      }
      return new List<E>(new FlatMapLastWhereListMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate")),
          getElementToMaterializer(Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull List<E> flatMapWhere(@NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return List.of();
      }
      if (knownSize == 1) {
        return new List<E>(new SingleFlatMapWhereListMaterializer<E>(materializer,
            Require.notNull(predicate, "predicate"),
            getElementToMaterializer(Require.notNull(mapper, "mapper"))));
      }
      return new List<E>(
          new FlatMapWhereListMaterializer<E>(materializer, Require.notNull(predicate, "predicate"),
              Require.notNull(mapper, "mapper")));
    }

    @Override
    public @NotNull List<E> flatMapWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return List.of();
      }
      if (knownSize == 1) {
        return new List<E>(new SingleFlatMapWhereListMaterializer<E>(materializer,
            toIndexedPredicate(Require.notNull(predicate, "predicate")),
            getElementToMaterializer(Require.notNull(mapper, "mapper"))));
      }
      return new List<E>(new FlatMapWhereListMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate")),
          toIndexedFunction(Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull <F> List<F> fold(final F identity,
        @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
      return foldLeft(identity, operation);
    }

    @Override
    public @NotNull <F> List<F> foldLeft(final F identity,
        @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return List.of(identity);
      }
      return new List<F>(new FoldLeftListMaterializer<E, F>(materializer, identity,
          Require.notNull(operation, "operation")));
    }

    @Override
    public @NotNull <F> List<F> foldLeftWhile(final F identity,
        @NotNull final Predicate<? super F> predicate,
        @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return List.of(identity);
      }
      return new List<F>(new FoldLeftWhileListMaterializer<E, F>(materializer, identity,
          Require.notNull(predicate, "predicate"), Require.notNull(operation, "operation")));
    }

    @Override
    public @NotNull <F> List<F> foldRight(final F identity,
        @NotNull final BinaryFunction<? super E, ? super F, ? extends F> operation) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return List.of(identity);
      }
      return new List<F>(new FoldRightListMaterializer<E, F>(materializer, identity,
          Require.notNull(operation, "operation")));
    }

    @Override
    public @NotNull <F> List<F> foldRightWhile(final F identity,
        @NotNull final Predicate<? super F> predicate,
        @NotNull final BinaryFunction<? super E, ? super F, ? extends F> operation) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return List.of(identity);
      }
      return new List<F>(new FoldRightWhileListMaterializer<E, F>(materializer, identity,
          Require.notNull(predicate, "predicate"), Require.notNull(operation, "operation")));
    }

    @Override
    public E get(final int index) {
      if (index < 0) {
        throw new IndexOutOfBoundsException(Integer.toString(index));
      }
      return materializer.materializeElement(index);
    }

    @Override
    public @NotNull List<Boolean> includes(final Object element) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return FALSE_LIST;
      }
      return new List<Boolean>(
          new ExistsListMaterializer<E>(materializer, equalsElement(element), false));
    }

    @Override
    public @NotNull List<Boolean> includesAll(@NotNull final Iterable<?> elements) {
      return new List<Boolean>(
          new IncludesAllListMaterializer<E>(materializer, Require.notNull(elements, "elements")));
    }

    @Override
    public @NotNull List<Boolean> includesSlice(@NotNull final Iterable<?> elements) {
      return new List<Boolean>(new IncludesSliceListMaterializer<E>(materializer,
          getElementsMaterializer(Require.notNull(elements, "elements"))));
    }

    @Override
    public int indexOf(final Object o) {
      final ListMaterializer<E> materializer = this.materializer;
      int index = 0;
      if (o == null) {
        while (materializer.canMaterializeElement(index)) {
          if (materializer.materializeElement(index) == null) {
            return index;
          }
          ++index;
        }
      } else {
        while (materializer.canMaterializeElement(index)) {
          if (o.equals(materializer.materializeElement(index))) {
            return index;
          }
          ++index;
        }
      }
      return -1;
    }

    @Override
    public @NotNull List<E> insertAfter(final int numElements, final E element) {
      if (numElements < 0 || numElements == Integer.MAX_VALUE) {
        return this;
      }
      if (numElements == 0) {
        return prepend(element);
      }
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return this;
      }
      if (knownSize > 0) {
        if (knownSize < numElements) {
          return this;
        }
        if (knownSize == numElements) {
          return append(element);
        }
      }
      return new List<E>(new InsertAfterListMaterializer<E>(materializer, numElements, element));
    }

    @Override
    public @NotNull List<E> insertAllAfter(final int numElements,
        @NotNull final Iterable<? extends E> elements) {
      if (numElements < 0 || numElements == Integer.MAX_VALUE) {
        return this;
      }
      if (numElements == 0) {
        return prependAll(elements);
      }
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return this;
      }
      if (knownSize > 0) {
        if (knownSize < numElements) {
          return this;
        }
        if (knownSize == numElements) {
          return appendAll(elements);
        }
      }
      return new List<E>(new InsertAllAfterListMaterializer<E>(materializer, numElements,
          getElementsMaterializer(Require.notNull(elements, "elements"))));
    }

    @Override
    public @NotNull List<E> intersect(@NotNull final Iterable<?> elements) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      if (getKnownSize(elements) == 0) {
        return List.of();
      }
      return new List<E>(new IntersectListMaterializer<E>(materializer,
          getElementsMaterializer(Require.notNull(elements, "elements"))));
    }

    @Override
    public boolean isEmpty() {
      return materializer.materializeEmpty();
    }

    @Override
    public @NotNull Iterator<E> iterator() {
      return Iterator.wrap(this);
    }

    @Override
    public E last() {
      if (isEmpty()) {
        throw new IndexOutOfBoundsException("0");
      }
      return materializer.materializeElement(size() - 1);
    }

    @Override
    public int lastIndexOf(final Object o) {
      final ListMaterializer<E> materializer = this.materializer;
      int index = materializer.materializeSize() - 1;
      if (o == null) {
        while (index >= 0) {
          if (materializer.materializeElement(index) == null) {
            return index;
          }
          --index;
        }
      } else {
        while (index >= 0) {
          if (o.equals(materializer.materializeElement(index))) {
            return index;
          }
          --index;
        }
      }
      return -1;
    }

    @Override
    public @NotNull ListIterator<E> listIterator() {
      return new ListIterator<E>(this);
    }

    @Override
    public @NotNull ListIterator<E> listIterator(final int index) {
      if (index < 0 || index == Integer.MAX_VALUE) {
        throw new IndexOutOfBoundsException(Integer.toString(index));
      }
      final int knownSize = materializer.knownSize();
      if (knownSize >= 0 && index > knownSize) {
        throw new IndexOutOfBoundsException(Integer.toString(index));
      }
      return new ListIterator<E>(this, index);
    }

    @Override
    public @NotNull <F> List<F> map(@NotNull final Function<? super E, F> mapper) {
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return List.of();
      }
      if (knownSize == 1) {
        return new List<F>(new SingleMapListMaterializer<E, F>(materializer,
            toIndexedFunction(Require.notNull(mapper, "mapper"))));
      }
      return new List<F>(new MapListMaterializer<E, F>(materializer,
          toIndexedFunction(Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull <F> List<F> map(@NotNull final IndexedFunction<? super E, F> mapper) {
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return List.of();
      }
      if (knownSize == 1) {
        return new List<F>(
            new SingleMapListMaterializer<E, F>(materializer, Require.notNull(mapper, "mapper")));
      }
      return new List<F>(
          new MapListMaterializer<E, F>(materializer, Require.notNull(mapper, "mapper")));
    }

    @Override
    public @NotNull List<E> mapAfter(final int numElements,
        @NotNull final Function<? super E, ? extends E> mapper) {
      if (numElements < 0 || numElements == Integer.MAX_VALUE) {
        return this;
      }
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return this;
      }
      if (numElements == 0 && knownSize == 1) {
        return new List<E>(new SingleMapListMaterializer<E, E>(materializer,
            toIndexedFunction(Require.notNull(mapper, "mapper"))));
      }
      return new List<E>(new MapAfterListMaterializer<E>(materializer, numElements,
          toIndexedFunction(Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull List<E> mapAfter(final int numElements,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      if (numElements < 0 || numElements == Integer.MAX_VALUE) {
        return this;
      }
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return this;
      }
      if (numElements == 0 && knownSize == 1) {
        return new List<E>(
            new SingleMapListMaterializer<E, E>(materializer, Require.notNull(mapper, "mapper")));
      }
      return new List<E>(new MapAfterListMaterializer<E>(materializer, numElements,
          Require.notNull(mapper, "mapper")));
    }

    @Override
    public @NotNull List<E> mapFirstWhere(@NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return this;
      }
      if (knownSize == 1) {
        return new List<E>(new SingleMapWhereListMaterializer<E>(materializer,
            Require.notNull(predicate, "predicate"), Require.notNull(mapper, "mapper")));
      }
      return new List<E>(new MapFirstWhereListMaterializer<E>(materializer,
          Require.notNull(predicate, "predicate"), Require.notNull(mapper, "mapper")));
    }

    @Override
    public @NotNull List<E> mapFirstWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends E> mapper) {
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return this;
      }
      if (knownSize == 1) {
        return new List<E>(new SingleMapWhereListMaterializer<E>(materializer,
            toIndexedPredicate(Require.notNull(predicate, "predicate")),
            toIndexedFunction(Require.notNull(mapper, "mapper"))));
      }
      return new List<E>(new MapFirstWhereListMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate")),
          toIndexedFunction(Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull List<E> mapLastWhere(@NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return this;
      }
      if (knownSize == 1) {
        return new List<E>(new SingleMapWhereListMaterializer<E>(materializer,
            Require.notNull(predicate, "predicate"), Require.notNull(mapper, "mapper")));
      }
      return new List<E>(
          new MapLastWhereListMaterializer<E>(materializer, Require.notNull(predicate, "predicate"),
              Require.notNull(mapper, "mapper")));
    }

    @Override
    public @NotNull List<E> mapLastWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends E> mapper) {
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return this;
      }
      if (knownSize == 1) {
        return new List<E>(new SingleMapWhereListMaterializer<E>(materializer,
            toIndexedPredicate(Require.notNull(predicate, "predicate")),
            toIndexedFunction(Require.notNull(mapper, "mapper"))));
      }
      return new List<E>(new MapLastWhereListMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate")),
          toIndexedFunction(Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull List<E> mapWhere(@NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new List<E>(new MapListMaterializer<E, E>(materializer,
          filteredMapper(Require.notNull(predicate, "predicate"),
              Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull List<E> mapWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends E> mapper) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new List<E>(new MapListMaterializer<E, E>(materializer,
          filteredMapper(Require.notNull(predicate, "predicate"),
              Require.notNull(mapper, "mapper"))));
    }

    @Override
    public @NotNull List<E> max(@NotNull final Comparator<? super E> comparator) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new List<E>(
          new MaxListMaterializer<E>(materializer, Require.notNull(comparator, "comparator")));
    }

    @Override
    public @NotNull List<E> min(@NotNull final Comparator<? super E> comparator) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new List<E>(new MaxListMaterializer<E>(materializer,
          reversed(Require.notNull(comparator, "comparator"))));
    }

    @Override
    public @NotNull List<Boolean> none(@NotNull final IndexedPredicate<? super E> predicate) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return TRUE_LIST;
      }
      return new List<Boolean>(new EachListMaterializer<E>(materializer,
          negated(Require.notNull(predicate, "predicate")), true));
    }

    @Override
    public @NotNull List<Boolean> none(@NotNull final Predicate<? super E> predicate) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return TRUE_LIST;
      }
      return new List<Boolean>(new EachListMaterializer<E>(materializer,
          toNegatedIndexedPredicate(Require.notNull(predicate, "predicate")), true));
    }

    @Override
    public @NotNull List<Boolean> notAll(@NotNull final IndexedPredicate<? super E> predicate) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return TRUE_LIST;
      }
      return new List<Boolean>(new ExistsListMaterializer<E>(materializer,
          negated(Require.notNull(predicate, "predicate")), true));
    }

    @Override
    public @NotNull List<Boolean> notAll(@NotNull final Predicate<? super E> predicate) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return TRUE_LIST;
      }
      return new List<Boolean>(new ExistsListMaterializer<E>(materializer,
          toNegatedIndexedPredicate(Require.notNull(predicate, "predicate")), true));
    }

    @Override
    public @NotNull List<E> orElse(@NotNull final Iterable<? extends E> elements) {
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      final ListMaterializer<E> elementsMaterializer = getElementsMaterializer(
          Require.notNull(elements, "elements"));
      if (knownSize == 0) {
        return new List<E>(elementsMaterializer);
      }
      return new List<E>(new OrElseListMaterializer<E>(materializer, elementsMaterializer));
    }

    @Override
    public @NotNull List<E> orElseGet(
        @NotNull final Supplier<? extends Iterable<? extends E>> supplier) {
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return new List<E>(new SuppliedMaterializer<E>(Require.notNull(supplier, "supplier")));
      }
      if (knownSize > 0) {
        return this;
      }
      return new List<E>(new OrElseListMaterializer<E>(materializer,
          new SuppliedMaterializer<E>(Require.notNull(supplier, "supplier"))));
    }

    @Override
    public @NotNull List<E> plus(final E element) {
      return append(element);
    }

    @Override
    public @NotNull List<E> plusAll(@NotNull final Iterable<? extends E> elements) {
      return appendAll(elements);
    }

    @Override
    public @NotNull List<E> prepend(final E element) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return List.of(element);
      }
      return new List<E>(new PrependListMaterializer<E>(materializer, element));
    }

    @Override
    public @NotNull List<E> prependAll(@NotNull final Iterable<? extends E> elements) {
      final ListMaterializer<E> materializer = this.materializer;
      final ListMaterializer<E> elementsMaterializer = getElementsMaterializer(
          Require.notNull(elements, "elements"));
      if (materializer.knownSize() == 0) {
        return new List<E>(elementsMaterializer);
      }
      return new List<E>(new PrependAllListMaterializer<E>(materializer, elementsMaterializer));
    }

    @Override
    public @NotNull List<E> reduce(
        @NotNull final BinaryFunction<? super E, ? super E, ? extends E> operation) {
      return reduceLeft(operation);
    }

    @Override
    public @NotNull List<E> reduceLeft(
        @NotNull final BinaryFunction<? super E, ? super E, ? extends E> operation) {
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0 || knownSize == 1) {
        return this;
      }
      return new List<E>(
          new ReduceLeftListMaterializer<E>(materializer, Require.notNull(operation, "operation")));
    }

    @Override
    public @NotNull List<E> reduceRight(
        @NotNull final BinaryFunction<? super E, ? super E, ? extends E> operation) {
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0 || knownSize == 1) {
        return this;
      }
      return new List<E>(new ReduceRightListMaterializer<E>(materializer,
          Require.notNull(operation, "operation")));
    }

    @Override
    public @NotNull List<E> removeAfter(final int numElements) {
      if (numElements < 0 || numElements == Integer.MAX_VALUE) {
        return this;
      }
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return this;
      }
      if (numElements == 0 && knownSize == 1) {
        return List.of();
      }
      return new List<E>(new RemoveAfterListMaterializer<E>(materializer, numElements));
    }

    @Override
    public @NotNull List<E> removeEach(final E element) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new List<E>(new RemoveWhereListMaterializer<E>(materializer, equalsElement(element)));
    }

    @Override
    public @NotNull List<E> removeFirst(final E element) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new List<E>(
          new RemoveFirstWhereListMaterializer<E>(materializer, equalsElement(element)));
    }

    @Override
    public @NotNull List<E> removeFirstWhere(@NotNull final IndexedPredicate<? super E> predicate) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new List<E>(new RemoveFirstWhereListMaterializer<E>(materializer,
          Require.notNull(predicate, "predicate")));
    }

    @Override
    public @NotNull List<E> removeFirstWhere(@NotNull final Predicate<? super E> predicate) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new List<E>(new RemoveFirstWhereListMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull List<E> removeLast(final E element) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new List<E>(
          new RemoveLastWhereListMaterializer<E>(materializer, equalsElement(element)));
    }

    @Override
    public @NotNull List<E> removeLastWhere(@NotNull final IndexedPredicate<? super E> predicate) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new List<E>(new RemoveLastWhereListMaterializer<E>(materializer,
          Require.notNull(predicate, "predicate")));
    }

    @Override
    public @NotNull List<E> removeLastWhere(@NotNull final Predicate<? super E> predicate) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new List<E>(new RemoveLastWhereListMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull List<E> removeSlice(final int start, final int end) {
      if (end >= 0 && start >= end) {
        return this;
      }
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return this;
      }
      if (knownSize > 0) {
        final int knownStart;
        if (start < 0) {
          knownStart = Math.max(0, knownSize + start);
        } else {
          knownStart = Math.min(knownSize, start);
        }
        final int knownEnd;
        if (end < 0) {
          knownEnd = Math.max(0, knownSize + end);
        } else {
          knownEnd = Math.min(knownSize, end);
        }
        if (knownStart >= knownEnd) {
          return this;
        }
        final int knownLength = knownEnd - knownStart;
        if (knownLength == 1) {
          return removeAfter(knownStart);
        }
        if (knownLength == knownSize) {
          return List.of();
        }
      }
      return new List<E>(new RemoveSliceListMaterializer<E>(materializer, start, end));
    }

    @Override
    public @NotNull List<E> removeWhere(@NotNull final IndexedPredicate<? super E> predicate) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new List<E>(new RemoveWhereListMaterializer<E>(materializer,
          Require.notNull(predicate, "predicate")));
    }

    @Override
    public @NotNull List<E> removeWhere(@NotNull final Predicate<? super E> predicate) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new List<E>(new RemoveWhereListMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull List<E> replaceAfter(final int numElements, final E replacement) {
      if (numElements < 0 || numElements == Integer.MAX_VALUE) {
        return this;
      }
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return this;
      }
      if (numElements == 0 && knownSize == 1) {
        return new List<E>(
            new SingleMapListMaterializer<E, E>(materializer, replacementMapper(replacement)));
      }
      return new List<E>(new MapAfterListMaterializer<E>(materializer, numElements,
          replacementMapper(replacement)));
    }

    @Override
    public @NotNull List<E> replaceEach(final E element, final E replacement) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new List<E>(new MapListMaterializer<E, E>(materializer,
          filteredMapper(equalsElement(element), replacementMapper(replacement))));
    }

    @Override
    public @NotNull List<E> replaceFirst(final E element, final E replacement) {
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return this;
      }
      if (knownSize == 1) {
        return new List<E>(
            new SingleMapWhereListMaterializer<E>(materializer, equalsElement(element),
                replacementMapper(replacement)));
      }
      return new List<E>(new MapFirstWhereListMaterializer<E>(materializer, equalsElement(element),
          replacementMapper(replacement)));
    }

    @Override
    public @NotNull List<E> replaceFirstWhere(@NotNull final IndexedPredicate<? super E> predicate,
        final E replacement) {
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return this;
      }
      if (knownSize == 1) {
        return new List<E>(new SingleMapWhereListMaterializer<E>(materializer,
            Require.notNull(predicate, "predicate"), replacementMapper(replacement)));
      }
      return new List<E>(new MapFirstWhereListMaterializer<E>(materializer,
          Require.notNull(predicate, "predicate"), replacementMapper(replacement)));
    }

    @Override
    public @NotNull List<E> replaceFirstWhere(@NotNull final Predicate<? super E> predicate,
        final E replacement) {
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return this;
      }
      if (knownSize == 1) {
        return new List<E>(new SingleMapWhereListMaterializer<E>(materializer,
            toIndexedPredicate(Require.notNull(predicate, "predicate")),
            replacementMapper(replacement)));
      }
      return new List<E>(new MapFirstWhereListMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate")),
          replacementMapper(replacement)));
    }

    @Override
    public @NotNull List<E> replaceLast(final E element, final E replacement) {
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return this;
      }
      if (knownSize == 1) {
        return new List<E>(
            new SingleMapWhereListMaterializer<E>(materializer, equalsElement(element),
                replacementMapper(replacement)));
      }
      return new List<E>(new MapLastWhereListMaterializer<E>(materializer, equalsElement(element),
          replacementMapper(replacement)));
    }

    @Override
    public @NotNull List<E> replaceLastWhere(@NotNull final IndexedPredicate<? super E> predicate,
        final E replacement) {
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return this;
      }
      if (knownSize == 1) {
        return new List<E>(new SingleMapWhereListMaterializer<E>(materializer,
            Require.notNull(predicate, "predicate"), replacementMapper(replacement)));
      }
      return new List<E>(
          new MapLastWhereListMaterializer<E>(materializer, Require.notNull(predicate, "predicate"),
              replacementMapper(replacement)));
    }

    @Override
    public @NotNull List<E> replaceLastWhere(@NotNull final Predicate<? super E> predicate,
        final E replacement) {
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return this;
      }
      if (knownSize == 1) {
        return new List<E>(new SingleMapWhereListMaterializer<E>(materializer,
            toIndexedPredicate(Require.notNull(predicate, "predicate")),
            replacementMapper(replacement)));
      }
      return new List<E>(new MapLastWhereListMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate")),
          replacementMapper(replacement)));
    }

    @Override
    public @NotNull List<E> replaceSlice(final int start, final int end,
        @NotNull final Iterable<? extends E> patch) {
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize >= 0) {
        final int knownStart;
        if (start < 0) {
          knownStart = Math.max(0, knownSize + start);
        } else {
          knownStart = Math.min(knownSize, start);
        }
        final int knownEnd;
        if (end < 0) {
          knownEnd = Math.max(0, knownSize + end);
        } else {
          knownEnd = Math.min(knownSize, end);
        }
        if (knownStart >= knownEnd) {
          return insertAllAfter(knownStart, patch);
        }
        final int knownLength = knownEnd - knownStart;
        if (knownLength == knownSize) {
          return new List<E>(getElementsMaterializer(Require.notNull(patch, "patch")));
        }
      }
      if (getKnownSize(patch) == 0) {
        return new List<E>(new RemoveSliceListMaterializer<E>(materializer, start, end));
      }
      return new List<E>(new ReplaceSliceListMaterializer<E>(materializer, start, end,
          getElementsMaterializer(Require.notNull(patch, "patch"))));
    }

    @Override
    public @NotNull List<E> replaceWhere(@NotNull final IndexedPredicate<? super E> predicate,
        final E replacement) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new List<E>(new MapListMaterializer<E, E>(materializer,
          filteredMapper(Require.notNull(predicate, "predicate"), replacementMapper(replacement))));
    }

    @Override
    public @NotNull List<E> replaceWhere(@NotNull final Predicate<? super E> predicate,
        final E replacement) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new List<E>(new MapListMaterializer<E, E>(materializer,
          filteredMapper(Require.notNull(predicate, "predicate"), replacementMapper(replacement))));
    }

    @Override
    public @NotNull List<E> resizeTo(@NotNegative final int numElements, final E padding) {
      Require.notNegative(numElements, "numElements");
      if (numElements == 0) {
        return List.of();
      }
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize >= 0) {
        if (knownSize == 0) {
          return List.times(numElements, padding);
        }
        if (knownSize == numElements) {
          return this;
        }
        if (knownSize > numElements) {
          return new List<E>(new TakeListMaterializer<E>(materializer, numElements));
        }
        return new List<E>(new AppendAllListMaterializer<E>(materializer,
            new RepeatListMaterializer<E>(numElements - knownSize, padding)));
      }
      return new List<E>(new ResizeListMaterializer<E>(materializer, numElements, padding));
    }

    @Override
    public @NotNull List<E> reverse() {
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0 || knownSize == 1) {
        return this;
      }
      return new List<E>(new ReverseListMaterializer<E>(materializer));
    }

    @Override
    public int size() {
      return materializer.materializeSize();
    }

    @Override
    public @NotNull List<E> slice(final int start) {
      return slice(start, Integer.MAX_VALUE);
    }

    @Override
    public @NotNull List<E> slice(final int start, final int end) {
      if (end == Integer.MAX_VALUE && start >= 0) {
        return drop(start);
      }
      if (start == 0 && end >= 0) {
        return take(end);
      }
      if ((start == end) || (end >= 0 && start >= end)) {
        return List.of();
      }
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0) {
        return this;
      }
      if (knownSize > 0) {
        final int knownStart;
        if (start < 0) {
          knownStart = Math.max(0, knownSize + start);
        } else {
          knownStart = Math.min(knownSize, start);
        }
        final int knownEnd;
        if (end < 0) {
          knownEnd = Math.max(0, knownSize + end);
        } else {
          knownEnd = Math.min(knownSize, end);
        }
        if (knownStart >= knownEnd) {
          return List.of();
        }
      }
      return new List<E>(new SliceListMaterializer<E>(materializer, start, end));
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull List<? extends List<E>> slidingWindow(@Positive final int maxSize,
        @Positive final int step) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return List.of();
      }
      return new List<List<E>>(new SlidingWindowListMaterializer<E, List<E>>(materializer,
          Require.positive(maxSize, "maxSize"), Require.positive(step, "step"),
          (Splitter<E, ? extends List<E>>) SPLITTER));
    }

    @Override
    public @NotNull List<? extends List<E>> slidingWindowWithPadding(@Positive final int size,
        @Positive final int step, final E padding) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return List.of();
      }
      if (size == 1) {
        return slidingWindow(1, step);
      }
      return new List<List<E>>(new SlidingWindowListMaterializer<E, List<E>>(materializer, size,
          Require.positive(step, "step"), getSplitter(Require.positive(size, "size"), padding)));
    }

    @Override
    public @NotNull List<E> sorted(@NotNull final Comparator<? super E> comparator) {
      final ListMaterializer<E> materializer = this.materializer;
      final int knownSize = materializer.knownSize();
      if (knownSize == 0 || knownSize == 1) {
        return this;
      }
      return new List<E>(
          new SortedListMaterializer<E>(materializer, Require.notNull(comparator, "comparator")));
    }

    @Override
    public @NotNull List<Boolean> startsWith(@NotNull final Iterable<?> elements) {
      if (getKnownSize(elements) == 0) {
        return TRUE_LIST;
      }
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return FALSE_LIST;
      }
      return new List<Boolean>(new StartsWithListMaterializer<E>(materializer,
          getElementsMaterializer(Require.notNull(elements, "elements"))));
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull List<E> symmetricDiff(@NotNull final Iterable<? extends E> elements) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return (List<E>) List.from(elements);
      }
      if (getKnownSize(elements) == 0) {
        return this;
      }
      return new List<E>(new SymmetricDiffListMaterializer<E>(materializer,
          getElementsMaterializer(Require.notNull(elements, "elements"))));
    }

    @Override
    public @NotNull List<E> take(final int maxElements) {
      if (maxElements <= 0) {
        return List.of();
      }
      if (maxElements == Integer.MAX_VALUE) {
        return this;
      }
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new List<E>(new TakeListMaterializer<E>(materializer, maxElements));
    }

    @Override
    public @NotNull List<E> takeRight(final int maxElements) {
      if (maxElements <= 0) {
        return List.of();
      }
      if (maxElements == Integer.MAX_VALUE) {
        return this;
      }
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new List<E>(new TakeRightListMaterializer<E>(materializer, maxElements));
    }

    @Override
    public @NotNull List<E> takeRightWhile(@NotNull final IndexedPredicate<? super E> predicate) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new List<E>(new TakeRightWhileListMaterializer<E>(materializer,
          Require.notNull(predicate, "predicate")));
    }

    @Override
    public @NotNull List<E> takeRightWhile(@NotNull final Predicate<? super E> predicate) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new List<E>(new TakeRightWhileListMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate"))));
    }

    @Override
    public @NotNull List<E> takeWhile(@NotNull final IndexedPredicate<? super E> predicate) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new List<E>(
          new TakeWhileListMaterializer<E>(materializer, Require.notNull(predicate, "predicate")));
    }

    @Override
    public @NotNull List<E> takeWhile(@NotNull final Predicate<? super E> predicate) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return this;
      }
      return new List<E>(new TakeWhileListMaterializer<E>(materializer,
          toIndexedPredicate(Require.notNull(predicate, "predicate"))));
    }

    // TODO: extra
    public @NotNull future.List<E> toFuture(@NotNull final ExecutionContext context) {
      return new future.List<E>(Require.notNull(context, "context"),
          new AtomicReference<CancellationException>(),
          new ListToListFutureMaterializer<E>(this, context));
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull List<E> union(@NotNull final Iterable<? extends E> elements) {
      final ListMaterializer<E> materializer = this.materializer;
      if (materializer.knownSize() == 0) {
        return (List<E>) List.from(elements);
      }
      if (getKnownSize(elements) == 0) {
        return this;
      }
      return new List<E>(new AppendAllListMaterializer<E>(materializer, new DiffListMaterializer<E>(
          getElementsMaterializer(Require.notNull(elements, "elements")), materializer)));
    }

    int knownSize() {
      return materializer.knownSize();
    }

    private static class SuppliedMaterializer<E> implements ListMaterializer<E> {

      private volatile ListMaterializer<E> state;

      private SuppliedMaterializer(
          @NotNull final Supplier<? extends Iterable<? extends E>> supplier) {
        state = new ImmaterialState(Require.notNull(supplier, "supplier"));
      }

      @Override
      public boolean canMaterializeElement(@NotNegative final int index) {
        return state.canMaterializeElement(index);
      }

      @Override
      public int knownSize() {
        return state.knownSize();
      }

      @Override
      public boolean materializeContains(final Object element) {
        return state.materializeContains(element);
      }

      @Override
      public E materializeElement(@NotNegative final int index) {
        return state.materializeElement(index);
      }

      @Override
      public int materializeElements() {
        return state.materializeElements();
      }

      @Override
      public boolean materializeEmpty() {
        return state.materializeEmpty();
      }

      @Override
      public @NotNull java.util.Iterator<E> materializeIterator() {
        return state.materializeIterator();
      }

      @Override
      public int materializeSize() {
        return state.materializeSize();
      }

      private class ImmaterialState implements ListMaterializer<E> {

        private final Supplier<? extends Iterable<? extends E>> supplier;

        private ImmaterialState(@NotNull final Supplier<? extends Iterable<? extends E>> supplier) {
          this.supplier = supplier;
        }

        @Override
        public boolean canMaterializeElement(@NotNegative final int index) {
          try {
            final ListMaterializer<E> elementsMaterializer = getElementsMaterializer(
                supplier.get());
            return (state = elementsMaterializer).canMaterializeElement(index);
          } catch (final Exception e) {
            throw UncheckedException.throwUnchecked(e);
          }
        }

        @Override
        public int knownSize() {
          return -1;
        }

        @Override
        public boolean materializeContains(final Object element) {
          try {
            final ListMaterializer<E> elementsMaterializer = getElementsMaterializer(
                supplier.get());
            return (state = elementsMaterializer).materializeContains(element);
          } catch (final Exception e) {
            throw UncheckedException.throwUnchecked(e);
          }
        }

        @Override
        public E materializeElement(@NotNegative final int index) {
          try {
            final ListMaterializer<E> elementsMaterializer = getElementsMaterializer(
                supplier.get());
            return (state = elementsMaterializer).materializeElement(index);
          } catch (final Exception e) {
            throw UncheckedException.throwUnchecked(e);
          }
        }

        @Override
        public int materializeElements() {
          try {
            final ListMaterializer<E> elementsMaterializer = getElementsMaterializer(
                supplier.get());
            return (state = elementsMaterializer).materializeElements();
          } catch (final Exception e) {
            throw UncheckedException.throwUnchecked(e);
          }
        }

        @Override
        public boolean materializeEmpty() {
          try {
            final ListMaterializer<E> elementsMaterializer = getElementsMaterializer(
                supplier.get());
            return (state = elementsMaterializer).materializeEmpty();
          } catch (final Exception e) {
            throw UncheckedException.throwUnchecked(e);
          }
        }

        @Override
        public @NotNull java.util.Iterator<E> materializeIterator() {
          try {
            final ListMaterializer<E> elementsMaterializer = getElementsMaterializer(
                supplier.get());
            return (state = elementsMaterializer).materializeIterator();
          } catch (final Exception e) {
            throw UncheckedException.throwUnchecked(e);
          }
        }

        @Override
        public int materializeSize() {
          try {
            final ListMaterializer<E> elementsMaterializer = getElementsMaterializer(
                supplier.get());
            return (state = elementsMaterializer).materializeSize();
          } catch (final Exception e) {
            throw UncheckedException.throwUnchecked(e);
          }
        }
      }
    }
  }

  public static class ListIterator<E> implements itf.ListIterator<E> {

    private static final ListIterator<?> EMPTY_ITERATOR = new ListIterator<Object>(List.of());
    private static final ListIterator<Boolean> FALSE_ITERATOR = new ListIterator<Boolean>(
        List.of(true));
    private static final Function<? extends List<?>, ? extends ListIterator<?>> LIST_TO_ITERATOR = new Function<List<?>, ListIterator<?>>() {
      @Override
      public ListIterator<?> apply(final List<?> param) {
        return param.listIterator();
      }
    };
    private static final ListIterator<?> NULL_ITERATOR = new ListIterator<Object>(List.of(null));
    private static final ListIterator<Boolean> TRUE_ITERATOR = new ListIterator<Boolean>(
        List.of(true));
    private static final ListIterator<Integer> ZERO_ITERATOR = new ListIterator<Integer>(
        List.of(0));

    private final List<E> list;

    private int pos; // WARNING: pos cna be > list.size()

    ListIterator(@NotNull final List<E> list) {
      this.list = list;
    }

    ListIterator(@NotNull final List<E> list, final int pos) {
      this(list);
      this.pos = pos;
    }

    public static @NotNull <E> ListIterator<E> from(@NotNull final Iterable<E> elements) {
      return new ListIterator<E>(List.from(elements));
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> ListIterator<E> of() {
      return (ListIterator<E>) EMPTY_ITERATOR;
    }

    public static @NotNull <E> ListIterator<E> of(final E first) {
      if (first == null) {
        return NULL_ITERATOR.as();
      }
      if (Boolean.TRUE.equals(first)) {
        return TRUE_ITERATOR.as();
      }
      if (Boolean.FALSE.equals(first)) {
        return FALSE_ITERATOR.as();
      }
      if (Integer.valueOf(0).equals(first)) {
        return ZERO_ITERATOR.as();
      }
      return new ListIterator<E>(List.of(first));
    }

    public static @NotNull <E> ListIterator<E> of(final E first, final E second) {
      return new ListIterator<E>(List.of(first, second));
    }

    public static @NotNull <E> ListIterator<E> of(final E first, final E second, final E third) {
      return new ListIterator<E>(List.of(first, second, third));
    }

    public static @NotNull <E> ListIterator<E> of(final E first, final E second, final E third,
        final E fourth) {
      return new ListIterator<E>(List.of(first, second, third, fourth));
    }

    public static @NotNull <E> ListIterator<E> of(final E first, final E second, final E third,
        final E fourth, final E fifth) {
      return new ListIterator<E>(List.of(first, second, third, fourth, fifth));
    }

    public static @NotNull <E> ListIterator<E> of(final E first, final E second, final E third,
        final E fourth, final E fifth, final E sixth) {
      return new ListIterator<E>(List.of(first, second, third, fourth, fifth, sixth));
    }

    public static @NotNull <E> ListIterator<E> of(final E first, final E second, final E third,
        final E fourth, final E fifth, final E sixth, final E seventh) {
      return new ListIterator<E>(List.of(first, second, third, fourth, fifth, sixth, seventh));
    }

    public static @NotNull <E> ListIterator<E> of(final E first, final E second, final E third,
        final E fourth, final E fifth, final E sixth, final E seventh, final E eighth) {
      return new ListIterator<E>(
          List.of(first, second, third, fourth, fifth, sixth, seventh, eighth));
    }

    public static @NotNull <E> ListIterator<E> of(final E first, final E second, final E third,
        final E fourth, final E fifth, final E sixth, final E seventh, final E eighth,
        final E ninth) {
      return new ListIterator<E>(
          List.of(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth));
    }

    public static @NotNull <E> ListIterator<E> of(final E first, final E second, final E third,
        final E fourth, final E fifth, final E sixth, final E seventh, final E eighth,
        final E ninth, final E tenth) {
      return new ListIterator<E>(
          List.of(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth));
    }

    public static @NotNull <E> ListIterator<E> ofArray(final E... elements) {
      return new ListIterator<E>(List.ofArray(elements));
    }

    public static @NotNull ListIterator<Character> ofChars(@NotNull final CharSequence chars) {
      return new ListIterator<Character>(List.ofChars(chars));
    }

    public static @NotNull ListIterator<Double> ofDoubles(final double... elements) {
      if (elements == null) {
        return ListIterator.of();
      }
      return new ListIterator<Double>(List.ofDoubles(elements));
    }

    public static @NotNull ListIterator<Float> ofFloats(final float... elements) {
      if (elements == null) {
        return ListIterator.of();
      }
      return new ListIterator<Float>(List.ofFloats(elements));
    }

    public static @NotNull ListIterator<Integer> ofInts(final int... elements) {
      if (elements == null) {
        return ListIterator.of();
      }
      return new ListIterator<Integer>(List.ofInts(elements));
    }

    public static @NotNull ListIterator<Long> ofLongs(final long... elements) {
      if (elements == null) {
        return ListIterator.of();
      }
      return new ListIterator<Long>(List.ofLongs(elements));
    }

    public static @NotNull <E> ListIterator<E> times(final int count, final E element) {
      if (count == 0) {
        return ListIterator.of();
      }
      return new ListIterator<E>(List.times(count, element));
    }

    @SuppressWarnings("unchecked")
    public static @NotNull <E> ListIterator<E> wrap(@NotNull final Iterable<? extends E> elements) {
      if (elements instanceof ListIterator) {
        return (ListIterator<E>) elements;
      }
      return new ListIterator<E>(List.wrap(elements));
    }

    @Override
    public void add(final E e) {
      throw new UnsupportedOperationException();
    }

    @Override
    public <T> T apply(@NotNull Function<? super itf.Sequence<E>, T> mapper) {
      return null;
    }

    @Override
    public @NotNull ListIterator<E> append(final E element) {
      return new ListIterator<E>(nextList().append(element));
    }

    @Override
    public @NotNull ListIterator<E> appendAll(@NotNull final Iterable<? extends E> elements) {
      return new ListIterator<E>(nextList().appendAll(elements));
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <F> ListIterator<F> as() {
      return (ListIterator<F>) this;
    }

    @Override
    public @NotNull ListIterator<Integer> count() {
      if (atEnd()) {
        return ZERO_ITERATOR;
      }
      return new ListIterator<Integer>(nextList().count());
    }

    @Override
    public @NotNull ListIterator<Integer> countWhere(
        @NotNull final IndexedPredicate<? super E> predicate) {
      if (atEnd()) {
        return ZERO_ITERATOR;
      }
      return new ListIterator<Integer>(nextList().countWhere(predicate));
    }

    @Override
    public @NotNull ListIterator<Integer> countWhere(
        @NotNull final Predicate<? super E> predicate) {
      if (atEnd()) {
        return ZERO_ITERATOR;
      }
      return new ListIterator<Integer>(nextList().countWhere(predicate));
    }

    @Override
    public @NotNull ListIterator<E> diff(@NotNull final Iterable<?> elements) {
      return new ListIterator<E>(nextList().diff(elements));
    }

    @Override
    public @NotNull ListIterator<E> distinct() {
      return new ListIterator<E>(nextList().distinct());
    }

    @Override
    public @NotNull <K> ListIterator<E> distinctBy(
        @NotNull final Function<? super E, K> keyExtractor) {
      return new ListIterator<E>(nextList().distinctBy(keyExtractor));
    }

    @Override
    public @NotNull <K> ListIterator<E> distinctBy(
        @NotNull final IndexedFunction<? super E, K> keyExtractor) {
      return new ListIterator<E>(nextList().distinctBy(keyExtractor));
    }

    @Override
    public void doFor(@NotNull final Consumer<? super E> consumer) {
      if (!atEnd()) {
        nextList().doFor(consumer);
      }
    }

    @Override
    public void doFor(@NotNull final IndexedConsumer<? super E> consumer) {
      if (!atEnd()) {
        nextList().doFor(consumer);
      }
    }

    @Override
    public void doWhile(@NotNull final IndexedPredicate<? super E> predicate) {
      if (!atEnd()) {
        nextList().doWhile(predicate);
      }
    }

    @Override
    public void doWhile(@NotNull final IndexedPredicate<? super E> condition,
        @NotNull final IndexedConsumer<? super E> consumer) {
      if (!atEnd()) {
        nextList().doWhile(condition, consumer);
      }
    }

    @Override
    public void doWhile(@NotNull final Predicate<? super E> predicate) {
      if (!atEnd()) {
        nextList().doWhile(predicate);
      }
    }

    @Override
    public void doWhile(@NotNull final Predicate<? super E> condition,
        @NotNull final Consumer<? super E> consumer) {
      if (!atEnd()) {
        nextList().doWhile(condition, consumer);
      }
    }

    @Override
    public @NotNull ListIterator<E> drop(final int maxElements) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().drop(maxElements));
    }

    @Override
    public @NotNull ListIterator<E> dropRight(final int maxElements) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().dropRight(maxElements));
    }

    @Override
    public @NotNull ListIterator<E> dropRightWhile(
        @NotNull final IndexedPredicate<? super E> predicate) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().dropRightWhile(predicate));
    }

    @Override
    public @NotNull ListIterator<E> dropRightWhile(@NotNull final Predicate<? super E> predicate) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().dropRightWhile(predicate));
    }

    @Override
    public @NotNull ListIterator<E> dropWhile(
        @NotNull final IndexedPredicate<? super E> predicate) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().dropWhile(predicate));
    }

    @Override
    public @NotNull ListIterator<E> dropWhile(@NotNull final Predicate<? super E> predicate) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().dropWhile(predicate));
    }

    @Override
    public @NotNull ListIterator<Boolean> each(
        @NotNull final IndexedPredicate<? super E> predicate) {
      if (atEnd()) {
        return FALSE_ITERATOR;
      }
      return new ListIterator<Boolean>(nextList().each(predicate));
    }

    @Override
    public @NotNull ListIterator<Boolean> each(@NotNull final Predicate<? super E> predicate) {
      if (atEnd()) {
        return FALSE_ITERATOR;
      }
      return new ListIterator<Boolean>(nextList().each(predicate));
    }

    @Override
    public @NotNull ListIterator<Boolean> endsWith(@NotNull final Iterable<?> elements) {
      return new ListIterator<Boolean>(nextList().endsWith(elements));
    }

    @Override
    public @NotNull ListIterator<Boolean> exists(
        @NotNull final IndexedPredicate<? super E> predicate) {
      if (atEnd()) {
        return FALSE_ITERATOR;
      }
      return new ListIterator<Boolean>(nextList().exists(predicate));
    }

    @Override
    public @NotNull ListIterator<Boolean> exists(@NotNull final Predicate<? super E> predicate) {
      if (atEnd()) {
        return FALSE_ITERATOR;
      }
      return new ListIterator<Boolean>(nextList().exists(predicate));
    }

    @Override
    public @NotNull ListIterator<E> filter(@NotNull final IndexedPredicate<? super E> predicate) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().filter(predicate));
    }

    @Override
    public @NotNull ListIterator<E> filter(@NotNull final Predicate<? super E> predicate) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().filter(predicate));
    }

    @Override
    public @NotNull ListIterator<E> findAny(@NotNull final IndexedPredicate<? super E> predicate) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().findAny(predicate));
    }

    @Override
    public @NotNull ListIterator<E> findAny(@NotNull final Predicate<? super E> predicate) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().findAny(predicate));
    }

    @Override
    public @NotNull ListIterator<E> findFirst(
        @NotNull final IndexedPredicate<? super E> predicate) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().findFirst(predicate));
    }

    @Override
    public @NotNull ListIterator<E> findFirst(@NotNull final Predicate<? super E> predicate) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().findFirst(predicate));
    }

    @Override
    public @NotNull ListIterator<Integer> findIndexOf(final Object element) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<Integer>(nextList().findIndexOf(element));
    }

    @Override
    public @NotNull ListIterator<Integer> findIndexOfSlice(@NotNull final Iterable<?> elements) {
      return new ListIterator<Integer>(nextList().findIndexOfSlice(elements));
    }

    @Override
    public @NotNull ListIterator<Integer> findIndexWhere(
        @NotNull final IndexedPredicate<? super E> predicate) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<Integer>(nextList().findIndexWhere(predicate));
    }

    @Override
    public @NotNull ListIterator<Integer> findIndexWhere(
        @NotNull final Predicate<? super E> predicate) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<Integer>(nextList().findIndexWhere(predicate));
    }

    @Override
    public @NotNull ListIterator<E> findLast(@NotNull final IndexedPredicate<? super E> predicate) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().findLast(predicate));
    }

    @Override
    public @NotNull ListIterator<E> findLast(@NotNull final Predicate<? super E> predicate) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().findLast(predicate));
    }

    @Override
    public @NotNull ListIterator<Integer> findLastIndexOf(final Object element) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<Integer>(nextList().findLastIndexOf(element));
    }

    @Override
    public @NotNull ListIterator<Integer> findLastIndexOfSlice(
        @NotNull final Iterable<?> elements) {
      return new ListIterator<Integer>(nextList().findLastIndexOfSlice(elements));
    }

    @Override
    public @NotNull ListIterator<Integer> findLastIndexWhere(
        @NotNull final IndexedPredicate<? super E> predicate) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<Integer>(nextList().findLastIndexWhere(predicate));
    }

    @Override
    public @NotNull ListIterator<Integer> findLastIndexWhere(
        @NotNull final Predicate<? super E> predicate) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<Integer>(nextList().findLastIndexWhere(predicate));
    }

    @Override
    public E first() {
      return list.get(pos);
    }

    @Override
    public @NotNull <F> ListIterator<F> flatMap(
        @NotNull final Function<? super E, ? extends Iterable<F>> mapper) {
      return new ListIterator<F>(nextList().flatMap(mapper));
    }

    @Override
    public @NotNull <F> ListIterator<F> flatMap(
        @NotNull final IndexedFunction<? super E, ? extends Iterable<F>> mapper) {
      return new ListIterator<F>(nextList().flatMap(mapper));
    }

    @Override
    public @NotNull ListIterator<E> flatMapAfter(final int numElements,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().flatMapAfter(numElements, mapper));
    }

    @Override
    public @NotNull ListIterator<E> flatMapAfter(final int numElements,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().flatMapAfter(numElements, mapper));
    }

    @Override
    public @NotNull ListIterator<E> flatMapFirstWhere(
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().flatMapFirstWhere(predicate, mapper));
    }

    @Override
    public @NotNull ListIterator<E> flatMapFirstWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().flatMapFirstWhere(predicate, mapper));
    }

    @Override
    public @NotNull ListIterator<E> flatMapLastWhere(
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().flatMapLastWhere(predicate, mapper));
    }

    @Override
    public @NotNull ListIterator<E> flatMapLastWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().flatMapLastWhere(predicate, mapper));
    }

    @Override
    public @NotNull ListIterator<E> flatMapWhere(
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().flatMapWhere(predicate, mapper));
    }

    @Override
    public @NotNull ListIterator<E> flatMapWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().flatMapWhere(predicate, mapper));
    }

    @Override
    public @NotNull <F> ListIterator<F> fold(final F identity,
        @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
      if (atEnd()) {
        return new ListIterator<F>(List.of(identity));
      }
      return new ListIterator<F>(nextList().fold(identity, operation));
    }

    @Override
    public @NotNull <F> ListIterator<F> foldLeft(final F identity,
        @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
      if (atEnd()) {
        return new ListIterator<F>(List.of(identity));
      }
      return new ListIterator<F>(nextList().foldLeft(identity, operation));
    }

    @Override
    public @NotNull <F> ListIterator<F> foldLeftWhile(final F identity,
        @NotNull final Predicate<? super F> predicate,
        @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
      if (atEnd()) {
        return new ListIterator<F>(List.of(identity));
      }
      return new ListIterator<F>(nextList().foldLeftWhile(identity, predicate, operation));
    }

    @Override
    public @NotNull <F> ListIterator<F> foldRight(final F identity,
        @NotNull final BinaryFunction<? super E, ? super F, ? extends F> operation) {
      if (atEnd()) {
        return new ListIterator<F>(List.of(identity));
      }
      return new ListIterator<F>(nextList().foldRight(identity, operation));
    }

    @Override
    public @NotNull <F> ListIterator<F> foldRightWhile(final F identity,
        @NotNull final Predicate<? super F> predicate,
        @NotNull final BinaryFunction<? super E, ? super F, ? extends F> operation) {
      if (atEnd()) {
        return new ListIterator<F>(List.of(identity));
      }
      return new ListIterator<F>(nextList().foldRightWhile(identity, predicate, operation));
    }

    @Override
    public boolean hasNext() {
      return list.materializer.canMaterializeElement(pos);
    }

    @Override
    public boolean hasPrevious() {
      return pos > 0;
    }

    @Override
    public @NotNull ListIterator<Boolean> includes(final Object element) {
      if (atEnd()) {
        return FALSE_ITERATOR;
      }
      return new ListIterator<Boolean>(nextList().includes(element));
    }

    @Override
    public @NotNull ListIterator<Boolean> includesAll(@NotNull final Iterable<?> elements) {
      return new ListIterator<Boolean>(nextList().includesAll(elements));
    }

    @Override
    public @NotNull ListIterator<Boolean> includesSlice(@NotNull final Iterable<?> elements) {
      return new ListIterator<Boolean>(nextList().includesSlice(elements));
    }

    @Override
    public @NotNull ListIterator<E> insert(final E element) {
      return new ListIterator<E>(nextList().insertAfter(0, element));
    }

    @Override
    public @NotNull ListIterator<E> insertAfter(final int numElements, final E element) {
      if (numElements != 0 && atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().insertAfter(numElements, element));
    }

    @Override
    public @NotNull ListIterator<E> insertAll(@NotNull final Iterable<? extends E> elements) {
      return new ListIterator<E>(nextList().insertAllAfter(0, elements));
    }

    @Override
    public @NotNull ListIterator<E> insertAllAfter(final int numElements,
        @NotNull final Iterable<? extends E> elements) {
      if (numElements != 0 && atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().insertAllAfter(numElements, elements));
    }

    @Override
    public @NotNull ListIterator<E> intersect(@NotNull final Iterable<?> elements) {
      return new ListIterator<E>(nextList().intersect(elements));
    }

    @Override
    public @NotNull Iterator<E> iterator() {
      if (atEnd()) {
        return Iterator.of();
      }
      return nextList().iterator();
    }

    @Override
    public boolean isEmpty() {
      return list.isEmpty() || pos >= list.size();
    }

    @Override
    public E last() {
      return list.last();
    }

    @Override
    public @NotNull <F> ListIterator<F> map(@NotNull final Function<? super E, F> mapper) {
      return new ListIterator<F>(nextList().map(mapper));
    }

    @Override
    public @NotNull <F> ListIterator<F> map(@NotNull final IndexedFunction<? super E, F> mapper) {
      return new ListIterator<F>(nextList().map(mapper));
    }

    @Override
    public @NotNull ListIterator<E> mapAfter(final int numElements,
        @NotNull final Function<? super E, ? extends E> mapper) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().mapAfter(numElements, mapper));
    }

    @Override
    public @NotNull ListIterator<E> mapAfter(final int numElements,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().mapAfter(numElements, mapper));
    }

    @Override
    public @NotNull ListIterator<E> mapFirstWhere(
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().mapFirstWhere(predicate, mapper));
    }

    @Override
    public @NotNull ListIterator<E> mapFirstWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends E> mapper) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().mapFirstWhere(predicate, mapper));
    }

    @Override
    public @NotNull ListIterator<E> mapLastWhere(
        @NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().mapLastWhere(predicate, mapper));
    }

    @Override
    public @NotNull ListIterator<E> mapLastWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends E> mapper) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().mapLastWhere(predicate, mapper));
    }

    @Override
    public @NotNull ListIterator<E> mapWhere(@NotNull final IndexedPredicate<? super E> predicate,
        @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
      return new ListIterator<E>(nextList().mapWhere(predicate, mapper));
    }

    @Override
    public @NotNull ListIterator<E> mapWhere(@NotNull final Predicate<? super E> predicate,
        @NotNull final Function<? super E, ? extends E> mapper) {
      return new ListIterator<E>(nextList().mapWhere(predicate, mapper));
    }

    @Override
    public @NotNull ListIterator<E> max(@NotNull final Comparator<? super E> comparator) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().max(comparator));
    }

    @Override
    public @NotNull ListIterator<E> min(@NotNull final Comparator<? super E> comparator) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().min(comparator));
    }

    @Override
    public @NotNull ListIterator<E> moveBy(final int maxElements) {
      if (maxElements != 0) {
        final int knownSize = list.knownSize();
        if (knownSize >= 0) {
          pos = (int) Math.min(knownSize, Math.max(0, (long) pos + maxElements));
        } else {
          pos = (int) Math.min(Integer.MAX_VALUE, Math.max(0, (long) pos + maxElements));
        }
      }
      return this;
    }

    @Override
    public @NotNull ListIterator<E> moveTo(final int index) {
      Require.notNegative(index, "index");
      final int knownSize = list.knownSize();
      if (knownSize >= 0) {
        pos = Math.min(knownSize, Math.max(0, index));
      } else {
        pos = Math.max(0, index);
      }
      return this;
    }

    @Override
    public E next() {
      try {
        return list.get(pos++);
      } catch (final IndexOutOfBoundsException ignored) {
        // FIXME: where the exception come from?
        throw new NoSuchElementException();
      }
    }

    @Override
    public int nextIndex() {
      final List<E> list = this.list;
      final int knownSize = list.knownSize();
      if (knownSize >= 0) {
        return Math.min(knownSize, pos);
      }
      return Math.min(list.size(), pos);
    }

    @Override
    public @NotNull List<E> nextList() {
      return list.drop(pos);
    }

    @Override
    public @NotNull ListIterator<Boolean> none(
        @NotNull final IndexedPredicate<? super E> predicate) {
      if (atEnd()) {
        return TRUE_ITERATOR;
      }
      return new ListIterator<Boolean>(nextList().none(predicate));
    }

    @Override
    public @NotNull ListIterator<Boolean> none(@NotNull final Predicate<? super E> predicate) {
      if (atEnd()) {
        return TRUE_ITERATOR;
      }
      return new ListIterator<Boolean>(nextList().none(predicate));
    }

    @Override
    public @NotNull ListIterator<Boolean> notAll(
        @NotNull final IndexedPredicate<? super E> predicate) {
      if (atEnd()) {
        return TRUE_ITERATOR;
      }
      return new ListIterator<Boolean>(nextList().notAll(predicate));
    }

    @Override
    public @NotNull ListIterator<Boolean> notAll(@NotNull final Predicate<? super E> predicate) {
      if (atEnd()) {
        return TRUE_ITERATOR;
      }
      return new ListIterator<Boolean>(nextList().notAll(predicate));
    }

    @Override
    public boolean notEmpty() {
      return pos < list.size();
    }

    @Override
    public @NotNull ListIterator<E> orElse(@NotNull final Iterable<? extends E> elements) {
      if (atEnd()) {
        return new ListIterator<E>(List.wrap(elements));
      }
      return new ListIterator<E>(nextList().orElse(elements));
    }

    @Override
    public @NotNull ListIterator<E> orElseGet(
        @NotNull final Supplier<? extends Iterable<? extends E>> supplier) {
      return new ListIterator<E>(nextList().orElseGet(supplier));
    }

    @Override
    public @NotNull ListIterator<E> plus(final E element) {
      return new ListIterator<E>(nextList().plus(element));
    }

    @Override
    public @NotNull ListIterator<E> plusAll(@NotNull final Iterable<? extends E> elements) {
      return new ListIterator<E>(nextList().plusAll(elements));
    }

    @Override
    public E previous() {
      try {
        int pos = this.pos;
        if (pos > 0) {
          final List<E> list = this.list;
          if (!list.materializer.canMaterializeElement(pos - 1)) {
            pos = Math.min(list.size(), pos);
          }
          return list.get(this.pos = pos - 1);
        }
      } catch (final IndexOutOfBoundsException ignored) {
        // FIXME: where the exception come from?
        throw new NoSuchElementException();
      }
      throw new NoSuchElementException();
    }

    @Override
    public int previousIndex() {
      return nextIndex() - 1;
    }

    @Override
    public @NotNull List<E> previousList() {
      return list.take(pos);
    }

    @Override
    public @NotNull ListIterator<E> reduce(
        @NotNull final BinaryFunction<? super E, ? super E, ? extends E> operation) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().reduce(operation));
    }

    @Override
    public @NotNull ListIterator<E> reduceLeft(
        @NotNull final BinaryFunction<? super E, ? super E, ? extends E> operation) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().reduceLeft(operation));
    }

    @Override
    public @NotNull ListIterator<E> reduceRight(
        @NotNull final BinaryFunction<? super E, ? super E, ? extends E> operation) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().reduceRight(operation));
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull ListIterator<E> removeAfter(final int numElements) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().removeAfter(numElements));
    }

    @Override
    public @NotNull ListIterator<E> removeEach(final E element) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().removeEach(element));
    }

    @Override
    public @NotNull ListIterator<E> removeFirst(final E element) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().removeFirst(element));
    }

    @Override
    public @NotNull ListIterator<E> removeFirstWhere(
        @NotNull final IndexedPredicate<? super E> predicate) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().removeFirstWhere(predicate));
    }

    @Override
    public @NotNull ListIterator<E> removeFirstWhere(
        @NotNull final Predicate<? super E> predicate) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().removeFirstWhere(predicate));
    }

    @Override
    public @NotNull ListIterator<E> removeLast(final E element) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().removeLast(element));
    }

    @Override
    public @NotNull ListIterator<E> removeLastWhere(
        @NotNull final IndexedPredicate<? super E> predicate) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().removeLastWhere(predicate));
    }

    @Override
    public @NotNull ListIterator<E> removeLastWhere(@NotNull final Predicate<? super E> predicate) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().removeLastWhere(predicate));
    }

    @Override
    public @NotNull ListIterator<E> removeSlice(final int start, final int end) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().removeSlice(start, end));
    }

    @Override
    public @NotNull ListIterator<E> removeWhere(
        @NotNull final IndexedPredicate<? super E> predicate) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().removeWhere(predicate));
    }

    @Override
    public @NotNull ListIterator<E> removeWhere(@NotNull final Predicate<? super E> predicate) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().removeWhere(predicate));
    }

    @Override
    public @NotNull ListIterator<E> replaceAfter(final int numElements, final E replacement) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().replaceAfter(numElements, replacement));
    }

    @Override
    public @NotNull ListIterator<E> replaceEach(final E element, final E replacement) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().replaceEach(element, replacement));
    }

    @Override
    public @NotNull ListIterator<E> replaceFirst(final E element, final E replacement) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().replaceFirst(element, replacement));
    }

    @Override
    public @NotNull ListIterator<E> replaceFirstWhere(
        @NotNull final IndexedPredicate<? super E> predicate, final E replacement) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().replaceFirstWhere(predicate, replacement));
    }

    @Override
    public @NotNull ListIterator<E> replaceFirstWhere(@NotNull final Predicate<? super E> predicate,
        final E replacement) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().replaceFirstWhere(predicate, replacement));
    }

    @Override
    public @NotNull ListIterator<E> replaceLast(final E element, final E replacement) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().replaceLast(element, replacement));
    }

    @Override
    public @NotNull ListIterator<E> replaceLastWhere(
        @NotNull final IndexedPredicate<? super E> predicate, final E replacement) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().replaceLastWhere(predicate, replacement));
    }

    @Override
    public @NotNull ListIterator<E> replaceLastWhere(@NotNull final Predicate<? super E> predicate,
        final E replacement) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().replaceLastWhere(predicate, replacement));
    }

    @Override
    public @NotNull ListIterator<E> replaceSlice(final int start, final int end,
        @NotNull final Iterable<? extends E> patch) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().replaceSlice(start, end, patch));
    }

    @Override
    public @NotNull ListIterator<E> replaceWhere(
        @NotNull final IndexedPredicate<? super E> predicate, final E replacement) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().replaceWhere(predicate, replacement));
    }

    @Override
    public @NotNull ListIterator<E> replaceWhere(@NotNull final Predicate<? super E> predicate,
        final E replacement) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().replaceWhere(predicate, replacement));
    }

    @Override
    public @NotNull ListIterator<E> resizeTo(@NotNegative final int numElements, final E padding) {
      Require.notNegative(numElements, "numElements");
      if (atEnd()) {
        return new ListIterator<E>(List.times(numElements, padding));
      }
      return new ListIterator<E>(nextList().resizeTo(numElements, padding));
    }

    @Override
    public int skip(final int maxElements) {
      if (maxElements > 0) {
        final int currPos = pos;
        final int newPos = (int) Math.min(list.size(), (long) currPos + maxElements);
        return (pos = newPos) - currPos;
      }
      return 0;
    }

    @Override
    public @NotNull ListIterator<E> reverse() {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().reverse());
    }

    @Override
    public void set(final E e) {
      throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
      return Math.min(0, list.size() - pos);
    }

    @Override
    public @NotNull ListIterator<E> slice(final int start) {
      return slice(start, Integer.MAX_VALUE);
    }

    @Override
    public @NotNull ListIterator<E> slice(final int start, final int end) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().slice(start, end));
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull ListIterator<? extends ListIterator<E>> slidingWindow(
        @Positive final int maxSize, @Positive final int step) {
      return new ListIterator<ListIterator<E>>(nextList().slidingWindow(maxSize, step)
          .map((Function<List<E>, ListIterator<E>>) LIST_TO_ITERATOR));
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull ListIterator<? extends ListIterator<E>> slidingWindowWithPadding(
        @Positive final int size, @Positive final int step, final E padding) {
      return new ListIterator<ListIterator<E>>(
          nextList().slidingWindowWithPadding(size, step, padding)
              .map((Function<List<E>, ListIterator<E>>) LIST_TO_ITERATOR));
    }

    @Override
    public @NotNull ListIterator<Boolean> startsWith(@NotNull final Iterable<?> elements) {
      return new ListIterator<Boolean>(nextList().startsWith(elements));
    }

    @Override
    public @NotNull ListIterator<E> symmetricDiff(@NotNull final Iterable<? extends E> elements) {
      return new ListIterator<E>(nextList().symmetricDiff(elements));
    }

    @Override
    public @NotNull ListIterator<E> take(final int maxElements) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().take(maxElements));
    }

    @Override
    public @NotNull ListIterator<E> takeRight(final int maxElements) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().takeRight(maxElements));
    }

    @Override
    public @NotNull ListIterator<E> takeRightWhile(
        @NotNull final IndexedPredicate<? super E> predicate) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().takeRightWhile(predicate));
    }

    @Override
    public @NotNull ListIterator<E> takeRightWhile(@NotNull final Predicate<? super E> predicate) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().takeRightWhile(predicate));
    }

    @Override
    public @NotNull ListIterator<E> takeWhile(
        @NotNull final IndexedPredicate<? super E> predicate) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().takeWhile(predicate));
    }

    @Override
    public @NotNull ListIterator<E> takeWhile(@NotNull final Predicate<? super E> predicate) {
      if (atEnd()) {
        return ListIterator.of();
      }
      return new ListIterator<E>(nextList().takeWhile(predicate));
    }

    // TODO: extra
    public @NotNull List<E> toList() {
      return nextList();
    }

    @Override
    public @NotNull ListIterator<E> union(@NotNull final Iterable<? extends E> elements) {
      return new ListIterator<E>(nextList().union(elements));
    }

    protected boolean atEnd() {
      final int knownSize = list.knownSize();
      return knownSize >= 0 && pos >= knownSize;
    }
  }
}
