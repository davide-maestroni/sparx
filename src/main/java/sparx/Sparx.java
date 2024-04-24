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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.NoSuchElementException;
import org.jetbrains.annotations.NotNull;
import sparx.collection.AbstractListSequence;
import sparx.collection.IteratorSequence;
import sparx.collection.Sequence;
import sparx.collection.internal.iterator.AllIteratorMaterializer;
import sparx.collection.internal.iterator.AppendAllIteratorMaterializer;
import sparx.collection.internal.iterator.AppendIteratorMaterializer;
import sparx.collection.internal.iterator.ArrayToIteratorMaterializer;
import sparx.collection.internal.iterator.CollectionToIteratorMaterializer;
import sparx.collection.internal.iterator.CountIteratorMaterializer;
import sparx.collection.internal.iterator.CountWhereIteratorMaterializer;
import sparx.collection.internal.iterator.DropIteratorMaterializer;
import sparx.collection.internal.iterator.DropRightIteratorMaterializer;
import sparx.collection.internal.iterator.DropRightWhileIteratorMaterializer;
import sparx.collection.internal.iterator.DropWhileIteratorMaterializer;
import sparx.collection.internal.iterator.ElementToIteratorMaterializer;
import sparx.collection.internal.iterator.EmptyIteratorMaterializer;
import sparx.collection.internal.iterator.EndsWithIteratorMaterializer;
import sparx.collection.internal.iterator.ExistsIteratorMaterializer;
import sparx.collection.internal.iterator.FinallyIteratorMaterializer;
import sparx.collection.internal.iterator.FindFirstIteratorMaterializer;
import sparx.collection.internal.iterator.FindIndexIteratorMaterializer;
import sparx.collection.internal.iterator.FindIndexOfSliceIteratorMaterializer;
import sparx.collection.internal.iterator.FindLastIndexIteratorMaterializer;
import sparx.collection.internal.iterator.FindLastIndexOfSliceIteratorMaterializer;
import sparx.collection.internal.iterator.FindLastIteratorMaterializer;
import sparx.collection.internal.iterator.FlatMapAfterIteratorMaterializer;
import sparx.collection.internal.iterator.FlatMapExceptionallyIteratorMaterializer;
import sparx.collection.internal.iterator.FlatMapFirstWhereIteratorMaterializer;
import sparx.collection.internal.iterator.FlatMapIteratorMaterializer;
import sparx.collection.internal.iterator.FlatMapLastWhereIteratorMaterializer;
import sparx.collection.internal.iterator.FlatMapWhereIteratorMaterializer;
import sparx.collection.internal.iterator.FoldLeftIteratorMaterializer;
import sparx.collection.internal.iterator.FoldRightIteratorMaterializer;
import sparx.collection.internal.iterator.GroupIteratorMaterializer;
import sparx.collection.internal.iterator.IncludesAllIteratorMaterializer;
import sparx.collection.internal.iterator.IncludesSliceIteratorMaterializer;
import sparx.collection.internal.iterator.InsertAfterIteratorMaterializer;
import sparx.collection.internal.iterator.InsertAllAfterIteratorMaterializer;
import sparx.collection.internal.iterator.InsertAllIteratorMaterializer;
import sparx.collection.internal.iterator.InsertIteratorMaterializer;
import sparx.collection.internal.iterator.IteratorMaterializer;
import sparx.collection.internal.iterator.IteratorToIteratorMaterializer;
import sparx.collection.internal.iterator.ListMaterializerToIteratorMaterializer;
import sparx.collection.internal.iterator.ListToIteratorMaterializer;
import sparx.collection.internal.iterator.RemoveWhereIteratorMaterializer;
import sparx.collection.internal.iterator.RepeatIteratorMaterializer;
import sparx.collection.internal.list.AllListMaterializer;
import sparx.collection.internal.list.AppendAllListMaterializer;
import sparx.collection.internal.list.AppendListMaterializer;
import sparx.collection.internal.list.ArrayToListMaterializer;
import sparx.collection.internal.list.CharSequenceToListMaterializer;
import sparx.collection.internal.list.CollectionToListMaterializer;
import sparx.collection.internal.list.CountListMaterializer;
import sparx.collection.internal.list.CountWhereListMaterializer;
import sparx.collection.internal.list.DoubleArrayToListMaterializer;
import sparx.collection.internal.list.DropListMaterializer;
import sparx.collection.internal.list.DropRightListMaterializer;
import sparx.collection.internal.list.DropRightWhileListMaterializer;
import sparx.collection.internal.list.DropWhileListMaterializer;
import sparx.collection.internal.list.ElementToListMaterializer;
import sparx.collection.internal.list.EmptyListMaterializer;
import sparx.collection.internal.list.EndsWithListMaterializer;
import sparx.collection.internal.list.ExistsListMaterializer;
import sparx.collection.internal.list.FindFirstListMaterializer;
import sparx.collection.internal.list.FindIndexListMaterializer;
import sparx.collection.internal.list.FindIndexOfSliceListMaterializer;
import sparx.collection.internal.list.FindLastIndexListMaterializer;
import sparx.collection.internal.list.FindLastIndexOfSliceListMaterializer;
import sparx.collection.internal.list.FindLastListMaterializer;
import sparx.collection.internal.list.FlatMapAfterListMaterializer;
import sparx.collection.internal.list.FlatMapFirstWhereListMaterializer;
import sparx.collection.internal.list.FlatMapLastWhereListMaterializer;
import sparx.collection.internal.list.FlatMapListMaterializer;
import sparx.collection.internal.list.FlatMapWhereListMaterializer;
import sparx.collection.internal.list.FloatArrayToListMaterializer;
import sparx.collection.internal.list.FoldLeftListMaterializer;
import sparx.collection.internal.list.FoldRightListMaterializer;
import sparx.collection.internal.list.GroupListMaterializer;
import sparx.collection.internal.list.IncludesAllListMaterializer;
import sparx.collection.internal.list.IncludesSliceListMaterializer;
import sparx.collection.internal.list.InsertAfterListMaterializer;
import sparx.collection.internal.list.InsertAllAfterListMaterializer;
import sparx.collection.internal.list.IntArrayToListMaterializer;
import sparx.collection.internal.list.IteratorToListMaterializer;
import sparx.collection.internal.list.ListMaterializer;
import sparx.collection.internal.list.ListToListMaterializer;
import sparx.collection.internal.list.LongArrayToListMaterializer;
import sparx.collection.internal.list.MapAfterListMaterializer;
import sparx.collection.internal.list.MapFirstWhereListMaterializer;
import sparx.collection.internal.list.MapLastWhereListMaterializer;
import sparx.collection.internal.list.MapListMaterializer;
import sparx.collection.internal.list.MapWhereListMaterializer;
import sparx.collection.internal.list.MaxListMaterializer;
import sparx.collection.internal.list.OrElseListMaterializer;
import sparx.collection.internal.list.PrependAllListMaterializer;
import sparx.collection.internal.list.PrependListMaterializer;
import sparx.collection.internal.list.ReduceLeftListMaterializer;
import sparx.collection.internal.list.ReduceRightListMaterializer;
import sparx.collection.internal.list.RemoveAfterListMaterializer;
import sparx.collection.internal.list.RemoveFirstWhereListMaterializer;
import sparx.collection.internal.list.RemoveLastWhereListMaterializer;
import sparx.collection.internal.list.RemoveSliceListMaterializer;
import sparx.collection.internal.list.RemoveWhereListMaterializer;
import sparx.collection.internal.list.RepeatListMaterializer;
import sparx.collection.internal.list.ReplaceSliceListMaterializer;
import sparx.collection.internal.list.ResizeListMaterializer;
import sparx.collection.internal.list.ReverseListMaterializer;
import sparx.collection.internal.list.SingleFlatMapListMaterializer;
import sparx.collection.internal.list.SingleFlatMapWhereListMaterializer;
import sparx.collection.internal.list.SingleMapListMaterializer;
import sparx.collection.internal.list.SingleMapWhereListMaterializer;
import sparx.collection.internal.list.SliceListMaterializer;
import sparx.collection.internal.list.SortedListMaterializer;
import sparx.collection.internal.list.StartsWithListMaterializer;
import sparx.collection.internal.list.TakeListMaterializer;
import sparx.collection.internal.list.TakeRightListMaterializer;
import sparx.collection.internal.list.TakeRightWhileListMaterializer;
import sparx.collection.internal.list.TakeWhileListMaterializer;
import sparx.util.IndexOverflowException;
import sparx.util.Require;
import sparx.util.SizeOverflowException;
import sparx.util.UncheckedException;
import sparx.util.function.Action;
import sparx.util.function.BinaryFunction;
import sparx.util.function.Consumer;
import sparx.util.function.Function;
import sparx.util.function.IndexedConsumer;
import sparx.util.function.IndexedFunction;
import sparx.util.function.IndexedPredicate;
import sparx.util.function.Predicate;
import sparx.util.function.Supplier;

// TODO: Stream <= Iterator && <= ListIterator
// TODO: equals, clone, Serializable

public class Sparx {

  private static final IndexedPredicate<?> EQUALS_NULL = new IndexedPredicate<Object>() {
    @Override
    public boolean test(final int index, final Object param) {
      return param == null;
    }
  };
  private static final IndexedPredicate<?> NOT_EQUALS_NULL = new IndexedPredicate<Object>() {
    @Override
    public boolean test(final int index, final Object param) {
      return param != null;
    }
  };

  private Sparx() {
  }

  private static @NotNull <E> IndexedPredicate<E> elementsContains(
      @NotNull final ListMaterializer<E> elementsMaterializer) {
    return new IndexedPredicate<E>() {
      @Override
      public boolean test(final int index, final E param) {
        return elementsMaterializer.materializeContains(param);
      }
    };
  }

  private static @NotNull <E> IndexedPredicate<E> elementsNotContains(
      @NotNull final ListMaterializer<E> elementsMaterializer) {
    return new IndexedPredicate<E>() {
      @Override
      public boolean test(final int index, final E param) {
        return !elementsMaterializer.materializeContains(param);
      }
    };
  }

  @SuppressWarnings("unchecked")
  private static @NotNull <E> IndexedPredicate<E> equalsElement(final Object element) {
    if (element == null) {
      return (IndexedPredicate<E>) EQUALS_NULL;
    }
    return new IndexedPredicate<E>() {
      @Override
      public boolean test(final int index, final E param) {
        return element.equals(param);
      }
    };
  }

  private static @NotNull <P> IndexedPredicate<P> negated(
      @NotNull final IndexedPredicate<P> predicate) {
    Require.notNull(predicate, "predicate");
    return new IndexedPredicate<P>() {
      @Override
      public boolean test(final int index, final P param) throws Exception {
        return !predicate.test(index, param);
      }
    };
  }

  @SuppressWarnings("unchecked")
  private static @NotNull <E> IndexedPredicate<E> notEqualsElement(final Object element) {
    if (element == null) {
      return (IndexedPredicate<E>) NOT_EQUALS_NULL;
    }
    return new IndexedPredicate<E>() {
      @Override
      public boolean test(final int index, final E param) {
        return !element.equals(param);
      }
    };
  }

  private static @NotNull <E> IndexedFunction<E, E> replacementMapper(final E element) {
    return new IndexedFunction<E, E>() {
      @Override
      public E apply(final int index, final E ignored) {
        return element;
      }
    };
  }

  private static @NotNull <T> Comparator<T> reversed(@NotNull final Comparator<T> comparator) {
    Require.notNull(comparator, "comparator");
    return new Comparator<T>() {
      @Override
      public int compare(final T o1, final T o2) {
        return comparator.compare(o2, o1);
      }
    };
  }

  private static @NotNull <E, F> IndexedFunction<E, F> toIndexedFunction(
      @NotNull final Function<E, F> function) {
    return new IndexedFunction<E, F>() {
      @Override
      public F apply(final int index, final E parma) throws Exception {
        return function.apply(parma);
      }
    };
  }

  private static @NotNull <E> IndexedPredicate<E> toIndexedPredicate(
      @NotNull final Predicate<E> predicate) {
    Require.notNull(predicate, "predicate");
    return new IndexedPredicate<E>() {
      @Override
      public boolean test(final int index, final E param) throws Exception {
        return predicate.test(param);
      }
    };
  }

  private static @NotNull <E> IndexedPredicate<E> toNegatedIndexedPredicate(
      @NotNull final Predicate<E> predicate) {
    Require.notNull(predicate, "predicate");
    return new IndexedPredicate<E>() {
      @Override
      public boolean test(final int index, final E param) throws Exception {
        return !predicate.test(param);
      }
    };
  }

  public static class lazy {

    private lazy() {
    }

    public static class Iterator<E> implements IteratorSequence<E> {

      private static final Iterator<?> EMPTY_ITERATOR = new Iterator<Object>(
          EmptyIteratorMaterializer.instance());
      private static final Function<? extends java.util.List<?>, ? extends Iterator<?>> FROM_JAVA_LIST = new Function<java.util.List<Object>, Iterator<Object>>() {
        @Override
        public Iterator<Object> apply(final java.util.List<Object> param) {
          return new Iterator<Object>(new ListToIteratorMaterializer<Object>(param));
        }
      };

      private final IteratorMaterializer<E> materializer;

      private Iterator(@NotNull final IteratorMaterializer<E> materializer) {
        this.materializer = Require.notNull(materializer, "materializer");
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
            new ArrayToIteratorMaterializer<E>(first, second, third, fourth, fifth, sixth,
                seventh));
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

      public static @NotNull <E> Iterator<E> times(final int count, final E element) {
        if (Require.notNegative(count, "count") == 0) {
          return Iterator.of();
        }
        return new Iterator<E>(new RepeatIteratorMaterializer<E>(count, element));
      }

      public static @NotNull <E> Iterator<E> wrap(@NotNull final Iterable<? extends E> elements) {
        return new Iterator<E>(getElementsMaterializer(elements));
      }

      @SuppressWarnings("unchecked")
      private static @NotNull <E> IteratorMaterializer<E> getElementsMaterializer(
          @NotNull final Iterable<? extends E> elements) {
        if (elements instanceof Iterator) {
          return ((Iterator<E>) elements).materializer;
        }
        if (elements instanceof List) {
          return new ListMaterializerToIteratorMaterializer<E>(((List<E>) elements).materializer);
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
        return new IteratorToIteratorMaterializer<E>((Iterator<E>) elements.iterator());
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
        Require.notNull(exceptionType, "exceptionType");
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
            if (exception instanceof Error) {
              throw (Error) exception;
            }
            throw UncheckedException.throwUnchecked(exception);
          }
        };
      }

      private static @NotNull <E, T extends Throwable> IndexedFunction<Throwable, IteratorMaterializer<E>> getExceptionToMaterializer(
          @NotNull final Class<T> exceptionType,
          @NotNull final IndexedFunction<? super T, ? extends Iterable<? extends E>> mapper) {
        Require.notNull(exceptionType, "exceptionType");
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
      public @NotNull Iterator<Boolean> all(@NotNull final IndexedPredicate<? super E> predicate) {
        final IteratorMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return Iterator.of(true);
        }
        return new Iterator<Boolean>(new AllIteratorMaterializer<E>(materializer, predicate, true));
      }

      @Override
      public @NotNull Iterator<Boolean> all(@NotNull final Predicate<? super E> predicate) {
        final IteratorMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return Iterator.of(true);
        }
        return new Iterator<Boolean>(
            new AllIteratorMaterializer<E>(materializer, toIndexedPredicate(predicate), true));
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
        if (materializer.knownSize() == 0) {
          return new Iterator<E>(getElementsMaterializer(elements));
        }
        return new Iterator<E>(
            new AppendAllIteratorMaterializer<E>(materializer, getElementsMaterializer(elements)));
      }

      @Override
      public <T> T apply(@NotNull final Function<? super Sequence<E>, T> mapper) {
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
      public @NotNull Iterator<Integer> count(
          @NotNull final IndexedPredicate<? super E> predicate) {
        final IteratorMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return Iterator.of(0);
        }
        return new Iterator<Integer>(
            new CountWhereIteratorMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull Iterator<Integer> count(@NotNull final Predicate<? super E> predicate) {
        final IteratorMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return Iterator.of(0);
        }
        return new Iterator<Integer>(
            new CountWhereIteratorMaterializer<E>(materializer, toIndexedPredicate(predicate)));
      }

      @Override
      public @NotNull Iterator<E> diff(@NotNull final Iterable<?> elements) {
        final IteratorMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        final ListMaterializer<Object> elementsMaterializer = List.getElementsMaterializer(
            elements);
        if (elementsMaterializer.knownSize() == 0) {
          return this;
        }
        return new Iterator<E>(new RemoveWhereIteratorMaterializer<E>(materializer,
            elementsContains(elementsMaterializer)));
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
        final IteratorMaterializer<E> materializer = this.materializer;
        if (maxElements <= 0 || materializer.knownSize() == 0) {
          return this;
        }
        return new Iterator<E>(new DropIteratorMaterializer<E>(materializer, maxElements));
      }

      @Override
      public @NotNull Iterator<E> dropRight(final int maxElements) {
        final IteratorMaterializer<E> materializer = this.materializer;
        if (maxElements <= 0 || materializer.knownSize() == 0) {
          return this;
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
        return new Iterator<E>(new DropRightWhileIteratorMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull Iterator<E> dropRightWhile(@NotNull final Predicate<? super E> predicate) {
        final IteratorMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new Iterator<E>(
            new DropRightWhileIteratorMaterializer<E>(materializer, toIndexedPredicate(predicate)));
      }

      @Override
      public @NotNull Iterator<E> dropWhile(@NotNull final IndexedPredicate<? super E> predicate) {
        final IteratorMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new Iterator<E>(new DropWhileIteratorMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull Iterator<E> dropWhile(@NotNull final Predicate<? super E> predicate) {
        final IteratorMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new Iterator<E>(
            new DropWhileIteratorMaterializer<E>(materializer, toIndexedPredicate(predicate)));
      }

      @Override
      public @NotNull Iterator<Boolean> endsWith(@NotNull final Iterable<?> elements) {
        return new Iterator<Boolean>(new EndsWithIteratorMaterializer<E>(materializer,
            List.getElementsMaterializer(elements)));
      }

      @Override
      public @NotNull Iterator<Boolean> exists(
          @NotNull final IndexedPredicate<? super E> predicate) {
        final IteratorMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return Iterator.of(false);
        }
        return new Iterator<Boolean>(
            new ExistsIteratorMaterializer<E>(materializer, predicate, false));
      }

      @Override
      public @NotNull Iterator<Boolean> exists(@NotNull final Predicate<? super E> predicate) {
        final IteratorMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return Iterator.of(false);
        }
        return new Iterator<Boolean>(
            new ExistsIteratorMaterializer<E>(materializer, toIndexedPredicate(predicate), false));
      }

      @Override
      public @NotNull Iterator<E> filter(@NotNull final IndexedPredicate<? super E> predicate) {
        final IteratorMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new Iterator<E>(
            new RemoveWhereIteratorMaterializer<E>(materializer, negated(predicate)));
      }

      @Override
      public @NotNull Iterator<E> filter(@NotNull final Predicate<? super E> predicate) {
        final IteratorMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new Iterator<E>(new RemoveWhereIteratorMaterializer<E>(materializer,
            toNegatedIndexedPredicate(predicate)));
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
        return new Iterator<E>(new FindFirstIteratorMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull Iterator<E> findFirst(@NotNull final Predicate<? super E> predicate) {
        final IteratorMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new Iterator<E>(
            new FindFirstIteratorMaterializer<E>(materializer, toIndexedPredicate(predicate)));
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
      public @NotNull Iterator<Integer> findIndexWhere(
          @NotNull final IndexedPredicate<? super E> predicate) {
        final IteratorMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return Iterator.of();
        }
        return new Iterator<Integer>(new FindIndexIteratorMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull Iterator<Integer> findIndexWhere(
          @NotNull final Predicate<? super E> predicate) {
        final IteratorMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return Iterator.of();
        }
        return new Iterator<Integer>(
            new FindIndexIteratorMaterializer<E>(materializer, toIndexedPredicate(predicate)));
      }

      @Override
      public @NotNull Iterator<Integer> findIndexOfSlice(@NotNull final Iterable<?> elements) {
        final ListMaterializer<Object> elementsMaterializer = List.getElementsMaterializer(
            elements);
        return new Iterator<Integer>(
            new FindIndexOfSliceIteratorMaterializer<E>(materializer, elementsMaterializer));
      }

      @Override
      public @NotNull Iterator<E> findLast(@NotNull final IndexedPredicate<? super E> predicate) {
        final IteratorMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new Iterator<E>(new FindLastIteratorMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull Iterator<E> findLast(@NotNull final Predicate<? super E> predicate) {
        final IteratorMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new Iterator<E>(
            new FindLastIteratorMaterializer<E>(materializer, toIndexedPredicate(predicate)));
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
      public @NotNull Iterator<Integer> findLastIndexWhere(
          @NotNull final IndexedPredicate<? super E> predicate) {
        final IteratorMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return Iterator.of();
        }
        return new Iterator<Integer>(
            new FindLastIndexIteratorMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull Iterator<Integer> findLastIndexWhere(
          @NotNull final Predicate<? super E> predicate) {
        final IteratorMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return Iterator.of();
        }
        return new Iterator<Integer>(
            new FindLastIndexIteratorMaterializer<E>(materializer, toIndexedPredicate(predicate)));
      }

      @Override
      public @NotNull Iterator<Integer> findLastIndexOfSlice(@NotNull final Iterable<?> elements) {
        return new Iterator<Integer>(new FindLastIndexOfSliceIteratorMaterializer<E>(materializer,
            List.getElementsMaterializer(elements)));
      }

      @Override
      public @NotNull <F> Iterator<F> flatMap(
          @NotNull final Function<? super E, ? extends Iterable<F>> mapper) {
        final IteratorMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return Iterator.of();
        }
        return new Iterator<F>(
            new FlatMapIteratorMaterializer<E, F>(materializer, getElementToMaterializer(mapper)));
      }

      @Override
      public E first() {
        return materializer.materializeNext();
      }

      @Override
      public @NotNull <F> Iterator<F> flatMap(
          @NotNull final IndexedFunction<? super E, ? extends Iterable<F>> mapper) {
        final IteratorMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return Iterator.of();
        }
        return new Iterator<F>(
            new FlatMapIteratorMaterializer<E, F>(materializer, getElementToMaterializer(mapper)));
      }

      @Override
      public @NotNull Iterator<E> flatMapAfter(final int numElements,
          @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
        if (numElements < 0 || numElements == Integer.MAX_VALUE) {
          return this;
        }
        final IteratorMaterializer<E> materializer = this.materializer;
        final int knownSize = materializer.knownSize();
        if (knownSize == 0 || (knownSize > 0 && knownSize <= numElements)) {
          return this;
        }
        return new Iterator<E>(new FlatMapAfterIteratorMaterializer<E>(materializer, numElements,
            getElementToMaterializer(mapper)));
      }

      @Override
      public @NotNull Iterator<E> flatMapAfter(final int numElements,
          @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
        if (numElements < 0 || numElements == Integer.MAX_VALUE) {
          return this;
        }
        final IteratorMaterializer<E> materializer = this.materializer;
        final int knownSize = materializer.knownSize();
        if (knownSize == 0 || (knownSize > 0 && knownSize <= numElements)) {
          return this;
        }
        return new Iterator<E>(new FlatMapAfterIteratorMaterializer<E>(materializer, numElements,
            getElementToMaterializer(mapper)));
      }

      @Override
      public @NotNull <T extends Throwable> Iterator<E> flatMapExceptionally(
          @NotNull final Class<T> exceptionType,
          @NotNull final Function<? super T, ? extends Iterable<? extends E>> mapper) {
        final IteratorMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new Iterator<E>(new FlatMapExceptionallyIteratorMaterializer<E>(materializer,
            getExceptionToMaterializer(exceptionType, mapper)));
      }

      @Override
      public @NotNull <T extends Throwable> Iterator<E> flatMapExceptionally(
          @NotNull final Class<T> exceptionType,
          @NotNull final IndexedFunction<? super T, ? extends Iterable<? extends E>> mapper) {
        final IteratorMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new Iterator<E>(new FlatMapExceptionallyIteratorMaterializer<E>(materializer,
            getExceptionToMaterializer(exceptionType, mapper)));
      }

      @Override
      public @NotNull Iterator<E> flatMapExceptionally(
          @NotNull final Function<? super Throwable, ? extends Iterable<? extends E>> mapper) {
        final IteratorMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new Iterator<E>(new FlatMapExceptionallyIteratorMaterializer<E>(materializer,
            getElementToMaterializer(mapper)));
      }

      @Override
      public @NotNull Iterator<E> flatMapExceptionally(
          @NotNull final IndexedFunction<? super Throwable, ? extends Iterable<? extends E>> mapper) {
        final IteratorMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new Iterator<E>(new FlatMapExceptionallyIteratorMaterializer<E>(materializer,
            getElementToMaterializer(mapper)));
      }

      @Override
      public @NotNull Iterator<E> flatMapFirstWhere(
          @NotNull final IndexedPredicate<? super E> predicate,
          @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
        final IteratorMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new Iterator<E>(new FlatMapFirstWhereIteratorMaterializer<E>(materializer, predicate,
            getElementToMaterializer(mapper)));
      }

      @Override
      public @NotNull Iterator<E> flatMapFirstWhere(@NotNull final Predicate<? super E> predicate,
          @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
        final IteratorMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new Iterator<E>(new FlatMapFirstWhereIteratorMaterializer<E>(materializer,
            toIndexedPredicate(predicate), getElementToMaterializer(mapper)));
      }

      @Override
      public @NotNull Iterator<E> flatMapLastWhere(
          @NotNull final IndexedPredicate<? super E> predicate,
          @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
        final IteratorMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new Iterator<E>(new FlatMapLastWhereIteratorMaterializer<E>(materializer, predicate,
            getElementToMaterializer(mapper)));
      }

      @Override
      public @NotNull Iterator<E> flatMapLastWhere(@NotNull final Predicate<? super E> predicate,
          @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
        final IteratorMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new Iterator<E>(
            new FlatMapLastWhereIteratorMaterializer<E>(materializer, toIndexedPredicate(predicate),
                getElementToMaterializer(mapper)));
      }

      @Override
      public @NotNull Iterator<E> flatMapWhere(@NotNull final IndexedPredicate<? super E> predicate,
          @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
        final IteratorMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new Iterator<E>(new FlatMapWhereIteratorMaterializer<E>(materializer, predicate,
            getElementToMaterializer(mapper)));
      }

      @Override
      public @NotNull Iterator<E> flatMapWhere(@NotNull final Predicate<? super E> predicate,
          @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
        final IteratorMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new Iterator<E>(
            new FlatMapWhereIteratorMaterializer<E>(materializer, toIndexedPredicate(predicate),
                getElementToMaterializer(mapper)));
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
        return new Iterator<F>(
            new FoldLeftIteratorMaterializer<E, F>(materializer, identity, operation));
      }

      @Override
      public @NotNull <F> Iterator<F> foldRight(final F identity,
          @NotNull final BinaryFunction<? super E, ? super F, ? extends F> operation) {
        final IteratorMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return Iterator.of(identity);
        }
        return new Iterator<F>(
            new FoldRightIteratorMaterializer<E, F>(materializer, identity, operation));
      }

      @Override
      @SuppressWarnings("unchecked")
      public @NotNull Iterator<? extends Iterator<E>> group(final int maxSize) {
        final IteratorMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return Iterator.of();
        }
        return new Iterator<Iterator<E>>(
            new GroupIteratorMaterializer<E, Iterator<E>>(materializer, maxSize,
                (Function<? super java.util.List<E>, ? extends Iterator<E>>) FROM_JAVA_LIST));
      }

      @Override
      @SuppressWarnings("unchecked")
      public @NotNull Iterator<? extends Iterator<E>> group(final int size, final E padding) {
        final IteratorMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return Iterator.of();
        }
        return new Iterator<Iterator<E>>(
            new GroupIteratorMaterializer<E, Iterator<E>>(materializer, size, padding,
                (Function<? super java.util.List<E>, ? extends Iterator<E>>) FROM_JAVA_LIST));
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
        return new Iterator<Boolean>(
            new IncludesAllIteratorMaterializer<E>(materializer, elements));
      }

      @Override
      public @NotNull Iterator<Boolean> includesSlice(@NotNull final Iterable<?> elements) {
        return new Iterator<Boolean>(new IncludesSliceIteratorMaterializer<E>(materializer,
            List.getElementsMaterializer(elements)));
      }

      @Override
      public @NotNull Iterator<E> insert(final E element) {
        return new Iterator<E>(new InsertIteratorMaterializer<E>(materializer, element));
      }

      @Override
      public @NotNull Iterator<E> insertAfter(final int numElements, final E element) {
        if (numElements < 0 || numElements == Integer.MAX_VALUE) {
          return this;
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
            return this;
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
        return new Iterator<E>(
            new InsertAllIteratorMaterializer<E>(materializer, getElementsMaterializer(elements)));
      }

      @Override
      public @NotNull Iterator<E> insertAllAfter(final int numElements,
          @NotNull final Iterable<? extends E> elements) {
        if (numElements < 0 || numElements == Integer.MAX_VALUE) {
          return this;
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
            return this;
          }
          if (knownSize == numElements) {
            return appendAll(elements);
          }
        }
        return new Iterator<E>(new InsertAllAfterIteratorMaterializer<E>(materializer, numElements,
            getElementsMaterializer(elements)));
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
      public E next() {
        return materializer.materializeNext();
      }

      @Override
      public boolean notEmpty() {
        return materializer.materializeHasNext();
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
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
        return new Iterator<E>(new FinallyIteratorMaterializer<E>(materializer, action));
      }

      @Override
      public int size() {
        final IteratorMaterializer<E> materializer = this.materializer;
        int size = 0;
        while (materializer.materializeHasNext()) {
          materializer.materializeNext();
          ++size;
        }
        return size;
      }

      public @NotNull List<E> toList() {
        return List.wrap(this);
      }
    }

    public static class List<E> extends AbstractListSequence<E> {

      private static final List<?> EMPTY_LIST = new List<Object>(EmptyListMaterializer.instance());
      private static final List<Boolean> FALSE_LIST = new List<Boolean>(
          new ElementToListMaterializer<Boolean>(false));
      private static final List<Boolean> TRUE_LIST = new List<Boolean>(
          new ElementToListMaterializer<Boolean>(true));
      private static final List<Integer> ZERO_LIST = new List<Integer>(
          new ElementToListMaterializer<Integer>(0));
      private static final Function<? extends java.util.List<?>, ? extends List<?>> FROM_JAVA_LIST = new Function<java.util.List<Object>, List<Object>>() {
        @Override
        public List<Object> apply(final java.util.List<Object> param) {
          return new List<Object>(new ListToListMaterializer<Object>(param));
        }
      };

      private final ListMaterializer<E> materializer;

      private List(@NotNull final ListMaterializer<E> materializer) {
        super(materializer);
        this.materializer = materializer;
      }

      public static @NotNull <E> List<E> from(@NotNull final Iterable<E> elements) {
        final ArrayList<E> list = new ArrayList<E>();
        for (final E element : elements) {
          list.add(element);
        }
        return new List<E>(new ListToListMaterializer<E>(list));
      }

      @SuppressWarnings("unchecked")
      public static @NotNull <E> List<E> of() {
        return (List<E>) EMPTY_LIST;
      }

      public static @NotNull <E> List<E> of(final E first) {
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
        return new List<E>(
            new ArrayToListMaterializer<E>(Arrays.copyOf(elements, elements.length)));
      }

      public static @NotNull List<Character> ofChars(@NotNull final CharSequence chars) {
        return new List<Character>(new CharSequenceToListMaterializer(chars));
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
        if (Require.notNegative(count, "count") == 0) {
          return List.of();
        }
        return new List<E>(new RepeatListMaterializer<E>(count, element));
      }

      public static @NotNull <E> List<E> wrap(@NotNull final Iterable<? extends E> elements) {
        return new List<E>(getElementsMaterializer(elements));
      }

      @SuppressWarnings("unchecked")
      private static @NotNull <E> ListMaterializer<E> getElementsMaterializer(
          @NotNull final Iterable<? extends E> elements) {
        if (elements instanceof List) {
          return ((List<E>) elements).materializer;
        }
        if (elements instanceof java.util.List) {
          final java.util.List<E> list = (java.util.List<E>) elements;
          if (list.isEmpty()) {
            return EmptyListMaterializer.instance();
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
        return new IteratorToListMaterializer<E>((Iterator<E>) elements.iterator());
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
      public @NotNull List<Boolean> all(@NotNull final IndexedPredicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return TRUE_LIST;
        }
        return new List<Boolean>(new AllListMaterializer<E>(materializer, predicate, true));
      }

      @Override
      public @NotNull List<Boolean> all(@NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return TRUE_LIST;
        }
        return new List<Boolean>(
            new AllListMaterializer<E>(materializer, toIndexedPredicate(predicate), true));
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
        if (materializer.knownSize() == 0) {
          return new List<E>(getElementsMaterializer(elements));
        }
        return new List<E>(
            new AppendAllListMaterializer<E>(materializer, getElementsMaterializer(elements)));
      }

      @Override
      public <T> T apply(@NotNull final Function<? super Sequence<E>, T> mapper) {
        return null;
      }

      @Override
      @SuppressWarnings("unchecked")
      public @NotNull <F> List<F> as() {
        return (List<F>) this;
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
      public @NotNull List<Integer> count(@NotNull final IndexedPredicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return ZERO_LIST;
        }
        return new List<Integer>(new CountWhereListMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull List<Integer> count(@NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return ZERO_LIST;
        }
        return new List<Integer>(
            new CountWhereListMaterializer<E>(materializer, toIndexedPredicate(predicate)));
      }

      @Override
      public @NotNull List<E> diff(@NotNull final Iterable<?> elements) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        final ListMaterializer<Object> elementsMaterializer = getElementsMaterializer(elements);
        if (elementsMaterializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new RemoveWhereListMaterializer<E>(materializer,
            elementsContains(elementsMaterializer)));
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
        final ListMaterializer<E> materializer = this.materializer;
        final int knownSize = materializer.knownSize();
        if (maxElements <= 0 || knownSize == 0) {
          return this;
        }
        if (maxElements == Integer.MAX_VALUE || (knownSize > 0 && maxElements >= knownSize)) {
          return List.of();
        }
        return new List<E>(new DropListMaterializer<E>(materializer, maxElements));
      }

      @Override
      public @NotNull List<E> dropRight(final int maxElements) {
        final ListMaterializer<E> materializer = this.materializer;
        final int knownSize = materializer.knownSize();
        if (maxElements <= 0 || knownSize == 0) {
          return this;
        }
        if (maxElements == Integer.MAX_VALUE || (knownSize > 0 && maxElements >= knownSize)) {
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
        return new List<E>(new DropRightWhileListMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull List<E> dropRightWhile(@NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(
            new DropRightWhileListMaterializer<E>(materializer, toIndexedPredicate(predicate)));
      }

      @Override
      public @NotNull List<E> dropWhile(@NotNull final IndexedPredicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new DropWhileListMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull List<E> dropWhile(@NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(
            new DropWhileListMaterializer<E>(materializer, toIndexedPredicate(predicate)));
      }

      @Override
      public @NotNull List<Boolean> endsWith(@NotNull final Iterable<?> elements) {
        final ListMaterializer<?> elementsMaterializer = getElementsMaterializer(elements);
        if (elementsMaterializer.knownSize() == 0) {
          return TRUE_LIST;
        }
        return new List<Boolean>(
            new EndsWithListMaterializer<E>(materializer, elementsMaterializer));
      }

      @Override
      public @NotNull List<Boolean> exists(@NotNull final IndexedPredicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return FALSE_LIST;
        }
        return new List<Boolean>(new ExistsListMaterializer<E>(materializer, predicate, false));
      }

      @Override
      public @NotNull List<Boolean> exists(@NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return FALSE_LIST;
        }
        return new List<Boolean>(
            new ExistsListMaterializer<E>(materializer, toIndexedPredicate(predicate), false));
      }

      @Override
      public @NotNull List<E> filter(@NotNull final IndexedPredicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new RemoveWhereListMaterializer<E>(materializer, negated(predicate)));
      }

      @Override
      public @NotNull List<E> filter(@NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(
            new RemoveWhereListMaterializer<E>(materializer, toNegatedIndexedPredicate(predicate)));
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
        return new List<E>(new FindFirstListMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull List<E> findFirst(@NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(
            new FindFirstListMaterializer<E>(materializer, toIndexedPredicate(predicate)));
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
      public @NotNull List<Integer> findIndexWhere(
          @NotNull final IndexedPredicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return List.of();
        }
        return new List<Integer>(new FindIndexListMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull List<Integer> findIndexWhere(@NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return List.of();
        }
        return new List<Integer>(
            new FindIndexListMaterializer<E>(materializer, toIndexedPredicate(predicate)));
      }

      @Override
      public @NotNull List<Integer> findIndexOfSlice(@NotNull final Iterable<?> elements) {
        final ListMaterializer<?> elementsMaterializer = getElementsMaterializer(elements);
        return new List<Integer>(
            new FindIndexOfSliceListMaterializer<E>(materializer, elementsMaterializer));
      }

      @Override
      public @NotNull List<E> findLast(@NotNull final IndexedPredicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new FindLastListMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull List<E> findLast(@NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(
            new FindLastListMaterializer<E>(materializer, toIndexedPredicate(predicate)));
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
      public @NotNull List<Integer> findLastIndexWhere(
          @NotNull final IndexedPredicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return List.of();
        }
        return new List<Integer>(new FindLastIndexListMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull List<Integer> findLastIndexWhere(
          @NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return List.of();
        }
        return new List<Integer>(
            new FindLastIndexListMaterializer<E>(materializer, toIndexedPredicate(predicate)));
      }

      @Override
      public @NotNull List<Integer> findLastIndexOfSlice(@NotNull final Iterable<?> elements) {
        final ListMaterializer<?> elementsMaterializer = getElementsMaterializer(elements);
        return new List<Integer>(
            new FindLastIndexOfSliceListMaterializer<E>(materializer, elementsMaterializer));
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
              getElementToMaterializer(mapper)));
        }
        return new List<F>(
            new FlatMapListMaterializer<E, F>(materializer, toIndexedFunction(mapper)));
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
              getElementToMaterializer(mapper)));
        }
        return new List<F>(new FlatMapListMaterializer<E, F>(materializer, mapper));
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
              getElementToMaterializer(mapper)));
        }
        return new List<E>(new FlatMapAfterListMaterializer<E>(materializer, numElements,
            getElementToMaterializer(mapper)));
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
              getElementToMaterializer(mapper)));
        }
        return new List<E>(new FlatMapAfterListMaterializer<E>(materializer, numElements,
            getElementToMaterializer(mapper)));
      }

      @Override
      public @NotNull List<E> flatMapFirstWhere(
          @NotNull final IndexedPredicate<? super E> predicate,
          @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
        final ListMaterializer<E> materializer = this.materializer;
        final int knownSize = materializer.knownSize();
        if (knownSize == 0) {
          return List.of();
        }
        if (knownSize == 1) {
          return new List<E>(new SingleFlatMapWhereListMaterializer<E>(materializer, predicate,
              getElementToMaterializer(mapper)));
        }
        return new List<E>(new FlatMapFirstWhereListMaterializer<E>(materializer, predicate,
            getElementToMaterializer(mapper)));
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
          return new List<E>(
              new SingleFlatMapWhereListMaterializer<E>(materializer, toIndexedPredicate(predicate),
                  getElementToMaterializer(mapper)));
        }
        return new List<E>(
            new FlatMapFirstWhereListMaterializer<E>(materializer, toIndexedPredicate(predicate),
                getElementToMaterializer(mapper)));
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
          return new List<E>(new SingleFlatMapWhereListMaterializer<E>(materializer, predicate,
              getElementToMaterializer(mapper)));
        }
        return new List<E>(new FlatMapLastWhereListMaterializer<E>(materializer, predicate,
            getElementToMaterializer(mapper)));
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
          return new List<E>(
              new SingleFlatMapWhereListMaterializer<E>(materializer, toIndexedPredicate(predicate),
                  getElementToMaterializer(mapper)));
        }
        return new List<E>(
            new FlatMapLastWhereListMaterializer<E>(materializer, toIndexedPredicate(predicate),
                getElementToMaterializer(mapper)));
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
          return new List<E>(new SingleFlatMapWhereListMaterializer<E>(materializer, predicate,
              getElementToMaterializer(mapper)));
        }
        return new List<E>(new FlatMapWhereListMaterializer<E>(materializer, predicate, mapper));
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
          return new List<E>(
              new SingleFlatMapWhereListMaterializer<E>(materializer, toIndexedPredicate(predicate),
                  getElementToMaterializer(mapper)));
        }
        return new List<E>(
            new FlatMapWhereListMaterializer<E>(materializer, toIndexedPredicate(predicate),
                toIndexedFunction(mapper)));
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
        return new List<F>(new FoldLeftListMaterializer<E, F>(materializer, identity, operation));
      }

      @Override
      public @NotNull <F> List<F> foldRight(final F identity,
          @NotNull final BinaryFunction<? super E, ? super F, ? extends F> operation) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return List.of(identity);
        }
        return new List<F>(new FoldRightListMaterializer<E, F>(materializer, identity, operation));
      }

      @Override
      @SuppressWarnings("unchecked")
      public @NotNull List<? extends List<E>> group(final int maxSize) {
        Require.positive(maxSize, "maxSize");
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return List.of();
        }
        return new List<List<E>>(new GroupListMaterializer<E, List<E>>(materializer, maxSize,
            (Function<? super java.util.List<E>, ? extends List<E>>) FROM_JAVA_LIST));
      }

      @Override
      @SuppressWarnings("unchecked")
      public @NotNull List<? extends List<E>> group(final int size, final E padding) {
        Require.positive(size, "size");
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return List.of();
        }
        if (size == 1) {
          return group(1);
        }
        return new List<List<E>>(new GroupListMaterializer<E, List<E>>(materializer, size, padding,
            (Function<? super java.util.List<E>, ? extends List<E>>) FROM_JAVA_LIST));
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
        return new List<Boolean>(new IncludesAllListMaterializer<E>(materializer, elements));
      }

      @Override
      public @NotNull List<Boolean> includesSlice(@NotNull final Iterable<?> elements) {
        final ListMaterializer<?> elementsMaterializer = getElementsMaterializer(elements);
        return new List<Boolean>(
            new IncludesSliceListMaterializer<E>(materializer, elementsMaterializer));
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
            getElementsMaterializer(elements)));
      }

      @Override
      public @NotNull List<E> intersect(@NotNull final Iterable<?> elements) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        final ListMaterializer<Object> elementsMaterializer = getElementsMaterializer(elements);
        if (elementsMaterializer.knownSize() == 0) {
          return List.of();
        }
        return new List<E>(new RemoveWhereListMaterializer<E>(materializer,
            elementsNotContains(elementsMaterializer)));
      }

      @Override
      public @NotNull Iterator<E> iterator() {
        return Iterator.wrap(this);
      }

      @Override
      public @NotNull ListIterator<E> listIterator() {
        return new ListIterator<E>(List.<E>of(), this);
      }

      @Override
      public @NotNull ListIterator<E> listIterator(final int index) {
        if (index < 0 || index == Integer.MAX_VALUE) {
          throw new IndexOutOfBoundsException(Integer.toString(index));
        }
        final int knownSize = materializer.knownSize();
        if (knownSize >= 0 && index >= knownSize) {
          throw new IndexOutOfBoundsException(Integer.toString(index));
        }
        return new ListIterator<E>(List.<E>of(), this, index);
      }

      @Override
      public @NotNull <F> List<F> map(@NotNull final Function<? super E, F> mapper) {
        final ListMaterializer<E> materializer = this.materializer;
        final int knownSize = materializer.knownSize();
        if (knownSize == 0) {
          return List.of();
        }
        if (knownSize == 1) {
          return new List<F>(
              new SingleMapListMaterializer<E, F>(materializer, toIndexedFunction(mapper)));
        }
        return new List<F>(new MapListMaterializer<E, F>(materializer, toIndexedFunction(mapper)));
      }

      @Override
      public @NotNull <F> List<F> map(@NotNull final IndexedFunction<? super E, F> mapper) {
        final ListMaterializer<E> materializer = this.materializer;
        final int knownSize = materializer.knownSize();
        if (knownSize == 0) {
          return List.of();
        }
        if (knownSize == 1) {
          return new List<F>(new SingleMapListMaterializer<E, F>(materializer, mapper));
        }
        return new List<F>(new MapListMaterializer<E, F>(materializer, mapper));
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
          return new List<E>(
              new SingleMapListMaterializer<E, E>(materializer, toIndexedFunction(mapper)));
        }
        return new List<E>(
            new MapAfterListMaterializer<E>(materializer, numElements, toIndexedFunction(mapper)));
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
          return new List<E>(new SingleMapListMaterializer<E, E>(materializer, mapper));
        }
        return new List<E>(new MapAfterListMaterializer<E>(materializer, numElements, mapper));
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
          return new List<E>(
              new SingleMapWhereListMaterializer<E>(materializer, predicate, mapper));
        }
        return new List<E>(new MapFirstWhereListMaterializer<E>(materializer, predicate, mapper));
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
          return new List<E>(
              new SingleMapWhereListMaterializer<E>(materializer, toIndexedPredicate(predicate),
                  toIndexedFunction(mapper)));
        }
        return new List<E>(
            new MapFirstWhereListMaterializer<E>(materializer, toIndexedPredicate(predicate),
                toIndexedFunction(mapper)));
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
          return new List<E>(
              new SingleMapWhereListMaterializer<E>(materializer, predicate, mapper));
        }
        return new List<E>(new MapLastWhereListMaterializer<E>(materializer, predicate, mapper));
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
          return new List<E>(
              new SingleMapWhereListMaterializer<E>(materializer, toIndexedPredicate(predicate),
                  toIndexedFunction(mapper)));
        }
        return new List<E>(
            new MapLastWhereListMaterializer<E>(materializer, toIndexedPredicate(predicate),
                toIndexedFunction(mapper)));
      }

      @Override
      public @NotNull List<E> mapWhere(@NotNull final IndexedPredicate<? super E> predicate,
          @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new MapWhereListMaterializer<E>(materializer, predicate, mapper));
      }

      @Override
      public @NotNull List<E> mapWhere(@NotNull final Predicate<? super E> predicate,
          @NotNull final Function<? super E, ? extends E> mapper) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(
            new MapWhereListMaterializer<E>(materializer, toIndexedPredicate(predicate),
                toIndexedFunction(mapper)));
      }

      @Override
      public @NotNull List<E> max(@NotNull final Comparator<? super E> comparator) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new MaxListMaterializer<E>(materializer, comparator));
      }

      @Override
      public @NotNull List<E> min(@NotNull final Comparator<? super E> comparator) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new MaxListMaterializer<E>(materializer, reversed(comparator)));
      }

      @Override
      public @NotNull List<Boolean> notAll(@NotNull final IndexedPredicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return FALSE_LIST;
        }
        return new List<Boolean>(
            new ExistsListMaterializer<E>(materializer, negated(predicate), false));
      }

      @Override
      public @NotNull List<Boolean> notAll(@NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return FALSE_LIST;
        }
        return new List<Boolean>(
            new ExistsListMaterializer<E>(materializer, toNegatedIndexedPredicate(predicate),
                false));
      }

      @Override
      public @NotNull List<Boolean> notExists(
          @NotNull final IndexedPredicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return TRUE_LIST;
        }
        return new List<Boolean>(
            new AllListMaterializer<E>(materializer, negated(predicate), true));
      }

      @Override
      public @NotNull List<Boolean> notExists(@NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return TRUE_LIST;
        }
        return new List<Boolean>(
            new AllListMaterializer<E>(materializer, toNegatedIndexedPredicate(predicate), true));
      }

      @Override
      public @NotNull List<E> orElse(@NotNull final Iterable<E> elements) {
        final ListMaterializer<E> elementsMaterializer = getElementsMaterializer(elements);
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return new List<E>(elementsMaterializer);
        }
        return new List<E>(new OrElseListMaterializer<E>(materializer, elementsMaterializer));
      }

      @Override
      public @NotNull List<E> orElseGet(
          @NotNull final Supplier<? extends Iterable<? extends E>> supplier) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return new List<E>(new SuppliedMeterializer<E>(supplier));
        }
        return new List<E>(
            new OrElseListMaterializer<E>(materializer, new SuppliedMeterializer<E>(supplier)));
      }

      @Override
      public @NotNull List<E> plus(final E element) {
        return append(element);
      }

      @Override
      public @NotNull List<E> plusAll(@NotNull final Iterable<E> elements) {
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
        final ListMaterializer<E> elementsMaterializer = getElementsMaterializer(elements);
        final ListMaterializer<E> materializer = this.materializer;
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
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new ReduceLeftListMaterializer<E>(materializer, operation));
      }

      @Override
      public @NotNull List<E> reduceRight(
          @NotNull final BinaryFunction<? super E, ? super E, ? extends E> operation) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new ReduceRightListMaterializer<E>(materializer, operation));
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
        return new List<E>(
            new RemoveWhereListMaterializer<E>(materializer, equalsElement(element)));
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
      public @NotNull List<E> removeFirstWhere(
          @NotNull final IndexedPredicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new RemoveFirstWhereListMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull List<E> removeFirstWhere(@NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(
            new RemoveFirstWhereListMaterializer<E>(materializer, toIndexedPredicate(predicate)));
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
      public @NotNull List<E> removeLastWhere(
          @NotNull final IndexedPredicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new RemoveLastWhereListMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull List<E> removeLastWhere(@NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(
            new RemoveLastWhereListMaterializer<E>(materializer, toIndexedPredicate(predicate)));
      }

      @Override
      public @NotNull List<E> removeSlice(final int start, final int end) {
        if (end <= start && start >= 0 && end >= 0) {
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
        }
        return new List<E>(new RemoveSliceListMaterializer<E>(materializer, start, end));
      }

      @Override
      public @NotNull List<E> removeWhere(@NotNull final IndexedPredicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new RemoveWhereListMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull List<E> removeWhere(@NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(
            new RemoveWhereListMaterializer<E>(materializer, toIndexedPredicate(predicate)));
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
        return new List<E>(new MapWhereListMaterializer<E>(materializer, equalsElement(element),
            replacementMapper(replacement)));
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
        return new List<E>(
            new MapFirstWhereListMaterializer<E>(materializer, equalsElement(element),
                replacementMapper(replacement)));
      }

      @Override
      public @NotNull List<E> replaceFirstWhere(
          @NotNull final IndexedPredicate<? super E> predicate, final E replacement) {
        final ListMaterializer<E> materializer = this.materializer;
        final int knownSize = materializer.knownSize();
        if (knownSize == 0) {
          return this;
        }
        if (knownSize == 1) {
          return new List<E>(new SingleMapWhereListMaterializer<E>(materializer, predicate,
              replacementMapper(replacement)));
        }
        return new List<E>(new MapFirstWhereListMaterializer<E>(materializer, predicate,
            replacementMapper(replacement)));
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
          return new List<E>(
              new SingleMapWhereListMaterializer<E>(materializer, toIndexedPredicate(predicate),
                  replacementMapper(replacement)));
        }
        return new List<E>(
            new MapFirstWhereListMaterializer<E>(materializer, toIndexedPredicate(predicate),
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
          return new List<E>(new SingleMapWhereListMaterializer<E>(materializer, predicate,
              replacementMapper(replacement)));
        }
        return new List<E>(new MapLastWhereListMaterializer<E>(materializer, predicate,
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
          return new List<E>(
              new SingleMapWhereListMaterializer<E>(materializer, toIndexedPredicate(predicate),
                  replacementMapper(replacement)));
        }
        return new List<E>(
            new MapLastWhereListMaterializer<E>(materializer, toIndexedPredicate(predicate),
                replacementMapper(replacement)));
      }

      @Override
      public @NotNull List<E> replaceSlice(final int start, final int end,
          @NotNull final Iterable<? extends E> patch) {
        if (start >= 0 && start == end) {
          return insertAllAfter(start, patch);
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
            return insertAllAfter(knownStart, patch);
          }
        }
        return new List<E>(new ReplaceSliceListMaterializer<E>(materializer, start, end,
            getElementsMaterializer(patch)));
      }

      @Override
      public @NotNull List<E> replaceWhere(@NotNull final IndexedPredicate<? super E> predicate,
          final E replacement) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new MapWhereListMaterializer<E>(materializer, predicate,
            replacementMapper(replacement)));
      }

      @Override
      public @NotNull List<E> replaceWhere(@NotNull final Predicate<? super E> predicate,
          final E replacement) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(
            new MapWhereListMaterializer<E>(materializer, toIndexedPredicate(predicate),
                replacementMapper(replacement)));
      }

      @Override
      public @NotNull List<E> resizeTo(final int numElements, final E padding) {
        if (Require.notNegative(numElements, "numElements") == 0) {
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
          if (knownSize < numElements) {
            return new List<E>(new TakeListMaterializer<E>(materializer, numElements));
          }
          return new List<E>(new AppendAllListMaterializer<E>(materializer,
              new RepeatListMaterializer<E>(knownSize - numElements, padding)));
        }
        return new List<E>(new ResizeListMaterializer<E>(materializer, numElements, padding));
      }

      @Override
      public @NotNull List<E> reverse() {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new ReverseListMaterializer<E>(materializer));
      }

      @Override
      public @NotNull List<E> slice(final int start, final int end) {
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
      public @NotNull List<Boolean> startsWith(@NotNull final Iterable<?> elements) {
        final ListMaterializer<?> elementsMaterializer = getElementsMaterializer(elements);
        if (elementsMaterializer.knownSize() == 0) {
          return TRUE_LIST;
        }
        return new List<Boolean>(
            new StartsWithListMaterializer<E>(materializer, elementsMaterializer));
      }

      @Override
      public @NotNull List<E> sorted(@NotNull final Comparator<? super E> comparator) {
        final ListMaterializer<E> materializer = this.materializer;
        final int knownSize = materializer.knownSize();
        if (knownSize == 0 || knownSize == 1) {
          return this;
        }
        return new List<E>(new SortedListMaterializer<E>(materializer, comparator));
      }

      @Override
      public @NotNull List<E> take(final int maxElements) {
        if (maxElements <= 0) {
          return List.of();
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
        return new List<E>(new TakeRightWhileListMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull List<E> takeRightWhile(@NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(
            new TakeRightWhileListMaterializer<E>(materializer, toIndexedPredicate(predicate)));
      }

      @Override
      public @NotNull List<E> takeWhile(@NotNull final IndexedPredicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new TakeWhileListMaterializer<E>(materializer, predicate));
      }

      @Override
      public @NotNull List<E> takeWhile(@NotNull final Predicate<? super E> predicate) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(
            new TakeWhileListMaterializer<E>(materializer, toIndexedPredicate(predicate)));
      }

      @Override
      @SuppressWarnings("unchecked")
      public @NotNull List<E> union(@NotNull final Iterable<? extends E> elements) {
        final ListMaterializer<E> materializer = this.materializer;
        if (materializer.knownSize() == 0) {
          return (List<E>) List.from(elements);
        }
        final ListMaterializer<E> elementsMaterializer = getElementsMaterializer(elements);
        if (elementsMaterializer.knownSize() == 0) {
          return this;
        }
        return new List<E>(new AppendAllListMaterializer<E>(materializer,
            new RemoveWhereListMaterializer<E>(elementsMaterializer,
                elementsContains(materializer))));
      }

      private static class SuppliedMeterializer<E> implements ListMaterializer<E> {

        private volatile ListMaterializer<E> state;

        private SuppliedMeterializer(
            @NotNull final Supplier<? extends Iterable<? extends E>> supplier) {
          state = new ImmaterialState(Require.notNull(supplier, "supplier"));
        }

        @Override
        public boolean canMaterializeElement(final int index) {
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
        public E materializeElement(final int index) {
          return state.materializeElement(index);
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

          private ImmaterialState(
              @NotNull final Supplier<? extends Iterable<? extends E>> supplier) {
            this.supplier = supplier;
          }

          @Override
          public boolean canMaterializeElement(final int index) {
            try {
              final ListMaterializer<E> elementsMaterializer = getElementsMaterializer(
                  supplier.get());
              state = elementsMaterializer;
              return elementsMaterializer.canMaterializeElement(index);
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
              state = elementsMaterializer;
              return elementsMaterializer.materializeContains(element);
            } catch (final Exception e) {
              throw UncheckedException.throwUnchecked(e);
            }
          }

          @Override
          public E materializeElement(final int index) {
            try {
              final ListMaterializer<E> elementsMaterializer = getElementsMaterializer(
                  supplier.get());
              state = elementsMaterializer;
              return elementsMaterializer.materializeElement(index);
            } catch (final Exception e) {
              throw UncheckedException.throwUnchecked(e);
            }
          }

          @Override
          public boolean materializeEmpty() {
            try {
              final ListMaterializer<E> elementsMaterializer = getElementsMaterializer(
                  supplier.get());
              state = elementsMaterializer;
              return elementsMaterializer.materializeEmpty();
            } catch (final Exception e) {
              throw UncheckedException.throwUnchecked(e);
            }
          }

          @Override
          public @NotNull java.util.Iterator<E> materializeIterator() {
            try {
              final ListMaterializer<E> elementsMaterializer = getElementsMaterializer(
                  supplier.get());
              state = elementsMaterializer;
              return elementsMaterializer.materializeIterator();
            } catch (final Exception e) {
              throw UncheckedException.throwUnchecked(e);
            }
          }

          @Override
          public int materializeSize() {
            try {
              final ListMaterializer<E> elementsMaterializer = getElementsMaterializer(
                  supplier.get());
              state = elementsMaterializer;
              return elementsMaterializer.materializeSize();
            } catch (final Exception e) {
              throw UncheckedException.throwUnchecked(e);
            }
          }
        }
      }
    }

    public static class ListIterator<E> implements Sequence<E>, java.util.ListIterator<E> {

      private static final ListIterator<?> EMPTY_ITERATOR = new ListIterator<Object>(List.of(),
          List.of());
      private static final ListIterator<Boolean> FALSE_ITERATOR = new ListIterator<Boolean>(
          List.<Boolean>of(), List.of(true));
      private static final Function<Integer, Integer> INDEX_IDENTITY = new Function<Integer, Integer>() {
        @Override
        public Integer apply(final Integer param) {
          return param;
        }
      };
      private static final Function<? extends List<?>, ? extends ListIterator<?>> LIST_TO_ITERATOR = new Function<List<?>, ListIterator<?>>() {
        @Override
        public ListIterator<?> apply(final List<?> param) {
          return param.listIterator();
        }
      };
      private static final ListIterator<Boolean> TRUE_ITERATOR = new ListIterator<Boolean>(
          List.<Boolean>of(), List.of(true));
      private static final ListIterator<Integer> ZERO_ITERATOR = new ListIterator<Integer>(
          List.<Integer>of(), List.of(0));

      private final List<E> left;
      private final List<E> right;

      private int pos;

      private ListIterator(@NotNull final List<E> left, @NotNull final List<E> right) {
        this.left = left;
        this.right = right;
      }

      private ListIterator(@NotNull final List<E> left, @NotNull final List<E> right,
          final int pos) {
        this(left, right);
        this.pos = pos;
      }

      public static @NotNull <E> ListIterator<E> from(@NotNull final Iterable<E> elements) {
        return new ListIterator<E>(List.<E>of(), List.from(elements));
      }

      @SuppressWarnings("unchecked")
      public static @NotNull <E> ListIterator<E> of() {
        return (ListIterator<E>) EMPTY_ITERATOR;
      }

      public static @NotNull <E> ListIterator<E> of(final E first) {
        return new ListIterator<E>(List.<E>of(), List.of(first));
      }

      public static @NotNull <E> ListIterator<E> of(final E first, final E second) {
        return new ListIterator<E>(List.<E>of(), List.of(first, second));
      }

      public static @NotNull <E> ListIterator<E> of(final E first, final E second, final E third) {
        return new ListIterator<E>(List.<E>of(), List.of(first, second, third));
      }

      public static @NotNull <E> ListIterator<E> of(final E first, final E second, final E third,
          final E fourth) {
        return new ListIterator<E>(List.<E>of(), List.of(first, second, third, fourth));
      }

      public static @NotNull <E> ListIterator<E> of(final E first, final E second, final E third,
          final E fourth, final E fifth) {
        return new ListIterator<E>(List.<E>of(), List.of(first, second, third, fourth, fifth));
      }

      public static @NotNull <E> ListIterator<E> of(final E first, final E second, final E third,
          final E fourth, final E fifth, final E sixth) {
        return new ListIterator<E>(List.<E>of(),
            List.of(first, second, third, fourth, fifth, sixth));
      }

      public static @NotNull <E> ListIterator<E> of(final E first, final E second, final E third,
          final E fourth, final E fifth, final E sixth, final E seventh) {
        return new ListIterator<E>(List.<E>of(),
            List.of(first, second, third, fourth, fifth, sixth, seventh));
      }

      public static @NotNull <E> ListIterator<E> of(final E first, final E second, final E third,
          final E fourth, final E fifth, final E sixth, final E seventh, final E eighth) {
        return new ListIterator<E>(List.<E>of(),
            List.of(first, second, third, fourth, fifth, sixth, seventh, eighth));
      }

      public static @NotNull <E> ListIterator<E> of(final E first, final E second, final E third,
          final E fourth, final E fifth, final E sixth, final E seventh, final E eighth,
          final E ninth) {
        return new ListIterator<E>(List.<E>of(),
            List.of(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth));
      }

      public static @NotNull <E> ListIterator<E> of(final E first, final E second, final E third,
          final E fourth, final E fifth, final E sixth, final E seventh, final E eighth,
          final E ninth, final E tenth) {
        return new ListIterator<E>(List.<E>of(),
            List.of(first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth));
      }

      public static @NotNull <E> ListIterator<E> ofArray(final E... elements) {
        return new ListIterator<E>(List.<E>of(), List.ofArray(elements));
      }

      public static @NotNull <E> ListIterator<E> times(final int count, final E element) {
        if (Require.notNegative(count, "count") == 0) {
          return ListIterator.of();
        }
        return new ListIterator<E>(List.<E>of(), List.times(count, element));
      }

      public static @NotNull <E> ListIterator<E> wrap(
          @NotNull final Iterable<? extends E> elements) {
        return new ListIterator<E>(List.<E>of(), List.wrap(elements));
      }

      private static @NotNull <E> IndexedConsumer<E> offsetConsumer(final long offset,
          @NotNull final IndexedConsumer<E> consumer) {
        if (offset == 0) {
          return consumer;
        }
        return new IndexedConsumer<E>() {
          @Override
          public void accept(int index, E param) throws Exception {
            consumer.accept(IndexOverflowException.safeCast(offset + index), param);
          }
        };
      }

      private static @NotNull <E, F> IndexedFunction<E, F> offsetFunction(final long offset,
          @NotNull final IndexedFunction<E, F> function) {
        if (offset == 0) {
          return function;
        }
        return new IndexedFunction<E, F>() {
          @Override
          public F apply(final int index, final E param) throws Exception {
            return function.apply(IndexOverflowException.safeCast(offset + index), param);
          }
        };
      }

      private static @NotNull <E> IndexedPredicate<E> offsetPredicate(final long offset,
          @NotNull final IndexedPredicate<E> predicate) {
        if (offset == 0) {
          return predicate;
        }
        return new IndexedPredicate<E>() {
          @Override
          public boolean test(final int index, final E param) throws Exception {
            return predicate.test(IndexOverflowException.safeCast(offset + index), param);
          }
        };
      }

      private static @NotNull Function<Integer, Integer> offsetMapper(final long offset) {
        if (offset == 0) {
          return INDEX_IDENTITY;
        }
        return new Function<Integer, Integer>() {
          @Override
          public Integer apply(final Integer param) {
            return IndexOverflowException.safeCast(offset + param);
          }
        };
      }

      @Override
      public void add(final E e) {
        throw new UnsupportedOperationException();
      }

      @Override
      public @NotNull ListIterator<Boolean> all(
          @NotNull final IndexedPredicate<? super E> predicate) {
        if (atEnd()) {
          return TRUE_ITERATOR;
        }
        return new ListIterator<Boolean>(List.<Boolean>of(),
            currentRight().all(offsetPredicate(nextIndex(), predicate)));
      }

      @Override
      public @NotNull ListIterator<Boolean> all(@NotNull final Predicate<? super E> predicate) {
        if (atEnd()) {
          return TRUE_ITERATOR;
        }
        return new ListIterator<Boolean>(List.<Boolean>of(), currentRight().all(predicate));
      }

      @Override
      public <T> T apply(@NotNull Function<? super Sequence<E>, T> mapper) {
        return null;
      }

      public @NotNull ListIterator<E> append(final E element) {
        return new ListIterator<E>(left, right.append(element), pos);
      }

      public @NotNull ListIterator<E> appendAll(@NotNull final Iterable<? extends E> elements) {
        return new ListIterator<E>(left, right.appendAll(elements), pos);
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
        return new ListIterator<Integer>(List.<Integer>of(), currentRight().count());
      }

      @Override
      public @NotNull ListIterator<Integer> count(
          @NotNull final IndexedPredicate<? super E> predicate) {
        if (atEnd()) {
          return ZERO_ITERATOR;
        }
        return new ListIterator<Integer>(List.<Integer>of(),
            currentRight().count(offsetPredicate(nextIndex(), predicate)));
      }

      @Override
      public @NotNull ListIterator<Integer> count(@NotNull final Predicate<? super E> predicate) {
        if (atEnd()) {
          return ZERO_ITERATOR;
        }
        return new ListIterator<Integer>(List.<Integer>of(), currentRight().count(predicate));
      }

      @Override
      public @NotNull ListIterator<E> diff(@NotNull final Iterable<?> elements) {
        return new ListIterator<E>(currentLeft().diff(elements), currentRight().diff(elements));
      }

      @Override
      public void doFor(@NotNull final Consumer<? super E> consumer) {
        if (!atEnd()) {
          currentRight().doFor(consumer);
        }
      }

      @Override
      public void doFor(@NotNull final IndexedConsumer<? super E> consumer) {
        if (!atEnd()) {
          currentRight().doFor(offsetConsumer(nextIndex(), consumer));
        }
      }

      @Override
      public void doWhile(@NotNull final IndexedPredicate<? super E> predicate) {
        if (!atEnd()) {
          currentRight().doWhile(offsetPredicate(nextIndex(), predicate));
        }
      }

      @Override
      public void doWhile(@NotNull final IndexedPredicate<? super E> condition,
          @NotNull final IndexedConsumer<? super E> consumer) {
        if (!atEnd()) {
          final int offset = nextIndex();
          currentRight().doWhile(offsetPredicate(offset, condition),
              offsetConsumer(offset, consumer));
        }
      }

      @Override
      public void doWhile(@NotNull final Predicate<? super E> predicate) {
        if (!atEnd()) {
          currentRight().doWhile(predicate);
        }
      }

      @Override
      public void doWhile(@NotNull final Predicate<? super E> condition,
          @NotNull final Consumer<? super E> consumer) {
        if (!atEnd()) {
          currentRight().doWhile(condition, consumer);
        }
      }

      @Override
      public @NotNull ListIterator<E> drop(final int maxElements) {
        if (maxElements <= 0 || atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft(), currentRight().drop(maxElements));
      }

      @Override
      public @NotNull ListIterator<E> dropRight(final int maxElements) {
        if (maxElements <= 0 || atEnd()) {
          return this;
        }
        final int pos = this.pos;
        final List<E> right = this.right;
        final int knownSize = right.materializer.knownSize();
        if (knownSize >= 0 && knownSize - Math.max(0, pos) >= maxElements) {
          new ListIterator<E>(left, right.dropRight(maxElements), pos);
        }
        return new ListIterator<E>(currentLeft(), currentRight().dropRight(maxElements));
      }

      @Override
      public @NotNull ListIterator<E> dropRightWhile(
          @NotNull final IndexedPredicate<? super E> predicate) {
        if (atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft(),
            currentRight().dropRightWhile(offsetPredicate(nextIndex(), predicate)));
      }

      @Override
      public @NotNull ListIterator<E> dropRightWhile(
          @NotNull final Predicate<? super E> predicate) {
        if (atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft(), currentRight().dropRightWhile(predicate));
      }

      @Override
      public @NotNull ListIterator<E> dropWhile(
          @NotNull final IndexedPredicate<? super E> predicate) {
        if (atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft(),
            currentRight().dropWhile(offsetPredicate(nextIndex(), predicate)));
      }

      @Override
      public @NotNull ListIterator<E> dropWhile(@NotNull final Predicate<? super E> predicate) {
        if (atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft(), currentRight().dropWhile(predicate));
      }

      @Override
      public @NotNull ListIterator<Boolean> endsWith(@NotNull final Iterable<?> elements) {
        return new ListIterator<Boolean>(List.<Boolean>of(), currentRight().endsWith(elements));
      }

      @Override
      public @NotNull ListIterator<Boolean> exists(
          @NotNull final IndexedPredicate<? super E> predicate) {
        if (atEnd()) {
          return FALSE_ITERATOR;
        }
        return new ListIterator<Boolean>(List.<Boolean>of(),
            currentRight().exists(offsetPredicate(nextIndex(), predicate)));
      }

      @Override
      public @NotNull ListIterator<Boolean> exists(@NotNull final Predicate<? super E> predicate) {
        if (atEnd()) {
          return FALSE_ITERATOR;
        }
        return new ListIterator<Boolean>(List.<Boolean>of(), currentRight().exists(predicate));
      }

      @Override
      public @NotNull ListIterator<E> filter(@NotNull final IndexedPredicate<? super E> predicate) {
        if (atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft().filter(predicate),
            currentRight().filter(offsetPredicate(nextIndex(), predicate)));
      }

      @Override
      public @NotNull ListIterator<E> filter(@NotNull final Predicate<? super E> predicate) {
        if (atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft().filter(predicate),
            currentRight().filter(predicate));
      }

      @Override
      public @NotNull ListIterator<E> findAny(
          @NotNull final IndexedPredicate<? super E> predicate) {
        if (atEnd()) {
          return ListIterator.of();
        }
        return new ListIterator<E>(List.<E>of(),
            currentRight().findAny(offsetPredicate(nextIndex(), predicate)));
      }

      @Override
      public @NotNull ListIterator<E> findAny(@NotNull final Predicate<? super E> predicate) {
        if (atEnd()) {
          return ListIterator.of();
        }
        return new ListIterator<E>(List.<E>of(), currentRight().findAny(predicate));
      }

      @Override
      public @NotNull ListIterator<E> findFirst(
          @NotNull final IndexedPredicate<? super E> predicate) {
        if (atEnd()) {
          return ListIterator.of();
        }
        return new ListIterator<E>(List.<E>of(),
            currentRight().findFirst(offsetPredicate(nextIndex(), predicate)));
      }

      @Override
      public @NotNull ListIterator<E> findFirst(@NotNull final Predicate<? super E> predicate) {
        if (atEnd()) {
          return ListIterator.of();
        }
        return new ListIterator<E>(List.<E>of(), currentRight().findFirst(predicate));
      }

      @Override
      public @NotNull ListIterator<Integer> findIndexOf(final Object element) {
        if (atEnd()) {
          return ListIterator.of();
        }
        return new ListIterator<Integer>(List.<Integer>of(),
            currentRight().findIndexOf(element).map(offsetMapper(nextIndex())));
      }

      @Override
      public @NotNull ListIterator<Integer> findIndexWhere(
          @NotNull final IndexedPredicate<? super E> predicate) {
        if (atEnd()) {
          return ListIterator.of();
        }
        return new ListIterator<Integer>(List.<Integer>of(),
            currentRight().findIndexWhere(offsetPredicate(nextIndex(), predicate))
                .map(offsetMapper(nextIndex())));
      }

      @Override
      public @NotNull ListIterator<Integer> findIndexWhere(
          @NotNull final Predicate<? super E> predicate) {
        if (atEnd()) {
          return ListIterator.of();
        }
        return new ListIterator<Integer>(List.<Integer>of(),
            currentRight().findIndexWhere(predicate).map(offsetMapper(nextIndex())));
      }

      @Override
      public @NotNull ListIterator<Integer> findIndexOfSlice(@NotNull final Iterable<?> elements) {
        return new ListIterator<Integer>(List.<Integer>of(),
            currentRight().findIndexOfSlice(elements).map(offsetMapper(nextIndex())));
      }

      @Override
      public @NotNull ListIterator<E> findLast(
          @NotNull final IndexedPredicate<? super E> predicate) {
        if (atEnd()) {
          return ListIterator.of();
        }
        return new ListIterator<E>(List.<E>of(),
            currentRight().findLast(offsetPredicate(nextIndex(), predicate)));
      }

      @Override
      public @NotNull ListIterator<E> findLast(@NotNull final Predicate<? super E> predicate) {
        if (atEnd()) {
          return ListIterator.of();
        }
        return new ListIterator<E>(List.<E>of(), currentRight().findLast(predicate));
      }

      @Override
      public @NotNull ListIterator<Integer> findLastIndexOf(final Object element) {
        if (atEnd()) {
          return ListIterator.of();
        }
        return new ListIterator<Integer>(List.<Integer>of(),
            currentRight().findLastIndexOf(element).map(offsetMapper(nextIndex())));
      }

      @Override
      public @NotNull ListIterator<Integer> findLastIndexWhere(
          @NotNull final IndexedPredicate<? super E> predicate) {
        if (atEnd()) {
          return ListIterator.of();
        }
        return new ListIterator<Integer>(List.<Integer>of(),
            currentRight().findLastIndexWhere(offsetPredicate(nextIndex(), predicate))
                .map(offsetMapper(nextIndex())));
      }

      @Override
      public @NotNull ListIterator<Integer> findLastIndexWhere(
          @NotNull final Predicate<? super E> predicate) {
        if (atEnd()) {
          return ListIterator.of();
        }
        return new ListIterator<Integer>(List.<Integer>of(),
            currentRight().findLastIndexWhere(predicate).map(offsetMapper(nextIndex())));
      }

      @Override
      public @NotNull ListIterator<Integer> findLastIndexOfSlice(
          @NotNull final Iterable<?> elements) {
        return new ListIterator<Integer>(List.<Integer>of(),
            currentRight().findLastIndexOfSlice(elements).map(offsetMapper(nextIndex())));
      }

      @Override
      public E first() {
        final int pos = this.pos;
        if (pos >= 0) {
          return right.get(pos);
        }
        final List<E> left = this.left;
        return left.get(left.size() + pos);
      }

      @Override
      public @NotNull <F> ListIterator<F> flatMap(
          @NotNull final Function<? super E, ? extends Iterable<F>> mapper) {
        return new ListIterator<F>(currentLeft().flatMap(mapper), currentRight().flatMap(mapper));
      }

      @Override
      public @NotNull <F> ListIterator<F> flatMap(
          @NotNull final IndexedFunction<? super E, ? extends Iterable<F>> mapper) {
        return new ListIterator<F>(currentLeft().flatMap(mapper),
            currentRight().flatMap(offsetFunction(nextIndex(), mapper)));
      }

      @Override
      public @NotNull ListIterator<E> flatMapAfter(final int numElements,
          @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
        if (numElements < 0 || numElements == Integer.MAX_VALUE || atEnd()) {
          return this;
        }
        final int pos = this.pos;
        if (pos >= 0) {
          return new ListIterator<E>(left,
              right.flatMapAfter(SizeOverflowException.safeCast((long) numElements + pos), mapper));
        }
        return new ListIterator<E>(currentLeft(), currentRight().flatMapAfter(numElements, mapper));
      }

      @Override
      public @NotNull ListIterator<E> flatMapAfter(final int numElements,
          @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
        if (numElements < 0 || numElements == Integer.MAX_VALUE || atEnd()) {
          return this;
        }
        final int pos = this.pos;
        if (pos >= 0) {
          return new ListIterator<E>(left,
              right.flatMapAfter(SizeOverflowException.safeCast((long) numElements + pos),
                  offsetFunction(nextIndex(), mapper)));
        }
        return new ListIterator<E>(currentLeft(),
            currentRight().flatMapAfter(numElements, offsetFunction(nextIndex(), mapper)));
      }

      @Override
      public @NotNull ListIterator<E> flatMapFirstWhere(
          @NotNull final IndexedPredicate<? super E> predicate,
          @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
        if (atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft(),
            currentRight().flatMapFirstWhere(offsetPredicate(nextIndex(), predicate),
                offsetFunction(nextIndex(), mapper)));
      }

      @Override
      public @NotNull ListIterator<E> flatMapFirstWhere(
          @NotNull final Predicate<? super E> predicate,
          @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
        if (atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft(),
            currentRight().flatMapFirstWhere(predicate, mapper));
      }

      @Override
      public @NotNull ListIterator<E> flatMapLastWhere(
          @NotNull final IndexedPredicate<? super E> predicate,
          @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
        if (atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft(),
            currentRight().flatMapLastWhere(offsetPredicate(nextIndex(), predicate),
                offsetFunction(nextIndex(), mapper)));
      }

      @Override
      public @NotNull ListIterator<E> flatMapLastWhere(
          @NotNull final Predicate<? super E> predicate,
          @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
        if (atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft(),
            currentRight().flatMapLastWhere(predicate, mapper));
      }

      @Override
      public @NotNull ListIterator<E> flatMapWhere(
          @NotNull final IndexedPredicate<? super E> predicate,
          @NotNull final IndexedFunction<? super E, ? extends Iterable<? extends E>> mapper) {
        if (atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft().flatMapWhere(predicate, mapper),
            currentRight().flatMapWhere(offsetPredicate(nextIndex(), predicate),
                offsetFunction(nextIndex(), mapper)));
      }

      @Override
      public @NotNull ListIterator<E> flatMapWhere(@NotNull final Predicate<? super E> predicate,
          @NotNull final Function<? super E, ? extends Iterable<? extends E>> mapper) {
        if (atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft().flatMapWhere(predicate, mapper),
            currentRight().flatMapWhere(predicate, mapper));
      }

      @Override
      public @NotNull <F> ListIterator<F> fold(final F identity,
          @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
        if (atEnd()) {
          return new ListIterator<F>(List.<F>of(), List.of(identity));
        }
        return new ListIterator<F>(List.<F>of(), currentRight().fold(identity, operation));
      }

      @Override
      public @NotNull <F> ListIterator<F> foldLeft(final F identity,
          @NotNull final BinaryFunction<? super F, ? super E, ? extends F> operation) {
        if (atEnd()) {
          return new ListIterator<F>(List.<F>of(), List.of(identity));
        }
        return new ListIterator<F>(List.<F>of(), currentRight().foldLeft(identity, operation));
      }

      @Override
      public @NotNull <F> ListIterator<F> foldRight(final F identity,
          @NotNull final BinaryFunction<? super E, ? super F, ? extends F> operation) {
        if (atEnd()) {
          return new ListIterator<F>(List.<F>of(), List.of(identity));
        }
        return new ListIterator<F>(List.<F>of(), currentRight().foldRight(identity, operation));
      }

      @Override
      @SuppressWarnings("unchecked")
      public @NotNull ListIterator<? extends ListIterator<E>> group(final int maxSize) {
        return new ListIterator<ListIterator<E>>(
            currentLeft().group(maxSize).map((Function<List<E>, ListIterator<E>>) LIST_TO_ITERATOR),
            currentRight().group(maxSize)
                .map((Function<List<E>, ListIterator<E>>) LIST_TO_ITERATOR));
      }

      @Override
      @SuppressWarnings("unchecked")
      public @NotNull ListIterator<? extends ListIterator<E>> group(final int size,
          final E padding) {
        return new ListIterator<ListIterator<E>>(currentLeft().group(size, padding)
            .map((Function<List<E>, ListIterator<E>>) LIST_TO_ITERATOR),
            currentRight().group(size, padding)
                .map((Function<List<E>, ListIterator<E>>) LIST_TO_ITERATOR));
      }

      @Override
      public boolean hasNext() {
        final int pos = this.pos;
        if (pos >= 0) {
          return right.materializer.canMaterializeElement(pos);
        }
        return true;
      }

      @Override
      public boolean hasPrevious() {
        final int pos = this.pos;
        if (pos == 0) {
          return !left.isEmpty();
        }
        if (pos < 0) {
          return left.size() + pos >= 0;
        }
        return true;
      }

      @Override
      public @NotNull ListIterator<Boolean> includes(final Object element) {
        if (atEnd()) {
          return FALSE_ITERATOR;
        }
        return new ListIterator<Boolean>(List.<Boolean>of(), currentRight().includes(element));
      }

      @Override
      public @NotNull ListIterator<Boolean> includesAll(@NotNull final Iterable<?> elements) {
        return new ListIterator<Boolean>(List.<Boolean>of(), currentRight().includesAll(elements));
      }

      @Override
      public @NotNull ListIterator<Boolean> includesSlice(@NotNull final Iterable<?> elements) {
        return new ListIterator<Boolean>(List.<Boolean>of(),
            currentRight().includesSlice(elements));
      }

      public @NotNull ListIterator<E> insert(final E element) {
        final int pos = this.pos;
        if (pos >= 0) {
          return new ListIterator<E>(left, right.insertAfter(pos, element), pos);
        }
        return new ListIterator<E>(left.insertAfter(nextIndex(), element), right, pos);
      }

      public @NotNull ListIterator<E> insertAll(@NotNull final Iterable<E> elements) {
        final int pos = this.pos;
        if (pos >= 0) {
          return new ListIterator<E>(left, right.insertAllAfter(pos, elements), pos);
        }
        return new ListIterator<E>(left.insertAllAfter(nextIndex(), elements), right, pos);
      }

      public @NotNull ListIterator<E> insertAllAfter(final int numElements,
          @NotNull final Iterable<? extends E> elements) {
        if (numElements < 0 || numElements == Integer.MAX_VALUE || atEnd()) {
          return this;
        }
        final int pos = this.pos;
        if (pos >= 0) {
          return new ListIterator<E>(left,
              right.insertAllAfter(SizeOverflowException.safeCast((long) numElements + pos),
                  elements));
        }
        return new ListIterator<E>(currentLeft(),
            currentRight().insertAllAfter(numElements, elements));
      }

      public @NotNull ListIterator<E> insertAfter(final int numElements, final E element) {
        if (numElements < 0 || numElements == Integer.MAX_VALUE || atEnd()) {
          return this;
        }
        final int pos = this.pos;
        if (pos >= 0) {
          return new ListIterator<E>(left,
              right.insertAfter(SizeOverflowException.safeCast((long) numElements + pos), element));
        }
        return new ListIterator<E>(currentLeft(), currentRight().insertAfter(numElements, element));
      }

      @Override
      public @NotNull ListIterator<E> intersect(@NotNull final Iterable<?> elements) {
        return new ListIterator<E>(currentLeft().intersect(elements),
            currentRight().intersect(elements));
      }

      @NotNull
      @Override
      public Iterator<E> iterator() {
        if (atEnd()) {
          return Iterator.of();
        }
        return currentRight().iterator();
      }

      @Override
      public boolean isEmpty() {
        return left.isEmpty() && right.isEmpty();
      }

      @Override
      public E last() {
        final List<E> right = this.right;
        return right.isEmpty() ? left.last() : right.last();
      }

      @Override
      public @NotNull <F> ListIterator<F> map(@NotNull final Function<? super E, F> mapper) {
        return new ListIterator<F>(currentLeft().map(mapper), currentRight().map(mapper));
      }

      @Override
      public @NotNull <F> ListIterator<F> map(@NotNull final IndexedFunction<? super E, F> mapper) {
        return new ListIterator<F>(currentLeft().map(mapper),
            currentRight().map(offsetFunction(nextIndex(), mapper)));
      }

      @Override
      public @NotNull ListIterator<E> mapAfter(final int numElements,
          @NotNull final Function<? super E, ? extends E> mapper) {
        if (numElements < 0 || numElements == Integer.MAX_VALUE || atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft(), currentRight().mapAfter(numElements, mapper));
      }

      @Override
      public @NotNull ListIterator<E> mapAfter(final int numElements,
          @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
        if (numElements < 0 || numElements == Integer.MAX_VALUE || atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft(),
            currentRight().mapAfter(numElements, offsetFunction(nextIndex(), mapper)));
      }

      @Override
      public @NotNull ListIterator<E> mapFirstWhere(
          @NotNull final IndexedPredicate<? super E> predicate,
          @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
        if (atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft(),
            currentRight().mapFirstWhere(offsetPredicate(nextIndex(), predicate),
                offsetFunction(nextIndex(), mapper)));
      }

      @Override
      public @NotNull ListIterator<E> mapFirstWhere(@NotNull final Predicate<? super E> predicate,
          @NotNull final Function<? super E, ? extends E> mapper) {
        if (atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft(), currentRight().mapFirstWhere(predicate, mapper));
      }

      @Override
      public @NotNull ListIterator<E> mapLastWhere(
          @NotNull final IndexedPredicate<? super E> predicate,
          @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
        if (atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft(),
            currentRight().mapLastWhere(offsetPredicate(nextIndex(), predicate),
                offsetFunction(nextIndex(), mapper)));
      }

      @Override
      public @NotNull ListIterator<E> mapLastWhere(@NotNull final Predicate<? super E> predicate,
          @NotNull final Function<? super E, ? extends E> mapper) {
        if (atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft(), currentRight().mapLastWhere(predicate, mapper));
      }

      @Override
      public @NotNull ListIterator<E> mapWhere(@NotNull final IndexedPredicate<? super E> predicate,
          @NotNull final IndexedFunction<? super E, ? extends E> mapper) {
        final List<E> left = this.left;
        final int offset = left.size();
        return new ListIterator<E>(left.mapWhere(predicate, mapper),
            right.mapWhere(offsetPredicate(offset, predicate), offsetFunction(offset, mapper)),
            pos);
      }

      @Override
      public @NotNull ListIterator<E> mapWhere(@NotNull final Predicate<? super E> predicate,
          @NotNull final Function<? super E, ? extends E> mapper) {
        return new ListIterator<E>(left.mapWhere(predicate, mapper),
            right.mapWhere(predicate, mapper), pos);
      }

      @Override
      public @NotNull ListIterator<E> max(@NotNull final Comparator<? super E> comparator) {
        if (atEnd()) {
          return ListIterator.of();
        }
        return new ListIterator<E>(List.<E>of(), currentRight().max(comparator));
      }

      @Override
      public @NotNull ListIterator<E> min(@NotNull final Comparator<? super E> comparator) {
        if (atEnd()) {
          return ListIterator.of();
        }
        return new ListIterator<E>(List.<E>of(), currentRight().min(comparator));
      }

      public @NotNull ListIterator<E> moveOf(final int maxElements) {
        if (maxElements == 0) {
          return this;
        }
        final long pos = this.pos;
        final long newPos = pos + maxElements;
        if (newPos >= Integer.MAX_VALUE || newPos <= Integer.MIN_VALUE) {
          throw new IndexOverflowException(newPos);
        }
        if ((newPos >= 0 && newPos < pos) || (newPos <= 0 && newPos > pos)) {
          return new ListIterator<E>(left, right, (int) newPos);
        }
        if (newPos >= 0) {
          final int knownSize = right.materializer.knownSize();
          if (newPos <= knownSize) {
            return new ListIterator<E>(left, right, (int) newPos);
          }
        } else {
          final int knownSize = left.materializer.knownSize();
          if (-newPos <= knownSize) {
            return new ListIterator<E>(left, right, (int) newPos);
          }
        }
        final List<E> newLeft;
        if (newPos == 0) {
          newLeft = left;
        } else if (newPos > 0) {
          newLeft = left.appendAll(right.take((int) newPos));
        } else {
          final int knownSize = left.materializer.knownSize();
          if (knownSize >= 0) {
            newLeft = left.take((int) (knownSize + newPos));
          } else {
            newLeft = left.dropRight((int) -newPos);
          }
        }
        final List<E> newRight;
        if (newPos == 0) {
          newRight = right;
        } else if (newPos > 0) {
          newRight = right.drop((int) newPos);
        } else {
          final int knownSize = left.materializer.knownSize();
          if (knownSize >= 0) {
            newRight = left.drop((int) (knownSize + newPos)).appendAll(right);
          } else {
            newRight = left.takeRight((int) -newPos).appendAll(right);
          }
        }
        return new ListIterator<E>(newLeft, newRight);
      }

      public @NotNull ListIterator<E> moveTo(final int index) {
        Require.notNegative(index, "index");
        int knownSize = left.materializer.knownSize();
        if (knownSize >= 0) {
          final int newPos = index - knownSize;
          if (newPos < 0) {
            return new ListIterator<E>(left, right, newPos);
          }
          knownSize = right.materializer.knownSize();
          if (knownSize >= 0 && newPos <= knownSize) {
            return new ListIterator<E>(left, right, newPos);
          }
        }
        return new ListIterator<E>(left.count().flatMap(new Function<Integer, List<E>>() {
          @Override
          public List<E> apply(final Integer size) {
            final int newPos = index - size;
            if (newPos == 0) {
              return left;
            } else if (newPos > 0) {
              return left.appendAll(right.take(newPos));
            } else {
              return left.take(size + newPos);
            }
          }
        }), left.count().flatMap(new Function<Integer, List<E>>() {
          @Override
          public List<E> apply(final Integer size) {
            final int newPos = index - size;
            if (newPos == 0) {
              return right;
            } else if (newPos > 0) {
              return right.drop(newPos);
            } else {
              return left.drop(size + newPos).appendAll(right);
            }
          }
        }));
      }

      @Override
      public E next() {
        try {
          if (pos >= 0) {
            return right.get(pos++);
          }
          final List<E> left = this.left;
          return left.get(left.size() + pos++);
        } catch (final IndexOutOfBoundsException ignored) {
          // FIXME: where the exception come from?
          throw new NoSuchElementException();
        }
      }

      @Override
      public int nextIndex() {
        return IndexOverflowException.safeCast((long) left.size() + pos);
      }

      @Override
      public @NotNull ListIterator<Boolean> notAll(
          @NotNull final IndexedPredicate<? super E> predicate) {
        if (atEnd()) {
          return FALSE_ITERATOR;
        }
        return new ListIterator<Boolean>(List.<Boolean>of(),
            currentRight().notAll(offsetPredicate(nextIndex(), predicate)));
      }

      @Override
      public @NotNull ListIterator<Boolean> notAll(@NotNull final Predicate<? super E> predicate) {
        if (atEnd()) {
          return FALSE_ITERATOR;
        }
        return new ListIterator<Boolean>(List.<Boolean>of(), currentRight().notAll(predicate));
      }

      @Override
      public boolean notEmpty() {
        return !left.isEmpty() || !right.isEmpty();
      }

      @Override
      public @NotNull ListIterator<Boolean> notExists(
          @NotNull final IndexedPredicate<? super E> predicate) {
        if (atEnd()) {
          return TRUE_ITERATOR;
        }
        return new ListIterator<Boolean>(List.<Boolean>of(),
            currentRight().notExists(offsetPredicate(nextIndex(), predicate)));
      }

      @Override
      public @NotNull ListIterator<Boolean> notExists(
          @NotNull final Predicate<? super E> predicate) {
        if (atEnd()) {
          return TRUE_ITERATOR;
        }
        return new ListIterator<Boolean>(List.<Boolean>of(), currentRight().notExists(predicate));
      }

      @Override
      public @NotNull ListIterator<E> orElse(@NotNull final Iterable<E> elements) {
        if (atEnd()) {
          return new ListIterator<E>(List.<E>of(), List.wrap(elements));
        }
        return new ListIterator<E>(List.<E>of(), currentRight().orElse(elements));
      }

      @Override
      public @NotNull ListIterator<E> orElseGet(
          @NotNull final Supplier<? extends Iterable<? extends E>> supplier) {
        return new ListIterator<E>(List.<E>of(), currentRight().orElseGet(supplier));
      }

      @Override
      public @NotNull ListIterator<E> plus(final E element) {
        return new ListIterator<E>(left, right.plus(element), pos);
      }

      @Override
      public @NotNull ListIterator<E> plusAll(@NotNull final Iterable<E> elements) {
        return new ListIterator<E>(left, right.plusAll(elements), pos);
      }

      @Override
      public @NotNull ListIterator<E> reduce(
          @NotNull final BinaryFunction<? super E, ? super E, ? extends E> operation) {
        if (atEnd()) {
          return ListIterator.of();
        }
        return new ListIterator<E>(List.<E>of(), currentRight().reduce(operation));
      }

      @Override
      public @NotNull ListIterator<E> reduceLeft(
          @NotNull final BinaryFunction<? super E, ? super E, ? extends E> operation) {
        if (atEnd()) {
          return ListIterator.of();
        }
        return new ListIterator<E>(List.<E>of(), currentRight().reduceLeft(operation));
      }

      @Override
      public @NotNull ListIterator<E> reduceRight(
          @NotNull final BinaryFunction<? super E, ? super E, ? extends E> operation) {
        if (atEnd()) {
          return ListIterator.of();
        }
        return new ListIterator<E>(List.<E>of(), currentRight().reduceRight(operation));
      }

      @Override
      public E previous() {
        try {
          if (pos > 0) {
            return right.get(--pos);
          }
          final List<E> left = this.left;
          return left.get(left.size() + --pos);
        } catch (final IndexOutOfBoundsException ignored) {
          // FIXME: where the exception come from?
          throw new NoSuchElementException();
        }
      }

      @Override
      public int previousIndex() {
        return nextIndex() - 1;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }

      @Override
      public @NotNull ListIterator<E> removeAfter(final int numElements) {
        if (numElements < 0 || numElements == Integer.MAX_VALUE || atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft(), currentRight().removeAfter(numElements));
      }

      @Override
      public @NotNull ListIterator<E> removeEach(final E element) {
        if (atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft().removeEach(element),
            currentRight().removeEach(element));
      }

      @Override
      public @NotNull ListIterator<E> removeFirst(final E element) {
        if (atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft(), currentRight().removeFirst(element));
      }

      @Override
      public @NotNull ListIterator<E> removeFirstWhere(
          @NotNull final IndexedPredicate<? super E> predicate) {
        if (atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft(),
            currentRight().removeFirstWhere(offsetPredicate(nextIndex(), predicate)));
      }

      @Override
      public @NotNull ListIterator<E> removeFirstWhere(
          @NotNull final Predicate<? super E> predicate) {
        if (atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft(), currentRight().removeFirstWhere(predicate));
      }

      @Override
      public @NotNull ListIterator<E> removeLast(final E element) {
        if (atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft(), currentRight().removeLast(element));
      }

      @Override
      public @NotNull ListIterator<E> removeLastWhere(
          @NotNull final IndexedPredicate<? super E> predicate) {
        if (atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft(),
            currentRight().removeLastWhere(offsetPredicate(nextIndex(), predicate)));
      }

      @Override
      public @NotNull ListIterator<E> removeLastWhere(
          @NotNull final Predicate<? super E> predicate) {
        if (atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft(), currentRight().removeLastWhere(predicate));
      }

      @Override
      public @NotNull ListIterator<E> removeSlice(final int start, final int end) {
        if (atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft(), currentRight().removeSlice(start, end));
      }

      @Override
      public @NotNull ListIterator<E> removeWhere(
          @NotNull final IndexedPredicate<? super E> predicate) {
        if (atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft(),
            currentRight().removeWhere(offsetPredicate(nextIndex(), predicate)));
      }

      @Override
      public @NotNull ListIterator<E> removeWhere(@NotNull final Predicate<? super E> predicate) {
        if (atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft(), currentRight().removeWhere(predicate));
      }

      @Override
      public @NotNull ListIterator<E> replaceAfter(final int numElements, final E replacement) {
        if (numElements < 0 || numElements == Integer.MAX_VALUE || atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft(),
            currentRight().replaceAfter(numElements, replacement));
      }

      @Override
      public @NotNull ListIterator<E> replaceEach(final E element, final E replacement) {
        if (atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft().replaceEach(element, replacement),
            currentRight().replaceEach(element, replacement));
      }

      @Override
      public @NotNull ListIterator<E> replaceFirst(final E element, final E replacement) {
        if (atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft(),
            currentRight().replaceFirst(element, replacement));
      }

      @Override
      public @NotNull ListIterator<E> replaceFirstWhere(
          @NotNull final IndexedPredicate<? super E> predicate, final E replacement) {
        if (atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft(),
            currentRight().replaceFirstWhere(offsetPredicate(nextIndex(), predicate), replacement));
      }

      @Override
      public @NotNull ListIterator<E> replaceFirstWhere(
          @NotNull final Predicate<? super E> predicate, final E replacement) {
        if (atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft(),
            currentRight().replaceFirstWhere(predicate, replacement));
      }

      @Override
      public @NotNull ListIterator<E> replaceLast(final E element, final E replacement) {
        if (atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft(), currentRight().replaceLast(element, replacement));
      }

      @Override
      public @NotNull ListIterator<E> replaceLastWhere(
          @NotNull final IndexedPredicate<? super E> predicate, final E replacement) {
        if (atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft(),
            currentRight().replaceLastWhere(offsetPredicate(nextIndex(), predicate), replacement));
      }

      @Override
      public @NotNull ListIterator<E> replaceLastWhere(
          @NotNull final Predicate<? super E> predicate, final E replacement) {
        if (atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft(),
            currentRight().replaceLastWhere(predicate, replacement));
      }

      @Override
      public @NotNull ListIterator<E> replaceSlice(final int start, final int end,
          @NotNull final Iterable<? extends E> patch) {
        if (atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft(), currentRight().replaceSlice(start, end, patch));
      }

      @Override
      public @NotNull ListIterator<E> replaceWhere(
          @NotNull final IndexedPredicate<? super E> predicate, final E replacement) {
        if (atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft().replaceWhere(predicate, replacement),
            currentRight().replaceWhere(offsetPredicate(nextIndex(), predicate), replacement));
      }

      @Override
      public @NotNull ListIterator<E> replaceWhere(@NotNull final Predicate<? super E> predicate,
          final E replacement) {
        if (atEnd()) {
          return this;
        }
        return new ListIterator<E>(currentLeft().replaceWhere(predicate, replacement),
            currentRight().replaceWhere(predicate, replacement));
      }

      @Override
      public @NotNull ListIterator<E> resizeTo(final int numElements, final E padding) {
        if (atEnd()) {
          return new ListIterator<E>(left.appendAll(right), List.times(numElements, padding));
        }
        return new ListIterator<E>(currentLeft(), currentRight().resizeTo(numElements, padding));
      }

      public @NotNull ListIterator<E> reverse() {
        if (atEnd()) {
          return new ListIterator<E>(left.appendAll(right).reverse(), List.<E>of());
        }
        return new ListIterator<E>(currentRight().reverse(), currentLeft().reverse());
      }

      @Override
      public void set(final E e) {
        throw new UnsupportedOperationException();
      }

      @Override
      public int size() {
        return left.size() + right.size();
      }

      @Override
      public @NotNull ListIterator<E> slice(final int start, final int end) {
        if (atEnd()) {
          return ListIterator.of();
        }
        return new ListIterator<E>(List.<E>of(), currentRight().slice(start, end));
      }

      @Override
      public @NotNull ListIterator<Boolean> startsWith(@NotNull final Iterable<?> elements) {
        return new ListIterator<Boolean>(List.<Boolean>of(), currentRight().startsWith(elements));
      }

      @Override
      public @NotNull ListIterator<E> take(final int maxElements) {
        if (atEnd()) {
          return ListIterator.of();
        }
        return new ListIterator<E>(currentLeft(), currentRight().take(maxElements));
      }

      @Override
      public @NotNull ListIterator<E> takeRight(final int maxElements) {
        if (atEnd()) {
          return ListIterator.of();
        }
        return new ListIterator<E>(List.<E>of(), currentRight().takeRight(maxElements));
      }

      @Override
      public @NotNull ListIterator<E> takeRightWhile(
          @NotNull final IndexedPredicate<? super E> predicate) {
        if (atEnd()) {
          return ListIterator.of();
        }
        return new ListIterator<E>(List.<E>of(),
            currentRight().takeRightWhile(offsetPredicate(nextIndex(), predicate)));
      }

      @Override
      public @NotNull ListIterator<E> takeRightWhile(
          @NotNull final Predicate<? super E> predicate) {
        if (atEnd()) {
          return ListIterator.of();
        }
        return new ListIterator<E>(List.<E>of(), currentRight().takeRightWhile(predicate));
      }

      @Override
      public @NotNull ListIterator<E> takeWhile(
          @NotNull final IndexedPredicate<? super E> predicate) {
        if (atEnd()) {
          return ListIterator.of();
        }
        return new ListIterator<E>(currentLeft(),
            currentRight().takeWhile(offsetPredicate(nextIndex(), predicate)));
      }

      @Override
      public @NotNull ListIterator<E> takeWhile(@NotNull final Predicate<? super E> predicate) {
        if (atEnd()) {
          return ListIterator.of();
        }
        return new ListIterator<E>(currentLeft(), currentRight().takeWhile(predicate));
      }

      @Override
      public @NotNull ListIterator<E> union(@NotNull final Iterable<? extends E> elements) {
        return new ListIterator<E>(List.<E>of(), left.appendAll(right).union(elements),
            nextIndex());
      }

      private boolean atEnd() {
        return pos >= 0 && pos == right.materializer.knownSize();
      }

      private @NotNull List<E> currentLeft() {
        final int pos = this.pos;
        if (pos == 0) {
          return left;
        } else if (pos > 0) {
          return left.appendAll(right.take(pos));
        }
        return left.dropRight(-pos);
      }

      private @NotNull List<E> currentRight() {
        final int pos = this.pos;
        if (pos == 0) {
          return right;
        } else if (pos > 0) {
          return right.drop(pos);
        }
        return left.takeRight(-pos).appendAll(right);
      }
    }
  }
}
